# Unique Paths

LeetCode 62 · Medium · https://leetcode.com/problems/unique-paths/

## Brief

A robot starts top-left of an `m × n` grid and moves only **right** or **down**. How many paths reach the bottom-right? The answer fits in 32 bits.

## Constraints

- `1 <= m, n <= 100`

## Examples

```
m=3, n=7  ->  28
m=3, n=2  ->  3
```

## Signature

```java
public int uniquePaths(int m, int n)
```

## Target complexity

O(m · n) time, O(n) space; O(min(m, n)) with combinatorics.

## The skill

The grid DP everything else builds on: `paths[i][j] = paths[i−1][j] + paths[i][j−1]`, with the first row and column all `1`. One row is enough, because each cell needs only the cell above (still in the array) and the cell to its left (just updated). The closed form is `C(m+n−2, m−1)` — compute it incrementally in `long`.

## Hints

<details><summary>Hint 1</summary>

Initialise the row to all ones.

</details>
