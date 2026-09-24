# Sudoku Solver

LeetCode 37 · Hard · https://leetcode.com/problems/sudoku-solver/

## Brief

Fill the empty cells (`'.'`) of a 9×9 board **in place** so every row, column and 3×3 box holds
each digit `1–9` exactly once. LeetCode's inputs have exactly one solution; the judge checks
the property (complete, valid, givens unchanged), so any correct fill passes.

## Constraints

- `board` is 9×9 of `'1'`–`'9'` and `'.'`; solvable

## Examples

```
[["5","3",".",".","7",".",".",".","."], ...]  ->  the unique completed grid
```

## Signature

```java
public void solveSudoku(char[][] board)
```

## Target complexity

Exponential in the worst case; fast in practice with constraint tracking.

## The skill

Backtracking with **O(1) legality checks**: keep a bitmask (or `boolean[9][10]`) per row,
column and box, so testing a digit doesn't scan 27 cells. The big practical speed-up is picking
the empty cell with the **fewest** legal digits next — it prunes the tree dramatically. Undo
the masks exactly as you set them.

## Hints

<details><summary>Hint 1</summary>

Return `boolean` from the recursion so the first complete fill stops the search.

</details>
