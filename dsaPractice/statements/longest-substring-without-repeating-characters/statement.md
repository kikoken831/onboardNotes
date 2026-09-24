# Longest Substring Without Repeating Characters

LeetCode 3 · Medium · https://leetcode.com/problems/longest-substring-without-repeating-characters/

## Brief

Return the length of the longest **substring** (contiguous) with no repeated character.

## Constraints

- `0 <= s.length <= 5 * 10^4`; letters, digits, symbols and spaces

## Examples

```
"abcabcbb"  ->  3    "abc"
"bbbbb"     ->  1
"pwwkew"    ->  3    "wke" — "pwke" is a subsequence, not a substring
```

## Signature

```java
public int lengthOfLongestSubstring(String s)
```

## Target complexity

O(n).

## The skill

The canonical **variable-size sliding window**: grow the right edge; when the new character
repeats inside the window, move the left edge past its previous occurrence. The trap is
`"abba"`: at the second `a`, its last index (0) is *behind* the window, and jumping to it
moves the left edge **backwards**. Only jump when `last[c] >= left`.

## Hints

<details><summary>Hint 1</summary>

Keep `last[c]` = most recent index of `c`.

</details>
