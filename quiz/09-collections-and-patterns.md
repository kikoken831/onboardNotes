# Quiz 09 — Collections, Design Patterns & Java Fundamentals

18 questions. The bread-and-butter Java questions that come before the hard concurrency
ones. Runnable demonstrations in `problems/java/P11Collections.java`.

Target: 25 min.

---

### 1. Explain the Builder pattern and when you'd use it.

<details><summary>Answer</summary>

**Problem it solves:** constructing an object with many parameters, several optional, where
a constructor becomes unusable.

```java
// The telescoping-constructor anti-pattern it replaces:
new Order(id, customer, items, null, null, discount, null, true);
// Which is 'true'? What are the nulls? Swap two Strings and it still compiles.
```

**The pattern:**
```java
Order order = Order.builder()
    .id(123)
    .customer("acme")
    .items(items)
    .discount(0.1)
    .build();          // validation happens HERE
```

**Use it when:**
- **Four or more constructor parameters**, especially several of the same type (adjacent
  same-typed parameters are silently swappable — the bug a builder eliminates).
- **Many optional parameters.** The alternative is 2ⁿ constructor overloads.
- **You want immutability.** All fields `final`, no setters, so the object is safely
  publishable and shareable across threads with no synchronisation (quiz 01 Q12/Q25). This
  is the concurrency argument for builders and worth volunteering.
- **Cross-field validation** — `build()` is the one place to check invariants
  ("`endDate` must be after `startDate`"), so an invalid object can never exist.
- **Stepwise construction** — the caller assembles the object over several statements or
  branches.

**Don't use it when:** 2–3 required parameters (just use a constructor), or the object is
genuinely mutable and setters are honest. A builder on a 2-field class is ceremony.

**Trade-offs to name:** more code (mitigate with **Lombok `@Builder`**); one extra
allocation per construction (irrelevant except in a hot loop); and errors move from
**compile time to runtime** — a plain constructor won't compile if you omit a required
argument, whereas a builder throws in `build()`. That last point is the real cost, and you
can mitigate it with a **staged/telescoping builder** where required fields are enforced by
returning different interface types.

**Modern Java alternatives worth mentioning:**
- **`record`** (JDK 16+) — for a pure immutable data carrier, a record plus a compact
  constructor for validation covers many builder use cases with one line.
- `record` + **`withX` copy methods**, or a nested builder inside the record for the
  many-optional-fields case.

**Scala:** you rarely need this. `case class` with **default parameter values** and **named
arguments** gives you the same thing natively:
```scala
case class Order(id: Long, customer: String, items: List[Item],
                 discount: Double = 0.0, express: Boolean = false)
Order(id = 123, customer = "acme", items = items, express = true)
```
Plus `copy(discount = 0.2)` for derived instances, and immutability by default. **The
Builder pattern is largely a workaround for a language without named/default parameters** —
a good observation to offer, since it shows you understand *why* the pattern exists rather
than just what it is.

**Where you've already seen it:** `StringBuilder`, `Stream.Builder`,
`HttpRequest.newBuilder()`, `Caffeine.newBuilder()`, protobuf builders, most AWS SDK v2
clients.
</details>

---

### 2. `TreeMap` vs `HashMap` — difference in implementation, and when would you use each?

<details><summary>Answer</summary>

**`HashMap` — a hash table with separate chaining.**
- A **contiguous array** of buckets (`Node[] table`), length always a power of two.
- `put`: take `key.hashCode()`, **spread** it (`h ^ (h >>> 16)`, so high bits influence the
  low-order index), then index with `hash & (table.length - 1)` — a mask, not a modulo,
  which is why the length is a power of two.
- Collisions form a **linked list** in the bucket. Within a bucket, `equals()` finds the
  match.
- **Resize** when `size > capacity × loadFactor` (default **0.75**, initial capacity 16):
  allocate a table twice as large and rehash everything. Expensive — pre-size it if you
  know the count.
- **Java 8+**: a bucket with **8+ entries** (and a table of at least **64**) is
  **treeified** into a red-black tree, so pathological collisions degrade to **O(log n)**
  rather than O(n). Reverts to a list at 6.
- **O(1) average** get/put/remove, **O(log n) worst case**. Iteration order is
  **unspecified and unstable** across resizes.
- **Nulls:** one `null` key allowed, any number of `null` values.

**`TreeMap` — a red-black (self-balancing binary search) tree.**
- Ordered by **natural ordering** (`Comparable`) or an explicit **`Comparator`** given at
  construction.
- Each node holds key, value, left, right, parent, and a colour bit. Insert/delete rebalance
  via rotations to keep the tree height O(log n).
- **O(log n) guaranteed** for get/put/remove — no worst-case cliff, but no O(1) either.
- **Iteration is in sorted key order**, always.
- Implements **`NavigableMap`/`SortedMap`**: `firstKey`, `lastKey`, `floorKey`, `ceilingKey`,
  `higherKey`, `headMap`, `tailMap`, `subMap`, `descendingMap`. **This is the actual reason
  to choose it.**
