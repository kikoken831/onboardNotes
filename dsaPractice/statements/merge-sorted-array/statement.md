# Merge Sorted Array

LeetCode 88 · Easy · https://leetcode.com/problems/merge-sorted-array/

## Brief

`nums1` has length `m + n`: its first `m` slots are sorted values and the last `n` are
zeros (placeholders). `nums2` holds `n` sorted values. Merge `nums2` into `nums1` **in
place** so that `nums1` ends up fully sorted. Return nothing.

## Constraints

- `nums1.length == m + n`, `nums2.length == n`
- `0 <= m, n <= 200`, `1 <= m + n <= 200`
- both inputs sorted non-decreasing

## Examples

```
[1,2,3,0,0,0], m=3, [2,5,6], n=3  ->  [1,2,2,3,5,6]
[1], m=1, [], n=0                 ->  [1]
[0], m=0, [1], n=1                ->  [1]
```

## Signature

```java
public void merge(int[] nums1, int m, int[] nums2, int n)
```

## Target complexity

O(m + n) time, O(1) extra space.

## The skill

**Merge from the back.** Filling from the front overwrites values of `nums1` you haven't
read yet; filling from the back writes only into slots that are already free. That
reversal is the entire problem.

## Hints

<details><summary>Hint 1</summary>

Three indices: `i = m-1`, `j = n-1`, `w = m+n-1`. Take the larger of `nums1[i]` and
`nums2[j]`, write it at `w`.

</details>

<details><summary>Hint 2 — the termination condition</summary>

Loop while `j >= 0`. When `nums2` is exhausted, whatever remains of `nums1` is already in
place. When `nums1` is exhausted first, you still have to copy the rest of `nums2`.
Which of those does `m = 0` test?

</details>
