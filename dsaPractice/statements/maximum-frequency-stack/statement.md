# Maximum Frequency Stack

LeetCode 895 · Hard · https://leetcode.com/problems/maximum-frequency-stack/

## Brief

Implement `class FreqStack`: `push(val)`, and `pop()` which removes and returns the **most
frequent** value, breaking ties by the one pushed **most recently**. `pop` is only called on a
non-empty stack.

## Constraints

- `0 <= val <= 10^9`; at most `2 * 10^4` calls

## Examples

```
["FreqStack","push","push","push","push","push","push","pop","pop","pop","pop"]
[[],[5],[7],[5],[7],[4],[5],[],[],[],[]]
->  [null,null,null,null,null,null,null,5,7,5,4]
```

## Signature

```java
class FreqStack {

    public FreqStack() {

    }

    public void push(int val) {

    }

    public int pop() {

    }
}
```

## Target complexity

O(1) per operation.

## The skill

Keep **one stack per frequency level**: when a value's count rises to `f`, push it onto stack
`f`. The answer is always the top of the highest non-empty stack, and a value with count 3 sits
in stacks 1, 2 *and* 3 — so popping it from stack 3 leaves it correctly represented at 2. A
priority queue keyed by (frequency, push time) is O(log n) and also accepted.

## Hints

<details><summary>Hint 1</summary>

Track `maxFreq`; decrement it when its stack empties.

</details>
