# MCQ 16 — Core Java & OOP

22 questions. Source: [`quiz/16`](../quiz/16-java-core-and-oop.md).

---

### Q1. `Integer a = 128, b = 128; a == b` evaluates to?

- **A.** `true` — autoboxing returns the same cached object
- **B.** A compile error — you can't use `==` on wrappers
- **C.** `false` — 128 is outside the wrapper cache, so two distinct objects
- **D.** `true` on 64-bit JVMs, `false` on 32-bit

<details><summary>Answer</summary>

**C.** The `Integer` cache covers **−128..127**. At 127 the same expression is `true`; at 128 it's
`false`. That flip is what makes this the classic trap — the bug survives testing because small test
values are cached.

`Long`, `Short`, `Byte` and `Character` (0–127) cache the same low range; `Boolean` caches both values.
Only the **upper** bound of `Integer`'s is tunable, via `-XX:AutoBoxCacheMax`.

Always `equals` (or `Objects.equals`) on wrappers. And remember unboxing a `null` wrapper throws NPE on
an innocuous-looking arithmetic line.

→ `quiz/16` Q1
</details>

---

### Q2. Which is true of `protected`?

- **A.** Subclasses only — package classes cannot access it
- **B.** Only the declaring class and its direct parent
- **C.** It's identical to default (package-private)
- **D.** Package classes and subclasses can both access it

<details><summary>Answer</summary>

**D.** This is the detail most people get wrong. `protected` = **package + subclasses**, so it is
strictly *wider* than default, not a sibling of it. A same-package class needs no inheritance
relationship at all.

Full ordering, narrowest to widest: `private` → default → `protected` → `public`.

Second subtlety: a `protected` member is accessible in a subclass **through a reference of the subclass
type**, not through an arbitrary superclass reference from another package.

→ `quiz/16` Q2
</details>

---

### Q3. Can a class be both `abstract` and `final`?

- **A.** No — `abstract` requires extension, `final` forbids it; direct contradiction
- **B.** Yes, but only for nested classes
- **C.** Yes, and it means the class has only static members
- **D.** Only from JDK 17 with `sealed`

<details><summary>Answer</summary>

**A.** `abstract` means "must be extended to be useful", `final` means "cannot be extended". The
compiler rejects the combination. Same for `abstract final` methods, and `abstract` + `private` or
`static`.

**D** points at something real but gets it wrong: `sealed` (JDK 17) gives you the genuine middle
ground — a **closed** hierarchy where you name the permitted subtypes. That's what enables exhaustive
`switch` pattern matching, and it's the right tool for an ADT.

→ `quiz/16` Q5
</details>

---

### Q4. Which is **not** polymorphic in Java?

- **A.** An overridden instance method
- **B.** A `static` method
- **C.** A method called through an interface reference
- **D.** An abstract method implemented by a subclass

<details><summary>Answer</summary>

**B.** Static methods are **hidden**, not overridden, and dispatch is by **compile-time** type. So a
`static` method called through a superclass-typed reference runs the *superclass* version, regardless of
the runtime object.

Also not polymorphic: **fields** (hidden, resolved by declared type) and `private`/`final` methods
(statically bound). All three are standard trick questions.

Worth distinguishing in the answer: **overriding** is runtime polymorphism (dynamic dispatch);
**overloading** is compile-time resolution and strictly isn't polymorphism at all.

→ `quiz/16` Q4
</details>

---

### Q5. A `record Order(long id, List<Item> items)` — is it immutable?

- **A.** Yes, records are deeply immutable
- **B.** Yes, because records defensively copy all components automatically
- **C.** No, records generate setters
- **D.** Only shallowly — the `items` reference is final, but the list itself may be mutable

<details><summary>Answer</summary>

**D.** The record makes the *reference* final; it does nothing about the referent. Fix it in the compact
constructor:
```java
record Order(long id, List<Item> items) {
    Order { items = List.copyOf(items); }
}
```

This is the same "`final` isn't immutability" error that recurs across the pack: `private final List` in
Java, `val buf = mutable.ListBuffer()` in Scala, and a `volatile Map` field in a Spring bean.

→ `quiz/16` Q5, Q6; `quiz/09` Q14
</details>

---

### Q6. What does a `transient` field hold after deserialisation?

- **A.** The value assigned in the constructor
- **B.** The value it had when serialised
- **C.** Its type default — `null`, `0`, or `false`
- **D.** It throws `NotSerializableException`

<details><summary>Answer</summary>

**C.** And the reason matters: **deserialisation does not run the constructor**, so constructor-assigned
defaults never happen. You get the raw type default.

