#!/usr/bin/env python3
"""
Mutation test for the judge: does it REJECT wrong solutions and ACCEPT right ones?

`verify` proves a reference matches its pins; `selftest` proves it agrees with itself.
Neither proves the judge can tell a wrong answer from a right one. This does. Each
mutant is a complete Solution.java with a known verdict:

    reject  a plausible bug -- off-by-one, missed edge case, overflow, wrong order.
            The judge must NOT say ACCEPTED.
    accept  a correct solution that differs from the reference (other algorithm,
            other valid output). The judge MUST say ACCEPTED -- this is what catches
            a comparison mode that is too strict.

Mutants run in a temp dir. Your solutions/ directory is never touched.

    python3 tools/mutants.py                 # every problem that has mutants
    python3 tools/mutants.py merge-sorted    # substring match on slug
"""

import os
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)

from judge import build, catalogue  # noqa: E402
from mutants_t2 import MUTANTS  # noqa: E402
from mutants_t3 import MUTANTS as _T3  # noqa: E402

MUTANTS.update(_T3)

ITERATIONS = 1500
SEED = 1
MUTANT_TIMEOUT_S = 45


def run_one(problem, name, expect, source):
    with tempfile.TemporaryDirectory() as tmp:
        src = os.path.join(tmp, "Solution.java")
        with open(src, "w") as f:
            f.write(source)
        r = subprocess.run(["javac", "-cp", build.classpath(problem), "-d", tmp, src],
                           capture_output=True, text=True)
        if r.returncode != 0:
            return False, "mutant does not compile:\n" + (r.stdout + r.stderr)[:600]
        try:
            build.check_no_shadowing(problem, tmp, "mutant")
        except build.CompileError as e:
            return False, str(e)
        verdict, out = build.run_judge(problem, "stress", ITERATIONS, SEED, sol_dir=tmp,
                                        timeout=MUTANT_TIMEOUT_S)

    if verdict is None:
        return False, "no verdict\n" + out[-600:]
    if "REFERENCE BUG" in verdict or "COMPILE" in verdict:
        return False, "judge reported %s\n%s" % (verdict, out[-600:])
    accepted = verdict.startswith("ACCEPTED")
    if expect == "accept" and not accepted:
        return False, "should be ACCEPTED, judge said %s\n%s" % (verdict, out[-800:])
    if expect == "reject" and accepted:
        return False, "should be REJECTED, judge ACCEPTED it -- a hole in the tests"
    return True, verdict


def main(filters):
    build.harness()
    total = failed = 0
    for slug in sorted(MUTANTS):
        if filters and not any(f in slug for f in filters):
            continue
        problem = catalogue.by_slug(slug)
        if problem is None:
            print("  ? %s: not in catalogue" % slug)
            failed += 1
            continue
        build.reference(problem)
        for name, expect, source in MUTANTS[slug]:
            total += 1
            ok, detail = run_one(problem, name, expect, source)
            mark = "✓" if ok else "✗"
            print("  %s %-44s %-7s %-34s %s" % (mark, slug[:44], expect, name[:34],
                                                 detail.splitlines()[0] if ok else ""))
            if not ok:
                failed += 1
                for line in detail.splitlines()[:14]:
                    print("        " + line)
    print()
    print("  %d mutants, %d unexpected" % (total, failed))
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
