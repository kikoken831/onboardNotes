# Sum by returning values up

Tier 1 drill · recursion-shape · unlocks LC 104, 543, 124

## Brief

Return the sum of all values in a binary tree. Empty tree sums to 0.

Write it **recursively**, with no accumulator parameter and no field.

## Constraints

- `0 <= node count <= 10^4`
- `-1000 <= node values <= 1000`

## Examples

```
[1,2,3]        ->  6
[]             ->  0
[5]            ->  5
[1,null,2,null,3] -> 6
```

Trees use LeetCode's level-order notation: `[1,2,3]` is root 1 with children 2 and 3.

## Signature

```java
public int sumTree(TreeNode root)
```

`TreeNode` is on the classpath already — do not declare it.

## Target complexity

O(n) time, O(h) stack.

## The skill

**Recursion shape 1 of 3: return a value up.**

```java
if (root == null) return 0;
return root.val + sumTree(root.left) + sumTree(root.right);
```

The defining property: each call computes its own answer from its children's answers
and hands it back. Nothing is passed *down*, nothing is stored outside the call stack.
This is postorder — children before parent — whether or not you think of it that way.

Most tree problems are this shape, and recognising it is worth more than the code:
height, count, sum, max, isBalanced, diameter, LCA are all "return a value up". When
you see a tree problem, the first question is which of the three shapes it wants.

The base case returns the **identity** of whatever you are combining: 0 for a sum, 1 or
0 for a count, `-1` or `Integer.MIN_VALUE` for a max. Choosing it wrong is the usual bug.

## Hints

<details><summary>Hint 1 — why null returns 0</summary>

0 is the additive identity, so adding it changes nothing and the empty subtree
contributes nothing. If you were computing a product the base case would be 1. Ask
"what value makes the combine step a no-op?" and the base case follows.

</details>
