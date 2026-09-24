# Set Matrix Zeroes

LeetCode 73 · Medium · https://leetcode.com/problems/set-matrix-zeroes/

## Brief

For every `0` in the matrix, set its **entire row and column** to `0`, in place.

## Constraints

- `1 <= m, n <= 200`

## Examples

```
[[1,1,1],[1,0,1],[1,1,1]]        ->  [[1,0,1],[0,0,0],[1,0,1]]
[[0,1,2,0],[3,4,5,2],[1,3,1,5]]  ->  [[0,0,0,0],[0,4,5,0],[0,3,1,0]]
```

## Signature

```java
public void setZeroes(int[][] matrix)
```

## Target complexity

O(m · n) time; the follow-up asks for O(1) extra space.

## The skill

Record first, write second — zeroing as you scan creates new zeros that spread across the whole
matrix. Two boolean arrays are O(m + n). The O(1) version stores those flags **in the first row
and column**, with one extra flag for whether the first column itself must be zeroed.

## Hints

<details><summary>Hint 1</summary>

In the O(1) version, fill from the bottom-right so the flag row and column are read before
they're overwritten.

</details>
