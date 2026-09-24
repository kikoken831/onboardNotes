# Course Schedule II

LeetCode 210 · Medium · https://leetcode.com/problems/course-schedule-ii/

## Brief

As LC 207, but return **an order** in which to take all the courses, or `[]` if impossible. Any valid order is accepted.

## Constraints

- `1 <= numCourses <= 2000`

## Examples

```
2, [[1,0]]                    ->  [0,1]
4, [[1,0],[2,0],[3,1],[3,2]]  ->  [0,1,2,3] or [0,2,1,3]
2, [[0,1],[1,0]]              ->  []
```

## Signature

```java
public int[] findOrder(int numCourses, int[][] prerequisites)
```

## Target complexity

O(V + E).

## The skill

Kahn's algorithm again, now recording the order nodes come off the queue. If fewer than `n` were emitted there's a cycle, and the partial order must **not** be returned. The DFS version emits in post-order and reverses. The judge checks the property — a permutation respecting every edge — not one order.

## Hints

<details><summary>Hint 1</summary>

Courses with no prerequisites at all still belong in the output (`3, []`).

</details>
