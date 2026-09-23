# Intersection of Two Arrays

LeetCode 349 · Easy · https://leetcode.com/problems/intersection-of-two-arrays/

## Brief

Return the values that appear in both arrays. **Each value appears once** in the result,
in any order.

## Constraints

- `1 <= nums1.length, nums2.length <= 1000`
- `0 <= nums1[i], nums2[i] <= 1000`

## Examples

```
[1,2,2,1], [2,2]      ->  [2]
[4,9,5], [9,4,9,8,4]  ->  [9,4]   (or [4,9])
```

## Signature

```java
public int[] intersection(int[] nums1, int[] nums2)
```

## Target complexity

O(n + m) with sets, or O(n log n + m log m) sorting with two pointers.

## The skill

Set semantics: "once each" is what the second set is for. The common bug is adding every
match, so `[2]` comes back as `[2,2]`.

The sort-and-two-pointers version is worth writing too — it's the one that survives when
you're told memory is tight.

## Hints

<details><summary>Hint 1</summary>

A set of `nums1`, then a **second** set collecting matches from `nums2`.

</details>
