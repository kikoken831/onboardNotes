# Quiz 16 — Core Java & OOP

25 questions. Covers the core-language and OOP half of the "100 Java Questions" list, plus the
significant gaps that list has (generics, serialisation, modern Java).

Baseline: **JDK 21 LTS** unless stated. Target: 35 min.

---

### 1. `==` vs `.equals()` — and when does `==` work anyway?

<details><summary>Answer</summary>

`==` compares **the bits in the variable**: for primitives the value, for references the **identity**
(same object or not). `.equals()` is a method, so it does whatever the class defines — by default
`Object.equals` *is* `==`, and value classes override it to compare contents.

**The reason this is asked** is that `==` works *sometimes*, which is what makes the bug survive
testing:
```java
String a = "hello", b = "hello";          a == b   // true  — both are the pooled literal
String c = new String("hello");           a == c   // false — new object
Integer x = 127, y = 127;                 x == y   // true  — Integer cache
Integer p = 128, q = 128;                 p == q   // FALSE — outside the cache
```
The `Integer` case is the classic trap: **the wrapper cache covers −128..127**, so the same
expression flips behaviour at 128. `Long`, `Short`, `Byte` and `Character` (0–127) cache the same
low range; `Boolean` caches both values. Only the *upper* bound of `Integer`'s cache is tunable
(`-XX:AutoBoxCacheMax`).

Also: **unboxing a `null` wrapper throws `NullPointerException`**, and the stack trace points at an
innocent-looking arithmetic or comparison line.

**Rules:** `equals` for objects, always. `Objects.equals(a, b)` if either may be null. `==` for
primitives and for deliberate identity checks (`if (other == this)`). Never `==` on wrappers or
strings.
</details>

---

### 2. `public`, `protected`, default, `private` — state them precisely.

<details><summary>Answer</summary>

| Modifier | Same class | Same package | Subclass (other package) | Anywhere |
|---|---|---|---|---|
| `private` | yes | — | — | — |
| *default* (package-private) | yes | yes | — | — |
| `protected` | yes | **yes** | yes | — |
| `public` | yes | yes | yes | yes |

**The detail people miss: `protected` includes package access.** It's not "subclasses only" — it's
"package + subclasses". So `protected` is strictly wider than default, and a same-package class needs
no inheritance relationship to touch it.

Second detail: a `protected` member is accessible in a subclass **through a reference of the subclass
type**, not through an arbitrary superclass reference from another package.

**Defaults to know:** interface members are implicitly `public` (fields also `static final`);
`private` interface methods are legal **from JDK 9**, for sharing code between `default` methods. Top-
level classes can only be `public` or package-private.

**What to say about design:** default to the **most restrictive** modifier that works, and make
fields `private` with accessors only where genuinely needed — that's what makes a class's invariants
enforceable, and it's the encapsulation half of the OOP answer.
</details>

---

### 3. What does `static` actually mean?

<details><summary>Answer</summary>

**Belongs to the class, not to an instance.** One copy, shared, initialised when the class is
initialised.

- **Static field** — one per class (per classloader, which is the subtlety). `static final` with a
  compile-time constant is inlined by the compiler.
- **Static method** — no `this`, so it can't be overridden. It can be **hidden** by a subclass
  declaring the same signature, and dispatch is by *compile-time* type — a genuine gotcha.
- **Static block** — runs once at class initialisation, in source order with static field
  initialisers.
- **Static nested class** — a nested class with no reference to the outer instance (see Q18).
- **Static import** — brings names into scope.

**Where it matters for this pack:** `static` mutable state is **shared across every thread** with no
synchronisation, so it's the same bug as a mutable field on a Spring singleton (`quiz/01` Q24). And
static fields are **GC roots** — a `static Map` used as a cache with no eviction is the single most
common Java memory leak (`quiz/04` Q5, measured in `problems/gc-labs/L02Leak.java`).

**Init order to be able to state:** static fields/blocks run at class init, once, before any
instance exists; instance fields/blocks run per construction, before the constructor body. Class init
is thread-safe — the JVM holds a lock — which is what makes the holder idiom work (`quiz/09`, `P07`).
</details>

---

### 4. How does polymorphism work, with a real example?

<details><summary>Answer</summary>

Two kinds, and naming both is the better answer:

- **Runtime (subtype) polymorphism** — method **overriding**. The method invoked depends on the
  object's *runtime* type, resolved by dynamic dispatch through the vtable. This is what people mean
  by "polymorphism".
- **Compile-time polymorphism** — method **overloading**. Resolved by the compiler from the
  *declared* argument types. Strictly speaking not polymorphism at all, and the distinction is worth
  drawing.

```java
sealed interface Instrument permits Equity, Bond {}
// ...
BigDecimal price(Instrument i) { return i.presentValue(); }   // dispatch by runtime type
```

**Give a real example, not `Animal`/`Dog`.** Good ones from actual work:
- A `PricingStrategy` interface with equity/bond/FX implementations, selected per instrument — the
  caller has one code path.
- `Comparator` — the sort algorithm is fixed, the comparison is polymorphic.
- `RejectedExecutionHandler` — `ThreadPoolExecutor` calls one method, four behaviours.

**What the interviewer is checking:** whether you know it exists to remove conditionals. If your code
has `if (type == EQUITY) … else if (type == BOND)` repeated in five places, polymorphism replaces it
with one dispatch and the compiler helps you find every site.

