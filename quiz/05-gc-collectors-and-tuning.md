# Quiz 05 — Collectors, Flags & Reading GC Logs

16 questions. The applied half of GC. Target: 20 min.

---

### 1. Name the collectors available in JDK 21 and when you'd pick each.

<details><summary>Answer</summary>

| Collector | Flag | Pauses | Best for |
|---|---|---|---|
| **Serial** | `-XX:+UseSerialGC` | long, single-threaded | tiny heaps (< ~100 MB), single-core, CLI tools, cheap containers, minimal footprint |
| **Parallel** ("throughput") | `-XX:+UseParallelGC` | STW but multi-threaded | batch/ETL, anything where total throughput beats tail latency |
| **G1** (default since JDK 9) | `-XX:+UseG1GC` | target-driven, ~10–200 ms | the general-purpose default; heaps ~2–32 GB; balanced |
| **ZGC** | `-XX:+UseZGC` | **< 1 ms** | latency-sensitive services, large heaps (up to 16 TB) |
| **Shenandoah** | `-XX:+UseShenandoahGC` | ~ms | same niche as ZGC; Red Hat-originated, strong on smaller heaps too |
| **Epsilon** | `-XX:+UseEpsilonGC` | n/a — never collects | benchmarking, short-lived jobs that never fill the heap, GC-overhead attribution |

**Removed:** CMS (deprecated JDK 9, **removed JDK 14**). Don't propose it. If someone mentions `-XX:+UseConcMarkSweepGC` in a config, that config predates JDK 14 and won't even boot.

**JDK 21 specifically:** ZGC became **generational** (`-XX:+ZGenerational`, JEP 439) — a big deal, since non-generational ZGC's weakness was throughput and footprint on high-allocation-rate workloads. In JDK 21 you opt in; in 23+ generational is the default and non-generational was removed. Knowing this is a strong currency signal.

Selection heuristic to state: **start with the default (G1). Change it only with a measurement that says why.** Move to ZGC when the p99 pause budget is single-digit ms or the heap is > ~32 GB; move to Parallel when it's a batch job and you want the last 10% of throughput; move to Serial when the container is tiny.
</details>

---

### 2. Explain G1's design. Why "garbage-first"?

<details><summary>Answer</summary>

The heap is divided into ~2048 equal-sized **regions** (1–32 MB, `G1HeapRegionSize`). Each region is *dynamically* labelled **eden**, **survivor**, **old**, or **humongous** — generations are logical sets of regions, not contiguous address ranges. That's the key structural difference from Parallel/Serial.

**"Garbage-first"** = during marking G1 tracks the **liveness of each region**, then evacuates the regions with the **most garbage and least live data first** — best reclaimed-bytes per unit of copying work. That's what lets it hit a pause target: it picks a **collection set (CSet)** sized to fit the predicted pause budget, using a model built from observed copy costs.