That's why a `transient` field needing a value must be restored in a custom `readObject`, or computed
lazily on first access.

Contrast the three modifiers that get asked together: `transient` is **serialisation**, `volatile` is
**concurrency**, `static` is **scope**. Their only overlap is that neither `transient` nor `static`
fields are serialised — so `static transient` is redundant.

→ `quiz/16` Q7
</details>

---

### Q7. You add a field to a `Serializable` class with no declared `serialVersionUID`. What happens to previously serialised data?

- **A.** It still deserialises; the new field gets its default
- **B.** Deserialisation fails with `InvalidClassException`, because the compiler-computed UID changed
- **C.** It deserialises but silently corrupts the new field
- **D.** Nothing — `serialVersionUID` only matters across JVM versions

<details><summary>Answer</summary>

**B.** If you don't declare one, the compiler **computes** it from the class structure — name, fields,
methods, interfaces. Any structural change alters it, and a mismatch throws `InvalidClassException`
outright.

**So always declare it explicitly:** `private static final long serialVersionUID = 1L;` — then *you*
decide what counts as a breaking change rather than the compiler deciding by accident. With it
declared, **A** becomes the correct behaviour: adding a field is compatible and old streams leave it at
its default.

The senior addendum: Java serialisation is widely regarded as a mistake — it's a hidden constructor
that bypasses invariants and a known RCE vector. For anything crossing a process boundary, use an
explicit schema (JSON/Avro/Protobuf).

→ `quiz/16` Q8
</details>

---

### Q8. `String` `+` inside a loop is what complexity?

- **A.** O(n²) — each concatenation copies the accumulated string
- **B.** O(n) — the compiler converts it to a `StringBuilder`
- **C.** O(n log n)
- **D.** O(1) amortised, because of the string pool

<details><summary>Answer</summary>

**A.** `String` is immutable, so `result += s` allocates a new string containing everything so far,
every iteration.

**B** is the trap and it's half-true: the compiler *does* optimise `+` **within a single expression**
(via `StringConcatFactory`/`invokedynamic` since JDK 9). It cannot do so **across loop iterations**,
because each one is a separate statement. Use `StringBuilder` explicitly.

And use **`StringBuilder`**, not `StringBuffer` — the latter synchronises every method, which is
useless for a thread-confined local and is legacy.

→ `quiz/16` Q10
</details>

---

### Q9. When is `finalize()` guaranteed to run?

- **A.** Immediately before the object's memory is reclaimed
- **B.** At the next full GC after the object becomes unreachable
- **C.** Never — there is no guarantee it runs at all
- **D.** On JVM shutdown, for all remaining objects

<details><summary>Answer</summary>

**C.** That's the whole answer to "when is it called". No guarantee it ever runs; the JVM may exit
first. Plus: it needs at least two GC cycles, a single slow finalizer stalls the queue and can OOM you
with reclaimable garbage, it can **resurrect** the object, and exceptions in it are swallowed.

Deprecated in **JDK 9**, deprecated *for removal* in JDK 18 (JEP 421), which also added
`--finalization=disabled`. Treat it as gone.

**Use instead:** `try`-with-resources / `AutoCloseable` (the answer 95% of the time — deterministic,
and `close()` failures become *suppressed* exceptions rather than masking the original), or `Cleaner`
as a native-memory safety net.

The framing: **GC manages memory, not resources.**

→ `quiz/16` Q12
</details>

---

### Q10. `WeakHashMap` — what is held weakly?

- **A.** Both keys and values
- **B.** The **key** only; values are held strongly
- **C.** The value only
- **D.** Neither — the whole map is weakly reachable

<details><summary>Answer</summary>

**B.** And it's the source of a real leak: if a **value references its own key**, the key stays strongly
reachable through the value, so the entry never clears.

Measured in `problems/gc-labs/L02Leak.java`, where exactly that leak was planted and OOM'd a 128 MB
heap in about 2 seconds.

**`ThreadLocal` has the identical shape** — weak key, strong value — which is why an unremoved
`ThreadLocal` on a pooled thread leaks (measured 22 MB → 4 MB after `remove()` in `P08`).

So: weak references solve "don't keep this alive just because I'm tracking it". They do **not** solve
"cache with a bound" — that's Caffeine.

→ `quiz/16` Q13; `quiz/04` Q21
</details>

---

### Q11. Difference between `Class.forName("X")` and `classLoader.loadClass("X")`?

