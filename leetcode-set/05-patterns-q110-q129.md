# Q110–Q129 — design patterns ("Edit 1")

The post pairs each pattern with "can you give an example?" — so these are answered as **10 pairs**, the
first with the concept and the second with runnable-shaped code plus a JDK/Spring sighting. That's how
they'll be asked too: the follow-up is always "where have you actually used it?"

---

### 110. What are design patterns, and how do they help in Java programming?

<details><summary>Answer</summary>

Named, reusable solutions to recurring design problems — catalogued by the "Gang of Four" in 1994.

**How they help, honestly:** mostly as a **shared vocabulary**. "It's a Strategy" replaces two minutes of
explanation. They also encode designs you'd eventually arrive at anyway, which shortens the arriving.

The senior addendum, which is what distinguishes a good answer: **several GoF patterns are workarounds for
what 1994 languages lacked.** Strategy and Command are first-class functions. Iterator is `for-each`.
Factory Method is often just a static method or a constructor reference. In a language with lambdas and
algebraic data types several patterns dissolve into one line — which is exactly why a Scala codebase looks
pattern-light while following the same principles.

And the cost: pattern-driven design produces `AbstractSingletonProxyFactoryBean`. A pattern is a solution
to a problem you **have**; applying one pre-emptively is speculative generality.

→ `quiz/19` Q1
</details>

---

### 111. Can you explain the difference between creational, structural, and behavioral design patterns in Java?

<details><summary>Answer</summary>

- **Creational** — how objects get **made**: Singleton, Factory Method, Abstract Factory, Builder,
  Prototype.
- **Structural** — how objects are **composed**: Adapter, Decorator, Proxy, Facade, Composite, Bridge,
  Flyweight.
- **Behavioural** — how objects **interact** and distribute responsibility: Observer, Strategy, Command,
  Template Method, Iterator, State, Chain of Responsibility, Visitor, Mediator, Memento.

Behavioural is the largest group and the most relevant here — **Command and Observer are structurally what
event sourcing and CQRS read-model projections are.**

Say one more thing, because it shows you've used them rather than memorised them: **the taxonomy is a weak
framework.** Adapter, Decorator and Proxy are structurally *identical* — same interface, wrapped object —
and differ only in **intent**. So use the categories to organise recall, never as an argument.

→ `quiz/19` Q2
</details>

---

### 112. What is the Singleton pattern, and how is it implemented in Java?

<details><summary>Answer</summary>

"Exactly one instance, globally reachable." The implementations, worst to best:

1. **Eager static field** — simple, not lazy.
2. **`synchronized` getter** — locks on every read.
3. **Double-checked locking** — correct **only** with `volatile`; without it the JMM permits a caller to
   see a partially-constructed object. `problems/java/P07` demonstrates the broken version.
4. **Holder idiom** — `private static class Holder { static final X I = new X(); }`. Lazy and thread-safe
   by class-initialisation locking, with zero synchronisation on reads. Best non-enum option.
5. **`enum`** — one instance guaranteed by the JVM, **serialisation-safe for free**, and
   **reflection-proof**. The best one.

What the interviewer is usually probing is whether you know the **criticisms**: it's global mutable state
with a **hidden dependency** (the signature lies about what the class needs), it makes tests
order-dependent and unparallelisable, and you can't substitute a fake without a static setter.

So the answer that scores: **use dependency injection instead.** A Spring singleton-scoped bean gives you
"one instance" with a managed lifecycle and the dependency declared in the constructor — and note it's one
per `ApplicationContext`, **not per JVM**, so it isn't the GoF pattern at all.

→ `quiz/19` Q3
</details>

---

### 113. Can you give an example of the Singleton pattern in Java?

<details><summary>Answer</summary>

```java
public enum RateCache {
    INSTANCE;
    private final Map<String, BigDecimal> rates = new ConcurrentHashMap<>();
    public BigDecimal rate(String ccy) { return rates.get(ccy); }
}
```

