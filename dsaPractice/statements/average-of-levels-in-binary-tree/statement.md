# Average of Levels in Binary Tree

LeetCode 637 · Easy · https://leetcode.com/problems/average-of-levels-in-binary-tree/

## Brief

Return the average value of each level, top to bottom. Answers within `10^-5` are accepted.
The standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `1 <= nodes <= 10^4`, `-2^31 <= val <= 2^31 - 1`

## Examples

```
[3,9,20,null,null,15,7]  ->  [3.0,14.5,11.0]
```

## Signature

```java
public List<Double> averageOfLevels(TreeNode root)
```

## Target complexity

O(n).

## The skill

LC 102's loop with a sum instead of a list. The trap is the constraint line: values go to
`2^31 − 1`, so an `int` level sum overflows. The judge pins a root plus a level of two
`Integer.MAX_VALUE`s.

## Hints

<details><summary>Hint 1</summary>

`long sum`, then `(double) sum / count`.

</details>
