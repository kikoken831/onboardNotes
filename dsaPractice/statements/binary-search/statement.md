# Binary Search

LeetCode 704 · Easy · https://leetcode.com/problems/binary-search/

## Brief

`nums` is sorted ascending with distinct values. Return the index of `target`, or `-1`.

## Constraints

- `1 <= n <= 10^4`, distinct, sorted

## Examples

```
[-1,0,3,5,9,12], 9  ->  4
[-1,0,3,5,9,12], 2  ->  -1
```

## Signature

```java
public int search(int[] nums, int target)
```

## Target complexity

O(log n).

## The skill

**Ramp:** Tier 1 `p-bs-exact`. The closed-interval template: `lo <= hi`, `hi = mid - 1`,
`lo = mid + 1`. Pick one template and never mix its pieces with another's — that's where
infinite loops come from.

## Hints

<details><summary>Hint 1</summary>

`mid = lo + (hi - lo) / 2` avoids overflow on huge arrays.

</details>
