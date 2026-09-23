# N-th Tribonacci Number

LeetCode 1137 · Easy · https://leetcode.com/problems/n-th-tribonacci-number/

## Brief

`T0 = 0`, `T1 = 1`, `T2 = 1`, and `T(n+3) = T(n) + T(n+1) + T(n+2)`. Return `Tn`.

## Constraints

- `0 <= n <= 37`
- the answer fits in a 32-bit signed integer

## Examples

```
4   ->  4        0, 1, 1, 2, 4
25  ->  1389537
```

## Signature

```java
public int tribonacci(int n)
```

## Target complexity

O(n) time, O(1) space.

## The skill

Fibonacci with a three-variable window. The difference that actually bites: naive
recursion is O(3ⁿ) and **does** time out here, so the memoisation is no longer optional.

`T37 = 2082876103` is right under `Integer.MAX_VALUE` — the constraint says it fits, and the
judge checks it.

## Hints

<details><summary>Hint 1</summary>

Handle `n = 0` separately, then roll `a, b, c` forward from `i = 3`.

</details>
