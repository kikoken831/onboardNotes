# Minimum Height Trees

LeetCode 310 · Medium · https://leetcode.com/problems/minimum-height-trees/

## Brief

Given an undirected **tree** with `n` nodes, return every node that, chosen as the root, gives the minimum height. Any order.

## Constraints

- `1 <= n <= 2 * 10^4`, `edges.length == n − 1`

## Examples

```
4, [[1,0],[1,2],[1,3]]              ->  [1]
6, [[3,0],[3,1],[3,2],[3,4],[5,4]]  ->  [3,4]
```

## Signature

```java
public List<Integer> findMinHeightTrees(int n, int[][] edges)
```

## Target complexity

O(n).

## The skill

The answer is the **centre** of the tree's longest path — always one or two nodes. Find it by **peeling leaves** layer by layer (a topological sort from the outside in) until at most two nodes remain. BFS from every node is O(n²).

## Hints

<details><summary>Hint 1</summary>

`n = 1` is `[0]` — there are no leaves to peel.

</details>
