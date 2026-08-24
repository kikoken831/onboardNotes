# Three-way partition (Dutch national flag)

Tier 1 drill · write-index · unlocks LC 75

## Brief

`nums` contains only the values `0`, `1` and `2`. Sort it **in place** in a single pass,
without counting occurrences and without calling a library sort.

## Constraints

- `1 <= nums.length <= 300`
- `nums[i]` is `0`, `1` or `2`

## Examples

```
[2,0,2,1,1,0]  ->  [0,0,1,1,2,2]
[2,0,1]        ->  [0,1,2]
[0]            ->  [0]
[2,2,2]        ->  [2,2,2]
[1,1,1]        ->  [1,1,1]
```

## Signature

```java
public void dutchFlag(int[] nums)
```

## Target complexity

O(n) time, O(1) extra space, **one pass**. Counting sort is two passes and is
explicitly not what this drill is for.

## The skill

Three regions and three pointers. `lo` marks the end of the zeros, `hi` marks the start
of the twos, `i` scans. The invariant is:

```
[0 .. lo-1]  all 0
[lo .. i-1]  all 1
[i .. hi]    unexamined
[hi+1 .. n-1] all 2
```

**The trap:** after swapping `nums[i]` with `nums[hi]`, you must NOT advance `i`. The
value you just pulled in from the tail has never been examined. After swapping with
`nums[lo]` you *do* advance, because that value is already known to be a 1.

Almost everyone gets this wrong the first time. Write the invariant down before the loop.

## Hints

<details><summary>Hint 1 — the loop</summary>

```java
int lo = 0, i = 0, hi = nums.length - 1;
while (i <= hi) {
    if      (nums[i] == 0) swap(nums, i++, lo++);
    else if (nums[i] == 2) swap(nums, i,   hi--);   // note: i does NOT advance
    else                   i++;
}
```

</details>

<details><summary>Hint 2 — why `i <= hi` and not `i < hi`</summary>

`hi` is the last unexamined index, not the first known 2. If you stop at `i < hi` you
leave `nums[hi]` unexamined and a trailing 0 or 1 stays in the wrong place.

</details>
