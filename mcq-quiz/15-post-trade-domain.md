# MCQ 15 — Securities Post-Trade Domain

14 questions. Source: [`quiz/15`](../quiz/15-post-trade-domain.md).

**Verification status:** a dedicated fact-check pass (Aug 2026) **confirmed the ISO 15022/20022
message mapping** but **could not corroborate any settlement-cycle or regulatory date** — including
the EU/UK T+1 target and the CSDR buy-in status, flagged as the two most volatile facts in the pack.
Non-corroboration is not disproof (verifier agents hit rate limits), but **check anything
date-sensitive against a primary source before stating it.** This is also **domain knowledge
inferred from the JD, not verified against BofA's systems**, and no GMT Post Trade interview reports were found. Treat it as a
vocabulary primer. Some conventions (notably value date vs settlement date) vary by firm — **"I'd want
to check how you do that here" is always a safe move**, and confidently wrong domain claims are worse
than admitted gaps.

**Note:** `quiz/15` Q13 (what would you ask them) stays in the source file — it's a list to use, not a
question to answer.

---

### Q1. Which stages make up "post-trade"?

- **A.** Research, pricing and order creation
- **B.** Order routing and execution on a venue
- **C.** Capture, confirmation, clearing, settlement and asset servicing — everything after the price is agreed
- **D.** Only clearing and settlement

<details><summary>Answer</summary>

**C.** Pre-trade is research/pricing/risk; execution is the order matching on a venue (usually FIX);
**post-trade is everything after that** — the plumbing that turns an execution into actual ownership
and cash movement, plus reconciliation and regulatory reporting throughout.

The framing that shows you understand *why* it's hard: it's **not a low-latency problem, it's a
correctness, auditability and deadline problem**. Front office is microseconds; post-trade is
throughput plus correctness plus finishing before the cut-off. And **most of the complexity is in the
exceptions** — fails, partials, amendments, cancellations, breaks.

→ `quiz/15` Q1
</details>

---

### Q2. What is T+1, and where does it stand?

- **A.** Settlement one business day after trade date — adopted by the US, Canada and Mexico in May 2024, with the EU and UK targeting 2027
- **B.** Trade and settlement on the same day, globally mandated
- **C.** A limit of one trade per counterparty per day
- **D.** A regulatory reporting deadline only

<details><summary>Answer</summary>

**A.** The UK and EU are currently **T+2**. ⚠️ **The October 2027 date is unverified** — the
fact-check pass could not corroborate it, and both the target and its legal status have moved
before. If unsure on the day, say **"targeted for 2027"** rather than naming a month.

**Why it matters, and this is the answer to "what trends face this business?":** compressing the cycle
**removes the slack that manual exception handling relied on**. Affirmation and confirmation must be
same-day, funding and FX get squeezed, and the overnight batch window largely disappears. So it pushes
firms from **batch toward real-time, event-driven** processing with automated exception management —
which is presumably part of why this role is framed around strategic event-driven systems.

That connects the industry to the architecture in one move.

→ `quiz/15` Q2, Q14
</details>

---

### Q3. Trade date, value date, settlement date — which is the *contractual due date* for the exchange?

- **A.** Trade date
- **B.** Record date
- **C.** Actual settlement date
- **D.** Value date

<details><summary>Answer</summary>

**D.** Trade date is when the economics were agreed; **value date** is when the exchange is *due*;
**actual settlement date** is when it really happened, which may be later if it failed.

Be aware "settlement date" is used loosely for both the due date and the actual date — **asking which
convention a team uses is better than being confidently wrong.**

The engineering consequence: value date calculation needs a **business-day calendar per market and per
currency**, holidays differ, and cross-border trades involve two calendars. A real source of defects,
and why injecting the clock *and* the calendar matters.

→ `quiz/15` Q2
</details>

---

### Q4. What does a CCP do?

- **A.** Executes trades on behalf of clients
- **B.** Novates the trade so it becomes counterparty to both sides — mutualising counterparty risk and netting obligations
- **C.** Maintains the definitive record of securities ownership
- **D.** Provides market data

<details><summary>Answer</summary>

