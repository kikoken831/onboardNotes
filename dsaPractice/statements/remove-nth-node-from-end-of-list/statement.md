# Remove Nth Node From End of List

LeetCode 19 · Medium · https://leetcode.com/problems/remove-nth-node-from-end-of-list/

## Brief

Remove the `n`-th node from the **end** of the list and return the head. The standard `ListNode` (`val`, `next`) is provided.

## Constraints

- `1 <= size <= 30`, `1 <= n <= size`

## Examples

```
[1,2,3,4,5], n=2  ->  [1,2,3,5]
[1], n=1          ->  []
```

## Signature

```java
public ListNode removeNthFromEnd(ListNode head, int n)
```

## Target complexity

One pass.

## The skill

**Ramp:** Tier 1 `p-list-kth-from-end` — the same problem. Two pointers `n + 1` apart
starting from a **dummy** node, so that removing the head (`n == size`) needs no special case.

## Hints

<details><summary>Hint 1</summary>

When `fast` falls off the end, `slow` is the node *before* the one to remove.

</details>
