# Offline DSA Judge + Ramp-Up Ladder — Design

**Date:** 2026-08-20
**Status:** awaiting review
**Location:** `dsaPractice/`
**Language:** Java only (JDK 21). Orchestration in Python 3 (stdlib only, matching `tools/mcq_*.py`).

---

## 1. Goal

An offline judge for a supplied 179-problem LeetCode list, plus a **ramp-up ladder below that
list's floor**, for an engineer who is fluent in Java and production backend work but weak at
algorithmic problem solving. Target: a bank DSA screen (BofA GMT Post Trade — see
`README.md` for the wider prep pack this joins).

Everything runs locally with no network, no login, no Maven/Gradle. Consistent with the
repo's standing convention: *everything here compiles and runs, and quoted numbers are measured.*

## 2. Non-goals

- Not a LeetCode scraper. No network calls at any point.
- Not multi-language. Java only, by explicit instruction.
- Not a replacement for reading real problem statements when stuck — every problem keeps
  its leetcode.com URL.
- Not a spaced-repetition app. Scheduling is a field in a JSON file, not a system.

---

## 3. Decisions taken

| # | Decision | Chosen |
|---|---|---|
| D1 | How expected answers are established | **Hidden reference solution + randomised differential testing** |
| D2 | Problem statements | **Full statement offline**, authored per problem |
| D3 | Build sequencing | **Ramp first** (Tiers 0–2 complete), then the 179 in batches of 20 |
| D4 | Ramp composition | **Two rungs** — authored primitives, then real LC Easy below the set's floor |
| D5 | Solution invocation | LC-faithful `class Solution` + exact signature, dispatched by reflection |
| D6 | Spec format | Java code, not JSON (Java stdlib has no JSON parser; generators must be code) |

### Assumptions where no answer was given

- **A1: `judge time` (complexity growth curve) is IN.** Justified by the prep pack's own note
  that the HireVue round requires narrating time complexity aloud. Cheap to build. Cut on request.
- **A2: stub carries a compact header** (restatement, constraints, 2 examples, target complexity,
  signature); `judge show <slug>` prints the full `statement.md` including hints. Keeps the file
  you type in workable while honouring D2. Cut on request.

---

## 4. Research findings that drove the design

Two research passes were run. **`WebSearch` was unavailable and `WebFetch` was blocked for all
non-GitHub hosts.** Evidence is tiered accordingly and the tiers are load-bearing — do not
promote a tier when quoting this document.

| Tier | Meaning |
|---|---|
| **MEASURED** | Run locally on JDK 21.0.11 and independently re-verified. Trustworthy. |
| **FETCHED** | Pulled from a real GitHub artifact. For Grind 75 / NeetCode this *is* the primary source. |
| **RECALLED** | Model knowledge; citation could not be opened. A lead, not a fact. |
| **FOLKLORE** | Practitioner consensus, no study behind it. |

### F1 — "Easy" is at least three rungs [FETCHED]

LeetCode Easy spans **17.5% to 91% acceptance**. Source: `zubyj/leetcode-explained`
→ `src/assets/data/problem_data.json`, a cached scrape of LeetCode's own numbers, roughly
2022–23 vintage. **Use for relative ranking only, not as current absolute values** — rates drift
upward as solutions proliferate (Two Sum reads 50.4% there vs ~56% today).

Consequence: failing "Easy" may be a sampling problem, not a capability problem. This is the
single strongest objective justification for the ramp, and it is independent of any pedagogy claim.

### F2 — Five patterns have no Easy entry point anywhere [FETCHED]

Backtracking, Trie, Topological Sort, Union-Find, and LIS/LCS/Knapsack DP start at **Medium** on
LeetCode. There is no easier real problem to drop down to. **For these, authored primitives are
not a preference, they are the only option** — this is what makes D4 necessary rather than nice.

### F3 — Curated beginner lists contain traps [FETCHED]

- LC **20 Valid Parentheses (40.2%)** is the hardest Easy stack problem, and is everyone's first
  stack problem — including this set's floor.
- LC **125 Valid Palindrome (45.2%)** is harder than 344 (77.1%) and 977 (71.8%).
- LeetCode's own **Arrays 101** card contains 941 (33.3%), 414 (33.7%), 1346 (36.7%) — three of
  the lowest-acceptance Easy problems on the site.
