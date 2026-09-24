# Word Squares

LeetCode 425 · Hard · Premium · https://leetcode.com/problems/word-squares/

## Brief

*(Paraphrased — a Premium problem.)* `words` are unique and all the same length `L`. A word
square is a sequence of `L` words (repeats allowed) where the `k`-th row reads the same as the
`k`-th column. Return every word square, in any order.

## Constraints

- `1 <= words.length <= 1000`, `1 <= L <= 4`

## Examples

```
["area","lead","wall","lady","ball"]
->  [["ball","area","lead","lady"],["wall","area","lead","lady"]]
```

## Signature

```java
public List<List<String>> wordSquares(String[] words)
```

## Target complexity

Backtracking with O(1) prefix lookup per step.

## The skill

Once rows `0..r-1` are chosen, row `r` is **forced** to start with the letters already in column
`r`. So each step is a **prefix query**: index every prefix of every word (a `HashMap` of
prefix → words, or a trie), and only try the words that fit. Trying every word at every row is
the exponential brute force.

## Hints

<details><summary>Hint 1</summary>

The prefix for row `r` is `sq[0][r] + sq[1][r] + … + sq[r-1][r]`.

</details>
