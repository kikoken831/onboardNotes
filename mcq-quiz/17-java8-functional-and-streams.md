# MCQ 17 — Java 8 Functional & Streams

18 questions. Source: [`quiz/17`](../quiz/17-java8-functional-and-streams.md).

---

### Q1. Inside a lambda, what does `this` refer to?

- **A.** The lambda instance itself
- **B.** `null` — lambdas have no `this`
- **C.** A synthetic wrapper object
- **D.** The **enclosing** instance

<details><summary>Answer</summary>

**D.** This is the sharpest behavioural difference from an anonymous inner class, where `this` is the
anonymous instance. A lambda is not a new scope for `this` — it's lexically scoped, like the surrounding
method.

The other real differences: a lambda produces **no separate `.class` file** (an anonymous class produces
`Outer$1.class`); it's linked lazily at first execution by **`invokedynamic`** +
`LambdaMetafactory`; a non-capturing lambda can be **cached as a singleton**, whereas
`new Runnable(){}` allocates every time; and a lambda cannot have fields or extra state.

Practical consequence: an anonymous inner class holds the implicit outer reference *plus* is a distinct
class — a capturing lambda holds the captured values. Both can leak, which is exactly what
`gc-labs/L02Leak.java` demonstrates with a `List<Runnable>` of capturing lambdas.

→ `quiz/17` Q1
</details>

---

### Q2. What does `@FunctionalInterface` do?

- **A.** Nothing at runtime — it's a **compile-time assertion** that the interface has exactly one abstract method
- **B.** It enables lambda syntax for the interface
- **C.** It makes the interface's method implicitly `static`
- **D.** It generates the `invokedynamic` call site

<details><summary>Answer</summary>

**A.** It's documentation with teeth: the compiler errors if you add a second abstract method. Its value
is protecting your *callers* — without it, someone can silently break every lambda at every call site by
adding a method.

**B is the common misconception.** Lambdas work on *any* interface with a single abstract method,
annotated or not — which is why `Runnable`, `Comparator` and `Callable` all worked with lambdas from day
one despite predating the annotation.

`default` and `static` methods don't count against the one-abstract-method rule, nor do public methods
of `Object` (`equals`, `hashCode`, `toString`) — which is why `Comparator` still qualifies.

→ `quiz/17` Q2
</details>

---

### Q3. Which interface has the shape `() -> T`?

- **A.** `Consumer<T>`
- **B.** `Function<T,R>`
- **C.** `Supplier<T>`
- **D.** `Predicate<T>`

<details><summary>Answer</summary>

**C.** The four core shapes:

| Interface | Method | Shape | Use |
|---|---|---|---|
| `Predicate<T>` | `test` | `T -> boolean` | `filter` |
| `Consumer<T>` | `accept` | `T -> void` | `forEach` |
| `Supplier<T>` | `get` | `() -> T` | **lazy** values, `orElseGet` |
| `Function<T,R>` | `apply` | `T -> R` | `map` |

`Supplier` is the one that carries a design idea rather than just a signature: it is **deferred
computation**. That's why `orElseGet(this::expensiveDefault)` beats
`orElse(expensiveDefault())` — the latter evaluates eagerly, every call, even when the `Optional` is
present.

Also worth naming: `UnaryOperator<T>` = `Function<T,T>`, `BinaryOperator<T>` = `BiFunction<T,T,T>`, and
the primitive specialisations (`IntPredicate`, `ToIntFunction`, …) that exist purely to dodge boxing.

→ `quiz/17` Q3
</details>

---

### Q4. Why is there no `BiSupplier`?

- **A.** It exists, but only as `Supplier2`
- **B.** A supplier takes **no** arguments, so there's nothing to have two of
- **C.** It was removed in JDK 9
- **D.** Because it would clash with `BiFunction`

<details><summary>Answer</summary>

**B.** The `Bi-` prefix means *two inputs*. `Supplier` has zero inputs, so `BiSupplier` is meaningless —
and a "two-output" function would just return a tuple or record.

