#!/usr/bin/env python3
"""Track MCQ coverage and drill the questions you keep getting wrong.

State lives in .mcq-progress.json at the repo root -- plain JSON, safe to read,
diff, or delete. Standard library only.

USAGE
    tools/mcq_track.py status              # coverage + accuracy + weak areas
    tools/mcq_track.py drill               # interactive, auto-graded (start here)
    tools/mcq_track.py drill --deck 13     # only that file
    tools/mcq_track.py drill --new         # only questions never attempted
    tools/mcq_track.py drill --weak -n 15  # 15 worst-performing questions
    tools/mcq_track.py due                 # what's scheduled for review
    tools/mcq_track.py log 13:Q4 wrong     # record a result by hand
    tools/mcq_track.py export progress.md  # markdown report
    tools/mcq_track.py reset --deck 13     # clear progress (asks first)

HOW SCHEDULING WORKS
    A five-box Leitner system. Right answers move a question up a box and push its
    review further out (0d / 1d / 3d / 7d / 21d); a wrong answer drops it straight
    back to box 1. A correct-but-guessed answer counts as WRONG for scheduling --
    a lucky guess is not knowledge, and treating it as one is how you arrive at an
    interview with false confidence.
"""

from __future__ import annotations

import argparse
import json
import random
import re
import shutil
import sys
import textwrap
from datetime import datetime, timedelta, timezone
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from mcq_lib import LETTERS, Question, default_root, load_decks, permute  # noqa: E402

BOX_DAYS = {1: 0, 2: 1, 3: 3, 4: 7, 5: 21}
MAX_BOX = 5
STATE_NAME = ".mcq-progress.json"

C = {
    "ok": "\033[32m", "bad": "\033[31m", "warn": "\033[33m",
    "dim": "\033[2m", "bold": "\033[1m", "off": "\033[0m",
}
if not sys.stdout.isatty():
    C = {k: "" for k in C}


def _now() -> datetime:
    return datetime.now(timezone.utc)


def _iso(dt: datetime) -> str:
    return dt.replace(microsecond=0).isoformat()


def _wrap(text: str, indent: str = "  ", width: int | None = None) -> str:
    w = width or min(shutil.get_terminal_size((100, 24)).columns - 2, 100)
    out = []
    for para in text.split("\n"):
        if not para.strip():
            out.append("")
            continue
        out.append(textwrap.fill(para, width=w, initial_indent=indent,
                                 subsequent_indent=indent, break_long_words=False,
                                 break_on_hyphens=False))
    return "\n".join(out)


def _plain(md: str) -> str:
    """Strip the markdown that adds noise in a terminal.

    DOTALL matters: emphasis in these files frequently spans a line break, and
    without it the `**` markers leak into the output.
    """
    md = re.sub(r"<details>|</details>|<summary>Answer</summary>", "", md)
    md = re.sub(r"\*\*(.+?)\*\*", r"\1", md, flags=re.S)
    md = re.sub(r"(?<![`*])\*(?!\*)(.+?)(?<![`*])\*(?!\*)", r"\1", md, flags=re.S)
    return md.strip()


