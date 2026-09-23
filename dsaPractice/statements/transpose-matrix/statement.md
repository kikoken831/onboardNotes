# Transpose Matrix

LeetCode 867 · Easy · https://leetcode.com/problems/transpose-matrix/

## Brief

Return the transpose of an `m × n` matrix: rows become columns, so the result is `n × m`
with `t[j][i] = matrix[i][j]`.

## Constraints

- `1 <= m, n <= 1000`, `1 <= m × n <= 10^5`

## Examples

```
[[1,2,3],[4,5,6],[7,8,9]]  ->  [[1,4,7],[2,5,8],[3,6,9]]
[[1,2,3],[4,5,6]]          ->  [[1,4],[2,5],[3,6]]
```

## Signature

```java
public int[][] transpose(int[][] matrix)
```

## Target complexity

O(m × n).

## The skill

**The output has a different shape.** The in-place swap you may know (`swap(a[i][j], a[j][i])`
for `j > i`) only works for **square** matrices; the second example isn't square, and the
judge generates plenty of non-square inputs. Allocate `new int[n][m]` — columns first.

## Hints

<details><summary>Hint 1</summary>

`int[][] t = new int[n][m];` then `t[j][i] = matrix[i][j]`.

</details>
