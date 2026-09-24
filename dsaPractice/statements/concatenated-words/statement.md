# Concatenated Words

LeetCode 472 · Hard · https://leetcode.com/problems/concatenated-words/

## Brief

`words` are unique. Return every word that is formed by concatenating **at least two** shorter words from the list (repeats allowed), in any order.

## Constraints

- `1 <= words.length <= 10^4`, `1 <= words[i].length <= 30`

## Examples

```
["cat","cats","catsdogcats","dog","dogcatsdog","hippopotamuses","rat","ratcatdogcat"]
->  ["catsdogcats","dogcatsdog","ratcatdogcat"]
```

## Signature

```java
public List<String> findAllConcatenatedWordsInADict(String[] words)
```

## Target complexity

O(Σ L²) with word-break DP.

## The skill

LC 139 run for every word, with one trap: a word must not be "built" from **itself**. Process words shortest-first and only let already-seen (strictly shorter) words into the dictionary — that excludes the word itself automatically and prunes the search.

## Hints

<details><summary>Hint 1</summary>

A trie over the words makes each segment check a walk instead of a substring allocation.

</details>
