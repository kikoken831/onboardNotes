# Merge two sorted lists

Tier 1 drill · linked-list · unlocks LC 21, 23, 148, 88

## Brief

Given two lists sorted ascending, splice them into one sorted list and return its head.
Reuse the existing nodes — do not allocate new ones (other than a dummy).

## Constraints

- `0 <= each list length <= 50`
- sorted ascending

## Examples

```
[1,2,4] + [1,3,4]  ->  [1,1,2,3,4,4]
[] + []            ->  []
[] + [0]           ->  [0]
[1,2,3] + []       ->  [1,2,3]
[5] + [1]          ->  [1,5]
```

## Signature

```java
public ListNode mergeTwoSorted(ListNode a, ListNode b)
```

## Target complexity

O(n + m) time, O(1) extra space.

## The skill

The dummy head again, now used for **building** rather than deleting. Without it you
need a special case for "which list supplies the first node", and that case is pure
noise.

```java
ListNode dummy = new ListNode(), tail = dummy;
while (a != null && b != null) {
    if (a.val <= b.val) { tail.next = a; a = a.next; }
    else                { tail.next = b; b = b.next; }
    tail = tail.next;
}
tail.next = (a != null) ? a : b;   // attach whatever remains, in one line
return dummy.next;
```

Two details that matter more than they look:

- **`<=` not `<`.** With `<`, equal values still merge correctly but the relative order
  of equal elements flips. That is the difference between a stable merge and an
  unstable one, and merge sort (LC 148) depends on stability.
- **The tail attach is one line, not a loop.** Both remainders are already sorted and
  already linked; you just point at whichever is non-null. Writing two more while-loops
  here is the usual over-complication.

This is the merge step of merge sort, so it is also the core of LC 148 sort-list and
LC 23 merge-k-lists.

## Hints

<details><summary>Hint 1 — the empty cases</summary>

Both empty: the loop never runs, `tail.next = null`, returns null. One empty: the loop
never runs, the tail attach picks up the other list whole. No special cases needed —
convince yourself of that rather than adding guards.

</details>
