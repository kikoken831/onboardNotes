# Quiz 04 — GC Fundamentals

22 questions. Mechanisms and vocabulary. Collector-specific tuning is in `quiz/05`.
Target: 20 min.

---

### 1. What is the generational hypothesis, and what does the JVM build on top of it?

<details><summary>Answer</summary>

**Most objects die young.** Empirically, the vast majority of allocations become unreachable almost immediately (loop temporaries, StringBuilders, boxed values, iterators). A smaller coroll: **few references point from old objects to young ones**.

What the JVM builds on it:
- Split the heap into a small **young generation** (eden + two survivor spaces) and a larger **old generation**.
- Collect young frequently with a **copying collector**: trace from roots, copy the few survivors out, then declare the whole region free. Cost is proportional to the **survivors**, not to the garbage — so a young GC over 4 GB of dead objects is nearly free.
- Objects that survive enough young collections are **promoted** to old, which is collected rarely and by a different algorithm (mark-sweep-compact, or concurrent marking).
- The second half of the hypothesis (few old→young pointers) is what makes young GC possible without scanning the old gen — you only need to track the exceptions, via a **card table / remembered set**.

Consequence to state: **allocation is cheap; survival is expensive.** GC cost tracks live set and promotion rate, not allocation count. That reframes almost every "we allocate too much" conversation.
</details>

---

### 2. Walk through an allocation. What's a TLAB?

<details><summary>Answer</summary>

A **TLAB** (Thread-Local Allocation Buffer) is a chunk of eden handed to a single thread. Allocation inside it is a **pointer bump** — increment the top pointer, check against the end, done. A handful of instructions, no synchronisation, no CAS, and the JIT often inlines it. That's why "object allocation in Java is ~10 ns" is true.

The path:
1. JIT-emitted fast path: `if (tlab.top + size <= tlab.end) { obj = tlab.top; tlab.top += size; }`.
2. TLAB full → **slow path**: retire it and request a new TLAB from eden (this *does* need atomics). TLAB size is adaptive per thread (`-XX:TLABSize`, `-XX:+ResizeTLAB` on by default) based on that thread's allocation rate.
3. Object too big for any TLAB → allocate directly in eden (shared, needs a CAS), or for very large objects **straight into old gen / a humongous region**.
4. Eden full → **young GC**.

Then the object header (mark word + class word, 12–16 bytes) is written, fields are zeroed (Java guarantees zero-initialisation — a real cost for large arrays), and the constructor runs.

Bonus: this is why **allocation is a bump but *sequential* allocation gives you cache locality** — objects allocated together in time are adjacent in memory, and a copying young collector preserves that. Also why `-XX:+UseTLAB` is never turned off, and why `-Xlog:gc+tlab` exists.
</details>

---

### 3. Eden, S0, S1, tenuring — trace an object's life.

<details><summary>Answer</summary>

1. Allocated in **eden** (via TLAB).
2. Eden fills → **young GC (minor GC)**, a stop-the-world copying collection. Live objects in eden are copied into the **empty survivor space** (say S1); live objects in the **occupied survivor space** (S0) are also copied to S1, with their **age incremented**. Eden and S0 are now entirely free. S0 and S1 swap roles. This is "copying/scavenging" — hence `PSScavenge` / "Pause Young".
3. An object whose age reaches the **tenuring threshold** (`-XX:MaxTenuringThreshold`, default 15 for G1/Parallel; the JVM computes a smaller *dynamic* threshold each GC to fit survivors into the space) is **promoted** to the **old generation**.
4. Objects are also promoted early if the survivor space can't hold them — **premature promotion**. The `TargetSurvivorRatio` (default 50%) governs when the adaptive threshold drops.
5. Old gen fills → **major/old collection** (concurrent marking in G1/ZGC, or a stop-the-world full GC as a fallback).

Ratios: `-XX:NewRatio` (old:young), `-XX:SurvivorRatio` (eden:survivor, default 8 → eden is 8/10 of young). With G1 these are adaptive by default and you shouldn't set them.

