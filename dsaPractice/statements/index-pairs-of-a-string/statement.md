# Index Pairs of a String

LeetCode 1065 · Easy · Premium · https://leetcode.com/problems/index-pairs-of-a-string/

## Brief

*(Paraphrased — a Premium problem.)* Return every `[i, j]` such that `text[i..j]` (inclusive)
is one of `words`, sorted by `i` then `j`. Matches may overlap.

## Constraints

- `1 <= text.length <= 100`, `1 <= words.length <= 20`, words unique

## Examples

```
"thestoryofleetcodeandme", ["story","fleet","leetcode"]  ->  [[3,7],[9,13],[10,17]]
"ababa", ["aba","ab"]                                     ->  [[0,1],[0,2],[2,3],[2,4]]
```

## Signature

```java
public int[][] indexPairs(String text, String[] words)
```

## Target complexity

O(n · L) with a trie walked from each start.

## The skill

A trie of the words, walked from **every** start index of `text`: each flagged node reached is a
match ending there, so one walk yields all words starting at `i` in increasing `j` — already in
the required order. Calling `startsWith` per word works too; then you must sort.

## Hints

<details><summary>Hint 1</summary>

Inclusive end: `[i, i + w.length() - 1]`.

</details>
