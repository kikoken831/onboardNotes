# First index with value > x  (upper bound)

Tier 1 drill · binary-search · unlocks LC 744, 34

## Brief

`nums` is sorted ascending and may contain duplicates. Return the smallest index `i`
such that `nums[i] > x`, strictly greater. Return `nums.length` if there is none.

## Constraints

- `0 <= nums.length <= 10^4`
- sorted ascending, duplicates allowed

## Examples

```
[1,3,5,6],  5  ->  3
[1,3,5,6],  2  ->  1
[1,3,5,6],  6  ->  4
[2,2,2],    2  ->  3   (past the whole equal run)
[2,2,2],    1  ->  0
[],         1  ->  0
```

## Signature

```java
public int bsFirstGT(int[] nums, int x)
```

## Target complexity

O(log n) time, O(1) space.

## The skill

**Template 3, and it differs from template 2 by exactly one character:** `<` becomes
`<=`.

```java
if (nums[mid] <= x) lo = mid + 1;   // was `<` in lower_bound
```

That is the entire difference. Write both templates side by side and stare at them —
knowing *which one character* separates them is worth more than memorising two blocks
of code, and it is the thing that stops you fumbling under pressure.

Contrast the `[2,2,2]` rows: `bsFirstGE` returns 0, `bsFirstGT` returns 3. Together
they bracket the run of equal values, which is how you count occurrences — the next
drill.

## Hints

<details><summary>Hint 1</summary>

```java
int lo = 0, hi = nums.length;
while (lo < hi) {
    int mid = lo + (hi - lo) / 2;
    if (nums[mid] <= x) lo = mid + 1;
    else                hi = mid;
}
return lo;
```

</details>
