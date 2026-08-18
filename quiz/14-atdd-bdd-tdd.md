# Quiz 14 — TDD, ATDD, BDD & SDLC Automation

The JD explicitly lists **"Specification by Example / Feature Files / ATDD / BDD"** and
**"rigorously adheres to TDD"**. That phrasing means they'll expect more than "yes I write
tests" — expect a scenario-based question like *"walk me through the feature file for a
settlement scenario"*.

12 questions. Target: 20 min. Overlaps deliberately with `quiz/08` Q4 (Definition of Done)
and `quiz/13` Q15 (testing event-sourced systems).

---

### 1. What is TDD, actually? Walk through the cycle.

<details><summary>Answer</summary>

**Red → Green → Refactor**, in small increments:
1. **Red** — write a failing test for the next small piece of behaviour. Watch it fail, for the
   *right reason*. (A test that passes before you write the code is testing nothing.)
2. **Green** — write the simplest code that makes it pass. Deliberately naive is fine here.
3. **Refactor** — with the test as a safety net, improve the design. No new behaviour.

**What it's actually for** — and this is where most candidates give a weak answer. TDD is
**primarily a design technique**, and regression tests are a by-product:
- It forces you to define **the interface before the implementation**, from the caller's
  point of view. Code that's hard to test is usually badly coupled, so TDD surfaces design
  problems *while they're cheap to fix*.
- It gives you **fast feedback** and keeps you working in small, verifiable steps.
- It produces a suite you **trust enough to refactor against** — which is the difference
  between a codebase you can change and one you can only add to.
- It **documents intent** in executable form.

**Say what "rigorous" means honestly**, because a claim of purity invites a challenge:
> I write tests first for logic — domain rules, state machines, calculations, parsing, error
> handling. For exploratory work I sometimes spike first and then write the tests before
> committing, because I don't always know the design yet. What I hold to is that **nothing
> merges without tests**, and that a bug fix always starts with a **failing test that
> reproduces it** — otherwise you don't actually know you fixed it.

That's defensible and true of good teams. Claiming you never write a line of production code
without a failing test first is usually not credible.

**Common criticisms and the honest responses:**
- *"It's slow."* Slower to first commit, faster to a working system, and much faster to the
  fiftieth change. The cost is paid in the wrong place if you skip it.
- *"It leads to over-mocked, brittle tests."* A real failure mode — see Q6. It comes from
  testing implementation rather than behaviour, not from TDD itself.
- *"You can't TDD a UI or a spike."* Correct. Use it where it pays.
</details>

---

### 2. How does ATDD differ from unit-level TDD?

<details><summary>Answer</summary>

**Different scope, different audience, different question, and — importantly — a different
point in time.**

| | **TDD** | **ATDD** |
|---|---|---|
| Scope | a unit — class, function, aggregate | a **feature**, end to end through the system |
| Written by | the developer | **developer + QA + business/BA together** |
| Audience | developers | the whole team, including non-technical |
| Question | *"is this built right?"* | *"are we building the right thing?"* |
| When | during implementation | **before** implementation, as part of refining the story |
| Language | code | business language (often Given/When/Then) |
| Count | hundreds/thousands, milliseconds | a handful per story, slower |
| On failure | a bug in this unit | the feature doesn't do what was agreed |

**ATDD = Acceptance Test Driven Development.** Before writing any code, the team agrees on
**concrete examples** of the expected behaviour, expressed as acceptance tests. Those become
the definition of done for the story (quiz 08 Q4) and are automated.

**How they work together, which is the answer they want:**
```
ATDD outer loop:  write a failing acceptance test for the feature
   |
   +-- TDD inner loop:  red -> green -> refactor, many times, on units
   |
   until the acceptance test passes -> the feature is done
```
This is the **double-loop / outside-in** model. The acceptance test tells you *when you're
finished*; the unit tests tell you *whether each piece is right*. Naming the double loop is a
good signal.

**The relationship to BDD and Specification by Example** — these terms overlap heavily and
people use them interchangeably, so define your usage rather than assuming:
- **ATDD** — the practice: agree automated acceptance criteria before building.
- **Specification by Example** — the technique: express requirements as **concrete examples**
  rather than abstract prose, because abstraction is where ambiguity hides.
- **BDD** — Dan North's reframing of TDD around **behaviour** and a shared **ubiquitous
  language**, with Given/When/Then as the notation and often Cucumber as the tool.

