# Interval List Intersections

LeetCode 986 · Medium · https://leetcode.com/problems/interval-list-intersections/

## Brief

Each list is sorted and pairwise disjoint. Return the intersection of the two lists, sorted.
Closed intervals: `[3,5]` and `[5,9]` intersect in `[5,5]`.

## Constraints

- `0 <= lengths <= 1000`

## Examples

```
[[0,2],[5,10],[13,23],[24,25]], [[1,5],[8,12],[15,24],[25,26]]
->  [[1,2],[5,5],[8,10],[15,23],[24,24],[25,25]]
```

## Signature

```java
public int[][] intervalIntersection(int[][] firstList, int[][] secondList)
```

## Target complexity

O(m + n).

## The skill

Two pointers, one per list. The overlap of the current pair is `[max(starts), min(ends)]` if
non-empty. Then advance whichever interval **ends first** — it can't intersect anything later.

## Hints

<details><summary>Hint 1</summary>

Advancing by start rather than end is the bug.

</details>
