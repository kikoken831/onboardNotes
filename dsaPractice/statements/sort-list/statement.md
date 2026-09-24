# Sort List

LeetCode 148 · Medium · https://leetcode.com/problems/sort-list/

## Brief

Sort the linked list in ascending order and return its head. the standard `ListNode` (`val`, `next`) is provided.

## Constraints

- `0 <= size <= 5 * 10^4`

## Examples

```
[4,2,1,3]     ->  [1,2,3,4]
[-1,5,3,4,0]  ->  [-1,0,3,4,5]
```

## Signature

```java
public ListNode sortList(ListNode head)
```

## Target complexity

O(n log n) time; the follow-up asks for O(1) space (bottom-up merge sort).

## The skill

**Merge sort** is the natural linked-list sort: splitting at the middle is O(n) with
fast/slow pointers and merging needs no extra array. Start `fast` at `head.next` so a
two-node list splits into two one-node halves — starting at `head` recurses forever.

## Hints

<details><summary>Hint 1</summary>

Copying values into an array, sorting and rebuilding passes — and dodges the question.

</details>
