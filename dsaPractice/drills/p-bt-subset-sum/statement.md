# Does any subset reach the target?

Tier 1 drill · backtracking · unlocks LC 39, 416, 494, 698

## Brief

Return `true` if some subset of `nums` sums exactly to `target`. The empty subset sums
to 0. Values may be negative.

## Constraints

- `0 <= nums.length <= 18`
- `-100 <= nums[i], target <= 100`

## Examples

```
[2,4,8],   target = 10  ->  true    (2 + 8)
[2,4,8],   target = 5   ->  false
[],        target = 0   ->  true    (the empty subset)
[],        target = 1   ->  false
[1,-1],    target = 0   ->  true
[5],       target = 5   ->  true
```

## Signature

```java
public boolean subsetSum(int[] nums, int target)
```

## Target complexity

O(2^n) worst case. Early exit as soon as an answer is found.

## The skill

The include/exclude recursion again, now returning a **boolean up** rather than
collecting into a list — recursion shape 1 layered on top of the backtracking shape.

```java
private boolean go(int[] nums, int i, int remaining) {
    if (remaining == 0) return true;          // found one; stop
    if (i == nums.length) return false;       // ran out of elements
    return go(nums, i + 1, remaining - nums[i])   // include
        || go(nums, i + 1, remaining);            // exclude
}
```

Two things worth dwelling on:

- **`||` short-circuits, which gives you the early exit for free.** If the include
  branch finds an answer, the exclude branch never runs. No explicit "stop searching"
  flag, no exception-as-control-flow. This is why returning a boolean beats collecting
  results when you only need existence.
- **No un-choose step is needed.** `remaining - nums[i]` creates a new value rather than
  mutating shared state, so there is nothing to undo. Backtracking only needs an
  un-choose when you mutate something shared. Recognising when you *don't* need it is
  as useful as remembering when you do.

**Why negatives matter:** with negative values you cannot prune on `remaining < 0`,
because a later negative could bring the sum back up. LC 39 and 416 both guarantee
positive values and therefore permit that pruning; this drill deliberately does not, so
you have to notice the difference. Say out loud which assumption your pruning relies on.

## Hints

<details><summary>Hint 1 — base case order</summary>

Check `remaining == 0` **before** `i == nums.length`, or you will miss an answer that
completes exactly as the elements run out. `[5], target=5` catches this.

</details>

<details><summary>Hint 2 — the DP that follows</summary>

Once this works, note that `(i, remaining)` is a small state space, so memoising it
turns O(2^n) into O(n · range). That is precisely LC 416 partition-equal-subset-sum,
and the reason it is filed under both Backtracking and Dynamic Programming.

</details>
