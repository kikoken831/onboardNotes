# Count Unique Characters of All Substrings of a Given String

LeetCode 828 · Hard · https://leetcode.com/problems/count-unique-characters-of-all-substrings-of-a-given-string/

## Brief

For a string `t`, `countUniqueChars(t)` is how many characters appear in `t` **exactly once**.
Return the sum of `countUniqueChars` over **every** substring of `s`.

## Constraints

- `1 <= s.length <= 10^5`, uppercase letters; the answer fits in 32 bits

## Examples

```
"ABC"       ->  10
"ABA"       ->  8
"LEETCODE"  ->  92
```

## Signature

```java
public int uniqueLetterString(String s)
```

## Target complexity

O(n).

## The skill

**Count contributions, not substrings.** Character `s[i]` is unique in exactly those
substrings that include `i` but neither the previous nor the next occurrence of the same
letter: `(i − prev) × (next − i)` of them. Summing that over `i` is O(n). Enumerating
substrings is O(n²) at best and times out at `10^5`.

## Hints

<details><summary>Hint 1</summary>

For each letter, remember its last two positions. When a third occurrence arrives, the middle
one's contribution is now known.

</details>
