# Path Sum II

LeetCode 113 · Medium · https://leetcode.com/problems/path-sum-ii/

## Brief

Return **every** root-to-leaf path whose values sum to `targetSum`, as lists of values. Paths
in any order. The standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `0 <= nodes <= 5000`

## Examples

```
[5,4,8,11,null,13,4,7,2,null,null,5,1], 22  ->  [[5,4,11,2],[5,8,4,5]]
```

## Signature

```java
public List<List<Integer>> pathSum(TreeNode root, int targetSum)
```

## Target complexity

O(n²) worst case (copying paths).

## The skill

**Ramp:** Tier 1 `p-tree-root-to-leaf`. Backtracking on a tree: add the node, recurse,
**remove it** on the way back, and **copy** the path when you record it. Recording `path`
itself stores one list that ends up empty. Equal-valued paths are distinct answers (`[1,2,2]`).

## Hints

<details><summary>Hint 1</summary>

`out.add(new ArrayList<>(path))`

</details>
