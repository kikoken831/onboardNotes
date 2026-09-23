# Binary Tree Preorder Traversal

LeetCode 144 · Easy · https://leetcode.com/problems/binary-tree-preorder-traversal/

## Brief

Return the node values in **preorder**: node, left subtree, right subtree.

## Constraints

- `0 <= number of nodes <= 100`
- `-100 <= Node.val <= 100`

## Examples

```
[1,null,2,3]                        ->  [1,2,3]
[1,2,3,4,5,null,8,null,null,6,7,9]  ->  [1,2,4,5,6,7,3,8,9]
```

## Signature

```java
public List<Integer> preorderTraversal(TreeNode root)
```

## Target complexity

O(n) time, O(h) space.

## The skill

**Ramp:** Tier 1 `p-tree-preorder-iter`. The iterative version is the easiest of the three:
pop, visit, push children. The one detail is **push right before left**, because a stack
reverses order.

## Hints

<details><summary>Hint 1</summary>

With `ArrayDeque`, use `push`/`pop` (stack end). Mixing in `add`/`poll` turns it into a
queue and gives you level order by accident.

</details>
