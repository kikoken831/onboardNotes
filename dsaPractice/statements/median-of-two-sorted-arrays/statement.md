# Median of Two Sorted Arrays

LeetCode 4 · Hard · https://leetcode.com/problems/median-of-two-sorted-arrays/

## Brief

Return the median of the two sorted arrays combined. Either may be empty (not both).
Answers within `10^-5` are accepted.

## Constraints

- `0 <= m, n <= 1000`, `1 <= m + n <= 2000`

## Examples

```
[1,3], [2]    ->  2.0
[1,2], [3,4]  ->  2.5
```

## Signature

```java
public double findMedianSortedArrays(int[] nums1, int[] nums2)
```

## Target complexity

**O(log(min(m, n)))** — the stated requirement.

## The skill

Binary-search a **partition**: take `i` elements from the shorter array and `half − i` from the
longer, so the left side holds half of everything. The partition is right when both left maxima
are `<=` both right minima; then the median is read off the four boundary values. Sentinels
(`±∞`) for empty sides remove every edge case. Merging (O(m + n)) is correct and a fine first
version.

## Hints

<details><summary>Hint 1</summary>

Search over the **shorter** array, so `j = half − i` never goes negative.

</details>
