# Sqrt(x)

LeetCode 69 · Easy · https://leetcode.com/problems/sqrtx/

## Brief

Return `floor(sqrt(x))` **without** `Math.sqrt` or `pow`.

## Constraints

- `0 <= x <= 2^31 - 1`

## Examples

```
4  ->  2
8  ->  2    sqrt(8) = 2.828...
```

## Signature

```java
public int mySqrt(int x)
```

## Target complexity

O(log x).

## The skill

**Ramp:** Tier 1 `p-bs-on-answer` — "largest `r` with `r*r <= x`". The same two traps as
Arranging Coins: `mid * mid` overflows `int` for large `x` (the judge tests `2^31 - 1`), and
searching for the *largest* value needs `mid` rounded up.

## Hints

<details><summary>Hint 1</summary>

Search `[0, x]`. Compute `mid * mid` in `long` — or compare `mid <= x / mid` instead, which
avoids the multiplication entirely (careful with `mid = 0`).

</details>
