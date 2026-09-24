# Prefix and Suffix Search

LeetCode 745 · Hard · https://leetcode.com/problems/prefix-and-suffix-search/

## Brief

Implement `class WordFilter`: the constructor takes the dictionary `words`; `f(pref, suff)`
returns the **largest index** of a word with that prefix **and** that suffix, or `-1`.

## Constraints

- `1 <= words.length <= 10^4`, `1 <= words[i].length <= 7`; at most `10^4` calls

## Examples

```
["WordFilter","f"]
[[["apple"]],["a","e"]]
->  [null,0]
```

## Signature

```java
class WordFilter {

    public WordFilter(String[] words) {

    }

    public int f(String pref, String suff) {

    }
}
```

## Target complexity

O(1) per query after O(N · L²) preprocessing.

## The skill

Words are at most 7 letters, so **precompute every (prefix, suffix) pair** into a map — ~50 keys
per word. Iterating words in order means a later index overwrites an earlier one, which gives
"largest index" for free (duplicated words are pinned). The trie alternative stores
`suffix + '#' + word` for every suffix and walks `suff + '#' + pref`.

## Hints

<details><summary>Hint 1</summary>

The key `pref + "#" + suff` works because `#` can't appear in a word.

</details>
