# Construct Binary Tree from Preorder and Inorder Traversal

LeetCode 105 · Medium · https://leetcode.com/problems/construct-binary-tree-from-preorder-and-inorder-traversal/

## Brief

Given the preorder and inorder traversals of a tree with **distinct** values, rebuild it.
the standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `1 <= n <= 3000`, values distinct

## Examples

```
pre=[3,9,20,15,7], in=[9,3,15,20,7]  ->  [3,9,20,null,null,15,7]
```

## Signature

```java
public TreeNode buildTree(int[] preorder, int[] inorder)
```

## Target complexity

O(n) with a value → inorder-index map.

## The skill

The first preorder value is the root; its position in the inorder array splits the rest
into left and right subtrees, and the left subtree's **size** tells you where it ends in
the preorder array. A `HashMap` of inorder positions turns the O(n) search per node into
O(1) — the difference between O(n²) and O(n).

## Hints

<details><summary>Hint 1</summary>

Consume preorder with a single moving index, building left before right.

</details>
