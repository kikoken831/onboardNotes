# Quiz 15 — Securities Post-Trade Domain

The JD asks for "understanding of capital markets especially securities trade processing,
clearing, settlements". **You do not need to be a domain expert** — you need to speak the
language credibly, understand the lifecycle, and connect it to the architecture.

## Verification status — read this first

A dedicated fact-check pass (Aug 2026, ~100 agents, adversarial verification) was run over this
file. What it found:

**CONFIRMED against primary sources:**
- The **ISO 15022 / ISO 20022 message mapping** in Q4 — `MT540` receive free, `MT541` receive
  against payment, `MT542` deliver free, `MT543` deliver against payment, `MT548` settlement
  status; `sese.023` = `SecuritiesSettlementTransactionInstruction`, `sese.024` =
  `SecuritiesSettlementTransactionStatusAdvice`. (Corrected and tightened in Q4 as a result.)

**NOT CORROBORATED — treat as unverified and check before quoting:**
- **All settlement-cycle and regulatory dates** in Q2 and Q10: US/Canada/Mexico T+1 in May 2024,
  the **EU/UK October 2027 target and its legal status**, UK/EU currently T+2, India T+1, FX spot
  conventions, and **the current status of the CSDR buy-in regime**. The verification pass
  explicitly flagged the **exact EU/UK T+1 date and the CSDR buy-in element as the two most
  volatile items in the whole pack** — and neither was checked.
- The CCP/CSD lists (Q4), identifier lengths (Q11), DTCC CTM (Q1), and **all AMPS/60East claims
  including the SOW cache and SQL-92 filtering** (Q9).

**Important:** "not corroborated" means the research pass didn't reach or couldn't cleanly cite
these — **not that they're wrong**. Two verifier agents also died on API rate limits, and several
otherwise-sound claims were voted down purely on citation hygiene. So don't discard this content;
**do** check anything date- or status-sensitive against a current primary source before you state
it as fact in an interview.

**The safe move remains:** on any specific convention or current status, *"that's my
understanding, but I'd want to check how you handle it here"* costs you nothing and protects you
from a confidently wrong claim.

14 questions. Target: 25 min.

---

### 1. Walk through the trade lifecycle.

<details><summary>Answer</summary>

**Pre-trade → execution → post-trade.** The JD's team owns the third part.

```
 1. PRE-TRADE      research, pricing, risk/credit checks, order creation
 2. EXECUTION      order routed to a venue (exchange/MTF/OTC), matched -> a TRADE exists
       |           usually FIX protocol; produces an execution report
 3. CAPTURE        the trade is booked into internal systems; enrichment begins
 4. CONFIRMATION   both counterparties agree the economics
       |           (affirmation, then confirmation; matched via a platform like CTM)
 5. CLEARING       a CCP novates and becomes counterparty to both sides;
       |           margin is calculated and called; positions are netted
 6. SETTLEMENT     on the value date: securities move (DvP) against cash
       |           via CSDs/custodians; the leg that fails is a settlement fail
 7. ASSET SERVICING  ongoing: coupons, dividends, corporate actions, tax,
                     proxy voting, stock lending, collateral management
     +  RECONCILIATION and regulatory REPORTING throughout
```

**"Post-trade" = steps 3–7**, i.e. everything after the price is agreed. It's the plumbing
that turns an execution into actual ownership and cash movement.

**The framing that shows you understand *why* it's hard:**
- **It's high-volume, deadline-driven, and unforgiving.** A settlement fail has real financial
  consequences — buy-in risk, interest claims, and under **CSDR** in Europe, mandatory cash
  penalties. So it's less "low latency" than **"correct, auditable, and finished before the
  cut-off"**. Getting that distinction right matters: front-office is microseconds,
  post-trade is throughput plus correctness plus deadlines.
- **It's a long-running, multi-party state machine** with many failure and exception paths, and
  most of the real complexity is in the exceptions — fails, partials, amendments, cancellations,
  and breaks.
- **Everything must reconcile** — with the counterparty, the CCP, the custodian, and your own
  books. Reconciliation is a first-class function, not an afterthought.
- **It's heavily regulated**, so the audit trail is a requirement rather than a nice-to-have.

**And that's exactly why event sourcing fits** (quiz 13 Q14): a genuine state machine, a
regulatory audit requirement, temporal queries for reconciliation and dispute resolution, and
many different views (settlement, positions, regulatory reporting) over the same facts.
**Make that connection explicitly** — it's the single most valuable thing to say in a
domain question for this role.
</details>

---

### 2. What's the difference between trade date, value date, and settlement date? What is T+1?

