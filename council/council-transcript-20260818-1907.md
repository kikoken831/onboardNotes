# Council Transcript — CV revision for BofA GMT Post Trade

**Date:** 18 Aug 2026
**Counciled:** `cv.tex` + `team jd.pdf` — Bank of America, GMT: Software Engineer, Global Markets
Technology (Post Trade — settlements and asset servicing, equities and fixed income)
**Composition:** 5 advisors → 3 peer reviewers (unanimous) → 1 chairman
**Note on method:** the reference methodology specifies 5 peer reviewers. 3 were run; all three
agreed unanimously on both the strongest response and the biggest blind spot, so further reviewers
were judged unlikely to change the verdict. Recorded here for transparency.

---

## Original question

"Consult the council on what I should fix and improve and any additional input is required" — for a
CV being submitted to the BofA GMT Post Trade req.

## Framed question

How should Kendrick Kee (Singapore, ~4 yr 4 mo experience) revise his CV for this role, and what
additional information is needed from him? Seven sub-questions were put: (1) how to handle being
3 months into an Apple contract; (2) how to close the Scala/event-sourcing/CQRS gap honestly, and
whether the self-study pack belongs on the CV; (3) whether to lead with Java/trading instead of Go;
(4) which metrics to cut, keep or reframe; (5) the top five changes in order; (6) what extra
information is needed; (7) what the pre-analysis got wrong or over-weighted.

The framing included the full JD requirements, the verbatim CV content, and a pre-analysis of twelve
suspected issues (A–L) for the advisors to validate, correct or extend.

---

## Critical fact established BEFORE synthesis

The Executor advisor inspected the repository rather than trusting its description. This was
independently re-verified twice — once by the orchestrator and once by a peer reviewer:

- `problems/` contains **12 Java concurrency problems, 3 Scala files, 4 GC labs** — all JVM/GC
- **Zero** event-sourcing code, **zero** CQRS code, **zero** `.feature`/Gherkin files
- **No** `build.gradle`, **no** `build.sbt`, **no** `pom.xml`
- Event sourcing, CQRS and ATDD/BDD exist **only as prose** — in `quiz/13-event-sourcing-and-cqrs.md`
  (846 lines) and `quiz/14-atdd-bdd-tdd.md` (622 lines), plus `mcq-quiz/` equivalents
- Filenames are derived from this employer's own JD, and there is a `cheatsheets/answer-scripts.md`

This inverted the majority recommendation and is the pivot of the whole session.

---

## Advisor responses

### The Contrarian

The fatal flaw isn't keywords — it's verifiability. A bank runs education and employment verification.
His BSc conferred Oct 2023 *while* he was employed full-time from Apr 2022, and a Georgia Tech MSc
landed May 2025 *while* at Citibank. That invites "when did the 6+ years start, and were you a student
or an engineer?" The "6+ years" isn't sloppy rounding — combined with the degree dates it looks like
inflation, and inflation found during background check gets a signed offer rescinded. Fix the number
before touching a single keyword.

Second flaw: everyone is neutralising the wrong thing. Three months at Apple isn't the problem; the
problem is four employers in four years, ~14-month mean tenure, two of them agency contracts.
Explaining Apple in isolation treats a symptom. The story must be "contract roles ended, I'm seeking
permanent" — and if he can't say that truthfully, he's a flight risk to them regardless of the CV.

Third: numbers that invite attack. "200% performance improvement" is incoherent — an interviewer will
ask 200% of what and he'll flounder. "97%→99.8% uptime" means he's claiming to have inherited 11
days/year of outage; either false or an indictment of his employer. "8s→4.8s" is bragging about a
still-slow dashboard. Cut all three. They're not weak metrics, they're liabilities in a
rigour-obsessed shop.

On the study pack: do not put it on the CV. "245 Q&A, 210 MCQs" reads as exam cramming — it tells them
he's studying *for the interview*, which is the opposite of credibility. Convert it into one public
repo: an event-sourced trade-lifecycle service in Scala with Gherkin feature files and a commit
history showing test-first. One shipped artifact beats a study log.

Over-weighted by the analysis: the ATS keyword list. Stuffing "event sourcing/CQRS" gets him into a
room where a Scala pairing round will expose him in 20 minutes. Keywords buy an interview he isn't
ready to pass — sequence the repo first.

Missing information: notice period and TEKsystems contract terms; work authorisation independent of
employer sponsorship; whether Apple/Citi will confirm exact dates and titles; and whether he has *any*
production Scala service code, not Spark jobs.

And the placeholder URLs: `github.com/yourusername` is not a typo, it's proof of an unproofread
mass-apply template — sent to a team whose JD shouts "RIGOROUSLY."

