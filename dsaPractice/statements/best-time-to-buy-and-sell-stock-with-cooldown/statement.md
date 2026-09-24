# Best Time to Buy and Sell Stock with Cooldown

LeetCode 309 · Medium · https://leetcode.com/problems/best-time-to-buy-and-sell-stock-with-cooldown/

## Brief

Trade as many times as you like, holding at most one share, but after selling you must wait **one day** before buying again. Return the maximum profit.

## Constraints

- `1 <= n <= 5000`

## Examples

```
[1,2,3,0,2]  ->  3    buy, sell, cooldown, buy, sell
```

## Signature

```java
public int maxProfit(int[] prices)
```

## Target complexity

O(n), O(1) space.

## The skill

A **state machine** DP. Three states per day — *holding*, *just sold*, *resting (free to buy)* — with transitions: hold ← hold or buy from rest; sold ← sell from hold; rest ← rest or come out of cooldown. Writing the states down first makes the recurrence mechanical; guessing the recurrence directly is where people go wrong.

## Hints

<details><summary>Hint 1</summary>

Only *rest* can buy; *sold* must pass through *rest* first.

</details>
