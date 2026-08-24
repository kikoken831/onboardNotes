# Count occurrences using both bounds

Tier 1 drill · binary-search · unlocks LC 34

## Brief

`nums` is sorted ascending with duplicates. Return how many times `target` occurs.

Must be O(log n) — a linear scan is not accepted.

## Constraints

- `0 <= nums.length <= 10^4`
- sorted ascending

## Examples

```
[5,7,7,8,8,10], 8  ->  2
[5,7,7,8,8,10], 6  ->  0
[2,2,2,2],      2  ->  4
[],             1  ->  0
[1],            1  ->  1
```

## Signature

```java
public int bsCountOccurrences(int[] nums, int target)
```

## Target complexity

O(log n) time, O(1) space.

## The skill

The payoff for the previous two drills: **composition**. The answer is

```
bsFirstGT(nums, target) - bsFirstGE(nums, target)
```

and there is nothing else to it. No special-casing "not present" — if the target is
absent, both bounds land on the same index and the difference is 0, which is correct
for free.

The lesson generalises. Once you have `lower_bound` and `upper_bound` as reliable
building blocks, a whole family of problems becomes subtraction. This is exactly LC 34
(find-first-and-last-position), which is otherwise a fiddly two-sided search that
people write forty lines of nested conditionals for.

## Hints

<details><summary>Hint 1</summary>

Write both helpers as private methods, then return the difference. Do not try to fuse
them into one loop — it is harder and no faster.

</details>

<details><summary>Hint 2 — check the absent case by hand</summary>

For `[5,7,7,8,8,10]` and target 6: `bsFirstGE` returns 1, `bsFirstGT` returns 1,
difference 0. Trace it before you run it.

</details>
