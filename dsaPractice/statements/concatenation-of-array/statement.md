# Concatenation of Array

LeetCode 1929 · Easy · https://leetcode.com/problems/concatenation-of-array/

## Brief

Return an array `ans` of length `2n` where `ans[i] == nums[i]` and `ans[i + n] == nums[i]`
— in other words, `nums` followed by itself.

## Constraints

- `1 <= n <= 1000`
- `1 <= nums[i] <= 1000`

## Examples

```
[1,2,1]    ->  [1,2,1,1,2,1]
[1,3,2,1]  ->  [1,3,2,1,1,3,2,1]
```

## Signature

```java
public int[] getConcatenation(int[] nums)
```

## Target complexity

O(n) time, O(n) for the output.

## The skill

Index arithmetic with an offset. The whole problem is getting `i + n` right and sizing the
output once rather than growing it.

## Hints

<details><summary>Hint 1</summary>

Allocate `new int[2 * n]` up front. One loop can write both halves.

</details>
