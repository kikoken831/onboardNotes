"""Command-line interface for the offline judge."""

import argparse
import os
import random
import shutil
import subprocess
import sys
import time

from . import build, catalogue, pages, progress

# ------------------------------------------------------------------ formatting

_COLOUR = sys.stdout.isatty() and os.environ.get("NO_COLOR") is None


def _c(code, s):
    return "\033[%sm%s\033[0m" % (code, s) if _COLOUR else s


def green(s): return _c("32", s)
def red(s):   return _c("31", s)
def yellow(s): return _c("33", s)
def dim(s):   return _c("2", s)
def bold(s):  return _c("1", s)


DIFFICULTY_COLOUR = {"Easy": green, "Medium": yellow, "Hard": red}


def die(message, detail=None):
    print(red("error: ") + message, file=sys.stderr)
    if detail:
        print(detail, file=sys.stderr)
    sys.exit(1)


def resolve_or_die(query):
    problem, err = catalogue.resolve(query)
    if err:
        die(err)
    return problem


# ----------------------------------------------------------------------- list

def cmd_list(args):
    state = progress.load()
    rows = []
    for p in catalogue.all_problems():
        if args.tier is not None and p["tier"] != args.tier:
            continue
        if args.difficulty and p.get("difficulty", "").lower() != args.difficulty.lower():
            continue
        if args.pattern and not any(args.pattern.lower() in pat.lower()
                                    for pat in p.get("pattern", [])):
            continue
        if args.group and p.get("group", "") != args.group:
            continue
        if args.company and args.company not in catalogue.companies_of(p):
            continue

        e = state["problems"].get(p["slug"])
        solved = bool(e and e["solved"])
        implemented = catalogue.is_implemented(p)

        if args.todo and (solved or not implemented):
            continue
        if args.solved and not solved:
            continue
        if args.ready and not implemented:
            continue

        rows.append((p, e, solved, implemented))

    if not rows:
        print(dim("nothing matches those filters"))
        return

    for p, e, solved, implemented in rows:
        if solved:
            mark = green("✓")
        elif e and e["attempts"]:
            mark = yellow("·")
        elif implemented:
            mark = dim("○")
        else:
            mark = dim("　")

        colour = DIFFICULTY_COLOUR.get(p.get("difficulty", ""), dim)
        diff = colour(p.get("difficulty", "")[:6].ljust(6))
        tier = dim("T%d" % p["tier"])
        pats = dim(", ".join(p.get("pattern", [])[:3]))
        name = p["slug"] if implemented else dim(p["slug"])
        print("  %s %s %s  %-52s %s" % (mark, tier, diff, name, pats))

    done = sum(1 for _, _, s, _ in rows if s)
    ready = sum(1 for _, _, _, i in rows if i)
    print()
    print(dim("  %d shown · %d solved · %d ready to solve" % (len(rows), done, ready)))


# ----------------------------------------------------------------------- open

def cmd_open(args):
    problem = resolve_or_die(args.slug)
    p = catalogue.paths(problem)

    if not catalogue.is_implemented(problem):
        die("'%s' has no reference solution yet, so it cannot be judged."
            % problem["slug"],
            "Run `judge list --ready` to see what is implemented.")

    if os.path.exists(p["solution"]):
        if args.reset:
            do_reset = True
        else:
            try:
                ans = input("  solution exists — reset to blank? [y/N] ").strip().lower()
            except (EOFError, KeyboardInterrupt):
                print()
                return
            do_reset = ans.startswith("y")

        if do_reset:
            os.makedirs(p["attempts"], exist_ok=True)
            stamp = time.strftime("%Y%m%d-%H%M%S")
            shutil.move(p["solution"], os.path.join(p["attempts"],
                                                    "Solution-%s.java" % stamp))
            print(dim("  archived your previous attempt to .attempts/"))
        else:
            print(dim("  keeping existing solution"))

    if not os.path.exists(p["solution"]):
        os.makedirs(p["solution_dir"], exist_ok=True)
        with open(p["solution"], "w") as f:
            f.write(_stub(problem))

    progress.mark_opened(problem["slug"])

    print("  %s" % bold(p["solution"]))
    print()
    print(dim("  judge show %s     full statement and hints" % problem["slug"]))
    print(dim("  judge run  %s     compile and test" % problem["slug"]))

    subprocess.Popen(["idea", p["solution"], p["statement"]],
                     stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)


