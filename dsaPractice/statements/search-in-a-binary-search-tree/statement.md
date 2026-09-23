# Search in a Binary Search Tree

LeetCode 700 · Easy · https://leetcode.com/problems/search-in-a-binary-search-tree/

## Brief

Find the node whose value equals `val` and return the **subtree rooted there**, or `null`
if there is none.

## Constraints

- `1 <= number of nodes <= 5000`
- a valid BST with distinct values
- `1 <= val <= 10^7`

## Examples

```
[4,2,7,1,3], val=2  ->  [2,1,3]
[4,2,7,1,3], val=5  ->  []
```

## Signature

```java
public TreeNode searchBST(TreeNode root, int val)
```

## Target complexity

O(h) — not O(n). Don't search both sides.

## The skill

Using the BST invariant to discard half the tree at each step. The iterative loop is two
lines; a full-tree DFS gives the right answer and misses the point, and the judge's timing
mode will show the difference on a deep tree.

## Hints

<details><summary>Hint 1</summary>

`while (n != null && n.val != val) n = val < n.val ? n.left : n.right;`

</details>
