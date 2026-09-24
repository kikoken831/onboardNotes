# Rearrange String k Distance Apart

LeetCode 358 · Hard · Premium · https://leetcode.com/problems/rearrange-string-k-distance-apart/

## Brief

*(Paraphrased — a Premium problem.)* Rearrange `s` so that equal characters are **at least `k`
positions apart**, or return `""` if impossible. Any valid arrangement is accepted.

## Constraints

- `1 <= s.length <= 3 * 10^5`, lowercase, `0 <= k <= s.length`

## Examples

```
"aabbcc", k=3    ->  "abcabc"
"aaabc",  k=3    ->  ""
"aaadbbcc", k=2  ->  "abacabcd"   (one of several)
```

## Signature

```java
public String rearrangeString(String s, int k)
```

## Target complexity

O(n log 26).

## The skill

LC 767 generalised from distance 2 to `k`. Greedy with a **max-heap by remaining count** plus a
**cooldown queue**: take the most frequent available letter, then park it for `k` steps before
it can be used again. If the heap runs dry while letters are still parked, it's impossible.
`k <= 1` means no constraint.

## Hints

<details><summary>Hint 1</summary>

The queue holds exactly the last `k - 1` letters used; when it reaches size `k`, the oldest is
free again.

</details>
