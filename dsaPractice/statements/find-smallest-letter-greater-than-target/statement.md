# Find Smallest Letter Greater Than Target

LeetCode 744 · Easy · https://leetcode.com/problems/find-smallest-letter-greater-than-target/

## Brief

`letters` is sorted (with duplicates allowed). Return the smallest letter **strictly greater**
than `target`; if none exists, **wrap around** and return `letters[0]`.

## Constraints

- `2 <= letters.length <= 10^4`, at least two different characters

## Examples

```
["c","f","j"], "a"  ->  "c"
["c","f","j"], "c"  ->  "f"
["x","x","y","y"], "z"  ->  "x"
```

## Signature

```java
public char nextGreatestLetter(char[] letters, char target)
```

## Target complexity

O(log n).

## The skill

**Ramp:** Tier 1 `p-bs-first-gt` — "first index with value `> target`", the upper-bound
template, then `% n` for the wrap-around. Using `>=` instead of `>` returns `target` itself.

## Hints

<details><summary>Hint 1</summary>

Search `[0, n]`; an answer of `n` means "wrap".

</details>
