# Rotate Image

LeetCode 48 · Medium · https://leetcode.com/problems/rotate-image/

## Brief

Rotate an `n × n` matrix 90° **clockwise**, in place — no second matrix.

## Constraints

- `1 <= n <= 20`

## Examples

```
[[1,2,3],[4,5,6],[7,8,9]]  ->  [[7,4,1],[8,5,2],[9,6,3]]
```

## Signature

```java
public void rotate(int[][] matrix)
```

## Target complexity

O(n²) time, O(1) space.

## The skill

**Transpose, then reverse each row.** Two simple in-place passes compose into the rotation.
Transposing with `j` from `0` instead of `i + 1` swaps every pair twice and undoes itself. (The
other route — rotating four cells at a time, ring by ring — is worth knowing too.)

## Hints

<details><summary>Hint 1</summary>

Reverse rows then transpose gives the **counter**-clockwise rotation. Order matters.

</details>
