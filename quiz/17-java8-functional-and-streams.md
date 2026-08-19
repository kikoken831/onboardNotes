# Quiz 17 — Java 8+ Functional Interfaces & Streams

18 questions. Covers the lambda/functional/stream questions from the source list, plus the semantics
that quizzes routinely state incorrectly.

Target: 25 min.

---

### 1. Lambda vs anonymous inner class — the real differences.

<details><summary>Answer</summary>

Both give you an inline implementation. Six differences that matter:

| | Anonymous class | Lambda |
|---|---|---|
| `this` | **the anonymous instance** | the **enclosing** instance |
| Compiled to | a separate `.class` file (`Outer$1`) | an `invokedynamic` call site, no class file |
| Instances | a new object every evaluation | **non-capturing lambdas are cached** |
| Can implement | one interface **or** extend a class | a **functional interface** only (one abstract method) |
| State | can have fields | no fields |
| Shadowing | can shadow enclosing locals | **cannot** — same scope |

**The `this` difference is the one that bites:**
```java
Runnable anon = new Runnable() { public void run() { System.out.println(this); } };  // the Runnable
Runnable lam  = () -> System.out.println(this);                                      // the OUTER object
```
That's why lambdas can't self-reference for recursion without an extra variable, and why an anonymous
class can be a memory-leak vector (it holds the outer instance — `quiz/16` Q18) in a way a
non-capturing lambda doesn't.

**Mechanism, if pushed:** the compiler emits an `invokedynamic` instruction whose bootstrap method is
`LambdaMetafactory`, linking a call site to a method handle for the lambda body (usually a private
synthetic method in the same class). So there's no per-lambda class file, and a **non-capturing**
lambda is instantiated once and reused. A **capturing** lambda allocates per evaluation — which is why
a capturing lambda in a hot loop shows up in an allocation profile.

**Choose an anonymous class when:** you need state, multiple methods, to extend a class, or a `this`
reference to the implementation itself. Otherwise use the lambda.
</details>

---

### 2. What is a functional interface, and what does `@FunctionalInterface` do?

<details><summary>Answer</summary>

An interface with **exactly one abstract method** (the SAM — single abstract method), so a lambda or
method reference can implement it.

`@FunctionalInterface` is **optional** and purely a compile-time assertion: it makes the compiler
error if the interface doesn't qualify. Use it on your own functional interfaces so someone can't
break every lambda by adding a second abstract method.

**What doesn't count against the one-method limit:**
- `default` methods
- `static` methods
- `private` methods (JDK 9+)
- **public methods of `Object`** — so `Comparator` declaring `equals(Object)` is still functional. This
  is a genuine subtlety and a good detail to know.

```java
@FunctionalInterface
interface TradeValidator {
    Either<Error, Trade> validate(Trade t);                        // the SAM
    default TradeValidator and(TradeValidator other) { … }         // doesn't count
    static TradeValidator noop() { … }                             // doesn't count
}
```

**Generic methods disqualify a lambda target:** an interface whose abstract method is itself generic
(`<T> T apply(T t)`) can't be implemented by a lambda, because lambdas can't be generic.
</details>

---

### 3. `Predicate`, `Consumer`, `Supplier`, `Function` — signatures and when.

<details><summary>Answer</summary>

The four core shapes. Know the **exact** abstract method:

| Interface | Method | Shape | Use |
|---|---|---|---|
| `Predicate<T>` | `boolean test(T)` | T → boolean | filtering, matching |
| `Consumer<T>` | `void accept(T)` | T → void | side effects |
| `Supplier<T>` | `T get()` | () → T | lazy/deferred production |
| `Function<T,R>` | `R apply(T)` | T → R | transformation |

Plus: `UnaryOperator<T>` (`Function<T,T>`), `BinaryOperator<T>` (`BiFunction<T,T,T>`), and the
two-argument forms in Q4.

**Default methods worth knowing** — because composition is the point:
```java
Predicate<Trade> big = t -> t.qty() > 10_000;
Predicate<Trade> eq  = t -> t.assetClass() == EQUITY;
big.and(eq)  ·  big.or(eq)  ·  big.negate()  ·  Predicate.not(big)   // JDK 11
Function<A,C> f = g.andThen(h);      // g first
Function<A,C> f = h.compose(g);      // g first, read right-to-left
Consumer<T> both = c1.andThen(c2);
```

