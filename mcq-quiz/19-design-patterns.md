# MCQ 19 — Design Patterns

14 questions. Source: [`quiz/19`](../quiz/19-design-patterns.md).

The JD names "design patterns, best engineering practices" explicitly, and Citi asked the Builder
question directly (`quiz/05` Q1). Expect *one* pattern question, and expect it to reward judgement over
recitation.

---

### Q1. What do design patterns actually buy you?

- **A.** Guaranteed extensibility
- **B.** Better performance
- **C.** A **shared vocabulary** for structures you'd arrive at anyway — plus, for several, a workaround for a missing language feature
- **D.** Compliance with SOLID

<details><summary>Answer</summary>

**C.** The honest answer, and the one that reads as senior. "It's a Strategy" replaces two minutes of
explanation. That's most of the value.

The second half is the part worth saying out loud: **many GoF patterns are workarounds for what C++ and
Java-of-1994 lacked.** Strategy and Command are first-class functions. Iterator is `for-each`. Factory
Method is often just a static method or a constructor reference. In a language with lambdas and ADTs,
several patterns dissolve into one line — which is exactly why a Scala codebase looks pattern-light while
following the same principles.

The cost side is real too: pattern-driven design produces `AbstractSingletonProxyFactoryBean`. Patterns
are named *solutions to recurring problems*, so the problem has to exist first. Reaching for one
pre-emptively is speculative generality.

→ `quiz/19` Q1
</details>

---

### Q2. Which of these is a **behavioural** pattern?

- **A.** **Observer**
- **B.** Decorator
- **C.** Builder
- **D.** Facade

<details><summary>Answer</summary>

**A.** The taxonomy in one line each:

- **Creational** — how objects get made: Singleton, Factory Method, Abstract Factory, **Builder**,
  Prototype.
- **Structural** — how objects are composed: Adapter, **Decorator**, Proxy, **Facade**, Composite,
  Bridge, Flyweight.
- **Behavioural** — how objects interact and distribute responsibility: **Observer**, Strategy, Command,
  Template Method, Iterator, State, Chain of Responsibility, Visitor, Mediator, Memento.

Behavioural is the largest group and the most relevant to the BofA role — Command and Observer are
structurally what **event sourcing** and **CQRS read-model projections** are.

The taxonomy itself is a weak framework (Adapter and Decorator and Proxy are structurally identical and
differ only in intent), so use it to organise recall, not as an argument.

→ `quiz/19` Q2
</details>

---

### Q3. The strongest criticism of Singleton?

- **A.** It's slow
- **B.** It's not thread-safe
- **C.** It can't be serialised
- **D.** It's **global mutable state with a hidden dependency**, which makes tests order-dependent and untestable in isolation

<details><summary>Answer</summary>

**D.** Three linked problems. The dependency is **invisible** — `Registry.getInstance()` buried in a
method means the signature lies about what the class needs. Tests **share state** across cases, so they
become order-dependent and unparallelisable. And you cannot substitute a fake without a static setter,
which is itself a new bug.

**B** is a *defect of specific implementations*, not of the pattern — and it's fixable (enum, holder
idiom, correct DCL with `volatile`). The design objection isn't.

`Runtime.getRuntime()` and `Logger` are the defensible cases: genuinely one-per-JVM, effectively
stateless. Beyond that, prefer **dependency injection** — a Spring singleton-scoped bean gives you the
"one instance" property with a managed lifecycle and the dependency declared in the constructor.

Worth flagging: a Spring **singleton bean is one per `ApplicationContext`**, not per JVM — so it is not
the GoF pattern, and that distinction is a good interview answer on its own.

→ `quiz/19` Q3; `mcq-quiz/16` Q21
</details>

---

### Q4. Factory Method vs Abstract Factory?

- **A.** Factory Method is static; Abstract Factory isn't
- **B.** Factory Method defers **one** product's creation to a subclass; Abstract Factory creates a **family** of related products behind one interface
- **C.** Abstract Factory is Factory Method plus caching
- **D.** They're the same pattern

<details><summary>Answer</summary>

**B.** Factory Method: one overridable creator, one product type
(`Collection.iterator()`, `ThreadFactory.newThread()`). Abstract Factory: one object producing several
products that must be used together (`DocumentBuilderFactory`, JDBC's `Connection` producing
`Statement`/`PreparedStatement`/`CallableStatement` for the same vendor).

The test for Abstract Factory is **"must these vary together?"** If a `PostgresConnection` must only ever
hand out Postgres statements, the family constraint is real. If not, you've built two independent
factories with extra ceremony — which is where the pattern earns its reputation for enterprise bloat.

Note **A** points at a real distinction the GoF book doesn't cover: a **static factory method**
(`List.of`, `Integer.valueOf`, `Optional.of`) is a *different* thing — it's about naming, caching, and
returning a subtype, not about deferring to subclasses. Effective Java Item 1 is about that, not about
Factory Method.

