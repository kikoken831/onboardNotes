# MCQ 12 — Scala Language & Functional Programming

18 questions. Source: [`quiz/12`](../quiz/12-scala-language-and-fp.md).

**Note:** `quiz/12` Q20 (sketch a trade domain model) stays in the source file — it's the synthesis
question and needs writing, not selecting.

---

### Q1. `val`, `var`, `lazy val`, `def` — which is evaluated on every access?

- **A.** `val`
- **B.** `var`
- **C.** `def`
- **D.** `lazy val`

<details><summary>Answer</summary>

**C.** `def` recomputes each call. `val` and `var` are evaluated eagerly once at construction;
`lazy val` on first access and then **memoised**.

Two things worth knowing about `lazy val`: it **is** thread-safe (it compiles to a bitmap plus a
`synchronized` double-checked initialisation, so you get correct lazy init for free — no holder idiom
needed). But it **can deadlock** when two threads initialise interdependent lazy vals in opposite
orders, with nothing in the source hinting a lock is involved — and on **JDK 21 that `synchronized`
pins a virtual thread's carrier**.

And the trap that recurs throughout this pack: **`val` makes the *reference* immutable, not the
object.** `val buf = mutable.ListBuffer()` is still an unsynchronised mutable structure.

→ `quiz/12` Q1
</details>

---

### Q2. What does `case class` generate that a regular class doesn't?

- **A.** Structural `equals`/`hashCode`, `toString`, `copy`, `apply`, `unapply`, and `val` params
- **B.** Thread-safe accessors
- **C.** A builder
- **D.** Lazy initialisation of all fields

<details><summary>Answer</summary>

**A.** Which makes it correct as a map key, usable in pattern matching (via `unapply`), and cheap to
derive variants from (`copy`).

Use a case class for **immutable data** — DTOs, domain events, value objects, messages, and the
branches of a `sealed trait` ADT. Use a regular class for **identity rather than value semantics**
(a connection, a repository — where two instances with equal fields are *not* interchangeable).

Caveats: don't extend case classes (it breaks `equals` symmetry — make them `final`), and **`copy` is
shallow**.

The Java bridge: this is exactly **`record`** (JDK 16+), and records are Java deliberately adopting
`case class`.

→ `quiz/12` Q2
</details>

---

### Q3. What's the main advantage of pattern matching over an `if`/`instanceof` chain?

- **A.** It's faster at runtime
- **B.** It works with `null`
- **C.** It avoids allocation
- **D.** With a `sealed` trait the compiler checks **exhaustivity** — add a case and every match needing an update warns at compile time

<details><summary>Answer</summary>

**D.** This is the argument to lead with, especially for a domain where the event set grows: add a
`Failed` event and the compiler tells you every site that needs updating. With `isInstanceOf` you get
a silent runtime fall-through.

Also: destructuring in the same expression as the test, and it's an *expression* so it returns a value.

Where it bites: `sealed` only works within one file; **non-sealed types get no exhaustivity check**; a
missing case throws `MatchError` at runtime (so treat the warning as an error —
`-Xfatal-warnings` / `-Werror`); erasure means `case l: List[String]` can't check the type parameter;
and **lowercase identifiers bind rather than compare** — `case expected =>` creates a *new variable*
matching everything, which is a genuinely baffling bug. Use backticks or an uppercase name.

→ `quiz/12` Q3
</details>

---

### Q4. What does `unapply` do?

- **A.** Reverses a `map` operation
- **B.** It's the extractor — the inverse of `apply` — and it's what makes pattern matching work
- **C.** It removes an element from a collection
- **D.** It undoes an implicit conversion

<details><summary>Answer</summary>

**B.** `case Money(amt, ccy) =>` calls `Money.unapply(m)`, which returns `Option[(Long, Currency)]`.
Case classes generate it; you write it by hand for custom extractors. `unapplySeq` extracts a variable
number of elements.

