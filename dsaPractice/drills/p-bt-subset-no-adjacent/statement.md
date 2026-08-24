# Subsets with no two adjacent elements

Tier 1 drill · backtracking · unlocks LC 198, 213, 1043

## Brief

Count the subsets of `nums` (by **index**, so duplicate values are still distinct
elements) in which no two chosen elements are adjacent in the array, and whose sum
equals `target`.

The empty subset counts when `target` is 0.

## Constraints

- `0 <= nums.length <= 18`
- `-50 <= nums[i], target <= 50`

## Examples

```
[2,4,6],   target = 8   ->  1    ({2,6}; {2,4} and {4,6} are adjacent)
[1,1,1],   target = 2   ->  1    (indices 0 and 2)
[],        target = 0   ->  1    (the empty subset)
[],        target = 1   ->  0
[5],       target = 5   ->  1
[3,3],     target = 3   ->  2    (either index, separately)
```

## Signature

```java
public int countNonAdjacentSubsets(int[] nums, int target)
```

## Target complexity

O(2^n) worst case.

## The skill

The first drill where **the choice constrains the next choice**. Including index `i`
forbids `i+1`; excluding it forbids nothing. That asymmetry is encoded entirely in the
index you recurse on:

```java
private int go(int[] nums, int i, int remaining) {
    if (i >= nums.length) return remaining == 0 ? 1 : 0;
    int take = go(nums, i + 2, remaining - nums[i]);   // skip the neighbour
    int skip = go(nums, i + 1, remaining);
    return take + skip;
}
```

`i + 2` versus `i + 1` **is** the constraint. No separate bookkeeping, no "last taken
index" parameter, no validity check at the leaf. When a constraint can be pushed into
the recursion's argument like this, do it — it is faster and there is far less to get
wrong than filtering invalid candidates after the fact.

Also note the base case now returns a **count** rather than a boolean, so both branches
must be explored and summed; there is no short-circuit. Compare with the previous
drill, where `||` let you stop at the first hit. Existence and counting are different
problems even when the search tree is identical.

This is the recursion behind house-robber (LC 198). Memoise `(i, remaining)` and it
becomes the DP.

## Hints

<details><summary>Hint 1 — `i >= nums.length`, not `==`</summary>

`i + 2` can overshoot the end by one, so the base case must use `>=`. Using `==` gives
an out-of-bounds or a silently missed subset. `[5], target=5` exercises it.

</details>

<details><summary>Hint 2 — by index, not by value</summary>

`[3,3], target=3` returns 2, not 1. The two 3s are different elements even though they
are equal. This is deliberately the opposite of LC 90's de-duplication rule, so read
the statement rather than pattern-matching.

</details>
