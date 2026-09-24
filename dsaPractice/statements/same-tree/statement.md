# Same Tree

LeetCode 100 · Easy · https://leetcode.com/problems/same-tree/

## Brief

Are the two trees structurally identical with equal values? The standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `0 <= nodes <= 100`

## Examples

```
[1,2,3], [1,2,3]    ->  true
[1,2],   [1,null,2] ->  false
```

## Signature

```java
public boolean isSameTree(TreeNode p, TreeNode q)
```

## Target complexity

O(n).

## The skill

The two-tree recursion that Symmetric Tree (LC 101) mirrors: both null → true; one null →
false; values equal and both subtrees equal. Comparing traversals alone loses shape.

## Hints

<details><summary>Hint 1</summary>

`if (p == null || q == null) return p == q;`

</details>