The `Bi-` family that does exist: `BiPredicate<T,U>` (`(T,U) -> boolean`), `BiConsumer<T,U>`
(`(T,U) -> void`, which is what `Map.forEach` takes), `BiFunction<T,U,R>` (`(T,U) -> R`, which is what
`Map.merge` and `CompletableFuture.thenCombine` take).

There is also no `TriFunction` — beyond two parameters the JDK expects you to pass a record, which is
usually the better design anyway.

→ `quiz/17` Q4
</details>

---

### Q5. `String::length` is which kind of method reference?

- **A.** Static method reference
- **B.** Bound instance method reference
- **C.** **Unbound** instance method reference — the receiver becomes the first parameter
- **D.** Constructor reference

<details><summary>Answer</summary>

**C.** `String::length` is a `Function<String,Integer>`: the instance you'd call it *on* arrives as the
argument. Equivalent to `s -> s.length()`.

The four kinds:

| Kind | Example | Equivalent lambda |
|---|---|---|
| Static | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| **Bound** instance | `System.out::println` | `x -> System.out.println(x)` |
| **Unbound** instance | `String::length` | `s -> s.length()` |
| Constructor | `ArrayList::new` | `() -> new ArrayList<>()` |

Bound vs unbound is the distinction people miss, and it has a real consequence: a **bound** reference
captures the receiver *at the point the reference is created*, so
`someList::add` pins `someList` for as long as the reference lives.

→ `quiz/17` Q5
</details>

---

### Q6. `filter` vs `map` — which describes them?

- **A.** `filter` keeps the element type and may reduce the count; `map` keeps the count and may change the type
- **B.** Both may change count and type
- **C.** `filter` changes the type; `map` reduces the count
- **D.** `map` is lazy, `filter` is eager

<details><summary>Answer</summary>

**A.** `filter(Predicate<T>) → Stream<T>`: same type, 0..n elements out of n.
`map(Function<T,R>) → Stream<R>`: possibly new type, exactly n out of n.

`flatMap` is the third one, and it's the one worth being able to explain: it maps each element to a
**stream** and concatenates the results, so it's the only one of the three that changes *both* type and
count. It's how you flatten `List<Order>` → all their `items`, or unwrap
`Stream<Optional<T>>` → `Stream<T>`.

**D** is wrong in both directions — *all* intermediate operations are lazy.

→ `quiz/17` Q6
</details>

---

### Q7. `list.stream().peek(System.out::println).filter(x -> x > 5);` prints what?

- **A.** Every element
- **B.** Only the elements greater than 5
- **C.** Every element, but in unspecified order
- **D.** **Nothing** — there is no terminal operation

<details><summary>Answer</summary>

**D.** No terminal operation means the pipeline never runs. The whole statement is a no-op.

That is `peek`'s first trap. The second is worse and survives adding a terminal op: **`peek` may be
skipped even in a running pipeline.** The Javadoc says the action may be elided if the element count can
be computed without traversal — so
`list.stream().peek(log).count()` can legitimately print nothing on JDK 9+.

`peek` is intended for **debugging only**. For a real side effect use `forEach` (a terminal op that
always runs); to observe *and* keep the stream, prefer `map(x -> { log(x); return x; })`, which cannot be
elided.

`forEach` vs `forEachOrdered`: on a parallel stream `forEach` does **not** preserve encounter order.

→ `quiz/17` Q7
</details>

---

### Q8. What does stream laziness actually buy you?

- **A.** Thread safety
- **B.** **Short-circuiting and single-pass fusion** — work is only done for elements that reach the terminal op
- **C.** Lower memory because streams cache results
- **D.** Automatic parallelism

<details><summary>Answer</summary>

**B.** Two concrete wins. **Fusion:** `filter` then `map` doesn't build an intermediate list — each
element flows through the whole chain once. **Short-circuiting:** `findFirst`, `anyMatch`, `limit` stop
consuming as soon as the answer is known, so
`hugeList.stream().filter(expensive).findFirst()` may call `expensive` once.

It's also what makes **infinite** streams usable: `Stream.iterate(1, x -> x*2).limit(10)`.

The costs to be honest about: a stream is **single-use** (a second terminal op throws
`IllegalStateException`), the *source* is read lazily so mutating it mid-pipeline is undefined, and
exceptions surface at the terminal op rather than where the lambda was written — which makes stack
traces harder to read.

