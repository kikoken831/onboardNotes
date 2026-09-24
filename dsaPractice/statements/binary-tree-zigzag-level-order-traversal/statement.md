# Binary Tree Zigzag Level Order Traversal

LeetCode 103 · Medium · https://leetcode.com/problems/binary-tree-zigzag-level-order-traversal/

## Brief

Level order, but alternate direction: left-to-right, then right-to-left, and so on. the standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `0 <= nodes <= 2000`

## Examples

```
[3,9,20,null,null,15,7]  ->  [[3],[20,9],[15,7]]
```

## Signature

```java
public List<List<Integer>> zigzagLevelOrder(TreeNode root)
```

## Target complexity

O(n).

## The skill

Keep the traversal itself normal and reverse only the **output** of alternate levels (add to
the front of the level list). Swapping the order in which you *enqueue* children is the
tempting alternative and gets deeper levels wrong — `[1,2,3,4,null,null,5]` is pinned for it.

## Hints

<details><summary>Hint 1</summary>

`LinkedList.addFirst` on odd levels.

</details>
