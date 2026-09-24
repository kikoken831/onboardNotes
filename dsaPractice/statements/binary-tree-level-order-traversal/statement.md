# Binary Tree Level Order Traversal

LeetCode 102 · Medium · https://leetcode.com/problems/binary-tree-level-order-traversal/

## Brief

Return the node values level by level, left to right. The standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `0 <= nodes <= 2000`

## Examples

```
[3,9,20,null,null,15,7]  ->  [[3],[9,20],[15,7]]
[]                       ->  []
```

## Signature

```java
public List<List<Integer>> levelOrder(TreeNode root)
```

## Target complexity

O(n).

## The skill

**Ramp:** Tier 1 `p-tree-level-order`. BFS with a **size snapshot**: read `q.size()` once
before the inner loop — the queue grows while you drain the current level. Every level
problem below (103, 107, 199, 637, 662) is this loop with a different body.

## Hints

<details><summary>Hint 1</summary>

`for (int n = q.size(); n > 0; n--)`

</details>