**Primitive specialisations — the part that matters for performance.** Because generics can't hold
primitives (`quiz/16` Q22), every `Function<Integer,Integer>` boxes. So the JDK provides
`IntPredicate`, `IntFunction<R>`, `ToIntFunction<T>`, `IntUnaryOperator`, `IntBinaryOperator`,
`IntSupplier`, `IntConsumer` — and the `Long`/`Double` equivalents. **Use them in hot paths.**
(There are no `Short`/`Byte`/`Char`/`Boolean` specialisations, apart from `BooleanSupplier`.)

**`Supplier` is the one people under-use:** it's how you defer work. `orElseGet(supplier)` instead of
`orElse(expensiveCall())`, `requireNonNull(x, () -> costlyMessage())`, and lazy logging.
</details>

---

### 4. `BiPredicate`, `BiConsumer`, `BiFunction` — and why there's no `BiSupplier`.

<details><summary>Answer</summary>

The two-argument forms:

| Interface | Method |
|---|---|
| `BiPredicate<T,U>` | `boolean test(T, U)` |
| `BiConsumer<T,U>` | `void accept(T, U)` |
| `BiFunction<T,U,R>` | `R apply(T, U)` |

**There is no `BiSupplier`** — and the reason is a nice question: a supplier takes **no** arguments, so
"two-argument supplier" is meaningless. If you need to return two things, return a record.

**Where you meet them constantly** — `Map`'s API is built on them:
```java
map.forEach((k, v) -> …);                          // BiConsumer
map.replaceAll((k, v) -> v.toUpperCase());          // BiFunction
map.merge(k, 1, Integer::sum);                      // BiFunction (remapping)
map.compute(k, (k2, v) -> v == null ? 1 : v + 1);   // BiFunction
map.computeIfPresent(k, (k2, v) -> v + 1);          // BiFunction
map.computeIfAbsent(k, k2 -> load(k2));             // Function — note: key only
```
That last asymmetry is worth noticing: `computeIfAbsent` takes a `Function` (there's no existing value
to pass), while the others take a `BiFunction`.

Also `Stream.reduce(identity, accumulator, combiner)` and `Collector`'s components are all built from
these shapes, and there are primitive `Bi` forms like `ToIntBiFunction<T,U>`.
</details>

---

### 5. Method references — the four kinds.

<details><summary>Answer</summary>

| Kind | Syntax | Example | Equivalent lambda |
|---|---|---|---|
| Static method | `Type::staticMethod` | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| Instance method of a **particular** object | `instance::method` | `System.out::println` | `x -> System.out.println(x)` |
| Instance method of an **arbitrary** object of a type | `Type::instanceMethod` | `String::length` | `s -> s.length()` |
| Constructor | `Type::new` | `ArrayList::new` | `() -> new ArrayList<>()` |

**The third kind is the one that confuses people:** `String::toUpperCase` as a `Function<String,String>`
means "call `toUpperCase` **on** the argument" — the receiver becomes the first parameter. So
`String::compareTo` works as a `BiFunction<String,String,Integer>`: first arg is the receiver, second
is the parameter.

**Why prefer them:** shorter, and they don't re-wrap. But **not always** — `x -> foo(x)` is clearer
than `SomeClass::foo` when the target isn't obvious, and a method reference can't add a null check or
reorder arguments.

**Two gotchas:**
- `this::method` and `super::method` are legal and useful.
- **`instance::method` evaluates the receiver eagerly**, at the point the reference is created. If the
  variable is later reassigned, the reference still points at the old object — surprising in a loop.
- Ambiguity: if a type has both a static and an instance method with the same name and compatible
  shape, `Type::name` won't compile.
</details>

---

### 6. `Stream.filter()` vs `Stream.map()`.

<details><summary>Answer</summary>

- **`filter(Predicate<T>)` → `Stream<T>`** — **selects**. Same element type, **fewer or equal** elements.
- **`map(Function<T,R>)` → `Stream<R>`** — **transforms**. Possibly different type, **exactly the same
  number** of elements, one-to-one.