→ `quiz/17` Q8
</details>

---

### Q9. Why prefer `orElseGet(this::loadDefault)` over `orElse(loadDefault())`?

- **A.** `orElse` throws if the value is present
- **B.** `orElse` doesn't accept null
- **C.** `orElse` can't be chained
- **D.** `orElse`'s argument is evaluated **eagerly**, even when the `Optional` is present

<details><summary>Answer</summary>

**D.** `orElse` takes a *value*, so `loadDefault()` runs on every call — including the common case where
it's thrown away. `orElseGet` takes a `Supplier` and only invokes it when empty. With a DB call or a
remote fetch as the default, that's a real, easily-missed cost.

The rest of correct `Optional` use: chain with `map`/`flatMap`/`filter`; terminate with
`orElse`/`orElseGet`/`orElseThrow`/`ifPresent`. Never `if (o.isPresent()) return o.get();` — that's the
null check you were trying to escape, with more syntax. Bare `get()` is now aliased as `orElseThrow()`
precisely because it reads as safe and isn't.

→ `quiz/17` Q9
</details>

---

### Q10. Which is the correct use of `Optional`?

- **A.** As a **return type** for "may legitimately be absent"
- **B.** As a field type, to make nullability explicit in the model
- **C.** As a method parameter, so callers needn't null-check
- **D.** As a collection element type

<details><summary>Answer</summary>

**A.** Optional was designed as a **return type**, and its author (Brian Goetz) has said so explicitly.

Why the others are wrong:
- **B** — `Optional` is **not `Serializable`**, and it adds an allocation per field. Use a nullable field
  with `@Nullable`, or model absence in the type.
- **C** — an `Optional` parameter gives callers *three* states to pass (`null`, empty, present), which is
  worse than two. Overload the method instead.
- **D** — an empty collection already means "nothing here"; `List<Optional<T>>` is noise. **Never return
  an empty `Optional` wrapping a collection** — return the empty collection.

And it's not free: it's an extra object, it doesn't work with primitives without
`OptionalInt`/`OptionalLong`/`OptionalDouble`, and in a hot loop it's measurable allocation pressure.
That's why the JDK's own hot paths still return `null`.

→ `quiz/17` Q10
</details>

---

### Q11. The biggest practical problem with `parallelStream()`?

- **A.** It's not thread-safe
- **B.** It runs on the **shared common `ForkJoinPool`**, so one blocking task can starve every other user in the JVM
- **C.** It always produces results out of order
- **D.** It's slower than sequential in all cases

<details><summary>Answer</summary>

**B.** Every `parallelStream()` in the process shares `ForkJoinPool.commonPool()`, sized to
`availableProcessors() - 1`. One I/O-blocking parallel stream stalls unrelated code — including other
libraries' — and there is **no way to pass an executor** to `parallelStream()`. The only workaround is
submitting the whole pipeline to your own `ForkJoinPool`.

