# Merge Two Binary Trees

LeetCode 617 · Easy · https://leetcode.com/problems/merge-two-binary-trees/

## Brief

Overlay the trees: where both have a node, the merged node's value is the sum; otherwise use
whichever node exists. the standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `0 <= nodes <= 2000` each

## Examples

```
[1,3,2,5], [2,1,3,null,4,null,7]  ->  [3,4,5,5,4,null,7]
```

## Signature

```java
public TreeNode mergeTrees(TreeNode root1, TreeNode root2)
```

## Target complexity

O(min(m, n)) — when one side runs out, reuse the other subtree whole.

## The skill

Two-tree recursion where a **null short-circuits**: return the other tree as-is rather than
walking it. Whether you build new nodes or reuse `root1` in place is your choice here.

## Hints

<details><summary>Hint 1</summary>

`if (a == null) return b; if (b == null) return a;`

</details>
