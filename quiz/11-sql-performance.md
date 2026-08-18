# Quiz 11 — SQL & Database Performance

15 questions built around *"a query is running slow — what would you check?"*, which is one
of the most reliably-asked questions in any server role and the one most people answer as a
list of tips rather than a method.

Target: 25 min.

---

### 1. A query is running slow. What do you check?

> **Probe areas: DB load · locked tables · avoiding `SELECT *` · CTEs · avoiding
> functions/loops in queries · joins and filters (data volume pulled, wildcards, `NOT IN`,
> inline views) · indexes · sub-queries vs unions**

<details><summary>Answer</summary>

**Lead with a method, not a tip list.** The probe areas are all *candidate causes*; what's
being scored is whether you diagnose or guess.

**Step 0 — Establish what "slow" means and whether it's the query.**
- Slow **always**, or slow **sometimes**? Intermittent points at load, locking, or plan
  instability — not at the SQL text.
- Slow for **all inputs** or **specific parameters**? Parameter-dependent slowness means
  data skew or a cached plan that suits one value and not another.
- **Did it change?** New code, a data-volume increase, a dropped index, statistics gone
  stale, or a plan flip. "It was fine last week" is the most useful sentence available.
- **Is it the query or the surroundings?** Time spent *waiting for a connection* is not
  query time (`hikaricp.connections.pending`), and neither is time serialising a huge
  result set or a client fetching 10 000 rows one round trip at a time.

**Step 1 — Get the execution plan. This is the actual answer.**
```sql
EXPLAIN (ANALYZE, BUFFERS) SELECT ...;     -- Postgres: ANALYZE runs it, gives real rows/timing
```
(`EXPLAIN ANALYZE` in MySQL 8+; `SET STATISTICS PROFILE` / actual plan in SQL Server;
`DBMS_XPLAN` in Oracle.) Read it for:
- **Sequential/full table scan** where you expected an index seek.
- **Estimated vs actual row counts diverging badly** → stale or missing statistics, and the
  optimiser is choosing a plan for the wrong data shape. This is the highest-value single
  observation in a plan.
- **The join algorithm**: a nested loop over a large outer input is a classic disaster.
- **Sorts and hashes spilling to disk** (`external merge`, low `work_mem`).
- **Where the time actually goes** — the plan is a tree; find the expensive node rather than
  assuming.

**Step 2 — Server and contention state.** *Probe areas: DB load, locked tables.*
- Is the **whole database** slow, or this query? Check CPU, I/O wait, buffer cache hit rate,
  active session count.
- **Blocking/locking**: `pg_stat_activity` (`wait_event_type = 'Lock'`), `pg_locks`,
  `SHOW ENGINE INNODB STATUS`, `sys.dm_exec_requests`. A query "running slow" that's
  actually **waiting on a lock** needs a completely different fix (Q8).
- Long-running transactions holding locks, or blocking vacuum and causing bloat.

**Step 3 — Only now, look at the SQL.** Work through the probe list:
- **Indexes** (Q3): is there one usable for the predicates and joins? Is the query written
  so the index *can* be used (Q4)?
- **`SELECT *`** (Q2): pulling columns you don't need, defeating covering indexes and
  inflating network transfer.
- **Data volume**: how many rows does it *need* vs *pull*? Filter as early and as
  selectively as possible, and paginate (Q11).
- **Functions on indexed columns** (Q4): `WHERE UPPER(name) = 'X'` cannot use a plain index
  on `name`.
- **Leading wildcards**: `LIKE '%foo'` can't use a B-tree index; `LIKE 'foo%'` can.
- **`NOT IN`** (Q6): NULL semantics and often a poor plan. Prefer `NOT EXISTS`.
- **Correlated subqueries** executing per row (Q7) → rewrite as a join or an aggregate.
- **`OR` across different columns** — often better as a `UNION ALL` of two indexable queries.
- **Implicit type casts** on a join or predicate column — silently kills index use.
- **`DISTINCT` masking a bad join** that's multiplying rows.
- **Row-by-row processing** — a cursor or a loop in a stored procedure, or the application
  doing **N+1** (Q9). Set-based beats iterative, essentially always.

**Step 4 — Fix, then verify.** Re-run `EXPLAIN ANALYZE` and compare. Then check you haven't
made writes worse (a new index costs every `INSERT`/`UPDATE`) and that the fix holds at
production data volume, not on your 1 000-row dev table.

**The one-line summary to say first:** *"I'd get the execution plan before changing
anything, and I'd first establish whether it's the query, the data volume, contention, or
the connection pool — because those have completely different fixes."*
</details>

---

### 2. What's actually wrong with `SELECT *`?

<details><summary>Answer</summary>

