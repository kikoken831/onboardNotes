# Pacific Atlantic Water Flow

LeetCode 417 · Medium · https://leetcode.com/problems/pacific-atlantic-water-flow/

## Brief

The Pacific touches the top and left edges of `heights`; the Atlantic the bottom and right. Water flows to a neighbour of **equal or lower** height. Return every cell from which water can reach **both** oceans, in any order.

## Constraints

- `1 <= m, n <= 200`

## Examples

```
[[1,2,2,3,5],[3,2,3,4,4],[2,4,5,3,1],[6,7,1,4,5],[5,1,1,2,4]]
->  [[0,4],[1,3],[1,4],[2,2],[3,0],[3,1],[4,0]]
```

## Signature

```java
public List<List<Integer>> pacificAtlantic(int[][] heights)
```

## Target complexity

O(m · n).

## The skill

**Reverse the flow.** Searching from each cell is O((mn)²). Instead, start from each ocean's shore and walk **uphill** (to neighbours that are `>=`), marking everything that can drain there. The answer is the intersection of the two marked sets. **Ramp:** Tier 1 `p-grid-bfs-shortest` (multi-source BFS).

## Hints

<details><summary>Hint 1</summary>

Two BFS runs, one per ocean, each seeded with its whole shoreline.

</details>
