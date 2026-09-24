# Minimum Size Subarray Sum

LeetCode 209 · Medium · https://leetcode.com/problems/minimum-size-subarray-sum/

## Brief

All values are **positive**. Return the length of the shortest subarray whose sum is
`>= target`, or `0` if there is none.

## Constraints

- `1 <= target <= 10^9`, `1 <= n <= 10^5`, `1 <= nums[i] <= 10^4`

## Examples

```
7, [2,3,1,2,4,3]       ->  2    [4,3]
11, [1,1,1,1,1,1,1,1]  ->  0
```

## Signature

```java
public int minSubArrayLen(int target, int[] nums)
```

## Target complexity

O(n) — the follow-up asks for O(n log n) too (prefix sums + binary search).

## The skill

Shrinking window: grow right; **while** the sum still reaches `target`, record and shrink
left. It only works because values are positive — adding an element never lowers the sum, so
the window is monotone. With negatives (LC 862) it breaks and needs a monotonic deque.

## Hints

<details><summary>Hint 1</summary>

`while`, not `if`: one new element can let the window shrink several times.

</details>
