# Employee Free Time

LeetCode 759 · Hard · Premium · https://leetcode.com/problems/employee-free-time/

## Brief

*(Paraphrased — a Premium problem.)* Each employee's working intervals are sorted and
non-overlapping. Return the **finite** intervals when **all** employees are free, in sorted
order. Intervals that merely touch leave no free time between them.

LeetCode's `Interval` class (`start`, `end`) is provided — don't declare it. Test cases are
written as nested arrays.

## Constraints

- `1 <= employees <= 50`, `1 <= intervals each <= 50`

## Examples

```
[[[1,2],[5,6]],[[1,3]],[[4,10]]]           ->  [[3,4]]
[[[1,3],[6,7]],[[2,4]],[[2,5],[9,12]]]     ->  [[5,6],[7,9]]
```

## Signature

```java
public List<Interval> employeeFreeTime(List<List<Interval>> schedule)
```

## Target complexity

O(N log N), or O(N log E) merging the per-employee lists with a heap.

## The skill

Everyone is free exactly in the **gaps of the merged union** of all working intervals. So:
flatten, sort by start, sweep keeping the furthest end seen, and each start beyond that end
opens a gap. The per-employee lists are already sorted, which is what the heap version uses. The
infinite intervals before the first and after the last meeting are excluded.

## Hints

<details><summary>Hint 1</summary>

LC 56 Merge Intervals, but you output the spaces between merged blocks.

</details>
