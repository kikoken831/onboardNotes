# Symmetric Tree

LeetCode 101 · Easy · https://leetcode.com/problems/symmetric-tree/

## Brief

Return whether the tree is a mirror of itself around its centre.

## Constraints

- `1 <= number of nodes <= 1000`
- `-100 <= Node.val <= 100`

## Examples

```
[1,2,2,3,4,4,3]        ->  true
[1,2,2,null,3,null,3]  ->  false
```

## Signature

```java
public boolean isSymmetric(TreeNode root)
```

## Target complexity

O(n).

## The skill

A recursion over **two** nodes at once. The mirror relation pairs `a.left` with `b.right`
and `a.right` with `b.left` — the crossing is the whole problem.

Two tempting wrong answers: comparing each level's values as a palindrome (ignores `null`
positions — `[1,2,2,2,null,2]` fools it), and comparing inorder traversals (ignores shape).
The judge tests both.

## Hints

<details><summary>Hint 1</summary>

`mirror(a, b)`: both null → true; one null → false; otherwise values equal **and**
`mirror(a.left, b.right)` **and** `mirror(a.right, b.left)`.

</details>

<details><summary>Hint 2 — iterative</summary>

A queue of **pairs**: enqueue `(root.left, root.right)`, and for each pair enqueue the two
crossed pairs.

</details>