→ `quiz/19` Q4
</details>

---

### Q5. What does Decorator require?

- **A.** The decorator **implements the same interface** as the thing it wraps, and composes it at runtime
- **B.** The decorator subclasses the concrete type
- **C.** The decorated type must be `final`
- **D.** A shared abstract base class with template methods

<details><summary>Answer</summary>

**A.** Same interface in, same interface out — which is what allows arbitrary stacking:
`new BufferedReader(new InputStreamReader(new FileInputStream(f)))`. Each layer adds one concern
(buffering, decoding, byte source).

The value over inheritance is combinatorial: 3 features means 3 decorators, versus 8 subclasses for every
combination. And you compose at **runtime**, not compile time.

JDK examples: `java.io` streams (the canonical one), `Collections.unmodifiableList`/`synchronizedList`,
`HttpServletRequestWrapper`.

The costs interviewers like to hear acknowledged: **many small classes**, **debugging through a deep
wrapper stack**, **broken identity** (`equals`/`==` no longer match the underlying object), and the fact
that a decorator can't add methods *outside* the interface without defeating the point.

→ `quiz/19` Q5
</details>

---

### Q6. Proxy is structurally identical to Decorator. What distinguishes it?

- **A.** Proxy wraps interfaces, Decorator wraps classes
- **B.** Proxy is creational
- **C.** Proxy can only wrap one object
- **D.** **Intent** — Proxy controls *access* (lazy loading, security, remoting, transactions); Decorator adds *behaviour*

<details><summary>Answer</summary>

**D.** Both are structural, both wrap a same-interface object. The distinction is purely intent, which is
exactly why the pattern taxonomy is a weak framework and why these three questions come up together:

- **Adapter** — *"these two don't fit"*: **changes** the interface (`InputStreamReader`,
  `Arrays.asList`).
- **Decorator** — *"I want this plus something"*: **keeps** the interface, adds behaviour.
- **Proxy** — *"I want to control getting to this"*: keeps the interface, **mediates access**.

Java implementations: **JDK dynamic proxy** (`java.lang.reflect.Proxy`, **interfaces only**) and
**CGLIB/ByteBuddy** subclassing for concrete classes, which needs the class and methods to be non-`final`.
Hibernate lazy loading, RMI stubs, and all of Spring AOP are Proxy — hence `@Transactional`, `@Async`,
`@Cacheable`.

And hence the bug class: **self-invocation bypasses the proxy**, so `this.doWork()` silently skips the
advice; only `public` methods are advised.

→ `quiz/19` Q6; `quiz/16` Q16; `quiz/10` Q4
</details>

---

### Q7. The main production hazard with Observer?

- **A.** Observers can't be removed
- **B.** **Unremoved listeners leak, and synchronous notification means one slow or throwing observer blocks the publisher**
- **C.** It requires reflection
- **D.** Notification order is undefined

<details><summary>Answer</summary>

**B.** Both halves bite in real systems. The **leak** is the classic: a listener registered and never
deregistered keeps its whole object graph alive — measured in `gc-labs/L02Leak.java`, where a
`List<Runnable>` of capturing lambdas OOM'd a 128 MB heap in about 2 seconds. `WeakReference`-holding
registries are the mitigation, with the caveat that a listener with no other strong reference then
vanishes unpredictably.

The **coupling** half: naive `for (l : listeners) l.onEvent(e);` runs on the publisher's thread, so one
slow observer adds its latency to the publisher and one thrown exception stops the remaining observers
from being notified at all. Catch per-listener; consider dispatching asynchronously.

**D** is true but usually a design smell rather than a hazard — if your observers depend on notification
order, they aren't independent and shouldn't be observers.

Java's own `java.util.Observer`/`Observable` were **deprecated in JDK 9** (not thread-safe, not
serialisable, no event typing). Modern equivalents: Spring's `ApplicationEventPublisher`, an EventBus,
`Flow.Subscriber`/reactive streams (which add **backpressure**, the thing Observer lacks) — or,
architecturally, a message broker. Which is the point worth making for this role: **event-driven
architecture is Observer across a process boundary**, with durability and replay added.

→ `quiz/19` Q7
</details>

---

### Q8. In modern Java, Strategy is usually implemented as?

- **A.** An abstract class per strategy
- **B.** A `switch` on an enum
- **C.** **A lambda / method reference, often held in a `Map<Key, Function<…>>`**
- **D.** A separate microservice

<details><summary>Answer</summary>

**C.** Strategy *is* a function. `Comparator` is the JDK's Strategy interface, and
`list.sort(comparing(Trade::getId))` is the pattern with no classes involved.

