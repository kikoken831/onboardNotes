# Source Coverage Map — "Java Interview 100 Important Questions"

Maps all **130 questions** from the LeetCode post (110 core + 20 design patterns) to where each is
answered in this pack.

**Why a map instead of 130 new questions:** the source list has ~12 outright duplicates, ~14 entries
that are individual `CompletableFuture` methods, and heavy overlap with `quiz/01`, `quiz/02` and
`quiz/09` which already existed. Answering each separately would have produced a pack that repeats
itself and rewards enumeration over understanding. Every source question is covered — some by a
pointer rather than a new entry.

---

## Where the answers live

| New file | Covers |
|---|---|
| [`quiz/16-java-core-and-oop.md`](16-java-core-and-oop.md) | `==`/`equals`, access modifiers, `static`, polymorphism, abstract vs final vs interface, `transient`/`volatile`/`static`, `finalize`, `clone`, `throw`/`throws`, checked vs unchecked, `Class.forName` vs `loadClass`, `defineClass`, `Proxy`, `MethodHandle`, `StringBuffer`/`StringBuilder` |
| [`quiz/17-java8-functional-and-streams.md`](17-java8-functional-and-streams.md) | lambdas vs anonymous classes, `Predicate`/`Consumer`/`Supplier`/`Function`/`Bi*`, `filter`/`map`/`forEach`/`peek`/`collect`, `Collector`, `Optional`, `parallelSort` |
| [`quiz/18-concurrency-api-inventory.md`](18-concurrency-api-inventory.md) | the whole `CompletableFuture` method family, `Callable`/`Runnable`/`Future`, `CompletionService`, concurrent collections, synchronisers, locks, `Executors` factories, `TimeUnit`, `AtomicLong` |
| [`quiz/19-design-patterns.md`](19-design-patterns.md) | all 20 design-pattern questions |

MCQ versions: `mcq-quiz/16`–`19`.

---

## Question-by-question

`16`/`17`/`18`/`19` = new file. `01`/`02`/`04`/`09` etc. = already covered before this post was added.

### Core Java (source Q1–15)

| # | Question | Where |
|---|---|---|
| 1 | `==` vs `.equals()` | **16** Q1 |
| 2 | JVM garbage collection + collector types | **04** Q1–Q23, **05** Q1 (far deeper than the source expects) |
| 3 | Checked vs unchecked exceptions | **09** Q11, **16** Q20 |
| 4 | How `synchronized` works + alternatives | **01** Q5, **16** Q9 |
| 5 | Lambda vs anonymous inner class | **17** Q1 |
| 6 | `transient` vs `volatile` | **16** Q7 |
| 7 | `finalize()` purpose and when called | **16** Q12, **09** Q17 |
| 8 | Polymorphism + real examples | **16** Q4 |
| 9 | `static` keyword | **16** Q3 |
| 10 | Interface vs abstract class | **09** Q12, **16** Q5 |
| 11 | `public`/`protected`/`private`/default | **16** Q2 |
| 12 | Abstract vs final classes | **16** Q5 |
| 13 | `ArrayList` vs `LinkedList` | **09** Q8 (measured: 28× and 178×) |
| 14 | `hashCode()` | **09** Q5, Q6 (measured: unremovable entry) |
| 15 | `transient` vs `static` | **16** Q7, Q8 |

### Concurrency and API surface (source Q16–110)

