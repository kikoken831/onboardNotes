# Spiral Matrix

LeetCode 54 · Medium · https://leetcode.com/problems/spiral-matrix/

## Brief

Return the values of an `m × n` matrix in clockwise spiral order, from the top-left.

## Constraints

- `1 <= m, n <= 10`

## Examples

```
[[1,2,3],[4,5,6],[7,8,9]]            ->  [1,2,3,6,9,8,7,4,5]
[[1,2,3,4],[5,6,7,8],[9,10,11,12]]   ->  [1,2,3,4,8,12,11,10,9,5,6,7]
```

## Signature

```java
public List<Integer> spiralOrder(int[][] matrix)
```

## Target complexity

O(m · n).

## The skill

Four shrinking boundaries: walk the top row, right column, bottom row (reversed), left column
(reversed), then pull all four in. The bug lives in **single-row and single-column** remainders,
where the bottom and left walks revisit cells the top and right walks already took. Guard them
(`top < bot`, `left < right`); `[[1],[2],[3]]` and `[[1,2,3]]` are pinned.

## Hints

<details><summary>Hint 1</summary>

Alternatively: walk with a direction vector and turn when the next cell is out of bounds or seen.

</details>
