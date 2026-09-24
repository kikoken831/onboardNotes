# Binary Tree Right Side View

LeetCode 199 · Medium · https://leetcode.com/problems/binary-tree-right-side-view/

## Brief

Standing to the right of the tree, return the values you can see, top to bottom — the
**last** node of each level. The standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `0 <= nodes <= 100`

## Examples

```
[1,2,3,null,5,null,4]  ->  [1,3,4]
[1,2]                  ->  [1,2]     a left child is visible when nothing is to its right
```

## Signature

```java
public List<Integer> rightSideView(TreeNode root)
```

## Target complexity

O(n).

## The skill

Level order, keeping each level's last node. Following only right children is the classic
wrong answer (`[1,2]` and `[1,2,3,4]` are pinned). The DFS version visits right before left and
records the first node seen at each new depth.

## Hints

<details><summary>Hint 1</summary>

BFS with a size snapshot; the last node polled in each level is the answer for that level.

</details>