def _stub(problem):
    """Generate Solution.java with a compact header. Full text lives in statement.md."""
    p = catalogue.paths(problem)
    header, signature = _statement_parts(p["statement"])

    lines = ["/*", " * %s  %s" % (problem.get("title", problem["slug"]),
                                  dim("") and "" or "")]
    lines = ["/*"]
    lines.append(" * %s" % problem.get("title", problem["slug"]))
    if problem["tier"] >= 2:
        lines.append(" * LeetCode %s · %s" % (problem.get("lc_id", problem.get("id")),
                                              problem.get("difficulty", "")))
        lines.append(" * https://leetcode.com/problems/%s/" % problem["slug"])
    else:
        lines.append(" * Tier %d drill · %s" % (problem["tier"],
                                                problem.get("group", "")))
    lines.append(" *")
    for line in header:
        lines.append((" * " + line).rstrip())
    lines.append(" *")
    lines.append(" * Patterns: %s" % ", ".join(problem.get("pattern", [])))
    lines.append(" * Full statement and hints:  judge show %s" % problem["slug"])
    lines.append(" */")
    lines.append("")
    # leetcode.com auto-imports java.util.*, so a signature like List<List<Integer>>
    # compiles there without an import. Mirror that, or a faithful solution won't
    # compile here.
    lines.append("import java.util.*;")
    lines.append("")
    if isinstance(signature, list):
        # Design and interactive problems: the statement supplies the whole class
        # skeleton (class MyQueue { ... }, or Solution extends GuessGame).
        lines.extend(signature)
        return "\n".join(lines) + "\n"
    lines.append("class Solution {")
    lines.append("    %s {" % signature)
    lines.append("        ")
    lines.append("    }")
    lines.append("}")
    return "\n".join(lines) + "\n"


def _statement_parts(statement_path):
    """
    Pull the compact header and the Java signature out of statement.md.

    Convention: everything between '## Brief' and the next heading is the compact
    header; the signature lives in a ```java fenced block after '## Signature'.
    """
    if not os.path.exists(statement_path):
        return ["(statement missing)"], "void solve()"

    with open(statement_path) as f:
        text = f.read()

    header = _section(text, "## Brief")
    # Only the FIRST fenced block is the signature. A Signature section may carry
    # further blocks for reference -- e.g. the ListNode definition -- which must not
    # end up in the stub.
    code = _first_code_block(text, "## Signature")
    if any(l.strip().startswith(("class ", "public class ")) for l in code):
        return [l for l in header if l.strip() or True], code
    sig_block = code or _section(text, "## Signature")
    signature = "void solve()"
    for line in sig_block:
        s = line.strip()
        if s.startswith("public "):
            signature = s.rstrip("{").strip()
            break
    return [l for l in header if l.strip() or True], signature


def _first_code_block(text, heading):
    """Lines inside the first ``` fence after `heading`, before the next heading."""
    out, in_section, in_code = [], False, False
    for line in text.splitlines():
        if line.strip() == heading:
            in_section = True
            continue
        if not in_section:
            continue
        if line.startswith("## "):
            break
        if line.strip().startswith("```"):
            if in_code:
                return out
            in_code = True
            continue
        if in_code:
            out.append(line)
    return out


def _section(text, heading):
    out, capturing = [], False
    for line in text.splitlines():
        if line.strip() == heading:
            capturing = True
            continue
        if capturing and line.startswith("## "):
            break
        if capturing:
            if line.strip().startswith("```"):
                continue
            out.append(line)
    while out and not out[0].strip():
        out.pop(0)
    while out and not out[-1].strip():
        out.pop()
    return out


# ----------------------------------------------------------------------- show

def cmd_show(args):
    problem = resolve_or_die(args.slug)
    p = catalogue.paths(problem)
    if not os.path.exists(p["statement"]):
        die("no statement written for '%s' yet" % problem["slug"])
    with open(p["statement"]) as f:
        text = f.read()
    if not args.hints:
        text = _strip_hints(text)
    print(text)

    subprocess.Popen(["idea", p["statement"]],
                     stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)


