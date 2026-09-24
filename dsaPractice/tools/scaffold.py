#!/usr/bin/env python3
"""
Scaffolds a drill or problem: statement.md plus a Ref.java skeleton.

    python3 tools/scaffold.py p-remove-value

Only creates what is missing; it never overwrites an existing file.
"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from judge import catalogue  # noqa: E402

STATEMENT = """# {title}

{lc_line}

## Brief

TODO one-paragraph restatement.

## Constraints

- TODO

## Examples

```
TODO
```

## Signature

```java
public TODO {method}(TODO)
```

## Target complexity

TODO

## Hints

<details><summary>Hint 1</summary>

TODO

</details>
"""

REF = """class Ref implements Problem {{

    public Spec spec() {{
        return Spec.method("{method}", T.TODO).returns(T.TODO)
            .compare(Cmp.EXACT)
            .example("TODO", "TODO")
            .gen(g -> new Object[]{{ /* TODO */ }})
            .patterns({patterns})
            .limitMs(2000);
    }}

    public TODO {method}(TODO) {{
        // TODO reference implementation
    }}
}}
"""


def main(slug):
    problem, err = catalogue.resolve(slug)
    if err:
        raise SystemExit(err)
    p = catalogue.paths(problem)
    method = _camel(problem["slug"])

    lc = ""
    if problem["tier"] >= 2:
        lc = "LeetCode %s · %s · https://leetcode.com/problems/%s/" % (
            problem.get("lc_id"), problem.get("difficulty"), problem["slug"])
    else:
        lc = "Tier %d drill · %s · unlocks %s" % (
            problem["tier"], problem.get("group", ""), problem.get("unlocks", ""))

    for path, body in (
        (p["statement"], STATEMENT.format(title=problem["title"], lc_line=lc, method=method)),
        (p["ref"], REF.format(
            method=method,
            patterns=", ".join('"%s"' % x for x in problem.get("pattern", [])))),
    ):
        if os.path.exists(path):
            print("exists  %s" % path)
            continue
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w") as f:
            f.write(body)
        print("created %s" % path)


def _camel(slug):
    parts = [x for x in slug.replace("p-", "", 1).split("-") if x]
    return parts[0] + "".join(w.capitalize() for w in parts[1:])


if __name__ == "__main__":
    if len(sys.argv) < 2:
        raise SystemExit("usage: scaffold.py <slug>")
    for s in sys.argv[1:]:
        main(s)
