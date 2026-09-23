# Valid Palindrome

LeetCode 125 · Easy · https://leetcode.com/problems/valid-palindrome/

## Brief

After lower-casing and removing every character that isn't a letter or **digit**, does `s`
read the same forwards and backwards?

## Constraints

- `1 <= s.length <= 2 * 10^5`, printable ASCII

## Examples

```
"A man, a plan, a canal: Panama"  ->  true
"race a car"                      ->  false
" "                               ->  true     empty after filtering
```

## Signature

```java
public boolean isPalindrome(String s)
```

## Target complexity

O(n) time, O(1) space.

## The skill

Two converging pointers that **skip** non-alphanumerics instead of building a filtered copy.
The traps the judge covers: digits are kept (`"0P"` is false), and an all-punctuation string
is `true`.

## Hints

<details><summary>Hint 1</summary>

`Character.isLetterOrDigit` and `Character.toLowerCase`.

</details>
