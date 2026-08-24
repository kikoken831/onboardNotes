# Find the middle with fast and slow pointers

Tier 1 drill · linked-list · unlocks LC 876, 234, 143, 148, 141

## Brief

Return the middle node of the list. If the list has an even number of nodes, return the
**second** of the two middles. Return null for an empty list.

## Constraints

- `0 <= list length <= 100`

## Examples

```
[1,2,3,4,5]    ->  node 3         (returned as [3,4,5])
[1,2,3,4,5,6]  ->  node 4         (returned as [4,5,6])  <- SECOND middle
[]             ->  null
[1]            ->  node 1
[1,2]          ->  node 2
```

The judge compares the sublist starting at the node you return, so returning the wrong
middle shows up as a wrong-length list.

## Signature

```java
public ListNode middleNode(ListNode head)
```

## Target complexity

O(n) time, one pass, O(1) space.

## The skill

**Fast and slow.** Slow moves one step, fast moves two. When fast runs off the end,
slow is at the middle. No length count, one pass.

```java
ListNode slow = head, fast = head;
while (fast != null && fast.next != null) {
    slow = slow.next;
    fast = fast.next.next;
}
return slow;
```

**Which middle you get is decided by the loop condition, and this is the part to
actually understand rather than memorise:**

- `while (fast != null && fast.next != null)` — starting both at `head` — gives the
  **second** middle on even lengths. That is what this drill wants.
- `while (fast.next != null && fast.next.next != null)` gives the **first** middle,
  which is what you need when splitting a list for merge sort, because the first half
  must be non-empty or the recursion never terminates.

Getting this backwards in LC 148 produces infinite recursion, not a wrong answer, which
is a much worse debugging session. Know which variant you wrote.

The null-check order in `fast != null && fast.next != null` also matters: reverse it and
you dereference null on an even-length list. Java's `&&` short-circuits left to right,
which is what saves you.

## Hints

<details><summary>Hint 1 — trace both parities</summary>

Walk `[1,2,3]` and `[1,2,3,4]` by hand. Three and four nodes is enough to see the
difference; five is not more informative.

</details>
