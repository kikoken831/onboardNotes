# Container With Most Water

LeetCode 11 · Medium · https://leetcode.com/problems/container-with-most-water/

## Brief

`height[i]` is a vertical line at `x = i`. Choose two lines that, with the x-axis, hold the
most water. Return that area: `min(height[i], height[j]) * (j - i)`.

## Constraints

- `2 <= n <= 10^5`
- `0 <= height[i] <= 10^4`

## Examples

```
[1,8,6,2,5,4,8,3,7]  ->  49    lines at 1 and 8, height 7, width 7
[1,1]                ->  1
```

## Signature

```java
public int maxArea(int[] height)
```

## Target complexity

O(n).

## The skill

Start with the widest container and **move the shorter line inward**. Why that's safe is
the whole interview: any container using the shorter line with a closer partner is narrower
*and* no taller, so it can never win — you may discard that line forever.

## Hints

<details><summary>Hint 1</summary>

O(n²) over all pairs is the baseline. Which pairs can you prove are never better?

</details>
