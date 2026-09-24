# Find Minimum in Rotated Sorted Array

LeetCode 153 · Medium · https://leetcode.com/problems/find-minimum-in-rotated-sorted-array/

## Brief

A sorted array of distinct values was rotated between 1 and `n` times. Return its minimum.

## Constraints

- `1 <= n <= 5000`, distinct

## Examples

```
[3,4,5,1,2]      ->  1
[11,13,15,17]    ->  11     rotated n times = not rotated
```

## Signature

```java
public int findMin(int[] nums)
```

## Target complexity

O(log n).

## The skill

Compare `mid` with the **right** end: `a[mid] > a[hi]` means the drop is to the right of
`mid`, otherwise at or left of it. Comparing with the left end fails on an unrotated array.

## Hints

<details><summary>Hint 1</summary>

`lo < hi`; `hi = mid` (not `mid - 1`) because `mid` may be the minimum.

</details>