**The point of all three is the conversation, not the tooling.** Getting business, QA and
developers to agree on concrete examples *before* code is where the value is — it's cheaper to
resolve a misunderstanding in a discussion than in UAT. Say that; it's what separates someone
who's done BDD from someone who's used Cucumber.
</details>

---

### 3. Write a feature file for a settlement scenario.

<details><summary>Answer</summary>

This is the likely live exercise. Structure it well and narrate your choices.

```gherkin
Feature: Trade settlement
  As an operations user
  I want trades to settle on their value date once confirmed and funded
  So that positions and cash are accurate and we meet our settlement obligations

  Background:
    Given the business date is 2026-08-18
    And account "ACC-001" is active

  Scenario: A confirmed and funded trade settles on its value date
    Given a trade "TRD-123" for 1,000 units of "GB00B16KPT44" at 99.50 GBP
    And the trade is confirmed
    And the trade has a value date of 2026-08-20
    And account "ACC-001" has available cash of 100,000.00 GBP
    When the business date becomes 2026-08-20
    And the settlement cycle runs
    Then the trade "TRD-123" is settled
    And the position for "ACC-001" in "GB00B16KPT44" increases by 1,000
    And the available cash for "ACC-001" decreases by 99,500.00 GBP
    And a "TradeSettled" event is published for "TRD-123"

  Scenario: Settlement fails when the account is underfunded
    Given a trade "TRD-124" for 1,000 units of "GB00B16KPT44" at 99.50 GBP
    And the trade is confirmed
    And the trade has a value date of 2026-08-20
    And account "ACC-001" has available cash of 500.00 GBP
    When the business date becomes 2026-08-20
    And the settlement cycle runs
    Then the trade "TRD-124" is marked as failed with reason "INSUFFICIENT_FUNDS"
    And the position for "ACC-001" in "GB00B16KPT44" is unchanged
    And a settlement failure notification is raised to operations

  Scenario: An unconfirmed trade does not settle
    Given a trade "TRD-125" that has not been confirmed
    And the trade has a value date of 2026-08-20
    When the business date becomes 2026-08-20
    And the settlement cycle runs
    Then the trade "TRD-125" is not settled
    And the trade "TRD-125" remains in status "PENDING_CONFIRMATION"

  Scenario Outline: Settlement is deferred over non-business days
    Given a trade with a value date of <valueDate>
    When the settlement cycle runs on <valueDate>
    Then settlement is <outcome>

    Examples:
      | valueDate  | outcome                       |
      | 2026-08-20 | attempted                     |
      | 2026-08-22 | deferred to the next business day |
      | 2026-08-25 | attempted                     |
```

**Narrate these choices while you write, because they're what's being assessed:**

1. **The `Feature` preamble states the *value*, not the mechanism.** "So that positions and
   cash are accurate" — if you can't complete that sentence, the story may not be worth doing.
2. **Business language only. No UI, no API, no SQL.** `When the settlement cycle runs`, not
   `When I POST to /settlements`. The scenario must survive a re-implementation; if it breaks
   when you change the technology, it was the wrong abstraction.
3. **Concrete examples, real values.** `1,000 units at 99.50 GBP`, not "some units". That's
   the whole point of Specification by Example — ambiguity hides in abstraction. And using a
   plausible ISIN rather than "SECURITY_A" shows domain familiarity.
4. **One behaviour per scenario.** Three scenarios, not one with three `When`s.
5. **Cover the unhappy paths.** Underfunded and unconfirmed are where the real requirements
   live, and where the disagreements surface in refinement. **A feature file with only the
   happy path is the tell of someone who hasn't done this.**
6. **`Scenario Outline`** for the same rule across data variations — but not for genuinely
   different behaviour, which deserves its own scenario.
7. **`Background`** for shared setup, kept short. If it grows past a few lines the scenarios
   are probably coupled.
8. **Declarative, not imperative.** `Given the trade is confirmed` — one line — rather than
   five steps walking through the confirmation workflow. Imperative Gherkin is the single most
   common failure mode: it becomes a script, it's unreadable, and every scenario breaks when
   the workflow changes.
9. **Note the domain detail:** value date vs trade date, business-day calendars, cut-off
   times. Raising those unprompted signals genuine post-trade familiarity (quiz 15).