<details><summary>Answer</summary>

- **Trade date (T)** — when the trade was executed and the economics agreed.
- **Value date / settlement date** — when the exchange of securities and cash is *due*.
  ("Value date" is the more common term for the contractual due date; "settlement date" is
  often used for when it *actually* settles. Being aware they're used loosely — and asking
  which convention a team uses — is better than being confidently wrong.)
- **Actual settlement date** — when it really happened, which may be later if it failed.

**T+n** is the settlement cycle: business days between trade date and value date.
- **T+1** — US, Canada, Mexico moved to T+1 in **May 2024**. India is T+1 (and moving further).
- **T+2** — the UK and EU standard, though **the EU and UK have committed to T+1, with October
  2027 the widely-cited target**. ⚠️ **Unverified date — confirm before quoting.** This is the
  single most volatile fact in the pack; the target date and its legal status have both moved
  before. If you're unsure on the day, say "targeted for 2027" rather than naming a month.
- **T+0 / same-day** — some money-market and repo instruments.
- FX conventions differ (**T+2** spot for most pairs, T+1 for USD/CAD).

**Why T+1 matters and is a great thing to raise unprompted** — it's the biggest current driver
of post-trade technology investment, and it demonstrates you follow the industry:
> Compressing the cycle removes most of the slack that manual exception handling relied on.
> Affirmation and confirmation have to happen same-day, funding and FX have to be arranged
> faster, and the overnight batch window largely disappears. So it pushes firms from
> batch-oriented processing toward **real-time, event-driven** architectures with automated
> exception management — which is precisely the kind of programme a "strategic systems" JD in
> post-trade is likely describing.

That connects the domain to the JD's architecture in one move, and it's a strong answer to
"what trends are affecting this business?" (quiz 08 Q7 / the BofA behavioural round).

**Also worth knowing:** value date calculation needs a **business-day calendar per market and
per currency**, holidays differ, and cross-border trades involve two calendars. That's a real
source of defects and it's why quiz 14 Q7 (injecting the clock and the calendar) matters here.
</details>

---

### 3. What does clearing do, and what's a CCP?

<details><summary>Answer</summary>

**Clearing** is the process between execution and settlement that determines what is actually
owed, and manages the risk that a counterparty fails before then.

**A CCP (Central Counterparty)** — e.g. LCH, ICE Clear, Eurex Clearing, DTCC's NSCC — steps
into the middle of a trade through **novation**: the original bilateral contract between A and
B is replaced by two contracts, A↔CCP and CCP↔B.

**What that buys:**
1. **Counterparty risk mutualisation.** You now face the CCP, not an unknown counterparty. The
   CCP's default waterfall (margin, default fund, mutualised loss) absorbs a member failure.
2. **Multilateral netting.** Instead of settling every trade gross, the CCP nets all of a
   member's obligations in an instrument to a single net position. **This is the big
   operational win** — it can reduce settlement volumes by an order of magnitude, which means
   less settlement risk, less funding, and fewer fails.
3. **Standardisation and anonymity** of the settlement process.

**Margin** — the CCP's protection, and the vocabulary to know:
- **Initial margin (IM)** — collateral posted up front to cover potential future exposure
  (typically a VaR or SPAN-style model over a close-out period).
- **Variation margin (VM)** — daily (or intraday) mark-to-market, so gains and losses are
  settled continuously rather than accumulating.
- **Margin calls** — the demand for more collateral when exposure moves. Time-critical: miss a
  call and you're in default.

**Clearing member vs client clearing** — a bank is typically a **clearing member** of the CCP
and clears for its clients, so it has obligations both to the CCP and from its clients. That
introduces a whole layer of client-money segregation and porting rules.

**Bilateral / non-cleared** trades still exist (many OTC derivatives, some bonds), settled
directly between counterparties with bilateral collateral agreements (CSAs) — post-crisis
regulation (**EMIR**, **Dodd-Frank**) pushed standardised derivatives into mandatory clearing
precisely because of the 2008 counterparty-risk experience.

**Post-2008 context**, if trends come up: central clearing mandates, higher margin
requirements, and **uncleared margin rules (UMR)** are all direct consequences, and they're a
large part of why post-trade technology spend has grown.
</details>

---

### 4. What actually happens at settlement? What's DvP?

<details><summary>Answer</summary>

**Settlement** is the actual transfer: securities from seller to buyer, cash from buyer to
seller.

**DvP — Delivery versus Payment.** The two legs are **conditional on each other**: securities
are delivered **if and only if** cash is paid, simultaneously. This eliminates **principal
risk** (delivering and not being paid). The cash equivalent is **PvP** (payment versus payment)
in FX, which is what **CLS** provides.

