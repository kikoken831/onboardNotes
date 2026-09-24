# Permutation in String

LeetCode 567 · Medium · https://leetcode.com/problems/permutation-in-string/

## Brief

Does `s2` contain a **contiguous** substring that is a permutation of `s1`?

## Constraints

- `1 <= s1.length, s2.length <= 10^4`, lowercase

## Examples

```
"ab", "eidbaooo"  ->  true     "ba"
"ab", "eidboaoo"  ->  false
```

## Signature

```java
public boolean checkInclusion(String s1, String s2)
```

## Target complexity

O(|s2|) with a fixed-size window and 26 counts.

## The skill

A **fixed-size** window of length `|s1|` sliding across `s2`, maintaining letter counts.
Comparing all 26 counts at each step is O(26n) and perfectly good; tracking how many letters
currently *differ* makes each step O(1). Mind `|s1| > |s2|`.

## Hints

<details><summary>Hint 1</summary>

Adding `s2[r]` and removing `s2[r - n]` is all that changes per step.

</details>
