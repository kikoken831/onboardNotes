# Find the Duplicate Number

LeetCode 287 · Medium · https://leetcode.com/problems/find-the-duplicate-number/

## Brief

`nums` has `n + 1` values in `[1, n]`. Exactly **one** value is repeated (possibly more than
twice). Return it **without modifying `nums`** and with O(1) extra space.

## Constraints

- `1 <= n <= 10^5`

## Examples

```
[1,3,4,2,2]  ->  2
[3,1,3,4,2]  ->  3
[3,3,3,3,3]  ->  3
```

## Signature

```java
public int findDuplicate(int[] nums)
```

## Target complexity

O(n) time, O(1) space, input read-only.

## The skill

Read `i -> nums[i]` as a **linked list**: two indices share a value, so two nodes point at
the same next node — a **cycle**, and its entrance is the duplicate. Floyd's tortoise and
hare finds it (**ramp:** the fast/slow pointers of Tier 1 `p-list-middle`). Binary search on
the value range by counting `<= mid` is the other O(1)-space answer, O(n log n).

The judge can't enforce "don't modify", but sorting or sign-flipping breaks the rules.

## Hints

<details><summary>Hint 1</summary>

Why can index `0` never be part of the cycle, and why does that make it a safe start?

</details>
