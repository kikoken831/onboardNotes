# Search a 2D Matrix II

LeetCode 240 · Medium · https://leetcode.com/problems/search-a-2d-matrix-ii/

## Brief

Rows are sorted left to right and columns top to bottom — but rows don't chain into each
other as in LC 74. Return whether `target` is present.

## Constraints

- `1 <= m, n <= 300`

## Examples

```
[[1,4,7,11,15],[2,5,8,12,19],[3,6,9,16,22],[10,13,14,17,24],[18,21,23,26,30]], 5   ->  true
... , 20  ->  false
```

## Signature

```java
public boolean searchMatrix(int[][] matrix, int target)
```

## Target complexity

O(m + n).

## The skill

Start at the **top-right** corner: everything left of it is smaller, everything below is
larger. So each comparison discards a whole row or a whole column — a "staircase" walk. From the
top-left corner both moves increase and the trick doesn't work.

## Hints

<details><summary>Hint 1</summary>

Too big → move left. Too small → move down.

</details>
