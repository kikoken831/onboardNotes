# Climbing Stairs

LeetCode 70 · Easy · https://leetcode.com/problems/climbing-stairs/

## Brief

Climbing `n` steps one or two at a time, how many distinct ways are there to reach the top?

## Constraints

- `1 <= n <= 45`

## Examples

```
2  ->  2    1+1, 2
3  ->  3
```

## Signature

```java
public int climbStairs(int n)
```

## Target complexity

O(n), O(1) space.

## The skill

**Ramp:** Tier 1 `p-dp-fib-memo-then-table`. `ways(n) = ways(n−1) + ways(n−2)` — Fibonacci shifted by one. Naive recursion is O(φⁿ) and **far too slow at `n = 45`** (over 2 billion calls; the judge pins that case); that's the point of the problem.

## Hints

<details><summary>Hint 1</summary>

The last move was either a 1-step or a 2-step.

</details>
