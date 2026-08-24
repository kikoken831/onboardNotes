#!/usr/bin/env python3
"""
Generates problems.json, the judge's catalogue.

Run after editing any of the tables below:

    python3 tools/make_catalogue.py

Tier 0 is deliberately absent: it is a predict-the-output quiz over measured JVM
behaviour, not a set of judged problems. It lives in drills/00-java-mechanics.md.
"""

import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# Supplied id -> real LeetCode id, where the supplied list has drifted.
ID_CORRECTIONS = {
    792: 704, 1250: 1143, 1019: 977, 940: 904, 908: 876, 931: 895,
    1014: 973, 855: 828, 882: 852, 778: 767, 874: 844, 893: 863,
    800: 784, 1028: 986, 1075: 1065, 2132: 2022, 761: 759,
    745: 744, 746: 745,
}

# --------------------------------------------------------------------- tier 1
# Authored primitives. One mechanical sub-skill each.
# (slug, title, group, patterns, unlocks)
TIER1 = [
    # A. write-index / in-place
    ("p-remove-value", "Remove every occurrence of a value", "write-index",
     ["Array", "Two Pointers"], "27, 26, 283"),
    ("p-dedupe-sorted", "De-duplicate a sorted array in place", "write-index",
     ["Array", "Two Pointers"], "26, 80, 83"),
    ("p-move-zeros", "Move zeros to the back, keep order", "write-index",
     ["Array", "Two Pointers"], "283"),
    ("p-partition-even-odd", "Partition into evens then odds", "write-index",
     ["Array", "Two Pointers"], "905, 75"),
    ("p-reverse-in-place", "Reverse an array in place", "write-index",
     ["Array", "Two Pointers"], "344, 151"),
    ("p-rotate-left-k", "Rotate left by k using three reversals", "write-index",
     ["Array", "Math"], "189, 61"),
    ("p-dutch-flag", "Three-way partition (Dutch national flag)", "write-index",
     ["Array", "Two Pointers", "Sorting"], "75"),
    ("p-replace-and-remove", "Replace and remove, length changes", "write-index",
     ["Array", "Two Pointers"], "443"),

    # B. hash counting
    ("p-char-frequency", "Count characters into int[26]", "hash-counting",
     ["Hash Table", "String", "Counting"], "242, 383, 387"),
    ("p-first-unique-char", "First non-repeating character", "hash-counting",
     ["Hash Table", "String", "Counting"], "387"),
    ("p-anagram-by-counts", "Anagram check without sorting", "hash-counting",
     ["Hash Table", "String"], "242, 49"),
    ("p-group-by-key", "Group values with computeIfAbsent", "hash-counting",
     ["Hash Table"], "49"),
    ("p-two-sum-seen-map", "Complement lookup in one pass", "hash-counting",
     ["Hash Table", "Array"], "1, 167"),

    # C. binary search, the four templates
    ("p-bs-exact", "Binary search, exact match", "binary-search",
     ["Binary Search"], "704"),
    ("p-bs-first-ge", "First index with value >= x", "binary-search",
     ["Binary Search"], "35, 278"),
    ("p-bs-first-gt", "First index with value > x", "binary-search",
     ["Binary Search"], "744"),
    ("p-bs-predicate-flip", "First index where a predicate flips", "binary-search",
     ["Binary Search"], "278, 162"),
    ("p-bs-count-occurrences", "Count occurrences using both bounds", "binary-search",
     ["Binary Search"], "34"),
    ("p-bs-on-answer", "Binary search the answer, not the array", "binary-search",
     ["Binary Search"], "875, 1011, 410"),

    # D. linked-list rewiring
    ("p-list-build-traverse", "Build a list, walk it, return the values", "linked-list",
     ["Linked List"], "everything list-shaped"),
    ("p-list-reverse", "Reverse a list iteratively", "linked-list",
     ["Linked List"], "206"),
    ("p-list-delete-value", "Delete by value with a dummy head", "linked-list",
     ["Linked List"], "203, 83"),
    ("p-list-kth-from-end", "Delete the k-th node from the end", "linked-list",
     ["Linked List", "Two Pointers"], "19"),
    ("p-list-merge-sorted", "Merge two sorted lists", "linked-list",
     ["Linked List"], "21, 23, 148"),
    ("p-list-middle", "Find the middle with fast and slow pointers", "linked-list",
     ["Linked List", "Two Pointers"], "876, 234, 143"),
    ("p-list-reverse-sublist", "Reverse only positions m..n", "linked-list",
     ["Linked List"], "92, 25"),
    ("p-list-partition", "Stable partition around a value", "linked-list",
     ["Linked List", "Two Pointers"], "86, 328"),

    # E. recursion shapes
    ("p-rec-sum-return-up", "Sum by returning values up", "recursion-shape",
     ["Recursion"], "104, 543"),
    ("p-rec-depth-thread-down", "Depth by threading state down", "recursion-shape",
     ["Recursion", "Tree"], "112, 129"),
    ("p-tree-height", "Height of a binary tree", "recursion-shape",
     ["Tree", "Depth-First Search"], "104, 110"),
    ("p-tree-root-to-leaf", "Collect root-to-leaf paths", "recursion-shape",
     ["Tree", "Backtracking", "Depth-First Search"], "257, 113"),
    ("p-tree-both-directions", "Carry context down, return a result up", "recursion-shape",
     ["Tree", "Depth-First Search"], "124, 437"),
    ("p-tree-count-matching", "Count nodes satisfying a predicate", "recursion-shape",
     ["Tree", "Depth-First Search"], "938"),
    ("p-tree-is-balanced", "Balanced check with a sentinel height", "recursion-shape",
     ["Tree", "Depth-First Search"], "110"),

    # F. iterative traversal
    ("p-tree-preorder-iter", "Preorder without recursion", "iterative-traversal",
     ["Tree", "Stack"], "144"),
    ("p-tree-inorder-iter", "Inorder without recursion (left spine)", "iterative-traversal",
     ["Tree", "Stack"], "94, 230, 98"),
    ("p-tree-postorder-iter", "Postorder without recursion", "iterative-traversal",
     ["Tree", "Stack"], "145"),
    ("p-tree-level-order", "Level order with a size snapshot", "iterative-traversal",
     ["Tree", "Breadth-First Search"], "102, 103, 199, 637"),

    # G. grid search
    ("p-grid-dfs-region", "Size of one connected region", "grid-search",
     ["Depth-First Search", "Matrix"], "200, 695, 733"),
    ("p-grid-bfs-shortest", "Shortest path on an unweighted grid", "grid-search",
     ["Breadth-First Search", "Matrix"], "1091, 994"),
    ("p-grid-visited-on-enqueue", "Mark visited at enqueue, not dequeue", "grid-search",
     ["Breadth-First Search", "Matrix"], "200, 994"),
    ("p-grid-flood-fill", "Flood fill from a starting cell", "grid-search",
     ["Depth-First Search", "Matrix"], "733"),

    # H. backtracking (no LeetCode Easy exists for this pattern)
    ("p-bt-binary-strings", "Generate every binary string of length n", "backtracking",
     ["Backtracking"], "78, 22"),
    ("p-bt-include-exclude", "Subsets by include/exclude recursion", "backtracking",
     ["Backtracking", "Bit Manipulation"], "78, 90"),
    ("p-bt-subset-sum", "Does any subset reach the target?", "backtracking",
     ["Backtracking", "Dynamic Programming"], "39, 416, 494"),
    ("p-bt-subset-no-adjacent", "Subsets with no two adjacent elements", "backtracking",
     ["Backtracking"], "198, 213"),
    ("p-bt-permutations-swap", "Permutations by swapping in place", "backtracking",
     ["Backtracking"], "46, 47"),
    ("p-bt-combinations", "Choose k of n, copying at the leaf", "backtracking",
     ["Backtracking"], "77, 39, 216"),

    # I. trie (no LeetCode Easy exists)
    ("p-trie-insert-search", "Insert and search with a 26-child trie", "trie",
     ["Trie", "Design"], "208"),
    ("p-trie-prefix-count", "Count words sharing a prefix", "trie",
     ["Trie"], "208, 1268"),
    ("p-trie-collect-prefix", "Collect every word under a prefix", "trie",
     ["Trie", "Depth-First Search"], "211, 212, 720"),

    # J. union-find (no LeetCode Easy exists)
    ("p-dsu-naive", "Find and union with no optimisation", "union-find",
     ["Union-Find"], "547, 323"),
    ("p-dsu-optimised", "Path compression and union by size", "union-find",
     ["Union-Find"], "684, 721, 128"),
    ("p-dsu-count-components", "Count connected components", "union-find",
     ["Union-Find", "Graph Theory"], "323, 547, 261"),

    # K. topological sort (no LeetCode Easy exists)
    ("p-topo-kahn", "Kahn's algorithm with in-degrees", "topological-sort",
     ["Topological Sort", "Breadth-First Search"], "207, 210"),
    ("p-topo-dfs-cycle", "DFS post-order topo sort with cycle detection", "topological-sort",
     ["Topological Sort", "Depth-First Search"], "207, 210, 269"),

    # L. monotonic stack
    ("p-mono-next-greater", "Next greater element to the right", "monotonic-stack",
     ["Stack", "Monotonic Stack"], "496, 739"),
    ("p-mono-prev-smaller", "Previous smaller element to the left", "monotonic-stack",
     ["Stack", "Monotonic Stack"], "84, 42"),
    ("p-mono-sentinel", "Flush a monotonic stack with a sentinel", "monotonic-stack",
     ["Stack", "Monotonic Stack"], "84, 42, 654"),

    # M. prefix sum
    ("p-prefix-build-query", "Prefix sums with the +1 offset", "prefix-sum",
     ["Prefix Sum", "Array"], "303, 1480"),
    ("p-prefix-count-target", "Count subarrays summing to k", "prefix-sum",
     ["Prefix Sum", "Hash Table"], "560, 437"),
    ("p-prefix-2d", "Two-dimensional prefix sums", "prefix-sum",
     ["Prefix Sum", "Matrix"], "304"),

    # N. heap
    ("p-heap-k-smallest", "K smallest with a bounded max-heap", "heap",
     ["Heap (Priority Queue)"], "215, 973, 347"),
    ("p-heap-comparator", "PriorityQueue over int[] with a comparator", "heap",
     ["Heap (Priority Queue)"], "973, 253, 1046"),
    ("p-heap-merge-k", "Merge k sorted arrays with a heap", "heap",
     ["Heap (Priority Queue)", "Merge Sort"], "23, 373, 632"),

    # O. 1-D DP
    ("p-dp-fib-memo-then-table", "Memoise, then tabulate", "dynamic-programming",
     ["Dynamic Programming", "Memoization"], "509, 70, 1137"),
    ("p-dp-climb-cost", "Minimum cost to climb", "dynamic-programming",
     ["Dynamic Programming"], "746, 70, 198"),
    ("p-dp-knapsack-bool", "Reachable-sum knapsack table", "dynamic-programming",
     ["Dynamic Programming", "0-1 Knapsack"], "416, 494, 698"),
    ("p-dp-lis-quadratic", "Longest increasing subsequence, O(n^2)", "dynamic-programming",
     ["Dynamic Programming", "Longest Increasing Subsequence"], "300, 673"),
]