**Who's involved:**
- **CSD (Central Securities Depository)** — maintains the definitive record of securities
  ownership in book-entry form. Euroclear, Clearstream, DTC (US), CREST (UK). **Settlement
  ultimately happens on the CSD's books.**
- **ICSD** — international CSDs (Euroclear Bank, Clearstream Banking Luxembourg) for
  cross-border and Eurobonds.
- **Custodian** — holds securities on a client's behalf and instructs settlement. A **global
  custodian** uses a network of **sub-custodians** in local markets.
- **Nostro/vostro accounts** — your cash account at another bank / their account at you.
  Reconciling nostro balances is a core daily post-trade function.
- **SSIs (Standing Settlement Instructions)** — where a counterparty wants its securities and
  cash delivered. **Wrong or stale SSIs are one of the most common causes of settlement
  fails**, which is a good concrete detail to know.

**The message flows** — worth naming: **SWIFT ISO 15022** settlement instructions —
`MT540` **receive free**, `MT541` **receive against payment**, `MT542` **deliver free**,
`MT543` **deliver against payment**, with `MT548` the settlement status and processing advice
(and `MT544`/`MT546` the free-of-payment *confirmations*, as distinct from the instructions).
Then the newer **ISO 20022**: `sese.023` is `SecuritiesSettlementTransactionInstruction` and
`sese.024` is `SecuritiesSettlementTransactionStatusAdvice` — note `sese.023` is **not a 1:1
replacement for any single MT**, since it carries the free/against-payment distinction as a field
rather than in the message type. The industry migration
from 15022 to 20022 is another live programme.

**Settlement fails** — the important part operationally, because handling them is most of the
work:
- Causes: the seller doesn't have the securities (**short/inventory fail**), wrong or missing
  SSIs, insufficient cash, a mismatch between the two sides' instructions, a market-side
  problem, or a late instruction past the cut-off.
- Consequences: **buy-in** risk, interest/compensation claims, and under **CSDR Settlement
  Discipline** in the EU, **mandatory cash penalties accruing daily** until it settles.
- Mitigation: **partial settlement**, **shaping** (splitting a large instruction), securities
  **borrowing** to cover, auto-borrow facilities, and pre-matching before value date.
- **Pre-matching** — proactively confirming the counterparty's instruction matches yours
  *before* the value date, so you fix breaks before they become fails. **The highest-value
  automation in settlement operations**, and a good thing to mention because it's where an
  event-driven system with real-time status genuinely helps.

**FoP (Free of Payment)** — a transfer with no corresponding cash leg (moving stock between
your own accounts, or a collateral pledge). Riskier, since there's no DvP protection.
</details>

---

### 5. What is netting, and what's the difference between the kinds?

<details><summary>Answer</summary>

**Netting** reduces many gross obligations to fewer net ones. It cuts settlement volume,
funding requirements, and risk — so it's central to post-trade efficiency.

- **Bilateral netting** — between two counterparties. You owe me 100 of X, I owe you 80 of X →
  one net delivery of 20.
- **Multilateral netting** — across many parties through a CCP. Far more effective, because
  offsetting positions across the whole membership collapse (see Q3).
- **Payment netting / settlement netting** — netting the *cash and securities to be delivered*
  on a value date. Operational, applies to live obligations.
- **Close-out netting** — netting all obligations under a master agreement into a single net
  amount **on default**. This is a **legal** concept (ISDA/GMRA master agreements), not an
  operational one, and it's what makes derivatives exposure manageable — without enforceable
  close-out netting, gross exposures would be enormous. **Knowing the distinction between
  settlement netting and close-out netting is a genuine signal of domain literacy.**
- **Novation netting** — replacing two offsetting contracts with a single new one.

**Why it matters technically:** netting is a **batch-like aggregation over a set of trades at a
point in time**, which sits awkwardly with pure per-trade event streams. In a CQRS design a
netting set is naturally its own **aggregate** (quiz 13 Q2), and the netting run is a
process that consumes trade events and emits net obligations. Cut-off times matter enormously:
what's included depends on *when* you run, so the run needs a well-defined **as-of** point —
which is exactly what an event log with ordered offsets gives you cleanly, and what a
mutable-state system gives you badly.

That's a good place to connect the domain back to the architecture.
</details>

---

### 6. What are corporate actions, and why are they hard?

<details><summary>Answer</summary>

**Asset servicing** — everything that happens to a holding after settlement.

**Types:**
- **Mandatory** — no choice: cash dividends, coupon payments, stock splits, reverse splits,
  mergers, spin-offs, redemptions, name/ISIN changes.