**B.** **Novation**: the bilateral contract A↔**B** is replaced by A↔CCP and CCP↔B. Examples: LCH, ICE
Clear, Eurex Clearing, DTCC's NSCC.

Two benefits: **counterparty risk mutualisation** (you face the CCP, protected by its default
waterfall) and — operationally more important — **multilateral netting**, which can reduce settlement
volumes by an order of magnitude, meaning less settlement risk, less funding, and fewer fails.

- **C** is a **CSD** (Euroclear, Clearstream, DTC, CREST) — that's where settlement actually happens, on
  its books.

Margin vocabulary: **initial margin** (potential future exposure, VaR/SPAN-style), **variation margin**
(daily or intraday mark-to-market), and **margin calls** — time-critical, since missing one puts you in
default.

→ `quiz/15` Q3
</details>

---

### Q5. What is DvP, and what risk does it eliminate?

- **A.** Delivery versus Payment — the two legs are conditional on each other, eliminating principal risk
- **B.** Daily Value Position — a risk metric
- **C.** Deferred Value Payment — settling cash later than securities
- **D.** Dual Verification Protocol — a two-person authorisation control

<details><summary>Answer</summary>

**A.** Securities are delivered **if and only if** cash is paid, simultaneously — so you can't deliver
and go unpaid. The FX equivalent is **PvP**, which is what CLS provides.

The contrast: **FoP (Free of Payment)** has no corresponding cash leg (moving stock between your own
accounts, or a collateral pledge) and is riskier precisely because there's no DvP protection.

→ `quiz/15` Q4
</details>

---

### Q6. What's the most common cause of settlement fails that engineering can address?

- **A.** Market volatility
- **B.** Insufficient CCP margin
- **C.** Wrong or stale **SSIs** (standing settlement instructions), and mismatched instructions between the two sides
- **D.** Time zone differences

<details><summary>Answer</summary>

**C.** SSIs say where a counterparty wants its securities and cash delivered, and stale ones are a
leading cause of fails. Other causes: the seller doesn't have the securities (a short/inventory fail),
insufficient cash, or a late instruction past the cut-off.

Consequences: **buy-in** risk, interest/compensation claims, and under **CSDR Settlement Discipline**
in the EU, **mandatory cash penalties accruing daily**.

**The highest-value automation is pre-matching** — proactively confirming the counterparty's
instruction matches yours *before* the value date, so you fix breaks before they become fails. That's
exactly where an event-driven system with real-time status genuinely helps.

→ `quiz/15` Q4
</details>

---

### Q7. Which kind of netting is a *legal* concept rather than an operational one?

- **A.** Payment netting
- **B.** **Close-out** netting
- **C.** Settlement netting
- **D.** Multilateral netting

<details><summary>Answer</summary>

**B.** Close-out netting reduces all obligations under a master agreement (ISDA/GMRA) to a single net
amount **on default**. It's what makes derivatives exposure manageable — without enforceable close-out
netting, gross exposures would be enormous.

**A**, **C** and **D** are operational: netting the cash and securities actually to be delivered on a value date.

**Knowing the distinction between settlement netting and close-out netting is a genuine signal of
domain literacy.**

The technical angle: netting is a **batch-like aggregation over a set of trades at a point in time**,
which sits awkwardly with pure per-trade event streams. **Cut-off times matter enormously** — what's
included depends on *when* you run — so the run needs a well-defined **as-of** point, which an ordered
event log gives you cleanly and a mutable-state system gives you badly.

→ `quiz/15` Q5
</details>

---

### Q8. Why are corporate actions notoriously hard?

- **A.** They're rare, so nobody has good tooling
- **B.** They require manual approval by regulators
- **C.** They only affect derivatives
- **D.** Announcement data arrives from several sources in different formats that **disagree**, and entitlement depends on what has **settled** as of a historical record date

<details><summary>Answer</summary>

**D.** The data problem is genuinely one of the messiest in the industry — you need scrubbing and
**golden-copy** logic to reconcile issuer, exchange, custodian and vendor feeds, and someone must
adjudicate.