The other conditions it needs to actually be faster: a large N, a cheap **splittable** source
(`ArrayList` and arrays split well; `LinkedList` and `Iterator` sources don't), a stateless
non-blocking lambda, and no ordering constraint — `findFirst`, `limit` and `forEachOrdered` all impose
coordination that eats the win.

**C** overstates it: `collect(toList())` *does* preserve encounter order. It's `forEach` that doesn't.

Rule of thumb: default to sequential; parallelise only with a measurement in hand.

→ `quiz/17` Q11
</details>

---

### Q12. `collect()` is best described as?

- **A.** A shortcut for `reduce`
- **B.** A terminal operation that returns an immutable view
- **C.** A **mutable reduction** — supplier, accumulator, combiner
- **D.** A lazy operation that materialises on first access

<details><summary>Answer</summary>

**C.** The three functions are the whole idea: **supplier** creates a fresh container, **accumulator**
folds one element into it, **combiner** merges two containers (only used in parallel).

The contrast with `reduce` is the point: `reduce` is *immutable* reduction — each step returns a new
value, so `reduce("", String::concat)` is O(n²). `collect` mutates a container, so
`collect(joining())` is O(n).

Common collectors: `toList`, `toSet`, `toMap`, `joining`, `groupingBy`, `partitioningBy`, `counting`,
`summingInt`, `averagingDouble`, `teeing` (JDK 12+).

Two gotchas: **`Collectors.toMap` throws `IllegalStateException` on a duplicate key** — pass a merge
function — and it **NPEs on a null value**, unlike `toList`. Be precise on that second one if pushed: the
NPE comes from a `requireNonNull` in the accumulator, and is *not* in `toMap`'s documented contract; only
`toUnmodifiableMap` documents the prohibition. Null **keys** are accepted by `toMap`, because the backing
map is a `HashMap`. JDK 16's `stream.toList()` is shorter than `collect(toList())` and returns an
*unmodifiable* list; `collect(toList())` returns a mutable `ArrayList`.

→ `quiz/17` Q12
</details>

---

### Q13. Which `Collector` characteristic lets the pipeline skip the finisher?

- **A.** `CONCURRENT`
- **B.** `IDENTITY_FINISH`
- **C.** `UNORDERED`
- **D.** `PARALLEL_SAFE`

<details><summary>Answer</summary>

**B.** `IDENTITY_FINISH` declares that the accumulation container *is* the result, so the framework can
cast instead of calling `finisher()`. That's why `toList` is cheap.

The other two real ones: **`UNORDERED`** says the result doesn't depend on encounter order (`toSet`),
allowing more parallel freedom; **`CONCURRENT`** says a *single* container can be shared across threads,
so the combiner is never needed — but it's only honoured when the collector is *also* `UNORDERED` (or
the stream is), which is the subtlety behind `groupingByConcurrent`.

The five methods: `supplier`, `accumulator`, `combiner`, `finisher`, `characteristics`. Use
`Collector.of(...)` for a custom one — but reach for composition
(`groupingBy(f, mapping(g, toList()))`) first; a hand-written collector is rarely warranted.

→ `quiz/17` Q13
</details>

---

### Q14. `partitioningBy` differs from `groupingBy` how?

- **A.** It always returns a map with **both** `true` and `false` keys, even when one side is empty
- **B.** It returns a `List` rather than a `Map`
- **C.** It's parallel-safe and `groupingBy` isn't
- **D.** It permits more than two partitions

<details><summary>Answer</summary>

**A.** `partitioningBy(Predicate)` returns `Map<Boolean, List<T>>` with **both keys always present**.
`groupingBy(pred)` on the same predicate gives you a map that may be missing a key entirely — which is
exactly the difference that produces a surprise `null` downstream.

It's also faster: it uses a fixed two-slot implementation rather than a `HashMap`.

Both take a **downstream collector**, which is where the real power is:
```java
groupingBy(Trade::getCurrency, summingDouble(Trade::getNotional))
groupingBy(Trade::getBook, mapping(Trade::getId, toList()))
groupingBy(Trade::getStatus, counting())
```
And `groupingBy` can nest — `groupingBy(a, groupingBy(b))` — for a multi-level breakdown.

→ `quiz/17` Q14
</details>

---

### Q15. Why prefer `IntStream` over `Stream<Integer>`?

- **A.** `IntStream` is parallel by default
- **B.** `Stream<Integer>` can't be sorted
- **C.** **No boxing** — plus `sum()`/`average()`/`max()` exist directly on it
- **D.** `IntStream` is lazy and `Stream<Integer>` is eager

<details><summary>Answer</summary>

**C.** `Stream<Integer>` boxes every element: an allocation each, poor cache locality, and a pointer
chase per access. `IntStream` carries primitives.

The API difference is just as practical — `sum()`, `average()`, `max()`, `summaryStatistics()` exist on
`IntStream` and have no equivalent on `Stream<Integer>` without a collector.

Conversions: `stream.mapToInt(T::getX)`, `intStream.boxed()`, `intStream.mapToObj(...)`. Three exist:
`IntStream`, `LongStream`, `DoubleStream` — none for `char`, `short`, `float`.

This is the same allocation-pressure theme as `quiz/09` (Scala boxing, tuples, eager interpolation) and
`gc-labs/L04`: the cheapest GC tuning is **not allocating**.