- **Voluntary / elective** — the holder chooses: tender offers, rights issues, optional
  dividends (cash or scrip), conversions, proxy voting.
- **Mandatory with options** — mandatory event, elective terms.

**Why they're notoriously hard — this is the interesting part:**
1. **Announcement data is unreliable and inconsistent.** The same event arrives from several
   sources (issuer, exchange, custodian, data vendors) in **different formats with different
   details**, and they disagree. So you need **scrubbing and golden-copy** logic to reconcile
   them, and someone has to adjudicate. This is genuinely one of the messiest data problems in
   the industry.
2. **Entitlement depends on dates that are easy to get wrong** — **ex-date**, **record date**,
   **pay date**. Whether you're entitled depends on your position on the record date, which
   depends on what has *settled*, not what has *traded*. So a trade pending settlement across
   an ex-date creates a **market claim** — you're contractually entitled but the securities
   weren't in your name. Handling claims is a whole workflow.
3. **Elections have deadlines**, and the custodian's deadline is earlier than the market's.
   Missing one has direct financial consequences, and the default action may not be what the
   client wanted.
4. **They change positions and prices retroactively**, so historical data, cost basis,
   valuations, and benchmark comparisons must all be **adjusted**. Adjusting historical
   time-series correctly is a persistent source of bugs.
5. **Tax** — withholding rates vary by instrument, jurisdiction, and beneficial-owner status,
   with reclaim processes.

**The architectural connection worth making:** corporate actions are the clearest possible case
for an event log. An entitlement is a function of *your position as of a historical record
date*, which requires **temporal state reconstruction** — exactly what event sourcing gives you
and what a current-state-only model cannot (quiz 13 Q1). And an adjustment is a **new event**
that corrects the record rather than an `UPDATE` that destroys it, which is what you need when
a claim is disputed six months later.
</details>

---

### 7. What is reconciliation, and how would you build it?

<details><summary>Answer</summary>

**Reconciliation** — independently verifying that two records of the same reality agree, and
investigating the differences (**breaks**). It's a core daily post-trade function and a common
source of engineering work.

**What gets reconciled:**
- **Position recon** — your books vs the custodian's/CSD's holdings.
- **Cash recon** — your ledger vs nostro account statements.
- **Trade recon** — your trades vs the counterparty's, or vs the CCP's.
- **Intersystem recon** — front office vs middle vs back office vs the general ledger.
- **Margin/collateral recon** — your calculation vs the CCP's or counterparty's.

**How to build one:**
1. **Normalise both sides** to a common model — the hard part in practice, because identifiers
   differ (ISIN vs CUSIP vs SEDOL vs internal), quantities have different sign conventions,
   and dates are stated differently. **Identifier mapping is where most of the effort goes.**
2. **Match** on a key, with tolerance rules. Exact-match first, then fuzzy/one-to-many matching
   for partials and aggregated bookings.
3. **Classify breaks** — timing (will resolve itself), genuine discrepancy, or a data problem.
   **Auto-classification of known-benign timing breaks is the highest-value automation**,
   because it's the difference between 5 000 breaks and 50 that need a human.
4. **Age and escalate** breaks, with an audit trail of who investigated and what they concluded.
5. **Report** — for operations, and for regulators.

**Where event sourcing helps concretely:**
- **As-of reconciliation.** "Reconcile as of yesterday's close" requires reconstructing state at
  a point in time — trivial with an event log, awkward without.
- **Explaining a break.** The event stream *is* the explanation: you can show every event that
  moved the position and when, rather than inferring it from a current balance.
- **Rebuild-and-compare** as a control: replay from zero into a fresh projection and compare
  against the live one. That's the check that catches a projection bug (quiz 13 Q6/Q9), and it's
  worth proposing as a design idea.

**The line to use:** *"Reconciliation is really a question about temporal state — and a system
that only stores current state can tell you that you disagree but not why. That's the
strongest practical argument for an event log in this domain."*
</details>

---

### 8. How does event sourcing / CQRS map onto post-trade processing?

<details><summary>Answer</summary>

**The synthesis question, and the one most likely to be asked given this JD's wording.** Have
this ready.

**The domain-to-architecture mapping:**

