# Partition to K Equal Sum Subsets

LeetCode 698 · Medium · https://leetcode.com/problems/partition-to-k-equal-sum-subsets/

## Brief

Can `nums` be divided into `k` non-empty subsets with equal sums?

## Constraints

- `1 <= k <= n <= 16`, `1 <= nums[i] <= 10^4`

## Examples

```
[4,3,2,3,5,2,1], k=4  ->  true    (5) (1,4) (2,3) (2,3)
[1,2,3,4], k=3        ->  false
```

## Signature

```java
public boolean canPartitionKSubsets(int[] nums, int k)
```

## Target complexity

O(k · 2ⁿ) backtracking with pruning, or O(n · 2ⁿ) bitmask DP.

## The skill

Backtracking into `k` buckets of target `sum / k`, with the prunes that make it fast: sort **descending** (big items fail early), and if placing an item into an **empty** bucket fails, don't try the other empty buckets — they're interchangeable. The bitmask DP (`dp[mask]` = partial sum of the current bucket) is the clean alternative.

## Hints

<details><summary>Hint 1</summary>

`sum % k != 0` is an instant `false`.

</details>
