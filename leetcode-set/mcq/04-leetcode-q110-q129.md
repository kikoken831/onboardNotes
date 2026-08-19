# MCQ — LeetCode set, Q110–Q129 (design patterns)

Drill deck for [`../05-patterns-q110-q129.md`](../05-patterns-q110-q129.md).

---

### Q110. The most defensible answer to "how do design patterns help?"

- **A.** They guarantee extensibility
- **B.** **They give a shared vocabulary — and several are workarounds for missing language features**
- **C.** They improve performance
- **D.** They enforce SOLID

<details><summary>Answer</summary>

**B.** "It's a Strategy" replaces two minutes of explanation. That's most of the value.

The second clause is what makes the answer senior: **many GoF patterns are workarounds for what 1994
languages lacked.** Strategy and Command are first-class functions; Iterator is `for-each`; Factory Method
is often a static method or constructor reference. In a language with lambdas and ADTs several dissolve
into one line — which is why a Scala codebase looks pattern-light while following the same principles.

And the cost: pattern-driven design produces `AbstractSingletonProxyFactoryBean`. A pattern solves a
problem you **have**; applying one pre-emptively is speculative generality.

→ source Q110
</details>

---

### Q111. Which is **behavioural**?

- **A.** Builder
- **B.** Facade
- **C.** **Command**
- **D.** Flyweight

<details><summary>Answer</summary>

**C.** Creational = how objects are **made** (Singleton, Factory Method, Abstract Factory, Builder,
Prototype). Structural = how they're **composed** (Adapter, Decorator, Proxy, Facade, Composite, Bridge,
Flyweight). Behavioural = how they **interact** (Command, Observer, Strategy, Template Method, Iterator,
State, Chain of Responsibility, Visitor, Mediator, Memento).

Behavioural is the largest group and the most relevant here — **Command and Observer are structurally what
event sourcing and CQRS projections are.**

Worth adding: the taxonomy is a **weak framework**. Adapter, Decorator and Proxy are structurally identical
and differ only in intent.

→ source Q111
</details>

---

### Q112. The strongest criticism of Singleton?

- **A.** **Global mutable state with a hidden dependency** — untestable in isolation
- **B.** It can't be serialised
- **C.** It's slow
- **D.** It's not thread-safe

<details><summary>Answer</summary>

**A.** Three linked problems: the dependency is **invisible** (a `getInstance()` buried in a method means
the signature lies about what the class needs), tests **share state** so they become order-dependent and
unparallelisable, and you can't substitute a fake without a static setter.

**D** is a defect of *specific implementations*, and it's fixable (enum, holder idiom, `volatile` DCL). The
design objection isn't.

The answer that scores: use **dependency injection**. And note a Spring singleton bean is one per
`ApplicationContext`, **not per JVM** — so it isn't the GoF pattern at all.

→ source Q112
</details>

---

### Q113. Best Singleton implementation in Java?

- **A.** Double-checked locking with `volatile`
- **B.** **A single-element `enum`**
- **C.** A `synchronized` accessor
- **D.** A static field in a static block

<details><summary>Answer</summary>

**B.** One instance guaranteed by the JVM, **serialisation-safe for free** (no `readResolve`), and
**reflection-proof** — you cannot construct an enum reflectively. That defeats both attacks that break the
others.

**A** is correct **only** with `volatile`; without it the JMM permits a caller to see a partially
constructed object (`problems/java/P07` demonstrates it). The **holder idiom** is the best non-enum
option — lazy, thread-safe by class-init locking, zero synchronisation on reads.

Note what the JDK's own defensible singletons have in common — `Runtime.getRuntime()`,
`Collections.emptyList()`, `Comparator.naturalOrder()` are **stateless or immutable**.

→ source Q113
</details>

---

### Q114. Factory Method vs Abstract Factory?

- **A.** They're the same
- **B.** Factory Method is static
- **C.** Abstract Factory adds caching
- **D.** **Factory Method defers one product to a subclass; Abstract Factory creates a family of related products**

<details><summary>Answer</summary>

