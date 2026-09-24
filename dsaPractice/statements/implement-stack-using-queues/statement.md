# Implement Stack using Queues

LeetCode 225 · Easy · https://leetcode.com/problems/implement-stack-using-queues/

## Brief

Implement a LIFO stack using **only queues** — only `add` to the back, `peek`/`poll` from the
front, `size` and `isEmpty`. `pop` and `top` are always called on a non-empty stack.

Design problem: implement `class MyStack`.

## Constraints

- `1 <= x <= 9`
- at most 100 calls, all valid

## Examples

```
["MyStack","push","push","top","pop","empty"]
[[],[1],[2],[],[],[]]
->  [null,null,null,2,2,false]
```

## Signature

```java
class MyStack {

    public MyStack() {

    }

    public void push(int x) {

    }

    public int pop() {

    }

    public int top() {

    }

    public boolean empty() {

    }
}
```

## Target complexity

One operation O(n), the others O(1). The follow-up asks for a **single** queue.

## The skill

A queue can't be reversed cheaply, so you pay on one side. The one-queue trick: after
adding `x`, rotate the queue `size - 1` times (poll from the front, add to the back), which
leaves `x` at the front — now `pop` and `top` are O(1).

Unlike 232 this is **not** amortised O(1): every push is O(n). Knowing that difference is the
real interview content of the pair.

## Hints

<details><summary>Hint 1</summary>

`q.add(x); for (int i = 1; i < q.size(); i++) q.add(q.poll());`

</details>