**Expect the follow-up: "how many of these would you have?"** Answer: **few**. A handful of
acceptance scenarios per feature covering the significant behaviours and the key failure
modes, with the combinatorial detail pushed down to fast unit tests. See Q5 — the failure mode
of BDD is 800 slow scenarios nobody trusts.
</details>

---

### 4. How do the step definitions work, and what should they *not* do?

<details><summary>Answer</summary>

Step definitions bind Gherkin text to code. The critical design rule: **steps should call your
domain, not your UI or your HTTP layer**, wherever possible.

```scala
class SettlementSteps extends ScalaDsl with EN {
  private var events: List[TradeEvent] = Nil
  private var result: Either[TradeError, List[TradeEvent]] = Right(Nil)

  Given("""a trade {string} for {int} units of {string} at {bigdecimal} GBP""") {
    (id: String, qty: Int, isin: String, px: BigDecimal) =>
      events :+= Executed(TradeId(id), Quantity(qty), Money.gbp(px), clock.instant())
  }

  Given("""the trade is confirmed""") { () =>
    events :+= Confirmed(currentTradeId, clock.instant())
  }

  When("""the settlement cycle runs""") { () =>
    result = decide(replay(events), SettleTrade(currentTradeId, valueDate))
  }

  Then("""the trade {string} is settled""") { (id: String) =>
    result.value should contain(Settled(TradeId(id), valueDate, any))
  }
}
```

**This is where the event-sourced architecture pays off enormously** — and it's the connection
worth making explicitly. Because the write side is
`decide(state, command): Either[Error, List[Event]]` (quiz 13 Q8):

- **`Given` = a list of past events.** Setup is constructing a list. No database, no fixtures,
  no mocks, no containers.
- **`When` = handling one command.**
- **`Then` = asserting on the returned events.**

So Gherkin's Given/When/Then maps **exactly onto the architecture's own shape**, and the
acceptance tests run in milliseconds. That's presumably why this JD asks for event sourcing
*and* BDD in the same breath, and saying so shows you see how the pieces fit rather than
treating them as two separate bullet points.

**What step definitions should *not* do:**
- **Not drive a browser or an HTTP endpoint** unless the scenario is genuinely about the API
  contract. UI-driven Cucumber suites are slow, flaky, and the reason BDD gets a bad name.
- **Not contain business logic.** If a step computes an expected value, that logic is now in
  two places and the test can agree with itself while both are wrong.
- **Not share mutable state carelessly.** Use per-scenario scoped context, and remember
  Cucumber's runner can be parallel — shared mutable step state is a race (quiz 01).
- **Not be over-parameterised into meaninglessness.** `Given the {string} is {string}` matches
  everything and documents nothing.
- **Not duplicate.** Reuse steps; a suite with three variants of "given a trade" drifts.

**The layering to describe:** Gherkin → step definitions → **a thin test DSL / driver layer** →
the system. That middle layer (sometimes called the "screenplay" or "driver" pattern) is what
lets you point the same scenarios at a unit-level, service-level, or end-to-end
implementation. It's the difference between a BDD suite that survives and one that gets
deleted in eighteen months.
</details>

---

### 5. What goes wrong with BDD in practice?

<details><summary>Answer</summary>

Being able to answer this is what distinguishes experience from having read the book. The
failure modes, in rough order of frequency:

1. **Nobody but developers reads the feature files.** The entire justification for Gherkin's
   overhead is that business people engage with it. If they don't, you've taken on a clumsy
   test-authoring syntax for no benefit — you'd be better off with plain unit tests and
   readable names. **This is the most common outcome, and it's worth saying plainly.**
2. **Imperative Gherkin.** Scenarios become click-by-click scripts: "When I click Submit, then
   I see the confirmation page". Unreadable, brittle, and coupled to the UI.
3. **Too many scenarios, too slow.** 800 end-to-end scenarios taking 90 minutes. People stop
   running them, then stop trusting them, then start ignoring failures. **Flakiness is fatal**
   — a suite with 2% flaky tests trains the team to re-run rather than investigate.
4. **Testing at the wrong level.** Driving everything through the UI or a deployed environment
   when the behaviour could be tested against the domain in milliseconds (Q4).
5. **Combinatorial explosion in `Scenario Outline`.** Twenty rows of a data table belong in a
   parameterised unit test, not an acceptance suite.
