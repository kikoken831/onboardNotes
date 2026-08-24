# Move zeros to the back, keep order

Tier 1 drill · write-index · unlocks LC 283

## Brief

Move every `0` in `nums` to the end **in place**, while keeping the relative order of
the non-zero elements unchanged. Return nothing — the array itself is the answer.

You may not allocate a second array.

## Constraints

- `1 <= nums.length <= 10^4`
- `-2^31 <= nums[i] <= 2^31 - 1`

## Examples

```
[0,1,0,3,12]  ->  [1,3,12,0,0]
[0]           ->  [0]
[1,2,3]       ->  [1,2,3]
[0,0,1]       ->  [1,0,0]
```

## Signature

```java
public void moveZeros(int[] nums)
```

## Target complexity

O(n) time, O(1) extra space.

## The skill

The first drill where the array is judged rather than a returned length, so you cannot
leave junk in the tail — you have to actively zero it.

Two ways to do it. Compact-then-fill is two passes and is the one to reach for first
because it is obviously correct. Swap-as-you-go is one pass and is what interviewers
expect you to arrive at. Write both; they teach different things.

Note "keep relative order" is the entire difficulty. Without it you would just swap
zeros to the back and be done.

## Hints

<details><summary>Hint 1 — compact then fill</summary>

Run the write index keeping non-zeros. When the loop ends, `w` is the count of
non-zeros, so fill `nums[w..n-1]` with zero.

</details>

<details><summary>Hint 2 — the one-pass version</summary>

`if (nums[r] != 0) swap(nums, w++, r);`

When `w == r` the swap is a no-op, which is why this stays correct on an array with no
zeros. Convince yourself of that rather than trusting it.

</details>
