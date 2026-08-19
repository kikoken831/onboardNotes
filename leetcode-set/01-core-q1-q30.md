# Q1–Q30 — OOP, language basics, collections, first concurrency classes

Source order, verbatim questions. Answer out loud before opening each block.

---

### 1. What is a difference between the `==` operator and `.equals()` method in Java?

<details><summary>Answer</summary>

`==` compares **references** (identity) for objects and **values** for primitives. `.equals()` compares
whatever the class defines as equality — reference identity by default, since `Object.equals` is `==`.

The trap is autoboxing: `Integer a = 127, b = 127; a == b` is `true` (cached), but at `128` it's
`false`. Small test values hide the bug.

Two rules: override `equals` and `hashCode` **together**, and use `Objects.equals(a, b)` for
null-safety. For `String`, `==` may accidentally work because literals are interned — never rely on it.

→ `quiz/16` Q1, Q3
</details>

---

### 2. How does the JVM handle garbage collection, and what are the different types of garbage collectors available in Java?

<details><summary>Answer</summary>

**Mechanism:** the JVM traces from GC **roots** (stacks, statics, JNI) and reclaims anything
unreachable. It exploits the **weak generational hypothesis** — most objects die young — so the heap is
split into a young generation (eden + two survivor spaces) and an old generation. Allocation is a
pointer bump in a thread-local **TLAB**. Minor GCs copy survivors and are cheap; objects surviving
enough copies are **tenured**. All of it happens at **safepoints**, which is why time-to-safepoint
matters as much as pause time.

**The collectors on JDK 21:** **Serial** (tiny heaps, single-threaded), **Parallel** (throughput,
stop-the-world), **G1** (default — region-based, concurrent marking, `MaxGCPauseMillis` target),
**ZGC** (sub-millisecond pauses, concurrent compaction, generational since 21), **Shenandoah**,
and **Epsilon** (no-op, for testing allocation).

Pick by requirement: batch throughput → Parallel; a latency SLA → G1 then ZGC; huge heap with a strict
tail → ZGC.

→ `quiz/04` (whole file, Q23 is the consolidated walkthrough); `quiz/05`; `problems/gc-labs/`
</details>

---

### 3. What is the difference between checked and unchecked exceptions in Java, and when would you use one over the other?

<details><summary>Answer</summary>

**Checked** = everything under `Exception` except `RuntimeException` and its subclasses; the compiler
forces catch-or-declare. **Unchecked** = `RuntimeException` and `Error`.

```
Throwable → Error (unchecked) | Exception (CHECKED) → RuntimeException (unchecked)
```

**Use checked** for a recoverable condition the caller must decide about (a file that may not exist).
**Use unchecked** for programming errors (`IllegalArgumentException`, `IllegalStateException`) and for
conditions no caller can sensibly handle.

Modern practice leans unchecked — checked exceptions don't compose with lambdas or streams, which is
why Scala and Kotlin dropped them. Note "catch or specify" is a **compiler** rule, not a JVM one.

→ `quiz/16` Q20; `quiz/09` Q11
</details>

---

### 4. How does the `synchronized` keyword work in Java, and what are some alternatives to using it?

<details><summary>Answer</summary>

Every object has a **monitor**. `synchronized` acquires it on entry and releases on exit — including on
exception — giving **mutual exclusion** plus, crucially, a **happens-before** edge: everything before
the release is visible after the next acquire. It's reentrant. On a method it locks `this`
(or the `Class` object if `static`).

The JVM optimises it: biased locking (removed in 15), thin locks via CAS, inflating to an OS monitor
only under real contention. So uncontended `synchronized` is cheap.

**Alternatives:** `volatile` for visibility-only; `AtomicInteger`/`LongAdder` for counters;
`ReentrantLock` when you need `tryLock`, a timeout, interruptibility or multiple `Condition`s;
`ReadWriteLock`/`StampedLock` for read-heavy state; concurrent collections instead of locking a plain
one; and best of all **immutability or confinement**, which needs no lock.

