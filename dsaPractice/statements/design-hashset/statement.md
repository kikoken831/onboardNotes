# Design HashSet

LeetCode 705 · Easy · https://leetcode.com/problems/design-hashset/

## Brief

Implement a hash set of integers **without any built-in hash table** (`HashSet`, `HashMap`,
…): `add(key)`, `remove(key)` (a no-op if absent), `contains(key)`.

Design problem: implement `class MyHashSet`.

## Constraints

- `0 <= key <= 10^6`
- at most `10^4` calls

## Examples

```
["MyHashSet","add","add","contains","contains","add","contains","remove","contains"]
[[],[1],[2],[1],[3],[2],[2],[2],[2]]
->  [null,null,null,true,false,null,true,null,false]
```

## Signature

```java
class MyHashSet {

    public MyHashSet() {
        
    }

    public void add(int key) {
        
    }

    public void remove(int key) {
        
    }

    public boolean contains(int key) {
        
    }
}
```

## Target complexity

O(1) average per call.

## The skill

What a hash table **is**: an array of buckets, a hash that picks the bucket, and a
collision strategy (chaining, or open addressing). With keys only up to `10^6`, a
`boolean[1_000_001]` is also accepted and is a fine first answer — it's direct addressing,
the degenerate hash table.

The judge tests the **largest key** (`10^6`), so `new boolean[1_000_000]` is off by one, and
it tests `remove` of an absent key and removing twice.

## Hints

<details><summary>Hint 1</summary>

Chaining: `List<Integer>[] buckets` (a `List` is allowed; a `Set` isn't), bucket `key % B`.

</details>
