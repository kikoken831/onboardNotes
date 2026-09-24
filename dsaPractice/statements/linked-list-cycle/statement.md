# Linked List Cycle

LeetCode 141 · Easy · https://leetcode.com/problems/linked-list-cycle/

## Brief

Return whether the list contains a cycle. Test cases are written as `[values], pos`, where
`pos` is the index the tail links back to (`-1`: no cycle). **`pos` is not passed to you**
— you only get `head`. The standard `ListNode` (`val`, `next`) is provided.

## Constraints

- `0 <= size <= 10^4`

## Examples

```
[3,2,0,-4], pos=1  ->  true
[1,2],      pos=0  ->  true
[1],        pos=-1 ->  false
```

## Signature

```java
public boolean hasCycle(ListNode head)
```

## Target complexity

O(n) time, **O(1)** space (the follow-up).

## The skill

**Floyd's tortoise and hare**: `slow` moves one step, `fast` two. In a cycle, `fast` gains
one step per move and must land on `slow`; without one it runs off the end. A
`HashSet<ListNode>` of visited nodes is O(n) space and a fine first answer.

## Hints

<details><summary>Hint 1</summary>

Check `fast != null && fast.next != null` before every double step.

</details>
