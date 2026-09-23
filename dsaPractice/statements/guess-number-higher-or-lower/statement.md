# Guess Number Higher or Lower

LeetCode 374 · Easy · https://leetcode.com/problems/guess-number-higher-or-lower/

## Brief

A number `pick` in `[1, n]` is hidden. Call the provided `guess(num)`:

- `-1` — your guess is **higher** than the pick (`num > pick`)
- `1` — your guess is **lower** than the pick (`num < pick`)
- `0` — correct

Return the pick. This is **interactive**: your class `extends GuessGame`, which provides
`guess`. The pick is never passed to you.

## Constraints

- `1 <= n <= 2^31 - 1`
- `1 <= pick <= n`

## Examples

```
n=10, pick=6                    ->  6
n=1, pick=1                     ->  1
n=2126753390, pick=1702766719   ->  1702766719
```

## Signature

```java
/**
 * The guess API is defined in the parent class GuessGame.
 * @return -1 if num is higher than the picked number
 *          1 if num is lower than the picked number
 *          0 if num is equal to the picked number
 * int guess(int num);
 */
public class Solution extends GuessGame {
    public int guessNumber(int n) {
        
    }
}
```

## Target complexity

O(log n) calls to `guess`.

## The skill

**Ramp:** Tier 1 `p-bs-predicate-flip` — a binary search where the predicate is a function
call rather than an array read. Two traps, both tested:

- **The sign convention is inverted** from what most people expect: `-1` means *go lower*.
- **`(lo + hi) / 2` overflows** when `n` is near `2^31 - 1` — `lo + hi` exceeds
  `Integer.MAX_VALUE` and goes negative. Use `lo + (hi - lo) / 2`. The judge includes
  `n = pick = 2147483647`.

A linear scan is correct and hopeless at `n = 2 × 10^9`.

## Hints

<details><summary>Hint 1</summary>

Search for the first `num` with `guess(num) <= 0` — the first guess that isn't too low.

</details>