class Store:
    """The progress file."""

    def __init__(self, path: Path):
        self.path = path
        self.data: dict = {"version": 1, "questions": {}}
        if path.exists():
            try:
                loaded = json.loads(path.read_text(encoding="utf-8"))
                if isinstance(loaded.get("questions"), dict):
                    self.data = loaded
                else:
                    print(f"{C['warn']}warning: {path.name} has no 'questions' map; starting fresh{C['off']}")
            except json.JSONDecodeError as e:
                backup = path.with_suffix(".corrupt.json")
                path.replace(backup)
                print(f"{C['warn']}warning: {path.name} was unreadable ({e}); moved to "
                      f"{backup.name} and starting fresh{C['off']}")

    def save(self) -> None:
        tmp = self.path.with_suffix(".tmp")
        tmp.write_text(json.dumps(self.data, indent=2, sort_keys=True) + "\n", encoding="utf-8")
        tmp.replace(self.path)

    def rec(self, qid: str) -> dict:
        return self.data["questions"].setdefault(
            qid, {"box": 1, "right": 0, "wrong": 0, "guessed": 0, "attempts": []}
        )

    def record(self, qid: str, outcome: str) -> dict:
        r = self.rec(qid)
        if outcome == "right":
            r["right"] += 1
            r["box"] = min(MAX_BOX, r["box"] + 1)
        else:
            r["wrong"] += 1
            if outcome == "guessed":
                r["guessed"] += 1
            r["box"] = 1
        r["last"] = _iso(_now())
        r["due"] = _iso(_now() + timedelta(days=BOX_DAYS[r["box"]]))
        r["attempts"].append({"at": r["last"], "outcome": outcome})
        r["attempts"] = r["attempts"][-30:]
        return r

    def is_due(self, qid: str) -> bool:
        r = self.data["questions"].get(qid)
        if not r:
            return True
        due = r.get("due")
        return True if not due else datetime.fromisoformat(due) <= _now()

    def attempted(self, qid: str) -> bool:
        r = self.data["questions"].get(qid)
        return bool(r and (r["right"] or r["wrong"]))

    def accuracy(self, qid: str) -> float | None:
        r = self.data["questions"].get(qid)
        if not r:
            return None
        n = r["right"] + r["wrong"]
        return None if n == 0 else r["right"] / n


# ---------------------------------------------------------------- selection

def pick(decks, store: Store, mode: str, deck_filter: list[str], limit: int, rng) -> list[Question]:
    qs = []
    for d in decks:
        num = d.slug.split("-")[0]
        if deck_filter and not (d.slug in deck_filter or num in [f.zfill(2) for f in deck_filter]):
            continue
        qs.extend(d.questions)
    if not qs:
        raise SystemExit("no questions matched -- check --deck")

    if mode == "new":
        sel = [q for q in qs if not store.attempted(q.qid)]
    elif mode == "weak":
        scored = [(store.accuracy(q.qid), q) for q in qs if store.attempted(q.qid)]
        scored.sort(key=lambda t: (t[0] if t[0] is not None else 1.0,
                                  -store.rec(t[1].qid)["wrong"]))
        sel = [q for _, q in scored]
    elif mode == "due":
        sel = [q for q in qs if store.is_due(q.qid) and store.attempted(q.qid)]
    else:  # mixed: due first, then new, then the rest
        due = [q for q in qs if store.attempted(q.qid) and store.is_due(q.qid)]
        new = [q for q in qs if not store.attempted(q.qid)]
        rest = [q for q in qs if q not in due and q not in new]
        rng.shuffle(due); rng.shuffle(new); rng.shuffle(rest)
        sel = due + new + rest

    if mode in ("new", "due"):
        rng.shuffle(sel)
    return sel[:limit] if limit else sel


# ---------------------------------------------------------------- commands

