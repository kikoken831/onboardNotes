# Kahn's algorithm with in-degrees

Tier 1 drill · topological-sort · unlocks LC 207, 210, 269, 310

## Brief

Given `n` nodes labelled `0..n-1` and a list of directed `edges` where `[a,b]` means
"a must come before b", return **any** valid topological order as an `int[]`. If the
graph has a cycle, return an empty array.

Any valid order is accepted — the judge validates the ordering property rather than
comparing against one specific answer.

## Constraints

- `1 <= n <= 2000`
- `0 <= edges.length <= 5000`

## Examples

```
n=4, edges=[[0,1],[0,2],[1,3],[2,3]]  ->  [0,1,2,3] or [0,2,1,3]
n=2, edges=[[0,1],[1,0]]              ->  []          (cycle)
n=1, edges=[]                         ->  [0]
n=3, edges=[]                         ->  any order of 0,1,2
```

## Signature

```java
public int[] topoKahn(int n, int[][] edges)
```

## Target complexity

O(n + e).

## The skill

Kahn's algorithm is BFS over a dependency graph:

1. Count each node's **in-degree** — how many edges point at it.
2. Queue every node with in-degree 0; they have no unmet prerequisites.
3. Pop one, append it to the output, and decrement the in-degree of each node it points
   at. Any that reach 0 are now ready — enqueue them.
4. Repeat.

**Cycle detection is free, and this is the elegant part.** If the output contains fewer
than `n` nodes when the queue empties, the leftovers are in a cycle: each is waiting on
another, so none ever reaches in-degree 0. You do not write a separate cycle check —
you compare a count.

```java
if (order.size() < n) return new int[0];
```

That is why LC 207 course-schedule ("is it possible?") and LC 210 course-schedule-ii
("give me an order") are the same algorithm with a different return statement.

**Edge direction is the thing to get right.** `[a,b]` meaning "a before b" means the
edge goes a→b, so `b`'s in-degree increases. LeetCode's course-schedule states its
pairs as `[course, prerequisite]`, which is the *opposite* convention. Read the
statement rather than reaching for a template; the reversed version compiles, runs, and
gives a plausible wrong answer.

Topological sort has no LeetCode Easy problems.

## Hints

<details><summary>Hint 1 — self-loop</summary>

An edge `[a,a]` gives `a` an in-degree of 1 that nothing can ever decrement, so it is a
cycle of length one and the answer is empty. The count check handles it with no special
case.

</details>

<details><summary>Hint 2 — which container</summary>

An `ArrayDeque` used as a queue (`add`/`poll`) is standard. A `PriorityQueue` gives the
lexicographically smallest valid order, which some problems ask for. Both are Kahn's;
only the tie-break differs.

</details>
