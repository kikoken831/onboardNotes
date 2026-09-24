# dsaPractice — offline judge

An offline LeetCode-style judge for Java, plus a ramp-up ladder built to sit **below**
the difficulty floor of the 179-problem target list.

No network. No login. No Maven or Gradle. JDK 21 and Python 3 standard library only.

```bash
./judge status                 # where you are
./judge next                   # what to do now
./judge open  p-remove-value   # creates solutions/<slug>/Solution.java
./judge show  p-remove-value   # full statement (--hints to reveal hints)
./judge run   p-remove-value   # compile, test, grade
./judge time  p-remove-value   # empirical growth curve of your solution
./judge list  --tier 2 --todo  # browse (also --pattern, --group, --company, --solved)
```

---

## How it grades

Every problem has a hidden reference solution in `.ref/<slug>/Ref.java`. Your solution
is judged in two phases:

1. **Pinned examples** — compared against answers taken from LeetCode, not from the
   reference. If the reference ever disagrees with a pin, the verdict is
   `REFERENCE BUG` and you are told explicitly not to change your code.
2. **Randomised differential testing** — thousands of generated inputs run through both
   your code and the reference. On disagreement the input is **shrunk** to the smallest
   one that still fails.

That second part is the reason this exists. LeetCode tells you "Wrong Answer, testcase
137". This tells you:

```
  stress   ✗  2/3000 failed
      minimised input
      [-1,0], -1
    yours     []
    expected  [0,1]
    count differs: yours 0, expected 2; missing 0, 1
```

### Honest limitation

Differential testing only detects *disagreement*. If the reference is wrong in the same
way your solution is wrong, both pass. The pinned examples are the only defence against
that, which is why `judge verify` exists and why subtle problems carry more pins.

```bash
./judge verify        # every reference against every pinned example
./judge selftest      # every reference against itself under stress
```

`selftest` compiles each reference as its own solution and stress-runs it. A failure
there means a nondeterministic generator or an unstable comparison mode — a judge bug,
not a solution bug. Run it after touching anything in `harness/`.

Neither of those proves the judge can tell a wrong answer from a right one. That is what
**mutation testing** is for:

```bash
python3 tools/mutants.py              # every problem that has mutants
python3 tools/mutants.py hashmap      # substring match on slug
```

Each problem carries plausible **buggy** solutions — off-by-one, `int` overflow, the
un-guarded flood fill, the queue that transfers on every pop — which must be *rejected*,
and **correct-but-different** ones (another algorithm, another valid output) which must be
*accepted*. The second kind is what catches a comparison mode that is too strict. It runs
in a temp dir and never touches `solutions/`.

It has already earned its keep: its first run found that the exact comparator was
**accepting any wrong answer of the right length** that wasn't a reversal or reordering
(`[7,0]` for `[7,7]`). That bug predated tier 2; if you solved drills before it was fixed,
re-run them.

---

## The tiers

The target list has a **floor problem**: for a third of its patterns the easiest
problem in it is already Medium. LeetCode "Easy" also spans a 5× acceptance-rate range
(17.5%–91%), so "I can't do Easy" is often a sampling problem rather than a capability
one. Hence four tiers:

| Tier | What | Count | Status |
|---|---|---|---|
| **0** | Java mechanics — measured JVM traps, predict-then-run | 17 traps (read, not graded) | ready |
| **1** | Authored primitives — one mechanical sub-skill each, 5–15 lines | 69 drills | **ready** |
| **2** | Real LeetCode Easy below the target list's floor | 58 problems | **ready** |
| **3** | The supplied target list | 179 problems | **ready** |

All four tiers are complete. Start at tier 1; tier 2 is where each tier 1 group pays off
on a real LeetCode problem, and tier 3 is the supplied list itself. `judge next` works
through them in catalogue order.

### Design and interactive problems

Eight tier 2 problems aren't "a method on `Solution`":

- **Design** (232, 225, 933, 703, 1603, 705, 706) — you implement the class LeetCode
  names (`class MyQueue`, not `class Solution`). Pinned examples use LeetCode's exact
  format, and a failure is reported at the **first diverging call** and shrunk to the
  shortest call sequence that still fails:

  ```
  minimised to 5 calls
  ["MyQueue","push","push","pop","push","peek"]
  [[],[4],[8],[],[5],[]]
  call 5, peek(): yours 5, expected 8
  ```

- **Interactive** (374) — `class Solution extends GuessGame`, calling the provided
  `guess(num)`. The pick is installed per test and never passed to you.

Tier 3 adds a few more shapes, all matching leetcode.com:

- **Your own class name** — 297 and 271 are `public class Codec`. A public class must live
  in a file of the same name, so the judge compiles `Solution.java` from a copy named after
  it; just write the class as LeetCode shows it. The judge then drives the round trip
  itself (`deserialize(serialize(root))`, on **separate** instances), and rejects results
  that reuse the input's nodes or list.
- **Arguments that are nodes of the input** — 235/236's `p` and `q`, 863's `target`, and
  141/142's cycle (`pos`) are given by value in the examples; each call receives the real
  node objects inside its own copy of the tree or list. 142 also checks you didn't modify
  the list.
