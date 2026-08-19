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
---

## Verification pass (August 2026) — and how much it actually proves

These files went through a research pass against primary sources (OpenJDK `src.zip` and GA-tag sources,
Javadoc, JEPs, the Adoptium support table). **Read the evidence tiers below before quoting any of it**,
because the pass was much stronger on some topics than others and the headline number is misleading:

> 29 sources → **95 claims extracted** → only the **top 25 were adversarially verified** (3 independent
> refutation attempts each) → **24 confirmed, 1 refuted**.

The verified 25 covered class loading / proxies / method handles, the Java 8 stream-and-`Optional`
semantics, and one `ConcurrentHashMap` item. **Everything else — most of `java.util.concurrent`, all the
core-language facts, the LTS baseline, and the 2026-frequency question — produced no verified claims**,
and the research's own caveats say those sections "must not be published as verified". Two verifier
fetches also died on HTTP 429 rate limits, and `docs.oracle.com` was blocked throughout, so Javadoc
quotes were read from OpenJDK source mirrors instead of the rendered pages.

So the corrections below are split by what actually backs them.

### Tier 1 — I re-verified these myself by running code on JDK 21.0.11

These don't depend on the research being right. The commands are in the answers.

| Was written | Actually | Fixed in |
|---|---|---|
| "On 1 CPU the FJ common pool has **parallelism 0** and runs a thread per task" | Behaviour confirmed — `supplyAsync` ran on `Thread-0/1/2` under `-XX:ActiveProcessorCount=1`, no workers. But `getCommonPoolParallelism()` **returns 1**, because the getter floors it. Don't say "0"; it's checkable and wrong. | `mcq-quiz/18` Q3, Q22; `quiz/18` Q23; `quiz/02` Q7; `quiz/04` Q12; `cheatsheets/gc-flags-and-logs.md` |
| "FJP default parallelism = `availableProcessors() − 1`" | **Common pool only.** Measured on 14 cores: `new ForkJoinPool()` → 14, `Executors.newWorkStealingPool()` → 14, `commonPool()` → 13. | `mcq-quiz/18` Q22; `quiz/18` Q23 |
| — (new) | `newSingleThreadExecutor()` is returned **wrapped**: `instanceof ThreadPoolExecutor` is **false**, where `newFixedThreadPool(1)` is **true**. So it can't be cast and reconfigured. | `mcq-quiz/18` Q21 |
| `parallelSort`'s 8192 threshold, stated as universal | Read out of JDK 21.0.11 `src.zip`: `MIN_ARRAY_SORT_GRAN` (8192) appears **only** in the four object-array paths. `int/long/float/double` use `DualPivotQuicksort.MIN_PARALLEL_SORT_SIZE` = **4096** and also require `parallelism > 1`. **`byte[]`/`char[]`/`short[]` never parallelise at all** — their `sort()` overload has no parallelism parameter. | `mcq-quiz/17` Q17 |

### Tier 2 — in the adversarially verified 25 (high confidence)

Confirmed as the pack already stated them: `Class.forName` initialises and uses the **caller's defining
loader**, not the TCCL; `forName(name, false, loader)` is the non-initialising form;
`MethodHandles.Lookup.defineClass` (`@since 9`) and `defineHiddenClass` (`@since 15`) are the modern
alternatives to the `protected final` `ClassLoader.defineClass`; dynamic proxies are **interface-only**;
method-handle access checks happen **once at lookup** versus `Method.invoke`'s per-call check; `peek` may
not run; `Collectors.toMap` throws `IllegalStateException` on duplicate keys.

One item **CORRECTED**: `Optional.get()` is **not deprecated** in any release through mainline — only
discouraged by an `@apiNote` pointing at `orElseThrow()` (no-arg form `@since 10`). `Optional` is **not
`Serializable`**, and the "never use it as a field or parameter" rule is **JDK-team commentary, not
documentation** — so attribute it that way rather than to the Javadoc. `quiz/17` Q9's phrasing
("effectively soft-deprecated") is accurate; don't upgrade it to "deprecated".

Two version boundaries to state explicitly: the `peek`/`count()` elision is **JDK 9+** (absent in 8u),
and the duplicate-key `IllegalStateException` **message text changed in JDK 9** — assert the exception
type, never the message.

### Tier 3 — single-source finder claims, NOT adversarially verified

I applied these because they're well-established and low-risk, but they came from the unverified two-
thirds of the pass. **Re-check before relying on them in an interview.**

| Change | Status |
|---|---|
| `finalize()` is **deprecated for removal** (JDK 18, JEP 421), still present and **enabled by default** — not "removed" | The research flags this as "the highest-risk core-language fact, never verified". The JEP itself is well known; the *current default* is the soft part. Fixed in `quiz/09`. |
| Latest LTS is **JDK 25**; latest GA is **26**; **27** lands Sep 2026; supported LTS line 8/11/17/21/25 | From the Adoptium support table only, and the research explicitly lists "which JDK is the current LTS" as still open. Verify at adoptium.net before quoting a date. Fixed in `quiz/16` Q25. |
| `StructuredTaskScope.ShutdownOnFailure` doesn't exist in JDK 25+ (`open()` + `Joiner`, `@since 25`); still preview in Aug 2026 | Read from mainline source, unverified. Directionally safe because it's stated as a hedge ("don't quote 21's signatures as final"). `quiz/03` Q6; `mcq-quiz/03`. |
| `ConcurrentHashMap`'s "Recursive update" guard has 9 throw sites and is best-effort | The **"detectably"** wording is in the verified set; the throw-site count is not. `mcq-quiz/18` Q14. |
| `Collectors.toMap`'s null-value NPE comes from the accumulator, not the contract | Verified item, but the "null keys are accepted" half is finder-only. `mcq-quiz/17` Q12. |

### What was refuted, and what stays open

**One claim was killed 0–3:** that `Stream.forEach`'s documented non-determinism is scoped to *parallel*
pipelines. The Javadoc's statement is **unqualified**, so don't claim sequential `forEach` is guaranteed
ordered — say "use `forEachOrdered` if you need encounter order". The pack's wording (parallel `forEach`
doesn't preserve encounter order) is true and untouched by this.

**Still unanswered by any pass:** whether `invokeExact` vs `invoke` is exactly the adaptation distinction
as stated; which 2023-list gaps are genuinely highest-frequency in 2026 (no curriculum source was
reachable — treat any ranking here, including mine, as judgement not evidence); and the precise
`Executors` factory constants.

**Deliberately not applied:** with Valhalla preview features enabled, `Integer`, `Optional` and `clone()`
semantics all change (value classes, `IdentityException`). Real in mainline, irrelevant to a 2026
interview — recorded here rather than in the answers.
