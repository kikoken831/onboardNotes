# Binary Tree Postorder Traversal

LeetCode 145 · Easy · https://leetcode.com/problems/binary-tree-postorder-traversal/

## Brief

Return the node values in **postorder**: left subtree, right subtree, node.

## Constraints

- `0 <= number of nodes <= 100`
- `-100 <= Node.val <= 100`

## Examples

```
[1,null,2,3]                        ->  [3,2,1]
[1,2,3,4,5,null,8,null,null,6,7,9]  ->  [4,6,7,5,2,9,8,3,1]
```

## Signature

```java
public List<Integer> postorderTraversal(TreeNode root)
```

## Target complexity

O(n) time, O(h) space.

## The skill

**Ramp:** Tier 1 `p-tree-postorder-iter`. The hard one iteratively, because a node is visited
only after **both** children — so on the way back up you must know whether you arrived from
the left or the right. A `lastVisited` pointer answers that.

The shortcut — preorder with left and right swapped, then reverse — is correct and worth
knowing, but it isn't a streaming traversal: it can't emit a value until the whole tree has
been walked.

## Hints

<details><summary>Hint 1</summary>

Peek at the top. If it has a right child you haven't just come back from, go right.
Otherwise visit it, pop it, and remember it as `last`.

</details>
