# Trapping Rain Water

LeetCode 42 · Hard · https://leetcode.com/problems/trapping-rain-water/

## Brief

`height[i]` is a bar of width 1. How much water is trapped after rain?

## Constraints

- `1 <= n <= 2 * 10^4`, `0 <= height[i] <= 10^5`

## Examples

```
[0,1,0,2,1,0,1,3,2,1,2,1]  ->  6
[4,2,0,3,2,5]              ->  9
```

## Signature

```java
public int trap(int[] height)
```

## Target complexity

O(n) time, O(1) space.

## The skill

Water over bar `i` is `min(maxLeft, maxRight) − height[i]`. Three ways to get there, and an
interviewer may ask for all of them: **prefix-max arrays** (O(n) space), a **monotonic stack**
that fills basins as it pops (**ramp:** Tier 1 `p-mono-prev-smaller`), and **two pointers**
— move whichever side has the lower wall, because that side's max already bounds its water.

## Hints

<details><summary>Hint 1</summary>

Compute `leftMax[]` and `rightMax[]` first. Then ask which of the two you actually need at
each step.

</details>
