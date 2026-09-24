# Palindrome Partitioning

LeetCode 131 · Medium · https://leetcode.com/problems/palindrome-partitioning/

## Brief

Split `s` into pieces that are **all** palindromes. Return every such partition, in any order.

## Constraints

- `1 <= s.length <= 16`, lowercase

## Examples

```
"aab"  ->  [["a","a","b"],["aa","b"]]
```

## Signature

```java
public List<List<String>> partition(String s)
```

## Target complexity

O(n · 2ⁿ).

## The skill

Backtracking over **cut positions**: from index `i`, try every palindromic prefix `s[i..j)` and recurse on the rest. Precomputing a `isPal[i][j]` table (LC 5's DP) removes the repeated palindrome checks.

## Hints

<details><summary>Hint 1</summary>

Each partition is a path from `0` to `n` through palindromic pieces.

</details>
