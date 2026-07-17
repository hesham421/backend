"""
ERP Governance Tools — Marker Parser Engine
=============================================
Shared parsing engine used by Agent 3.
Reads HTML comment markers (Section 6.7 of P3) and builds
a structured tree representing the artifact's addressable elements.

This module does NOT modify any content — it only reads and indexes.
"""

import re
from pathlib import Path
from dataclasses import dataclass, field

import sys
sys.path.insert(0, str(Path(__file__).parent))
from config import MARKERS


# ─────────────────────────────────────────────────────────────────────────────
# DATA STRUCTURES
# ─────────────────────────────────────────────────────────────────────────────

@dataclass
class MarkerBlock:
    """Represents one START/END marker pair and its content."""
    kind: str            # phase | mark | sub | api | xm | tc
    marker_id: str        # e.g. CORE, JUNIT, SCR-ORG-001, API-ORG-001
    start_line: int        # 1-indexed line of START marker
    end_line: int           # 1-indexed line of END marker
    content: str             # raw text BETWEEN start and end (markers excluded)
    children: list = field(default_factory=list)   # nested MarkerBlocks
    parent: "MarkerBlock" = None


@dataclass
class ParseError:
    severity: str   # CRITICAL | WARNING
    message: str
    line: int = 0


@dataclass
class ParseResult:
    root_blocks: list           # top-level blocks (usually PHASE blocks)
    errors: list                # ParseError list
    raw_lines: list             # original file lines (for content extraction)
    total_lines: int = 0


# ─────────────────────────────────────────────────────────────────────────────
# TOKENIZER — find all marker occurrences in order
# ─────────────────────────────────────────────────────────────────────────────

def _tokenize(lines: list[str]) -> list[dict]:
    """
    Scan all lines and return a flat ordered list of marker tokens:
    {kind, marker_id, type: START|END, line}
    """
    tokens = []
    for idx, line in enumerate(lines, start=1):
        for kind, pattern in MARKERS.items():
            m = pattern.search(line)
            if m:
                marker_id, action = m.group(1), m.group(2)
                tokens.append({
                    "kind": kind,
                    "marker_id": marker_id,
                    "type": action,
                    "line": idx,
                })
    return tokens


# ─────────────────────────────────────────────────────────────────────────────
# STRUCTURE VALIDATOR — Rule 1 (every START has END), Rule 2 (no cross-nesting)
# ─────────────────────────────────────────────────────────────────────────────

# Allowed nesting hierarchy per Section 6.7.2
ALLOWED_PARENTS = {
    "phase": [None],                  # top level only
    "mark":  ["phase"],               # MARK only inside PHASE (test-plan)
    "sub":   ["phase", "mark"],       # SUB inside PHASE or MARK
    "api":   ["phase", "sub"],        # API inside PHASE or SUB
    "xm":    ["phase", "sub"],        # XM inside PHASE or SUB
    "tc":    ["mark", "sub"],         # TC inside MARK or SUB
}


# ─────────────────────────────────────────────────────────────────────────────
# TREE BUILDER — opens block at START, closes at matching END
# Validates: Rule 1 (every START has END), Rule 2 (no cross-nesting)
# ─────────────────────────────────────────────────────────────────────────────

