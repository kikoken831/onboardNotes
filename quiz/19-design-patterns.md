# Quiz 19 — Design Patterns in Java

14 questions covering all 20 design-pattern entries from the source list. The source asks "what is X"
and "give an example of X" as two separate questions per pattern — merged here, because **the example
*is* the answer**, and a pattern you can't point at in the JDK you don't really know.

Target: 25 min.

---

### 1. What are design patterns, and what do they actually buy you?

<details><summary>Answer</summary>

**Named, reusable solutions to recurring design problems** — catalogued by the "Gang of Four" (Gamma,
Helm, Johnson, Vlissides, 1994).

**What they buy you, in order of honesty:**

1. **Vocabulary.** The primary value. "This is a strategy" communicates a whole structure in three
   words, in a code review or a design discussion. That's the benefit that survives scrutiny.
2. **Solutions that are already debugged** — someone has hit the edge cases.
3. **A pointer at the underlying principles** — most patterns are an application of "program to an
   interface", "favour composition over inheritance", or "encapsulate what varies".

**And the part that separates a senior answer:**

**Patterns are vocabulary, not goals.** Applying patterns you don't need is how you get
`AbstractSingletonProxyFactoryBean`. The failure mode is real and has a name — *pattern-happy* code,
where three layers of indirection exist because a pattern was available rather than because a problem
was.

**Many GoF patterns are workarounds for missing language features.** Say this, because it's true and
it demonstrates you understand *why* they exist:
- **Strategy** is a function.
- **Command** is a closure.
- **Template Method** is a higher-order function.
- **Builder** substitutes for named and default parameters.
- **Singleton** is a module.
- **Iterator** is now built into the language (`for-each`).

In Java 8+ several collapse into a lambda; in Scala most collapse into a function value, a `case class`,
or an `object` (`quiz/09` Q8, `quiz/12` Q17). That's not an argument against knowing them — the
*problems* persist — but it reframes them as historical solutions rather than eternal truths.
</details>

---

### 2. Creational, structural, behavioural — what's the taxonomy?

<details><summary>Answer</summary>

The GoF grouping, by **what the pattern is about**:

| Category | Concerned with | Patterns |
|---|---|---|
| **Creational** | **object creation** — decoupling *what* you make from *who* makes it | Singleton, Factory Method, Abstract Factory, Builder, Prototype |
| **Structural** | **composition** — how objects are assembled into larger structures | Adapter, Decorator, Facade, Composite, Proxy, Bridge, Flyweight |
| **Behavioural** | **communication** — how objects interact and distribute responsibility | Strategy, Observer, Template Method, Command, Iterator, State, Chain of Responsibility, Mediator, Memento, Visitor, Interpreter |

**A one-line test for each**, which is more useful than the definitions:
- Creational → *"who decides which class gets instantiated?"*
- Structural → *"what does this object wrap or contain?"*
- Behavioural → *"who calls whom, and when?"*

**The pairs people confuse**, worth having ready because interviewers probe exactly these:
- **Adapter vs Decorator** — Adapter **changes the interface**, same behaviour. Decorator **keeps the
  interface**, adds behaviour. (Q5/Q6.)
- **Decorator vs Proxy** — same shape; different *intent*. Decorator **adds** functionality; Proxy
  **controls access** (lazy loading, security, remoting).
- **Strategy vs Template Method** — Strategy varies behaviour by **composition** (swap an object);
  Template Method by **inheritance** (override a hook). (Q8/Q9.)
- **Factory Method vs Abstract Factory** — Factory Method is **one** product via a subclass-overridden
  method; Abstract Factory is a **family** of related products behind one interface.
- **Strategy vs State** — identical structure; Strategy is chosen by the client, State transitions
  itself.
</details>

---

### 3. Singleton — and what's wrong with it?

<details><summary>Answer</summary>

**One instance, globally reachable.** The most-asked and most-criticised pattern.

