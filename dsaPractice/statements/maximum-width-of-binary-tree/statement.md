# Maximum Width of Binary Tree

LeetCode 662 · Medium · https://leetcode.com/problems/maximum-width-of-binary-tree/

## Brief

A level's width is the distance between its leftmost and rightmost non-null nodes, **counting
the null positions between them** as if the tree were complete. Return the maximum width. The
answer fits in 32 bits. The standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `1 <= nodes <= 3000`

## Examples

```
[1,3,2,5,3,null,9]              ->  4
[1,3,2,5,null,null,9,6,null,7]  ->  7
```

## Signature

```java
public int widthOfBinaryTree(TreeNode root)
```

## Target complexity

O(n).

## The skill

Number nodes heap-style — children of `i` are `2i` and `2i + 1` — and a level's width is
`last − first + 1`. The trap: positions double every level, so a deep tree overflows even a
`long`. **Re-base each level** (subtract the level's first position) and the numbers stay
small.

## Hints

<details><summary>Hint 1</summary>

BFS carrying `(node, position)` pairs.

</details>
