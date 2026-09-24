# House Robber II

LeetCode 213 · Medium · https://leetcode.com/problems/house-robber-ii/

## Brief

LC 198, but the houses are in a **circle**: the first and last are adjacent.

## Constraints

- `1 <= n <= 100`

## Examples

```
[2,3,2]    ->  3
[1,2,3,1]  ->  4
```

## Signature

```java
public int rob(int[] nums)
```

## Target complexity

O(n).

## The skill

Break the circle: the first and last house can't both be robbed, so solve LC 198 twice — once without the last house, once without the first — and take the better. A single house is its own special case.

## Hints

<details><summary>Hint 1</summary>

`max(rob(a[0..n−2]), rob(a[1..n−1]))`

</details>
