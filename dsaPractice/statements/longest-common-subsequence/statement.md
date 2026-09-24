# Longest Common Subsequence

LeetCode 1143 · Medium · https://leetcode.com/problems/longest-common-subsequence/

## Brief

Return the length of the longest subsequence common to both strings (0 if none).

## Constraints

- `1 <= lengths <= 1000`, lowercase

## Examples

```
"abcde", "ace"  ->  3
"abc", "def"    ->  0
```

## Signature

```java
public int longestCommonSubsequence(String text1, String text2)
```

## Target complexity

O(m · n) time, O(min(m, n)) space.

## The skill

The canonical 2-D string DP: `dp[i][j]` over prefixes; equal last characters → `1 + dp[i−1][j−1]`, otherwise `max(dp[i−1][j], dp[i][j−1])`. Collapsing to one row needs a saved **diagonal** value, because `dp[j−1]` has already been overwritten with the current row. (Edit distance, LC 72, is the same table with different rules.)

## Hints

<details><summary>Hint 1</summary>

Size the table `(m + 1) × (n + 1)`; row and column 0 are empty prefixes.

</details>
