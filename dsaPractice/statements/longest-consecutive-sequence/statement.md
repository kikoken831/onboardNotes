# Longest Consecutive Sequence

LeetCode 128 · Medium · https://leetcode.com/problems/longest-consecutive-sequence/

## Brief

Return the length of the longest run of **consecutive integers** (`k, k+1, k+2, …`) whose
values all appear in `nums`, in any order. Duplicates count once.

## Constraints

- `0 <= n <= 10^5`
- `-10^9 <= nums[i] <= 10^9`

## Examples

```
[100,4,200,1,3,2]      ->  4    1, 2, 3, 4
[0,3,7,2,5,8,4,6,0,1]  ->  9
[]                     ->  0
```

## Signature

```java
public int longestConsecutive(int[] nums)
```

## Target complexity

O(n) — so no sorting.

## The skill

Put everything in a set, then only start counting from a value `x` whose `x - 1` is
**absent** — the start of a run. Every value is then visited by at most one run, which is
why the nested loop is still O(n). Starting a count from every value is O(n²).

## Hints

<details><summary>Hint 1</summary>

Sorting gives O(n log n) and is a fine first answer. Mind duplicates in the sorted scan.

</details>