The registry form is the one to reach for when you'd otherwise write a growing `switch`:
```java
Map<AssetClass, Function<Trade, Fee>> feeStrategies = Map.of(
    EQUITY,       t -> t.notional().multiply(EQUITY_BPS),
    FIXED_INCOME, t -> t.notional().multiply(FI_BPS));
```
Adding a case now means adding an entry, not editing a method — Open/Closed, concretely.

**B** isn't wrong as a *starting point*, and Java's **sealed interfaces + exhaustive pattern matching**
(JDK 21) make it genuinely good: the compiler tells you when a case is missing, which a `Map` cannot. The
trade is centralised-and-checked versus extensible-and-open. Say which you're choosing and why.

Spring's version: inject `List<FeeStrategy>` or `Map<String, FeeStrategy>` and let the container collect
every implementation.

→ `quiz/19` Q8
</details>

---

### Q9. Template Method vs Strategy?

- **A.** Template Method varies steps via **inheritance** (compile-time, one hierarchy); Strategy varies behaviour via **composition** (runtime, swappable)
- **B.** Template Method is behavioural, Strategy is structural
- **C.** Template Method requires a functional interface
- **D.** They differ only in naming

<details><summary>Answer</summary>

**A.** Template Method fixes the *algorithm skeleton* in a base class and lets subclasses fill in steps —
so the variation is chosen at compile time, by which subclass you instantiate, and you get one axis of
variation because Java has single inheritance.

JDK/Spring examples: `AbstractList`, `InputStream.read()`, `JdbcTemplate` (you supply the
`RowMapper`/`PreparedStatementSetter`, it owns connection/exception/resource handling), `HttpServlet`'s
`doGet`/`doPost`.

The **hook method** detail is worth knowing: a `protected` no-op the subclass *may* override, versus an
`abstract` method it *must*. And the discipline: the template method itself should be **`final`** so
subclasses can't break the skeleton, and the steps `protected`, not `public`.

Modern preference is Strategy — "**favour composition over inheritance**" — because a template method is
a fragile base class: change the skeleton and every subclass may break. `JdbcTemplate` is interesting
precisely because it's Template Method that takes its steps as *parameters*, which is Template Method
converging on Strategy.

→ `quiz/19` Q9
</details>

---

### Q10. Why does Command matter for this role specifically?

- **A.** It's how Spring MVC routes requests
- **B.** It enables undo, which post-trade systems need
- **C.** It's the basis of `Runnable`
- **D.** **Reifying a request as an object is exactly what a command in event sourcing/CQRS is** — serialisable, queueable, auditable, replayable

<details><summary>Answer</summary>

**D.** Command turns "call this method with these arguments" into a **value**. Once a request is data, you
can queue it, log it, retry it, authorise it, ship it across a network, and store it.

That is the CQRS write side, verbatim: `SettleTrade(tradeId, date, amount)` is a command object; the
handler validates it and emits events. And the events themselves — `TradeSettled` — are the same idea
applied to *facts* rather than *intents*. Which is why "do you know Command?" and "do you know event
sourcing?" have the same structural answer, and why naming the distinction (**command = intent, may be
rejected, imperative; event = fact, immutable, past tense**) is the high-value part.

**C** is true and worth mentioning — `Runnable`, `Callable`, `ActionListener` and every
`ExecutorService.submit` argument are Command — and in modern Java a command is often just a **record**
plus a lambda, no class hierarchy.

**B** is real but secondary: undo needs an `undo()` method or an inverse command, and in post-trade the
equivalent is a **compensating transaction** — you don't delete a settlement, you book a reversal.

→ `quiz/19` Q10; `quiz/12`
</details>

---

### Q11. What does implementing `Iterable` buy you over exposing the collection?

- **A.** Faster iteration
- **B.** **`for-each` support plus encapsulation** — callers traverse without seeing or mutating your internal structure
- **C.** Thread safety
- **D.** Automatic stream support with no extra code

<details><summary>Answer</summary>

**B.** The pattern's point is decoupling traversal from representation. `for (Trade t : book)` works
whether `book` holds an `ArrayList`, a tree, or a lazily-paged cursor — and returning your internal
`List` directly hands callers the ability to mutate it.

**D** is *nearly* right and the distinction is worth knowing: `Iterable` does **not** give you `stream()`.
You need `StreamSupport.stream(spliterator(), false)`, or to implement `spliterator()` properly — and a
good `Spliterator` (with `trySplit` and correct `characteristics`) is what makes parallel streams over
your type actually work.

The two flavours to name: **external** iteration (`Iterator` — the caller pulls, and can stop early) vs
**internal** iteration (`forEach` — the collection pushes, which allows the collection to choose the
traversal but makes early exit awkward). Streams are internal iteration with laziness bolted on.

