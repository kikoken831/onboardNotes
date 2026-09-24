# Maximum Product Subarray

LeetCode 152 · Medium · https://leetcode.com/problems/maximum-product-subarray/

## Brief

Return the largest product of a non-empty contiguous subarray. The answer fits in 32 bits.

## Constraints

- `1 <= n <= 2 * 10^4`, `-10 <= nums[i] <= 10`

## Examples

```
[2,3,-2,4]  ->  6
[-2,3,-4]   ->  24     two negatives
```

## Signature

```java
public int maxProduct(int[] nums)
```

## Target complexity

O(n).

## The skill

Kadane doesn't work directly: a negative number turns the **smallest** product into the largest. Track both the max and the min product ending at `i`, and swap roles on a negative. Zeros reset both.

## Hints

<details><summary>Hint 1</summary>

`hi = max(x, hi·x, lo·x)` and `lo = min(x, hi·x, lo·x)`, computed from the *old* values.

</details>
