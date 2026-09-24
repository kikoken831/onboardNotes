# Maximum Average Subarray I

LeetCode 643 · Easy · https://leetcode.com/problems/maximum-average-subarray-i/

## Brief

Return the maximum average of any contiguous subarray of length exactly `k`. Answers within
`10^-5` are accepted.

## Constraints

- `1 <= k <= n <= 10^5`, `-10^4 <= nums[i] <= 10^4`

## Examples

```
[1,12,-5,-6,50,3], k=4  ->  12.75
[5], k=1                ->  5.0
```

## Signature

```java
public double findMaxAverage(int[] nums, int k)
```

## Target complexity

O(n).

## The skill

The simplest **fixed** window: add the entering element, subtract the leaving one. Maximise
the *sum* and divide once at the end — dividing inside the loop costs precision for nothing.
Initialise the best from the first window, not `0`: every average can be negative.

## Hints

<details><summary>Hint 1</summary>

`s += nums[i] - nums[i - k]`

</details>
