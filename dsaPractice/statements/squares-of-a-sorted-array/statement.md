# Squares of a Sorted Array

LeetCode 977 · Easy · https://leetcode.com/problems/squares-of-a-sorted-array/

## Brief

`nums` is sorted non-decreasing and may contain negatives. Return the squares, also sorted
non-decreasing.

## Constraints

- `1 <= nums.length <= 10^4`
- `-10^4 <= nums[i] <= 10^4`, sorted

## Examples

```
[-4,-1,0,3,10]  ->  [0,1,9,16,100]
[-7,-3,2,3,11]  ->  [4,9,9,49,121]
```

## Signature

```java
public int[] sortedSquares(int[] nums)
```

## Target complexity

O(n) — the follow-up forbids re-sorting.

## The skill

The largest square is always at one of the two **ends**. Two pointers converging from the
outside, writing the output **from the back**, gives O(n). Squaring then sorting is
O(n log n) and correct; the judge's growth curve will tell you which you wrote.

## Hints

<details><summary>Hint 1</summary>

Compare `|nums[i]|` with `|nums[j]|`; the bigger one's square goes at `out[w--]`.

</details>