```java
trades.stream()
      .filter(t -> t.qty() > 10_000)     // Stream<Trade>, fewer
      .map(Trade::notional)               // Stream<Money>,  same count
      .reduce(Money.ZERO, Money::plus);
```

**The third one they should have asked about: `flatMap`.** `flatMap(Function<T, Stream<R>>)` is
one-to-**many** — it flattens nested structure. That's the one people get wrong:
```java
orders.stream().map(Order::items)      // Stream<List<Item>>  -- nested, usually not what you want
orders.stream().flatMap(o -> o.items().stream())   // Stream<Item> -- flattened
```

**Also worth naming:**
- **Order matters for performance.** `filter` before `map` does less transformation work. Cheap
  predicates before expensive ones.
- **`mapToInt`/`mapToLong`/`mapToDouble`** avoid boxing and unlock `sum()`, `average()`,
  `summaryStatistics()`. Use them in numeric pipelines.
- **`mapMulti`** (JDK 16+) is a lower-allocation alternative to `flatMap` when most elements map to
  zero or one result.
- Both are **intermediate and lazy** — nothing runs until a terminal operation (Q8).
</details>

---

### 7. `Stream.forEach()` vs `Stream.peek()` — and the trap in `peek`.

<details><summary>Answer</summary>

- **`forEach(Consumer)`** — a **terminal** operation. Consumes the stream, returns `void`.
- **`peek(Consumer)`** — an **intermediate** operation. Returns the stream so the pipeline continues.
  Intended for **debugging**.

**The trap, and it's the reason this question is worth asking: `peek` is not guaranteed to run for
every element.** Its Javadoc says so explicitly. Because the pipeline is lazy, the implementation may
**elide** it when the result doesn't require the elements:
```java
long n = Stream.of("a","b","c").peek(System.out::println).count();
// may print NOTHING — count() can be computed from the source size without traversing
```
So **never put logic with side effects in `peek`**. It is for temporary diagnostics, and even then it
can mislead.

**`forEach` vs `forEachOrdered`:**
- `forEach` makes **no ordering guarantee** — on a parallel stream elements arrive in arbitrary order,
  and even on a sequential stream you shouldn't rely on it.
- **`forEachOrdered`** guarantees encounter order, at the cost of parallelism.

**And the wider point:** if you're using `forEach` to mutate an external collection, you probably want
`collect` instead. `forEach(list::add)` is a stateful side effect that's a race in parallel and a code
smell in sequence — `collect(toList())` says what you mean and is safe.
</details>

---

### 8. Streams are lazy — what does that actually buy you?

<details><summary>Answer</summary>

**Intermediate** operations (`filter`, `map`, `sorted`, `limit`, `peek`, `distinct`) build a pipeline
and do **nothing**. Only a **terminal** operation (`collect`, `forEach`, `reduce`, `count`, `findFirst`,
`anyMatch`, `min`/`max`, `toArray`) triggers execution.

**Three consequences:**

1. **Single-pass fusion.** `filter().map().findFirst()` does *not* filter the whole collection then map
   the whole result. Each element flows through the whole chain, one at a time. So intermediate
   collections are never materialised — the same fusion measured in Scala's `.view` (173 MB → 92 MB in
   `problems/scala/S03AllocationPressure.scala`).
2. **Short-circuiting.** `findFirst`, `findAny`, `anyMatch`, `allMatch`, `noneMatch`, `limit` stop as
   soon as the answer is known. On an infinite stream that's the difference between working and
   hanging: `Stream.iterate(1, i -> i*2).filter(…).findFirst()` terminates.
3. **A stream is single-use.** Consuming it twice throws
   `IllegalStateException: stream has already been operated upon or closed`.

**Stateful vs stateless intermediate ops** — a good detail: `sorted` and `distinct` are **stateful**,
so they must buffer (and `sorted` must see everything) before emitting. That breaks short-circuiting
and defeats the streaming property, which is why `sorted().findFirst()` is much worse than
`min(comparator)`.

