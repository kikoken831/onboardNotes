# Target Sum

LeetCode 494 · Medium · https://leetcode.com/problems/target-sum/

## Brief

Put `+` or `-` before each number. Count the expressions that evaluate to `target`.

## Constraints

- `1 <= n <= 20`, `0 <= nums[i] <= 1000`, `-1000 <= target <= 1000`

## Examples

```
[1,1,1,1,1], 3  ->  5
[0,0,1], 1      ->  4     each 0 can be +0 or -0
```

## Signature

```java
public int findTargetSumWays(int[] nums, int target)
```

## Target complexity

O(n · sum).

## The skill

Brute force is `2ⁿ` and fine at `n = 20`. The insight: if `P` is the sum of the `+` numbers, `P = (sum + target) / 2`, so this is **counting subsets** with that sum — LC 416's knapsack counting instead of or-ing. Guard `|target| > sum` and odd `sum + target`, or the index goes negative. Zeros double the count (pinned).

## Hints

<details><summary>Hint 1</summary>

`d[t] += d[t − x]`, target descending.

</details>
