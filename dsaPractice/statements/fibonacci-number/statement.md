# Fibonacci Number

LeetCode 509 · Easy · https://leetcode.com/problems/fibonacci-number/

## Brief

`F(0) = 0`, `F(1) = 1`, `F(n) = F(n-1) + F(n-2)`. Return `F(n)`.

## Constraints

- `0 <= n <= 30`

## Examples

```
2  ->  1
3  ->  2
4  ->  3
```

## Signature

```java
public int fib(int n)
```

## Target complexity

O(n) time, O(1) space.

## The skill

**Ramp:** Tier 1 `p-dp-fib-memo-then-table`. Write all three: naive recursion (O(2ⁿ) — note
it still passes at `n = 30`, which is why this problem teaches nothing unless you do the
progression deliberately), memoised, then two rolling variables.

## Hints

<details><summary>Hint 1</summary>

The table only ever reads the last two entries, so it can be two variables.

</details>
