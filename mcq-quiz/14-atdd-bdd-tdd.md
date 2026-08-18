# MCQ 14 — TDD, ATDD, BDD & SDLC Automation

10 questions. Source: [`quiz/14`](../quiz/14-atdd-bdd-tdd.md).

**Note:** `quiz/14` Q3 (write the settlement feature file) stays in the source file — it's the likely
live exercise and needs writing by hand, not selecting.

---

### Q1. What is TDD primarily for?

- **A.** Producing a regression test suite
- **B.** Achieving high code coverage
- **C.** It's a **design** technique — it forces you to define the interface from the caller's point of view before the implementation
- **D.** Catching bugs before QA

<details><summary>Answer</summary>

**C.** The regression suite is a **by-product**. Code that's hard to test is usually badly coupled, so
TDD surfaces design problems while they're cheap to fix — and it produces a suite you trust enough to
refactor against, which is the difference between a codebase you can change and one you can only add
to.

**A**, **B** and **D** are all real benefits, but leading with them is the weaker answer.

**Say what "rigorous" means honestly**, because claiming purity invites a challenge: tests first for
logic; sometimes spike then test before committing for exploratory work; and **a bug fix always starts
with a failing test that reproduces it** — otherwise you don't know you fixed it.

→ `quiz/14` Q1
</details>

---

### Q2. What's the key difference between ATDD and unit-level TDD?

- **A.** ATDD is feature-scoped, written **with business and QA before** implementation, answering "are we building the right thing?"
- **B.** ATDD uses different tools
- **C.** ATDD runs after deployment
- **D.** ATDD is only for UI testing

<details><summary>Answer</summary>

**A.** Different scope, audience, question, **and point in time**. TDD is a developer practice during
implementation asking *"am I building it right?"*; ATDD is a whole-team practice **before**
implementation asking *"are we building the right thing?"*

They nest — the **double loop**: the outer loop is a failing acceptance test telling you when the
feature is done; the inner loop is red-green-refactor on units, many times, until it passes. Naming
the double loop is a good signal.

And the framing that matters: **the point of all of it is the conversation, not the tooling.** Getting
business, QA and developers to agree on concrete examples before code is cheaper than discovering the
misunderstanding in UAT. Gherkin is optional; the shared understanding is the deliverable.

→ `quiz/14` Q2
</details>

---

### Q3. Which Gherkin step is written correctly?

- **A.** `When I click the Submit button on the settlement screen`
- **B.** `When I POST to /api/settlements with the trade id`
- **C.** `When settlementService.settle(tradeId) is called`
- **D.** `When the settlement cycle runs`

<details><summary>Answer</summary>

**D.** **Business language only**, so the scenario survives a re-implementation. If it breaks when you
change the technology, it was the wrong abstraction.

**A** is **imperative Gherkin** — the single most common failure mode. Scenarios become click-by-click
scripts: unreadable, brittle, coupled to the UI, and every scenario breaks when the workflow changes.
**B** and **C** leak the transport and the implementation.

Also **declarative over imperative in the `Given`**: `Given the trade is confirmed` (one line) rather
than five steps walking through the confirmation workflow.

→ `quiz/14` Q3
</details>

---

### Q4. What should step definitions call?

- **A.** A browser via Selenium
- **B.** The domain directly — which in an event-sourced system means `Given` is a list of events, `When` is one command, `Then` asserts on the events returned
- **C.** The HTTP API, for realism
- **D.** The database, to verify persistence

<details><summary>Answer</summary>

**B.** And this is where the architecture pays off enormously. Because the write side is
`decide(state, command): Either[Error, List[Event]]`, Gherkin's Given/When/Then maps **exactly onto the
architecture's own shape** — no database, no fixtures, no mocks, running in milliseconds.

**Worth making this connection unprompted** — it's presumably why this JD asks for event sourcing and
BDD in the same breath.

**A** and **C** are what produce slow, flaky Cucumber suites and give BDD a bad name. Use them only when the
scenario is genuinely *about* the UI or the API contract.

