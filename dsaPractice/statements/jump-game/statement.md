# Jump Game

LeetCode 55 · Medium · https://leetcode.com/problems/jump-game/

## Brief

You start at index 0; `nums[i]` is the **maximum** jump length from `i`. Can you reach the last index?

## Constraints

- `1 <= n <= 10^4`, `0 <= nums[i] <= 10^5`

## Examples

```
[2,3,1,1,4]  ->  true
[3,2,1,0,4]  ->  false    stuck at the 0
```

## Signature

```java
public boolean canJump(int[] nums)
```

## Target complexity

O(n), O(1) space.

## The skill

Greedy: track the **furthest reachable index**. If the scan ever reaches an index beyond it, you're stuck. The DP over "can reach `i`" is O(n²) and teaches why the greedy is enough — reachability is always a prefix.

## Hints

<details><summary>Hint 1</summary>

`if (i > reach) return false;`

</details>
