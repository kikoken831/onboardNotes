# Balanced check with a sentinel height

Tier 1 drill · recursion-shape · unlocks LC 110, 124

## Brief

Return `true` if the tree is height-balanced: for **every** node, the heights of its two
subtrees differ by at most 1. An empty tree is balanced.

Must be O(n). The obvious recursive definition is O(n²) and will time out.

## Constraints

- `0 <= node count <= 5000`

## Examples

```
[3,9,20,null,null,15,7]            ->  true
[1,2,2,3,3,null,null,4,4]          ->  false
[]                                 ->  true
[1,2,null,3]                       ->  false
[1,2,3]                            ->  true
```

## Signature

```java
public boolean isBalanced(TreeNode root)
```

## Target complexity

O(n) time, O(h) stack.

## The skill

The naive version calls `height()` at every node, and `height()` is itself O(n) — so
the whole thing is O(n²). On a degenerate chain that is genuinely slow, and the judge's
stress test will catch it.

The fix is a **sentinel return**: compute the height and the balanced-ness in one pass
by returning `-1` to mean "already unbalanced somewhere below".

```java
private int height(TreeNode n) {
    if (n == null) return 0;
    int l = height(n.left);
    if (l == -1) return -1;                       // bail early
    int r = height(n.right);
    if (r == -1) return -1;
    if (Math.abs(l - r) > 1) return -1;
    return 1 + Math.max(l, r);
}
public boolean isBalanced(TreeNode root) { return height(root) != -1; }
```

The general lesson: when a recursion needs to return *two* things — here a height and a
boolean — you have three options, in rough order of preference:

1. **Encode one in an impossible value of the other** (this drill: `-1`, since a real
   height is never negative). Cheapest when a spare value exists.
2. Return a small result object or `int[2]`. Always available, slightly more code.
3. Keep a field or an `int[1]` and mutate it. Works, but the shared mutable state makes
   it harder to reason about.

LC 124 binary-tree-max-path-sum is option 3 done deliberately; this drill is option 1.
Knowing all three, and why you picked one, is the interview-grade answer.

## Hints

<details><summary>Hint 1 — check the early bail actually helps</summary>

Returning `-1` as soon as a subtree is unbalanced avoids computing the other subtree at
all. Without those two early returns you still get O(n) but you do more work than
necessary. Both versions pass; the early one is what you want to write.

</details>
