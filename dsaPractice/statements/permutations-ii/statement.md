# Permutations II

LeetCode 47 · Medium · https://leetcode.com/problems/permutations-ii/

## Brief

`nums` may contain **duplicates**. Return every **distinct** permutation, in any order.

## Constraints

- `1 <= nums.length <= 8`

## Examples

```
[1,1,2]  ->  [[1,1,2],[1,2,1],[2,1,1]]
```

## Signature

```java
public List<List<Integer>> permuteUnique(int[] nums)
```

## Target complexity

O(n · n!).

## The skill

Sort, then allow an equal value only if its left twin is **already used** in the current branch: `if (i > 0 && a[i] == a[i-1] && !used[i-1]) continue;`. That forces equal values to be placed in a fixed order, so each distinct arrangement is generated once. A `Set<List<Integer>>` also works and costs the duplicate work.

## Hints

<details><summary>Hint 1</summary>

Why `!used[i-1]` and not `used[i-1]`? Both are correct; trace which prunes earlier.

</details>
