# Rotate Array

LeetCode 189 · Medium · https://leetcode.com/problems/rotate-array/

## Brief

Rotate `nums` to the **right** by `k` steps, in place. `k` may exceed the length.

## Constraints

- `1 <= nums.length <= 10^5`
- `0 <= k <= 10^5`

## Examples

```
[1,2,3,4,5,6,7], k=3  ->  [5,6,7,1,2,3,4]
[-1,-100,3,99],  k=2  ->  [3,99,-1,-100]
```

## Signature

```java
public void rotate(int[] nums, int k)
```

## Target complexity

O(n) time, O(1) extra space.

## The skill

**Ramp:** Tier 1 `p-rotate-left-k` (mirrored). **Three reversals**: reverse all, reverse the
first `k`, reverse the rest. First, `k %= n` — the judge includes `k > n` and `k = 10^5` on a
one-element array. Rotating one step `k` times is O(nk) and times out.

## Hints

<details><summary>Hint 1</summary>

Rotating right by `k` moves the last `k` elements to the front. What does reversing the
whole array do to them?

</details>