**D.** Factory Method: one overridable creator, one product (`Collection.iterator()`,
`ThreadFactory.newThread()`). Abstract Factory: one object producing several products used together
(JDBC's `Connection` handing out `Statement`/`PreparedStatement` for the same vendor).

The test for Abstract Factory is **"must these vary together?"** If not, you've built two independent
factories with extra ceremony — where the pattern earns its enterprise-bloat reputation.

**B** points at something real but different: a **static factory method** (`List.of`, `Integer.valueOf`) is
about naming, caching and returning a subtype — not GoF at all. Effective Java Item 1 is about *that*.

→ source Q114
</details>

---

### Q115. In modern Java, a factory is often just?

- **A.** A `switch` statement
- **B.** A `record`
- **C.** **A constructor reference** — `Mt541::new` in a `Map<Venue, Function<Trade, Instruction>>`
- **D.** An annotation

<details><summary>Answer</summary>

**C.** Saying this shows you know when the pattern has dissolved: a constructor reference **is** a factory,
so the class hierarchy buys nothing.

```java
Map<Venue, Function<Trade, SettlementInstruction>> factories =
    Map.of(SWIFT, Mt541::new, DTCC, DtccInstruction::new);
```

Where the classic form still earns its place: when the factory method sits **inside a Template Method**, so
the base class owns the algorithm and defers one step. That's how it usually appears in real code.

Spring's version: `BeanFactory` and `FactoryBean<T>`.

→ source Q115
</details>

---

### Q116. Decorator requires?

- **A.** **Implementing the same interface as the wrapped object**
- **B.** Subclassing the concrete type
- **C.** The wrapped type to be `final`
- **D.** A shared abstract base

<details><summary>Answer</summary>

**A.** Same interface in, same interface out — that's what allows arbitrary **stacking** and makes the
pattern combinatorial: 3 features means **3 decorators** instead of 8 subclasses for every combination. And
you compose at **runtime**.

Costs to acknowledge: many small classes, debugging through a deep wrapper stack, **broken identity**
(`equals`/`==` no longer match the underlying object), and no way to add methods outside the interface.

Versus its twins: **Adapter** changes the interface; **Decorator** keeps it and adds behaviour; **Proxy**
keeps it and controls *access*.

→ source Q116
</details>

---

### Q117. `new BufferedReader(new InputStreamReader(new FileInputStream(f)))` is?

- **A.** Adapter only
- **B.** **Both** — `InputStreamReader` adapts bytes to chars, `BufferedReader` decorates
- **C.** Decorator only
- **D.** Proxy

<details><summary>Answer</summary>

**B.** And spotting that is a better answer than naming one. `InputStreamReader` **changes the interface**
(`InputStream` → `Reader`) — that's Adapter, the clearest example in the JDK. `BufferedReader` keeps the
`Reader` interface and adds buffering — that's Decorator.

Worth volunteering in your own example: **layer order is a deliberate choice.** Metering outside caching
measures cache hits; inside, it measures only misses.

Other JDK decorators: `Collections.unmodifiableList`/`synchronizedList`, `HttpServletRequestWrapper`.

→ source Q117
</details>

---

### Q118. Why does Adapter matter for the "enrich my endpoint from another team's service" design question?

- **A.** It makes the call faster
- **B.** It removes the need for DTOs
- **C.** **Define your own interface and put their client behind it** — their DTOs and failure modes stop at the boundary
- **D.** It provides retries automatically

<details><summary>Answer</summary>

**C.** Their SDK's wire DTOs, checked exception types and timeout behaviour stay in one class with one job,
instead of leaking through your domain — and you can test your service with a two-line fake.

Two flavours: **object adapter** (composition, preferred) and class adapter (inheritance — impossible in
Java for two concrete classes, since there's one superclass slot).

Distinguish from **Facade**, which also wraps but **simplifies a whole subsystem** rather than translating
one interface.

→ source Q118; `quiz/10` Q1
</details>

---

### Q119. Which is a JDK Adapter?

- **A.** `BufferedInputStream`
- **B.** `Optional.of`
- **C.** `Collections.unmodifiableList`
- **D.** **`Arrays.asList`**

<details><summary>Answer</summary>

**D.** Array → `List`: same data, different interface. Also `InputStreamReader` (bytes → chars) and
`Collections.enumeration` (`Iterator` → `Enumeration`).

**A** and **C** are **Decorators** — same interface, added behaviour (buffering, immutability).

Worth knowing about `Arrays.asList` specifically, since it's a favourite follow-up: the returned list is
**fixed-size** and writes through to the array, so `add` throws `UnsupportedOperationException` while `set`
works — a leaky adapter.

Spring's adapters: `HandlerAdapter`, `HandlerInterceptorAdapter`.

→ source Q119
</details>

---

### Q120. Observer's two production hazards?

- **A.** **Listener leaks, and synchronous notification on the publisher's thread**
- **B.** Slow dispatch and memory alignment
- **C.** Ordering and serialisation
- **D.** Reflection cost and boxing

<details><summary>Answer</summary>

**A.** The **leak**: an observer registered and never deregistered keeps its whole object graph alive —
`problems/gc-labs/L02Leak.java` OOMs a 128 MB heap in about 2 seconds with a `List<Runnable>` of capturing
lambdas. The **coupling**: naive `for (l : listeners) l.onEvent(e);` runs on the publisher's thread, so one
slow observer adds its latency, and one thrown exception stops the remaining observers being notified.

Java's own `Observer`/`Observable` were **deprecated in JDK 9** (not thread-safe, not serialisable, no event
typing).

The framing for this JD: **event-driven architecture is Observer across a process boundary**, with
durability and replay added — and with **backpressure**, which Observer lacks.

→ source Q120
</details>

---

### Q121. Best way to make a hand-rolled listener registry leak-resistant?

- **A.** **Return an `AutoCloseable` unsubscribe handle from `subscribe`**
- **B.** Call `System.gc()` periodically
- **C.** Use a `WeakHashMap` of listeners
- **D.** Store listeners in a `ThreadLocal`

<details><summary>Answer</summary>

**A.** "You must remember to deregister" is exactly how leaks happen; handing back the handle makes
unsubscription the default path:

```java
AutoCloseable subscribe(Consumer<TradeSettled> l) {
    listeners.add(l);
    return () -> listeners.remove(l);
}
```

**C** is the tempting one and it's a trap: a listener with no other strong reference then vanishes
unpredictably, so events stop arriving for no visible reason.

Also: `CopyOnWriteArrayList` for the registry (small, read-constantly, snapshot iteration), and a
**per-listener catch** so one bad observer can't silence the rest.

→ source Q121
</details>

---

### Q122. In modern Java, Strategy is usually?

- **A.** An abstract class per strategy
- **B.** **A lambda or method reference, often in a `Map<Key, Function<…>>`**
- **C.** A separate microservice
- **D.** An enum with a `switch`

<details><summary>Answer</summary>

**B.** Strategy *is* a function. `Comparator` is the JDK's Strategy interface, and
`list.sort(comparing(Trade::getId))` is the pattern with no classes involved. The registry form replaces a
growing `switch`, so adding a case means adding an **entry**, not editing a method.

**D** isn't wrong as a starting point, and with **sealed interfaces + exhaustive pattern matching** (JDK 21)
it's genuinely good — the compiler tells you when a case is missing, which a `Map` cannot. Centralised-and-
checked versus open-and-extensible: say which you're choosing and why.

Spring form: inject `List<FeeStrategy>` and let the container collect every implementation.

→ source Q122
</details>

---

### Q123. Which JDK type is a Strategy?

- **A.** `BufferedReader`
- **B.** `Optional`
- **C.** `StringBuilder`
- **D.** **`RejectedExecutionHandler`**

<details><summary>Answer</summary>

**D.** `ThreadPoolExecutor` ships four interchangeable ones — `AbortPolicy`, `CallerRunsPolicy`,
`DiscardPolicy`, `DiscardOldestPolicy` — selected at construction. That's Strategy exactly.

Others: **`Comparator`** (the canonical one), `ThreadFactory`, `Collector`.

The Spring pattern worth being able to write:
```java
FeeCalculator(List<FeeStrategy> strategies) {
    this.byClass = strategies.stream().collect(toMap(FeeStrategy::appliesTo, identity()));
}
```
A new `@Service` implementing `FeeStrategy` is now wired in with **no edit** to `FeeCalculator` —
Open/Closed, concretely.

→ source Q123
</details>

---

### Q124. Template Method vs Strategy?

- **A.** Strategy requires a base class
- **B.** Template Method is structural
- **C.** **Inheritance, compile-time, one axis — versus composition, runtime, swappable**
- **D.** They're the same

<details><summary>Answer</summary>

**C.** Template Method fixes the skeleton in a base class and lets subclasses fill in steps, so variation is
chosen by which subclass you instantiate — and you get one axis of it, because Java has single inheritance.

Discipline that shows you've used it: the template method should be **`final`** so subclasses can't break
the skeleton, the steps `protected`, and distinguish a **hook** (`protected` no-op, *may* override) from an
**abstract step** (*must*).

Modern preference is Strategy — **favour composition over inheritance** — because a template method is a
**fragile base class**. `JdbcTemplate` is interesting because it takes its steps as *parameters*, which is
Template Method converging on Strategy.

→ source Q124
</details>

---

### Q125. `JdbcTemplate` is primarily which pattern?

- **A.** Proxy
- **B.** **Template Method**
- **C.** Facade
- **D.** Strategy

<details><summary>Answer</summary>

**B.** Hence the name. It owns the algorithm — connection acquisition, exception translation into
`DataAccessException`, cleanup — while you supply the varying step (`RowMapper`).

The wider Spring map, since "which patterns does Spring use?" is a common question: **Singleton** (default
bean scope — one per context), **Factory** (`BeanFactory`, `FactoryBean`), **Proxy** (all of AOP:
`@Transactional`, `@Async`, `@Cacheable`), **Template Method** (`JdbcTemplate`, `RestTemplate`,
`TransactionTemplate`), **Observer** (`ApplicationEvent`), **Front Controller** (`DispatcherServlet`),
**Adapter** (`HandlerAdapter`), and **DI** throughout.

The framing that lands: **Spring is a pattern catalogue you configure rather than write** — and its cost is
that when something breaks you're debugging *through* three of them.

→ source Q125
</details>

---

### Q126. Why is Command the most JD-relevant pattern here?

- **A.** It enables undo
- **B.** It's how Spring MVC routes requests
- **C.** **Reifying a request as an object is exactly what a command in event sourcing/CQRS is**
- **D.** It's the basis of `Runnable`

<details><summary>Answer</summary>

**C.** Once a request is **data**, you can queue it, log it, authorise it, retry it, ship it over a network
and store it. `SettleTrade(tradeId, date, amount)` is a command object; the handler validates it and emits
events.

So have the distinction ready: **a command is an intent — imperative, may be rejected; an event is a fact —
past tense, immutable.** That one sentence answers both "do you know Command?" and half of "do you
understand event sourcing?".

**D** is true and worth mentioning (`Runnable`, `Callable`, `ActionListener` are all Command). **A** is
real but secondary — in post-trade the analogue is a **compensating transaction**: you don't delete a
settlement, you book a reversal.

→ source Q126
</details>

---

### Q127. In modern Java a command is usually?

- **A.** An abstract class with `execute()`
- **B.** A `Map<String, Object>`
- **C.** An enum
- **D.** **A `record` implementing a `sealed` interface, handled by an exhaustive `switch`**

<details><summary>Answer</summary>

**D.** No hierarchy needed, and the compiler enforces completeness:

```java
sealed interface Command permits SettleTrade, CancelTrade {}
record SettleTrade(TradeId id, LocalDate value, BigDecimal amount) implements Command {}

List<Event> handle(Command c, Trade state) {
    return switch (c) {                       // exhaustive -- compiler checked
        case SettleTrade s -> state.settle(s.value(), s.amount());
        case CancelTrade x -> state.cancel(x.reason());
    };
}
```

Because the command is data you get audit, replay, idempotency keys and retry **for free** — the object that
drove the change is the one you logged.

**B** is what you get instead if you skip the types, and it's how schema drift starts.

→ source Q127
</details>

---

### Q128. `ConcurrentModificationException` is most often caused by?

- **A.** **Removing from a collection inside a for-each, in a single thread**
- **B.** Two threads writing a `HashMap`
- **C.** Iterating a `CopyOnWriteArrayList`
- **D.** Calling `size()` during iteration

<details><summary>Answer</summary>

**A.** Despite the name, the usual cause has nothing to do with threads: the iterator tracks `modCount` and
throws on the next `next()`. Fix with `Iterator.remove()`, **`removeIf()`**, or by collecting and removing
afterwards.

**C** is wrong and worth knowing why: `CopyOnWriteArrayList` iterates an **immutable snapshot**, so it never
throws — fail-**safe**. `ConcurrentHashMap`'s iterators are **weakly consistent**.

And detection is **best-effort**, so never write logic that relies on the exception being thrown.

→ source Q128
</details>

---

### Q129. Implementing `Iterable<T>` gives you?

- **A.** **`for-each` plus encapsulation** — but *not* `stream()`
- **B.** Thread safety
- **C.** `stream()` for free
- **D.** Automatic parallelism

<details><summary>Answer</summary>

**A.** Callers traverse without seeing or being able to mutate your internal structure — so the source can be
a list, a cursor, or a paged remote API and the `for-each` doesn't change.

**C** is the trap: you need `StreamSupport.stream(spliterator(), false)`, and a good `Spliterator` (with
`trySplit` and correct `characteristics`) is what makes parallel streams over your type actually work.

Two flavours to name: **external** iteration (`Iterator` — the caller pulls, can stop early) and
**internal** (`forEach` — the collection pushes). Streams are internal iteration with laziness added.

→ source Q129
</details>
