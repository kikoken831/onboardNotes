"""
Progress tracking and spaced retrieval.

The schedule is the best-evidenced part of the whole design. Re-solving a problem from
an empty file after a delay is worth considerably more than re-reading a solution you
already wrote, so `judge next` surfaces due re-solves ahead of new problems.

Intervals are 1, 3, 7 and 21 days. A failed re-solve drops back to the first interval.
"""

import json
import os
import time

from . import catalogue

INTERVALS_DAYS = [1, 3, 7, 21]
DAY = 86400


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


def record(slug, verdict, elapsed_ms=None):
    """Log one attempt and advance or reset the retrieval schedule."""
    state = load()
    e = entry(state, slug)
    now = time.time()

    e["attempts"] += 1
    e["last_verdict"] = verdict
    e["last_attempt"] = now
    e["history"].append({"t": now, "v": verdict, "ms": elapsed_ms})
    e["history"] = e["history"][-40:]

    if verdict == "ACCEPTED":
        if not e["solved"]:
            e["solved"] = True
            e["first_solved"] = now
        stage = e.get("stage", 0)
        e["due"] = now + INTERVALS_DAYS[min(stage, len(INTERVALS_DAYS) - 1)] * DAY
        e["stage"] = min(stage + 1, len(INTERVALS_DAYS))
    elif verdict in ("WRONG ANSWER", "RUNTIME ERROR", "TIME LIMIT EXCEEDED"):
        # A failed re-solve means it did not stick. Back to the shortest interval.
        if e["solved"]:
            e["stage"] = 0
            e["due"] = now + INTERVALS_DAYS[0] * DAY

    save(state)
    return e


def due_now(state=None):
    """Slugs whose re-solve is due, most overdue first."""
    state = state or load()
    now = time.time()
    out = []
    for slug, e in state["problems"].items():
        if e.get("solved") and e.get("due") and e["due"] <= now:
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
            if e.get("due") and e["due"] <= time.time():
                row["due"] += 1
    return tiers, state