**When laziness bites:** if the source is **mutable** and you build the pipeline before consuming it,
the stream sees later mutations — the same caveat as Scala's views. And exceptions surface at the
*terminal* operation, not where the lambda was written, which makes stack traces confusing.
</details>

---

### 9. `Optional` — what is it for, and how do you use it correctly?

<details><summary>Answer</summary>

A container that is either **present** or **empty**, whose purpose is to make absence **visible in the
type** so the compiler forces the caller to consider it.

**Correct usage:**
```java
Optional<Trade> find(TradeId id);                     // return type — the intended use

find(id).map(Trade::notional)
        .filter(n -> n.isPositive())
        .orElseGet(Money::zero);                      // compose, don't unwrap early

find(id).orElseThrow(() -> new NotFound(id));         // explicit failure
find(id).ifPresentOrElse(this::process, this::warn);  // JDK 9
```

**The rules, in order of how often they're broken:**
1. **Never call `get()` without checking.** Prefer **`orElseThrow()`** (the no-arg form, JDK 10+) —
   same effect, but the name says it can fail. `get()` is effectively soft-deprecated for this reason.
   `Optional` misused is just `null` with extra allocation.
2. **Don't use it as a field or a constructor/method parameter.** It isn't `Serializable`, it adds an
   allocation per field, and for a parameter it just pushes the null check onto the caller — an
   overload is clearer. The API designers' documented guidance is: **return type only.**
