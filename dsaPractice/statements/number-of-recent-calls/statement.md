# Number of Recent Calls

LeetCode 933 · Easy · https://leetcode.com/problems/number-of-recent-calls/

## Brief

`ping(t)` records a request at time `t` (milliseconds) and returns how many requests
happened in the **inclusive** window `[t - 3000, t]`, including this one. Every call uses a
strictly larger `t` than the previous.

Design problem: implement `class RecentCounter`.

## Constraints

- `1 <= t <= 10^9`, strictly increasing
- at most `10^4` calls

## Examples

```
["RecentCounter","ping","ping","ping","ping"]
[[],[1],[100],[3001],[3002]]
->  [null,1,2,3,3]
```

## Signature

```java
class RecentCounter {

    public RecentCounter() {

    }

    public int ping(int t) {

    }
}
```

## Target complexity

Amortised O(1) per call.

## The skill

A **sliding window over a stream**: because times only increase, anything that falls out
of the window never comes back — so a queue, evicting from the front, is enough. Each
timestamp is added once and removed once.

The window is **inclusive** (`t - 3000` counts). The judge pins both a ping at `1` then `3001`
(exactly 3000 ms apart: inside, count 2) and a ping at `1` then `3002` (3001 ms apart:
outside, count 1).

## Hints

<details><summary>Hint 1</summary>

Add `t`, then poll while the front is `< t - 3000`. Return the size.

</details>
