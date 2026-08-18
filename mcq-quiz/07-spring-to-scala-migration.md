# MCQ 07 — Spring Boot → Scala: What Breaks

10 questions. Source: [`quiz/07`](../quiz/07-spring-to-scala-migration.md).

**Note:** `quiz/07` Q8 (`Future` vs Cats Effect/ZIO) is deliberately **not** here — the real
answer is "it depends on the team", and forcing one correct option would teach a wrong instinct.

---

### Q1. What fundamentally changes when moving from Spring MVC to an async Scala model?

- **A.** Requests are processed faster
- **B.** You no longer need a connection pool
- **C.** A request no longer owns one thread for its lifetime, so anything relying on thread confinement breaks
- **D.** Exceptions propagate differently but nothing else changes

<details><summary>Answer</summary>

**C.** Everything else follows from this. Spring MVC is thread-per-request: one thread runs filters,
controller, service and the JDBC call, so blocking is fine and `ThreadLocal` works as ambient
context *because* one thread owns the request.

The moment work hops threads, every `ThreadLocal`-backed feature stops working — `@Transactional`'s
connection binding, `MDC`, `SecurityContextHolder`, `RequestContextHolder`,
`LocaleContextHolder`, `@RequestScope`.

- **A** is not automatic at all; async changes the *shape* of capacity, not per-request speed.

→ `quiz/07` Q1
</details>

---

### Q2. `@Transactional` on a method returning `Future[Result]`. What happens?

- **A.** It commits immediately on method return — before the work happens — and a failure inside the future produces no rollback
- **B.** The transaction spans the whole future, committing when it completes
- **C.** Spring detects the return type and throws at startup
- **D.** The transaction is propagated to the EC thread automatically

<details><summary>Answer</summary>

**A.** And it **fails silently**, which is what makes it the most dangerous item in the migration.
The proxy sees a normal return and commits; the actual DB work then runs on an EC thread with no
bound connection; a failure completes the future as `Failure`, which the proxy never sees.

Net effect: `@Transactional` is in the source, code review sees "it's transactional", and there is
no transaction. Partial writes, no atomicity, no rollback.

Fixes: keep the transactional unit **synchronous inside one task**
(`Future { blocking { txTemplate.execute(...) } }(blockingEc)`), or move to a transaction
abstraction that's a **value** — doobie's `ConnectionIO.transact(xa)`, Slick's `DBIO`, ZIO Quill.

**At minimum, write a test that asserts rollback happens.** It catches the whole class.

→ `quiz/07` Q2
</details>

---

### Q3. Trace IDs vanish from logs after the port. Why?

