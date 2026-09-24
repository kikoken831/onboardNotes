# Word Search II

LeetCode 212 · Hard · https://leetcode.com/problems/word-search-ii/

## Brief

Return every word from `words` that can be traced in `board` through horizontally or vertically adjacent cells, each cell used at most once per word. Each word appears at most once in the answer, in any order.

## Constraints

- `1 <= m, n <= 12`, `1 <= words.length <= 3 * 10^4`, `1 <= words[i].length <= 10`

## Examples

```
board=[[o,a,a,n],[e,t,a,e],[i,h,k,r],[i,f,l,v]], words=["oath","pea","eat","rain"]
->  ["eat","oath"]
```

## Signature

```java
public List<String> findWords(char[][] board, String[] words)
```

## Target complexity

One board DFS guided by a trie, instead of one DFS per word.

## The skill

**Ramp:** Tier 1 `p-trie-collect-prefix` plus LC 79. Running Word Search once per word repeats the same board walks thousands of times. Put all the words in a **trie** and walk the board once: each DFS step follows a trie edge, and a path with no trie edge is abandoned immediately. Clear a word from its trie node when found, so it's reported **once** (`[[a,a]], ["aa"]` finds it from two starts — pinned).

## Hints

<details><summary>Hint 1</summary>

Store the whole word at its terminal trie node; no need to rebuild it from the path.

</details>
