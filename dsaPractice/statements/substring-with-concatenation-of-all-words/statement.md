# Substring with Concatenation of All Words

LeetCode 30 · Hard · https://leetcode.com/problems/substring-with-concatenation-of-all-words/

## Brief

All strings in `words` have the **same length**. Return every start index in `s` of a
substring formed by concatenating **all** of `words` exactly once each, in any order.
Duplicates in `words` must be used as many times as they appear. Indices in any order.

## Constraints

- `1 <= s.length <= 10^4`
- `1 <= words.length <= 5000`, `1 <= words[i].length <= 30`

## Examples

```
"barfoothefoobarman", ["foo","bar"]                    ->  [0,9]
"wordgoodgoodgoodbestword", ["word","good","best","word"]  ->  []
```

## Signature

```java
public List<Integer> findSubstring(String s, String[] words)
```

## Target complexity

O(|s| · wordLength): one sliding window per offset `0..wordLength-1`.

## The skill

A sliding window whose unit is a **word**, not a character. Because words don't have to
align with index 0, run the window once for each offset in `[0, wordLength)`. Inside, it is
Minimum Window's count-and-shrink, using a `Map<String, Integer>` in place of an `int[128]`.

The brute force — check every start with a fresh map — is O(|s| · total length) and a good
first version.

## Hints

<details><summary>Hint 1</summary>

When a word shows up more times than `words` allows, slide the left edge a word at a time
until it doesn't.

</details>
