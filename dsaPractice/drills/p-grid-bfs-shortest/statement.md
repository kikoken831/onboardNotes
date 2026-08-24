# Shortest path on an unweighted grid

Tier 1 drill · grid-search · unlocks LC 1091, 994, 542, 286

## Brief

Given a grid where `0` is open and `1` is a wall, return the length of the shortest path
from `(0,0)` to the bottom-right cell, counting **cells visited** including both ends.
Movement is 4-directional. Return `-1` if no path exists, or if either endpoint is a
wall.

## Constraints

- `1 <= rows, cols <= 100`

## Examples

```
[[0,0],[1,0]]                ->  3
[[0,1],[1,0]]                -> -1
[[0]]                        ->  1
[[1]]                        -> -1
[[0,0,0],[1,1,0],[0,0,0]]    ->  5
```

## Signature

```java
public int shortestPath(int[][] grid)
```

## Target complexity

O(rows · cols) time and space.

## The skill

**BFS, not DFS.** On an unweighted graph BFS visits cells in order of distance, so the
first time you reach the target you are guaranteed to have reached it by a shortest
path. DFS gives you *a* path, not the shortest, and fixing that requires exploring
everything.

Distance is tracked by processing one level at a time — the same size-snapshot idea as
level-order tree traversal:

```java
int dist = 1;
while (!q.isEmpty()) {
    int size = q.size();
    for (int i = 0; i < size; i++) {
        int[] cell = q.poll();
        if (cell is target) return dist;
        for (each neighbour) { if (open && !visited) { mark; enqueue; } }
    }
    dist++;
}
return -1;
```

**Mark visited at enqueue time, not at dequeue time.** This is the point of the drill
and it gets its own follow-up. Marking on dequeue lets the same cell be enqueued
several times before it is first processed, which blows the queue up and can make the
whole thing quadratic.

Check the start cell is open before you begin — `[[1]]` must return `-1`, and a BFS
that enqueues a wall as its seed will happily report 1.

## Hints

<details><summary>Hint 1 — the 1x1 case</summary>

`[[0]]` is both start and target, so the answer is 1, not 0. Test the target *before*
expanding neighbours and this falls out; test it after and you return 2 or -1.

</details>

<details><summary>Hint 2 — representing a cell</summary>

`int[]{r,c}` in the queue is simplest. Alternatively encode as `r * cols + c` into a
single int, which avoids allocation and is what you would reach for if the grid were
huge. Note `int[]` cannot be used as a `HashSet` key — see Tier 0, trap 10.

</details>
