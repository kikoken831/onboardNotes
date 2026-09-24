# Path Sum III

LeetCode 437 · Medium · https://leetcode.com/problems/path-sum-iii/

## Brief

Count the **downward** paths (parent to child, starting and ending anywhere) whose values sum
to `targetSum`. the standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `0 <= nodes <= 1000`, `-10^9 <= val <= 10^9`, `-1000 <= targetSum <= 1000`

## Examples

```
[10,5,-3,3,2,null,11,3,-2,null,1], 8           ->  3
[5,4,8,11,null,13,4,7,2,null,null,5,1], 22     ->  3
```

## Signature

```java
public int pathSum(TreeNode root, int targetSum)
```

## Target complexity

O(n) with prefix sums.

## The skill

**Ramp:** Tier 1 `p-prefix-count-target`, on a tree. Carry the root-to-here prefix sum; the
paths ending here are counted by `seen[sum − target]`. The tree twist is **backtracking** the
map on the way out, so it only ever holds this node's ancestors. Values reach `10^9`, so prefix
sums must be `long` — the judge has a case where `int` sums overflow into a false match.

## Hints

<details><summary>Hint 1</summary>

The O(n²) version — start a sum at every node — is a good first step and also needs `long`.

</details>