# --------------------------------------------------------------------- tier 2
# Real LeetCode Easy sitting below the supplied set's floor.
# Chosen by acceptance rate rather than reputation; the traps (605, 941, 414,
# 1346, 168) are deliberately excluded.
# (lc_id, slug, title, patterns)
TIER2 = [
    (1929, "concatenation-of-array", "Concatenation of Array", ["Array"]),
    (1920, "build-array-from-permutation", "Build Array from Permutation", ["Array", "Simulation"]),
    (1470, "shuffle-the-array", "Shuffle the Array", ["Array"]),
    (1431, "kids-with-the-greatest-number-of-candies", "Kids With the Greatest Number of Candies", ["Array"]),
    (344, "reverse-string", "Reverse String", ["Two Pointers", "String", "Recursion"]),
    (26, "remove-duplicates-from-sorted-array", "Remove Duplicates from Sorted Array", ["Array", "Two Pointers"]),
    (27, "remove-element", "Remove Element", ["Array", "Two Pointers"]),
    (88, "merge-sorted-array", "Merge Sorted Array", ["Array", "Two Pointers", "Sorting"]),
    (905, "sort-array-by-parity", "Sort Array By Parity", ["Array", "Two Pointers", "Sorting"]),

    (771, "jewels-and-stones", "Jewels and Stones", ["Hash Table", "String"]),
    (1512, "number-of-good-pairs", "Number of Good Pairs", ["Array", "Hash Table", "Counting"]),
    (349, "intersection-of-two-arrays", "Intersection of Two Arrays", ["Array", "Hash Table", "Sorting"]),
    (383, "ransom-note", "Ransom Note", ["Hash Table", "String", "Counting"]),
    (387, "first-unique-character-in-a-string", "First Unique Character in a String", ["Hash Table", "String", "Counting"]),
    (1365, "how-many-numbers-are-smaller-than-the-current-number", "How Many Numbers Are Smaller Than the Current Number", ["Array", "Hash Table", "Counting"]),

    (1768, "merge-strings-alternately", "Merge Strings Alternately", ["Two Pointers", "String"]),
    (409, "longest-palindrome", "Longest Palindrome", ["Hash Table", "String", "Greedy"]),
    (1678, "goal-parser-interpretation", "Goal Parser Interpretation", ["String", "Simulation"]),
    (2114, "maximum-number-of-words-found-in-sentences", "Maximum Number of Words Found in Sentences", ["Array", "String"]),

    (1480, "running-sum-of-1d-array", "Running Sum of 1d Array", ["Array", "Prefix Sum"]),
    (724, "find-pivot-index", "Find Pivot Index", ["Array", "Prefix Sum"]),

    (35, "search-insert-position", "Search Insert Position", ["Array", "Binary Search"]),
    (374, "guess-number-higher-or-lower", "Guess Number Higher or Lower", ["Binary Search", "Interactive"]),
    (441, "arranging-coins", "Arranging Coins", ["Math", "Binary Search"]),
    (69, "sqrtx", "Sqrt(x)", ["Math", "Binary Search"]),
    (367, "valid-perfect-square", "Valid Perfect Square", ["Math", "Binary Search"]),

    (682, "baseball-game", "Baseball Game", ["Array", "Stack", "Simulation"]),
    (232, "implement-queue-using-stacks", "Implement Queue using Stacks", ["Stack", "Design", "Queue"]),
    (225, "implement-stack-using-queues", "Implement Stack using Queues", ["Stack", "Design", "Queue"]),
    (1047, "remove-all-adjacent-duplicates-in-string", "Remove All Adjacent Duplicates In String", ["String", "Stack"]),
    (933, "number-of-recent-calls", "Number of Recent Calls", ["Design", "Queue", "Data Stream"]),

    (94, "binary-tree-inorder-traversal", "Binary Tree Inorder Traversal", ["Stack", "Tree", "Depth-First Search"]),
    (144, "binary-tree-preorder-traversal", "Binary Tree Preorder Traversal", ["Stack", "Tree", "Depth-First Search"]),
    (145, "binary-tree-postorder-traversal", "Binary Tree Postorder Traversal", ["Stack", "Tree", "Depth-First Search"]),
    (543, "diameter-of-binary-tree", "Diameter of Binary Tree", ["Tree", "Depth-First Search"]),
    (110, "balanced-binary-tree", "Balanced Binary Tree", ["Tree", "Depth-First Search"]),
    (101, "symmetric-tree", "Symmetric Tree", ["Tree", "Depth-First Search", "Breadth-First Search"]),
    (700, "search-in-a-binary-search-tree", "Search in a Binary Search Tree", ["Tree", "Binary Search Tree"]),
    (938, "range-sum-of-bst", "Range Sum of BST", ["Tree", "Depth-First Search", "Binary Search Tree"]),

    (463, "island-perimeter", "Island Perimeter", ["Array", "Depth-First Search", "Matrix"]),
    (733, "flood-fill", "Flood Fill", ["Array", "Depth-First Search", "Breadth-First Search", "Matrix"]),
    (1971, "find-if-path-exists-in-graph", "Find if Path Exists in Graph", ["Depth-First Search", "Breadth-First Search", "Union-Find", "Graph Theory"]),
    (997, "find-the-town-judge", "Find the Town Judge", ["Array", "Hash Table", "Graph Theory"]),

    (455, "assign-cookies", "Assign Cookies", ["Array", "Greedy", "Sorting"]),
    (860, "lemonade-change", "Lemonade Change", ["Array", "Greedy"]),

    (1046, "last-stone-weight", "Last Stone Weight", ["Array", "Heap (Priority Queue)"]),
    (703, "kth-largest-element-in-a-stream", "Kth Largest Element in a Stream", ["Design", "Heap (Priority Queue)", "Data Stream"]),

    (832, "flipping-an-image", "Flipping an Image", ["Array", "Two Pointers", "Matrix"]),
    (867, "transpose-matrix", "Transpose Matrix", ["Array", "Matrix", "Simulation"]),
    (566, "reshape-the-matrix", "Reshape the Matrix", ["Array", "Matrix", "Simulation"]),
    (1672, "richest-customer-wealth", "Richest Customer Wealth", ["Array", "Matrix"]),

    (1603, "design-parking-system", "Design Parking System", ["Design", "Counting", "Simulation"]),
    (705, "design-hashset", "Design HashSet", ["Array", "Hash Table", "Design"]),
    (706, "design-hashmap", "Design HashMap", ["Array", "Hash Table", "Design"]),

    (509, "fibonacci-number", "Fibonacci Number", ["Math", "Dynamic Programming", "Memoization"]),
    (1137, "n-th-tribonacci-number", "N-th Tribonacci Number", ["Math", "Dynamic Programming", "Memoization"]),
    (746, "min-cost-climbing-stairs", "Min Cost Climbing Stairs", ["Array", "Dynamic Programming"]),

    (231, "power-of-two", "Power of Two", ["Math", "Bit Manipulation", "Recursion"]),
]
