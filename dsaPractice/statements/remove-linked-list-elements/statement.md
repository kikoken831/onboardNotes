# Remove Linked List Elements

LeetCode 203 · Easy · https://leetcode.com/problems/remove-linked-list-elements/

## Brief

Remove every node whose value equals `val` and return the new head. The standard `ListNode` (`val`, `next`) is provided.

## Constraints

- `0 <= size <= 10^4`

## Examples

```
[1,2,6,3,4,5,6], val=6  ->  [1,2,3,4,5]
[7,7,7,7],       val=7  ->  []
```

## Signature

```java
public ListNode removeElements(ListNode head, int val)
```

## Target complexity

O(n).

## The skill

**Ramp:** Tier 1 `p-list-delete-value` — the same problem. A dummy head makes removing a run
of matching nodes at the front identical to removing them anywhere else.

## Hints

<details><summary>Hint 1</summary>

Don't advance after a removal.

</details>