- **A.** None — `forName` just delegates to `loadClass`
- **B.** `forName` works only for classes on the bootstrap classpath
- **C.** `loadClass` initialises; `forName` only links
- **D.** `forName` **initialises** the class (runs static blocks); `loadClass` does not

<details><summary>Answer</summary>

**D.** `forName` loads, links **and initialises**. `loadClass` stops before initialisation, so static
blocks don't run until the class is first actively used.

That's why the old JDBC idiom `Class.forName("com.mysql.jdbc.Driver")` worked — it ran the static block
that registered the driver. With `loadClass` it would silently not register. (Since JDBC 4.0,
`ServiceLoader` makes the call unnecessary.)

Use the three-arg `Class.forName(name, false, loader)` for `forName` semantics *without* initialisation.
Two documented details almost no quiz has: requesting an **array** class — `forName("[Lcom.Foo;", true,
loader)` — loads `Foo` but **never initialises** it regardless of the flag; and the JDK 9
`Class.forName(Module, String)` overload neither links nor initialises, and **returns `null`** on failure
instead of throwing.

Related pair worth knowing: **`ClassNotFoundException`** is checked, thrown on reflective loading
failure; **`NoClassDefFoundError`** is an `Error`, thrown when the class was present at compile time but
is missing or failed initialisation at runtime.

→ `quiz/16` Q14
</details>

---

### Q12. `java.lang.reflect.Proxy` can proxy which of these?

- **A.** Any class, final or not
- **B.** Any non-final class
- **C.** **Interfaces only**
- **D.** Only classes annotated `@Proxyable`

<details><summary>Answer</summary>

**C.** JDK dynamic proxies implement **interfaces**. You cannot proxy a concrete class — for that you
need bytecode subclassing via **CGLIB** or **ByteBuddy**, which in turn requires the class and method
not to be `final`.

**Why this matters practically:** it's how Spring AOP implements `@Transactional`, `@Async`,
`@Cacheable` and `@PreAuthorize` — JDK proxy if the bean implements an interface, CGLIB subclass
otherwise.

And it explains the bug class: **self-invocation bypasses the proxy**, so `this.doWork()` means
`@Transactional` silently does nothing. Only `public` methods are advised, and the injected object isn't
the one you wrote (`Service$$EnhancerBySpringCGLIB`).

Two details that separate a read-about-it answer from a used-it one. The *structural* reason JDK proxies
can't wrap a class: the generated proxy is `final` and already **extends `java.lang.reflect.Proxy`**, so
there's no superclass slot left. And **`equals`, `hashCode` and `toString` are dispatched to your
`InvocationHandler`** (with `Method.getDeclaringClass() == Object.class`) — so a handler that blindly
delegates every call can break equality — while other `Object` methods (`getClass`, `wait`, `notify`) are
not overridden.

→ `quiz/16` Q16; `quiz/10` Q4, Q12
</details>

---

### Q13. What's the main advantage of `MethodHandle` over core reflection?

- **A.** The access check happens once at lookup, not on every invocation — and it's designed to be JIT-inlined
- **B.** It can access private members without `setAccessible`
- **C.** It works without a `SecurityManager`
- **D.** It's the only way to invoke a constructor reflectively

<details><summary>Answer</summary>

**A.** Reflection re-checks permissions on **every** `invoke`; a `MethodHandle` checks once, when the
`Lookup` creates it. Combined with a typed `MethodType` (and `invokeExact`, which permits no
conversions), the JIT can treat an invocation through a `static final` handle almost like a direct call.

**B** is wrong in an interesting way: the `Lookup` **carries** the access rights of the class that
created it, so it can't grant access the creating context doesn't already have.

**Why it exists:** to support **`invokedynamic`**, which underpins **lambdas** (`LambdaMetafactory`),
string concatenation since JDK 9, and records. So every lambda you write is a call site linked by a
method handle.

Sibling to know: **`VarHandle`** (JDK 9+) for field/array access with explicit memory-ordering modes —
the supported replacement for `sun.misc.Unsafe`.

→ `quiz/16` Q17
</details>

---

### Q14. Why should a nested class be `static` unless you need otherwise?

- **A.** Static nested classes are faster to instantiate
- **B.** A non-static inner class holds an implicit reference to the enclosing instance, so it can pin it in memory
- **C.** Non-static inner classes can't implement interfaces
- **D.** Only static nested classes can be generic

<details><summary>Answer</summary>

**B.** `Outer.this` is an implicit field. Hand an inner-class instance to a long-lived registry and
you've pinned the entire enclosing object — the classic Android/Swing leak.

