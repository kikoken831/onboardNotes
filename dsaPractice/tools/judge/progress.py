"""
Progress tracking and spaced retrieval.

The schedule is the best-evidenced part of the whole design. Re-solving a problem from
an empty file after a delay is worth considerably more than re-reading a solution you
already wrote, so `judge next` surfaces due re-solves ahead of new problems.

Intervals are 1, 3, 7 and 21 days. Only a stress-tested ACCEPTED on a problem not yet
solved, or on a re-solve that is due, moves the schedule forward. A failed DUE re-solve
drops back to the first interval; attempts between re-solves change nothing.
"""

import json
import os
import time

from . import catalogue

INTERVALS_DAYS = [1, 3, 7, 21]
DAY = 86400


def _start_of_day(epoch):
    """Truncate an epoch timestamp to midnight local time."""
    t = time.localtime(epoch)
    return time.mktime(time.struct_time((t.tm_year, t.tm_mon, t.tm_mday,
                                         0, 0, 0, 0, 0, t.tm_isdst)))


def _is_due(due_epoch):
    """True when the due date is today or in the past (ignores time-of-day)."""
    return _start_of_day(due_epoch) <= _start_of_day(time.time())


def path():
    return os.path.join(catalogue.ROOT, ".progress.json")


def load():
    p = path()
    if not os.path.exists(p):
        return {"version": 1, "problems": {}}
    with open(p) as f:
        return json.load(f)


def save(state):
    tmp = path() + ".tmp"
    with open(tmp, "w") as f:
        json.dump(state, f, indent=2, sort_keys=True)
    os.replace(tmp, path())


def entry(state, slug):
    return state["problems"].setdefault(slug, {
        "attempts": 0,
        "solved": False,
        "first_solved": None,
        "last_verdict": None,
        "last_attempt": None,
        "stage": 0,          # index into INTERVALS_DAYS
        "due": None,         # epoch seconds, or None when not scheduled
        "history": [],
    })


FAILURES = ("WRONG ANSWER", "RUNTIME ERROR", "TIME LIMIT EXCEEDED")


def record(slug, verdict, elapsed_ms=None, full=True):
    """
    Log one attempt and move the retrieval schedule. Returns (entry, event).

    `full` is False for a pinned-examples-only run (`--fixed`, `-n 0`); that never
    counts as a solve. The schedule moves only on the attempts it is about:

      first full ACCEPTED        solved; first re-solve due in INTERVALS_DAYS[0]
      full ACCEPTED, re-solve due next interval (stage advances)
      full ACCEPTED, not due     nothing -- practising early is not a re-solve
      failure, re-solve due      back to the first interval (due in 1 day again)
      failure, not due           nothing -- a failed practice run keeps its progress

    event is one of: solved, advanced, not-due, reset, pinned-only, None.
    """
    state = load()
    e = entry(state, slug)
    now = time.time()

    e["attempts"] += 1
    e["last_verdict"] = verdict
    e["last_attempt"] = now
    e["history"].append({"t": now, "v": verdict, "ms": elapsed_ms,
                         **({} if full else {"pinned_only": True})})
    e["history"] = e["history"][-40:]

    due = bool(e["solved"] and e.get("due") and _is_due(e["due"]))
    event = None
    last = len(INTERVALS_DAYS) - 1

    if verdict == "ACCEPTED":
        if not full:
            event = "pinned-only"
        elif not e["solved"]:
            e["solved"] = True
            e["first_solved"] = now
            e["due"] = now + INTERVALS_DAYS[0] * DAY
            e["stage"] = 1
            event = "solved"
        elif due:
            stage = e.get("stage", 0)
            e["due"] = now + INTERVALS_DAYS[min(stage, last)] * DAY
            e["stage"] = min(stage + 1, len(INTERVALS_DAYS))
            event = "advanced"
        else:
            event = "not-due"
    elif verdict in FAILURES and due:
        # A failed re-solve means it did not stick: back to the first interval, as if
        # solved for the first time today.
        e["stage"] = 1
        e["due"] = now + INTERVALS_DAYS[0] * DAY
        event = "reset"

    save(state)
    return e, event


def due_now(state=None):
    """Slugs whose re-solve is due, most overdue first."""
    state = state or load()
    now = time.time()
    out = []
    for slug, e in state["problems"].items():
        if e.get("solved") and e.get("due") and _is_due(e["due"]):
            out.append((e["due"], slug))
    out.sort()
    return [slug for _, slug in out]


def status_of(state, slug):
    return state["problems"].get(slug)


def summary():
    """Counts by tier, for `judge status`."""
    state = load()
    tiers = {}
    for p in catalogue.all_problems():
        t = p["tier"]
        row = tiers.setdefault(t, {"total": 0, "implemented": 0, "solved": 0,
                                   "attempted": 0, "due": 0})
        row["total"] += 1
        if catalogue.is_implemented(p):
            row["implemented"] += 1
        e = state["problems"].get(p["slug"])
        if e:
            if e["attempts"]:
                row["attempted"] += 1
            if e["solved"]:
                row["solved"] += 1
            if e.get("due") and _is_due(e["due"]):
                row["due"] += 1
    return tiers, state
