# Gas Station

LeetCode 134 · Medium · https://leetcode.com/problems/gas-station/

## Brief

Station `i` has `gas[i]` fuel; driving to station `i + 1` costs `cost[i]`. Starting with an empty tank, return the station from which you can drive the whole circle clockwise, or `-1`. If a start exists it is unique.

## Constraints

- `1 <= n <= 10^5`

## Examples

```
gas=[1,2,3,4,5], cost=[3,4,5,1,2]  ->  3
gas=[2,3,4],     cost=[3,4,3]      ->  -1
```

## Signature

```java
public int canCompleteCircuit(int[] gas, int[] cost)
```

## Target complexity

O(n).

## The skill

Two facts make it one pass. If total gas ≥ total cost, a start **exists**. And if you run dry at station `i` having started at `s`, **no station between `s` and `i`** works either — each of them starts with less fuel than you had passing through. So restart at `i + 1`. Trying every start is O(n²).

## Hints

<details><summary>Hint 1</summary>

Track the running tank and the grand total separately.

</details>
