# Preorder without recursion

Tier 1 drill · iterative-traversal · unlocks LC 144, 589

## Brief

Return the preorder traversal (root, left, right) as a `List<Integer>`, **iteratively**
using an explicit stack. No recursion.

## Constraints

- `0 <= node count <= 100`

## Examples

```
[1,null,2,3]  ->  [1,2,3]
[]            ->  []
[1]           ->  [1]
[1,2,3]       ->  [1,2,3]
[3,1,2]       ->  [3,1,2]
```

## Signature

```java
public List<Integer> preorderIterative(TreeNode root)
```

## Target complexity

O(n) time, O(h) space.

## The skill

The easiest of the three iterative traversals, because preorder visits a node the
moment it is popped — no bookkeeping.

```java
Deque<TreeNode> st = new ArrayDeque<>();
if (root != null) st.push(root);
while (!st.isEmpty()) {
    TreeNode n = st.pop();
    out.add(n.val);
    if (n.right != null) st.push(n.right);   // RIGHT first
    if (n.left  != null) st.push(n.left);    // so LEFT pops first
}
```

**Push right before left.** A stack reverses, so the last pushed is the first visited,
and you want left visited first. Getting this backwards produces a mirror-image
traversal that looks plausible until you check it.

**A Java trap that bites here specifically:** use `push`/`pop`, never `add`/`pop`, on an
`ArrayDeque`. `add` appends to the tail while `pop` removes from the head, so mixing
them gives you a **queue** — and a queue here produces a level-order traversal that
passes `[1,2,3]` and fails on anything deeper. This exact mistake is documented as
correct in a widely-circulated GitHub cheatsheet; see the Tier 0 sheet, trap 1.

Also guard the null root before pushing: `ArrayDeque` throws `NullPointerException` on
`push(null)` rather than accepting it.

## Hints

<details><summary>Hint 1 — check for the queue bug</summary>

If your output for `[1,2,null,3]` is `[1,2,3]` you are fine; if a deeper tree comes out
level by level, you used `add` instead of `push`.

</details>
