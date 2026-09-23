# Richest Customer Wealth

LeetCode 1672 · Easy · https://leetcode.com/problems/richest-customer-wealth/

## Brief

`accounts[i][j]` is what customer `i` holds in bank `j`. A customer's wealth is the sum of
their row. Return the largest wealth.

## Constraints

- `1 <= m, n <= 50`
- `1 <= accounts[i][j] <= 100`

## Examples

```
[[1,2,3],[3,2,1]]    ->  6
[[1,5],[7,3],[3,5]]  ->  10
```

## Signature

```java
public int maximumWealth(int[][] accounts)
```

## Target complexity

O(m × n).

## The skill

Row-major aggregation: reduce each row, then reduce the results. The bug to avoid is
summing a **column** (`accounts[j][i]`) — which for square matrices can even look right on
some inputs.

## Hints

<details><summary>Hint 1</summary>

Reset the row sum **inside** the outer loop.

</details>
