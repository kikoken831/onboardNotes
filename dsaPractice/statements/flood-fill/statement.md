# Flood Fill

LeetCode 733 · Easy · https://leetcode.com/problems/flood-fill/

## Brief

Starting at pixel `(sr, sc)`, recolour it and every pixel 4-directionally connected to it
**that has the same original colour** to `color`. Return the image.

## Constraints

- `1 <= rows, cols <= 50`
- `0 <= image[i][j], color < 2^16`

## Examples

```
[[1,1,1],[1,1,0],[1,0,1]], sr=1, sc=1, color=2  ->  [[2,2,2],[2,2,0],[2,0,1]]
[[0,0,0],[0,0,0]],         sr=0, sc=0, color=0  ->  [[0,0,0],[0,0,0]]
```

## Signature

```java
public int[][] floodFill(int[][] image, int sr, int sc, int color)
```

## Target complexity

O(rows × cols).

## The skill

**Ramp:** Tier 1 `p-grid-flood-fill` and `p-grid-dfs-region`.

**The trap is example 2.** When `color` already equals the starting colour, a DFS that uses
"pixel has the original colour" as its only visited-check never marks anything as done: it
recolours a pixel to the same value and recurses into it again, forever
(`StackOverflowError`). Check `if (image[sr][sc] == color) return image;` first, or keep a
separate visited set.

## Hints

<details><summary>Hint 1</summary>

Remember the original colour before you change anything, then spread to neighbours that
still have it.

</details>
