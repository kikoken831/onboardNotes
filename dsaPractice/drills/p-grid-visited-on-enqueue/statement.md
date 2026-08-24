# Mark visited at enqueue, not dequeue

Tier 1 drill · grid-search · unlocks LC 200, 994, 1091

## Brief

Return the number of **enqueue operations** a correct BFS performs while flood-filling
from `(0,0)` over all `0` cells of the grid, counting the initial seed.

For a correct implementation this equals the number of reachable cells. That is the
whole point: if your answer is larger, you enqueued something twice.

## Constraints

- `1 <= rows, cols <= 60`

## Examples

```
[[0,0],[0,0]]        ->  4
[[0,1],[1,0]]        ->  1   (only the start is reachable)
[[0]]                ->  1
[[1]]                ->  0   (start is a wall)
[[0,0,0],[0,0,0]]    ->  6
```

## Signature

```java
public int countEnqueues(int[][] grid)
```

## Target complexity

O(rows · cols).

## The skill

This drill exists to make a specific bug **visible**, because in every other BFS problem
it hides.

Consider marking `seen` when a cell is *dequeued* rather than when it is *enqueued*:

```java
// WRONG
while (!q.isEmpty()) {
    int[] c = q.poll();
    if (seen[c[0]][c[1]]) continue;
    seen[c[0]][c[1]] = true;
    for (each open neighbour) q.add(neighbour);   // no seen check
}
```

This still produces the correct *set* of visited cells, and on LeetCode it usually still
passes. But a cell with four open neighbours gets enqueued up to four times before it
is ever processed. On an open grid the queue grows to O(cells) redundant entries, and
in the worst case the work becomes quadratic.

Because the visible output is identical, you can carry this bug for years. Here the
enqueue count is the answer, so the bug is a wrong number rather than a slow program.

**The rule: mark `seen` at the moment you add to the queue, and never add a cell that
is already marked.** Then every cell is enqueued exactly once, and the enqueue count
equals the reachable-cell count.

## Hints

<details><summary>Hint 1 — implement it correctly and just count</summary>

Write the correct BFS from the previous drill, increment a counter on every `q.add`,
and return it. If your number ever exceeds the number of reachable cells, the marking
is in the wrong place.

</details>

<details><summary>Hint 2 — see the bug for yourself</summary>

Write the dequeue-marking version too and compare on `[[0,0,0],[0,0,0]]`. Correct
gives 6. The buggy one gives more. That gap is the thing to remember.

</details>
