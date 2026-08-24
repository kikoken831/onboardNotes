# Depth by threading state down

Tier 1 drill · recursion-shape · unlocks LC 112, 129, 257, 113

## Brief

Return the sum of `depth * value` over every node, where the root is at depth 1, its
children at depth 2, and so on. Empty tree returns 0.

The point is *how* you get the depth to each node, not the arithmetic.

## Constraints

- `0 <= node count <= 10^4`
- `-1000 <= node values <= 1000`

## Examples

```
[1,2,3]   ->  1*1 + 2*2 + 3*2  = 11
[]        ->  0
[5]       ->  5
[1,2,null,3] -> 1*1 + 2*2 + 3*3 = 14
```

## Signature

```java
public int depthWeightedSum(TreeNode root)
```

## Target complexity

O(n) time, O(h) stack.

## The skill

**Recursion shape 2 of 3: thread state down.**

The depth is not something a node can compute from its children — it is context from
above. So it has to travel *down* as a parameter, which means a private helper, because
the public signature has no room for it:

```java
public int depthWeightedSum(TreeNode root) {
    return go(root, 1);
}
private int go(TreeNode n, int depth) {
    if (n == null) return 0;
    return depth * n.val + go(n.left, depth + 1) + go(n.right, depth + 1);
}
```

**"Add a helper with extra parameters" is the move**, and being slow to reach for it is
one of the most common ways people get stuck on tree problems. If the public signature
does not carry what you need, wrap it.

This one happens to combine both directions — depth goes down, the sum comes back up —
which is extremely common. The distinction that matters is *which* information flows
which way:

- computable from children → return it up
- context from ancestors → pass it down

Root-to-leaf path problems (LC 112, 113, 257, 129) are all this shape. So is validating
a BST (98), where the valid range is threaded down.

## Hints

<details><summary>Hint 1 — the accumulator alternative</summary>

You can also thread the *running total* down and return nothing, using a field or an
`int[1]` to collect the result. It works, but a returned value composes better and does
not need mutable shared state. Try both and notice which reads more clearly.

</details>
