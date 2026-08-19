# LeetCode "Java Interview: 100 Important Questions" — standalone set

The 129 questions from that post, **in the post's own order, one answer each, duplicates intact.**

This is deliberately a *second* pass over ground `quiz/16`–`19` already covers. The difference is the
shape:

| | `quiz/16`–`19` | this folder |
|---|---|---|
| Organisation | grouped by topic, duplicates merged, 14 `CompletableFuture`-method questions consolidated into 7 | **1:1 with the post**, Q1…Q129 in source order |
| Answer length | full treatment — why it works, what breaks, measured numbers | **short** — what it is, the idiom, one gotcha |
| Use it for | learning the topic properly | **checking you can answer the actual list**, and drilling in the order you'll re-read it |

Where the post asks the same thing twice (it does, about a dozen times) the second occurrence is
answered **from a different angle** rather than pointing back — so a repeat still earns its space. Those
are flagged `↺`.

Every answer ends with a `→` pointer into the deep treatment. If you can't answer one here, read there.

---

## Files

| File | Questions | Covers |
|---|---|---|
| [`01-core-q1-q30.md`](01-core-q1-q30.md) | 1–30 | OOP and language basics, collections, first concurrency classes |
| [`02-core-q31-q60.md`](02-core-q31-q60.md) | 31–60 | locks, synchronisers, `CompletableFuture` methods, concurrent collections |
| [`03-core-q61-q90.md`](03-core-q61-q90.md) | 61–90 | functional interfaces, more `CompletableFuture`, queue family, `Executors` |
| [`04-core-q91-q109.md`](04-core-q91-q109.md) | 91–109 | `Future`, `CompletionService`, remaining `j.u.c.` inventory |
| [`05-patterns-q110-q129.md`](05-patterns-q110-q129.md) | 110–129 | the 20 design-pattern questions from "Edit 1" |

MCQ versions of all 129 are in [`mcq/`](mcq/), with their own progress file:

```bash
python3 tools/mcq_shuffle.py --root leetcode-set/mcq --split
python3 tools/mcq_track.py  --root leetcode-set/mcq drill -n 15
python3 tools/mcq_track.py  --root leetcode-set/mcq status
```

Progress lands in `leetcode-set/.mcq-progress.json`, kept separate from the main pack's, so this set's
coverage is tracked independently.

---

## An honest read on the source list

Worth knowing before you spend time on it.

**What it's good for:** breadth. It will surface any `java.util.concurrent` class you've never opened,
and roughly 30 of the questions are things you genuinely should be able to answer in one sentence.

**Its three weaknesses**, all of which the pack compensates for elsewhere:

1. **It's an API inventory, not an interview.** Around 40 of the 109 core questions are
   "what is `X`, and how is it used?" for one class or one method — 14 of them for individual
   `CompletableFuture` methods. Real interviews ask *why*, *what breaks*, and *what did you measure*.
   Knowing `thenAcceptBoth` exists is worth almost nothing; knowing a non-`Async` callback runs on an
   unspecified thread is worth a lot.
2. **It's from 2023 and shows it.** Zero questions on generics or type erasure (the single most-asked
   core-Java topic it omits), records, sealed types, pattern matching, virtual threads, the Java Memory
   Model, or immutable design. Its `finalize()` question treats overriding it as normal practice — that
   was deprecated in JDK 9 and deprecated *for removal* in 18.
3. **It has no answers.** So the failure mode is reading it, feeling familiar with the words, and
   discovering under pressure that you can't produce the content. Hence: answer out loud, then check.

**Do it in this order:** `mcq/` to find gaps fast → the long-form file for whatever you missed →
then stop revising breadth and go do `quiz/01` (JMM), `quiz/04`/`05` (GC) and the runnable
measurements in `problems/`. That's where interviews are decided. The post's own
question 2 — "how does the JVM handle garbage collection" — is answerable in two sentences or in twenty
minutes, and only one of those gets you hired.