1. **It defeats covering indexes.** If an index contains every column the query needs, the
   database answers from the index alone (**index-only scan**). Ask for one extra column and
   it must go back to the table for every row — often the difference between fast and slow.
2. **Unnecessary I/O and network transfer** — wide columns, `TEXT`/`BLOB`/`JSON` you never
   read. On a wide table that can be 10× the bytes.
3. **It breaks when the schema changes.** A new column silently appears in results;
   `ORDER BY 2` or positional access shifts; a `SELECT *` in a view or `INSERT INTO ...
   SELECT *` breaks outright.
4. **Hidden dependencies** — you can't tell from the code which columns are actually used,
   so nobody can safely drop a column.
5. **ORM knock-on**: with JPA it encourages fetching whole entities (and their eager
   associations) when a projection would do.

**Where it's fine:** ad-hoc exploration, `EXISTS` subqueries (nothing is projected),
`SELECT COUNT(*)` (which is not the same thing at all), and genuinely needing every column.

**The mature version of the answer:** name the columns because it makes the query's
**contract explicit** — the performance argument is real but secondary. And note that
`SELECT *` in application code is a maintenance problem, whereas in a one-off diagnostic
query it's nothing.
</details>

---

### 3. How do indexes work, and when do they *not* help?

<details><summary>Answer</summary>

**A B-tree index** is a balanced, sorted structure: O(log n) descent to find a key, plus the
leaves are linked, so **range scans** and **ordered retrieval** are cheap. The leaf holds the
key and a pointer to the row (or, in a clustered index, the row itself).

**What an index can serve:** equality, ranges (`>`, `BETWEEN`), prefix `LIKE 'foo%'`,
`ORDER BY` (avoiding a sort), `MIN`/`MAX`, join predicates, and uniqueness enforcement.

**Composite indexes and the leftmost-prefix rule** — the thing to get right:
```sql
CREATE INDEX idx ON trades (account_id, trade_date, status);
```
Usable for: `account_id`; `account_id + trade_date`; all three; and
`account_id = ? AND trade_date > ?`.
**Not usable** (or only poorly) for: `trade_date` alone, or `status` alone. **Column order
matters** — put equality predicates before range predicates, and the most selective /
most-commonly-filtered column first.

**Covering index** — includes every column the query needs, so it's answered without
touching the table (`INCLUDE (...)` in Postgres/SQL Server).

**When indexes don't help, or hurt:**
- **Low selectivity.** An index on a boolean or a status with three values, where you're
  fetching 40% of the table: a sequential scan is *cheaper*, and the optimiser is right to
  choose it. Roughly, above 5–20% of rows, scanning wins.
- **Small tables** — a full scan is a couple of pages.
- **The query defeats it** — a function on the column, a leading wildcard, an implicit cast
  (Q4).
- **Write cost**: every index must be maintained on `INSERT`/`UPDATE`/`DELETE`, and each one
  is extra I/O and WAL. **Too many indexes is a real and common problem** — a table with 15
  indexes has slow writes and a bloated cache footprint.
- **Stale statistics** — the optimiser won't use an index if it believes the wrong
  cardinality. `ANALYZE` / `UPDATE STATISTICS`.
- **Index bloat/fragmentation** after heavy churn (`REINDEX`).

