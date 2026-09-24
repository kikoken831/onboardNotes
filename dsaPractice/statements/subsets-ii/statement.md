# Subsets II

LeetCode 90 · Medium · https://leetcode.com/problems/subsets-ii/

## Brief

`nums` may contain **duplicates**. Return every **distinct** subset, in any order.

## Constraints

- `1 <= nums.length <= 10`

## Examples

```
[1,2,2]  ->  [[],[1],[1,2],[1,2,2],[2],[2,2]]
```

## Signature

```java
public List<List<Integer>> subsetsWithDup(int[] nums)
```

## Target complexity

O(n · 2ⁿ).

## The skill

LC 78 plus LC 40's de-duplication: sort, record every node, and skip an equal value at the same depth (`j > i && a[j] == a[j-1]`). The bitmask approach needs a `Set` here, because different masks produce equal subsets.

## Hints

<details><summary>Hint 1</summary>

Sorting puts duplicates next to each other; the skip depends on it.

</details>
