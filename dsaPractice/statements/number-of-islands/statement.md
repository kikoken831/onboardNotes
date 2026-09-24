# Number of Islands

LeetCode 200 · Medium · https://leetcode.com/problems/number-of-islands/

## Brief

`grid` is `'1'` (land) and `'0'` (water). Count the islands — groups of land connected **horizontally or vertically** (not diagonally).

## Constraints

- `1 <= m, n <= 300`

## Examples

```
[["1","1","1","1","0"],["1","1","0","1","0"],["1","1","0","0","0"],["0","0","0","0","0"]]  ->  1
```

## Signature

```java
public int numIslands(char[][] grid)
```

## Target complexity

O(m · n).

## The skill

**Ramp:** Tier 1 `p-grid-dfs-region` and `p-grid-visited-on-enqueue`. Scan; each unvisited land cell starts a new island and a flood fill that marks the whole thing. Recursive DFS can overflow the stack on a 300×300 island — an explicit stack or BFS is safer. Union-find is the other standard answer.

## Hints

<details><summary>Hint 1</summary>

Mark cells **when you push them**, not when you pop them, or cells get pushed many times.

</details>
