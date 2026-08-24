# Subsets by include/exclude recursion

Tier 1 drill · backtracking · unlocks LC 78, 90, 416

## Brief

Return every subset of `nums` as a `List<List<Integer>>`. The input has **distinct**
values. Any order is accepted, both of the subsets and within each subset.

## Constraints

- `0 <= nums.length <= 14`
- values are distinct

## Examples

```
[1,2,3]  ->  [[],[1],[2],[3],[1,2],[1,3],[2,3],[1,2,3]]   (any order)
[]       ->  [[]]
[0]      ->  [[],[0]]
[1,2]    ->  [[],[1],[2],[1,2]]
```

## Signature

```java
public List<List<Integer>> subsets(int[] nums)
```

## Target complexity

O(n · 2^n).

## The skill

Same shape as binary strings, reframed: for each index, either **include** `nums[i]` or
**exclude** it. A subset is a binary string over the input positions — recognising that
those are the same problem is the point.

```java
private void go(int[] nums, int i, List<Integer> cur, List<List<Integer>> out) {
    if (i == nums.length) { out.add(new ArrayList<>(cur)); return; }
    go(nums, i + 1, cur, out);                  // exclude
    cur.add(nums[i]);
    go(nums, i + 1, cur, out);                  // include
    cur.remove(cur.size() - 1);                 // un-choose
}
```

**`new ArrayList<>(cur)` is mandatory.** Adding `cur` directly puts the same mutable
list into `out` 2^n times; by the end it is empty and every entry is empty. The judge's
comparison is order-insensitive here, so this bug shows up as "count differs" rather
than as an ordering complaint — but it is the same copy-at-the-leaf rule as the tree
paths drill.

Note the `i` parameter. Backtracking over an array almost always threads an index down
to mean "decisions from here onward" — that is recursion shape 2 again.

## Hints

<details><summary>Hint 1 — the other formulation</summary>

The more common template loops from a start index and recurses on `i+1`, recording at
*every* node rather than only at the leaves:

```java
out.add(new ArrayList<>(cur));
for (int i = start; i < nums.length; i++) {
    cur.add(nums[i]);
    go(nums, i + 1, cur, out);
    cur.remove(cur.size() - 1);
}
```

Both are correct and produce the same set. Write both — the second generalises to
combination-sum, the first is easier to see as "a binary choice per element".

</details>

<details><summary>Hint 2 — duplicates (LC 90)</summary>

With duplicate values you get duplicate subsets. The fix is to sort first and skip
`nums[i] == nums[i-1]` when `i > start`. Not needed here, but that is the next step and
it is worth knowing this drill deliberately avoids it.

</details>
