# Meeting Rooms II

LeetCode 253 · Medium · Premium · https://leetcode.com/problems/meeting-rooms-ii/

## Brief

*(Paraphrased — a Premium problem.)* Return the minimum number of rooms needed to hold all the
meetings. A room freed at `t` can host a meeting starting at `t`.

## Constraints

- `1 <= n <= 10^4`

## Examples

```
[[0,30],[5,10],[15,20]]  ->  2
[[7,10],[2,4]]           ->  1
```

## Signature

```java
public int minMeetingRooms(int[][] intervals)
```

## Target complexity

O(n log n).

## The skill

The answer is the maximum number of meetings in progress at once. Two ways: a **min-heap of
end times** (sort by start; reuse the room that frees up earliest), or a **sweep line** over
sorted starts and sorted ends. For `[[1,10],[2,3],[4,5],[6,7]]` — pinned — "count overlaps with
the previous meeting" undercounts.

## Hints

<details><summary>Hint 1</summary>

Process starts and ends as events; at equal times, handle ends first.

</details>
