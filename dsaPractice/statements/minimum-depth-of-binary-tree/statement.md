# Minimum Depth of Binary Tree

LeetCode 111 · Easy · https://leetcode.com/problems/minimum-depth-of-binary-tree/

## Brief

Return the number of nodes on the shortest path from the root to a **leaf** (a node with no
children). The standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `0 <= nodes <= 10^5`

## Examples

```
[3,9,20,null,null,15,7]                  ->  2
[2,null,3,null,4,null,5,null,6]          ->  5
```

## Signature

```java
public int minDepth(TreeNode root)
```

## Target complexity

O(n); BFS can stop at the first leaf.

## The skill

Not `1 + min(left, right)`: a missing child is **not** a leaf, so `[1,2]` has depth 2, not
1. When one side is null you must take the other. BFS is the better tool here — it stops at
the first leaf it finds instead of exploring the whole tree.

## Hints

<details><summary>Hint 1</summary>

Three cases: no left, no right, both.

</details>
