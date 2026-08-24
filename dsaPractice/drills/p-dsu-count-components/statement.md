# Count connected components

Tier 1 drill · union-find · unlocks LC 323, 547, 261, 200

## Brief

Given `n` nodes labelled `0..n-1` and an undirected `edges` list, return the number of
connected components.

## Constraints

- `1 <= n <= 20000`
- `0 <= edges.length <= 20000`

## Examples

```
n=5, edges=[[0,1],[1,2],[3,4]]  ->  2
n=5, edges=[]                   ->  5
n=1, edges=[]                   ->  1
n=3, edges=[[0,1],[1,2],[0,2]]  ->  1   (a cycle is still one component)
n=2, edges=[[0,0]]              ->  2   (a self-loop joins nothing)
```

## Signature

```java
public int countComponents(int n, int[][] edges)
```

## Target complexity

O((n + e) · α(n)).

## The skill

The pay-off, and it is a two-line idea: **start the count at `n`, and decrement every
time a union actually merges two different sets.**

```java
int components = n;
for (int[] e : edges) {
    int ra = find(e[0]), rb = find(e[1]);
    if (ra == rb) continue;          // already connected: changes nothing
    unionRoots(ra, rb);
    components--;
}
return components;
```

No second pass counting distinct roots, no set of roots. The `ra == rb` check is doing
double duty — it is both the correctness guard from the first drill and the thing that
makes the count right in the presence of cycles and duplicate edges. `[[0,0]]` and a
triangle both exercise it.

Worth knowing the alternative: DFS or BFS from every unvisited node, counting how many
times you have to start. Same O(n + e), no DSU needed, and for a *static* graph it is
simpler. **DSU wins when edges arrive incrementally** and you must answer connectivity
questions as you go — which is why LC 261 graph-valid-tree and LC 684
redundant-connection are DSU problems while LC 200 number-of-islands is usually DFS.

Being able to say which tool fits which situation is the thing being tested here, not
the twenty lines of code.

## Hints

<details><summary>Hint 1 — graph-valid-tree falls straight out</summary>

A graph is a tree exactly when it has `n - 1` edges and one component — or
equivalently, when no union is ever a no-op and the count reaches 1. That is LC 261 in
two extra lines. Try it once this works.

</details>
