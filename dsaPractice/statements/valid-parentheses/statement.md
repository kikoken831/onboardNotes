# Valid Parentheses

LeetCode 20 · Easy · https://leetcode.com/problems/valid-parentheses/

## Brief

`s` contains only `()[]{}`. It's valid if every bracket is closed by the **same type**, in
the correct **order**.

## Constraints

- `1 <= s.length <= 10^4`

## Examples

```
"()[]{}"  ->  true
"(]"      ->  false
"([)]"    ->  false    right counts, wrong nesting
```

## Signature

```java
public boolean isValid(String s)
```

## Target complexity

O(n).

## The skill

The stack's reason for existing: the most recently opened bracket must close first. Three
separate failures, all tested: a closer with an empty stack (`"]"`), a closer of the wrong
type (`"(]"`), and openers left over at the end (`"("`). Counting brackets can't see
`"([)]"`.

## Hints

<details><summary>Hint 1</summary>

Push openers; on a closer, pop and compare.

</details>