**Things that are NOT polymorphic:** static methods (hidden, not overridden), fields (hidden, resolved
by compile-time type), and `private`/`final` methods (statically bound). All three are classic trick
questions.
</details>

---

### 5. Interface vs abstract class vs final class — when each?

<details><summary>Answer</summary>

> `quiz/09` Q12 has the full comparison table. Summary plus the bits the source list asks separately.

**Interface** — a capability contract. Multiple inheritance, no instance state, no constructor.
Since JDK 8 it can have `default` and `static` methods; since JDK 9, `private` methods. **The default
choice.**

**Abstract class** — when subclasses genuinely share **state** and constructor logic. Single
inheritance. Can have any access modifier.

**Final class** — cannot be extended. Use for value types (`String`, wrappers, your `case class`
equivalents) and where subclassing would break an invariant. Also enables some JIT optimisation.

**"Can a class be both abstract and final?"** — **No, and that's the point of the question.**
`abstract` means "must be extended", `final` means "cannot be extended". Direct contradiction; the
compiler rejects it. Same for `abstract final` methods, and `abstract` + `private`/`static`.

**The modern answer worth adding:** since JDK 17, **`sealed`** gives you the middle ground — a closed
hierarchy where you name the permitted subtypes. That's what you want for an ADT (a set of trade
events, a result type), and it's what makes `switch` pattern matching exhaustively checkable. So the
choice is no longer just open-or-closed.

**And the design line:** prefer **composition over inheritance**. Deep hierarchies are the classic
maintenance trap; `default` methods let you share behaviour without inheritance at all.
</details>

---

### 6. Design an immutable class. What are the rules?

<details><summary>Answer</summary>

Not in the source list, and it should be — it's the concurrency answer and the `equals`/`hashCode`
answer at the same time.

1. **`final` class** (or private constructor + factory) so behaviour can't be overridden.
2. **All fields `private final`.**
3. **No setters**, and no method that mutates state — "wither" methods return a new instance.
4. **Defensive-copy mutable inputs in the constructor** and **outputs in getters**. This is the rule
   people forget:
   ```java
   public final class Order {
       private final List<Item> items;
       public Order(List<Item> items) { this.items = List.copyOf(items); }   // copy IN
       public List<Item> items() { return items; }        // already immutable, safe to hand out
   }
   ```
5. **Don't let `this` escape the constructor** — no registering listeners, no starting threads, no
   calling overridable methods. Otherwise another thread can observe the object before its final
   fields are frozen.

**Why it's the concurrency answer:** `final` fields get JSR-133 **freeze semantics**, so any thread
that sees a reference to a correctly-constructed object sees the correct field values **with no
synchronisation** (`quiz/01` Q25). Immutable objects are shareable, safe as map keys, and can't be
corrupted by a race.

**The trap:** `final` on a **reference** freezes the reference, not the referent.
`private final List<X> l = new ArrayList<>()` is still mutable and still unsafe to share — the same
error as `val buf = mutable.ListBuffer()` in Scala and a `record` holding a mutable list.

**Modern shortcut:** a **`record`** (JDK 16+) gives you final fields, a final class, and structural
`equals`/`hashCode` — but it's only *shallowly* immutable, so you still need `List.copyOf` in the
compact constructor.
</details>

---

### 7. `transient` vs `volatile` vs `static` — three different things.

<details><summary>Answer</summary>

They get asked together because they look similar and share nothing.

- **`transient`** — a **serialisation** modifier. "Do not write this field to the stream."
  On deserialisation the field gets its **type default** (`null`, `0`, `false`), *not* its constructor
  value, because deserialisation doesn't run the constructor. Use for caches, derived values,
  passwords, and anything non-serialisable (`Thread`, `Socket`, a connection).
- **`volatile`** — a **concurrency** modifier. Visibility and ordering guarantees, no atomicity for
  compound operations (`quiz/01` Q1). Nothing whatsoever to do with serialisation.
- **`static`** — a **scope** modifier: belongs to the class (Q3).

**The pairwise questions the list asks:**

*`transient` vs `volatile`* — completely orthogonal. A field can legally be both, and it's rare but
meaningful: excluded from serialisation *and* safely visible across threads.

*`transient` vs `static`* — the overlap is that **neither is serialised**. `static` fields are class
state, and the default serialisation mechanism writes *instance* state, so statics are skipped
automatically. But the reasons differ: `transient` is your explicit choice, `static` is structural.
So `static transient` is redundant.

**Follow-up to be ready for:** how do you serialise a field that's `transient` but genuinely needed?
Custom `writeObject`/`readObject`, or `Externalizable`, or better — reconstruct it lazily in
`readObject`.
</details>

---

### 8. Serialisation: `serialVersionUID`, and what happens when it doesn't match?

<details><summary>Answer</summary>

Absent from the source list except `transient`, and it's a real production topic.

**`serialVersionUID`** is a version stamp on a `Serializable` class. On deserialisation the JVM
compares the stream's UID with the loaded class's; **a mismatch throws
`InvalidClassException`** — the deserialisation fails outright.

**The trap:** if you don't declare one, the compiler **computes** it from the class's structure —
name, fields, methods, interfaces. So adding a field or even changing a method signature silently
changes the UID, and yesterday's serialised data becomes unreadable. **Always declare it explicitly:**
```java
private static final long serialVersionUID = 1L;
```
Then *you* decide when a change is breaking, rather than the compiler deciding by accident.