- LeetCode **75** has the gentlest opening of any list; **Top Interview 150** reaches Trapping Rain
  Water (Hard) inside its first section and is actively wrong for this candidate.
- **Grind 75** front-loads 24 Easy across 10 topics before any Medium; **NeetCode 150** hits Medium
  in topic 1. For someone who can't reliably clear Easy, global-difficulty ordering beats
  depth-first-by-topic.

### F4 — Consensus is interleave, not gate [FETCHED]

Both curriculum authors publish an explicit pre-rung. NeetCode's `howToUseNeetcode.md` (updated
2026-07-03) defines prerequisites *below* the problem lists and points at a "implement common data
structures from scratch" practice set. `yangshun/tech-interview-handbook` says plainly: "if you are
unfamiliar with core data structures and algorithms knowledge, you are advised to revise them
before starting." But both also say **don't gate** — learn/implement one structure, immediately do
2–3 problems on it, move on. Note both sources have a commercial interest in "you need
fundamentals first"; that is the expected direction of bias.

### F5 — Far transfer is rare, so pattern selection must be drilled separately [RECALLED]

Detterman (1993); Barnett & Ceci (2002). Drilling "reverse a sublist with a dummy head" transfers
to LC 92 (near transfer). It will **not** teach recognition of "this problem wants a monotonic
deque" (far transfer). **Citations unverified.** Design consequence is adopted anyway because it
is cheap and the failure mode it guards against is real: a ladder of primitives that tops out at
primitives.

### F6 — The best-evidenced levers are spacing and retrieval, not primitive drilling [RECALLED]

Roediger & Karpicke (2006) testing effect; Cepeda et al. (2006) distributed-practice meta-analysis.
Also relevant and *against* over-claiming: Macnamara, Hambrick & Oswald (2014) found deliberate
practice explains ~4% of variance in education and <1% in professions. **Citations unverified.**

Stated plainly: **"drill primitives and they compose into problem-solving" is FOLKLORE.** The
ladder is better-justified than "grind 300 problems" *for a mechanics gap specifically*, but
composition and pattern-selection need their own explicit slots or it will not generalise.

### F7 — Java mechanical traps [MEASURED, independently re-verified]

All confirmed on JDK 21.0.11 (AppleJDK, arm64):

| Trap | Measured |
|---|---|
| `ArrayDeque.add`×3 then `pop()` | **`1`** — FIFO. Silent wrong answer when used as a stack |
| `ArrayDeque.push`×3 then `pop()` | `3`. `ArrayDeque` iterates `[3,2,1]`, `java.util.Stack` iterates `[1,2,3]` (bottom-first) |
| `(a,b) -> a - b` on `(MIN_VALUE, 1)` | **`+2147483647`** — wrong sign. `Integer.compare` → `-1` |
| `-7 % 3` | **`-1`** (Python gives `2`). `Math.floorMod` → `2`. Top bug source when porting Python |
| `(lo+hi)/2` at `lo=MAX-3, hi=MAX-1` | **`-3`**. `lo+(hi-lo)/2` → `2147483645` |
| `"a.b.c".split(".")` | **length 0** — `split` takes a regex |
| `Arrays.asList(int[]).size()` | **`1`** — it's a `List<int[]>` |
| `Set<int[]>` with two equal pairs | size **`2`** — identity hash |
| `List<Integer>.remove(1)` | removes **index** 1 → `[10,30]` |
| `Integer 128 == 128` | **`false`** (cache is −128..127) |
| `s += "x"` × 40 000 vs `StringBuilder` | 60 ms vs 1 ms (~60×) |
| `substring(1)` × 1 000 000 on a 2 MB string | **38 193 ms** — the recursive string-peeling idiom is O(n²) |
| Default recursion depth | `StackOverflowError` at **~45 817** trivial frames |
| `Arrays.sort(int[], cmp)` | does not compile — no comparator overload for primitives |

**A commonly repeated claim that did NOT reproduce:** the TimSort
`"Comparison method violates its general contract!"` exception. An antisymmetry-violating
comparator `(u,v) -> 1` on `Integer[]` threw nothing at n = 40, 100, 1 000, or 100 000 on JDK 21.
**Do not put "inconsistent comparator throws IAE" in the prep pack as stated.**

