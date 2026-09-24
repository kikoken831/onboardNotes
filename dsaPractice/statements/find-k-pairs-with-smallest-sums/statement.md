# Find K Pairs with Smallest Sums

LeetCode 373 · Medium · https://leetcode.com/problems/find-k-pairs-with-smallest-sums/

## Brief

Both arrays are sorted. Return the `k` pairs `(u, v)` — `u` from `nums1`, `v` from `nums2` — with the smallest sums (`k` never exceeds `nums1.length * nums2.length`). With ties, any correct selection is accepted.

## Constraints

- `1 <= lengths <= 10^5`, `1 <= k <= 10^4`, `k <= nums1.length * nums2.length`

## Examples

```
[1,7,11], [2,4,6], k=3  ->  [[1,2],[1,4],[1,6]]
[1,1,2], [1,2,3], k=2   ->  [[1,1],[1,1]]
```

## Signature

```java
public List<List<Integer>> kSmallestPairs(int[] nums1, int[] nums2, int k)
```

## Target complexity

O(k log k).

## The skill

**Ramp:** Tier 1 `p-heap-merge-k`. Treat each `nums1[i]` as the head of a sorted list `nums1[i] + nums2[0..]` and **merge those lists** with a heap: seed `(i, 0)` for the first `k` rows, and after popping `(i, j)` push `(i, j + 1)`. Generating all `m·n` pairs times out at `10^5 × 10^5`.

## Hints

<details><summary>Hint 1</summary>

Compare sums as `long` if values are large.

</details>
