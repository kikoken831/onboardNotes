# Search Insert Position

LeetCode 35 · Easy · https://leetcode.com/problems/search-insert-position/

## Brief

`nums` is sorted with **distinct** values. Return the index of `target` if present;
otherwise the index where it would be inserted to keep the array sorted.

## Constraints

- `1 <= nums.length <= 10^4`
- strictly increasing
- `-10^4 <= target <= 10^4`

## Examples

```
[1,3,5,6], 5  ->  2
[1,3,5,6], 2  ->  1
[1,3,5,6], 7  ->  4     (past the end is a valid answer)
```

## Signature

```java
public int searchInsert(int[] nums, int target)
```

## Target complexity

O(log n).

## The skill

**Ramp:** Tier 1 `p-bs-first-ge`. "Found, or where it would go" is exactly *the first index
whose value is ≥ target* — one question, not two. Note the answer can be `n`, so the search
range is `[0, n]`, not `[0, n-1]`.

## Hints

<details><summary>Hint 1</summary>

`lo = 0, hi = n`, loop `while (lo < hi)`, `if (nums[mid] < target) lo = mid + 1; else hi = mid;`
Return `lo`.

</details>
