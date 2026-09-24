# Search a 2D Matrix

LeetCode 74 · Medium · https://leetcode.com/problems/search-a-2d-matrix/

## Brief

Each row is sorted, and each row's first value is greater than the previous row's last.
Return whether `target` is in the matrix.

## Constraints

- `1 <= m, n <= 100`

## Examples

```
[[1,3,5,7],[10,11,16,20],[23,30,34,60]], 3   ->  true
[[1,3,5,7],[10,11,16,20],[23,30,34,60]], 13  ->  false
```

## Signature

```java
public boolean searchMatrix(int[][] matrix, int target)
```

## Target complexity

O(log(m · n)).

## The skill

Those two rules make the matrix **one sorted array** read row by row. Binary-search indices
`0..mn-1` and map `k → (k / n, k % n)` — the same index arithmetic as Reshape the Matrix.

## Hints

<details><summary>Hint 1</summary>

Divide and mod by the number of **columns**.

</details>
