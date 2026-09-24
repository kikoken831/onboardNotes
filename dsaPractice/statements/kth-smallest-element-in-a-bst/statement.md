# Kth Smallest Element in a BST

LeetCode 230 · Medium · https://leetcode.com/problems/kth-smallest-element-in-a-bst/

## Brief

Return the `k`-th smallest value (1-indexed) in a BST. the standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `1 <= k <= n <= 10^4`

## Examples

```
[3,1,4,null,2], k=1            ->  1
[5,3,6,2,4,null,null,1], k=3   ->  3
```

## Signature

```java
public int kthSmallest(TreeNode root, int k)
```

## Target complexity

O(h + k) — stop as soon as you've seen `k` values.

## The skill

**Ramp:** Tier 1 `p-tree-inorder-iter`. A BST's inorder traversal is sorted, so the answer is
the `k`-th node it visits. The iterative version stops early; collecting the whole traversal
into a list is O(n) and correct. Follow-up (frequent updates): store subtree sizes in nodes.

## Hints

<details><summary>Hint 1</summary>

Decrement `k` on each visit; return when it reaches 0.

</details>
