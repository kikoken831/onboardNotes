# Choose k of n, copying at the leaf

Tier 1 drill · backtracking · unlocks LC 77, 39, 216, 40

## Brief

Return every combination of `k` numbers chosen from `1..n`. Any order of the
combinations; within a combination the numbers must be **ascending**.

## Constraints

- `0 <= k <= n <= 12`

## Examples

```
n=4, k=2  ->  [[1,2],[1,3],[1,4],[2,3],[2,4],[3,4]]
n=1, k=1  ->  [[1]]
n=3, k=0  ->  [[]]
n=3, k=3  ->  [[1,2,3]]
n=2, k=3  ->  []          (impossible)
```

## Signature

```java
public List<List<Integer>> combinations(int n, int k)
```

## Target complexity

O(k · C(n,k)).

## The skill

The **start-index** template, which is the one you will reuse most:

```java
private void go(int n, int k, int start, List<Integer> cur, List<List<Integer>> out) {
    if (cur.size() == k) { out.add(new ArrayList<>(cur)); return; }
    for (int v = start; v <= n; v++) {
        cur.add(v);
        go(n, k, v + 1, cur, out);      // v + 1: never look back
        cur.remove(cur.size() - 1);
    }
}
```

**Recursing on `v + 1` is what makes combinations combinations.** It prevents ever
reusing a value and forces ascending order, which together mean `{1,2}` and `{2,1}` are
never both generated. Change it to `v` and you allow reuse — which is exactly LC 39
combination-sum. Change it to `start` from a fresh loop each level and you get
permutations. **One parameter distinguishes three classic problems**, and being able to
say which is which is worth more than the templates themselves.

The pruning worth adding: stop the loop when there are not enough values left to reach
size `k`. `v <= n - (k - cur.size()) + 1`. Not needed for correctness, and the judge
does not require it, but it is the natural thing to mention when asked about complexity.

## Hints

<details><summary>Hint 1 — k = 0</summary>

Returns `[[]]`, one empty combination. The base case fires immediately. Check you do
not special-case this into returning `[]`.

</details>

<details><summary>Hint 2 — impossible cases</summary>

`n=2, k=3` returns `[]`, an empty list of combinations — not `[[]]`. The loop simply
never reaches size 3. No explicit check needed; make sure yours falls out naturally
rather than being special-cased.

</details>
