# Convert 1D Array Into 2D Array

LeetCode 2022 · Easy · https://leetcode.com/problems/convert-1d-array-into-2d-array/

## Brief

Fill an `m × n` array row by row from `original`. If that can't use every element exactly
once, return an **empty** 2D array.

## Constraints

- `1 <= original.length <= 5 * 10^4`, `1 <= m, n <= 4 * 10^4`

## Examples

```
[1,2,3,4], m=2, n=2  ->  [[1,2],[3,4]]
[1,2],     m=1, n=1  ->  []
```

## Signature

```java
public int[][] construct2DArray(int[] original, int m, int n)
```

## Target complexity

O(m · n).

## The skill

Reshape the Matrix (T2) again: check `m · n == length` first, then `k → (k / n, k % n)`. With
`m, n` up to `4 × 10^4`, `m * n` overflows `int` — compare as `long`. "Empty" means
`new int[0][]`, not `null`.

## Hints

<details><summary>Hint 1</summary>

`(long) m * n != original.length`

</details>
