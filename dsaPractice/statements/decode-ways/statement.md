# Decode Ways

LeetCode 91 · Medium · https://leetcode.com/problems/decode-ways/

## Brief

Letters map to `"1"`–`"26"`. Count the ways to decode the digit string `s`. A `"0"` can't stand alone, and `"06"` is not `"6"`.

## Constraints

- `1 <= s.length <= 100`; the answer fits in 32 bits

## Examples

```
"12"   ->  2    AB, L
"226"  ->  3
"06"   ->  0
```

## Signature

```java
public int numDecodings(String s)
```

## Target complexity

O(n), O(1) space.

## The skill

Climbing Stairs with **conditions on each step**: take one digit if it isn't `0`, take two if they form `10`–`26`. The zeros are the whole problem — `"10"`, `"2101"`, `"06"` are all pinned.

## Hints

<details><summary>Hint 1</summary>

`dp[i]` = ways to decode the first `i` characters; `dp[0] = 1`.

</details>