6. **Written after the code.** Then it's just an awkward test framework, and you've lost the
   entire "agree before building" value — which was the point.
7. **The step-definition layer becomes its own untested codebase**, with duplicated logic and
   its own bugs.

**How to keep it healthy:**
- **Few scenarios, high value.** The significant behaviours and key failure modes; push
  detail down the pyramid.
- **Test at the lowest level that gives confidence** — domain-level steps by default.
- **Ruthless about flakiness** — quarantine and fix, never re-run-and-ignore.
- **Only use Gherkin where a non-developer genuinely engages.** If they don't, drop the
  ceremony and keep the practice: agree concrete examples, then write ordinary tests. That's
  ATDD without Cucumber, and it's often the right call.

**The honest position to state:** *"I've seen BDD work and I've seen it become a 90-minute
flaky suite nobody trusts. The difference was whether business stakeholders actually read the
scenarios, and whether the steps talked to the domain or the UI. So I'd want to know who the
audience is before I'd commit to Gherkin — the practice of agreeing examples first is
valuable regardless; the tooling only pays if someone outside the team reads it."*
</details>

---

### 6. What makes a good test? What makes a bad one?

<details><summary>Answer</summary>

**Good tests are FIRST:** **F**ast, **I**solated, **R**epeatable, **S**elf-validating,
**T**imely. Plus:

- **They test behaviour, not implementation.** A test that breaks when you refactor without
  changing behaviour is a liability — it's a tax on every future change. This is the single
  most important property.
- **One reason to fail**, and the name tells you what broke.
  `rejectsSettlementOfUnconfirmedTrade` not `testSettle3`.
- **Arrange-Act-Assert**, visibly separated.
- **No conditionals or loops in the test.** If a test has an `if`, it can pass without testing
  anything.
- **Deterministic.** No wall-clock `now()`, no random data without a fixed seed, no reliance
  on execution order, no sleeps. **Inject the clock** — this is also what makes testing
  time-based settlement logic possible at all (Q7).
- **Fails informatively.** `expected SETTLED but was PENDING_CONFIRMATION` beats
  `assertTrue(false)`.

**Bad tests:**
- **Over-mocked.** A test that mocks five collaborators and asserts on the interactions
  between them tests your *wiring*, not your behaviour — and it will pass while the system is
  broken. **Prefer tests that need no mocks**, which is why the pure
  `decide(state, command)` shape (quiz 13 Q8) and constructor injection (quiz 10 Q4) matter.
- **Assertion-free** — exercises code and asserts nothing. Passes forever.
- **Testing the framework.** Verifying that `@GetMapping` maps.
- **Coupled to each other** through shared mutable state or ordering.
- **Flaky.** Worse than no test: it consumes attention and trains the team to ignore red.
- **Coverage-driven.** 100% coverage with weak assertions is worse than 60% with strong ones,
  because it produces false confidence. **Coverage tells you what *isn't* tested; it says
  nothing about whether what is tested is tested well.** Useful as a gap-finder, dangerous as
  a target.
- **Snapshot/golden-file tests** used as a substitute for thinking — they lock in current
  behaviour including the bugs, and get blanket-regenerated when they fail.

**On mutation testing** — worth naming if you want to sound rigorous. PIT/Stryker mutate your
code and check whether your tests notice. It measures **test effectiveness** rather than
execution coverage, and it's the honest answer to "is our suite any good?"
</details>

---

### 7. How do you test time-dependent logic like value dates and settlement cycles?

<details><summary>Answer</summary>

Very relevant for post-trade, where nearly everything is date-driven, and a good question to
be ready for.

**Never call `Instant.now()`, `LocalDate.now()`, or `System.currentTimeMillis()` directly in
domain code.** That makes the logic untestable and non-deterministic. **Inject the clock:**

```scala
class SettlementService(clock: Clock, calendar: BusinessCalendar) {
  def isDue(trade: Trade): Boolean = !trade.valueDate.isAfter(LocalDate.now(clock))
}
// production:  Clock.systemUTC()
// test:        Clock.fixed(Instant.parse("2026-08-20T09:00:00Z"), ZoneOffset.UTC)
```
`java.time.Clock` exists precisely for this and is the standard answer.

