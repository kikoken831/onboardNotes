# K Closest Points to Origin

LeetCode 973 · Medium · https://leetcode.com/problems/k-closest-points-to-origin/

## Brief

Return the `k` points closest to the origin (Euclidean distance), in any order. The answer is unique.

## Constraints

- `1 <= k <= n <= 10^4`, coordinates in `[-10^4, 10^4]`

## Examples

```
[[1,3],[-2,2]], k=1         ->  [[-2,2]]
[[3,3],[5,-1],[-2,4]], k=2  ->  [[3,3],[-2,4]]
```

## Signature

```java
public int[][] kClosest(int[][] points, int k)
```

## Target complexity

O(n log k) with a heap; O(n) average with quickselect.

## The skill

**Ramp:** Tier 1 `p-heap-comparator`. Keep a **max**-heap of size `k` keyed by squared distance; anything farther than its top is discarded. Compare **squared** distances — `sqrt` is unnecessary and inexact — and compute them as `long` if coordinates are large.

## Hints

<details><summary>Hint 1</summary>

A comparator of `(a, b) -> d(b) - d(a)` on `int` distances can overflow; use `Long.compare`.

</details>
