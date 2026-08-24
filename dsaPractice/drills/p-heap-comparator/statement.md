# PriorityQueue over int[] with a comparator

Tier 1 drill · heap · unlocks LC 973, 253, 1046, 692

## Brief

Given `points` as `{x, y}` pairs, return the `k` closest to the origin by Euclidean
distance, sorted ascending by distance. Break ties by `x`, then by `y`, so the answer is
unique.

## Constraints

- `0 <= points.length <= 10^4`
- `-10^4 <= x, y <= 10^4`
- `0 <= k <= points.length`

## Examples

```
[[1,3],[-2,2]], k=1        ->  [[-2,2]]
[[3,3],[5,-1],[-2,4]], k=2 ->  [[3,3],[-2,4]]
[], k=0                    ->  []
[[0,0]], k=1               ->  [[0,0]]
[[1,0],[0,1]], k=2         ->  [[0,1],[1,0]]   (equal distance; x breaks the tie)
```

## Signature

```java
public int[][] kClosest(int[][] points, int k)
```

## Target complexity

O(n log k).

## The skill

Same bounded max-heap as the previous drill, but over `int[]` rather than boxed
integers — which is where Java gets specifically awkward:

```java
PriorityQueue<int[]> heap = new PriorityQueue<>(
    (a, b) -> Long.compare(dist(b), dist(a)));    // max-heap by distance
```

Four Java-specific traps, all of them real:

- **`PriorityQueue<int[]>`, not `PriorityQueue<Integer>`.** A widely-circulated GitHub
  cheatsheet declares `PriorityQueue<Integer>` with an `int[]` comparator; it does not
  compile. Check the type parameter matches what you actually store.
- **Do not subtract to compare.** `dist` reaches 2 × 10^8 here, which is still inside
  `int`, but the habit is what matters — measured, `a - b` on `(MIN_VALUE, 1)` returns
  the wrong sign. Use `Long.compare` or `Integer.compare`.
- **Compare squared distances.** No `Math.sqrt`: it is slower, and floating point
  introduces ties that should not exist. `x*x + y*y` preserves ordering exactly.
  Use `long` for the squares if the coordinate range is large.
- **Sorting `int[][]` at the end.** `Arrays.sort(int[][], cmp)` *is* available — the
  no-comparator restriction only applies to arrays of primitives like `int[]`. This is
  a genuine asymmetry in the API worth knowing.

Then the ordering: comparators compose with `thenComparing`, but for `int[]` it is
usually clearest to write the three-way comparison by hand and be explicit about the
tie-breaks.

## Hints

<details><summary>Hint 1 — heap direction, again</summary>

Keeping the k *closest* means evicting the *farthest*, so the heap root must be the
farthest: a max-heap by distance, hence `dist(b)` compared against `dist(a)`. Getting
this backwards returns the k farthest points, which looks structurally correct.

</details>

<details><summary>Hint 2 — the required output order</summary>

The heap gives you the right k points in the wrong order. Drain, then sort with the
same comparator you would use for the final ordering: distance, then x, then y.

</details>