**Compatible vs incompatible changes** (worth knowing the shape): adding a field is compatible — old
streams leave it at its default. Removing a field is compatible-ish. **Changing a field's type,
renaming it, changing the class hierarchy, or changing a class from `Serializable` to not** are
incompatible.

**`Externalizable`** — you write `writeExternal`/`readExternal` and control the format completely.
Faster and more compact, but you own compatibility entirely, and it requires a **public no-arg
constructor**.

**The senior framing:** Java's built-in serialisation is **widely regarded as a mistake** — it's a
hidden, extralinguistic constructor that bypasses your invariants, it's a notorious source of
remote-code-execution vulnerabilities (deserialisation gadget chains), and it couples your wire
format to your class structure. For anything crossing a process boundary, use an **explicit schema** —
JSON, Avro, Protobuf. That connects straight to `quiz/13` Q5 on event schema evolution.
</details>

---

### 9. How does `synchronized` work, and what are the alternatives?

<details><summary>Answer</summary>

> `quiz/01` Q5 covers the choice in depth; this is the mechanism.

**Mechanism.** Every object has a **monitor**. `synchronized` compiles to `monitorenter`/`monitorexit`
(or an `ACC_SYNCHRONIZED` flag on a method). Entering acquires the monitor; exiting releases it,
including on an exception path. It's **reentrant** — the same thread can re-acquire a monitor it
already holds.

**What it gives you** — and both halves matter:
1. **Mutual exclusion** — one thread at a time.
2. **Memory visibility** — unlock is a *release*, lock is an *acquire*, so everything written before
   the unlock is visible after a subsequent lock of the same monitor (`quiz/01` Q2).

**Lock states** (worth naming): the JVM optimises through **thin/biased/inflated** locks —
uncontended locking is a CAS on the mark word, not an OS call. Biased locking was disabled by default
in JDK 15 and removed later. The JIT also does **lock elision** (a non-escaping object's lock is
deleted) and **lock coarsening**.

**Alternatives:**
- **`ReentrantLock`** — `tryLock`, timeouts, interruptible acquisition, fairness, multiple
  `Condition`s. And on **JDK 21 virtual threads it doesn't pin the carrier, whereas `synchronized`
  does** — measured at **34× in `problems/java/P10VirtualThreads.java`** with *uncontended* locks.
- **`ReadWriteLock` / `StampedLock`** — read-mostly workloads.
- **Atomics / CAS** — `AtomicInteger`, `LongAdder`; lock-free.
- **Immutability** — no shared mutable state, so nothing to lock. The best answer where available.
- **Confinement** — one thread owns the data (an actor, a partitioned consumer).
- **Concurrent collections** — `ConcurrentHashMap` instead of a synchronised map.

**Say the ordering:** don't reach for a lock first. Ask whether the state can be immutable, confined,
or atomic — in that order.
</details>

---

### 10. `String` vs `StringBuilder` vs `StringBuffer`, and the string pool.

<details><summary>Answer</summary>

- **`String`** — **immutable**. Every "modification" allocates a new object.
- **`StringBuilder`** — mutable, **not** synchronised. The default.
- **`StringBuffer`** — mutable, **synchronised** on every method. Legacy; the synchronisation is
  almost always useless because builders are near-always thread-confined locals, and the JIT usually
  elides the lock anyway.

**Choose:** `StringBuilder`, essentially always. `StringBuffer` only if a builder genuinely escapes to
multiple threads — which is a design smell.

**The performance point they want:** `+` in a **loop** is O(n²), because each concatenation copies
the whole accumulated string.
```java
for (String s : list) result += s;              // O(n^2) — new String each iteration
StringBuilder sb = new StringBuilder();
for (String s : list) sb.append(s);             // O(n)
```
The compiler optimises `+` **within a single expression** (it becomes a builder, or
`StringConcatFactory` via invokedynamic since JDK 9) — but not across iterations.

**Why `String` is immutable** — worth volunteering: safe sharing across threads with no
synchronisation; **`hashCode` can be cached**, which is why it's a good map key; security (a validated
path can't be mutated behind you); and interning is only possible at all because the value can't
change.

**The pool:** literals are interned automatically, one instance per distinct literal. Since **JDK 7
the string table lives on the heap** (previously PermGen) and is resizable via `-XX:StringTableSize`.
**Don't `intern()` unbounded user input** — the table is a JVM-level structure you can't evict from,
so you've built an unbounded cache. And **compact strings** (JDK 9+, default) store Latin-1 as
`byte[]`, roughly halving most strings' footprint.
</details>

---

### 11. `Object.clone()` — how does it work, and what should you do instead?

<details><summary>Answer</summary>

`clone()` is `protected native` on `Object`. It creates a new instance and **copies the fields
bitwise** — a **shallow** copy. To use it you must implement **`Cloneable`**, which is a
**marker interface with no methods**; if you call `clone()` without it you get
`CloneNotSupportedException`.

```java
class Order implements Cloneable {
    private List<Item> items;
    @Override public Order clone() {
        try {
            Order c = (Order) super.clone();
            c.items = new ArrayList<>(items);   // deep-copy the mutable field yourself
            return c;
        } catch (CloneNotSupportedException e) { throw new AssertionError(e); }
    }
}
```

**Shallow vs deep:** shallow copies the references, so both objects share the same mutable
sub-objects — mutate the list through one and the other sees it. Deep copying means recursively
copying everything mutable, and you must do it by hand.

