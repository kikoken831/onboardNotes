# Validate Binary Search Tree

LeetCode 98 · Medium · https://leetcode.com/problems/validate-binary-search-tree/

## Brief

Is this a valid BST? Every value in a node's left subtree must be **strictly less**, and
every value in its right subtree **strictly greater** — the whole subtree, not just the
child. The standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `1 <= nodes <= 10^4`, `-2^31 <= val <= 2^31 - 1`

## Examples

```
[2,1,3]                ->  true
[5,1,4,null,null,3,6]  ->  false
[5,4,6,null,null,3,7]  ->  false    3 sits in 5's right subtree
```

## Signature

```java
public boolean isValidBST(TreeNode root)
```

## Target complexity

O(n).

## The skill

Pass an **allowed range** down: left children tighten the upper bound, right children the
lower. Checking only `left.val < node.val < right.val` passes the first example and fails
the third. Two more traps, both pinned: duplicates are invalid, and the bounds must be
`long` (or nullable), because `Integer.MAX_VALUE` is a legal value. An inorder traversal
that must be strictly increasing is the other O(n) answer.

## Hints

<details><summary>Hint 1</summary>

`ok(node, lo, hi)` with `lo < node.val < hi`.

</details>
