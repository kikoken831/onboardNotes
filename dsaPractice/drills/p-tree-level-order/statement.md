# Level order with a size snapshot

Tier 1 drill · iterative-traversal · unlocks LC 102, 103, 107, 199, 637, 662

## Brief

Return the level-order traversal as a `List<List<Integer>>` — one inner list per level,
top to bottom, left to right within a level.

## Constraints

- `0 <= node count <= 2000`

## Examples

```
[3,9,20,null,null,15,7]  ->  [[3],[9,20],[15,7]]
[]                       ->  []
[1]                      ->  [[1]]
[1,2,3,4]                ->  [[1],[2,3],[4]]
```

## Signature

```java
public List<List<Integer>> levelOrder(TreeNode root)
```

## Target complexity

O(n) time, O(w) space where w is the maximum level width.

## The skill

BFS with a queue is easy. Knowing **where one level ends** is the drill.

```java
Queue<TreeNode> q = new ArrayDeque<>();
if (root != null) q.add(root);
while (!q.isEmpty()) {
    int size = q.size();              // SNAPSHOT before the inner loop
    List<Integer> level = new ArrayList<>();
    for (int i = 0; i < size; i++) {  // exactly this many, no more
        TreeNode n = q.poll();
        level.add(n.val);
        if (n.left  != null) q.add(n.left);
        if (n.right != null) q.add(n.right);
    }
    out.add(level);
}
```

**`int size = q.size()` before the inner loop is the entire trick.** You are adding to
the queue while draining it, so `q.size()` is a moving target — writing
`for (int i = 0; i < q.size(); i++)` reads a value that changes underneath you and
scrambles the level boundaries. Snapshot it.

Note this is a **queue**, so use `add`/`poll` (or `offer`/`poll`) — the OPP family.
Mixing in `push` here would give you a stack and a depth-first order. The same
`ArrayDeque` serves as both; the method names are what decide which.

Once you have this, a whole family falls out with one-line changes: reverse the outer
list for LC 107, alternate the inner order for zigzag (103), take the last element of
each level for right-side-view (199), average each level for 637.

## Hints

<details><summary>Hint 1 — check the boundaries</summary>

For `[1,2,3,4]` you should get `[[1],[2,3],[4]]`. If you get `[[1,2,3,4]]` or
`[[1],[2],[3],[4]]`, the snapshot is wrong or missing.

</details>
