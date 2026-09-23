# Find All Numbers Disappeared in an Array

LeetCode 448 · Easy · https://leetcode.com/problems/find-all-numbers-disappeared-in-an-array/

## Brief

`nums` has length `n` and values in `[1, n]`. Return every value in `[1, n]` that doesn't
appear, in any order.

## Constraints

- `1 <= n <= 10^5`

## Examples

```
[4,3,2,7,8,2,3,1]  ->  [5,6]
[1,1]              ->  [2]
```

## Signature

```java
public List<Integer> findDisappearedNumbers(int[] nums)
```

## Target complexity

O(n) time, O(1) extra space (the output doesn't count).

## The skill

**Use the array as its own hash set.** Values are valid indices, so for each `v` flip
`nums[|v| - 1]` negative; any index still positive at the end was never visited. The
`Math.abs` matters — the slot you read may already have been flipped.

## Hints

<details><summary>Hint 1</summary>

A `boolean[n + 1]` first. Then: can the input array itself hold those booleans?

</details>
