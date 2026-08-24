# Postorder without recursion

Tier 1 drill · iterative-traversal · unlocks LC 145, 590

## Brief

Return the postorder traversal (left, right, root) as a `List<Integer>`, **iteratively**
with an explicit stack.

## Constraints

- `0 <= node count <= 100`

## Examples

```
[1,null,2,3]  ->  [3,2,1]
[]            ->  []
[1]           ->  [1]
[1,2,3]       ->  [2,3,1]
[3,1,2]       ->  [1,2,3]
```

## Signature

```java
public List<Integer> postorderIterative(TreeNode root)
```

## Target complexity

O(n) time, O(h) space.

## The skill

The hardest of the three, and the one almost nobody can write cold. A node must be
visited only after **both** subtrees, so on popping a node you cannot tell whether you
are arriving for the first time or returning from its right subtree.

Two ways, and both are worth knowing:

**The reversal trick (easy, and what to reach for under pressure).** Do a preorder but
push left before right — giving root, right, left — then reverse the whole list. That
is exactly postorder.

```java
// modified preorder: root, right, left
while (!st.isEmpty()) {
    TreeNode n = st.pop();
    out.add(n.val);
    if (n.left  != null) st.push(n.left);    // note: LEFT first this time
    if (n.right != null) st.push(n.right);
}
Collections.reverse(out);
```

**The `lastVisited` marker (the real thing).** Peek rather than pop, and only visit when
the right child is absent or was the node you just visited:

```java
TreeNode cur = root, lastVisited = null;
while (cur != null || !st.isEmpty()) {
    while (cur != null) { st.push(cur); cur = cur.left; }
    TreeNode peek = st.peek();
    if (peek.right != null && lastVisited != peek.right) {
        cur = peek.right;                     // right subtree still to do
    } else {
        out.add(peek.val);
        lastVisited = st.pop();
    }
}
```

Write the reversal version first — it will work in two minutes. Then write the
`lastVisited` version, because it is the one that generalises to "do something on the
way down and something else on the way up", and because being asked for a
constant-extra-space postorder is a real interview question.

## Hints

<details><summary>Hint 1 — why reversal works</summary>

Postorder is left, right, root. Reverse that and you get root, right, left — which is a
preorder with the children swapped. So generate that, reverse, done. Being able to
state the reason is better than remembering the trick.

</details>

<details><summary>Hint 2 — the marker comparison</summary>

`lastVisited != peek.right` compares **references**, not values, and must stay that
way. Two distinct nodes can hold equal values, so `.equals` or `==` on `val` would
falsely conclude the right subtree was already done.

</details>
