#!/usr/bin/env python3
"""Builds problems.json from the tables in catalogue_data.py and catalogue_tier3.py."""

import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from catalogue_data import ID_CORRECTIONS, TIER1, TIER2  # noqa: E402
from catalogue_tier3 import TIER3  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def build():
    problems = []
    order = 0

    for slug, title, group, patterns, unlocks in TIER1:
        problems.append({
            "slug": slug, "title": title, "tier": 1, "group": group,
            "pattern": patterns, "difficulty": "Drill", "premium": False,
            "unlocks": unlocks, "order": order, "companies": [],
        })
        order += 1

    for lc_id, slug, title, patterns in TIER2:
        problems.append({
            "slug": slug, "title": title, "tier": 2, "lc_id": lc_id, "id": lc_id,
            "pattern": patterns, "difficulty": "Easy", "premium": False,
            "order": order, "companies": [],
        })
        order += 1

    for pid, slug, title, difficulty, premium, patterns, companies in TIER3:
        problems.append({
            "slug": slug, "title": title, "tier": 3,
            "id": pid, "lc_id": ID_CORRECTIONS.get(pid, pid),
            "pattern": patterns, "difficulty": difficulty,
            "premium": bool(premium), "order": order,
            "companies": [{"slug": s, "frequency": f}
                          for s, f in sorted(companies.items(), key=lambda kv: -kv[1])],
        })
        order += 1

    seen = {}
    for p in problems:
        if p["slug"] in seen:
            raise SystemExit("duplicate slug: %s" % p["slug"])
        seen[p["slug"]] = True

    return {
        "version": 1,
        "source": "supplied 179-problem list, plus authored ramp tiers 1 and 2",
        "id_corrections": {str(k): v for k, v in sorted(ID_CORRECTIONS.items())},
        "counts": {
            "tier1": len(TIER1), "tier2": len(TIER2), "tier3": len(TIER3),
            "total": len(problems),
        },
        "problems": problems,
    }


if __name__ == "__main__":
    data = build()
    out = os.path.join(ROOT, "problems.json")
    with open(out, "w") as f:
        json.dump(data, f, indent=1)
        f.write("\n")
    c = data["counts"]
    print("wrote %s" % out)
    print("  tier 1 (primitives) %3d" % c["tier1"])
    print("  tier 2 (LC easy)    %3d" % c["tier2"])
    print("  tier 3 (your set)   %3d" % c["tier3"])
    print("  total               %3d" % c["total"])
