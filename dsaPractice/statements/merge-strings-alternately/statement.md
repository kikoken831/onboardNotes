# Merge Strings Alternately

LeetCode 1768 · Easy · https://leetcode.com/problems/merge-strings-alternately/

## Brief

Interleave the two words letter by letter, starting with `word1`. When one runs out,
append the rest of the other.

## Constraints

- `1 <= word1.length, word2.length <= 100`
- lowercase English letters

## Examples

```
"abc",  "pqr"   ->  "apbqcr"
"ab",   "pqrs"  ->  "apbqrs"
"abcd", "pq"    ->  "apbqcd"
```

## Signature

```java
public String mergeAlternately(String word1, String word2)
```

## Target complexity

O(n + m).

## The skill

Two pointers of **unequal length**. Either run while *both* have letters and then append
both tails, or run while *either* has letters and guard each append. Both are fine; mixing
the two is where the bug comes from.

## Hints

<details><summary>Hint 1</summary>

Use a `StringBuilder`. `String +=` in a loop is O(n²).

</details>
