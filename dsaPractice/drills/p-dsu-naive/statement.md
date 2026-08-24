# Find and union with no optimisation

Tier 1 drill · union-find · unlocks LC 547, 323, 261

## Brief

You have `n` elements labelled `0..n-1`, each initially in its own set. Apply the given
`unions` in order, then answer the `queries`: for each `{a, b}`, is `a` in the same set
as `b`?

Return a `boolean[]`, one per query.

Write the **naive** version: a parent array, `find` walking to the root, `union`
pointing one root at the other. No path compression, no union by rank — those come next.

## Constraints

- `1 <= n <= 200`
- `0 <= unions.length, queries.length <= 500`

## Examples

```
n=5, unions=[[0,1],[1,2]], queries=[[0,2],[0,3]]  ->  [true,false]
n=1, unions=[],            queries=[[0,0]]        ->  [true]
n=3, unions=[[0,1],[0,1]], queries=[[1,0]]        ->  [true]   (repeat union is a no-op)
n=2, unions=[],            queries=[[0,1]]        ->  [false]
```

## Signature

```java
public boolean[] dsuNaive(int n, int[][] unions, int[][] queries)
```

## Target complexity

O(n) per operation in the worst case — deliberately, since this version is unoptimised.

## The skill

Disjoint-set union in its simplest honest form:

```java
int[] parent = new int[n];
for (int i = 0; i < n; i++) parent[i] = i;      // each its own root

private int find(int[] p, int x) {
    while (p[x] != x) x = p[x];                 // walk to the root
    return x;
}
private void union(int[] p, int a, int b) {
    int ra = find(p, a), rb = find(p, b);
    if (ra != rb) p[ra] = rb;                   // link ROOT to ROOT
}
```

**Union links roots, never the elements themselves.** `parent[a] = b` is the classic
mistake: it works when `a` and `b` happen to be roots and silently corrupts the
structure when they are not, detaching whole subtrees. Always `find` both first.

"Same set" is then just `find(a) == find(b)`.

Union-find has no LeetCode Easy problems either. It is also the pattern your target
list touches only as a secondary tag — LC 128, 200, 261, 323 all *can* be done with
DSU — so there is no natural place to learn it. Hence three drills.

## Hints

<details><summary>Hint 1 — why this version is slow</summary>

Union in a bad order and the tree becomes a chain of length n, making `find` O(n).
Chain 0→1→2→…→n-1 and every query walks the whole thing. The next drill fixes exactly
this, so it is worth building the slow one first and seeing the shape of the problem.

</details>

<details><summary>Hint 2 — self-union and repeats</summary>

`union(a, a)` and a repeated `union(a, b)` must both be harmless no-ops. The
`if (ra != rb)` guard handles both. Both are pinned.

</details>
