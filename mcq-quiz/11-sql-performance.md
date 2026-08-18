# MCQ 11 — SQL & Database Performance

15 questions. Source: [`quiz/11`](../quiz/11-sql-performance.md).

---

### Q1. A query is slow. What's the first thing you do?

- **A.** Add an index on the columns in the `WHERE` clause
- **B.** Rewrite it to avoid `SELECT *`
- **C.** Get the execution plan, and establish whether it's the query, contention, or the connection pool
- **D.** Increase the database's memory

<details><summary>Answer</summary>

**C.** `EXPLAIN (ANALYZE, BUFFERS)` before changing anything. And first establish *what kind* of slow:
always or sometimes (intermittent points at load, locking, or plan instability — not the SQL text);
all inputs or specific parameters (data skew or a cached plan); and did it change recently.

Critically: **time spent waiting for a connection is not query time.** Check
`hikaricp_connections_pending` before blaming the database.

In the plan, the highest-value single observation is **estimated vs actual row counts diverging
badly** — that means stale statistics and the optimiser picked a plan for the wrong data shape, which
is a completely different fix from adding an index.

- **A** and **B** are candidate fixes applied before diagnosis.

→ `quiz/11` Q1
</details>

---

### Q2. Beyond transferring extra bytes, what's the main technical cost of `SELECT *`?

- **A.** It defeats covering indexes, forcing a table lookup per row
- **B.** It locks more rows
- **C.** It bypasses the query cache
- **D.** It prevents the use of prepared statements

<details><summary>Answer</summary>

**A.** If an index contains every column the query needs, the database answers from the index alone
(an **index-only scan**). Ask for one extra column and it must visit the table for every row — often
the difference between fast and slow.

Plus: it breaks when the schema changes (a new column silently appears; `SELECT *` in a view or
`INSERT INTO ... SELECT *` breaks outright), and it hides which columns are actually used so nobody
can safely drop one.

Fine for: ad-hoc exploration, `EXISTS` subqueries (nothing is projected), and genuinely needing every
column.

→ `quiz/11` Q2
</details>

---

### Q3. `CREATE INDEX idx ON trades (account_id, trade_date, status)`. Which query can use it well?

- **A.** `WHERE status = 'SETTLED'`
- **B.** `WHERE trade_date > '2026-01-01'`
- **C.** `WHERE status = 'SETTLED' AND trade_date > '2026-01-01'`
- **D.** `WHERE account_id = 5 AND trade_date > '2026-01-01'`

<details><summary>Answer</summary>

**D.** The **leftmost-prefix rule**: a composite index is usable for a leading prefix of its columns.
So `account_id`, `account_id + trade_date`, or all three — but not `trade_date` or `status` alone.

Design guidance: put **equality predicates before range predicates**, and the most selective /
most-commonly-filtered column first.

And know when an index **doesn't** help: low selectivity (fetching 40% of the table — a sequential
scan is genuinely cheaper, and the optimiser is right to choose it), small tables, stale statistics,
or a query written so the index can't be used (Q4). Plus every index costs every write — **too many
indexes is a real and common problem**.

→ `quiz/11` Q3
</details>

---

### Q4. Which predicate can use a plain B-tree index on `email`?

- **A.** `WHERE UPPER(email) = 'A@B.COM'`
- **B.** `WHERE email = 'a@b.com'`
- **C.** `WHERE email LIKE '%@bofa.com'`
- **D.** `WHERE COALESCE(email, '') = 'a@b.com'`

<details><summary>Answer</summary>

**B.** The principle: **keep the indexed column bare on one side of the operator.** Any function,
arithmetic or cast applied *to the column* means the database must evaluate it per row — exactly what
an index exists to avoid. The term is **sargable**.

- **A** and **D** apply functions to the column. Fix with an **expression index**
  (`CREATE INDEX ON users (lower(email))`) or `citext`.
- **C** has a **leading wildcard**, which a B-tree can't seek on. `LIKE 'foo%'` can. For genuine
  substring search you want `pg_trgm` + GIN.

The sneakiest variant: an **implicit cast** from comparing a `varchar` column to a number. Silently
disables the index, with no visible mistake in the SQL — and it's a classic cause of "the same query
is fast in one environment and slow in another" when column types differ.

→ `quiz/11` Q4
</details>

---

### Q5. `WHERE account_id NOT IN (SELECT account_id FROM closed_accounts)` and the subquery returns a NULL. Result?

- **A.** **Zero rows**, silently
- **B.** NULLs are ignored; the query works normally
- **C.** A SQL error
- **D.** Only rows with a non-null `account_id` are returned

<details><summary>Answer</summary>

**A.** `x NOT IN (1, 2, NULL)` expands to `x <> 1 AND x <> 2 AND x <> NULL`. The last term is
**`UNKNOWN`**, never `TRUE`, so the whole conjunction can never be `TRUE`.

**No error, no warning, no rows.** One of the highest-value SQL gotchas precisely because the failure
is silent and total.

**Default to `NOT EXISTS`** — NULL-safe, expresses the anti-join intent, and modern optimisers handle
it well. `LEFT JOIN ... IS NULL` also works.

