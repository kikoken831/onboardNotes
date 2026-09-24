# Reverse Linked List

LeetCode 206 · Easy · https://leetcode.com/problems/reverse-linked-list/

## Brief

Reverse the list and return the new head. the standard `ListNode` (`val`, `next`) is provided.

## Constraints

- `0 <= size <= 5000`

## Examples

```
[1,2,3,4,5]  ->  [5,4,3,2,1]
[]           ->  []
```

## Signature

```java
public ListNode reverseList(ListNode head)
```

## Target complexity

O(n), O(1) space iteratively.

## The skill

**Ramp:** Tier 1 `p-list-reverse`. Save `next` first, repoint, advance — and the follow-up
wants the recursive version too: `head.next.next = head; head.next = null;`. The second line
is the one people forget, and the judge's cycle check will name it.

## Hints

<details><summary>Hint 1</summary>

Three variables: `prev`, `head`, and a temporary for `head.next`.

</details>
