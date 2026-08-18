"""Shared parser for the mcq-quiz markdown files.

Used by mcq_shuffle.py (generate randomised practice sheets) and mcq_track.py
(progress tracking + interactive drill). Standard library only.

The expected file shape, per question:

    ### Q3. Stem text, possibly wrapped
    over several lines

    - **A.** option text (always one line)
    - **B.** option text
    - **C.** option text
    - **D.** option text

    <details><summary>Answer</summary>

    **B.** explanation, which may reference the other options by letter

    → `quiz/01` Q3
    </details>

Letter references inside the explanation are remapped when options are shuffled,
which is the fiddly part -- see remap_letters().
"""

from __future__ import annotations

import re
from dataclasses import dataclass, field
from pathlib import Path

LETTERS = ("A", "B", "C", "D")

_Q_SPLIT = re.compile(r"(?m)^(?=### Q)")
_Q_HEAD = re.compile(r"(?m)^### (Q(\d+))\.\s*(.*)$")
_OPTION = re.compile(r"(?m)^- \*\*([ABCD])\.\*\* (.*)$")
_ANSWER = re.compile(r"(?m)^\*\*([ABCD])\.\*\*")
_FOOTER = re.compile(r"(?m)^(?=## )")
_STABLE_ID = re.compile(r"(?m)^<!-- mcq-id: (\S+) -->$")

@dataclass
class Question:
    """One MCQ: stem, four options, and the answer block."""

    number: int
    stem: str
    options: dict[str, str]
    answer: str
    body: str
    stable_id: str = ""
    source: str = ""

    @property
    def qid(self) -> str:
        """Stable identity that survives shuffling, e.g. '01-threading:Q7'."""
        return self.stable_id or f"{self.source}:Q{self.number}"

    @property
    def correct_text(self) -> str:
        return self.options[self.answer]

    def render(self, number: int | None = None, with_answer: bool = True) -> str:
        """Render back to markdown. `number` renumbers the heading if given."""
        n = self.number if number is None else number
        out = [f"### Q{n}. {self.stem}", ""]
        out.append(f"<!-- mcq-id: {self.qid} -->")
        out.append("")
        out += [f"- **{L}.** {self.options[L]}" for L in LETTERS]
        out.append("")
        if with_answer:
            out.append(self.body.rstrip())
        return "\n".join(out) + "\n"


@dataclass
class Deck:
    """All questions from one mcq-quiz file, plus its header and footer prose."""

    path: Path
    title: str
    header: str
    footer: str
    questions: list[Question] = field(default_factory=list)

    @property
    def slug(self) -> str:
        return self.path.stem


def remap_letters(text: str, mapping: dict[str, str]) -> str:
    """Rewrite option-letter references in `text` according to `mapping`.

    IMPORTANT: only **bolded** forms are rewritten -- ``**A.**`` and ``**A**``.

    Bare capitals are deliberately left alone. An earlier version of this function
    also rewrote them, which corrupted prose: the English article in "A `Trade`
    aggregate" and "A handful of instructions" was silently turned into "C" / "B",
    and technical strings like ``W/C`` and "B-tree" were mangled. A bare capital
    A-D is genuinely ambiguous between an option reference and ordinary English, so
    the tool refuses to guess.

    The consequence is a convention the source files must follow: **every option
    reference inside an explanation must be bolded.** `validate_deck()` enforces it,
    and `mcq_shuffle.py --check` reports violations.

    The substitution happens in ONE pass so a 4-way permutation can't cascade
    (A->B followed by B->C would otherwise corrupt it).
    """
    if all(k == v for k, v in mapping.items()):
        return text

    pattern = re.compile(r"\*\*([ABCD])\.\*\*|\*\*([ABCD])\*\*")

    def _sub(m: re.Match[str]) -> str:
        if m.group(1):
            return f"**{mapping[m.group(1)]}.**"
        return f"**{mapping[m.group(2)]}**"

    return pattern.sub(_sub, text)


# Bare capitals followed by one of these words are almost certainly option
# references that someone forgot to bold -- see remap_letters().
_UNBOLDED_REF = re.compile(
    r"(?<![A-Za-z0-9`*\-])([ABCD]) "
    r"(?:and|or|is|are|was|were|describes|happens|would|treats|has|have|gives"
    r"|remain|remains|leak|leaks|both|all)\b"
)