def _strip_hints(text):
    out, skipping = [], False
    for line in text.splitlines():
        if line.strip().startswith("## Hints"):
            skipping = True
            out.append(line)
            out.append("")
            out.append(dim("  (hidden — rerun with --hints)"))
            continue
        if skipping and line.startswith("## "):
            skipping = False
        if not skipping:
            out.append(line)
    return "\n".join(out)


def _rebuild_pages():
    """Regenerate docs/index.html and auto-push to update GitHub Pages."""
    try:
        html = pages.build()
        out = os.path.join(pages.DOCS_DIR, "index.html")
        os.makedirs(os.path.dirname(out), exist_ok=True)
        with open(out, "w") as f:
            f.write(html)
        repo_root = os.path.dirname(catalogue.ROOT)
        progress_rel = os.path.relpath(progress.path(), repo_root)
        docs_rel = os.path.relpath(out, repo_root)
        subprocess.run(["git", "add", progress_rel, docs_rel],
                       cwd=repo_root, capture_output=True)
        subprocess.run(["git", "commit", "-m", "Update progress dashboard"],
                       cwd=repo_root, capture_output=True)
        subprocess.run(["git", "push"],
                       cwd=repo_root, capture_output=True)
    except Exception:
        pass


# ------------------------------------------------------------------------ run

def cmd_run(args):
    problem = resolve_or_die(args.slug)
    seed = args.seed if args.seed is not None else random.randrange(1, 2**31)

    print()
    print("  %s  %s" % (bold(problem["slug"]),
                        dim(problem.get("difficulty", "") + " · tier %d" % problem["tier"])))
    print()

    try:
        build.harness()
        build.reference(problem)
    except build.CompileError as e:
        die(str(e), e.output)

    try:
        build.solution(problem)
    except build.CompileError as e:
        print(red("  compile error"))
        print()
        for line in e.output.splitlines():
            print("    " + line)
        progress.record(problem["slug"], "COMPILE ERROR")
        sys.exit(1)

    mode = "fixed" if args.fixed else "stress"
    iterations = 0 if args.fixed else args.n
    # Pinned examples alone are not a solve: they are the examples you read in the
    # statement. Only a stress-tested pass counts toward the schedule.
    pinned_only = iterations <= 0
    started = time.time()
    verdict, out = build.run_judge(problem, mode, iterations, seed)
    elapsed = int((time.time() - started) * 1000)

    print(out)
    if verdict:
        e, event = progress.record(problem["slug"], verdict, elapsed, full=not pinned_only)
        days = None
        if e.get("due"):
            days = max(1, int((e["due"] - time.time()) / 86400 + 0.5))
        plural = lambda d: "" if d == 1 else "s"
        ease_str = dim("  ease: %.2f" % e.get("ease", 2.5))
        if event == "pinned-only":
            print(dim("  pinned examples only -- not counted as a solve; drop --fixed / -n 0 to stress test"))
        elif event in ("solved", "advanced") and days:
            print(dim("  scheduled for a blank-file re-solve in %d day%s" % (days, plural(days))))
            print(ease_str)
        elif event == "not-due" and days:
            print(dim("  re-solve not due yet (in %d day%s) -- schedule unchanged" % (days, plural(days))))
        elif event == "reset" and days:
            print(dim("  re-solve failed -- back to the first interval (due in %d day%s)" % (days, plural(days))))
            print(ease_str)
        if verdict == "ACCEPTED":
            _rebuild_pages()
        print()
    sys.exit(0 if verdict == "ACCEPTED" else 1)


# ----------------------------------------------------------------------- time

def cmd_time(args):
    problem = resolve_or_die(args.slug)
    try:
        build.harness()
        build.reference(problem)
        build.solution(problem)
    except build.CompileError as e:
        die(str(e), e.output)
    print()
    _, out = build.run_judge(problem, "time", 0, args.seed or 42)
    print(out)
    print(dim("  Indicative only: single JVM, JIT-warmed but not isolated."))
    print(dim("  Read it as 'consistent with', never as proof."))
    print()


# --------------------------------------------------------------------- verify

