# Range Sum Query - Immutable

LeetCode 303 · Easy · https://leetcode.com/problems/range-sum-query-immutable/

## Brief

Implement `class NumArray`: construct with `nums`; `sumRange(left, right)` returns the sum of
`nums[left..right]` **inclusive**. The array never changes.

## Constraints

- `1 <= n <= 10^4`; at most `10^4` calls

## Examples

```
["NumArray","sumRange","sumRange","sumRange"]
[[[-2,0,3,-5,2,-1]],[0,2],[2,5],[0,5]]
->  [null,1,-1,-3]
```

## Signature

```java
class NumArray {

    public NumArray(int[] nums) {

    }

    public int sumRange(int left, int right) {

    }
}
```

## Target complexity

O(n) construction, O(1) per query.

## The skill

**Ramp:** Tier 1 `p-prefix-build-query` — this problem exactly. A size-`n+1` prefix array makes
every range `pre[right + 1] − pre[left]`, with no special case at `left = 0`. Summing the range
per query is O(n) per call.

## Hints

<details><summary>Hint 1</summary>

`pre[0] = 0`, `pre[i + 1] = pre[i] + nums[i]`.

</details>
