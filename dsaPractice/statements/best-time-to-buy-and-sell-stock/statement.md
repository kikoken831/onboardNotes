# Best Time to Buy and Sell Stock

LeetCode 121 · Easy · https://leetcode.com/problems/best-time-to-buy-and-sell-stock/

## Brief

Buy on one day and sell on a **later** day. Return the maximum profit, or `0` if no trade helps.

## Constraints

- `1 <= n <= 10^5`

## Examples

```
[7,1,5,3,6,4]  ->  5    buy at 1, sell at 6
[7,6,4,3,1]    ->  0
```

## Signature

```java
public int maxProfit(int[] prices)
```

## Target complexity

O(n).

## The skill

Keep the **lowest price so far**; each day's best sale is `price − low`. Selling before buying (`max − min` over the whole array) is the classic wrong answer — `[2,4,1]` is pinned.

## Hints

<details><summary>Hint 1</summary>

One pass, two variables.

</details>
