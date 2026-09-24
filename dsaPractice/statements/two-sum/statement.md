# Two Sum

LeetCode 1 · Easy · https://leetcode.com/problems/two-sum/

## Brief

Find the **indices** of the two different elements of `nums` whose values add up to `target`.
Every input has exactly one such pair, and an element may not be paired with itself. Return
the two indices in either order.

## Constraints

- `2 <= nums.length <= 10^4`
- `-10^9 <= nums[i] <= 10^9`, `-10^9 <= target <= 10^9`
- exactly one valid answer exists

## Examples

```
[2,7,11,15], target=9        ->  [0,1]
[3,2,4], target=6            ->  [1,2]
[3,3], target=6              ->  [0,1]
[-1,-2,-3,-4,-5], target=-8  ->  [2,4]
```

## Signature

```java
public int[] twoSum(int[] nums, int target)
```

## Target complexity

O(n) time, O(n) space — one pass.

## The skill

**Ramp:** Tier 1 `p-two-sum-seen-map`. The brute force checks every pair in O(n²). The fix is
to ask, at each `nums[i]`, "have I **already seen** `target - nums[i]`?" — a `HashMap` from
value to index answers that in O(1). Look up **before** inserting `nums[i]`, so an element
can't match itself (`[3,2,4], 6` must not return `[0,0]`), while `[3,3], 6` still works because
the first `3` is in the map when the second arrives.

## Hints

<details><summary>Hint 1</summary>

Map value → index. For each `i`: if the map holds `target - nums[i]`, return that index and
`i`; otherwise put `nums[i] -> i`.

</details>

<details><summary>Hint 2</summary>

Why is one pass enough? The pair `(j, i)` with `j < i` is found at step `i`, because
`nums[j]` was stored at step `j`.

</details>
