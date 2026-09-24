# Reverse Bits

LeetCode 190 · Easy · https://leetcode.com/problems/reverse-bits/

## Brief

Reverse the 32 bits of `n`, treated as **unsigned**. In Java the input and output are `int`s, so values with the top bit set appear negative.

## Constraints

- a 32-bit integer

## Examples

```
43261596  (00000010100101000001111010011100)  ->  964176192  (00111001011110000010100101000000)
-3        (11111111111111111111111111111101)  ->  -1073741825 (10111111111111111111111111111111)
```

## Signature

```java
public int reverseBits(int n)
```

## Target complexity

O(32); the follow-up (called many times) suggests caching bytes.

## The skill

Shift the answer left and pull `n`'s lowest bit in, 32 times. Loop **exactly 32 times**: looping *while `n != 0`* stops early and drops the trailing zeros that become leading bits (`1 → -2147483648` is pinned) — and with `>>` it never terminates for negative `n`, because the sign bit keeps refilling. With a fixed 32 iterations, `>>` and `>>>` both work.

## Hints

<details><summary>Hint 1</summary>

`r = (r << 1) | (n & 1); n >>>= 1;` exactly 32 times.

</details>
