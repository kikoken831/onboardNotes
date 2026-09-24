# Count of Range Sum

LeetCode 327 · Hard · https://leetcode.com/problems/count-of-range-sum/

## Brief

Count the pairs `(i, j)`, `i <= j`, whose subarray sum `nums[i] + … + nums[j]` lies in `[lower, upper]` inclusive.

## Constraints

- `1 <= n <= 10^5`, `-2^31 <= nums[i] <= 2^31 - 1`

## Examples

```
[-2,5,-1], lower=-2, upper=2  ->  3    [0,0], [2,2], [0,2]
[0], 0, 0                     ->  1
```

## Signature

```java
public int countRangeSum(int[] nums, int lower, int upper)
```

## Target complexity

O(n log n).

## The skill

With prefix sums, the question becomes: count pairs `i < j` with `lower <= P[j] − P[i] <= upper`. **Merge sort** the prefix array: before merging two sorted halves, a two-pointer sweep counts, for each left element, how many right elements fall in its window. A Fenwick tree over compressed prefix values is the other O(n log n) route. Prefix sums **must be `long`** — the judge pins a case where `int` sums overflow.

## Hints

<details><summary>Hint 1</summary>

The O(n²) double loop over prefix sums is correct and a good first version.

</details>
