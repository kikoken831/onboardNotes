# Rotate left by k using three reversals

Tier 1 drill · write-index · unlocks LC 189, 61

## Brief

Rotate `nums` left by `k` positions, **in place**. The element at index `k` ends up at
index 0. `k` may be zero, may exceed the array length, and the array may be empty.

## Constraints

- `0 <= nums.length <= 10^5`
- `0 <= k <= 10^9`

## Examples

```
[1,2,3,4,5], k = 2  ->  [3,4,5,1,2]
[1,2,3],     k = 0  ->  [1,2,3]
[1,2,3],     k = 3  ->  [1,2,3]
[1,2,3],     k = 4  ->  [2,3,1]
[],          k = 5  ->  []
```

Note this drill rotates **left**. LeetCode 189 rotates right — deliberately different,
so you have to think rather than recall.

## Constraints on approach

O(1) extra space. Do not allocate a copy.

## Signature

```java
public void rotateLeftK(int[] nums, int k)
```

## Target complexity

O(n) time, O(1) extra space.

## The skill

The reversal trick: reverse the first `k`, reverse the rest, then reverse the whole
thing. Three linear passes, no extra memory, and it generalises to rotating any
sub-range.

The two failure modes are both about `k`, not about the reversals: forgetting
`k %= n`, and dividing by zero when `n == 0`. Handle the empty array before you touch
`k`.

## Hints

<details><summary>Hint 1 — normalise k first</summary>

```java
if (nums.length == 0) return;
k %= nums.length;          // now 0 <= k < n
if (k == 0) return;
```

</details>

<details><summary>Hint 2 — the three reversals</summary>

```java
reverse(nums, 0, k - 1);
reverse(nums, k, nums.length - 1);
reverse(nums, 0, nums.length - 1);
```

Work through `[1,2,3,4,5]` with `k=2` by hand before running it.

</details>
