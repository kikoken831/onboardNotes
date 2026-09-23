# Goal Parser Interpretation

LeetCode 1678 · Easy · https://leetcode.com/problems/goal-parser-interpretation/

## Brief

`command` is made only of the tokens `"G"`, `"()"` and `"(al)"`. Translate them to `"G"`,
`"o"` and `"al"` respectively, in order.

## Constraints

- `1 <= command.length <= 100`
- only the three tokens above

## Examples

```
"G()(al)"         ->  "Goal"
"G()()()()(al)"   ->  "Gooooal"
"(al)G(al)()()G"  ->  "alGalooG"
```

## Signature

```java
public String interpret(String command)
```

## Target complexity

O(n).

## The skill

Tokenising with a variable-width step: look at the current character, decide which token
it is, and advance by **that token's length**. `replace` chains work too, but the scanner
is the version that generalises to real parsers.

## Hints

<details><summary>Hint 1</summary>

At `(`, peek at the next character: `)` means `"()"` (advance 2), `a` means `"(al)"`
(advance 4).

</details>
