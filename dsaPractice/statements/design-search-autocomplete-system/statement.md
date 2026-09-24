# Design Search Autocomplete System

LeetCode 642 · Hard · Premium · https://leetcode.com/problems/design-search-autocomplete-system/

## Brief

*(Paraphrased — a Premium problem.)* Construct with historical `sentences` and how many times
each was typed. Then characters arrive one at a time through `input(c)`:

- For a normal character, return the **top 3** historical sentences starting with everything
  typed so far — most-typed first, ties broken by **ASCII order**. Fewer than 3 if fewer match.
- `'#'` ends the current sentence: record it (one more time typed), reset, and return `[]`.

Design problem: implement `class AutocompleteSystem`.

## Constraints

- lowercase letters and spaces; at most 100 sentences of length <= 100; at most 5000 calls

## Examples

```
["AutocompleteSystem","input","input","input","input"]
[[["i love you","island","iroman","i love leetcode"],[5,3,2,2]],["i"],[" "],["a"],["#"]]
->  [null,["i love you","island","i love leetcode"],["i love you","i love leetcode"],[],[]]
```

## Signature

```java
class AutocompleteSystem {

    public AutocompleteSystem(String[] sentences, int[] times) {

    }

    public List<String> input(char c) {

    }
}
```

## Target complexity

A trie keyed by characters, each node pointing at candidate sentences, gives fast prefix
lookup; the ranking then sorts only the matches.

## The skill

State across calls (the current prefix) plus ranking with a **two-level tiebreak**. A
sentence typed via `'#'` must be added to the history and can then appear in suggestions —
including a brand-new one (pinned). Space is an ordinary character. Once a prefix matches
nothing, later characters can't match either until `'#'`.

## Hints

<details><summary>Hint 1</summary>

The comparator: `hot` descending, then `String.compareTo` ascending.

</details>
