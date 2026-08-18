# Quiz 08 — Screening, Behavioral & Delivery Process

**These need *your* content, not mine.** The other quizzes have right answers; these have
*structures* you fill with real examples. An interviewer can tell within one sentence
whether you're describing something you actually built.

You have an advantage here: these questions came out of an interviewer's bank, so **you
already know the probe areas**. Those probes are what the interviewer is scoring. Prepare
material that hits them without being asked.

**Do this before the interview:** write out answers to Q2, Q3 and Q4 in full, once, by
hand. Not to memorise — to find the gaps. You'll discover you can't remember your own
release process in detail, and that's exactly what gets probed.

**Part 1 (Q1–Q7)** is the generic screening set; **Part 2 (Q8–Q12)** is BofA-specific — why
BofA, market trends, mentoring, conflict, and the HireVue format.

Target: 45 min of writing, not reading.

---

### 1. What was your motivation for leaving your existing workplace?

<details><summary>How to handle it</summary>

**What's being screened:** not your reason — your *judgement and professionalism*. They're
checking whether you'll badmouth an employer, whether you leave for push or pull reasons,
and whether what you want matches what this role is.

**The shape that works:** pull, not push. Forward-looking, specific, and connected to *this*
role.

> "I've spent [N] years on [X] and I'm looking for [specific thing this role has].
> [One concrete example of why that matters to me.] What drew me to this one specifically
> is [the migration / the scale / the domain]."

**Do:**
- Be **specific about what you're moving toward**. "I want to work on a large-scale JVM
  migration" is credible; "new challenges" is noise.
- **Name something real about their work.** For your case: the Java→Scala migration is a
  genuinely interesting engineering problem, and saying *why* it's interesting to you
  demonstrates you thought about the role rather than the salary band.
- Keep it to **30–45 seconds**. It's a screening question; a long answer signals baggage.
- If there's an obvious external factor (restructure, team dissolved, contract ended,
  relocation), **state it plainly and briefly**. Unexplained gaps or vagueness invite more
  digging than the honest reason would.

**Don't:**
- Criticise individuals, your manager, or the codebase. Even fair criticism reads as a risk.
  "The codebase was a mess" tells them how you'll describe *their* codebase in two years.
- Lead with compensation. If it's a factor, it's the *second* thing you say, not the first.
- Say "I was bored" without saying what you want instead.
- Over-explain. One reason, delivered once. Don't stack three.

**If pushed** ("was there nothing keeping you?") — that's a probe for badmouthing. Give
credit genuinely, then return to the pull: "I learned a lot there and I'd work with those
people again; it's just that the work I want to do next isn't on their roadmap."

**Your prep:** write one sentence naming what you want, one naming why this role has it.
That's the whole answer.
</details>

---

### 2. Tell us about a system you worked on — purpose, key components, technology stack.

> **Probe areas: deliverables and upstream/downstream dependencies · redundancy and
> failover · protocols used · architectural patterns**

<details><summary>The structure to use</summary>

This is the **highest-value question in the whole interview** and the one most people
answer badly — they list technologies instead of describing a system. The probe areas tell
you they want *systems thinking*: not "what did you use" but "what happens when it breaks".

**A 4-minute skeleton. Hit these in order:**

**1. Purpose and scale, in two sentences (~20s).** Business function first, then numbers.
> "It was the [X] service — it [did what, for whom]. Roughly [N] requests/sec at peak,
> [N] TB of data, [N] instances, p99 of [N] ms, and it fed [the thing that mattered]."

**Numbers are the credibility signal.** Throughput, latency, data volume, instance count,
team size. If you don't remember exact figures, say "on the order of" — an estimate beats
a shrug, and vagueness on your own system is the worst possible answer.

**2. Where it sat — upstream and downstream (~45s).** *Probe area: they will ask this.*
> "Upstream we consumed [A] over [protocol] and [B] via [queue]. Downstream, [C] and [D]
> depended on us — [C] synchronously, so our latency was their latency, and [D] via
> [async], so it tolerated us being down for minutes."

Say **who broke if you broke**. That's the answer that shows you understood your blast
radius rather than just your repo.