**Also FETCHED — the reference material is itself wrong.** `emanuele-em/java-cheatsheet` (61★,
derived from a LeetCode discuss study guide) documents `stack.add(num)` + `stack.pop()` under the
heading "ArrayDeque implementation (stack)" — that is a FIFO queue. It also ships three
non-compiling snippets. `kal038/java-dsa-cheatsheet` declares
`PriorityQueue<Integer>` with an `int[]` comparator. Both are widely circulated. Worth using as a
find-the-bug exercise, per Tier 0.

Useful mnemonic worth keeping, from the latter repo: **Queue = OPP (`offer`/`poll`/`peek`),
Stack = PPP (`push`/`pop`/`peek`). Never mix families on one `Deque`.**

---

## 5. Where the supplied 179-problem set has a cliff

Cross-referencing the set's per-pattern floor against F1/F2's easiest-entry data:

| Pattern | Set's floor | Rung below |
|---|---|---|
| Grid DFS/BFS | 200 Islands (Med, 57.6) | 463 Island Perimeter (69.8), 733 Flood Fill (62.8) |
| Graph, adjacency list | 133 Clone Graph (Med) | 1971 Path Exists (51.6), 997 Town Judge (49.3) |
| Greedy | 55 Jump Game (Med, 38.7) | 455 Assign Cookies, 860 Lemonade Change |
| Heap / PQ | 373 (Med) | 1046 Last Stone Weight (65.3), 703 Kth Largest in Stream |
| Monotonic Stack | 42 Trapping Rain Water (Hard) | 496 Next Greater Element I, 739 Daily Temperatures |
| Matrix | 2022 Convert 1D→2D | 832 Flipping an Image (81.1), 867 Transpose (65.3), 566 Reshape |
| Stack | 20 Valid Parens (**40.2 — a trap**) | 682 Baseball Game (74.9), 232 Queue via Stacks (63.8) |
| Two Pointers | 125 Valid Palindrome (45.2) | 344 Reverse String (77.1), 977 Squares (71.8) |
| 1-D DP | 70 Climbing Stairs (52.2) | 509 Fibonacci (70.2), 1137 Tribonacci, 746 Min Cost Climbing |
| Sliding Window | 643 Max Avg Subarray I (43.4) | none Easy — 1343 (Med, 68.0) is objectively easier |
| **Backtracking** | 77/78 (Med) | ⚠️ **none exists — authored only** |
| **Trie** | 208 (Med) | ⚠️ **none exists — authored only** |
| **Topological Sort** | 207 (Med, 46.1) | ⚠️ **none exists — authored only** |
| **Union-Find** | no primary entry | ⚠️ **none exists — authored only** |
| **LIS / LCS / Knapsack** | 300 / 1143 / 416 (Med) | ⚠️ **none exists — authored only** |

### Data-quality note on the supplied list

Ids are LeetCode-*ish* but a number have drifted; slugs are all correct. **The judge keys on slug
throughout** and carries the supplied id as a display label only. Known drift, to be recorded in
`problems.json` as a correction map:

`792`→704 binary-search · `1250`→1143 longest-common-subsequence · `1019`→977 squares-of-a-sorted-array ·
`940`→904 fruit-into-baskets · `908`→876 middle-of-the-linked-list · `931`→895 maximum-frequency-stack ·
`1014`→973 k-closest-points-to-origin · `855`→828 count-unique-characters · `882`→852 peak-index-in-a-mountain-array ·
`778`→767 reorganize-string · `874`→844 backspace-string-compare · `893`→863 all-nodes-distance-k ·
`800`→784 letter-case-permutation · `1028`→986 interval-list-intersections · `1075`→1065 index-pairs-of-a-string ·
`2132`→2022 convert-1d-array-into-2d-array · `761`→759 employee-free-time · `745`/`746` swapped (744 / 745).

13 of the 179 are LeetCode Premium: 252, 253, 254, 261, 269, 271, 320, 323, 358, 425, 642, 761,
1075. Irrelevant offline — statements are authored either way — but flagged in metadata so you know
discussion threads may be inaccessible.

---

## 6. Architecture

### 6.1 Layout

