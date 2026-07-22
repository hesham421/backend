"""
ERP Governance Tools — Marker Parser Engine
=============================================
Shared parsing engine used by Agent 3.
Reads HTML comment markers (PROJECT-3-REGISTRY.md Section 5.7 — moved
here in v2.1 from the old "P3 Section 6.7"; content unchanged, only
the file location) and builds a structured tree representing the
artifact's addressable elements.

This module does NOT modify any content — it only reads and indexes.

v2.1 UPDATE — DUAL GOVERNANCE MODEL SUPPORT
=============================================
parse_file() now accepts a `governance_model` parameter ("v1" or "v2",
default "v1" for backward compatibility with every existing call site).

  v1 hierarchy (unchanged): PHASE → [MARK] → [SUB] → ATOM
    MARK is required for TC blocks inside a combined test-plan.md
    (MARK:JUNIT or MARK:PLAYWRIGHT).

  v2 hierarchy (new): PHASE → [SUB] → ATOM — no MARK level at all.
    backend-test-plan.md and frontend-test-plan.md are separate files
    by construction, so the tool-boundary MARK tag that used to
    distinguish JUnit vs Playwright sections inside ONE file is
    redundant (see PROJECT-3-REGISTRY.md Section 5.7.4 "v2.0
    SIMPLIFICATION" note). A MARK token found in a v2-model file is a
    CRITICAL parse error, not silently ignored — it usually means a
    v1-style file was mislabeled as v2, which callers must know about.
"""

import re
from pathlib import Path
from dataclasses import dataclass, field

import sys
sys.path.insert(0, str(Path(__file__).parent))
from config import MARKERS, GOVERNANCE_MODEL_V1, GOVERNANCE_MODEL_V2


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

# Allowed nesting hierarchy per PROJECT-3-REGISTRY.md Section 5.7.2/5.7.6
# v1 (legacy — unchanged from the original "Section 6.7.2" rule)
ALLOWED_PARENTS_V1 = {
    "phase": [None],                  # top level only
    "mark":  ["phase"],               # MARK only inside PHASE (test-plan)
    "sub":   ["phase", "mark"],       # SUB inside PHASE or MARK
    "api":   ["phase", "sub"],        # API inside PHASE or SUB
    "xm":    ["phase", "sub"],        # XM inside PHASE or SUB
    "tc":    ["mark", "sub"],         # TC inside MARK or SUB
}

# v2 (new — no MARK level; TC nests directly under PHASE or SUB, same
# as API/XM already did). A "mark" token appearing at all in a v2
# file is illegal — see ALLOWED_PARENTS_V2["mark"] = [] below, which
# makes _build_tree() reject it with a CRITICAL error no matter what
# it's nested inside.
ALLOWED_PARENTS_V2 = {
    "phase": [None],                  # top level only
    "mark":  [],                      # MARK is not used in v2 — any
                                       # occurrence is illegal at any nesting level
    "sub":   ["phase"],               # SUB inside PHASE only (no MARK level)
    "api":   ["phase", "sub"],        # API inside PHASE or SUB (unchanged)
    "xm":    ["phase", "sub"],        # XM inside PHASE or SUB (unchanged)
    "tc":    ["phase", "sub"],        # TC inside PHASE or SUB directly (v2 change)
}

# Backward-compatible alias — old call sites that import ALLOWED_PARENTS
# directly keep working, always resolving to v1. New code should use
# get_allowed_parents(governance_model) instead.
ALLOWED_PARENTS = ALLOWED_PARENTS_V1


def get_allowed_parents(governance_model: str) -> dict:
    """Model-aware replacement for the bare ALLOWED_PARENTS constant."""
    return ALLOWED_PARENTS_V2 if governance_model == GOVERNANCE_MODEL_V2 else ALLOWED_PARENTS_V1


# ─────────────────────────────────────────────────────────────────────────────
# TREE BUILDER — opens block at START, closes at matching END
# Validates: Rule 1 (every START has END), Rule 2 (no cross-nesting)
# ─────────────────────────────────────────────────────────────────────────────

def _build_tree(tokens: list[dict], lines: list[str],
                 governance_model: str = GOVERNANCE_MODEL_V1) -> tuple[list[MarkerBlock], list[ParseError]]:
    """
    Single-pass tree builder.
    Opens a block at START (attaches to current parent immediately),
    fills in content + end_line at matching END.

    governance_model: "v1" (default, legacy hierarchy with MARK) or
      "v2" (new hierarchy, no MARK level — see ALLOWED_PARENTS_V2).
    """
    errors: list[ParseError] = []
    stack: list[MarkerBlock] = []   # currently open blocks
    roots: list[MarkerBlock] = []
    allowed_parents = get_allowed_parents(governance_model)

    for tok in tokens:
        kind, marker_id, action, line = tok["kind"], tok["marker_id"], tok["type"], tok["line"]

        if action == "START":
            parent_kind = stack[-1].kind if stack else None
            allowed = allowed_parents.get(kind, [])
            if kind == "mark" and governance_model == GOVERNANCE_MODEL_V2:
                errors.append(ParseError(
                    severity="CRITICAL",
                    message=(
                        f"MARK token found in a v2-model file at line {line}: "
                        f"<MARK:{marker_id}:START>. The v2 governance model has no "
                        f"MARK level (PROJECT-3-REGISTRY.md Section 5.7.4 "
                        f"'v2.0 SIMPLIFICATION') — this file may be mislabeled "
                        f"(check governance_model in modules-registry.json), or it "
                        f"is genuinely a v1-style file being parsed with the wrong model."
                    ),
                    line=line,
                ))
            elif parent_kind not in allowed:
                errors.append(ParseError(
                    severity="CRITICAL",
                    message=(
                        f"Illegal nesting: <{kind.upper()}:{marker_id}:START> at line {line} "
                        f"found inside '{parent_kind or 'document root'}' — "
                        f"not permitted by PROJECT-3-REGISTRY.md Section 5.7.6 Rule 2 "
                        f"(governance_model={governance_model})."
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

def parse_file(filepath: Path, governance_model: str = GOVERNANCE_MODEL_V1) -> ParseResult:
    """
    Parse a markdown artifact file and return its marker tree.
    Does not raise on structural errors — collects them in result.errors.

    governance_model: "v1" (default — every existing call site keeps
      working unchanged) or "v2". Callers that know which model a
      module uses (via config.get_governance_model(mod)) should pass
      it explicitly; the default only exists for backward compatibility,
      not because v1 is a sensible default going forward.
    """
    text = filepath.read_text(encoding="utf-8")
    lines = text.splitlines(keepends=True)

    tokens = _tokenize(lines)
    roots, errors = _build_tree(tokens, lines, governance_model=governance_model)
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