- **Provided classes** — 133's graph `Node` and 759's `Interval` are on the classpath for
  those problems only. Don't declare them.
- **Many correct answers** — Sudoku, Find Peak, Course Schedule II, Alien Dictionary,
  Reorganize String and others are checked by property (valid, complete, consistent), not
  against one expected output.

A few judge behaviours worth knowing:

- A **fresh `Solution` instance per test case**, as on LeetCode — instance fields reset;
  `static` fields do not.
- **Every** test is time-limited, including pinned examples; several pins exist precisely
  because the naive solution is exponential on them.
- An infinite loop is reported as `TIME LIMIT EXCEEDED` after 120 s rather than hanging.
- Don't declare a top-level class whose name the judge already uses — `ListNode`,
  `TreeNode`, `Spec`, `Gen`, `Match`, `Calls`, `Pre` and the rest of `harness/`. `judge run`
  refuses such a solution and says which name clashed; a nested class is fine.

Tier 1 is mandatory rather than optional for five patterns — **backtracking, trie,
topological sort, union-find, and LIS/knapsack DP have no Easy entry point on LeetCode
at all**. There is nothing to warm up on, so the warm-up had to be written.

### The 15 Tier 1 groups

| Group | Drills | The sub-skill |
|---|---|---|
| write-index | 8 | read pointer and write pointer are not the same pointer |
| hash-counting | 5 | `int[26]`, count-then-decide, `computeIfAbsent` |
| binary-search | 6 | four distinct templates, not one you mutate |
| linked-list | 8 | dummy head, save `next` first, return `dummy.next` |
| recursion-shape | 7 | return up / thread down / both |
| iterative-traversal | 4 | left spine, `lastVisited`, size snapshot |
| grid-search | 4 | bounds first, mark before recursing, mark on enqueue |
| backtracking ⚠️ | 6 | choose / recurse / un-choose, copy at the leaf |
| trie ⚠️ | 3 | 26-child nodes, `isWord`, augment for O(1) queries |
| union-find ⚠️ | 3 | link roots not elements, path halving, count down from n |
| topological-sort ⚠️ | 2 | in-degree count is the cycle check; three-colour DFS |
| monotonic-stack | 3 | push indices, pop condition, sentinel flush |
| prefix-sum | 3 | size `n+1`, look up before inserting, inclusion–exclusion |
| heap | 3 | max-heap to keep the k smallest, `int[]` comparators |
| dynamic-programming ⚠️ | 4 | naive → memoise → tabulate; define the state first |

Start here:

```bash
cat drills/00-java-mechanics/README.md    # tier 0, read it once
./judge next --tier 1
```

---

## Spaced re-solving

Solving something once teaches you less than re-deriving it later from an empty file.
Your first full `ACCEPTED` schedules a blank-file re-solve **1 day** later. Each
successful re-solve pushes the next one further out — **3, then 7, then 21 days**, and
21 days thereafter. A failed re-solve starts the ladder again at 1 day. Runs when nothing
is due don't move the schedule either way, and `--fixed` / `-n 0` runs (pinned examples
only) are logged but never count as solving.

`judge next` surfaces due re-solves ahead of new problems. `judge open <slug> --reset`
archives your previous attempt to `.attempts/` and gives you an empty file.

This is the best-evidenced component in the design — the testing effect and distributed
practice are among the more robust findings in learning science, considerably better
supported than "drilling primitives composes into problem-solving", which is largely
folklore.

## Pattern recognition

Drilling mechanics does not teach you to *recognise* which pattern a cold problem
wants; transfer research is fairly clear that this kind of far transfer does not happen
for free. So it gets its own drill — no code, name the pattern and the invariant; aim for about
90 seconds each (the drill doesn't time you):

```bash
./judge classify -n 10
```

---

## Layout

```
judge                        the CLI
problems.json                catalogue (generated — see tools/make_catalogue.py)
harness/                     the Java judge: LcCodec, Match, Shrink, Gen, Judge, Calls
drills/<slug>/statement.md   tier 0 and 1 statements
statements/<slug>/           tier 2 and 3 statements
solutions/<slug>/            YOUR code
.ref/<slug>/Ref.java         reference solution + test spec  (spoilers)
tools/mutants.py             mutation test: does the judge reject bugs, accept alternatives?
.progress.json               attempts and the re-solve schedule
```

`.ref/` holds the answers. Looking is your call — but the shrunk counterexample is
almost always more useful than the reference, so try `judge run` first.

## A note on the supplied list's ids

Several ids in the source list have drifted from real LeetCode — `792` is Binary Search
(really 704), `1250` is Longest Common Subsequence (really 1143), `745`/`746` are
each off by one (really 744/745), and fifteen others. Every slug is correct, so **the judge keys on slug** and
carries the supplied id as a display label. The full correction map is in
`problems.json` under `id_corrections`.

## Regenerating the catalogue

```bash
python3 tools/make_catalogue.py     # after editing tools/catalogue_*.py
python3 tools/scaffold.py <slug>    # stub out a new statement + Ref
```
