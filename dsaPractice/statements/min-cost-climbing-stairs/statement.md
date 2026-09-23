# Min Cost Climbing Stairs

LeetCode 746 · Easy · https://leetcode.com/problems/min-cost-climbing-stairs/

## Brief

`cost[i]` is what you pay to step **off** stair `i`; you then climb 1 or 2 stairs. You may
start on stair 0 or 1. Return the minimum cost to reach the **top**, which is one past the
last stair.

## Constraints

- `2 <= cost.length <= 1000`
- `0 <= cost[i] <= 999`

## Examples

```
[10,15,20]                   ->  15   start at 1, pay 15, jump 2 to the top
[1,100,1,1,1,100,1,1,100,1]  ->  6
```

## Signature

```java
public int minCostClimbingStairs(int[] cost)
```

## Target complexity

O(n) time, O(1) space.

## The skill

**Ramp:** Tier 1 `p-dp-climb-cost`. **Define the state first**: `dp[i]` = min cost to *stand
on* step `i`, with `dp[0] = dp[1] = 0` (you may start there free) and the answer at
`dp[n]`. Get that definition wrong and every recurrence you write is off by one.

## Hints

<details><summary>Hint 1</summary>

`dp[i] = min(dp[i-1] + cost[i-1], dp[i-2] + cost[i-2])`. The top is `i = n`, not `n-1`.

</details>
