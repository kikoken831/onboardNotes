# Collect root-to-leaf paths

Tier 1 drill · recursion-shape · unlocks LC 257, 113, 112, 129

## Brief

Return every root-to-leaf path as a `List<List<Integer>>`. A leaf is a node with no
children. An empty tree returns an empty list.

Paths must appear in **left-to-right order** — the leftmost leaf's path first.

## Constraints

- `0 <= node count <= 5000`

## Examples

```
[1,2,3]        ->  [[1,2],[1,3]]
[]             ->  []
[1]            ->  [[1]]
[1,2,null,3]   ->  [[1,2,3]]
[1,null,2,null,3] -> [[1,2,3]]
```

## Signature

```java
public List<List<Integer>> rootToLeafPaths(TreeNode root)
```

## Target complexity

O(n · h) time — every node is visited once, and each of the up-to-n paths costs O(h)
to copy.

## The skill

This is where **backtracking** starts, and it is only two lines more than the previous
drill.

```java
private void go(TreeNode n, List<Integer> path, List<List<Integer>> out) {
    if (n == null) return;
    path.add(n.val);                                  // choose
    if (n.left == null && n.right == null) {
        out.add(new ArrayList<>(path));               // COPY at the leaf
    } else {
        go(n.left, path, out);
        go(n.right, path, out);
    }
    path.remove(path.size() - 1);                     // un-choose
}
```

Two rules, and they are the two rules of all backtracking:

1. **Copy at the leaf.** `out.add(path)` adds a *reference* to the live list, which you
   are about to keep mutating — every entry in `out` ends up being the same object, and
   by the end it is empty. `new ArrayList<>(path)` is mandatory. This is the single most
   common backtracking bug and it produces a spectacularly confusing result.
2. **Un-choose on the way out.** Remove the last element after recursing, so a sibling
   branch sees the path as it was. Forget it and paths accumulate garbage from branches
   you already left.

The leaf test is `n.left == null && n.right == null`, not `n == null`. A node with one
child is not a leaf, and treating it as one gives you duplicate half-paths.

## Hints

<details><summary>Hint 1 — verify the copy bug by causing it</summary>

Write `out.add(path)` without the copy and run it on `[1,2,3]`. You will get
`[[],[]]`. Seeing that once makes the rule stick permanently.

</details>

<details><summary>Hint 2 — where else this shape goes</summary>

Replace "is it a leaf" with "does the running sum equal the target" and you have LC 113
path-sum-ii. Replace the tree with a decision over array indices and you have subsets
and combination-sum. Same three lines: choose, recurse, un-choose.

</details>
