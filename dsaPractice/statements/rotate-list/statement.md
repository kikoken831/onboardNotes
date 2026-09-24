# Rotate List

LeetCode 61 · Medium · https://leetcode.com/problems/rotate-list/

## Brief

Rotate the list to the **right** by `k` places. `k` can be far larger than the length. the standard `ListNode` (`val`, `next`) is provided.

## Constraints

- `0 <= size <= 500`, `0 <= k <= 2 * 10^9`

## Examples

```
[1,2,3,4,5], k=2  ->  [4,5,1,2,3]
[0,1,2],     k=4  ->  [2,0,1]
```

## Signature

```java
public ListNode rotateRight(ListNode head, int k)
```

## Target complexity

O(n).

## The skill

Find the length and the tail, reduce `k %= n`, then cut at node `n - k` and splice the old
tail onto the old head. The judge includes `k = 2 × 10^9` — rotating one step at a time
times out — and the empty list.

## Hints

<details><summary>Hint 1</summary>

Connecting the tail to the head makes a ring; rotation is then just choosing where to cut it.

</details>
