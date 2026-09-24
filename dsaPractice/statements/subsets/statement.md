# Subsets

LeetCode 78 · Medium · https://leetcode.com/problems/subsets/

## Brief

`nums` has **distinct** values. Return every subset (the power set), including `[]`, in any order.

## Constraints

- `1 <= nums.length <= 10`

## Examples

```
[1,2,3]  ->  [[],[1],[2],[1,2],[3],[1,3],[2,3],[1,2,3]]
```

## Signature

```java
public List<List<Integer>> subsets(int[] nums)
```

## Target complexity

O(n · 2ⁿ).

## The skill

**Ramp:** Tier 1 `p-bt-include-exclude`. Three equivalent generators: include/exclude recursion, the "record every node" loop recursion, and **bitmasks** — each `mask` in `0..2ⁿ−1` is one subset. The empty set counts.

## Hints

<details><summary>Hint 1</summary>

`for (mask = 0; mask < 1 << n; mask++)`

</details>
