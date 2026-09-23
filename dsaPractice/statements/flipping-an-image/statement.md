# Flipping an Image

LeetCode 832 · Easy · https://leetcode.com/problems/flipping-an-image/

## Brief

For an `n × n` binary matrix, reverse each row, then invert every bit (`0 ↔ 1`). Return the
result.

## Constraints

- `1 <= n <= 20`
- cells are `0` or `1`

## Examples

```
[[1,1,0],[1,0,1],[0,0,0]]  ->  [[1,0,0],[0,1,0],[1,1,1]]
```

## Signature

```java
public int[][] flipAndInvertImage(int[][] image)
```

## Target complexity

O(n²), and in place if you like.

## The skill

**Ramp:** Tier 1 `p-reverse-in-place`, applied per row, plus a detail: the in-place version
swaps `row[i]` and `row[n-1-i]` **and inverts both** — and when `n` is odd the middle element
is its own partner and must be inverted **exactly once**. The judge's first example has odd
width.

## Hints

<details><summary>Hint 1</summary>

Invert is `1 - x` or `x ^ 1`. Reversing and inverting commute, so you can do both in one
swap.

</details>
