# Find K Closest Elements

LeetCode 658 · Medium · https://leetcode.com/problems/find-k-closest-elements/

## Brief

`arr` is sorted. Return the `k` values closest to `x`, **sorted ascending**. On a tie in
distance, the **smaller** value wins.

## Constraints

- `1 <= k <= n <= 10^4`, `-10^4 <= arr[i], x <= 10^4`

## Examples

```
[1,2,3,4,5], k=4, x=3   ->  [1,2,3,4]
[1,1,2,3,4,5], k=4, x=-1  ->  [1,1,2,3]
```

## Signature

```java
public List<Integer> findClosestElements(int[] arr, int k, int x)
```

## Target complexity

O(log(n − k) + k).

## The skill

The answer is a **contiguous window** of length `k`, so binary-search its *start* in
`[0, n − k]`: compare `x − arr[m]` with `arr[m + k] − x` to decide whether the window should
move right. Comparing absolute values breaks when both sides are equal-valued duplicates;
the signed form handles it. Shrinking from both ends is O(n) and a fine first answer.

## Hints

<details><summary>Hint 1</summary>

If `arr[m + k]` is strictly closer than `arr[m]`, the window must start after `m`.

</details>