**The implementations, worst to best:**

```java
// 1. Eager. Simple, thread-safe by class-init locking, but not lazy.
public class A { private static final A INSTANCE = new A(); private A(){} 
                 public static A get(){ return INSTANCE; } }

// 2. Synchronised accessor. Lazy and correct, but locks on EVERY read.
public static synchronized B get() { if (i == null) i = new B(); return i; }

// 3. Double-checked locking. MUST be volatile or it's broken (quiz/09, P07).
private static volatile C instance;
public static C get() {
    C r = instance;
    if (r == null) synchronized (C.class) { r = instance; if (r == null) instance = r = new C(); }
    return r;
}

// 4. Holder idiom. Lazy, thread-safe by class-init, ZERO synchronisation on reads.
public class D { private static class H { static final D I = new D(); }
                 public static D get(){ return H.I; } }

// 5. ENUM — the preferred implementation.
public enum E { INSTANCE; public void work() { … } }
```

**Why enum wins** (Effective Java): one instance guaranteed by the JVM, **serialisation-safe for free**
(no `readResolve` needed), and **reflection-proof** — you cannot construct an enum reflectively. It
defeats the two attacks that break version 3 and 4: someone calling the private constructor via
`setAccessible(true)`, and deserialisation creating a second instance (`quiz/16` Q24).

**The criticisms — and having them ready is the actual test:**
- **It's global mutable state.** Hidden dependencies: a class that uses `Registry.get()` doesn't declare
  that in its constructor, so you can't tell what it needs.
- **Untestable.** You can't substitute a test double, and state leaks between tests.
- **Concurrency hazard** — a singleton with mutable fields is shared by every thread (`quiz/01` Q24).
- **Lifecycle** — no control over when it's destroyed.
- **"One per classloader", not one per JVM** — which surprises people in containers and app servers.

**What to say:** *"In practice I use dependency injection instead. A Spring singleton bean gives you
one instance with the lifecycle managed and the dependency declared in the constructor — you get the
'one instance' property without the global-state problems. If I genuinely need the GoF version, enum."*
That answer shows you know the pattern *and* why the industry moved on.
</details>

---

### 4. Factory Method (and Abstract Factory).

<details><summary>Answer</summary>

**Factory Method:** define an interface for creating an object, but let **subclasses decide which class
to instantiate**. "Virtual constructor."

```java
abstract class TradeProcessor {
    protected abstract Validator createValidator();          // the factory method
    public final Result process(Trade t) {                   // uses it without knowing the type
        return createValidator().validate(t);
    }
}
class EquityProcessor extends TradeProcessor {
    protected Validator createValidator() { return new EquityValidator(); }
}
```

**The looser, far more common form** — a static factory that hides which implementation you get:
```java
static Instrument of(String isin) {
    return isin.startsWith("GB00") ? new Gilt(isin) : new Equity(isin);
}
```
Strictly this is a *static factory method* rather than GoF Factory Method — worth distinguishing, since
most "factory" code in the wild is this.

**Where the JDK does it:** `Calendar.getInstance()`, `NumberFormat.getInstance()`,
`Executors.newFixedThreadPool()`, `List.of()`, `Optional.of()`, `Charset.forName()`,
`DriverManager.getConnection()`. In Spring: every **`@Bean`** method.

**Abstract Factory** goes one level up: an interface producing a **family** of related products, so the
whole family is swapped together.
```java
interface MarketFactory { Calendar calendar(); SettlementRules rules(); FeeSchedule fees(); }
class UkMarket implements MarketFactory { … }
class UsMarket implements MarketFactory { … }
```
Use when the products must be **consistent with each other** — a UK calendar with US settlement rules
would be a bug, and the factory makes that combination unrepresentable.

**Why bother at all:** `new` hard-codes a concrete class at the call site. A factory gives you one place
to change the decision, enables testing with a stub, and centralises validation. **The cost** is
indirection — don't add a factory for a class with one implementation and no validation.
</details>

