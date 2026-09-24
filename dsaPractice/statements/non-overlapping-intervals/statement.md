# Non-overlapping Intervals

LeetCode 435 · Medium · https://leetcode.com/problems/non-overlapping-intervals/

## Brief

Return the minimum number of intervals to remove so the rest don't overlap. Touching
intervals (`[1,2]`, `[2,3]`) do **not** overlap here.

## Constraints

- `1 <= n <= 10^5`

## Examples

```
[[1,2],[2,3],[3,4],[1,3]]  ->  1
[[1,2],[1,2],[1,2]]        ->  2
```

## Signature

```java
public int eraseOverlapIntervals(int[][] intervals)
```

## Target complexity

O(n log n).

## The skill

The interval-scheduling greedy: sort by **end**, keep every interval that starts at or after
the last kept end. Earliest end leaves the most room. Sorting by start and keeping the first
fails on `[[1,100],[11,22],[1,11],[2,12]]`, which is pinned.

## Hints

<details><summary>Hint 1</summary>

Count the maximum number you can keep; the answer is `n − kept`.

</details>
