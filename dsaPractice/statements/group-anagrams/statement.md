# Group Anagrams

LeetCode 49 · Medium · https://leetcode.com/problems/group-anagrams/

## Brief

Group the strings that are anagrams of each other. Groups in any order, strings within a
group in any order; duplicate strings stay in their group.

## Constraints

- `1 <= strs.length <= 10^4`, `0 <= strs[i].length <= 100`, lowercase

## Examples

```
["eat","tea","tan","ate","nat","bat"]  ->  [["bat"],["nat","tan"],["ate","eat","tea"]]
[""]                                   ->  [[""]]
```

## Signature

```java
public List<List<String>> groupAnagrams(String[] strs)
```

## Target complexity

O(n · k log k) with sorted keys, or O(n · k) with count keys.

## The skill

**Ramp:** Tier 1 `p-group-by-key`. Everything rides on the **key**: a canonical form equal
for exactly the anagrams. Sorted characters work; a 26-count signature is faster. A key
built by *summing* character codes collides (`"ad"` and `"bc"`) — the judge's `aab`/`abb`
pin catches the related "same letters, different counts" mistake.

## Hints

<details><summary>Hint 1</summary>

`groups.computeIfAbsent(key, k -> new ArrayList<>()).add(s)`

</details>
