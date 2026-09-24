# Graph Valid Tree

LeetCode 261 · Medium · Premium · https://leetcode.com/problems/graph-valid-tree/

## Brief

*(Paraphrased — a Premium problem.)* `n` nodes labelled `0..n-1` and a list of undirected
edges (no duplicates, no self-loops). Is the graph a **tree** — connected and acyclic?

## Constraints

- `1 <= n <= 2000`, `0 <= edges.length <= 5000`

## Examples

```
5, [[0,1],[0,2],[0,3],[1,4]]         ->  true
5, [[0,1],[1,2],[2,3],[1,3],[1,4]]   ->  false    cycle 1-2-3
4, [[0,1],[2,3]]                     ->  false    disconnected
```

## Signature

```java
public boolean validTree(int n, int[][] edges)
```

## Target complexity

O(n + E · α(n)).

## The skill

**Ramp:** Tier 1 `p-dsu-optimised`. A tree on `n` nodes has **exactly `n − 1` edges**; with that
count, "no cycle" and "connected" are equivalent, so union-find only has to detect an edge
joining two nodes already in the same set. Checking only acyclicity misses the disconnected
case; checking only the edge count misses a cycle plus an isolated node.

## Hints

<details><summary>Hint 1</summary>

`edges.length != n - 1` → `false` before anything else.

</details>
