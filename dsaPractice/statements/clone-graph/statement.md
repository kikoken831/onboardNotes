# Clone Graph

LeetCode 133 · Medium · https://leetcode.com/problems/clone-graph/

## Brief

Given a reference to one node of a connected undirected graph, return a **deep copy** of the
whole graph. Nodes have distinct values `1..n`. LeetCode's `Node` class is provided — don't
declare it:

```java
class Node {
    public int val;
    public List<Node> neighbors;
}
```

Test cases are written as adjacency lists (`adjList[i]` = neighbours of node `i + 1`); you are
handed node 1. The judge checks the copy's structure **and** that it shares no node with the
original.

## Constraints

- `0 <= n <= 100`, connected, no repeated edges or self-loops

## Examples

```
[[2,4],[1,3],[2,4],[1,3]]  ->  [[2,4],[1,3],[2,4],[1,3]]
[[]]                       ->  [[]]      one node, no neighbours
[]                         ->  []        empty graph: node is null
```

## Signature

```java
public Node cloneGraph(Node node)
```

## Target complexity

O(V + E).

## The skill

Graph traversal with an **original → copy** map. The map does two jobs: it's the visited set
(so cycles terminate) and it lets a neighbour you've seen before be linked to its *existing*
copy rather than copied again. Create each copy when you first **discover** a node, before you
wire its edges.

## Hints

<details><summary>Hint 1</summary>

`Map<Node, Node>` from original to clone, filled by BFS or DFS.

</details>
