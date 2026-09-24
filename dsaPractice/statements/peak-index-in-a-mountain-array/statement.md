# Peak Index in a Mountain Array

LeetCode 852 · Medium · https://leetcode.com/problems/peak-index-in-a-mountain-array/

## Brief

`arr` strictly increases to a single peak, then strictly decreases. Return the peak's index.

## Constraints

- `3 <= n <= 10^5`, `0 <= arr[i] <= 10^6`, a valid mountain

## Examples

```
[0,1,0]      ->  1
[0,10,5,2]   ->  1
```

## Signature

```java
public int peakIndexInMountainArray(int[] arr)
```

## Target complexity

O(log n).

## The skill

**Ramp:** Tier 1 `p-bs-predicate-flip`. The predicate `arr[m] < arr[m + 1]` ("still climbing")
is true then false; the peak is the first index where it's false. Nothing is searched for — the
array's **shape** is.

## Hints

<details><summary>Hint 1</summary>

`lo < hi`, compare `arr[m]` with `arr[m + 1]` (always in range because `m < hi`).

</details>
