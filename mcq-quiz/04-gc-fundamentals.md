# MCQ 04 — GC Fundamentals

20 questions. Source: [`quiz/04`](../quiz/04-gc-fundamentals.md).

---

### Q1. What is the weak generational hypothesis?

- **A.** Objects allocated together are usually collected together
- **B.** Large objects live longer than small ones
- **C.** Most objects die young, and few references point from old objects to young ones
- **D.** GC cost is proportional to heap size

<details><summary>Answer</summary>

**C.** Both halves matter. "Most die young" justifies collecting a small young generation
frequently with a **copying** collector. "Few old→young pointers" is what makes young GC possible
*without scanning old gen* — you only track the exceptions, via a card table.

The consequence to state: **allocation is cheap; survival is expensive.** GC cost tracks the live
set and promotion rate, not the allocation count. That reframes almost every "we allocate too
much" conversation.

→ `quiz/04` Q1
</details>

---

### Q2. What makes allocation fast in HotSpot?

- **A.** A thread-local allocation buffer where allocation is a pointer bump requiring no synchronisation
- **B.** A free-list search with best-fit matching
- **C.** The OS pre-allocating pages on demand
- **D.** Objects are allocated on the stack whenever possible

<details><summary>Answer</summary>

**A.** A **TLAB** is a chunk of eden owned by one thread. Allocation is: check `top + size <= end`,
increment `top`. A handful of instructions, no CAS, often inlined by the JIT — hence "~10 ns".

- **D** is the common misconception: **HotSpot does not do true stack allocation.** What it does is
  escape analysis → **scalar replacement**, deleting the object and putting its fields in registers
  — and only in C2-compiled code, only when inlining succeeds.

→ `quiz/04` Q2, Q13
</details>

---

### Q3. During a young GC, what gets copied where?

- **A.** Live objects in eden are copied to old gen
- **B.** Dead objects are copied out and eden is left intact
- **C.** All objects are compacted in place within eden
- **D.** Live objects in eden and the occupied survivor space are copied into the *empty* survivor space, with ages incremented

<details><summary>Answer</summary>

**D.** Then eden and the vacated survivor are entirely free, and the two survivors swap roles.
Objects reaching the **tenuring threshold** are promoted to old.

