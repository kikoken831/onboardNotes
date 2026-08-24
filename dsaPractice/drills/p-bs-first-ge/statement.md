# First index with value >= x  (lower bound)

Tier 1 drill · binary-search · unlocks LC 35, 278, 34, 300

## Brief

`nums` is sorted ascending and **may contain duplicates**. Return the smallest index
`i` such that `nums[i] >= x`. If every element is smaller than `x`, return
`nums.length`.

This is C++'s `lower_bound` and it is the single most useful binary search variant.

## Constraints

- `0 <= nums.length <= 10^4`
- sorted ascending, duplicates allowed

## Examples

```
[1,3,5,6],    5  ->  2
[1,3,5,6],    2  ->  1
[1,3,5,6],    7  ->  4   (past the end)
[1,3,5,6],    0  ->  0
[2,2,2],      2  ->  0   (FIRST of the equal run)
[],           1  ->  0
```

## Signature

```java
public int bsFirstGE(int[] nums, int x)
```

## Target complexity

O(log n) time, O(1) space.

## The skill

**Template 2: `while (lo < hi)`, `hi = mid`, return `lo`.**

Structurally different from template 1 and worth understanding rather than memorising:

- the search space is the *half-open* interval `[lo, hi)`, and `hi` starts at
  `nums.length`, **not** `length - 1`
- there is no `return` inside the loop; the answer is `lo` when the interval collapses
- `hi = mid`, not `mid - 1`, because `mid` might itself be the answer

The `[2,2,2], 2 -> 0` example is the one that catches people: with duplicates you must
return the *first* match, and template 1 would hand you whichever one it landed on.

Returning `nums.length` for "no such element" is deliberate. It makes the result a
valid **insertion point**, which is exactly what LC 35 asks for.

## Hints

<details><summary>Hint 1 — the template</summary>

```java
int lo = 0, hi = nums.length;          // note: length, not length - 1
while (lo < hi) {
    int mid = lo + (hi - lo) / 2;
    if (nums[mid] < x) lo = mid + 1;   // mid is too small, discard it
    else               hi = mid;       // mid might be the answer, keep it
}
return lo;
```

</details>

<details><summary>Hint 2 — why it terminates</summary>

`mid` is always `< hi` when `lo < hi`, so `hi = mid` strictly decreases `hi`, and
`lo = mid + 1` strictly increases `lo`. The interval shrinks every iteration. This is
why `hi = mid` is safe here but would hang in template 1.

</details>
