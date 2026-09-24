# Implement Queue using Stacks

LeetCode 232 · Easy · https://leetcode.com/problems/implement-queue-using-stacks/

## Brief

Implement a FIFO queue using **only two stacks** — only `push`-to-top, `peek`/`pop`-from-top,
`size` and `isEmpty` are allowed. `pop` and `peek` are always called on a non-empty queue.

This is a **design** problem: implement `class MyQueue`, not `class Solution`. The judge
replays LeetCode-style call sequences and compares every return value.

## Constraints

- `1 <= x <= 9`
- at most 100 calls
- every `pop` and `peek` is valid

## Examples

```
["MyQueue","push","push","peek","pop","empty"]
[[],[1],[2],[],[],[]]
->  [null,null,null,1,1,false]
```

## Signature

```java
class MyQueue {

    public MyQueue() {

    }

    public void push(int x) {

    }

    public int pop() {

    }

    public int peek() {

    }

    public boolean empty() {

    }
}
```

## Target complexity

Follow-up: **amortised** O(1) per operation.

## The skill

Two stacks: `in` takes every push; `out` serves pops. Move everything from `in` to `out`
**only when `out` is empty** — each element moves once, which is why it's amortised O(1).

The classic bug is transferring on every pop (or transferring back afterwards), which is
O(n) per call — and the other is transferring when `out` is *not* empty, which scrambles the
order. The judge's third example pushes after a transfer to catch exactly that.

## Hints

<details><summary>Hint 1</summary>

Once elements are in `out`, the top of `out` is the front of the queue. Leave them there.

</details>
