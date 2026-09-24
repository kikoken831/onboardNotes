# Combination Sum III

LeetCode 216 · Medium · https://leetcode.com/problems/combination-sum-iii/

## Brief

Find every combination of `k` **distinct** numbers from `1–9` that sums to `n`.

## Constraints

- `2 <= k <= 9`, `1 <= n <= 60`

## Examples

```
k=3, n=7  ->  [[1,2,4]]
k=3, n=9  ->  [[1,2,6],[1,3,5],[2,3,4]]
```

## Signature

```java
public List<List<Integer>> combinationSum3(int k, int n)
```

## Target complexity

At most C(9, k).

## The skill

LC 77 with a sum constraint. Stop a branch as soon as it has `k` numbers — whether or not they sum to `n` — and prune values larger than the remainder.

## Hints

<details><summary>Hint 1</summary>

Record only when size is `k` **and** the remainder is `0`.

</details>