Measured in the same shape in `problems/gc-labs/L02Leak.java`: a `List<Runnable>` of **capturing
lambdas** retained every session object, because the lambda captured it.

The four kinds: **static nested** (no outer reference — the default choice), **inner** (implicit outer
reference), **local** (in a method body), **anonymous** (declared and instantiated at once). A **lambda**
is different again — an `invokedynamic` call site, no class file per instance, and `this` refers to the
enclosing instance.

→ `quiz/16` Q18
</details>

---

### Q15. `map.get(k)` returns `null`. What do you know?

- **A.** The key is definitely absent
- **B.** A `NullPointerException` was suppressed
- **C.** The map is empty
- **D.** For a `HashMap`, either the key is absent **or** it's mapped to `null` — ambiguous

<details><summary>Answer</summary>

**D.** `HashMap` permits one null key and many null values, so `get() == null` is genuinely ambiguous.
`containsKey` distinguishes them.

But note **`ConcurrentHashMap` and `TreeMap` forbid null values**, so there `get() == null` *does* mean
absent — and CHM forbids them precisely *because* the ambiguity would be unresolvable in a map where you
can't safely check-then-act.

**And the important part:** `containsKey` then `get` is **two lookups** and, under concurrency, **a
race** — measured in `P04`: 8 threads, `expensive()` ran 8 times, 7 results discarded. Prefer
`getOrDefault`, `computeIfAbsent`, or `Optional.ofNullable(map.get(k))`.

→ `quiz/16` Q19
</details>

---

### Q16. `ConcurrentModificationException` is most often caused by?

- **A.** Removing from a collection inside a for-each loop, in a single thread
- **B.** Two threads writing to a `HashMap`
- **C.** Iterating a `CopyOnWriteArrayList`
- **D.** Calling `size()` during iteration

<details><summary>Answer</summary>

**A.** Despite the name, the most common cause has nothing to do with threads. The iterator tracks a
`modCount`; structurally modifying the collection during a for-each throws on the next `next()`.

Fix with `Iterator.remove()`, `removeIf()`, or by collecting and removing afterwards.

**C** is wrong and worth knowing why: `CopyOnWriteArrayList` iterates an **immutable snapshot**, so it
never throws CME — that's fail-**safe**. Same for `ConcurrentHashMap`'s weakly consistent iterators.

And note fail-fast detection is **best-effort**, not guaranteed — never write logic that relies on the
exception being thrown.

→ `quiz/16` Q19
</details>

---

### Q17. Which is checked?

- **A.** `IllegalArgumentException`
- **B.** `NullPointerException`
- **C.** `InterruptedException`
- **D.** `OutOfMemoryError`

<details><summary>Answer</summary>

**C.** The rule: **everything under `Exception` except `RuntimeException` and its subclasses is
checked.** `InterruptedException` extends `Exception` directly.

```
Throwable
├── Error                unchecked  (OutOfMemoryError, StackOverflowError)
└── Exception            CHECKED
    └── RuntimeException unchecked  (NPE, IllegalArgument, IllegalState)
```

`InterruptedException` is also the one where you have no choice about handling it properly:
**propagate it, or restore the flag** with `Thread.currentThread().interrupt()`. Catching it *clears*
the interrupt status, so swallowing it destroys the only cancellation signal the JVM has.

Note "checked" is a **compiler** rule, not a JVM one — which is why Scala and Kotlin ignore it entirely.

→ `quiz/16` Q20; `quiz/01` Q14
</details>

---

### Q18. An overriding method may declare which checked exceptions?

- **A.** Any it likes
- **B.** The same or **narrower** — never broader
- **C.** Only the exact same set
- **D.** None; overrides can't declare `throws`

<details><summary>Answer</summary>

**B.** An override may declare **fewer or narrower** checked exceptions, never broader. That's Liskov
substitution enforced by the compiler: code written against the supertype must not be surprised.

Related `throw`/`throws` details worth having: `throw` is a **statement** that raises;
`throws` is a **signature clause** that declares. `throw null` compiles and throws NPE. And **more
precise rethrow** (JDK 7+) lets `catch (Exception e) { throw e; }` compile in a method declaring only
the specific checked types, because the compiler tracks what can actually be thrown.

And the `finally` hazard: a `return` or `throw` in `finally` **discards** an in-flight exception. Never
do it — `try`-with-resources handles this properly via suppressed exceptions.

→ `quiz/16` Q21
</details>

---

### Q19. `List<String>` and `List<Integer>` at runtime:

- **A.** Are the **same class** — the type parameter is erased
- **B.** Are different classes
- **C.** Differ only in their `getClass().getName()`
- **D.** Are the same class only if compiled with `-Xlint:unchecked`

