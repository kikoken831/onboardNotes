# Two-dimensional prefix sums

Tier 1 drill · prefix-sum · unlocks LC 304, 1314

## Brief

Given a `matrix` and a list of `queries`, each `{r1, c1, r2, c2}` describing an
inclusive rectangle, return the sum inside each rectangle as an `int[]`.

## Constraints

- `1 <= rows, cols <= 200`
- `0 <= r1 <= r2 < rows`, `0 <= c1 <= c2 < cols`

## Examples

```
matrix = [[1,2],[3,4]], queries = [[0,0,1,1],[0,0,0,0],[1,0,1,1]]  ->  [10,1,7]
matrix = [[5]],         queries = [[0,0,0,0]]                       ->  [5]
matrix = [[1,2],[3,4]], queries = []                                ->  []
```

## Signature

```java
public int[] rectangleSums(int[][] matrix, int[][] queries)
```

## Target complexity

O(rows · cols) to build, **O(1) per query**.

## The skill

The same `+1` offset as the 1-D drill, now in both dimensions, plus
inclusion–exclusion.

Build a `(rows+1) × (cols+1)` table where `p[r][c]` is the sum of the rectangle from
the origin to `(r-1, c-1)`:

```java
p[r+1][c+1] = matrix[r][c] + p[r][c+1] + p[r+1][c] - p[r][c];
```

The subtraction is because the region above and the region to the left overlap in the
top-left block, which would otherwise be counted twice.

Querying reverses it:

```java
sum = p[r2+1][c2+1] - p[r1][c2+1] - p[r2+1][c1] + p[r1][c1];
```

Take the whole rectangle to the bottom-right corner, remove the strip above, remove the
strip to the left, then **add back** the top-left block you just removed twice. Drawing
this on paper once is worth more than memorising the four terms — and if you cannot
draw it, you will not reconstruct it under pressure.

The `+1` padding does the same job as in 1-D: the `r1 = 0` and `c1 = 0` cases index into
the zero row and column instead of needing branches.

## Hints

<details><summary>Hint 1 — verify on the 2x2</summary>

`[[1,2],[3,4]]` gives the padded table
```
0 0 0
0 1 3
0 4 10
```
Check each entry against the formula before writing the query half.

</details>

<details><summary>Hint 2 — the four-term sign pattern</summary>

Both build and query use `+ - - +`. That symmetry is not a coincidence — one is the
inverse of the other. Noticing it halves what you have to remember.

</details>
