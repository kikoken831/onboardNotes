# Linked List Cycle II

LeetCode 142 · Medium · https://leetcode.com/problems/linked-list-cycle-ii/

## Brief

Return the node where the cycle **begins**, or `null` if there is no cycle. As in 141, the
cycle is described by `pos`, which you don't get. **Do not modify the list** — the judge
checks. the standard `ListNode` (`val`, `next`) is provided.

## Constraints

- `0 <= size <= 10^4`

## Examples

```
[3,2,0,-4], pos=1  ->  the node at index 1
[1],        pos=-1 ->  null
```

The judge reports the index of the node you return (`-1` for `null`).

## Signature

```java
public ListNode detectCycle(ListNode head)
```

## Target complexity

O(n) time, O(1) space.

## The skill

After Floyd's pointers meet, reset one to `head` and advance both **one** step at a time;
they meet at the cycle's entrance. Why: if the entrance is `a` steps from the head and they
met `b` steps into the cycle, then `a` is congruent to "the rest of the cycle from the
meeting point" modulo its length. Be able to derive that — it's the whole question.

## Hints

<details><summary>Hint 1</summary>

The meeting point is **not** the entrance in general (`[1,2,3,4,5,6], pos=3`).

</details>