Its partner `apply` makes an object *callable* — `Money(100, USD)` is `Money.apply(100, USD)`. The
important use is a **smart constructor**: make the real constructor `private` and validate in `apply`,
so an invalid instance cannot exist.

The design point: **construction and deconstruction are symmetric and user-definable**, so pattern
matching isn't tied to the concrete representation — something Java's record patterns can't do.

→ `quiz/12` Q4
</details>

---

### Q5. Why does the Scala standard library use multiple parameter lists (currying) so often?

- **A.** **Type inference flows left to right across parameter lists**, so `fold[B](z: B)(op: (B,A) => B)` can infer `B` from `z` and then check `op`
- **B.** It's shorter to write
- **C.** It's required for recursion
- **D.** It makes the function faster

<details><summary>Answer</summary>

**A.** With one parameter list the compiler often can't infer it. **This is the real reason**, and it's
a much better answer than reciting the definition of currying.

The other three reasons: it enables the **implicit parameter list**
(`(implicit ec: ExecutionContext)`); it enables **by-name blocks that look like syntax**
(`using(conn) { c => ... }`); and partial application for reuse.

Related distinction worth having: **partially applied** (supply some arguments, get a function back)
vs **`PartialFunction`** (defined only for *some inputs*, with `isDefinedAt`) — the latter is what a
`case` block is, and why `collect` can filter and map in one pass.

→ `quiz/12` Q5
</details>

---

### Q6. What is a monad, in one sentence?

- **A.** A design pattern for wrapping side effects
- **B.** A class that implements `map`
- **C.** A type with `flatMap` and a way to lift a plain value in, obeying three laws — a context you can sequence computations inside
- **D.** A container that can hold at most one value

<details><summary>Answer</summary>

**C.** Don't lead with category theory. The practical framing: the *context's plumbing is handled for
you*.

`Option` handles **absence** (short-circuits on `None`). `Either` handles **failure with a reason**
(short-circuits on `Left`). `Try` handles **thrown exceptions**. `Future` handles **asynchrony**.
`List` handles **multiplicity**. **The uniformity is the value** — one syntax for all of them.

- **B** is a **functor** (map only).
- **D** describes `Option` specifically, not monads.

The laws: left identity, right identity, associativity. They're what make `for`-comprehensions mean
what you expect and let you re-nest and factor out steps safely.

→ `quiz/12` Q6
</details>

---

### Q7. Is `scala.concurrent.Future` a lawful monad?

- **A.** Yes, fully
- **B.** Arguably not — it's eager and side-effecting, so `pure(a).flatMap(f)` isn't substitutable for `f(a)`, and it memoises
- **C.** No, because it lacks `flatMap`
- **D.** Only when used with a single-threaded `ExecutionContext`

<details><summary>Answer</summary>

**B.** It behaves monadically for composition purposes — which is why the `for` syntax works — but
referential transparency is broken. `val f = Future(effect); (f, f)` is one effect;
`(Future(effect), Future(effect))` is two.

`IO` and `ZIO` are lawful because they're **descriptions**, not running computations.

This distinction is worth volunteering because it's the root cause of the practical problems: no
retry, no cancellation, and sequential-vs-parallel being a positional accident.

Related: **`for` over different monads doesn't mix** — you can't `flatMap` an `Option` into a
`Future`. That's what monad transformers (`OptionT`, `EitherT`) are for, or you convert first.

→ `quiz/12` Q6
</details>

---

### Q8. `Future` vs `Promise` — what's the difference?

- **A.** `Promise` is the older API, deprecated in favour of `Future`
- **B.** They're the same thing with different names
- **C.** `Promise` is for values, `Future` for effects
- **D.** `Future` is the read side (you observe it); `Promise` is the write side (you complete it exactly once)

<details><summary>Answer</summary>

**D.** One `Promise` has one `Future`. Completing twice throws (hence `tryComplete`).

**When you actually need a `Promise`:** bridging a **callback- or push-based API** into `Future` — a
Kafka consumer callback, a JMS listener, an **AMPS subscription**, a Netty channel future, a legacy
Java listener interface. `Future { }` can't do that, because it assumes you have a *block of code* to
run rather than an event that will arrive.

