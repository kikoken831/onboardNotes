# Palindromic Substrings

LeetCode 647 · Medium · https://leetcode.com/problems/palindromic-substrings/

## Brief

Count the substrings of `s` that are palindromes. Substrings at different positions count
separately even if they're equal.

## Constraints

- `1 <= s.length <= 1000`, lowercase

## Examples

```
"abc"  ->  3    "a", "b", "c"
"aaa"  ->  6    "a" ×3, "aa" ×2, "aaa"
```

## Signature

```java
public int countSubstrings(String s)
```

## Target complexity

O(n²) time, O(1) space.

## The skill

LC 5 with a counter: expand around all `2n − 1` centres and count every successful step.
Each expansion from a centre finds exactly the palindromes centred there, so nothing is
counted twice.

## Hints

<details><summary>Hint 1</summary>

Centre `c` in `0..2n-2`: `l = c / 2`, `r = l + c % 2`.

</details>
