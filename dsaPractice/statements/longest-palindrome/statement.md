# Longest Palindrome

LeetCode 409 · Easy · https://leetcode.com/problems/longest-palindrome/

## Brief

Using the letters of `s` (each at most once, any order), how long is the longest
palindrome you can **build**? Case-sensitive: `"Aa"` is not a palindrome.

## Constraints

- `1 <= s.length <= 2000`
- upper- and lowercase English letters

## Examples

```
"abccccdd"  ->  7    e.g. "dccaccd"
"a"         ->  1
```

## Signature

```java
public int longestPalindrome(String s)
```

## Target complexity

O(n).

## The skill

Greedy from counts: every **pair** of a letter goes in (one each side), and **one** letter
with a leftover can sit in the middle — only one, however many odd counts there are.

Two classic bugs: adding 1 for *every* odd count, and forgetting that an odd count still
contributes `count - 1` letters as pairs.

## Hints

<details><summary>Hint 1</summary>

For each letter, `count / 2 * 2` letters go into pairs.

</details>
