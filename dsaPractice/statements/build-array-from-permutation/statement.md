# Build Array from Permutation

LeetCode 1920 · Easy · https://leetcode.com/problems/build-array-from-permutation/

## Brief

`nums` is a permutation of `0..n-1`. Return `ans` where `ans[i] = nums[nums[i]]`.

## Constraints

- `1 <= nums.length <= 1000`
- `0 <= nums[i] < nums.length`, all distinct

## Examples

```
[0,2,1,5,3,4]  ->  [0,1,2,4,5,3]
[5,0,1,2,3,4]  ->  [4,5,0,1,2,3]
```

## Signature

```java
public int[] buildArray(int[] nums)
```

## Target complexity

O(n) time. The follow-up asks for O(1) extra space.

## The skill

Using values as indices. The follow-up is the interesting part: storing two numbers in
one slot as `a + n * b`, so you can overwrite in place without losing the original.

## Hints

<details><summary>Hint 1</summary>

The O(n)-space version is one line in a loop. Do that first.

</details>

<details><summary>Hint 2 — the O(1) follow-up</summary>

Every value is `< n`, so `nums[i] + n * (nums[nums[i]] % n)` encodes both old and new.
The `% n` recovers an old value that may already have been encoded. A second pass
divides by `n`.

</details>