Then the dates: entitlement depends on your position on the **record date**, which depends on what has
**settled**, not what has **traded** — so a trade pending settlement across an ex-date creates a
**market claim**. Plus elective deadlines (the custodian's is earlier than the market's), retroactive
adjustment of historical positions and prices, and withholding tax.

**The architectural connection:** this is the clearest case for an event log, because entitlement
requires **temporal state reconstruction** — exactly what event sourcing gives you and what a
current-state-only model cannot. And an adjustment is a **new event** correcting the record, not an
`UPDATE` that destroys it.

→ `quiz/15` Q6
</details>

---

### Q9. What is AMPS's "State of the World" (SOW)?

- **A.** A global market-data feed
- **B.** A disaster-recovery replication mode
- **C.** A server-side **last-value-per-key cache**, queryable with SQL-92-style syntax, so a subscriber gets current state plus a live stream in one operation
- **D.** A distributed transaction log for replay

<details><summary>Answer</summary>

**C.** Along with **server-side content filtering** — subscribers register a filter and AMPS evaluates
it, reducing network and client CPU versus filtering client-side.

**If you haven't used AMPS, say so and then be useful:** conceptually it maps onto the same problem you
solve with Kafka — distributing state changes to many consumers with different interests. Where you'd
still want **Kafka is durable, replayable history**, which is what an event store needs. Where AMPS
looks stronger is **low-latency fan-out and last-value queries**.

The observation that connects it to the architecture: **the SOW is essentially a materialised
projection maintained by the messaging layer**, so it fits the CQRS read-side role directly. So they're
complementary — a durable store as system of record, AMPS as distribution and current-state query.

Then ask: *"Are you using AMPS as distribution over a durable store, or is the SOW acting as the read
model?"*

→ `quiz/15` Q9
</details>

---

### Q10. Which regulation imposes **mandatory cash penalties for settlement fails**?

- **A.** CSDR
- **B.** EMIR
- **C.** MiFID II
- **D.** BCBS 239

<details><summary>Answer</summary>

**A.** CSDR Settlement Discipline (EU), plus a buy-in regime. The engineering consequence: you must
**track fails precisely**, calculate penalties, and reconcile them with the CSD — so settlement
performance becomes a measured KPI.

The others: **MiFID II/MiFIR** — transaction reporting, best execution, and **clock synchronisation**
requirements (so timestamps are *regulated data*, not incidental metadata). **EMIR** — derivative
reporting and the clearing obligation. **BCBS 239** — risk data aggregation principles, i.e. **data
lineage**, which is very architecture-relevant.

The recurring engineering themes across all of them: immutable audit trails, accurate synchronised
timestamps, demonstrable lineage, hard reporting deadlines, and **5–7 year retention** — which makes
schema evolution a decade-scale commitment.

**Position it honestly:** *"I wouldn't claim regulatory expertise — that's what compliance is for. What
I'd bring is understanding the engineering requirements they generate."*

→ `quiz/15` Q10
</details>

---

### Q11. Which identifier is **market-specific**, so the same instrument has different values in different markets?

- **A.** ISIN
- **B.** CUSIP
- **C.** LEI
- **D.** SEDOL

<details><summary>Answer</summary>

**D.** SEDOL (7 characters, UK/Ireland) is market-specific — exactly the kind of thing that breaks naive
identifier mapping. **ISIN** is 12 characters and global (one instrument, one ISIN — though *not*
unique per trading venue); **CUSIP** is 9, North America; **LEI** is 20 and identifies a *legal
entity*, not an instrument.

The engineering points: **identifier mapping is a first-class problem**, not a lookup — many-to-many,
market-dependent, and changing over time (an ISIN change after a corporate action means the same
economic instrument has two identifiers across a date boundary). Reference data is slow-changing and
read-heavy, so it's the textbook **caching** case. And **type your identifiers** (`opaque type Isin`)
so you can't pass a SEDOL where an ISIN belongs — a `String`-typed model cannot catch that, and here
the mistake moves real money.

→ `quiz/15` Q11
</details>

---

### Q12. How do you model a trade amendment in an event-sourced system?

- **A.** Update the original `TradeExecuted` event with the new values
- **B.** Append a new `TradeAmended` event; the original stays in the log forever
- **C.** Delete the original event and append a corrected one
- **D.** Store amendments in a separate audit table

