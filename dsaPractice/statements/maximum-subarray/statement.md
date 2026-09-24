# Maximum Subarray

LeetCode 53 · Medium · https://leetcode.com/problems/maximum-subarray/

## Brief

Return the largest sum of a non-empty contiguous subarray.

## Constraints

- `1 <= n <= 10^5`, `-10^4 <= nums[i] <= 10^4`

## Examples

```
[-2,1,-3,4,-1,2,1,-5,4]  ->  6     [4,-1,2,1]
[-3,-1,-2]               ->  -1    all negative
```

## Signature

```java
public int maxSubArray(int[] nums)
```

## Target complexity

O(n).

## The skill

**Kadane**: the best subarray ending at `i` either extends the one ending at `i − 1` or starts fresh at `i` — `cur = max(a[i], cur + a[i])`. The subarray is **non-empty**, so initialise from `a[0]`, not `0`: an all-negative array's answer is its largest element (pinned).

## Hints

<details><summary>Hint 1</summary>

State: the best sum of a subarray that **ends exactly** at `i`.

</details>
