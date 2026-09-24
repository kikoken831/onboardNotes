# Find Peak Element

LeetCode 162 · Medium · https://leetcode.com/problems/find-peak-element/

## Brief

A peak is strictly greater than its neighbours; imagine `-∞` beyond both ends. Adjacent
values differ. Return the index of **any** peak.

## Constraints

- `1 <= n <= 1000`, `nums[i] != nums[i + 1]`

## Examples

```
[1,2,3,1]        ->  2
[1,2,1,3,5,6,4]  ->  1 or 5
```

## Signature

```java
public int findPeakElement(int[] nums)
```

## Target complexity

O(log n) — required.

## The skill

Binary search on an **unsorted** array. If `nums[m] < nums[m + 1]`, climbing right must reach
a peak (it can't climb forever — the edge is `-∞`), so a peak exists to the right. That
guarantee, not sortedness, is what makes discarding half safe. The judge accepts any peak.

## Hints

<details><summary>Hint 1</summary>

Same loop as LC 852.

</details>
