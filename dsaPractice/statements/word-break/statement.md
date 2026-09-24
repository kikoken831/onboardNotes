# Word Break

LeetCode 139 · Medium · https://leetcode.com/problems/word-break/

## Brief

Can `s` be split into a sequence of one or more dictionary words? Words may be reused.

## Constraints

- `1 <= s.length <= 300`, `1 <= wordDict.length <= 1000`

## Examples

```
"leetcode", ["leet","code"]                          ->  true
"catsandog", ["cats","dog","sand","and","cat"]    ->  false
```

## Signature

```java
public boolean wordBreak(String s, List<String> wordDict)
```

## Target complexity

O(n² · L) — or O(n · W) iterating over words.

## The skill

`ok[i]` = the first `i` characters can be segmented; `ok[i]` is true if some `ok[j]` is true and `s[j..i)` is a word. Two traps, both pinned: greedily taking the longest word fails (`"aaaaaaa"` with `aaaa`/`aaa`), and plain recursion without memoisation is **exponential** on `"aaa…ab"`.

## Hints

<details><summary>Hint 1</summary>

Put the dictionary in a `HashSet`.

</details>
