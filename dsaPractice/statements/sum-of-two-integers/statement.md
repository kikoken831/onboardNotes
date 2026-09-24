# Sum of Two Integers

LeetCode 371 · Medium · https://leetcode.com/problems/sum-of-two-integers/

## Brief

Return `a + b` **without** using `+` or `-`.

## Constraints

- `-1000 <= a, b <= 1000`

## Examples

```
1, 2   ->  3
-1, 1  ->  0
```

## Signature

```java
public int getSum(int a, int b)
```

## Target complexity

O(32).

## The skill

Binary addition: `a ^ b` is the sum **without** carries; `(a & b) << 1` is the carries. Repeat until no carry remains. In Java this works for negatives unchanged, because `int` is two's complement and the carry out of bit 31 simply falls off.

## Hints

<details><summary>Hint 1</summary>

Loop while the carry is non-zero.

</details>
