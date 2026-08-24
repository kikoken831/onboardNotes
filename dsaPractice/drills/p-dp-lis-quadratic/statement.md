# Longest increasing subsequence, O(n^2)

Tier 1 drill · dynamic-programming · unlocks LC 300, 673, 354, 1143

## Brief

Return the length of the longest **strictly increasing** subsequence of `nums`. A
subsequence keeps relative order but need not be contiguous.

## Constraints

- `0 <= nums.length <= 2500`
- `-10^4 <= nums[i] <= 10^4`

## Examples

```
[10,9,2,5,3,7,101,18]  ->  4    ([2,3,7,101])
[0,1,0,3,2,3]          ->  4
[7,7,7,7]              ->  1    (strictly increasing, so ties do not extend)
[]                     ->  0
[5]                    ->  1
[3,2,1]                ->  1
```

## Signature

```java
public int lisLength(int[] nums)
```

## Target complexity

O(n²) is what this drill wants. The O(n log n) version is the follow-up.

## The skill

The state definition that makes this work, and it is not the obvious one.

The natural first guess — "`f(i)` = the LIS length among the first `i` elements" —
**does not work**, because to extend a subsequence you need to know what its last
element was, and that definition throws it away. This failure is worth experiencing;
it is the same class of mistake as the naive validate-BST.

The definition that does work anchors the subsequence at its end:

```
f(i) = length of the longest increasing subsequence ENDING EXACTLY AT index i
f(i) = 1 + max{ f(j) : j < i and nums[j] < nums[i] },  or 1 if there is no such j
answer = max over all i of f(i)
```

```java
int[] f = new int[nums.length];
int best = 0;
for (int i = 0; i < nums.length; i++) {
    f[i] = 1;
    for (int j = 0; j < i; j++) {
        if (nums[j] < nums[i]) f[i] = Math.max(f[i], f[j] + 1);
    }
    best = Math.max(best, f[i]);
}
return best;
```

Two consequences of "ending exactly at i" worth noting:

- **The answer is the max over the whole table, not `f[n-1]`.** The longest subsequence
  need not end at the last element. `[3,2,1]` makes this obvious; subtler inputs do not.
- **`<` not `<=`** in the comparison, because the requirement is *strictly* increasing.
  `[7,7,7,7]` is pinned for exactly this.

"Anchor the state at the last element" recurs constantly — LC 673
(count-of-LIS), 354 (russian-doll-envelopes, which is LIS after a sort), and the
longest-common-subsequence family all use it. LIS and LCS have no LeetCode Easy
problems, so this drill is the entry point.

## Hints

<details><summary>Hint 1 — the empty array</summary>

Returns 0, not 1. The outer loop never runs and `best` stays at its initial 0. Make
sure you initialised `best` to 0 rather than 1.

</details>

<details><summary>Hint 2 — the O(n log n) version</summary>

Maintain a `tails` array where `tails[k]` is the smallest possible tail of an increasing
subsequence of length `k+1`, and binary-search it for each element — which is exactly
`bsFirstGE` from the binary-search group. Write it after this one; it is a satisfying
payoff for two earlier drills, and it is the expected answer for LC 300.

</details>
