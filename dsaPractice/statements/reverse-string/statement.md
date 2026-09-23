# Reverse String

LeetCode 344 · Easy · https://leetcode.com/problems/reverse-string/

## Brief

Reverse the `char[]` **in place**, using O(1) extra memory. Return nothing.

## Constraints

- `1 <= s.length <= 10^5`
- `s[i]` is a printable ASCII character

## Examples

```
["h","e","l","l","o"]      ->  ["o","l","l","e","h"]
["H","a","n","n","a","h"]  ->  ["h","a","n","n","a","H"]
```

## Signature

```java
public void reverseString(char[] s)
```

## Target complexity

O(n) time, O(1) extra space.

## The skill

Converging two pointers. **Ramp:** Tier 1 `p-reverse-in-place` is exactly this on an
`int[]`. The only new thing is the element type.

## Hints

<details><summary>Hint 1</summary>

`i` from the left, `j` from the right, swap, move both inward, stop when they meet. The
loop condition is `i < j` — not `<=`, and certainly not `i < n`, which reverses twice.

</details>
