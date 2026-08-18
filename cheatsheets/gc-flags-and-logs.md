# Cheatsheet — GC flags, logs, and tooling

One page. All flags verified on JDK 21.

---

## Production baseline

```bash
# Sizing (containers)
-XX:MaxRAMPercentage=70        # NOT -Xmx: survives a pod resize
-XX:InitialRAMPercentage=70    # == max, so the heap never resizes
# (on fixed hosts: -Xms4g -Xmx4g)

# Collector -- explicit beats implicit
-XX:+UseG1GC                   # or -XX:+UseZGC -XX:+ZGenerational

# Caps on the unbounded defaults
-XX:MaxMetaspaceSize=256m      # metaspace is UNBOUNDED by default
-XX:MaxDirectMemorySize=256m   # defaults to roughly -Xmx

# Latency
-XX:+AlwaysPreTouch            # no page faults mid-request; slower startup

# Observability -- always on, all of it
-Xlog:gc*:file=/var/log/app/gc.log:time,uptime,level,tags:filecount=10,filesize=20M
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/app/
-XX:+ExitOnOutOfMemoryError    # after an OOM the JVM is unreliable -- let it restart
-XX:NativeMemoryTracking=summary
-XX:StartFlightRecording=maxsize=256m,maxage=6h,settings=profile
```

**Leave alone unless a measurement demands it:** `NewRatio`, `SurvivorRatio`,
`MaxTenuringThreshold`, `-Xmn`, `G1HeapRegionSize`, `InitiatingHeapOccupancyPercent`,
`ParallelGCThreads`, `ConcGCThreads`, `G1ReservePercent`, `SoftRefLRUPolicyMSPerMB`.
Modern collectors are adaptive and most hand-set generation flags **fight the ergonomics**
— setting `-Xmn` under G1 disables its adaptive young sizing outright.

---

## Logging (`-Xlog` — JDK 9+; the old `-XX:+PrintGC*` flags are **removed**)

```bash
-Xlog:gc                          # one line per collection. Cheap. No excuse not to.
-Xlog:gc*                         # all gc tags at info
-Xlog:gc+heap=debug               # per-generation / per-region occupancy
-Xlog:gc+age=trace                # tenuring age histogram -> premature promotion
-Xlog:gc+humongous=debug          # humongous allocations (G1)
-Xlog:gc+ergo=debug               # WHY the collector sized things as it did
-Xlog:gc+cpu                      # user/sys/real per GC  (real >> user => OS problem)
-Xlog:gc+ref=debug                # reference processing time
-Xlog:safepoint                   # TIME TO SAFEPOINT -- invisible in GC pause times!
```

Decorators: `time` (wall clock — essential for correlating with latency graphs),
`uptime`, `level`, `tags`, `pid`. Always rotate: `filecount=10,filesize=20M`.

---

## Reading a log line

```
[15.234s][info][gc] GC(42) Pause Young (Normal) (G1 Evacuation Pause) 2048M->1856M(4096M) 18.451ms
 |            |         |     |          |        |                    |     |     |       |
 uptime    level    tag   GC id  type  subtype   CAUSE            before after total  pause
```

- **Reclaimed** = 2048 − 1856 = **192 MB**; **survived** = 1856 MB.
- **The cause is the diagnosis.** Learn these:

| Cause | Means |
|---|---|
| `G1 Evacuation Pause` | routine eden-full young collection. **Healthy.** |
| `G1 Humongous Allocation` | an object > ½ region. Investigate (L04). |
| `Metadata GC Threshold` | **metaspace** pressure, not heap |
| `System.gc()` | someone called it — your code, a library, or RMI DGC |
| `GCLocker Initiated GC` | a JNI critical section delayed a GC |
| `G1 Preventive Collection` | G1 pre-empting an evacuation failure |
| `to-space exhausted` / `Evacuation Failure` | **no room to copy into.** Precedes a full GC. Bad. |
| `Pause Full (...)` | in G1/ZGC this means the concurrent machinery **failed**. Treat as an incident. |

`Pause Young (Concurrent Start)` = a concurrent marking cycle is beginning (IHOP crossed).
`(Mixed)` = young regions **plus** high-garbage old regions.

---

## Collectors (JDK 21)

| Collector | Flag | Pauses | Use for |
|---|---|---|---|
| Serial | `-XX:+UseSerialGC` | long, 1 thread | tiny heaps (<100 MB), CLI tools, minimal footprint |
| Parallel | `-XX:+UseParallelGC` | STW, multi-threaded | batch/ETL/Spark; best raw throughput |
| **G1** (default) | `-XX:+UseG1GC` | ~10–200 ms, target-driven | **the default.** Heaps 2–32 GB |
| ZGC | `-XX:+UseZGC -XX:+ZGenerational` | **<1 ms** | tail-latency SLOs, heaps >32 GB |
| Shenandoah | `-XX:+UseShenandoahGC` | ~ms | same niche as ZGC |
| Epsilon | `-XX:+UseEpsilonGC` | never collects | benchmarking, GC-overhead attribution |

**CMS was removed in JDK 14.** Don't propose it; a config containing
`-XX:+UseConcMarkSweepGC` won't even boot.

**JDK 21 note:** ZGC became **generational** (`-XX:+ZGenerational`, JEP 439), fixing its
throughput/footprint weakness. Default in 23+; non-generational removed. Knowing this is a
cheap currency signal.

---

## Numbers to know