**Then the domain-specific points that show familiarity:**
- **"Now" is a business date, not a wall clock.** Post-trade systems run on a **business
  date** that's set by an end-of-day process and may not equal the calendar date — during
  batch, or across regions. Model it explicitly (`BusinessDate` as an injected value), don't
  derive it from a clock. **Raising this unprompted is a strong domain signal.**
- **Business-day calendars per market.** T+2 in one market isn't T+2 in another, and holidays
  differ per currency and per exchange. That's an injected `BusinessCalendar`, and it needs
  its own tests (weekends, holidays, year boundaries, and the market-specific cases).
- **Time zones and cut-offs.** A trade booked at 23:00 New York time may be the next business
  date in London. Store instants in UTC, apply business rules in the market's zone, and test
  the boundary explicitly.
- **Value date vs trade date vs settlement date** — different things (quiz 15 Q2), and mixing
  them up is a real defect class.

**For asynchronous and scheduled behaviour:**
- **ZIO's `TestClock` / Cats Effect's `TestControl`** give you **virtual time**: `adjust(2.days)`
  and everything scheduled happens instantly and deterministically. Testing a T+2 settlement
  cycle or an exponential-backoff retry becomes a fast unit test instead of a sleep. **This is
  one of the strongest practical arguments for those libraries** (quiz 07 Q9, quiz 12 Q16).
- Otherwise, inject the scheduler so tests can trigger it synchronously.
- **Never `Thread.sleep` in a test.** It's slow when it works and flaky when it doesn't.

**And test the awkward dates deliberately:** month and year boundaries, leap years, DST
transitions, a value date falling on a holiday in one of two relevant markets. Those are where
settlement bugs actually live.
</details>

---

### 8. Describe the test pyramid you'd want, with rough numbers.

<details><summary>Answer</summary>

```
        /\        E2E / manual exploratory        ~10      minutes    (deployed env)
       /  \       Acceptance / BDD                ~50      seconds    (domain-level steps)
      /    \      Integration / contract         ~200      seconds    (Testcontainers, Pact)
     /      \     Unit                        ~2000+       seconds total, ms each
    /________\
```

**The principles that matter more than the numbers:**
- **Test at the lowest level that gives you confidence.** Cost, speed, and diagnostic clarity
  all improve as you go down; realism improves as you go up.
- **The whole pyramid should run in CI on every commit**, or people stop trusting it. If your
  suite takes 40 minutes, that's a defect to fix, not a fact to accept.
- **Failures should be diagnosable.** A failing unit test points at a function; a failing E2E
  test points at "something, somewhere". That's the real cost of top-heavy suites.

**The two anti-shapes to name:**
- **The ice-cream cone** — mostly manual and E2E, few unit tests. Slow, flaky, expensive, and
  common in legacy enterprise systems.
- **The hourglass** — lots of unit and lots of E2E, nothing in between. You miss integration
  problems entirely and then find them the slowest possible way.

**For this architecture specifically**, the pyramid is unusually favourable and worth saying:
- The **write side is a pure function**, so aggregate behaviour — the most valuable logic —
  tests at the **bottom** of the pyramid, in milliseconds, with no mocks (quiz 13 Q15).
- **Projections** are pure folds; test them with event lists, then once against a real
  database with Testcontainers.
- **Contract tests** (Pact) at the integration layer cover the boundaries with other teams,
  which is where the expensive surprises otherwise come from.
- **Deserialisation fixtures of real historical events** — the regression test for schema
  evolution (quiz 13 Q5/Q15). Cheap, fast, and it prevents a genuine outage class.

So the honest claim is: **event sourcing plus CQRS lets you push most of the valuable testing
to the fast layers**, which is a real architectural benefit rather than a slogan.
</details>

---

### 9. Walk me through your CI pipeline — Jenkins, Gradle, GitHub, Artifactory, Ansible.

<details><summary>Answer</summary>

The JD names these, so describe a concrete pipeline and the *reasoning*, not just the tools.

```
 commit / PR (GitHub)
   |
   +-- Jenkins pipeline (declarative Jenkinsfile, in the repo)
        |
        1. checkout, cache dependencies
        2. gradle compile + static analysis   (Scalafix/Scalastyle/scalafmt --check, SpotBugs)
        3. gradle test                        (unit -- must be fast, parallel)
        4. gradle integrationTest             (Testcontainers)
        5. acceptance / BDD suite
        6. coverage + quality gate            (JaCoCo/scoverage, SonarQube)
        7. dependency & container scanning    (OWASP, Snyk/Trivy)
        8. BUILD THE ARTIFACT ONCE            -> publish to Artifactory, immutable, versioned
        |
   +-- deploy: promote THE SAME artifact  dev -> UAT -> pre-prod -> prod
        |         (Ansible playbooks / Helm; config injected per environment)
        |
        +-- smoke tests + health checks after each deploy
```

