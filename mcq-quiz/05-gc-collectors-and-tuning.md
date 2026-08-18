# MCQ 05 — Collectors, Flags & Reading GC Logs

15 questions. Source: [`quiz/05`](../quiz/05-gc-collectors-and-tuning.md).

---

### Q1. Which collector was **removed** from the JDK?

- **A.** Serial
- **B.** Parallel
- **C.** CMS
- **D.** Shenandoah

<details><summary>Answer</summary>

**C.** CMS was deprecated in JDK 9 and **removed in JDK 14**. Don't propose it — a config
containing `-XX:+UseConcMarkSweepGC` won't even boot on a modern JDK.

Why it went: it swept in place without compacting, so it fragmented, eventually hit a promotion
failure / concurrent mode failure, and fell back to a **single-threaded stop-the-world compacting
full GC** measured in tens of seconds.

→ `quiz/05` Q1
</details>

---

### Q2. Heap is 8 GB, the service is latency-sensitive with a p99 budget of ~50 ms. Which collector?

- **A.** G1
- **B.** Parallel
- **C.** Serial
- **D.** Epsilon

<details><summary>Answer</summary>

**A.** G1 is the default and the right starting point for heaps roughly 2–32 GB with a pause target
you can reason about. 50 ms is comfortably achievable.

- **C** is for tiny heaps (<~100 MB) and CLI tools.
- **B** maximises throughput with unbounded STW compaction — right for batch, wrong for a p99 SLO.
- **D** never collects; it's for benchmarking and GC-overhead attribution.

You'd move to **Generational ZGC** if the budget were single-digit milliseconds or the heap were
past ~32 GB.

→ `quiz/05` Q1
</details>

---

### Q3. Why is G1 called "garbage-first"?

- **A.** It collects the youngest regions first
- **B.** It runs a full GC before any young collection
- **C.** It prioritises collecting garbage over serving application threads
- **D.** It evacuates the regions with the most garbage and least live data first — best reclaimed bytes per unit of copying work

<details><summary>Answer</summary>

**D.** During marking G1 tracks per-region liveness, then picks a **collection set** sized to fit
the predicted pause budget, choosing the highest-garbage regions first. That's what lets it target
a pause time.

Structure: ~2048 equal-sized regions (1–32 MB), each *dynamically* labelled eden, survivor, old or
humongous — generations are logical sets of regions, not contiguous address ranges.

→ `quiz/05` Q2
</details>

---

### Q4. You set `-XX:MaxGCPauseMillis=10`. What actually happens?

- **A.** Pauses are guaranteed under 10 ms
- **B.** G1 picks tiny collection sets and effectively shrinks young gen, so collections become far more frequent, throughput drops, and promotion increases — tail latency can get *worse*
- **C.** The flag is ignored below 50 ms
- **D.** G1 switches automatically to ZGC

<details><summary>Answer</summary>

**B.** It's a **target, not a guarantee** — and note it's 200 **only under G1**: the declared
HotSpot default is `max_uintx-1`, and G1 injects 200 at initialisation if you left the flag alone
(which is why `PrintFlagsFinal` still reports it as `{default}`).

Smaller collection sets mean more frequent collections,
each paying fixed overhead (safepoint, root scan). Objects get less time to die in young, so
promotion rises, old gen fills, concurrent cycles multiply, and you raise the chance of
**evacuation failure** → a full GC of hundreds of ms.

The right answer: **if you need single-digit-ms pauses, use ZGC** rather than torturing G1. And
leave `NewRatio`/`SurvivorRatio`/`-Xmn` alone — setting `-Xmn` explicitly *disables* G1's adaptive
young sizing.

→ `quiz/05` Q3
</details>

---

### Q5. How does ZGC achieve sub-millisecond pauses?

- **A.** Coloured pointers plus load barriers, so references are repaired lazily and objects can be relocated concurrently
- **B.** By running the collector at a higher thread priority
- **C.** By never compacting the heap
- **D.** By collecting only the young generation

<details><summary>Answer</summary>

**A.** Metadata lives in unused high bits of the 64-bit reference. Every reference **load** goes
through a barrier that checks the colour and, if stale, fixes it on the spot — **self-healing** the
reference. Because the barrier repairs references as the application touches them, ZGC relocates
objects concurrently.

The result: **pauses are proportional to the number of roots/threads, not the heap or live set** —
so they're flat from 1 GB to 16 TB.

The costs, stated honestly: **no compressed oops** (ZGC needs the pointer bits, so 8-byte
references), a load-barrier throughput tax on the hottest operation in the language, and higher
footprint for relocation headroom.

→ `quiz/05` Q4
</details>

---

### Q6. `[15.234s][info][gc] GC(42) Pause Young (Normal) (G1 Evacuation Pause) 2048M->1856M(4096M) 18.451ms` — how much was reclaimed?

- **A.** 2048 MB
- **B.** 1856 MB
- **C.** 192 MB
- **D.** 4096 MB

