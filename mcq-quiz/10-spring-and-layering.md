# MCQ 10 — Spring: Layering, Annotations & Integration

12 questions. Source: [`quiz/10`](../quiz/10-spring-and-layering.md).

**Note:** `quiz/10` Q1 (design the enriched endpoint) is an open design question and stays in the
source file — it needs sketching, not selecting.

---

### Q1. Which stereotype annotation has actual added behaviour beyond marking a bean?

- **A.** `@Service`
- **B.** `@Component`
- **C.** `@Repository`
- **D.** They're all functionally identical

<details><summary>Answer</summary>

**C.** `@Repository` enables `PersistenceExceptionTranslationPostProcessor`, which translates
vendor-specific `SQLException`s and JPA exceptions into Spring's unchecked `DataAccessException`
hierarchy. That's a real functional difference.

`@Service` is **semantic only** — documentation the framework happens to read. `@Component` is the
generic stereotype. `@Controller` enables handler-method mapping (and `@RestController` =
`@Controller` + `@ResponseBody`).

Use the specific ones anyway: they communicate intent and are targetable by AOP pointcuts and
ArchUnit rules ("no `@Controller` depends on a `@Repository`").

→ `quiz/10` Q2
</details>

---

### Q2. What's the default bean scope, and what's the concurrency consequence?

- **A.** `singleton` — one instance shared by every request thread, so every field is shared mutable state
- **B.** `prototype` — a new instance per injection, so no sharing
- **C.** `request` — one per HTTP request, so fields are request-confined
- **D.** `thread` — one per thread

<details><summary>Answer</summary>

**A.** One instance per application context (not the static-field Singleton pattern — container-managed).

So **the rule is that Spring beans should be stateless**: dependencies in `final` fields via
constructor injection, per-request data in method parameters and locals (stack-confined, so
automatically safe). If you genuinely need shared state, make it explicit — an `AtomicLong`, a
`ConcurrentHashMap`, a bounded cache, or the database.

This is the number-one real Spring concurrency bug (`quiz/01` Q24). Note also that `request` and
`session` scopes are `ThreadLocal`-backed, which is why they break across async boundaries.

→ `quiz/10` Q3
</details>

---

### Q3. Why prefer constructor injection over field injection?

- **A.** It avoids the need for `@Autowired` entirely in all cases
- **B.** It's faster at runtime
- **C.** It allows circular dependencies
- **D.** It's the only form Spring supports for `final` fields, it fails fast on missing dependencies, and it's testable without a container

<details><summary>Answer</summary>

**D.** Four reasons in order of weight: `final` fields (immutability + safe publication); mandatory
dependencies enforced by the compiler so a missing one fails at **startup**, not first request;
`new Service(mock, mock)` with no Spring context, so unit tests run in milliseconds; and a
nine-parameter constructor **surfaces** that the class does too much, which field injection hides.

- **C** is backwards — a constructor cycle **fails fast** with a clear error, whereas field injection
  can silently permit cycles that half-work.

Spring doesn't need `@Autowired` on a single-constructor class (since 4.3), but that's a convenience,
not the reason.

→ `quiz/10` Q4
</details>

---

### Q4. `@Transactional` on a `private` method. What happens?

- **A.** It works normally
- **B.** It's silently ignored in the default proxy mode
- **C.** A compile error
- **D.** A startup failure

<details><summary>Answer</summary>

**B.** Silently ignored — one of three ways `@Transactional` does nothing without telling you.

The other two: **self-invocation** (calling `this.doWork()` bypasses the proxy) and
**returning a `Future`** (the proxy commits before the work happens and never sees the failure, so
no rollback — the migration landmine).

Fixes for self-invocation: split into two beans (best), inject the bean into itself,
`AopContext.currentProxy()`, or AspectJ weaving.

→ `quiz/10` Q5
</details>

---

### Q5. `@Transactional void process() throws IOException { repo.save(x); throw new IOException(); }` — commit or rollback?

- **A.** **Commit** — Spring rolls back on `RuntimeException` and `Error` by default, not on checked exceptions
- **B.** Rollback — any exception rolls back
- **C.** Rollback, but only the failed statement
- **D.** It throws `UnexpectedRollbackException`

<details><summary>Answer</summary>

**A.** The one that surprises everyone. Default rollback rules cover unchecked exceptions and `Error`
only. Override with `@Transactional(rollbackFor = Exception.class)`.

Also: **catching the exception inside the method prevents rollback** — the proxy never sees it. And
once a transaction is marked rollback-only, a subsequent commit attempt *does* throw
`UnexpectedRollbackException` (which is **D**, but as a different scenario).

→ `quiz/10` Q5
</details>

---

### Q6. `REQUIRES_NEW` propagation — what's the practical consequence?

- **A.** It reuses the current connection with a savepoint
- **B.** It's identical to `REQUIRED`
- **C.** It suspends the outer transaction and uses a **second connection**, so it can deadlock against its own parent
- **D.** It disables transactions for that method

<details><summary>Answer</summary>

**C.** A genuinely nasty one: two connections held simultaneously, committing independently, and if
the inner one needs a row the outer has locked, you deadlock against yourself. It also doubles your
connection-pool consumption per request.

- **A** describes `NESTED` (a savepoint within the current transaction, rolling back only to the
  savepoint).
- `REQUIRED` (the default) joins an existing transaction or starts one.

→ `quiz/10` Q5
</details>

---

### Q7. Calling another team's HTTP service. What's the most common cause of thread-pool exhaustion?

- **A.** Too small a thread pool
- **B.** No timeouts on the HTTP client
- **C.** Too many retries
- **D.** JSON deserialisation cost

<details><summary>Answer</summary>

