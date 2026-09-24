# Factor Combinations

LeetCode 254 · Medium · Premium · https://leetcode.com/problems/factor-combinations/

## Brief

*(Paraphrased — a Premium problem.)* Return every way to write `n` as a product of integer
factors **greater than 1 and less than `n`**, each combination once (factor order doesn't
matter). `n` itself alone doesn't count, so primes and `1` give `[]`.

## Constraints

- `1 <= n <= 10^7`

## Examples

```
12  ->  [[2,6],[3,4],[2,2,3]]
37  ->  []
1   ->  []
```

## Signature

```java
public List<List<Integer>> getFactors(int n)
```

## Target complexity

Proportional to the output; factor search up to `√n` at each level.

## The skill

Backtracking where each level tries factors **no smaller than the previous one** (that order is
what removes duplicate combinations), up to `√n`. At each factor `f`, record `f × (n / f)`, then
try splitting `n / f` further. Searching `f` all the way to `n` is correct and slow.

## Hints

<details><summary>Hint 1</summary>

`for (f = lo; f * f <= n; f++)` — and use `long` for `f * f`.

</details>
