# Range Sum of BST

LeetCode 938 · Easy · https://leetcode.com/problems/range-sum-of-bst/

## Brief

Return the sum of every node value in the **inclusive** range `[low, high]`.

## Constraints

- `1 <= number of nodes <= 2 * 10^4`
- a valid BST with distinct values
- `1 <= low <= high <= 10^5`

## Examples

```
[10,5,15,3,7,null,18], low=7, high=15        ->  32    7 + 10 + 15
[10,5,15,3,7,13,18,1,null,6], low=6, high=10 ->  23    6 + 7 + 10
```

## Signature

```java
public int rangeSumBST(TreeNode root, int low, int high)
```

## Target complexity

O(n) worst case; prune with the BST property to visit only what can matter.

## The skill

**Ramp:** Tier 1 `p-tree-count-matching`. Summing every in-range node is correct. The BST
lesson is **pruning**: if `node.val < low`, nothing in its left subtree can be in range, so
don't go there. Mind the inclusive bounds — `<` vs `<=` is where this goes wrong.

## Hints

<details><summary>Hint 1</summary>

Three cases: below range → recurse right only; above range → left only; inside → count it
and recurse both sides.

</details>
