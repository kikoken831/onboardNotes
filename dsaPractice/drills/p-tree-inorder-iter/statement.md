# Inorder without recursion (left spine)

Tier 1 drill · iterative-traversal · unlocks LC 94, 230, 98, 173

## Brief

Return the inorder traversal (left, root, right) as a `List<Integer>`, **iteratively**
with an explicit stack.

## Constraints

- `0 <= node count <= 100`

## Examples

```
[1,null,2,3]  ->  [1,3,2]
[]            ->  []
[1]           ->  [1]
[1,2,3]       ->  [2,1,3]
[3,1,2]       ->  [1,3,2]
```

## Signature

```java
public List<Integer> inorderIterative(TreeNode root)
```

## Target complexity

O(n) time, O(h) space.

## The skill

Genuinely harder than preorder, because you cannot visit a node when you first reach
it — its whole left subtree has to come first.

The **left spine** pattern: from the current node, push everything down the left edge.
When you can go no further left, pop and visit, then move to the popped node's right
child and repeat.

```java
Deque<TreeNode> st = new ArrayDeque<>();
TreeNode cur = root;
while (cur != null || !st.isEmpty()) {
    while (cur != null) { st.push(cur); cur = cur.left; }   // run down the left spine
    cur = st.pop();
    out.add(cur.val);
    cur = cur.right;
}
```

Two things to notice:

- **The loop condition needs both clauses.** `cur != null` covers "there is more to
  descend into"; `!st.isEmpty()` covers "there is more to come back up to". Drop either
  and the traversal stops early.
- **After visiting, you go right and then descend left again.** The inner while
  restarts on the new subtree. That interleaving is the whole algorithm.

Worth the effort because **inorder on a BST yields sorted order**, which is the entire
trick behind LC 98 validate-BST (check the sequence is increasing) and LC 230
kth-smallest (stop after k pops, without building the whole list).

## Hints

<details><summary>Hint 1 — trace it</summary>

Walk `[1,2,3]` — root 1, left 2, right 3 — and write down `cur` and the stack after
every line. Expect `[2,1,3]`. Six or seven iterations. Do it on paper.

</details>

<details><summary>Hint 2 — the payoff</summary>

Once this works, LC 230 is: run the same loop, decrement k on each pop, return when it
hits zero. You never build the list. That early exit is only possible in the iterative
form, which is the practical reason to know it.

</details>
