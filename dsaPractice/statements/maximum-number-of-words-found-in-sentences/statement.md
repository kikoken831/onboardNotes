# Maximum Number of Words Found in Sentences

LeetCode 2114 · Easy · https://leetcode.com/problems/maximum-number-of-words-found-in-sentences/

## Brief

Each sentence is words separated by **single** spaces, with no leading or trailing space.
Return the largest word count among the sentences.

## Constraints

- `1 <= sentences.length <= 100`
- `1 <= sentences[i].length <= 100`

## Examples

```
["alice and bob love leetcode","i think so too","this is great thanks very much"]  ->  6
["please wait","continue to fight","continue to win"]                                ->  3
```

## Signature

```java
public int mostWordsFound(String[] sentences)
```

## Target complexity

O(total characters).

## The skill

Reading the constraints for what they license: "single spaces, no leading or trailing"
means **words = spaces + 1**. No `split` needed. `split(" ")` also works — and allocates an
array per sentence to count it.

## Hints

<details><summary>Hint 1</summary>

Count the spaces.

</details>
