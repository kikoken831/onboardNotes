# Arranging Coins

LeetCode 441 · Easy · https://leetcode.com/problems/arranging-coins/

## Brief

You build a staircase with `n` coins: row `i` needs exactly `i` coins. Return how many
rows are **complete**.

## Constraints

- `1 <= n <= 2^31 - 1`

## Examples

```
5  ->  2    rows of 1, 2, then 2 of the 3 needed
8  ->  3
```

## Signature

```java
public int arrangeCoins(int n)
```

## Target complexity

O(log n) by binary search, or O(1) with the quadratic formula.

## The skill

**Ramp:** Tier 1 `p-bs-on-answer`. Binary search on the *answer* `k`, testing
`k(k+1)/2 <= n`.

**The trap is overflow.** At `n = 2^31 - 1`, `k` reaches ~65535 and `k*(k+1)` is ~4.3 × 10^9,
which does not fit in an `int`. The judge includes that case. Use `long`.

## Hints

<details><summary>Hint 1</summary>

You want the **largest** `k` that satisfies the test, so round `mid` **up**
(`lo + (hi - lo + 1) / 2`) or the loop never terminates when `hi = lo + 1`.

</details>