def cmd_drill(args, decks, store: Store) -> int:
    rng = random.Random(args.seed)
    queue = pick(decks, store, args.mode, args.deck, args.number, rng)
    if not queue:
        print(f"{C['ok']}nothing to drill in that selection — try --mode mixed or another --deck{C['off']}")
        return 0

    print(f"\n{C['bold']}{len(queue)} question(s){C['off']}  "
          f"{C['dim']}a/b/c/d to answer · ? reveal · s skip · q quit{C['off']}\n")
    right = wrong = skipped = 0
    try:
        for i, q in enumerate(queue, 1):
            targets = list(LETTERS); rng.shuffle(targets)
            sq = permute(q, dict(zip(LETTERS, targets)))
            r = store.data["questions"].get(q.qid)
            hist = ""
            if r and (r["right"] or r["wrong"]):
                hist = f" · box {r['box']} · {r['right']}✓/{r['wrong']}✗"
            print(f"{C['dim']}[{i}/{len(queue)}] {q.qid}{hist}{C['off']}")
            print(f"\n{C['bold']}{_wrap(_plain(sq.stem), indent='')}{C['off']}\n")
            for L in LETTERS:
                print(_wrap(f"{L}. {_plain(sq.options[L])}", indent="   ")[3:].rjust(0))
            print()

            while True:
                try:
                    ans = input("  > ").strip().lower()
                except EOFError:
                    print(); raise KeyboardInterrupt
                if ans in ("q", "quit"):
                    raise KeyboardInterrupt
                if ans in ("s", "skip"):
                    skipped += 1; print(f"  {C['dim']}skipped{C['off']}\n"); break
                if ans in ("?", "r", "reveal"):
                    print(f"\n{_wrap(_plain(sq.body))}\n")
                    store.record(q.qid, "wrong"); store.save()
                    wrong += 1
                    print(f"  {C['warn']}recorded as not-known{C['off']}\n"); break
                if ans in ("a", "b", "c", "d"):
                    chosen = ans.upper()
                    ok = chosen == sq.answer
                    if ok:
                        g = input(f"  {C['ok']}✓ correct{C['off']} — did you guess? [y/N] ").strip().lower()
                        outcome = "guessed" if g.startswith("y") else "right"
                        if outcome == "right":
                            right += 1
                        else:
                            wrong += 1
                            print(f"  {C['warn']}counted as wrong — a lucky guess isn't knowledge{C['off']}")
                    else:
                        print(f"  {C['bad']}✗ wrong{C['off']} — the answer is "
                              f"{C['bold']}{sq.answer}{C['off']}")
                        outcome = "wrong"; wrong += 1
                    rec = store.record(q.qid, outcome); store.save()
                    print(f"\n{_wrap(_plain(sq.body))}\n")
                    print(f"  {C['dim']}box {rec['box']} · next due "
                          f"{rec['due'][:10]}{C['off']}\n")
                    break
                print(f"  {C['dim']}a/b/c/d, ? to reveal, s to skip, q to quit{C['off']}")
            if i < len(queue):
                print("─" * min(shutil.get_terminal_size((100, 24)).columns, 100) + "\n")
    except KeyboardInterrupt:
        print(f"\n{C['dim']}stopped early — progress saved{C['off']}")

    done = right + wrong
    print(f"\n{C['bold']}session:{C['off']} {right} right, {wrong} wrong"
          + (f", {skipped} skipped" if skipped else "")
          + (f"  ({100*right/done:.0f}%)" if done else ""))
    print(f"{C['dim']}tools/mcq_track.py status{C['off']}")
    return 0


def cmd_status(args, decks, store: Store) -> int:
    width = min(shutil.get_terminal_size((100, 24)).columns, 100)
    print(f"\n{C['bold']}MCQ coverage{C['off']}\n")
    print(f"  {'deck':<34}{'seen':>10}{'acc':>7}{'due':>6}{'weak':>6}")
    print("  " + "─" * (width - 4))
    t_total = t_seen = t_right = t_wrong = t_due = 0
    rows = []
    for d in decks:
        qs = d.questions
        seen = [q for q in qs if store.attempted(q.qid)]
        rt = sum(store.rec(q.qid)["right"] for q in seen)
        wr = sum(store.rec(q.qid)["wrong"] for q in seen)
        due = sum(1 for q in qs if store.attempted(q.qid) and store.is_due(q.qid))
        weak = sum(1 for q in seen if (store.accuracy(q.qid) or 0) < 0.5)
        acc = rt / (rt + wr) if (rt + wr) else None
        t_total += len(qs); t_seen += len(seen); t_right += rt; t_wrong += wr; t_due += due
        rows.append((d.slug, len(seen), len(qs), acc, due, weak))

    for slug, seen, total, acc, due, weak in rows:
        pct = seen / total if total else 0
        colour = C["ok"] if pct == 1 else (C["warn"] if pct else C["dim"])
        acc_s = "—" if acc is None else f"{100*acc:.0f}%"
        acc_c = "" if acc is None else (C["ok"] if acc >= 0.8 else C["bad"] if acc < 0.5 else C["warn"])
        print(f"  {slug:<34}{colour}{seen:>5}/{total:<4}{C['off']}"
              f"{acc_c}{acc_s:>7}{C['off']}{due:>6}{C['bad'] if weak else ''}{weak:>6}{C['off']}")

    print("  " + "─" * (width - 4))
    acc = t_right / (t_right + t_wrong) if (t_right + t_wrong) else 0
    print(f"  {C['bold']}{'TOTAL':<34}{t_seen:>5}/{t_total:<4}"
          f"{100*acc:>6.0f}%{t_due:>6}{C['off']}")
    print(f"\n  coverage {100*t_seen/t_total:.0f}%  ·  {t_right + t_wrong} attempts")

    weakest = sorted(
        ((store.accuracy(q.qid), store.rec(q.qid)["wrong"], q)
         for d in decks for q in d.questions if store.attempted(q.qid)),
        key=lambda t: (t[0], -t[1]),
    )[:8]
    if weakest:
        print(f"\n{C['bold']}weakest{C['off']}")
        for a, wr, q in weakest:
            print(f"  {C['bad']}{100*a:>3.0f}%{C['off']} {q.qid:<38} "
                  f"{C['dim']}{_plain(q.stem)[:44]}{C['off']}")

    if t_seen < t_total:
        nxt = next((d.slug for d, in [(d,) for d in decks]
                    if any(not store.attempted(q.qid) for q in d.questions)), None)
        print(f"\n  next: {C['bold']}tools/mcq_track.py drill --new --deck "
              f"{nxt.split('-')[0]}{C['off']}")
    elif t_due:
        print(f"\n  next: {C['bold']}tools/mcq_track.py drill --due{C['off']}  ({t_due} due)")
    else:
        print(f"\n  {C['ok']}all covered and nothing due — "
              f"now say the answers out loud (cheatsheets/answer-scripts.md){C['off']}")
    print()
    return 0