Other NULL traps: `NULL = NULL` is `UNKNOWN`; `COUNT(column)` skips NULLs but `COUNT(*)` doesn't;
`SUM` of an empty set is `NULL` not `0`; `UNIQUE` permits multiple NULLs in most databases.

→ `quiz/11` Q6
</details>

---

### Q6. `UNION` vs `UNION ALL` — what's the cost difference?

- **A.** None; they're synonyms
- **B.** `UNION ALL` de-duplicates
- **C.** `UNION` de-duplicates, requiring a sort or hash over the whole result
- **D.** `UNION` is parallelisable and `UNION ALL` isn't

<details><summary>Answer</summary>

**C.** If you know the branches are disjoint, `UNION ALL` is strictly cheaper. Using `UNION` out of
habit is a very common, invisible cost.

A legitimate use of `UNION ALL`: splitting an `OR` across *different* columns into two separately
indexable queries, since `WHERE email = ? OR phone = ?` often can't use either index.

→ `quiz/11` Q7
</details>

---

### Q7. In PostgreSQL, what changed about CTEs (`WITH`) in version 12?

- **A.** They became recursive
- **B.** They stopped being an optimisation fence and are now inlined by default when referenced once
- **C.** They became materialised by default
- **D.** They were deprecated

<details><summary>Answer</summary>

**B.** Before 12 a CTE was **always materialised**, so predicates couldn't be pushed into it — which
made CTEs a notorious performance trap. Postgres 12+ inlines them by default, with explicit control
via `WITH ... AS MATERIALIZED` / `NOT MATERIALIZED`.

**Know your version** — this is a great detail to raise because it shows you've actually been burned.
Other databases differ (SQL Server, Oracle and MySQL 8+ generally inline).

Occasionally you *want* the fence: materialising an expensive subquery once and reusing it.

→ `quiz/11` Q7
</details>

---

### Q8. A plan shows a nested loop with an estimate of 1 row and an actual of 2 million. What's the fix?

- **A.** Add an index on the join column
- **B.** Increase `work_mem`
- **C.** Rewrite as a `UNION`
- **D.** Update the statistics — the optimiser chose a plan for the wrong data shape

<details><summary>Answer</summary>

**D.** That divergence *is* the diagnosis. A nested loop is excellent when the outer input is small
and catastrophic when it isn't — and the optimiser only chose it because it believed the wrong
cardinality. `ANALYZE`, extended statistics for correlated columns, or a rewrite.

- **A** treats the symptom; the plan will still be wrong.
- **B** helps a *hash* join spilling to disk, which is a different signature.

Worth knowing the three algorithms: **nested loop** (O(n·m), or O(n log m) with an indexed inner
side); **hash join** (O(n+m), needs memory, spills if it exceeds `work_mem`); **merge join** (sorted
inputs walked together).

→ `quiz/11` Q5
</details>

---

### Q9. A `LEFT JOIN` with a condition on the right table in the `WHERE` clause. What happens?

- **A.** It works as intended
- **B.** It returns a cartesian product
- **C.** It silently becomes an inner join, because `NULL` fails the predicate
- **D.** A syntax error

<details><summary>Answer</summary>

**C.** Rows where the right side didn't match have `NULL` there, and `NULL <op> value` is `UNKNOWN`,
so those rows are filtered out — defeating the entire point of the outer join.

If you meant to filter the optional side, the condition belongs in the **`ON` clause**. Very common
bug, and it passes review because the SQL looks reasonable.

Related join hazard: **fan-out** — joining one-to-many multiplies rows so aggregates double-count.
The tell is someone adding `DISTINCT` to "fix" it, which masks the real problem and is slow.

→ `quiz/11` Q5
</details>

---

### Q10. Why is the N+1 query problem so hard to spot?

- **A.** Each individual query is fast, so it never appears in the slow-query log
- **B.** It only occurs under concurrency
- **C.** It only affects `SELECT` statements
- **D.** ORMs hide it in the stack trace

<details><summary>Answer</summary>

**A.** 501 queries at 0.5 ms each is 250 ms of pure round trips, and **no single query is slow**. The
tell in production is latency proportional to result-set size, with low DB CPU and a high query
*count*.

Fixes in order: **fetch join / entity graph**; **batch fetching**
(`hibernate.default_batch_fetch_size=50` — cheap to enable globally and a very good default);
**a projection/DTO query**; or two queries with an `IN` clause and an in-memory join — which is also
what you do when the "association" is another team's HTTP service (one bulk call, not N).

Detect it by **asserting the query count in a test** (datasource-proxy or Hibernate statistics).
That's how you stop it regressing.

→ `quiz/11` Q9
</details>

---

### Q11. How large should a database connection pool be?

- **A.** One per application thread
- **B.** 100 as a safe default
- **C.** As large as the database's `max_connections`
- **D.** Roughly `(cores × 2) + spindles` — typically 10–20, not 200

<details><summary>Answer</summary>

**D.** HikariCP's own guidance. A connection isn't free on the server — each is a backend process or
thread with memory (`work_mem` **per sort, per connection**) — and **more connections than the
database can usefully run in parallel makes throughput worse**, not better.

