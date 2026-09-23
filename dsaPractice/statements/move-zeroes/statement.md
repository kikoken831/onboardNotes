# Move Zeroes

LeetCode 283 · Easy · https://leetcode.com/problems/move-zeroes/

## Brief

Move every `0` to the end of `nums` **in place**, keeping the relative order of the non-zero
values. Return nothing.

## Constraints

- `1 <= nums.length <= 10^4`
- `-2^31 <= nums[i] <= 2^31 - 1`

## Examples

```
[0,1,0,3,12]  ->  [1,3,12,0,0]
[0]           ->  [0]
```

## Signature

```java
public void moveZeroes(int[] nums)
```

## Target complexity

O(n) time, O(1) space, and ideally the minimum number of writes.

## The skill

**Ramp:** Tier 1 `p-move-zeros` — the same problem. If your tier 1 solution compacted the
non-zeros but never filled the tail with zeros, this is where that bug shows up.

## Hints

<details><summary>Hint 1</summary>

Swap-as-you-go: `if (nums[r] != 0) swap(nums, w++, r);` fills the tail with zeros for free.

</details>