| # | Question | Where |
|---|---|---|
| 16 | `Callable` vs `Runnable` | **18** Q1 |
| 17, 88 | `ThreadLocal` *(duplicate)* | **01** Q13, **18** Q24 — measured leak in `P08` |
| 18, 32 | `Comparator` / vs `Comparable` *(near-duplicate)* | **09** Q11 |
| 19 | `ClassLoader.loadClass()` vs `Class.forName()` | **16** Q14 |
| 20 | `System.gc()` | **04** Q9 |
| 21 | `Proxy` class | **16** Q16 |
| 22 | `Map.get()` vs `Map.containsKey()` | **16** Q19 |
| 23 | `ConcurrentHashMap` vs `HashMap` | **01** Q10, **18** Q13 |
| 24 | Thread pool | **02** Q1–Q4, **18** Q22 |
| 25, 50 | `ThreadLocalRandom` / `.current()` *(duplicate)* | **18** Q25 |
| 26 | `CompletableFuture` | **02** Q8–Q12, **18** Q2 |
| 27 | `throw` vs `throws` | **16** Q21 |
| 28 | `Collector` interface | **17** Q13 |
| 29 | `WeakReference` | **04** Q21, **16** Q13 |
| 30, 82 | `ConcurrentSkipListMap` *(duplicate)* | **18** Q17 |
| 31 | `Thread.sleep()` vs `Object.wait()` | **01** Q15 |
| 33, 97 | `Phaser` *(duplicate)* | **01** Q17, **18** Q10 |
| 34 | `StampedLock` | **01** Q17 |
| 35 | `LockSupport` | **18** Q12 |
| 36, 74 | `supplyAsync` / `runAsync` | **18** Q3 |
| 37 | `Stream.filter()` vs `map()` | **17** Q6 |
| 38 | `thenApply()` | **18** Q4 |
| 39 | `join()` | **18** Q8 |
| 40, 99 | `CountDownLatch` *(duplicate)* | **01** Q16, **18** Q9 |
| 41, 98 | `CyclicBarrier` *(duplicate)* | **01** Q16, **18** Q9 |
| 42 | `ClassLoader.defineClass()` | **16** Q15 |
| 43, 57 | `ReentrantReadWriteLock` / `ReadWriteLock` | **01** Q17, **18** Q11 |
| 44 | `ForkJoinPool` | **02** Q7, **18** Q23 |
| 45 | `Semaphore` | **01** Q16, **18** Q9 |
| 46 | `computeIfAbsent()` | **01** Q10, **18** Q14 — measured in `P04` |
| 47 | `ConcurrentLinkedDeque` | **18** Q18 |
| 48 | `StringBuffer` vs `StringBuilder` | **16** Q10 |
| 49 | `Thread.UncaughtExceptionHandler` | **18** Q26 |
| 51 | `Arrays.parallelSort()` | **17** Q17 |
| 52 | `thenCompose()` | **18** Q5 |
| 53 | `thenAccept()` | **18** Q4 |
| 54 | `anyOf()` | **18** Q7 |
| 55 | `allOf()` | **18** Q7 |
| 56 | `ConcurrentHashMap.KeySetView` | **18** Q15 |
| 58 | `Lock` interface | **01** Q5, **18** Q11 |
| 59 | `CopyOnWriteArrayList` | **01** Q18 |
| 60 | `CopyOnWriteArraySet` | **18** Q19 |
| 61–64 | `Predicate` / `Consumer` / `Supplier` / `Function` | **17** Q3 |
| 65 | `completeExceptionally()` | **18** Q8 |
| 66 | `CompletableFuture.cancel()` | **18** Q8 — measured in `P05` Part 6 |
| 67 | `Stream.forEach()` vs `peek()` | **17** Q7 |
| 68 | `Exchanger` | **18** Q20 |
| 69 | `availableProcessors()` vs `getCommonPoolParallelism()` | **18** Q23 |
| 70 | `Object.clone()` | **16** Q11 |
| 71 | `Optional` | **17** Q9, Q10 |
| 72 | `complete()` | **18** Q8 |
| 73 | `Stream.collect()` | **17** Q12 |
| 75, 79 | `BiPredicate` / `BiConsumer` | **17** Q4 |
| 76 | `thenRun()` | **18** Q4 |
| 77 | `exceptionally()` | **18** Q6 |
| 80 | `thenCombine()` / `thenAcceptBoth()` | **18** Q7 |
| 81 | `MethodHandle` | **16** Q17 |
| 83 | `PriorityBlockingQueue` | **02** Q5, **18** Q21 |
| 84, 86 | `TransferQueue` / `LinkedTransferQueue` | **02** Q5, **18** Q16 |
| 85 | `SynchronousQueue` | **02** Q5, **18** Q16 |
| 87, 105 | `LinkedBlockingDeque` / `BlockingDeque` | **18** Q18 |
| 89 | `newCachedThreadPool()` | **02** Q2, **18** Q22 |
| 90 | `newFixedThreadPool()` | **02** Q2, **18** Q22 |
| 91 | `newSingleThreadExecutor()` | **02** Q2, **18** Q22 |
| 92–94 | `Future.get()` / `isDone()` / `isCancelled()` | **18** Q1 |
| 95 | `CompletionService` | **18** Q27 |
| 96, 106 | `LinkedBlockingQueue` / `.take()` | **02** Q5, **18** Q16 |
| 100 | `computeIfPresent()` | **18** Q14 |
| 101 | `BrokenBarrierException` | **18** Q10 |
| 102 | `TimeoutException` | **18** Q8 |
| 103 | `TimeUnit` | **18** Q28 |
| 104 | `AtomicLong` | **01** Q6 (measured: `LongAdder` 36× faster) |
| 107 | `DelayQueue` | **02** Q5, **18** Q21 |
| 108 | `ThreadPoolExecutor` | **02** Q1, Q3 |
| 109 | `newSingleThreadScheduledExecutor()` | **02** Q6, **18** Q22 |

