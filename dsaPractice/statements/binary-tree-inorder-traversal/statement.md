# Binary Tree Inorder Traversal

LeetCode 94 · Easy · https://leetcode.com/problems/binary-tree-inorder-traversal/

## Brief

Return the node values in **inorder**: left subtree, node, right subtree.

## Constraints

- `0 <= number of nodes <= 100`
- `-100 <= Node.val <= 100`

## Examples

```
[1,null,2,3]                     ->  [1,3,2]
[1,2,3,4,5,null,8,null,null,6,7,9]  ->  [4,2,6,5,7,1,3,9,8]
[]                               ->  []
```

## Signature

```java
public List<Integer> inorderTraversal(TreeNode root)
```

## Target complexity

O(n) time, O(h) space.

## The skill

**Ramp:** Tier 1 `p-tree-inorder-iter`. The recursive version is three lines; the follow-up
("do it iteratively") is the real exercise — push the **left spine**, pop, visit, then move
to the right child and repeat. Write both.

## Hints

<details><summary>Hint 1 — iterative</summary>

```
cur = root
while cur != null or stack not empty:
    while cur != null: push cur; cur = cur.left
    cur = pop; visit cur; cur = cur.right
```

</details>
