# Fruit Into Baskets

LeetCode 904 · Medium · https://leetcode.com/problems/fruit-into-baskets/

## Brief

`fruits[i]` is the fruit type of tree `i`. With **two** baskets, each holding one type, you
pick one fruit from every tree moving right from some start, stopping at the first tree whose
type fits neither basket. Return the most fruit you can pick — i.e. the longest subarray with
**at most two distinct values**.

## Constraints

- `1 <= n <= 10^5`

## Examples

```
[1,2,1]                    ->  3
[1,2,3,2,2]                ->  4    [2,3,2,2]
[3,3,3,1,2,1,1,2,3,3,4]    ->  5
```

## Signature

```java
public int totalFruit(int[] fruits)
```

## Target complexity

O(n).

## The skill

Strip the story away and it's "longest window with at most `K` distinct values", `K = 2`.
Keep counts in a map; when it holds three keys, shrink from the left and **remove** a key
whose count reaches zero — forgetting the removal is the usual bug.

## Hints

<details><summary>Hint 1</summary>

`if (c.merge(x, -1, Integer::sum) == 0) c.remove(x);`

</details>
