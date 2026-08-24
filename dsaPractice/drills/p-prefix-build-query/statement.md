# Prefix sums with the +1 offset

Tier 1 drill · prefix-sum · unlocks LC 303, 1480, 304

## Brief

Given `nums` and a list of `queries`, each `{i, j}` asking for the sum of
`nums[i..j]` **inclusive**, return the answers as an `int[]`.

Build a prefix array first. Answering each query with a loop is O(n) per query and the
growth curve will show it.

## Constraints

- `0 <= nums.length <= 10^5`
- `0 <= i <= j < nums.length`

## Examples

```
nums=[1,2,3,4],  queries=[[0,0],[0,3],[1,2]]  ->  [1,10,5]
nums=[5],        queries=[[0,0]]              ->  [5]
nums=[-1,-2,-3], queries=[[0,2]]              ->  [-6]
nums=[1,2,3],    queries=[]                   ->  []
```

## Signature

```java
public int[] rangeSums(int[] nums, int[][] queries)
```

## Target complexity

O(n) to build, **O(1) per query**.

## The skill

The whole drill is one convention, and getting it right permanently is worth more than
the ten lines of code:

```java
int[] p = new int[nums.length + 1];              // length n+1, NOT n
for (int i = 0; i < nums.length; i++) p[i+1] = p[i] + nums[i];
// then:
sum(i..j) = p[j+1] - p[i];
```

**`p[k]` is the sum of the first `k` elements**, so `p[0] = 0` and `p[n]` is the total.
The `+1` sizing is what makes it work: the empty prefix has somewhere to live, so
`i = 0` needs no special case.

The alternative convention — `p[i]` is the sum up to and including `i` — gives
`sum(i..j) = p[j] - p[i-1]`, and now `i = 0` reads `p[-1]` and you need a branch. That
branch is the bug. **Always size it `n + 1`.**

Say the invariant out loud before writing the loop: *"p of k is the sum of the first k
elements."* Everything else follows from it, including the `j+1` in the query, which
otherwise looks arbitrary.

## Hints

<details><summary>Hint 1 — check the total</summary>

`p[nums.length]` should equal the sum of everything. If it does not, the loop bounds
are off by one.

</details>

<details><summary>Hint 2 — overflow</summary>

With n = 10^5 and values up to 10^4 the total reaches 10^9, which fits in an `int` but
is within a factor of two of overflowing. For LC 560 and friends, `long` for the prefix
array is the safer habit.

</details>
