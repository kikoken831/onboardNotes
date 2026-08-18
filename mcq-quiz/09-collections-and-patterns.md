# MCQ 09 — Collections, Design Patterns & Java Fundamentals

16 questions. Source: [`quiz/09`](../quiz/09-collections-and-patterns.md).
Runnable demonstrations in `problems/java/P11Collections.java`.

---

### Q1. When is the Builder pattern the right choice?

- **A.** Any class with more than one field
- **B.** When the object needs to be mutable after construction
- **C.** Four or more parameters, several optional, and you want an immutable object with validated invariants
- **D.** When you need multiple inheritance

<details><summary>Answer</summary>

**C.** It replaces the telescoping constructor —
`new Order(id, customer, items, null, null, discount, null, true)` — where you can't tell what the
nulls are and two adjacent same-typed parameters are silently swappable.

Key benefits: named steps, one place (`build()`) for cross-field validation so an invalid object
can't exist, and **immutability** — all fields `final`, so safely publishable across threads with no
synchronisation.

Costs: more code (mitigate with Lombok `@Builder`), and required-field errors move from **compile
time to runtime** — that's the real trade.

- **A** is over-application; a builder on a 2-field class is ceremony.

→ `quiz/09` Q1
</details>

---

### Q2. `TreeMap` vs `HashMap` — which is faster for `put` and `get`?

- **A.** `HashMap` — O(1) average vs `TreeMap`'s O(log n); measured ~8× faster on `put`
- **B.** `TreeMap`, because a balanced tree avoids hash collisions
- **C.** They're equivalent
- **D.** `TreeMap` for small maps, `HashMap` for large ones

<details><summary>Answer</summary>

**A.** Measured on 200,000 shuffled keys: `HashMap` **8 ms put / 2 ms get** vs `TreeMap` **64 ms /
51 ms**. At n = 200k that's ~17 comparisons per operation, each a virtual `compareTo` call.

**This is the crib-note error to be ready for.** "TreeMap is good for frequent add/remove" is
**backwards as a speed claim**. The defensible version: if you need data **kept in sorted order**
while it's frequently modified, `TreeMap` maintains that incrementally at O(log n), which beats
re-sorting a list on every change. **The advantage is the ordering, not the mutation speed.**

→ `quiz/09` Q2, measured in `P11Collections.java` Part 1
</details>

---

### Q3. So what *is* the reason to choose `TreeMap`?

- **A.** Lower memory use
- **B.** Support for null keys
- **C.** Thread safety
- **D.** Sorted iteration and `NavigableMap` range operations — `floorKey`, `ceilingKey`, `subMap`, `headMap`

<details><summary>Answer</summary>

**D.** `HashMap` cannot do range queries or nearest-match at any price. Verified:
`floorKey(35) = 30`, `ceilingKey(35) = 40`, `subMap(20, 45) = {20,30,40}`.

