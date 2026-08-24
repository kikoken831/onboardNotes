# Size of one connected region

Tier 1 drill · grid-search · unlocks LC 200, 695, 733, 1254

## Brief

Given a grid of `0` and `1`, return the size of the connected region of `1`s containing
cell `(r, c)`. Connected means orthogonally adjacent — up, down, left, right, **not**
diagonal. If `(r, c)` is a `0`, return 0.

You may modify the grid.

## Constraints

- `1 <= rows, cols <= 50`
- cells are `0` or `1`
- `(r, c)` is inside the grid

## Examples

```
[[1,1,0],[0,1,0],[0,0,1]], r=0, c=0  ->  3
[[1,1,0],[0,1,0],[0,0,1]], r=2, c=2  ->  1
[[0]],                     r=0, c=0  ->  0
[[1,1],[1,1]],             r=0, c=0  ->  4
```

## Signature

```java
public int regionSize(int[][] grid, int r, int c)
```

## Target complexity

O(rows · cols) time.

## The skill

The grid-DFS template, and the four things that always appear in it:

```java
private int dfs(int[][] g, int r, int c) {
    if (r < 0 || r >= g.length || c < 0 || c >= g[0].length) return 0;  // 1. bounds
    if (g[r][c] != 1) return 0;                                          // 2. blocked or seen
    g[r][c] = 2;                                                         // 3. mark BEFORE recursing
    return 1 + dfs(g, r+1, c) + dfs(g, r-1, c) + dfs(g, r, c+1) + dfs(g, r, c-1);
}
```

1. **Bounds first.** Check before you index, or you get an
   `ArrayIndexOutOfBoundsException` on the edge cells.
2. **One check covers both** "it is water" and "already visited", because marking uses a
   value that is neither 0 nor 1.
3. **Mark before recursing, not after.** Mark afterwards and the four neighbours each
   walk straight back into the current cell — infinite recursion, then
   `StackOverflowError`.
4. Marking in place avoids a separate `boolean[][]`. If you are not allowed to mutate
   the input, allocate one — but say out loud which you are doing.

The direction array `{{1,0},{-1,0},{0,1},{0,-1}}` with a loop is the tidier form and
scales to 8-directional. Write it both ways.

## Hints

<details><summary>Hint 1 — recursion depth is a real risk</summary>

A 50×50 grid that is entirely `1` gives a recursion up to 2500 deep, which is fine. A
1000×1000 grid would not be — measured stack overflow is around 45,000 trivial frames
and DFS frames are much fatter. For large grids use the BFS form from the next drill.

</details>
