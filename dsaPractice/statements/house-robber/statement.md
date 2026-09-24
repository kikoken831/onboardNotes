# House Robber

LeetCode 198 · Medium · https://leetcode.com/problems/house-robber/

## Brief

You can't rob two **adjacent** houses. Return the most money you can rob.

## Constraints

- `1 <= n <= 100`, `0 <= nums[i] <= 400`

## Examples

```
[1,2,3,1]    ->  4
[2,7,9,3,1]  ->  12
```

## Signature

```java
public int rob(int[] nums)
```

## Target complexity

O(n), O(1) space.

## The skill

**Ramp:** Tier 1 `p-bt-subset-no-adjacent` (the brute force) and `p-dp-climb-cost`. At each house: skip it (keep the best so far) or rob it (best from two houses back plus this one). Taking every other house is wrong — `[2,1,1,2]` robs both ends.

## Hints

<details><summary>Hint 1</summary>

`best[i] = max(best[i−1], best[i−2] + a[i])`

</details>
