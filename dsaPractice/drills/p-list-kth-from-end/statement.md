# Delete the k-th node from the end

Tier 1 drill · linked-list · unlocks LC 19, 876, 141

## Brief

Remove the `k`-th node counting from the end of the list and return the head. `k` is
always valid: `1 <= k <= length`. `k = 1` means the last node.

Do it in **one pass**.

## Constraints

- `1 <= list length <= 30`
- `1 <= k <= list length`

## Examples

```
[1,2,3,4,5], k = 2  ->  [1,2,3,5]
[1],         k = 1  ->  []
[1,2],       k = 1  ->  [1]
[1,2],       k = 2  ->  [2]
```

## Signature

```java
public ListNode deleteKthFromEnd(ListNode head, int k)
```

## Target complexity

O(n) time, single pass, O(1) space.

## The skill

**The gap.** Advance a lead pointer `k` steps first, then move both pointers together.
When the lead falls off the end, the trailing pointer is exactly `k` from the end. One
pass, no length count.

Combine it with the dummy head from the previous drill and the "delete the first node"
case disappears — which is exactly what `[1], k = 1` and `[1,2], k = 2` are testing.

```java
ListNode dummy = new ListNode(0, head);
ListNode lead = dummy, trail = dummy;
for (int i = 0; i < k; i++) lead = lead.next;
while (lead.next != null) { lead = lead.next; trail = trail.next; }
trail.next = trail.next.next;
return dummy.next;
```

The off-by-one to get right: start both at `dummy` and advance the lead `k` times, then
walk until `lead.next == null`. That leaves `trail` on the node *before* the target,
which is where you need to be to unlink. Starting at `head` instead, or looping while
`lead != null`, both leave you one node off.

## Hints

<details><summary>Hint 1 — verify the gap</summary>

After the setup loop, count the nodes between `trail` and `lead`. It should be exactly
`k`. Check with `[1,2,3,4,5], k = 2` before writing the second loop.

</details>

<details><summary>Hint 2 — the same gap idea elsewhere</summary>

Set the gap to half the list and you get find-the-middle (LC 876). Set both to the head
and advance one twice as fast and you get cycle detection (LC 141). Same two-pointer
family, different offsets.

</details>
