# Previous smaller element to the left

Tier 1 drill · monotonic-stack · unlocks LC 84, 42, 907

## Brief

For each index `i`, return the index of the nearest `j < i` with `nums[j] < nums[i]`, or
`-1` if there is none.

## Constraints

- `0 <= nums.length <= 10^5`

## Examples

```
[2,1,5,6,2,3]  ->  [-1,-1,1,2,1,4]
[1,2,3]        ->  [-1,0,1]
[3,2,1]        ->  [-1,-1,-1]
[]             ->  []
[4,4,4]        ->  [-1,-1,-1]
```

## Signature

```java
public int[] prevSmallerLeft(int[] nums)
```

## Target complexity

O(n) time.

## The skill

The mirror of the previous drill, and doing both is what makes the pattern stick — the
four variants (next/previous × greater/smaller) differ only in **scan direction** and
the **comparison operator**, and confusing them is the usual failure.

This one resolves the current element's answer immediately rather than resolving pending
ones:

```java
Deque<Integer> st = new ArrayDeque<>();      // indices, increasing values
for (int i = 0; i < nums.length; i++) {
    while (!st.isEmpty() && nums[st.peek()] >= nums[i]) st.pop();
    out[i] = st.isEmpty() ? -1 : st.peek();
    st.push(i);
}
```

Note the difference in *when* you write the answer. In next-greater you write into
`out[j]` for popped indices — the answer arrives later than the question. Here you write
`out[i]` for the current index — the answer is already on the stack. Same data
structure, opposite information flow. Being able to say which one a problem needs is
the skill.

Why `>=` and not `>`: popping equal values keeps the stack **strictly** increasing, so
`peek` gives a genuinely smaller element. With `>` you would leave equals in place and
`[4,4,4]` would report index 0 for the later 4s, which is wrong for "smaller".

Combine this with next-smaller and you get, for each bar, the maximal window in which it
is the minimum — which is the whole of LC 84 largest-rectangle-in-histogram, and the
stack half of LC 42 trapping-rain-water. Both are in your target list as Hard.

## Hints

<details><summary>Hint 1 — one array, two passes</summary>

Next-smaller-to-the-right is this same loop run backwards. Write it as a helper taking
a direction flag, or just write it twice — for LC 84 you need both and they should take
two minutes each once this one works.

</details>