- **A.** The logging framework changed
- **B.** Log levels reset on thread switch
- **C.** Scala's `Future` strips diagnostic context
- **D.** `MDC` is a `ThreadLocal`, so the executing thread has an empty (or a previous request's) context

<details><summary>Answer</summary>

**D.** Populated by a filter on the request thread; empty on the EC thread. You can't reconstruct a
request from logs after the first async hop — usually discovered during the first production
incident after the migration.

Fixes, worst to best: wrap the EC to copy MDC at submission (works, but leaky — one unwrapped EC
breaks the chain, and you must clear in a `finally` or you get cross-request bleed); a **fiber-local**
(`IOLocal`, ZIO `FiberRef`) which propagates along the fiber tree; **pass it explicitly** as a value;
or use a tracing library that already solved it (OpenTelemetry, `natchez`, ZIO Telemetry).

The lesson: **`ThreadLocal` is not a context mechanism in an async world** — it's an optimisation
that happened to work under thread-per-request.

→ `quiz/07` Q3
</details>

---

### Q4. You must call a blocking JDBC layer you can't rewrite. How many threads in its pool?

- **A.** As many as possible, to maximise throughput
- **B.** Roughly equal to the connection pool size
- **C.** One per CPU core
- **D.** Unbounded, with `blocking { }` to compensate

<details><summary>Answer</summary>

**B.** More worker threads than DB connections just moves the queue from your executor (where you
can see it) to HikariCP (where you have less visibility and a worse timeout story). And the database
has a real concurrency limit too — 500 connections to Postgres is worse than 50.

The full recipe: a dedicated pool, **bounded** `ArrayBlockingQueue`, `AbortPolicy` so overload sheds
loudly as a 503, named threads so the dump is readable, transactions kept inside one task, and
metrics on queue depth plus Hikari's `connections_pending`.

- **D** converts starvation into unbounded thread growth.

→ `quiz/07` Q4
</details>

---

### Q5. Why is constructor injection with `val`s better than Spring field injection, for concurrency specifically?

- **A.** Fields can be `final`, so they're safely published by final-field semantics and can't be reassigned
- **B.** It's faster at startup
- **C.** It prevents the bean from being a singleton
- **D.** It makes the bean thread-confined

<details><summary>Answer</summary>

**A.** `@Autowired` on a field **cannot be `final`**, so the field is non-final and set
post-construction — the same unsafe-publication shape as any post-constructor assignment. Usually
benign because the context is fully initialised before traffic, but it's the wrong default.

Plus: mandatory dependencies enforced at compile time, testable with `new Service(mock, mock)` and no
container, and a nine-parameter constructor *tells you* the class does too much (field injection
hides that).

- **C** — scope is unchanged; a singleton with `final` immutable deps is exactly what you want.

→ `quiz/07` Q5, `quiz/10` Q4
</details>

---

### Q6. Which Spring annotation has **no** direct Scala equivalent because the capability moves into the type?

- **A.** `@Cacheable`
- **B.** `@Scheduled`
- **C.** `@Async`
- **D.** `@Valid`

<details><summary>Answer</summary>

**C.** Asynchrony becomes the **return type** — `Future`, `IO`, `ZIO`. There's no annotation to
forget, no proxy, and therefore **no self-invocation trap**.

The others all have direct analogues: `@Cacheable` → Caffeine wrapped in the effect or a `Ref`;
`@Scheduled` → `fs2.Stream.awakeEvery`, `ZIO.repeat(Schedule.fixed(...))`, `Source.tick` — all
cancellable and composable, and unlike `@Scheduled` a thrown exception doesn't silently kill the job
forever; `@Valid` → `Validated`/`ValidatedNel`, which **accumulates** errors instead of failing on
the first.

→ `quiz/07` Q6
</details>

---

### Q7. You're porting a `synchronized` block to Scala. What's the main risk?

- **A.** Scala has no `synchronized`
- **B.** You've ported the *design* — the shared mutable state — rather than reconsidering whether it should be immutable, confined, or in a `Ref`
- **C.** `synchronized` deadlocks more often in Scala
- **D.** It won't compile without an explicit monitor object

<details><summary>Answer</summary>

**B.** `x.synchronized { }` exists on every `AnyRef`, so a mechanical port compiles and works — and
you get none of the migration's concurrency benefit.

**Treat every `synchronized` in the Java source as a design question, not a translation task.** Each
one is either (a) state that should be immutable, (b) state that should live in one place
(`Ref`/actor/DB), or (c) a genuine lock. Very few turn out to be (c).

Secondary risks worth knowing: on **JDK 21 virtual threads `synchronized` pins**; `lazy val`
compiles to synchronised double-checked init and can deadlock on interdependent lazy vals; and
`object` initialisation is itself guarded by the class-init lock, so mutually-referencing objects
initialised concurrently deadlock with a baffling stack trace.

→ `quiz/07` Q7
</details>

---

### Q8. What's the biggest *new* concurrency risk introduced by the migration?

- **A.** Scala's GC pressure causing longer pauses
- **B.** Type erasure hiding concurrency bugs
- **C.** Actors dropping messages silently
- **D.** The `ExecutionContext` — one blocking call on a cores-sized compute pool can wedge the whole application at low CPU, with no compile error or warning

<details><summary>Answer</summary>

**D.** The Java model was **forgiving**: blocking was normal, one slow call cost you one thread out
of 200, and the failure was gradual and legible. In Scala the same instinct wedges the service.

It's a footgun a Java-experienced team walks into precisely *because* their instincts are correct for
the old model.

The other two top risks: **silently non-transactional code** (Q2) and **unbounded concurrency**
(`Future.sequence` over a large collection taking out a downstream). All three share a shape:
**the Java version's implicit bound is gone and nothing replaced it.**

- **A** is real but a young-gen frequency story, not an outage.

→ `quiz/07` Q11
</details>

---

### Q9. And the biggest *win*?

- **A.** Better performance
- **B.** Shorter code
- **C.** Immutability by default — shared mutation becomes opt-in rather than the default
- **D.** Compile-time dependency injection

<details><summary>Answer</summary>

**C.** In Java, thread safety is opt-in: every field on every shared bean is a race you must reason
about, and that's the bug that actually ships (`quiz/01` Q24). In Scala, `val` and immutable
collections are the default and case classes are safely publishable by construction.

That deletes a whole category of bug and changes the review question from *"is this thread-safe?"*
to *"why is this mutable?"*

- **A** is not a claim you should make — Scala typically allocates *more* for the same logic.
- **D** is real (ZIO's `ZLayer`) but secondary, and only if you adopt ZIO.

→ `quiz/07` Q11
</details>

---

### Q10. In the new service, which metric stops being a useful health signal?

- **A.** Thread count
- **B.** Request latency percentiles
- **C.** Connection pool pending count
- **D.** GC overhead

<details><summary>Answer</summary>

**A.** With fibers or virtual threads the thread count stays flat and small **no matter how bad
things get**. In the old service "threads climbing" was a reliable alarm; teams lose it and don't
notice.

The replacements: **EC/fiber-runtime saturation** (queue depth, active count, **rejections**), fiber
count, in-flight request count, mailbox depth per actor if Pekko, and **allocation rate** explicitly
(the metric most likely to differ from the Java service).

**C** and **D** remain exactly as useful — arguably more so, since the connection pool becomes the binding
constraint.

→ `quiz/07` Q10
</details>

---

## Score yourself

- **8+** — good.
- **Q2, Q3 and Q8 are the three that matter most.** They're the silent failures: no error, no
  warning, just wrong behaviour.

The one-paragraph version of this whole file is at the end of `quiz/07` — worth memorising as the
answer to "what do you think the concurrency risks of this migration are?"
