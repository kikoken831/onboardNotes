# Search in Rotated Sorted Array II

LeetCode 81 · Medium · https://leetcode.com/problems/search-in-rotated-sorted-array-ii/

## Brief

LC 33, but values may **repeat**. Return whether `target` is present.

## Constraints

- `1 <= n <= 5000`

## Examples

```
[2,5,6,0,0,1,2], 0  ->  true
[1,0,1,1,1], 0      ->  true
```

## Signature

```java
public boolean search(int[] nums, int target)
```

## Target complexity

O(log n) typical, **O(n) worst case** — unavoidable (the follow-up asks why).

## The skill

Duplicates break LC 33's test: with `a[lo] == a[mid] == a[hi]`, as in `[1,0,1,1,1]`, you can't
tell which half is sorted. The only safe move is to shrink both ends by one and look again —
which is why an all-equal array with one odd element out forces a linear scan.

## Hints

<details><summary>Hint 1</summary>

Add exactly one case to your LC 33 solution.

</details>
