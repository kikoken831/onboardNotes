# First Missing Positive

LeetCode 41 · Hard · https://leetcode.com/problems/first-missing-positive/

## Brief

Return the smallest positive integer **not** in `nums`, which is unsorted and may contain
negatives, zeros, duplicates and huge values.

## Constraints

- `1 <= n <= 10^5`
- `-2^31 <= nums[i] <= 2^31 - 1`

## Examples

```
[1,2,0]        ->  3
[3,4,-1,1]     ->  2
[7,8,9,11,12]  ->  1
```

## Signature

```java
public int firstMissingPositive(int[] nums)
```

## Target complexity

O(n) time **and** O(1) extra space.

## The skill

The answer is always in `1..n+1`, so only values in that range matter. **Cyclic sort**:
swap each such value `v` into slot `v - 1`, then scan for the first slot that holds the
wrong value. The loop guard `a[a[i] - 1] != a[i]` is what stops duplicates like `[1,1]` from
swapping forever — the judge includes that case.

## Hints

<details><summary>Hint 1</summary>

A `HashSet` gives O(n) time and O(n) space. Which values could possibly be the answer?

</details>

<details><summary>Hint 2</summary>

Use `while`, not `if`, when placing: the value swapped into `i` also needs placing.

</details>
