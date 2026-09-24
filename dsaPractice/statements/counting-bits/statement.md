# Counting Bits

LeetCode 338 · Easy · https://leetcode.com/problems/counting-bits/

## Brief

Return an array of length `n + 1` whose `i`-th entry is the number of `1` bits in `i`.

## Constraints

- `0 <= n <= 10^5`

## Examples

```
2  ->  [0,1,1]
5  ->  [0,1,1,2,1,2]
```

## Signature

```java
public int[] countBits(int n)
```

## Target complexity

O(n) — the follow-up forbids `Integer.bitCount`-per-number.

## The skill

DP on the binary representation: `i` has the same bits as `i >> 1` plus its lowest bit — `bits[i] = bits[i >> 1] + (i & 1)`. Equivalently `bits[i] = bits[i & (i − 1)] + 1`, since `i & (i − 1)` clears the lowest set bit.

## Hints

<details><summary>Hint 1</summary>

Every entry depends on a smaller index you've already computed.

</details>
