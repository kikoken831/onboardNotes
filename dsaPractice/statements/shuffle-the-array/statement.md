# Shuffle the Array

LeetCode 1470 · Easy · https://leetcode.com/problems/shuffle-the-array/

## Brief

`nums` is `[x1,x2,...,xn,y1,y2,...,yn]`. Return `[x1,y1,x2,y2,...,xn,yn]`.

## Constraints

- `1 <= n <= 500`
- `nums.length == 2n`
- `1 <= nums[i] <= 10^3`

## Examples

```
[2,5,1,3,4,7], n=3      ->  [2,3,5,4,1,7]
[1,2,3,4,4,3,2,1], n=4  ->  [1,4,2,3,3,2,4,1]
[1,1,2,2], n=2          ->  [1,2,1,2]
```

## Signature

```java
public int[] shuffle(int[] nums, int n)
```

## Target complexity

O(n) time.

## The skill

Two read pointers into one array, one write pointer into another. The drill is writing
the output index formula (`2i` and `2i+1`) instead of a counter you forget to bump.

## Hints

<details><summary>Hint 1</summary>

`x` values live at `i`, `y` values at `n + i`, for `i` in `0..n-1`.

</details>
