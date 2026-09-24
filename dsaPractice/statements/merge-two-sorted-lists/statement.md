# Merge Two Sorted Lists

LeetCode 21 · Easy · https://leetcode.com/problems/merge-two-sorted-lists/

## Brief

Merge two sorted lists into one sorted list by **splicing** their nodes together. The standard `ListNode` (`val`, `next`) is provided.

## Constraints

- `0 <= size <= 50` each, sorted non-decreasing

## Examples

```
[1,2,4], [1,3,4]  ->  [1,1,2,3,4,4]
[], [0]           ->  [0]
```

## Signature

```java
public ListNode mergeTwoLists(ListNode list1, ListNode list2)
```

## Target complexity

O(m + n), O(1) extra.

## The skill

**Ramp:** Tier 1 `p-list-merge-sorted`. A dummy head, then attach the rest of whichever
list remains in **one** assignment — it's already sorted and already linked.

## Hints

<details><summary>Hint 1</summary>

`t.next = a != null ? a : b;`

</details>
