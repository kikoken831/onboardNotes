# Assign Cookies

LeetCode 455 · Easy · https://leetcode.com/problems/assign-cookies/

## Brief

Child `i` is content with a cookie of size `>= g[i]`. Each child gets at most one cookie
and each cookie goes to at most one child. Return the maximum number of content children.

## Constraints

- `1 <= g.length <= 3 * 10^4`, `0 <= s.length <= 3 * 10^4`
- `1 <= g[i], s[j] <= 2^31 - 1`

## Examples

```
g=[1,2,3], s=[1,1]    ->  1
g=[1,2],   s=[1,2,3]  ->  2
```

## Signature

```java
public int findContentChildren(int[] g, int[] s)
```

## Target complexity

O(n log n + m log m).

## The skill

The textbook greedy: sort both, give the **smallest cookie that works** to the **least
greedy** child. Why it's safe: using a bigger cookie than needed can never help a later,
greedier child more than the smaller one would have.

Note `s` may be empty.

## Hints

<details><summary>Hint 1</summary>

After sorting, walk the cookies; each time the current cookie satisfies the current child,
move to the next child.

</details>
