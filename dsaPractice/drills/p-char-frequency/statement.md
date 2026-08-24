# Count characters into int[26]

Tier 1 drill · hash-counting · unlocks LC 242, 383, 387, 49

## Brief

Given a lowercase string `s`, return an `int[26]` where index 0 is the count of `'a'`,
index 1 the count of `'b'`, and so on. Always return a length-26 array, even for an
empty string.

## Constraints

- `0 <= s.length <= 10^5`
- lowercase English letters only

## Examples

```
"aab"  ->  [2,1,0,0, ... ] (26 entries)
""     ->  [0,0,0, ... ]
"zzz"  ->  [0, ... ,3]     (index 25 is 3)
```

## Signature

```java
public int[] charFrequency(String s)
```

## Target complexity

O(n) time, O(1) space (26 is a constant).

## The skill

`s.charAt(i) - 'a'` is the single most-used idiom in the string section, and Java makes
it slightly awkward: `char` arithmetic promotes to `int`, so `s.charAt(i) - 'a'` is an
`int` and can be used as an index directly, but `'a' + 1` is also an `int` and will
print as `98` rather than `b` unless you cast it back.

Prefer `int[26]` to `HashMap<Character,Integer>` whenever the alphabet is fixed. It is
faster, it never boxes, and — importantly for an interview — it makes the O(1) space
claim obviously true.

## Hints

<details><summary>Hint 1</summary>

```java
int[] freq = new int[26];
for (int i = 0; i < s.length(); i++) freq[s.charAt(i) - 'a']++;
return freq;
```

</details>

<details><summary>Hint 2 — the Java trap this avoids</summary>

With a `HashMap<Character,Integer>` you would need `getOrDefault(c, 0)`, because
`map.get(c)` on a missing key returns `null` and unboxing `null` to `int` throws a
`NullPointerException` rather than giving 0. Measured, not theoretical — see the
Tier 0 mechanics sheet.

</details>
