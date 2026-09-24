# Longest Palindromic Substring

LeetCode 5 · Medium · https://leetcode.com/problems/longest-palindromic-substring/

## Brief

Return a longest substring of `s` that is a palindrome. If several share the maximum length,
**any** of them is accepted.

## Constraints

- `1 <= s.length <= 1000`; letters and digits

## Examples

```
"babad"  ->  "bab"    ("aba" is also correct)
"cbbd"   ->  "bb"
```

## Signature

```java
public String longestPalindrome(String s)
```

## Target complexity

O(n²) time, O(1) space by expanding around centres. (Manacher's is O(n); not expected.)

## The skill

**Expand around each centre** — and there are `2n − 1` of them: every character *and* every
gap between two characters. Forgetting the even centres misses `"bb"`. The DP table
(`dp[i][j]` = `s[i..j]` is a palindrome) is the same complexity with O(n²) memory.

The judge checks the property (a palindromic substring of maximal length), not one string.

## Hints

<details><summary>Hint 1</summary>

For each `c`, expand `(c, c)` and `(c, c + 1)`.

</details>