Cycle:
1. **Young collections** — STW, evacuate eden + survivors into new survivor/old regions.
2. When old-gen occupancy crosses **IHOP** (`InitiatingHeapOccupancyPercent`, default 45, adaptive by default via `G1UseAdaptiveIHOP` — so 45 is the *initial* threshold, not a fixed operating point. **Renamed `G1IHOP` from JDK 27**, with the old name a deprecated alias: deprecated 27, obsolete 28, expired 29. No GA JDK up to 26 is affected, so it's forward-looking currency) → start a **concurrent marking cycle**: initial mark (piggybacked on a young pause), concurrent mark (SATB), remark (STW), cleanup (STW).
3. **Mixed collections** — young regions **plus** the highest-garbage old regions, spread over several pauses.
4. **Full GC** — the STW compacting fallback when the above fails. Since JDK 10 it's at least parallel.

Extras to name: **remembered sets** per region (needed because G1 collects arbitrary region sets), **eager reclaim** of humongous objects, **string dedup** (`-XX:+UseStringDeduplication`), and the fact that G1's remembered sets + card table cost **~10% of heap in overhead**.

Why it's the default: it degrades gracefully, needs almost no tuning, handles big heaps without CMS's fragmentation cliff, and gives you a *knob you actually understand* (`MaxGCPauseMillis`).
</details>

---

### 3. What does `-XX:MaxGCPauseMillis` actually do? What happens if you set it to 10?

<details><summary>Answer</summary>

It's a **target/hint**, not a guarantee. G1 uses it to choose how many regions go into the collection set: it predicts copy cost from historical data and adds regions until the predicted pause approaches the target. **200 ms under G1** — and note the nuance: the *declared* HotSpot default is `max_uintx-1` (effectively unset), and G1 sets 200 at collector initialisation if the flag is untouched. So it is 200 only because G1 is the default collector on server-class hardware; under Serial, Parallel or ZGC it is `max_uintx-1`. Verified on JDK 21.0.11 with `-XX:+PrintFlagsFinal`.

Setting it very low (say 10 ms) does *not* give you 10 ms pauses. It makes G1:
- Pick **very small collection sets**, so each pause collects little.
- Effectively **shrink the young generation** (young size is adaptive and bounded by `G1NewSizePercent`/`G1MaxNewSizePercent`), so collections become **far more frequent**.
- Pay fixed per-GC overhead (root scanning, safepoint, remembered-set work) many more times → **throughput drops**, sometimes badly.
- Promote more aggressively (objects get less time to die in young) → **more old-gen pressure**, more concurrent cycles, and a higher chance of **evacuation failure** → a full GC of hundreds of ms. You can end up with *worse* tail latency than the default.

The right answer: **if you need single-digit-millisecond pauses, use ZGC** — it's designed for it — rather than torturing G1. And whatever you set, set `-Xms == -Xmx` and leave `NewRatio`/`NewSize`/`SurvivorRatio` alone, because pinning young size fights G1's pause-target machinery (explicitly setting `-Xmn` disables the adaptive young sizing entirely).
</details>

---

### 4. How does ZGC achieve sub-millisecond pauses?

<details><summary>Answer</summary>

Two mechanisms:

**1. Coloured pointers.** ZGC stores metadata **in unused high bits of the 64-bit reference itself** — marked0, marked1, remapped, finalizable. The bits say "what do I know about the object this points to". (This is why ZGC requires 64-bit and **cannot use compressed oops** — a real footprint cost, and a great follow-up detail. It also uses multi-mapped virtual memory so the same object is reachable at several addresses with different colours.) JDK 21's generational ZGC switched to **coloured *stores*** / store barriers for the generational part.

**2. Load barriers.** Every reference **load** goes through a barrier that checks the colour bits. If the pointer is "bad" (stale colour), the barrier fixes it *on the spot* — marks the object, or looks up its new address in a forwarding table and **self-heals** the reference. Because the barrier repairs references lazily as the application touches them, ZGC can **relocate objects concurrently** with the application running.

Consequence: **the pauses are only for root scanning and phase transitions** — work proportional to the number of **threads/roots**, not to the heap or the live set. Hence pause times that are flat from a 1 GB heap to a 16 TB heap, and sub-millisecond in practice.

Costs to state honestly:
- **No compressed oops** → 8-byte references, bigger heaps.
- **Load barrier on every reference read** → throughput tax (single-digit percent, but it's on the hottest operation in the language).
- **Higher footprint** — needs relocation headroom and forwarding tables. Historically 15–25% more heap than G1 for the same workload.
- Non-generational ZGC struggled with high allocation rates (all marking work was full-heap). **Generational ZGC in JDK 21 fixes exactly this** and is why it became the default in later versions.
</details>

---

### 5. Read this log line. What happened?

```
[15.234s][info][gc] GC(42) Pause Young (Normal) (G1 Evacuation Pause) 2048M->1856M(4096M) 18.451ms
```

<details><summary>Answer</summary>

- `15.234s` — uptime timestamp.
- `GC(42)` — 42nd collection; the id ties together multi-line phases of one cycle.
- `Pause Young` — stop-the-world young collection. `(Normal)` distinguishes it from `(Concurrent Start)`, `(Prepare Mixed)`, `(Mixed)`.
- `(G1 Evacuation Pause)` — **the cause**: routine eden-full evacuation. Healthy. (Contrast: `(G1 Humongous Allocation)`, `(Metadata GC Threshold)`, `(System.gc())`, `(GCLocker Initiated GC)` — each tells a different story.)
- `2048M->1856M(4096M)` — heap used **before → after**, with total heap. So this GC reclaimed **192 MB**; 1856 MB survived.
- `18.451ms` — pause duration.

**Assessment:** an 18 ms young pause is fine. The thing worth a raised eyebrow is that it reclaimed only 192 MB out of 2048 MB used — a low garbage-to-live ratio for a *young* collection, which suggests either a large genuine live set in old gen (fine) or heavy survival/promotion (not fine). To decide, look at the sequence: if post-GC occupancy creeps up every cycle and never drops after a concurrent cycle, you have growth. Add `-Xlog:gc+heap=info` to see per-generation breakdown and `gc+age=trace` for the tenuring histogram.

Also worth saying: **`-Xlog:gc` alone gives one line per GC and is cheap enough to run in production always.** There's no excuse for not having it.
</details>

---

### 6. What's the modern GC logging setup?

<details><summary>Answer</summary>

JDK 9 replaced the old ad-hoc flags with **unified logging** (`-Xlog`). The old ones (`-XX:+PrintGCDetails`, `-XX:+PrintGCDateStamps`, `-Xloggc:file`) are **removed** in JDK 9+ — quoting them dates you.

Always-on production setting:
```
-Xlog:gc*:file=/var/log/app/gc.log:time,uptime,level,tags:filecount=10,filesize=20M
```
- `gc*` — all `gc`-prefixed tags at default (info) level. Use plain `gc` for one line per collection if you want it minimal.
- `:file=...` — output. `:stdout` if your platform aggregates logs.
- **decorators** `time,uptime,level,tags` — wall-clock time is essential for correlating with request-latency graphs; uptime alone is useless in an incident.
- **rotation** `filecount`/`filesize` — otherwise you fill the disk.

Targeted additions when investigating:
```
-Xlog:gc+heap=debug          # per-generation / per-region occupancy
-Xlog:gc+age=trace           # tenuring age histogram (premature promotion)
-Xlog:gc+humongous=debug     # humongous allocations
-Xlog:gc+ergo=debug          # why the collector made its sizing decisions
-Xlog:safepoint              # time-to-safepoint (see quiz 04 Q7)
-Xlog:gc+cpu                 # user/sys/real per GC
-Xlog:gc+ref=debug           # reference processing time (Soft/Weak/Phantom/Cleaner)
```

Companion always-on flags:
```
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/app/
-XX:+ExitOnOutOfMemoryError
-XX:NativeMemoryTracking=summary      # small overhead, huge diagnostic value
-XX:StartFlightRecording=maxsize=256m,maxage=6h,settings=profile
```
JFR is the one to emphasise: continuous, ~1–2% overhead, and it captures GC, allocation, lock contention, and safepoints together with a timeline you can align to your traces. `jcmd <pid> JFR.dump` when something goes wrong. Analyse with GCeasy/GCViewer for logs, JMC for JFR.
</details>

---

### 7. Which GC flags do you actually set in production, and which do you leave alone?

<details><summary>Answer</summary>

**Set:**
```
-XX:MaxRAMPercentage=70              # containers: survives resizing
-XX:InitialRAMPercentage=70          # == max, avoid resize churn
   (or -Xms4g -Xmx4g on fixed hosts)
-XX:+UseG1GC                         # or ZGC; explicit beats implicit
-XX:MaxMetaspaceSize=256m            # cap the unbounded default
-XX:+AlwaysPreTouch                  # latency-sensitive services
-Xlog:gc*:file=...                   # always
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=...
-XX:+ExitOnOutOfMemoryError
-XX:ActiveProcessorCount=N           # only if the cgroup-derived value is wrong
-XX:MaxGCPauseMillis=...             # only if you have a real SLO
```

**Leave alone unless a measurement demands it:** `NewRatio`, `SurvivorRatio`, `MaxTenuringThreshold`, `-Xmn`, `G1HeapRegionSize` (except for a humongous-object problem), `InitiatingHeapOccupancyPercent`, `ParallelGCThreads`/`ConcGCThreads`, `G1ReservePercent`, `UseStringDeduplication`, `SoftRefLRUPolicyMSPerMB`.

The reasoning to give out loud: **modern collectors are adaptive, and most hand-set generation flags actively fight the ergonomics.** Setting `-Xmn` under G1 disables its adaptive young sizing; setting `SurvivorRatio` overrides the dynamic tenuring computation. A flag you set from a 2011 blog post is a flag that will be wrong on your workload.

And the priority order for a GC problem: **(1) fix the allocation/retention in the code, (2) give it more heap, (3) change the collector, (4) tune flags.** Most people start at 4. Say that explicitly — it's the single best signal you can give in a GC discussion.
</details>

---

### 8. Your GC log shows young pauses climbing from 10 ms to 300 ms over an hour. Diagnose.

<details><summary>Answer</summary>

Young pause cost ∝ **volume of live data copied** + root scanning + remembered-set work. Something is making survivors bigger or the scan more expensive.

Hypotheses, in order:
1. **Live set is genuinely growing** — a leak or a filling cache. More objects survive each young GC, so more copying. Check post-GC occupancy trend (quiz 04 Q22 step 1). **Most likely cause.**
2. **Promotion/survivor pressure** — survivor spaces too small, so the adaptive tenuring threshold collapsed to 1 and everything is copied to old immediately; old gen grows, remembered sets grow, and the young pause pays for scanning them. `-Xlog:gc+age=trace` shows the histogram.
3. **Old gen filling** → more dirty cards and bigger remembered sets to scan during young collections. Correlate with old-gen occupancy and check whether concurrent cycles are keeping up (`gc+heap`).
4. **Humongous allocations** appearing in the log → old gen fragmenting and filling from a path that skips eden.
5. **Thread count growing** — root scanning is proportional to stacks. An executor leak (quiz 02) would do it.
6. **Not GC at all** — the heap grew (if `Xms != Xmx`), or the *machine* changed: CFS throttling, a noisy neighbour, swapping (check `gc+cpu`: high `sys` time or `real >> user` means the OS is the problem, not the collector).

Method: get the log, plot pause vs post-GC-occupancy vs time. If pause tracks occupancy, it's retention (1/2/3). If occupancy is flat and pauses rise anyway, look at threads, humongous, or the environment. Then confirm with a class histogram before touching a single flag.
</details>

---

### 9. What's the difference between `-Xmx` and `-XX:MaxRAMPercentage`, and which do you use?

<details><summary>Answer</summary>

`-Xmx` is an **absolute** maximum heap size. `-XX:MaxRAMPercentage=70` is **relative to the memory the JVM believes is available** — which, with container support on, is the **cgroup memory limit**, not the host's RAM.

In containers, prefer the percentage:
- Resizing the pod's memory limit automatically resizes the heap. No image rebuild, no config drift, no "we bumped the container to 8 GB and the app still uses 2".
- It's the difference between one Helm value and two that must be kept in sync.

Gotchas:
- `MinRAMPercentage` does **not** mean "minimum heap". It's the fraction used when *total memory is small* (< ~250 MB). Use `InitialRAMPercentage` for the `-Xms` equivalent. This naming is a genuinely confusing wart and worth knowing.
- The default `MaxRAMPercentage` is **25%**, which is very conservative — deliberate, because the JVM can't know your non-heap footprint. On a service container, 50–75% is typical, and you validate it with NMT/RSS rather than guessing.
- Explicit `-Xmx` always wins over the percentage flags.
- Neither bounds total RSS — see quiz 04 Q11. **Percentage flags do not protect you from a container OOM-kill**, they just make the heap portion proportional.
</details>

---

### 10. How do you measure allocation rate, and why does it matter more than total allocations?

<details><summary>Answer</summary>

**Measure:**
- From the GC log: `Σ (heap_used_before[i] − heap_used_after[i−1])` over the window ÷ elapsed. Eden filled per unit time. GCeasy/GCViewer compute it for you.
- **JFR**: `jdk.ObjectAllocationInNewTLAB` / `ObjectAllocationSample` events give you the rate **plus the allocating stack traces** — which is what you actually need.
- **async-profiler `-e alloc`** — a flame graph of allocation by call site. The single best tool for "where is the garbage coming from".
- `ThreadMXBean.getThreadAllocatedBytes(tid)` for per-thread attribution in a targeted test.

**Why the rate matters:** it determines **GC frequency** (`eden_size / alloc_rate` = time between young collections). A high rate means:
- Frequent young collections, each with fixed overhead (safepoint, root scan) — so throughput drops even if each pause is short.
- Objects get **less wall-clock time to die** before eden fills, so more of them are still live at collection time → more copying → **more promotion** → old-gen pressure and concurrent cycles. This is the mechanism by which a pure young-gen allocation problem becomes an old-gen pause problem.
- Concurrent collectors can be **outrun**, giving evacuation failure and a full GC.

Total allocations over a process's lifetime tells you nothing; **bytes per second, and what fraction survives**, tells you everything. Two knobs follow: reduce the rate (fix the code) or increase eden (more heap) so the same rate collects less often with a better garbage-to-live ratio.

This is precisely where the Scala conversation lands — see `quiz/06` Q10.
</details>

---

### 11. What is string deduplication and string interning? Are they the same?

<details><summary>Answer</summary>

**No.**

**`String.intern()`** — application-level, explicit. Puts the `String` **object** in the JVM's string table and returns the canonical instance, so `==` works. Historically in PermGen (a leak risk); since JDK 7 it's on the heap and the table is resizable (`-XX:StringTableSize`). Calling it on unbounded user input is a classic mistake: the table is a **GC root-adjacent structure** with hashing costs, and you've made an unbounded cache you can't evict. Use a `HashMap` or Guava `Interner` if you want canonicalisation you control.

**`-XX:+UseStringDeduplication`** — collector-level, transparent (G1 originally, now also Shenandoah/ZGC in recent JDKs). During marking, the GC finds `String`s with equal contents and points them at a **shared `char[]`/`byte[]`**. The `String` objects remain distinct (so `==` is unaffected and semantics don't change); only the backing arrays are shared. Zero code change.

When dedup helps: heaps full of duplicated strings — JSON/XML field names, enum-like values, tenant ids, log messages, database column values. Can be a double-digit percentage of heap in some services. Cost: extra work during marking plus the dedup table.

Related: **compact strings** (JDK 9+, on by default) store Latin-1 strings as `byte[]` instead of `char[]`, halving the footprint of most strings. That was a bigger win than either of the above and required no flags.
</details>

---

### 12. `jstat`, `jcmd`, `jmap`, `jstack`, `jinfo`, JFR — what do you use for what?

<details><summary>Answer</summary>

**`jcmd <pid> <command>`** is the modern front-end and should be your default; the others are legacy or special-purpose.
```
jcmd <pid> help                      # list available commands
jcmd <pid> GC.heap_info              # current occupancy
jcmd <pid> GC.class_histogram        # live objects by class  (cheap!)
jcmd <pid> GC.heap_dump /tmp/h.hprof # replaces jmap -dump
jcmd <pid> Thread.print              # replaces jstack
jcmd <pid> Thread.dump_to_file -format=json /tmp/t.json   # includes virtual threads
jcmd <pid> VM.native_memory summary  # NMT; needs the startup flag
jcmd <pid> VM.flags -all             # effective flag values
jcmd <pid> VM.system_properties
jcmd <pid> JFR.start / JFR.dump / JFR.stop
```

- **`jstat -gcutil <pid> 1s`** — still the fastest way to *watch* GC live: percentage occupancy of each space, GC counts and cumulative times, one line per second. No setup, works over SSH. Great first look.
- **`jstack`** — thread dump. Take **three, ~5 s apart**, and diff: a thread stuck in the same frame across all three is your problem. Detects Java-level deadlocks automatically.
- **`jmap`** — heap dump / histogram. Superseded by `jcmd`. Remember `-dump:live` triggers a full GC first.
- **`jinfo`** — read/write manageable flags at runtime (`jinfo -flag +PrintGC <pid>`). Only some flags are manageable.
- **JFR** — the one to advocate for. Always-on, low overhead, and it correlates GC, allocation sites, lock contention, safepoints, exceptions, and I/O on a single timeline. `-XX:StartFlightRecording=maxsize=256m,maxage=6h`. Analyse in JDK Mission Control.
- **async-profiler** — not in the JDK, but the practical answer for CPU and allocation flame graphs without safepoint bias.

Say the operational caveat: **heap dumps pause the JVM and are the size of your live set.** In production, prefer `class_histogram` → JFR → dump-from-a-depooled-canary, in that order.
</details>

---

### 13. What is `-XX:+UseCompressedOops` doing to your object layout? Compute the size of a small object.

<details><summary>Answer</summary>

Layout of any object (compressed oops, 64-bit):
```
mark word          8 bytes   (identity hash, lock state, GC age bits)
klass pointer      4 bytes   (compressed)
[fields]           ...       (reordered by the JVM: longs/doubles, ints, shorts, bytes, refs)
[padding]                    (to an 8-byte boundary)
```
So the header is **12 bytes**, and every object is **padded to a multiple of 8**.

Worked examples:
- `class Empty {}` → 12 + 4 padding = **16 bytes**.
- `Integer` (one `int`) → 12 + 4 = **16 bytes** to hold 4 bytes of payload. **4× overhead.**
- `Long` → 12 + 8 = 20 → padded to **24 bytes**.
- `new int[10]` → 12 header + 4 length + 40 = 56 → **56 bytes**.
- A `String` (JDK 9+) → the `String` object (12 + 4 ref + 4 hash + 1 coder + pad = 24) **plus** a separate `byte[]` (16 + n). So `"hi"` ≈ **48 bytes** total.
- `HashMap.Node` → 12 + 4 (hash) + 4 (key ref) + 4 (value ref) + 4 (next ref) = **32 bytes** *per entry*, before the entry's key and value. A `HashMap<Integer,Integer>` with 1 M entries costs ~32 MB of nodes + ~32 MB of boxed keys/values to store 8 MB of data.

Without compressed oops (heap ≥ 32 GB) the klass pointer and every reference double, so that `HashMap.Node` becomes 48 bytes. That's the concrete version of quiz 04 Q10's cliff.

Tools: **JOL** (`org.openjdk.jol`) prints exact layouts — `ClassLayout.parseClass(X.class).toPrintable()`. Great for settling arguments.

This computation is the setup for the Scala boxing conversation: `Option[Int]` containing a boxed `Integer` is **16 (Some) + 16 (Integer) = 32 bytes** to carry one `int`.
</details>

---

### 14. When is Parallel GC the right choice over G1 in 2026?

<details><summary>Answer</summary>

When you genuinely don't care about individual pause lengths, only total work done:

- **Batch and ETL jobs** — a 2-second pause in a 40-minute Spark task is irrelevant; 10% more throughput is not. This is the main real case, and it's relevant if the Scala side runs Spark.
- **CI builds, compilers, short-lived CLI processes** — no user is waiting on a p99.
- **Small containers where G1's ~10% metadata overhead matters** (though Serial may be better still below ~100 MB).
- **Benchmark baselines** — Parallel is the simplest, most predictable throughput reference.

Why it can win: no concurrent-phase CPU competing with the app, **cheaper write barriers** (plain card marking, no SATB pre-barrier, no remembered sets), no floating garbage, and simple contiguous generations with a straightforward bump allocator. Typically **5–15% better throughput** than G1 on allocation-heavy work.

Why not for a service: full-heap STW compaction. On a 4 GB heap that's ~1 second; on 32 GB, tens of seconds. No pause target to bound it.

Bonus flag if you use it: `-XX:+UseAdaptiveSizePolicy` is on by default and will retune generations toward `MaxGCPauseMillis`/`GCTimeRatio` — `-XX:GCTimeRatio=99` means "spend ≤1% of time in GC" and is the throughput-goal knob.
</details>

---

### 15. What's the relationship between heap size and GC frequency, pause time, and throughput?

<details><summary>Answer</summary>

**Bigger heap:**
- **Fewer collections** — eden is bigger, so it takes longer to fill at the same allocation rate. Frequency ∝ 1/heap.
- **Better garbage-to-live ratio** — objects have more wall-clock time to die before eden fills, so a *smaller fraction* survives. Copying cost per collection can actually *fall*.
- **Higher throughput**, for both reasons above. GC cost per unit of application work goes down roughly hyperbolically.
- **Longer *full* GC pauses** (mark and compact scale with live set/heap), and slightly longer young pauses if the survivor volume grows.
- **Less headroom pressure** → fewer evacuation failures and concurrent-mode failures, which is where the really bad pauses live.

**Smaller heap:** the reverse — frequent short collections, more promotion, more risk of the collector being outrun, worse throughput, but a smaller footprint and shorter worst-case full GC.

The non-obvious part: for a **concurrent** collector (G1/ZGC), **more heap improves tail latency**, because the pathological pauses come from running out of room to evacuate into, not from the heap being large. So "shrink the heap to shorten pauses" is usually backwards for G1/ZGC and only correct for Parallel/Serial.

Practical: **the throughput curve is steep at first and then flat.** Going from 1 GB to 4 GB on a service might cut GC overhead from 15% to 3%; going from 4 GB to 16 GB might take it to 2% while costing you 12 GB of RAM per instance and slower full GCs. Find the knee empirically, and remember the 32 GB compressed-oops cliff.
</details>

---

### 16. Sketch a GC tuning process for a service missing its p99 latency SLO.

<details><summary>Answer</summary>

**0. Establish that GC is the problem.** Get `-Xlog:gc*` and JFR. Compare the pause distribution against the latency gap. If your p99 is 500 ms over budget and your worst pause is 20 ms, **GC is not your problem** — go look at queueing, locks, and downstreams (quiz 02 Q15). Half of "GC tuning" requests end here, and saying so is the most valuable thing you can contribute.

**1. Define the target.** "p99 < 50 ms" is actionable; "reduce GC" is not. Note the pause budget you can spend, and check for time-to-safepoint separately (`-Xlog:safepoint`) — it's invisible in GC pause times.

**2. Characterise, don't guess.** From the log/JFR, extract: allocation rate; post-GC live set trend; young pause distribution; frequency; promotion rate; count and cause of full GCs / evacuation failures; concurrent-cycle duration vs allocation rate; GC CPU as a share of total.

**3. Fix the application first.** Almost always the biggest win, and it's free of operational risk.
- Kill the top allocation sites (async-profiler `-e alloc`). Usually: logging in a hot loop, string concatenation/formatting, boxing, defensive copies, oversized collections, JSON serialisation, an accidental `O(n²)`.
- Bound the caches. Eliminate retention. Fix the leak.
- Avoid giant arrays (humongous allocations).

**4. Size the heap.** `-Xms == -Xmx`, enough headroom that G1 isn't running near occupancy limits. Validate total RSS with NMT against the container limit. Usually the second-biggest win and one flag.

**5. Change collector if the SLO demands it.** Single-digit-ms pause budget or > 32 GB heap → **Generational ZGC**. Batch → Parallel. Otherwise stay on G1.

**6. Only now, tune flags.** One at a time, with a load test that reproduces production traffic shape, and a measurement of both latency *and* throughput after each change. Most likely useful: `MaxGCPauseMillis`, `G1HeapRegionSize` (humongous), metaspace cap, `AlwaysPreTouch`.

**7. Verify and lock in.** Compare distributions, not means. Record the reasoning next to the flags in source control, so the next person knows why each one is there — an untraceable flag is worse than no flag.

The one-liner: **measure, fix the code, then the heap, then the collector, then the flags — and prove GC was the problem before you start.**
</details>

---

## Score yourself

Q1, Q7, and Q16 are the ones most likely to come up verbatim. Q5 (reading a log line)
is the most likely *live* exercise — practise on the output of `problems/gc-labs/L01`.
