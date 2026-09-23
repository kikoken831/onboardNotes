# Ransom Note

LeetCode 383 · Easy · https://leetcode.com/problems/ransom-note/

## Brief

Can `ransomNote` be built from the letters of `magazine`? Each magazine letter can be used
**at most once**.

## Constraints

- `1 <= ransomNote.length, magazine.length <= 10^5`
- lowercase English letters

## Examples

```
"a",  "b"    ->  false
"aa", "ab"   ->  false
"aa", "aab"  ->  true
```

## Signature

```java
public boolean canConstruct(String ransomNote, String magazine)
```

## Target complexity

O(n + m) time, O(1) space (26 counters).

## The skill

**Ramp:** Tier 1 `p-char-frequency`. Count the supply, then spend it; fail the moment a
count goes negative.

The bug to avoid is checking *membership* (`magazine.contains(c)`) instead of *quantity* —
that accepts `"aa"` from `"ab"`.

## Hints

<details><summary>Hint 1</summary>

`int[26]` from `magazine`. Decrement for each note letter.

</details>
