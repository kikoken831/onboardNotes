# Combination Sum II

LeetCode 40 · Medium · https://leetcode.com/problems/combination-sum-ii/

## Brief

Candidates may contain **duplicates**, and each may be used **at most once**. Return every unique combination summing to `target` — no duplicate combinations.

## Constraints

- `1 <= candidates.length <= 100`, `1 <= candidates[i] <= 50`, `1 <= target <= 30`

## Examples

```
[10,1,2,7,6,1,5], 8  ->  [[1,1,6],[1,2,5],[1,7],[2,6]]
[2,5,2,1,2], 5       ->  [[1,2,2],[5]]
```

## Signature

```java
public List<List<Integer>> combinationSum2(int[] candidates, int target)
```

## Target complexity

Exponential.

## The skill

**Ramp:** Tier 1 `p-bt-include-exclude`. Two changes from LC 39: recurse with `j + 1` (use once), and **skip equal values at the same depth** (`j > i && a[j] == a[j-1]`) after sorting. The skip is `j > i`, not `j > 0` — using `1` twice in `[1,1,6]` happens at *different* depths and is allowed.

## Hints

<details><summary>Hint 1</summary>

Sort first; duplicates must be adjacent for the skip to work.

</details>