**Other index types worth naming:** **hash** (equality only), **GIN/GiST** (Postgres —
full-text, JSONB, arrays, geometry), **partial** (`WHERE deleted = false` — small and very
effective), **expression** (`ON (lower(email))` — the fix for Q4), **BRIN** (huge
naturally-ordered tables), **clustered** vs **non-clustered** (the clustered index *is* the
table's physical order; you get one).

**Finding what's missing:** `pg_stat_user_tables.seq_scan` (high sequential-scan counts),
`pg_stat_user_indexes.idx_scan = 0` (unused indexes you can drop), the slow-query log, and
`EXPLAIN` on the actual query.
</details>

---

### 4. Why can't this query use the index? *(sargability)*

<details><summary>Answer</summary>

**SARGable** = "Search ARGument able" — the predicate can be resolved by an index seek.
These are the common ways to break it:

```sql
-- NOT sargable                              -- sargable rewrite
WHERE UPPER(email) = 'A@B.COM'               WHERE email = 'a@b.com'  -- + citext / expression index
WHERE YEAR(trade_date) = 2026                WHERE trade_date >= '2026-01-01'
                                               AND trade_date <  '2027-01-01'
WHERE created_at + INTERVAL '1 day' > now()  WHERE created_at > now() - INTERVAL '1 day'
WHERE price * 100 > 5000                     WHERE price > 50
WHERE name LIKE '%smith'                     WHERE name LIKE 'smith%'   -- or a trigram index
WHERE account_id = '12345'                   WHERE account_id = 12345   -- avoid implicit cast
WHERE COALESCE(status,'X') = 'X'             WHERE status IS NULL OR status = 'X'
```

**The principle: keep the indexed column bare on one side of the operator.** Any function,
arithmetic, or cast applied *to the column* means the database would have to evaluate it for
every row, which is exactly what an index exists to avoid.

**Fixes when you can't change the predicate:**
- An **expression index**: `CREATE INDEX ON users (lower(email));` — then
  `WHERE lower(email) = ?` *is* indexable.
- A **generated/computed column** plus an index on it.
- A different type: `citext` for case-insensitive text; **`pg_trgm` + a GIN index** for
  genuine `%substring%` searches; a full-text index for text search.

**The implicit-cast one is the sneakiest** — comparing a `varchar` column to a number (or
vice versa) may force a cast on the *column* side, silently disabling the index with no
visible mistake in the SQL. It shows up in the plan as a seq scan you can't explain, and
it's a classic cause of "the same query is fast in one environment and slow in another"
when column types differ.
</details>

---

### 5. Explain the join algorithms. Why does it matter?

<details><summary>Answer</summary>

- **Nested loop** — for each row of the outer input, probe the inner. O(n·m) unless the inner
  side is indexed, in which case it's O(n · log m) and excellent. **Best when the outer input
  is small.** Catastrophic when the optimiser underestimates the outer row count — this is
  the single most common cause of a query that used to be fast suddenly taking minutes.
- **Hash join** — build a hash table on the smaller input, stream the larger one through it.
  O(n + m), needs memory (spills to disk if it exceeds `work_mem`, which shows in the plan).
  **Best for large, unindexed, equality joins.**
- **Merge join** — sort both inputs (or read them in order from indexes), then walk them
  together. O(n log n + m log m), or O(n + m) if both are already sorted. Good for large
  joins where indexes provide the ordering, and for range joins.

**Why it matters:** you don't choose the algorithm, but **the plan tells you what the
optimiser believed**, and mismatches are your diagnostic signal. "Nested loop with an
estimate of 1 row and an actual of 2 million" is a complete diagnosis: the statistics are
wrong, or the predicate's selectivity was mis-estimated (correlated columns are a classic
cause), and the fix is `ANALYZE`, extended statistics, or a rewrite — **not** an index.

**Related things worth saying:**
- **Join order** matters enormously, and the optimiser chooses it. With many joined tables
  (`join_collapse_limit`, `from_collapse_limit` in Postgres) it may stop searching and pick
  a poor order — one reason very large joins get unpredictable.
- **Filter early.** Reducing rows before a join beats filtering after it. The optimiser
  usually pushes predicates down, but not always — particularly through `OUTER JOIN`s and
  some subqueries, where pushing down would change semantics.
- **A `LEFT JOIN` with a `WHERE` on the right table's column silently becomes an inner
  join** (because `NULL` fails the predicate). If you meant to filter the optional side, the
  condition belongs in the `ON` clause. Very common bug.
- **Fan-out**: joining one-to-many multiplies rows, and aggregates then double-count. The
  usual tell is someone adding `DISTINCT` to "fix" it, which masks the real problem and is
  slow.
</details>

---

### 6. `NOT IN` vs `NOT EXISTS` vs `LEFT JOIN ... IS NULL`?

<details><summary>Answer</summary>

**The critical difference is NULL semantics, and it's a correctness issue, not a performance
one.**

```sql
-- If the subquery returns ANY NULL, this returns NO ROWS AT ALL.
SELECT * FROM trades WHERE account_id NOT IN (SELECT account_id FROM closed_accounts);
```
Why: `x NOT IN (1, 2, NULL)` expands to `x <> 1 AND x <> 2 AND x <> NULL`. The last term is
**`UNKNOWN`**, never `TRUE`, so the whole conjunction can never be `TRUE`. **The query
silently returns nothing.** No error. This is one of the highest-value SQL gotchas to know,
because the failure is silent and total.

```sql
-- Correct and usually well-optimised: NULL-safe, short-circuits, uses an anti-join.
SELECT * FROM trades t
WHERE NOT EXISTS (SELECT 1 FROM closed_accounts c WHERE c.account_id = t.account_id);

-- Also correct; sometimes a better plan, more verbose.
SELECT t.* FROM trades t
LEFT JOIN closed_accounts c ON c.account_id = t.account_id
WHERE c.account_id IS NULL;
```

**Recommendation: default to `NOT EXISTS`.** It's NULL-safe, expresses the intent
("anti-join"), and modern optimisers handle it well. Use `NOT IN` only with a literal list
you know contains no NULLs — or when the column is `NOT NULL`, and even then `NOT EXISTS`
costs you nothing.

