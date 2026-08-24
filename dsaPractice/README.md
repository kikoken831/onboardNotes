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
```

---

## The tiers

The target list has a **floor problem**: for a third of its patterns the easiest
problem in it is already Medium. LeetCode "Easy" also spans a 5× acceptance-rate range
(17.5%–91%), so "I can't do Easy" is often a sampling problem rather than a capability
one. Hence four tiers:

| Tier | What | Count |
|---|---|---|
| **0** | Java mechanics — measured JVM traps, predict-then-run | 20 items |
| **1** | Authored primitives — one mechanical sub-skill each, 5–15 lines | 69 drills |
| **2** | Real LeetCode Easy below the target list's floor | 58 problems |
| **3** | The supplied target list | 179 problems |

Tier 1 is mandatory rather than optional for five patterns — **backtracking, trie,
topological sort, union-find, and LIS/knapsack DP have no Easy entry point on LeetCode
at all**. There is nothing to warm up on, so the warm-up had to be written.

Start here:

```bash
cat drills/00-java-mechanics/README.md    # tier 0, read it once
./judge next --tier 1
```

---

## Spaced re-solving

Solving something once teaches you less than re-deriving it later from an empty file.
When you get an `ACCEPTED`, the problem is scheduled for a blank-file re-solve at
**day 1, 3, 7 and 21**. A failed re-solve resets it to day 1.

`judge next` surfaces due re-solves ahead of new problems. `judge open <slug> --reset`
archives your previous attempt to `.attempts/` and gives you an empty file.

This is the best-evidenced component in the design — the testing effect and distributed
practice are among the more robust findings in learning science, considerably better
supported than "drilling primitives composes into problem-solving", which is largely
folklore.

## Pattern recognition

Drilling mechanics does not teach you to *recognise* which pattern a cold problem
wants; transfer research is fairly clear that this kind of far transfer does not happen
for free. So it gets its own drill — no code, 90 seconds, name the pattern and the
invariant:

```bash
./judge classify -n 10
```

---

## Layout

```
judge                        the CLI
problems.json                catalogue (generated — see tools/make_catalogue.py)
harness/                     the Java judge: Codec, Match, Shrink, Gen, Judge
drills/<slug>/statement.md   tier 0 and 1 statements
statements/<slug>/           tier 2 and 3 statements
solutions/<slug>/            YOUR code
.ref/<slug>/Ref.java         reference solution + test spec  (spoilers)
.progress.json               attempts and the re-solve schedule
```

`.ref/` holds the answers. Looking is your call — but the shrunk counterexample is
almost always more useful than the reference, so try `judge run` first.

## A note on the supplied list's ids

Several ids in the source list have drifted from real LeetCode — `792` is Binary Search
(really 704), `1250` is Longest Common Subsequence (really 1143), `745`/`746` are
swapped, and fourteen others. Every slug is correct, so **the judge keys on slug** and
carries the supplied id as a display label. The full correction map is in
`problems.json` under `id_corrections`.

## Regenerating the catalogue

```bash
python3 tools/make_catalogue.py     # after editing tools/catalogue_*.py
python3 tools/scaffold.py <slug>    # stub out a new statement + Ref
```
