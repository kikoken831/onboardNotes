# Remove Element

LeetCode 27 · Easy · https://leetcode.com/problems/remove-element/

## Brief

Remove every occurrence of `val` from `nums` **in place** and return `k`, the number of
elements that remain. The first `k` slots must hold the survivors **in any order**; slots
past `k` are ignored.

## Constraints

- `0 <= nums.length <= 100`
- `0 <= nums[i] <= 50`
- `0 <= val <= 100`

## Examples

```
[3,2,2,3], val=3          ->  k=2, [2,2,_,_]
[0,1,2,2,3,0,4,2], val=2  ->  k=5, [0,1,4,0,3,_,_,_]   (any order of those five)
```

## Signature

```java
public int removeElement(int[] nums, int val)
```

## Target complexity

O(n) time, O(1) extra space.

## The skill

**Ramp:** Tier 1 `p-remove-value`. This judge, like LeetCode's, accepts **any order**
in the first `k` — which is what licenses the faster "swap with the end" version, where
you never shift the survivors.

Note `n` can be **0** here, unlike LC 26.

## Hints

<details><summary>Hint 1 — order-preserving</summary>

Write-index: copy each non-`val` element to `nums[w++]`.

</details>

<details><summary>Hint 2 — fewer writes</summary>

When `val` is rare, overwrite a matching `nums[i]` with `nums[n-1]` and shrink `n`,
without advancing `i`. Why must `i` not advance?

</details>
