# Generalized Abbreviation

LeetCode 320 · Medium · Premium · https://leetcode.com/problems/generalized-abbreviation/

## Brief

*(Paraphrased — a Premium problem.)* Abbreviate `word` by replacing any chosen letters with
their count, where a run of consecutive replaced letters becomes **one** number (`"word"` →
`"1o2"`, never `"1o11"`). Return every abbreviation, in any order — each letter is either kept or
replaced, so there are `2^n` of them.

## Constraints

- `1 <= word.length <= 15`, lowercase

## Examples

```
"word"  ->  ["4","3d","2r1","2rd","1o2","1o1d","1or1","1ord","w3","w2d","w1r1","w1rd","wo2","wo1d","wor1","word"]
"a"     ->  ["1","a"]
```

## Signature

```java
public List<String> generateAbbreviations(String word)
```

## Target complexity

O(n · 2ⁿ).

## The skill

Each letter is either **kept** or **abbreviated** — a subset problem, so a bitmask works. The
subtlety is merging adjacent abbreviated letters into one number: carry a **pending count** down
the recursion and flush it only when a letter is kept, or at the end.

## Hints

<details><summary>Hint 1</summary>

`go(i, run)`: abbreviate → `go(i + 1, run + 1)`; keep → append `run` (if > 0) and the letter,
then `go(i + 1, 0)`.

</details>
