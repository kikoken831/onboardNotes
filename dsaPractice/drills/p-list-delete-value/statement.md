# Delete by value with a dummy head

Tier 1 drill · linked-list · unlocks LC 203, 83, 82, 19

## Brief

Remove every node whose value equals `val` and return the head of the resulting list.
Removing the current head must work, including when *every* node is removed.

## Constraints

- `0 <= list length <= 10^4`

## Examples

```
[1,2,6,3,4,5,6], val = 6  ->  [1,2,3,4,5]
[], val = 1               ->  []
[7,7,7], val = 7          ->  []
[7,7,7], val = 3          ->  [7,7,7]
[1], val = 1              ->  []
```

## Signature

```java
public ListNode deleteByValue(ListNode head, int val)
```

## Target complexity

O(n) time, O(1) space.

## The skill

**The dummy head.** Deleting a node requires a handle on the node *before* it — but the
real head has nothing before it, so head deletion becomes a special case, and a chain of
head deletions (`[7,7,7]` removing 7) becomes a nasty one.

The fix is to invent a node that sits before the head:

```java
ListNode dummy = new ListNode(0, head);
ListNode prev = dummy;
while (prev.next != null) {
    if (prev.next.val == val) prev.next = prev.next.next;  // unlink, do NOT advance
    else                      prev = prev.next;
}
return dummy.next;   // NOT head — head may itself have been deleted
```

Three rules, all load-bearing:

1. Allocate the dummy and point it at `head`.
2. **Return `dummy.next`, never `head`.** This is the whole point; returning `head`
   throws away the fix.
3. After unlinking, **do not advance `prev`** — the new `prev.next` has not been
   examined yet. Advancing is how `[7,7,7]` leaves a stray 7 behind.

Any time a linked-list problem says "the head might change", reach for the dummy
without thinking about it.

## Hints

<details><summary>Hint 1 — why not advance after unlinking</summary>

Trace `[7,7,7]` removing 7. If you advance after each unlink you check node 1, unlink
it, move to node 2, unlink it, and never look at node 3. One 7 survives.

</details>
