# Majority Element

LeetCode 169 · Easy · https://leetcode.com/problems/majority-element/

## Brief

Return the value that appears **more than** `n / 2` times. It always exists.

## Constraints

- `1 <= n <= 5 * 10^4`
- `-10^9 <= nums[i] <= 10^9`

## Examples

```
[3,2,3]          ->  3
[2,2,1,1,1,2,2]  ->  2
```

## Signature

```java
public int majorityElement(int[] nums)
```

## Target complexity

O(n) time, O(1) space.

## The skill

**Boyer–Moore voting**: keep a candidate and a counter; a match votes +1, anything else −1,
and when the count hits zero the next value becomes the candidate. The majority survives
because it outnumbers everything else combined. (Sorting and taking `nums[n/2]` is O(n log n)
and also correct.)

## Hints

<details><summary>Hint 1</summary>

Pair each majority element with a non-majority element and cancel both. What's left?

</details>
