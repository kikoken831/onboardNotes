# Lemonade Change

LeetCode 860 · Easy · https://leetcode.com/problems/lemonade-change/

## Brief

Lemonade costs $5. Customers pay in order with a $5, $10 or $20 bill, and you start with no
change. Return whether you can give every customer correct change.

## Constraints

- `1 <= bills.length <= 10^5`
- `bills[i]` is 5, 10 or 20

## Examples

```
[5,5,5,10,20]   ->  true
[5,5,10,10,20]  ->  false   the last customer needs $15; you hold two $10s
```

## Signature

```java
public boolean lemonadeChange(int[] bills)
```

## Target complexity

O(n), O(1) space.

## The skill

A greedy **choice**: for a $20 you can give `10+5` or `5+5+5`. Always prefer `10+5` — $5s
are the only bill that can change a $10, so spending them when a $10 would do can strand a
later customer. The judge includes the case where the other order fails.

## Hints

<details><summary>Hint 1</summary>

You never need to give out a $20, so track only counts of $5 and $10.

</details>
