# Combination Sum IV

LeetCode 377 · Medium · https://leetcode.com/problems/combination-sum-iv/

## Brief

Count the **ordered** sequences of values from `nums` (distinct, reusable) that sum to `target`. `(1,2)` and `(2,1)` are different. The answer fits in 32 bits.

## Constraints

- `1 <= nums.length <= 200`, `1 <= target <= 1000`

## Examples

```
[1,2,3], 4  ->  7
[9], 3      ->  0
```

## Signature

```java
public int combinationSum4(int[] nums, int target)
```

## Target complexity

O(target · n).

## The skill

Despite the name, this counts **permutations**, and the loop order is what decides it: put **target in the outer loop** and every ordering is counted; put the numbers outside and you count combinations (LC 518) instead. That one swap is the lesson — know which loop order means what.

## Hints

<details><summary>Hint 1</summary>

`d[t] = Σ d[t − x]` over `x <= t`.

</details>