| Domain concept | Architectural concept |
|---|---|
| A trade's lifecycle | an **aggregate** with a state machine (quiz 12 Q20) |
| Execution, confirmation, clearing, settlement | **events** — immutable, past tense |
| "Settle this trade", "amend this trade" | **commands** — can be rejected |
| "Cannot settle an unconfirmed trade" | an **invariant inside the aggregate** |
| Position keeping | a **projection** over settled-trade events |
| Settlement dashboard, regulatory report, client API | **separate projections**, each shaped for its query |
| Confirm → clear → fund → settle across systems | a **saga / process manager** with timeouts and compensation |
| A correction or reversal | a **compensating event**, never an `UPDATE` |
| Regulatory audit trail | the **event log itself** |
| As-of reporting, reconciliation | **replay to an offset** |
| Netting run at a cut-off | a process over events **as of** a defined point (Q5) |
| Corporate action entitlement at a record date | **temporal state reconstruction** (Q6) |

**Why this is a genuinely good fit — five of quiz 13 Q14's "when to use it" criteria hit:**
1. **Regulatory audit is a hard requirement** — you must prove what you knew and when.
2. **Temporal correctness is intrinsic** — record dates, as-of positions, reconciliation.
3. **A genuine, complex state machine** with many exception paths.
4. **Many consumers wanting different views** of the same facts.
5. **Integration-heavy** — the log is the natural contract with downstream systems.

**And the honest caveats, which is what makes it a senior answer:**
- **Eventual consistency has to be checked against each use.** A settlement *instruction*
  cut-off is a hard deadline; if a projection lag means you miss it, that's a real financial
  loss, not a UX annoyance. So lag must be **monitored and alerted**, and anything on the
  critical path to a cut-off should read authoritatively (quiz 13 Q4).
- **Netting and batch cut-offs sit awkwardly** with pure streaming and need explicit as-of
  semantics (Q5).
- **Schema evolution over a multi-decade retention period is a serious ongoing commitment**
  (quiz 13 Q5) — you will be reading events written by people who left years ago.
- **The team has to be able to work this way.** The most common failure of event-sourced
  projects is unfamiliarity, not architecture (quiz 13 Q14).

**Closing line:** *"Post-trade is one of the few domains where I'd argue for event sourcing
rather than treat it as a default — because the audit trail and as-of reconstruction aren't
features you're adding, they're regulatory requirements the architecture gives you for free."*
</details>

---

### 9. What is AMPS, and how does it compare to Kafka?

<details><summary>Answer</summary>

The JD lists AMPS as "a plus", so **be honest if you haven't used it** and relate it to what
you have. That's a much better answer than bluffing.

**AMPS (60East Technologies)** — a low-latency publish/subscribe messaging engine used widely
in financial services for market data, order state, and trade status distribution.

**Its distinguishing features:**
- **Very low latency**, high throughput pub/sub — designed for the front-to-middle office.
- **Content-based filtering** — subscribers register a filter and AMPS evaluates it
  server-side, so clients receive only matching messages. Reduces network and client CPU
  substantially versus filtering client-side.
- **SOW (State of the World)** — a server-side cache of the **last value per key** for a
  topic. So a new subscriber can get the **current state** immediately and then a continuous
  stream of updates, in one operation ("SOW and subscribe"). That's the key architectural idea.
- **SQL-92-style queries** over the SOW — you can query current state directly from the
  messaging layer.
- **Views and aggregation**, conflation (dropping intermediate updates for slow consumers),
  and delta publishing.
- Focused replay/recovery features rather than long-term retention.

**Compared to what you know:**

| | **AMPS** | **Kafka** | **RabbitMQ / JMS** |
|---|---|---|---|
| Model | pub/sub + queryable **state cache** | distributed **append-only log** | broker queues / topics |
| Retention | recent + SOW current state | **long / configurable, replayable** | until consumed |
| Latency | very low (microseconds) | low (low ms) | low |
| Filtering | **server-side content filter** | client-side / stream processing | routing keys, header filters |
| Current state | **built in (SOW)** | needs a compacted topic or an external store | not a feature |
| Replay history | limited | **native — the point of it** | no |
| Ordering | per topic | **per partition** | per queue |

**The framing to use:**
> I haven't used AMPS directly, but conceptually it maps onto the same problem I've solved with
> Kafka — distributing state changes to many consumers with different interests. The
> distinctive parts are server-side content filtering and the State of the World cache, so a
> subscriber gets current state plus a live stream in one step. Where I'd reach for Kafka is
> **durable, replayable history** — which is what an event store needs. Where AMPS looks
> stronger is **low-latency fan-out and last-value queries**.
>
> In an event-sourced design (quiz 13 Q12) I'd expect them to be complementary rather than
> competing: a durable store as the system of record, and AMPS as the low-latency distribution
> and current-state query layer for downstream consumers and screens. **AMPS's SOW is
> essentially a materialised projection maintained by the messaging layer** — so it fits the
> CQRS read-side role directly.

