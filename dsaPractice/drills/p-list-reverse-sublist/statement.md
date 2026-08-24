# Reverse only positions m..n

Tier 1 drill · linked-list · unlocks LC 92, 25

## Brief

Reverse the nodes from position `m` to position `n` inclusive (1-indexed) and return
the head. Everything outside that range keeps its order. `1 <= m <= n <= length`.

One pass, in place.

## Constraints

- `1 <= list length <= 500`
- `1 <= m <= n <= list length`

## Examples

```
[1,2,3,4,5], m=2, n=4  ->  [1,4,3,2,5]
[1,2,3,4,5], m=1, n=5  ->  [5,4,3,2,1]
[5],         m=1, n=1  ->  [5]
[1,2],       m=1, n=2  ->  [2,1]
[1,2,3],     m=2, n=2  ->  [1,2,3]
```

## Signature

```java
public ListNode reverseSublist(ListNode head, int m, int n)
```

## Target complexity

O(n) time, one pass, O(1) space.

## The skill

The composition drill: **dummy head** plus **iterative reversal**, and the reason both
earlier drills exist.

`m = 1` reverses from the head, so the head changes — that is the dummy. The middle
section is the reversal you already know. The hard part is neither of those; it is
**reconnecting the three pieces**, and the reason it is hard is that after reversing,
the node you started from is now the *end* of the reversed section.

The clean way is the head-insertion technique. Park a pointer just before the section,
then repeatedly take the node after the current one and move it to the front of the
section:

```java
ListNode dummy = new ListNode(0, head);
ListNode prev = dummy;
for (int i = 1; i < m; i++) prev = prev.next;   // node before position m

ListNode cur = prev.next;
for (int i = 0; i < n - m; i++) {
    ListNode moved = cur.next;      // the node to relocate
    cur.next = moved.next;          // unlink it
    moved.next = prev.next;         // it becomes the new front
    prev.next = moved;
}
return dummy.next;
```

`cur` never moves. It starts as the first node of the section and ends as the last,
which is exactly right, and it means you never have to reattach anything at the end.

Trace `[1,2,3,4,5], m=2, n=4` line by line. This one does not become obvious by staring
at it — you have to walk it.

## Hints

<details><summary>Hint 1 — the loop count</summary>

`n - m` iterations, not `n - m + 1`. A section of length `k` needs `k - 1` relocations,
because the first node is already in place. `m == n` means zero iterations, which is
why `[1,2,3], m=2, n=2` is a pinned example.

</details>

<details><summary>Hint 2 — if head insertion feels alien</summary>

The alternative is: walk to the section, save `beforeSection` and `sectionStart`,
run the standard three-pointer reversal for `n - m + 1` nodes, then set
`beforeSection.next` to the new head and `sectionStart.next` to whatever came after.
More pointers, more chances to slip, but equally valid. Write both.

</details>
