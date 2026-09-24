# Binary Tree Paths

LeetCode 257 · Easy · https://leetcode.com/problems/binary-tree-paths/

## Brief

Return every root-to-leaf path as a string `"a->b->c"`, in any order. the standard `TreeNode` (`val`, `left`, `right`) is provided.

## Constraints

- `1 <= nodes <= 100`, values may be negative

## Examples

```
[1,2,3,null,5]  ->  ["1->2->5","1->3"]
[1]             ->  ["1"]
```

## Signature

```java
public List<String> binaryTreePaths(TreeNode root)
```

## Target complexity

O(n · h).

## The skill

**Ramp:** Tier 1 `p-tree-root-to-leaf`. Passing an immutable `String` down makes the
backtracking automatic — each call has its own prefix. With a shared `StringBuilder` you must
trim it back on return. Only leaves produce output.

## Hints

<details><summary>Hint 1</summary>

Mind the separator: none before the first value.

</details>