That last observation connects it to the architecture rather than treating it as trivia, and
it's the kind of answer that works even without hands-on experience.

**A reasonable question to ask them:** "Are you using AMPS as the distribution layer over a
durable store, or is the SOW acting as the read model?" It's a real design question and it
shows you understand the trade-off.
</details>

---

### 10. What are the key regulations you'd expect to affect a post-trade system?

<details><summary>Answer</summary>

You don't need depth — you need to know the names, roughly what each demands, and the
*engineering* consequence.

| Regulation | What it demands | Engineering consequence |
|---|---|---|
| **MiFID II / MiFIR** (EU/UK) | transaction reporting (T+1), best execution, clock synchronisation, record keeping | reporting pipelines, **timestamp precision and traceable clocks**, 5+ year retention |
| **EMIR** (EU) | derivative trade reporting, clearing obligation, risk mitigation | reporting to trade repositories, reconciliation of both sides |
| **Dodd-Frank** (US) | swap reporting, clearing mandates, SDR reporting | similar |
| **CSDR** (EU) | settlement discipline: **mandatory cash penalties for fails**, buy-in regime | you must **track fails precisely**, calculate penalties, and reconcile them with the CSD |
| **SFTR** (EU) | securities financing transaction reporting | repo/stock-lending reporting |
| **BCBS 239** | risk data aggregation and reporting principles | **data lineage**, accuracy, completeness, and timeliness — very architecture-relevant |
| **SOX** | financial reporting controls | segregation of duties, change control, audit trails in the SDLC (quiz 08 Q3) |
| **GDPR** | data protection, right to erasure | awkward with an immutable log → **crypto-shredding** (quiz 13 Q14) |
| **T+1 migration** | shorter settlement cycle | the big current driver (Q2) |
| **Basel III/IV** | capital and liquidity | accurate, timely position and exposure data |

**The engineering themes that recur across all of them** — this is the useful part, and it's
how to answer without pretending to be a compliance expert:
1. **Immutable audit trails** — who did what, when, and on what basis. **Event sourcing gives
   you this structurally** rather than as a bolt-on.
2. **Accurate, synchronised timestamps** — MiFID II has explicit clock-sync requirements. So
   timestamps aren't incidental metadata; they're regulated data. (Which is another reason not
   to derive them in a projector — quiz 13 Q9.)
3. **Data lineage** — being able to show where a reported number came from. A fold over an
   event log *is* lineage.
4. **Reporting deadlines** — T+1 reporting is a hard operational SLA with penalties.
5. **Long retention** — 5–7 years typically, which makes **schema evolution** a decade-scale
   commitment (quiz 13 Q5).
6. **Reconciliation and completeness controls** — proving you reported everything you should.

**The honest positioning:** *"I wouldn't claim regulatory expertise — that's what compliance
and the business analysts are for. What I'd bring is understanding the engineering
requirements they generate: immutable audit trails, accurate timestamps, demonstrable data
lineage, hard reporting deadlines, and multi-year retention with schema evolution. Those shape
the architecture more than any individual rule."*

That's credible, useful, and doesn't overclaim.
</details>

---

### 11. What identifiers and reference data would you expect to deal with?

<details><summary>Answer</summary>

Reference data is where a surprising amount of post-trade defect volume comes from, so knowing
the vocabulary is genuinely useful.

**Instrument identifiers:**
- **ISIN** — 12 characters, the global standard (`GB00B16KPT44`). One instrument, one ISIN —
  but **not** unique per trading venue.
- **CUSIP** — 9 characters, North America.
- **SEDOL** — 7 characters, UK/Ireland, and **market-specific** — the same ISIN has different
  SEDOLs in different markets, which is exactly the kind of thing that breaks naive mapping.
- **RIC / Bloomberg ticker** — vendor-specific, venue-specific.
- **FIGI** — an open identifier, increasingly used.

**Party identifiers:**
- **LEI** — Legal Entity Identifier, 20 characters, mandated for regulatory reporting.
- **BIC / SWIFT code** — for messaging and settlement routing.
- **Internal counterparty and account ids**, plus **BAs/hierarchies** for client structures.

**Other reference data:**
- **SSIs** — standing settlement instructions per counterparty, per market, per currency.
  **Stale SSIs are a leading cause of settlement fails** (Q4).
- **Business-day / holiday calendars** per market and currency (Q2, quiz 14 Q7).
- **Currency codes** (ISO 4217), FX rates and fixing sources.
- **Corporate action announcements** (Q6).
- **Market/venue codes** (MIC).