Also useful for an in-flight-request registry (`Map[CorrelationId, Promise[Response]]` for
request/reply over a message bus — a very common post-trade pattern).

The Java contrast: `CompletableFuture` **conflates both roles**, so a consumer can complete your
future. Scala splitting them is better encapsulation.

→ `quiz/12` Q7
</details>

---

### Q9. You're validating a trade with three independent rules and want **all** the failures. Which type?

- **A.** `Either[Error, Trade]`
- **B.** `Try[Trade]`
- **C.** `Validated[NonEmptyList[Error], Trade]`
- **D.** `Option[Trade]`

<details><summary>Answer</summary>

**C.** The key distinction: **`Either`/`Try` short-circuit (monadic); `Validated` accumulates
(applicative).**

```scala
for { a <- validateQty(t); b <- validateCcy(t) } yield ...   // reports only the FIRST problem
(validateQty(t), validateCcy(t), validateDate(t)).mapN(Trade.apply)   // reports ALL of them
```

For a trade-validation endpoint returning a 400, you almost always want the second — which is also
why `Validated` is *not* a monad (a monad must short-circuit).

Choosing generally: `Option` for absence with no reason needed; `Either[E, A]` for **enumerated domain
errors** (the default for business logic); `Try` for wrapping code that **throws** (Java interop,
parsing); `Validated` for accumulating.

→ `quiz/12` Q8
</details>

---

### Q10. Which use of `implicit` should you avoid in production Scala?

- **A.** Implicit **conversions** between types
- **B.** Implicit classes, for extension methods
- **C.** Implicit parameters, for passing an `ExecutionContext`
- **D.** Implicit type class instances in a companion object

<details><summary>Answer</summary>

**A.** `implicit def stringToTradeId(s: String): TradeId` means any `String` silently becomes a
`TradeId` anywhere the compiler wants one. Unreadable (invisible behaviour at the call site), hard to
debug, and it can hide type errors that should have failed.

**Scala 3 deliberately made it harder** — requiring an explicit import and steering you to
`Conversion` — because the overloading of `implicit` for three different features was recognised as a
design mistake. Scala 3 splits them: `using`/`given` for parameters and instances, `extension` for
methods, `Conversion` for conversions.

**C**, **B** and **D** are all idiomatic. Safety rules: put type class instances in the **companion object** so
they're found without an import, **import implicits narrowly** (never `._`), and prefer explicit when
the value is a *decision* rather than *context*.

→ `quiz/12` Q9
</details>

---

### Q11. What does a type class give you that an interface doesn't?

- **A.** Better runtime performance
- **B.** Compile-time nullability checking
- **C.** Automatic serialisation
- **D.** You can add behaviour to types you don't own, retroactively, and have several instances per type

<details><summary>Answer</summary>

**D.** You can define `EventSerializer[java.time.Instant]` for a type you can't modify — inheritance
requires editing the class. And you can have two different `Ordering[Trade]`s (by date, by notional)
chosen at the call site, where an interface gives you exactly one `compareTo`.

Plus: your domain type doesn't extend a transport interface, so the domain doesn't depend on
serialisation — which matters a lot in an event-sourced system. Resolution is at **compile time**, so
a missing instance is a compile error.

Costs to be balanced: more machinery, **cryptic errors** when an instance is missing, slower compile,
and it's unfamiliar to a team coming from Java.

The framing that makes it click for a Java audience: **passing a `Comparator` explicitly *is* a type
class instance** — Scala just has the compiler supply it.

→ `quiz/12` Q10
</details>

---

### Q12. Why should you always annotate `@tailrec`?

- **A.** It enables the optimisation, which is off by default
- **B.** It makes it a **compile error** if the tail-call elimination can't be applied
- **C.** It improves the stack trace
- **D.** It's required for mutual recursion

<details><summary>Answer</summary>

