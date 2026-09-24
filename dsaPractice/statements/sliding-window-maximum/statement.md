# Sliding Window Maximum

LeetCode 239 · Hard · https://leetcode.com/problems/sliding-window-maximum/

## Brief

Return the maximum of each window of size `k` as it slides from left to right.

## Constraints

- `1 <= k <= n <= 10^5`, `-10^4 <= nums[i] <= 10^4`

## Examples

```
[1,3,-1,-3,5,3,6,7], k=3  ->  [3,3,5,5,6,7]
[1], k=1                  ->  [1]
```

## Signature

```java
public int[] maxSlidingWindow(int[] nums, int k)
```

## Target complexity

O(n).

## The skill

The **monotonic deque**: keep indices whose values are decreasing. A new value evicts every
smaller value from the back (they can never be a maximum again while it's in the window), and
the front is evicted when it slides out. **Ramp:** Tier 1 `p-mono-next-greater`, turned into
a queue. A max-heap is O(n log n); re-scanning each window is O(nk).

## Hints

<details><summary>Hint 1</summary>

Store indices, not values — you need them to know when the front has left the window.

</details>