→ `quiz/17` Q15
</details>

---

### Q16. Multi-key sort, descending on the second key — which is right?

- **A.** `Comparator.comparing(T::a).thenComparing(T::b, Comparator.reverseOrder())`
- **B.** `Comparator.comparing(T::a).thenComparing(T::b).reversed()`
- **C.** `Comparator.comparing(T::a, T::b).reversed()`
- **D.** `Comparator.reverseOrder(T::a, T::b)`

<details><summary>Answer</summary>

**A.** **`reversed()` reverses the entire comparator built so far**, not just the last key — that's the
classic bug, and it's why **B** is wrong. To reverse one key only, pass a comparator to that
`thenComparing`.

Other essentials: `comparingInt`/`comparingLong`/`comparingDouble` avoid boxing the key;
`nullsFirst`/`nullsLast` wrap a comparator to tolerate nulls.

And the contract, which matters because violating it can throw
`IllegalArgumentException: Comparison method violates its general contract!` from TimSort: it must be
**antisymmetric, transitive, and consistent**. Never write `(a,b) -> a.x - b.x` on ints — it overflows.
Use `Integer.compare`.

Also: `compareTo` (natural order, on the class) vs `compare` (external, in a `Comparator`) — and
`equals`/`compareTo` consistency matters for `TreeSet`, which dedupes by `compareTo == 0`, not `equals`.

→ `quiz/17` Q16
</details>

---

### Q17. `Arrays.parallelSort()` — when does it help?

- **A.** Always; it's strictly better
- **B.** Only on primitive arrays
- **C.** Above roughly 8192 elements — below that it falls back to sequential anyway
- **D.** Only when the array is already partially sorted

<details><summary>Answer</summary>

**C.** `parallelSort` uses a parallel merge sort on the **common `ForkJoinPool`**, with a threshold of
8192 (`MIN_ARRAY_SORT_GRAN`) below which it just calls the sequential sort. It also needs **O(n) extra
space** for the merge (allocated via `Array.newInstance` on the object path).

One nuance worth having, since the 8192 number gets quoted as universal: **it only gates the object-array
overloads** (`Comparable`/`Comparator`). Since the JDK 14 `DualPivotQuicksort` rewrite, the *primitive*
overloads such as `parallelSort(int[])` no longer branch on the threshold at all — they delegate
unconditionally, passing the common pool's parallelism, and the sort decides internally.

Same shared-pool caveat as `parallelStream`, and the same conclusion: measure.

Worth knowing about `Arrays.sort` itself — it uses **two different algorithms**: dual-pivot quicksort
for primitives (in-place, unstable, fine because primitives are indistinguishable when equal) and
**TimSort** for objects (stable, O(n) on nearly-sorted input, which is why a broken comparator throws
there).

→ `quiz/17` Q17
</details>

---

### Q18. Which is the strongest reason *not* to use a stream?

- **A.** Streams are always slower
- **B.** The logic needs **`break`/`continue` with an index, mutation of locals, or checked exceptions**
- **C.** The team doesn't know streams
- **D.** Streams can't be unit tested

<details><summary>Answer</summary>

**B.** These are hard limits, not preferences. A lambda can't assign to an enclosing local (captured
variables are effectively final); a lambda can't throw a checked exception without a wrapper interface;
and index-based early exit reads far worse as a stream than as a loop.

The other legitimate reasons: **primitive hot loops** (a plain `for` over an `int[]` avoids the pipeline
overhead entirely), **debuggability** (stepping a stream is genuinely worse, and stack traces are full
of `ReferencePipeline` frames), and **readability** — a 5-stage pipeline with nested collectors is often
clearer as two loops.

The framing to use: streams are excellent for **transformation pipelines** over collections
(filter/map/group/reduce) and a poor fit for **imperative control flow**. Choose per-site, not
per-codebase.

→ `quiz/17` Q18
</details>

---

## Score yourself

- **15+** — good. The Java 8 half of an interview is usually 3–4 of these.
- **The five worth knowing cold:** Q7 (`peek` may not run), Q9 (`orElse` eager), Q10 (`Optional` is a
  return type), Q11 (shared common pool), Q16 (`reversed()` reverses everything).
