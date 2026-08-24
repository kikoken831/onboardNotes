# Collect every word under a prefix

Tier 1 drill · trie · unlocks LC 211, 212, 642, 720

## Brief

Insert every word in `words`, then return all distinct inserted words beginning with
`prefix`, in **lexicographic order**. Return an empty list if there are none.

Duplicates in the input appear once in the output.

## Constraints

- `0 <= words.length <= 1000`
- lowercase English letters, `0 <= length <= 20`

## Examples

```
words = ["app","apple","apply","banana"], prefix = "app"
  ->  ["app","apple","apply"]

words = ["b","a"],   prefix = ""    ->  ["a","b"]
words = ["abc"],     prefix = "x"   ->  []
words = ["a","a"],   prefix = "a"   ->  ["a"]
```

## Signature

```java
public List<String> wordsWithPrefix(String[] words, String prefix)
```

## Target complexity

O(prefix length + total characters in the output).

## The skill

Two halves, and the second is the reusable one:

1. Walk to the prefix node. If you fall off, return empty.
2. **DFS the subtree from there**, accumulating characters on the way down and
   recording whenever you hit a node with `isWord`.

```java
private void collect(Node n, StringBuilder path, List<String> out) {
    if (n == null) return;
    if (n.isWord) out.add(path.toString());
    for (int c = 0; c < 26; c++) {
        if (n.next[c] == null) continue;
        path.append((char)('a' + c));
        collect(n.next[c], path, out);
        path.deleteCharAt(path.length() - 1);     // un-choose
    }
}
```

Two things fall out for free and are worth noticing rather than engineering:

- **Lexicographic order needs no sort.** Iterating children `0..25` visits them in
  alphabetical order, so a DFS emits words already sorted. If you found yourself
  reaching for `Collections.sort`, the trie was doing work you threw away.
- **A prefix that is itself a word is emitted first**, because `isWord` is checked
  before descending — `"app"` before `"apple"`. That is preorder, and it is the correct
  lexicographic position.

This is backtracking over a trie: the same choose/recurse/un-choose from group H, with
`path` as the mutable state. That combination — trie plus DFS with backtracking — is
exactly LC 212 word-search-ii, which is otherwise one of the harder Hards.

## Hints

<details><summary>Hint 1 — start the path at the prefix</summary>

The accumulated path must begin as the prefix itself, not empty, or you emit suffixes
instead of whole words. Seed the `StringBuilder` with `prefix` before the DFS.

</details>

<details><summary>Hint 2 — duplicates</summary>

`isWord` is a boolean, not a counter, so inserting `"a"` twice sets the same flag twice
and the word is emitted once. The de-duplication is free; make sure you did not add a
counter and then emit once per count.

</details>