---

### 5. Decorator.

<details><summary>Answer</summary>

**Add behaviour to an object dynamically, keeping the same interface**, by wrapping it. The alternative
to subclassing for every combination.

```java
interface PriceSource { Money price(Isin isin); }

class CachingPriceSource implements PriceSource {        // the decorator
    private final PriceSource delegate;                   // wraps the same interface
    private final Cache<Isin, Money> cache;
    public Money price(Isin isin) {
        return cache.get(isin, delegate::price);          // adds behaviour, then delegates
    }
}
class MeteredPriceSource implements PriceSource { … }
class RetryingPriceSource implements PriceSource { … }

// compose freely, at runtime:
PriceSource s = new MeteredPriceSource(new CachingPriceSource(new RetryingPriceSource(live)));
```

**The structural requirement:** the decorator **implements the same interface** as the thing it wraps,
and holds a reference to it. That's what makes them stackable in any order and any number.

**Why not subclassing:** with 3 orthogonal features you'd need 2³ = 8 subclasses to cover the
combinations. Decorators give you 3 classes and composition at runtime.

**The canonical JDK example** — and it's the one to cite:
```java
new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(f))))
```
The whole `java.io` package is decorators. Also `Collections.unmodifiableList/synchronizedList`,
servlet `HttpServletRequestWrapper`, and Spring's `TransactionAwareDataSourceProxy`.

**Decorator vs Proxy** — same shape, different intent: a decorator **adds** behaviour the client wants;
a proxy **controls access** (lazy, remote, security). Spring AOP is technically Proxy, but what it does
(adding transactions, metrics) is decoration — which is why the two blur in practice.

**Costs:** a deep stack of wrappers is hard to debug (whose `price()` am I in?), and identity is lost —
`equals` and `getClass()` see the outermost wrapper.
</details>

---

### 6. Adapter.

<details><summary>Answer</summary>

**Convert one interface into another** so two incompatible things can work together. The "wrapper" you
write because you can't change either side.

```java
// Their API — a legacy vendor feed you don't own.
class VendorFeed { VendorTick fetch(String vendorCode) { … } }

// Your API — what your domain wants.
interface MarketData { Optional<Tick> tick(Isin isin); }

// The adapter.
class VendorFeedAdapter implements MarketData {
    private final VendorFeed feed;
    private final IsinToVendorCode mapping;
    public Optional<Tick> tick(Isin isin) {
        VendorTick v = feed.fetch(mapping.codeFor(isin));    // translate the CALL
        return Optional.ofNullable(v).map(this::toDomainTick); // translate the RESULT
    }
}
```

**Two variants:** **object adapter** (composition — holds the adaptee, as above; preferred) and **class
adapter** (inheritance — extends the adaptee, impossible in Java for multiple adaptees and generally
worse).

**Adapter vs Decorator — the distinction interviewers test:**
- **Adapter changes the interface**, keeps the behaviour. *"These don't fit together."*
- **Decorator keeps the interface**, changes the behaviour. *"I want this plus something."*

**JDK examples:** `Arrays.asList(array)` (array → `List`), `Collections.list(Enumeration)`,
`InputStreamReader` (byte stream → char stream — arguably the clearest one), `java.sql` driver
implementations.

**Why this pattern matters for the BofA role specifically:** it's exactly the design point in
`quiz/10` Q1 — you define **your own** `ReferenceDataClient` interface and put the other team's Feign
client behind an adapter, so their DTOs and failure modes stay at the boundary and you can swap the
transport without touching business logic. That's Adapter plus Dependency Inversion, and it's the
answer to "how would you integrate with another team's service".
</details>

---

### 7. Observer.

<details><summary>Answer</summary>

**One-to-many notification:** when the subject's state changes, all registered observers are told.
Publish/subscribe at the object level.