def cmd_due(args, decks, store: Store) -> int:
    items = [(store.rec(q.qid), q) for d in decks for q in d.questions
             if store.attempted(q.qid) and store.is_due(q.qid)]
    if not items:
        print(f"{C['ok']}nothing due{C['off']}")
        return 0
    items.sort(key=lambda t: t[0].get("due", ""))
    print(f"\n{len(items)} due:\n")
    for r, q in items:
        print(f"  box {r['box']}  {q.qid:<40} {C['dim']}{_plain(q.stem)[:40]}{C['off']}")
    print(f"\n  tools/mcq_track.py drill --due\n")
    return 0


def resolve_qid(spec: str, ids: dict) -> str:
    """Resolve a loose question spec to a full qid.

    Accepts the full id, or a `deck:question` shorthand where either half may be
    partial: '13:Q4', '13:4', 'event-sourcing:Q4', 'gc:9'.
    """
    if spec in ids:
        return spec
    deck_part, _, q_part = spec.rpartition(":")
    q_part = q_part.upper().lstrip("Q")
    cands = []
    for k in ids:
        kd, _, kq = k.rpartition(":")
        if q_part and kq.upper().lstrip("Q") != q_part:
            continue
        if deck_part and deck_part.lower() not in kd.lower():
            continue
        cands.append(k)
    if len(cands) == 1:
        return cands[0]
    if cands:
        raise SystemExit("ambiguous — did you mean:\n  " + "\n  ".join(sorted(cands)[:10]))
    raise SystemExit(f"unknown question: {spec!r}\n"
                     f"try a form like 13:Q4, 13:4, or the full "
                     f"13-event-sourcing-and-cqrs:Q4")


def cmd_log(args, decks, store: Store) -> int:
    ids = {q.qid: q for d in decks for q in d.questions}
    qid = resolve_qid(args.qid, ids)
    r = store.record(qid, args.outcome); store.save()
    print(f"  {qid}: {args.outcome} → box {r['box']}, due {r['due'][:10]}")
    return 0