**The `EXISTS`/`IN` positive forms** are largely equivalent in modern optimisers (both become
semi-joins), so choose for readability. Historically `EXISTS` was preferred for correlated
cases and `IN` for small static lists; that folklore is mostly obsolete, but saying you'd
*check the plan* rather than trust folklore is the right answer.

**More NULL traps while you're here:** `NULL = NULL` is `UNKNOWN` (use `IS NULL`, or
`IS NOT DISTINCT FROM`); `COUNT(column)` skips NULLs but `COUNT(*)` doesn't; `SUM` of an
empty set is `NULL` not `0`; NULLs sort together and `ORDER BY` needs
`NULLS FIRST`/`NULLS LAST`; a `UNIQUE` constraint permits multiple NULLs in most databases.
</details>

---

### 7. Subqueries vs `UNION` vs CTEs — when do you use each?

<details><summary>Answer</summary>

**Correlated subquery** — references the outer query, so it may execute **per outer row**:
```sql
SELECT t.*, (SELECT MAX(price) FROM quotes q WHERE q.instrument = t.instrument) AS high
FROM trades t;                                    -- potentially one lookup per trade
```
Modern optimisers often rewrite these into joins, but not always. If the plan shows repeated
execution, rewrite as a **join to an aggregate**:
```sql
SELECT t.*, q.high FROM trades t
LEFT JOIN (SELECT instrument, MAX(price) AS high FROM quotes GROUP BY instrument) q
       ON q.instrument = t.instrument;
```
Or better, a **window function**, which does it in one pass:
```sql
SELECT t.*, MAX(price) OVER (PARTITION BY instrument) AS high FROM ...;
```

**`UNION` vs `UNION ALL`** — **`UNION` de-duplicates**, which means a sort or hash over the
whole result. **`UNION ALL` doesn't.** If you know the branches are disjoint, `UNION ALL` is
strictly cheaper. Using `UNION` out of habit is a very common, invisible cost.

A legitimate use of `UNION ALL`: splitting an `OR` across different columns into two
separately-indexable queries.
```sql
-- may scan:                          -- can use two indexes:
WHERE email = ? OR phone = ?          SELECT ... WHERE email = ?
                                      UNION ALL
                                      SELECT ... WHERE phone = ? AND email <> ?
```

**CTEs (`WITH`)** — *probe area.* Primarily for **readability**: naming a step, decomposing a
long query, and de-duplicating repeated subqueries.

**The performance caveat that matters:** in **PostgreSQL before 12**, a CTE was an
**optimisation fence** — always materialised, never inlined, so predicates couldn't be
pushed into it. That made CTEs a notorious performance trap. **Postgres 12+ inlines them by
default** when referenced once and not recursive, with explicit control via
`WITH ... AS MATERIALIZED` / `AS NOT MATERIALIZED`. Other databases differ (SQL Server and
Oracle have generally always inlined; MySQL 8+ inlines). **Know your version** — this is a
great detail to raise because it shows you've actually been burned.

Occasionally you *want* the fence: materialising once and reusing it, rather than
re-evaluating an expensive subquery several times.

**Recursive CTEs** (`WITH RECURSIVE`) for hierarchies — org charts, bills of materials,
graph traversal. Always bound the recursion depth; an unbounded one on cyclic data runs
forever.

**Derived tables / inline views** — a subquery in `FROM`. Functionally close to a
non-materialised CTE, usually less readable. The probe list mentions "inline views to avoid",
and the reason is exactly that: prefer a CTE for legibility unless the fence semantics or an
old Postgres version argues otherwise.
</details>

---

### 8. Locking, isolation levels, and database deadlocks.

<details><summary>Answer</summary>

**Isolation levels and what each prevents:**

| Level | Dirty read | Non-repeatable read | Phantom read |
|---|---|---|---|
| `READ UNCOMMITTED` | possible | possible | possible |
| `READ COMMITTED` | prevented | possible | possible |
| `REPEATABLE READ` | prevented | prevented | possible* |
| `SERIALIZABLE` | prevented | prevented | prevented |

Defaults: **Postgres and Oracle `READ COMMITTED`**; **MySQL/InnoDB `REPEATABLE READ`**;
SQL Server `READ COMMITTED`. *Postgres's `REPEATABLE READ` (snapshot isolation) actually
prevents phantoms too, but permits **write skew**, which is what its true `SERIALIZABLE`
(SSI) exists to stop.

**MVCC** (Postgres, Oracle, InnoDB): readers see a **snapshot** and **don't block writers**;
writers don't block readers. The costs are **row versions** needing cleanup (`VACUUM`,
bloat, transaction-ID wraparound) and long-running transactions holding old snapshots open
and preventing that cleanup. **A long idle-in-transaction session is a real production
problem** and a good thing to mention.

