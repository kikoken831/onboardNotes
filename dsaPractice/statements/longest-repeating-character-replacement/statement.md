# Longest Repeating Character Replacement

LeetCode 424 · Medium · https://leetcode.com/problems/longest-repeating-character-replacement/

## Brief

You may change at most `k` characters of `s` (uppercase letters). Return the length of the
longest substring you can make consist of a single repeated letter.

## Constraints

- `1 <= s.length <= 10^5`, `0 <= k <= s.length`

## Examples

```
"ABAB",    k=2  ->  4
"AABABBA", k=1  ->  4
```

## Signature

```java
public int characterReplacement(String s, int k)
```

## Target complexity

O(n).

## The skill

A window is fixable when `length − (count of its most frequent letter) <= k`. The clever
part: `maxf` **never needs to decrease** when the window shrinks. A stale, too-large `maxf`
only keeps the window from shrinking when it should — it never lets the *answer* exceed
something that was genuinely valid once. Recomputing the max each step is O(26n) and also
fine.

## Hints

<details><summary>Hint 1</summary>

Replacements needed = window length − its most common letter's count.

</details>
