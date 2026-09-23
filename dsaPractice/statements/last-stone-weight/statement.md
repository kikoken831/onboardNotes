# Last Stone Weight

LeetCode 1046 · Easy · https://leetcode.com/problems/last-stone-weight/

## Brief

Repeatedly smash the two **heaviest** stones `y >= x`: if equal both vanish, otherwise one
stone of weight `y - x` remains. Return the weight of the last stone, or `0` if none.

## Constraints

- `1 <= stones.length <= 30`
- `1 <= stones[i] <= 1000`

## Examples

```
[2,7,4,1,8,1]  ->  1
[1]            ->  1
```

## Signature

```java
public int lastStoneWeight(int[] stones)
```

## Target complexity

O(n log n) with a max-heap.

## The skill

**Ramp:** Tier 1 `p-heap-comparator`. Java's `PriorityQueue` is a **min**-heap; you need
`Collections.reverseOrder()` (or `(a, b) -> b - a`, safe here because values are small).

Re-sorting the array every round also passes at `n = 30` — and is exactly what an
interviewer is checking you don't do.

## Hints

<details><summary>Hint 1</summary>

Poll twice, push the difference back if it's non-zero, loop while more than one remains.

</details>
