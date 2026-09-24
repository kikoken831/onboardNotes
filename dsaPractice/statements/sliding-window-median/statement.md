# Sliding Window Median

LeetCode 480 · Hard · https://leetcode.com/problems/sliding-window-median/

## Brief

Return the median of each window of size `k`. For even `k` the median is the mean of the two
middle values. Answers within `10^-5` are accepted.

## Constraints

- `1 <= k <= n <= 10^5`
- `-2^31 <= nums[i] <= 2^31 - 1`

## Examples

```
[1,3,-1,-3,5,3,6,7], k=3  ->  [1.0,-1.0,-1.0,3.0,5.0,6.0]
[1,2,3,4,2,3,1,4,2], k=3  ->  [2.0,3.0,3.0,3.0,2.0,3.0,2.0]
```

## Signature

```java
public double[] medianSlidingWindow(int[] nums, int k)
```

## Target complexity

O(n log k).

## The skill

LC 295's **two heaps** (a max-heap for the low half, a min-heap for the high half), plus
the hard part: **removing** the element that leaves the window. `PriorityQueue.remove(x)` is
O(k); a pair of `TreeMap` multisets, or two heaps with lazy deletion, gets it to O(log k).

**Overflow:** the mean of two ints near `2^31 - 1` overflows if you add them as `int`. The
judge includes `[2147483647,2147483647]`.

## Hints

<details><summary>Hint 1</summary>

`((long) a + b) / 2.0`, or `a / 2.0 + b / 2.0`.

</details>
