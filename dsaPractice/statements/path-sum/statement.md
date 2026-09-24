# Path Sum

LeetCode 112 · Easy · https://leetcode.com/problems/path-sum/

## Brief

Is there a **root-to-leaf** path whose values sum to `targetSum`? the standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `0 <= nodes <= 5000`, values may be negative

## Examples

```
[5,4,8,11,null,13,4,7,2,null,null,null,1], 22  ->  true
[1,2,3], 5                                     ->  false
[], 0                                          ->  false
```

## Signature

```java
public boolean hasPathSum(TreeNode root, int targetSum)
```

## Target complexity

O(n).

## The skill

**Ramp:** Tier 1 `p-rec-depth-thread-down` — thread the remaining sum **down**, test at the
leaf. Two traps, both pinned: the empty tree has no path even for target 0, and the check must
happen at a leaf, not at any node (`[1,2], 1` is false). Negatives mean you can't prune early.

## Hints

<details><summary>Hint 1</summary>

At a leaf, compare `leaf.val` with what's left of the target.

</details>