- **C** — neither is thread-safe (that's `ConcurrentHashMap` / `ConcurrentSkipListMap`).
- **B** is backwards, and it's the other half-wrong crib note: **`TreeMap` rejects a null KEY**
  (NPE under natural ordering) but **null values are fine**; `HashMap` allows one null key.
- **A** is arguable, not a slam dunk — `TreeMap` avoids `HashMap`'s partly-empty bucket array, but
  each node carries three references plus a colour bit.

If you want *insertion* order rather than sorted order, that's **`LinkedHashMap`** — O(1) like
`HashMap` (measured: nearly free), and the basis of an LRU cache.

→ `quiz/09` Q2, measured in `P11Collections.java` Part 2
</details>

---

### Q4. What is the load factor's role in a `HashMap`?

- **A.** It caps the number of entries
- **B.** When `size > capacity × loadFactor` (default 0.75), the table doubles and everything is rehashed
- **C.** It sets the maximum chain length before treeification
- **D.** It controls how the hash is spread across bits

<details><summary>Answer</summary>

**B.** Default capacity 16, load factor 0.75 → resize at 12 entries. Resizing rehashes everything,
so **pre-size your maps** (`new HashMap<>(expected / 0.75f + 1)`) when you know the count.

- **C** is `TREEIFY_THRESHOLD` (8, with a table of at least 64; reverts at 6).
- **D** is the spread function `h ^ (h >>> 16)`, which mixes high bits down because the index only
  uses the low bits (`hash & (length - 1)` — a mask, valid only because the length is a power of two).

→ `quiz/09` Q4
</details>

---

### Q5. You override `equals` but not `hashCode`, then use the class as a `HashMap` key. What happens?

- **A.** `map.get(equalKey)` returns `null`, and a `HashSet` can contain two "equal" elements
- **B.** A compile error
- **C.** It works, but slowly
- **D.** A `ClassCastException` at runtime

<details><summary>Answer</summary>

**A.** Verified: `equals` returned `true`, `map.get` returned **null**, and a `HashSet` grew to
**size 2** after adding two equal objects. Two equal objects land in different buckets, so lookup
never finds the match.

The contract: if `a.equals(b)` then `a.hashCode() == b.hashCode()` — mandatory. The converse isn't
required (collisions are legal).

Practically: use **`record`** (JDK 16+), which generates both correctly, or Lombok, or your IDE.

→ `quiz/09` Q5, measured in `P11Collections.java` Part 3 Case A
</details>

---

### Q6. You mutate a field used by `hashCode` *after* putting the object in a `HashMap`. What's the result?

- **A.** The map rehashes automatically on next access
- **B.** The map throws `ConcurrentModificationException`
- **C.** The entry becomes unreachable **and unremovable** — `get`, `containsKey` and `remove` all fail, but `size()` still counts it
- **D.** Only iteration order changes

<details><summary>Answer</summary>

**C.** The most striking demonstration in the pack. Verified, using the *same object reference*:
`get` → `null`, `containsKey` → `false`, `remove` → `null`, `size()` → **1**, and iteration still
shows the entry.

The object sits in the bucket computed from its **old** hash; lookups check the **new** bucket.

**This is why map keys must be immutable.** Same argument applies to `HashSet` membership, and it's
why JPA entities keyed on mutable fields are a persistent bug source.

→ `quiz/09` Q5, measured in `P11Collections.java` Part 3 Case C
</details>

---

### Q7. `hashCode()` that returns a constant — is it legal, and what's the cost?

- **A.** Illegal; the JVM rejects it
- **B.** Legal and correct, but catastrophic for performance — measured 473× slower
- **C.** Legal and harmless
- **D.** Legal, and Java 8's treeification makes it as fast as a good hash

<details><summary>Answer</summary>

**B.** Contract-compliant (equal objects have equal hashes) and every entry lands in **one bucket**.
Measured: **10 ms vs 4734 ms** — 473× slower.

- **D** is half-right: Java 8+ **does** treeify the bin, so it's O(log n) rather than O(n) — but
  still 473× worse than O(1). Pre-Java-8 this was a genuine denial-of-service vector via crafted
  collisions.

→ `quiz/09` Q5, measured in `P11Collections.java` Part 3 Case D
</details>

---

### Q8. `ArrayList` vs `LinkedList` for frequent insertion in the middle — which wins?

- **A.** `LinkedList`, because unlinking is O(1)
- **B.** `LinkedList`, but only above 10,000 elements
- **C.** They're equivalent
- **D.** `ArrayList` — measured 28× faster, because you must traverse to the position anyway and `arraycopy` is a single vectorised move

<details><summary>Answer</summary>

**D.** Measured: 20,000 middle-insertions took **8 ms (ArrayList)** vs **223 ms (LinkedList)**. And
indexed access: 100,000 `get(i)` on `ArrayList` took 3 ms; just 20,000 on `LinkedList` took 534 ms.

**A is the textbook answer and it's wrong in practice**, because: you must **traverse** to the
position (O(n)) so the O(1) unlink is irrelevant — the `List` API never hands you the node;
`arraycopy` is one vectorised memory move; and **cache misses dominate** at realistic sizes.
Constant factors beat asymptotics here.

Use `ArrayList` for lists and `ArrayDeque` for queues/stacks. **`LinkedList` is effectively never
the right answer.**

→ `quiz/09` Q6, measured in `P11Collections.java` Part 4
</details>

---

### Q9. `List.contains(x)` inside a loop over n elements. What's the complexity, and why does it matter?

- **A.** O(n) — fine
- **B.** O(n log n)
- **C.** O(n²) — and measured 114× slower than a `Set`, growing linearly with n
- **D.** O(1), because `contains` is optimised

<details><summary>Answer</summary>

**C.** `List.contains` is O(n), so inside an n-iteration loop it's O(n²). Measured with 20,000
lookups: **98.7 ms (List) vs 0.9 ms (Set)**.

**This is the most common real performance bug in ordinary business code**, and it hides beautifully:
fast with 100 elements in a unit test, quadratic with 50,000 in production.

Whenever you see `.contains()` inside a loop, or a nested loop over two collections, reach for a
`Set` or `Map`. Same shape as the **N+1 query problem** — correct, and accidentally quadratic.

→ `quiz/09` Q3, measured in `P11Collections.java` Part 5
</details>

---

### Q10. Which collection for a stack?

- **A.** `ArrayDeque`
- **B.** `java.util.Stack`
- **C.** `LinkedList`
- **D.** `Vector`

<details><summary>Answer</summary>

**A.** `ArrayDeque` is the right stack *and* queue — array-backed, O(1) at both ends, good locality.

- **B** and **D** are legacy: `Stack` extends `Vector` and is **synchronised**, so you pay for
  locking you don't need.
- **C** allocates a node per element and chases pointers.

Naming `ArrayDeque` over `Stack` is a cheap credibility point in a coding round.

→ `quiz/09` Q3, Q18
</details>

---

### Q11. `Comparator.comparing(Order::qty)` vs `Comparator.comparingInt(Order::qty)` — why prefer the latter?

- **A.** It's more readable
- **B.** It's stable, unlike `comparing`
- **C.** It handles nulls correctly
- **D.** It avoids boxing the `int` on every comparison

<details><summary>Answer</summary>

**D.** `comparing` boxes to `Integer` on every comparison — irrelevant once, real in a hot sort.

Also worth knowing about comparators: **never subtract** (`a - b` overflows) and **never use a
two-way ternary** (`a > b ? 1 : -1`), because that never returns 0 and violates the contract —
TimSort will throw `IllegalArgumentException: Comparison method violates its general contract!`,
which is a real production error. Use `Integer.compare(a, b)`.

- **B** — stability is a property of the *sort* (TimSort is stable), not the comparator.

→ `quiz/09` Q7
</details>

---

### Q12. Where does Spring use the Proxy pattern, and why does that matter?

- **A.** In `@Autowired` field injection
- **B.** For `@Transactional`, `@Async` and `@Cacheable` — which is why self-invocation silently does nothing
- **C.** In the `DispatcherServlet`
- **D.** For component scanning

<details><summary>Answer</summary>

**B.** The proxy wraps the bean, so calling `this.doWork()` from another method of the same bean
**bypasses it** and the annotation has no effect. Only `public` methods are advised in the default
proxy mode, and `final` classes can't be CGLIB-proxied.

Knowing *why* — that it's the Proxy pattern and `this` isn't the proxy — is the difference between
memorising the gotcha and understanding it. It also explains why a debugger shows you
`PositionService$$EnhancerBySpringCGLIB`.

- **C** is Front Controller (also a real pattern, different one).

→ `quiz/09` Q8, `quiz/10` Q5, Q12
</details>

---

### Q13. Why has the industry largely moved to unchecked exceptions?

- **A.** Checked exceptions don't compose — they break lambdas and force either `throws` pollution or `catch (Exception e) {}`
- **B.** They're faster
- **C.** The compiler no longer enforces them
- **D.** They produce better stack traces

<details><summary>Answer</summary>

**A.** `Function.apply` can't throw checked exceptions, so they break every functional interface. They
leak implementation details up through abstractions, and in practice the pressure produces either
`throws` on every signature or — far worse — swallowed exceptions.

The strongest single piece of evidence: **Spring deliberately wraps `SQLException` into the unchecked
`DataAccessException` hierarchy.** A major framework converting checked to unchecked on purpose.

The exception where you have no choice: **`InterruptedException`**, which must be propagated or the
flag restored.

Scala's answer: put failure **in the return type** — `Either`, `Try`, `Validated`, or ZIO's typed
`E`. That composes the way checked exceptions were supposed to, without the syntactic tax.

→ `quiz/09` Q11
</details>

---

### Q14. `record Order(long id, List<Item> items)` — is it immutable?

- **A.** Yes, fully — records are deeply immutable
- **B.** No, records are mutable by design
- **C.** Only shallowly — the `items` reference is final, but the list it points at may be mutable
- **D.** Yes, because records copy their components

<details><summary>Answer</summary>

**C.** The record makes the *reference* final, not the referent. Fix it in the compact constructor:
```java
record Order(long id, List<Item> items) {
    Order { items = List.copyOf(items); }
}
```

This is the same "final/`val` isn't immutability" error that appears throughout the pack — with
`final List` in Java, `val buf = mutable.ListBuffer()` in Scala, and `volatile Map` in a Spring bean.

Records are otherwise excellent for DTOs, value objects and map keys. **Poor fit for JPA entities**
(they need a no-arg constructor and mutable fields) and for identity-semantics types.

→ `quiz/09` Q14
</details>

---

### Q15. Which SOLID principle has the most practical payoff?

- **A.** Single Responsibility
- **B.** Dependency Inversion
- **C.** Liskov Substitution
- **D.** Open/Closed

<details><summary>Answer</summary>

**B.** Depend on abstractions, and the abstraction belongs to the *consumer*. It's what makes code
testable — the reason you define your **own** client interface and put the vendor's Feign client
behind it, so the service unit-tests with a stub and no container.

All five matter, but if you're asked which you'd never compromise, this is the defensible answer.

And the mature caveat to add: **SOLID is a set of heuristics for managing change, not laws.** Applied
mechanically you get an interface per class and a factory per interface, which is worse than the
problem. Also worth noting several principles are less pressing in FP because the units are smaller —
a function has single responsibility by default.

→ `quiz/12` Q17
</details>

---

### Q16. `final`, `finally`, `finalize` — which one gives you a memory-model guarantee?

- **A.** None of them
- **B.** `finally`
- **C.** `finalize`
- **D.** `final`

<details><summary>Answer</summary>

**D.** `final` **fields** get JSR-133 **freeze semantics**: any thread seeing a reference to a
correctly-constructed object sees the correct final-field values, **without synchronisation**. That's
what makes immutable objects safe to share freely.

The load-bearing caveat: "correctly constructed" means **`this` did not escape the constructor**.

- **B** is a block that runs regardless of exceptions (and a `return` in `finally` discards an
  in-flight exception — never do it).
- **C** is deprecated and unusable: no guarantee it runs, needs two GC cycles, one slow finalizer
  stalls the queue and can OOM you, and it can resurrect objects. Use try-with-resources, or
  `Cleaner` as a native-memory safety net.

→ `quiz/09` Q17, `quiz/01` Q25
</details>

---

## Score yourself

- **13+** — good.
- **Q2 and Q3 are the ones from the actual question bank**, including both crib-note corrections
  (speed claim backwards; nulls are keys-only). Being precise there is a cheap way to demonstrate
  rigour.
- **Q6 and Q9 are the two most memorable measurements** — the unremovable map entry and the 114×
  `contains` gap. Both are good things to have a number for.
