# Group values with computeIfAbsent

Tier 1 drill · hash-counting · unlocks LC 49, 249, 1152

## Brief

Given `String[] words`, group them by their **sorted-letters key** — two words share a
group when they are anagrams. Return the groups as a `List<List<String>>`.

Within each group, keep the words in their original input order. The groups themselves
may be returned in any order.

## Constraints

- `0 <= words.length <= 10^4`
- lowercase English letters only

## Examples

```
["eat","tea","tan","ate","nat","bat"]
  ->  [["eat","tea","ate"], ["tan","nat"], ["bat"]]   (group order irrelevant)

[]        ->  []
[""]      ->  [[""]]
["a"]     ->  [["a"]]
```

## Signature

```java
public List<List<String>> groupByKey(String[] words)
```

## Target complexity

O(n · k log k) where k is the word length, dominated by sorting each key.

## The skill

`computeIfAbsent` is the idiom, and getting it wrong is one of the most common Java
DSA fumbles:

```java
map.computeIfAbsent(key, k -> new ArrayList<>()).add(word);
```

Note it is `k -> new ArrayList<>()`, a *function*, not `new ArrayList<>()` directly.
The whole point is that the list is only allocated when the key is missing.

**A real hazard, measured:** calling `computeIfAbsent` on a `HashMap` from inside
another `computeIfAbsent` on the *same* map throws `ConcurrentModificationException`
on modern JDKs. Nesting them looks natural and is a bug.

## Hints

<details><summary>Hint 1 — the key</summary>

```java
char[] c = word.toCharArray();
Arrays.sort(c);
String key = new String(c);
```

`Arrays.sort(char[])` exists and is fine. Note there is no comparator overload for
primitive arrays — `Arrays.sort(int[], cmp)` does not compile.

</details>

<details><summary>Hint 2 — returning the groups</summary>

`return new ArrayList<>(map.values());` — `map.values()` is a view, not a `List`, so
you have to wrap it.

</details>
