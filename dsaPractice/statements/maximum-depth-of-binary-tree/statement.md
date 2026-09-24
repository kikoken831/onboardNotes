# Maximum Depth of Binary Tree

LeetCode 104 · Easy · https://leetcode.com/problems/maximum-depth-of-binary-tree/

## Brief

Return the number of **nodes** on the longest root-to-leaf path. The standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `0 <= nodes <= 10^4`

## Examples

```
[3,9,20,null,null,15,7]  ->  3
[]                       ->  0
```

## Signature

```java
public int maxDepth(TreeNode root)
```

## Target complexity

O(n).

## The skill

**Ramp:** Tier 1 `p-tree-height` — the same function. One line of "return up" recursion; BFS
counting levels is the iterative version.

## Hints

<details><summary>Hint 1</summary>

`1 + max(depth(left), depth(right))`

</details>
