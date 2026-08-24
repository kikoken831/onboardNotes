# Binary search the answer, not the array

Tier 1 drill · binary-search · unlocks LC 875, 1011, 410, 1482

## Brief

`weights` is a list of package weights in the order they must ship. You have `days`
days. Each day you ship a contiguous prefix of the remaining packages, without exceeding
the ship's capacity.

Return the **minimum capacity** that lets you ship everything within `days` days.

## Constraints

- `1 <= weights.length <= 5 * 10^4`
- `1 <= weights[i] <= 500`
- `1 <= days <= weights.length`

## Examples

```
[1,2,3,4,5,6,7,8,9,10], days = 5   ->  15
[3,2,2,4,1,4],          days = 3   ->  6
[1,2,3,1,1],            days = 4   ->  3
[5],                    days = 1   ->  5
```

## Signature

```java
public int bsOnAnswer(int[] weights, int days)
```

## Target complexity

O(n log S) where S is the sum of the weights.

## The skill

The array here is **not** what you search. You binary-search the *space of candidate
answers*, using template 4 with the predicate "can this capacity finish in time?".

Three things to work out, in this order:

1. **Bounds.** The smallest workable capacity is `max(weights)` — anything less and one
   package never ships. The largest ever needed is `sum(weights)`, shipping everything
   in one day. Getting these wrong is the usual failure.
2. **The predicate.** `feasible(capacity)` greedily packs days and returns whether the
   count is `<= days`. This is O(n) and it is where the real work happens.
3. **Monotonicity.** If capacity `c` works then `c + 1` works. Say why out loud —
   that is the justification that licenses the binary search, and an interviewer will
   ask for it.

This drill is where binary search stops being an array trick and becomes an
optimisation technique.

## Hints

<details><summary>Hint 1 — the feasibility check</summary>

```java
private int daysNeeded(int[] w, int cap) {
    int d = 1, load = 0;
    for (int x : w) {
        if (load + x > cap) { d++; load = 0; }
        load += x;
    }
    return d;
}
```

</details>

<details><summary>Hint 2 — the search</summary>

```java
int lo = max(weights), hi = sum(weights);
while (lo < hi) {
    int mid = lo + (hi - lo) / 2;
    if (daysNeeded(weights, mid) <= days) hi = mid;
    else                                  lo = mid + 1;
}
return lo;
```

Identical in shape to template 4. Only the predicate changed.

</details>
