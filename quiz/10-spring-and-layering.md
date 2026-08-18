# Quiz 10 — Spring: Layering, Annotations & Integration

14 questions built around the design question in your bank: *"an endpoint serving DB data
enriched from another team's HTTP service — how do you organise it with Spring?"*

That question is really testing **layering discipline** and whether you know what each
annotation buys you. Target: 20 min.

---

### 1. Design it: an endpoint serving DB data enriched with another team's HTTP service.

<details><summary>Answer</summary>

**The layering, one responsibility per component:**

```java
// 1. WEB LAYER -- HTTP concerns only. No business logic, no persistence.
@RestController
@RequestMapping("/api/v1/positions")
class PositionController {
    private final PositionEnrichmentService service;          // constructor injection
    PositionController(PositionEnrichmentService service) { this.service = service; }

    @GetMapping("/{id}")
    PositionResponse get(@PathVariable long id) {
        return PositionResponse.from(service.getEnriched(id));  // map domain -> DTO here
    }
}

// 2. SERVICE LAYER -- the orchestration and business rules. Knows nothing about HTTP.
@Service
class PositionEnrichmentService {
    private final PositionRepository repository;
    private final ReferenceDataClient referenceData;

    PositionEnrichmentService(PositionRepository r, ReferenceDataClient c) { ... }

    EnrichedPosition getEnriched(long id) {
        Position p = repository.findById(id)
                .orElseThrow(() -> new PositionNotFoundException(id));
        RefData ref = referenceData.lookup(p.instrumentId());   // may fail/timeout
        return EnrichedPosition.of(p, ref);
    }
}

// 3. PERSISTENCE LAYER
@Repository                                  // or a Spring Data interface
interface PositionRepository extends JpaRepository<Position, Long> { }

// 4. THE OTHER TEAM'S SERVICE -- an interface YOU own, so you can swap/mock it
interface ReferenceDataClient { RefData lookup(String instrumentId); }

@FeignClient(name = "reference-data", url = "${refdata.url}",
             configuration = RefDataConfig.class)
interface ReferenceDataFeignClient { @GetMapping("/ref/{id}") RefData lookup(@PathVariable String id); }
```

**Why each layer exists — this is what's being scored:**
- **Controller** = translation. HTTP → domain call → DTO. If it contains an `if` about
  business rules, it's in the wrong place. Keeping it thin means the service is testable
  without `MockMvc`.
- **Service** = the actual behaviour, and the transaction boundary. Depends only on
  interfaces, so it unit-tests with plain mocks and no Spring context.
- **Repository** = persistence, and the only layer that knows about JPA/SQL. Entities
  shouldn't leak to the controller.
- **A client interface you own** — the important design point. Don't inject the Feign
  interface directly into the service; wrap it behind *your* interface. Then the other
  team's API shape, their DTOs, and their failure modes stay at the boundary, and you can
  swap Feign for `WebClient` without touching business logic.
- **Separate DTOs from entities.** Entity → domain → response DTO. Exposing JPA entities
  directly couples your API to your schema and causes lazy-loading serialisation errors.

**Then volunteer the things that make it production-ready** — this is what turns a
textbook answer into a strong one:

1. **Timeouts.** The other team's service *will* hang. Connect and read timeouts on the
   client, always. An HTTP client with no timeout is the most common cause of thread-pool
   exhaustion in a Spring app (quiz 02 Q15).
2. **Resilience.** Circuit breaker + retry + bulkhead (Resilience4j). Decide explicitly:
   if enrichment fails, do you **fail the request** or **degrade** and return the
   un-enriched position? That's a product decision — ask for it.
3. **Caching.** Reference data is usually slow-changing; a Caffeine cache with a TTL turns
   N calls into one. `@Cacheable`, or explicit for visibility.
4. **N+1.** If the endpoint returns a *list*, don't call the service per element. Batch:
   one query, one bulk enrichment call. This is the single most likely follow-up
   (quiz 11 Q9).