One JDK 21 specific: `synchronized` **pins a virtual thread** to its carrier, `ReentrantLock` doesn't.

→ `quiz/01` Q4, Q5; `quiz/03` Q3
</details>

---

### 5. What is a lambda expression in Java, and how does it differ from an anonymous inner class?

<details><summary>Answer</summary>

A lambda is a concise instance of a **functional interface** (exactly one abstract method).

Differences that matter:
- **`this`** refers to the **enclosing** instance in a lambda; to the anonymous object in an anonymous
  class.
- **No separate class file** — a lambda is an `invokedynamic` call site linked lazily by
  `LambdaMetafactory`; an anonymous class produces `Outer$1.class`.
- A **non-capturing** lambda can be reused as a singleton; `new Runnable(){}` allocates every time.
- A lambda **cannot hold state or fields**; an anonymous class can.

Both capture only effectively-final locals.

→ `quiz/17` Q1
</details>

---

### 6. What is the difference between `transient` and `volatile` variables in Java, and when would you use one over the other?

<details><summary>Answer</summary>

Unrelated concerns that only look similar because both are field modifiers.

**`transient`** = **serialisation**: "don't write this field". On deserialisation it comes back as the
type default (`null`/`0`/`false`), because **deserialisation doesn't run the constructor**. Use it for
caches, derived values, and secrets.

**`volatile`** = **concurrency**: reads and writes go to main memory, giving visibility and a
happens-before edge, plus atomicity for `long`/`double`. Use it for a flag one thread writes and others
read. It does **not** make `count++` atomic — that's read-modify-write.

→ `quiz/16` Q7; `quiz/01` Q2
</details>

---

### 7. What is the purpose of the `finalize()` method in Java, and when is it called?

<details><summary>Answer</summary>

It was meant as a last-chance cleanup hook before an object's memory is reclaimed. **When is it
called? There is no guarantee it is ever called** — the JVM may exit first.

It's worse than unreliable: it needs two GC cycles, a single slow finalizer stalls the shared queue and
can OOM you with reclaimable garbage, it can **resurrect** the object, and exceptions inside it are
swallowed.

**Status:** deprecated in JDK 9, **deprecated for removal** in JDK 18 (JEP 421, which added
`--finalization=disabled`). Still present and enabled by default, but treat it as gone.

**Use instead:** `try`-with-resources / `AutoCloseable` — deterministic, and `close()` failures become
*suppressed* exceptions rather than masking the original. `Cleaner` as a native-memory safety net.

The framing: **GC manages memory, not resources.**

→ `quiz/16` Q12; `quiz/04` Q20
</details>

---

### 8. How does polymorphism work in Java, and what are some real-world examples of how it can be used?

<details><summary>Answer</summary>

**Runtime polymorphism** = overriding. The compiler checks against the *declared* type; the JVM
dispatches on the *runtime* type via the vtable. So `List<Trade> l = new ArrayList<>()` calls
`ArrayList.add`.

Not polymorphic, and all three are trick questions: **`static` methods** (hidden, dispatched by
compile-time type), **fields** (hidden, resolved by declared type), and `private`/`final` methods
(statically bound).

**Overloading** is compile-time resolution by signature — strictly not polymorphism.

Real examples: `Comparator` implementations swapped at a sort site; `PaymentProcessor` with card/ACH/wire
implementations chosen by config; JDBC's `Driver` interface; Spring injecting a `@Primary` bean where an
interface is declared. The value in all four is the same — **the caller doesn't change when the
implementation does.**

→ `quiz/16` Q4
</details>

---

### 9. What is the purpose of the `static` keyword in Java, and when would you use it?

<details><summary>Answer</summary>

`static` binds a member to the **class**, not an instance: one copy, no `this`, accessible without an
object.

Uses: **constants** (`static final`), **utility methods** (`Math.max`), **factory methods**
(`List.of`, `Integer.valueOf`), **nested classes that don't need the outer instance**, and
`static` initialiser blocks for one-time setup.

