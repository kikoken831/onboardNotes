# N-Queens

LeetCode 51 · Hard · https://leetcode.com/problems/n-queens/

## Brief

Place `n` queens on an `n × n` board so none attack each other. Return every distinct board, each as `n` strings of `'Q'` and `'.'`, in any order.

## Constraints

- `1 <= n <= 9`

## Examples

```
4  ->  [[".Q..","...Q","Q...","..Q."],["..Q.","Q...","...Q",".Q.."]]
1  ->  [["Q"]]
```

## Signature

```java
public List<List<String>> solveNQueens(int n)
```

## Target complexity

O(n!) with pruning.

## The skill

One queen per **row**, so the recursion places row by row and only columns and diagonals need checking. Every cell on a `/` diagonal shares `r + c`; every cell on a `\` diagonal shares `r − c` (offset by `n` to index an array). Three boolean arrays make each check O(1) — scanning the board for attacks is the slow version. `n = 2` and `n = 3` have no solutions.

## Hints

<details><summary>Hint 1</summary>

`d1[r + c]` and `d2[r - c + n]`.

</details>
