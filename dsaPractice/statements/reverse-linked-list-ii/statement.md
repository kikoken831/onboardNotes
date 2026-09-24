# Reverse Linked List II

LeetCode 92 · Medium · https://leetcode.com/problems/reverse-linked-list-ii/

## Brief

Reverse the nodes from position `left` to `right` (1-indexed, inclusive) and return the
head. the standard `ListNode` (`val`, `next`) is provided.

## Constraints

- `1 <= left <= right <= size <= 500`

## Examples

```
[1,2,3,4,5], left=2, right=4  ->  [1,4,3,2,5]
[5], left=1, right=1          ->  [5]
```

## Signature

```java
public ListNode reverseBetween(ListNode head, int left, int right)
```

## Target complexity

One pass.

## The skill

**Ramp:** Tier 1 `p-list-reverse-sublist` — the same problem. The one-pass trick is
**head insertion**: repeatedly move the node after `cur` to just after `prev`. A dummy
handles `left = 1`.

## Hints

<details><summary>Hint 1</summary>

`cur` never changes: it starts as the segment's first node and ends as its last.

</details>