**Why it's considered broken** — this is the answer that shows judgement:
- `Cloneable` is a marker with **no `clone()` method on it**, so the interface doesn't actually give
  you the capability. It just changes `Object.clone`'s behaviour. That's a design error.
- `clone()` **doesn't run a constructor**, so it bypasses your invariants — the same objection as
  serialisation.
- It interacts badly with `final` fields (you can't reassign them in `clone`).
- The contract is vague about whether `super.clone()` must be used.

**Use instead:** a **copy constructor** (`new Order(other)`) or a **static factory**
(`Order.copyOf(other)`). Both are explicit, work with `final` fields, run your validation, and can
return a different type. And for value types, prefer **immutability plus a `with`-style method** so
copying never comes up. Josh Bloch — who wrote `Cloneable` — recommends against it.
</details>

---

### 12. What is `finalize()` for, when is it called, and what replaced it?

<details><summary>Answer</summary>

**Intended** as a last-chance cleanup hook the GC calls before reclaiming an object.
**Answer to "when is it called": there is no guarantee it is ever called at all**, and that's the
whole point of the question.

**Why it's unusable:**
- No guarantee it runs, or when. The JVM may exit first.
- An object with a finalizer needs **at least two GC cycles** and goes through a queue serviced by a
  single finalizer thread — a slow finalizer stalls the queue and you can OOM with reclaimable
  garbage on the heap.
- A finalizer can **resurrect** the object by storing `this` somewhere.
- Exceptions thrown in a finalizer are **swallowed**.
- **Finalizer attacks** — a subclass's finalizer can reach a partially-constructed object even if the
  constructor threw.

**Status:** deprecated in **JDK 9** and deprecated *for removal* in JDK 18 (JEP 421), which also added
`--finalization=disabled` so you can turn it off and test. Treat it as gone.

**What to use instead:**
1. **`try`-with-resources / `AutoCloseable`** — deterministic, lexically visible, and the answer 95%
   of the time. `close()` is called on normal *and* exceptional exit; multiple resources close in
   reverse order; an exception from `close()` is added as a **suppressed** exception rather than
   masking the original.
2. **`java.lang.ref.Cleaner`** (JDK 9+) — a *safety net* for native memory. Register a state object
   that must **not** capture the referent, or it can never be collected. Still non-deterministic.
3. **`PhantomReference` + `ReferenceQueue`** if you want to drive the loop yourself.

**The framing:** GC manages **memory**, not **resources**. File handles, sockets and locks need
deterministic release, and the language gives you `try`-with-resources for exactly that.
</details>

---

### 13. Strong, soft, weak, phantom references — when would you use each?

<details><summary>Answer</summary>

> Also `quiz/04` Q21. The source list asks specifically about `WeakReference`.

Reachability ladder: **strongly → softly → weakly → phantom → unreachable.**

- **Strong** — an ordinary reference. Never collected while reachable.
- **Soft** — cleared **at the GC's discretion when memory is tight**, before OOM. Nominally a
  memory-sensitive cache; in practice a **bad** one, because the policy is crude
  (`-XX:SoftRefLRUPolicyMSPerMB`), clearing happens during a full GC, and it tends to hold garbage
  right up until it causes a long pause. Use a real bounded cache (Caffeine).
- **Weak** — cleared **as soon as the referent is not strongly reachable**, at the next GC. The
  correct tool for **canonicalising maps** and **metadata keyed by an object you don't own**:
  `WeakHashMap`, `ThreadLocal`'s keys, listener registries that shouldn't leak.
- **Phantom** — `get()` always returns `null`; enqueued **after** finalisation. Purely a "this is
  definitely gone, run my cleanup" signal — the mechanism behind `Cleaner`.

**The `WeakReference` gotchas worth naming:**
- **`WeakHashMap` holds the KEY weakly and the VALUE strongly.** If a value references its own key,
  the entry never clears — measured in `problems/gc-labs/L02Leak.java`, where exactly that leak was
  planted and OOM'd a 128 MB heap in ~2 seconds.
- **`ThreadLocal` has the same shape**: weak key, strong value. That's why an unremoved `ThreadLocal`
  on a pooled thread leaks (`quiz/01` Q13, measured 22 MB → 4 MB after `remove()` in `P08`).

So the summary: weak references solve "don't keep this alive just because I'm tracking it". They do
**not** solve "cache with a bound".
</details>

---

### 14. `Class.forName()` vs `ClassLoader.loadClass()`.

<details><summary>Answer</summary>

Both load a class by name. **The difference is initialisation.**

- **`Class.forName("com.X")`** — loads, links, **and initialises**: static initialisers and static
  field initialisers run.
- **`ClassLoader.loadClass("com.X")`** — loads and links, but does **not** initialise. Static blocks
  do not run until you first actively use the class.
- **`Class.forName("com.X", false, loader)`** — the three-arg form, with `initialize=false`, gives you
  `forName` semantics *without* initialisation, plus an explicit loader.

**Why it ever mattered:** the old JDBC idiom `Class.forName("com.mysql.jdbc.Driver")` worked precisely
*because* it ran the static block, which registered the driver with `DriverManager`. With
`loadClass` it would silently not register. (Since JDBC 4.0 the `ServiceLoader` mechanism makes the
call unnecessary — good detail to add.)

**Also:** `Class.forName` uses the **caller's** classloader by default, which matters in a container
or a module with several loaders — a frequent source of `ClassNotFoundException` that "should" work.

**Related distinction to have ready:** `ClassNotFoundException` is a **checked** exception thrown when
*reflective* loading fails; `NoClassDefFoundError` is an **`Error`** thrown when the class was present
at compile time but is missing (or failed initialisation) at runtime. Being able to separate those two
is a good signal.
</details>

---

### 15. `ClassLoader.defineClass()` — what is it, and what should you use now?

<details><summary>Answer</summary>

`defineClass(String name, byte[] b, int off, int len)` turns a **byte array of bytecode** into a
`Class` object. It's **`protected` and `final`** on `ClassLoader`, so you can't call it from outside —
you subclass `ClassLoader` and expose it, which is exactly what frameworks do.

```java
static class MyLoader extends ClassLoader {
    Class<?> define(String name, byte[] bytes) {
        return defineClass(name, bytes, 0, bytes.length);   // protected -> accessible in a subclass
    }
}
```
(That's precisely the technique used in `problems/gc-labs/L03OomVariants.java` to generate classes on
demand and reproduce a **`Metaspace` OOM** — a good concrete example to cite, since it shows why
class generation is a memory-leak vector.)

**Who uses it:** CGLIB, ByteBuddy, Javassist, Spring's proxy generation, mocking frameworks, Groovy
and other JVM languages, ORM instrumentation.

**Modern alternatives — this is the part that shows currency:**
- **`MethodHandles.Lookup.defineClass(byte[])`** (JDK 9+) — defines a class in the *lookup's* package
  with its access rights. No custom classloader needed.
- **`Lookup.defineHiddenClass`** (JDK 15+, JEP 371) — a class that is **not discoverable by name**,
  can't be found by `Class.forName`, and is **unloadable independently** of its loader. This is now
  the right primitive for generated proxies and lambdas, and it exists specifically to avoid the
  classloader-leak problem below.

**The operational hazard to name:** every generated class consumes **metaspace**, and metaspace is
**unbounded by default**. A framework that generates classes per request, or a classloader retained by
a `ThreadLocal`, gives you a **classloader leak** and eventually `OutOfMemoryError: Metaspace` — which
raising `-Xmx` does **not** fix (`quiz/04` Q8).
</details>

---

### 16. What is `java.lang.reflect.Proxy`, and what is it used for?

<details><summary>Answer</summary>

A **dynamic proxy**: at runtime it generates a class that implements a given set of **interfaces** and
routes every method call to a single `InvocationHandler`.

```java
Foo foo = (Foo) Proxy.newProxyInstance(
    Foo.class.getClassLoader(),
    new Class<?>[]{ Foo.class },
    (proxy, method, args) -> {                     // InvocationHandler
        long t0 = System.nanoTime();
        try { return method.invoke(target, args); }
        finally { record(method.getName(), System.nanoTime() - t0); }
    });
```

**The critical limitation: JDK proxies work for interfaces only.** You cannot proxy a concrete class.
For that you need bytecode subclassing — **CGLIB** or **ByteBuddy** — which in turn means the class
and the method must not be `final`.

**Where you already rely on it:** this is how **Spring AOP** implements `@Transactional`, `@Async`,
`@Cacheable`, `@Retryable` and `@PreAuthorize` — JDK proxy if the bean implements an interface, CGLIB
subclass otherwise. Also Spring Data repositories, Feign clients, MyBatis mappers, and RMI.

**And therefore the bug class it explains** — worth volunteering because it's the practically useful
part: **self-invocation bypasses the proxy.** `this.doWork()` inside the same bean doesn't go through
the proxy, so `@Transactional` silently does nothing (`quiz/10` Q4/Q5, `quiz/07` Q2). Also only
`public` methods are advised, and the object you inject is not the object you wrote — which is why a
debugger shows `Service$$EnhancerBySpringCGLIB`.

**Costs:** reflection overhead per call (small, and JIT-optimised), an extra stack frame, and lost
type identity (`getClass()` is the proxy). Annotations aren't inherited onto the proxy unless
`@Inherited`.
</details>

---

### 17. `MethodHandle` vs reflection — why does `java.lang.invoke` exist?

<details><summary>Answer</summary>

A **`MethodHandle`** is a directly-executable, typed reference to a method, field or constructor,
obtained through a `MethodHandles.Lookup`.

```java
MethodHandles.Lookup lookup = MethodHandles.lookup();
MethodType type = MethodType.methodType(int.class, int.class);
MethodHandle mh = lookup.findVirtual(Calc.class, "twice", type);
int r = (int) mh.invokeExact(calc, 21);
```

**The differences that matter:**

| | Core reflection (`Method`) | `MethodHandle` |
|---|---|---|
| Access check | on **every** `invoke` | **once, at lookup time** |
| Typing | `Object[]` args, boxed | typed `MethodType`, `invokeExact` is strict |
| JIT | harder to optimise | designed to inline like a direct call |
| `setAccessible` | needed to bypass access | the `Lookup` carries the access rights |

The **access-check-once** property is the substantive one: reflection re-checks permissions per call,
a method handle checks at creation. Combined with the typed signature, the JIT can treat an invocation
through a `static final` handle almost like a direct call.

`invokeExact` requires the argument types to match the `MethodType` **exactly** — no conversions.
`invoke` permits asType conversions (boxing, widening), so it's more forgiving and slower.

**Why it really exists:** to support **`invokedynamic`**, which is the JVM instruction underpinning
**lambdas** (via `LambdaMetafactory`), string concatenation since JDK 9 (`StringConcatFactory`),
records' generated methods, and non-Java languages on the JVM. Every lambda you write is a call site
linked by a method handle — which is a nice thing to be able to say, because it connects this API to
something you use daily.

**Also worth naming:** `VarHandle` (JDK 9+) is the sibling for **field and array access** with
explicit memory-ordering modes — `getAcquire`, `setRelease`, `compareAndSet`, `fullFence`. It's the
modern, supported replacement for `sun.misc.Unsafe` and directly relevant to `quiz/01`'s memory-model
material.
</details>

---

### 18. Inner vs static nested vs local vs anonymous classes — and the leak.

<details><summary>Answer</summary>

Absent from the source list; commonly asked.

- **Static nested class** — `static class Inner {}`. **No reference to the outer instance.** Just a
  top-level class scoped for namespacing. Use this by default.
- **Inner (non-static) class** — `class Inner {}`. Holds an **implicit reference to the enclosing
  instance** (`Outer.this`), so it can read outer fields, and it can only be created from an instance
  (`outer.new Inner()`).
- **Local class** — declared inside a method body. Captures effectively-final locals.
- **Anonymous class** — declared and instantiated at once. No name, can't have a constructor, may
  extend a class *or* implement one interface.
- **Lambda** — not a class in the same sense: compiled to an `invokedynamic` call site, **no `this`
  capture** (`this` refers to the enclosing instance), and no separate class file per lambda.

**The memory-leak answer, which is why this gets asked:** a **non-static inner class instance keeps
its outer instance alive**. If you hand a `Listener` inner-class instance to a long-lived registry,
you have pinned the whole enclosing object — a classic Android/Swing leak, and the same shape as the
captured-lambda leak measured in `problems/gc-labs/L02Leak.java` (a `List<Runnable>` retaining every
session because the lambda captured it).

**So the rule: make nested classes `static` unless you actually need the outer instance.**

**Lambda vs anonymous class** is Q1 of `quiz/17` — the short version: `this` means different things,
lambdas don't generate a class file per instance, non-capturing lambdas are cached singletons, and
lambdas can't have state or extend anything.
</details>

---

### 19. `map.get()` vs `map.containsKey()`, and fail-fast iterators.

<details><summary>Answer</summary>

**`get` vs `containsKey`** — the distinction is **null**:
- `get(k)` returns `null` both when the key is **absent** and when it's **mapped to null**. Ambiguous.
- `containsKey(k)` distinguishes them.

```java
map.put("a", null);
map.get("a")          // null   -- looks absent
map.containsKey("a")  // true   -- but it's present
```
For a `HashMap` (which permits one null key and many null values) that ambiguity is real. For a
`ConcurrentHashMap` or `TreeMap` **values cannot be null**, so `get() == null` does mean absent.

**And the important part:** `containsKey` followed by `get` is **two lookups**, and under concurrency
**a race** — the classic check-then-act (`quiz/01` Q10; measured in `P04`: 8 threads, 8 redundant
computations, 7 results discarded). Prefer:
- `getOrDefault(k, d)` — one lookup.
- `computeIfAbsent(k, f)` — atomic, computes at most once.
- `Optional.ofNullable(map.get(k))`.

**Fail-fast vs fail-safe iterators** — also absent from the source list and commonly asked:
- **Fail-fast** (`ArrayList`, `HashMap`, most of `java.util`): the iterator tracks a `modCount`, and
  a structural modification during iteration throws **`ConcurrentModificationException`** on the next
  `next()`. It is **best-effort**, not guaranteed — never write logic that depends on it.
- **Fail-safe** (`CopyOnWriteArrayList`, `ConcurrentHashMap`): iterate a snapshot or a weakly
  consistent view. No CME; you may not see concurrent updates.

**And note `CME` is not only a threading bug** — the most common cause is removing from a collection
inside a for-each loop in a *single* thread. Fix with `Iterator.remove()`, `removeIf()`, or by
collecting and removing afterwards.
</details>

---

### 20. Checked vs unchecked exceptions — and the hierarchy.

<details><summary>Answer</summary>

> `quiz/09` Q11 has the full argument. This is the mechanism and the hierarchy.

```
Throwable
├── Error                 unchecked — JVM-level, don't catch (OutOfMemoryError, StackOverflowError)
└── Exception             CHECKED
    └── RuntimeException  unchecked (NullPointerException, IllegalArgumentException, IllegalStateException)
```
**The rule: everything under `Exception` except `RuntimeException` and its subclasses is checked.**

**Checked** — the **compiler** forces `catch` or `throws`. It is purely a *compile-time* rule; the JVM
does not enforce it, which is why other JVM languages (Scala, Kotlin) ignore it entirely and why
bytecode manipulation can throw a checked exception from a method that doesn't declare it.

**Where the industry landed:** mostly unchecked, because checked exceptions **don't compose** — they
break lambdas (`Function.apply` can't throw checked), they leak implementation detail through
interfaces, and they pressure people into `throws Exception` everywhere or, worse,
`catch (Exception e) {}`. The strongest evidence: **Spring deliberately wraps `SQLException` into the
unchecked `DataAccessException`.**

**Use checked** when the caller can genuinely and specifically recover and you want to force the
decision. And one where you have no choice: **`InterruptedException`**, which must be propagated or
the flag restored — never swallowed (`quiz/01` Q14).

**Rules regardless:** never swallow; preserve the cause (`throw new ServiceException(msg, e)`);
include context in the message (the id, not just the type); don't use exceptions for control flow
(they capture a stack trace, which isn't free); catch at a boundary that can act.
</details>

---

### 21. `throw` vs `throws`.

<details><summary>Answer</summary>

- **`throw`** — a **statement** that actually raises an exception: `throw new IllegalStateException("…")`.
  One object, at runtime.
- **`throws`** — a **method-signature clause** declaring what may propagate:
  `void f() throws IOException, SQLException`. A compile-time contract; throws nothing itself.

Mnemonic: `throw` *does*, `throws` *declares*.

**Details worth adding, because the bare distinction is a five-second answer:**
- You only need `throws` for **checked** exceptions. Declaring unchecked ones is legal and sometimes
  useful as documentation.
- **Overriding narrows:** an override may declare **fewer or narrower** checked exceptions than the
  method it overrides, never broader. That's Liskov substitution enforced by the compiler.
- `throw null` compiles and throws `NullPointerException`.
- Rethrowing preserves the original stack trace; `throw new X(e)` chains it as the cause. Losing the
  cause is a real diagnostic loss.
- **More precise rethrow** (JDK 7+): `catch (Exception e) { throw e; }` can compile even if the
  method only declares the specific checked types, because the compiler tracks what can actually be
  thrown in the block.
- `finally` interacts badly: a `return` or `throw` in `finally` **discards** an in-flight exception.
  Never do it. `try`-with-resources handles this properly via **suppressed** exceptions.
</details>

---

### 22. Generics: what is type erasure, and what does it cost you?

<details><summary>Answer</summary>

**Completely absent from the source list**, and asked in essentially every Java interview.

**Erasure:** generics are a **compile-time** feature. The compiler checks types, inserts casts, and
then **erases** the type parameters — `List<String>` becomes `List`, `T` becomes `Object` (or its
leftmost bound). There is one `List` class at runtime.

**Why:** backwards compatibility. Java 5 generics had to interoperate with pre-generic code and
existing class files, so erasure was chosen over reified generics (which is what C# did).

**What it costs — the consequences you should be able to list:**
```java
List<String> a = new ArrayList<>();  List<Integer> b = new ArrayList<>();
a.getClass() == b.getClass()                    // TRUE — same runtime class
new T()                                          // illegal — no type info
new T[10]                                        // illegal
if (x instanceof List<String>)                   // illegal — can't check the parameter
catch (MyException<String> e)                    // illegal
static T field;                                  // illegal in a generic class
```
- **No primitives** as type arguments, hence boxing everywhere (`List<int>` is illegal). Measured:
  `List[Int]`-style boxing costs ~10× the footprint of a primitive array
  (`problems/scala/S03AllocationPressure.scala`).
- **Heap pollution** and the `unchecked` warning: `@SuppressWarnings("unchecked")` exists because the
  compiler can't verify some casts after erasure.
- **Bridge methods** are generated so overriding works across erasure.
- You cannot overload on `List<String>` vs `List<Integer>` — same erasure, same signature.

**The workaround to name:** pass a `Class<T>` token (`Class.newInstance`, Jackson's `TypeReference`,
Spring's `ParameterizedTypeReference`) or capture the type via an anonymous subclass — because
**a generic supertype IS retained in the class file**, which is how those libraries recover it.
</details>

---

### 23. Wildcards and PECS — `? extends` vs `? super`.

<details><summary>Answer</summary>

**PECS — Producer `Extends`, Consumer `Super`.**

- **`List<? extends Number>`** — you can **read** `Number` out; you **cannot add** anything (except
  `null`), because the actual list might be `List<Integer>` and you'd corrupt it. **Producer.**
- **`List<? super Integer>`** — you can **add** `Integer`; reads come back as `Object`, because the
  list might be `List<Number>` or `List<Object>`. **Consumer.**
- **`List<?>`** — unbounded; read as `Object`, add nothing.

The canonical illustration is the JDK's own signature:
```java
static <T> void copy(List<? super T> dest, List<? extends T> src)
//                        consumer            producer
```

**Why you need them at all:** generics are **invariant**. `List<Integer>` is **not** a
`List<Number>`, even though `Integer` is a `Number`. Wildcards recover the flexibility invariance
takes away.

**And why invariance is necessary** — the best answer, because it shows you understand rather than
memorise:
```java
List<Integer> ints = new ArrayList<>();
List<Number> nums = ints;        // if this were legal...
nums.add(3.14);                  // ...you could put a Double in a List<Integer>
int x = ints.get(0);             // ClassCastException
```
**Java's arrays *are* covariant** and therefore unsound — which is why `Object[] a = new String[1];
a[0] = 42;` throws **`ArrayStoreException`** at runtime. Generics chose compile-time safety instead.

**Related:** `<T extends Comparable<T>>` is a **bounded type parameter** (not a wildcard) — use it
when you need to *name* the type. Multiple bounds: `<T extends A & B>`, class first.

**Scala comparison** if it comes up: Scala has **declaration-site** variance (`List[+A]`) rather than
Java's **use-site** wildcards — declared once by the library author instead of at every call site
(`quiz/12` Q14).
</details>

---

### 24. Enums: what can they do beyond constants?

<details><summary>Answer</summary>

An `enum` is a **class** with a fixed set of instances. It can have fields, constructors, methods,
and implement interfaces — it just can't extend a class (it already extends `java.lang.Enum`).

```java
enum Side {
    BUY("B")  { public int sign() { return  1; } },     // constant-specific body
    SELL("S") { public int sign() { return -1; } };
    private final String code;
    Side(String code) { this.code = code; }             // constructor is implicitly private
    public String code() { return code; }
    public abstract int sign();
}
```

**What to know:**
- `values()`, `valueOf(String)` (throws `IllegalArgumentException` on no match), `name()`, `ordinal()`.
  **Never persist `ordinal()`** — reordering the constants silently changes stored data. Persist
  `name()` or an explicit code.
- **`EnumMap`** and **`EnumSet`** — array/bit-vector backed, so dramatically faster and smaller than
  `HashMap`/`HashSet` for enum keys. Use them; almost nobody does.
- `switch` over an enum needs no qualification, and modern `switch` **checks exhaustiveness** for a
  sealed-like effect.
- Enums are **`Comparable`** by ordinal and serialise by **name**.

**The enum singleton** — and this is the connection to the design-patterns file:
```java
public enum Registry { INSTANCE; /* state and methods */ }
```
This is the **preferred Singleton implementation** (Effective Java): the JVM guarantees one instance
per classloader, it's **serialisation-safe for free** (no `readResolve` needed), and it's
**reflection-proof** — you cannot construct an enum via reflection. It defeats the two attacks that
break the classic `private static final INSTANCE` version (`quiz/19` Q3).

Enum constant initialisation is thread-safe by class-init locking, so no double-checked locking is
needed either.
</details>

---

### 25. What's in modern Java that a 2023 question list would miss?

<details><summary>Answer</summary>

The source list predates or ignores most of this. Being fluent here is a cheap way to sound current.

- **`record`** (JDK 16) — a transparent immutable data carrier. Generates a canonical constructor,
  accessors, and structural `equals`/`hashCode`/`toString`. Use a **compact constructor** for
  validation and defensive copies. Poor fit for JPA entities (needs a no-arg constructor and mutable
  fields). See `quiz/09` Q14.
- **`sealed` classes and interfaces** (JDK 17) — a closed hierarchy with `permits`. Combined with
  records, this is an **algebraic data type**, and it's what lets `switch` be checked for
  exhaustiveness. Directly useful for modelling a trade-event set (`quiz/12` Q20).
- **Pattern matching** — `instanceof` patterns (JDK 16) remove the cast;
  **`switch` patterns** (JDK 21) add type patterns, guards (`when`), and record **deconstruction**:
  ```java
  return switch (event) {
      case Executed(var id, var qty, _) when qty > 10_000 -> block(id);
      case Executed e -> normal(e);
      case Settled s  -> done(s);
  };
  ```
- **Text blocks** (JDK 15) — `"""…"""` for multi-line SQL/JSON without escaping.
- **`var`** (JDK 10) — local *inference*, not dynamic typing. The type is fixed at compile time. Use
  where the initialiser makes the type obvious; not for `var x = foo()`.
- **Virtual threads** (JDK 21, JEP 444) and **structured concurrency** (preview in 21) — the biggest
  change in the concurrency model in a decade. Whole file: `quiz/03`.
- **Sequenced collections** (JDK 21) — `SequencedCollection`/`Map` with `getFirst`, `getLast`,
  `reversed()`. Fixes a long-standing gap where `LinkedHashMap` had no way to ask for the first entry.
- **Enhanced `switch`** (JDK 14) — arrow labels, `yield`, no fall-through.
- **JPMS / modules** (JDK 9) — `module-info.java`, strong encapsulation, and the reason
  `--add-opens` exists and reflection into JDK internals now fails.
- **Helpful NullPointerExceptions** (JDK 14, on by default since 15) — the message names *which*
  expression was null. Genuinely changes debugging.

**Version baseline to state (checked August 2026):** the **latest LTS is JDK 25** (GA Sep 2025, Temurin
support to at least Sep 2031); the latest GA feature release is **JDK 26** (Mar 2026); **JDK 27** lands
Sep 2026. The still-supported LTS line is **8 / 11 / 17 / 21 / 25**. So don't say "21 is the current LTS"
— it isn't any more, and that dates you.

What this pack actually is: **verified against JDK 21.0.11**, which remains the realistic banking
baseline (BofA and Citi are typically on 17 or 21, not on 25). Say exactly that — "we're on 17, I've
verified this on 21" is more credible than claiming everything. Class-file major version = feature
release **+ 44**, so JDK 8 = 52, 11 = 55, 17 = 61, 21 = 65, 25 = 69 — handy when a
`UnsupportedClassVersionError` names only the number.
</details>

---

## Score yourself

- **20+** — the core-Java half of any Java interview is covered.
- **Highest-value here:** Q1 (the `Integer` cache trap), Q6 (immutability), Q12 (`finalize` →
  try-with-resources), Q22–Q23 (generics — the source list's biggest omission), Q16 (`Proxy`, because
  it explains the `@Transactional` bug class).
- **Don't stop here.** These are mostly one-paragraph answers. The interview is decided on `quiz/01`
  (JMM), `quiz/04`–`05` (GC) and whether you can read the measurements in `problems/`.
