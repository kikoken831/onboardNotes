# Reorder List

LeetCode 143 · Medium · https://leetcode.com/problems/reorder-list/

## Brief

Reorder `L0 → L1 → … → Ln` into `L0 → Ln → L1 → Ln-1 → L2 → …` **in place**, by relinking
nodes. Return nothing. the standard `ListNode` (`val`, `next`) is provided.

## Constraints

- `1 <= size <= 5 * 10^4`

## Examples

```
[1,2,3,4]    ->  [1,4,2,3]
[1,2,3,4,5]  ->  [1,5,2,4,3]
```

## Signature

```java
public void reorderList(ListNode head)
```

## Target complexity

O(n) time, O(1) space.

## The skill

Three list primitives in sequence — **ramps:** Tier 1 `p-list-middle`, `p-list-reverse`,
and a merge: find the middle, reverse the second half, interleave. The bug the judge is
built to catch: forgetting to **cut** the first half (`slow.next = null`) leaves a cycle,
which the judge detects and names.

## Hints

<details><summary>Hint 1</summary>

Stop `fast` when `fast.next.next` is null so the first half is the longer one for odd
sizes.

</details>