Costs to name: static state is **global mutable state** — hard to test, and a shared mutable `static`
field is a concurrency bug waiting to happen. Static methods can't be overridden or stubbed, which is
why heavy static utility layers are hard to test. And **static fields are never serialised**.

Prefer a `static` nested class to an inner class unless you need the enclosing instance — an inner class
holds an implicit reference to it and can pin it in memory.

→ `quiz/16` Q3, Q18
</details>

---

### 10. What is the difference between an interface and an abstract class in Java, and when would you use one over the other?

<details><summary>Answer</summary>

**Interface:** a contract. Multiple inheritance of *type*. `default` and `static` methods since 8,
`private` methods since 9. Fields are implicitly `public static final` — so no state.

**Abstract class:** partial implementation. Single inheritance, but it can hold **state**,
constructors, and any access modifier.

**Choose an interface** by default — it keeps the type open, works with lambdas if it has one abstract
method, and doesn't spend the one inheritance slot. **Choose an abstract class** when subtypes must
share fields or constructor logic (Template Method).

Since JDK 17 there's a third option that's often the right one: a **`sealed` interface** with `record`
implementations — a closed hierarchy the compiler can check exhaustively. That's how you'd model
`TradeEvent` in an event-sourced system.

→ `quiz/16` Q5; `quiz/19` Q9
</details>

---

### 11. What is the difference between `public`, `protected`, `private`, and `default` access modifiers in Java?

<details><summary>Answer</summary>

| | Class | Package | Subclass | World |
|---|---|---|---|---|
| `private` | ✓ | | | |
| *default* (package-private) | ✓ | ✓ | | |
| `protected` | ✓ | **✓** | ✓ | |
| `public` | ✓ | ✓ | ✓ | ✓ |

**The detail most people get wrong:** `protected` grants **package access as well as subclass access**,
so it's strictly wider than default — a same-package class needs no inheritance relationship at all.

Second subtlety (JLS §6.6.2.1): from a *different* package, a subclass may access a protected instance
member only **through a reference of its own type**, not through an arbitrary superclass reference.

Default access is the useful one people forget — it's how you expose something to tests in the same
package without making it public API.

→ `quiz/16` Q2
</details>

---

### 12. What is the difference between `abstract` and `final` classes in Java, and when would you use one over the other?

<details><summary>Answer</summary>