<details><summary>Answer</summary>

**C.** `before->after(total)`: 2048 − 1856 = **192 MB** reclaimed, 1856 MB survived, out of a
4096 MB heap. Pause 18.451 ms, which is fine.

The thing worth an eyebrow: only 192 of 2048 MB reclaimed is a low garbage-to-live ratio for a
*young* collection — suggesting either a large genuine live set or heavy survival/promotion. To
decide, look at the sequence over time.

**`(G1 Evacuation Pause)` is the cause, and the cause is the diagnosis.** Contrast
`(G1 Humongous Allocation)`, `(Metadata GC Threshold)`, `(System.gc())`.

→ `quiz/05` Q5
</details>

---

### Q7. Which GC logging flag is safe and useful to run always in production?

- **A.** `-XX:+PrintGCDetails`
- **B.** `-Xlog:gc*:file=/var/log/gc.log:time,uptime,level,tags:filecount=10,filesize=20M`
- **C.** `-verbose:gc` only during incidents
- **D.** None — GC logging is too expensive for production

<details><summary>Answer</summary>

**B.** Unified logging (JDK 9+) with a file, **wall-clock time** in the decorators (essential for
correlating with latency graphs — uptime alone is useless in an incident), and **rotation** so you
don't fill the disk.

- **A** was **removed** in JDK 9; quoting it dates you.
- **D** is wrong: `-Xlog:gc` is one line per collection and cheap enough that there's no excuse not
  to have it.

Companion always-on flags: `-XX:+HeapDumpOnOutOfMemoryError`, `-XX:+ExitOnOutOfMemoryError`,
`-XX:NativeMemoryTracking=summary`, and `-XX:StartFlightRecording=maxsize=256m,maxage=6h`.

→ `quiz/05` Q6
</details>

---

### Q8. Which flag should you generally **not** set by hand?

- **A.** `-XX:MaxRAMPercentage`
- **B.** `-XX:MaxMetaspaceSize`
- **C.** `-XX:+HeapDumpOnOutOfMemoryError`
- **D.** `-XX:SurvivorRatio`

<details><summary>Answer</summary>

**D.** Modern collectors compute survivor sizing and the dynamic tenuring threshold adaptively, and
a hand-set `SurvivorRatio` **overrides that machinery**. Same for `NewRatio`,
`MaxTenuringThreshold`, `-Xmn`, `ParallelGCThreads`, and `InitiatingHeapOccupancyPercent`.

**A**, **B** and **C** you *should* set: a percentage so the heap survives a container resize, a metaspace cap
because the default is unbounded, and heap-dump-on-OOM always.

**A flag you set from a 2011 blog post is a flag that will be wrong on your workload.**

→ `quiz/05` Q7
</details>

---

### Q9. Young pauses climb from 10 ms to 300 ms over an hour. Most likely cause?

- **A.** The JIT is deoptimising repeatedly
- **B.** The CPU is thermally throttling
- **C.** The live set is growing, so more data is copied each young collection
- **D.** Eden is too large

<details><summary>Answer</summary>

**C.** Young pause cost ∝ **volume of live data copied**, plus root scanning and remembered-set
work. A growing live set (a leak or a filling cache) means more survivors, so more copying.

Other real candidates, in order: survivor/promotion pressure (adaptive tenuring threshold collapsed
to 1, so everything goes straight to old); old gen filling, growing remembered sets to scan;
humongous allocations; **growing thread count** (root scanning is proportional to stacks — an
executor leak would do it); or the environment (CFS throttling, swapping — check `gc+cpu` for
`real >> user`).

Method: plot pause vs post-GC occupancy vs time. If pause tracks occupancy, it's retention.

→ `quiz/05` Q8
</details>

---

### Q10. In a container, why prefer `-XX:MaxRAMPercentage=70` over `-Xmx4g`?

- **A.** Resizing the pod's memory limit resizes the heap automatically — no image rebuild, no two values to keep in sync
- **B.** It produces a larger heap
- **C.** It bounds total RSS, unlike `-Xmx`
- **D.** `-Xmx` is ignored inside containers

<details><summary>Answer</summary>

**A.** One Helm value instead of two that must agree.

- **C** is the dangerous misconception: **neither bounds total RSS.** Percentage flags only make the
  *heap* portion proportional; metaspace, stacks, direct buffers and GC metadata are all still
  outside it.
- **D** — explicit `-Xmx` always wins over the percentage flags.

Two gotchas: the default `MaxRAMPercentage` is a very conservative **25%**; and `MinRAMPercentage`
does **not** mean "minimum heap" — it applies only when total memory is small. Use
`InitialRAMPercentage` for the `-Xms` equivalent.

→ `quiz/05` Q9
</details>

---

### Q11. Which metric drives GC *frequency*?

- **A.** Total allocations over the process lifetime
- **B.** Thread count
- **C.** The number of live objects
- **D.** Allocation rate in bytes per second, relative to eden size

