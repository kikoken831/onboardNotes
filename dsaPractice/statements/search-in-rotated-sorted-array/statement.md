# Search in Rotated Sorted Array

LeetCode 33 · Medium · https://leetcode.com/problems/search-in-rotated-sorted-array/

## Brief

A sorted array of **distinct** values was rotated at an unknown pivot (`[0,1,2,4,5,6,7]` →
`[4,5,6,7,0,1,2]`). Return the index of `target`, or `-1`.

## Constraints

- `1 <= n <= 5000`, distinct

## Examples

```
[4,5,6,7,0,1,2], 0  ->  4
[4,5,6,7,0,1,2], 3  ->  -1
```

## Signature

```java
public int search(int[] nums, int target)
```

## Target complexity

O(log n).

## The skill

At every `mid`, **one half is sorted** — `a[lo] <= a[mid]` tells you which. If the target lies
within the sorted half's range, search there; otherwise search the other half. The `<=` (not
`<`) matters when `lo == mid`, the two-element case pinned as `[3,1], 1`.

## Hints

<details><summary>Hint 1</summary>

Alternatively: find the rotation point first (LC 153), then binary-search the right side.

</details>
