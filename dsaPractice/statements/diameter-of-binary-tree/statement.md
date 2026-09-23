# Diameter of Binary Tree

LeetCode 543 · Easy · https://leetcode.com/problems/diameter-of-binary-tree/

## Brief

The diameter is the number of **edges** on the longest path between any two nodes. The
path **need not pass through the root**.

## Constraints

- `1 <= number of nodes <= 10^4`
- `-100 <= Node.val <= 100`

## Examples

```
[1,2,3,4,5]  ->  3    path 4-2-1-3 or 5-2-1-3
[1,2]        ->  1
```

## Signature

```java
public int diameterOfBinaryTree(TreeNode root)
```

## Target complexity

O(n) — one post-order pass.

## The skill

**Ramp:** Tier 1 `p-tree-both-directions`. The recursion **returns** one thing (height,
upwards) and **records** another (the best path, in a field). Those are different
quantities, and conflating them is the bug.

Two more traps the judge includes: answering `leftHeight(root) + rightHeight(root)` — wrong
when the longest path is inside one subtree — and counting nodes instead of edges.

## Hints

<details><summary>Hint 1</summary>

At each node, the longest path *through it* is `height(left) + height(right)` edges. Track
the maximum over all nodes; return the height.

</details>
