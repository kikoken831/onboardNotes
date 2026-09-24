# Add Two Numbers

LeetCode 2 · Medium · https://leetcode.com/problems/add-two-numbers/

## Brief

Two non-negative integers are stored as linked lists of digits, **least significant digit
first**. Return their sum in the same form. There are no leading zeros except the number 0.
the standard `ListNode` (`val`, `next`) is provided.

## Constraints

- each list has 1 to 100 nodes; digits 0–9

## Examples

```
[2,4,3], [5,6,4]            ->  [7,0,8]            342 + 465 = 807
[9,9,9,9,9,9,9], [9,9,9,9]  ->  [8,9,9,9,0,0,0,1]
```

## Signature

```java
public ListNode addTwoNumbers(ListNode l1, ListNode l2)
```

## Target complexity

O(max(m, n)).

## The skill

Grade-school addition with a carry, walking both lists at once. Two traps: lists of
**different lengths** (treat a finished list as 0), and a **final carry** that needs one more
node (`[5] + [5] = [0,1]`). Converting to `long` fails — 100 digits don't fit.

## Hints

<details><summary>Hint 1</summary>

Loop `while (a != null || b != null || carry != 0)`, with a dummy head.

</details>