def cmd_verify(args):
    try:
        build.harness()
    except build.CompileError as e:
        die(str(e), e.output)

    problems = [p for p in catalogue.all_problems() if catalogue.is_implemented(p)]
    if args.tier is not None:
        problems = [p for p in problems if p["tier"] == args.tier]

    print()
    print("  verifying %d reference%s against pinned examples"
          % (len(problems), "" if len(problems) == 1 else "s"))
    print()

    bad = []
    for p in problems:
        try:
            build.reference(p)
        except build.CompileError as e:
            bad.append((p["slug"], "reference does not compile"))
            print("  %s %s" % (red("✗"), p["slug"]))
            for line in e.output.splitlines()[:6]:
                print("      " + dim(line))
            continue

        verdict, out = build.run_judge(p, "verify", 0, 1)
        # Only an explicit VERIFIED is a pass. A crash, a timeout, a load error or a
        # missing verdict line must never be read as "the reference is fine".
        if verdict != "VERIFIED":
            bad.append((p["slug"], verdict or "no verdict"))
            print("  %s %s %s" % (red("✗"), p["slug"], dim(verdict or "no verdict")))
            for line in out.splitlines():
                if line.strip():
                    print("    " + line)
        else:
            no_pins = "0 pinned" in out or "0 examples" in out
            mark = yellow("!") if no_pins else green("✓")
            note = dim("  no pinned examples") if no_pins else ""
            print("  %s %s%s" % (mark, p["slug"], note))

    print()
    if bad:
        print(red("  %d reference%s broken" % (len(bad), "" if len(bad) == 1 else "s")))
        sys.exit(1)
    print(green("  all %d references agree with their pinned examples" % len(problems)))


# ----------------------------------------------------------------------- next

def cmd_selftest(args):
    """
    Run every reference against ITSELF under stress.

    Any failure is a judge bug, not a solution bug: either a generator that produces
    different values on the same seed, or a comparison mode that does not consider a
    value equal to itself. Worth running after touching anything in harness/.
    """
    try:
        build.harness()
    except build.CompileError as e:
        die(str(e), e.output)

    problems = [p for p in catalogue.all_problems() if catalogue.is_implemented(p)]
    if args.tier is not None:
        problems = [p for p in problems if p["tier"] == args.tier]

    print()
    print("  self-testing %d reference%s at %d iterations each"
          % (len(problems), "" if len(problems) == 1 else "s", args.n))
    print()

    bad = []
    for p in problems:
        try:
            build.reference(p)
            sol_dir = build.reference_as_solution(p)
        except build.CompileError as e:
            bad.append(p["slug"])
            print("  %s %s %s" % (red("✗"), p["slug"], dim("(compile)")))
            for line in e.output.splitlines()[:4]:
                print("      " + dim(line))
            continue

        verdict, out = build.run_judge(p, "stress", args.n, args.seed, sol_dir=sol_dir)
        if verdict == "ACCEPTED":
            print("  %s %s" % (green("✓"), p["slug"]))
        else:
            bad.append(p["slug"])
            print("  %s %s %s" % (red("✗"), p["slug"], dim(verdict or "no verdict")))
            for line in out.splitlines():
                if line.strip():
                    print("    " + line)

    print()
    if bad:
        print(red("  %d reference%s inconsistent with itself — this is a judge bug"
                  % (len(bad), "" if len(bad) == 1 else "s")))
        sys.exit(1)
    print(green("  all %d references self-consistent" % len(problems)))


def cmd_next(args):
    state = progress.load()
    due = progress.due_now(state)

    if due and not args.new:
        slug = due[0]
        problem = catalogue.by_slug(slug)
        if problem:
            print()
            print("  %s" % bold("re-solve due"))
            print(dim("  You solved this before. Do it again from a blank file."))
            print()
            _print_pick(problem)
            return

    candidates = []
    for p in catalogue.all_problems():
        if not catalogue.is_implemented(p):
            continue
        if args.tier is not None and p["tier"] != args.tier:
            continue
        if args.pattern and not any(args.pattern.lower() in pat.lower()
                                    for pat in p.get("pattern", [])):
            continue
        e = state["problems"].get(p["slug"])
        if e and e["solved"]:
            continue
        candidates.append(p)

    if not candidates:
        print(dim("  nothing left matching those filters"))
        return

    # Lowest tier first, then the finance-weighted problems inside a tier.
    candidates.sort(key=lambda p: (p["tier"], p.get("order", 0), -catalogue.bank_weight(p)))
    print()
    _print_pick(candidates[0])