```java
interface TradeListener { void onTrade(Trade t); }          // observer

class TradeBook {                                           // subject
    private final List<TradeListener> listeners = new CopyOnWriteArrayList<>();
    public void register(TradeListener l)   { listeners.add(l); }
    public void unregister(TradeListener l) { listeners.remove(l); }
    private void publish(Trade t) { for (var l : listeners) l.onTrade(t); }
}
```
**Note the `CopyOnWriteArrayList`** — a listener registry is *the* canonical fit for it: many reads
(every event), almost no writes (registration at startup), and iteration must not throw
`ConcurrentModificationException` if someone registers mid-notification (`quiz/18` Q19).

**The problems, which is what a good answer covers:**
1. **The leak.** Register and never unregister and the subject pins every observer forever. Worse if the
   observer is a **non-static inner class or a capturing lambda**, because it also pins its enclosing
   object — measured in `problems/gc-labs/L02Leak.java`, where a `List<Runnable>` of capturing lambdas
   OOM'd a 128 MB heap in ~2 seconds. **Weak references or explicit deregistration.**
2. **Ordering** is unspecified; don't let observers depend on each other.
3. **A slow or throwing observer** blocks or breaks the notification loop for everyone after it. Wrap
   each call, and consider async dispatch.
4. **Debugging** — no call stack from cause to effect. You need correlation ids.
5. **Reentrancy** — an observer that mutates the subject during notification.

**Where you see it:** Spring's `ApplicationEventPublisher`/`@EventListener`, Swing/AWT listeners,
`PropertyChangeListener`, and — at the architectural scale — **Reactive Streams**, which is Observer
plus **backpressure**, the thing plain Observer lacks (`quiz/06` B4).

**`java.util.Observer`/`Observable` were deprecated in JDK 9** — they were untyped, not serialisable,
and had no ordering or threading guarantees. Don't propose them.
</details>

---

### 8. Strategy.

<details><summary>Answer</summary>

**Encapsulate an algorithm behind an interface so it can be swapped at runtime.** Composition instead
of conditionals.

```java
interface FeeStrategy { Money fee(Trade t); }

class FlatFee    implements FeeStrategy { public Money fee(Trade t) { return Money.of(5); } }
class TieredFee  implements FeeStrategy { … }
class ExemptFee  implements FeeStrategy { public Money fee(Trade t) { return Money.ZERO; } }

class Billing {
    private final FeeStrategy strategy;                       // injected, not chosen internally
    Billing(FeeStrategy s) { this.strategy = s; }
    Money charge(Trade t) { return strategy.fee(t); }
}
```

**The problem it removes:** the `if (clientType == RETAIL) … else if (clientType == INSTITUTIONAL) …`
chain, repeated in five places, that you must edit in all five whenever a new client type appears.
Strategy replaces it with one dispatch and a new class.

**In modern Java, a strategy is usually just a lambda** — say this, it's the honest version:
```java
Money charge(Trade t, Function<Trade, Money> fee) { return fee.apply(t); }
charge(t, x -> Money.of(5));
```
So the JDK's `Comparator`, `Predicate`, `RejectedExecutionHandler` and `ThreadFactory` are all
strategies; you just don't write the ceremony. That's the "patterns are workarounds for missing
language features" point from Q1.

**Strategy vs Template Method** — the pair to distinguish:

| | Strategy | Template Method |
|---|---|---|
| Mechanism | **composition** | **inheritance** |
| Swap when | **runtime** | compile time (subclass) |
| Varies | the whole algorithm | specific **steps** |
| Coupling | loose | tight (subclass knows the parent) |

**Strategy vs State:** structurally identical. Strategy is selected by the **client** and doesn't change
itself; State **transitions itself** in response to events. A trade lifecycle is State (`quiz/12` Q20);
a fee calculation is Strategy.
</details>

---

### 9. Template Method.

<details><summary>Answer</summary>

