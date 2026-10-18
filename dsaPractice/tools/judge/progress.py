"""
Progress tracking and SM-2 spaced retrieval.

Re-solving a problem from an empty file after a delay is worth considerably more than
re-reading a solution you already wrote, so `judge next` surfaces due re-solves ahead
of new problems.

Uses the SM-2 algorithm with per-problem ease factors. Quality is derived from solve
time (wall-clock from `judge open` to successful `judge run`) and failed attempts in
the current session:

    quality 5  solved in < 10 min, no prior failures
    quality 4  solved in 10-30 min, or ≤1 prior failure
    quality 3  solved in 30+ min, or 2+ prior failures
    quality 1  failed a due re-solve

SM-2 interval formula:
    repetition 0 → 1 day
    repetition 1 → 6 days
    repetition 2+ → previous_interval * ease

Ease adjusts after each review:
    ease += 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)
    ease = max(1.3, ease)
"""

import json
import os
import time

from . import catalogue

DAY = 86400
DEFAULT_EASE = 2.5
MIN_EASE = 1.3


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
    e = state["problems"].setdefault(slug, {
        "attempts": 0,
        "solved": False,
        "first_solved": None,
        "last_verdict": None,
        "last_attempt": None,
        "repetition": 0,     # SM-2 repetition count
        "interval": 0,       # current interval in days
        "ease": DEFAULT_EASE,
        "due": None,         # epoch seconds, or None when not scheduled
        "opened_at": None,   # epoch when judge open was called
        "history": [],
    })
    _migrate(e)
    return e


def _migrate(e):
    """Migrate old fixed-interval entries to SM-2 fields."""
    if "stage" in e:
        stage = e.pop("stage")
        if not e.get("repetition"):
            e["repetition"] = stage
        if not e.get("interval") and e.get("due") and e.get("last_attempt"):
            e["interval"] = max(1, round((e["due"] - e["last_attempt"]) / DAY))
        if "ease" not in e:
            e["ease"] = DEFAULT_EASE
    e.setdefault("ease", DEFAULT_EASE)
    e.setdefault("repetition", 0)
    e.setdefault("interval", 0)
    e.setdefault("opened_at", None)


FAILURES = ("WRONG ANSWER", "RUNTIME ERROR", "TIME LIMIT EXCEEDED")


def _sm2_quality(e):
    """Derive SM-2 quality (3-5) from solve time and recent failures."""
    opened = e.get("opened_at")
    now = time.time()
    solve_minutes = (now - opened) / 60 if opened else 999

    recent_failures = 0
    for h in reversed(e.get("history", [])):
        if h["v"] == "ACCEPTED" and not h.get("pinned_only"):
            break
        if h["v"] in FAILURES:
            recent_failures += 1

    if solve_minutes < 10 and recent_failures == 0:
        return 5
    if solve_minutes <= 30 or recent_failures <= 1:
        return 4
    return 3


def _sm2_interval(repetition, interval, ease):
    """Compute the next interval in days using SM-2."""
    if repetition == 0:
        return 1
    if repetition == 1:
        return 6
    return max(1, round(interval * ease))


def _sm2_ease(ease, quality):
    """Adjust ease factor after a review."""
    new = ease + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
    return max(MIN_EASE, new)


def mark_opened(slug):
    """Record the timestamp when a problem is opened for solving."""
    state = load()
    e = entry(state, slug)
    e["opened_at"] = time.time()
    save(state)


def record(slug, verdict, elapsed_ms=None, full=True):
    """
    Log one attempt and move the SM-2 schedule. Returns (entry, event).

    `full` is False for a pinned-examples-only run (`--fixed`, `-n 0`); that never
    counts as a solve.

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

    if verdict == "ACCEPTED":
        if not full:
            event = "pinned-only"
        elif not e["solved"]:
            e["solved"] = True
            e["first_solved"] = now
            quality = _sm2_quality(e)
            e["ease"] = _sm2_ease(e["ease"], quality)
            e["interval"] = 1
            e["repetition"] = 1
            e["due"] = now + 1 * DAY
            e["opened_at"] = None
            event = "solved"
        elif due:
            quality = _sm2_quality(e)
            e["ease"] = _sm2_ease(e["ease"], quality)
            e["interval"] = _sm2_interval(e["repetition"], e["interval"], e["ease"])
            e["repetition"] += 1
            e["due"] = now + e["interval"] * DAY
            e["opened_at"] = None
            event = "advanced"
        else:
            event = "not-due"
    elif verdict in FAILURES and due:
        e["ease"] = _sm2_ease(e["ease"], 1)
        e["repetition"] = 0
        e["interval"] = 1
        e["due"] = now + 1 * DAY
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


def upcoming(state=None):
    """All slugs with a future due date (not yet due today), soonest first."""
    state = state or load()
    out = []
    for slug, e in state["problems"].items():
        if e.get("solved") and e.get("due") and not _is_due(e["due"]):
            out.append((e["due"], slug))
    out.sort()
    return out


def status_of(state, slug):
    return state["problems"].get(slug)


def total_practice_seconds(state=None):
    """Estimate total practice time from history sessions.

    Groups attempts into sessions (gap < 2h) and sums session durations.
    Each session is at least 5 minutes (accounts for single-attempt solves).
    """
    state = state or load()
    total = 0
    session_gap = 2 * 3600
    min_session = 5 * 60

    for e in state["problems"].values():
        timestamps = sorted(h["t"] for h in e.get("history", []))
        if not timestamps:
            continue
        session_start = timestamps[0]
        prev = timestamps[0]
        for t in timestamps[1:]:
            if t - prev > session_gap:
                total += max(min_session, prev - session_start)
                session_start = t
            prev = t
        total += max(min_session, prev - session_start)
    return total


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
