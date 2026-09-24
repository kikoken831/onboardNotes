# Palindrome Linked List

LeetCode 234 · Easy · https://leetcode.com/problems/palindrome-linked-list/

## Brief

Return whether the list reads the same forwards and backwards. the standard `ListNode` (`val`, `next`) is provided.

## Constraints

- `1 <= size <= 10^5`, `0 <= val <= 9`

## Examples

```
[1,2,2,1]  ->  true
[1,2]      ->  false
```

## Signature

```java
public boolean isPalindrome(ListNode head)
```

## Target complexity

O(n) time; the follow-up asks for O(1) space.

## The skill

The O(1) version combines two primitives: find the middle (**ramp:** Tier 1
`p-list-middle`), reverse the second half in place, compare the halves — and, to be a good
citizen, reverse it back. Copying to an array is O(n) space and the obvious first answer.

Comparing values with `==` on `Integer`s is the other trap: fine for 0–9, wrong above 127.

## Hints

<details><summary>Hint 1</summary>

For an odd length, the middle node belongs to neither half.

</details>