<details><summary>Answer</summary>

**B.** And it's right in both the technical and the business sense: you legally and practically **cannot
pretend it didn't happen** — regulators, auditors and the counterparty all need to see the original
*and* the amendment. Downstream systems have already acted on the original and must be told.

- **A** and **C** destroy information you're required to retain, and in a regulated environment editing
  events is likely an actual compliance issue.

The hard parts, which is where the real answer is: **after clearing or settlement an "amendment" usually
isn't one** — it's a cancel-and-rebook or an offsetting trade, because the original has had external
effects; the aggregate must enforce that as an invariant. And the **cascade** — netting recomputed,
margin recalculated, confirmations reissued, instructions cancelled and replaced, reports amended —
which is a saga with compensation.

**And name bitemporality:** the audit trail must distinguish **when it happened** (`occurredAt`,
business time) from **when we learned about it** (`recordedAt`, system time). That's what answers "what
did the position look like as of last Tuesday, according to what we knew *then* versus what we know
*now*?" **Very few candidates raise this**, and it's genuinely important here.

→ `quiz/15` Q12
</details>

---

### Q13. What is reconciliation, and where does the engineering effort actually go?

- **A.** Independently verifying two records of the same reality and investigating breaks — with most effort in **normalisation and identifier mapping**
- **B.** Balancing the general ledger at month end
- **C.** Matching buy and sell orders
- **D.** Confirming trades with the counterparty

<details><summary>Answer</summary>

**A.** Positions vs the custodian, cash vs nostro statements, trades vs the CCP, intersystem, and
margin. The hard part in practice is normalising both sides: identifiers differ, sign conventions
differ, dates are stated differently.

Then matching with tolerance rules, and — the highest-value automation — **auto-classifying known-benign
timing breaks**, which is the difference between 5,000 breaks and 50 that need a human.

Where event sourcing helps concretely: **as-of reconciliation** (reconstructing state at a point in
time is trivial with a log); **explaining a break** (the event stream *is* the explanation — every event
that moved the position and when, rather than inferring from a balance); and **rebuild-and-compare** as
a control against projection bugs.

The line to use: *"Reconciliation is really a question about temporal state — a system that only stores
current state can tell you that you disagree but not why."*

→ `quiz/15` Q7
</details>

---

### Q14. What's the honest caveat when arguing event sourcing suits post-trade?

- **A.** It's slower than a relational database
- **B.** Regulators don't accept event logs as an audit trail
- **C.** The eventual-consistency window must be checked per use — a **settlement cut-off is a hard deadline**, so projection lag there is a real financial loss, not a UX annoyance
- **D.** Trades change too often for an append-only model

<details><summary>Answer</summary>

**C.** This is what makes the answer senior rather than enthusiastic. Lag must be **monitored and
alerted**, and anything on the critical path to a cut-off should read authoritatively rather than from a
projection.

The other honest caveats: **netting and batch cut-offs sit awkwardly** with pure streaming and need
explicit as-of semantics; **schema evolution over a multi-decade retention period** is a serious
ongoing commitment; and **the team has to be able to work this way** — unfamiliarity, not architecture,
is the usual cause of failure.

- **B** is backwards — the audit trail is a primary *argument for* it.
- **D** is backwards too — amendments are appended, not edited (Q12).

**The closing line:** *"Post-trade is one of the few domains where I'd argue for event sourcing rather
than treat it as a default — because the audit trail and as-of reconstruction aren't features you're
adding, they're regulatory requirements the architecture gives you for free."*

→ `quiz/15` Q8, `quiz/13` Q14
</details>

---

## Score yourself

- **11+** — you can hold a domain conversation credibly, which is all that's needed from an engineer.
- **Vocabulary floor:** Q1 (lifecycle), Q2 (T+1), Q4 (CCP), Q5 (DvP), Q6 (fails).
- **The differentiators:** **Q12's bitemporality point**, **Q14's honest caveat**, and **Q2's
  T+1-to-architecture connection**.
- **Be honest where you're thin.** Domain gaps are expected and forgivable in an engineer; confidently
  wrong domain claims are not.
