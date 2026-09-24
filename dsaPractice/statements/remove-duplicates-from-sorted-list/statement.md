# Remove Duplicates from Sorted List

LeetCode 83 · Easy · https://leetcode.com/problems/remove-duplicates-from-sorted-list/

## Brief

The list is sorted. Delete duplicates so each value appears once, and return the head. The standard `ListNode` (`val`, `next`) is provided.

## Constraints

- `0 <= size <= 300`

## Examples

```
[1,1,2]      ->  [1,2]
[1,1,2,3,3]  ->  [1,2,3]
```

## Signature

```java
public ListNode deleteDuplicates(ListNode head)
```

## Target complexity

O(n).

## The skill

**Ramp:** Tier 1 `p-list-delete-value`. When the next node duplicates the current one,
unlink it and **don't advance** — the new next may be a duplicate too (`[1,1,1]`).

## Hints

<details><summary>Hint 1</summary>

Advance only when `p.next.val != p.val`.

</details>
