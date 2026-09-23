# Valid Anagram

LeetCode 242 · Easy · https://leetcode.com/problems/valid-anagram/

## Brief

Is `t` a rearrangement of exactly the letters of `s`?

## Constraints

- `1 <= s.length, t.length <= 5 * 10^4`, lowercase letters

## Examples

```
"anagram", "nagaram"  ->  true
"rat", "car"          ->  false
```

## Signature

```java
public boolean isAnagram(String s, String t)
```

## Target complexity

O(n) with 26 counters; O(n log n) by sorting both.

## The skill

**Ramp:** Tier 1 `p-anagram-by-counts`. One `int[26]`: `+1` for `s`, `-1` for `t`, then
all zero. Check the lengths first — without it, `"a"` vs `"ab"` indexes past the end of the
shorter string or passes by accident.

## Hints

<details><summary>Hint 1</summary>

Follow-up (Unicode): swap the array for a `HashMap<Character, Integer>`.

</details>
