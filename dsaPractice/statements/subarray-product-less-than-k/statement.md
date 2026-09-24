# Subarray Product Less Than K

LeetCode 713 · Medium · https://leetcode.com/problems/subarray-product-less-than-k/

## Brief

Count the contiguous subarrays whose product is **strictly less** than `k`. All values are
positive.

## Constraints

- `1 <= n <= 3 * 10^4`, `1 <= nums[i] <= 1000`, `0 <= k <= 10^6`

## Examples

```
[10,5,2,6], k=100  ->  8
[1,2,3],    k=0    ->  0
```

## Signature

```java
public int numSubarrayProductLessThanK(int[] nums, int k)
```

## Target complexity

O(n).

## The skill

The **counting** window: for each right edge, every subarray ending there and starting at or
after `l` qualifies — that's `r − l + 1` of them. The guard `k <= 1 → 0` isn't optional: with
`k = 1` the shrink loop would divide the product down past an empty window.

## Hints

<details><summary>Hint 1</summary>

Why is `r - l + 1` exactly the number of new valid subarrays when `r` advances?

</details>
