# Middle of the Linked List

LeetCode 876 · Easy · https://leetcode.com/problems/middle-of-the-linked-list/

## Brief

Return the middle node. With two middle nodes, return the **second**. The standard `ListNode` (`val`, `next`) is provided.

## Constraints

- `1 <= size <= 100`

## Examples

```
[1,2,3,4,5]    ->  [3,4,5]
[1,2,3,4,5,6]  ->  [4,5,6]
```

## Signature

```java
public ListNode middleNode(ListNode head)
```

## Target complexity

One pass.

## The skill

**Ramp:** Tier 1 `p-list-middle`. Fast/slow pointers; *which* middle you get depends
entirely on the loop condition. `while (fast != null && fast.next != null)` gives the second
middle, as required here.

## Hints

<details><summary>Hint 1</summary>

Trace a two-node list by hand.

</details>
