# Lowest Common Ancestor of a Binary Tree

LeetCode 236 · Medium · https://leetcode.com/problems/lowest-common-ancestor-of-a-binary-tree/

## Brief

As LC 235, but for an arbitrary binary tree — no ordering to exploit. `p` and `q` are nodes
of the tree you're given. the standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `2 <= nodes <= 10^5`, distinct values, `p != q`, both present

## Examples

```
[3,5,1,6,2,0,8,null,null,7,4], p=5, q=1  ->  3
[3,5,1,6,2,0,8,null,null,7,4], p=5, q=4  ->  5
```

## Signature

```java
public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q)
```

## Target complexity

O(n).

## The skill

A post-order search that **returns up** "whichever of p/q I found below". If both sides
report something, this node is the split point; otherwise pass up the non-null side. Stopping
at `root == p` is what makes "a node is its own ancestor" work. Compare **nodes** (`==`), not
values.

## Hints

<details><summary>Hint 1</summary>

`return left != null && right != null ? root : (left != null ? left : right);`

</details>