### Design patterns (source Q111–130)

All 20 are in **`quiz/19`**: patterns overview and the creational/structural/behavioural taxonomy
(Q1–Q2), then Singleton (Q3), Factory Method (Q4), Decorator (Q5), Adapter (Q6), Observer (Q7),
Strategy (Q8), Template Method (Q9), Command (Q10), Iterator (Q11) — each with a Java example and
where the JDK/Spring itself uses it. The source asks "what is X" and "give an example of X" as two
questions per pattern; they're merged, since the example *is* the answer.

---

## What the source list omits — added anyway

Sizeable gaps for a current Java interview. All added:

| Topic | Where | Why it matters |
|---|---|---|
| **Generics** — erasure, PECS, wildcards, bounded types | **16** Q22–Q23 | Zero questions in the source. Asked in essentially every Java interview |
| **Serialization** beyond `transient` — `serialVersionUID`, `Externalizable`, `InvalidClassException` | **16** Q8 | Source mentions only `transient` |
| **`equals`/`hashCode` contract** in depth | **09** Q5 | Source asks what `hashCode()` *is*, not what breaks |
| **String pool, interning, `Integer` cache** | **16** Q10, **09** Q10 | The `Integer a=128, b=128; a==b` trap is a classic |
| **Immutable class design** | **16** Q6 | Directly relevant to concurrency |
| **Inner vs static nested vs anonymous vs local classes** | **16** Q18 | Standard question; also the memory-leak angle |
| **`try`-with-resources / `AutoCloseable`** | **16** Q12 | The actual answer to the `finalize()` question |
| **Fail-fast vs fail-safe iterators, `ConcurrentModificationException`** | **16** Q19 | Very common |
| **Enums** — `EnumMap`/`EnumSet`, enum singleton | **16** Q24, **19** Q3 | Enum singleton is the *preferred* Singleton |
| **Records, sealed types, pattern matching, text blocks, `var`** | **16** Q25 | The list is pre-modern-Java; these are now table stakes |
| **Virtual threads / structured concurrency** | **03** (whole file) | Absent from the source; GA since JDK 21 |
| **Java Memory Model / happens-before** | **01** Q1–Q3 | The source asks how `synchronized` works without asking *why* it works |

---

## How to use this

The source list rewards breadth. **Breadth is the easy half** — most of these are one or two
sentences, and an interviewer who only asks them isn't testing much.

Do it in this order:

1. **`mcq-quiz/16`–`19`** to find out which of the ~130 you genuinely can't answer. Fast.
2. **`quiz/16`–`19`** for the ones you missed.
3. **Then stop revising breadth and go depth**, because that's where interviews are actually decided:
   `quiz/01` (JMM), `quiz/04`/`05` (GC), and the runnable measurements in `problems/`. The source
   question "how does the JVM handle garbage collection" is answerable in two sentences or in twenty
   minutes — only one of those gets you hired.

---

## Verification pass (August 2026)

These four files went through a research pass against primary sources (OpenJDK source, Javadoc, JLS,
JEPs, the Adoptium support table) with an adversarial verification stage — **41 claims were put to
independent refutation attempts and none were refuted.** What follows is what the pass *changed*, so you
know which sentences are load-bearing and which were repaired.

