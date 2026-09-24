# Longest Word in Dictionary

LeetCode 720 · Medium · https://leetcode.com/problems/longest-word-in-dictionary/

## Brief

Return the longest word that can be built **one letter at a time**, where every prefix is also in `words`. Break ties by the **lexicographically smallest**; return `""` if none.

## Constraints

- `1 <= words.length <= 1000`, `1 <= words[i].length <= 30`

## Examples

```
["w","wo","wor","worl","world"]                      ->  "world"
["a","banana","app","appl","ap","apply","apple"]  ->  "apple"
```

## Signature

```java
public String longestWord(String[] words)
```

## Target complexity

O(Σ L²) with a set; O(Σ L) with a trie.

## The skill

A word qualifies when **every** proper prefix is present. With a set, check each prefix; with a trie, walk only nodes flagged as words. The tie-break is part of the answer — `"apple"` beats `"apply"`. Starting at a missing one-letter prefix (`["ab"]`) means no word qualifies.

## Hints

<details><summary>Hint 1</summary>

Sorting by length then alphabetically lets you build a set of "buildable" words incrementally.

</details>
