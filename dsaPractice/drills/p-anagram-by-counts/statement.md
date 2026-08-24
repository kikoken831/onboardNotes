# Anagram check without sorting

Tier 1 drill · hash-counting · unlocks LC 242, 49, 438, 567

## Brief

Return `true` if `t` is an anagram of `s` — same characters, same counts, any order.

Do it in O(n). Sorting both strings is O(n log n) and is not what this drill is for.

## Constraints

- `0 <= s.length, t.length <= 5 * 10^4`
- lowercase English letters only

## Examples

```
"anagram", "nagaram"  ->  true
"rat",     "car"      ->  false
"",        ""         ->  true
"a",       "ab"       ->  false
"aacc",    "ccac"     ->  false
```

## Signature

```java
public boolean anagramByCounts(String s, String t)
```

## Target complexity

O(n) time, O(1) space.

## The skill

The increment-then-decrement trick: count up for `s`, count down for `t`, and the
strings are anagrams exactly when every counter is back to zero. One array, one pass
each, no second data structure.

Check the lengths first. It is a one-line early exit that also saves you from the
subtle case where `t` is a prefix-superset of `s`.

## Hints

<details><summary>Hint 1</summary>

```java
if (s.length() != t.length()) return false;
int[] freq = new int[26];
for (int i = 0; i < s.length(); i++) {
    freq[s.charAt(i) - 'a']++;
    freq[t.charAt(i) - 'a']--;
}
for (int f : freq) if (f != 0) return false;
return true;
```

</details>

<details><summary>Hint 2 — the early-exit variant</summary>

You can bail the moment a counter goes negative on the decrement pass, but only
because you already checked the lengths match. Without that check the early exit is
wrong. Worth understanding rather than memorising.

</details>