**Lock types:** row vs table; shared (read) vs exclusive (write); intent locks; plus
explicit `SELECT ... FOR UPDATE` (lock rows for a read-modify-write) and
`FOR UPDATE SKIP LOCKED` — which is the correct way to build a work-queue table, worth
knowing.

**Database deadlocks** — same Coffman conditions as quiz 01 Q8, but with a crucial
difference: **the database detects the cycle and resolves it** by killing a victim
transaction with a retryable error (Postgres `40P01`, MySQL 1213). Your application must
**catch it and retry**, ideally with backoff. In Java that's a `DeadlockLoserDataAccessException`
from Spring's translation.

**Common causes and fixes:**
- **Inconsistent ordering** — the same fix as in-process: **update rows in a consistent
  order** (e.g. always by ascending primary key). This is why a batch update sorted
  differently in two code paths deadlocks.
- **Lock escalation / gap locks** — InnoDB's `REPEATABLE READ` takes gap locks on range
  scans, so two inserts into the same gap can deadlock. Often surprising.
- **Long transactions** — shrink the transaction, and **never do an HTTP call inside one**
  (quiz 10 Q5).
- **Missing index** on a `WHERE` clause of an `UPDATE` — the database locks far more rows
  than you intended (in InnoDB, potentially every row it scans). Adding the index fixes both
  the performance *and* the locking.

**Diagnosing blocking:** `pg_stat_activity` + `pg_locks` (or `pg_blocking_pids()`),
`SHOW ENGINE INNODB STATUS` for the last deadlock, `sys.dm_tran_locks`. And set a
`lock_timeout` / `statement_timeout` so a blocked query fails rather than hanging forever —
that turns a stuck request thread into a fast error.
</details>

---

### 9. What is the N+1 query problem, and how do you fix it?

<details><summary>Answer</summary>

**The single most common database performance bug in application code**, and the most likely
follow-up to quiz 10 Q1.

You fetch N parent rows with one query, then the ORM issues one more query per parent to load
an association: **1 + N queries**.

```java
List<Order> orders = orderRepository.findAll();            // 1 query
for (Order o : orders) {
    o.getCustomer().getName();                              // +1 query EACH -> 1 + N
}
```
With 500 orders that's 501 round trips. Each is fast in isolation, so **the slow-query log
shows nothing** — that's what makes it insidious. The cost is 501 × network latency, and it's
invisible to every per-query metric you have.

**Fixes, in order of preference:**

1. **Fetch join / entity graph** — one query with a join:
   ```java
   @Query("SELECT o FROM Order o JOIN FETCH o.customer")
   @EntityGraph(attributePaths = "customer")
   ```
2. **Batch fetching** — `@BatchSize(size = 50)` or
   `spring.jpa.properties.hibernate.default_batch_fetch_size=50`. Turns 1+N into 1+N/50 by
   collecting ids and using `WHERE id IN (...)`. Cheap to enable globally and a very good
   default.
3. **A projection / DTO query** — select exactly the columns you need in one statement.
   Usually the best answer for a read-only endpoint, and it avoids entity overhead entirely.
4. **Two queries and join in memory** — fetch parents, collect the ids, one
   `WHERE id IN (:ids)` query, then map. Explicit and predictable; this is what you do when
   the "association" is another team's HTTP service (quiz 10 Q1, point 4) — one bulk call
   instead of N.

**The variant to watch for:** `MultipleBagFetchException`, or the **cartesian product** when
you `JOIN FETCH` two collections — you get N×M rows. Fetch one collection per query, and use
batch fetching for the rest.

**How to detect it:**
- **`spring.jpa.show-sql=true`** in a test, or better, count queries with
  Hibernate's statistics or **datasource-proxy**. Assert the query count in a test — that's
  how you stop it regressing.
- `p6spy` / `datasource-proxy` logging in dev.
- The tell in production: latency proportional to result-set size, with low DB CPU and a
  high query *count*.

**Also worth naming:** the mirror-image bug, **over-fetching** — `FetchType.EAGER` on
associations pulls a huge object graph on every load. **Default to `LAZY`** and fetch
explicitly where needed. (`@ManyToOne` and `@OneToOne` are EAGER by default in JPA, which is
the wrong default and a frequent surprise.)

**Scala:** doobie and Slick make the queries explicit — there's no lazy-loading proxy to
accidentally trigger, so N+1 becomes something you'd have to write on purpose. That's a
genuine argument for the migration and worth mentioning.
</details>

---

### 10. How do you size a database connection pool?

<details><summary>Answer</summary>

**Smaller than people expect**, and the reasoning connects directly to quiz 02 Q4.

