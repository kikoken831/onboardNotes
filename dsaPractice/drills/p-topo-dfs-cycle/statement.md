# DFS post-order topo sort with cycle detection

Tier 1 drill · topological-sort · unlocks LC 207, 210, 269, 802

## Brief

Same contract as the Kahn drill — return any valid topological order, or an empty array
if there is a cycle — but implement it with **DFS and three-colour marking** instead.

## Constraints

- `1 <= n <= 2000`
- `0 <= edges.length <= 5000`

## Examples

```
n=4, edges=[[0,1],[0,2],[1,3],[2,3]]  ->  any valid order
n=2, edges=[[0,1],[1,0]]              ->  []
n=1, edges=[]                         ->  [0]
n=2, edges=[[0,0]]                    ->  []
```

## Signature

```java
public int[] topoDfs(int n, int[][] edges)
```

## Target complexity

O(n + e).

## The skill

The DFS formulation, and the reason it is worth knowing alongside Kahn's:

**A node's position in the output is decided when you finish it, not when you reach
it.** Append each node to a list *after* recursing into all its successors, then reverse
the list at the end. Everything a node depends on is already placed behind it.

**Three colours, not a boolean.** This is the part people get wrong:

- **white** (0) — not visited
- **grey** (1) — on the current recursion stack
- **black** (2) — finished, all descendants explored

```java
private boolean dfs(int v) {
    colour[v] = 1;                              // grey: now on the stack
    for (int w : adj.get(v)) {
        if (colour[w] == 1) return false;       // back edge -> CYCLE
        if (colour[w] == 0 && !dfs(w)) return false;
    }
    colour[v] = 2;                              // black: done
    order.add(v);                               // post-order
    return true;
}
```

A plain `visited` boolean **cannot** detect a cycle, because it conflates "on the
current path" with "already finished". Reaching a black node is completely normal — it
is a shortcut through a diamond — while reaching a grey node means you have looped back
onto your own path. Collapsing those two states is the classic bug and it reports
cycles that are not there.

Kahn's versus DFS: Kahn's is iterative and gives you the cycle check as a count
comparison; DFS is shorter but risks stack overflow on a long chain (measured: about
45,000 trivial frames, far fewer with real frames). For a 10⁵-node graph, prefer Kahn's.
Knowing that trade-off is the interview-grade answer.

## Hints

<details><summary>Hint 1 — remember to reverse</summary>

Post-order gives you dependencies first, which is the *reverse* of a topological order.
Either reverse at the end or push onto a stack. Forgetting is a wrong answer that looks
structured and plausible.

</details>

<details><summary>Hint 2 — start from every node</summary>

The graph may be disconnected, so loop over all `n` nodes and start a DFS from each
white one. Starting only from in-degree-0 nodes is Kahn's thinking and misses nodes in
a component that has no source.

</details>
