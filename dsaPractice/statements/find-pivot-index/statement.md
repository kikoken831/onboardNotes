# Find Pivot Index

LeetCode 724 · Easy · https://leetcode.com/problems/find-pivot-index/

## Brief

The pivot index is where the sum of everything **strictly left** equals the sum of
everything **strictly right**. Return the **leftmost** pivot, or `-1`. The sum of an empty
side is 0.

## Constraints

- `1 <= nums.length <= 10^4`
- `-1000 <= nums[i] <= 1000`

## Examples

```
[1,7,3,6,5,6]  ->  3    left 1+7+3 = 11, right 5+6 = 11
[1,2,3]        ->  -1
[2,1,-1]       ->  0    left is empty (0), right 1 + -1 = 0
```

## Signature

```java
public int pivotIndex(int[] nums)
```

## Target complexity

O(n) time, O(1) space.

## The skill

`right = total - left - nums[i]`, so one prefix variable is enough — no second array.

Negatives are allowed, so you **cannot** stop early when `left` exceeds `right`, and
index `0` can be the answer.

## Hints

<details><summary>Hint 1</summary>

Compute `total` once. Then walk left-to-right, checking before you add `nums[i]` to
`left`.

</details>