### The First Principles Thinker

You're asking a CV-formatting question when the actual problem is evidentiary.

Strip it down: the hiring manager has one question — "will this person be productive in a Scala
event-sourced post-trade codebase?" Every CV edit is a proxy for that. Proxies have a ceiling. He has
something better than a proxy and he's leaving it in a folder.

That study pack isn't a CV bullet. It's a public GitHub repository. 245 Q&As and *runnable, verified*
Scala/Java on event sourcing, CQRS, JVM internals and settlements is a body of work an engineer can
open and inspect in ninety seconds. It converts "familiar with event sourcing" — a claim every
applicant makes and half get destroyed on — into a link. It simultaneously fills the empty
`github.com/yourusername` placeholder, which right now is the single most damaging artifact on the
page: it says *this person does not check their own work*. Fix that placeholder or delete the line
entirely. Non-negotiable.

Second reframe. The three-month Apple tenure isn't a loyalty problem; it's a *fear* problem, and the
fear is "he'll leave us too." That fear dissolves on a fact, not spin: TEKsystems and LTIMindtree
engagements are fixed-term. Contracts end. Write "Contract engagement" on both, and the entire
job-hopping read collapses into a normal contractor trajectory. Don't argue it — label it.

What the analysis over-weights: keyword-stuffing. Injecting all fifteen missing terms produces a
document that reads as machine-fitted and hands an interviewer fifteen fresh attack surfaces. Pick the
four he can defend under questioning. Also, commodities-vs-fixed-income is not a real gap — trade
lifecycle, breaks, T+n mechanics transfer — and "architected" overuse has never rejected anyone.

What's missing: the band/grade of the req. A JD demanding he "coach and mentor a team" may be a senior
level his four years won't clear regardless of wording. Ask the recruiter before rewriting a word.

> **Chairman's note:** the "publish the pack" recommendation is STRUCK — it rests on the same factual
> error as the Expansionist's. The placeholder and contract-labelling points are upheld, and the
> "fear, not loyalty" reframe is the most useful single sentence in this response.

### The Expansionist

The self-study pack is not a footnote — it is the strongest asset in this entire file, and the analysis
has buried it.

245 Q&A, 210 MCQs, and *runnable verified* Scala/event-sourcing code is a curriculum. Nobody else in
that applicant pile has one. Turn it into a public GitHub repo and it simultaneously solves five
problems: it fills the `github.com/yourusername` placeholder with something worth clicking, it is
honest Scala-as-a-service-language evidence, it is coach-and-mentor proof *dated today* rather than
three jobs down, it demonstrates "clearly communicate technical details," and it makes the interview a
conversation about his code instead of an interrogation about his gaps.

Then go further: extract from it a small **event-sourced post-trade settlement engine** — Scala, CQRS
read models, Cucumber feature files, TDD commit history, Gradle build, Jenkins pipeline. One
weekend-scale project honestly captures event sourcing, CQRS, ATDD/BDD, TDD, Gradle, settlements, and
FP-plus-OOP. Seven missing keywords, zero fabrication. Put it as a **Projects** section directly under
the summary, above Apple.

The Apple tenure is being read backwards. Apple recruited him *out of a bank* three months ago — that
is external validation of his engineering bar that BofA cannot generate internally. The story is "Apple
proved I can work at that level; I'm returning to post-trade deliberately," not an apology.

Two undervalued things: a 3.83 Georgia Tech MSc plus Apple plus real trading domain means he is likely
aiming a band too low — he should push for scope and mentoring responsibility in the conversation. And
this same repositioning unlocks every Scala post-trade desk in Singapore: GS, JPM, Morgan Stanley,
StanChart, DBS. One repo, ten applications.

The analysis over-weights keyword hygiene and the "6+ years" arithmetic. Those are twenty-minute fixes.
The artifact is the leverage.

> **Chairman's note:** the central factual premise here is FALSE — the pack contains no event-sourcing
> or CQRS code. Publish it and a reviewer finds prep notes named after their own JD plus an
> answer-script file. All derived conclusions fall: "publish it", "above Apple", "aim a band higher",
> and "Apple recruited him out of a bank" (it was a TEKsystems placement). STRUCK 3/3 by reviewers.
> What SURVIVES is the artifact-extraction idea, which becomes recommendation step 3 — build the
> settlement engine in a clean new repo, don't publish the study pack.

### The Outsider

What I'd notice in 30 seconds, in order:

The placeholder links. `github.com/yourusername` tells me he didn't proofread the one document he's
judged on. For a role demanding "rigorous" TDD, that's the whole story. Nothing else on the page
recovers from it.

