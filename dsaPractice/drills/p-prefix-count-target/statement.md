# Count subarrays summing to k

Tier 1 drill · prefix-sum · unlocks LC 560, 437, 974, 523

## Brief

Return the number of **contiguous** subarrays of `nums` whose sum equals `k`. Values may
be negative and subarrays may overlap; count every distinct `(start, end)` pair.

## Constraints

- `1 <= nums.length <= 2 * 10^4`
- `-1000 <= nums[i], k <= 1000`

## Examples

```
[1,1,1],     k=2   ->  2    ([0,1] and [1,2])
[1,2,3],     k=3   ->  2    ([0,1] and [2,2])
[1],         k=0   ->  0
[0,0,0],     k=0   ->  6    (every one of the 6 subarrays)
[1,-1,0],    k=0   ->  3
[-1,-1,1],   k=0   ->  1
```

## Signature

```java
public int countSubarraysSumK(int[] nums, int k)
```

## Target complexity

O(n) time, O(n) space.

## The skill

The combination that makes this O(n): **prefix sums plus the seen-map from the hash
drill.**

A subarray `(i..j)` sums to `k` exactly when `p[j+1] - p[i] == k`, i.e. when
`p[i] == p[j+1] - k`. So walk the array maintaining the running prefix, and at each
step ask how many earlier prefixes had the value `running - k`.

```java
Map<Integer,Integer> seen = new HashMap<>();
seen.put(0, 1);                      // the empty prefix
int running = 0, count = 0;
for (int x : nums) {
    running += x;
    count += seen.getOrDefault(running - k, 0);   // look up BEFORE inserting
    seen.merge(running, 1, Integer::sum);
}
return count;
```

Three things that are each a bug if you miss them:

- **`seen.put(0, 1)` before the loop.** This accounts for subarrays that start at index
  0 — their prefix `p[i]` is the empty prefix. Omit it and every such subarray is
  missed. `[1,2,3], k=3` catches it.
- **Look up before inserting**, exactly as in the two-sum drill, or a zero-length
  subarray counts itself. `[1], k=0` must return 0.
- **Count occurrences, not presence.** A `Set` would undercount when the same prefix
  value recurs. `[0,0,0], k=0` returns 6, and only a counting map gets there.

**Why sliding window does not work here:** negative values mean the running sum is not
monotonic, so shrinking the window can *increase* the sum. Sliding window needs
monotonicity; prefix-plus-map does not. That distinction is exactly what separates this
from LC 209, and it is the kind of thing an interviewer probes.

## Hints

<details><summary>Hint 1 — trace the zeros</summary>

Walk `[0,0,0], k=0` by hand: prefixes are 0,0,0,0 and the map accumulates. You should
count 1 + 2 + 3 = 6. If you get 3, you are counting presence rather than occurrences.

</details>
