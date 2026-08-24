# Remove every occurrence of a value

Tier 1 drill · write-index · unlocks LC 27, 26, 283

## Brief

Given an array `nums` and a value `val`, remove every element equal to `val` **in place**
and return the number of elements that remain. The first `k` slots of `nums` must hold
the surviving elements, in their original relative order. Whatever sits beyond index
`k - 1` is ignored and may be anything.

You may not allocate a second array.

## Constraints

- `0 <= nums.length <= 100`
- `0 <= nums[i], val <= 100`

## Examples

```
nums = [3,2,2,3], val = 3   ->  k = 2, nums starts [2,2]
nums = [0,1,2,2,3,0,4,2], val = 2  ->  k = 5, nums starts [0,1,3,0,4]
nums = [], val = 1          ->  k = 0
nums = [2,2,2], val = 2     ->  k = 0
```

## Signature

```java
public int removeValue(int[] nums, int val)
```

## Target complexity

O(n) time, O(1) extra space, single pass.

## The skill

This is the **write index** and it is the most reused mechanical idea in the whole
array section. One pointer reads every slot; a second, slower pointer says where the
next surviving element goes. They are not the same pointer and they do not advance
together — the write pointer only moves when you actually keep something.

Get this one wrong and you will get LC 26, 27, 80, 283 and 443 wrong the same way.

## Hints

<details><summary>Hint 1 — the shape</summary>

```java
int w = 0;
for (int r = 0; r < nums.length; r++) {
    if (keep(nums[r])) nums[w++] = nums[r];
}
return w;
```

The whole pattern is those four lines. Everything else is deciding what `keep` means.

</details>

<details><summary>Hint 2 — why it cannot clobber</summary>

`w <= r` at every step, because `w` advances at most once per iteration and `r`
advances exactly once. So `nums[w] = nums[r]` never overwrites a slot you have not
already read. That invariant is worth being able to state out loud.

</details>
