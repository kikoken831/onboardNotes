# Find Median from Data Stream

LeetCode 295 · Hard · https://leetcode.com/problems/find-median-from-data-stream/

## Brief

Implement `class MedianFinder`: `addNum(num)` adds a value, `findMedian()` returns the median
of everything added so far (mean of the two middles for an even count). Answers within
`10^-5` are accepted. `findMedian` is only called after at least one `addNum`.

## Constraints

- `-10^5 <= num <= 10^5`; at most `5 * 10^4` calls

## Examples

```
["MedianFinder","addNum","addNum","findMedian","addNum","findMedian"]
[[],[1],[2],[],[3],[]]
->  [null,null,null,1.5,null,2.0]
```

## Signature

```java
class MedianFinder {

    public MedianFinder() {
        
    }

    public void addNum(int num) {
        
    }

    public double findMedian() {
        
    }
}
```

## Target complexity

O(log n) `addNum`, O(1) `findMedian`.

## The skill

**Two heaps**: a max-heap for the smaller half and a min-heap for the larger half, balanced so
their sizes differ by at most one. The median is then one or both tops. The subtle part is the
invariant: route every new number *through* one heap into the other, so nothing in the low
half ever exceeds the high half. Sorting on every query is O(n log n) per call.

## Hints

<details><summary>Hint 1</summary>

`lo.add(x); hi.add(lo.poll()); if (hi.size() > lo.size()) lo.add(hi.poll());`

</details>
