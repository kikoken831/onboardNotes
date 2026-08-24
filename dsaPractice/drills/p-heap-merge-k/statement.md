# Merge k sorted arrays with a heap

Tier 1 drill · heap · unlocks LC 23, 373, 632, 378

## Brief

Given `k` arrays, each already sorted ascending, merge them into one sorted `int[]`.

## Constraints

- `0 <= k <= 1000`
- total elements `<= 10^5`
- rows may be empty

## Examples

```
[[1,4,5],[1,3,4],[2,6]]  ->  [1,1,2,3,4,4,5,6]
[]                       ->  []
[[]]                     ->  []
[[1]]                    ->  [1]
[[],[1],[]]              ->  [1]
```

## Signature

```java
public int[] mergeKSorted(int[][] arrays)
```

## Target complexity

O(N log k) where N is the total element count. Concatenating and sorting is
O(N log N) and passes, but misses the point.

## The skill

The heap holds **one candidate per array** — the smallest element not yet taken from
each. Pop the global minimum, emit it, and push the next element from whichever array it
came from. That is why each entry has to carry its provenance:

```java
// {value, whichArray, indexWithinThatArray}
PriorityQueue<int[]> heap = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));
for (int i = 0; i < arrays.length; i++) {
    if (arrays[i].length > 0) heap.offer(new int[]{arrays[i][0], i, 0});
}
while (!heap.isEmpty()) {
    int[] top = heap.poll();
    out[w++] = top[0];
    int next = top[2] + 1;
    if (next < arrays[top[1]].length) {
        heap.offer(new int[]{arrays[top[1]][next], top[1], next});
    }
}
```

**The heap never holds more than k entries**, which is the entire complexity argument:
N pops, each O(log k), rather than O(log N). When k is small and N is large that is a
real difference, and when the inputs are streams rather than arrays it is the only
option.

Two things to be careful about:

- **Skip empty arrays when seeding.** Pushing `arrays[i][0]` for an empty row throws
  `ArrayIndexOutOfBoundsException`. `[[]]` and `[[],[1],[]]` are pinned for this.
- **Carry the index, not just the array id.** Without it you cannot find "the next
  element from that array", and reconstructing it costs a scan.

This is LC 23 merge-k-sorted-lists with arrays instead of lists — the algorithm is
identical, only the traversal changes. It is also the shape of LC 632
smallest-range-covering-k-lists, where you track the heap's min and max simultaneously.

## Hints

<details><summary>Hint 1 — sizing the output</summary>

Sum the row lengths first and allocate exactly. Growing an `ArrayList<Integer>` boxes
every element and then needs converting back — see Tier 0, trap 9, for the conversion
idiom if you do go that route.

</details>

<details><summary>Hint 2 — the divide-and-conquer alternative</summary>

Merging pairs of arrays repeatedly, tournament style, is also O(N log k) and needs no
heap at all. For LC 23 it is often the cleaner answer. Worth writing once you have this.

</details>