5. **Transaction scope.** The `@Transactional` boundary belongs on the service — and it
   should **not** span the HTTP call. Holding a DB connection while waiting on another
   team's network call is how you exhaust the connection pool (see Q5).
6. **Error mapping.** A `@ControllerAdvice` maps `PositionNotFoundException` → 404, a
   client timeout → 503 or 504, and validation failures → 400.
7. **Observability.** Metrics on the downstream call (latency, error rate, circuit state),
   and trace-context propagation so the other team's spans join yours.

**If they push on async:** return `CompletableFuture<PositionResponse>` (Spring MVC
supports it), or use `WebClient` reactively, or — on JDK 21 — just enable virtual threads
and keep the blocking code (quiz 03 Q9). Then note the trap: **the transaction must stay
on one thread** (quiz 07 Q2).
</details>

---

### 2. `@Component` vs `@Service` vs `@Repository` vs `@Controller` — is there a real difference?

<details><summary>Answer</summary>

**Functionally, mostly no** — `@Service`, `@Repository`, and `@Controller` are all
`@Component` meta-annotated, so all four are picked up by component scanning and become
beans. The differences:

- **`@Component`** — the generic stereotype. Use when nothing more specific fits.
- **`@Service`** — semantic only. Marks business logic. No added behaviour; it's
  documentation the framework happens to read.
- **`@Repository`** — **has real behaviour**: it enables
  `PersistenceExceptionTranslationPostProcessor`, which translates vendor-specific
  `SQLException`s and JPA exceptions into Spring's unchecked `DataAccessException`
  hierarchy. That's the one with a functional difference, and it's a good detail to know.
- **`@Controller`** — enables handler-method mapping for Spring MVC.
  **`@RestController`** = `@Controller` + `@ResponseBody` on every method (so return values
  are serialised to the body rather than resolved as view names).
