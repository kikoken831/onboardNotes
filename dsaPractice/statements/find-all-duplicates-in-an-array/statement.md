# Find All Duplicates in an Array

LeetCode 442 · Medium · https://leetcode.com/problems/find-all-duplicates-in-an-array/

## Brief

Values are in `[1, n]` and each appears **once or twice**. Return every value that appears
twice, in any order.

## Constraints

- `1 <= n <= 10^5`

## Examples

```
[4,3,2,7,8,2,3,1]  ->  [2,3]
[1,1,2]            ->  [1]
```

## Signature

```java
public List<Integer> findDuplicates(int[] nums)
```

## Target complexity

O(n) time, O(1) extra space.

## The skill

448's sign-flip trick, read the other way: when you go to flip `nums[|v| - 1]` and it's
**already negative**, you've seen `v` before — record it.

## Hints

<details><summary>Hint 1</summary>

Flip on first sight; report on second sight.

</details>
