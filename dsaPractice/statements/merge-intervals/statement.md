# Merge Intervals

LeetCode 56 · Medium · https://leetcode.com/problems/merge-intervals/

## Brief

Merge all overlapping intervals and return the non-overlapping result (any order). Intervals
that merely **touch** (`[1,4]` and `[4,5]`) overlap.

## Constraints

- `1 <= n <= 10^4`, `0 <= start <= end <= 10^4`

## Examples

```
[[1,3],[2,6],[8,10],[15,18]]  ->  [[1,6],[8,10],[15,18]]
[[1,4],[4,5]]                 ->  [[1,5]]
```

## Signature

```java
public int[][] merge(int[][] intervals)
```

## Target complexity

O(n log n).

## The skill

**Sort by start**, then one pass: extend the last merged interval while the next one starts
at or before its end. Extend to `max(end, next.end)` — `[[1,4],[2,3]]` is pinned because
taking `next.end` shrinks it. The input isn't sorted (`[[4,7],[1,4]]`).

## Hints

<details><summary>Hint 1</summary>

`Arrays.sort(a, (x, y) -> Integer.compare(x[0], y[0]))`

</details>