<details><summary>Answer</summary>

**A.** Generics are compile-time only. The compiler checks types, inserts casts, and **erases** the
parameters — `T` becomes `Object` (or its leftmost bound). One `List` class at runtime.

Consequences to be able to list: `new T()` and `new T[10]` are illegal; you can't
`instanceof List<String>`; you can't catch a generic exception type; you can't overload on
`List<String>` vs `List<Integer>` (same erasure, same signature); and you can't use primitives, hence
boxing everywhere.

**Why erasure:** backwards compatibility with pre-generic class files.

**The workaround:** pass a `Class<T>` token, or capture the type via an anonymous subclass — a generic
**supertype** *is* retained in the class file, which is how Jackson's `TypeReference` and Spring's
`ParameterizedTypeReference` work.

→ `quiz/16` Q22
</details>

---

### Q20. Given `List<? extends Number> list`, which compiles?

- **A.** `list.add(1);`
- **B.** `list.clear(); list.add(0);`
- **C.** `list.add(null); list.add(1.0);`
- **D.** `Number n = list.get(0);`

<details><summary>Answer</summary>

**D.** **PECS — Producer `Extends`, Consumer `Super`.** With `? extends Number` you can **read** a
`Number` out but **cannot add** anything except `null`, because the actual list might be
`List<Integer>` and adding a `Double` would corrupt it.

For adding you need `List<? super Integer>` — then you can `add(1)`, and reads come back as `Object`.

**Why wildcards exist at all:** generics are **invariant**. `List<Integer>` is *not* a `List<Number>`,
and it must not be:
```java
List<Number> nums = ints;   // if legal...
nums.add(3.14);             // ...you'd put a Double in a List<Integer>
```
**Java's arrays *are* covariant and therefore unsound** — hence `ArrayStoreException` at runtime.
Generics chose compile-time safety instead.

→ `quiz/16` Q23
</details>

---

### Q21. Best way to implement a Singleton in Java?

- **A.** A single-element `enum`
- **B.** A `synchronized` accessor
- **C.** Double-checked locking with a `volatile` field
- **D.** A static field initialised in a static block

<details><summary>Answer</summary>

**A.** `public enum Registry { INSTANCE; … }` — one instance guaranteed by the JVM,
**serialisation-safe for free** (no `readResolve`), and **reflection-proof**, because you cannot
construct an enum reflectively.

That defeats the two attacks that break **C**, **B** and **D**: someone calling the private constructor via
`setAccessible(true)`, and deserialisation creating a second instance.

**C** is correct but must have `volatile` or it's broken, and it's more code for less safety. The
**holder idiom** is the best non-enum option — lazy, thread-safe by class-init locking, zero
synchronisation on reads.

**The answer that scores best, though, is the design one:** use **dependency injection** instead. A
Spring singleton bean gives you one instance with a managed lifecycle and the dependency declared in the
constructor — the "one instance" property without the global-state, testability and hidden-dependency
problems.

→ `quiz/19` Q3; `quiz/16` Q24
</details>

---

### Q22. Adapter vs Decorator — the distinction?

- **A.** Adapter wraps one object, Decorator wraps many
- **B.** Adapter is creational, Decorator is structural
- **C.** Adapter **changes the interface**; Decorator **keeps the interface** and adds behaviour
- **D.** They're the same pattern under different names

<details><summary>Answer</summary>

**C.** Adapter: *"these two don't fit together"* — same behaviour, different interface
(`InputStreamReader` turning a byte stream into a char stream, `Arrays.asList`). Decorator: *"I want this
plus something"* — same interface, added behaviour (`BufferedReader`,
`Collections.unmodifiableList`).

**B** is wrong: **both are structural.**

The third member of the confusable set is **Proxy** — structurally identical to Decorator, but the
*intent* is **controlling access** (lazy loading, security, remoting) rather than adding functionality.
Spring AOP is technically Proxy while behaving like Decorator, which is why the two blur in practice.

**Why Adapter matters for the BofA role specifically:** it's the design point in `quiz/10` Q1 — define
*your own* client interface and put the other team's Feign client behind an adapter, so their DTOs and
failure modes stay at the boundary.

→ `quiz/19` Q2, Q5, Q6
</details>

---

## Score yourself

- **18+** — the core-Java half is solid.
- **The five worth knowing cold:** Q1 (`Integer` cache), Q7 (`serialVersionUID`), Q9 (`finalize` never
  guaranteed), Q19–Q20 (generics — the source list's biggest omission).
