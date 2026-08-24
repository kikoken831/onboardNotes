# Count words sharing a prefix

Tier 1 drill · trie · unlocks LC 208, 1268, 720

## Brief

Insert every word in `words`, then for each query return how many inserted words start
with that query. Duplicated words count once each — inserting `"ab"` twice means two
words start with `"a"`.

Return the counts as an `int[]`, one per query.

## Constraints

- `0 <= words.length, queries.length <= 1000`
- lowercase English letters, `0 <= length <= 20`

## Examples

```
words = ["apple","app","ape"], queries = ["app","ap","b",""]  ->  [2,3,0,3]
words = ["a","a"],             queries = ["a"]                ->  [2]
words = [],                    queries = ["x"]                ->  [0]
words = ["abc"],               queries = ["abcd"]             ->  [0]
```

## Signature

```java
public int[] prefixCounts(String[] words, String[] queries)
```

## Target complexity

O(total characters) to build, **O(query length)** per query.

## The skill

**Augmenting the node.** Add a counter to each node, incremented on the way through
during insert:

```java
private static class Node {
    Node[] next = new Node[26];
    int passing;      // how many inserted words pass through this node
}
```

Then a prefix count is one walk and one field read — no subtree traversal, no counting
at query time.

The idea generalises far beyond this: store at each node whatever aggregate you will be
asked for. Smallest word in the subtree, count of words, longest word — all become O(1)
lookups at the cost of maintaining them during insert. That is the difference between
LC 208 (bare trie) and LC 642 (autocomplete with rankings), and it is the standard
follow-up when an interviewer asks how you would make the query faster.

Contrast with the obvious alternative: walk to the prefix node, then DFS the subtree
counting `isWord` markers. Correct, but O(subtree size) per query instead of O(1). Know
both, and know why you would pick the counter.

## Hints

<details><summary>Hint 1 — increment where?</summary>

Increment `passing` on **every node you step onto** during insert, including the final
one. Whether you also count the root depends on whether you want `""` to report the
total word count — the pinned example `["apple","app","ape"], ""` expects 3, so yes.

</details>

<details><summary>Hint 2 — falling off the tree</summary>

`["abc"]` queried with `"abcd"` walks off the end and must return 0, not the count at
`"abc"`. Return as soon as a link is null.

</details>
