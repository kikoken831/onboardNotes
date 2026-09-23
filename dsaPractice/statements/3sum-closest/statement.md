# 3Sum Closest

LeetCode 16 · Medium · https://leetcode.com/problems/3sum-closest/

## Brief

Find three values (at different indices) whose sum is **closest** to `target`, and return
that sum. Each input has exactly one answer.

## Constraints

- `3 <= nums.length <= 500`
- `-1000 <= nums[i] <= 1000`, `-10^4 <= target <= 10^4`

## Examples

```
[-1,2,1,-4], target=1  ->  2     -1 + 2 + 1
[0,0,0],     target=1  ->  0
```

## Signature

```java
public int threeSumClosest(int[] nums, int target)
```

## Target complexity

O(n²).

## The skill

The 3Sum skeleton, tracking the best distance instead of collecting zeros. You can return
immediately on an exact hit. Initialise `best` to a **real** triple sum, not
`Integer.MAX_VALUE` — `MAX_VALUE - target` overflows when `target` is negative.

## Hints

<details><summary>Hint 1</summary>

Sort. For each `i`, move `l`/`r` toward `target - a[i]`, updating `best` whenever
`|s - target|` shrinks.

</details>
