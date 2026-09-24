# Is Subsequence

LeetCode 392 · Easy · https://leetcode.com/problems/is-subsequence/

## Brief

Is `s` a subsequence of `t` — can you delete characters from `t` (keeping order) to get `s`?

## Constraints

- `0 <= s.length <= 100`, `0 <= t.length <= 10^4`

## Examples

```
"abc", "ahbgdc"  ->  true
"axc", "ahbgdc"  ->  false
```

## Signature

```java
public boolean isSubsequence(String s, String t)
```

## Target complexity

O(|t|).

## The skill

Greedy two pointers: match each character of `s` at the **earliest** possible place in `t`.
Matching later can never help. The follow-up (a billion `s` strings against one `t`)
precomputes, for each character, the sorted positions in `t` and binary-searches the next one.

## Hints

<details><summary>Hint 1</summary>

The empty string is a subsequence of everything.

</details>