- **A** happens only on promotion (or **premature promotion** when survivor space can't hold them).
- **B** is backwards — a copying collector moves the *live* objects, which is why cost tracks
  survivors rather than garbage.

The failure mode to know: too-small survivor space → medium-lived objects promoted early → they die
in old gen where collecting them is expensive. `-Xlog:gc+age=trace` shows the histogram.

→ `quiz/04` Q3
</details>

---

### Q4. In G1, what does a "Full GC" indicate?

- **A.** Normal, routine operation — it happens on every old-gen collection
- **B.** That the concurrent machinery failed to keep up; treat it as an incident
- **C.** That the heap has been successfully compacted, which is desirable
- **D.** That `System.gc()` was called; nothing else triggers one

<details><summary>Answer</summary>

**B.** A full GC is a **stop-the-world, compacting, whole-heap** pass — seconds on a large heap. In
G1 it's a *fallback*. Causes: allocation outran concurrent marking (evacuation failure / "to-space
exhausted"), a humongous allocation couldn't find contiguous regions, metaspace exhaustion,
`System.gc()`, or a heap dump.

So when someone says "we see full GCs", the correct next question is **"what's the GC cause in the
log?"** — the log line prints it.

- **D** is one cause among several (and RMI's distributed GC calls it on a timer).

→ `quiz/04` Q4
</details>

---

### Q5. Which is **not** a GC root?

- **A.** An object referenced only by another object on the heap
- **B.** Local variables on a thread's stack
- **C.** Static fields of loaded classes
- **D.** Active monitors

<details><summary>Answer</summary>

**A.** That object is *reachable*, but it isn't a root — it's reachable *from* one. Roots are the
starting set for tracing.

Roots: thread stacks (locals and parameters), static fields, JNI locals and globals, active
monitors, the threads themselves, and internal JVM structures.

The practical consequence: **a leak in Java is always "something is still reachable from a root
that you thought was dead"** — which is why heap-dump analysis is a "path to GC roots" query.

→ `quiz/04` Q5
</details>

---

### Q6. What is a card table for?

- **A.** Tracking which objects have been marked during a concurrent cycle
- **B.** Storing the age of each object for tenuring decisions
- **C.** Recording which regions of old gen contain references into young gen, so young GC needn't scan all of old
- **D.** Mapping virtual addresses to physical pages

<details><summary>Answer</summary>

**C.** A **write barrier** after every reference store marks the containing **card** (a 512-byte
granule) dirty. At young-GC time only dirty cards are scanned for old→young pointers, and those
references count as extra roots.

That's why write barriers are a real cost: every reference assignment does an extra store. G1 adds
per-region **remembered sets** on top (because it collects arbitrary region sets), plus a **SATB**
pre-write barrier so concurrent marking sees a consistent snapshot.

The framing: the **tri-colour invariant** — never let a scanned (black) object hold the only
reference to an unscanned (white) one without recording it.

→ `quiz/04` Q6
</details>

---

### Q7. What is "time to safepoint", and why does it matter?

- **A.** The duration of the GC pause itself
- **B.** The interval between "please stop" and the last thread actually stopping — and it is *not* included in reported GC pause times
- **C.** The time the JIT takes to compile a method
- **D.** How long a thread waits to acquire a contended monitor

<details><summary>Answer</summary>

**B.** The application is stalled for TTSP but it doesn't appear in GC pause metrics — which makes
it a nasty invisible latency source. Causes: counted loops without polls (largely fixed by loop
strip mining), long JNI calls, page faults/swapping, and very high thread counts.

`-Xlog:safepoint` is the only way to see it. **If you have unexplained p99 spikes and GC pauses
look fine, TTSP is a prime suspect.**

Related: it's why `jstack`-based profilers have **safepoint bias** and async-profiler is preferred.

→ `quiz/04` Q7
</details>

---

### Q8. Which `OutOfMemoryError` is **not** fixed by increasing `-Xmx`?

- **A.** `Java heap space`
- **B.** `GC overhead limit exceeded`
- **C.** Both A and B
- **D.** `Metaspace`

<details><summary>Answer</summary>

**D.** Metaspace holds **class metadata**, not objects, and lives outside the heap. Causes are
classloader leaks — dynamic proxies, CGLIB, scripting engines, repeated redeploys. It's
**unbounded by default**, so always set `-XX:MaxMetaspaceSize`.

Others not fixed by `-Xmx`: `Direct buffer memory` (off-heap), `unable to create native thread`
(and a *bigger* heap makes it **more** likely, since stacks and heap compete), `Requested array
size exceeds VM limit` (a VM limit).

Only `heap space` and `GC overhead limit exceeded` are heap problems — and they have the same root
cause and the same fix.

→ `quiz/04` Q8, reproduced in `problems/gc-labs/L03OomVariants.java`
</details>

---

### Q9. Exit code 137 / SIGKILL on a containerised JVM means what?

- **A.** A Java `OutOfMemoryError` with a heap dump written
- **B.** The JVM crashed with a segfault
- **C.** The kernel OOM-killed the process because total RSS exceeded the cgroup limit — no dump, no stack trace
- **D.** The application called `System.exit(137)`

<details><summary>Answer</summary>

**C.** And it's worse than a Java OOM precisely *because* you get nothing — no heap dump, no
graceful shutdown, no clue. The problem is **non-heap** memory: metaspace, code cache, thread
stacks, direct buffers, GC metadata, or native allocation.

That's what `-XX:NativeMemoryTracking=summary` plus `jcmd <pid> VM.native_memory summary.diff` is
for.

→ `quiz/04` Q8, Q11, Q12
</details>

---

### Q10. Is `System.gc()` ever appropriate?

- **A.** Rarely — benchmarking, before a deliberate heap dump, or forcing `Cleaner`s for native memory
- **B.** Yes — call it after any large batch to keep the heap tidy
- **C.** Never under any circumstances
- **D.** Yes — it's the only way to trigger a concurrent cycle

<details><summary>Answer</summary>

**A.** It's a *hint* and in G1/Parallel it triggers a **stop-the-world full GC** — the most
expensive thing your JVM can do — while destroying the collector's adaptive sizing heuristics.

Legitimate: benchmark harnesses, before a leak-free heap dump, deliberately returning memory in an
idle container, and some off-heap situations needing `Cleaner`s to run.

Where it bites unexpectedly: **RMI's distributed GC calls it on a timer**. Periodic unexplained
full GCs at a suspiciously round interval? Check that first.

→ `quiz/04` Q9
</details>

---

### Q11. What happens at a heap of 32 GB or more?

- **A.** G1 automatically switches to ZGC
- **B.** Regions become fixed at 32 MB
- **C.** The JVM refuses to start without `-XX:+UseLargePages`
- **D.** Compressed oops are silently disabled, so every reference doubles in size — and a 32 GB heap may hold *less* live data than a 31 GB one

<details><summary>Answer</summary>

**D.** Compressed oops store a reference in 4 bytes by exploiting 8-byte alignment, addressing
`2^32 × 8` = 32 GB. Past that the JVM turns them off, references become 8 bytes, and effective
capacity can *drop*.

The advice: **stay under ~31 GB, or jump well past 40 GB** where raw capacity wins again.

→ `quiz/04` Q10, `quiz/05` Q13
</details>

---

### Q12. Which is **not** bounded by `-Xmx`?

- **A.** Eden
- **B.** Metaspace
- **C.** Old generation
- **D.** Survivor spaces

<details><summary>Answer</summary>

**B.** `-Xmx` bounds **only the Java heap** — eden, survivors, old. Outside it: metaspace,
compressed class space, code cache (240 MB *effective* — 48 MB platform default × 5 under tiered
compilation, not a declared default), thread stacks, direct byte buffers, GC metadata (**G1 ≈ 10%
of heap**), mapped files, malloc arenas, JVM internals.

Rule of thumb: **budget the container at ~1.5× `-Xmx`**, then measure with NMT. And note
`MaxDirectMemorySize` **defaults to roughly `-Xmx`**, so direct buffers can double your footprint
by default.

→ `quiz/04` Q11
</details>

---

### Q13. A container has `cpu: 500m`. What does `Runtime.availableProcessors()` report?

- **A.** 1
- **B.** The host's full core count
- **C.** 0
- **D.** 500

<details><summary>Answer</summary>

**A.** It's derived from the cgroup **quota**, rounded up — so 500m reports **1**. That sets
`ParallelGCThreads` to 1, makes the FJP common-pool parallelism **0**, and can serialise your whole
application.

Override deliberately with `-XX:ActiveProcessorCount=N` if the derived value is wrong.

And the sneaky companion: **CFS throttling.** With a 100 ms period and a 500m quota you get 50 ms of
CPU per window — burst past it and **every thread freezes for the rest of the period**,
indistinguishable from a GC pause in your dashboards. Watch
`container_cpu_cfs_throttled_seconds_total`.

→ `quiz/04` Q12
</details>

---

### Q14. Does HotSpot allocate objects on the stack?

- **A.** Yes, whenever escape analysis proves the object doesn't escape
- **B.** Yes, for all objects under 64 bytes
- **C.** No — it does escape analysis and then **scalar replacement**, deleting the object and putting its fields in registers or on the stack
- **D.** No — every object is always on the heap with no optimisation

<details><summary>Answer</summary>

**C.** The careful distinction. For a **NoEscape** allocation, C2 deletes the object entirely and
its fields become individual values. It also enables **lock elision** (which is why a local
`StringBuffer` costs nothing).

The limits that matter: only in **C2-compiled** code (not interpreted or C1), **inlining is a
prerequisite**, it's all-or-nothing per allocation site and fairly fragile, and arrays are only
scalar-replaced if the length is a small compile-time constant.

Why it matters for Scala: people assume tuples, `Option`s and closures are free because "escape
analysis will get it." **Sometimes it does, often it doesn't.** Measure.

→ `quiz/04` Q13
</details>

---

### Q15. What makes an object "humongous" in G1?

- **A.** Larger than 1 MB
- **B.** Larger than half a G1 region
- **C.** Larger than the eden space
- **D.** Any array with more than 1 million elements

<details><summary>Answer</summary>

**B.** Region size is `-XX:G1HeapRegionSize` (a power of two, 1–32 MB, auto-chosen at roughly
heap/2048). Anything larger than **half** a region is humongous.

Consequences: allocated **directly into old gen** (skipping eden entirely, so it defeats the
generational hypothesis), needs **contiguous** free regions (a fragmented heap can fail even with
free space → full GC), and **wastes the tail** of its last region.

Measured with 1 MB regions and 3.5 MB arrays: 4 regions each, **512 KB wasted per allocation**, 54
GC events caused by humongous allocation, 23.5% GC overhead. Chunking into 64 KB dropped it to
12.9%.

→ `quiz/04` Q14, measured in `problems/gc-labs/L04Humongous.java`
</details>

---

### Q16. Which collector type is immune to external fragmentation?

- **A.** Mark-sweep without compaction
- **B.** All of them are equally susceptible
- **C.** Reference-counting collectors
- **D.** Copying/evacuating collectors

<details><summary>Answer</summary>

**D.** Copying collectors move live objects into fresh regions, leaving a compact result and wholly
empty source regions — so allocation stays a pointer bump forever. That's the underappreciated
benefit of copying.

- **A** fragments; **CMS** was the notorious case, eventually hitting a promotion failure and
  falling back to a single-threaded STW compacting full GC. Essentially why CMS was removed.

The cost of copying: you need **headroom** to copy into. No free regions → **evacuation failure /
"to-space exhausted"** → full GC. Which is why running G1 at 95% occupancy is dangerous.

→ `quiz/04` Q15
</details>

---

### Q17. What must still stop the world even in a "concurrent" collector?

- **A.** Nothing — concurrent means zero pauses
- **B.** Only the final compaction
- **C.** Root scanning / initial mark, and remark / phase transitions
- **D.** Every reference write

<details><summary>Answer</summary>

**C.** You must snapshot thread stacks at a safepoint. Plus remark (catching up on mutations the
marker missed), cleanup, and relocation-start pauses. **All young collections in G1 and Parallel
are stop-the-world.**

The costs of going concurrent: **CPU** competing with the application, **barriers** on every load
(ZGC) or store (G1 SATB), **floating garbage** (objects dying during marking survive to next
cycle, so you need headroom), and **you must start early enough** or you get a STW fallback that's
worse than the pause you were avoiding.

→ `quiz/04` Q16
</details>

---

### Q18. Objects A and B reference each other; both are unreachable from any root. Collected?

- **A.** Yes — HotSpot uses tracing from roots, so cycles are collected with no special handling
- **B.** No — the mutual reference keeps both alive
- **C.** Only after a full GC
- **D.** Only if neither overrides `finalize()`

<details><summary>Answer</summary>

**A.** Tracing collectors care only about reachability from roots. **Reference counting** is what
leaks cycles — CPython's mechanism, or C++'s `shared_ptr`.

Worth volunteering the trade-off: reference counting gives immediate reclamation and deterministic
destructors, at the cost of per-assignment counter updates (atomic when shared) and leaked cycles.
Tracing handles cycles, batches the work, and — the deeper win — **can move objects**, which is
what makes bump allocation and compaction possible.

→ `quiz/04` Q19
</details>

---

### Q19. What does `-XX:+AlwaysPreTouch` do?

- **A.** Pre-loads classes at startup to reduce JIT warmup
- **B.** Pre-allocates all TLABs for every thread
- **C.** Warms the CPU caches before the first request
- **D.** Writes to every heap page at startup, forcing the OS to back it with physical memory

<details><summary>Answer</summary>

**D.** `-Xms` commits address space, but the OS maps physical pages **lazily on first touch** — so
you take page faults during warm-up and the first few GCs, at exactly the worst moment.

Trade: slower startup (proportional to heap size), predictable steady-state latency, and honest RSS
from the start (which makes container sizing and OOM-kill risk visible immediately).

Standard pairing for a latency-sensitive service: **`-Xms == -Xmx` + `AlwaysPreTouch`**.

→ `quiz/04` Q20
</details>

---

### Q20. RSS keeps growing but heap usage after full GC is flat. First thing to check?

- **A.** Increase `-Xmx`
- **B.** Non-heap memory — direct buffers, metaspace, thread count, native allocation — via NMT
- **C.** Switch to ZGC
- **D.** Add more `System.gc()` calls

<details><summary>Answer</summary>

**B.** A flat post-GC live set means **no Java heap leak**. So it's direct/mapped byte buffers
(Netty, NIO, off-heap caches), a metaspace/classloader leak, growing thread count (~1 MB of stack
each), native library allocation, glibc malloc arena growth, or simply the JVM having committed up
to `-Xmx` and never returned it.

The single most useful command: `-XX:NativeMemoryTracking=summary` at startup, then
`jcmd <pid> VM.native_memory summary.diff` after a baseline.

- **A** makes several of these *worse*.

→ `quiz/04` Q21, Q22
</details>

---

## Score yourself

- **17+** — you can hold a GC conversation.
- **Must-knows regardless of score:** Q1 (the hypothesis), Q8/Q9 (the OOM taxonomy), Q12/Q13
  (footprint and containers), Q20 (the leak method). Those cover most of what a server team cares
  about day to day.

The consolidated spoken walkthrough is `quiz/04` **Q23** — use that if asked "how does HotSpot GC
work?" as one open question.
