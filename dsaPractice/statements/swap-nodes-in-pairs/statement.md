# Swap Nodes in Pairs

LeetCode 24 · Medium · https://leetcode.com/problems/swap-nodes-in-pairs/

## Brief

Swap every two adjacent **nodes** (not just their values) and return the head. the standard `ListNode` (`val`, `next`) is provided.

## Constraints

- `0 <= size <= 100`

## Examples

```
[1,2,3,4]  ->  [2,1,4,3]
[1,2,3]    ->  [2,1,3]
```

## Signature

```java
public ListNode swapPairs(ListNode head)
```

## Target complexity

O(n), O(1) space iteratively.

## The skill

Three pointer rewires per pair — `a.next = b.next; b.next = a; prev.next = b;` — in an order
that never loses the rest of the list. Draw it. A dummy node makes the first pair like every
other.

## Hints

<details><summary>Hint 1</summary>

After swapping, the node that was first (`a`) is now the `prev` for the next pair.

</details>
