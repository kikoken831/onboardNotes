# Tier 0 — Java mechanics

Not algorithms. Twenty pieces of JVM behaviour that produce **silently wrong answers**
in a DSA interview, every one of them measured on this machine's JDK 21.0.11 rather
than recalled.

## How to use this

Predict the output **before** running. Write your guesses down — the point is to find
the ones you were confident and wrong about, and you cannot do that from memory after
seeing the answer.

```bash
cd drills/00-java-mechanics
javac G.java && java G
javac H.java && java H
```

Then come back and check the table.

---

## The traps

### 1. `ArrayDeque` used as a stack

The one most likely to cost you an interview, because it fails **silently** — no
exception, just a queue pretending to be a stack.

```java
Deque<Integer> d = new ArrayDeque<>();
d.add(1); d.add(2); d.add(3);
d.pop();     // 1   <- FIFO. You wanted 3.
```

`add` is `addLast`; `pop` is `removeFirst`. Together they are a queue.

```java
d.push(1); d.push(2); d.push(3);
d.pop();     // 3   <- correct
```

**The rule:** Queue = **OPP** (`offer` / `poll` / `peek`). Stack = **PPP**
(`push` / `pop` / `peek`). Never mix families on one `Deque`.

Iteration order differs too — `ArrayDeque` after three pushes iterates `[3, 2, 1]`,
but `java.util.Stack` iterates `[1, 2, 3]`, bottom-first. Printing a `Stack` to debug
shows it upside down relative to how you think about it.

> This exact bug appears in `emanuele-em/java-cheatsheet` (61★ on GitHub, derived from a
> LeetCode discuss study guide) under the literal heading *"ArrayDeque implementation
> (stack)"*. Widely circulated reference material is wrong about this. Verify idioms;
> do not inherit them.

### 2. Comparator subtraction overflows

```java
Comparator<Integer> bad = (a, b) -> a - b;
bad.compare(Integer.MIN_VALUE, 1);   // +2147483647   <- wrong sign
Integer.compare(Integer.MIN_VALUE, 1);   // -1        <- correct
```

`a - b` is fine while values are small and catastrophic when they are not. Always
`Integer.compare`. The same applies to `(a, b) -> a[0] - b[0]` on coordinate pairs,
which is the form you will actually reach for in K-closest-points.

### 3. `%` on negatives

```java
-7 % 3              // -1   (Python gives 2)
Math.floorMod(-7,3) //  2
-7 / 3              // -2
Math.floorDiv(-7,3) // -3
```

Java truncates toward zero; Python floors. **The top source of bugs when porting a
Python solution**, and it silently corrupts any hash-bucket or cyclic-index arithmetic.

### 4. `(lo + hi) / 2` overflows

```java
int lo = Integer.MAX_VALUE - 3, hi = Integer.MAX_VALUE - 1;
(lo + hi) / 2        // -3            <- negative index
lo + (hi - lo) / 2   // 2147483645    <- correct
```

This bug lived in the JDK's own `Arrays.binarySearch` for roughly nine years before
Joshua Bloch wrote it up in 2006. Always `lo + (hi - lo) / 2`, or `(lo + hi) >>> 1`.

### 5. `Integer` caching and `==`

```java
Integer a = 127, b = 127;  a == b   // true   (cached -128..127)
Integer c = 128, d = 128;  c == d   // false
c.equals(d)                         // true
```

Small test values hide it. `Long` behaves the same way, and `Map<Long,V>.get(1)` with
an `int` literal returns `null` because `Integer(1)` never equals `Long(1L)`.

### 6. Unboxing `null`

```java
Map<String,Integer> m = new HashMap<>();
int v = m.get("missing");            // NullPointerException
int w = m.getOrDefault("missing",0); // 0
```

### 7. `List<Integer>.remove(int)` removes by index

```java
List<Integer> l = new ArrayList<>(List.of(10,20,30));
l.remove(1);                    // [10, 30]  <- removed INDEX 1
l.remove(Integer.valueOf(20));  // removes the VALUE 20
```

