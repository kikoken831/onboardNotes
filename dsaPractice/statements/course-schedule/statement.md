# Course Schedule

LeetCode 207 · Medium · https://leetcode.com/problems/course-schedule/

## Brief

There are `numCourses` courses. `[a, b]` means you must take `b` before `a`. Can you finish them all?

## Constraints

- `1 <= numCourses <= 2000`, pairs unique

## Examples

```
2, [[1,0]]        ->  true
2, [[1,0],[0,1]]  ->  false    a cycle
```

## Signature

```java
public boolean canFinish(int numCourses, int[][] prerequisites)
```

## Target complexity

O(V + E).

## The skill

**Ramp:** Tier 1 `p-topo-kahn` and `p-topo-dfs-cycle`. It's "does this directed graph have a cycle?". **Kahn's algorithm**: repeatedly take courses with no remaining prerequisites; if you finish fewer than `n`, the rest are on a cycle. Or DFS with **three colours** — a back edge to a `gray` (in-progress) node is a cycle; a plain visited set can't tell a cycle from a diamond.

## Hints

<details><summary>Hint 1</summary>

Mind the direction: `[a, b]` is an edge `b → a`.

</details>