**The engineering points that matter:**
1. **Identifier mapping is a first-class problem**, not a lookup. The mappings are
   many-to-many, market-dependent, and change over time (an ISIN change after a corporate
   action means the *same economic instrument* has two identifiers across a date boundary).
2. **Reference data is slow-changing and read-heavily** — the textbook case for **caching**,
   and the concrete example used in quiz 10 Q1's enriched-endpoint design. Cache with a TTL,
   bound it, and have an invalidation story.
3. **Golden copy** — one authoritative internal representation, with mappings to external
   identifiers, rather than every system doing its own translation.
4. **Type the identifiers.** `opaque type Isin`, `opaque type Cusip` — so you cannot pass a
   SEDOL where an ISIN belongs. That's quiz 12 Q20's point, and this domain is exactly why it
   pays: a `String`-typed model cannot catch an identifier mix-up, and here that mistake moves
   real money.
5. **Reference data changes should probably be events too** — you need to know what you
   believed an instrument's terms were at the time you acted.
</details>

---

### 12. How would you handle a trade amendment or cancellation?

<details><summary>Answer</summary>

A great question because the naive answer is wrong in an event-sourced system, and the right
answer is also the right *business* answer.

**The wrong instinct:** find the trade and mutate it, or delete it.

**The correct model: append a new event.**
- **Amendment** → `TradeAmended(tradeId, changes, reason, amendedBy, at)`. The original
  `TradeExecuted` **stays in the log forever**. Current state is the fold, so it reflects the
  amendment, but the history shows what was originally booked, what changed, when, why, and by
  whom.
- **Cancellation** → `TradeCancelled(tradeId, reason, cancelledBy, at)`.
- **Correction of a booking error** → a **compensating event**, and if a downstream effect
  already happened (a payment, a position update), a **reversal** — a new business fact, not an
  erasure.

**Why this is right in both senses:**
1. **You legally and practically cannot pretend it didn't happen.** Regulators, auditors, and
   the counterparty all need to see the original and the amendment. A destructive `UPDATE`
   loses information you are required to retain.
2. **Downstream systems have already acted** on the original. They need to be told about the
   change, which means an event, which means the log is the natural mechanism (quiz 13 Q13).
3. It's the domain's own language: operations call these amendments and cancellations, not
   updates.

**The hard parts, which is where the real answer is:**
- **What's amendable, and when?** Economics (quantity, price) versus non-economic fields
  (settlement instructions, account). **After clearing or settlement, an "amendment" usually
  isn't an amendment at all** — it's a cancel-and-rebook, or a new offsetting trade, because
  the original has already had external effects. The aggregate must enforce that as an
  invariant (quiz 13 Q8), and the state machine should make the illegal transitions
  unrepresentable (quiz 12 Q20).
- **Cascade effects.** An amendment invalidates downstream results: netting must be recomputed,
  margin recalculated, confirmations reissued, settlement instructions cancelled and replaced,
  reports amended. That's a **saga** with compensation (quiz 13 Q11), and it's where the real
  complexity lives.
- **Amendment across a reporting boundary.** Amending a trade after it's been reported means
  submitting an **amended regulatory report**, with its own rules and deadlines.
- **Amendment after a record date** interacts with corporate action entitlements and claims
  (Q6).
- **As-of amendments** — backdated changes, which retroactively alter historical positions. The
  audit trail must distinguish **when it happened** from **when we learned about it** —
  i.e. **bitemporality**: `occurredAt` (business/valid time) versus `recordedAt` (system time).
  **Naming bitemporality here is a strong signal** — it's exactly what event sourcing plus an
  explicit business timestamp gives you, and it's essential for "what did the position look
  like as of last Tuesday, according to what we knew last Tuesday?" versus "according to what
  we know now."

That last distinction is genuinely important in this domain and very few candidates raise it.
</details>

---

### 13. What questions would you ask about their system?

<details><summary>Answer</summary>

Asking good questions is scored, and in a domain interview it's also how you avoid overclaiming.
Pick three or four whose answers would change how you work:

**Architecture:**
- "Is the strategic platform genuinely event-sourced with a log as the system of record, or
  event-*driven* over a state-oriented store?" (Very different, and people use the terms
  interchangeably — quiz 13 Q3.)
- "What's the event store, and how do you handle schema evolution over your retention period?"
- "How do you handle the eventual-consistency window where a settlement cut-off is involved?"
- "Are you using AMPS as distribution over a durable store, or is the SOW acting as the read
  model?" (Q9.)

**Scala and the migration:**
- "Which Scala stack — `Future`, Pekko, Cats Effect, or ZIO? And Scala 2.13 or 3?"
  (quiz 07 Q8, quiz 12 Q19.)
