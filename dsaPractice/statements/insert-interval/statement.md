# Insert Interval

LeetCode 57 · Medium · https://leetcode.com/problems/insert-interval/

## Brief

`intervals` is sorted by start and non-overlapping. Insert `newInterval`, merging where
needed, and return the result **still sorted**.

## Constraints

- `0 <= n <= 10^4`

## Examples

```
[[1,3],[6,9]], [2,5]                        ->  [[1,5],[6,9]]
[[1,2],[3,5],[6,7],[8,10],[12,16]], [4,8]   ->  [[1,2],[3,10],[12,16]]
```

## Signature

```java
public int[][] insert(int[][] intervals, int[] newInterval)
```

## Target complexity

O(n) — the input is already sorted, so don't re-sort.

## The skill

Three phases: copy everything that ends **before** the new interval starts; absorb everything
that overlaps it, widening it; copy the rest. The output must be sorted, so an interval that
goes before all others (`[[3,5]], [1,2]`) or after all others must land in the right place.

## Hints

<details><summary>Hint 1</summary>

"Overlaps" here is `interval.start <= newEnd` once you're past the ones ending before `newStart`.

</details>