def cmd_export(args, decks, store: Store) -> int:
    lines = [f"# MCQ progress — {_iso(_now())[:16].replace('T', ' ')} UTC", ""]
    lines += ["| deck | seen | total | accuracy | due | weak |", "|---|---|---|---|---|---|"]
    for d in decks:
        seen = [q for q in d.questions if store.attempted(q.qid)]
        rt = sum(store.rec(q.qid)["right"] for q in seen)
        wr = sum(store.rec(q.qid)["wrong"] for q in seen)
        acc = f"{100*rt/(rt+wr):.0f}%" if (rt + wr) else "—"
        due = sum(1 for q in d.questions if store.attempted(q.qid) and store.is_due(q.qid))
        weak = sum(1 for q in seen if (store.accuracy(q.qid) or 0) < 0.5)
        lines.append(f"| `{d.slug}` | {len(seen)} | {len(d.questions)} | {acc} | {due} | {weak} |")
    weakest = sorted(((store.accuracy(q.qid), q) for d in decks for q in d.questions
                      if store.attempted(q.qid)), key=lambda t: t[0])[:15]
    if weakest:
        lines += ["", "## Weakest questions", ""]
        for a, q in weakest:
            lines.append(f"- **{100*a:.0f}%** `{q.qid}` — {_plain(q.stem)[:90]}")
    Path(args.out).write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"  wrote {args.out}")
    return 0


def cmd_reset(args, decks, store: Store) -> int:
    if args.deck:
        keep = {q.qid for d in decks for q in d.questions
                if not (d.slug in args.deck or d.slug.split("-")[0] in [f.zfill(2) for f in args.deck])}
        drop = [k for k in store.data["questions"] if k not in keep]
    else:
        drop = list(store.data["questions"])
    if not drop:
        print("  nothing to reset")
        return 0
    if input(f"  clear progress for {len(drop)} question(s)? [y/N] ").strip().lower() != "y":
        print("  cancelled")
        return 1
    for k in drop:
        store.data["questions"].pop(k, None)
    store.save()
    print(f"  cleared {len(drop)}")
    return 0


def main() -> int:
    ap = argparse.ArgumentParser(
        description="Track MCQ coverage and drill weak questions.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__.split("USAGE")[1] if "USAGE" in __doc__ else None,
    )
    ap.add_argument("--root", type=Path, help="path to mcq-quiz/")
    ap.add_argument("--state", type=Path, help=f"progress file (default: <root>/../{STATE_NAME})")
    sub = ap.add_subparsers(dest="cmd")

    d = sub.add_parser("drill", help="interactive, auto-graded practice")
    d.add_argument("--deck", action="append", default=[], metavar="N", help="file number or slug (repeatable)")
    d.add_argument("-n", "--number", type=int, default=10, help="how many (0 = all)")
    d.add_argument("--seed", type=int)
    g = d.add_mutually_exclusive_group()
    g.add_argument("--new", dest="mode", action="store_const", const="new", help="never attempted")
    g.add_argument("--weak", dest="mode", action="store_const", const="weak", help="worst first")
    g.add_argument("--due", dest="mode", action="store_const", const="due", help="scheduled for review")
    d.set_defaults(mode="mixed", func=cmd_drill)

    s = sub.add_parser("status", help="coverage summary"); s.set_defaults(func=cmd_status)
    u = sub.add_parser("due", help="list what's due"); u.set_defaults(func=cmd_due)

    l = sub.add_parser("log", help="record one result by hand")
    l.add_argument("qid", help="e.g. 13:Q4 (substring match is fine)")
    l.add_argument("outcome", choices=["right", "wrong", "guessed"])
    l.set_defaults(func=cmd_log)

    e = sub.add_parser("export", help="write a markdown report")
    e.add_argument("out", nargs="?", default="mcq-progress.md")
    e.set_defaults(func=cmd_export)

    r = sub.add_parser("reset", help="clear progress")
    r.add_argument("--deck", action="append", default=[], metavar="N")
    r.set_defaults(func=cmd_reset)

    args = ap.parse_args()
    if not args.cmd:
        args = ap.parse_args(["status"])

    root = args.root or default_root()
    try:
        decks = load_decks(root)
    except ValueError as e:
        raise SystemExit(f"parse error: {e}")
    store = Store(args.state or root.parent / STATE_NAME)
    return args.func(args, decks, store)


if __name__ == "__main__":
    raise SystemExit(main())
