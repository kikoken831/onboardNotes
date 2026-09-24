# Number of Longest Increasing Subsequence

LeetCode 673 · Medium · https://leetcode.com/problems/number-of-longest-increasing-subsequence/

## Brief

Return how many **strictly** increasing subsequences have the maximum length. Subsequences at different index sets count separately.

## Constraints

- `1 <= n <= 2000`; the answer fits in 32 bits

## Examples

```
[1,3,5,4,7]  ->  2    [1,3,4,7] and [1,3,5,7]
[2,2,2,2,2]  ->  5    each single element
```

## Signature

```java
public int findNumberOfLIS(int[] nums)
```

## Target complexity

O(n²).

## The skill

**Ramp:** Tier 1 `p-dp-lis-quadratic`, carrying a **count** alongside each length. When a predecessor gives a longer run, **replace** the count; when it ties, **add** to it. Summing counts over every index with the maximum length gives the answer — not the count at the last index.

## Hints

<details><summary>Hint 1</summary>

Two arrays: `len[i]` and `cnt[i]`.

</details>
