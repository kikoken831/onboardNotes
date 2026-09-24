# Implement Trie (Prefix Tree)

LeetCode 208 · Medium · https://leetcode.com/problems/implement-trie-prefix-tree/

## Brief

Implement `class Trie` with `insert(word)`, `search(word)` (was this exact word inserted?)
and `startsWith(prefix)` (does any inserted word start with it?). Lowercase letters only.

Design problem: the judge replays LeetCode-style call sequences.

## Constraints

- `1 <= length <= 2000`; at most `3 * 10^4` calls

## Examples

```
["Trie","insert","search","search","startsWith","insert","search"]
[[],["apple"],["apple"],["app"],["app"],["app"],["app"]]
->  [null,null,true,false,true,null,true]
```

## Signature

```java
class Trie {

    public Trie() {
        
    }

    public void insert(String word) {
        
    }

    public boolean search(String word) {
        
    }

    public boolean startsWith(String prefix) {
        
    }
}
```

## Target complexity

O(L) per call.

## The skill

**Ramp:** Tier 1 `p-trie-insert-search`. Each node has 26 child slots and an **`isWord` flag**.
`search` and `startsWith` walk the same path; they differ only in whether the last node must be
flagged. Without the flag, `"app"` is "found" after inserting only `"apple"` — pinned.

## Hints

<details><summary>Hint 1</summary>

A `Trie` node can be its own class: `Trie[] next = new Trie[26]`.

</details>
