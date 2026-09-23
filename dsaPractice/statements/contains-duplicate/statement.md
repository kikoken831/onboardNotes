# Contains Duplicate

LeetCode 217 · Easy · https://leetcode.com/problems/contains-duplicate/

## Brief

Return `true` if any value appears at least twice.

## Constraints

- `1 <= nums.length <= 10^5`
- `-10^9 <= nums[i] <= 10^9`

## Examples

```
[1,2,3,1]  ->  true
[1,2,3,4]  ->  false
```

## Signature

```java
public boolean containsDuplicate(int[] nums)
```

## Target complexity

O(n) time with a set, or O(n log n) and O(1) extra by sorting.

## The skill

`Set.add` returns `false` when the value is already present — that one boolean is the
whole algorithm. Know the trade-off: the set costs O(n) memory; sorting costs time and
mutates the input.

## Hints

<details><summary>Hint 1</summary>

`if (!seen.add(x)) return true;`

</details>