Then the dates don't add up. Summary claims "6+ years," but the earliest job listed is Apr 2022 — four
years. And a BSc completing Oct 2023, *after* two of the four jobs, with an MSc finishing May 2025,
eighteen months later. I can't build a timeline. As a stranger I don't conclude "interesting path," I
conclude the numbers are decorative — and then I stop believing "$2B+" and "200% improvement" too.

"Via TEKsystems," "via LTIMindtree" — I read contractor, four employers in four years, and 3 months at
the current one. Nobody explains why they left; the CV must.

Words I'd skip because I don't know them: "two-tower retrieval," "dual-hat," "payments- and
commerce-adjacent surfaces," "inference footprints." Also "on-device personalization across the global
iOS install base" — impressive-sounding, but I can't tell what *he* built versus what Apple ships.

"Deep Golang expertise" in line one, for a Scala job. I'd file him as a Go person and move on.

What's absent is louder than what's there: event sourcing, CQRS, TDD, BDD, Gradle, Ansible, Artifactory
— zero appearances. Scala appears third in a language list and only ever near "PySpark."

The self-study pack: "private" means unverifiable, and naming it says "I haven't done this at work."
Make it a public repo with a running event-sourced settlement demo, linked — then it's evidence, not an
apology.

"Architected" three times in four years reads as inflation.

### The Executor

I ran your repo. Grounding first, because it changes the answer to Q2: `problems/` has 13 Java
concurrency files, 3 Scala files, 4 GC labs. **Zero event-sourcing code. Zero CQRS code. Zero feature
files. No `build.gradle`, no `build.sbt`.** Events/CQRS and ATDD exist only as prose in
`quiz/13-*.md` and `quiz/14-*.md`. So publishing that repo does not close the gap — a BofA reviewer
clicking it finds interview prep notes for their own JD. That reads as studying, not building. Don't
link it.

**The five changes, in order:**

1. **Delete the template placeholders** in `cv.tex` and change "6+ years" to "4+". **10 minutes.**
   Non-negotiable; `github.com/yourusername` is an instant screen-out and it's currently shipping.
2. **Rewrite the summary** — Java/Scala and trade processing in the first eight words, Go demoted to
   one clause. Reorder the skills line to match. **45 minutes.**
3. **Reframe the Citi metrics.** Cut "100K+ daily transactions" (1.2 req/sec, and a reader who divides
   will find it). Keep sub-200ms, keep $2B notional, keep 8s→4.8s, keep 2 weeks→3 days. **30 minutes.**
4. **Retrofit the real vocabulary** into Citi/ComBuilder bullets — only where true. Your integration
   bus and audit trails are probably event-sourcing-adjacent; say so precisely. **90 minutes.**
5. **Build ONE artifact: a small Scala event-sourced trade lifecycle service** — commands, immutable
   event log, projection, Gradle build, one Cucumber feature file. **Two days.** This is the only item
   that converts a claim into evidence, and it's the only one worth a weekend.

Total: ~3 hours, plus one weekend for #5. Items 1–4 ship tonight.

**Over-weighted:** the 245 Q&A volume (invisible to a recruiter), the Apple brand name, and the
3-month tenure — nobody rejects on tenure at CV stage, they ask at interview. One sentence of prep, not
a CV restructure.

**Need from him:** peak TPS (not daily average), whether Citi touched fixed income at all,
Gradle-vs-Maven exposure, and notice period.

---

## Peer reviews

Anonymisation map used for review: **A** = Outsider · **B** = Executor · **C** = Contrarian ·
**D** = Expansionist · **E** = First Principles Thinker

### Reviewer 1

**Strongest: B (Executor).** The only response that grounded itself in the artifact instead of the
summary — it enumerated what's actually in the repo and drew the correct inference: publishing it makes
a BofA reviewer read prep notes for their own JD. It then gives ordered, time-boxed actions with a
keep/cut metric split and a specific "build one artifact" spec. C (Contrarian) is a close second on the
verification/background-check angle and the "200% of what?" liability framing.

**Biggest blind spot: D (Expansionist)** — E shares it. D asserts "runnable verified
Scala/event-sourcing code" and would put it in a Projects section above Apple. That code does not
exist. D's entire thesis — one repo, ten applications, aim a band higher — is built on a fabrication,
and acting on it would hand a TDD-obsessed team a link that disproves the claim. D also never registers
that the pack is derived from this JD.

**All five missed:**
- Channel: at 3 months tenure the recruiter screen, not the CV, is the filter. A BofA Singapore GMT
  referral outranks every edit listed.