def _build_tree(tokens: list[dict], lines: list[str]) -> tuple[list[MarkerBlock], list[ParseError]]:
    """
    Single-pass tree builder.
    Opens a block at START (attaches to current parent immediately),
    fills in content + end_line at matching END.
    """
    errors: list[ParseError] = []
    stack: list[MarkerBlock] = []   # currently open blocks
    roots: list[MarkerBlock] = []

    for tok in tokens:
        kind, marker_id, action, line = tok["kind"], tok["marker_id"], tok["type"], tok["line"]

        if action == "START":
            parent_kind = stack[-1].kind if stack else None
            allowed = ALLOWED_PARENTS.get(kind, [])
            if parent_kind not in allowed:
                errors.append(ParseError(
                    severity="CRITICAL",
                    message=(
                        f"Illegal nesting: <{kind.upper()}:{marker_id}:START> at line {line} "
                        f"found inside '{parent_kind or 'document root'}' — "
                        f"not permitted by Section 6.7.6 Rule 2."
                    ),
                    line=line,
                ))

            block = MarkerBlock(
                kind=kind, marker_id=marker_id,
                start_line=line, end_line=-1, content="",
            )
            if stack:
                stack[-1].children.append(block)
                block.parent = stack[-1]
            else:
                roots.append(block)
            stack.append(block)

        elif action == "END":
            if not stack:
                errors.append(ParseError(
                    severity="CRITICAL",
                    message=f"Unmatched END marker: <{kind.upper()}:{marker_id}:END> at line {line} — no open START.",
                    line=line,
                ))
                continue

            top = stack[-1]
            if top.kind != kind or top.marker_id != marker_id:
                errors.append(ParseError(
                    severity="CRITICAL",
                    message=(
                        f"Mismatched END at line {line}: expected END for "
                        f"<{top.kind.upper()}:{top.marker_id}> (opened line {top.start_line}) "
                        f"but found <{kind.upper()}:{marker_id}:END>."
                    ),
                    line=line,
                ))
                stack.pop()
                continue

            top.end_line = line
            top.content = "".join(lines[top.start_line: line - 1])
            stack.pop()

    # anything left open = missing END
    for unclosed in stack:
        errors.append(ParseError(
            severity="CRITICAL",
            message=(
                f"Unclosed marker: <{unclosed.kind.upper()}:{unclosed.marker_id}:START> "
                f"at line {unclosed.start_line} has no matching END."
            ),
            line=unclosed.start_line,
        ))

    return roots, errors


# ─────────────────────────────────────────────────────────────────────────────
# UNIQUENESS VALIDATOR — Rule 3 (every ID is unique)
# ─────────────────────────────────────────────────────────────────────────────

def _check_uniqueness(roots: list[MarkerBlock]) -> list[ParseError]:
    """Verify every atomic ID (api/xm/tc) appears exactly once."""
    errors = []
    seen = {}

    def walk(block: MarkerBlock):
        if block.kind in ("api", "xm", "tc"):
            key = (block.kind, block.marker_id)
            if key in seen:
                errors.append(ParseError(
                    severity="CRITICAL",
                    message=(
                        f"Duplicate ID: {block.kind.upper()}:{block.marker_id} "
                        f"appears at line {block.start_line} and was already "
                        f"defined at line {seen[key]}."
                    ),
                    line=block.start_line,
                ))
            else:
                seen[key] = block.start_line
        for c in block.children:
            walk(c)

    for r in roots:
        walk(r)

    return errors


# ─────────────────────────────────────────────────────────────────────────────
# PUBLIC API
# ─────────────────────────────────────────────────────────────────────────────

def parse_file(filepath: Path) -> ParseResult:
    """
    Parse a markdown artifact file and return its marker tree.
    Does not raise on structural errors — collects them in result.errors.
    """
    text = filepath.read_text(encoding="utf-8")
    lines = text.splitlines(keepends=True)

    tokens = _tokenize(lines)
    roots, errors = _build_tree(tokens, lines)
    errors += _check_uniqueness(roots)

    return ParseResult(
        root_blocks=roots,
        errors=errors,
        raw_lines=lines,
        total_lines=len(lines),
    )


def flatten(roots: list[MarkerBlock]) -> list[MarkerBlock]:
    """Return all blocks (at every depth) as a flat list."""
    result = []

    def walk(block):
        result.append(block)
        for c in block.children:
            walk(c)

    for r in roots:
        walk(r)
    return result


def find_by_kind(roots: list[MarkerBlock], kind: str) -> list[MarkerBlock]:
    """Find all blocks of a given kind (phase/mark/sub/api/xm/tc)."""
    return [b for b in flatten(roots) if b.kind == kind]


def print_tree(roots: list[MarkerBlock], indent: int = 0):
    """Debug helper: print the marker tree structure."""
    for b in roots:
        line_count = (b.end_line - b.start_line - 1) if b.end_line > 0 else 0
        print("  " * indent + f"{b.kind.upper()}:{b.marker_id}  "
              f"(lines {b.start_line}-{b.end_line}, {line_count} content lines)")
        print_tree(b.children, indent + 1)