- **Nulls:** a `null` **key** throws `NullPointerException` under natural ordering (it must
  call `compareTo`); `null` **values** are fine. A custom comparator that tolerates null
  could permit a null key.

**When to use which:**

| Use `HashMap` | Use `TreeMap` |
|---|---|
| the default — you just need a lookup table | you need keys **in sorted order** |
| O(1) matters, and n is large | you need **range queries** (`subMap`, `headMap`) |
| keys have a good `hashCode` | you need nearest-match (`floorKey`/`ceilingKey`) |
| you don't care about order | keys are only `Comparable`, with no sane `hashCode` |

**Default to `HashMap`.** Reach for `TreeMap` when *ordering or range access* is a
requirement. If you want *insertion* order rather than sorted order, that's
**`LinkedHashMap`**, which is a `HashMap` plus a doubly-linked list through the entries —
O(1) like `HashMap`, predictable iteration, and the basis of an LRU cache via
`removeEldestEntry` (see `P09Exercises` EX4 and P11).

**Two things in the common crib notes that are worth getting right**, because a sharp
interviewer may push:

1. **"`TreeMap` is good for frequent add/remove"** — as a raw-speed claim this is
   **backwards**: `TreeMap` is O(log n) for add/remove where `HashMap` is O(1), and P11
   measures `HashMap` several times faster. The charitable and *correct* version is: if you
   need data **kept in sorted order** while it's being frequently modified, `TreeMap`
   maintains that order incrementally at O(log n) per operation, which is far better than
   re-sorting a list on every change. **The advantage is the ordering, not the mutation
   speed.** Say it that way.

2. **"`TreeMap` doesn't support nulls"** — true for **keys**, not for **values**. And
   `HashMap` permits one null key. Worth being precise about.

3. **"`TreeMap` is memory efficient"** — arguable, not a slam dunk. `TreeMap` avoids
   `HashMap`'s partly-empty bucket array (25%+ slack at a 0.75 load factor), but each node
   carries **three references plus a colour bit** versus `HashMap.Node`'s one `next`
   reference. Roughly comparable in practice; if footprint is the actual concern, neither
   is the answer — parallel primitive arrays or a specialised map (Eclipse Collections,
   fastutil) is.

**Concurrency note:** neither is thread-safe. The concurrent counterparts are
`ConcurrentHashMap` and `ConcurrentSkipListMap` (a skip list, not a tree — it's lock-free,
which a balanced tree is very hard to be).
</details>

---

### 3. What's the difference between `Set` and `List`? Complete the picture.

<details><summary>Answer</summary>

**The short answer:** a `List` is an **ordered sequence allowing duplicates, addressable by
index**. A `Set` is a **collection of unique elements**, generally with no index.

But answer the fuller version, because the short one is a one-liner and they'll keep going:

| | `List` | `Set` | `Queue`/`Deque` | `Map` |
|---|---|---|---|---|
| duplicates | yes | **no** | yes | unique keys |
| index access | **yes**, `get(i)` | no | no | by key |
| ordering | insertion / positional | depends on impl | insertion / priority | depends on impl |
| defining op | positional access | membership (`contains`) | add/remove at ends | key→value |

**Implementations that matter:**
- **`ArrayList`** — array-backed. O(1) `get`, amortised O(1) `add` at the end,
  **O(n) insert/remove in the middle**. The default.
- **`LinkedList`** — doubly-linked. O(1) insert/remove *given the node*, but **O(n) to find
  it**, and terrible cache locality. It's a `Deque` too. **Almost always the wrong choice**
  — `ArrayDeque` beats it as a queue and `ArrayList` beats it as a list, even for
  middle-insertion workloads, because pointer-chasing costs more than `System.arraycopy`.
- **`HashSet`** — a `HashMap` with dummy values. O(1), unordered.
- **`LinkedHashSet`** — `HashSet` + insertion order. O(1) *and* predictable iteration; a
  good default when you want deterministic output.
- **`TreeSet`** — a `TreeMap` with dummy values. Sorted, O(log n), `NavigableSet`.
- **`ArrayDeque`** — the right stack and queue. Faster than `Stack` (which is synchronised
  and extends `Vector`) and faster than `LinkedList`.
- **`EnumSet`/`EnumMap`** — bit-vector backed, extremely fast; use them for enum keys.

**Things worth volunteering:**
- **`Set` depends on `hashCode`/`equals`** (or `compareTo` for `TreeSet`). Put a mutable
  object in a `HashSet` and mutate a field used by `hashCode`, and **the element is lost** —
  it's in the wrong bucket, so `contains` returns false. P11 demonstrates this.
- **`TreeSet` uses `compareTo`, not `equals`**, to decide uniqueness. If they disagree,
  `TreeSet` behaves inconsistently with `Set`'s contract — a real, subtle bug.
- **`List.contains` is O(n); `Set.contains` is O(1).** The single most common real
  performance bug in ordinary code is a `List.contains` inside a loop, which is O(n·m). If
  someone shows you a slow loop, look for this first.