<details><summary>Answer</summary>

**D.** Frequency ≈ `eden_size / allocation_rate`. Total lifetime allocations tell you nothing;
**bytes per second, and what fraction survives**, tells you everything.

High rate means frequent collections (each with fixed overhead), *and* objects get less wall-clock
time to die before eden fills — so more survive, more copying, **more promotion**. That's the
mechanism by which a pure young-gen allocation problem becomes an old-gen pause problem.

Measure with JFR's allocation events (which give you the **stacks**) or async-profiler `-e alloc`.

→ `quiz/05` Q10
</details>

---

### Q12. `String.intern()` vs `-XX:+UseStringDeduplication` — same thing?

- **A.** Yes, one is just the flag form of the other
- **B.** No — `intern()` canonicalises the `String` *object* at application level; dedup makes the collector share identical backing *arrays*, transparently
- **C.** No — dedup only works on literals
- **D.** Yes, but `intern()` is faster

<details><summary>Answer</summary>

**B.** `intern()` returns a canonical instance so `==` works; it's explicit and application-level.
Dedup is collector-level: during marking it points equal-content `String`s at a **shared byte
array**, leaving the `String` objects distinct — so `==` semantics are unchanged and no code changes.

The warning worth adding: **don't `intern()` unbounded user input.** The string table is a
JVM-level structure you can't evict from, so you've built an unbounded cache. Use a `HashMap` or
Guava `Interner`.

Related and bigger win: **compact strings** (JDK 9+, default) store Latin-1 as `byte[]`, halving
most strings' footprint with no flags at all.

→ `quiz/05` Q11
</details>

---

### Q13. Cheapest first step to investigate a suspected leak in production?

- **A.** `jcmd <pid> GC.class_histogram`
- **B.** `jcmd <pid> GC.heap_dump`
- **C.** Restart with a bigger heap and see if it recurs
- **D.** `jmap -dump:live` on every instance in the fleet

<details><summary>Answer</summary>

**A.** Live object counts by class, low impact, no file written. Often enough on its own — "three
million `LinkedHashMap$Entry`" points straight at a cache.

Escalation order: `class_histogram` → **JFR** (~1–2% overhead, gives allocation stacks and
old-object samples) → heap dump **from a depooled canary**.

- **B**/**D** pause the JVM for seconds and write a file the size of your live set. Verified: 130 MB
  for a 128 MB heap. Never blindly across a fleet.

→ `quiz/05` Q12, `quiz/04` Q22
</details>

---

### Q14. When is Parallel GC the right choice over G1 in 2026?

- **A.** Any latency-sensitive web service
- **B.** Heaps larger than 32 GB
- **C.** Batch/ETL work where a multi-second pause is irrelevant and you want the last 10% of throughput
- **D.** Never — it's obsolete

<details><summary>Answer</summary>

**C.** Parallel wins on throughput because there's no concurrent-phase CPU competing with the app,
its write barriers are **cheaper** (plain card marking, no SATB pre-barrier, no remembered sets), no
floating garbage, and simple contiguous generations. Typically 5–15% better than G1 on
allocation-heavy work.

Relevant if the Scala side runs **Spark** — a 2-second pause in a 40-minute task is irrelevant.

- **B** is where you'd want **ZGC**, not Parallel.

→ `quiz/05` Q14
</details>

---

### Q15. Correct order for tackling a GC-related latency problem?

- **A.** Tune flags → change collector → resize heap → fix code
- **B.** Prove GC is the problem → fix allocation/retention in the code → size the heap → change collector → tune flags
- **C.** Change collector → tune flags → resize heap
- **D.** Resize heap → tune flags → prove GC is the problem

<details><summary>Answer</summary>

**B.** And **step zero is the one people skip**: if p99 is 500 ms over budget and the worst pause is
20 ms, **GC is not your problem** — go look at queueing, locks and downstreams. Also check
time-to-safepoint separately, since it's invisible in GC pause times.

Then: fixing the code is the biggest win with zero operational risk; more heap is the second-biggest
and it's one flag; the collector only if the SLO demands it; flags last, one at a time, measuring
latency *and* throughput.

**Most people start at step 4.** Saying this order out loud is the single strongest signal you can
give in a GC conversation.

→ `quiz/05` Q16
</details>

---

## Score yourself

- **12+** — good.
- Q1, Q8 and Q15 are the ones most likely to come up verbatim. **Q6 (reading a log line) is the
  most likely live exercise** — practise on real output from
  `problems/gc-labs/L01AllocationRate.java`.

Numbers worth memorising: healthy GC overhead <5%; young pauses 1–20 ms; **`MaxGCPauseMillis`
200 ms *under G1* — the declared default is `max_uintx-1` and G1 injects 200 ergonomically**;
ZGC <1 ms; object header 12 bytes; reference 4 bytes compressed; allocation ~10 ns;
compressed-oops cliff ~32 GB.