Little's law sanity check: 500 rps at 20 ms of DB time needs ~10 connections.

The alignment point: **Tomcat threads ≥ worker threads ≥ pool size.** Request threads *will* queue at
the pool, which is correct — and you must set `connectionTimeout` (default 30 s is far too long) so
that queueing becomes a fast, visible failure rather than invisible latency.

→ `quiz/11` Q10
</details>

---

### Q12. Why does `LIMIT 20 OFFSET 100000` get slower as the page number grows?

- **A.** The index becomes less effective at depth
- **B.** The database must fetch and discard 100,000 rows to return 20
- **C.** The result set is cached only for early pages
- **D.** It doesn't — `OFFSET` is constant-time

<details><summary>Answer</summary>

**B.** Cost grows linearly with page number, so page 1 is instant and page 5000 times out — a classic
"it's fast in testing" bug. And if rows are inserted or deleted between requests, **rows shift between
pages**, so users see duplicates or miss records.

The fix is **keyset (cursor/seek) pagination**: `WHERE (trade_date, id) < (:last_date, :last_id)`.
Constant time per page and stable under concurrent inserts. The cost: **no random access to page N**
— only next/previous, which is fine for infinite scroll and APIs but not for numbered pages.

Two related points: the `ORDER BY` **must be deterministic** (append the primary key, or rows get
skipped or repeated); and `COUNT(*)` for a total is often the expensive part.

→ `quiz/11` Q11
</details>

---

### Q13. `SELECT balance ...` then `UPDATE accounts SET balance = 90 ...` inside one `READ COMMITTED` transaction. Safe?

- **A.** No — a concurrent transaction can update between the read and the write, producing a lost update
- **B.** Yes — it's in a transaction, so it's atomic
- **C.** Yes, because `READ COMMITTED` prevents dirty reads
- **D.** Only if the table has a primary key

<details><summary>Answer</summary>

**A.** A transaction gives you atomicity and isolation *at the configured level* — and `READ
COMMITTED` permits non-repeatable reads, so read-modify-write across two statements races.

**Most application bugs blamed on "a database race condition" are really "we assumed serializable and
got read committed."**

Fixes: do it in one statement (`SET balance = balance - 10`), take a pessimistic lock
(`SELECT ... FOR UPDATE`), or use optimistic locking (a `version` column, retrying on zero rows
updated — JPA's `@Version`).

Defaults worth knowing: Postgres/Oracle/SQL Server `READ COMMITTED`; MySQL/InnoDB `REPEATABLE READ`.

→ `quiz/11` Q12, Q8
</details>

---

### Q14. Two transactions deadlock in the database. What happens, and what must your application do?

- **A.** Both block until a timeout
- **B.** The database escalates to a table lock
- **C.** The database detects the cycle, kills a victim with a retryable error, and your application must catch and retry
- **D.** Both roll back automatically and the client sees success

<details><summary>Answer</summary>

**C.** Unlike in-process deadlock, the database **detects** it (Postgres `40P01`, MySQL 1213) and
sacrifices a victim. Spring surfaces it as `DeadlockLoserDataAccessException`. **Retry with backoff.**

Common causes and fixes: **inconsistent update ordering** (fix: always update rows in a consistent
order, e.g. ascending primary key — same principle as in-process lock ordering); gap locks under
InnoDB's `REPEATABLE READ`; long transactions (shrink them, and **never do an HTTP call inside one**);
and a **missing index on an `UPDATE`'s `WHERE` clause**, which makes the database lock far more rows
than you intended — adding the index fixes both the performance and the locking.

→ `quiz/11` Q8
</details>

---

### Q15. You need to insert a million rows. What dominates the cost?

- **A.** Parsing the SQL
- **B.** Round trips and commits — 1 M inserts at 0.5 ms latency is 8 minutes of pure network time
- **C.** The primary key generation
- **D.** Disk write bandwidth

<details><summary>Answer</summary>

**B.** Round trips dominate regardless of how fast the database is, and each commit is an **fsync**.
So: batch (multi-row `INSERT`, or JDBC `addBatch` **with `reWriteBatchedInserts=true`** — without that
flag "batching" may still send one statement per row), and wrap batches in transactions, but not all
million in one.

Best of all for a genuine bulk load: **`COPY`** (Postgres) / `LOAD DATA INFILE` (MySQL) — orders of
magnitude faster.

Also: **drop and rebuild indexes** around a large one-off load; make it **restartable and idempotent**
(`ON CONFLICT DO NOTHING`) so a failure at row 700,000 is resumable; stream rather than materialising
a million rows in memory; and throttle if it shares a database with live traffic, or you cause
replica lag and bloat.

→ `quiz/11` Q15
</details>

---

## Score yourself

- **12+** — good.
- **Q1 is the actual question from the bank** — practise the *method* out loud, because the probe
  areas will be used to interrupt you.
- **Q5 (the silent-empty-result `NOT IN`) and Q10 (N+1) are the two highest-value gotchas.** Both fail
  without any error at all, which is what makes them worth knowing cold.