- `List.of(...)`/`Set.of(...)` (JDK 9+) return **immutable** collections that throw on
  mutation, and `Set.of` **rejects duplicates at construction**.
- `Arrays.asList` is **fixed-size** and writes through to the array — `add` throws.

**Scala:** `List` (immutable linked list), `Vector` (the general-purpose indexed sequence),
`Set`, `Map` — all **immutable by default**, with `scala.collection.mutable` variants you
opt into. Note Scala's `List` is a cons list, so **`apply(i)` is O(n)** — the opposite of
Java's `ArrayList`. That's a genuine porting trap: a Java `list.get(i)` loop translated
literally to a Scala `List` becomes O(n²). See quiz 06 D2 and the measurements in
`problems/scala/S03AllocationPressure.scala`.
</details>

---

### 4. Walk through what happens on `map.put(key, value)` in a `HashMap`.

<details><summary>Answer</summary>

1. **`hash(key)`** — `key.hashCode()`, then spread: `h ^ (h >>> 16)`. The spread mixes high
   bits down, because the index only uses the low bits.
2. **Index** — `i = hash & (table.length - 1)`. A bitmask, valid only because the table
   length is a power of two. Cheaper than `%`.
3. **Lazy init** — if `table` is null, allocate it (default capacity 16).
4. **Empty bucket** → store a new `Node`. Done.
5. **Occupied bucket** → walk the chain. For each entry compare
   **`hash` first, then `key == k || key.equals(k)`**. The hash comparison is a cheap filter
   before the potentially expensive `equals`. Match → replace the value and return the old
   one.
6. **No match** → append a new node. If the chain now has **≥ 8** entries **and** the table
   is **≥ 64**, **treeify** the bucket into a red-black tree. (If the table is smaller, it
   resizes instead — a small table with long chains just needs more buckets.)
7. **Resize** — if `++size > threshold` (capacity × 0.75), double the table and
   redistribute. Java 8+ splits each bucket into a "low" and "high" list using the new bit,
   preserving relative order, which is what removed the old concurrent-resize infinite loop
   (quiz 02 Q17).

**Why this matters in an interview:** it's the setup for `hashCode`/`equals` (Q5), for
"why is my `HashMap` slow" (a bad `hashCode` collapsing everything into one bucket), and
for the sizing point — **pre-size your maps** (`new HashMap<>(expected / 0.75f + 1)`) when
you know the element count, because repeated resizing rehashes everything.
</details>

---

### 5. What's the `hashCode`/`equals` contract, and what breaks if you violate it?

<details><summary>Answer</summary>

**The contract:**
1. If `a.equals(b)` then `a.hashCode() == b.hashCode()`. **Mandatory.**
2. If `a.hashCode() == b.hashCode()`, `a.equals(b)` may still be false (collisions are legal).
3. `equals` must be **reflexive, symmetric, transitive, consistent**, and `x.equals(null)`
   is false.
4. `hashCode` must be **consistent** — unchanged while the object's `equals`-relevant state
   is unchanged.

**What breaks:**

- **Override `equals` but not `hashCode`** — the classic. Two equal objects land in
  different buckets, so `map.get(equalKey)` returns `null` and `set.add(duplicate)` succeeds.
  Your `Set` now contains two "equal" elements.
- **Override `hashCode` but not `equals`** — everything hashes to the right bucket and then
  fails identity comparison. Same symptom.
- **Mutate a field used by `hashCode` after insertion** — the object sits in a bucket
  computed from its *old* hash. `contains` looks in the *new* bucket and finds nothing. The
  entry is **unreachable but not removed** — you can't get it, and you can't remove it
  either. P11 demonstrates this. **This is why map keys should be immutable.**
- **A bad but legal `hashCode`** (`return 1;`) — correct behaviour, catastrophic
  performance: every entry chains in one bucket, O(n) lookups, and pre-Java-8 that was a
  denial-of-service vector via crafted collisions.
- **Asymmetric `equals`** — typically from `instanceof` in one direction, or a subclass
  comparing against a superclass. Breaks `List.contains`, which may or may not find the
  element depending on argument order.
- **`equals` inconsistent with `compareTo`** — `TreeSet`/`TreeMap` use `compareTo`, so the
  collection's notion of duplicate diverges from `equals`. Subtle and nasty.

**Practically:** use **`record`** (JDK 16+), which generates both correctly from all
components, or Lombok's `@EqualsAndHashCode`, or your IDE. Hand-write them only when you
need a subset of fields, and then think hard about whether those fields are immutable.

**Scala:** `case class` generates `equals`, `hashCode`, `toString`, and `copy` structurally.
Combined with immutability by default, this entire class of bug largely disappears — worth
naming as another concrete migration benefit.
</details>

---

### 6. `ArrayList` vs `LinkedList` — which and why?

<details><summary>Answer</summary>

**`ArrayList` almost always.** Say that first, then justify.

