# Sort Characters By Frequency

LeetCode 451 · Medium · https://leetcode.com/problems/sort-characters-by-frequency/

## Brief

Rearrange `s` so characters appear in **decreasing frequency**, each character's copies together. Case-sensitive. Characters with equal counts may come in either order.

## Constraints

- `1 <= s.length <= 5 * 10^5`; letters and digits

## Examples

```
"tree"    ->  "eert"   ("eetr" is also correct)
"cccaaa"  ->  "cccaaa" or "aaaccc"   (not "cacaca")
```

## Signature

```java
public String frequencySort(String s)
```

## Target complexity

O(n) with bucket sort by count.

## The skill

Count, then output by count — a heap, a sort of the distinct characters, or **buckets indexed by frequency** (frequencies are at most `n`). The judge checks the property, since ties are unordered.

## Hints

<details><summary>Hint 1</summary>

The characters must be grouped: `"cacaca"` has the right counts in the wrong shape.

</details>