**B.** The default in several clients is **infinite**, so one hung downstream call holds a request
thread forever. You need **both** connect and read/response timeouts, always.

Then, in order: **connection pooling** (don't create a client per request); **retries only for
idempotent operations, with exponential backoff and jitter** and a bounded attempt count — naive
retries amplify an outage and multiply *their* load exactly when they're struggling; a **circuit
breaker** so you fail fast against a dead service; and a **bulkhead** (separate pool/semaphore per
downstream) so one slow dependency can't consume all capacity.

- **C** is real but second-order — it makes an existing problem worse.

→ `quiz/10` Q6
</details>

---

### Q8. Where should HTTP status codes be decided?

- **A.** In the service layer, so it can signal precisely
- **B.** In the client
- **C.** In the repository, closest to the failure
- **D.** In a `@RestControllerAdvice`, mapping domain exceptions to statuses in one place

<details><summary>Answer</summary>

**D.** Domain exceptions in the service, HTTP mapping in the advice. **The service shouldn't know
what a 404 is** — that's a transport concern, and keeping it out means the service is reusable and
testable without HTTP.

Principles worth adding: use **`ProblemDetail`** (Spring 6 / RFC 9457) rather than inventing an error
shape; **never leak internals** (no stack traces, SQL or class names in a response — return a
correlation id and log the detail against it); **log once, at the boundary**, because logging and
rethrowing at every layer produces five copies of one stack trace; and distinguish 4xx from 5xx
properly, since your alerting and SLOs are built on it.

→ `quiz/10` Q7
</details>

---

### Q9. Where should most of your tests live?

- **A.** `@SpringBootTest` integration tests, for realism
- **B.** End-to-end tests against a deployed environment
- **C.** Plain unit tests with no Spring context at all
- **D.** `@WebMvcTest` slice tests

<details><summary>Answer</summary>

**C.** `new PositionService(mockRepo, mockClient)` — milliseconds, no reflection, no context. And
**constructor injection is what makes it possible** (Q3).

Then slice tests (`@WebMvcTest`, `@DataJpaTest`), then a small number of `@SpringBootTest`
integration tests for wiring and critical paths, then a handful of E2E.

Two things worth volunteering: **`@MockBean`/`@MockitoBean` creates a new application context**, and
each distinct configuration is cached separately — a suite with many mock combinations spends most of
its time starting contexts, which is a very common cause of slow builds. And use **Testcontainers
rather than H2**, because H2 accepts SQL that Postgres rejects, so it passes tests and fails in prod.

→ `quiz/10` Q8
</details>

---

### Q10. Which conditional annotation explains why declaring your own `DataSource` silently replaces Spring Boot's?

- **A.** `@ConditionalOnMissingBean`
- **B.** `@ConditionalOnClass`
- **C.** `@ConditionalOnProperty`
- **D.** `@Primary`

<details><summary>Answer</summary>

**A.** It's the key to the whole auto-configuration design: "has the user already defined one?" If
yes, Boot's version backs off — no configuration flag required.

To debug what did and didn't match: **`--debug`** (or `debug=true`) prints the
**auto-configuration report** with the reasons; `/actuator/conditions` exposes the same at runtime.
That's the answer to "why isn't my bean there".

The trade-off worth naming: auto-configuration is enormously productive and makes behaviour
**implicit**, so when it's wrong you're debugging a conditional graph you didn't write. That's the
contrast with **ZIO's `ZLayer`**, where the dependency graph is explicit and **checked at compile
time**.

→ `quiz/10` Q9
</details>

---

### Q11. Liveness vs readiness probes — what's the consequence of getting them backwards?

- **A.** Nothing; they're interchangeable
- **B.** Traffic is routed to unhealthy pods
- **C.** Readiness probes will never pass
- **D.** If liveness depends on a downstream, a database blip restarts every pod — cascading failure

<details><summary>Answer</summary>

**D.** **Liveness** means "restart me" and must **not** depend on downstreams. **Readiness** means
"stop sending traffic" and may.

Get it backwards and a transient database problem becomes a fleet-wide restart storm — turning a
partial degradation into a full outage. It's a small configuration detail with a large blast radius,
and a good one to raise unprompted.

The other observability items worth naming: connection-pool `pending` (the best leading indicator of
overload), executor queue depth per pool, downstream latency/error rate/circuit state, cache hit
rate, and **structured logs with a correlation id** — remembering that MDC is `ThreadLocal`-backed
and vanishes across async boundaries.

→ `quiz/10` Q10
</details>

---

### Q12. What's the AOP consequence most likely to cause a real bug?

- **A.** Proxies add measurable call overhead
- **B.** Self-invocation bypasses the proxy, so annotations silently don't fire
- **C.** Aspects can't access method arguments
- **D.** Pointcut expressions are evaluated at runtime on every call

<details><summary>Answer</summary>

**B.** Spring's AOP is **proxy-based** (JDK dynamic proxy if there's an interface, else a CGLIB
subclass), created at startup. `this.method()` doesn't go through the proxy, so `@Transactional`,
`@Async`, `@Cacheable`, `@Retryable` and `@PreAuthorize` all silently do nothing.

That single fact explains a whole family of "my annotation isn't firing" bugs.

- **A** is real but negligible outside very hot paths.

The Scala contrast: you don't need AOP. Cross-cutting behaviour is a **higher-order function** or
middleware — `withMetrics(withRetry(withLogging(op)))` — explicit at the call site, type-checked, no
proxy magic. A legitimately better answer to the same problem.

→ `quiz/10` Q12
</details>

---

## Score yourself

- **10+** — good.
- **Q2, Q4 and Q5 are the deep-dive candidates**, and all three connect straight to the concurrency
  material: singleton shared state, and the two ways `@Transactional` silently doesn't transact.
