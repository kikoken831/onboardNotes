#!/usr/bin/env python3
"""Generate freshly-randomised MCQ practice sheets.

Shuffles question order AND the A/B/C/D choices within each question, remapping
the answer letter and every letter reference in the explanation. Run it again for
a different shuffle -- so the same 210 questions stay useful after you've started
remembering that "the answer to Q7 is C".

The canonical mcq-quiz/ files are never modified; output goes to a separate
directory (default: practice/).

USAGE
    tools/mcq_shuffle.py                      # shuffle everything -> practice/
    tools/mcq_shuffle.py 13 15                # only those files (by number or slug)
    tools/mcq_shuffle.py --split              # separate question sheet + answer key
    tools/mcq_shuffle.py --pick 20            # a 20-question mixed exam from all files
    tools/mcq_shuffle.py --seed 42            # reproducible
    tools/mcq_shuffle.py --choices-only       # keep question order, shuffle choices
    tools/mcq_shuffle.py --check              # parse + verify only, write nothing

WHY --split MATTERS
    Inline <details> blocks are easy to peek at by accident. --split writes the
    questions with no answers at all, plus a separate key -- which is the only
    format that actually forces you to commit before checking.
"""

from __future__ import annotations

import argparse
import random
import sys
from datetime import datetime, timezone
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from mcq_lib import (  # noqa: E402
    LETTERS, Deck, Question, default_root, load_decks, permute, validate_deck,
)


def _banner(seed: int, note: str = "") -> str:
    when = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M UTC")
    return (
        f"> **Generated practice sheet** — {when}, seed `{seed}`.\n"
        f"> Question order and A/B/C/D positions are randomised; the canonical\n"
        f"> version lives in `mcq-quiz/`. Regenerate with "
        f"`tools/mcq_shuffle.py --seed {seed}` to get this exact sheet again.\n"
        + (f">\n> {note}\n" if note else "")
    )


def shuffle_deck(deck: Deck, rng: random.Random, choices_only: bool) -> list[Question]:
    qs = list(deck.questions)
    if not choices_only:
        rng.shuffle(qs)
    out = []
    for q in qs:
        targets = list(LETTERS)
        rng.shuffle(targets)
        out.append(permute(q, dict(zip(LETTERS, targets))))
    return out


def write_inline(path: Path, title: str, banner: str, qs: list[Question]) -> None:
    parts = [f"# {title} — shuffled\n", banner, "\n---\n"]
    for i, q in enumerate(qs, 1):
        parts.append("\n" + q.render(number=i, with_answer=True) + "\n---\n")
    path.write_text("\n".join(parts).rstrip() + "\n", encoding="utf-8")


def write_split(qpath: Path, apath: Path, title: str, banner: str, qs: list[Question]) -> None:
    q_parts = [f"# {title} — shuffled (questions)\n", banner,
               f"\n**Answers:** `{apath.name}`. Write your letters down before opening it.\n", "\n---\n"]
    a_parts = [f"# {title} — shuffled (answer key)\n", banner, "\n---\n"]
    for i, q in enumerate(qs, 1):
        q_parts.append("\n" + q.render(number=i, with_answer=False) + "\n---\n")
        a_parts.append(f"\n### Q{i}. {q.stem}\n\n<!-- mcq-id: {q.qid} -->\n\n"
                       + q.body.rstrip() + "\n\n---\n")
    qpath.write_text("\n".join(q_parts).rstrip() + "\n", encoding="utf-8")
    apath.write_text("\n".join(a_parts).rstrip() + "\n", encoding="utf-8")


def select(decks: list[Deck], wanted: list[str]) -> list[Deck]:
    if not wanted:
        return decks
    out, misses = [], []
    for w in wanted:
        hit = [d for d in decks if d.slug == w or d.slug.startswith(f"{w.zfill(2)}-")]
        if hit:
            out.extend(hit)
        else:
            misses.append(w)
    if misses:
        avail = ", ".join(d.slug.split("-")[0] for d in decks)
        raise SystemExit(f"no such deck: {', '.join(misses)}\navailable: {avail}")
    return out