**The principles to articulate — these are what's being assessed:**

1. **Build once, promote the same binary.** The artifact tested in UAT is bit-for-bit the one
   that reaches production; **only configuration differs** (quiz 10 Q11). Rebuilding per
   environment means you deployed something you never tested. **This is the single most
   important thing to say.**
2. **Immutable, versioned artifacts** in Artifactory, with retention policies. Never overwrite
   a version. Traceable back to a commit — build metadata baked into the jar's manifest.
3. **Pipeline as code** — `Jenkinsfile` and Gradle build in the repo, versioned and reviewed
   like any other code. Not click-configured jobs that nobody can reproduce.
4. **Fail fast, in cost order** — compile and lint before unit tests before integration before
   acceptance. Developers get feedback in minutes, not at the end.
5. **Quality gates that actually block.** A gate that only warns is documentation. But gate on
   things that matter (new critical findings, coverage *decrease*) rather than absolutes that
   force people to game them.
6. **Reproducible builds** — pinned dependency versions, a lock file, dependency verification.
   Gradle's build cache and configuration cache for speed.
7. **Idempotent deploys.** Ansible playbooks that converge to the desired state, so re-running
   is safe. Same property as event handlers (quiz 13 Q10).
8. **Secrets never in the pipeline definition** — a credentials store or vault, injected at
   runtime (quiz 10 Q11).
9. **Rollback is part of the pipeline**, not an improvised procedure. Redeploy the previous
   artifact version; and remember migrations need expand/contract because they aren't
   reversible (quiz 11 Q14, quiz 08 Q3).
10. **DORA metrics** if they ask about improvement: deployment frequency, lead time, change
    failure rate, MTTR. That reframes "we have CI" into "here's how we know it's working."

**Have one improvement story ready** — "I cut the pipeline from 40 minutes to 12 by
parallelising the integration tests and adding a Gradle build cache" is worth more than a
tool list, and it also answers "tell me about your impact" (quiz 08).