The failure mode to name: if survivor space is too small, medium-lived objects (a request's working set, a cache entry being built) get **promoted prematurely**, so they die in old gen, where collecting them is expensive. Symptom: frequent old-gen collections despite a small true live set. `-Xlog:gc+age=trace` shows the age histogram, which is how you actually diagnose it.
</details>

---

### 4. Minor vs major vs full GC — and why does "full GC" mean something specific?

<details><summary>Answer</summary>

- **Minor / young GC** — collects the young generation only. Always stop-the-world (in all HotSpot collectors), but short, because the cost is copying survivors.
- **Major / old GC** — collects the old generation. In G1 this is the concurrent marking cycle plus mixed evacuations; in ZGC/Shenandoah it's almost entirely concurrent.
- **Full GC** — collects **the entire heap (young + old + metaspace) in a single stop-the-world, usually single-threaded, compacting pass**. In G1 this is a *fallback* — it means the concurrent machinery failed to keep up. Seconds to tens of seconds on a large heap.

**A full GC in G1 or ZGC is a bug or a misconfiguration, not normal operation.** Treat every one as an incident. Causes worth listing:
- **Allocation rate outran concurrent marking** → "concurrent mode failure" / **evacuation failure / to-space exhausted**.
- **Humongous allocation** couldn't find contiguous regions.
- **Metaspace** exhaustion triggers a full GC.
- Explicit `System.gc()` — from your code, a library, or **RMI's distributed GC** (which calls it on a timer; `-XX:+DisableExplicitGC` or `-XX:+ExplicitGCInvokesConcurrent` is the mitigation).
- A heap dump request (`jmap -dump:live`) forces one.

So when someone says "we see full GCs", the correct next question is **"what's the GC cause in the log?"** — the log line literally prints it: `Pause Full (G1 Evacuation Pause)`, `(Metadata GC Threshold)`, `(System.gc())`.
</details>

---

### 5. What are GC roots?

<details><summary>Answer</summary>

The starting set for reachability tracing. Anything not reachable from a root is garbage.

- **Local variables and parameters** on every thread's stack (found via the frame maps at a safepoint), plus JNI locals.
- **Static fields** of loaded classes (owned by the `Class` object → the classloader).
- **JNI global references**.
- **Active monitors** — objects being used for synchronisation.
- **The threads themselves**, and internal JVM structures (interned strings in the string table, `Class` objects, the code cache's oops, JVMTI references).

Two practical consequences:
- **A leak in Java is always "something is still reachable from a root that you thought was dead."** That's why heap-dump analysis is a "shortest path to GC root" query — MAT's "path to GC roots" and "dominator tree" are exactly this.
- The two overwhelmingly common real leaks are (1) an ever-growing **static/singleton collection** (a cache with no eviction) and (2) a **`ThreadLocal`** on a pooled thread. Both are roots that never go away. Third place: unbounded listener/callback registration.
</details>

---

### 6. Why does the collector need write barriers, and what's a card table?

<details><summary>Answer</summary>

To collect the young generation without scanning the old generation, the collector must know about every **old → young reference** (otherwise it would treat a young object referenced only from old as garbage). Application code creates such references at arbitrary times, so the JVM must intercept reference stores.

A **write barrier** is a small snippet the JIT emits after (or before) every reference field store. The generational barrier marks the **card** — a 512-byte granule of old gen — as **dirty** in the **card table**, a byte array with one entry per card. At young-GC time the collector scans only the dirty cards for old→young pointers, treating the references it finds as extra roots. Trading precision for speed: you scan 512 bytes to find one pointer.

This is why write barriers are a real cost: every reference assignment in your program does an extra store. It's also why **arrays of references are more expensive to mutate than arrays of primitives**, and part of why value types (Valhalla) matter.

The variants worth naming:
- **G1** additionally maintains per-region **remembered sets** (which regions point into me), because G1 collects arbitrary region sets, not just "young". G1's barrier is correspondingly more expensive — it has both a pre-write barrier (for SATB) and a post-write barrier (for the remembered set / dirty card queue).
- **SATB** (snapshot-at-the-beginning, G1/Shenandoah) — the **pre**-write barrier records the *old* value being overwritten, so the concurrent marker sees a consistent snapshot of the graph as of marking start. Solves the "mutator hides a live object from the marker" problem.
- **Incremental update** (CMS-style) — records the new value instead; requires a remark pause to fix up.
- **Load barriers** (ZGC) — intercept reference *loads* instead of stores, which is what lets ZGC relocate objects concurrently.

The classic framing is the **tri-colour invariant**: never allow a black object (scanned) to hold the only reference to a white object (unscanned) without recording it. SATB and incremental update are the two ways to preserve it.
</details>

---

### 7. What is a safepoint? What is "time to safepoint" and why should you care?

<details><summary>Answer</summary>

A **safepoint** is a point in execution where a thread's state (stack, registers) is fully described by the JVM's metadata, so the runtime can safely inspect or move objects. GC, thread dumps, heap dumps, biased-lock revocation, deoptimisation, and `jcmd` operations all need to bring **all** threads to a safepoint ("stop-the-world" = a global safepoint).

The JIT inserts safepoint **polls** at method returns and loop back-edges. To stop the world, the VM sets a flag/page-protects a polling page; each thread notices at its next poll and parks.

**Time to safepoint (TTSP)** is the interval from "please stop" to "the last thread has stopped." It's *not* GC work, but the application is stalled for it and it's **not counted in the GC pause time** — which is why it's such a nasty invisible latency source.

Causes of long TTSP:
- **Counted loops.** HotSpot historically omitted safepoint polls from `int`-counted loops for speed, so a long `for (int i = 0; i < n; i++)` over a big array could run for hundreds of milliseconds with no poll. (Loop strip mining, `-XX:+UseCountedLoopSafepoints`, on by default in modern JDKs, largely fixes this.) A `long`-counted loop always had polls.
- Very long-running **JNI** calls (a thread in native is *already* considered safepoint-safe, but it must not return into Java until released).
- **Page faults / swapping** — a thread being swapped in can't reach a poll.
- A huge number of threads.

Diagnose with `-Xlog:safepoint` — it prints "Reaching safepoint" and "At safepoint" times separately. If you have unexplained p99 spikes and GC pauses look fine, TTSP is a prime suspect. Also the reason profilers have **safepoint bias**: `jstack`-based samplers can only sample at safepoints, so they systematically misattribute time — which is why async-profiler (using `AsyncGetCallTrace`/perf) is preferred.
</details>

---

### 8. What actually happens on `OutOfMemoryError`? Name the varieties.

<details><summary>Answer</summary>

`OutOfMemoryError` is thrown when the JVM can't satisfy an allocation *and* GC can't help. The message tells you which resource:

- **`Java heap space`** — the classic. Live set exceeds `-Xmx`, from a genuine leak or from under-provisioning. Get a heap dump.
- **`GC overhead limit exceeded`** — >98% of time in GC recovering <2% of heap over 5 consecutive collections. A pre-emptive failure so you don't thrash forever. Same root causes as above; you'd have gotten `heap space` eventually. Disable with `-XX:-UseGCOverheadLimit` (rarely wise).
- **`Metaspace`** / **`Compressed class space`** — too many **classes**, not too many objects. Classloader leaks (repeated hot redeploys, dynamic proxy/CGLIB generation, scripting engines, per-tenant classloaders). Bounded by `-XX:MaxMetaspaceSize` (unbounded by default — it will eat the host). Compressed class space is separately capped at 1 GB by default.
- **`Direct buffer memory`** — `ByteBuffer.allocateDirect` beyond `-XX:MaxDirectMemorySize`. Netty, NIO, and off-heap caches live here. Direct buffers are freed via `Cleaner`, i.e. only after GC notices them, so you can OOM off-heap with a nearly empty heap.
- **`unable to create native thread`** — the OS refused a thread. Either you hit `ulimit -u`/`threads-max`, or the **address space is exhausted by thread stacks** (each ~1 MB × `-Xss`). Note this is *more* likely with a *bigger* heap on a fixed-memory box. The virtual-threads answer to this is Q3 of quiz 03.
- **`Requested array size exceeds VM limit`** — array longer than ~`Integer.MAX_VALUE - 2`.
- **`reason stack_trace_with_native_method`** and a few others, rare.

Operationally, always run with:
```
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/app/
-XX:+ExitOnOutOfMemoryError        # or CrashOnOutOfMemoryError
```
The last one matters: after an OOM the JVM is in an **undefined, half-broken state** — some threads died mid-operation, locks may be abandoned, and the process will limp and serve errors. Let it die and let the orchestrator restart it. `problems/gc-labs/L03OomVariants.java` reproduces the first four on demand.
</details>

---

### 9. Is `System.gc()` ever the right call?

<details><summary>Answer</summary>

Almost never in application code. It's a **hint** (`-XX:+DisableExplicitGC` makes it a no-op entirely), and in G1/Parallel it triggers a **stop-the-world full GC** — usually the most expensive thing your JVM can do. Calling it "to free memory" or "to reduce footprint" typically makes latency worse and throughput worse, and it destroys the collector's adaptive sizing heuristics.

Legitimate uses:
- **Benchmarking / test harnesses** — force a clean state and measure live set before/after (with `-XX:+ExplicitGCInvokesConcurrent`, or accept the pause).
- Before taking a **heap dump** you want to be leak-free of floating garbage (`jmap -dump:live` does this for you).
- Deliberately **releasing memory back to the OS** in an idle container to save cost — but modern collectors do this themselves (`-XX:+ShenandoahUncommit`, G1's periodic GC via `G1PeriodicGCInterval`, ZGC uncommits by default).
- Some off-heap/direct-buffer situations where you need `Cleaner`s to run to free native memory — a genuine, if ugly, reason.

Where it bites you unexpectedly: **RMI's distributed GC** calls it on a fixed interval (`sun.rmi.dgc.server.gcInterval`), and a surprising number of libraries and JMX agents call it. If you see periodic unexplained full GCs at a suspiciously round interval, that's the first thing to check.
</details>

---

### 10. Explain compressed oops and the 32 GB cliff.

<details><summary>Answer</summary>

On 64-bit, a reference ("oop" — ordinary object pointer) would naturally be 8 bytes. **Compressed oops** store it as a 4-byte value, scaled: because objects are 8-byte aligned, the low 3 bits are always zero, so a 32-bit value can address `2^32 × 8 = 32 GB`. The JIT emits a shift (and possibly an add for the heap base) to encode/decode.

Benefits: ~20–40% smaller reference-heavy heaps, far better cache utilisation, more objects per cache line. It's enabled by default (`-XX:+UseCompressedOops`) whenever the heap is small enough.

**The cliff:** set `-Xmx32g` or more (the exact threshold is a bit under 32 GB and depends on alignment/heap base) and the JVM **silently turns compressed oops off**. Every reference doubles in size, so your effective capacity may *drop* — a 32 GB heap can hold **less live data** than a 31 GB one. The classic advice: **either stay under ~31 GB, or jump well past 40 GB** where the raw capacity wins again.

Related: `-XX:+UseCompressedClassPointers` and the separate 1 GB compressed class space; `-XX:ObjectAlignmentInBytes=16` extends the ceiling to 64 GB at the cost of per-object padding. Verify with `java -XX:+PrintFlagsFinal -version | grep -i compressedoops`.

Sizing example to have ready: an object header is **12 bytes** with compressed oops (8-byte mark + 4-byte klass), padded to 16. So `new Integer(1)` is 16 bytes to hold 4 bytes of data — which is the setup for the Scala boxing discussion in quiz 06.
</details>

---

### 11. What's the difference between heap and non-heap memory? Enumerate a JVM's total footprint.

<details><summary>Answer</summary>

`-Xmx` bounds **only the Java heap**. Total RSS is much more, and this is *the* reason containers get OOM-killed with a heap that looks fine:

| Region | Bounded by | Notes |
|---|---|---|
| **Heap** | `-Xmx` | eden + survivors + old |
| **Metaspace** | `-XX:MaxMetaspaceSize` | **unbounded by default**; class metadata |
| **Compressed class space** | `-XX:CompressedClassSpaceSize` | default 1 GB reservation |
| **Code cache** (JIT) | `-XX:ReservedCodeCacheSize` | **240 MB *effective*, not a declared default** — the platform default is 48 MB (C2) and tiered compilation multiplies it by 5, so `PrintFlagsFinal` reports it as `{ergonomic}`. Segmented in JDK 9+ |
| **Thread stacks** | `-Xss` × thread count | ~1 MB each, default |
| **Direct byte buffers** | `-XX:MaxDirectMemorySize` | defaults to ≈`-Xmx`! Netty, NIO |
| **GC overhead** | — | card table, remembered sets, mark bitmaps: **G1 ≈ 10%+ of heap**; ZGC more |
| **Mapped files** | — | `MappedByteBuffer`, jar mmaps |
| **Native libraries / malloc arenas** | — | JNI, glibc arenas (see `MALLOC_ARENA_MAX`) |
| **JVM internals** | — | symbol table, string table, JFR buffers |

Rules of thumb: **budget the container at roughly 1.5× `-Xmx`** for a typical service, then measure. Use **`-XX:MaxRAMPercentage=70`** (or 60) rather than a fixed `-Xmx` in containers so the setting survives a resize.

Tooling: **Native Memory Tracking** — `-XX:NativeMemoryTracking=summary` then `jcmd <pid> VM.native_memory summary` — attributes non-heap usage by category. That's the answer to "the heap is 2 GB but RSS is 6 GB, why?"
</details>

---

### 12. How does the JVM behave in a container? What changed, and what's still wrong?

<details><summary>Answer</summary>

**Historically broken:** the JVM read `/proc/cpuinfo` and `/proc/meminfo`, i.e. **the host's** resources, ignoring cgroup limits. A 4 GB container on a 256 GB host defaulted `-Xmx` to 64 GB (¼ of host RAM) and sized GC threads and `ForkJoinPool` for 64 cores. Result: instant OOM-kill and wild thread oversubscription.

**Fixed** by `-XX:+UseContainerSupport` (default on since JDK 10, backported to 8u191): the JVM reads cgroup v1/v2 limits for memory and CPU.

Still needing care:
- **Set `-XX:MaxRAMPercentage`** (e.g. 70) instead of `-Xmx`, so vertical scaling doesn't require rebuilding the image. `InitialRAMPercentage`/`MinRAMPercentage` too (`MinRAMPercentage` confusingly applies only to *small* heaps).
- **CPU limits vs requests.** `availableProcessors()` is derived from cgroup **quota** (`cpu.max`), rounded up. A `cpu: 500m` limit reports **1** processor — which sets G1's `ParallelGCThreads` to 1, makes the FJP common pool degrade to **a new thread per task** (measured; note `getCommonPoolParallelism()` still reports 1, so it doesn't show up in metrics), and can serialise your whole app. Meanwhile a *request* with no limit reports the full host count. Override deliberately with `-XX:ActiveProcessorCount=N` if the derived value is wrong.
- **CFS throttling** is the sneaky one: with a 100 ms period and a 500m quota, you get 50 ms of CPU per 100 ms window. Burst past it and **every thread is frozen for the rest of the period** — indistinguishable from a GC pause in your dashboards. Watch `container_cpu_cfs_throttled_seconds_total`. Many teams set CPU *requests* but no *limits* specifically to avoid this.
- **The container OOM-killer (SIGKILL 137) is not a Java OOM.** No heap dump, no stack trace, no graceful shutdown — the kernel kills you because RSS (heap + all of Q11's regions) exceeded the memory limit. If you see exit code 137, look at *non-heap* memory.
- Don't set `-Xmx` == container memory limit. Ever.
</details>

---

### 13. Does the JVM ever allocate objects on the stack? What is escape analysis?

<details><summary>Answer</summary>

Careful answer: **HotSpot does not do true stack allocation of objects.** What C2 does is **escape analysis** → **scalar replacement**.

Escape analysis classifies an allocation as:
- **NoEscape** — never visible outside the compiled scope.
- **ArgEscape** — passed to a method but doesn't escape that call (may still be stack-safe).
- **GlobalEscape** — stored in a field, returned, thrown, or reachable by another thread.

For **NoEscape** objects the JIT applies **scalar replacement**: the object is deleted and its fields become individual values living in registers or on the stack. Zero allocation, zero GC cost. It also enables **lock elision** (a `synchronized` block on a non-escaping object is removed entirely — this is why using `StringBuffer` locally costs nothing) and other optimisations.

Limits worth stating, because they're the practical part:
- Only in **C2-compiled** code — interpreted and C1 code allocates normally. So it doesn't help short-lived processes or cold paths.
- **Inlining is a prerequisite.** If the allocating method's callee isn't inlined, the object escapes as far as the analysis can tell. Deep call chains, megamorphic call sites, and huge methods (past `-XX:MaxInlineSize`/`FreqInlineSize`) all defeat it.
- It's **all-or-nothing per allocation site** and fairly fragile — a single conditional path that stores the object kills it.
- Arrays are scalar-replaced only if the length is a compile-time constant and small (`-XX:EliminateAllocationArraySizeLimit`, default 64).

Verify with `-XX:+PrintEscapeAnalysis` / `-XX:+PrintEliminateAllocations` (needs a fastdebug JVM) or, in practice, by looking at allocation profiles in async-profiler (`-e alloc`) or JFR — if the allocation doesn't appear, it was eliminated.

Why it matters for Scala: people assume tuples, `Option`s, and closures are free because "escape analysis will get it." **Sometimes it does, often it doesn't.** Measure; don't assume. (And `Option` in a `for`-comprehension across method boundaries very often does escape.)
</details>

---

### 14. What are humongous objects?

<details><summary>Answer</summary>

G1-specific. G1 divides the heap into equal **regions** (`-XX:G1HeapRegionSize`, a power of two from 1 MB to 32 MB, auto-chosen as roughly `heap/2048`). An object **larger than half a region** is **humongous** and gets special treatment:

- Allocated **directly into old gen**, in a **contiguous run of regions** — skipping eden entirely, so no generational filtering.
- The tail of the last region is **wasted** (a 17 MB object in 16 MB regions occupies 2 regions = 32 MB).
- Historically only reclaimable at a full GC; modern G1 can free them during young collections if they're unreferenced (`G1ReclaimDeadHumongousObjectsAtYoungGC`), and JDK 18+ improved eager reclaim further.
- **They need contiguous free regions.** A fragmented heap can fail to satisfy one even with plenty of total free space → **full GC**, then possibly OOM.

Symptom in logs: `Pause Young (Concurrent Start) (G1 Humongous Allocation)` appearing frequently, or `to-space exhausted`. `-Xlog:gc+humongous=debug` gives detail.

Real-world sources: big `byte[]` for file uploads/downloads, large `String`s, a 1 MB+ protobuf or JSON payload, an oversized `ArrayList` backing array after growth, a `ByteBuffer` on heap.

Fixes: **increase `G1HeapRegionSize`** so the objects stop being humongous; or better, **stop allocating giant arrays** — stream in chunks, use a pooled/direct buffer, or a chunked list. `problems/gc-labs/L04Humongous.java` demonstrates it.
</details>

---

### 15. What is fragmentation, and which collectors suffer from it?

<details><summary>Answer</summary>

**External fragmentation**: enough total free memory, but not enough *contiguous* free memory for the allocation. **Internal fragmentation**: waste inside an allocated unit (object padding to 8-byte alignment; a humongous object's unused region tail).

Susceptibility:
- **Copying/evacuating collectors are immune by construction** — they copy live objects into fresh regions, leaving a compact result and wholly-empty source regions. G1's young collections, and G1/ZGC/Shenandoah's evacuation, all work this way. This is the underappreciated benefit of copying: allocation stays a pointer bump forever.
- **Mark-sweep without compaction** fragments. CMS was the notorious case: it swept in place and eventually hit a **promotion failure / concurrent mode failure**, then fell back to a **single-threaded, stop-the-world, compacting full GC** measured in tens of seconds. This is essentially why CMS was deprecated in JDK 9 and **removed in JDK 14**.
- **Mark-sweep-compact** (Serial/Parallel old gen) doesn't fragment, but compaction is a long stop-the-world pass.

Cost of copying: you need **headroom** to copy into. If G1 has no free regions to evacuate into, you get **evacuation failure / "to-space exhausted"** and a full GC. That's why running G1 at 95% occupancy is dangerous and why `-XX:G1ReservePercent` (default 10) exists.

Trade-off summary: **compaction costs pause time or CPU; not compacting costs a cliff.** Modern collectors pay CPU to compact concurrently.
</details>

---

### 16. What is a "concurrent" collector actually doing concurrently, and what must still stop the world?

<details><summary>Answer</summary>

"Concurrent" means the collector does most of its work **while application threads run**, at the cost of extra CPU and barrier overhead. Nothing is 100% pause-free.

**Concurrent:** marking (tracing the object graph), reference processing (mostly), relocation/evacuation in ZGC and Shenandoah, and remembered-set/card refinement.

**Still stop-the-world**, in every HotSpot collector:
- **Root scanning / initial mark** — you must snapshot thread stacks at a safepoint. (ZGC has made this per-thread and concurrent in later versions; G1 piggybacks initial mark on a young GC.)
- **Remark / final mark** — catch up on mutations the marker missed. SATB (G1) keeps this short; incremental-update (CMS) made it long and unpredictable.
- **Cleanup / relocation-start** pauses.
- **All young collections in G1 and Parallel** are stop-the-world.

The costs of going concurrent, which is the real answer:
1. **CPU** — the collector competes with the application. Concurrent GC trades throughput for latency, typically 10–20%.
2. **Barriers** — every load (ZGC) or store (G1 SATB) pays a little.
3. **Floating garbage** — objects that die *during* marking are missed and survive to the next cycle, so you need more headroom.
4. **You must start early enough.** If allocation outruns marking, you get a concurrent-mode/evacuation failure and a stop-the-world fallback — the pause you were trying to avoid, but worse. `-XX:InitiatingHeapOccupancyPercent` (G1, adaptive since JDK 9) is the knob.

The tri-colour framing plus SATB (Q6) is the mechanism that makes concurrent marking correct.
</details>

---

### 17. Given a 4 GB heap, what's a "good" GC overhead? What numbers should you know?

<details><summary>Answer</summary>

Useful reference points to have in your head (order-of-magnitude, not gospel):

- **GC CPU overhead**: healthy is **< 5%** of wall-clock for a latency-sensitive service; **< 10%** tolerable. Above ~20% you're in trouble; at 98% you get `GC overhead limit exceeded`.
- **Young GC pause**: **1–20 ms** typically, and it scales with the **live set copied**, not heap size. A young pause of 200 ms means either a huge survivor set or a sizing problem.
- **Young GC frequency**: every few seconds under load is fine; **many per second** means eden is too small or the allocation rate is extreme.
- **G1 target pause**: `MaxGCPauseMillis=200` **under G1 only**. The declared HotSpot default is `max_uintx-1` (effectively unset); G1 injects 200 at initialisation if you left the flag alone. Verified on JDK 21.0.11: `PrintFlagsFinal` shows 200 under G1 but 2^64−2 under Serial, Parallel and ZGC. Real p99 of 20–100 ms on a few-GB heap is normal. G1 full GC: **seconds**.
- **ZGC pauses**: **< 1 ms**, essentially independent of heap size (up to 16 TB). Generational ZGC in JDK 21 improved throughput and reduced the footprint penalty significantly.
- **Allocation rate**: **< 1 GB/s** per JVM is ordinary for a busy service; a few hundred MB/s is typical. Above a few GB/s you're allocation-bound and should look at the code, not the collector.
- **Object header**: 12 bytes (16 padded) with compressed oops. Reference: 4 bytes compressed, 8 uncompressed.
- **Allocation cost**: ~10 ns via TLAB bump.

How to compute overhead from a log: sum the pause times over the window and divide by wall-clock, then add concurrent-phase CPU (which pause time doesn't capture — an important caveat for G1/ZGC, where the pauses look tiny but the collector may be burning a core). `-Xlog:gc*` plus a tool like GCeasy/GCViewer, or JFR's GC events, gets you there.
</details>

---

### 18. What is the difference between throughput and latency as GC goals, and can you have both?

<details><summary>Answer</summary>

**Throughput** = fraction of CPU time doing application work. Maximised by doing GC rarely and in bulk with all cores: batch it, stop the world, use every thread, don't pay for barriers. → **Parallel GC**.

**Latency** = per-request response time, especially the tail. Maximised by never stopping for long: do the work concurrently, incrementally, in small slices. → **ZGC / Shenandoah**.

They're in genuine tension:
- Concurrent work needs **barriers**, which tax every application load or store (throughput cost even when GC is idle).
- Concurrent work **competes for CPU** with the application.
- Concurrent collectors need **more headroom** (floating garbage + relocation space), so the same live set needs a bigger heap.

The third axis people forget is **footprint**. You can generally get any two of {low latency, high throughput, small footprint}. Parallel: throughput + footprint. ZGC: latency + throughput, worse footprint. Serial: footprint, nothing else.

And the pragmatic point: **more heap improves both** throughput and pause frequency (fewer collections, more garbage per collection, higher garbage-to-live ratio) up to the point where it costs you compressed oops or gets you OOM-killed. Memory is usually the cheapest GC tuning knob, and "give it more heap" beats most flag-fiddling.
</details>

---

### 19. Object A references B. Both are unreachable but reference each other. Collected?

<details><summary>Answer</summary>

**Yes.** HotSpot uses **tracing** (reachability from roots), not reference counting, so cycles are collected with no special handling. Reference counting — CPython's primary mechanism, or C++'s `shared_ptr` — is what leaks cycles.

This is a good place to volunteer the trade-offs, since it's the real question behind the question:
- **Reference counting**: immediate reclamation (deterministic destructors), incremental cost spread out, but expensive per-assignment counter updates (worse: *atomic* updates when shared across threads), and it leaks cycles.
- **Tracing**: handles cycles, no per-assignment cost on the common path, batches work, and enables compaction (you can move objects because you find every reference) — but reclamation is non-deterministic and comes in bursts.

Tracing's ability to **move objects** is the deeper win: it's what makes bump allocation and compaction possible, which is why Java allocation is faster than `malloc`.
</details>

---

### 20. What is `-XX:+AlwaysPreTouch` and why would you use it?

<details><summary>Answer</summary>

`-Xms` **reserves and commits** virtual address space, but the OS maps physical pages **lazily**, on first touch. So the first time your application writes to a fresh part of the heap it takes a **page fault**, which costs latency at exactly the worst moment — during warm-up and during the first few GCs.

`-XX:+AlwaysPreTouch` makes the JVM write a byte to every page of the heap **at startup**, forcing the OS to back it with physical memory immediately. Effects:
- **Slower startup** (proportional to heap size — seconds for a large heap).
- **Predictable steady-state latency**: no page faults during request handling, no surprise TTSP stalls from faulting threads.
- Honest RSS from the start, which makes container sizing and OOM-kill risk visible immediately rather than an hour into the run.

Standard pairing: **`-Xms == -Xmx` + `AlwaysPreTouch`** for a latency-sensitive service. Setting `Xms == Xmx` also stops the collector from resizing the heap (each resize is work, and shrinking then re-growing is pure waste).

Related and worth mentioning: **huge pages** (`-XX:+UseLargePages`, or `-XX:+UseTransparentHugePages` on Linux) reduce TLB misses on large heaps for a few percent throughput. THP can cause latency spikes from in-kernel defragmentation, so explicit hugetlbfs is preferred if you can arrange it. Pre-touch also makes THP much more likely to actually back the heap with 2 MB pages.
</details>

---

### 21. Your app's RSS keeps growing but heap usage after full GC is flat. What's going on?

<details><summary>Answer</summary>

Flat post-GC live set means **no Java heap leak**. The growth is somewhere in Q11's non-heap list. Work down it:

1. **Direct/mapped byte buffers** — Netty, NIO, an off-heap cache, a compression library. Check `BufferPoolMXBean` metrics (`jvm_buffer_memory_used_bytes`) or NMT's "Internal"/"Other". Leaks here happen when buffers aren't released and the `Cleaner` never runs because the heap isn't under pressure.
2. **Metaspace / classloader leak** — check `jvm_memory_used_bytes{area="nonheap",id~"Metaspace"}` and `jcmd VM.classloader_stats`. Dynamic proxies, CGLIB, Groovy/JS scripting, repeated Spring context creation in tests.
3. **Thread count growing** — each thread is ~1 MB of stack. An unbounded `newCachedThreadPool` or a leaked executor per request. `jcmd Thread.print | grep -c tid`.
4. **Native library allocation** — a JNI library, a native compression/crypto codec, a JDBC driver's native bits. NMT won't attribute this well; you need `jemalloc`/`heaptrack` profiling.
5. **glibc malloc arena growth** — up to `8 × cores` arenas, each holding freed-but-unreturned memory. Symptom: RSS grows and plateaus at a suspiciously round multiple. Mitigate with `MALLOC_ARENA_MAX=2` or switch to jemalloc/tcmalloc.
6. **JIT code cache** growing toward its cap, JFR buffers, or GC's own metadata expanding with the heap.
7. **Not a leak at all** — the JVM committed up to `-Xmx` and simply never returns it to the OS. Post-GC *used* is flat but *committed* grew. `-XX:MaxHeapFreeRatio`/`MinHeapFreeRatio`, or G1's periodic GC / ZGC's uncommit, control returning memory.

Method: **`-XX:NativeMemoryTracking=summary`**, then `jcmd <pid> VM.native_memory summary.diff` after a baseline. That diff is the single most useful command for this problem, and it's a strong thing to name.
</details>

---

### 22. How would you find a memory leak in production?

<details><summary>Answer</summary>

State a method, not a tool list.

**1. Confirm it's a leak.** Plot **post-full-GC live set** over time (`jvm_memory_used_bytes{area="heap"}` sampled after collections, or the `Pause Full` lines in the GC log). Monotonic rise across GCs = leak. Sawtooth returning to the same floor = just allocation. This step eliminates most false alarms.

**2. Characterise it.** Correlate with deploys, traffic shape, and specific endpoints. Is it per-request, per-connection, per-deploy (classloader), or time-based? Rate tells you the object count you're looking for.

**3. Get evidence, cheapest first.**
- `jcmd <pid> GC.class_histogram` — live object counts by class, no full dump, low impact. Often enough: "3 million `LinkedHashMap$Entry`" points straight at a cache.
- **JFR** with `jdk.ObjectAllocationSample` and the **old-object-sample** event — continuous, ~1% overhead, and `TLAB`/old-object events give you allocation stacks *and* the leak candidates' paths to root. Best production option.
- **Heap dump** (`jcmd GC.heap_dump` or `jmap -dump:live,format=b,file=...`) — definitive, but it **pauses the JVM for seconds and writes a file the size of your live set**. Take it from a canary/depooled instance, never blindly from the whole fleet.

**4. Analyse.** Eclipse MAT: the **Leak Suspects** report, then the **dominator tree** (who retains the most), then **path to GC roots (excluding weak/soft)** on the top suspect. **Retained size** is the number that matters, not shallow size. Compare two dumps taken an hour apart (`histogram delta`) to isolate what's growing.

**5. Usual culprits**, in rough frequency order: an unbounded static/singleton `Map` used as a cache; `ThreadLocal` on pooled threads; listeners/callbacks registered and never removed; unclosed resources (streams, connections, `Cleaner`-tracked natives); string interning or key objects with mutable/broken `hashCode`; classloader retention (a `ThreadLocal` or static holding a webapp class); an ever-growing queue in front of a slow consumer (which is really a backpressure bug — see quiz 02 Q2).

**6. Prevent recurrence.** Bounded caches with eviction (Caffeine), `-XX:+HeapDumpOnOutOfMemoryError` always on, an alert on post-GC live set trend, and a load test long enough to see the slope.
</details>

---

## Score yourself

- **17+** — you can hold a GC conversation.
- **Must-knows regardless of score:** Q1 (generational hypothesis), Q8 (OOM taxonomy), Q11+Q12 (footprint & containers), Q22 (leak method). Those four cover most of what a server team cares about day to day.

---

## Appendix — Q23. "How does the HotSpot GC algorithm work?" (the one-question version)

This gets asked as a **single open question** — often phrased with the probe list *"Eden and
survivor spaces, tenured generation, full vs partial GC and pause times, allocation pressure
and thresholds, evacuation, GC roots and tri-colour marking, weak generational theory."*
That's Q1–Q6 and Q15 bundled together, so here's a **90-second spoken version** that hits
every probe in a sensible order.

<details><summary>The 90-second answer</summary>

> **It starts from the weak generational hypothesis: most objects die young**, and few old
> objects point to young ones. So the heap is split into a small **young generation** and a
> larger **old/tenured** generation.
>
> **Allocation** goes into **Eden**, via a thread-local allocation buffer — a TLAB — so it's
> just a pointer bump, no synchronisation, about ten nanoseconds.
>
> When Eden fills you get a **minor GC**, which is stop-the-world but short. It's a
> **copying** collector: it traces from the **GC roots** — thread stacks, static fields, JNI
> references, active monitors — and **evacuates** the live objects out of Eden into one of the
> two **survivor spaces**, incrementing their age. Objects in the *other* survivor space are
> copied across too. Then Eden and the vacated survivor are entirely free, and the two
> survivors swap roles.
>
> The key property is that **the cost is proportional to what survives, not to the garbage.**
> A young collection over four gigabytes of dead objects is almost free. Copying also means
> **no fragmentation**, so allocation stays a pointer bump forever.
>
> An object that survives enough collections — the **tenuring threshold** — is **promoted**
> to the old generation. If the survivor space is too small, objects get promoted early;
> that's **premature promotion**, and it's bad because they then die in old gen where
> collecting them is expensive.
>
> To collect young without scanning old, the JVM needs to know about **old-to-young
> references**. So there's a **write barrier** on every reference store, marking a **card**
> in the card table dirty; at GC time only the dirty cards are scanned.
>
> Old gen is collected by a different algorithm. Concurrent collectors mark **while the
> application runs**, which needs the **tri-colour invariant** — never let a scanned
> object hold the only reference to an unscanned one. G1 preserves it with **SATB**, a
> snapshot-at-the-beginning pre-write barrier that records the overwritten value.
>
> Finally, a **full GC** collects the entire heap in one stop-the-world compacting pass. In
> G1 or ZGC that means the concurrent machinery **failed to keep up** — usually allocation
> outran marking, or there was no contiguous room to evacuate into. Young pauses are
> milliseconds; a full GC on a large heap is seconds. **So a full GC is an incident, not
> normal operation**, and the first thing I'd check is the **GC cause** in the log.

**Then offer a number, which is what makes it land.** From `problems/gc-labs/L01`, same
workload, G1, 14 cores:

| heap | GCs | mean pause | GC overhead |
|---|---|---|---|
| 64 MB | 521 | 0.18 ms | 5.8% |
| 256 MB | 128 | 0.42 ms | 3.0% |
| 1 GB | 33 | 1.18 ms | 2.0% |

> "Collection count scales inversely with heap size, mean pause goes *up* while count goes
> down faster, so total overhead falls. And whether more heap helps *throughput* depends
> entirely on the survival rate — with near-zero survival GC was already free and more heap
> did nothing, but with real promotion, overhead went from 28.6% to 3%."

**Where the detail lives if they drill in:** Q1 hypothesis · Q2 TLAB · Q3 Eden/survivors/
tenuring · Q4 minor vs major vs full and GC causes · Q5 roots · Q6 barriers, card table,
SATB, tri-colour · Q7 safepoints · Q15 fragmentation and evacuation failure · quiz 05 Q2 for
G1's region model, Q4 for ZGC's coloured pointers.
</details>
