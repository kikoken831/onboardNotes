# Build a list, walk it, return the values

Tier 1 drill · linked-list · unlocks everything list-shaped

## Brief

Given the head of a singly linked list, return its values as an `int[]` in order.
An empty list returns an empty array.

Then, as the second half of the same drill, build a list from an array — but the judge
only checks the first direction, so the build is on your honour.

## Constraints

- `0 <= list length <= 10^4`

## Examples

```
1 -> 2 -> 3     ->  [1,2,3]
(empty)         ->  []
7               ->  [7]
```

## Signature

```java
public int[] listToArray(ListNode head)
```

`ListNode` is the standard LeetCode node and is already on the classpath — do not
declare it yourself:

```java
public class ListNode {
    public int val;
    public ListNode next;
    public ListNode() {}
    public ListNode(int val) { ... }
    public ListNode(int val, ListNode next) { ... }
}
```

## Target complexity

O(n) time. O(n) for the output array, O(1) beyond that.

## The skill

The absolute floor of the linked-list section, here so that the loop shape is automatic
before anything harder asks you to think.

```java
for (ListNode p = head; p != null; p = p.next) { ... }
```

Two things people get wrong even here:

- **`p != null`, not `p.next != null`.** The second one silently skips the last node.
  Which you want depends on whether you need to look ahead; know which you wrote and why.
- You need the length before you can allocate the array, so this is naturally **two
  passes** — count, allocate, fill. Trying to do it in one requires a growable list.

## Hints

<details><summary>Hint 1</summary>

```java
int n = 0;
for (ListNode p = head; p != null; p = p.next) n++;
int[] out = new int[n];
int i = 0;
for (ListNode p = head; p != null; p = p.next) out[i++] = p.val;
return out;
```

</details>
