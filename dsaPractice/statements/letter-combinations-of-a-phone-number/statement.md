# Letter Combinations of a Phone Number

LeetCode 17 · Medium · https://leetcode.com/problems/letter-combinations-of-a-phone-number/

## Brief

Each digit `2–9` maps to letters as on a phone keypad (`7` and `9` have four). Return every
string the digits could spell, in any order.

## Constraints

- `0 <= digits.length <= 4`

## Examples

```
"23"  ->  ["ad","ae","af","bd","be","bf","cd","ce","cf"]
""    ->  []
```

## Signature

```java
public List<String> letterCombinations(String digits)
```

## Target complexity

O(4ⁿ · n).

## The skill

**Ramp:** Tier 1 `p-bt-binary-strings`, with a branching factor of 3 or 4 instead of 2 —
choose, recurse, un-choose. The trap: `""` must return `[]`, not `[""]`.

## Hints

<details><summary>Hint 1</summary>

A `StringBuilder` with `setLength(len − 1)` on the way back.

</details>