**Opposites.** `abstract` = *must* be extended (can't be instantiated). `final` = *cannot* be extended.
`abstract final` is a compile error.

Use `abstract` when you have shared implementation and the type is incomplete on its own. Use `final`
to protect invariants — it's why `String` and the wrappers are final, and it's the default advice for
value types.

The middle ground since JDK 17 is `sealed`: extension permitted, but only by classes you name. That's
what enables exhaustive `switch` pattern matching and it's the right tool for an algebraic data type.

→ `quiz/16` Q5
</details>

---

### 13. What is the difference between `ArrayList` and `LinkedList` in Java, and when would you use one over the other?

<details><summary>Answer</summary>

`ArrayList` is a growable array: **O(1) random access**, O(n) insert/remove in the middle, one object
and contiguous memory. `LinkedList` is a doubly-linked list: O(1) insert/remove **given the node**,
**O(n) access by index**, and a node object per element.

**Use `ArrayList` essentially always.** `LinkedList`'s theoretical wins evaporate in practice because
you must *walk* to the position first, and pointer-chasing destroys cache locality. Even
`remove(0)`-heavy queue use is better served by `ArrayDeque`.

The honest answer to "when `LinkedList`?" is: when you need `Deque` semantics *and* `List` semantics on
the same object — which is rare. Say that; it's a better answer than reciting the complexity table.

→ `quiz/09` Q4
</details>

---

### 14. What is the `hashCode()` method in Java, and how is it used?

<details><summary>Answer</summary>

It returns an `int` used by hash-based collections to pick a **bucket**. `HashMap.get` hashes the key,
finds the bucket, then compares with `equals` within it.

**The contract:** equal objects **must** have equal hash codes; unequal objects *may* collide;
and the value must not change while the object is in a hash collection. Break the first rule and your
object vanishes from a `HashSet`; break the third — by mutating a field used in `hashCode` — and it
becomes unreachable in a map it's still stored in.

So: **always override `hashCode` with `equals`**, using the same fields — `Objects.hash(a, b)` is fine;
a `record` generates both correctly.

Implementation detail worth knowing: `HashMap` spreads the hash (`h ^ (h >>> 16)`) because buckets are
a power of two and only the low bits would otherwise be used, and a bucket becomes a **red-black tree**
past 8 entries so a collision attack degrades to O(log n) rather than O(n).

→ `quiz/09` Q5, Q6
</details>

---

### 15. What is the difference between `transient` and `static` variables in Java? ↺

<details><summary>Answer</summary>

*(Overlaps Q6 — this angle is serialisation-only.)*

Different purposes, one shared consequence: **neither is serialised.** Default serialisation covers only
**non-static, non-transient** fields. So `static transient` is redundant.

- **`static`** = one per **class**, not per instance. It's not serialised because it isn't part of the
  object's state.
- **`transient`** = per instance, but deliberately excluded from the stream.

The consequence people get wrong: after deserialisation a `transient` field holds its **type default**,
not whatever a constructor or field initialiser would have set — because deserialisation **doesn't run
the constructor**. Restore it in a custom `readObject`, or compute it lazily.

A `static` field, by contrast, is simply whatever the class already has loaded in that JVM — which is
why it appears to "survive" serialisation and confuses people.

→ `quiz/16` Q7, Q8
</details>

---

### 16. What is a `Callable` interface in Java, and how is it different from a `Runnable` interface?

<details><summary>Answer</summary>

`Callable<V>.call()` **returns `V` and may throw a checked exception**. `Runnable.run()` returns `void`
and throws nothing checked. Both are functional interfaces.

That difference dictates error handling. A `Runnable`'s exception has nowhere to go — it reaches the
thread's `UncaughtExceptionHandler`. A `Callable`'s is **captured in the `Future`** and only surfaces
when you call `get()`, wrapped in `ExecutionException`.

The trap: `executor.submit(runnable)` also wraps in a `FutureTask`, so **an exception from a submitted
`Runnable` is silently swallowed too**. Use `execute()` for fire-and-forget, or override
`afterExecute`.

→ `quiz/18` Q1
</details>

---

### 17. What is a `ThreadLocal` variable in Java, and how is it used?

<details><summary>Answer</summary>

Per-thread storage: each thread sees its own value through the same object. Used for non-thread-safe
objects you don't want to allocate per call (`SimpleDateFormat` — though `DateTimeFormatter` is
immutable and needs none) and for **implicit context** — correlation IDs, security principal, tenant,
SLF4J's MDC.

```java
private static final ThreadLocal<Ctx> CTX = ThreadLocal.withInitial(Ctx::new);
try { CTX.set(ctx); doWork(); } finally { CTX.remove(); }   // the finally is mandatory
```

**Two hazards.** In a **thread pool** the thread outlives the request, so without `remove()` you get a
memory leak *and* a correctness bug — request 2 sees request 1's user. And
`InheritableThreadLocal` copies to children **at creation**, which does nothing useful in a pool.

Measured in `problems/java/P08`: 22 MB retained, 4 MB after `remove()`.

→ `quiz/18` Q24; `quiz/04` Q21
</details>

---

### 18. What is a `Comparator` interface in Java, and how is it used?

<details><summary>Answer</summary>

An **external** ordering: `int compare(T a, T b)`. A functional interface, so a lambda works.

```java
list.sort(comparing(Trade::getBook).thenComparing(Trade::getId, reverseOrder()));
```

Three things to have ready:
- **`reversed()` reverses the whole comparator built so far**, not just the last key — pass a comparator
  to `thenComparing` to reverse one key only.
- `comparingInt`/`comparingLong` avoid boxing the key; `nullsFirst`/`nullsLast` tolerate nulls.
- The contract must be **antisymmetric, transitive and consistent**, or TimSort can throw
  `IllegalArgumentException: Comparison method violates its general contract!`. Never
  `(a,b) -> a.x - b.x` on ints — it overflows. Use `Integer.compare`.