```
dsaPractice/
  judge                            # executable entry point (python3)
  problems.json                    # supplied list, verbatim + id-correction map + tier tags
  tools/judge/*.py                 # CLI: list, open, show, run, next, status, verify, time, classify
  harness/                         # the Java judge
    Judge.java                     # reflection dispatch, verdicts, timing, shrinking
    Codec.java                     # LC literal notation <-> Java values
    Cmp.java                       # comparison modes
    Gen.java                       # seeded generators
    Spec.java  Problem.java        # spec API
    ListNode.java  TreeNode.java   # shared types
  statements/<slug>/statement.md   # statement, constraints, examples, hints, pattern tags
  drills/<slug>/statement.md       # Tier 1 authored primitives, same shape
  solutions/<slug>/Solution.java   # YOURS - generated on demand by `judge open`
  .ref/<slug>/Ref.java             # reference solution + spec (one file)
  .progress.json                   # attempts, verdicts, spaced-retrieval schedule
  00-java-gotcha-harness/          # Tier 0 - G.java, H.java (already present, to be folded in)
```

### 6.2 Spec as Java code (D6)

Java's stdlib has no JSON parser, and generators must be code — two-sum's generator has to
*plant* a valid answer, linked-list-cycle has to build a real cycle, tree problems must emit valid
trees. So one file holds both spec and reference:

```java
class Ref implements Problem {
  public Spec spec() {
    return Spec.method("twoSum", INT_ARRAY, INT).returns(INT_ARRAY)
        .compare(Cmp.EXACT)
        .example("[2,7,11,15], 9", "[0,1]")     // pinned - guards against MY bugs
        .example("[3,2,4], 6",     "[1,2]")
        .gen(g -> g.twoSumInput(2, 200, -1000, 1000))
        .patterns("Array", "Hash Table")        // used by `judge classify`
        .limitMs(2000);
  }
  public int[] twoSum(int[] nums, int target) { /* reference */ }
}
```

### 6.3 Comparison modes

"Correct" is not one thing. Six modes plus a tolerance variant:

| Mode | Semantics | Used by |
|---|---|---|
| `EXACT` | deep equality | scalars, order-significant arrays |
| `SET_OF_LISTS` | canonicalise inner, then outer | 3sum, subsets, permutations, n-queens, generate-parentheses, combination-sum, palindrome-partitioning, path-sum-ii |
| `VALIDATE` | spec-supplied predicate over (input, yours, ref) | any-valid-answer: find-peak, alien-dictionary, course-schedule-ii, reorganize-string, top-k-frequent, k-closest-points, longest-palindromic-substring, minimum-window-substring, sudoku-solver |
| `IN_PLACE` | judge the mutated argument, assert `void` | rotate-image, sort-colors, move-zeroes, set-matrix-zeroes, rotate-array, reorder-list |
| `ROUND_TRIP` | `decode(encode(x)) == x`; format is yours | encode-and-decode-strings, serialize-and-deserialize-binary-tree |
| `OP_SEQUENCE` | replay random op sequences against both instances | the 7 stateful design problems: implement-trie, design-add-and-search-words, find-median-from-data-stream, range-sum-query-immutable, design-search-autocomplete-system, prefix-and-suffix-search, maximum-frequency-stack |
| `EPSILON` | 1e-5 | median-of-two-sorted-arrays, average-of-levels, maximum-average-subarray-i |

**Inputs are deep-copied per invocation** so your mutations and the reference's cannot interfere.
This is required for `IN_PLACE` and is a silent-wrong-answer source if omitted.

`LINKED_LIST` and `TREE` comparison are structural. Cycle problems (141, 142) need care: 142
returns a *node*, compared by position index, and printing must be cycle-safe or the judge hangs.

### 6.4 Classloading

Separate `URLClassLoader` per side, parent-first for `ListNode`/`TreeNode` so both sides share
those types. Without this, a helper class you happen to name `Node` can silently bind to the
reference's `Node`. Compile to `build/ref/<slug>/` and `build/sol/<slug>/` separately, with mtime
caching to skip unchanged recompiles.

### 6.5 Guarding against reference bugs

179 hand-written references is a large surface for the author's own mistakes. Three safeguards:

1. Every problem pins its LeetCode example outputs. If the reference disagrees with a pinned
   example, the verdict is **`REFERENCE BUG`**, never `WRONG ANSWER`.