Defeats both attacks that break the other forms: `setAccessible(true)` on a private constructor (you
cannot construct an enum reflectively) and deserialisation creating a second instance (enums have built-in
serialisation identity, no `readResolve` needed).

The holder idiom, when you need a real class:
```java
class RateCache {
    private RateCache() {}
    private static class Holder { static final RateCache I = new RateCache(); }
    static RateCache get() { return Holder.I; }
}
```

**JDK sightings:** `Runtime.getRuntime()`, `Collections.emptyList()` (an immutable shared instance),
`Comparator.naturalOrder()`. Note what those have in common — they're **stateless or immutable**, which is
the only case where singleton-ness is uncontroversial.

The version to avoid in an interview answer:
```java
if (instance == null) instance = new Thing();   // race; and non-volatile DCL is no better
```

→ `quiz/19` Q3; `problems/java/P07`
</details>

---

### 114. What is the Factory Method pattern, and how is it used in Java?

<details><summary>Answer</summary>

Define an overridable **creator** method so subclasses decide which concrete product to instantiate — "new
by delegation instead of `new` in place". The caller depends on the product *interface* only.

Distinguish it from its two neighbours, because interviewers conflate all three:
- **Factory Method** — one overridable method, **one** product type. `Collection.iterator()`,
  `ThreadFactory.newThread()`.
- **Abstract Factory** — one object producing a **family** of products that must be used together.
  JDBC's `Connection` handing out `Statement`/`PreparedStatement`/`CallableStatement` for the same vendor.
- **Static factory method** — `List.of`, `Integer.valueOf`, `Optional.of`. Not GoF at all: it's about
  naming, caching and returning a subtype. Effective Java Item 1 is about *this*, not about Factory
  Method.

The test for Abstract Factory is **"must these vary together?"** If not, you've built two independent
factories with extra ceremony — which is where the pattern earns its enterprise-bloat reputation.

→ `quiz/19` Q4
</details>

---

### 115. Can you give an example of the Factory Method pattern in Java?

<details><summary>Answer</summary>

```java
interface SettlementInstruction { void send(); }

abstract class SettlementGateway {
    protected abstract SettlementInstruction create(Trade t);   // the factory method

    public final void settle(Trade t) {                          // shared algorithm
        var instruction = create(t);
        audit.record(instruction);
        instruction.send();
    }
}

class SwiftGateway extends SettlementGateway {
    protected SettlementInstruction create(Trade t) { return new Mt541(t); }
}
```

Note this is Factory Method **inside** a Template Method — which is how it usually appears, and worth
saying, because "the base class owns the algorithm and defers one step" is the actual shape.

In modern Java you'd often skip the hierarchy entirely:
```java
Map<Venue, Function<Trade, SettlementInstruction>> factories = Map.of(
    SWIFT, Mt541::new, DTCC, DtccInstruction::new);
```
A constructor reference **is** a factory. Say that — it shows you know when the pattern has dissolved.

**JDK/Spring sightings:** `Collection.iterator()`, `Calendar.getInstance()`,
`DocumentBuilderFactory.newDocumentBuilder()`, Spring's `BeanFactory` and `FactoryBean<T>`.

→ `quiz/19` Q4
</details>

---

### 116. What is the Decorator pattern, and how is it used in Java?

<details><summary>Answer</summary>

Wrap an object in another object **implementing the same interface**, adding behaviour, and compose at
**runtime**.

The requirement is that same-interface-in-same-interface-out property — it's what allows arbitrary
stacking, and what makes the pattern combinatorial: 3 features means **3 decorators** instead of 8
subclasses for every combination.

Costs to acknowledge: **many small classes**, debugging through a deep wrapper stack, **broken identity**
(`equals`/`==` no longer match the underlying object), and the fact that a decorator can't add methods
*outside* the interface without defeating the point.

Versus its structural twins: **Adapter** changes the interface, **Decorator** keeps it and adds behaviour,
**Proxy** keeps it and controls *access*.

→ `quiz/19` Q5
</details>

---

