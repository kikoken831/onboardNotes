# Minimum cost to climb

Tier 1 drill · dynamic-programming · unlocks LC 746, 70, 198, 213

## Brief

`cost[i]` is the price of stepping off stair `i`. From a stair you may climb one or two
steps. You may start at index 0 or index 1. Return the minimum total cost to get past
the top — i.e. to reach index `cost.length`.

## Constraints

- `2 <= cost.length <= 1000`
- `0 <= cost[i] <= 999`

## Examples

```
[10,15,20]              ->  15   (start at 1, pay 15, step two)
[1,100,1,1,1,100,1,1,100,1]  ->  6
[0,0]                   ->  0
[5,5]                   ->  5
[1,2]                   ->  1
```

## Signature

```java
public int minCostClimb(int[] cost)
```

## Target complexity

O(n) time, O(1) space.

## The skill

The first DP where **you have to choose the state yourself**, which is the actual
difficulty of dynamic programming — the recurrence is easy once the state is right.

Define `f(i)` = the minimum cost to *reach* stair `i`. Then:

```
f(i) = min( f(i-1) + cost[i-1],  f(i-2) + cost[i-2] )
f(0) = f(1) = 0        // both starts are free
answer = f(n)
```

Note what `f(i)` means: the cost to **arrive at** `i`, not including stepping off it.
The alternative definition — "minimum cost to step off `i`" — also works but produces a
different, easier-to-botch recurrence and a different answer extraction. **Pin down what
your state means in one sentence before writing anything.** Most wrong DP is a
well-implemented recurrence over a badly defined state.

Two details:

- **`f(0) = f(1) = 0`.** Starting at index 1 is free, so reaching stair 1 costs nothing.
  Setting `f(1) = cost[0]` is the standard slip and it fails `[10,15,20]`, which is
  pinned first for that reason.
- **The answer is `f(n)`, not `f(n-1)`.** You must get *past* the top. Off-by-one here
  gives a plausible smaller number.

Then collapse to two rolling variables, exactly as in the Tribonacci drill.

## Hints

<details><summary>Hint 1 — check against the second example</summary>

`[1,100,1,1,1,100,1,1,100,1]` should give 6. If you get 7 your base case is probably
`f(1) = cost[0]`; if you get 5 you likely stopped at `f(n-1)`.

</details>

<details><summary>Hint 2 — the connection to house robber</summary>

LC 198 house-robber is the same shape with `max` instead of `min` and a "cannot take
adjacent" constraint rather than a step limit. Once this feels routine, that one is a
ten-minute problem.

</details>
