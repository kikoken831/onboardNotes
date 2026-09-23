# Reshape the Matrix

LeetCode 566 · Easy · https://leetcode.com/problems/reshape-the-matrix/

## Brief

Reshape an `m × n` matrix into `r × c`, filling row by row in the original row-traversal
order. If the reshape is impossible, return the **original** matrix unchanged.

## Constraints

- `1 <= m, n <= 100`
- `1 <= r, c <= 300`

## Examples

```
[[1,2],[3,4]], r=1, c=4  ->  [[1,2,3,4]]
[[1,2],[3,4]], r=2, c=4  ->  [[1,2],[3,4]]    2x2 has 4 cells, 2x4 needs 8
```

## Signature

```java
public int[][] matrixReshape(int[][] mat, int r, int c)
```

## Target complexity

O(m × n).

## The skill

**One linear index, two coordinate systems.** Walk `k = 0..mn-1`; the source cell is
`(k / n, k % n)` and the destination is `(k / c, k % c)`. That formula is the whole problem —
no nested loops needed.

Checking feasibility is `r * c == m * n`, and it must happen before you allocate anything.

## Hints

<details><summary>Hint 1</summary>

Divide and modulo by the **column** count of whichever matrix you're indexing — `n` for the
source, `c` for the target. Mixing them up is the usual bug.

</details>
