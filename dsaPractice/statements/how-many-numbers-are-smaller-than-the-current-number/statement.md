# How Many Numbers Are Smaller Than the Current Number

LeetCode 1365 · Easy · https://leetcode.com/problems/how-many-numbers-are-smaller-than-the-current-number/

## Brief

For each `nums[i]`, count how many values in the array are **strictly smaller** than it.

## Constraints

- `2 <= nums.length <= 500`
- `0 <= nums[i] <= 100`

## Examples

```
[8,1,2,2,3]  ->  [4,0,1,1,3]
[6,5,4,8]    ->  [2,1,0,3]
[7,7,7,7]    ->  [0,0,0,0]
```

## Signature

```java
public int[] smallerNumbersThanCurrent(int[] nums)
```

## Target complexity

O(n + V) with counting (`V = 101`). O(n²) and O(n log n) sort-based both pass.

## The skill

A tiny value range is a hint in itself: **count values, then prefix-sum the counts**, and
"how many are below `v`" becomes one array read. That's counting sort's first half.

Equal values must all get the same answer — `[7,7,7,7]` is `0` everywhere.

## Hints

<details><summary>Hint 1</summary>

Brute force first: for each `i`, count `j` with `nums[j] < nums[i]`.

</details>

<details><summary>Hint 2 — linear</summary>

`count[v]` = how many times `v` appears. After a running sum, the number of values
strictly below `v` is the sum of counts for `0..v-1`.

</details>
