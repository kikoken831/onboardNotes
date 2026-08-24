# Flush a monotonic stack with a sentinel

Tier 1 drill · monotonic-stack · unlocks LC 84, 42, 654

## Brief

Given bar `heights`, return the area of the largest rectangle that fits entirely under
the histogram. Bars have width 1.

## Constraints

- `0 <= heights.length <= 10^5`
- `0 <= heights[i] <= 10^4`

## Examples

```
[2,1,5,6,2,3]  ->  10   (bars 5 and 6, height 5, width 2)
[2,4]          ->  4
[]             ->  0
[5]            ->  5
[3,3,3]        ->  9
[0]            ->  0
```

## Signature

```java
public int largestRectangle(int[] heights)
```

## Target complexity

O(n) time.

## The skill

The composition of the two previous drills, plus the trick that makes it clean.

For each bar, the largest rectangle *with that bar as the shortest* extends left until
a shorter bar and right until a shorter bar. Both boundaries come from a monotonic
stack. The awkward part is the end of the input: bars still on the stack have no
shorter bar to their right, so nothing pops them and their rectangles never get
measured.

**The sentinel.** Process one extra virtual bar of height `-1` (or 0) past the end. It
is shorter than everything, so it forces the stack to drain and every pending bar gets
measured by the normal code path:

```java
Deque<Integer> st = new ArrayDeque<>();
int best = 0;
for (int i = 0; i <= heights.length; i++) {          // note: <= , one past the end
    int h = (i == heights.length) ? -1 : heights[i]; // the sentinel
    while (!st.isEmpty() && heights[st.peek()] >= h) {
        int height = heights[st.pop()];
        int left = st.isEmpty() ? -1 : st.peek();
        best = Math.max(best, height * (i - left - 1));
    }
    st.push(i);
}
```

Two subtleties worth the time:

- **The width is `i - left - 1`, not `i - popped`.** After popping, the new stack top is
  the nearest strictly-shorter bar on the left, so the rectangle spans the *open*
  interval between the two boundaries. Getting this wrong gives an answer that is right
  on simple inputs and wrong on `[2,1,5,6,2,3]` — which is why that is the first pinned
  example.
- **The sentinel avoids a duplicated drain loop.** Without it you write the same
  measuring code twice, once in the main loop and once after, and the two copies drift.

"Add a sentinel so the main loop handles the boundary" generalises well beyond
histograms — it is the same reason you use a dummy head in a linked list.

## Hints

<details><summary>Hint 1 — build up to it</summary>

If this is not landing, first write a version that computes `prevSmaller` and
`nextSmaller` as two separate arrays (the two previous drills), then take
`max(h[i] * (next[i] - prev[i] - 1))`. Three passes, O(n), much easier to reason about.
Then collapse it into the single-pass sentinel version.

</details>

<details><summary>Hint 2 — overflow</summary>

Max area here is 10^5 × 10^4 = 10^9, which fits in an `int` but only just —
`Integer.MAX_VALUE` is about 2.1 × 10^9. Fine for these constraints, but worth noticing
that a slightly larger problem would need `long`.

</details>