**Define the skeleton of an algorithm in a base class, with specific steps deferred to subclasses.**
The invariant sequence is fixed; the variable parts are hooks.

```java
abstract class SettlementJob {
    public final void run() {                    // FINAL — the skeleton can't be reordered
        var batch = load();                      // hook
        validate(batch);                          // fixed step
        var result = process(batch);              // hook
        persist(result);                          // fixed step
        notifyDownstream(result);                 // hook with a default
    }
    protected abstract Batch load();
    protected abstract Result process(Batch b);
    protected void notifyDownstream(Result r) { }  // optional hook — no-op default
    private void validate(Batch b) { … }           // invariant, not overridable
}
```

**The two structural details that make it correct:**
1. **`run()` is `final`.** That's the point — a subclass can fill in steps but cannot change the order.
   A non-final template method is a broken template method.
2. **Hooks are `protected`**, and optional ones have a **default no-op** body so subclasses only
   override what they care about.

**The JDK/Spring examples to cite:** `JdbcTemplate` (Spring manages connection, transaction, exception
translation and cleanup; you supply the SQL and the row mapper), `RestTemplate`, `AbstractList` (you
implement `get` and `size`, you inherit everything else), `InputStream.read(byte[])` in terms of
`read()`, JUnit's setup/test/teardown lifecycle, and `ThreadPoolExecutor.beforeExecute`/`afterExecute`.

**The criticism:** it's **inheritance**, so it's tight coupling. The subclass depends on the parent's
internals, the base class can't evolve freely (the fragile-base-class problem), and Java's single
inheritance means you've spent the one slot you have. **Strategy or a higher-order function is usually
the better modern choice** — pass the varying steps in as lambdas rather than subclassing:
```java
void run(Supplier<Batch> load, Function<Batch,Result> process) { … }
```
That's Template Method with composition, and it's what `JdbcTemplate` effectively does with its
callback interfaces.
</details>

---

### 10. Command.

<details><summary>Answer</summary>

**Encapsulate a request as an object**, so it can be stored, queued, passed around, logged, undone, or
executed later.

```java
interface Command { void execute(); default void undo() { throw new UnsupportedOperationException(); } }

class SettleTradeCommand implements Command {
    private final TradeId id; private final SettlementService svc;   // all state needed
    public void execute() { svc.settle(id); }
    public void undo()    { svc.reverse(id); }                        // compensating action
}

// The invoker knows nothing about what the command does:
class CommandQueue {
    private final Deque<Command> history = new ArrayDeque<>();
    void submit(Command c) { c.execute(); history.push(c); }
    void undoLast() { history.pop().undo(); }
}
```

**What it gives you that a direct method call doesn't:**
- **Queueing and scheduling** — a command is an object, so it can sit in a queue. **`Runnable` is the
  Command pattern**, and that's why `ExecutorService` works: it takes commands and decides when and
  where to run them.
- **Undo/redo** — keep a history and call `undo()`. Requires each command to capture enough state.
- **Logging and replay** — persist commands and re-execute them.
- **Macro commands** — a `Command` containing a list of commands.
- **Decoupling** — the invoker (a button, a queue, a scheduler) needn't know the receiver.

**In modern Java, a command is a lambda:** `executor.submit(() -> svc.settle(id))`. Which is why Q1 says
Command is a closure — a closure *is* captured state plus behaviour, which is exactly the pattern's
definition.

**The connection worth making for this role:** a **command in CQRS** is this pattern taken
architecturally — an intent, addressed to one aggregate, that **can be rejected**, distinct from the
**event** that records what happened (`quiz/13` Q2). And the undo/compensating-action mechanism is
exactly what a **saga** does when a later step fails (`quiz/13` Q11). Making that link shows you see
patterns at both scales.
</details>

---

### 11. Iterator.

<details><summary>Answer</summary>

**Traverse a collection without exposing its internal representation.**