**Corrected here, and the correction re-verified by running it on JDK 21.0.11:**

| Was written | Actually | Where fixed |
|---|---|---|
| "On 1 CPU the FJ common pool has **parallelism 0** and runs a thread per task" | The *behaviour* is right — measured `Thread-0/1/2` under `-XX:ActiveProcessorCount=1` — but `getCommonPoolParallelism()` **returns 1**, because the getter floors it. Don't say "0"; it's checkable. | `mcq-quiz/18` Q3, Q22; `quiz/18` Q23; `quiz/02` Q7; `quiz/04` Q12; `cheatsheets/gc-flags-and-logs.md` |
| "FJP default parallelism = `availableProcessors() − 1`" | **Common pool only.** Measured on 14 cores: `new ForkJoinPool()` → 14, `newWorkStealingPool()` → 14, `commonPool()` → 13. | `mcq-quiz/18` Q22; `quiz/18` Q23 |
| `finalize()` described as "removed for use" | **Deprecated for removal** (JDK 18, JEP 421) — still present and still **enabled by default**. `--finalization=disabled` is opt-in. | `quiz/09` |
| "JDK 21 is the LTS baseline" | As of Aug 2026 the **latest LTS is JDK 25**; latest GA feature release is **26**; **27** lands Sep 2026. Supported LTS line: 8/11/17/21/25. The pack is still *verified against* 21, which is the realistic banking baseline — say it that way. | `quiz/16` Q25 |
| `parallelSort`'s 8192-element threshold, stated as universal | Gates the **object-array** overloads only. Since the JDK 14 `DualPivotQuicksort` rewrite the primitive overloads don't branch on it at all. | `mcq-quiz/17` Q17 |
| "`Recursive update` is `computeIfAbsent`'s guard" | The Javadoc says **"detectably"** — not a reliable guard — and JDK 21u has **nine** identical throw sites (`putVal`, `replaceNode`, `compute`, `merge`, …). | `mcq-quiz/18` Q14 |
| "`Collectors.toMap` NPEs on a null value" | True, but from a `requireNonNull` in the accumulator — **not in `toMap`'s contract**. Only `toUnmodifiableMap` documents it. Null *keys* are accepted. | `mcq-quiz/17` Q12 |
| `StructuredTaskScope.ShutdownOnFailure` shown without a version warning | Those constructors **don't exist in JDK 25+** (replaced by `open()` + `Joiner`, `@since 25`), and the API is **still preview** in Aug 2026. Virtual threads are final since 21 — separate claim. | `quiz/03` Q6; `mcq-quiz/03` |

**Confirmed against primary sources, no change needed:** the `Integer` cache range is a **JLS 5.1.7
requirement** (asserted in OpenJDK as `assert IntegerCache.high >= 127`) with only the upper bound
tunable; `peek` may not run (verbatim `@apiNote`, and the elision is a **JDK 9** change, not Java 8);
`Optional` is **not `Serializable`** and `get()` is **not deprecated**, only discouraged by `@apiNote`;
`protected` **does** grant package access (compiled and confirmed), with §6.6.2.1's qualifying-type rule
as the subtlety; JDK proxies are interface-only; method-handle access checks happen once, at lookup.

**Details the pass added that no version of the source list has:** `forName("[LFoo;")` never initialises
`Foo` regardless of the flag; the JDK 9 `forName(Module, String)` overload returns `null` instead of
throwing; `equals`/`hashCode`/`toString` are dispatched to your `InvocationHandler`; the generated proxy
extends `java.lang.reflect.Proxy`, which is *why* it can't wrap a class; `newSingleThreadExecutor()` is
wrapped and **cannot** be cast to `ThreadPoolExecutor` (measured — `newFixedThreadPool(1)` can);
class-file major version = feature release **+ 44**.

**Uncorroborated, and left alone deliberately:** with Valhalla preview features enabled, `Integer`,
`Optional` and `clone()` semantics all change (value classes, `IdentityException`). That's real in
mainline but irrelevant to any interview you'll sit in 2026 — noted here rather than in the answers.
