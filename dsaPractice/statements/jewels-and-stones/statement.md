# Jewels and Stones

LeetCode 771 · Easy · https://leetcode.com/problems/jewels-and-stones/

## Brief

Each character of `jewels` is a jewel type (all distinct). Count how many characters in
`stones` are jewels. **Case-sensitive**: `a` and `A` are different types.

## Constraints

- `1 <= jewels.length, stones.length <= 50`
- letters only; `jewels` characters are unique

## Examples

```
jewels="aA", stones="aAAbbbb"  ->  3
jewels="z",  stones="ZZ"       ->  0
```

## Signature

```java
public int numJewelsInStones(String jewels, String stones)
```

## Target complexity

O(J + S) with a set; O(J · S) is the nested-loop version.

## The skill

Build a membership structure once, then query it — the shape of almost every hash
problem. A `boolean[128]` indexed by `char` is the fast version of a `HashSet<Character>`.

## Hints

<details><summary>Hint 1</summary>

Put the jewels in a set first. Then one pass over `stones`.

</details>
