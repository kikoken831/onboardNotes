# All Nodes Distance K in Binary Tree

LeetCode 863 · Medium · https://leetcode.com/problems/all-nodes-distance-k-in-binary-tree/

## Brief

Return the values of every node exactly `k` edges from `target`, in any order. `target` is a
**node of the tree you're given**; examples list it by value. the standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `1 <= nodes <= 500`, distinct values, `0 <= k <= 1000`

## Examples

```
[3,5,1,6,2,0,8,null,null,7,4], target=5, k=2  ->  [7,4,1]
[1], target=1, k=3                            ->  []
```

## Signature

```java
public List<Integer> distanceK(TreeNode root, TreeNode target, int k)
```

## Target complexity

O(n).

## The skill

Distance runs **up** through parents as well as down, which a tree has no pointer for. Record
every node's parent, and the tree becomes an undirected graph: BFS `k` levels from `target`
with a visited set. `k = 0` returns the target itself.

## Hints

<details><summary>Hint 1</summary>

Neighbours of a node: left, right, parent.

</details>