And the fail-fast/fail-safe split: `ArrayList`'s iterator throws `ConcurrentModificationException` via
`modCount` (best-effort, never rely on it); `ConcurrentHashMap`'s is **weakly consistent**;
`CopyOnWriteArrayList`'s iterates an immutable **snapshot** and doesn't support `remove()`.

→ `quiz/19` Q11; `quiz/16` Q19
</details>

---

### Q12. `JdbcTemplate` is primarily which pattern?

- **A.** Proxy
- **B.** Strategy
- **C.** **Template Method** — it owns the algorithm (connect, execute, map, close, translate exceptions) and you supply the varying steps
- **D.** Facade

<details><summary>Answer</summary>

**C.** And it's the reason the class is named that way. You provide a `RowMapper`; it handles resource
acquisition, exception translation into Spring's `DataAccessException` hierarchy, and cleanup — the parts
everyone gets wrong.

The wider Spring map is worth having ready, since "which patterns does Spring use?" is a common question:

| Pattern | Where |
|---|---|
| **Singleton** | default bean scope (**one per context**, not per JVM) |
| **Factory** | `BeanFactory`, `FactoryBean` |
| **Proxy** | all of AOP — `@Transactional`, `@Async`, `@Cacheable` |
| **Template Method** | `JdbcTemplate`, `RestTemplate`, `TransactionTemplate` |
| **Observer** | `ApplicationEvent` / `@EventListener` |
| **Dependency Injection** | the whole container — Dependency Inversion made a runtime feature |
| **Front Controller** | `DispatcherServlet` |
| **Adapter** | `HandlerAdapter`, `HandlerInterceptor` |

The framing that lands: **Spring is a pattern catalogue you configure rather than write.** Its value is
that you get DI, Proxy and Template Method without hand-rolling them — and its cost is that when
something breaks you're debugging *through* three of them at once.

→ `quiz/19` Q12
</details>

---

### Q13. Which principle underlies most of these patterns?

- **A.** **Open/Closed, achieved through Dependency Inversion** — depend on abstractions so behaviour can be extended without editing existing code
- **B.** Single Responsibility
- **C.** Interface Segregation
- **D.** Liskov Substitution

<details><summary>Answer</summary>

**A.** "**Program to an interface, not an implementation**" is the GoF book's own first principle, and it
is Dependency Inversion. Strategy, Factory, Observer, Decorator, Proxy, Command, Template Method — every
one of them works by having the stable code depend on an abstraction that the varying code implements.
That's what makes them Open/Closed: new behaviour arrives as a new class, not as an edit to a working
one.

The others are present but narrower: **Liskov** is what makes substituting an implementation *safe* (and
is what a `Comparator` contract violation breaks); **Single Responsibility** is why each decorator does
one thing; **Interface Segregation** is why `Predicate` and `Function` are separate rather than one
`Callback`.

The senior addendum: **Open/Closed is a claim about an axis you predicted.** It's cheap when you guessed
the right axis of change and expensive when you guessed wrong — abstraction you don't need is harder to
remove than abstraction you postponed. So prefer to extract the interface at the *second* implementation,
not the first.

→ `quiz/19` Q13; `quiz/05` Q2
</details>

---

### Q14. "Name a pattern you'd argue against." The best answer structure?

- **A.** Refuse — all patterns have their place
- **B.** Name the most complex one
- **C.** Name whichever the interviewer's stack uses least
- **D.** **Name one, give the concrete failure you've seen, and say what you'd do instead**

<details><summary>Answer</summary>

**D.** The question tests whether you have opinions formed by consequences. **A** reads as either
inexperience or evasion.

Three defensible answers:

**Singleton**, on design grounds — global mutable state, hidden dependencies, order-dependent tests.
Instead: DI with a container-managed scope.

**Abstract Factory**, on cost grounds — it's the pattern that produces four interfaces and six classes to
support a second implementation that never arrives. Instead: one concrete class, extract the abstraction
at the second real implementation.

**Visitor**, on maintainability grounds — it makes adding an *operation* easy and adding a *type* a
change to every visitor, and the double-dispatch is genuinely hard to read. Instead: **sealed interfaces
plus exhaustive pattern matching** (JDK 21), which gets the compiler to enforce completeness with none of
the ceremony. This is a good answer partly because it shows *why* patterns fade: the language grew the
feature.

And the meta-point, which is the strongest thing you can say here: **the pattern isn't the problem —
applying it before the problem exists is.** Most pattern damage is speculative generality, not a bad
pattern.

→ `quiz/19` Q14
</details>

---

## Score yourself

- **11+** — enough. Pattern questions are usually one of the shorter parts of the loop.
- **The four worth knowing cold:** Q3 (Singleton's real defect is design, not thread-safety), Q6
  (Adapter/Decorator/Proxy differ by intent), Q10 (Command *is* the CQRS write side), Q14 (have an
  opinion, backed by a consequence).