def validate_deck(deck: Deck) -> list[str]:
    """Return a list of problems found in `deck`. Empty means clean.

    The important check is for unbolded option references in explanations, which
    would silently stop being remapped when the choices are shuffled -- leaving an
    explanation that dismisses the wrong options.
    """
    problems: list[str] = []
    for q in deck.questions:
        if set(q.options) != set(LETTERS):
            problems.append(f"{q.qid}: expected options A-D, got {sorted(q.options)}")
        if q.answer not in q.options:
            problems.append(f"{q.qid}: answer {q.answer} is not one of the options")
        for m in _UNBOLDED_REF.finditer(q.body):
            problems.append(
                f"{q.qid}: unbolded option reference {m.group(0)!r} — bold it "
                f"(**{m.group(1)}**) or it won't survive a shuffle"
            )
        if not q.stem.strip():
            problems.append(f"{q.qid}: empty stem")
    return problems



def permute(question: Question, mapping: dict[str, str]) -> Question:
    """Return a copy of `question` with its options relabelled per `mapping`."""
    inverse = {new: old for old, new in mapping.items()}
    return Question(
        number=question.number,
        stem=question.stem,
        options={L: question.options[inverse[L]] for L in LETTERS},
        answer=mapping[question.answer],
        body=remap_letters(question.body, mapping),
        stable_id=question.qid,
        source=question.source,
    )


def parse_deck(path: Path) -> Deck:
    """Parse one mcq-quiz markdown file. Raises ValueError on a malformed file."""
    raw = path.read_text(encoding="utf-8")
    chunks = _Q_SPLIT.split(raw)
    header = chunks[0]
    title = next(
        (l[2:].strip() for l in header.splitlines() if l.startswith("# ")), path.stem
    )

    footer = ""
    blocks = chunks[1:]
    if blocks:
        # The trailing prose ("## Score yourself") rides along on the last block.
        split = _FOOTER.split(blocks[-1], maxsplit=1)
        if len(split) == 2:
            blocks[-1], footer = split[0], "## " + split[1][3:] if False else split[1]

    questions: list[Question] = []
    for blk in blocks:
        head = _Q_HEAD.search(blk)
        if not head:
            continue
        options = dict(_OPTION.findall(blk))
        if set(options) != set(LETTERS):
            raise ValueError(f"{path.name} Q{head.group(2)}: expected 4 options, got {sorted(options)}")

        det = blk.find("<details>")
        if det == -1:
            raise ValueError(f"{path.name} Q{head.group(2)}: no <details> answer block")
        body = blk[det:].rstrip()
        ans = _ANSWER.search(body)
        if not ans:
            raise ValueError(f"{path.name} Q{head.group(2)}: no '**X.**' answer letter")

        # Stem = the heading text plus any wrapped continuation before the options.
        pre = blk[: _OPTION.search(blk).start()]
        stem_lines = pre.splitlines()[1:]
        stem = " ".join(
            l.strip() for l in stem_lines
            if l.strip() and not l.strip().startswith("<!--")
        )
        stem = (head.group(3) + (" " + stem if stem else "")).strip()

        sid = _STABLE_ID.search(blk)
        questions.append(
            Question(
                number=int(head.group(2)),
                stem=stem,
                options=options,
                answer=ans.group(1),
                body=body,
                stable_id=sid.group(1) if sid else "",
                source=path.stem,
            )
        )

    return Deck(path=path, title=title, header=header, footer=footer, questions=questions)


def load_decks(root: Path) -> list[Deck]:
    """Parse every question file under `root`, skipping README.md."""
    decks = []
    for p in sorted(root.glob("*.md")):
        if p.name.lower() == "readme.md":
            continue
        deck = parse_deck(p)
        if deck.questions:
            decks.append(deck)
    return decks


def default_root(start: Path | None = None) -> Path:
    """Locate mcq-quiz/ relative to this file or the cwd."""
    here = (start or Path(__file__).resolve().parent)
    for cand in (here / "mcq-quiz", here.parent / "mcq-quiz", Path.cwd() / "mcq-quiz"):
        if cand.is_dir():
            return cand
    raise SystemExit("could not find mcq-quiz/ -- pass --root explicitly")
