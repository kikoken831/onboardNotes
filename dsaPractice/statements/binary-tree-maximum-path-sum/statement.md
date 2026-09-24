# Binary Tree Maximum Path Sum

LeetCode 124 · Hard · https://leetcode.com/problems/binary-tree-maximum-path-sum/

## Brief

A path is any sequence of nodes connected by edges, each used at most once; it need not
pass through the root and must contain **at least one** node. Return the maximum path sum.
the standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `1 <= nodes <= 3 * 10^4`, `-1000 <= val <= 1000`

## Examples

```
[1,2,3]                    ->  6
[-10,9,20,null,null,15,7]  ->  42    15 + 20 + 7
[-3]                       ->  -3
```

## Signature

```java
public int maxPathSum(TreeNode root)
```

## Target complexity

O(n).

## The skill

**Ramp:** Tier 1 `p-tree-both-directions` — Diameter (LC 543) with weights. Return upward the
best **one-armed** path, clamped at zero (a negative arm is better left out); record at each
node the best **two-armed** path through it. Initialise the answer to `MIN_VALUE`, not 0: an
all-negative tree's answer is negative (`[-1,-2,-3] → -1`).

## Hints

<details><summary>Hint 1</summary>

`gain(n) = n.val + max(0, gain(left), gain(right))` — only one side can continue upward.

</details>