def main() -> int:
    ap = argparse.ArgumentParser(
        description="Generate randomised MCQ practice sheets.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__.split("USAGE")[1] if "USAGE" in __doc__ else None,
    )
    ap.add_argument("decks", nargs="*", help="file numbers or slugs (default: all)")
    ap.add_argument("--root", type=Path, help="path to mcq-quiz/")
    ap.add_argument("--out", type=Path, help="output dir (default: <root>/../practice)")
    ap.add_argument("--seed", type=int, help="RNG seed (default: random, printed)")
    ap.add_argument("--split", action="store_true",
                    help="write questions and answer key as separate files")
    ap.add_argument("--choices-only", action="store_true",
                    help="keep question order; shuffle only the A/B/C/D positions")
    ap.add_argument("--pick", type=int, metavar="N",
                    help="build one mixed N-question exam drawn from all selected decks")
    ap.add_argument("--check", action="store_true",
                    help="parse and validate only; write nothing")
    args = ap.parse_args()

    root = args.root or default_root()
    try:
        decks = select(load_decks(root), args.decks)
    except ValueError as e:
        return int(print(f"parse error: {e}") or 1)

    if args.check:
        total = sum(len(d.questions) for d in decks)
        problems: list[str] = []
        for d in decks:
            probs = validate_deck(d)
            problems += probs
            flag = "ok " if not probs else "BAD"
            print(f"  {flag} {d.slug:38s} {len(d.questions):3d} questions"
                  + (f"  {len(probs)} problem(s)" if probs else ""))
        if problems:
            print(f"\n{len(problems)} problem(s):")
            for p in problems:
                print(f"  - {p}")
            print("\nUnbolded option references are the important one: they would stop\n"
                  "being remapped when choices are shuffled, leaving an explanation that\n"
                  "dismisses the wrong options.")
            return 1
        print(f"\n{total} questions across {len(decks)} decks — all parsed and validated cleanly")
        return 0

    seed = args.seed if args.seed is not None else random.randrange(1, 1_000_000)
    rng = random.Random(seed)
    out_dir = args.out or (root.parent / "practice")
    out_dir.mkdir(parents=True, exist_ok=True)

    written: list[Path] = []
    if args.pick:
        pool = [q for d in decks for q in d.questions]
        if args.pick > len(pool):
            raise SystemExit(f"--pick {args.pick} exceeds the {len(pool)} available questions")
        rng.shuffle(pool)
        chosen = []
        for q in pool[: args.pick]:
            targets = list(LETTERS)
            rng.shuffle(targets)
            chosen.append(permute(q, dict(zip(LETTERS, targets))))
        title = f"Mixed exam ({args.pick} questions)"
        note = "Drawn from " + ", ".join(d.slug.split("-")[0] for d in decks) + "."
        banner = _banner(seed, note)
        if args.split:
            qp, apath = out_dir / "exam-questions.md", out_dir / "exam-answers.md"
            write_split(qp, apath, title, banner, chosen); written += [qp, apath]
        else:
            p = out_dir / "exam.md"
            write_inline(p, title, banner, chosen); written.append(p)
    else:
        banner = _banner(seed)
        for d in decks:
            qs = shuffle_deck(d, rng, args.choices_only)
            if args.split:
                qp = out_dir / f"{d.slug}-questions.md"
                apath = out_dir / f"{d.slug}-answers.md"
                write_split(qp, apath, d.title, banner, qs); written += [qp, apath]
            else:
                p = out_dir / f"{d.slug}.md"
                write_inline(p, d.title, banner, qs); written.append(p)

    print(f"seed {seed} — regenerate this exact sheet with --seed {seed}\n")
    for p in written:
        print(f"  {p.relative_to(Path.cwd()) if p.is_relative_to(Path.cwd()) else p}")
    print(f"\n{len(written)} file(s) in {out_dir}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
