# Lowest Common Ancestor of a Binary Search Tree

LeetCode 235 · Medium · https://leetcode.com/problems/lowest-common-ancestor-of-a-binary-search-tree/

## Brief

Given a BST and two of its nodes `p` and `q`, return their lowest common ancestor — the
deepest node that has both as descendants (a node is a descendant of itself). The standard `TreeNode` (`val`, `left`, `right`) is provided.

`p` and `q` are **nodes of the tree you're given**, not copies. Examples list them by value;
the judge reports the value of the node you return.

## Constraints

- `2 <= nodes <= 10^5`, distinct values, `p != q`, both present

## Examples

```
[6,2,8,0,4,7,9,null,null,3,5], p=2, q=8  ->  6
[6,2,8,0,4,7,9,null,null,3,5], p=2, q=4  ->  2     a node is its own ancestor
```

## Signature

```java
public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q)
```

## Target complexity

O(h), O(1) space.

## The skill

Use the ordering: if both values are smaller, go left; both larger, go right; otherwise
(they split here, or one of them **is** here) this is the answer. The general-tree algorithm
(LC 236) also works but ignores what makes this tree special.

## Hints

<details><summary>Hint 1</summary>

The first node whose value lies between `p` and `q` inclusive.

</details>
