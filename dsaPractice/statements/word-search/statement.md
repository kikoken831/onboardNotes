# Word Search

LeetCode 79 · Medium · https://leetcode.com/problems/word-search/

## Brief

Can `word` be traced through horizontally or vertically adjacent cells of `board`, using **each cell at most once**?

## Constraints

- `1 <= m, n <= 6`, `1 <= word.length <= 15`

## Examples

```
board=[[A,B,C,E],[S,F,C,S],[A,D,E,E]], "ABCCED"  ->  true
same board, "ABCB"                                ->  false   would reuse B
```

## Signature

```java
public boolean exist(char[][] board, String word)
```

## Target complexity

O(m · n · 3^L).

## The skill

**Ramp:** Tier 1 `p-grid-dfs-region` plus backtracking. Mark a cell **before** recursing and **restore** it after — a global visited array that isn't restored blocks paths that cross the same cell in a different attempt. `[[a,a]], "aaa"` is pinned for reuse.

## Hints

<details><summary>Hint 1</summary>

Overwrite the cell with `'#'`, recurse, write the letter back.

</details>