### 117. Can you give an example of the Decorator pattern in Java?

<details><summary>Answer</summary>

The canonical one is `java.io`:
```java
var in = new BufferedReader(new InputStreamReader(new FileInputStream(f), UTF_8));
```
Three layers, one concern each: byte source → character decoding → buffering.

Your own, in this domain:
```java
interface PriceFeed { Quote quote(String sym); }

record CachingFeed(PriceFeed delegate, Cache<String, Quote> cache) implements PriceFeed {
    public Quote quote(String sym) { return cache.get(sym, delegate::quote); }
}
record MeteredFeed(PriceFeed delegate, Timer timer) implements PriceFeed {
    public Quote quote(String sym) { return timer.record(() -> delegate.quote(sym)); }
}

PriceFeed feed = new MeteredFeed(new CachingFeed(new RemoteFeed(), cache), timer);
```
Each layer is independently testable and the order is a deliberate choice — metering outside caching
measures cache hits; inside, it measures only misses. That's a good detail to volunteer.

**JDK sightings:** `java.io` streams, `Collections.unmodifiableList`/`synchronizedList`,
`HttpServletRequestWrapper`.

→ `quiz/19` Q5
</details>

---

### 118. What is the Adapter pattern, and how is it used in Java?

<details><summary>Answer</summary>

**"These two don't fit together."** An adapter converts one interface into another the client expects —
same behaviour, different shape. Also called Wrapper.

Two flavours: **object adapter** (composition — holds the adaptee; preferred) and **class adapter**
(inheritance — impossible in Java for two concrete classes, since there's one superclass slot).

**Why it matters for this role specifically.** It's the design answer to the "endpoint enriched from
another team's service" question: define **your own** client interface and put their SDK or Feign client
behind an adapter. Then their DTOs, their exception types and their timeout behaviour stop at the boundary
instead of leaking into your domain — and you can test your service with a two-line fake.

Distinguish from Facade, which also wraps but **simplifies a whole subsystem** rather than translating one
interface.

→ `quiz/19` Q6; `quiz/10` Q1
</details>

---

### 119. Can you give an example of the Adapter pattern in Java?

<details><summary>Answer</summary>

```java
// what MY domain wants
interface SsiLookup { Optional<Ssi> forTrade(TradeId id); }

// what the other team gives me
class RefDataClient { RefDataResponse fetch(String tradeRef) throws RefDataException { … } }

// the adapter
class RefDataSsiAdapter implements SsiLookup {
    private final RefDataClient client;

    public Optional<Ssi> forTrade(TradeId id) {
        try {
            var raw = client.fetch(id.value());
            return raw.found() ? Optional.of(toSsi(raw)) : Optional.empty();
        } catch (RefDataException e) {
            throw new SsiUnavailableException(id, e);      // their failure type stops here
        }
    }
}
```

Everything ugly about the upstream API — the string ID, the checked exception, the `found()` flag, the
wire DTO — is contained in one class that has one job. My service depends on `SsiLookup`.

**JDK sightings:** `InputStreamReader` (byte stream → char stream — the clearest example in the JDK),
`Arrays.asList` (array → `List`), `Collections.enumeration` (`Iterator` → `Enumeration`).
**Spring:** `HandlerAdapter`, `HandlerInterceptorAdapter`.

→ `quiz/19` Q6
</details>

---

### 120. What is the Observer pattern, and how is it used in Java?

<details><summary>Answer</summary>

A subject maintains a list of observers and notifies them when its state changes — one-to-many, with the
subject not knowing the concrete observer types.

**Its two production hazards, which are the real answer:**
1. **Listener leaks.** An observer registered and never deregistered keeps its whole object graph alive.
   `problems/gc-labs/L02Leak.java` OOMs a 128 MB heap in about 2 seconds this way, with a
   `List<Runnable>` of capturing lambdas.
2. **Synchronous coupling.** Naive `for (l : listeners) l.onEvent(e);` runs on the **publisher's thread**,
   so one slow observer adds its latency to the publisher, and one thrown exception stops the remaining
   observers from being notified at all. Catch per-listener; consider dispatching asynchronously.

Java's own `java.util.Observer`/`Observable` were **deprecated in JDK 9** (not thread-safe, not
serialisable, no event typing).