**B.** `scalac` performs self-tail-call elimination automatically; the annotation makes failure
*visible*. Without it you get a silently stack-consuming method that works on your test data and
overflows on the one production input with 200,000 events.

Requirements: the call must be in tail position and the method must not be overridable (`private`,
`final`, or in an `object`).

- **D** is backwards: it can't handle **mutual recursion** or non-self tail calls. For those you need
  **trampolining** (`scala.util.control.TailCalls`, `cats.free.Trampoline`) — return a *description*
  of the next step and let a driver loop execute it, so the stack becomes heap.

And the point worth volunteering: **`IO`/`ZIO` do this for free** — their `flatMap` chains are
interpreted by a heap-allocated loop, so arbitrarily deep recursion is stack-safe with no `@tailrec`.

→ `quiz/12` Q11
</details>

---

### Q13. In `for { a <- fa; b <- fb; c <- fc } yield f(a,b,c)`, which combinators are called?

- **A.** `flatMap`, `flatMap`, `map`
- **B.** Three `map`s
- **C.** Three `flatMap`s
- **D.** `map`, `map`, `flatMap`

<details><summary>Answer</summary>

**A.** **Every `<-` except the last is a `flatMap`; the last one plus `yield` is a `map`; guards are
`withFilter`.** Be able to do this desugaring on a whiteboard — it comes up.

So a `for`-comprehension is pure syntax: anything with `map`, `flatMap` and `withFilter` works in one,
including your own types.

Two consequences: **`for` over `Future` is sequential** because each subsequent future is
*constructed* inside the previous `flatMap` (measured 920 ms vs 311 ms); and all monads in one
comprehension must be the **same type**.

→ `quiz/12` Q12
</details>

---

### Q14. Why is `Array[A]` invariant in Scala when `List[+A]` is covariant?

- **A.** Arrays are primitives, so variance doesn't apply
- **B.** Arrays are Java types and Scala can't annotate them
- **C.** `Array` is mutable, so `A` appears in both input and output position — covariance would allow storing a `Cat` in an `Array[Dog]`
- **D.** For performance reasons

<details><summary>Answer</summary>

**C.** The classic illustration, and a great concrete answer:
```scala
val dogs: Array[Dog] = Array(new Dog)
val animals: Array[Animal] = dogs   // would type-check if covariant
animals(0) = new Cat                // now the Dog array holds a Cat
val d: Dog = dogs(0)                // ClassCastException
```
**Java *does* allow this**, which is why you can get `ArrayStoreException` at runtime — a well-known
unsoundness. Scala makes `Array` invariant and catches it at compile time.

The rule: **covariant out, contravariant in, invariant if both** — the same intuition as Java's
**PECS** (Producer Extends, Consumer Super). Naming that equivalence lands well with a Java panel.

`Function1[-T, +R]` is the canonical example: contravariant in its argument, covariant in its result.

→ `quiz/12` Q13
</details>

---

### Q15. `class Handler extends Logging with Timed with Audited` — whose `process` runs?

- **A.** `Logging`'s, as the base
- **B.** `Audited`'s — linearisation is right to left, so the last mixin wins
- **C.** `Timed`'s, as the first mixin
- **D.** A compile error from the ambiguity

<details><summary>Answer</summary>

**B.** Linearisation is `Handler → Audited → Timed → Logging → AnyRef → Any`, and **`super` means "the
next trait in the linearisation"**, not a fixed parent. So `Audited.process`'s `super.process` calls
**`Timed`'s**, not `Logging`'s.

That enables **stackable modifications** — decorators composed by mixin — which is a legitimate and
elegant use.

The caveat: linearisation order **isn't obvious from reading the class**, so deep mixin stacks become
hard to reason about. Hence the modern advice: **composition over mixin inheritance**.

- **D** is how *Java* resolves diamonds (forbid the ambiguity, force an override); Scala **orders** it.

The variant you'll actually use daily is **`sealed trait`**, which restricts extension to one file and
enables exhaustive matching.

