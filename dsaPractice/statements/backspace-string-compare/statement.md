# Backspace String Compare

LeetCode 844 · Easy · https://leetcode.com/problems/backspace-string-compare/

## Brief

`#` is a backspace. Return whether `s` and `t` are equal after typing both into empty text
editors. Backspacing an empty editor does nothing.

## Constraints

- `1 <= s.length, t.length <= 200`; lowercase letters and `#`

## Examples

```
"ab#c", "ad#c"  ->  true     both "ac"
"ab##", "c#d#"  ->  true     both ""
"a#c",  "b"     ->  false
```

## Signature

```java
public boolean backspaceCompare(String s, String t)
```

## Target complexity

O(n) time; the follow-up asks for **O(1) space**.

## The skill

Building both results with a stack (or `StringBuilder`) is the obvious answer. The O(1)
answer walks both strings **backwards**, because a `#` only ever affects characters to its
left — keep a count of pending backspaces and skip that many real characters.

## Hints

<details><summary>Hint 1</summary>

Write a helper that moves an index to the next character that survives.

</details>
