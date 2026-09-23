# Remove All Adjacent Duplicates In String

LeetCode 1047 · Easy · https://leetcode.com/problems/remove-all-adjacent-duplicates-in-string/

## Brief

Repeatedly delete any two **adjacent equal** letters until none remain. Return the final
string (it is unique).

## Constraints

- `1 <= s.length <= 10^5`
- lowercase English letters

## Examples

```
"abbaca"  ->  "ca"    remove "bb" -> "aaca", remove "aa" -> "ca"
"azxxzy"  ->  "ay"
```

## Signature

```java
public String removeDuplicates(String s)
```

## Target complexity

O(n) — one pass.

## The skill

Removals **cascade**: deleting `bb` makes the two `a`s adjacent. Rescanning until nothing
changes is O(n²). A stack handles the cascade for free, because the top of the stack is
always the character now adjacent to the next one.

A `StringBuilder` *is* the stack here — `setLength(n-1)` is the pop.

## Hints

<details><summary>Hint 1</summary>

For each character: if it equals the top of the stack, pop; otherwise push.

</details>