- A connection isn't free on the server: each is a backend process (Postgres) or thread, with
  memory (`work_mem` per sort, per connection!) and scheduling cost. **More connections than
  the database can usefully run in parallel makes throughput *worse*** — you add context
  switching and lock contention, not capacity.
- HikariCP's own guidance: **`connections = ((core_count × 2) + effective_spindle_count)`** as
  a starting point. For a typical 4–8 core database that's roughly **10–20**, not 200.
- **Little's law** applies: `pool_size = throughput × mean_service_time`. 500 rps at 20 ms of
  DB time needs ~10 connections. If you have 200, you've built a queue.

**The alignment point — this is what's being scored:**
> **Tomcat threads ≥ application worker threads ≥ connection pool size.**

If your pool is smaller than your thread count (normal and correct), request threads will
**queue at the pool**, and that queueing shows up as latency, not as an error — unless you
set `connectionTimeout`, which converts it into a fast, visible failure. `connectionTimeout`
exhaustion is one of the most common causes of the "p99 is 3 s, CPU is 30%" shape in
quiz 02 Q15.

**Settings that matter:**
- `maximumPoolSize` — as above.
- `minimumIdle` — usually equal to max, to avoid connection-churn latency spikes.
- `connectionTimeout` (default 30 s — usually far too long; make it ~1–3 s so you fail fast).
- `maxLifetime` — shorter than any database or load-balancer idle timeout, or you'll hand out
  dead connections.
- `validationTimeout`, `leakDetectionThreshold` (invaluable for finding unclosed connections).

**Metrics to export:** `hikaricp.connections.active`, `.idle`, **`.pending`** (the leading
indicator), `.timeout`, and `.usage`/`.acquire` timings.

**And the design rules:**
- **Never hold a connection across a network call to another service** (quiz 10 Q1/Q5). That
  multiplies your pool's occupancy by someone else's latency.
- Keep transactions short.
- Separate pools for OLTP and reporting/batch, so a long analytical query can't starve
  request traffic — the bulkhead pattern again.
- **With virtual threads (JDK 21)** the thread count stops being the limit, so the connection
  pool becomes the *only* bound — you must set it deliberately and bound admission
  separately (quiz 03 Q9).
</details>

---

### 11. How do you paginate a large result set?

<details><summary>Answer</summary>

**`OFFSET`/`LIMIT` is the obvious answer and it doesn't scale:**
```sql
SELECT * FROM trades ORDER BY id LIMIT 20 OFFSET 100000;
```
The database must **fetch and discard 100 000 rows** to return 20. Cost grows linearly with
page number, so page 1 is instant and page 5 000 times out — a classic "it's fast in testing"
bug. Worse, if rows are inserted or deleted between requests, **rows shift between pages** so
users see duplicates or miss records entirely.

**Keyset (cursor / "seek") pagination** — the fix:
```sql
SELECT * FROM trades
WHERE (trade_date, id) < (:last_date, :last_id)     -- the cursor from the previous page
ORDER BY trade_date DESC, id DESC
LIMIT 20;
```
- **Constant time for every page**, because the index seeks directly to the position.
- **Stable** under concurrent inserts.
- Requires a **unique, ordered** key (append the primary key to break ties — otherwise rows
  with equal sort keys can be skipped or repeated).
- The cost: **no random access to page N** — only next/previous. That's usually fine for
  infinite scroll and APIs, and unacceptable for a UI with numbered pages.

**Other considerations:**
- **`ORDER BY` must be deterministic.** Pagination over a non-unique sort key is undefined
  and will silently drop rows. Always include a tiebreaker.
- **`COUNT(*)` for a total is often the expensive part** — it scans. Consider an approximate
  count (`pg_class.reltuples`), a cached count, or just not showing a total.
- **Cap the page size.** An unbounded `?limit=` parameter is both a performance and a
  denial-of-service issue.
- For **exports/streaming**, don't paginate at all — stream with a server-side cursor and a
  fetch size, so you don't materialise the whole result in memory. In JDBC that means
  `setFetchSize` plus autocommit off for Postgres; otherwise the driver buffers everything
  and you OOM.
</details>

---

### 12. Explain ACID, and where the guarantees are weaker than people assume.

<details><summary>Answer</summary>

- **Atomicity** — all or nothing. Implemented with a write-ahead log and rollback.
- **Consistency** — the transaction moves the database from one valid state to another,
  respecting constraints. (Note this is *not* the C in CAP, a common conflation worth
  flagging.)
- **Isolation** — concurrent transactions don't interfere. **This is the one that's weaker
  than assumed**: the default is almost never `SERIALIZABLE` (Q8), so `READ COMMITTED`
  permits non-repeatable reads and phantoms, and snapshot isolation permits write skew.
  Most application bugs attributed to "a race condition in the database" are really
  "we assumed serializable and got read committed".