**3. Components and stack (~45s).** Group by *responsibility*, not by technology.
> "Java 17, Spring Boot, Postgres, Kafka, deployed on Kubernetes. Four pieces: an API
> tier doing [X], a worker tier consuming [Y], a Postgres primary with read replicas, and
> Redis for [specific purpose]."

Name the stack once and move on. **Do not** recite a dependency list — that's the tell of
someone who used a system without understanding it.

**4. Redundancy and failover (~45s).** *Probe area, and the one most candidates fluff.*
Cover:
- **Instance level:** N replicas behind a load balancer, health checks (liveness vs
  readiness), rolling deploys.
- **Data:** primary/replica, sync or async replication, RPO/RTO if you know them, backup
  and *restore-tested* (say if it wasn't — honesty scores here).
- **Region/DC:** active-active, active-passive, or single — and what the failover
  procedure was, whether it was ever *tested*, and how long it took.
- **Graceful degradation:** circuit breakers, fallbacks, cached/stale responses, what you
  shed first under load.
- **What was NOT redundant.** Every real system has a single point of failure. Naming
  yours — "the Postgres primary was a single point of failure; failover was manual and
  took about 10 minutes" — is a *strength*, not a weakness. It's what distinguishes
  someone who operated a system from someone who read its README.

**5. Protocols (~20s).** *Probe area.* Be precise: REST/HTTP with JSON, gRPC/protobuf,
Kafka, JMS/MQ, WebSocket, JDBC, TLS/mTLS, FIX if it's Citi-adjacent finance work.
Say **why** for at least one: "gRPC internally for the latency and the schema, REST at the
edge because external consumers wanted it."

**6. Architectural patterns (~30s).** *Probe area.* Name them explicitly, because the
interviewer is listening for the vocabulary: layered/hexagonal, CQRS, event sourcing,
event-driven, saga, request-response vs pub/sub, sidecar, strangler fig, bulkhead,
circuit breaker, idempotent consumer, outbox, backpressure, cache-aside.
Then **one trade-off you actually lived with**:
> "We used [pattern] — it gave us [benefit], and the cost was [real cost]. In hindsight
> I'd [do X differently] because [what we learned]."

That last sentence is what gets you a strong rating. Anyone can name a pattern; describing
its cost proves you shipped it.

**Also prepare:**
- **Your** specific contribution, unprompted. "I owned [component]; I built [thing]."
  Interviewers get suspicious of "we" with no "I".
- **One incident**: what broke, how you found it, what you changed so it couldn't recur.
- **How it was observed**: metrics, logs, traces, alerts, dashboards, SLOs.

**Tie it to this role:** if you can say "that's why I'm interested in the Scala migration —
we had [analogous problem]", you've turned a history question into a fit argument.
</details>

---

### 3. How was work delivered in your current workplace?

> **Probe areas: methodologies (backlogs, sprints, Kanban, triaging) · testing (unit, QA,
> UAT, pre-prod, automated regressions) · sign-offs · release documentation ·
> rollback/back-out plans**

<details><summary>The structure to use</summary>

This is a **process maturity** check, and it's especially heavy at banks — Citi's probe
list is essentially an audit checklist. They want to know you can operate inside controls
without needing them explained, and that you've done a real release rather than pushing to
`main`.

**Walk a single change end to end.** That's far more convincing than describing the process
abstractly. "Let me take a change I actually shipped and walk it through."

**1. Intake and prioritisation.**
Backlog grooming, refinement/sizing (story points, t-shirts, or none), sprint cadence
(1/2/3-week) or Kanban with WIP limits, who prioritised, how **triaging** worked for
production issues vs planned work, severity/priority definitions, and how interrupts were
handled (a rotation? a support engineer? everyone?).

**2. Development.**
Branching model (trunk-based, GitFlow, short-lived feature branches), PR process and how
many reviewers, mandatory checks (build, tests, coverage threshold, static analysis,
SAST/dependency scanning), commit/ticket linkage for traceability, pair or mob work.

**3. Testing — name every tier you had.** *Heavily probed. Be specific.*
- **Unit** — framework, what you mocked, coverage expectation and whether it was enforced.
- **Integration** — Testcontainers, embedded DBs, WireMock/stubs for other teams.
- **Component/contract** — Pact or schema compatibility checks, which matters when you
  have upstream/downstream deps.
- **QA environment** — who tested, manual or automated, how long a cycle took.
- **UAT** — which *business* users signed off, and on what.
- **Pre-prod / staging** — production-like data? Anonymised? Same topology?
- **Automated regression suite** — size, runtime, when it ran (per PR? nightly?), and
  crucially **flakiness** and how you dealt with it.
- **Non-functional** — performance/load tests, soak tests, failover/DR tests, security
  testing, penetration tests.

If a tier didn't exist, **say so and say what you did instead.** Inventing a mature process
is transparent and fatal.

**4. Sign-offs and change control.** *Very heavily probed in banking.*
Who approved: tech lead, QA lead, product owner, business/UAT, security, DBA?
Was there a **CAB** (change advisory board) or a change ticket? Standard vs emergency
change paths? **Segregation of duties** — could the author deploy their own change?
Release windows, freeze periods (year-end, month-end close), and blackout dates.

**5. Release documentation.** *Probe area.*
Change ticket contents, release notes, runbook, deployment steps, configuration/feature-flag
changes, **DB migration scripts** (and whether they were backward-compatible), validation
steps for after the deploy, and comms to dependent teams.

**6. Deployment mechanics.**
CI/CD tooling, artifact promotion (build once, promote the same binary — say this, it's a
maturity signal), deployment strategy (rolling, blue-green, canary), feature flags for
decoupling deploy from release, and how long a deploy took.

**7. Rollback / back-out plan.** *Probe area, and the one that separates candidates.*
- What the documented back-out was, and **whether it was ever executed** (a real rollback
  story is worth a lot here).
- **Time to roll back**, and who could authorise it.
- The hard part: **database migrations are not trivially reversible.** If you can explain
  expand/contract — add the new column, dual-write, backfill, cut over reads, only then
  drop the old — you'll stand out, because that's the thing most people wave at.
- Forward-fix vs roll back: when did you choose which?
- Post-deploy validation and monitoring: what told you to roll back, and how fast?

**8. Afterwards.** Post-incident reviews, blameless postmortems, action-item tracking,
retros and whether anything actually changed as a result.

**Have ready:** one thing about the process that **worked well**, one that **didn't and
what you changed** (or tried to). That converts a process recital into evidence of
ownership. If you improved a pipeline, cut a test suite's runtime, or introduced a check —
that's your answer to "tell me about your impact" too.
</details>

---

### 4. What was the "Definition of Done" for work you completed?

> **Probe areas: an example · what testing was performed · BDD or acceptance criteria**

<details><summary>The structure to use</summary>

**Answer with a checklist, then a worked example.** The checklist proves you know what DoD
means; the example proves you lived it.

**The generic DoD** (adapt to what was actually true — do not claim all of these):
- Acceptance criteria met and demonstrated
- Code reviewed and approved by [N] reviewers
- Unit tests written and passing; coverage not decreased
- Integration tests passing; automated regression suite green
- Static analysis / security scan clean, no new critical findings
- Documentation updated: API spec, runbook, README, architecture decision record
- **Observability added** — metrics, logs, alerts for the new path. This one is a strong
  differentiator; most candidates omit it, and "done" without monitoring isn't done.
- Feature-flagged if risky
- Deployed to [pre-prod], validated
- UAT sign-off from [named role]
- Rollback plan documented
- Ticket updated, release notes drafted
- No known defects above [severity], or accepted and logged

**Then the worked example** — the interviewer explicitly wants one. Structure it:
> "For example, [specific story: 'add a settlement-status field to the positions API'].
> The acceptance criteria were [2–3 concrete criteria]. Done meant: [what testing],
> [who reviewed], [what docs], [who signed off], and it was behind a flag until
> [condition]. It shipped on [cadence] and we validated with [specific check]."

Pick something **small and concrete**. A tiny story described precisely beats an epic
described vaguely.

**On BDD / acceptance criteria** — the probe:
- **Acceptance criteria** are the conditions a story must satisfy, written *before* work
  starts, ideally by or with the product owner. Testable, not aspirational.
- **BDD** expresses them as behaviour in Given/When/Then:
  > Given a position with status `UNSETTLED`
  > When the settlement confirmation is received
  > Then the position status is `SETTLED` and a `PositionUpdated` event is published
- Tooling if you used it: **Cucumber/Gherkin**, JBehave, Spock, or Scala's `FeatureSpec`.
- **Be honest about how far you took it.** Very few teams do full BDD with executable
  Gherkin. Many write Given/When/Then acceptance criteria in the ticket and ordinary unit
  tests underneath — that's a perfectly respectable answer and much more credible than
  claiming a Cucumber suite you didn't have.
- The valuable insight to volunteer: **the point of BDD is the conversation**, getting
  business, QA and dev to agree on behaviour *before* code. The tooling is optional; the
  shared understanding is the deliverable. Say that and you sound like someone who's seen
  it work and seen it fail.

**Likely follow-up: "who decides when it's done?"** — the answer is *not* "the developer".
It's the acceptance criteria plus whoever owns them, usually the PO, with QA verifying.

**A good closing line:** "The DoD only mattered because it was enforced — it was in the PR
template and the pipeline blocked on most of it. When it was just a wiki page, it got
skipped." That's the observation of someone who's worked in both.
</details>

---

### 5. Tell me about a production incident you were involved in.

<details><summary>How to handle it</summary>

Not in the Citi list, but it follows directly from Q2's redundancy probe and Q3's rollback
probe. Prepare one. It's the single most useful story to have loaded.

**Use STAR, weighted toward the last two letters:**
- **Situation** (15s) — what broke, what the user impact was. Lead with impact, not cause.
- **Task** (10s) — your role. Were you on call, or did you pick it up?
- **Action** (60s) — how you *diagnosed* it, in order. This is the substance. What did you
  look at first, what did you rule out, what was the actual signal that cracked it?
- **Result** (30s) — resolution time, and **what you changed so it couldn't recur** —
  the fix, the alert you added, the test, the runbook entry.

**What's being scored:** diagnostic method under pressure, and whether you closed the loop.

**Make it credible:**
- **Name your dead ends.** "I spent 20 minutes convinced it was the database before I
  looked at the GC log" is the most credible sentence in an incident story.
- Blameless. "A config change went out without the corresponding flag" not "X broke prod".
- Give the timeline: detected at, mitigated at, resolved at.
- Say how it was **detected** — an alert (good) or a customer complaint (be honest, then
  say what alert you added).

**You have excellent raw material from this prep.** Any of these makes a strong story if
you've genuinely hit it: a thread pool exhausted by a slow downstream; a `ThreadLocal`
leaking context between requests; an unbounded queue turning overload into an OOM; a
`@Transactional` method that silently wasn't; a full GC from an unbounded cache. Pick the
one you've *actually* seen and tell that one.
</details>

---

### 6. Tell me about a technical disagreement and how it was resolved.

<details><summary>How to handle it</summary>

**What's being scored:** whether you can disagree without being difficult, and whether you
can be *wrong* gracefully. They are explicitly looking for evidence you're not a liability
in a code review.

**The shape:**
1. The disagreement, stated **fairly from both sides**. If you can't articulate the other
   position convincingly, you didn't understand it, and the interviewer will notice.
2. How you moved it from opinion to evidence — a spike, a benchmark, a prototype, a
   third opinion, writing the trade-offs down.
3. The outcome, **including if you lost**.
4. What you'd do differently.

**The strongest version of this answer is one where you were wrong**, or where you
disagreed, were overruled, committed anyway, and it worked out. That demonstrates
"disagree and commit" without you having to use the phrase.

**Avoid:** stories where you were right and everyone else was foolish. Even if true, it
reads as someone who can't be moved.

**Relevant to your situation:** the `Future` vs Cats Effect/ZIO decision (quiz 07 Q8) is
exactly this kind of disagreement — strong technical arguments on both sides where the
right answer depends on the team, not just the technology. If you've had a version of that
argument, it's a good one to tell, because the conclusion is "it depends on the team" and
that's a mature place to land.
</details>

---

### 7. What are you looking for in your next role, and where do you want to be in a few years?

<details><summary>How to handle it</summary>

**What's being screened:** retention risk and whether your trajectory fits their roadmap.

Be honest but **role-shaped**. If you want to go deep technically, say so — most teams need
that more than they need another aspiring manager. If you want breadth or leadership, say
that. Vagueness reads as not having thought about it; a plan that has nothing to do with
their work reads as a flight risk.

**Connect it to the actual work:** "a large migration is exactly the kind of thing I want
more of — it forces you to understand both systems properly, and the concurrency and
memory-model work underneath it is where I want to get stronger." That's specific,
verifiable against the rest of your interview, and true if you've done this prep.

**Also prepare the inverse**, since it's usually paired: **"what are your questions for
us?"** Have three, and make them ones whose answers change your work. There are four good
ones at the end of `cheatsheets/answer-scripts.md` Q15.
</details>

---

# Part 2 — BofA-specific

The process (recruiter screen → HireVue/online assessment → 1–2 technical rounds →
hiring-manager/behavioural final) puts real weight on the last round. These are the
BofA-flavoured questions.

⚠️ **The process description is anecdotal.** It comes from Glassdoor-style candidate reports, and
a dedicated verification pass found **no corroborating evidence** — including for the four-stage
shape, the two-coding-problem HireVue round, and the on-camera complexity narration. It is
inherently unverifiable from public sources. **Prepare for it as a plausible shape, not a
schedule** — and just ask your recruiter what the stages actually are. They'll tell you.

---

### 8. Why Bank of America, and why this division?

<details><summary>How to handle it</summary>

**Two parts, and most candidates only answer the first.** "Why us" needs to be about *them*;
"why this division" needs to be about *the work*.

**Why the firm** — pick one or two specific, verifiable things and connect them to you:
- **Scale and the nature of the problem.** Global Markets post-trade processes enormous
  volumes under hard regulatory deadlines. If you're motivated by high-volume, correctness-
  critical systems, say so — it's true and it's checkable against the rest of your interview.
- **Investment in strategic platform work.** The JD is about building strategic systems, not
  maintaining legacy. That's a real differentiator from a lot of bank tech roles and worth
  naming.
- **Longevity of the engineering.** These systems run for decades, which changes how you build
  — and is a genuine draw if you like designing for maintainability rather than shipping fast
  and rewriting.

**Why this division** — be specific to post-trade and the JD:
> "The combination in this JD is unusual: event-driven and event-sourced architecture, Scala
> and functional programming, and a domain where the audit trail and as-of correctness are
> regulatory requirements rather than nice-to-haves. Those normally pull in different
> directions — here they reinforce each other, and that's the kind of problem I want to work
> on."

That sentence works because it's true, specific, and demonstrates you read the JD properly.

**On "Responsible Growth"** — BofA's stated strategy, with four tenets usually summarised as:
grow, but *no excuses*; grow **client-focused**; grow **within risk appetite**; and grow in a
**sustainable** way. Don't recite it as a slogan — that reads badly. Connect one tenet to
engineering practice:
> "The 'within our risk appetite' part maps onto engineering pretty directly for me. In
> post-trade, risk appetite is expressed as controls — segregation of duties in the release
> process, tested rollback plans, reconciliation as a first-class function, and an audit trail
> you can actually defend. Those are things I'd argue for anyway; it helps that they're the
> stated culture rather than something you fight for."

**What to avoid:** generic praise ("great brand", "market leader"), anything that would apply
to any bank, and — critically — implying you'd take any offer. If you can't say something
specific, ask them what they'd say instead; that's a better move than filler.
</details>

---

### 9. What are the biggest issues or trends facing Bank of America or global markets today?

<details><summary>How to handle it</summary>

This is a real BofA screening question and it's a **preparation check** — they want to know
whether you read about the industry or just the tech.

**For a post-trade role, the highest-value answer is T+1** — see **`quiz/15` Q14** for the full
version with seven other options. The short form:

> "For this team specifically, T+1 settlement. The US, Canada and Mexico moved in May 2024 and
> the EU and UK are targeted for 2027. It removes most of the slack that manual
> exception handling relied on — affirmation has to be same-day, funding and FX get
> compressed, and the overnight batch window largely disappears. The engineering consequence
> is a push from batch toward real-time event-driven processing with much better automated
> exception management, which is presumably part of why this role is framed the way it is."

**Then have one or two broader ones ready**, said briefly:
- **Rate environment and its effect on net interest income** — the dominant driver of bank
  earnings over the last few years, and directly relevant to a bank's financial performance.
- **Regulatory capital** — Basel III endgame and its calibration has been the big prudential
  debate.
- **Operational resilience** — DORA in the EU and equivalent regimes raising the bar on
  recovery testing and third-party risk. Very relevant to how you're allowed to build.
- **AI adoption in a regulated context** — realistic applications (break prediction, fail
  prediction, developer productivity) versus the constraints (model governance, data
  residency, explainability).

**How to deliver it:** pick **two**, not seven. Say why each matters *to this team's work*.
And **be honest about your depth** — "I follow this from the engineering side rather than as a
markets specialist" is a fine framing and stops you being drawn into a discussion you can't
sustain. Overclaiming here is worse than a modest, well-connected answer.
</details>

---

### 10. Tell me about a time you coached or mentored someone.

<details><summary>How to handle it</summary>

**The JD explicitly requires "coach and mentor",** so this is near-certain and it's worth
preparing properly rather than improvising.

**What's being assessed:** whether you can raise the level of people around you, and whether
you understand that mentoring is *not* just doing the work for them.

**Structure — STAR, weighted to Action and Result:**
- **Situation** — who, and what the gap was. Be specific: "a graduate joining the team who was
  strong in Java but had never worked with an event-driven system."
- **Task** — your role. Formal mentor, tech lead, or informal.
- **Action** — **what you actually did**, and this is where most answers are too vague. Good
  specifics: pairing on a first real story rather than a toy task; reviewing PRs with
  explanations rather than just requested changes; giving them a piece of work with genuine
  ownership and being available rather than hovering; walking them through a production
  incident afterwards; recommending a specific book or talk; setting up a regular slot.
- **Result** — what *they* achieved, ideally something measurable or observable: "within three
  months they were running the on-call rotation and reviewing others' PRs."

**Details that make it credible:**
- **Say what you changed about your own approach.** "I started by explaining too much and it
  wasn't landing — so I switched to asking questions and letting them find it, which was
  slower but stuck." That shows reflection, not just activity.
- **Mention scaling it beyond one person** if you can: documentation, a brown-bag session, a
  code-review checklist, onboarding notes. That's mentoring with leverage, and it's what a
  senior role wants.
- **Coaching someone senior or a peer** is a strong variant — it demonstrates influence
  without authority.
- **Have a version where it was hard.** Someone resistant to feedback, or where you had to
  escalate. "It went great" is less interesting than "here's how I handled it not going great."

**For this role specifically:** a Java team moving to Scala and functional programming is
*exactly* a coaching problem. If you have any experience bringing people onto an unfamiliar
paradigm, that's the story to tell — and if you don't, the honest and useful version is:
> "I'd expect a lot of this role's mentoring to be helping Java engineers get productive in
> Scala without either writing Java-in-Scala or over-rotating into abstraction they don't need
> yet. From my own learning, the leverage is in a small set of house idioms and a clear line on
> what we don't use — not in teaching category theory."

That's a genuinely thoughtful answer and it signals judgement about the migration.
</details>

---

### 11. How do you handle constructive criticism, a conflicting opinion, or a teammate not delivering?

<details><summary>How to handle it</summary>

Three related questions; prepare one story for each. **See also Q6** for technical
disagreement.

**Receiving criticism:**
> The shape: hear it fully without defending, ask a clarifying question, separate the
> observation from the interpretation, act on it, and close the loop with the person.

The credibility comes from a **real example where the feedback was right and you changed
something**. "I was told my PRs were too large and hard to review — I'd been batching a week
of work. I started splitting by behaviour and the review turnaround dropped from days to
hours." Small, concrete, and it shows you can be corrected.

If you have an example where the feedback felt **unfair**, that's a good second story:
you looked for the kernel of truth, acted on that part, and discussed the rest. That's more
mature than either capitulating or dismissing it.

**Conflicting opinions:** move it from opinion to evidence — a spike, a benchmark, a
prototype, or writing the trade-offs down so they can be compared. Then **disagree and
commit**: if it goes the other way, you support the decision properly. The strongest version
of this story is one where **you were wrong**, or where you were overruled and it worked out.
(quiz 06/07's `Future` vs Cats Effect/ZIO decision is exactly this shape — strong arguments
both ways, and the right answer depends on the team.)

**A teammate not delivering** — the hardest one, and the one people answer badly by going
straight to escalation.
1. **Assume a cause you don't know about.** Blocked, unclear requirements, out of their depth
   and not saying, or something outside work. Most under-delivery is one of those, not
   indifference.
2. **Talk to them directly and privately first.** Ask, don't accuse: "I noticed X is taking
   longer than we expected — what's in the way?"
3. **Offer concrete help** — pair on it, break the work down smaller, clarify the requirement,
   or take a piece of it.
4. **Make the impact visible without blame** — in stand-up, surface the *risk to the
   deliverable*, not the person.
5. **Escalate only if it continues**, and frame it as a delivery risk needing a decision, with
   what you already tried. Never blindside them — tell them you're raising it.

**What's being scored:** whether you go to the person before the manager, whether you assume
good faith, and whether you can be direct without being adversarial. **Do not** tell a story
where the resolution is "I did their work for them" (that's a failure to address it) or "I told
my manager" as step one.
</details>

---

### 12. HireVue / recorded round — what's different?

<details><summary>How to handle it</summary>

Per your research the online assessment includes **two coding problems plus recorded
behavioural answers, including narrating your code's time complexity on video**. That format
has its own failure modes.

**What's different, and it matters:**
- **No feedback and no prompting.** Nobody nudges you toward the hint or tells you you've
  misread the question. Everything you'd normally get by asking, you must supply by **stating
  assumptions out loud**.
- **A hard time box**, often with limited or no re-takes.
- **Silence reads as being stuck.** In person, thinking quietly is fine. On camera it's dead air.

**The four-beat script for the coding narration** (the full version, with the complexity
formula and the losing-marks list, is at the bottom of
[`problems/java/P12DsaPractice.java`](../problems/java/P12DsaPractice.java) — run it and read
the notes):
1. **Restate and state your assumptions** — since you can't ask.
2. **Name the approach and its complexity before coding**, including the brute force you're
   rejecting and why.
3. **Code it, narrating the invariant**, not every keystroke.
4. **State complexity with the reason, then edge cases**:
   *"O(n) time because each character is visited at most twice. O(k) space because the map
   holds one entry per distinct character."*

**Naming what the space actually holds** is what distinguishes an analysis from a recitation.

**Practical setup, which people neglect and it costs them:**
- Test the platform, camera, mic, and screen-share **before** the window.
- Wired connection if possible. Quiet room, door closed, phone off.
- Camera at eye level, light in front of you not behind.
- Have water, and a notepad for scribbling — using paper on camera is fine and looks
  considered.
- **Do a full practice run recording yourself** solving one of P12's problems out loud, then
  watch it back. It's uncomfortable and it's the single highest-value thing you can do for this
  round — you'll find you say "um" while thinking, or code silently for ninety seconds.

**For the recorded behavioural answers:** 60–90 seconds each, STAR, and **lead with the
result** so the important part lands before the timer does. Have your Q2 (system), Q5
(incident), Q10 (mentoring) and Q11 (conflict) stories ready in short form — the recorded
versions need to be tighter than the live ones.
</details>

---

## What to actually do with this file

1. **Write out Q2 in full.** Four minutes of speech, hitting all five probe areas. You will
   find you don't remember your failover procedure. Fix that now, not live.
2. **Write out Q3's checklist** for your real process, marking honestly what didn't exist.
3. **Pick one story** for Q5 and one for Q6, and write the Action section of each.
4. **Say Q1 out loud twice.** It should take 40 seconds and not wander.

The technical quizzes reward recall. **These reward preparation, and it shows immediately
which you did.**
