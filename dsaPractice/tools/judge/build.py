"""Compilation: harness, reference, and your solution, with mtime caching."""

import os
import subprocess

from . import catalogue

HARNESS_DIR = os.path.join(catalogue.ROOT, "harness")
BUILD_HARNESS = os.path.join(catalogue.ROOT, "build", "harness")


class CompileError(Exception):
    def __init__(self, message, output):
        super().__init__(message)
        self.output = output


def _newest(paths):
    newest = 0.0
    for p in paths:
        if os.path.exists(p):
            newest = max(newest, os.path.getmtime(p))
    return newest


def _stale(sources, out_dir, marker):
    stamp = os.path.join(out_dir, marker)
    if not os.path.exists(stamp):
        return True
    return _newest(sources) > os.path.getmtime(stamp)


def _touch(out_dir, marker):
    os.makedirs(out_dir, exist_ok=True)
    open(os.path.join(out_dir, marker), "w").close()


def _javac(args, what):
    r = subprocess.run(["javac"] + args, capture_output=True, text=True)
    if r.returncode != 0:
        raise CompileError("could not compile %s" % what, r.stdout + r.stderr)
    return r.stdout + r.stderr


def harness(force=False):
    sources = [os.path.join(HARNESS_DIR, f)
               for f in os.listdir(HARNESS_DIR) if f.endswith(".java")]
    if not force and not _stale(sources, BUILD_HARNESS, ".stamp"):
        return
    os.makedirs(BUILD_HARNESS, exist_ok=True)
    _javac(["-d", BUILD_HARNESS] + sources, "the judge harness")
    _touch(BUILD_HARNESS, ".stamp")


def reference(problem, force=False):
    p = catalogue.paths(problem)
    if not os.path.exists(p["ref"]):
        raise CompileError(
            "no reference for '%s' yet" % problem["slug"],
            "This problem is in the catalogue but not implemented.\n"
            "Run `judge list --todo` to see what is ready to solve.")
    if not force and not _stale([p["ref"]], p["build_ref"], ".stamp"):
        return
    os.makedirs(p["build_ref"], exist_ok=True)
    _javac(["-cp", BUILD_HARNESS, "-d", p["build_ref"], p["ref"]],
           "the reference for " + problem["slug"])
    _touch(p["build_ref"], ".stamp")


def solution(problem, force=False):
    """
    Compile your Solution.java. Compiler output is returned rather than raised so the
    CLI can print javac's own error text, which is more useful than anything I'd write.
    """
    p = catalogue.paths(problem)
    if not os.path.exists(p["solution"]):
        raise CompileError(
            "you have not started '%s' yet" % problem["slug"],
            "Run:  judge open %s" % problem["slug"])
    if not force and not _stale([p["solution"]], p["build_sol"], ".stamp"):
        return
    os.makedirs(p["build_sol"], exist_ok=True)
    _javac(["-cp", BUILD_HARNESS, "-d", p["build_sol"], p["solution"]],
           "your solution")
    _touch(p["build_sol"], ".stamp")


def reference_as_solution(problem, force=False):
    """
    Compile the reference as if it were your Solution, for the self-test.

    Renames the class and hides spec() so the file becomes a plain Solution. Used by
    `judge selftest` to run every reference against itself: any disagreement means a
    nondeterministic generator or an unstable comparison mode, i.e. a judge bug.
    """
    import re
    p = catalogue.paths(problem)
    if not os.path.exists(p["ref"]):
        raise CompileError("no reference for '%s'" % problem["slug"], "")

    out_dir = os.path.join(catalogue.ROOT, "build", "selftest", problem["slug"])
    src_dir = os.path.join(out_dir, "src")
    os.makedirs(src_dir, exist_ok=True)

    with open(p["ref"]) as f:
        body = f.read()
    body = re.sub(r"\bclass\s+Ref\s+implements\s+Problem\s*\{", "class Solution {", body)
    body = body.replace("public Spec spec() {", "private Spec unusedSpec() {")
    # A reference that names its own class -- Ref::method, Ref.CONSTANT, new Ref() --
    # would not compile after the rename.
    body = re.sub(r"\bRef\b(?=\s*[.:(])", "Solution", body)

    src = os.path.join(src_dir, "Solution.java")
    with open(src, "w") as f:
        f.write(body)

    _javac(["-cp", BUILD_HARNESS, "-d", out_dir, src],
           "the reference-as-solution for " + problem["slug"])
    _touch(out_dir, ".stamp")
    return out_dir


def run_judge(problem, mode, iterations, seed, sol_dir=None):
    """Invoke the Java judge. Returns (verdict, stdout)."""
    p = catalogue.paths(problem)
    r = subprocess.run(
        ["java", "-cp", BUILD_HARNESS, "Judge",
         problem["slug"], p["build_ref"], sol_dir or p["build_sol"],
         mode, str(iterations), str(seed)],
        capture_output=True, text=True)

    out = r.stdout
    verdict = None
    kept = []
    for line in out.splitlines():
        if line.startswith("::verdict "):
            verdict = line[len("::verdict "):].strip()
        else:
            kept.append(line)
    if r.stderr.strip():
        kept.append(r.stderr.rstrip())
    return verdict, "\n".join(kept)
