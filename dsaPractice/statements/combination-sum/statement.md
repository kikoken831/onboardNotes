# Combination Sum

LeetCode 39 · Medium · https://leetcode.com/problems/combination-sum/

## Brief

Candidates are **distinct** positive integers. Return every unique combination that sums to `target`, where each candidate may be used **any number of times**. Combinations in any order; two combinations are the same if they use the same multiset of numbers.

## Constraints

- `1 <= candidates.length <= 30`, `2 <= candidates[i] <= 40`, `1 <= target <= 40`

## Examples

```
[2,3,6,7], 7  ->  [[2,2,3],[7]]
[2,3,5], 8    ->  [[2,2,2,2],[2,3,3],[3,5]]
```

## Signature

```java
public List<List<Integer>> combinationSum(int[] candidates, int target)
```

## Target complexity

Exponential; prune with sorting.

## The skill

**Ramp:** Tier 1 `p-bt-combinations`. The unlimited-reuse rule is one character: recurse with **`j`**, not `j + 1`. Starting each level at the current index — never earlier — is what stops `[2,3]` and `[3,2]` both appearing. Sort first and you can `break` once a candidate exceeds the remainder.

## Hints

<details><summary>Hint 1</summary>

`go(j, rem − a[j])` — same `j`, because it may be reused.

</details>
