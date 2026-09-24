# Task Scheduler

LeetCode 621 · Medium · https://leetcode.com/problems/task-scheduler/

## Brief

Each task takes one unit; two copies of the same task must be at least `n` units apart, and the CPU may idle. Return the minimum total time to run every task.

## Constraints

- `1 <= tasks.length <= 10^4`, `0 <= n <= 100`

## Examples

```
[A,A,A,B,B,B], n=2  ->  8     A B idle A B idle A B
[A,A,A,B,B,B], n=0  ->  6
```

## Signature

```java
public int leastInterval(char[] tasks, int n)
```

## Target complexity

O(N).

## The skill

The most frequent task decides the shape: `max − 1` frames of width `n + 1`, plus a final partial frame holding every task tied for `max`. If there are enough other tasks to fill all the gaps, no idling happens and the answer is just the task count — hence the `max(tasks.length, …)`. Simulating with a heap is the other correct route.

## Hints

<details><summary>Hint 1</summary>

Count how many tasks share the maximum frequency.

</details>
