# 3Sum

LeetCode 15 · Medium · https://leetcode.com/problems/3sum/

## Brief

Return every **distinct** triplet `[a, b, c]` of values from `nums` (at different indices)
with `a + b + c == 0`. No triplet may appear twice. Any order of triplets, any order within.

## Constraints

- `3 <= nums.length <= 3000`
- `-10^5 <= nums[i] <= 10^5`

## Examples

```
[-1,0,1,2,-1,-4]  ->  [[-1,-1,2],[-1,0,1]]
[0,1,1]           ->  []
[0,0,0]           ->  [[0,0,0]]
```

## Signature

```java
public List<List<Integer>> threeSum(int[] nums)
```

## Target complexity

O(n²).

## The skill

Sort, fix `a[i]`, then two-pointer the rest for `-a[i]` — **2Sum on a sorted array**, `n`
times. The actual difficulty is **de-duplication**: skip equal `a[i]`s, and after a hit skip
equal values on both sides. A `Set<List<Integer>>` also works and is worth writing once, to
see what the skipping replaces. The judge includes `[0,0,0,0]`.

## Hints

<details><summary>Hint 1</summary>

`if (i > 0 && a[i] == a[i-1]) continue;` — why `i-1` and not `i+1`?

</details>
