# Smallest Range Covering Elements from K Lists

LeetCode 632 · Hard · https://leetcode.com/problems/smallest-range-covering-elements-from-k-lists/

## Brief

You have `k` sorted lists. Find the smallest range `[a, b]` that includes at least one number from **each** list. A range is smaller if it is narrower, or equally narrow and starts lower.

## Constraints

- `1 <= k <= 3500`, `1 <= each list length <= 50`

## Examples

```
[[4,10,15,24,26],[0,9,12,20],[5,18,22,30]]  ->  [20,24]
[[1,2,3],[1,2,3],[1,2,3]]                   ->  [1,1]
```

## Signature

```java
public int[] smallestRange(List<List<Integer>> nums)
```

## Target complexity

O(N log k).

## The skill

**Ramp:** Tier 1 `p-heap-merge-k`. Keep one pointer per list in a **min-heap** and track the current **maximum**. The window `[heap min, max]` always covers every list; advancing the list that holds the minimum is the only move that can shrink it. Stop when that list runs out. Only replace the best on a **strictly** narrower range — the tie-break prefers the earlier start, which the sweep visits first.

## Hints

<details><summary>Hint 1</summary>

It's k-way merge, with a running max beside the heap.

</details>
