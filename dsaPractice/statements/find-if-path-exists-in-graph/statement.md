# Find if Path Exists in Graph

LeetCode 1971 · Easy · https://leetcode.com/problems/find-if-path-exists-in-graph/

## Brief

An **undirected** graph has `n` vertices `0..n-1` and the given edges (no self-loops, no
duplicates). Return whether there is a path from `source` to `destination`.

## Constraints

- `1 <= n <= 2 * 10^5`
- `0 <= edges.length <= 2 * 10^5`
- `source` may equal `destination`

## Examples

```
n=3, [[0,1],[1,2],[2,0]], 0 -> 2                ->  true
n=6, [[0,1],[0,2],[3,5],[5,4],[4,3]], 0 -> 5    ->  false
```

## Signature

```java
public boolean validPath(int n, int[][] edges, int source, int destination)
```

## Target complexity

O(n + E).

## The skill

The first real graph problem: **build an adjacency list**, then BFS/DFS from `source`. Or
union-find: union every edge, then ask whether `source` and `destination` share a root
(**ramp:** Tier 1 `p-dsu-optimised`).

Two traps the judge covers: edges are **undirected** (add both directions), and
`source == destination` is `true` even with no edges.

## Hints

<details><summary>Hint 1</summary>

At `n = 2 × 10^5`, recursive DFS can overflow the stack on a long chain. BFS with a queue
doesn't. The judge's inputs are much smaller (n ≤ 30 in stress, at most 20 000 random-graph nodes
in time mode, never a long chain), so passing here doesn't guarantee a recursive DFS
survives LeetCode's largest tests.

</details>