- "Is the Java-to-Scala migration incremental with both running side by side, or a rewrite?"
- "What's driving it — expressiveness and types, or specific operational pain?"

**Delivery:**
- "How far do you take BDD — do business stakeholders actually read the feature files?"
  (quiz 14 Q5 — a diplomatic way to find out if it's real.)
- "What does the release process look like, and how do you handle schema migrations given
  they're not reversible?"

**Domain and scope:**
- "Which part of the lifecycle does this team own — confirmation through settlement, or asset
  servicing too?"
- "How much of the work is the T+1 programme versus BAU?" (Q2 — shows you know the context.)
- "Where does the exception/fails management workflow live?"

**Team:**
- "How much Scala experience is on the team now — am I joining people who'll teach me the
  house idioms, or is the team learning together?" (An honest and useful question that also
  signals you know the difference matters.)
- "The JD mentions coaching and mentoring — what does that look like day to day?"

**Don't ask** anything you could have looked up, and don't ask about compensation or WFH in a
technical round.
</details>

---

### 14. What are the biggest trends facing this business right now?

<details><summary>Answer</summary>

This is a real BofA screening question (quiz 08 Q7 territory), and for a post-trade role it has
a specific, well-informed answer. Pick two or three and be able to say why they matter
*technically*.

1. **T+1 settlement, and T+1 in the EU/UK targeted for 2027** (⚠️ month unverified — see the
   verification note at the top of this file). The US, Canada and
   Mexico moved in May 2024. This is **the** dominant post-trade programme: it removes the
   slack that manual exception handling relied on, compresses affirmation/confirmation to
   same-day, squeezes funding and FX, and largely eliminates the overnight batch window. The
   technical consequence is a push from **batch to real-time, event-driven** processing with
   automated exception management — which is very likely what a "strategic systems" JD is
   about. **Lead with this one.**
2. **ISO 20022 migration** — replacing SWIFT MT/ISO 15022 with richer XML/structured messages.
   More data, better automation, and a long dual-running period.
3. **Automation of exception management.** Fails, breaks and claims are where the cost is. STP
   (straight-through processing) rates and auto-classification of breaks are where investment
   goes (Q7).
4. **DLT / tokenisation, treated soberly.** Plenty of pilots (digital bonds, tokenised
   collateral, atomic DvP), real potential to compress settlement to T+0, and **still limited
   production volume**. The measured view — "interesting, real experiments, not yet displacing
   CSDs" — is more credible than either hype or dismissal.
5. **Cost and consolidation pressure** — margin compression driving platform consolidation and
   utility/managed-service models, which is often what "strategic platform" means in practice:
   retiring several legacy systems onto one.
6. **Regulatory reporting burden and data quality** — BCBS 239-style lineage expectations,
   CSDR penalties making settlement performance a measured KPI.
7. **Cloud adoption in regulated workloads**, with data residency and resilience constraints;
   and **operational resilience** regimes (DORA in the EU) raising the bar on recovery testing.
8. **AI/ML in operations** — realistically, break prediction, fail prediction, and matching
   assistance rather than anything dramatic. Predicting which trades will fail so operations
   can intervene *before* the value date is a genuinely valuable application (Q4's
   pre-matching).

**How to answer well:** pick **two**, say why they matter to *this team's* work, and connect one
to the architecture. For example:
> The dominant one is T+1 — the US, Canada and Mexico moved in May 2024, and the EU and UK are
> targeted for 2027.
> It removes the overnight window that a lot of exception handling depended on, so the
> pressure is to move from batch to real-time event-driven processing with much better
> automated exception management. That's presumably part of why the role is framed around
> strategic systems and event-driven architecture — and it's the kind of constraint that
> makes an event log genuinely valuable, because you need real-time status *and* a defensible
> audit trail at the same time.

**And be honest about the limits of your domain knowledge** — "I've been reading into
post-trade rather than working in it directly; my background is on the engineering side of
high-volume event-driven systems" is a strong, credible framing that invites them to tell you
what actually matters.
</details>

---

## Score yourself

- **Must know:** Q1 (the lifecycle), Q2 (T+1 and date types), Q3 (clearing/CCP), Q4 (DvP and
  fails). That's the vocabulary floor.
- **The differentiators:** Q8 (mapping the domain onto event sourcing) — that's the synthesis
  question for this role — plus Q12's bitemporality point and Q14's T+1 answer.
- **Be honest where you're thin.** Domain gaps are expected and forgivable in an engineer;
  confidently wrong domain claims are not. "I'd want to check how you handle that here" is
  always a safe move, and Q13 gives you the questions to ask.