- **`@Configuration`** — a class containing `@Bean` factory methods. Proxied by default so
  that calling one `@Bean` method from another returns the *singleton*, not a new instance
  (`proxyBeanMethods = false` disables that and is faster if you don't rely on it).

**Why use the specific ones anyway:** they communicate intent, they're targetable by
AOP pointcuts and architecture tests (ArchUnit can assert "no `@Controller` depends on a
`@Repository`"), and `@Repository` genuinely does something.

**`@Bean` vs `@Component`:** `@Component` is for classes *you own* and can annotate;
`@Bean` is a factory method in a `@Configuration` class, for third-party classes or when
construction needs logic. Both produce beans.
</details>

---

### 3. What's the default bean scope, and why is that a concurrency question?

<details><summary>Answer</summary>

**Default is `singleton`** — one instance per application context, shared by **every
request thread**. (Note: singleton *per container*, not the static-field Singleton pattern.)

**That makes every field on every `@Service` shared mutable state.** This is the number-one
Spring concurrency bug, and it's the same one as **quiz 01 Q24** — go read that question,
it's the worked example with five defects.

```java
@Service
public class OrderService {
    private List<Order> pending = new ArrayList<>();     // SHARED. Corrupts under load.
    private int processed;                                // SHARED. Lost updates.
}
```

**The rule: Spring beans should be stateless.** Dependencies in `final` fields via
constructor injection; per-request data in method parameters and locals (stack-confined, so
automatically safe — quiz 02 Q13). If you genuinely need shared state, make it explicit:
an `AtomicLong`, a `ConcurrentHashMap`, a bounded cache, or the database.

**Other scopes:** `prototype` (new instance per injection — note Spring does **not** manage
its destruction lifecycle), `request`, `session`, `application`, `websocket`. Request and
session scopes are `ThreadLocal`-backed, which is why they **break across async
boundaries** (quiz 07 Q3), and injecting a request-scoped bean into a singleton needs a
scoped proxy.

**The migration angle:** constructor injection with `final` fields gets you safe
publication and immutability for the *dependencies*. Idiomatic Scala goes further —
constructor injection is just constructing an object, everything is a `val`, and there's no
container to make sharing implicit (quiz 07 Q5).
</details>

---

### 4. Constructor vs field vs setter injection — which and why?

<details><summary>Answer</summary>

**Constructor injection, essentially always.** Reasons, in order of weight:

1. **Immutability** — fields can be `final`, so the bean is safely published and can't be
   reassigned. `@Autowired` on a field cannot be `final`.
2. **Mandatory dependencies are enforced by the compiler.** You cannot construct the object
   in an invalid state; a missing dependency fails at startup, not at first request.
3. **Testable without Spring.** `new PositionService(mockRepo, mockClient)` — no context, no
   reflection, no `@MockBean`, so unit tests run in milliseconds.
4. **It surfaces design smells.** A constructor with nine parameters is *telling you* the
   class does too much. Field injection hides that.
5. **No circular-dependency surprises.** A constructor cycle fails fast at startup with a
   clear error; field injection can silently allow cycles that half-work.

Spring doesn't even need `@Autowired` on a single-constructor class (since 4.3).

**Field injection** (`@Autowired private Foo foo;`) — concise, and that's its only merit.
Non-final, hides dependencies from the public API, untestable without reflection or a
context, and permits cycles. Spring's own docs recommend against it, and IntelliJ warns.
**Setter injection** — for genuinely optional or reconfigurable dependencies. Rare.

**A good closing line:** "constructor injection means the class is honest about what it
needs, and works fine without Spring at all — which is a sign the design isn't
framework-dependent."
</details>

---

### 5. Explain `@Transactional`: propagation, rollback rules, and how it can silently do nothing.

<details><summary>Answer</summary>

**How it works:** Spring wraps the bean in an **AOP proxy**. On entry the proxy opens a
transaction and binds the connection to the current thread via
`TransactionSynchronizationManager` (a `ThreadLocal`); on normal return it commits; on a
qualifying exception it rolls back.

**Three ways it silently does nothing** — the important part:

1. **Self-invocation.** Calling `this.doWork()` from another method of the same bean
   **bypasses the proxy**, so the annotation has no effect. Same problem as `@Async`. Fix:
   inject the bean into itself, use `AopContext.currentProxy()`, split into two beans (best),
   or switch to AspectJ weaving.
2. **Non-public methods.** With the default proxy mode, `@Transactional` on a `private`,
   `protected`, or package-private method is ignored.
3. **Returning a `Future`/`CompletableFuture`/Scala `Future`.** The method returns
   immediately, so the proxy **commits before the work happens**, and a failure inside the
   future never reaches the proxy, so **there's no rollback**. This is the migration
   landmine — see **quiz 07 Q2** for the full treatment.

**Rollback rules — the one that surprises people:** by default Spring rolls back on
**`RuntimeException` and `Error`**, and **commits on checked exceptions**. So:
```java
@Transactional
void process() throws IOException { repo.save(x); throw new IOException("failed"); }  // COMMITS!
```
Override with `@Transactional(rollbackFor = Exception.class)`.
Also: **catching the exception inside the method prevents rollback** — the proxy never sees
it. And once a transaction is marked rollback-only, a subsequent commit attempt throws
`UnexpectedRollbackException`.

**Propagation:**
- `REQUIRED` (default) — join an existing transaction, or start one.
- `REQUIRES_NEW` — suspend the outer, start an independent one. Uses a **second connection**,
  so it can deadlock against its own parent, and it commits independently.
- `NESTED` — a savepoint within the current transaction; rolls back to the savepoint only.
- `SUPPORTS`, `NOT_SUPPORTED`, `MANDATORY`, `NEVER`.

**Isolation:** `READ_UNCOMMITTED`, `READ_COMMITTED` (typical default), `REPEATABLE_READ`
(MySQL's default), `SERIALIZABLE`. See quiz 11 Q8 for the anomalies each prevents.

**The practical rules to state:**
- **Keep transactions short.** Never do an HTTP call inside one — you hold a DB connection
  for the duration of someone else's latency, and that's how you exhaust the pool
  (directly relevant to Q1's design).
- Put the boundary on the **service**, not the controller or the repository.
- `readOnly = true` for queries — lets the driver and DB optimise, and Hibernate skip dirty
  checking.
- Set an explicit `timeout`.

**Scala/migration:** move to a transaction abstraction that's a **value, not a
thread-local** — doobie's `ConnectionIO` with `.transact(xa)`, Slick's `DBIO`, or ZIO Quill.
No proxies, no thread-affinity, no silent failure.
</details>

---

### 6. `RestTemplate` vs `WebClient` vs Feign — which for calling another team's service?

<details><summary>Answer</summary>

- **`RestTemplate`** — synchronous, blocking. **In maintenance mode** since Spring 5 (not
  deprecated, but no new features). Fine in existing code; don't choose it for new work.
- **`WebClient`** — the modern client, non-blocking and reactive (`Mono`/`Flux`), but usable
  synchronously via `.block()`. Works in a non-reactive app. This is the default
  recommendation now.
- **`Feign`** (Spring Cloud OpenFeign) — **declarative**: you write an annotated interface
  and it generates the implementation. Least boilerplate, integrates with Eureka/Ribbon and
  Resilience4j. Good when you have many endpoints against a service, or a shared client
  library.
- **`RestClient`** (Spring 6.1+) — a synchronous client with `WebClient`'s fluent API. The
  natural `RestTemplate` replacement, and a good pairing with virtual threads.
- **The JDK's own `HttpClient`** (JDK 11+) — no Spring dependency, sync and async, HTTP/2.

**What actually matters, whichever you pick** — and say this, because it's the difference
between naming libraries and having operated a service:

1. **Timeouts, always both.** Connect *and* read/response. The default in several clients is
   **infinite**, which means one hung downstream call holds a request thread forever.
2. **Connection pooling.** Don't create a client per request. Size the pool, and set an
   acquisition timeout.
3. **Retries — carefully.** Only for **idempotent** operations, with **exponential backoff
   and jitter**, and a bounded attempt count. Naive retries amplify an outage into a retry
   storm; and your retries multiply *their* load exactly when they're already struggling.
4. **Circuit breaker** (Resilience4j) so you fail fast instead of queueing against a dead
   service.
5. **Bulkhead** — a separate pool/semaphore per downstream, so one slow dependency can't
   consume all capacity (quiz 02 Q4).
6. **Propagate trace context** so the call is visible end-to-end.
7. **Wrap it behind your own interface** (Q1) so their DTOs don't leak into your domain.

**Testing:** WireMock or MockWebServer for the HTTP contract; **Pact** for consumer-driven
contract tests with the other team, which is the real answer to "how do you know when they
change their API".
</details>

---

### 7. How do you handle exceptions across an API?

<details><summary>Answer</summary>

**Centralise it** with `@RestControllerAdvice`, so mapping lives in one place and controllers
stay thin:

```java
@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(PositionNotFoundException.class)
    ProblemDetail notFound(PositionNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)   // @Valid failures
    ProblemDetail invalid(MethodArgumentNotValidException e) { ... }   // 400 + field errors
    @ExceptionHandler(DownstreamUnavailableException.class)
    ProblemDetail downstream(DownstreamUnavailableException e) { ... } // 503
    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception e) {
        log.error("unhandled", e);                            // log the stack ONCE, here
        return ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);  // leak nothing
    }
}
```

**Principles:**
- **Domain exceptions in the service, HTTP status in the advice.** The service shouldn't know
  what a 404 is.
- **`ProblemDetail`** (Spring 6 / RFC 9457) is the standard error body — use it rather than
  inventing a shape.
- **Never leak internals** — no stack traces, SQL, or class names in a response. Return a
  **correlation id** the client can quote, and log the detail server-side against that id.
- **Log once, at the boundary.** Logging and rethrowing at every layer produces five copies
  of one stack trace and makes logs useless.
- **Distinguish 4xx from 5xx properly**: client error vs your failure. Don't return 500 for
  a validation problem, and don't return 400 for your own bug. This matters because your
  alerting and SLOs are built on it.
- **`@Valid`/`@Validated`** with Bean Validation for request validation, so it fails before
  reaching your logic.

**Scala:** put errors in the return type — `Either[DomainError, A]`, ZIO's typed `E`, or
`Validated` to *accumulate* validation failures rather than failing on the first. Then map
`DomainError → HTTP status` once at the edge. Same architecture, enforced by the compiler
(quiz 09 Q11).
</details>

---

### 8. How do you test a Spring application at each layer?

<details><summary>Answer</summary>

**A pyramid, fastest first** — and the key insight is that **most tests shouldn't need
Spring at all**:

1. **Plain unit tests, no Spring.** The service with mocked collaborators:
   `new PositionService(mockRepo, mockClient)`. Milliseconds. **This is where most of your
   tests belong**, and constructor injection is what makes it possible.
2. **Slice tests** — load only part of the context:
   - `@WebMvcTest` — controllers + JSON serialisation + validation, with `MockMvc`. Services
     mocked via `@MockitoBean` (`@MockBean` in older Spring).
   - `@DataJpaTest` — repositories against a real transactional DB, rolled back per test.
   - `@JsonTest`, `@RestClientTest`.
3. **Integration tests** — `@SpringBootTest` with the full context. Slow; use sparingly for
   wiring and the critical paths.
   - **Testcontainers** for a real Postgres/Kafka rather than H2 — H2 accepts SQL Postgres
     rejects, so it passes tests and fails in prod. Worth saying explicitly.
   - **WireMock** for the other team's service.
4. **Contract tests** — **Pact**, so you find out at *build* time when the other team changes
   their API rather than in production.
5. **End-to-end** — a handful, against a deployed environment.

**Things worth volunteering:**
- **`@MockBean`/`@MockitoBean` replaces a bean in the context and therefore creates a new
  context**, and each distinct context configuration is cached separately. A test suite with
  many different mock combinations spends most of its time starting contexts — a very common
  cause of slow builds.
- **`@Transactional` on a test** rolls back after each test, which keeps them isolated —
  **but it means the test runs in a transaction the production code wouldn't have**, so it
  can hide lazy-loading errors and commit-time constraint violations.
- **Don't test the framework.** Testing that `@GetMapping` maps is testing Spring.
- **Concurrency tests:** see quiz 07 Q9 — prefer making it deterministic (inject the
  executor and clock) over stress tests, because a passing stress test proves very little
  (P06 measured a genuinely buggy queue passing a 600-item test).
</details>

---

### 9. How does Spring Boot auto-configuration work?

<details><summary>Answer</summary>

`@SpringBootApplication` = `@SpringBootConfiguration` + `@ComponentScan` +
**`@EnableAutoConfiguration`**.

Auto-configuration reads candidate configuration classes from
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` on the
classpath (it was `spring.factories` before Boot 2.7). Each candidate is guarded by
**`@Conditional`** annotations:

- `@ConditionalOnClass` — "is Hibernate on the classpath?"
- `@ConditionalOnMissingBean` — **"has the user already defined one?"** This is the key to
  the whole design: it's why declaring your own `DataSource` silently *replaces* Boot's,
  with no configuration flag.
- `@ConditionalOnProperty`, `@ConditionalOnWebApplication`, `@ConditionalOnBean`.

So: put `spring-boot-starter-data-jpa` on the classpath and set a datasource URL, and you
get a `DataSource`, an `EntityManagerFactory`, a `TransactionManager`, and repository
proxies — because the classes are present and you haven't defined your own.

**Debugging it — the practical part:**
- **`--debug`** or `debug=true` prints the **auto-configuration report**: which
  configurations matched, which didn't, and *why not*. This is the answer to "why isn't my
  bean there".
- `/actuator/conditions` exposes the same at runtime.
- **`spring.autoconfigure.exclude`** or `@SpringBootApplication(exclude = ...)` to turn one off.
- `@ConfigurationProperties` binds `application.yml` to a typed, validated object —
  preferable to scattered `@Value`.

**The trade-off worth naming:** auto-configuration is enormously productive and it makes
behaviour **implicit**. When something is misconfigured you're debugging a conditional graph
you didn't write. That's precisely the contrast with **ZIO's `ZLayer`**, where the dependency
graph is explicit and **checked at compile time** (quiz 06 C5) — you trade convenience for
"it won't compile" instead of "no qualifying bean of type".
</details>

---

### 10. What would you expose for observability?

<details><summary>Answer</summary>

**The four things, and the ones people forget:**

1. **Metrics** — Micrometer to Prometheus. Beyond the defaults (`http.server.requests`, JVM
   memory, GC, threads), the ones that matter:
   - **Connection pool**: `hikaricp.connections.pending`, `.timeout`, `.active`. Pending is
     one of the best leading indicators of overload you have.
   - **Executor queue depth** for every pool you create (quiz 02 Q2).
   - **Downstream calls**: latency, error rate, circuit-breaker state.
   - **Cache** hit rate and eviction count.
   - Business metrics — orders processed, enrichment failures.
2. **Structured logging** with a **correlation/trace id** on every line. JSON if the
   platform aggregates it. And the trap: **MDC is `ThreadLocal`-backed and vanishes across
   async boundaries** (quiz 07 Q3).
3. **Distributed tracing** — Micrometer Tracing / OpenTelemetry, with context propagated to
   the other team's service so the spans join up. Essential for Q1's design.
4. **Health checks** — and distinguish them properly: **liveness** ("restart me") must not
   depend on downstreams, or a database blip restarts every pod. **Readiness** ("stop
   sending traffic") may. Getting this backwards causes cascading restarts, and it's a good
   detail to raise.

**Also:** `/actuator` endpoints (secured!), **JFR always on**
(`-XX:StartFlightRecording=maxsize=256m,maxage=6h`), GC logging always on
(`cheatsheets/gc-flags-and-logs.md`), and `-XX:+HeapDumpOnOutOfMemoryError`.

**Define SLOs**, then alert on **symptoms** (error rate, latency) rather than causes (CPU).
And say the migration-specific point from quiz 07 Q10: **thread count stops being a useful
health metric** once you move to fibers or virtual threads — the equivalent signals become
queue depth, fiber count, and in-flight requests. Teams lose an alert they were relying on
and don't notice.
</details>

---

### 11. How do you manage configuration and secrets?

<details><summary>Answer</summary>

**Configuration:**
- **`@ConfigurationProperties`** on a typed record/class, with `@Validated` and JSR-380
  constraints, so bad config **fails at startup** rather than at first use. Strongly prefer
  this to scattered `@Value` — it's testable, documented by its own type, and validated once.
- **Profiles** (`application-{prod,uat,local}.yml`) for environment differences, but resist
  profile sprawl — profile-specific *code* paths are a maintenance trap.
- **Externalise** anything environment-specific. Boot's property precedence: command line >
  env vars > profile-specific file > application.yml > defaults. Know that ordering; it's
  the answer to "why isn't my setting taking effect".
- **Never commit environment-specific values.** Same artifact promoted through
  environments, configuration injected (this is the "build once, promote" point from
  quiz 08 Q3).

**Secrets:**
- **Never in source control, never in `application.yml`, never in a Docker image layer.**
- A secret manager: Vault, AWS Secrets Manager, or the platform's mechanism (Kubernetes
  Secrets — noting they're only base64-encoded at rest unless encryption is enabled).
- Injected as **env vars or mounted files** at runtime.
- **Rotatable** without a redeploy, ideally.
- Don't log them — and watch for accidental leakage via `toString()` on a config object,
  `/actuator/env`, or an exception message. `@ConfigurationProperties` classes are a common
  offender; override `toString`.
- Secure or disable the actuator endpoints that expose configuration.

**Scala:** **PureConfig**, **Ciris**, or **zio-config** — decode into a case class, validated
at startup, typed. Ciris is notable for treating secrets as first-class and redacting them
from `toString` by default, which is a genuinely better default than Spring's.
</details>

---

### 12. What is AOP, and where does Spring use it?

<details><summary>Answer</summary>

**Aspect-Oriented Programming** factors out **cross-cutting concerns** — logging,
transactions, security, caching, metrics — that would otherwise be duplicated in every
method.

**Vocabulary** (they may want the terms): **aspect** (the module), **join point** (a point
in execution — for Spring, a method call), **pointcut** (the expression selecting join
points), **advice** (the code to run: `@Before`, `@After`, `@AfterReturning`,
`@AfterThrowing`, `@Around`), **weaving** (applying it).

**How Spring does it:** **proxies**, created at startup — a JDK dynamic proxy if the bean
implements an interface, otherwise a CGLIB subclass. *Not* bytecode weaving by default
(AspectJ does that, and Spring can delegate to it via `@EnableLoadTimeWeaving`).

**Where you already use it:** `@Transactional`, `@Async`, `@Cacheable`, `@PreAuthorize`,
`@Retryable`, `@Timed`, `@Validated`. All proxies.

**The consequences of it being proxy-based — the reason this question matters:**
1. **Self-invocation doesn't work.** `this.method()` skips the proxy, so the annotation does
   nothing (Q5). This single fact explains a whole family of "my annotation isn't firing"
   bugs.
2. **Only `public` methods** are advised in the default proxy mode.
3. **`final` classes and methods can't be CGLIB-proxied.**
4. **The object you inject is not the object you wrote** — which is why a debugger shows you
   `PositionService$$EnhancerBySpringCGLIB` and why stack traces have extra frames.
5. Each proxy layer adds a small call-overhead — irrelevant except in very hot paths.

**Use it sparingly for your own aspects.** It's implicit behaviour invisible at the call
site, and heavy custom AOP makes code very hard to follow. The framework's uses earn it;
yours usually don't.

**Scala:** you don't need it. Cross-cutting behaviour is a **higher-order function** or a
wrapper around your effect: `withMetrics(withRetry(withLogging(operation)))`, or a
middleware chain in http4s. Explicit, visible at the call site, type-checked, and no proxy
magic. That's a legitimately better answer to the same problem, and a good note for the
migration.
</details>

---

### 13. Where should validation and caching live?

<details><summary>Answer</summary>

**Validation — in layers, with different purposes:**
- **Web layer**: **syntactic** validation. `@Valid` + Bean Validation on the request DTO
  (`@NotNull`, `@Size`, `@Pattern`). Reject malformed input before it reaches business
  logic → 400 with field-level detail from `MethodArgumentNotValidException`.
- **Service layer**: **semantic/business** validation. "This instrument isn't tradeable",
  "the account is frozen" — rules requiring domain knowledge or a DB lookup. These are
  domain exceptions, not 400s necessarily.
- **Persistence/DB**: **invariants** — NOT NULL, unique and foreign-key constraints, checks.
  **The last line of defence, and the only one that's actually reliable**, since it holds
  regardless of which application writes. Never rely *only* on application validation.

The distinction to state: **application validation is for good error messages; database
constraints are for correctness.** You want both.

**Caching:**
- `@Cacheable`/`@CacheEvict`/`@CachePut` with a Caffeine or Redis backend. Belongs on the
  **service** layer (or the client wrapper), not the controller or repository.
- **`@Cacheable` is also proxy-based**, so self-invocation doesn't work here either.
- Decide explicitly, and be able to say all of these: **the key**, the **TTL / max size**,
  the **eviction/invalidation** strategy, and whether stale data is acceptable. Those are
  the three things `@Cacheable` hides and that everyone gets wrong.
- **Always bound it.** An unbounded cache is a memory leak (measured in
  `problems/gc-labs/L02Leak.java` — 124 MB and an OOM in ~2 seconds; the bounded version
  sits flat at 13 MB forever).
- **Cache stampede**: N concurrent misses for the same key trigger N loads. You want
  **single-flight** loading — Caffeine's `AsyncLoadingCache`, or the
  `CompletableFuture`-as-value trick (implemented and verified in `P09Exercises` EX4:
  loader called exactly once for 16 concurrent gets).
- For Q1's design, caching the **reference data** is usually the single highest-value
  change: it's slow-changing, and it removes the downstream from your critical path.
</details>

---

### 14. Same endpoint, Scala. What changes?

<details><summary>Answer</summary>

Structurally very little — **the layering is the same** — but the mechanisms differ:

```scala
// Dependencies are constructor params. No annotations, no scanning, no container.
class PositionService(repo: PositionRepository, refData: ReferenceDataClient) {
  def getEnriched(id: Long): IO[EnrichedPosition] =
    for {
      p   <- repo.findById(id).flatMap(IO.fromOption(_)(PositionNotFound(id)))
      ref <- refData.lookup(p.instrumentId).timeout(500.millis)
    } yield EnrichedPosition.of(p, ref)
}

// Wiring happens once, explicitly, at the edge:
val service = new PositionService(new DoobiePositionRepository(xa), new Http4sRefDataClient(client))
```

| Spring | Scala |
|---|---|
| `@RestController` | http4s `HttpRoutes`, Tapir endpoints, Pekko HTTP, ZIO HTTP |
| `@Service` | a plain class |
| `@Repository` + JPA | doobie / Slick / Quill |
| Feign client | http4s `Client`, sttp |
| `@Transactional` | `.transact(xa)` — **a value, not a thread-local** |
| DI container | constructor calls, MacWire, or `ZLayer` |
| `@Valid` | `Validated`/`ValidatedNel` (**accumulates** errors), refined types |
| `@ControllerAdvice` | map `Either[DomainError, A]` → response at the edge |
| `@Cacheable` | Caffeine wrapped in the effect, or `Ref` |
| `@Async` | it's in the return type: `IO`/`Future`/`ZIO` |
| auto-configuration | explicit wiring; **compile-time checked** with `ZLayer` |

**What you gain:** wiring errors become **compile errors**; no proxies so no self-invocation
trap; `@Transactional` can't silently not-transact; timeouts and retries are composable
combinators; and with Cats Effect/ZIO you get **real cancellation** — if the client
disconnects, the enrichment call is actually interrupted and its resources released, which
Spring simply cannot do (quiz 06 C4, and P05 Part 6 measures the failure).

**What you lose, and be honest about it:** auto-configuration, actuator, the metrics and
health-check ecosystem, and the enormous body of Spring documentation and Stack Overflow
answers. You assemble those yourself. Teams that don't budget for this end up rebuilding
Spring Boot badly.

**And the intermediate option worth naming:** you can **write Scala inside Spring Boot**.
It's a legitimate migration step and means you fight one battle at a time (quiz 07 Q6).
</details>

---

## Score yourself

Q1 is the actual question from the bank — be able to sketch the four components *and* the
seven production concerns. Q5 (`@Transactional`) and Q3 (singleton state) are the two most
likely deep-dives, and both connect straight to the concurrency material. Q14 is your
bridge to the migration conversation.