### 8. `String.split` takes a regex

```java
"a.b.c".split(".")      // length 0   <- '.' matches everything
"a.b.c".split("\\.")    // length 3
"a,,b,,".split(",")     // length 3   <- trailing empties dropped
"a,,b,,".split(",", -1) // length 5
```

### 9. `Arrays.asList` on a primitive array

```java
Arrays.asList(new int[]{1,2,3}).size();   // 1  <- it's a List<int[]>
Arrays.stream(a).boxed().toList();        // the actual conversion
list.stream().mapToInt(Integer::intValue).toArray();   // and back
```

### 10. Arrays in hash containers use identity

```java
Set<int[]> s = new HashSet<>();
s.add(new int[]{1,2}); s.add(new int[]{1,2});
s.size();                              // 2   <- identity hash

Set<List<Integer>> t = new HashSet<>();  // ... this one gives 1
new int[]{1,2}.equals(new int[]{1,2})  // false
Arrays.equals(...)                     // true
```

Directly relevant to de-duplicating triplets in 3Sum. Use `List<Integer>` as the key,
or a string key, never `int[]`.

### 11. 2-D arrays are arrays of references

```java
int[][] g = new int[3][3];
Arrays.fill(g[0], 7);   // fills ONE row
g.clone();              // SHALLOW — rows are shared
new int[3][];           // rows are null
```

### 12. `substring` is an O(n) copy

Since Java 7u6 `substring` copies rather than sharing the backing array. Measured:
1,000,000 calls to `substring(1)` on a 2 MB string took **38,193 ms**.

The recursive `f(s.substring(1))` idiom — which CodingBat teaches — is therefore
**O(n²)** and will time out. Pass an index instead: `f(s, i + 1)`.

### 13. String concatenation in a loop

`s += "x"` 40,000 times took **60 ms**; `StringBuilder` took **1 ms**. About 60×.

### 14. Default recursion depth

`StackOverflowError` at roughly **45,817** trivial frames. Real DFS frames are much
larger, so a depth-10⁵ recursion on a degenerate tree or a path-shaped graph is a
genuine risk, not a theoretical one.

### 15. No comparator overload for primitives

```java
Arrays.sort(int[], cmp)   // does not compile
```

To sort descending you must box: `Integer[]` plus `Comparator.reverseOrder()`.

### 16. `ConcurrentModificationException`

For-each plus `remove` throws. So does a **nested `computeIfAbsent`** that touches the
same map — which looks natural and is a bug.

### 17. `TreeMap` / `TreeSet` navigation — Java's advantage

```java
floorKey(25)    // 20   greatest key <= 25
ceilingKey(25)  // 30   least key >= 25
higherKey(20)   // 30   strictly greater
subSet(2, 9)    // range view
```

Python has no stdlib equivalent. Candidates who learned patterns from Python solutions
hand-roll these. Know they exist — they collapse several interval and
sliding-window-median problems.

---

## One claim that did NOT reproduce

The commonly repeated *"an inconsistent comparator throws
`IllegalArgumentException: Comparison method violates its general contract!`"*.

Tested with an antisymmetry-violating comparator `(u,v) -> 1` on `Integer[]` at
n = 40, 100, 1,000 and 100,000 on JDK 21: **nothing was thrown**. TimSort's check fires
only on specific merge patterns, not on inconsistency as such.

Worth stating plainly because the claim circulates as fact. If an interviewer asserts
it, the accurate answer is "TimSort *can* detect it, but it is not guaranteed to."

---

## What to do with this

These do not need re-solving on a schedule; read them once, run both harnesses, and
come back when something behaves strangely. The ones genuinely worth committing to
memory are **1** (Deque), **2** (comparator overflow), **3** (negative modulo) and
**4** (binary-search overflow) — those four produce wrong answers rather than
exceptions, which means the judge catches them but an interviewer might not, and you
will not notice either.
