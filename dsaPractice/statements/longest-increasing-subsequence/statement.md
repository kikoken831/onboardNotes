# Longest Increasing Subsequence

LeetCode 300 · Medium · https://leetcode.com/problems/longest-increasing-subsequence/

## Brief

Return the length of the longest **strictly** increasing subsequence (not necessarily contiguous).

## Constraints

- `1 <= n <= 2500`

## Examples

```
[10,9,2,5,3,7,101,18]  ->  4     [2,3,7,101]
[7,7,7,7,7,7,7]        ->  1     strictly
```

## Signature

```java
public int lengthOfLIS(int[] nums)
```

## Target complexity

O(n²) DP; the follow-up asks for O(n log n).

## The skill

**Ramp:** Tier 1 `p-dp-lis-quadratic`. `dp[i]` = longest run ending at `i` — O(n²). The O(n log n) version keeps `tails[k]`, the smallest possible tail of an increasing run of length `k + 1`, and binary-searches where each value goes. `tails` is **not** an LIS itself; only its length is meaningful. Use lower bound (`<`), or equal values extend runs.

## Hints

<details><summary>Hint 1</summary>

Patience sorting: each value replaces the first tail that's `>=` it.

</details>
