# Island Perimeter

LeetCode 463 · Easy · https://leetcode.com/problems/island-perimeter/

## Brief

`grid` is land (`1`) and water (`0`). There is **exactly one** island (4-directionally
connected land) and it has **no lakes**. Each cell is a 1×1 square. Return the island's
perimeter.

## Constraints

- `1 <= rows, cols <= 100`
- exactly one island, no lakes

## Examples

```
[[0,1,0,0],
 [1,1,1,0],
 [0,1,0,0],
 [1,1,0,0]]   ->  16

[[1]]         ->  4
[[1,0]]       ->  4
```

## Signature

```java
public int islandPerimeter(int[][] grid)
```

## Target complexity

O(rows × cols).

## The skill

No search is needed. Each land cell contributes 4 edges, and every pair of **adjacent** land
cells hides 2 of them: `4 × land − 2 × adjacentPairs`. Count each adjacency **once** by only
looking down and right.

The DFS version (count edges that face water or the grid boundary) is also correct and
worth writing — it's the shape of every later grid problem.

## Hints

<details><summary>Hint 1</summary>

For a land cell, each of its 4 sides is perimeter if the neighbour is water **or off the
grid**. The second case is the one people forget.

</details>
