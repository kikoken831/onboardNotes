# Next greater element to the right

Tier 1 drill · monotonic-stack · unlocks LC 496, 739, 503

## Brief

For each index `i`, find the first index `j > i` with `nums[j] > nums[i]`, and return
`j - i` — the *distance* to that element. If no such element exists, return 0 for that
index.

Return an `int[]` the same length as the input.

## Constraints

- `0 <= nums.length <= 10^5`

## Examples

```
[73,74,75,71,69,72,76,73]  ->  [1,1,4,2,1,1,0,0]
[30,40,50]                 ->  [1,1,0]
[50,40,30]                 ->  [0,0,0]
[]                         ->  []
[5]                        ->  [0]
[2,2,2]                    ->  [0,0,0]   (strictly greater, so ties do not count)
```

## Signature

```java
public int[] nextGreaterRight(int[] nums)
```

## Target complexity

O(n) time. The obvious nested loop is O(n²) and the growth curve will show it.

## The skill

**Push indices, not values.** Almost every monotonic-stack problem needs the *distance*
or the *position*, and once you have pushed a bare value you cannot recover where it
came from. Pushing the index costs nothing and you can always read the value back with
`nums[i]`.

```java
Deque<Integer> st = new ArrayDeque<>();          // indices, decreasing values
for (int i = 0; i < nums.length; i++) {
    while (!st.isEmpty() && nums[st.peek()] < nums[i]) {
        int j = st.pop();
        out[j] = i - j;                          // i is j's next greater
    }
    st.push(i);
}
```

The invariant: **the stack holds indices whose answers are still unknown, and their
values are non-increasing from bottom to top.** When a new element arrives it resolves
every pending index smaller than it — and each index is pushed once and popped at most
once, which is why the whole thing is O(n) despite the inner `while`. Being able to
give that amortised argument is the point of the drill as much as the code is.

**`<` versus `<=` decides how ties behave.** Here "strictly greater" means the pop
condition is `nums[st.peek()] < nums[i]`, so equal values do not resolve each other and
`[2,2,2]` gives all zeros. Using `<=` would answer a different question. Read the
statement, then pick the operator; do not reach for a remembered template.

Indices left on the stack at the end never found a greater element, and their answers
stay 0 — which the array's default initialisation gives you for free.

## Hints

<details><summary>Hint 1 — Deque discipline</summary>

Use `push`/`pop`/`peek` on the `ArrayDeque` — the PPP family. Mixing in `add` makes it
a queue and the algorithm silently produces wrong answers rather than failing. See
Tier 0, trap 1.

</details>

<details><summary>Hint 2 — check it is really O(n)</summary>

Once it passes, run `judge time p-mono-next-greater`. You should see roughly t x2 per
doubling. If you see x4 you wrote the nested loop.

</details>
