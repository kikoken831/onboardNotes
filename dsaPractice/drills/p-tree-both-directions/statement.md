# Carry context down, return a result up

Tier 1 drill · recursion-shape · unlocks LC 98, 124, 437, 1372

## Brief

Return the number of **"good" nodes**: nodes whose value is greater than or equal to
every value on the path from the root to that node. The root is always good.

## Constraints

- `1 <= node count <= 10^5`
- `-10^4 <= node values <= 10^4`

## Examples

```
[3,1,4,3,null,1,5]  ->  4    (3, 3, 4, 5)
[3,3,null,4,2]      ->  3    (3, 3, 4)
[1]                 ->  1
[2,null,1]          ->  1    (only the root; 1 < 2)
```

## Signature

```java
public int countGoodNodes(TreeNode root)
```

## Target complexity

O(n) time, O(h) stack.

## The skill

**Recursion shape 3 of 3: both directions at once.** The max-so-far travels *down* as
context; the count travels *up* as a result. Neither alone is enough, and recognising
that you need both is the actual skill.

```java
private int go(TreeNode n, int maxSoFar) {
    if (n == null) return 0;
    int self = n.val >= maxSoFar ? 1 : 0;
    int next = Math.max(maxSoFar, n.val);
    return self + go(n.left, next) + go(n.right, next);
}
```

Seed it with `go(root, Integer.MIN_VALUE)` so the root always counts.

Once you can see problems in these terms the tree section stops being a collection of
tricks:

- **validate-BST (98)** — thread the `(low, high)` range down, return a boolean up. The
  reason the naive "compare with the parent" version fails is precisely that it uses
  shape 1 where shape 3 is needed.
- **binary-tree-max-path-sum (124)** — return "best downward path from here" up, while
  updating a global best. A Hard problem that is mostly just knowing this shape.
- **path-sum-iii (437)** — thread the prefix-sum map down, count up.

Before writing any tree recursion, answer two questions: *what do I need from above?*
and *what do I return upward?* If both answers are non-empty, it is shape 3.

## Hints

<details><summary>Hint 1 — the seed value</summary>

`Integer.MIN_VALUE` makes the root's `>=` test always true. Do not special-case the
root; pick a seed that makes the general rule handle it.

</details>

<details><summary>Hint 2 — why `>=` and not `>`</summary>

`[3,3,null,4,2]` gives 3, not 2 — a node equal to the running max still counts. The
pinned examples cover this because it is exactly the kind of detail that is easy to
read past in a statement.

</details>
