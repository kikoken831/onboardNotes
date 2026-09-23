# First Unique Character in a String

LeetCode 387 · Easy · https://leetcode.com/problems/first-unique-character-in-a-string/

## Brief

Return the **index** of the first character that appears exactly once in `s`, or `-1` if
there is none.

## Constraints

- `1 <= s.length <= 10^5`
- lowercase English letters

## Examples

```
"leetcode"      ->  0
"loveleetcode"  ->  2
"aabb"          ->  -1
```

## Signature

```java
public int firstUniqChar(String s)
```

## Target complexity

O(n) time, O(1) space — two passes over the string.

## The skill

**Ramp:** Tier 1 `p-first-unique-char`. Count first, decide second. The second pass walks
the **string**, not the 26 counters — walking the counters finds the alphabetically first
unique letter, which is a different answer.

## Hints

<details><summary>Hint 1</summary>

Pass 1 fills `int[26]`. Pass 2 returns the first `i` whose character has count 1.

</details>