And the framing that matters for this JD: **event-driven architecture is Observer across a process
boundary**, with durability and replay added — which is where AMPS/Kafka come in, and what Observer itself
lacks (**backpressure**).

→ `quiz/19` Q7
</details>

---

### 121. Can you give an example of the Observer pattern in Java?

<details><summary>Answer</summary>

Spring's version, which is what you'd actually write:
```java
publisher.publishEvent(new TradeSettled(tradeId, settledAt));

@EventListener
void onSettled(TradeSettled e) { projection.apply(e); }         // add @Async to decouple threads
```

Hand-rolled, with the hazards handled:
```java
class TradeBook {
    private final List<Consumer<TradeSettled>> listeners = new CopyOnWriteArrayList<>();

    AutoCloseable subscribe(Consumer<TradeSettled> l) {          // return the unsubscribe handle
        listeners.add(l);
        return () -> listeners.remove(l);                        // so callers CAN deregister
    }
    private void publish(TradeSettled e) {
        for (var l : listeners) {
            try { l.accept(e); } catch (RuntimeException ex) { log.error("listener failed", ex); }
        }
    }
}
```
`CopyOnWriteArrayList` because the set is small and read-constantly; returning an `AutoCloseable`
because "you must remember to deregister" is how leaks happen; per-listener catch because one bad
observer shouldn't silence the rest.

**JDK sightings:** `Flow.Subscriber`/reactive streams (Observer **plus backpressure**),
`PropertyChangeListener`, every Swing/Android listener. Modern alternatives: an EventBus, or a broker.

→ `quiz/19` Q7
</details>

---

### 122. What is the Strategy pattern, and how is it used in Java?

<details><summary>Answer</summary>

Encapsulate interchangeable algorithms behind one interface and select at runtime. The client depends on
the interface, so adding an algorithm doesn't edit existing code — Open/Closed, concretely.

**In modern Java, Strategy is a function.** `Comparator` is the JDK's Strategy interface, and
`list.sort(comparing(Trade::getId))` is the pattern with no classes involved.

The alternative worth weighing out loud: **sealed interface + exhaustive `switch`** (JDK 21). A `Map` of
strategies is *extensible* — add an entry, no edits. A sealed hierarchy is *checked* — the compiler tells
you when a case is missing. Centralised-and-checked versus open-and-extensible; say which you're choosing
and why. That's a better answer than reciting the UML.

Versus **Template Method**: Strategy varies behaviour by **composition** at runtime; Template Method by
**inheritance** at compile time.

→ `quiz/19` Q8
</details>

---

### 123. Can you give an example of the Strategy pattern in Java?

<details><summary>Answer</summary>

Registry form, which replaces a growing `switch`:
```java
Map<AssetClass, Function<Trade, Fee>> feeStrategies = Map.of(
    EQUITY,       t -> t.notional().multiply(EQUITY_BPS),
    FIXED_INCOME, t -> t.notional().multiply(FI_BPS).add(t.accrued()));

Fee fee = feeStrategies.get(trade.assetClass()).apply(trade);
```
Adding an asset class means adding an entry, not editing a method.

Spring form, which is what you'd ship — let the container collect every implementation:
```java
interface FeeStrategy { AssetClass appliesTo(); Fee calculate(Trade t); }

@Service
class FeeCalculator {
    private final Map<AssetClass, FeeStrategy> byClass;
    FeeCalculator(List<FeeStrategy> strategies) {                    // all beans injected
        this.byClass = strategies.stream()
            .collect(toMap(FeeStrategy::appliesTo, identity()));
    }
}
```
A new `@Service` implementing `FeeStrategy` is now automatically wired in — no edit to `FeeCalculator`.