- **Durability** — committed data survives a crash. Weaker than assumed if
  `synchronous_commit = off`, or with asynchronous replication (a committed transaction can
  be **lost on failover** — that's your RPO, and it belongs in the redundancy answer in
  quiz 08 Q2).

**The practical consequence:** read-modify-write across two statements is **not** safe just
because it's in a transaction:
```sql
SELECT balance FROM accounts WHERE id = 1;        -- 100
UPDATE accounts SET balance = 90 WHERE id = 1;    -- lost update under READ COMMITTED
```
Fixes: do it in one statement (`SET balance = balance - 10`), take a pessimistic lock
(`SELECT ... FOR UPDATE`), or use optimistic locking (a `version` column with
`WHERE version = ?`, retrying on zero rows updated — this is JPA's `@Version`).

**Distributed transactions:** two-phase commit exists and is generally avoided — it's slow
and the coordinator is a failure point. The usual alternatives are the **saga pattern**
(compensating actions), the **transactional outbox** (write the event in the same
transaction as the state change, publish it asynchronously), and **idempotent consumers**.
Naming the outbox pattern is a strong signal for anyone doing event-driven work.
</details>

---

### 13. What ORM traps should you know about?

<details><summary>Answer</summary>

Beyond N+1 (Q9):

- **`FetchType.EAGER` by default on `@ManyToOne`/`@OneToOne`** — pulls associations you never
  use, on every load. Set them `LAZY` explicitly.
- **`LazyInitializationException`** — touching a lazy association after the session closed.
  The bad fixes are `spring.jpa.open-in-view=true` (which holds a connection for the whole
  request — **off by default for good reason**, and Boot warns about it) and `EAGER`
  everywhere. The good fix is to fetch what you need in the query, or map to a DTO inside
  the transaction.
- **Entities escaping to the API layer** — couples your HTTP contract to your schema, and
  serialising a lazy proxy throws. Map to DTOs (quiz 10 Q1).
- **`@Transactional` on a read** — use `readOnly = true` so Hibernate skips dirty checking
  and the driver can optimise.
- **The first-level cache / flush timing** — Hibernate may reorder or defer your SQL until
  flush, which makes "my update didn't happen" and constraint-violation-at-commit confusing.
  Auto-flush before a query in the same transaction is a common surprise.
- **`equals`/`hashCode` on entities** — must be based on a stable business key, not a
  generated id (which is null before persist, breaking `Set` membership) and not all fields
  (which changes as the entity mutates). This is quiz 09 Q5 in a specific and painful form,
  and it's why **records are a poor fit for entities** (quiz 09 Q14).
- **Cascade and `orphanRemoval`** — `CascadeType.ALL` including `REMOVE` deletes more than
  you meant, sometimes across a whole graph.
- **Bulk operations** — a JPQL `UPDATE`/`DELETE` bypasses the first-level cache and lifecycle
  callbacks, leaving stale entities in the session.
- **Batch inserts don't batch by default** — you need
  `hibernate.jdbc.batch_size`, ordered inserts, and an id strategy that permits batching
  (`IDENTITY` **disables** JDBC batching, because each insert must round-trip to get the id;
  use a sequence with a pooled allocator).
- **Migrations aren't the ORM's job** — never `hibernate.hbm2ddl.auto=update` in production.
  Use **Flyway or Liquibase**, versioned and in source control (see Q14).

**The overall judgement to offer:** an ORM is excellent for CRUD on an object graph and poor
for reporting and bulk work. Mixing is fine — use JPA where it helps, and drop to plain SQL
(`JdbcTemplate`, jOOQ) where it doesn't. **Know what SQL your ORM emits**; the failure mode
of ORMs is that they make the expensive thing invisible.

**Scala:** doobie/Slick/Quill are much closer to SQL and don't have lazy loading, sessions, or
a first-level cache — so most of this list simply doesn't exist. The trade is that you write
more mapping code by hand.
</details>

---

### 14. How do you deploy a schema change safely, and how do you roll it back?

<details><summary>Answer</summary>

This bridges directly to the **rollback/back-out probe in quiz 08 Q3**, and it's where most
candidates are vague — so being specific pays.

**Migrations:** versioned scripts in source control, applied by **Flyway or Liquibase**, run
automatically on deploy, with a checksummed history table so you can't silently diverge.
Never `hbm2ddl.auto=update`. Never a DBA applying SQL by hand.

**The core principle: make every migration backward-compatible**, so the old and new
application versions can both run against the same schema. You need this for rolling deploys
and blue-green — during the deploy **both versions are live simultaneously**, which is the
fact people miss.

**Expand/contract (a.k.a. parallel change)** — the answer to "how do you rename a column
without downtime":
1. **Expand:** add the new column (nullable, or with a default). Deploy.
2. **Dual-write:** the application writes both old and new. Deploy.
3. **Backfill** the new column for existing rows — **in batches**, so you don't lock the
   table or blow up replication lag.
4. **Cut over reads** to the new column. Deploy. *This is the last easily-reversible point.*
5. **Stop writing** the old column. Deploy.
6. **Contract:** drop the old column, in a later release.

Each step is independently deployable and independently revertible.

**Why rollback is the hard part — say this explicitly:** *code* rolls back trivially, but
**a migration that dropped a column cannot be undone** — the data is gone. So:
- **Additive changes are safe and reversible; destructive ones are not.** Defer destruction
  to a separate, later release, once you're confident.
- The real back-out plan for a destructive change is **restore from backup**, which means
  data loss between the backup and now. That's usually unacceptable, which is why you avoid
  needing it.
- Prefer **forward-fix** for schema problems, and design so that rolling back the *code*
  alone is always sufficient.

**Operational hazards worth naming:**
- **Locking DDL.** `ALTER TABLE` may take an exclusive lock and block all traffic. In
  Postgres, adding a nullable column is instant, but adding one with a non-null default
  rewrote the whole table before v11. `CREATE INDEX CONCURRENTLY` (and
  `ALTER TABLE ... VALIDATE CONSTRAINT`) exist precisely to avoid long locks — use them, and
  know they can't run inside a transaction.
- **Long migrations under a lock timeout** — set `lock_timeout` so a migration that can't get
  its lock fails fast instead of queueing every query behind it. A blocked `ALTER TABLE`
  blocks everything behind it, which is a full outage from a "safe" change.
- **Test the migration on production-scale data.** A backfill that takes 3 seconds on 1 000
  rows takes 40 minutes on 50 million.
- **Test the restore**, not just the backup. An untested backup is a hope.
</details>

---

### 15. How would you insert a million rows?

<details><summary>Answer</summary>

**Not with a million `INSERT` statements.** In rough order of speed:

1. **Bulk load utility** — `COPY` (Postgres), `LOAD DATA INFILE` (MySQL), `bcp` (SQL Server).
   Orders of magnitude faster than `INSERT`s; this is the right answer for a genuine bulk load.
   In Java, Postgres's `CopyManager` streams straight from your application.
2. **Multi-row `INSERT`** — `INSERT INTO t (a,b) VALUES (?,?),(?,?),...` in batches of a few
   hundred to a few thousand. One round trip per batch.
3. **JDBC batching** — `addBatch()`/`executeBatch()` with `rewriteBatchedStatements=true`
   (MySQL) or `reWriteBatchedInserts=true` (Postgres), which rewrites them into multi-row
   inserts. Without those flags, "batching" may still send one statement per row.
4. **Individual inserts, autocommit on** — the worst case: one round trip *and one commit
   (fsync)* per row.

**The multipliers that dominate:**
- **Round trips.** 1 M inserts at 0.5 ms of network latency is **8 minutes of pure latency**,
  regardless of how fast the database is. Batching attacks this, and it's usually the whole
  problem.
- **Commits.** Each commit is an fsync. Wrap batches in a transaction — but not all million in
  one, or you build an enormous rollback segment and hold locks forever. A few thousand per
  transaction is a reasonable middle ground.
- **Indexes and constraints.** Every index is maintained per row. For a large one-off load,
  **drop the indexes, load, then rebuild** — often several times faster overall. Same for
  triggers and foreign-key checks, if you can defer them safely.

**Also:**
- **Idempotency and restartability.** A load that fails at row 700 000 should be resumable.
  Batch with a checkpoint, and use `INSERT ... ON CONFLICT DO NOTHING`/`ON DUPLICATE KEY` so
  a retry doesn't duplicate.
- **Don't hold it all in memory** — stream from the source, don't `List<Row>` a million rows
  (and note that a large `byte[]`/list becomes a humongous allocation — measured in
  `problems/gc-labs/L04Humongous.java`).
- **Consider the replication and vacuum impact** — a huge write burst causes replica lag and
  bloat. Throttle if it shares a database with live traffic.
- For **updates** of a million rows: batch by primary key range with a `LIMIT`, sleep between
  batches, and keep each transaction short — otherwise you hold locks and block production
  traffic (Q8).
</details>

---

## Score yourself

**Q1 is the actual question from the bank** — practise the four-step *method* out loud, since
the probe areas will be used to interrupt you. Q3, Q6, and Q9 are the highest-value follow-ups
(indexes, `NOT IN`'s silent-empty-result trap, and N+1). Q10 and Q14 connect this quiz to the
concurrency material and to the delivery-process questions in quiz 08 — being able to link
them is what makes the answers sound like one coherent engineer rather than three revision
topics.