→ `quiz/17` Q16
</details>

---

### 19. What is the difference between `ClassLoader.loadClass()` and `Class.forName()` methods in Java?

<details><summary>Answer</summary>

**`Class.forName(name)` initialises the class — it runs static initialisers. `loadClass(name)` does
not**; initialisation is deferred to first active use.

`forName(String)` is documented as exactly `forName(name, true, currentLoader)`, where `currentLoader`
is the **defining loader of the calling class** — *not* the thread context class loader. Use the
three-arg `forName(name, false, loader)` for load-without-initialise.

That's why the old JDBC idiom `Class.forName("com.mysql.jdbc.Driver")` worked: it ran the static block
that registered the driver. (Unnecessary since JDBC 4.0 — `ServiceLoader` handles it.)

Documented nuance almost nobody has: `forName("[Lcom.Foo;")` for an **array** type loads `Foo` but
**never initialises** it, whatever the flag says.

→ `quiz/16` Q14
</details>

---

### 20. What is the purpose of the `System.gc()` method in Java, and when is it called?

<details><summary>Answer</summary>

It's a **hint** — "a suggestion that the JVM expend effort toward recycling unused objects". Nothing
guarantees a collection happens.

**When is it called?** By you, and you shouldn't. It typically triggers a **full stop-the-world GC**,
which destroys tail latency, and on a large heap that's seconds. `-XX:+DisableExplicitGC` exists
precisely because libraries call it; `-XX:+ExplicitGCInvokesConcurrent` is the softer option.

Legitimate uses are narrow: before starting a benchmark measurement, or in a lab experiment where you
want a clean baseline — which is exactly how `problems/gc-labs/` uses it.

If you feel you need it in production, the real problem is elsewhere: a leak, an undersized heap, or the
wrong collector. Note the one thing `System.gc()` *does* reliably do is enqueue references and clear
`SoftReference`s — which is why it appears to "fix" a leak while actually just deferring it.

→ `quiz/04` Q19
</details>

---

### 21. What is a `Proxy` class in Java, and how is it used?

<details><summary>Answer</summary>

`java.lang.reflect.Proxy.newProxyInstance(loader, interfaces, handler)` generates a class at runtime
implementing the given **interfaces**, routing every call to your `InvocationHandler.invoke(proxy,
method, args)`.

**Interfaces only.** The generated class is `final` and already **extends `Proxy`**, so there's no
superclass slot — which is *why* you need CGLIB/ByteBuddy to proxy a concrete class, and why the target
class and methods must then be non-`final`.

Used for: AOP-style cross-cutting concerns, lazy loading (Hibernate), remoting stubs, and test doubles.
It's how Spring implements `@Transactional`, `@Async`, `@Cacheable`.

Details worth having: primitive args are **boxed**; `args` is **`null`**, not an empty array, for a
no-arg method; a checked exception not declared by the interface surfaces as
`UndeclaredThrowableException`; and **`equals`/`hashCode`/`toString` are also dispatched to your
handler**, while `getClass`/`wait`/`notify` are not.

And the bug class it produces: **self-invocation bypasses the proxy**, so `this.doWork()` silently skips
`@Transactional`.

→ `quiz/16` Q16; `quiz/10` Q4
</details>

---

### 22. What is the difference between `Map.get()` and `Map.containsKey()` methods in Java?

<details><summary>Answer</summary>

`get` returns the value or `null`; `containsKey` returns a boolean. They differ because **`HashMap`
allows null values**, so `get() == null` is ambiguous — absent, or mapped to null? Only `containsKey`
distinguishes them.

`ConcurrentHashMap` and `TreeMap` **forbid null values**, so there `get() == null` does mean absent —
and CHM forbids them precisely *because* the ambiguity would be unresolvable in a map where you can't
safely check-then-act.

