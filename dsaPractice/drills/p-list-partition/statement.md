# Stable partition around a value

Tier 1 drill · linked-list · unlocks LC 86, 328, 725

## Brief

Reorder the list so every node with value `< x` comes before every node with value
`>= x`. **Preserve the relative order** within each group. Return the new head.

## Constraints

- `0 <= list length <= 200`
- `-100 <= node values, x <= 100`

## Examples

```
[1,4,3,2,5,2], x = 3  ->  [1,2,2,4,3,5]
[2,1],         x = 2  ->  [1,2]
[],            x = 1  ->  []
[1,2,3],       x = 0  ->  [1,2,3]
[1,2,3],       x = 9  ->  [1,2,3]
```

## Signature

```java
public ListNode partitionList(ListNode head, int x)
```

## Target complexity

O(n) time, one pass, O(1) extra space.

## The skill

**Two dummy heads.** Build two separate lists as you walk — one for the small values,
one for the rest — then join them. Stability comes free because you always append.

```java
ListNode lo = new ListNode(), loTail = lo;
ListNode hi = new ListNode(), hiTail = hi;
for (ListNode p = head; p != null; p = p.next) {
    if (p.val < x) { loTail.next = p; loTail = p; }
    else           { hiTail.next = p; hiTail = p; }
}
hiTail.next = null;        // <- the one that will bite you
loTail.next = hi.next;
return lo.next;
```

**`hiTail.next = null` is mandatory.** The last node you appended to the high list still
points at whatever followed it in the *original* list. Forget this line and you get a
cycle, and the judge will hang or report a wildly wrong list rather than a near miss.

This is the "build several lists then splice" shape, which is also LC 328
odd-even-linked-list and LC 725 split-into-parts.

## Hints

<details><summary>Hint 1 — spot the cycle</summary>

Run `[1,4,3,2,5,2], x = 3` without the `hiTail.next = null` line and trace where node 5
points. That is the bug, and seeing it once is worth more than being told.

</details>

<details><summary>Hint 2 — why stability is free</summary>

You only ever append to a tail, never insert, and you walk the source in order. So
within each output list the nodes appear in source order by construction. Being able to
say that in one sentence is the interview answer.

</details>
