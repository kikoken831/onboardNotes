"""
Problem catalogue: the supplied 179-problem list plus the ramp tiers.

Everything is keyed on SLUG, never on id. A number of ids in the supplied list have
drifted from real LeetCode (792 is Binary Search, which is really 704; 1250 is Longest
Common Subsequence, really 1143; 745/746 are swapped). The slugs are all correct, so
the slug is the key and the supplied id is carried only as a display label.
"""

import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

TIER_NAMES = {
    0: "java-mechanics",
    1: "primitives",
    2: "easy",
    3: "set",
}

# Supplied id -> real LeetCode id, for problems whose id drifted.
ID_CORRECTIONS = {
    792: 704, 1250: 1143, 1019: 977, 940: 904, 908: 876, 931: 895,
    1014: 973, 855: 828, 882: 852, 778: 767, 874: 844, 893: 863,
    800: 784, 1028: 986, 1075: 1065, 2132: 2022, 761: 759,
    745: 744, 746: 745,
}


def catalogue_path():
    return os.path.join(ROOT, "problems.json")


def load():
    with open(catalogue_path()) as f:
        return json.load(f)


def all_problems():
    """Every problem across every tier, in catalogue order."""
    return load()["problems"]


def by_slug(slug):
    for p in all_problems():
        if p["slug"] == slug:
            return p
    return None


def resolve(query):
    """
    Accept a slug, a substring of a slug, or an id. Returns (problem, error_message).
    """
    problems = all_problems()

    for p in problems:
        if p["slug"] == query:
            return p, None

    if query.isdigit():
        n = int(query)
        hits = [p for p in problems if p.get("id") == n or p.get("lc_id") == n]
        if len(hits) == 1:
            return hits[0], None
        if len(hits) > 1:
            names = ", ".join(h["slug"] for h in hits)
            return None, "id %s is ambiguous: %s" % (query, names)

    hits = [p for p in problems if query in p["slug"]]
    if len(hits) == 1:
        return hits[0], None
    if len(hits) > 1:
        preview = ", ".join(h["slug"] for h in hits[:8])
        more = " (+%d more)" % (len(hits) - 8) if len(hits) > 8 else ""
        return None, "'%s' matches %d problems: %s%s" % (query, len(hits), preview, more)

    return None, "no problem matches '%s'" % query


def companies_of(problem):
    return {c["slug"]: c["frequency"] for c in problem.get("companies", [])}


def bank_weight(problem):
    """
    Frequency across the banks and trading firms that matter for this search.
    Used to order `judge next` within the supplied set.
    """
    finance = {
        "goldman-sachs", "jpmorgan", "morgan-stanley", "blackrock", "bloomberg",
        "citadel", "de-shaw", "jane-street", "virtu", "capital-one", "visa",
        "mastercard", "paypal", "sofi",
    }
    return sum(f for s, f in companies_of(problem).items() if s in finance)


def paths(problem):
    """Filesystem locations for one problem."""
    slug = problem["slug"]
    tier = problem["tier"]
    statement_dir = "drills" if tier in (0, 1) else "statements"
    return {
        "statement": os.path.join(ROOT, statement_dir, slug, "statement.md"),
        "ref": os.path.join(ROOT, ".ref", slug, "Ref.java"),
        "ref_dir": os.path.join(ROOT, ".ref", slug),
        "solution": os.path.join(ROOT, "solutions", slug, "Solution.java"),
        "solution_dir": os.path.join(ROOT, "solutions", slug),
        "attempts": os.path.join(ROOT, "solutions", slug, ".attempts"),
        "build_ref": os.path.join(ROOT, "build", "ref", slug),
        "build_sol": os.path.join(ROOT, "build", "sol", slug),
    }


def is_implemented(problem):
    """True when a reference exists, i.e. the problem is actually runnable."""
    return os.path.exists(paths(problem)["ref"])
