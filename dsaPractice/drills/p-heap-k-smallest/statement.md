# K smallest with a bounded max-heap

Tier 1 drill · heap · unlocks LC 215, 973, 347, 703

## Brief

Return the `k` smallest values of `nums`, sorted ascending. If `k >= nums.length`,
return everything sorted. If `k <= 0`, return an empty array.

Duplicates count separately: `[1,1,2]` with `k=2` gives `[1,1]`.

## Constraints

- `0 <= nums.length <= 10^5`
- `-10^4 <= nums[i] <= 10^4`

## Examples

```
[3,1,4,1,5], k=2  ->  [1,1]
[3,1,4,1,5], k=0  ->  []
[3,1,4], k=9      ->  [1,3,4]
[], k=3           ->  []
[5], k=1          ->  [5]
```

## Signature

```java
public int[] kSmallest(int[] nums, int k)
```

## Target complexity

O(n log k) time, O(k) space. Sorting everything is O(n log n) and passes, but the
bounded heap is what the drill is for.

## The skill

**To keep the k smallest, use a MAX-heap of size k.** This is backwards from most
people's first instinct and is the whole idea.

The heap holds the best k seen so far. Its root is the *worst* of those — the largest —
so it is the one to evict when something better arrives:

```java
PriorityQueue<Integer> heap = new PriorityQueue<>(Comparator.reverseOrder());
for (int x : nums) {
    heap.offer(x);
    if (heap.size() > k) heap.poll();     // drop the largest
}
```

The heap never exceeds `k + 1` elements, so each operation is O(log k) rather than
O(log n) — that is where the complexity win comes from, and it matters when n is huge
and k is small, or when the input is a stream you cannot hold in memory (LC 703).

Symmetrically: **k largest needs a min-heap.** If you find yourself reasoning it out
each time, the rule is *the heap's root must be the element you are willing to throw
away.*

Two Java specifics that bite here:

- **`Comparator.reverseOrder()`, not `(a,b) -> b - a`.** The subtraction form overflows:
  measured, `(a,b) -> a - b` on `(MIN_VALUE, 1)` returns `+2147483647`, the wrong sign.
  `Comparator.reverseOrder()` or `Comparator.comparingInt` are safe.
- **A `PriorityQueue` is not sorted when you iterate it.** `pq.toString()` on
  `[5,1,4,2,3]` prints `[1, 2, 4, 5, 3]`. Only `poll()` comes out in order — so drain
  it, then reverse, or sort the drained array.

## Hints

<details><summary>Hint 1 — getting the output sorted</summary>

Drain the heap into an array: since it is a max-heap, values come out descending, so
fill the output array **back to front**. Or drain and sort — same complexity, fewer
chances to slip.

</details>

<details><summary>Hint 2 — the alternative</summary>

Quickselect gives O(n) average, and that is the expected answer for LC 215. It is also
O(n²) worst case without randomised pivoting, and much longer to write correctly under
pressure. Know both; lead with the heap, mention quickselect.

</details>
