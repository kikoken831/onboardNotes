# Maximum Binary Tree

LeetCode 654 · Medium · https://leetcode.com/problems/maximum-binary-tree/

## Brief

Build the tree recursively: the root is the maximum value; the left subtree is built from the
part of the array to its left, the right subtree from the part to its right. Values are
distinct. The standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `1 <= n <= 1000`, distinct values

## Examples

```
[3,2,1,6,0,5]  ->  [6,3,5,null,2,0,null,null,1]
[3,2,1]        ->  [3,null,2,null,1]
```

## Signature

```java
public TreeNode constructMaximumBinaryTree(int[] nums)
```

## Target complexity

O(n) with a monotonic stack; O(n²) recursively on sorted input.

## The skill

The recursive definition translates directly and is O(n²) worst case. The O(n) version is a
**decreasing monotonic stack** (**ramp:** Tier 1 `p-mono-sentinel`): each new value adopts the
last popped node as its left child and becomes the right child of whatever remains on top.

## Hints

<details><summary>Hint 1</summary>

Write the recursion first: find the max index in `[lo, hi]`, recurse on both sides.

</details>
