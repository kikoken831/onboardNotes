# Reverse an array in place

Tier 1 drill · write-index · unlocks LC 344, 151, 189

## Brief

Reverse `nums` **in place**. No second array.

## Constraints

- `0 <= nums.length <= 10^5`

## Examples

```
[1,2,3,4]  ->  [4,3,2,1]
[1,2,3]    ->  [3,2,1]
[]         ->  []
[9]        ->  [9]
```

## Signature

```java
public void reverseInPlace(int[] nums)
```

## Target complexity

O(n) time, O(1) extra space.

## The skill

Trivial on its own. It is here because **three of your set's problems are just this
applied carefully**: rotate-array (189) is three reversals, reverse-words (151) is a
reversal per word plus one overall, and it is the tail half of reorder-list (143).

The one thing to get right: the loop bound. `lo < hi`, not `lo <= hi`. With `<=` an
odd-length array swaps the middle element with itself — harmless here, but the same
off-by-one is not harmless in the sublist variants later.

## Hints

<details><summary>Hint 1</summary>

```java
for (int lo = 0, hi = nums.length - 1; lo < hi; lo++, hi--) swap(nums, lo, hi);
```

</details>
