# Sort Array By Parity

LeetCode 905 · Easy · https://leetcode.com/problems/sort-array-by-parity/

## Brief

Return `nums` rearranged so that every even number comes before every odd number. **Any**
such arrangement is accepted.

## Constraints

- `1 <= nums.length <= 5000`
- `0 <= nums[i] <= 5000`

## Examples

```
[3,1,2,4]  ->  [2,4,3,1]   (also [4,2,3,1], [2,4,1,3], [4,2,1,3])
[0]        ->  [0]
```

## Signature

```java
public int[] sortArrayByParity(int[] nums)
```

## Target complexity

O(n) time, O(1) extra space if you rearrange `nums` itself.

## The skill

**Ramp:** Tier 1 `p-partition-even-odd` — same partition, but here you **return** the
array. Returning `nums` after partitioning it in place is fine; so is building a new one.

This judge checks the *property* (evens first, same multiset), not one specific
arrangement.

## Hints

<details><summary>Hint 1</summary>

Write-index with a swap: when `nums[r]` is even, swap it into `nums[w++]`.

</details>