def _print_pick(problem):
    print("  %s  %s" % (bold(problem["slug"]),
                        dim(problem.get("difficulty", "") + " · tier %d" % problem["tier"])))
    print(dim("  %s" % ", ".join(problem.get("pattern", []))))
    print()
    print("    judge open %s" % problem["slug"])
    print()


# ---------------------------------------------------------------------- due

def cmd_due(args):
    state = progress.load()
    due_slugs = progress.due_now(state)
    all_upcoming = progress.upcoming(state)

    print()
    if due_slugs:
        print("  %s %d re-solve%s due today" %
              (yellow("●"), len(due_slugs), "" if len(due_slugs) == 1 else "s"))
        print()
        for slug in due_slugs:
            p = catalogue.by_slug(slug)
            if not p:
                continue
            e = state["problems"].get(slug)
            overdue_days = max(0, int((time.time() - e["due"]) / 86400))
            colour = DIFFICULTY_COLOUR.get(p.get("difficulty", ""), dim)
            diff = colour(p.get("difficulty", "")[:6].ljust(6))
            tier = dim("T%d" % p["tier"])
            pats = dim(", ".join(p.get("pattern", [])[:3]))
            age = dim("(%dd overdue)" % overdue_days) if overdue_days >= 1 else ""
            print("    %s %s  %-40s %s %s" % (tier, diff, bold(slug), pats, age))
        print()
    else:
        print(dim("  no re-solves due today"))
        print()

    if all_upcoming:
        grouped = {}
        for due_epoch, slug in all_upcoming:
            days_until = max(1, int((due_epoch - time.time()) / 86400 + 0.5))
            grouped.setdefault(days_until, []).append(slug)

        print("  %s %d upcoming re-solve%s" %
              (dim("◇"), len(all_upcoming),
               "" if len(all_upcoming) == 1 else "s"))
        print()
        for days in sorted(grouped):
            print("    %s" % dim("in %d day%s" % (days, "" if days == 1 else "s")))
            for slug in grouped[days]:
                p = catalogue.by_slug(slug)
                if not p:
                    continue
                colour = DIFFICULTY_COLOUR.get(p.get("difficulty", ""), dim)
                diff = colour(p.get("difficulty", "")[:6].ljust(6))
                tier = dim("T%d" % p["tier"])
                pats = dim(", ".join(p.get("pattern", [])[:3]))
                print("    %s %s  %-40s %s" % (tier, diff, slug, pats))
        print()
    else:
        print(dim("  no upcoming re-solves scheduled"))
        print()


# --------------------------------------------------------------------- status

def cmd_status(args):
    tiers, state = progress.summary()
    print()
    print("  %-5s %-16s %8s %8s %8s %6s" %
          ("tier", "", "ready", "solved", "tried", "due"))
    print(dim("  " + "-" * 56))
    for t in sorted(tiers):
        row = tiers[t]
        name = catalogue.TIER_NAMES.get(t, "")
        bar = _bar(row["solved"], row["implemented"])
        print("  %-5s %-16s %8s %8s %8s %6s   %s" % (
            "T%d" % t, name,
            "%d/%d" % (row["implemented"], row["total"]),
            row["solved"], row["attempted"],
            row["due"] or "", bar))

    total_due = sum(r["due"] for r in tiers.values())
    print()
    if total_due:
        print("  %s %d re-solve%s due   %s" %
              (yellow("●"), total_due, "" if total_due == 1 else "s",
               dim("judge next")))
    else:
        print(dim("  no re-solves due"))
    print()


def _bar(done, total, width=18):
    if not total:
        return dim("·" * width)
    filled = int(width * done / total)
    return green("█" * filled) + dim("·" * (width - filled))


# ------------------------------------------------------------------- classify

