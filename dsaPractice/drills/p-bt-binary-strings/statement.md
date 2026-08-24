# Generate every binary string of length n

Tier 1 drill · backtracking · unlocks LC 78, 22, 46

## Brief

Return every binary string of length `n`, in **lexicographic order** ("0" before "1" at
every position). For `n = 0` return a list containing the single empty string.

## Constraints

- `0 <= n <= 14`

## Examples

```
n = 2  ->  ["00","01","10","11"]
n = 0  ->  [""]
n = 1  ->  ["0","1"]
n = 3  ->  ["000","001","010","011","100","101","110","111"]
```

## Signature

```java
public List<String> binaryStrings(int n)
```

## Target complexity

O(n · 2^n) — there are 2^n answers, each of length n.

## The skill

The simplest possible backtracking: at each of `n` positions, choose `'0'` then choose
`'1'`. No constraints, no pruning, nothing to think about except the shape.

```java
private void go(int n, StringBuilder cur, List<String> out) {
    if (cur.length() == n) { out.add(cur.toString()); return; }   // leaf: record
    cur.append('0');  go(n, cur, out);  cur.deleteCharAt(cur.length()-1);
    cur.append('1');  go(n, cur, out);  cur.deleteCharAt(cur.length()-1);
}
```

Three things, and they are the same three every backtracking problem has:

1. **Base case = a complete candidate.** Record it and return.
2. **Choose, recurse, un-choose.** The `deleteCharAt` is the un-choose. Drop it and the
   builder keeps growing and every answer is wrong.
3. **Choice order determines output order.** `'0'` before `'1'` gives lexicographic
   order for free — you do not sort at the end.

`cur.toString()` at the leaf is the "copy at the leaf" rule from the tree drill wearing
a different hat: `StringBuilder` is mutable, so you must snapshot it.

Backtracking has no LeetCode Easy problems at all — the pattern starts at Medium. This
drill and the five after it are the on-ramp that LeetCode does not provide.

## Hints

<details><summary>Hint 1 — why not just count to 2^n in binary?</summary>

You can, and for this problem it is shorter. But the point is the recursion template,
because the bit-counting trick does not generalise to subsets-with-constraints,
permutations, or n-queens. Write the recursion.

</details>

<details><summary>Hint 2 — n = 0</summary>

The answer is `[""]`, one empty string, not `[]`. The base case fires immediately with
an empty builder. Choosing zero things is still one way of choosing.

</details>
