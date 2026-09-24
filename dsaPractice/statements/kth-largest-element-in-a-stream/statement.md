# Kth Largest Element in a Stream

LeetCode 703 · Easy · https://leetcode.com/problems/kth-largest-element-in-a-stream/

## Brief

Construct with `k` and initial values `nums`. Each `add(val)` inserts `val` and returns the
**k-th largest** value seen so far (counting duplicates). It is guaranteed there are at least
`k` values whenever `add` returns.

Design problem: implement `class KthLargest`.

## Constraints

- `0 <= nums.length <= 10^4`
- `1 <= k <= nums.length + 1`
- at most `10^4` calls to `add`

## Examples

```
["KthLargest","add","add","add","add","add"]
[[3,[4,5,8,2]],[3],[5],[10],[9],[4]]
->  [null,4,5,5,8,8]
```

## Signature

```java
class KthLargest {

    public KthLargest(int k, int[] nums) {

    }

    public int add(int val) {

    }
}
```

## Target complexity

O(log k) per `add`, O(k) space.

## The skill

**Ramp:** Tier 1 `p-heap-k-smallest`, mirrored. Keep a **min**-heap of size `k`: the smallest
of the `k` largest values is at its top, and that *is* the answer. Anything smaller than the
top can never matter again.

Sorting on every `add` is O(n log n) per call. Keeping a max-heap of everything and polling
`k` times is O(k log n) and destroys the heap. The judge's growth curve will show both.

## Hints

<details><summary>Hint 1</summary>

After inserting, if the heap holds more than `k` values, poll once.

</details>
