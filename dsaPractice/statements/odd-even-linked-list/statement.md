# Odd Even Linked List

LeetCode 328 · Medium · https://leetcode.com/problems/odd-even-linked-list/

## Brief

Group the nodes at **odd positions** (1st, 3rd, …) followed by those at even positions,
keeping relative order within each group. Positions, not values. the standard `ListNode` (`val`, `next`) is provided.

## Constraints

- `0 <= size <= 10^4`

## Examples

```
[1,2,3,4,5]      ->  [1,3,5,2,4]
[2,1,3,5,6,4,7]  ->  [2,3,6,7,1,5,4]
```

## Signature

```java
public ListNode oddEvenList(ListNode head)
```

## Target complexity

O(n) time, O(1) space.

## The skill

**Ramp:** Tier 1 `p-list-partition`, partitioning by position instead of value. Two chains
built in one pass, then the odd tail points at the even head. The loop condition
`even != null && even.next != null` handles both parities.

## Hints

<details><summary>Hint 1</summary>

Remember the even chain's head before you start relinking.

</details>
