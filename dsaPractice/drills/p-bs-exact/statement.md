# Binary search, exact match

Tier 1 drill · binary-search · unlocks LC 704, 33, 81

## Brief

`nums` is sorted ascending with **distinct** values. Return the index of `target`, or
`-1` if it is not present.

## Constraints

- `0 <= nums.length <= 10^4`
- `-10^4 <= nums[i], target <= 10^4`
- sorted ascending, all distinct

## Examples

```
[-1,0,3,5,9,12], 9   ->  4
[-1,0,3,5,9,12], 2   -> -1
[],              1   -> -1
[5],             5   ->  0
[5],             -5  -> -1
```

## Signature

```java
public int bsExact(int[] nums, int target)
```

## Target complexity

O(log n) time, O(1) space. Iterative.

## The skill

This is template 1 of 4, and the four are genuinely different. Learn them as four, not
as one that you mutate under pressure — mutating a half-remembered template is the
single most common way binary search goes wrong in an interview.

**Template 1 (this one): `while (lo <= hi)`, `hi = mid - 1`, return from inside.**
The search space is the *closed* interval `[lo, hi]`, and the loop ends when it is empty.

Two rules that are not optional:

- `int mid = lo + (hi - lo) / 2;` never `(lo + hi) / 2`. The latter overflows. Measured:
  with `lo = MAX-3, hi = MAX-1` it returns `-3`. This exact bug sat in the JDK's own
  `Arrays.binarySearch` for about nine years.
- The empty array must return `-1` without entering the loop. `hi = -1` makes
  `lo <= hi` false immediately, so it falls out naturally — but check it.

## Hints

<details><summary>Hint 1 — the template</summary>

```java
int lo = 0, hi = nums.length - 1;
while (lo <= hi) {
    int mid = lo + (hi - lo) / 2;
    if      (nums[mid] == target) return mid;
    else if (nums[mid] <  target) lo = mid + 1;
    else                          hi = mid - 1;
}
return -1;
```

</details>

<details><summary>Hint 2 — why it terminates</summary>

Every branch strictly shrinks `[lo, hi]`, because `mid` is always inside it and both
updates step *past* `mid`. If you ever write `hi = mid` with `lo <= hi`, you get an
infinite loop when `lo == hi`. That is the bug template 2 is built to avoid.

</details>
