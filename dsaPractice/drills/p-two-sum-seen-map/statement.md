# Complement lookup in one pass

Tier 1 drill · hash-counting · unlocks LC 1, 167, 653

## Brief

Given `nums` and a `target`, return the indices `[i, j]` of the two elements that sum
to `target`, with `i < j`. Exactly one such pair exists. Return them in increasing
index order.

Solve it in **one pass**.

## Constraints

- `2 <= nums.length <= 10^4`
- `-10^9 <= nums[i], target <= 10^9`
- exactly one valid answer exists

## Examples

```
[2,7,11,15], 9  ->  [0,1]
[3,2,4],     6  ->  [1,2]
[3,3],       6  ->  [0,1]
[0,4,3,0],   0  ->  [0,3]
```

## Signature

```java
public int[] twoSumSeenMap(int[] nums, int target)
```

## Target complexity

O(n) time, O(n) space, one pass.

## The skill

The **seen map**: as you walk the array, ask whether the value you need has *already
gone past*, then record the current value. The order of those two steps is the whole
drill.

Look up first, insert second. Do it the other way round and `[3,3], 6` finds index 0
pairing with itself, and `[0,4,3,0], 0` breaks too. Both are in the pinned examples on
purpose.

This is the direct precursor to LC 1, and the same "have I already seen what I need"
shape drives LC 560 subarray-sum-equals-k and LC 3 longest-substring.

## Hints

<details><summary>Hint 1</summary>

```java
Map<Integer,Integer> seen = new HashMap<>();
for (int i = 0; i < nums.length; i++) {
    Integer j = seen.get(target - nums[i]);   // look up BEFORE inserting
    if (j != null) return new int[]{j, i};
    seen.put(nums[i], i);
}
```

</details>

<details><summary>Hint 2 — a Java trap worth knowing</summary>

`seen.get(...)` returns `Integer`, which may be `null`. Assigning it straight to an
`int` unboxes and throws `NullPointerException` on a miss. Either keep it as `Integer`
and null-check, or use `containsKey` first. Measured — see the Tier 0 mechanics sheet.

</details>
