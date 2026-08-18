# tools/

Two scripts for reusing the 210 MCQs in `mcq-quiz/`. **Standard library only** — no pip install,
no venv. Both are executable.

---

## `mcq_shuffle.py` — fresh randomised practice sheets

Shuffles **question order** and the **A/B/C/D positions**, remapping the answer letter and every
letter reference in the explanation. Run it again for a different shuffle, so the questions stay
useful after you've started remembering that "the answer to Q7 is C".

**The canonical `mcq-quiz/` files are never modified.** Output goes to `practice/`, which is
disposable — delete and regenerate it freely.

```bash
tools/mcq_shuffle.py                    # all 14 decks -> practice/
tools/mcq_shuffle.py 13 15              # only those decks (number or slug)
tools/mcq_shuffle.py --split            # question sheet + separate answer key
tools/mcq_shuffle.py --pick 20 --split  # one mixed 20-question exam
tools/mcq_shuffle.py --seed 42          # reproducible (the seed is always printed)
tools/mcq_shuffle.py --choices-only     # keep question order, shuffle only the choices
tools/mcq_shuffle.py --check            # parse + validate, write nothing
```

**Use `--split`.** Inline `<details>` blocks are easy to peek at by accident; a separate answer
key is the only format that actually forces you to commit to a letter first.

Every generated question carries an invisible `<!-- mcq-id: ... -->` comment, so you can still log
results against the original question after a shuffle has renumbered everything.

---

## `mcq_track.py` — coverage tracking and drilling

```bash
tools/mcq_track.py drill                # interactive, auto-graded — start here
tools/mcq_track.py drill --deck 13 -n 20
tools/mcq_track.py drill --new          # only never-attempted
tools/mcq_track.py drill --weak -n 15   # 15 worst-performing
tools/mcq_track.py drill --due          # scheduled for review

tools/mcq_track.py status               # coverage, accuracy, weakest questions
tools/mcq_track.py due                  # what's scheduled
tools/mcq_track.py log 13:Q4 wrong      # record by hand (13:4, event-sourcing:Q4 also work)
tools/mcq_track.py export progress.md   # markdown report
tools/mcq_track.py reset --deck 13      # clear progress (asks first)
```

In `drill`, type `a`/`b`/`c`/`d` and it grades you — no self-reporting. `?` reveals without
grading (counted as not-known), `s` skips, `q` quits. Progress saves after every answer, so
quitting mid-session loses nothing.

**A correct-but-guessed answer counts as wrong.** The drill asks. A lucky guess is not knowledge,
and counting it as one is how you arrive at an interview with false confidence.

### Scheduling

Five-box Leitner. Right → up a box and further out (`0d / 1d / 3d / 7d / 21d`); wrong → straight
back to box 1. State lives in `.mcq-progress.json` at the repo root — plain JSON, safe to read or
delete.

---

## A convention these files rely on

**Inside an explanation, every option reference must be bolded** — `**A**`, not a bare `A`.

The shuffler only rewrites bolded references, deliberately. An earlier version also rewrote bare
capitals and that corrupted prose: the English article in "**A** handful of instructions" became
"B", the thread-pool formula `W/C` became `W/B`, and "B-tree" became "C-tree". A bare capital A–D
is genuinely ambiguous between an option reference and ordinary English, so the tool refuses to
guess.

`--check` enforces this. Run it after editing any `mcq-quiz/` file:

```bash
tools/mcq_shuffle.py --check
```

It fails with a non-zero exit and names the offending question if it finds an unbolded reference —
which would otherwise silently stop being remapped, leaving an explanation that dismisses the
wrong options.

---

## Suggested loop

```bash
tools/mcq_track.py drill --new -n 20     # work through unseen questions
tools/mcq_track.py status                # find the weak decks
tools/mcq_track.py drill --weak -n 15    # hammer those
tools/mcq_shuffle.py --pick 30 --split   # mock exam on paper
tools/mcq_track.py drill --due           # next day, review what's scheduled
```

Then close the laptop and say the answers out loud — `cheatsheets/answer-scripts.md`. Picking a
letter from four is not the skill being tested in an interview.
