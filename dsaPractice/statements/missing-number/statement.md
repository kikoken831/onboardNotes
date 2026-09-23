# Missing Number

LeetCode 268 · Easy · https://leetcode.com/problems/missing-number/

## Brief

`nums` holds `n` **distinct** values from `0..n`. Exactly one is missing; return it.

## Constraints

- `1 <= n <= 10^4`

## Examples

```
[3,0,1]              ->  2
[0,1]                ->  2      the missing one can be n itself
[9,6,4,2,3,5,7,0,1]  ->  8
```

## Signature

```java
public int missingNumber(int[] nums)
```

## Target complexity

O(n) time, O(1) space.

## The skill

Three O(1)-space answers, each worth knowing: **Gauss** (`n(n+1)/2 − sum`), **XOR** of all
indices and values, and **cyclic placement**. The trap: the missing value can be `0` or `n`,
and the range is `0..n` — `n + 1` candidates for `n` slots.

## Hints

<details><summary>Hint 1</summary>

What should the values sum to if nothing were missing?

</details>
