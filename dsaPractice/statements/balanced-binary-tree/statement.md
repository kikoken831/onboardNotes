# Balanced Binary Tree

LeetCode 110 · Easy · https://leetcode.com/problems/balanced-binary-tree/

## Brief

A tree is height-balanced when, **at every node**, the heights of the left and right
subtrees differ by at most one. Return whether `root` is balanced. The empty tree is.

## Constraints

- `0 <= number of nodes <= 5000`
- `-10^4 <= Node.val <= 10^4`

## Examples

```
[3,9,20,null,null,15,7]    ->  true
[1,2,2,3,3,null,null,4,4]  ->  false
[]                         ->  true
```

## Signature

```java
public boolean isBalanced(TreeNode root)
```

## Target complexity

O(n). The top-down version that recomputes heights is O(n log n) on balanced trees and
can degrade towards O(n²) on deep skewed ones. The judge's generated trees are never deep
chains, so timing won't expose that case; aim for the single pass anyway.

## The skill

**Ramp:** Tier 1 `p-tree-is-balanced`. "At **every** node" is the whole trap: checking only
the root passes the first examples and fails `[1,2,2,3,null,null,3,4,null,null,4]`, where the
root's two subtrees have equal height but neither subtree is itself balanced. The judge
includes it.

## Hints

<details><summary>Hint 1</summary>

Return height from the recursion, and use a sentinel (`-1`) to mean "already unbalanced
below here" so you can stop early and stay O(n).

</details>
