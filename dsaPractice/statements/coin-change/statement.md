# Coin Change

LeetCode 322 · Medium · https://leetcode.com/problems/coin-change/

## Brief

Return the fewest coins (unlimited of each denomination) summing to `amount`, or `-1` if impossible.

## Constraints

- `1 <= coins.length <= 12`, `0 <= amount <= 10^4`

## Examples

```
[1,2,5], 11  ->  3     5 + 5 + 1
[2], 3       ->  -1
[1], 0       ->  0
```

## Signature

```java
public int coinChange(int[] coins, int amount)
```

## Target complexity

O(amount · coins).

## The skill

Unbounded knapsack: `d[x] = 1 + min over coins of d[x − c]`. **Greedy (largest coin first) fails** — `[1,3,4], 6` gives `4+1+1` instead of `3+3`; pinned. Guard the "impossible" sentinel: `MAX_VALUE + 1` overflows to negative and becomes the minimum.

## Hints

<details><summary>Hint 1</summary>

`amount = 0` needs zero coins.

</details>
