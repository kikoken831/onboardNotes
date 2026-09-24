# Meeting Rooms

LeetCode 252 · Easy · Premium · https://leetcode.com/problems/meeting-rooms/

## Brief

*(Paraphrased — a Premium problem.)* Given meeting times `[start, end]`, can one person attend
all of them? A meeting ending at `t` and another starting at `t` do not conflict.

## Constraints

- `0 <= n <= 10^4`, `start < end`

## Examples

```
[[0,30],[5,10],[15,20]]  ->  false
[[7,10],[2,4]]           ->  true
```

## Signature

```java
public boolean canAttendMeetings(int[][] intervals)
```

## Target complexity

O(n log n).

## The skill

Sort by start; any meeting that starts before the previous one ends is a conflict. Only
**adjacent** meetings after sorting need checking. Back-to-back meetings are fine (`<`, not `<=`).

## Hints

<details><summary>Hint 1</summary>

The empty list is `true`.

</details>
