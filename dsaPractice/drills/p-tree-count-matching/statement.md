# Count nodes satisfying a predicate

Tier 1 drill · recursion-shape · unlocks LC 938, 1315

## Brief

Return the sum of all node values that lie within the inclusive range `[low, high]`.
Empty tree returns 0. The tree is **not** necessarily a BST — visit every node.

## Constraints

- `0 <= node count <= 2 * 10^4`
- `-10^5 <= values, low, high <= 10^5`, and `low <= high`

## Examples

```
[10,5,15,3,7,null,18], low=7,  high=15  ->  32   (10 + 7 + 15)
[10,5,15,3,7,13,18,1,null,6], low=6, high=10 -> 23  (10 + 7 + 6)
[], low=1, high=2   ->  0
[5], low=5, high=5  ->  5
[5], low=6, high=9  ->  0
```

## Signature

```java
public int rangeSum(TreeNode root, int low, int high)
```

## Target complexity

O(n) time, O(h) stack.

## The skill

Shape 1 with a filter, and a deliberately easy one — it is here as the **consolidation**
drill for the group. If shapes 1 to 3 have landed, this should take under two minutes
and need no debugging. If it does not, go back to `p-rec-sum-return-up` rather than
pressing on.

```java
if (root == null) return 0;
int self = (root.val >= low && root.val <= high) ? root.val : 0;
return self + rangeSum(root.left, low, high) + rangeSum(root.right, low, high);
```

Note `low` and `high` are threaded down unchanged. They are context, but constant
context, so this is not really shape 3 — it just looks like it. Worth being able to
tell the difference.

## Hints

<details><summary>Hint 1 — the BST version</summary>

LC 938 guarantees a BST, which lets you prune: if `root.val < low` the entire left
subtree is out of range and can be skipped. That turns O(n) into O(h + k). Once this
version works, write the pruning one and convince yourself the pruning is sound.

</details>
