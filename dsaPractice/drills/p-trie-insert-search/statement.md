# Insert and search with a 26-child trie

Tier 1 drill · trie · unlocks LC 208, 211, 212, 720

## Brief

Given a list of `words` to insert and a list of `queries`, return a `List<String>` with
one entry per query: `"word"` if the query was inserted as a complete word,
`"prefix"` if it is a strict prefix of some inserted word but not itself a word, and
`"none"` otherwise.

Build an actual trie. A `HashSet` would pass the word check but not the prefix check in
the right complexity, and the trie is the point.

## Constraints

- `0 <= words.length, queries.length <= 1000`
- lowercase English letters only, `0 <= word length <= 20`

## Examples

```
words = ["app","apple"], queries = ["app","appl","apple","b",""]
  ->  ["word","prefix","word","none","prefix"]

words = [], queries = ["a"]        ->  ["none"]
words = ["a"], queries = [""]      ->  ["prefix"]
words = [""], queries = [""]       ->  ["word"]
```

Note `""` is a prefix of everything, so with any non-empty word inserted, querying `""`
gives `"prefix"`.

## Signature

```java
public List<String> trieQueries(String[] words, String[] queries)
```

## Target complexity

O(total characters) to build, O(query length) per query.

## The skill

A trie is a tree where **the edges are letters and the path is the string**. Nodes hold
no character themselves — position in the tree is the information.

```java
private static class Node {
    Node[] next = new Node[26];
    boolean isWord;
}
```

Insert walks the path and creates nodes as needed; the final node gets `isWord = true`.
Search walks the same path: fall off the tree and the answer is `"none"`; arrive and
check `isWord` to distinguish `"word"` from `"prefix"`.

**`isWord` is the whole reason this is not just a set of prefixes.** Without it you
cannot tell "app is a word" from "app is only on the way to apple", which is exactly
what LC 208 asks for and what this drill's three-way answer forces you to implement.

`Node[26]` versus `HashMap<Character,Node>`: the array is faster and simpler when the
alphabet is fixed and small, at the cost of 26 references per node even for sparse
tries. Say which you chose and why — it is a reasonable interview follow-up.

Trie has no LeetCode Easy problems. This drill and the two after it are the on-ramp.

## Hints

<details><summary>Hint 1 — the shared walk</summary>

Insert and search are the same loop; only what happens at each step differs (create vs
return null). Write a private `Node walk(String s)` that returns the node at the end of
a path or null, and search becomes three lines.

</details>

<details><summary>Hint 2 — the empty string</summary>

Inserting `""` marks the **root** as a word. Querying `""` returns the root. So
`words=[""], queries=[""]` gives `"word"`, and with a non-empty word inserted the root
exists but is not a word, giving `"prefix"`. Both are pinned; make sure your code does
not special-case the empty string but simply handles a zero-length walk.

</details>
