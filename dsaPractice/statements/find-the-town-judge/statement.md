# Find the Town Judge

LeetCode 997 · Easy · https://leetcode.com/problems/find-the-town-judge/

## Brief

People are labelled `1..n`. `trust[i] = [a, b]` means `a` trusts `b`. The town judge, if one
exists, **trusts nobody** and is **trusted by everybody else**. There is at most one. Return
the judge's label, or `-1`.

## Constraints

- `1 <= n <= 1000`
- `0 <= trust.length <= 10^4`
- all pairs unique, `a != b`

## Examples

```
n=2, [[1,2]]              ->  2
n=3, [[1,3],[2,3]]        ->  3
n=3, [[1,3],[2,3],[3,1]]  ->  -1    3 trusts someone
```

## Signature

```java
public int findJudge(int n, int[][] trust)
```

## Target complexity

O(n + trust.length), O(n) space.

## The skill

A graph problem answered by **degrees alone**. The judge has in-degree `n-1` and out-degree
`0` — equivalently, `in − out == n − 1`, since nobody else can reach that. One array of
scores does it.

Edge cases the judge covers: `n = 1` with no trust (the lone person is the judge), and
`n = 2` with no trust (no judge).

## Hints

<details><summary>Hint 1</summary>

`score[a]--`, `score[b]++`. Who ends at `n - 1`?

</details>
