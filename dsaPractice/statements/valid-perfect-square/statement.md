# Valid Perfect Square

LeetCode 367 · Easy · https://leetcode.com/problems/valid-perfect-square/

## Brief

Return whether `num` is a perfect square, without `sqrt`.

## Constraints

- `1 <= num <= 2^31 - 1`

## Examples

```
16  ->  true
14  ->  false
```

## Signature

```java
public boolean isPerfectSquare(int num)
```

## Target complexity

O(log num).

## The skill

**Ramp:** Tier 1 `p-bs-exact` — an exact-match binary search over `[1, num]`. Overflow again:
`mid * mid` in `int` wraps to a negative number for large `mid` and silently breaks the
comparison.

## Hints

<details><summary>Hint 1</summary>

Classic `while (lo <= hi)` search for a `mid` with `mid * mid == num`, using `long`.

</details>
