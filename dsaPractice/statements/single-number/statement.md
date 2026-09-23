# Single Number

LeetCode 136 · Easy · https://leetcode.com/problems/single-number/

## Brief

Every value appears **twice** except one, which appears once. Find it.

## Constraints

- `1 <= nums.length <= 3 * 10^4`
- `-3 * 10^4 <= nums[i] <= 3 * 10^4`

## Examples

```
[2,2,1]      ->  1
[4,1,2,1,2]  ->  4
```

## Signature

```java
public int singleNumber(int[] nums)
```

## Target complexity

O(n) time and **O(1)** space — that constraint is the point.

## The skill

XOR: `x ^ x == 0` and `x ^ 0 == x`, and it's commutative, so XOR-ing everything cancels
every pair and leaves the single value — negatives included. A hash map is O(n) space and a
fine first answer; the follow-up is what rules it out.

## Hints

<details><summary>Hint 1</summary>

Which operator makes `a ⊕ a` disappear regardless of order?

</details>
