# Generate Parentheses

LeetCode 22 · Medium · https://leetcode.com/problems/generate-parentheses/

## Brief

Return every well-formed string of `n` pairs of parentheses, in any order.

## Constraints

- `1 <= n <= 8`

## Examples

```
3  ->  ["((()))","(()())","(())()","()(())","()()()"]
```

## Signature

```java
public List<String> generateParenthesis(int n)
```

## Target complexity

O(Catalan(n) · n).

## The skill

Backtracking with **pruning by invariant**: you may add `(` while any remain, and `)` only while
more are open than closed. Those two rules generate exactly the valid strings — no need to
generate all `2^2n` strings and filter.

## Hints

<details><summary>Hint 1</summary>

Track remaining opens and remaining closes; `)` is legal when `close > open`.

</details>
