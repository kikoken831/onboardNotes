# Number of 1 Bits

LeetCode 191 · Easy · https://leetcode.com/problems/number-of-1-bits/

## Brief

Return the number of set bits in the binary representation of `n`.

## Constraints

- `1 <= n <= 2^31 - 1`

## Examples

```
11   ->  3    1011
128  ->  1
```

## Signature

```java
public int hammingWeight(int n)
```

## Target complexity

O(number of set bits).

## The skill

`n & (n − 1)` clears the lowest set bit, so looping until zero counts exactly the set bits — Brian Kernighan's trick, the same identity as Power of Two. Checking all 32 positions with `>>>` is also fine; `>>` on a negative number never reaches zero.

## Hints

<details><summary>Hint 1</summary>

`Integer.bitCount` is the library answer; know both.

</details>
