# First non-repeating character

Tier 1 drill · hash-counting · unlocks LC 387, 451

## Brief

Return the index of the first character in `s` that appears exactly once. Return `-1`
if there is no such character.

## Constraints

- `0 <= s.length <= 10^5`
- lowercase English letters only

## Examples

```
"leetcode"   ->  0   ('l')
"loveleetcode" -> 2  ('v')
"aabb"       -> -1
""           -> -1
"z"          ->  0
```

## Signature

```java
public int firstUniqueChar(String s)
```

## Target complexity

O(n) time, O(1) space.

## The skill

**Two passes over the same data.** Count first, then scan again and return on the first
count of 1. Trying to do it in one pass is the standard wrong turn — you cannot know a
character is unique until you have seen the whole string.

"Count in one pass, decide in a second pass" is the shape behind LC 387, 451, 383 and
a good chunk of the sliding-window family.

## Hints

<details><summary>Hint 1</summary>

Pass one fills `int[26]`. Pass two walks `s` in order and returns `i` the first time
`freq[s.charAt(i) - 'a'] == 1`.

</details>

<details><summary>Hint 2 — why the second pass walks the string, not the array</summary>

You need the *first by position in `s`*, not the alphabetically first. Scanning
`freq[0..25]` would give you the wrong answer for `"bbaa"` vs `"aabb"`. Check that you
know why.

</details>
