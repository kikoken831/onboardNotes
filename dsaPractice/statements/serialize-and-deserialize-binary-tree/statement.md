# Serialize and Deserialize Binary Tree

LeetCode 297 · Hard · https://leetcode.com/problems/serialize-and-deserialize-binary-tree/

## Brief

Design `class Codec`: `serialize(root)` turns a tree into a `String` (any format you like),
and `deserialize(data)` rebuilds the **identical** tree from it. The standard `TreeNode` (`val`, `left`, `right`) is provided.

The judge runs LeetCode's check — `new Codec().deserialize(new Codec().serialize(root))`, two
**separate** instances — and requires the result to be structurally equal to the input and
made of **new** nodes.

## Constraints

- `0 <= nodes <= 10^4`, `-1000 <= val <= 1000`

## Examples

```
[1,2,3,null,null,4,5]  ->  round-trips to [1,2,3,null,null,4,5]
[]                     ->  []
```

## Signature

```java
public class Codec {

    // Encodes a tree to a single string.
    public String serialize(TreeNode root) {

    }

    // Decodes your encoded data to tree.
    public TreeNode deserialize(String data) {

    }
}
```

## Target complexity

O(n) both ways.

## The skill

A traversal alone doesn't determine a tree; a traversal **with null markers** does. Preorder
with `#` for null, split on a delimiter, and rebuild by consuming tokens in the same order.
Negative numbers and multi-digit values rule out "one character per node" formats. Level
order with nulls (LeetCode's own format) works too.

## Hints

<details><summary>Hint 1</summary>

Deserialize with a queue of tokens and the same recursion shape as serialize.

</details>
