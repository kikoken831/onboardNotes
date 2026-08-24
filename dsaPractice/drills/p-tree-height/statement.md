# Height of a binary tree

Tier 1 drill · recursion-shape · unlocks LC 104, 110, 543, 111

## Brief

Return the height of a binary tree: the number of nodes on the longest root-to-leaf
path. An empty tree has height 0; a single node has height 1.

## Constraints

- `0 <= node count <= 10^4`

## Examples

```
[3,9,20,null,null,15,7]  ->  3
[]                       ->  0
[1]                      ->  1
[1,2,null,3,null,4]      ->  4   (a left-leaning chain)
```

## Signature

```java
public int treeHeight(TreeNode root)
```

## Target complexity

O(n) time, O(h) stack.

## The skill

Shape 1 again, but with the combine step being `max` rather than `+`, and the base case
correspondingly 0 rather than 0-as-identity-of-sum.

```java
if (root == null) return 0;
return 1 + Math.max(treeHeight(root.left), treeHeight(root.right));
```

**Height in nodes vs height in edges** is the definition trap and it is worth settling
once: this drill and LC 104 count *nodes*, so a single node is 1. Some textbooks count
*edges*, making a single node 0. Both conventions are used; the bug is mixing them
mid-problem. State which you are using before you write the base case.

Note this is also the first drill where the recursion depth can be a real problem: a
degenerate left-leaning chain of 45,000 nodes will overflow the default stack. The
generator here stays small, but LC 104 on an adversarial input is a genuine risk —
see the Tier 0 measurement.

## Hints

<details><summary>Hint 1 — minimum depth is not symmetric</summary>

Once this works, try changing `max` to `min` and see what breaks. LC 111 minimum-depth
is *not* just this with `min`, because a node with one null child is not a leaf, and
`min` would report 1 for it. That asymmetry is the whole difficulty of 111, and
noticing it here is cheaper than noticing it there.

</details>
