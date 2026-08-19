# MCQ Quiz — rapid recall drill

Multiple-choice versions of the `quiz/` files, for fast self-testing.

**These are a complement to `quiz/`, not a replacement.** MCQ is good at one thing — checking
whether a fact is *retrievable* — and bad at the thing interviews actually test, which is
whether you can explain and apply it. Use these to find gaps quickly, then read the full answer
in `quiz/` for anything you got wrong or guessed.

---

## How to use

1. **Cover the answer.** Every answer sits in a `<details>` block — collapsed in most markdown
   previewers, and in a plain editor you'll need to scroll carefully.
2. **Commit to an answer before expanding.** Guessing then reading feels productive and teaches
   nothing.
3. **Note *why* you rejected the others.** The distractors are deliberately built from real
   misconceptions — several are the exact errors the source quizzes warn about. If you can't say
   why B is wrong, you don't know the answer yet.
4. **Anything you guessed counts as wrong.** Mark it and read the full `quiz/` entry.

Each answer ends with a `→ quiz/NN Qn` pointer to the full treatment.

---

## Files

| MCQ file | Source | Q | Notes |
|---|---|---|---|
| [`01-threading-fundamentals.md`](01-threading-fundamentals.md) | `quiz/01` | 20 | JMM, `volatile`, publication, deadlock |
| [`02-executors-and-pools.md`](02-executors-and-pools.md) | `quiz/02` | 16 | pool config, queues, `CompletableFuture` |
| [`03-virtual-threads.md`](03-virtual-threads.md) | `quiz/03` | 10 | virtual threads, pinning, structured concurrency |
| [`04-gc-fundamentals.md`](04-gc-fundamentals.md) | `quiz/04` | 20 | generations, TLAB, barriers, OOM taxonomy |
| [`05-gc-collectors-and-tuning.md`](05-gc-collectors-and-tuning.md) | `quiz/05` | 15 | collectors, flags, logs, containers |
| [`06-scala-concurrency.md`](06-scala-concurrency.md) | `quiz/06` | 18 | `Future`/EC, Pekko, Cats Effect/ZIO, allocation |
| [`07-spring-to-scala-migration.md`](07-spring-to-scala-migration.md) | `quiz/07` | 10 | what breaks in the port |
| [`09-collections-and-patterns.md`](09-collections-and-patterns.md) | `quiz/09` | 16 | collections internals, Builder, `hashCode` |
| [`10-spring-and-layering.md`](10-spring-and-layering.md) | `quiz/10` | 12 | annotations, `@Transactional`, AOP |
| [`11-sql-performance.md`](11-sql-performance.md) | `quiz/11` | 15 | indexes, sargability, `NOT IN`, N+1 |
| [`12-scala-language-and-fp.md`](12-scala-language-and-fp.md) | `quiz/12` | 18 | `lazy val`, monads, `Promise`, implicits, variance |
| [`13-event-sourcing-and-cqrs.md`](13-event-sourcing-and-cqrs.md) | `quiz/13` | 16 | **JD-critical.** ES/CQRS mechanics |
| [`14-atdd-bdd-tdd.md`](14-atdd-bdd-tdd.md) | `quiz/14` | 10 | TDD/ATDD/BDD, feature files, CI |
| [`15-post-trade-domain.md`](15-post-trade-domain.md) | `quiz/15` | 14 | lifecycle, T+1, clearing, settlement |
| [`16-java-core-and-oop.md`](16-java-core-and-oop.md) | `quiz/16` | 22 | `Integer` cache, `serialVersionUID`, erasure, PECS |
| [`17-java8-functional-and-streams.md`](17-java8-functional-and-streams.md) | `quiz/17` | 18 | lambdas, `peek`, `Optional`, collectors |
| [`18-concurrency-api-inventory.md`](18-concurrency-api-inventory.md) | `quiz/18` | 26 | `j.u.c.` API surface, `CompletableFuture` in depth |
| [`19-design-patterns.md`](19-design-patterns.md) | `quiz/19` | 14 | GoF set, Spring's patterns, Command → CQRS |

**290 questions.**

Correct answers are distributed roughly evenly across A/B/C/D (77/70/76/67), so you can't game it
by always picking the same letter. `tools/mcq_shuffle.py` re-randomises the positions anyway.

---

## What is deliberately NOT here

**`quiz/08` (behavioural & delivery) has no MCQ version, on purpose.** "Tell us about a system
you worked on" has no correct option — the whole point is *your* content, hit against the probe
areas. An MCQ version would be actively misleading. Same for the open design questions:
`quiz/13` Q16 (design a trade-processing system), `quiz/12` Q20 (sketch a domain model),
`quiz/14` Q3 (write a feature file), and `quiz/15` Q13 (what would you ask them). Those are
in the source files and need to be practised by **writing and speaking**, not selecting.

Also skipped: anything whose answer is "it depends, here's the trade-off." Forcing those into
one correct option would teach you a wrong instinct. `quiz/07` Q8 (`Future` vs Cats Effect/ZIO)
is the clearest example — the real answer is that it depends on the team.

---

## Honest limits of this format

- **Recognition ≠ recall ≠ explanation.** Passing an MCQ means you can pick the right answer
  from four; an interviewer asks you to generate it from nothing and then defend it. These are a
  gap-finder, not a readiness test.
- **Four options leaks information.** Sometimes the shape of the options tells you the answer.
- **The interview is spoken.** After a clean run through a file, close it and say three of the
  answers out loud in full sentences. That's the actual skill —
  `cheatsheets/answer-scripts.md` is built for that.

Suggested sequence: MCQ to find gaps → `quiz/` for the gaps → `problems/` to run the code →
`cheatsheets/answer-scripts.md` out loud.
