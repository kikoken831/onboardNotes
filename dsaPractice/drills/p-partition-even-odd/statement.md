# Partition into evens then odds

Tier 1 drill · write-index · unlocks LC 905, 75

## Brief

Rearrange `nums` **in place** so every even number comes before every odd number.
The order *within* each group does not matter — any valid arrangement is accepted.

## Constraints

- `1 <= nums.length <= 5000`
- `0 <= nums[i] <= 5000`

## Examples

```
[3,1,2,4]  ->  [2,4,3,1] or [4,2,1,3] or any evens-then-odds arrangement
[0]        ->  [0]
[1,3,5]    ->  [1,3,5]      (no evens; still valid)
```

## Signature

```java
public void partitionEvenOdd(int[] nums)
```

## Target complexity

O(n) time, O(1) extra space, single pass.

## The skill

The first drill where **order within a group is not required**, which unlocks the
two-ends-inward variant: one pointer from the left looking for an odd, one from the
right looking for an even, swap, repeat until they cross.

This is Dutch-flag with two regions instead of three. Get it right and the next drill
is just one more region.

## Hints

<details><summary>Hint 1 — two ends</summary>

```java
int lo = 0, hi = nums.length - 1;
while (lo < hi) {
    if (nums[lo] % 2 == 0) lo++;
    else if (nums[hi] % 2 == 1) hi--;
    else swap(nums, lo++, hi--);
}
```

</details>

<details><summary>Hint 2 — why the write index also works</summary>

The single-pointer version (`if even, swap to nums[w++]`) is simpler and also O(n).
It does more swaps but is harder to get wrong. Write both and compare.

</details>
