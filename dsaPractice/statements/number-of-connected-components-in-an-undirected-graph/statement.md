# Number of Connected Components in an Undirected Graph

LeetCode 323 · Medium · Premium · https://leetcode.com/problems/number-of-connected-components-in-an-undirected-graph/

## Brief

*(Paraphrased — a Premium problem.)* `n` nodes and a list of undirected edges. Return the number
of connected components. Isolated nodes are components of their own.

## Constraints

- `1 <= n <= 2000`, no duplicate edges

## Examples

```
5, [[0,1],[1,2],[3,4]]  ->  2
3, []                   ->  3
```

## Signature

```java
public int countComponents(int n, int[][] edges)
```

## Target complexity

O(n + E · α(n)).

## The skill

**Ramp:** Tier 1 `p-dsu-count-components`. Start from `n` components and **decrement** on every
union that actually merges two different sets. Counting distinct roots at the end works too. A
DFS from every unvisited node is the other standard answer.

## Hints

<details><summary>Hint 1</summary>

Only a union of two *different* roots reduces the count.

</details>
