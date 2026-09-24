# Reverse Nodes in k-Group

LeetCode 25 · Hard · https://leetcode.com/problems/reverse-nodes-in-k-group/

## Brief

Reverse the nodes of the list `k` at a time. A final group of fewer than `k` nodes is left
**as is**. Only node links may change, not values. The standard `ListNode` (`val`, `next`) is provided.

## Constraints

- `1 <= k <= size <= 5000`

## Examples

```
[1,2,3,4,5], k=2  ->  [2,1,4,3,5]
[1,2,3,4,5], k=3  ->  [3,2,1,4,5]
```

## Signature

```java
public ListNode reverseKGroup(ListNode head, int k)
```

## Target complexity

O(n) time; the follow-up asks for O(1) space.

## The skill

**Ramp:** Tier 1 `p-list-reverse-sublist`, repeated. First **check** that `k` nodes remain,
then reverse them, reconnecting both ends: the group's old first node becomes its last and
the `prev` for the next group. Reversing before checking is the classic bug — it reverses
the tail.

## Hints

<details><summary>Hint 1</summary>

Reverse by pointing the group's first node at the node *after* the group, so the tail is
already attached when you're done.

</details>
