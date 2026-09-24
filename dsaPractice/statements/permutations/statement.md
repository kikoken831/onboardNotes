# Permutations

LeetCode 46 · Medium · https://leetcode.com/problems/permutations/

## Brief

`nums` has **distinct** values. Return every permutation, in any order.

## Constraints

- `1 <= nums.length <= 6`

## Examples

```
[1,2,3]  ->  [[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]
```

## Signature

```java
public List<List<Integer>> permute(int[] nums)
```

## Target complexity

O(n · n!).

## The skill

**Ramp:** Tier 1 `p-bt-permutations-swap`. Two templates: a `used[]` array (each level picks any unused element), or in-place swapping (swap `i` into position `k`, recurse, swap back). Order matters within a permutation, so the judge compares a set of sequences.

## Hints

<details><summary>Hint 1</summary>

Record a **copy** when the current list reaches length `n`.

</details>
