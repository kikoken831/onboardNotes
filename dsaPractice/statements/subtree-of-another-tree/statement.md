# Subtree of Another Tree

LeetCode 572 · Easy · https://leetcode.com/problems/subtree-of-another-tree/

## Brief

Is `subRoot` identical to some node of `root` **together with all of that node's
descendants**? The standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `1 <= nodes(root) <= 2000`, `1 <= nodes(subRoot) <= 1000`

## Examples

```
[3,4,5,1,2], [4,1,2]                           ->  true
[3,4,5,1,2,null,null,null,null,0], [4,1,2]     ->  false    4 has an extra descendant
```

## Signature

```java
public boolean isSubtree(TreeNode root, TreeNode subRoot)
```

## Target complexity

O(m · n) with Same Tree at each node; O(m + n) with serialisation + string matching.

## The skill

LC 100 applied at every node. The faster route serialises both trees and searches for one
string in the other — but only with **null markers and value delimiters**: without a leading
separator, `[12]` "contains" `[2]`. The judge pins that case.

## Hints

<details><summary>Hint 1</summary>

`same(root, sub) || isSubtree(root.left, sub) || isSubtree(root.right, sub)`

</details>