→ `quiz/12` Q14
</details>

---

### Q16. What is "tagless final"?

- **A.** A way to make traits final for performance
- **B.** Scala 3's replacement for implicits
- **C.** A pattern for tagging log output
- **D.** Writing code against an abstract effect `F[_]` constrained by type classes, choosing the concrete interpreter at the edge

<details><summary>Answer</summary>

**D.** `class TradeService[F[_]: MonadThrow](repo: TradeRepository[F])` — the constraints *document*
what the code can do, and you can instantiate `F` as `Either`, `Id`, or a recording `WriterT` for
tests that need no runtime.

Benefits: testability, capability tracking (a method with only `Monad[F]` provably can't perform I/O),
and portability across `IO`/`ZIO`/`Future`.

**And the honest cost, which is the better half of the answer:** it's a significant abstraction tax —
everything gains an `F[_]`, inference degrades, error messages get much worse, and it's steep for a
team new to Scala. In practice **many teams have moved back to concrete `IO`/`ZIO`** for application
code and reserve it for libraries; ZIO's ecosystem argues against it.

"I know what it is, here's when it's worth it, and it's often not" is a much stronger answer than
enthusiasm — and safer if their team went the other way.

→ `quiz/12` Q16
</details>

---

### Q17. Make the case for immutability — what's the strongest single argument?

- **A.** It's faster
- **B.** It uses less memory
- **C.** Thread safety for free — no synchronisation, no locks, no defensive copying, and `final` fields are safely publishable
- **D.** It makes debugging easier

<details><summary>Answer</summary>

**C.** And in the migration context: it's why the real concurrency win isn't a library, it's that
**the default flipped** from "thread-safety is opt-in" to "mutation is opt-in".

Then local reasoning (a value can't change under you), safety as a map key, and — for this domain —
it's the **foundation of event sourcing**.

**A and **B** are actively wrong and you should say so**, because an answer that only lists benefits
sounds naive: every "update" allocates, and a `List[Int]` of 1 M costs ~38 MB vs 3.8 MB for
`Array[Int]`. The mitigation is that most of it **dies in eden** and a copying collector reclaims it
almost free — so the cost is GC *frequency*, and it only matters in a genuinely hot loop.

The line to land: *"I default to immutable and treat mutation as a local optimisation I have to
justify — and I measure before introducing it."*

→ `quiz/12` Q18
</details>

---

### Q18. Which Scala 3 feature replaces value classes and fixes their biggest flaw?

- **A.** `opaque type`
- **B.** `enum`
- **C.** `given`/`using`
- **D.** Union types

<details><summary>Answer</summary>

**A.** A Scala 2 value class (`extends AnyVal`) **silently boxes** when used as a type parameter, in an
array, in an `Option`, or pattern-matched — so the optimisation evaporates in exactly the generic
contexts you wanted it. An `opaque type` compiles to the underlying type with no boxing surprises.

Genuinely useful for domain primitives — `TradeId`, `Quantity`, `Isin` — so you can't pass an account
id where a trade id belongs. Which is precisely what a post-trade domain model wants, and where that
mistake moves real money.

Other Scala 3 changes worth knowing: implicits split into `given`/`using`/`extension`/`Conversion`;
`enum` for concise ADTs; union and intersection types; trait parameters; significant indentation;
macros completely redesigned (the main migration pain); and 2.13 ↔ 3 interop via TASTy.

**Worth asking them which version they're on** — it tells you a lot about the codebase's age.

→ `quiz/12` Q18, Q19; `quiz/06` D1
</details>

---

## Score yourself

- **15+** — good.
- **Near-certain for a Scala role:** Q1, Q2, Q3, Q6 (monads), Q8 (`Future` vs `Promise`), Q12 (tail
  recursion). **Q10/Q11 (implicits and type classes) separate "used Scala" from "understands Scala."**
- **Q7 and Q16** are where a *nuanced* answer beats a confident one — `Future` not being lawful, and
  tagless final often not being worth it.