```java
public interface Iterator<E> {
    boolean hasNext();
    E next();                                  // throws NoSuchElementException when exhausted
    default void remove()      { throw new UnsupportedOperationException(); }
    default void forEachRemaining(Consumer<? super E> a) { … }
}
```
Implementing **`Iterable<E>`** (one method, `iterator()`) is what makes your type work with the
**for-each** loop — so the pattern is baked into the language syntax.

```java
class EventLog implements Iterable<Event> {
    public Iterator<Event> iterator() {
        return new Iterator<>() {                     // representation stays private
            private int i = 0;
            public boolean hasNext() { return i < size; }
            public Event next() {
                if (!hasNext()) throw new NoSuchElementException();
                return storage[i++];
            }
        };
    }
}
for (Event e : log) { … }                              // caller knows nothing about storage
```

**Why it matters:** the caller iterates a `List`, a `Set`, a tree, a database cursor or a paginated API
with the same code. It also allows **multiple simultaneous traversals** (each iterator holds its own
cursor), which an index-based loop over an abstract collection can't.

**The behaviours to know** — this is where the real content is:
- **Fail-fast** (`ArrayList`, `HashMap`): structural modification during iteration throws
  **`ConcurrentModificationException`** on the next `next()`, via a `modCount` check. It is
  **best-effort** — never write logic that depends on it. And the most common cause is **single-threaded**:
  removing inside a for-each loop. Fix with `Iterator.remove()`, `removeIf()`, or collect-then-remove.
- **Fail-safe / weakly consistent** (`CopyOnWriteArrayList`, `ConcurrentHashMap`): a snapshot or weakly
  consistent view — no CME, but you may not see concurrent updates (`quiz/16` Q19, `quiz/18` Q13).
- **`ListIterator`** adds bidirectional traversal, `set` and `add`.
- **`Spliterator`** (JDK 8) is the modern relative — it supports **splitting** for parallel traversal,
  and it's what streams are built on. `tryAdvance` + `trySplit` + characteristics
  (`ORDERED`, `SIZED`, `IMMUTABLE`…). If you implement a custom collection for streams, you implement
  this, not `Iterator`.

**Internal vs external iteration** — the framing that makes this current: `Iterator` is **external**
(the caller drives). `forEach` and streams are **internal** (the collection drives), which is what
allows laziness, fusion and parallelism (`quiz/17` Q8).
</details>

---

### 12. Which patterns does Spring itself use?

<details><summary>Answer</summary>

Worth having ready, because "give an example" is best answered with something the interviewer already
depends on.

| Pattern | In Spring / the JDK |
|---|---|
| **Proxy** | **`@Transactional`, `@Async`, `@Cacheable`, `@Retryable`, `@PreAuthorize`** — all AOP proxies |
| **Front Controller** | `DispatcherServlet` |
| **Template Method** | `JdbcTemplate`, `RestTemplate`, `TransactionTemplate` |
| **Factory Method** | every `@Bean` method; `Executors.*` |
| **Singleton** | the default bean scope — container-managed, not the static kind |
| **Dependency Injection / IoC** | the entire container |
| **Decorator** | servlet `Filter` chains, `HttpServletRequestWrapper`, `Collections.unmodifiable*` |
| **Adapter** | `HandlerAdapter`, `Arrays.asList`, `InputStreamReader` |
| **Observer** | `ApplicationEventPublisher` / `@EventListener` |
| **Strategy** | `Comparator`, `RejectedExecutionHandler`, `ThreadFactory`, any injected interface |
| **Builder** | `StringBuilder`, `HttpRequest.newBuilder()`, `Caffeine.newBuilder()`, Lombok `@Builder` |
| **Iterator** | `Iterable`/`for-each`, `Spliterator` |
| **Chain of Responsibility** | servlet filter chain, Spring Security filter chain |
| **Repository / DAO** | `@Repository`, Spring Data |

