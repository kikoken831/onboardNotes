# Sort Colors

LeetCode 75 · Medium · https://leetcode.com/problems/sort-colors/

## Brief

`nums` holds only `0`, `1` and `2`. Sort it **in place** without a library sort.

## Constraints

- `1 <= nums.length <= 300`
- `nums[i]` is 0, 1 or 2

## Examples

```
[2,0,2,1,1,0]  ->  [0,0,1,1,2,2]
[2,0,1]        ->  [0,1,2]
```

## Signature

```java
public void sortColors(int[] nums)
```

## Target complexity

One pass, O(1) space.

## The skill

**Ramp:** Tier 1 `p-dutch-flag`. Three pointers: everything before `lo` is 0, everything
after `hi` is 2, `mid` scans. The one subtle rule: after swapping a `2` to `hi`, **don't
advance `mid`** — the value swapped in hasn't been looked at. Counting sort (two passes) is
also correct and a fine first answer.

## Hints

<details><summary>Hint 1</summary>

Loop while `mid <= hi`. Which of the three cases advances `mid`?

</details>
