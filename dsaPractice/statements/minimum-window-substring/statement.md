# Minimum Window Substring

LeetCode 76 · Hard · https://leetcode.com/problems/minimum-window-substring/

## Brief

Return the shortest substring of `s` that contains every character of `t` **including
duplicates**, or `""` if none exists. The answer is unique.

## Constraints

- `1 <= s.length, t.length <= 10^5`; upper- and lowercase letters

## Examples

```
s="ADOBECODEBANC", t="ABC"  ->  "BANC"
s="a",  t="a"               ->  "a"
s="a",  t="aa"              ->  ""
```

## Signature

```java
public String minWindow(String s, String t)
```

## Target complexity

O(|s| + |t|).

## The skill

The **shrinking** sliding window: expand right until the window is valid, then shrink left
while it stays valid, recording the best. Track validity with one counter — `missing` = how
many characters of `t` are still unmatched — so each step is O(1) instead of comparing 128
counts. The judge's `"aa"` pin checks you count duplicates, not just membership.

## Hints

<details><summary>Hint 1</summary>

`need[c]` goes negative for surplus characters; only a decrement from a *positive* count
reduces `missing`.

</details>
