# Minimum Number of Arrows to Burst Balloons

LeetCode 452 · Medium · https://leetcode.com/problems/minimum-number-of-arrows-to-burst-balloons/

## Brief

Each balloon spans `[xstart, xend]` on the x-axis. An arrow shot at `x` bursts every balloon
with `xstart <= x <= xend`. Return the minimum number of arrows to burst them all.

## Constraints

- `1 <= n <= 10^5`, `-2^31 <= xstart < xend <= 2^31 - 1`

## Examples

```
[[10,16],[2,8],[1,6],[7,12]]  ->  2     arrows at 6 and 11
[[1,2],[2,3],[3,4],[4,5]]     ->  2     touching balloons share an arrow
```

## Signature

```java
public int findMinArrowShots(int[][] points)
```

## Target complexity

O(n log n).

## The skill

LC 435's greedy wearing a costume: sort by end, shoot at the first end, skip every balloon that
arrow hits. Two traps, both pinned: touching counts as overlapping here (`>`, not `>=`), and the
comparator `(a, b) -> a[1] - b[1]` **overflows** with coordinates near `±2^31`, silently
mis-sorting. Use `Integer.compare`.

## Hints

<details><summary>Hint 1</summary>

A new arrow is needed only when a balloon starts **after** the current arrow's position.

</details>