3. **Don't return `Optional<Collection>`** — return an empty collection.
4. **`Optional.of(x)` throws on null; `Optional.ofNullable(x)` doesn't.** Use `ofNullable` when
   wrapping something that might be null (e.g. a Java API's return value); use `of` to assert it isn't.
5. **`orElse` evaluates its argument eagerly.** `orElse(expensive())` runs `expensive()` even when the
   value is present. Use **`orElseGet(supplier)`**.
6. Don't nest them, and don't `if (o.isPresent()) o.get()` — that's the pre-`Optional` pattern with
   extra steps.

**Cost:** `Some`-equivalent is an allocation, and `Optional<Integer>` boxes. `OptionalInt`/`Long`/
`Double` exist for that. In a hot loop this is measurable.
</details>

---

### 10. `Optional` vs returning `null` — is it always better?

<details><summary>Answer</summary>

**No, and saying so is the stronger answer.**

**Use `Optional`** when absence is a **normal, expected outcome** the caller must handle:
a lookup that may miss, a parse that may fail, an optional configuration value. The win is that the
compiler makes you deal with it, and it composes (`map`/`flatMap`/`filter`/`or`).

**Don't use it** when:
- The value is a **field** or a **parameter** (Q9).
- Absence means a **programming error** — then throw. `Optional` for "you called me wrong" hides a bug.
- You're in a **hot path** where the allocation matters.
- The API is used from a framework that expects a plain value (JPA fields, Jackson-mapped DTOs).
- **A collection return type** — an empty list already expresses absence.

**And the honest limitation:** `Optional` doesn't carry a **reason**. If the caller needs to know *why*
it's absent, you want `Either<Error, A>` (or a sealed result type), which is exactly the argument in
`quiz/12` Q8. `Optional` is `Either` with the error channel thrown away.

**The comparison to draw if it comes up:** Scala's `Option` is the same idea with better ergonomics
(pattern matching, `for`-comprehensions, and it's a monad you can mix with others via transformers).
Java's version lacks pattern-matching support and can't be flat-mapped across `Stream` cleanly —
though `Optional.stream()` (JDK 9) exists precisely to bridge that:
`list.stream().map(this::find).flatMap(Optional::stream)`.
</details>

---

### 11. What's wrong with `parallelStream()`?

<details><summary>Answer</summary>

> Full treatment in `quiz/02` Q11. The short version, because it's a frequent follow-up.

It runs on the **shared `ForkJoinPool.commonPool()`** — JVM-wide, parallelism `cores − 1`.

**Bad idea when:**
- **The body blocks.** You stall the common pool for the whole JVM, including other libraries' parallel
  streams and default `CompletableFuture` work.
- **You're in a web server.** You already have request-level parallelism from N request threads; adding
  intra-request parallelism multiplies contention and makes latency less predictable — usually a
  throughput *loss*.
- **The source splits badly** — `LinkedList`, `Stream.iterate`, an unsized `Iterator`,
  `BufferedReader.lines()`. Arrays, `ArrayList`, `IntStream.range` and `HashMap` split well.
- **N is small or the work is trivial** — fork/join overhead dominates below roughly 10⁴ cheap elements.
- **The lambda is stateful**, or the collector isn't concurrent.

**Good idea when:** a large, well-splittable source with genuinely expensive per-element CPU work, and
you've measured it.

**The "control the pool" trick:** submitting the terminal operation from inside your own
`ForkJoinPool` works (`myPool.submit(() -> list.parallelStream()…).get()`) but relies on undocumented
behaviour — mention it as a hack, not a design.
</details>

---

### 12. `Stream.collect()` — how does it work?

<details><summary>Answer</summary>

`collect` is the **mutable reduction** terminal operation: instead of combining values immutably like
`reduce`, it accumulates into a mutable container.

Two forms:
```java
// 1. Explicit three-arg form
List<String> l = stream.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
//                              supplier        accumulator     combiner

// 2. A Collector (the normal way)
List<String> l = stream.collect(Collectors.toList());
```

**The `Collectors` inventory worth knowing:**
```java
toList()  ·  toSet()  ·  toUnmodifiableList()          // JDK 10
toMap(keyFn, valFn)  ·  toMap(keyFn, valFn, mergeFn)   // 3-arg form avoids the duplicate-key throw
joining(", ", "[", "]")
counting()  ·  summingInt()  ·  averagingDouble()  ·  summarizingInt()
groupingBy(classifier)  ·  groupingBy(classifier, downstream)
partitioningBy(predicate)                              // always exactly two keys: true and false
mapping()  ·  flatMapping()  ·  filtering()            // downstream adapters
reducing()  ·  collectingAndThen()  ·  teeing()        // teeing is JDK 12
```

**Two traps to name:**
1. **`toMap` throws `IllegalStateException` on a duplicate key** — surprisingly often in production.
   Use the three-arg form with a merge function.
2. **`toMap` rejects null values** (it uses `Map.merge` internally, which NPEs on null). `groupingBy`
   doesn't have this problem.

**`Stream.toList()`** (JDK 16) is the concise modern form — but note it returns an **unmodifiable**
list, unlike `collect(toList())` which historically returns an `ArrayList`. That difference has broken
real code.

**`reduce` vs `collect`:** use `reduce` for immutable combination (`sum`, `min`, `String::concat` —
though the last is O(n²)); use `collect` when accumulating into a container. Getting that distinction
right is the actual question behind "what is `collect`".
</details>

---

### 13. What is the `Collector` interface?

<details><summary>Answer</summary>

The recipe `collect` follows. Five components:

```java
interface Collector<T, A, R> {      // T = element, A = mutable accumulator, R = result
    Supplier<A>          supplier();          // create a new accumulator
    BiConsumer<A, T>     accumulator();       // fold one element in
    BinaryOperator<A>    combiner();          // merge two accumulators (parallel only)
    Function<A, R>       finisher();          // accumulator -> final result
    Set<Characteristics> characteristics();   // CONCURRENT, UNORDERED, IDENTITY_FINISH
}
```

A custom one, which is the likely follow-up:
```java
Collector<Trade, ?, Money> totalNotional = Collector.of(
    Money::zero,                              // supplier
    (acc, t) -> acc.plusInPlace(t.notional()), // accumulator
    Money::plus,                              // combiner
    Collector.Characteristics.UNORDERED);
```

**The characteristics matter:**
- **`IDENTITY_FINISH`** — the finisher is the identity function, so the framework can skip it
  (`toList` has this).
- **`UNORDERED`** — the result doesn't depend on encounter order, enabling optimisations.
- **`CONCURRENT`** — a *single* accumulator can be shared across threads, so no merging is needed.
  Only safe if the accumulator is thread-safe. This is why **`groupingByConcurrent`** exists and is
  different from `groupingBy` on a parallel stream.

**In practice you rarely write one** — `Collectors` plus the downstream adapters (`mapping`,
`filtering`, `collectingAndThen`, `teeing`) compose to cover almost everything. Knowing that
composition exists is more useful than knowing the interface:
```java
Map<String, Long> countByCcy = trades.stream()
    .collect(groupingBy(Trade::ccy, counting()));
```
</details>

---

### 14. `groupingBy` vs `partitioningBy`.

<details><summary>Answer</summary>

- **`groupingBy(classifier)` → `Map<K, List<T>>`** — one entry per distinct classifier value. Keys are
  discovered from the data, so **absent groups simply don't appear**.
- **`partitioningBy(predicate)` → `Map<Boolean, List<T>>`** — **always exactly two keys**, `true` and
  `false`, even when one side is empty. That guarantee is the whole reason it exists.

```java
Map<AssetClass, List<Trade>> byClass = trades.stream().collect(groupingBy(Trade::assetClass));
Map<Boolean, List<Trade>>    settled = trades.stream().collect(partitioningBy(Trade::isSettled));
settled.get(false)   // never null, even if everything settled
```

**So:** `partitioningBy` is faster (a boolean-keyed two-slot map) and gives you the empty-side
guarantee. `groupingBy(t -> pred(t))` gives you neither.

**Downstream collectors are where the power is** — worth demonstrating unprompted:
```java
groupingBy(Trade::ccy, counting())
groupingBy(Trade::ccy, summingLong(Trade::notionalMinor))
groupingBy(Trade::ccy, mapping(Trade::id, toList()))
groupingBy(Trade::ccy, TreeMap::new, toList())                       // choose the map type
groupingBy(Trade::ccy, collectingAndThen(toList(), List::copyOf))     // post-process
groupingBy(Trade::assetClass, groupingBy(Trade::ccy))                 // nested grouping
```
Note `groupingBy` returns a `HashMap` by default with **no ordering guarantee** — pass
`TreeMap::new` or `LinkedHashMap::new` if you need one.
</details>

---

### 15. `IntStream` vs `Stream<Integer>` — why does it matter?

<details><summary>Answer</summary>

`Stream<Integer>` holds **boxed** `Integer` objects — 16 bytes each to carry 4 bytes of data, scattered
on the heap. `IntStream` holds primitives.

**The measured scale of this:** in the Scala allocation lab
(`problems/scala/S03AllocationPressure.scala`), a boxed `Array[Integer]` of 1 M cost **19.1 MB** vs
**3.8 MB** for `Array[Int]` — 5×. And a boxed collection is worse still, because of the per-element
object header plus poor cache locality on traversal.

**Conversions:**
```java
list.stream().mapToInt(Trade::qty)          // Stream<Trade> -> IntStream
intStream.boxed()                            // IntStream -> Stream<Integer>
intStream.mapToObj(i -> "#" + i)             // IntStream -> Stream<String>
IntStream.range(0, n)  ·  IntStream.rangeClosed(1, n)
```

**What the primitive streams give you that `Stream` doesn't:**
`sum()`, `average()` (returns `OptionalDouble`), `max()`/`min()` (returns `OptionalInt`),
`summaryStatistics()` — which gets you count/sum/min/max/average in **one pass**, and is
underappreciated.

```java
IntSummaryStatistics s = trades.stream().mapToInt(Trade::qty).summaryStatistics();
s.getMax(); s.getAverage(); s.getCount();
```

**The gotcha:** `IntStream.average()` returns `OptionalDouble` because an empty stream has no average —
so you must handle it. And `Stream<Integer>.reduce(0, Integer::sum)` boxes on **every** step, whereas
`mapToInt(…).sum()` doesn't.
</details>

---

### 16. `Comparator` — how do you build a multi-key sort?

<details><summary>Answer</summary>

> Also `quiz/09` Q11 for `Comparator` vs `Comparable`.

Modern composition, which is what's being tested:
```java
trades.sort(Comparator.comparing(Trade::counterparty)
                      .thenComparing(Trade::valueDate, Comparator.reverseOrder())
                      .thenComparingLong(Trade::notionalMinor));          // primitive: no boxing
```

**The factory and default methods:**
`comparing` · `comparingInt`/`comparingLong`/`comparingDouble` · `thenComparing` (+ primitive forms) ·
`reversed()` · `naturalOrder()` · `reverseOrder()` · `nullsFirst(cmp)` · `nullsLast(cmp)`.

**Three things that go wrong:**
1. **Use `comparingInt`, not `comparing`, for primitives** — otherwise you box on every comparison.
2. **Never subtract**: `(a, b) -> a.value() - b.value()` **overflows** for large or negative values and
   silently returns the wrong sign. Use `Integer.compare(a, b)`.
3. **Never use a two-way ternary**: `(a, b) -> a.score() > b.score() ? 1 : -1` **never returns 0**, so
   it violates the comparator contract and `Arrays.sort`/TimSort throws
   **`IllegalArgumentException: Comparison method violates its general contract!`** — a real production
   error that appears only on certain inputs and certain sizes.

**Contract to state:** transitive, antisymmetric (`sgn(compare(a,b)) == -sgn(compare(b,a))`), and
consistent. And note `Collections.sort`/`List.sort` use **TimSort**, which is **stable** — equal
elements keep their relative order, which is what makes multi-pass sorting work.
</details>

---

### 17. `Arrays.sort()` vs `Arrays.parallelSort()`.

<details><summary>Answer</summary>

- **`Arrays.sort(int[])`** — dual-pivot **quicksort** for primitives: O(n log n) average, in-place,
  **not stable** (irrelevant for primitives, which have no identity).
- **`Arrays.sort(Object[])`** — **TimSort**: O(n log n), **stable**, and adaptive (near-sorted input is
  close to O(n)). Needs O(n) auxiliary space.
- **`Arrays.parallelSort()`** (JDK 8) — parallel merge sort on the **common `ForkJoinPool`**.

**The detail that makes the answer good:** `parallelSort` **falls back to a sequential sort below a
threshold** (in the region of a few thousand elements — the constant is an implementation detail, so
say "a few thousand" rather than quoting a number). So on a small array it costs you nothing and gains
you nothing.

**When it's actually worth it:** a large array (hundreds of thousands upwards), multiple free cores,
and the sort is a measurable part of your profile. Because it uses the **common pool**, all the
`parallelStream` caveats apply (Q11) — including that in a busy web server the cores aren't free and
you may make things worse.

It also needs **O(n) extra space** for the merge, so on a very large array the allocation itself
matters.

**The framing:** the default `sort` is the right answer nearly always. `parallelSort` is a one-word
change you make *after* measuring, not a default.
</details>

---

### 18. When should you *not* use streams?

<details><summary>Answer</summary>

Worth having ready, because enthusiasm for streams is easy and judgement is what's being assessed.

**Use a plain loop when:**
- **You need to break early with complex conditions**, or mutate an index, or iterate two collections
  in step. `for` is clearer than contorting a stream.
- **You need checked exceptions.** Lambdas can't throw them, so you end up wrapping in
  `RuntimeException`, which is worse than the loop.
- **The operation is trivial and the collection is small.** A stream has setup and megamorphic-call
  overhead; a `for` loop over 10 elements is faster and clearer.
- **It's a genuine hot path.** The measured Scala equivalent is stark: a fused pipeline allocated
  **71 MB** where the `while` loop over a primitive array allocated **0 MB and ran ~90× faster**
  (`problems/scala/S03AllocationPressure.scala` Part 1). Streams are for clarity, not speed.
- **You'd need a stateful lambda** — that's a race in parallel and a smell in sequence.
- **Debuggability matters** — breakpoints and stack traces are worse in a pipeline.

**Use streams when:** the pipeline reads better than the loop, you're doing
filter/map/group/collect over a collection, or you want the composition to be obvious. That's most
business logic.

**The line to deliver:** *"Streams where they make the intent clearer, loops where they don't, and
`parallelStream` only with a measurement."* That's a more senior answer than either "always streams"
or "streams are slow".
</details>

---

## Score yourself

- **14+** — good.
- **The three most likely to be probed:** Q7 (**`peek` may not run** — the Javadoc caveat most people
  don't know), Q9 (`Optional` misuse), Q12 (`toMap`'s duplicate-key and null-value throws).
- **Q18 is the one that separates candidates** — knowing when *not* to use the shiny API.
