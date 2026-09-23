# Product of Array Except Self

LeetCode 238 · Medium · https://leetcode.com/problems/product-of-array-except-self/

## Brief

Return `out` where `out[i]` is the product of every element **except** `nums[i]` —
**without division**. Every product fits in a 32-bit int.

## Constraints

- `2 <= n <= 10^5`
- `-30 <= nums[i] <= 30`

## Examples

```
[1,2,3,4]      ->  [24,12,8,6]
[-1,1,0,-3,3]  ->  [0,0,9,0,0]
```

## Signature

```java
public int[] productExceptSelf(int[] nums)
```

## Target complexity

O(n) time, O(1) extra space besides the output.

## The skill

`out[i] = (product of everything left of i) × (product of everything right of i)`. Build
the left products into `out`, then sweep from the right with one running variable. The
no-division rule exists because of **zeros** — the judge includes one zero and two zeros.

## Hints

<details><summary>Hint 1</summary>

Two arrays `left[]` and `right[]` first; then notice `right[]` can be a single variable.

</details>
