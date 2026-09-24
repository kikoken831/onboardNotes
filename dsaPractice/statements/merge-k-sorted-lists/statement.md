# Merge k Sorted Lists

LeetCode 23 · Hard · https://leetcode.com/problems/merge-k-sorted-lists/

## Brief

Merge an array of `k` sorted lists into one sorted list. Any list may be empty, and the
array itself may be empty. the standard `ListNode` (`val`, `next`) is provided.

## Constraints

- `0 <= k <= 10^4`, total nodes `<= 10^4`

## Examples

```
[[1,4,5],[1,3,4],[2,6]]  ->  [1,1,2,3,4,4,5,6]
[]                       ->  []
[[]]                     ->  []
```

## Signature

```java
public ListNode mergeKLists(ListNode[] lists)
```

## Target complexity

O(N log k).

## The skill

**Ramp:** Tier 1 `p-heap-merge-k`. A min-heap of list **heads**: poll the smallest, push its
successor. Or divide and conquer — merge pairs, halving `k` each round. Merging lists one
at a time into an accumulator is O(N·k). Skip `null` heads before offering them to the heap.

## Hints

<details><summary>Hint 1</summary>

`new PriorityQueue<ListNode>((x, y) -> Integer.compare(x.val, y.val))`

</details>
