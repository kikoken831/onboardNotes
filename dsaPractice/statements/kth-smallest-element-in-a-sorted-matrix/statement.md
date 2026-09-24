# Kth Smallest Element in a Sorted Matrix

LeetCode 378 · Medium · https://leetcode.com/problems/kth-smallest-element-in-a-sorted-matrix/

## Brief

An `n × n` matrix has sorted rows and sorted columns. Return the `k`-th smallest value
**counting duplicates**.

## Constraints

- `1 <= n <= 300`, `1 <= k <= n^2`; required memory better than O(n²)

## Examples

```
[[1,5,9],[10,11,13],[12,13,15]], k=8  ->  13
[[-5]], k=1                           ->  -5
```

## Signature

```java
public int kthSmallest(int[][] matrix, int k)
```

## Target complexity

O(n log(max − min)) by binary search on the value, or O(k log n) with a heap.

## The skill

Two good answers. A **min-heap** seeded with each row's first element, popped `k` times
(**ramp:** Tier 1 `p-heap-merge-k`). Or **binary search on the answer**: for a candidate value,
count the entries `<= mid` with the LC 240 staircase in O(n). The value search converges on a
value that actually appears because it finds the smallest `mid` with `count >= k`.

## Hints

<details><summary>Hint 1</summary>

Flattening and sorting is O(n² log n) and O(n²) memory — correct, and not what's asked.

</details>