- Timing: nobody asked whether to apply now or spend 6–9 months getting production Scala at Apple first.
- Saying "I built a study pack from your JD" out loud reads as gaming the process — B/C/D/E all
  recommend surfacing it in some form without pricing that.

### Reviewer 2

**Strongest: B (Executor).** The only response grounded in what the pack actually contains. From that
it draws the correct and non-obvious conclusion: linking the repo *hurts*. It also gives ordered,
time-boxed actions and the sharpest single catch — "100K+ daily transactions" is 1.2 req/sec and any
reader who divides will find it. C is a close second on the verification and liability-metrics analysis.

**Biggest blind spot: D.** It asserts "runnable verified Scala/event-sourcing code," which is factually
false, then builds its entire thesis on that phantom asset and tells him to put it above Apple. E
shares the hallucination. D also misreads a TEKsystems contract placement as "Apple recruited him," and
advises pushing for a *higher* band on evidence that does not exist.

**All five missed:** the Georgia Tech MSc is OMSCS — online and part-time — which fully explains the
overlapping degree/employment timeline that C escalates to offer-rescission risk. Also missed: the
sequencing decision (bank reqs are team-specific and rejection often blocks reapplication for 6–12
months, so applying before the artifact exists may be the costlier error), a referral into GMT Singapore
outweighing every keyword edit, and the cover letter as the only place a gap can be stated honestly.

### Reviewer 3

I verified the repo myself: `problems/` = 12 Java concurrency files, 3 Scala files
(`S01BlockingGlobalEC`, `S02FutureTraps`, `S03AllocationPressure`), 4 GC labs. No `.feature`, no
`build.gradle`, no `build.sbt`. Event sourcing/CQRS/ATDD exist only as prose in `quiz/` and `mcq-quiz/`.

**Strongest: B.** Only response that actually grounded itself in the repo and got it right, which
inverts the majority recommendation ("don't link it" vs everyone else's "publish it"). It also costs and
sequences each fix, correctly kills "100K+ daily transactions" while keeping defensible metrics, and
asks the right follow-ups. C is a strong second for the background-check/verification framing.

**Biggest blind spot: D.** It asserts the pack contains "runnable verified Scala/event-sourcing code."
It does not. D's entire thesis would send a BofA reviewer to a folder of quiz files named after their
own JD. Confidently wrong on the load-bearing fact.

**All five missed:** the pack can't be published as-is even after adding code — directory names like
`mcq-quiz/13-event-sourcing-and-cqrs.md` and `cheatsheets/answer-scripts.md` read as cramming for one
employer; he needs a clean, separate repo. Conversely, those 245 Q&A are the *spec* for the artifact,
cutting build time. And nobody asked the gating questions: is the req still open, is there a referral
path (which bypasses ATS entirely), and what does leaving Apple at month 3 cost contractually?

---

## Chairman synthesis

### Where the council agrees

Five for five, and all three reviewers: the CV as it stands is **actively harmful and currently
shipping**. `cv.tex` line 61 still reads `github.com/yourusername` and `linkedin.com/in/yourprofile`,
and line 67 opens "Software Engineer with 6+ years... Deep Golang and REST API expertise." For a req
whose literal phrasing is "RIGOROUSLY adheres to TDD," an unproofread contact line is not a typo; it is
a work-sample failure. Unanimous agreement also on: fix "6+ years" to 4+ (unverifiable-claim risk at
background check, not sloppiness), demote Go out of the first sentence, lead with Java/Scala and trade
processing, label both agency roles as fixed-term **contract engagements** so the four-employers-in-four-years
read collapses into a fact rather than a defence, and cut the incoherent metrics. Everyone agrees
keyword-stuffing "event sourcing / CQRS" is the wrong instinct: it buys an interview a Scala pairing
round ends in twenty minutes.

### Where the council clashes

The real clash is over one asset, and it decides everything else.

**The Expansionist and the First Principles Thinker both rest their central recommendation on a factual
error about the repo.** They describe the study pack as verified runnable Scala/event-sourcing evidence
and want it published and placed *above Apple*. `problems/` contains twelve Java concurrency files
(P01–P12), three Scala files, four GC labs. No `.feature`, no `build.gradle`, no `build.sbt`, zero
event-sourcing or CQRS code. Event sourcing and CQRS exist only as prose in
`mcq-quiz/13-event-sourcing-and-cqrs.md`, and there is a `cheatsheets/answer-scripts.md`. So publishing
it does the *opposite* of what they claim: a BofA reviewer clicks the link and reads interview-prep
notes named after their own job description, with an answer-script file. That is not evidence of
building; it is evidence of cramming, aimed at them, discoverable. **The Executor's inversion is
correct, and the Expansionist's derived conclusions — push for a higher band, "Apple recruited him out
of a bank" (it was a TEKsystems placement) — inherit the error and are struck.**

Second clash, on the degree timeline. The Contrarian escalates the overlapping BSc/MSc dates to
offer-rescission risk. Reviewer 2 dissolves it: Georgia Tech is OMSCS — online, part-time, explicitly
designed for working engineers. The overlap is normal and needs one word on the CV, not a strategy. What
the Contrarian is right about is narrower and still serious: "6+ years" against a start date of Apr 2022
is a quantified claim HR can disprove.

Third clash, and the one nobody put on the table: **apply now, or apply after the artifact exists?**
Bank requisitions are team-specific and rejection commonly blocks reapplication to the same desk for
6–12 months. Four advisors implicitly assume "apply now." I rule against them.

### Blind spots the council caught

- At three months' current tenure, **the recruiter screen is the filter, not the CV**. A BofA GMT
  Singapore referral bypasses ATS entirely and outranks every wording edit on this page.
- The 245 Q&A are worthless as a CV line (invisible to a recruiter) but valuable as the
  **specification** for the artifact — the design work is already done, which is why two days is
  realistic.
- Even after adding code, this repo cannot be published. Directory names give away that it was built
  from one employer's JD. He needs a clean, separate repository with no BofA fingerprints.
- The **cover letter** is the only place a gap or a three-month exit can be stated honestly without
  weakening the CV.
- The 1.2 req/sec division ("100K+ daily transactions at sub-200ms") is the sharpest single catch in the
  file. Any reviewer who divides finds it.

### The recommendation

Adopt the Executor's plan, resequenced with the referral path in front.

**Tonight (90 min).** Delete both placeholders — put real GitHub and LinkedIn URLs or remove the links.
"6+ years" → "4+ years." Rewrite the summary so the first eight words are Java/Scala and
post-trade/equities; Go becomes a subordinate clause. Reorder the skills line: Java, Scala, then the
rest. Label "Contract engagement via TEKsystems" and "via LTIMindtree." Mark the MSc "(OMSCS,
part-time)." Cut: "100K+ daily transactions," "200% performance improvement," "97%→99.8% uptime." Keep
$2B notional, sub-200ms (state which percentile and which hop), 2wk→3day, and 8s→4.8s stated as "40%
reduction" — an honest modest number survives a rigour shop; a suspicious huge one doesn't. Strike
"two-tower retrieval," "dual-hat," "inference footprints." Reduce "Architected" to one use.