| | `ArrayList` | `LinkedList` |
|---|---|---|
| `get(i)` | **O(1)** | O(n) |
| `add` at end | amortised O(1) | O(1) |
| `add`/`remove` at index | O(n) (arraycopy) | O(n) to *find* + O(1) to unlink |
| `add`/`remove` at head | O(n) | **O(1)** |
| memory per element | one reference (~4 B) | node: ~24 B + 2 refs |
| cache locality | **excellent** (contiguous) | poor (pointer chasing) |

The textbook says "`LinkedList` for frequent insertion/removal", and in practice **that's
usually wrong**, because:
- You must **traverse to the position first** — O(n) — so the O(1) unlink is irrelevant
  unless you already hold the node (which the `List` API never gives you).
- `ArrayList`'s O(n) removal is `System.arraycopy`: a single vectorised memory move, often
  faster than walking 1000 scattered nodes even though the complexity class is worse.
- **Cache misses dominate** at realistic sizes. Constant factors beat asymptotics here.
- `LinkedList` allocates a node per element — more GC pressure (and see quiz 06 D2 for the
  identical argument about Scala's `List`).

**Legitimate uses of `LinkedList`:** essentially only as a `Deque` where you need
head/tail operations — and **`ArrayDeque` beats it there too**. So: effectively never.

**What to actually do:**
- Need indexed access → `ArrayList`, **pre-sized** if you know the count.
- Need a queue/stack/deque → `ArrayDeque`.
- Need frequent removal by value → you want a `Set` or `Map`, not a list.
- Need concurrent → `CopyOnWriteArrayList` (read-mostly) or a `BlockingQueue`.
</details>

---

### 7. `Comparable` vs `Comparator`?

<details><summary>Answer</summary>

- **`Comparable<T>`** — `int compareTo(T other)`, implemented **by the class itself**. Its
  *natural ordering*. One per class. Used by `Collections.sort(list)`, `TreeMap`, `TreeSet`,
  `Arrays.sort`.
- **`Comparator<T>`** — `int compare(T a, T b)`, a **separate object**. Many per class,
  passed in where needed. Used by `list.sort(cmp)`, `new TreeMap<>(cmp)`,
  `stream.sorted(cmp)`, `max(cmp)`.

Use `Comparable` when there's one obvious ordering you own (`String`, `Integer`,
`LocalDate`). Use `Comparator` when the ordering is context-dependent, when you don't own
the class, or when you need several orderings.

Modern composition (JDK 8+), which is what they want to see:
```java
orders.sort(Comparator.comparing(Order::customer)
                      .thenComparing(Order::date, reverseOrder())
                      .thenComparingLong(Order::id));            // primitive: avoids boxing
Comparator.nullsFirst(Comparator.comparing(Order::customer));    // null-safe
```
Prefer `comparingInt`/`comparingLong`/`comparingDouble` in hot paths — `comparing` boxes.

**The contract matters:** a comparator must be **transitive**, **antisymmetric**
(`sgn(compare(a,b)) == -sgn(compare(b,a))`), and consistent. Violate it and
`Arrays.sort`/`TimSort` may throw **`IllegalArgumentException: Comparison method violates
its general contract!`** — a real production error, usually caused by
`(a, b) -> a.score() > b.score() ? 1 : -1` which **never returns 0**. Use
`Integer.compare(a, b)` / `Double.compare`, never subtraction (which overflows) and never
a two-way ternary.

**Scala:** `Ordering[T]` (the `Comparator` analogue, usually supplied implicitly) and
`Ordered[T]` (the `Comparable` analogue). `xs.sortBy(_.date)` / `xs.sorted` /
`xs.maxBy(_.score)` cover most needs, and `Ordering.by` composes.
</details>

---

### 8. Which design patterns do you actually use, and where do you see them in Spring?

<details><summary>Answer</summary>

Give a short list you can defend, and **name where they appear in frameworks you use** —
that's what separates memorised-from-a-book from applied.

| Pattern | What it's for | Where you've seen it |
|---|---|---|
| **Builder** | complex/immutable construction | `StringBuilder`, `HttpRequest.newBuilder`, Caffeine |
| **Factory / Factory Method** | decouple creation from use | `@Bean` methods, `ExecutorService` via `Executors`, `Calendar.getInstance` |
| **Strategy** | swap an algorithm at runtime | `Comparator`, `RejectedExecutionHandler`, any interface with several impls injected |
| **Template Method** | fixed skeleton, variable steps | `JdbcTemplate`, `RestTemplate`, `AbstractList` |
| **Proxy** | intercept calls | **`@Transactional`, `@Async`, `@Cacheable` — all Spring AOP proxies** |
| **Decorator** | layer behaviour | `BufferedInputStream`, `Collections.unmodifiableList`, servlet filters |
| **Observer** | publish/subscribe | `ApplicationEventPublisher`, listeners, Reactive Streams |
| **Singleton** | one instance | Spring's default bean scope — **container-managed, not the static kind** |
| **Adapter** | bridge incompatible APIs | `HandlerAdapter`, `Arrays.asList` |
| **DI / IoC** | invert construction | the entire Spring container |
| **Repository / DAO** | abstract persistence | `@Repository`, Spring Data |
| **Front Controller** | single request entry point | `DispatcherServlet` |

