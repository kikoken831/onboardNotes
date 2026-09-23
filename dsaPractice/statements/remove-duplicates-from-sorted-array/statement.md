# Remove Duplicates from Sorted Array

LeetCode 26 · Easy · https://leetcode.com/problems/remove-duplicates-from-sorted-array/

## Brief

`nums` is sorted non-decreasing. Remove duplicates **in place** so each value appears
once, keeping order. Return `k`, the number of unique values. Only the first `k` slots
are judged; anything after them is ignored.

## Constraints

- `1 <= nums.length <= 3 * 10^4`
- `-100 <= nums[i] <= 100`
- sorted non-decreasing

## Examples

```
[1,1,2]                ->  k=2, [1,2,_]
[0,0,1,1,1,2,2,3,3,4]  ->  k=5, [0,1,2,3,4,_,_,_,_,_]
```

## Signature

```java
public int removeDuplicates(int[] nums)
```

## Target complexity

O(n) time, O(1) extra space.

## The skill

**Ramp:** Tier 1 `p-dedupe-sorted` is this problem. The difference is that LeetCode
guarantees `n >= 1`, so the empty-array guard you wrote there is now optional — but keep
it, because it's free.

## Hints

<details><summary>Hint 1</summary>

Compare against the last value you **wrote** (`nums[w-1]`), not the previous value you
**read**. Both work here because the input is sorted, but only the first generalises to
LC 80 (at most two copies).

</details>