**Gradle-specific if pushed:** multi-project builds, `api` vs `implementation` (which controls
what leaks onto consumers' compile classpaths and matters a lot for build times), the
Scala/`sbt` question (many Scala shops use sbt or Mill; a bank standardising on Gradle for
polyglot consistency is common and worth asking about), version catalogs, and the build cache.
</details>

---

### 10. A poorly performing application or query lands on your desk. How do you approach it?

<details><summary>Answer</summary>

**Method, not a tip list.** And the first move is always to establish *where* the time goes
before changing anything.

**1. Quantify and reproduce.** What's slow, by how much, against what target? p50 or p99?
Always or sometimes? Which endpoint, which query, which input? "The app is slow" is not
actionable; "the settlement search p99 is 3 s against a 500 ms SLO, only for accounts with
>10 000 trades" is.

**2. Measure, don't guess.** This is the whole answer.
- **Is it CPU-bound or waiting?** High latency with **low CPU** means waiting — queueing,
  locks, I/O, or a downstream (quiz 02 Q15). High CPU means computation or GC.
- **Profile**: async-profiler for a CPU flame graph, `-e alloc` for allocation, `-e lock` for
  contention. **JFR** for a whole-system timeline correlating GC, allocation, locks and I/O.
- **Check GC**: `-Xlog:gc*`, and compare pause distribution against the latency gap. If p99 is
  500 ms over budget and the worst pause is 20 ms, **GC is not your problem** (quiz 05 Q16).
- **Check the pools**: connection pool `pending`, executor queue depth. Queueing at a bounded
  resource is the most common cause of this exact symptom.
- **For a query**: `EXPLAIN (ANALYZE, BUFFERS)` — and look for estimated-vs-actual row
  divergence, sequential scans, and spills (quiz 11 Q1).

**3. Find the actual bottleneck, then fix that one thing.** In practice, the repeat offenders:
- **N+1 queries** — invisible in a slow-query log because each query is fast (quiz 11 Q9).
- **`O(n²)` in ordinary code** — `List.contains` in a loop, measured at **114×** in
  `P11Collections.java` Part 5.
- **A missing or unusable index** — a function on the column, an implicit cast (quiz 11 Q4).
- **Unnecessary work** — fetching columns or rows you discard, no pagination, no caching of
  slow-changing reference data.
- **Serialisation and allocation** on the hot path (measured in `S03AllocationPressure.scala`).
- **A blocking call in a compute pool** — starves everything, at low CPU (measured in
  `S01BlockingGlobalEC.scala`).

**4. Verify.** Re-measure the same way. Confirm you haven't traded one problem for another —
a new index costs every write; a cache adds an invalidation problem.

**5. Prevent recurrence.** A performance test at production data volume in CI, an alert on the
SLO, and a query-count assertion if it was an N+1.

**The framing to lead with:** *"I'd measure before touching anything, because the intuitive
answer is wrong often enough to be expensive — and the first thing I'd establish is whether
we're CPU-bound or waiting, since those have completely different fixes."*
</details>

---

### 11. How do you write tests for legacy code with no tests?

<details><summary>Answer</summary>

A likely follow-up to a TDD claim, since the JD's system is a Java codebase being migrated —
and it's a genuinely useful skill to demonstrate.

**The core problem:** legacy code isn't testable *because* it wasn't written test-first —
hard-coded dependencies, static calls, `new` in the middle of logic, no seams.

**The approach (Feathers' *Working Effectively with Legacy Code* is the reference, and naming
it is fine):**

1. **Start with a characterisation test**, not a correct one. Write a test that captures what
   the code **currently does**, including behaviour you think is wrong. You're building a
   safety net, not asserting correctness. If the output is a mess, assert on the mess.
2. **Find a seam** — a place you can change behaviour without editing the code under test.
   Constructor parameters, an overridable method, an interface you can slip in.
3. **Break dependencies with the smallest safe change**: *extract and override*, parameterise
   the constructor, extract an interface, wrap the static call in an instance method you can
   stub. Do these **mechanically**, ideally with IDE refactorings, which are much safer than
   hand-editing untested code.
4. **Now you have a test.** Refactor behind it.
5. **Apply the boy-scout rule and the strangler fig** — don't attempt a big-bang rewrite. Add
   tests around what you touch, and route new behaviour through a new, tested path (quiz 07's
   migration framing).

**Two things worth adding:**
- **Approval/golden-master testing** is very effective here: capture the output for a broad
  set of real inputs, store it, and assert nothing changes. Coarse and imprecise, but it lets
  you refactor a big untested lump safely. The caveat is that it locks in bugs — which is
  exactly what you want *temporarily*.
- **Prioritise by risk × change frequency**, from version-control history. Don't try to cover
  everything; cover what you're about to change and what breaks most often.

**The migration-relevant point to close on:** *"For a Java-to-Scala migration this matters
double, because the characterisation tests are what let you prove the ported behaviour
matches. I'd want tests around the existing behaviour **before** porting it — otherwise you're
changing language and behaviour at the same time and you can't tell which broke it."*

That's a genuinely strong observation for this specific role.
</details>

---

### 12. What's your Definition of Done, and how does testing fit?

<details><summary>Answer</summary>

> **Fully covered in `quiz/08` Q4**, with the full checklist and a worked example. Cross-
> referenced here because it will very likely be asked alongside the TDD/BDD questions and
> you should give **the same answer** to both — consistency between rounds gets noticed.

The short version: acceptance criteria met and demonstrated; reviewed; unit and integration
tests passing and the regression suite green; static analysis clean; docs and runbook updated;
**observability added**; deployed to pre-prod and validated; UAT sign-off; rollback plan
documented.

The two lines from quiz 08 worth reusing verbatim:
- **"Observability added"** — most candidates omit it, and "done" without monitoring isn't done.
- **"The DoD only mattered because it was enforced — it was in the PR template and the
  pipeline blocked on most of it. When it was just a wiki page, it got skipped."**
</details>

---

## Score yourself

**Q3 is the one to actually practise writing** — it's the likely live exercise, and the JD
names feature files explicitly. Q2 (ATDD vs TDD) and Q5 (what goes wrong) are the ones where a
considered answer stands out. Q4's point — that Given/When/Then maps exactly onto
`decide(state, command)` — is the connection between this file and `quiz/13`, and worth making
unprompted.
