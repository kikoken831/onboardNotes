# Permutations by swapping in place

Tier 1 drill · backtracking · unlocks LC 46, 47, 31

## Brief

Return every permutation of `nums`, which has **distinct** values. Any order of the
permutations is accepted; the contents of each permutation obviously matter.

## Constraints

- `0 <= nums.length <= 7`
- values are distinct

## Examples

```
[1,2,3]  ->  [[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]   (any order)
[]       ->  [[]]
[1]      ->  [[1]]
[1,2]    ->  [[1,2],[2,1]]
```

## Signature

```java
public List<List<Integer>> permutations(int[] nums)
```

## Target complexity

O(n · n!).

## The skill

Permutations are **not** an include/exclude problem — every element is used exactly
once, and what varies is the order. The natural template is: for position `i`, try each
remaining element there.

The swap formulation avoids a `used[]` array entirely:

```java
private void go(int[] nums, int i, List<List<Integer>> out) {
    if (i == nums.length) { out.add(toList(nums)); return; }
    for (int j = i; j < nums.length; j++) {
        swap(nums, i, j);          // choose: put nums[j] at position i
        go(nums, i + 1, out);
        swap(nums, i, j);          // un-choose: put it back
    }
}
```

Three details:

- **`j` starts at `i`, not `i + 1`.** The element already at position `i` is a valid
  choice for that position — `j = i` is the self-swap. Starting at `i+1` loses every
  permutation that keeps an element in place.
- **The second swap is the un-choose**, and it is essential. Without it the array is
  scrambled for the next iteration of the loop and you get duplicates and omissions
  rather than a clean failure.
- **Snapshot at the leaf.** `nums` is being mutated throughout, so copy it into a fresh
  list before recording.

Compare with the alternative: keep a `boolean[] used` and build a separate `cur` list.
That version is easier to adapt to LC 47 (duplicates, needs sorting plus a skip rule)
but allocates more. Both are standard; write the swap version first because it makes
the "choose a value for this position" framing concrete.

## Hints

<details><summary>Hint 1 — the empty input</summary>

`[]` returns `[[]]`, one empty permutation. The base case fires immediately at `i = 0`
since `nums.length` is 0.

</details>

<details><summary>Hint 2 — why swapping breaks for duplicates</summary>

With duplicate values the swap version generates duplicate permutations and the usual
`nums[i] == nums[i-1]` skip does not work, because swapping destroys the sortedness the
skip relies on. That is why LC 47 is normally written with `used[]`. Knowing *why* the
simple fix fails is the interesting part.

</details>
