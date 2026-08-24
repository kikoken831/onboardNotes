# First index where a predicate flips

Tier 1 drill · binary-search · unlocks LC 278, 162, 852

## Brief

You are given a boolean array `flags` that is **monotone**: it is a run of `false`
followed by a run of `true`, and either run may be empty. Return the index of the first
`true`, or `flags.length` if there is none.

You must not scan linearly, and you should touch as few elements as possible.

## Constraints

- `0 <= flags.length <= 10^4`
- `flags` is monotone: once it becomes true it stays true

## Examples

```
[false,false,true,true]  ->  2
[true,true]              ->  0
[false,false]            ->  2   (no true; past the end)
[]                       ->  0
[false,true]             ->  1
```

## Signature

```java
public int bsPredicateFlip(boolean[] flags)
```

## Target complexity

O(log n) time, O(1) space.

## The skill

**Template 4, and it is the one that actually matters.** The previous three search a
sorted array of values. This one searches for the boundary of a *predicate*, and that
is a much bigger idea: the array need not be sorted, need not even exist.

The realisation to have here: templates 2 and 3 are special cases of this one.
`lower_bound` is "first index where `nums[i] >= x`" — a monotone predicate.
`upper_bound` is "first index where `nums[i] > x`". Same search, different predicate.

Once you see it this way, LC 278 first-bad-version is this drill verbatim, LC 162
find-peak-element is this with the predicate `nums[i] > nums[i+1]`, and the whole
"binary search the answer" family (LC 875, 1011, 410) is this over a range of candidate
answers rather than over an array.

## Hints

<details><summary>Hint 1 — same shape as template 2</summary>

```java
int lo = 0, hi = flags.length;
while (lo < hi) {
    int mid = lo + (hi - lo) / 2;
    if (flags[mid]) hi = mid;      // mid might be the first true
    else            lo = mid + 1;  // mid is false, so the answer is after it
}
return lo;
```

</details>

<details><summary>Hint 2 — the general form</summary>

Write it as `if (predicate(mid)) hi = mid; else lo = mid + 1;` and you have a reusable
tool. The only requirement is that `predicate` is monotone — false, then true, never
flipping back. Being able to *state* that requirement is what interviewers are
listening for.

</details>
