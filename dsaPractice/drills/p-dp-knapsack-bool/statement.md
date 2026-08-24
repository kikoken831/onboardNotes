# Reachable-sum knapsack table

Tier 1 drill · dynamic-programming · unlocks LC 416, 494, 698, 1049

## Brief

Given non-negative `nums` and a `target`, return `true` if some subset sums exactly to
`target`. The empty subset sums to 0.

This is the same question as the backtracking drill `p-bt-subset-sum`, but the inputs
are large enough that O(2^n) will not finish. Values are non-negative here, which is
what makes the DP possible.

## Constraints

- `1 <= nums.length <= 200`
- `0 <= nums[i] <= 100`
- `0 <= target <= 20000`

## Examples

```
[1,5,11,5], target=11  ->  true
[1,2,3,5],  target=5   ->  true
[1,2,5],    target=4   ->  false
[1],        target=0   ->  true
[0],        target=0   ->  true
[100],      target=100 ->  true
```

## Signature

```java
public boolean knapsackReachable(int[] nums, int target)
```

## Target complexity

O(n · target) time, O(target) space.

## The skill

The 0-1 knapsack table, and the one-dimensional collapse that everyone gets wrong once.

`reachable[s]` means "some subset of the elements considered so far sums to `s`". Start
with `reachable[0] = true` and process elements one at a time:

```java
boolean[] reachable = new boolean[target + 1];
reachable[0] = true;
for (int x : nums) {
    for (int s = target; s >= x; s--) {       // DOWNWARD
        reachable[s] |= reachable[s - x];
    }
}
return reachable[target];
```

**The inner loop must run downward, and this is the whole drill.**

Going upward, `reachable[s - x]` may already have been updated *by the current element*
in this same pass — so you would be using `x` more than once, silently turning 0-1
knapsack into unbounded knapsack. `[100], target=100` still passes; `[1], target=2`
would wrongly return true. Iterating downward guarantees `reachable[s - x]` still
reflects the previous element's state.

If that argument does not feel solid, write the honest two-dimensional version first —
`dp[i][s]` for "using the first i elements" — where the direction cannot matter because
the rows are separate. Then collapse it and see exactly which read the downward
direction is protecting. That two-step is worth doing once; the rule sticks afterwards.

**Why non-negative values matter:** with negatives the reachable sums are not bounded
below by 0, the array has nowhere to live, and the DP does not apply. That is precisely
why the backtracking drill allowed negatives and this one does not — same question,
different assumption, different tool.

## Hints

<details><summary>Hint 1 — LC 416</summary>

Partition-equal-subset-sum is this with `target = total / 2`, returning false
immediately when the total is odd. Two extra lines.

</details>

<details><summary>Hint 2 — prove the direction to yourself</summary>

Run `[1]` with `target = 2` using an upward inner loop. You should get `true`, which is
wrong. That single experiment is more convincing than the paragraph above.

</details>