**JDK sightings:** `Comparator`, `ThreadPoolExecutor`'s `RejectedExecutionHandler` (four supplied
strategies), `ThreadFactory`, `Collector`.

→ `quiz/19` Q8
</details>

---

### 124. What is the Template Method pattern, and how is it used in Java?

<details><summary>Answer</summary>

A base class fixes the **algorithm skeleton** and lets subclasses fill in named steps. Variation is chosen
at **compile time** by which subclass you instantiate — and you get one axis of it, because Java has
single inheritance.

Two details that show you've used it:
- The template method itself should be **`final`**, so subclasses can't break the skeleton, and the steps
  `protected`, not `public`.
- Distinguish a **hook** (a `protected` no-op the subclass *may* override) from an **abstract step** (it
  *must*).

Where it's used: `AbstractList`, `InputStream.read()`, `HttpServlet`'s `doGet`/`doPost`, and
`JdbcTemplate`.

Modern preference is Strategy — **favour composition over inheritance** — because a template method is a
**fragile base class**: change the skeleton and every subclass may break. `JdbcTemplate` is interesting
precisely because it's Template Method that takes its steps as *parameters* (`RowMapper`), which is
Template Method converging on Strategy.

→ `quiz/19` Q9
</details>

---

### 125. Can you give an example of the Template Method pattern in Java?

<details><summary>Answer</summary>

```java
abstract class ReconciliationJob {
    public final Report run(LocalDate date) {           // final: the skeleton is not negotiable
        var ours   = loadInternal(date);
        var theirs = loadExternal(date);
        var breaks = match(ours, theirs);               // overridable step
        onBreaks(breaks);                              // hook -- default does nothing
        return Report.of(date, breaks);
    }

    protected abstract List<Position> loadExternal(LocalDate d);   // must implement
    protected List<Break> match(…) { return defaultMatcher(…); }   // may override
    protected void onBreaks(List<Break> b) { }                     // hook
}
```
Every subclass gets identical ordering, logging and error handling; a custodian-specific job supplies only
`loadExternal` and perhaps a tolerance-based `match`.

The JDK/Spring example to name is **`JdbcTemplate`**: it owns connection acquisition, exception
translation into `DataAccessException`, and cleanup — the parts everyone gets wrong — while you supply the
`RowMapper`:
```java
jdbc.query("select * from trade where book = ?", tradeRowMapper, book);
```
Same family: `RestTemplate`, `TransactionTemplate`, `HttpServlet.service()`.

→ `quiz/19` Q9, Q12
</details>

---

### 126. What is the Command pattern, and how is it used in Java?

<details><summary>Answer</summary>

Turn "call this method with these arguments" into an **object** — a value you can pass around, queue, log,
authorise, retry, send over a network, and store.

**Why this is the most JD-relevant pattern on the list:** reifying a request as an object is *exactly*
what a command in event sourcing / CQRS is. `SettleTrade(tradeId, date, amount)` is a command object; the
handler validates it and emits events. The events — `TradeSettled` — are the same idea applied to
**facts** rather than **intents**.

So be able to state the distinction crisply: **a command is an intent, imperative, and may be rejected; an
event is a fact, past tense, and immutable.** That one sentence answers both "do you know Command?" and
half of "do you understand event sourcing?".

Undo is the textbook motivation, and in post-trade it has a real analogue: you don't delete a settlement,
you book a **compensating transaction** — a reversal.

→ `quiz/19` Q10; `quiz/13`
</details>

---

### 127. Can you give an example of the Command pattern in Java?

<details><summary>Answer</summary>

In modern Java a command is a **record** plus a handler — no class hierarchy needed:

```java
sealed interface Command permits SettleTrade, CancelTrade {}
record SettleTrade(TradeId id, LocalDate value, BigDecimal amount) implements Command {}
record CancelTrade(TradeId id, String reason)                      implements Command {}

List<Event> handle(Command c, Trade state) {
    return switch (c) {                                       // exhaustive -- compiler enforced
        case SettleTrade s -> state.settle(s.value(), s.amount());
        case CancelTrade x -> state.cancel(x.reason());
    };
}
```
Because the command is data, you get audit, replay, idempotency keys and retry **for free** — the same
object that drove the change is the one you logged.

