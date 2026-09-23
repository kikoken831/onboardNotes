# Design HashMap

LeetCode 706 · Easy · https://leetcode.com/problems/design-hashmap/

## Brief

Implement a hash map from `int` to `int` **without any built-in hash table**: `put(key,
value)` inserts or **updates**, `get(key)` returns the value or `-1` if absent, `remove(key)`
deletes the mapping if present.

Design problem: implement `class MyHashMap`.

## Constraints

- `0 <= key, value <= 10^6`
- at most `10^4` calls

## Examples

```
["MyHashMap","put","put","get","get","put","get","remove","get"]
[[],[1,1],[2,2],[1],[3],[2,1],[2],[2],[2]]
->  [null,null,null,1,-1,null,1,null,-1]
```

## Signature

```java
class MyHashMap {

    public MyHashMap() {
        
    }

    public void put(int key, int value) {
        
    }

    public int get(int key) {
        
    }

    public void remove(int key) {
        
    }
}
```

## Target complexity

O(1) average per call.

## The skill

705 plus a value, and one new trap: **`0` is a legal value.** A direct-address
`int[1_000_001]` that uses `0` to mean "absent" returns `-1` after `put(5, 0)`. Initialise to
`-1` (with `Arrays.fill`), or keep a separate presence flag. The judge's second example is
exactly that case.

Also: `put` on an existing key must **update**, not add a duplicate that a later `remove`
only half-deletes.

## Hints

<details><summary>Hint 1</summary>

Chaining with `List<int[]>[] buckets` where each entry is `{key, value}`.

</details>
