# Top K Frequent Elements

LeetCode 347 · Medium · https://leetcode.com/problems/top-k-frequent-elements/

## Brief

Return the `k` most frequent values, in any order. The answer is guaranteed unique (no tie
at the boundary).

## Constraints

- `1 <= n <= 10^5`
- `1 <= k <=` number of distinct values

## Examples

```
[1,1,1,2,2,3], k=2  ->  [1,2]
[1], k=1            ->  [1]
```

## Signature

```java
public int[] topKFrequent(int[] nums, int k)
```

## Target complexity

Better than O(n log n): a size-`k` heap is O(n log k); bucket sort is O(n).

## The skill

**Ramp:** Tier 1 `p-heap-k-smallest`. Count, then select. Two good selections: a **min-heap
of size `k`** keyed by frequency, or **bucket sort** — frequencies are bounded by `n`, so
`bucket[f]` lists the values with frequency `f` and you read buckets from the top.

## Hints

<details><summary>Hint 1</summary>

Sorting the map entries by count is O(d log d) and passes. Which bound on frequency makes
bucket sort possible?

</details>
