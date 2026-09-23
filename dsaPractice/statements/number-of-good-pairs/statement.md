# Number of Good Pairs

LeetCode 1512 · Easy · https://leetcode.com/problems/number-of-good-pairs/

## Brief

A pair `(i, j)` is good if `nums[i] == nums[j]` and `i < j`. Count the good pairs.

## Constraints

- `1 <= nums.length <= 100`
- `1 <= nums[i] <= 100`

## Examples

```
[1,2,3,1,1,3]  ->  4    (0,3), (0,4), (3,4), (2,5)
[1,1,1,1]      ->  6
[1,2,3]        ->  0
```

## Signature

```java
public int numIdenticalPairs(int[] nums)
```

## Target complexity

O(n) with a count map. O(n²) passes here, but write the linear one.

## The skill

**Count as you go.** When you meet a value you've seen `c` times before, it forms `c` new
pairs — add `c`, *then* increment. The order of those two lines is the whole problem.

The closed form `c·(c-1)/2` per value works too, and is the same idea done afterwards.

## Hints

<details><summary>Hint 1</summary>

`pairs += count[x]; count[x]++;` — why must it be in that order?

</details>