| Quantity | Healthy / typical |
|---|---|
| GC CPU overhead | **<5%** good, <10% tolerable, 98% → `GC overhead limit exceeded` |
| Young pause | 1–20 ms (scales with **survivors copied**, not heap size) |
| G1 pause target | `MaxGCPauseMillis=200` **under G1 only** (declared default is `max_uintx-1`; G1 injects 200 ergonomically) |
| ZGC pause | <1 ms, ~independent of heap (to 16 TB) |
| Allocation rate | a few hundred MB/s typical; >1 GB/s is busy; >few GB/s = allocation-bound |
| Object header | **12 bytes** (compressed oops), padded to 16 |
| Reference | **4 bytes** compressed, 8 uncompressed |
| Allocation cost | ~10 ns (TLAB pointer bump) |
| `Integer` | 16 bytes to hold 4 bytes of data |
| `HashMap.Node` | **32 bytes per entry**, before key and value |
| Thread stack | ~1 MB reserved (`-Xss`) |
| Virtual thread | ~2.2 KB, ~0.7 µs to create (measured, P10) |
| G1 metadata overhead | ~10% of heap |
| **Compressed-oops cliff** | **~32 GB** — above it, every reference doubles |

**The 32 GB cliff:** a 32 GB heap can hold **less live data** than a 31 GB one. Either stay
under ~31 GB, or go well past 40 GB.

---

## Total footprint ≠ `-Xmx`

`-Xmx` bounds **only the Java heap**. RSS also includes:

| Region | Bounded by |
|---|---|
| Metaspace | `-XX:MaxMetaspaceSize` — **unbounded by default** |
| Compressed class space | `-XX:CompressedClassSpaceSize` (1 GB reservation) |
| Code cache | `-XX:ReservedCodeCacheSize` (240 MB *effective*: 48 MB platform default × 5 under tiered compilation) |
| Thread stacks | `-Xss` × thread count |
| Direct byte buffers | `-XX:MaxDirectMemorySize` — **defaults to ≈`-Xmx`** |
| GC metadata | — (G1 ≈10% of heap; ZGC more) |
| Mapped files, malloc arenas, JVM internals | — |

Budget **~1.5× `-Xmx`** for the container, then measure with NMT. **Exit code 137 /
SIGKILL is not a Java OOM** — it's the kernel, and it means non-heap memory. No dump, no
stack trace, no graceful shutdown.

---

## Containers

- `-XX:+UseContainerSupport` — **on by default** since JDK 10 (8u191+). Reads cgroup limits.
- Use **`MaxRAMPercentage`**, not `-Xmx`. Default is a very conservative **25%**.
- `MinRAMPercentage` does **not** mean "minimum heap" — it applies to *small* (<250 MB)
  total memory. Use `InitialRAMPercentage` for the `-Xms` equivalent. Genuine naming wart.
- **CPU limits matter a lot.** `availableProcessors()` derives from the cgroup **quota**. A
  `cpu: 500m` limit reports **1** → `ParallelGCThreads=1` and FJP common-pool parallelism 0.
  Override with `-XX:ActiveProcessorCount=N` if wrong.
- **CFS throttling** is the sneaky one: a 500m quota over a 100 ms period freezes **every
  thread** once you burn 50 ms. Looks exactly like a GC pause in dashboards. Watch
  `container_cpu_cfs_throttled_seconds_total`. Many teams set CPU *requests* and no *limits*
  specifically to avoid it.

---

## Tooling

```bash
jcmd <pid> help                        # the modern front-end; start here
jcmd <pid> GC.heap_info
jcmd <pid> GC.class_histogram          # live objects by class -- CHEAP, do this first
jcmd <pid> GC.heap_dump /tmp/h.hprof   # replaces jmap -dump; PAUSES the JVM
jcmd <pid> VM.native_memory summary          # needs the startup flag
jcmd <pid> VM.native_memory summary.diff     # ** the answer to "heap is 2G, RSS is 6G" **
jcmd <pid> VM.flags -all               # effective flag values
jcmd <pid> JFR.start / JFR.dump filename=/tmp/r.jfr

jstat -gcutil <pid> 1s                 # fastest live GC watch, no setup
java -XX:+PrintFlagsFinal -version | grep -i <flag>    # what did ergonomics choose?
```

**Escalation order in production** (cheapest, least invasive first):
`GC.class_histogram` → **JFR** → heap dump from a **depooled canary**. Never dump the
whole fleet: a dump pauses the JVM for seconds and writes a file the size of your live set.

**Analysis:** GCeasy / GCViewer for logs, **JDK Mission Control** for JFR, **Eclipse MAT**
for heap dumps (Leak Suspects → dominator tree by **retained** size → path to GC roots,
excluding weak/soft), **async-profiler** for CPU and `-e alloc` for allocation flame graphs
(no safepoint bias, unlike `jstack`-based samplers).

---

## The tuning order — say this, it's the whole answer

> **0. Prove GC is the problem.** Compare the pause distribution to your latency gap. If
>    p99 is 500 ms over budget and the worst pause is 20 ms, **GC is not your problem** —
>    go look at queueing and locks. Check TTSP separately (`-Xlog:safepoint`); it's
>    invisible in GC pause times.
> **1. Fix the application.** Top allocation sites (async-profiler `-e alloc`), bound the
>    caches, kill retention, stop allocating giant arrays. Biggest win, zero ops risk.
> **2. Size the heap.** `-Xms == -Xmx`, enough headroom. Validate RSS against the container
>    limit. Second-biggest win, one flag.
> **3. Change the collector** — only if the SLO demands it.
> **4. Tune flags** — one at a time, with a load test, measuring latency *and* throughput.

Most people start at 4. Saying this out loud is the single strongest signal you can give
in a GC conversation.
