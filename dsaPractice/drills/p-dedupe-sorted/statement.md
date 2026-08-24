# De-duplicate a sorted array in place

Tier 1 drill · write-index · unlocks LC 26, 80, 83

## Brief

`nums` is sorted in non-decreasing order. Remove the duplicates **in place** so each
distinct value appears once, and return the number of distinct values `k`. The first
`k` slots must hold those distinct values in sorted order. Anything past index `k - 1`
is ignored.

## Constraints

- `0 <= nums.length <= 3 * 10^4`
- `-100 <= nums[i] <= 100`, sorted non-decreasing

## Examples

```
[1,1,2]           ->  k = 2, nums starts [1,2]
[0,0,1,1,1,2,2,3,3,4] -> k = 5, nums starts [0,1,2,3,4]
[]                ->  k = 0
[7]               ->  k = 1, nums starts [7]
[5,5,5,5]         ->  k = 1, nums starts [5]
```

## Signature

```java
public int dedupeSorted(int[] nums)
```

## Target complexity

O(n) time, O(1) extra space.

## The skill

Same write index as the previous drill, but the keep-condition now depends on what you
*last kept* rather than on a constant. That is the whole difference, and it is the
difference between LC 27 and LC 26.

Compare against `nums[w - 1]`, not against `nums[r - 1]`. Those are different values
once you have dropped anything, and reaching for `r - 1` is the standard mistake.

## Hints

<details><summary>Hint 1 — the empty case</summary>

`w` starts at 0, so `nums[w - 1]` is out of bounds on the first element. Either keep
the first element unconditionally and start the loop at `r = 1`, or guard the compare.
The empty array must return 0 without touching anything.

</details>

<details><summary>Hint 2 — the invariant</summary>

At the top of each iteration, `nums[0..w-1]` holds every distinct value seen so far,
in order. Say that sentence out loud before you write the loop; the code follows from it.

</details>
