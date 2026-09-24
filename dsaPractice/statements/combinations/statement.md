# Combinations

LeetCode 77 · Medium · https://leetcode.com/problems/combinations/

## Brief

Return every combination of `k` numbers chosen from `1..n`, in any order.

## Constraints

- `1 <= k <= n <= 20`

## Examples

```
n=4, k=2  ->  [[1,2],[1,3],[1,4],[2,3],[2,4],[3,4]]
```

## Signature

```java
public List<List<Integer>> combine(int n, int k)
```

## Target complexity

O(C(n, k) · k).

## The skill

**Ramp:** Tier 1 `p-bt-combinations`. Choose the next number only from **above** the last one — that ordering is what makes each set appear once. The prune `v <= n − (k − size) + 1` stops exploring branches that can't collect enough numbers.

## Hints

<details><summary>Hint 1</summary>

Loop `v` from `start`, recurse with `v + 1`.

</details>
