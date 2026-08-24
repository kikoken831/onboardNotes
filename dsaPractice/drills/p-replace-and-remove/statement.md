# Replace and remove, length changes

Tier 1 drill · write-index · unlocks LC 443, 833

## Brief

You are given a `char[] s` and a count `size` saying how many of its leading slots are
live; the rest is spare capacity. Apply two rules to the live prefix:

- every `'a'` becomes **two** `'d'` characters
- every `'b'` is **deleted**

Return the new live length. The array is guaranteed to be large enough to hold the
result. Anything beyond the returned length is ignored.

## Constraints

- `0 <= size <= s.length <= 10^4`
- `s` contains lowercase letters only

## Examples

```
s = ['a','c','d','b','b','c','a', _, _, _], size = 7
  live prefix is "acdbbca"
  delete the b's      -> "acdca"
  expand each a to dd -> "ddcdcdd"
  returns 7, s starts ['d','d','c','d','c','d','d']

s = ['b','b','b'],                size = 3  ->  returns 0
s = ['a', _],                     size = 1  ->  returns 2, s starts ['d','d']
s = ['c'],                        size = 1  ->  returns 1, s starts ['c']
```

## Signature

```java
public int replaceAndRemove(char[] s, int size)
```

## Target complexity

O(n) time, O(1) extra space.

## The skill

This is the drill where the naive write index **breaks**, and that is the point.

Forward compaction works when the output is never longer than the input. Here `'a'`
expands, so a single forward pass would overwrite characters you have not read yet.
The fix is two passes: go **forward** first to delete the `'b'`s and count the `'a'`s,
which tells you the final length; then go **backward** from that final length,
expanding as you go. Writing backwards is safe because the write cursor is always ahead
of the read cursor.

"Which direction is safe to write in?" is the question this drill trains, and it is
exactly the question behind LC 88 merge-sorted-array.

## Hints

<details><summary>Hint 1 — pass one</summary>

Forward, dropping `'b'`. Track `writeIndex` and separately count how many `'a'`s
survived. Final length is `writeIndex + countOfA`.

</details>

<details><summary>Hint 2 — pass two</summary>

Start `read` at `writeIndex - 1` and `write` at `finalLength - 1`, and walk both
downward. On `'a'` write two `'d'`s; otherwise copy one character.

</details>