The simplest example is one you use daily without naming it:
```java
executor.submit(() -> settle(tradeId));     // Runnable IS a Command
```
`Runnable`, `Callable` and `ActionListener` are all Command; a queue of them is a command queue.

**Sightings:** every `ExecutorService.submit` argument, Swing `Action`, Spring Batch's `Tasklet`, and any
CQRS `CommandHandler`.

→ `quiz/19` Q10
</details>

---

### 128. What is the Iterator pattern, and how is it used in Java?

<details><summary>Answer</summary>

Provide sequential access to a collection's elements **without exposing its representation**. Implement
`Iterable<T>` and you get `for-each` plus encapsulation — callers traverse without seeing or being able to
mutate your internal structure.

Two flavours worth naming: **external** iteration (`Iterator` — the caller pulls, and can stop early) and
**internal** iteration (`forEach` — the collection pushes, so it chooses the traversal but early exit is
awkward). Streams are internal iteration with laziness added.

The failure modes are the interesting part, and they're really a concurrency question:
- **Fail-fast** — `ArrayList`'s iterator tracks `modCount` and throws `ConcurrentModificationException`.
  Despite the name, the usual cause is **single-threaded**: removing inside a for-each. Detection is
  **best-effort**, so never write logic that depends on the throw.
- **Weakly consistent** — `ConcurrentHashMap`: never throws, reflects some state since creation.
- **Snapshot** — `CopyOnWriteArrayList`: immutable copy, so `remove()` is unsupported.

And a subtlety: implementing `Iterable` does **not** give you `stream()` — you need
`StreamSupport.stream(spliterator(), false)`, and a proper `Spliterator` (with `trySplit`) is what makes
parallel streams over your type work.

→ `quiz/19` Q11; `quiz/16` Q19
</details>

---

### 129. Can you give an example of the Iterator pattern in Java?

<details><summary>Answer</summary>

Fix the CME first, since that's what the example is usually testing:
```java
for (Trade t : trades) if (t.isCancelled()) trades.remove(t);   // ConcurrentModificationException
trades.removeIf(Trade::isCancelled);                            // correct
```

A custom `Iterable` that hides its representation — and streams a resource lazily rather than loading it:
```java
class TradeFile implements Iterable<Trade>, AutoCloseable {
    private final BufferedReader reader;

    public Iterator<Trade> iterator() {
        return new Iterator<>() {
            private String line = advance();
            public boolean hasNext() { return line != null; }
            public Trade next() {
                if (line == null) throw new NoSuchElementException();
                var t = parse(line); line = advance(); return t;
            }
        };
    }
}

try (var file = new TradeFile(path)) {
    for (Trade t : file) process(t);        // constant memory over a 10 GB file
}
```
The caller writes an ordinary `for-each` and never learns whether the source is a list, a cursor, or a
paged remote API. That decoupling is the entire point of the pattern.

**JDK sightings:** every `Collection`, `Scanner`, `Files.newDirectoryStream`, `ResultSet` (an iterator in
all but name), and `Spliterator` as the stream-era version.

→ `quiz/19` Q11
</details>

---

## Where to go next

The post ends here, and it's a reasonable place to *stop reciting* and start doing. Three things it can't
test that this role will:

- **`quiz/13`** — event sourcing and CQRS. Command and Observer above are the patterns; that file is the
  architecture, and it's named explicitly in the JD.
- **`quiz/12`** — Scala and FP. Half the patterns above are one-liners there, and being able to say *which
  ones and why* is a strong answer.
- **`quiz/19` Q14** — "name a pattern you'd argue against." Have an answer with a consequence attached.
  Singleton on design grounds, Abstract Factory on cost grounds, Visitor because sealed types plus
  pattern matching replaced it. Refusing to pick reads as evasion.
