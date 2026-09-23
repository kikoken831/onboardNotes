# Baseball Game

LeetCode 682 · Easy · https://leetcode.com/problems/baseball-game/

## Brief

Process a list of operations on a record of scores, then return the sum of the record:

- an integer `x` — record a new score `x`
- `"+"` — record the sum of the previous two scores
- `"D"` — record double the previous score
- `"C"` — invalidate (remove) the previous score

Every operation is guaranteed valid (there are always enough previous scores).

## Constraints

- `1 <= operations.length <= 1000`
- integers in `[-3 * 10^4, 3 * 10^4]`

## Examples

```
["5","2","C","D","+"]              ->  30    record: 5, 10, 15
["5","-2","4","C","D","9","+","+"] ->  27    record: 5, -2, -4, 9, 5, 14
["1","C"]                          ->  0
```

## Signature

```java
public int calPoints(String[] operations)
```

## Target complexity

O(n).

## The skill

A **stack as an editable history**: `C` is an undo, and `+`/`D` read the most recent
entries. The subtle one is `+`: it reads the top **two** without removing them — pop,
peek, push both back.

## Hints

<details><summary>Hint 1</summary>

`"+"` looks like a number to a careless `parseInt` check — match the three symbols first.

</details>
