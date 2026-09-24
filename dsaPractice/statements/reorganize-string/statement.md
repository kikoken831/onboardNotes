# Reorganize String

LeetCode 767 · Medium · https://leetcode.com/problems/reorganize-string/

## Brief

Rearrange `s` so no two adjacent characters are equal, or return `""` if impossible. Any valid arrangement is accepted.

## Constraints

- `1 <= s.length <= 500`, lowercase

## Examples

```
"aab"   ->  "aba"
"aaab"  ->  ""
```

## Signature

```java
public String reorganizeString(String s)
```

## Target complexity

O(n).

## The skill

Possible exactly when the most frequent letter appears at most `⌈n/2⌉` times. Then **place it on the even indices first** and fill the remaining slots (evens, then odds) with everything else. The heap version repeatedly takes the two most frequent letters. The judge checks the property.

## Hints

<details><summary>Hint 1</summary>

Feasibility first: `maxCount > (n + 1) / 2` → `""`.

</details>
