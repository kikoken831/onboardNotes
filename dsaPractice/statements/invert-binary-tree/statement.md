# Invert Binary Tree

LeetCode 226 · Easy · https://leetcode.com/problems/invert-binary-tree/

## Brief

Mirror the tree — swap every node's left and right children — and return the root. The standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `0 <= nodes <= 100`

## Examples

```
[4,2,7,1,3,6,9]  ->  [4,7,2,9,6,3,1]
```

## Signature

```java
public TreeNode invertTree(TreeNode root)
```

## Target complexity

O(n).

## The skill

Swap, recurse into both. The one ordering bug: `root.left = invertTree(root.right);
root.right = invertTree(root.left);` — the second line reads the child you've just replaced.
Save one side first.

## Hints

<details><summary>Hint 1</summary>

A BFS that swaps each dequeued node's children works too.

</details>