**The Spring proxy point is the highest-value one to raise**, because it explains a real bug
class: **`@Transactional`/`@Async` on a self-invoked method does nothing**, since the call
doesn't pass through the proxy (quiz 10 Q5, quiz 02 Q13). Knowing *why* — because it's the
Proxy pattern and `this` isn't the proxy — is the difference between memorising the gotcha
and understanding it.

**Two things to say to sound senior rather than certified:**
1. **Patterns are vocabulary, not goals.** They're for communicating a design ("this is a
   strategy") not a checklist to satisfy. Applying patterns you don't need is how you get
   `AbstractSingletonProxyFactoryBean`.
2. **Many GoF patterns are workarounds for missing language features.** Strategy is a
   function. Builder substitutes for named/default parameters. Template Method is a
   higher-order function. Command is a closure. Singleton is a module. In Scala most of
   these collapse into a function value, a `case class`, or an `object` — which is a good
   bridge to the migration and shows you know why the patterns exist.
</details>

---

### 9. What are the four preconditions for deadlock? Give an example and how you'd mitigate it.

<details><summary>Answer</summary>

> **Fully covered in `quiz/01` Q8, with a runnable reproduction, `jstack` output, and two
> working fixes in `problems/java/P03Deadlock.java`.** Summary here for completeness; go
> there for the detail.

**Coffman conditions** — all four must hold: mutual exclusion, hold-and-wait, no
preemption, circular wait.