Step definitions should also **not contain business logic** (if a step computes an expected value, that
logic is now in two places and the test can agree with itself while both are wrong), and not share
mutable state carelessly — Cucumber's runner can be parallel.

→ `quiz/14` Q4
</details>

---

### Q5. What's the most common reason BDD fails in practice?

- **A.** Nobody outside the development team ever reads the feature files
- **B.** Cucumber is too slow
- **C.** Gherkin can't express complex logic
- **D.** Developers dislike writing tests

<details><summary>Answer</summary>

**A.** The *entire* justification for Gherkin's overhead is that business people engage with it. If
they don't, you've taken on a clumsy test-authoring syntax for no benefit — you'd be better off with
plain unit tests and readable names.

Then: imperative Gherkin (Q3); too many scenarios too slow (800 end-to-end tests taking 90 minutes,
so people stop running them, then stop trusting them, then ignore failures — **flakiness is fatal**);
testing at the wrong level; combinatorial `Scenario Outline` explosion; and written *after* the code,
which loses the whole "agree before building" value.

**The honest position:** *"I've seen BDD work and I've seen it become a 90-minute flaky suite nobody
trusts. The difference was whether business stakeholders actually read the scenarios, and whether the
steps talked to the domain or the UI. So I'd want to know who the audience is before committing to
Gherkin — the practice of agreeing examples first is valuable regardless."*

→ `quiz/14` Q5
</details>

---

### Q6. Which is the strongest indicator of a **bad** test?

- **A.** It has no comments
- **B.** It takes 50 ms to run
- **C.** It breaks when you refactor without changing behaviour
- **D.** It tests a private method indirectly

<details><summary>Answer</summary>

**C.** A test coupled to implementation rather than behaviour is a **liability** — a tax on every
future change. It's the single most important property of a good test, and it's what over-mocking
produces.

**Prefer tests that need no mocks.** A test mocking five collaborators and asserting on their
interactions tests your *wiring*, not your behaviour — and will pass while the system is broken. That's
why the pure `decide(state, command)` shape and constructor injection matter.

Other markers of bad tests: assertion-free; conditionals or loops in the test (if a test has an `if`,
it can pass without testing anything); non-deterministic (wall-clock `now()`, unseeded random, sleeps);
coupled by shared state; **flaky** (worse than no test — it trains the team to ignore red); and
**coverage-driven**, since 100% coverage with weak assertions is worse than 60% with strong ones.

Worth naming: **mutation testing** (PIT) measures test *effectiveness* rather than execution coverage.

→ `quiz/14` Q6
</details>

---

### Q7. How do you make settlement-date logic testable?

- **A.** Use `LocalDate.now()` and run tests only on weekdays
- **B.** Inject a `Clock` and a `BusinessCalendar`, so tests use `Clock.fixed(...)`
- **C.** Mock the static `LocalDate.now()` with a bytecode-manipulating library
- **D.** Add a `Thread.sleep` to align to a date boundary

<details><summary>Answer</summary>

**B.** `java.time.Clock` exists precisely for this. Never call `Instant.now()` or `LocalDate.now()`
directly in domain code.

Then the domain-specific points that show familiarity: **"now" is a business date, not a wall clock**
— post-trade systems run on a business date set by an end-of-day process, which may not equal the
calendar date; model it explicitly rather than deriving it from a clock. Plus **business-day calendars
per market and currency**, time zones and cut-offs, and the distinction between value date, trade date
and settlement date.

For async/scheduled behaviour, **ZIO's `TestClock` / Cats Effect's `TestControl`** give you virtual
time — `adjust(2.days)` and everything happens instantly and deterministically. Testing a T+2 cycle
becomes a fast unit test. **One of the strongest practical arguments for those libraries.**

And test the awkward dates deliberately: month/year boundaries, leap years, DST, a value date falling
on a holiday in one of two relevant markets.

→ `quiz/14` Q7
</details>