def cmd_classify(args):
    """
    Pattern-recognition drill. No code: name the pattern, the invariant, and the
    data structure. Primitives teach mechanics; they do not teach recognition, and
    recognition is what actually fails in an interview.
    """
    pool = [p for p in catalogue.all_problems()
            if catalogue.is_implemented(p) and p.get("pattern")]
    if args.tier is not None:
        pool = [p for p in pool if p["tier"] == args.tier]
    if not pool:
        die("no problems available to drill yet")

    random.shuffle(pool)
    pool = pool[:args.n]
    correct = 0

    for i, problem in enumerate(pool, 1):
        p = catalogue.paths(problem)
        brief = []
        if os.path.exists(p["statement"]):
            with open(p["statement"]) as f:
                brief = _section(f.read(), "## Brief")

        print()
        print(dim("  %d/%d" % (i, len(pool))))
        print()
        for line in brief[:6]:
            print("  " + line)
        print()
        print(dim("  Name the pattern, the invariant in one sentence, and the data structure."))
        try:
            input(dim("  [enter to reveal] "))
        except (EOFError, KeyboardInterrupt):
            print()
            return
        print("  %s %s" % (green("pattern:"), ", ".join(problem["pattern"])))
        try:
            got = input(dim("  did you get it? [y/N] ")).strip().lower()
        except (EOFError, KeyboardInterrupt):
            print()
            return
        if got.startswith("y"):
            correct += 1

    print()
    print("  %d/%d" % (correct, len(pool)))
    print()


# ---------------------------------------------------------------------- pages

def cmd_pages(args):
    html = pages.build()
    out = os.path.join(pages.DOCS_DIR, "index.html")
    os.makedirs(os.path.dirname(out), exist_ok=True)
    with open(out, "w") as f:
        f.write(html)
    print()
    print("  %s" % bold(out))
    print(dim("  commit and push to update GitHub Pages"))
    print()


# ------------------------------------------------------------------------ main

def main(argv=None):
    ap = argparse.ArgumentParser(
        prog="judge", description="Offline DSA judge. Java solutions, local grading.")
    sub = ap.add_subparsers(dest="cmd")

    p = sub.add_parser("list", help="list problems")
    p.add_argument("--tier", type=int, choices=[0, 1, 2, 3])
    p.add_argument("--pattern")
    p.add_argument("--group")
    p.add_argument("--difficulty")
    p.add_argument("--company")
    p.add_argument("--todo", action="store_true", help="unsolved and ready to solve")
    p.add_argument("--solved", action="store_true")
    p.add_argument("--ready", action="store_true", help="has a reference, i.e. runnable")
    p.set_defaults(func=cmd_list)

    p = sub.add_parser("open", help="create your Solution.java")
    p.add_argument("slug")
    p.add_argument("--reset", action="store_true", help="archive current attempt, start blank")
    p.set_defaults(func=cmd_open)

    p = sub.add_parser("show", help="print the full statement")
    p.add_argument("slug")
    p.add_argument("--hints", action="store_true")
    p.set_defaults(func=cmd_show)

    p = sub.add_parser("run", help="compile and test your solution")
    p.add_argument("slug")
    p.add_argument("-n", type=int, default=1000, help="stress iterations (default 1000)")
    p.add_argument("--seed", type=int)
    p.add_argument("--fixed", action="store_true", help="pinned examples only")
    p.set_defaults(func=cmd_run)

    p = sub.add_parser("time", help="empirical growth curve")
    p.add_argument("slug")
    p.add_argument("--seed", type=int)
    p.set_defaults(func=cmd_time)

    p = sub.add_parser("verify", help="check every reference against its pinned examples")
    p.add_argument("--tier", type=int)
    p.set_defaults(func=cmd_verify)

    p = sub.add_parser("selftest",
                       help="run every reference against itself (catches judge bugs)")
    p.add_argument("--tier", type=int)
    p.add_argument("-n", type=int, default=1500)
    p.add_argument("--seed", type=int, default=99)
    p.set_defaults(func=cmd_selftest)

    p = sub.add_parser("next", help="what to work on now")
    p.add_argument("--tier", type=int)
    p.add_argument("--pattern")
    p.add_argument("--new", action="store_true", help="skip due re-solves")
    p.set_defaults(func=cmd_next)

    p = sub.add_parser("due", help="show due re-solves and next upcoming")
    p.set_defaults(func=cmd_due)

    p = sub.add_parser("status", help="progress overview")
    p.set_defaults(func=cmd_status)

    p = sub.add_parser("classify", help="pattern-recognition drill, no code")
    p.add_argument("-n", type=int, default=10)
    p.add_argument("--tier", type=int)
    p.set_defaults(func=cmd_classify)

    p = sub.add_parser("pages", help="generate GitHub Pages dashboard")
    p.set_defaults(func=cmd_pages)

    args = ap.parse_args(argv)
    if not getattr(args, "func", None):
        ap.print_help()
        return 0
    return args.func(args)
