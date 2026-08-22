"""
ERP Governance Tools — Marker Parser Engine
=============================================
Shared parsing engine used by Agent 3.
Reads HTML comment markers (PROJECT-3-REGISTRY.md Section 5.7) and
builds a structured tree representing the artifact's addressable
elements.

This module does NOT modify any content — it only reads and indexes.

Hierarchy: PHASE → [SUB] → ATOM (API/XM/TC) — the same shape for every
artifact type (backend-execution-plan.md, frontend-execution-plan.md,
backend-test-plan.md, frontend-test-plan.md). There is no MARK level —
each test-plan file is single-tool by construction (backend-test-plan.md
is JUnit-only, frontend-test-plan.md is Playwright-only), so the file
itself is the tool boundary; TC blocks nest directly under PHASE or SUB.
"""

import re
from pathlib import Path
from dataclasses import dataclass, field

import sys
sys.path.insert(0, str(Path(__file__).parent))
from config import MARKERS


@dataclass
class MarkerBlock:
    kind: str            # "phase" | "sub" | "api" | "xm" | "tc"
    marker_id: str
    start_line: int       # 1-indexed line number of the START marker
    end_line: int = 0     # 1-indexed line number of the END marker
    content: str = ""     # raw text between START and END (exclusive of marker lines)
    children: list = field(default_factory=list)
    parent: "MarkerBlock" = None


@dataclass
class ParseError:
    severity: str          # "CRITICAL" | "MAJOR" | "MINOR"
    message: str
    line: int = 0


@dataclass
class ParseResult:
    root_blocks: list[MarkerBlock]
    errors: list[ParseError]
    raw_lines: list[str]
    total_lines: int


# Allowed nesting hierarchy per PROJECT-3-REGISTRY.md Section 5.7.2/5.7.6
ALLOWED_PARENTS = {
    "phase": [None],                  # top level only
    "sub":   ["phase"],               # SUB inside PHASE only
    "api":   ["phase", "sub"],        # API inside PHASE or SUB
    "xm":    ["phase", "sub"],        # XM inside PHASE or SUB
    "tc":    ["phase", "sub"],        # TC inside PHASE or SUB directly
}


def _tokenize(lines: list[str]) -> list[dict]:
    """Scan every line for marker patterns, return ordered token list."""
    tokens = []
    for i, line in enumerate(lines, start=1):
        for kind, pattern in MARKERS.items():
            m = pattern.search(line)
            if m:
                marker_id, action = m.group(1), m.group(2)
                tokens.append({"kind": kind, "marker_id": marker_id, "type": action, "line": i})
    return tokens


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
                        f"not permitted by PROJECT-3-REGISTRY.md Section 5.7.6 Rule 2."
                    ),
                    line=line,
                ))

            block = MarkerBlock(kind=kind, marker_id=marker_id, start_line=line)
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
                    message=f"Unmatched END marker: <{kind.upper()}:{marker_id}:END> at line {line} "
                            f"— no corresponding START marker is open.",
                    line=line,
                ))
                continue

            open_block = stack[-1]
            if open_block.kind != kind or open_block.marker_id != marker_id:
                errors.append(ParseError(
                    severity="CRITICAL",
                    message=(
                        f"Mismatched END marker at line {line}: expected "
                        f"</{open_block.kind.upper()}:{open_block.marker_id}> but found "
                        f"</{kind.upper()}:{marker_id}>."
                    ),
                    line=line,
                ))
                continue

            open_block.end_line = line
            content_lines = lines[open_block.start_line: line - 1]
            open_block.content = "".join(content_lines)
            stack.pop()

    for unclosed in stack:
        errors.append(ParseError(
            severity="CRITICAL",
            message=f"Unclosed marker: <{unclosed.kind.upper()}:{unclosed.marker_id}:START> "
                    f"at line {unclosed.start_line} — no matching END marker found.",
            line=unclosed.start_line,
        ))

    return roots, errors


def _check_uniqueness(roots: list[MarkerBlock]) -> list[ParseError]:
    """Every marker_id within the same kind must be unique across the whole document."""
    errors = []
    seen: dict[str, list[MarkerBlock]] = {}

    def _walk(block: MarkerBlock):
        key = f"{block.kind}:{block.marker_id}"
        seen.setdefault(key, []).append(block)
        for child in block.children:
            _walk(child)

    for root in roots:
        _walk(root)

    for key, blocks in seen.items():
        if len(blocks) > 1:
            kind, marker_id = key.split(":", 1)
            lines = ", ".join(str(b.start_line) for b in blocks)
            errors.append(ParseError(
                severity="CRITICAL",
                message=f"Duplicate {kind.upper()}:{marker_id} — appears {len(blocks)} times "
                        f"(lines {lines}). Every marker_id must be unique within its kind.",
                line=blocks[0].start_line,
            ))

    return errors


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


def flatten(blocks: list[MarkerBlock]) -> list[MarkerBlock]:
    """Return every block in the tree (depth-first), including nested children."""
    result = []
    for b in blocks:
        result.append(b)
        result.extend(flatten(b.children))
    return result


def find_by_kind(blocks: list[MarkerBlock], kind: str) -> list[MarkerBlock]:
    """Return every block of a given kind, anywhere in the tree."""
    return [b for b in flatten(blocks) if b.kind == kind]
