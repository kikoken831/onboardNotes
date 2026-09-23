# Running Sum of 1d Array

LeetCode 1480 · Easy · https://leetcode.com/problems/running-sum-of-1d-array/

## Brief

Return `out` where `out[i] = nums[0] + ... + nums[i]`.

## Constraints

- `1 <= nums.length <= 1000`
- `-10^6 <= nums[i] <= 10^6`

## Examples

```
[1,2,3,4]     ->  [1,3,6,10]
[1,1,1,1,1]   ->  [1,2,3,4,5]
[3,1,2,10,1]  ->  [3,4,6,16,17]
```

## Signature

```java
public int[] runningSum(int[] nums)
```

## Target complexity

O(n).

## The skill

**Ramp:** Tier 1 `p-prefix-build-query`. This is the *inclusive* prefix array — same
length as the input. The drill used the size-`n+1` exclusive form, which is what makes
range queries off-by-one-free. Know which one a problem wants.

## Hints

<details><summary>Hint 1</summary>

Carry the sum in a variable, or reuse the previous output slot: `out[i] = out[i-1] + nums[i]`.

</details>
