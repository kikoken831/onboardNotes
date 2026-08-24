# Flood fill from a starting cell

Tier 1 drill · grid-search · unlocks LC 733, 200, 1034

## Brief

Recolour the connected region of same-coloured cells containing `(sr, sc)` to `newColor`.
Connected means 4-directional and same original colour. Return the modified grid.

If the starting cell already holds `newColor`, change nothing.

## Constraints

- `1 <= rows, cols <= 50`
- `0 <= colours <= 2^16`

## Examples

```
[[1,1,1],[1,1,0],[1,0,1]], sr=1, sc=1, newColor=2  ->  [[2,2,2],[2,2,0],[2,0,1]]
[[0,0,0],[0,0,0]],         sr=0, sc=0, newColor=0  ->  [[0,0,0],[0,0,0]]   (no change)
[[1]],                     sr=0, sc=0, newColor=5  ->  [[5]]
```

## Signature

```java
public int[][] floodFill(int[][] image, int sr, int sc, int newColor)
```

## Target complexity

O(rows · cols).

## The skill

Same DFS as the region drill, with one addition that is the entire difficulty:
**the termination guard.**

If `newColor` equals the starting colour, then "mark this cell" does not actually change
anything, so the recursion has no way to know it has been here before — and it recurses
forever between two adjacent cells. `[[0,0,0],[0,0,0]]` with `newColor=0` is that case,
and it is a pinned example precisely because it is the one everybody hits.

```java
int start = image[sr][sc];
if (start == newColor) return image;   // <- the guard
fill(image, sr, sc, start, newColor);
return image;
```

The wider lesson is about visited-marking in general: **your "visited" marker has to be
distinguishable from "not visited"**. Overwriting with a value that might already be
there is not a marker. In the region drill the marker was 2, which could not collide
with 0 or 1. Here it can collide, so you need the guard.

## Hints

<details><summary>Hint 1 — pass the original colour down</summary>

The helper needs the colour it is replacing, because after the first cell is repainted
you can no longer read it off the grid. Thread it down as a parameter — recursion
shape 2 from the tree group.

</details>