**The important part:** `containsKey` then `get` is **two lookups** and, concurrently, **a race**.
Measured in `problems/java/P04`: 8 threads, the expensive loader ran 8 times, 7 results discarded.
Prefer `getOrDefault`, `computeIfAbsent`, or `Optional.ofNullable(map.get(k))`.

→ `quiz/16` Q19
</details>

---

### 23. What is a `ConcurrentHashMap` in Java, and how is it different from a regular `HashMap`?

<details><summary>Answer</summary>

A thread-safe `Map` with **per-bin locking** for writes and **lock-free reads** (via `volatile` node
reads). `HashMap` is unsynchronised: concurrent writes can corrupt it, and historically could spin
forever during resize.

Differences to state:
- **Locking granularity** — per bin, not per map, so it scales far past
  `Collections.synchronizedMap` (one lock for everything, including reads). A CAS handles the first
  insert into an empty bin; `size()` uses `LongAdder`-style striped counters.
- **Iterators are weakly consistent** — never throw `ConcurrentModificationException`, reflect state
  at some point since creation.
- **Nulls forbidden** for both keys and values.
- **Aggregate methods are estimates** — `size()`, `isEmpty()`, `containsValue()` don't lock.

And the limit: atomic **per entry**, not across entries. Two `put`s are not a transaction — use
`compute`/`merge` when you need atomicity.

→ `quiz/18` Q13
</details>

---

### 24. What is a Thread pool in Java, and how is it used?

<details><summary>Answer</summary>

A set of reused worker threads pulling from a queue, so you pay thread-creation cost once and **bound**
concurrency.

```java
var pool = new ThreadPoolExecutor(
    8, 16, 60, SECONDS,
    new ArrayBlockingQueue<>(1000),          // BOUNDED
    new ThreadFactoryBuilder().setNameFormat("settle-%d").build(),
    new ThreadPoolExecutor.CallerRunsPolicy());
```

**Recite the admission algorithm** — it explains the surprising behaviour: (1) below core size → new
thread; (2) else **queue** it; (3) only if the queue *rejects* → new thread up to max; (4) else reject.
So with an **unbounded queue step 3 never happens** and `maximumPoolSize` is dead configuration.
Measured in `P05`: the queue reached 100,000 while the pool stayed at core size.

Sizing: CPU-bound ≈ cores; I/O-bound ≈ cores × (1 + wait/service). Always name your threads, always
bound the queue, always choose the rejection policy deliberately.

On JDK 21, for I/O-bound work, `newVirtualThreadPerTaskExecutor()` replaces the sizing problem entirely.

→ `quiz/02` (whole file)
</details>

---

### 25. What is a `ThreadLocalRandom` class in Java, and how is it used?

<details><summary>Answer</summary>

A `Random` with **per-thread seed state**: `ThreadLocalRandom.current().nextInt(100)`.

Why it exists: `Random` **is** thread-safe, and that's the problem — its seed is a single `AtomicLong`,
so concurrent calls contend on CAS and throughput collapses on one hot cache line.
`ThreadLocalRandom` removes the sharing entirely (with padding against false sharing), making
`current()` effectively free.

Never share the returned instance between threads — that reintroduces the problem without the safety.
Cost: you can't seed it, so it isn't reproducible; use `SplittableRandom` for parallel work that needs
reproducibility, and `SecureRandom` for anything security-relevant.

Same striping idea as **`LongAdder`** vs `AtomicLong`.

→ `quiz/18` Q25; `quiz/04` Q13
</details>

---

### 26. What is a `CompletableFuture` in Java, and how is it used?

<details><summary>Answer</summary>

A `Future` you can **compose, complete manually, and attach callbacks to** — it implements `Future` and
`CompletionStage`.

```java
supplyAsync(() -> loadTrade(id), ioPool)
    .thenCompose(t -> enrich(t))                    // flatMap, not map
    .thenCombine(loadSsi(id), Settlement::new)
    .orTimeout(300, MILLISECONDS)
    .exceptionally(ex -> Settlement.failed(ex));
```

Three things to say about it, all of which are traps:
1. **Default executor is the shared common `ForkJoinPool`** (`cores − 1`). Blocking work must pass its
   own executor.
