# Alien Dictionary

LeetCode 269 · Hard · Premium · https://leetcode.com/problems/alien-dictionary/

## Brief

*(Paraphrased — a Premium problem.)* `words` are sorted lexicographically in an alien language
that uses lowercase English letters in an unknown order. Return a string of the letters that
appear, in **any** order consistent with the sorting — or `""` if the input is inconsistent.

## Constraints

- `1 <= words.length <= 100`, `1 <= words[i].length <= 100`

## Examples

```
["wrt","wrf","er","ett","rftt"]  ->  "wertf"
["z","x","z"]                    ->  ""       contradiction
["abc","ab"]                     ->  ""       a word can't precede its own prefix
```

## Signature

```java
public String alienOrder(String[] words)
```

## Target complexity

O(total characters).

## The skill

Each **adjacent** pair of words contributes at most one edge: the first position where they
differ. Build that graph and topologically sort it (LC 210). Two traps, both pinned: a longer
word followed by its own prefix is invalid, and letters that never appear in any constraint must
still be output. The judge accepts any consistent order.

## Hints

<details><summary>Hint 1</summary>

Compare only neighbours — the order is transitive, so non-adjacent pairs add nothing new.

</details>
