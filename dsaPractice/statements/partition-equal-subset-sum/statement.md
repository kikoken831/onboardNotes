# Partition Equal Subset Sum

LeetCode 416 · Medium · https://leetcode.com/problems/partition-equal-subset-sum/

## Brief

Can `nums` be split into two subsets with equal sums?

## Constraints

- `1 <= n <= 200`, `1 <= nums[i] <= 100`

## Examples

```
[1,5,11,5]  ->  true     [1,5,5] and [11]
[1,2,3,5]   ->  false
```

## Signature

```java
public boolean canPartition(int[] nums)
```

## Target complexity

O(n · sum).

## The skill

**Ramp:** Tier 1 `p-dp-knapsack-bool`. It's "is there a subset summing to `total/2`?" — 0/1 knapsack on booleans. An odd total fails immediately. In the 1-D table, iterate the target **downwards** so each number is used at most once; upwards silently turns it into unlimited reuse.

## Hints

<details><summary>Hint 1</summary>

`can[t] |= can[t − x]` for `t` from `half` down to `x`.

</details>
