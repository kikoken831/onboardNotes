# Binary Tree Level Order Traversal II

LeetCode 107 · Medium · https://leetcode.com/problems/binary-tree-level-order-traversal-ii/

## Brief

Level order from the **bottom** level up; each level still left to right. The standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `0 <= nodes <= 2000`

## Examples

```
[3,9,20,null,null,15,7]  ->  [[15,7],[9,20],[3]]
```

## Signature

```java
public List<List<Integer>> levelOrderBottom(TreeNode root)
```

## Target complexity

O(n).

## The skill

LC 102 with the **list of levels** reversed — not each level. Prepending levels to a
`LinkedList` (or reversing once at the end) keeps it O(n).

## Hints

<details><summary>Hint 1</summary>

`Collections.reverse(out)` at the end is fine.

</details>
