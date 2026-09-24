# Design Add and Search Words Data Structure

LeetCode 211 · Medium · https://leetcode.com/problems/design-add-and-search-words-data-structure/

## Brief

Implement `class WordDictionary` with `addWord(word)` and `search(word)`, where `word` may
contain `'.'`, matching **any one** letter.

## Constraints

- `1 <= length <= 25`; at most 2 dots per search; at most `10^4` calls

## Examples

```
["WordDictionary","addWord","addWord","addWord","search","search","search","search"]
[[],["bad"],["dad"],["mad"],["pad"],["bad"],[".ad"],["b.."]]
->  [null,null,null,null,false,true,true,true]
```

## Signature

```java
class WordDictionary {

    public WordDictionary() {

    }

    public void addWord(String word) {

    }

    public boolean search(String word) {

    }
}
```

## Target complexity

O(L) for an exact search; a dot branches into up to 26 children.

## The skill

**Ramp:** Tier 1 `p-trie-collect-prefix`. A trie with a **DFS** for wildcards: at a `'.'`, try
every existing child. The dot matches **exactly one** letter — `"."` does not match `"ab"` and
`".."` does not match `"a"` (pinned).

## Hints

<details><summary>Hint 1</summary>

Recurse on `(node, index)`; the answer at the end is the node's `isWord` flag.

</details>