2. `judge verify` runs every reference against every pinned example across the whole set.
3. **Honest limitation, stated in the README:** differential testing detects *disagreement* only.
   If the reference is wrong the same way the solution is, both pass. Pinned examples are the
   only defence, so subtle problems get more of them.

### 6.6 Failure output

On disagreement, shrink the failing input (halve arrays, drop elements, shrink magnitudes) while
it still fails, then print the minimal case with both outputs. This is the main thing an offline
judge can do that the website cannot.

---

## 7. The ladder

### Tier 0 — Java mechanics inoculation (~20 items)

Predict-the-output, then run. Not algorithms. Sourced entirely from F7's measured table, folding
in the existing `00-java-gotcha-harness/{G,H}.java`. Includes one find-the-bug exercise against
the real, wrong `ArrayDeque`-as-stack cheatsheet snippet.

### Tier 1 — authored primitives (69 drills)

One mechanical sub-skill each, 5–15 lines, judged by the same harness. Mandatory for the five ⚠️
patterns from §5. Recursion drills **pass an index, never `substring(1)`** — see the 38 s
measurement in F7.

| Group | Drills |
|---|---|
| A. Write-index / in-place (8) | `removeValue` · `dedupeSorted` · `moveZerosKeepOrder` · `partitionEvenOdd` · `reverseInPlace` · `rotateLeftK` (reverse×3) · `dutchFlag3Way` · `replaceAndRemove` (length changes) |
| B. Hash counting (5) | `charFrequency` → `int[26]` · `firstUniqueChar` · `anagramByCounts` · `groupByKey` (`computeIfAbsent`) · `twoSumSeenMap` |
| C. Binary search, four templates (6) | `bsExact` · `bsFirstGE` · `bsFirstGT` · `bsPredicateFlip` · `bsCountOccurrences` · `bsOnAnswer` |
| D. Linked-list rewiring (8) | `buildAndTraverse` · `reverseIterative` · `reverseWithPrev` · `deleteByValueDummyHead` · `deleteKthFromEnd` · `mergeTwoSorted` · `reverseSublist(m,n)` · `partitionAroundValue` |
| E. Recursion shapes (7) | `sumReturnUp` · `depthThreadDown` · `treeHeight` · `rootToLeafPaths` (thread + backtrack) · `bothDirections` · `countMatching` · `isBalanced` (height-or-sentinel) |
| F. Iterative traversal (4) | `preorderIter` · `inorderIter` (left-spine) · `postorderIter` (`lastVisited`) · `levelOrderBoundary` (`int sz = q.size()` snapshot) |
| G. Grid search (4) | `gridDfsRegionSize` · `gridBfsShortestPath` · `markVisitedAtEnqueue` (reproduce the quadratic bug, then fix) · `floodFill4Dir` |
| H. Backtracking ⚠️ (6) | `allBinaryStrings` · `subsetsIncludeExclude` · `subsetSumToTarget` · `subsetsNoAdjacent` · `permutationsBySwap` · `combinationsChooseK` (copy-at-leaf) |
| I. Trie ⚠️ (3) | `trieInsertSearch` (26-child array) · `triePrefixCount` · `trieCollectWithPrefix` |
| J. Union-Find ⚠️ (3) | `dsuNaive` · `dsuPathCompressionUnionBySize` · `dsuCountComponents` |
| K. Topological sort ⚠️ (2) | `kahnIndegree` · `dfsPostorderTopoWithCycleDetect` |
| L. Monotonic stack (3) | `nextGreaterRight` (push the **index**) · `prevSmallerLeft` · `monoStackWithSentinel` |
| M. Prefix sum (3) | `prefixBuildAndQuery` (the `+1` offset) · `countSubarraysSumK` · `prefix2D` |
| N. Heap (3) | `kSmallestBoundedMaxHeap` · `pqComparatorOnIntArray` · `mergeKSortedViaPQ` |
| O. 1-D DP ⚠️ (4) | `fibMemoThenTabulate` · `climbCostVariant` · `knapsack01Boolean` (the 416 primitive) · `lisQuadratic` (the 300 primitive) |