---

### Q8. What's the "ice-cream cone" anti-pattern?

- **A.** Too many unit tests and no integration tests
- **B.** One giant test class
- **C.** Tests written after the code
- **D.** Mostly manual and end-to-end tests with few unit tests — slow, flaky, and expensive

<details><summary>Answer</summary>

**D.** The inverted pyramid, and common in legacy enterprise systems.

The principle that matters more than the shape: **test at the lowest level that gives you confidence.**
Cost, speed and diagnostic clarity all improve going down; realism improves going up. And a failing
unit test points at a function, whereas a failing E2E test points at "something, somewhere" — that's
the real cost of top-heavy suites.

The other anti-shape is the **hourglass**: lots of unit and lots of E2E, nothing in between — so you
miss integration problems and then find them the slowest possible way.

For this architecture the pyramid is unusually favourable: the write side is a pure function, so the
**most valuable logic tests at the bottom** in milliseconds.

→ `quiz/14` Q8
</details>

---

### Q9. What's the most important principle in a CI/CD pipeline?

- **A.** Every stage must run in parallel
- **B.** The pipeline must complete in under five minutes
- **C.** **Build the artifact once and promote the same binary** through environments; only configuration differs
- **D.** Every commit must deploy to production

<details><summary>Answer</summary>

**C.** The artifact tested in UAT is bit-for-bit the one that reaches production. **Rebuilding per
environment means you deployed something you never tested.**

Then: **pipeline as code** (a `Jenkinsfile` in the repo, reviewed like any other code — not
click-configured jobs nobody can reproduce); **fail fast in cost order** (compile and lint before unit
before integration before acceptance); **immutable versioned artifacts** in Artifactory, traceable to a
commit; **quality gates that actually block** (a gate that only warns is documentation); **idempotent
deploys**; secrets never in the pipeline definition; and **rollback as part of the pipeline**, not an
improvised procedure.

Have one improvement story ready — "I cut the pipeline from 40 to 12 minutes by parallelising
integration tests and adding a Gradle build cache" is worth more than a tool list, and it also answers
"tell me about your impact."

→ `quiz/14` Q9
</details>

---

### Q10. You need to add tests to legacy code with none. Where do you start?

- **A.** Write a **characterisation test** capturing what it currently does, including behaviour you think is wrong
- **B.** Refactor it first to make it testable
- **C.** Write the ideal test for what it *should* do, then fix the code
- **D.** Rewrite the module from scratch with TDD

<details><summary>Answer</summary>

**A.** You're building a **safety net**, not asserting correctness. If the output is a mess, assert on
the mess. Only then find a **seam**, break dependencies with the smallest safe change (extract and
override, parameterise the constructor, extract an interface), and refactor behind the test.

- **B** is backwards — refactoring untested code is exactly what you're trying to avoid.
- **C** conflates two changes; you won't know which broke it.
- **D** is a big-bang rewrite; prefer the **strangler fig** and the boy-scout rule.

Worth adding: **approval/golden-master testing** is very effective here — capture output for many real
inputs and assert nothing changes. It locks in bugs, which is exactly what you want *temporarily*. And
prioritise by **risk × change frequency** from version-control history.

**The migration-relevant close:** for a Java→Scala port this matters double — characterisation tests
are what let you prove the ported behaviour matches. Get tests around the existing behaviour
**before** porting, or you're changing language and behaviour at once and can't tell which broke it.

→ `quiz/14` Q11
</details>

---

## Score yourself

- **8+** — good.
- **Q2 and Q5 are where a considered answer stands out** — most candidates recite the BDD sales pitch;
  knowing its failure modes is what demonstrates experience.
- **Q4's point** — that Given/When/Then maps exactly onto `decide(state, command)` — is the connection
  between this file and `mcq-quiz/13`. Make it unprompted.
- **Then go and write the feature file** in `quiz/14` Q3 by hand. It's the likely live exercise and MCQ
  can't practise it for you.