**The single highest-value one to raise is Proxy**, because it explains a real bug class rather than
just naming a structure: **self-invocation bypasses the proxy**, so `@Transactional` on a method called
via `this.method()` silently does nothing — and only `public` methods are advised, and `final` classes
can't be CGLIB-proxied (`quiz/10` Q4/Q12, `quiz/16` Q16).

Knowing *why* — that it's the Proxy pattern and `this` isn't the proxy — is the difference between
memorising the gotcha and understanding it.
</details>

---

### 13. Which SOLID principle underlies most of these patterns?

<details><summary>Answer</summary>

> `quiz/12` Q17 has SOLID in full. The pattern connection:

**Dependency Inversion** and **Open/Closed** do most of the work.

- **Strategy, Factory, Adapter, Observer, Command** all rest on **"depend on an abstraction, not a
  concretion"** — that's Dependency Inversion, and it's what makes each of them testable and swappable.
- **Decorator, Strategy, Observer** are **Open/Closed** in action: you add behaviour by adding a class,
  without modifying existing code.
- **Template Method** is the inheritance-flavoured version of Open/Closed — extend by subclassing.
- **Interface Segregation** is why patterns use small interfaces: a `Command` has one method, a
  `Comparator` has one method. That's what makes them lambda-compatible in Java 8+.
- **Liskov** is what makes polymorphic dispatch safe at all — a decorator must honour the wrapped
  interface's contract, or every client breaks.

**And the honest caveat**, which is the same one as Q1: SOLID and the pattern catalogue are **heuristics
for managing change**, not laws. Applied mechanically you get an interface per class and a factory per
interface — worse than the problem. Apply them where you **expect change**.

**The one you'd never compromise is Dependency Inversion**, because it's what makes code testable —
and testability is the property that actually determines whether a codebase can be changed safely.
</details>

---

### 14. Name a pattern you'd argue *against* using.

<details><summary>Answer</summary>

A good closing question to be ready for, because it tests judgement over recall.

**Singleton** — the strongest candidate, for all the reasons in Q3: global mutable state, hidden
dependencies, untestable, and a concurrency hazard the moment it has fields. **Use dependency injection
instead**; you get one instance without the global-state problems.

**Others worth naming:**
- **Service Locator** — widely considered an anti-pattern now. It hides dependencies in a lookup
  (`locator.get(Foo.class)`) instead of declaring them in the constructor, so you can't tell what a
  class needs and can't fail fast at startup. DI superseded it.
- **Visitor** — solves a real problem (adding operations to a closed hierarchy) with enormous
  ceremony and double dispatch. In modern Java, **`sealed` interfaces plus `switch` pattern matching**
  do the same job with compiler-checked exhaustiveness and none of the boilerplate (`quiz/16` Q25).
- **Prototype** — largely obsolete, because it depends on `clone()`, which is itself broken
  (`quiz/16` Q11). Use a copy constructor.
- **Inheritance-heavy Template Method** where a higher-order function would do (Q9).
- **Abstract Factory** for a single implementation — pure indirection.

**The general principle to state:** the patterns that have aged worst are the ones that were
**workarounds for missing language features** (Q1). First-class functions killed most of the
behavioural boilerplate; DI containers killed Singleton and Service Locator; sealed types plus pattern
matching killed Visitor.

**And the corollary:** the patterns that have aged *well* are the ones describing genuine structural
relationships — Adapter, Decorator, Proxy, Observer. Those are about how systems are composed, not
about working around the language.
</details>

---

## Score yourself

- **11+** — good.
- **The source list's 20 questions are all here**, but the value isn't in reciting definitions. For each
  pattern be able to (a) name it in one sentence, (b) point at it in the JDK or Spring, and (c) say
  what its **cost** is. Interviewers ask (c) to separate reading from experience.
- **Q1's framing and Q14 are what a senior answer sounds like** — patterns as vocabulary with
  trade-offs, not a checklist to satisfy.
