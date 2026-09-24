# Encode and Decode Strings

LeetCode 271 · Medium · Premium · https://leetcode.com/problems/encode-and-decode-strings/

## Brief

*(Paraphrased — a Premium problem.)* Design `class Codec`: `encode(strs)` turns a list of
strings into **one** string, and `decode(s)` recovers the exact list. The strings may contain
**any** characters — including whatever you'd like to use as a delimiter — and may be empty.

The judge decodes on a **separate** `Codec` instance (the encoded string is "sent to another
machine"), and compares the result with the original list.

## Constraints

- `0 <= strs.length <= 200`, `0 <= strs[i].length <= 200`

## Examples

```
["lint","code","love","you"]  ->  round-trips unchanged
[""]                          ->  [""]         one empty string, not an empty list
```

## Signature

```java
public class Codec {

    // Encodes a list of strings to a single string.
    public String encode(List<String> strs) {
        
    }

    // Decodes a single string to a list of strings.
    public List<String> decode(String s) {
        
    }
}
```

## Target complexity

O(total length).

## The skill

A delimiter alone can't work when the strings can contain the delimiter. **Length-prefix**
each string — `len#text` — and decode by reading the length, then jumping exactly that many
characters, so whatever the text contains is never interpreted. Escaping (doubling the
delimiter) is the other correct route. `[""]` vs `[]` is pinned: they must encode differently.

## Hints

<details><summary>Hint 1</summary>

After reading `len` and the `#`, take `substring(j + 1, j + 1 + len)` — don't search for the next
delimiter.

</details>
