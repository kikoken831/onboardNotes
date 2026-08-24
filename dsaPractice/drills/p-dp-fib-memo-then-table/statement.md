# Memoise, then tabulate

Tier 1 drill · dynamic-programming · unlocks LC 509, 70, 1137, 198

## Brief

Return the `n`-th Tribonacci number: `T(0)=0`, `T(1)=1`, `T(2)=1`, and
`T(n) = T(n-1) + T(n-2) + T(n-3)`.

## Constraints

- `0 <= n <= 37` (T(37) is the largest that fits in an `int`)

## Examples

```
n=0   ->  0
n=1   ->  1
n=2   ->  1
n=4   ->  4
n=25  ->  1389537
n=37  ->  2082876103
```

## Signature

```java
public int tribonacci(int n)
```

## Target complexity

O(n) time, O(1) space for the final version.

## The skill

Not the recurrence — that is given. The skill is the **three-step progression** that
every DP problem follows, and doing it deliberately here so it is automatic later:

**1. Naive recursion.** Transcribe the definition. Correct, exponential, unusable
beyond n ≈ 35.

```java
return trib(n-1) + trib(n-2) + trib(n-3);
```

**2. Memoise.** Add a cache. Same code, one array, now O(n). This is the step that
matters most in an interview: it is nearly mechanical, so you can *always* get here from
a correct recursion, even under pressure.

```java
if (memo[n] != -1) return memo[n];
return memo[n] = trib(n-1) + trib(n-2) + trib(n-3);
```

**3. Tabulate.** Note that the recursion only ever reaches down by 3, so fill an array
bottom-up — and then notice you only need the last three values, not the array:

```java
int a = 0, b = 1, c = 1;
for (int i = 3; i <= n; i++) { int d = a + b + c; a = b; b = c; c = d; }
return n == 0 ? 0 : n == 1 || n == 2 ? 1 : c;
```

Write all three. The progression is the deliverable, not the answer.

**Why this matters beyond Tribonacci:** in an interview, getting to step 2 is usually
enough to pass, and step 2 is reachable from any correct brute-force recursion. Trying
to jump straight to step 3 is how people freeze. Every DP problem in your target list —
70, 198, 213, 322, 416, 1143, 300 — is this same ladder with a different recurrence.

## Hints

<details><summary>Hint 1 — base cases in the rolling version</summary>

The loop starts at `i = 3`, so `n` of 0, 1 or 2 never enters it and must be handled
directly. Those three are pinned.

</details>

<details><summary>Hint 2 — the overflow boundary</summary>

T(37) = 2,082,876,103, and `Integer.MAX_VALUE` is 2,147,483,647. T(38) overflows. The
constraint caps n at 37 for exactly this reason — noticing constraints that exist to
prevent overflow is a genuinely useful reading habit.

</details>
