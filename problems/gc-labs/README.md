# GC Labs

Four experiments. The goal is not to run them — it's to **read the output and be able
to explain it**. GC questions in interviews are usually "here's a log / a symptom, what
do you think is happening", and that's a skill you only get from looking at real output.

Everything here was verified on **JDK 21** (Apple JDK 21.0.11, ARM64).

---

## Setup

Some labs deliberately use a tiny heap. `java Foo.java` runs the compiler **in the same
JVM on the same heap**, so a 128 MB heap gets exhausted by your program *and* the
compiler, and the launcher dies before printing its diagnosis. For L02/L03 (and it
doesn't hurt for the others), compile first:

```bash
cd /Users/kendrickkee/onboardNotes
mkdir -p /tmp/l01 /tmp/l02 /tmp/l03 /tmp/l04
javac -d /tmp/l01 problems/gc-labs/L01AllocationRate.java
javac -d /tmp/l02 problems/gc-labs/L02Leak.java
javac -d /tmp/l03 problems/gc-labs/L03OomVariants.java
javac -d /tmp/l04 problems/gc-labs/L04Humongous.java
```

That's itself the first lesson: **`-Xmx` bounds the whole JVM's Java heap**, and
everything sharing that JVM shares the budget.

---

## L01 — Allocation rate, GC frequency, heap size

The core lab. Same work, different flags.

```bash
java -Xmx64m  -Xms64m  -Xlog:gc -cp /tmp/l01 L01AllocationRate
java -Xmx256m -Xms256m -Xlog:gc -cp /tmp/l01 L01AllocationRate
java -Xmx1g   -Xms1g   -Xlog:gc -cp /tmp/l01 L01AllocationRate

# then the same three with `retain`, which makes objects survive:
java -Xmx64m  -Xms64m  -cp /tmp/l01 L01AllocationRate retain
java -Xmx256m -Xms256m -cp /tmp/l01 L01AllocationRate retain
java -Xmx1g   -Xms1g   -cp /tmp/l01 L01AllocationRate retain
```

**Measured on this machine (14 cores, JDK 21, G1):**

| heap | mode | GCs | mean pause | overhead | throughput |
|---|---|---|---|---|---|
| 64 MB | churn | 521 | 0.18 ms | 5.8% | 18.3 M/s |
| 256 MB | churn | 128 | 0.42 ms | 3.0% | 16.7 M/s |
| 1 GB | churn | 33 | 1.18 ms | 2.0% | 15.4 M/s |
| 64 MB | **retain** | 960 | 0.75 ms | **28.6%** | 12.0 M/s |
| 256 MB | **retain** | 137 | 1.27 ms | 9.2% | 15.9 M/s |
| 1 GB | **retain** | 35 | 1.57 ms | 3.0% | 16.1 M/s |

**The three things to take from this table:**

1. **GC count scales inversely with heap size** — 4× the heap, ~¼ the collections.
   Frequency = allocation rate / eden size.
2. **Mean pause goes *up* while count goes *down* faster**, so total overhead falls.
   Anyone quoting only one of those numbers is telling you half the story.
3. **Whether more heap helps throughput depends entirely on whether GC was costing
   you anything.** In churn mode almost nothing survives, GC is already nearly free,
   and more heap does *nothing* (slightly worse, from cache locality). In retain mode
   GC is 28.6% of runtime at 64 MB, and more heap is a large win.

That last point is the answer to "should we just add heap?" — **it depends on your
survival rate, and you can measure it.**

**Collector comparison** (same heap, same work):
```bash
java -Xmx512m -Xms512m -XX:+UseSerialGC   -Xlog:gc -cp /tmp/l01 L01AllocationRate retain
java -Xmx512m -Xms512m -XX:+UseParallelGC -Xlog:gc -cp /tmp/l01 L01AllocationRate retain
java -Xmx512m -Xms512m -XX:+UseG1GC       -Xlog:gc -cp /tmp/l01 L01AllocationRate retain
java -Xmx512m -Xms512m -XX:+UseZGC -XX:+ZGenerational -Xlog:gc -cp /tmp/l01 L01AllocationRate retain
```
Check the ranking against what `quiz/05` Q1 predicts. Note ZGC's pauses are tiny but
its *overhead* may not be lower — pause time and CPU cost are different things.

---

## L02 — Find the leak

```bash
java -Xmx128m -Xms128m -Xlog:gc -cp /tmp/l02 L02Leak            # dies in ~2s
java -Xmx128m -Xms128m -Xlog:gc -cp /tmp/l02 L02Leak bounded    # runs forever, flat
```

**The measurement that matters** — post-GC live set over time:

```
LEAKING                          BOUNDED
requests   postGC live           requests   postGC live
    2000          36 MB              2000          12 MB
    4000          68 MB              4000          13 MB
    6000         100 MB              6000          13 MB
    7600         124 MB  CRITICAL   20000          13 MB
    -> OutOfMemoryError                -> no OOM, ever
```

**Monotonically rising post-GC live set = leak. Flat floor = healthy.** That single
distinction is step 1 of every leak investigation (quiz 04 Q22), and it eliminates most
false alarms — a sawtooth in *total* usage is normal and means nothing.

Three leaks are planted, all of which look reasonable in review:
1. **`STATIC_CACHE`** — unbounded static map. The #1 real-world leak. A cache without
   eviction is a memory leak with good intentions.
2. **`LISTENERS`** — the lambda **captures** the session, so a list of `Runnable`
   retains every session object. Easy to miss because the list looks harmless.
3. **`WEAK_BUT_LEAKY`** — a `WeakHashMap` whose **value holds a reference back to its
   own key**, so the key is still strongly reachable and the entry never clears.
   `WeakHashMap` does not protect you from a value→key reference.

Then get a dump and analyse it:
```bash
java -Xmx128m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/leak.hprof \
     -cp /tmp/l02 L02Leak
# writes ~130 MB. Open in Eclipse MAT:
#   Leak Suspects -> Dominator tree (sort by RETAINED size)
#   -> right-click -> Path to GC roots -> exclude weak/soft
```
And the cheaper production-first step, no dump needed:
```bash
jcmd <pid> GC.class_histogram | head -20
jstat -gcutil <pid> 500ms
```

Remember to delete the dump afterwards — it's the size of your live set.

---

## L03 — The OOM taxonomy

```bash
java -Xmx64m                                  -cp /tmp/l03 L03OomVariants heap
java -Xmx64m -XX:+UseParallelGC               -cp /tmp/l03 L03OomVariants overhead
java -Xmx64m -XX:MaxDirectMemorySize=32m      -cp /tmp/l03 L03OomVariants direct
java -Xmx64m                                  -cp /tmp/l03 L03OomVariants threads
java -Xmx64m                                  -cp /tmp/l03 L03OomVariants array
java -Xmx64m -XX:MaxMetaspaceSize=16m         -cp /tmp/l03 L03OomVariants metaspace
java -Xss256k                                 -cp /tmp/l03 L03OomVariants stack
```

**Verified messages:**

| mode | message | fixed by bigger `-Xmx`? |
|---|---|---|
| `heap` | `Java heap space` | yes (or fix the leak) |
| `direct` | `Cannot reserve 1048576 bytes of direct buffer memory (allocated: 33554432, limit: 33554432)` | **no** — off-heap |
| `threads` | `unable to create native thread` | **no** — makes it *worse* |
| `array` | `Requested array size exceeds VM limit` | **no** — a VM limit |
| `metaspace` | `Metaspace` | **no** — not the heap |
| `stack` | `StackOverflowError` (not an OOM at all) | **no** — use `-Xss` |
| `overhead` | see caveat below | yes |

**Only one of the seven is actually fixed by adding heap.** "We got an OOM, let's add
memory" is wrong more often than it's right, and *reading the message* is the whole
first step.

**Honest caveat on `overhead`:** I could not get `GC overhead limit exceeded` to
reproduce on demand on JDK 21 — Parallel and Serial both report `Java heap space`
instead, and G1 simply keeps up and survives. The condition (>98% of time in GC, <2%
of heap reclaimed, over 5 consecutive collections) is a heuristic that's genuinely hard
to hit deliberately. The lab reports whichever outcome it gets and explains why. That's
a real finding worth knowing: **treat the two messages as the same problem**, because
they have the same root cause and the same fix.

Two of these (`direct`, `threads`) are **off-heap**, so a heap dump tells you nothing.
That's what NMT is for:
```bash
java -XX:NativeMemoryTracking=summary ...
jcmd <pid> VM.native_memory summary
jcmd <pid> VM.native_memory summary.diff    # after a baseline
```

Worth naming: the **container OOM-killer (exit code 137 / SIGKILL)** is none of the
above. No heap dump, no stack trace, no graceful shutdown — the kernel kills you
because RSS exceeded the cgroup limit. If you see 137, look at *non-heap* memory
(quiz 04 Q11–Q12).

---

## L04 — Humongous allocations (G1)

```bash
# 1 MB regions (G1's default for a 512m heap): 3.5 MB arrays ARE humongous
java -Xmx512m -Xms512m -XX:+UseG1GC -Xlog:gc,gc+humongous=debug -cp /tmp/l04 L04Humongous

# 8 MB regions: the same arrays are no longer humongous
java -Xmx512m -Xms512m -XX:+UseG1GC -XX:G1HeapRegionSize=8m -cp /tmp/l04 L04Humongous

# chunked: the real fix
java -Xmx512m -Xms512m -XX:+UseG1GC -cp /tmp/l04 L04Humongous chunked
```

**Measured:**

| config | humongous? | GCs | GC overhead |
|---|---|---|---|
| 1 MB regions (default) | yes — 4 regions, **512 KB wasted per allocation** | 53 | 23.5% |
| 8 MB regions | no (3.5 MB < 4 MB threshold) | 41 | 17.5% |
| chunked into 64 KB | no | **25** | **12.9%** |

With the default 1 MB regions, `grep -c "Humongous Allocation"` on the log gives **54**
GC events caused by humongous allocation.

A humongous object (larger than **half** a region):
- goes **straight into old gen**, skipping eden — so a short-lived 3.5 MB buffer gets
  treated like long-lived data and defeats the generational hypothesis entirely
- needs **contiguous** free regions, so a fragmented heap can fail to allocate one even
  with plenty of free space → full GC → possibly OOM
- **wastes the tail** of its last region

**Both fixes work, but they're not equivalent.** `G1HeapRegionSize=8m` is one flag and
takes effect immediately, but it's a workaround: bigger regions coarsen G1's
collection-set granularity (potentially longer pauses) and do nothing about "we
allocate 3.5 MB per request". Chunking makes the allocations ordinary short-lived eden
objects — nearly free for a copying collector — and it's the better result here (25 GCs
vs 41). That ordering is quiz 05 Q7's point: **fix the code, then the heap, then the
collector, then the flags.**

---

## What to be able to do afterwards

1. **Read a GC log line cold** — cause, before→after→total, pause, and what it implies.
2. **Say whether a symptom is a leak** from the post-GC live-set trend alone.
3. **Name the OOM varieties** and which are off-heap.
4. **Explain the heap-size trade-off in both directions** — and know that "add heap"
   helps a lot in some workloads and not at all in others, with a reason.
5. **State the tuning order**: prove GC is the problem → fix allocation/retention →
   size the heap → change collector → tune flags.

If you can do those five, the GC half of your prep is done.