2. A **non-`Async` callback runs on an unspecified thread** — whoever completes the stage, or the
   caller if it's already done.
3. **`cancel()` does not interrupt** the running task; it just completes the future with
   `CancellationException`.

And: an unobserved failure is **silently swallowed** unless you attach `exceptionally`/`handle`.

→ `quiz/18` Q2–Q8; `quiz/02` Q7
</details>

---

### 27. What is the difference between `throw` and `throws` keywords in Java?

<details><summary>Answer</summary>

`throw` is a **statement** that raises an exception now: `throw new IllegalStateException("...")`.
`throws` is a **signature clause** declaring what a method may propagate:
`void settle() throws SettlementException`.

Details worth adding:
- `throw null` compiles and throws `NullPointerException`.
- An **override may declare fewer or narrower** checked exceptions, never broader — Liskov enforced by
  the compiler.
- **More precise rethrow** (JDK 7+) lets `catch (Exception e) { throw e; }` compile in a method
  declaring only the specific checked types.
- A `return` or `throw` inside `finally` **discards** an in-flight exception. Never do it —
  try-with-resources handles this properly via suppressed exceptions.

→ `quiz/16` Q21
</details>

---

### 28. What is a `Collector` interface in Java, and how is it used?

<details><summary>Answer</summary>

The recipe `collect()` uses for a **mutable reduction**: five pieces — `supplier` (new container),
`accumulator` (fold one element), `combiner` (merge two containers, parallel only), `finisher`,
`characteristics`.

You mostly use the factories rather than the interface: `toList`, `toSet`, `toMap`, `joining`,
`groupingBy`, `partitioningBy`, `counting`, `summingInt`, `teeing`.

Characteristics matter: **`IDENTITY_FINISH`** means the container *is* the result so the finisher is
skipped (why `toList` is cheap); **`UNORDERED`** frees the pipeline from encounter order;
**`CONCURRENT`** allows one shared container, honoured only when unordered too.

Prefer composition — `groupingBy(f, mapping(g, toList()))` — over `Collector.of(...)`. A hand-written
collector is rarely warranted.

→ `quiz/17` Q13
</details>

---

### 29. What is a `WeakReference` in Java, and how is it used?

<details><summary>Answer</summary>

A reference that does **not** prevent collection: if the only path to an object is weak, the GC clears
it at the next cycle and (optionally) enqueues the reference on a `ReferenceQueue`.

The strength ladder: **strong** (never collected) → **soft** (collected only under memory pressure —
intended for caches, but unpredictable) → **weak** (collected at the next GC) → **phantom** (enqueued
after finalisation, for cleanup).

Canonical use: `WeakHashMap` for metadata keyed by an object you don't own — the entry disappears when
the key does. **The leak:** if the *value* references its own key, the key stays strongly reachable
through the value and nothing ever clears. `ThreadLocal` has the same shape (weak key, strong value),
which is why an unremoved `ThreadLocal` leaks.

What weak references are **not**: a bounded cache. Use Caffeine.

→ `quiz/16` Q13; `quiz/04` Q20
</details>

---

### 30. What is a `ConcurrentSkipListMap` in Java, and how is it different from a regular `TreeMap`?

<details><summary>Answer</summary>

The concurrent `SortedMap`/`NavigableMap`. Both are sorted and **O(log n)**; the difference is that
`ConcurrentSkipListMap` is thread-safe and **lock-free**, and `TreeMap` is neither.

It uses a **probabilistic skip list** rather than a red-black tree precisely because rebalancing a tree
is very hard to do lock-free — a skip list needs only localised CAS.

Trade-offs: higher constant factor and more memory (multiple node levels), so single-threaded it loses
to `TreeMap`; and **`size()` is O(n)** because it walks the list — don't call it in a loop.

Reach for it when you need range queries (`headMap`, `tailMap`, `ceilingKey`) *and* concurrency: a live
order book keyed by price, a time-ordered event index.

→ `quiz/18` Q17
</details>
