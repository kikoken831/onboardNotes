# Path compression and union by size

Tier 1 drill · union-find · unlocks LC 684, 721, 128, 990

## Brief

Same contract as the previous drill — apply `unions`, answer `queries` — but now the
implementation must use **both** optimisations: path compression in `find`, and union by
size (or rank) in `union`.

The judge checks the answers, which are identical either way. The optimisations are on
your honour, but the timing constraint is real: `n` reaches 20,000 in the growth curve,
where the naive version is noticeably slower.

## Constraints

- `1 <= n <= 20000`
- `0 <= unions.length, queries.length <= 20000`

## Examples

```
n=5, unions=[[0,1],[1,2]], queries=[[0,2],[0,3]]  ->  [true,false]
n=1, unions=[],            queries=[[0,0]]        ->  [true]
n=4, unions=[[0,1],[2,3],[1,3]], queries=[[0,2]]  ->  [true]
```

## Signature

```java
public boolean[] dsuOptimised(int n, int[][] unions, int[][] queries)
```

## Target complexity

Effectively O(α(n)) per operation — inverse Ackermann, which is at most 4 for any
plausible input. Treat it as constant, but know it is not literally constant.

## The skill

Two independent improvements that compose:

**Path compression** — on the way back from `find`, point every node directly at the
root, so the next `find` is O(1):

```java
private int find(int[] p, int x) {
    while (p[x] != x) {
        p[x] = p[p[x]];      // path halving: point at grandparent
        x = p[x];
    }
    return x;
}
```

Path halving is the iterative form and is what to write — it is one extra line, avoids
recursion entirely (no stack-depth risk on a 20,000-element chain), and is
asymptotically as good as full compression.

**Union by size** — always attach the smaller tree under the larger root, so the depth
cannot grow linearly:

```java
if (size[ra] < size[rb]) { int t = ra; ra = rb; rb = t; }
parent[rb] = ra;
size[ra] += size[rb];
```

Either optimisation alone gives O(log n). **Together** they give the near-constant
bound. Being able to say that — and to say that the bound is amortised, not worst-case
per operation — is the interview answer.

Note the `size` array must be updated only for the surviving root; touching the other
one is harmless but reveals you have not thought about which is which.

## Hints

<details><summary>Hint 1 — check compression actually happened</summary>

After `find(x)`, `parent[x]` should be at or very near the root. Chain 0→1→…→99 by
unioning in order, call `find(0)`, then print the parent array and look at how much
flattened.

</details>

<details><summary>Hint 2 — union by size vs by rank</summary>

Size counts nodes; rank approximates depth. Both work, both give the same bound. Size
is easier to reason about and gives you component sizes for free, which several
problems want. Pick one and be consistent.

</details>