**The canonical example** (this is the one the bank's crib note describes — a fund transfer):
```java
// Thread 1: transfer(accountA, accountB, 100)   locks A, then wants B
// Thread 2: transfer(accountB, accountA, 50)    locks B, then wants A
synchronized (from) {
    synchronized (to) { from.balance -= amt; to.balance += amt; }
}
```
Both threads hold one lock and wait for the other's. Verified in P03; `jstack` and
`ThreadMXBean.findDeadlockedThreads()` both report it:
```
a->b is blocked on Account@58ea606c owned by b->a
b->a is blocked on Account@77f80c04 owned by a->b
```

**Mitigations, in order of practical value:**

1. **Global lock ordering** — always acquire in the same total order (by account id, or
   `System.identityHashCode` with a tie-breaker). Breaks *circular wait*. **The workhorse
   fix.** Verified working in P03 `ordered` mode.
2. **`tryLock` with timeout + randomised backoff** — breaks *hold-and-wait*. Verified
   working, and it cost **203 retries for 40 transfers** — that's the price. The backoff
   **must be randomised**; fixed backoff makes two threads retry in lockstep forever, which
   is **livelock**: busy, no progress. This is the mitigation the crib note names, and
   "randomised" is the word that shows you know why.
3. **Don't hold a lock across a call you don't control** — no I/O, no callbacks, no alien
   methods in a critical section.
4. **Eliminate the second lock** — a single coarser lock, or no locks at all.

**The best answer here uses no locks.** Put both balances in one immutable snapshot behind a
single CAS:
```java
record Balances(long a, long b) {}
state.updateAndGet(s -> new Balances(s.a() - amount, s.b() + amount));
```
Deadlock is impossible by construction. That generalises to the migration point: Scala's
immutability-by-default pushes you here instead of toward lock ordering.

And for money specifically, say the pragmatic thing: **you'd use the database's
transactions** and let it handle isolation — then be ready for the follow-up, because
databases deadlock too, and they resolve it by *detecting* the cycle and killing a victim
transaction with a retryable error (quiz 11 Q8).
</details>

---

### 10. `String` — why immutable? What's the string pool? `==` vs `equals`?

<details><summary>Answer</summary>

**Why immutable:**
- **Safe sharing** — freely shareable across threads with no synchronisation (final fields,
  quiz 01 Q25). No defensive copying.
- **Hash caching** — `hashCode` is computed once and cached, which is why `String` is a good
  map key.
- **Security** — a file path or SQL string can't be mutated after validation by another
  reference holder (the TOCTOU problem).
- **Interning is possible** at all only because the value can't change.

**The string pool:** string *literals* are interned automatically — a single instance per
distinct literal per classloader. Since JDK 7 it lives **on the heap** (previously PermGen)
and the table is resizable (`-XX:StringTableSize`).
```java
String a = "hello";               String b = "hello";               a == b   // true  (pooled)
String c = new String("hello");                                     a == c   // false (new object)
String d = c.intern();                                              a == d   // true
String e = "hel" + "lo";                                            a == e   // true  (compile-time constant)
String f = "hel" + getLo();                                         a == f   // false (runtime concat)
```

**`==` compares references; `equals` compares content.** Always `equals` for strings (or
`Objects.equals` for null-safety). `==` working sometimes — because of pooling — is exactly
what makes this bug survive testing.

**Don't call `intern()` on unbounded user input**: the pool is a JVM-level structure you
can't evict from, so you've built an unbounded cache. Use a `HashMap` or Guava `Interner`
if you want canonicalisation you control (quiz 05 Q11).

**Related, and worth volunteering:**
- **Compact strings** (JDK 9+, default): Latin-1 strings are stored as `byte[]` not
  `char[]`, roughly halving footprint for most strings.
- **`+` in a loop is O(n²)** — each concatenation copies. Use `StringBuilder`. (The compiler
  optimises `+` within a *single* expression, not across iterations.)
- **`-XX:+UseStringDeduplication`** — the *collector* shares identical backing arrays.
  Different from interning (quiz 05 Q11).
- **Scala:** `String` is Java's. But `s"..."` interpolation is **eager**, which makes
  `logger.debug(s"...")` a real allocation hotspot — the top find in most Scala allocation
  profiles (quiz 06 D1, measured in S03).
</details>

---

### 11. Checked vs unchecked exceptions — when do you use each?

<details><summary>Answer</summary>

- **Checked** (`extends Exception`) — the compiler forces `catch` or `throws`. Intended for
  *recoverable* conditions the caller should plan for.
- **Unchecked** (`extends RuntimeException`) — no compiler obligation. Programming errors
  and unrecoverable conditions.
- **`Error`** (`OutOfMemoryError`, `StackOverflowError`) — don't catch. The JVM is in
  trouble (quiz 04 Q8).

**Where the industry landed:** mostly **unchecked**, because checked exceptions don't
compose. They break lambdas (`Function.apply` can't throw checked), they leak
implementation details up through interfaces, they force either `throws` pollution across
every layer or — far worse — `catch (Exception e) {}`. Spring wraps `SQLException` into the
unchecked `DataAccessException` hierarchy for exactly this reason, and that's the strongest
argument available: **a major framework deliberately converts checked to unchecked.**

**Use checked** when the caller can *genuinely and specifically* do something about it and
you want to force the decision — and one where you have no choice:
**`InterruptedException`**, which must be propagated or restored with
`Thread.currentThread().interrupt()`, never swallowed (quiz 01 Q15).

**Rules regardless:**
- **Never swallow.** `catch (Exception e) {}` is how outages become mysteries.
- Don't catch what you can't handle; let it propagate to a boundary that can (a
  `@ControllerAdvice`, a supervisor, a retry).
- **Preserve the cause**: `throw new ServiceException("loading user " + id, e)`.
- Include context in the message. "NullPointerException" tells you nothing; the id does.
- Don't use exceptions for control flow — they're expensive (stack capture) and obscure
  intent.

**Scala:** all exceptions are unchecked. Idiomatic error handling puts failure **in the
return type** — `Option`, `Either[Error, A]`, `Try`, `Validated` for accumulating errors, or
**ZIO's typed `E` channel**, where the compiler proves you've handled it. That's a genuine
improvement over both Java options, and a good thing to be able to argue: it composes like
checked exceptions were supposed to, without the syntactic tax (quiz 06 C5).
</details>

---

### 12. What's the difference between an interface and an abstract class, in modern Java?

<details><summary>Answer</summary>

| | interface | abstract class |
|---|---|---|
| multiple inheritance | **yes** | no (single) |
| state / instance fields | no (only `static final`) | **yes** |
| constructors | no | yes |
| method bodies | `default`, `static`, `private` (JDK 8/9+) | yes |
| access modifiers | public (and `private` for helpers) | any |

The line has blurred since JDK 8 gave interfaces `default` and `static` methods. What
remains: **interfaces can't hold instance state and can't have constructors.**

**Choose an interface** to define a capability or contract, especially if implementers may
already extend something. That's most of the time, and it's what you want for testability
and for DI.
**Choose an abstract class** when subclasses genuinely share **state** and constructor
logic — a real Template Method with fields.

**Prefer composition over inheritance.** Deep hierarchies are the classic maintenance trap;
`default` methods let you share behaviour without inheritance. And a **`sealed`** interface
or class (JDK 17+) plus records gives you an exhaustively-checkable closed hierarchy — the
Java equivalent of an ADT, which pattern matching can check for completeness.

**Scala:** **`trait`** covers both cases — it can have state, concrete members, and be mixed
in multiply (with linearisation rules resolving conflicts). `sealed trait` + `case class` is
*the* idiomatic ADT and the thing exhaustive `match` checks against. If you can say "Java's
`sealed interface` + `record` is converging on Scala's `sealed trait` + `case class`", that's
a nice observation for a migration conversation.
</details>

---

### 13. `Stream` API — when is it the right tool, and what are the traps?

<details><summary>Answer</summary>

**Good for:** declarative transformation pipelines (`filter`/`map`/`collect`), where the
intent reads better than the loop. Lazy — intermediate operations do nothing until a
terminal operation runs, so it can short-circuit (`findFirst`, `anyMatch`, `limit`).

**Traps:**
- **`parallelStream()` runs on the shared `ForkJoinPool.commonPool()`.** Blocking in it
  stalls the whole JVM's async work; and in a web server you already have request-level
  parallelism, so it's often a throughput *loss*. Fully covered in quiz 02 Q11.
- **A stream is single-use.** Reusing one throws `IllegalStateException`.
- **Debugging is worse** than a loop — no easy breakpoints, uglier stack traces.
- **Not faster than a loop** for simple cases; there's setup and megamorphic-call overhead.
  Fine for clarity, not a performance technique.
- **Stateful lambdas are a bug**, especially in parallel. `forEach` gives no ordering
  guarantee (`forEachOrdered` does, at a cost).
- **`Collectors.toMap` throws on duplicate keys** unless you pass a merge function — a
  frequent production surprise.
- **Boxing:** use `IntStream`/`LongStream`/`DoubleStream` in hot paths (`mapToInt`, `sum`).
- **`Optional` is for return values**, not fields or parameters. Don't `Optional.get()`
  without checking; use `orElse`/`orElseThrow`/`map`.

**The honest summary:** use streams where they make code clearer, use loops where they
don't, and don't use `parallelStream` without a measurement and a reason.

**Scala:** the collections API *is* this, but better integrated — and `.view`/`.iterator`
are the equivalent of stream laziness. Same lesson about intermediate collections: measured
in `problems/scala/S03AllocationPressure.scala`.
</details>

---

### 14. What does `equals` on a `record` do, and when would you not use one?

<details><summary>Answer</summary>

A **`record`** (JDK 16+) generates: a canonical constructor, accessors named after the
components, and **structural `equals`, `hashCode`, `toString`** over *all* components. Fields
are `final`; the class is `final`; it can't extend anything.

```java
record Order(long id, String customer, List<Item> items) {
    Order {                                        // compact constructor: validation
        if (id <= 0) throw new IllegalArgumentException("id must be positive");
        items = List.copyOf(items);                // defensive copy -> genuinely immutable
    }
}
```

**Use for:** DTOs, value objects, map keys, tuple-ish return values, and as the branches of
a `sealed` hierarchy for pattern matching.

**Don't use when:** you need mutability; you need to extend a class; you need `equals` over
a *subset* of fields (identity semantics — e.g. a JPA entity keyed on `id` only); or you
need to hide the representation, since accessors are public by definition. Note **JPA
entities are a poor fit** — they need a no-arg constructor and mutable fields.

**Shallow immutability is the trap:** `record Order(List<Item> items)` still hands out a
mutable list unless you `List.copyOf` in the compact constructor, as above. The record makes
the *reference* final, not the referent (same point as quiz 01 Q25).

**Scala:** this is `case class`, which Java's records are explicitly converging on — plus
`copy`, named/default parameters, pattern matching, and `unapply`. Worth saying: **records
are Java adopting `case class`**, which makes the Scala transition easier than people expect
for the data-modelling half of a codebase.
</details>

---

### 15. How does `HotSpot`'s GC work? (Eden, survivors, tenured, evacuation, tri-colour, weak generational theory)

<details><summary>Answer</summary>

> This is the same question as **`quiz/04` Q1–Q6**, phrased as one big walkthrough. There's
> a **consolidated 90-second spoken version in `quiz/04` Q23** — use that. The individual
> mechanisms are in:
> - **weak generational hypothesis** → quiz 04 Q1
> - **TLAB / allocation** → Q2
> - **Eden, S0/S1, tenuring threshold, premature promotion** → Q3
> - **minor vs major vs full GC** → Q4
> - **GC roots** → Q5
> - **write barriers, card table, SATB, tri-colour invariant** → Q6
> - **evacuation / copying / fragmentation** → Q15, and G1's region model in quiz 05 Q2
>
> And the measurements are in `problems/gc-labs/` — including the allocation-rate vs
> heap-size table, which is the thing to quote.
</details>

---

### 16. Autoboxing — what's the cost and where does it bite?

<details><summary>Answer</summary>

**Autoboxing** converts a primitive to its wrapper (`int` → `Integer`) implicitly;
unboxing reverses it. It exists because generics erase to `Object` and can't hold
primitives.

**Costs:**
- **Allocation**: an `Integer` is **16 bytes** to carry 4 bytes of data (quiz 05 Q13).
- **Indirection**: a pointer dereference instead of a register read; poor cache locality.
- **`Integer.valueOf` caches only −128..127** (`-XX:AutoBoxCacheMax` raises the upper
  bound). Outside that range, every box allocates.

**Where it bites:**
```java
Map<Integer, Integer> counts = ...;
counts.put(k, counts.get(k) + 1);      // unbox, add, box -- 2 boxes per update
Long sum = 0L;
for (long i = 0; i < 1_000_000; i++) sum += i;   // 1M Long allocations! Use `long`.
```
- **`==` on wrappers compares references.** `Integer a = 1000, b = 1000; a == b` is
  **false**; at `127` it's **true** because of the cache. Devastating and it passes small
  tests. Always `equals` or unbox explicitly.
- **`NullPointerException` on unboxing** a `null` `Integer` — and the stack trace points at
  an innocent-looking arithmetic line.
- `List<Integer>` vs `int[]`: roughly **5–6× the footprint** (measured in S03: 19.1 MB vs
  3.8 MB for 1 M).
- `Collectors.summingInt` vs `reducing` — prefer the primitive-specialised collectors, and
  `IntStream` over `Stream<Integer>`.

**Scala's version is worse in one respect and better in another:** `List[Int]`,
`Option[Int]`, and `(Int, Int)` all box (measured: `List[Int]` of 1 M ≈ 38 MB vs 3.8 MB for
`Array[Int]`), and **value classes silently box** when used generically. But `Array[Int]` is
a true primitive array, `ArraySeq.ofInt` is unboxed and immutable, and `@specialized` /
Scala 3 `opaque type` exist. Quiz 06 D1 has the full list.
</details>

---

### 17. What's the difference between `final`, `finally`, and `finalize`?

<details><summary>Answer</summary>

A classic screening question — quick, and it's really three questions.

- **`final`** — a modifier. On a **variable**: assign once. On a **method**: can't be
  overridden. On a **class**: can't be extended. On a **field**: gives you JMM **freeze
  semantics**, so a correctly-constructed object's final fields are visible to any thread
  without synchronisation (quiz 01 Q25 — the important part).
- **`finally`** — a block that runs whether or not an exception was thrown. For releasing
  resources: unlocking a `ReentrantLock`, `ThreadLocal.remove()`. Prefer
  **try-with-resources** for `AutoCloseable` things. Gotcha: a `return` in `finally`
  discards an in-flight exception — never do it.
- **`finalize()`** — deprecated in JDK 9, **deprecated *for removal*** in JDK 18 (JEP 421, which
  also added `--finalization=disabled`). Precisely: it is still present and still **enabled by
  default** as of the JDK 25/26 line — "deprecated for removal", not removed. Never rely on it:
  no guarantee it runs, needs two GC cycles,
  a single slow finalizer stalls the queue and can OOM you, and it can resurrect objects.
  Use **try-with-resources**, or **`Cleaner`** as a native-memory safety net (quiz 04 Q20).

**Bonus if they're probing:** `finalize` vs `Cleaner` vs `PhantomReference`, and the Scala
answer — `Resource[IO, A]` / ZIO `Scope`, which give *deterministic*, composable,
**cancellation-safe** release, something Java has no real equivalent for (quiz 06 C4).
</details>

---

### 18. Give me the collection you'd choose for each of these.

<details><summary>Answer — a lookup table worth memorising</summary>

| Requirement | Choice |
|---|---|
| general list | `ArrayList` (pre-sized if count known) |
| stack / queue / deque | `ArrayDeque` |
| unique elements, don't care about order | `HashSet` |
| unique elements, insertion order | `LinkedHashSet` |
| unique elements, sorted | `TreeSet` |
| key→value lookup | `HashMap` |
| key→value, insertion order | `LinkedHashMap` |
| key→value, sorted / range queries | `TreeMap` |
| **LRU cache** | `LinkedHashMap` with `removeEldestEntry` — or **Caffeine** in production |
| enum keys | `EnumMap` / `EnumSet` |
| concurrent map | `ConcurrentHashMap` |
| concurrent sorted map | `ConcurrentSkipListMap` |
| producer–consumer handoff | `ArrayBlockingQueue` (**bounded**) |
| many readers, near-zero writes | `CopyOnWriteArrayList` |
| priority ordering | `PriorityQueue` (**unbounded** — beware) |
| scheduled/delayed work | `DelayQueue` |
| immutable snapshot to share | `List.of` / `Map.copyOf` |
| count occurrences | `HashMap<K, Integer>` + `merge(k, 1, Integer::sum)`, or `LongAdder` values if concurrent |
| bidirectional lookup | two maps, or Guava `BiMap` |

**Two things to say alongside:**
1. **The concurrent collections are not a substitute for reasoning.** Each *operation* is
   atomic; **combinations are not** — `containsKey` + `put` is still a race (quiz 01 Q11,
   measured in P04: 8 threads, 8 redundant computations).
2. **Bound anything that can grow.** `PriorityQueue`, `LinkedBlockingQueue`, and
   `LinkedTransferQueue` are unbounded by default, and an unbounded queue is a deferred
   outage (quiz 02 Q2).
</details>

---

## Score yourself

Q1, Q2, Q3 and Q9 are the ones from the actual bank — know those cold, including the two
crib-note corrections in Q2, which are a cheap opportunity to demonstrate precision.
Q4, Q5 and Q6 are the near-certain follow-ups. Runnable demonstrations of Q2, Q5 and Q6 are
in `problems/java/P11Collections.java`.
