# Kids With the Greatest Number of Candies

LeetCode 1431 · Easy · https://leetcode.com/problems/kids-with-the-greatest-number-of-candies/

## Brief

For each kid, report whether giving them all `extraCandies` would leave them with **at
least as many** candies as the current maximum. Ties count as greatest.

## Constraints

- `2 <= candies.length <= 100`
- `1 <= candies[i] <= 100`
- `1 <= extraCandies <= 50`

## Examples

```
[2,3,5,1,3], extra=3  ->  [true,true,true,false,true]
[4,2,1,1,2], extra=1  ->  [true,false,false,false,false]
[12,1,12],  extra=10  ->  [true,false,true]
```

## Signature

```java
public List<Boolean> kidsWithCandies(int[] candies, int extraCandies)
```

## Target complexity

O(n) — two passes.

## The skill

Precompute the aggregate once, then answer per element. The trap is recomputing the max
inside the loop (O(n²)), or using `>` where the statement says "greatest" includes ties.

## Hints

<details><summary>Hint 1</summary>

The maximum doesn't change as you ask about each kid — the extra candies are
hypothetical. Find it first.

</details>
