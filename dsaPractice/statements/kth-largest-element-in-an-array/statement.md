# Kth Largest Element in an Array

LeetCode 215 · Medium · https://leetcode.com/problems/kth-largest-element-in-an-array/

## Brief

Return the `k`-th largest element — by sorted position, so duplicates count (`[2,2]`, k=2 → 2).

## Constraints

- `1 <= k <= n <= 10^5`

## Examples

```
[3,2,1,5,6,4], k=2        ->  5
[3,2,3,1,2,4,5,5,6], k=4  ->  4
```

## Signature

```java
public int findKthLargest(int[] nums, int k)
```

## Target complexity

O(n log k) with a heap; O(n) average with quickselect.

## The skill

**Ramp:** Tier 1 `p-heap-k-smallest`, mirrored: a **min**-heap of size `k` holds the `k` largest seen, and its top is the answer. Quickselect (partition around a pivot, recurse into one side) is O(n) on average — randomise the pivot, or sorted input makes it O(n²).

## Hints

<details><summary>Hint 1</summary>

Sorting is O(n log n) and correct — and the question asks you to beat it.

</details>
