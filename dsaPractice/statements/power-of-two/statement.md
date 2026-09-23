# Power of Two

LeetCode 231 · Easy · https://leetcode.com/problems/power-of-two/

## Brief

Return whether `n == 2^x` for some integer `x >= 0`.

## Constraints

- `-2^31 <= n <= 2^31 - 1`

## Examples

```
1   ->  true     2^0
16  ->  true
3   ->  false
```

## Signature

```java
public boolean isPowerOfTwo(int n)
```

## Target complexity

O(1).

## The skill

A power of two has **exactly one bit set**, and `n & (n - 1)` clears the lowest set bit —
so it's zero exactly when one bit was set.

**The trap:** `Integer.MIN_VALUE` is `1000...0` — also one bit set — and it is negative.
`(n & (n-1)) == 0` alone says `true` for it, and for `0`. You need `n > 0` as well. The judge
tests both.

## Hints

<details><summary>Hint 1</summary>

`Integer.bitCount(n) == 1` is a fine first answer. What does it return for
`Integer.MIN_VALUE`?

</details>