The ladder shape for groups A, D, E, H is modelled on CodingBat's Array-2/Array-3/Recursion-1/
Recursion-2 progressions and EPIJudge's bootcamp sequence [both FETCHED] — specifically
CodingBat's `groupSum` → `groupSum6` → `groupNoAdj` → `groupSum5` → `groupSumClump` include/exclude
ladder, which is the best pre-backtracking sequence found and a direct precursor to LC 78/39/416.

### Tier 2 — real LC Easy below the set's floor (58 problems)

Selected **by acceptance rate, not reputation**. Explicitly excludes the F3 traps (605, 941, 414,
1346, 168). Indicative selection, by pattern:

- **Array / two-pointer:** 1929, 1920, 1470, 344, 26, 27, 88, 905, 1431
- **Hash / counting:** 771, 1512, 349, 383, 387, 1365
- **String:** 1768, 409, 1678, 2114
- **Prefix sum:** 1480, 724
- **Binary search:** 35, 374, 441, 69, 367
- **Stack / queue:** 682, 232, 225, 1047, 933
- **Tree:** 94, 144, 145, 543, 110, 101, 700, 938
- **Grid / graph:** 463, 733, 1971, 997
- **Greedy:** 455, 860
- **Heap:** 1046, 703
- **Matrix:** 832, 867, 566, 1672
- **Design:** 1603, 705, 706
- **1-D DP:** 509, 1137, 746
- **Bit:** 231

### Tier 3 — the supplied 179

Unchanged in scope. Batched 20 at a time after Tiers 0–2 are complete (D3).

### Cross-tier: `judge classify` — pattern selection as its own drill

Per F5, primitives will not produce pattern recognition. The judge shows a cold statement; you
have 90 seconds to give **pattern name + the invariant in one sentence + the data structure**. No
code. Graded against `Spec.patterns(...)`. Pattern vocabulary taken from the supplied list's own
`pattern` field, which is already a clean label set.

---

## 8. CLI

```bash
./judge list --tier 1 --group binary-search --todo
./judge list --pattern sliding-window --difficulty medium
./judge list --company goldman-sachs        # supplied JSON carries frequency data
./judge open  two-sum                       # generate stub, print path
./judge show  two-sum                       # full statement + hints
./judge run   two-sum -n 2000 --seed 42     # fixed cases, then randomised stress
./judge next                                # due re-solves first, then next unsolved
./judge status                              # progress by tier / pattern / difficulty
./judge verify                              # every reference vs every pinned example
./judge time  two-sum                       # complexity growth curve (A1)
./judge classify --n 10                     # pattern-recognition drill
```

## 9. Progress model

`.progress.json`, keyed by slug: attempt count, verdict history, best runtime, first-solved date,
and a spaced-retrieval schedule at **day 1 / 3 / 7 / 21** for re-solving *from an empty file*.
`judge next` surfaces due re-solves ahead of new problems. Per F6 this is the best-evidenced
component in the design, so it is not optional.

`judge open` on an already-solved problem archives the previous attempt to
`solutions/<slug>/.attempts/` rather than overwriting it, so a re-solve starts blank without
losing history.

---

## 10. Risks and honest limitations

| Risk | Mitigation |
|---|---|
| Reference solutions contain bugs | Pinned examples + `REFERENCE BUG` verdict + `judge verify` (§6.5) |
| Differential testing misses shared misconceptions | Stated in README; more pinned examples on subtle problems |
| Acceptance-rate data is a 2022–23 snapshot | Used for **relative ranking only**; never quoted as current |
| Pedagogy citations unverified | Tiered as RECALLED throughout; design choices justified independently where possible |
| Authored statements are paraphrases | Every problem keeps its leetcode.com URL for the canonical text |
| ~69 primitives + 58 Easy + 179 is a large build | D3 sequencing: ramp complete and usable before Tier 3 starts |
| Primitives may not compose into problem-solving (FOLKLORE) | `judge classify` + spaced retrieval are the explicit countermeasures |

## 11. Open items

- A1 (`judge time`) and A2 (compact stub header) are assumptions, not decisions. Confirm or cut.
- `00-java-gotcha-harness/{G,H}.java` are currently untracked, authored by a research agent and
  verified by re-running. Proposal: fold into Tier 0. Confirm or delete.
- Tier 2's problem selection is indicative. It will be finalised against the acceptance-rate data
  at build time, with anything below ~50% acceptance excluded and the exclusion logged.