**Tonight, in parallel.** Start the referral hunt: BofA GMT Singapore engineers on LinkedIn, ex-Citi
colleagues, the LTIMindtree network. This takes 1–2 weeks to land — exactly the artifact window. **Do
not submit a cold application yet.**

**This weekend (2 days).** Build the artifact in a **clean new public repo**: a small Scala
event-sourced trade-settlement service — commands, immutable append-only event log, a CQRS projection
for positions, one Cucumber `.feature` file, Gradle build, test-first commit history, README explaining
the settlement model. Use the quiz notes as the spec. This is the only item that converts a claim into
evidence, and it makes the CV honest: a Projects entry, not a keyword.

**Next week.** Apply through the referral, with a cover letter that names the event-sourcing gap and
points at the repo.

Do not push for a higher band. Ask the recruiter what band the req is — at 4y4mo against "coach and
mentor team," level may be the binding constraint, and no wording fixes that.

### The one thing to do first

Open `cv.tex`, delete `yourusername` and `yourprofile` from line 61, and change "6+ years" to "4+
years" on line 67. Ten minutes. Everything else on this page is optimisation; those two are a document
that currently disqualifies him on sight and a number that could cost him a signed offer.

### Information needed from him, ranked

1. Is there any referral path into BofA GMT Singapore, and is the req still open?
2. What band/grade is it?
3. Contract end dates and notice period for both agency engagements — the "contract engagement" label
   must be literally true — and what leaving Apple at month 3 costs contractually.
4. Any production Scala *service* code, not Spark? Any Kafka, immutable audit-trail, or integration-bus
   work at Citi that is genuinely event-sourcing-adjacent?
5. Did Citi touch fixed income at all, or equities/commodities only?
6. Peak TPS, and what the sub-200ms figure actually measures.
7. Gradle vs Maven, Jenkins vs TeamCity, any Ansible/Artifactory.
8. Work authorisation independent of sponsorship.
9. Will Apple and Citi HR confirm the exact titles and dates as written?
10. One truthful sentence per role on why he left.
11. Has he already applied to, or been rejected by, this desk?
