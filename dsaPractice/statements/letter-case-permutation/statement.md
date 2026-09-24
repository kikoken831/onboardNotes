# Letter Case Permutation

LeetCode 784 · Medium · https://leetcode.com/problems/letter-case-permutation/

## Brief

Changing the case of any letters in `s` (digits stay), return every string you can make, in any order.

## Constraints

- `1 <= s.length <= 12`; letters and digits

## Examples

```
"a1b2"   ->  ["a1b2","a1B2","A1b2","A1B2"]
"12345"  ->  ["12345"]
```

## Signature

```java
public List<String> letterCasePermutation(String s)
```

## Target complexity

O(n · 2^L), L = number of letters.

## The skill

Binary choices only at **letters**; digits pass through. Branching on digits too produces duplicates. Set both cases explicitly rather than "toggling", which depends on the input's original case (`"C"`).

## Hints

<details><summary>Hint 1</summary>

Each letter doubles the output.

</details>
