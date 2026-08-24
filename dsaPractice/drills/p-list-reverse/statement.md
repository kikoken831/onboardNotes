# Reverse a list iteratively

Tier 1 drill · linked-list · unlocks LC 206, 92, 25, 234, 143

## Brief

Reverse a singly linked list and return the new head.

Do it **iteratively**, in one pass, with O(1) extra space. The recursive version is
also worth writing afterwards, but the iterative one is the drill.

## Constraints

- `0 <= list length <= 5000`

## Examples

```
1 -> 2 -> 3 -> 4 -> 5   ->   5 -> 4 -> 3 -> 2 -> 1
1 -> 2                  ->   2 -> 1
(empty)                 ->   (empty)
7                       ->   7
```

## Signature

```java
public ListNode reverseList(ListNode head)
```

## Target complexity

O(n) time, O(1) space.

## The skill

Three pointers and one rule: **save `next` before you overwrite it.** Overwrite first
and you have severed the rest of the list with no way back.

```java
ListNode prev = null, cur = head;
while (cur != null) {
    ListNode next = cur.next;   // save FIRST
    cur.next = prev;            // then rewire
    prev = cur;                 // then advance
    cur = next;
}
return prev;                    // NOT head — head is now the tail
```

Two things worth noticing:

- **Return `prev`, not `head`.** After the loop `cur` is null and `prev` is the last
  node visited, which is the new head. Returning `head` gives you a one-element list
  and is the single most common slip.
- The empty list works without a special case: the loop never runs, `prev` is null,
  and null is the right answer.

This is the mechanical core of reverse-sublist (92), reverse-in-k-groups (25),
palindrome-linked-list (234) and reorder-list (143). Get it into muscle memory.

## Hints

<details><summary>Hint 1 — trace it</summary>

Draw `1 -> 2 -> 3` and step through, writing down `prev`, `cur` and `next` after every
line. Four iterations. Do this on paper before you type — it is the fastest way to stop
guessing at pointer order.

</details>

<details><summary>Hint 2 — the recursive form, afterwards</summary>

```java
if (head == null || head.next == null) return head;
ListNode newHead = reverseList(head.next);
head.next.next = head;
head.next = null;
return newHead;
```

Note `head.next.next = head` — the node after you points back at you. Elegant, but it
is O(n) stack and will overflow around 45,000 nodes on the default stack size.

</details>
