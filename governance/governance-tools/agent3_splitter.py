"""
ERP Governance Tools — Agent 3: Artifact Splitter
====================================================
Reads Marker Protocol (PROJECT-3-REGISTRY.md Section 5.7) from
backend-execution-plan.md / frontend-execution-plan.md and
backend-test-plan.md / frontend-test-plan.md, then splits them into
addressable package files.

Staged execution — each stage requires explicit approval before proceeding.
Stages are independently resumable: if Stage 3 fails, Stage 1/2 results
are preserved and Stage 3 alone can be re-run.

Usage:
    python agent3_splitter.py --module FIN --track backend
    python agent3_splitter.py --module FIN --track backend --stage 1
    python agent3_splitter.py --module FIN --track backend --resume
    python agent3_splitter.py --module FIN --track backend --status

    (run again later, from the frontend repo's own copy, after real
    implementation + API docs + GATE: BACKEND MODULE COMPLETE +
    GATE: UI SHELL COMPLETE)
    python agent3_splitter.py --module FIN --track frontend

--track is always required — it picks WHICH artifact pair Stage 2/3
split:
  backend  : backend-execution-plan.md (P3_1/) + backend-test-plan.md
             (P3_5_BE/) — output stays entirely in this repo
             (packages/backend-execution/, packages/backend-test/)
  frontend : frontend-execution-plan.md (P3_2/) + frontend-test-plan.md
             (P3_5_FE/) — these source files are natively frontend-
             generated (they live in the frontend repo's own P3_2/
             P3_5_FE folders) — this track is normally invoked with
             THIS SCRIPT'S COPY living in the frontend repo, not this one.

This is a separate flag from --stage (which means 1-5, the five
internal pipeline steps). --track picks WHICH artifact pair gets
split; --stage picks WHICH step of that split to run. The two compose:
--track backend --stage 2 means "run just the split-execution step,
for the backend track."

Stages:
    1. Parse & Plan          — read markers, validate structure, show plan
    2. Split execution-plan  — write PHASE/SUB/API/XM package files
    3. Split test-plan       — write PHASE/SUB/TC package files (no
                                MARK level — each test-plan file is
                                single-tool by construction)
    4. Generate Index Files  — index.md per package folder
    5. Verify Completeness   — content-hash cross-check against the
                                archived source artifact
"""

import argparse
import json
import sys
sys.stdout.reconfigure(encoding="utf-8", errors="replace")
from pathlib import Path
from datetime import datetime

sys.path.insert(0, str(Path(__file__).parent))
from config import (
    REPO_BASE_PATH,
    FRONTEND_OUTPUT_BASE_PATH,
    get_module_version_path,
    get_frontend_module_version_path,
    validate_module,
    load_modules_registry,
)
from marker_parser import (
    parse_file, flatten, find_by_kind, MarkerBlock, ParseResult,
)

STAGE_NAMES = {
    1: "Parse & Plan",
    2: "Split execution-plan",
    3: "Split test-plan",
    4: "Generate Index Files",
    5: "Verify Completeness",
}

# ─────────────────────────────────────────────────────────────────────────────
# STAGE STATE — tracks progress, allows resume
# ─────────────────────────────────────────────────────────────────────────────

def _state_path(mod: str, version: int, base: Path = None) -> Path:
    if base is None:
        base = get_module_version_path(mod, version)
    return base / "packages" / "_agent3-state.json"


def load_state(mod: str, version: int, base: Path = None) -> dict:
    p = _state_path(mod, version, base)
    if p.exists():
        with open(p, "r", encoding="utf-8") as fh:
            return json.load(fh)
    return {"stages_completed": [], "stages": {}}


def save_state(mod: str, version: int, state: dict, base: Path = None):
    p = _state_path(mod, version, base)
    p.parent.mkdir(parents=True, exist_ok=True)
    with open(p, "w", encoding="utf-8") as fh:
        json.dump(state, fh, indent=2, ensure_ascii=False)


def mark_stage_complete(state: dict, stage: int):
    if stage not in state["stages_completed"]:
        state["stages_completed"].append(stage)
    state["stages"][str(stage)] = {"completed_at": datetime.now().isoformat()}


def print_status(mod: str, version: int, base: Path = None, track: str = None):
    state = load_state(mod, version, base)
    print()
    print("═" * 62)
    print(f"  AGENT 3 — Status")
    print(f"  Module  : {mod}  (v{version})" + (f"  Track: {track}" if track else ""))
    print("═" * 62)
    for stage_num, name in STAGE_NAMES.items():
        done = stage_num in state.get("stages_completed", [])
        status = "✓ DONE" if done else "— pending"
        print(f"  Stage {stage_num} — {name:<25} {status}")
    if state.get("stages", {}).get("5"):
        print()
        print(f"  Last run: {state['stages']['5']['completed_at']}")
    print()


def confirm(prompt: str = "  Proceed?") -> bool:
    answer = input(f"{prompt} [y/N]: ").strip().lower()
    return answer == "y"


# ─────────────────────────────────────────────────────────────────────────────
# FILE WRITER HELPERS
# ─────────────────────────────────────────────────────────────────────────────

def _write_block(path: Path, block: "MarkerBlock", header: str = ""):
    """Write a single MarkerBlock's content to a file — copy/paste only."""
    _guard_frontend_content_path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    text = (header + "\n\n") if header else ""
    text += block.content
    path.write_text(text, encoding="utf-8")


def _write_content(path: Path, content: str, header: str = ""):
    """Write raw text content to a file (used for preamble/header files)."""
    _guard_frontend_content_path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    text = (header + "\n\n") if header else ""
    text += content
    path.write_text(text, encoding="utf-8")


def _execute_write_plan(write_plan: list[dict]):
    """Execute all write operations — handles both block-based and content-based entries."""
    for w in write_plan:
        if "block" in w:
            _write_block(w["dest"], w["block"], w.get("header", ""))
        else:
            _write_content(w["dest"], w["content"], w.get("header", ""))


def _safe_filename(marker_id: str) -> str:
    return marker_id.strip().replace(" ", "-") + ".md"


def _guard_frontend_content_path(path: Path):
    """
    Refuse to write frontend-track content (frontend-execution/
    frontend-test package output) anywhere inside backend/governance/
    (REPO_BASE_PATH) — forbidden per the STRUCTURAL LAW section in
    backend/CLAUDE.md and frontend/CLAUDE.md. This is a permanent
    guardrail: it fires regardless of whether FRONTEND_OUTPUT_BASE_PATH
    or a --frontend-output override is ever misconfigured back into
    backend/governance/, so this specific bug cannot silently recur.
    """
    resolved = path.resolve()
    forbidden_root = REPO_BASE_PATH.resolve()
    is_frontend_content = "frontend-execution" in str(path) or "frontend-test" in str(path)
    if is_frontend_content and (resolved == forbidden_root or forbidden_root in resolved.parents):
        raise RuntimeError(
            f"REFUSING TO WRITE FRONTEND CONTENT — resolved path\n"
            f"    {resolved}\n"
            f"  is inside backend/governance/ ({forbidden_root}).\n"
            f"  Frontend content must never live in backend/governance/ — see the\n"
            f"  STRUCTURAL LAW section in backend/CLAUDE.md and frontend/CLAUDE.md.\n"
            f"  Check config.py's FRONTEND_OUTPUT_BASE_PATH."
        )


def _display_dest(dest: Path, base: Path, frontend_base: Path) -> str:
    """
    Show a write-plan destination relative to whichever root it actually
    belongs under, tagging frontend-rooted paths explicitly so a printed
    plan never silently implies everything lands under one tree.
    """
    try:
        return str(dest.relative_to(base))
    except ValueError:
        pass
    try:
        return f"[frontend/governance] {dest.relative_to(frontend_base)}"
    except ValueError:
        return str(dest)


def _preamble_content(block: "MarkerBlock", raw_lines: list[str]) -> str:
    """
    Extract content that sits between a container's START marker and its
    first child SUB/MARK — the 'preamble' that belongs to the container
    but is outside any SUB. Returns empty string if no preamble exists.

    block      : the PHASE or MARK MarkerBlock whose preamble we want
    raw_lines  : the full raw lines list from ParseResult (1-indexed usage)
    """
    children_with_sub = [c for c in block.children if c.kind in ("sub", "mark")]
    if not children_with_sub:
        return ""   # no SUBs — whole content is handled as one unit

    first_child_start = children_with_sub[0].start_line  # 1-indexed
    # content is raw_lines[block.start_line .. first_child_start - 2]
    # block.start_line is 1-indexed line OF the START marker itself
    # content starts at block.start_line (0-indexed = block.start_line)
    preamble_lines = raw_lines[block.start_line: first_child_start - 1]
    preamble = "".join(preamble_lines).strip()
    return preamble


# ─────────────────────────────────────────────────────────────────────────────
# STAGE 2 — Split execution-plan.md
# ─────────────────────────────────────────────────────────────────────────────

# Maps PHASE marker_id → packages/execution/<folder>
# v1 — legacy, unchanged. Combined backend+frontend phase keys.

# ─────────────────────────────────────────────────────────────────────────────
# PHASE FOLDER MAPS
# ─────────────────────────────────────────────────────────────────────────────

PHASE_FOLDER_MAP_BACKEND = {
    "CORE":      "CORE",
    "DATA-DOM":  "DATA-DOM",
    "SVC-API":   "SVC-API",
    "DOC":       "DOC",
    "INT-C":     "INT-C",
    "INT-R":     "INT-R",
    "SEC-BE":    "SEC-BE",
    "ALIGN-BE":  "ALIGN-BE",
}

PHASE_FOLDER_MAP_FRONTEND = {
    "F1":        "F1",
    "F2":        "F2",
    "F3":        "F3",
    "F4":        "F4",
    "SEC-FE":    "SEC-FE",
    "ALIGN-FE":  "ALIGN-FE",
}


# ─────────────────────────────────────────────────────────────────────────────
# STAGE 1 — Parse & Plan
# ─────────────────────────────────────────────────────────────────────────────

def stage1_parse_and_plan(mod: str, version: int, state: dict, track: str,
                               base: Path = None, frontend_base: Path = None) -> dict | None:
    """
    Parses backend-execution-plan.md + backend-test-plan.md
    (track="backend") or frontend-execution-plan.md +
    frontend-test-plan.md (track="frontend"), validates marker
    structure, and shows a generation plan before anything is written.

    Reports coverage counts (PHASE/SUB/API/XM/TC) for both source
    artifacts, orphan-TC warnings, and asks for approval before Stage 1
    is marked complete. TC blocks nest directly under PHASE/SUB — no
    MARK level.
    """
    if base is None:
        base = get_module_version_path(mod, version)
    if frontend_base is None:
        frontend_base = get_frontend_module_version_path(mod, version)

    if track == "backend":
        exec_path = base / "P3_1" / "backend-execution-plan.md"
        test_path = base / "P3_5_BE" / "backend-test-plan.md"
        exec_label, test_label = "backend-execution-plan.md", "backend-test-plan.md"
    elif track == "frontend":
        exec_path = frontend_base / "P3_2" / "frontend-execution-plan.md"
        test_path = frontend_base / "P3_5_FE" / "frontend-test-plan.md"
        exec_label, test_label = "frontend-execution-plan.md", "frontend-test-plan.md"
    else:
        print(f"  ERROR: unknown track '{track}' — must be 'backend' or 'frontend'.")
        return None

    print()
    print("═" * 70)
    print(f"  STAGE 1 — Parse & Plan")
    print(f"  Module : {mod}  (v{version})   Track: {track}")
    print("═" * 70)
    print()

    exec_result: ParseResult | None = None
    test_result: ParseResult | None = None

    if exec_path.exists():
        exec_result = parse_file(exec_path)
        print(f"  ✓ Read {exec_label}  ({exec_result.total_lines} lines)")
    else:
        print(f"  ⚠ {exec_label} not found at {exec_path}")
        print(f"    Run agent2_archive.py --track {track} first.")

    if test_path.exists():
        test_result = parse_file(test_path)
        print(f"  ✓ Read {test_label}      ({test_result.total_lines} lines)")
    else:
        print(f"  — {test_label} not found — will skip Stage 3 (acceptable if not generated yet)")

    if not exec_result and not test_result:
        print()
        print(f"  ERROR: Neither {exec_label} nor {test_label} found. Nothing to split.")
        return None

    all_errors = []
    if exec_result:
        all_errors += [(exec_label, e) for e in exec_result.errors]
    if test_result:
        all_errors += [(test_label, e) for e in test_result.errors]

    if all_errors:
        print()
        print("  ✗ STRUCTURAL ERRORS FOUND — splitting blocked until fixed:")
        print()
        for fname, err in all_errors:
            print(f"    [{err.severity}] {fname} line {err.line}: {err.message}")
        print()
        print("  Fix the marker structure in the source artifact and re-run Stage 1.")
        return None

    print()
    print("  ✓ No structural errors — marker hierarchy is valid.")

    plan = {
        "exec_path": str(exec_path) if exec_result else None,
        "test_path": str(test_path) if test_result else None,
        "track": track,
        "exec_summary": {},
        "test_summary": {},
    }

    if exec_result:
        phases = find_by_kind(exec_result.root_blocks, "phase")
        apis = find_by_kind(exec_result.root_blocks, "api")
        xms = find_by_kind(exec_result.root_blocks, "xm")
        subs = find_by_kind(exec_result.root_blocks, "sub")

        print()
        print(f"  ── {exec_label} plan ──────────────────────────────────")
        print(f"    PHASE blocks : {len(phases)}")
        for p in phases:
            sub_count = len([s for s in p.children if s.kind == "sub"])
            api_count = len([a for a in flatten([p]) if a.kind == "api"])
            xm_count  = len([x for x in flatten([p]) if x.kind == "xm"])
            extra = ""
            if sub_count:
                extra += f", {sub_count} sub-phase(s)"
            if api_count:
                extra += f", {api_count} API(s)"
            if xm_count:
                extra += f", {xm_count} XM(s)"
            print(f"      - PHASE:{p.marker_id:<14} → 1 file{extra}")
        print(f"    Total API atomic files : {len(apis)}")
        print(f"    Total XM atomic files  : {len(xms)}" + (" (backend track only)" if track == "frontend" and xms else ""))

        plan["exec_summary"] = {
            "phases": len(phases), "apis": len(apis),
            "xms": len(xms), "subs": len(subs),
        }

    if test_result:
        # TCs sit directly under PHASE or SUB — no MARK level.
        tcs = find_by_kind(test_result.root_blocks, "tc")
        subs_t = find_by_kind(test_result.root_blocks, "sub")
        phases_t = find_by_kind(test_result.root_blocks, "phase")

        print()
        print(f"  ── {test_label} plan ───────────────────────────────────────")
        for p in phases_t:
            sub_count = len([s for s in p.children if s.kind == "sub"])
            tc_count = len([t for t in flatten([p]) if t.kind == "tc"])
            extra = f", {sub_count} sub-section(s)" if sub_count else " (no SUB — below threshold)"
            print(f"      - PHASE:{p.marker_id:<12} → {tc_count} TC(s){extra}")
        print(f"    Total TC atomic files : {len(tcs)}")

        # Orphan TC detection — same principle as v1, just PHASE instead of MARK
        orphan_warnings = []
        for p in phases_t:
            sub_blocks = [c for c in p.children if c.kind == "sub"]
            if not sub_blocks:
                continue
            tcs_in_subs = {t.marker_id for sub in sub_blocks for t in flatten([sub]) if t.kind == "tc"}
            all_tcs_in_phase = [t for t in flatten([p]) if t.kind == "tc"]
            orphans = [t for t in all_tcs_in_phase if t.marker_id not in tcs_in_subs]
            if orphans:
                orphan_warnings.append((p.marker_id, orphans))

        if orphan_warnings:
            print()
            print("  ⚠ WARNING — Orphan TCs (inside PHASE but outside any SUB block):")
            print("    Stage 3 will NOT write these TCs to any package file.")
            print("    Wrap them in <!-- SUB:...:START/END --> before continuing.")
            for phase_id, orphans in orphan_warnings:
                ids = ", ".join(t.marker_id for t in orphans)
                print(f"    PHASE:{phase_id} → {len(orphans)} orphan TC(s): {ids}")
            print()

        plan["test_summary"] = {
            "phases": len(phases_t), "tcs": len(tcs), "subs": len(subs_t),
        }

    total_files = (
        plan["exec_summary"].get("apis", 0)
        + plan["exec_summary"].get("xms", 0)
        + plan["exec_summary"].get("phases", 0)
        + plan["test_summary"].get("tcs", 0)
        + plan["test_summary"].get("phases", 0)
    )
    print()
    print(f"  Estimated package files to generate: ~{total_files}")
    print()

    if not confirm("  Approve Stage 1 plan and proceed?"):
        print("\n  Stage 1 cancelled — no files written.\n")
        return None

    state["exec_plan_path"] = plan["exec_path"]
    state["test_plan_path"] = plan["test_path"]
    state["track"] = track
    mark_stage_complete(state, 1)
    save_state(mod, version, state, base)

    print("  ✓ Stage 1 complete.\n")
    return plan


# ─────────────────────────────────────────────────────────────────────────────
# FILE WRITER HELPERS
# ─────────────────────────────────────────────────────────────────────────────


# ─────────────────────────────────────────────────────────────────────────────
# STAGE 2 — Split backend-/frontend-execution-plan.md
# ─────────────────────────────────────────────────────────────────────────────

def stage2_split_execution(mod: str, version: int, state: dict, plan: dict | None,
                                track: str, base: Path = None,
                                frontend_base: Path = None, dry_run: bool = False) -> bool:
    """
    Splits
    backend-execution-plan.md (track="backend") or
    frontend-execution-plan.md (track="frontend") into PHASE/SUB/API/XM
    package files.

    track="backend"  → reads P3_1/backend-execution-plan.md (this repo)
                        writes packages/backend-execution/ (this repo)
    track="frontend" → reads P3_2/frontend-execution-plan.md
                        (frontend_base — natively frontend-generated,
                        see WORKSPACE-ARCHITECTURE-REFERENCE.md 11.3)
                        writes packages/frontend-execution/
                        (frontend_base, same repo as the source)
    """
    if base is None:
        base = get_module_version_path(mod, version)
    if frontend_base is None:
        frontend_base = get_frontend_module_version_path(mod, version)

    if track == "backend":
        src_path = base / "P3_1" / "backend-execution-plan.md"
        pkg_root = base / "packages" / "backend-execution"
        phase_map = PHASE_FOLDER_MAP_BACKEND
        artifact_label = "backend-execution-plan.md"
    elif track == "frontend":
        src_path = frontend_base / "P3_2" / "frontend-execution-plan.md"
        pkg_root = frontend_base / "packages" / "frontend-execution"
        phase_map = PHASE_FOLDER_MAP_FRONTEND
        artifact_label = "frontend-execution-plan.md"
    else:
        print(f"  ERROR: unknown track '{track}' — must be 'backend' or 'frontend'.")
        return False

    print()
    print("═" * 70)
    print(f"  STAGE 2 — Split {artifact_label}")
    print(f"  Module : {mod}  (v{version})   Track: {track}")
    print("═" * 70)
    print()

    if not src_path.exists():
        print(f"  — {artifact_label} not found at {src_path}. Skipping Stage 2.\n")
        return True  # not a failure — just nothing to do yet

    result = parse_file(src_path)
    if result.errors:
        print("  ✗ Structural errors present:")
        for e in result.errors[:10]:
            print(f"    [{e.severity}] line {e.line}: {e.message}")
        if len(result.errors) > 10:
            print(f"    ... and {len(result.errors) - 10} more")
        print()
        return False

    phases = find_by_kind(result.root_blocks, "phase")
    write_plan = []

    for phase in phases:
        folder_name = phase_map.get(phase.marker_id)
        if folder_name is None:
            print(f"  ⚠ PHASE:{phase.marker_id} not in the {track} phase map — "
                  f"skipped. (Expected keys: {list(phase_map.keys())}. If this phase "
                  f"legitimately belongs to the OTHER track, that's a generation-time "
                  f"bug in the source file, not a splitter bug.)")
            continue
        folder = pkg_root / folder_name

        sub_blocks = [c for c in phase.children if c.kind == "sub"]
        api_count = len([a for a in flatten([phase]) if a.kind == "api"])
        xm_count  = len([x for x in flatten([phase]) if x.kind == "xm"])

        if sub_blocks:
            preamble = _preamble_content(phase, result.raw_lines)
            header_filename = _safe_filename(f"{phase.marker_id}-HEADER") if preamble else None

            if preamble:
                write_plan.append({
                    "dest": folder / header_filename,
                    "content": preamble,
                    "header": f"<!-- Source: PHASE:{phase.marker_id} / PREAMBLE (before first SUB) -->",
                    "note": "phase-level content (tables, strategy, intro)",
                })

            for sub in sub_blocks:
                fname = _safe_filename(f"{phase.marker_id}-{sub.marker_id}")
                sub_api_count = len([a for a in flatten([sub]) if a.kind == "api"])
                sub_xm_count  = len([x for x in flatten([sub]) if x.kind == "xm"])
                context_ref = (
                    f"<!-- Context: see {header_filename} for phase-level "
                    f"strategy, registry table, and intro -->"
                    if header_filename else ""
                )
                header_line = f"<!-- Source: PHASE:{phase.marker_id} / SUB:{sub.marker_id} -->"
                if context_ref:
                    header_line += f"\n{context_ref}"
                write_plan.append({
                    "dest": folder / fname,
                    "block": sub,
                    "header": header_line,
                    "note": f"{sub_api_count} API(s), {sub_xm_count} XM(s) embedded" if (sub_api_count or sub_xm_count) else "",
                })
        else:
            fname = _safe_filename(phase.marker_id)
            write_plan.append({
                "dest": folder / fname,
                "block": phase,
                "header": f"<!-- Source: PHASE:{phase.marker_id} -->",
                "note": f"{api_count} API(s), {xm_count} XM(s) embedded" if (api_count or xm_count) else "",
            })

    print(f"  Files to write: {len(write_plan)}")
    for w in write_plan[:15]:
        extra = f"  ({w['note']})" if w.get("note") else ""
        rel_base = frontend_base if track == "frontend" else base
        print(f"    {w['dest'].relative_to(rel_base)}{extra}")
    if len(write_plan) > 15:
        print(f"    ... and {len(write_plan) - 15} more")
    print()

    if dry_run:
        print("  — DRY RUN: no files written, no state changed.\n")
        return True

    if not confirm("  Approve Stage 2 — write these files?"):
        print("\n  Stage 2 cancelled — no files written.\n")
        return False

    _execute_write_plan(write_plan)

    print(f"\n  ✓ {len(write_plan)} files written to packages/{track}-execution/")
    mark_stage_complete(state, 2)
    save_state(mod, version, state, base)
    print("  ✓ Stage 2 complete.\n")
    return True



# ─────────────────────────────────────────────────────────────────────────────
# STAGE 3 — Split backend-/frontend-test-plan.md
# ─────────────────────────────────────────────────────────────────────────────

def stage3_split_test(mod: str, version: int, state: dict, plan: dict | None,
                           track: str, base: Path = None,
                           frontend_base: Path = None, dry_run: bool = False) -> bool:
    """
    Splits
    backend-test-plan.md (track="backend") or frontend-test-plan.md
    (track="frontend") into PHASE/SUB/TC package files.

    NO MARK LEVEL — this is the key structural difference from v1's
    stage3_split_test. Each file is single-tool by construction
    (backend-test-plan.md is JUnit-only, frontend-test-plan.md is
    Playwright-only), so there is nothing to distinguish via MARK
    inside the file — see PROJECT-3-REGISTRY.md Section 5.7.4
    "v2.0 SIMPLIFICATION". TC blocks nest directly under PHASE or SUB.
    """
    if base is None:
        base = get_module_version_path(mod, version)
    if frontend_base is None:
        frontend_base = get_frontend_module_version_path(mod, version)

    if track == "backend":
        src_path = base / "P3_5_BE" / "backend-test-plan.md"
        pkg_root = base / "packages" / "backend-test"
        expected_phase_key = "TEST-PLAN-BE"
        artifact_label = "backend-test-plan.md"
    elif track == "frontend":
        src_path = frontend_base / "P3_5_FE" / "frontend-test-plan.md"
        pkg_root = frontend_base / "packages" / "frontend-test"
        expected_phase_key = "TEST-PLAN-FE"
        artifact_label = "frontend-test-plan.md"
    else:
        print(f"  ERROR: unknown track '{track}' — must be 'backend' or 'frontend'.")
        return False

    print()
    print("═" * 70)
    print(f"  STAGE 3 — Split {artifact_label}")
    print(f"  Module : {mod}  (v{version})   Track: {track}")
    print("═" * 70)
    print()

    if not src_path.exists():
        print(f"  — {artifact_label} not found at {src_path}. Skipping Stage 3.\n")
        return True

    result = parse_file(src_path)
    if result.errors:
        print("  ✗ Structural errors present:")
        for e in result.errors[:10]:
            print(f"    [{e.severity}] line {e.line}: {e.message}")
        if len(result.errors) > 10:
            print(f"    ... and {len(result.errors) - 10} more")
        print()
        return False

    phases = find_by_kind(result.root_blocks, "phase")
    write_plan = []

    for phase in phases:
        if phase.marker_id != expected_phase_key:
            print(f"  ⚠ PHASE:{phase.marker_id} does not match the expected "
                  f"'{expected_phase_key}' for {artifact_label} — processing "
                  f"anyway, but this may indicate a generation-time naming bug.")
        folder = pkg_root  # each test-plan file has exactly one top-level PHASE

        sub_blocks = [c for c in phase.children if c.kind == "sub"]
        tc_count = len([t for t in flatten([phase]) if t.kind == "tc"])

        if sub_blocks:
            preamble = _preamble_content(phase, result.raw_lines)
            header_filename = _safe_filename(f"{phase.marker_id}-HEADER") if preamble else None

            if preamble:
                write_plan.append({
                    "dest": folder / header_filename,
                    "content": preamble,
                    "header": f"<!-- Source: PHASE:{phase.marker_id} / PREAMBLE (before first SUB) -->",
                    "note": "phase-level content (mandatory scenarios, intro)",
                })

            for sub in sub_blocks:
                fname = _safe_filename(sub.marker_id)
                sub_tc_count = len([t for t in flatten([sub]) if t.kind == "tc"])
                context_ref = (
                    f"<!-- Context: see {header_filename} for phase-level "
                    f"intro and mandatory scenarios -->"
                    if header_filename else ""
                )
                header_line = f"<!-- Source: PHASE:{phase.marker_id} / SUB:{sub.marker_id} -->"
                if context_ref:
                    header_line += f"\n{context_ref}"
                write_plan.append({
                    "dest": folder / fname,
                    "block": sub,
                    "header": header_line,
                    "note": f"{sub_tc_count} TC(s) embedded",
                })
        else:
            # Below the SUB-threshold (PROJECT-3-REGISTRY.md 5.7.4) — all
            # TCs sit directly under PHASE, one combined file.
            fname = _safe_filename(phase.marker_id)
            write_plan.append({
                "dest": folder / fname,
                "block": phase,
                "header": f"<!-- Source: PHASE:{phase.marker_id} -->",
                "note": f"{tc_count} TC(s) embedded",
            })

    print(f"  Files to write: {len(write_plan)}")
    for w in write_plan[:15]:
        extra = f"  ({w['note']})" if w.get("note") else ""
        rel_base = frontend_base if track == "frontend" else base
        print(f"    {w['dest'].relative_to(rel_base)}{extra}")
    if len(write_plan) > 15:
        print(f"    ... and {len(write_plan) - 15} more")
    print()

    if dry_run:
        print("  — DRY RUN: no files written, no state changed.\n")
        return True

    if not confirm("  Approve Stage 3 — write these files?"):
        print("\n  Stage 3 cancelled — no files written.\n")
        return False

    _execute_write_plan(write_plan)

    print(f"\n  ✓ {len(write_plan)} files written to packages/{track}-test/")
    mark_stage_complete(state, 3)
    save_state(mod, version, state, base)
    print("  ✓ Stage 3 complete.\n")
    return True


# ─────────────────────────────────────────────────────────────────────────────
# STAGE 4 — Generate Index Files
# ─────────────────────────────────────────────────────────────────────────────


# ─────────────────────────────────────────────────────────────────────────────
# STAGE 4 — Generate Index Files
# ─────────────────────────────────────────────────────────────────────────────

def stage4_generate_index(mod: str, version: int, state: dict, track: str,
                           base: Path = None, frontend_base: Path = None) -> bool:
    """
    Generate index.md in every package folder listing its contents.

    track-aware (fixed): for track="frontend", indexes
    frontend_base/packages/ (the frontend repo) — NOT base/packages/
    (the backend repo). An earlier version of this function always
    indexed the backend path regardless of track, which silently
    produced empty/wrong index files for the frontend track. Fixed here.
    """
    if base is None:
        base = get_module_version_path(mod, version)
    if frontend_base is None:
        frontend_base = get_frontend_module_version_path(mod, version)

    active_base = frontend_base if track == "frontend" else base
    pkg_root = active_base / "packages"

    print()
    print("═" * 70)
    print(f"  STAGE 4 — Generate Index Files")
    print(f"  Module : {mod}  (v{version})   Track: {track}")
    print("═" * 70)
    print()

    if not pkg_root.exists():
        print("  — No packages/ folder found. Run Stage 2/3 first.\n")
        return False

    # Find every folder under packages/ that contains at least one .md file
    index_targets = []
    for folder in sorted(pkg_root.rglob("*")):
        if not folder.is_dir():
            continue
        md_files = sorted([f for f in folder.glob("*.md") if f.name != "index.md"])
        if md_files:
            index_targets.append((folder, md_files))

    print(f"  Folders to index: {len(index_targets)}")
    for folder, files in index_targets:
        print(f"    {folder.relative_to(active_base)}  ({len(files)} file(s))")
    print()

    if not confirm("  Approve Stage 4 — write index.md files?"):
        print("\n  Stage 4 cancelled — no index files written.\n")
        return False

    for folder, files in index_targets:
        lines = [f"# Index — {folder.relative_to(pkg_root)}", ""]
        for f in files:
            lines.append(f"- [{f.stem}]({f.name})")
        (folder / "index.md").write_text("\n".join(lines) + "\n", encoding="utf-8")

    print(f"\n  ✓ {len(index_targets)} index.md files written.")
    mark_stage_complete(state, 4)
    save_state(mod, version, state, base)
    print("  ✓ Stage 4 complete.\n")
    return True


# ─────────────────────────────────────────────────────────────────────────────
# STAGE 5 — Verify Completeness
# ─────────────────────────────────────────────────────────────────────────────

def _content_hash(text: str) -> str:
    """SHA-256 hash of content, normalized (strip leading/trailing whitespace)
    so that header line additions don't break comparison — only the actual
    block content matters."""
    import hashlib
    return hashlib.sha256(text.strip().encode("utf-8")).hexdigest()


def _extract_body(file_text: str) -> str:
    """
    Strip the '<!-- Source: ... -->' header line (added by _write_block)
    from a package file's content, returning only the original block body.
    """
    lines = file_text.split("\n")
    if lines and lines[0].strip().startswith("<!-- Source:"):
        rest = lines[1:]
        if rest and rest[0].strip() == "":
            rest = rest[1:]
        return "\n".join(rest)
    return file_text


def stage5_verify(mod: str, version: int, state: dict, track: str,
                   base: Path = None, frontend_base: Path = None) -> bool:
    """
    Same completeness + content-hash
    integrity check, scoped to one track's artifact pair.

    track="backend"  → backend-execution-plan.md + backend-test-plan.md,
                        checked against packages/backend-execution/ and
                        packages/backend-test/ (this repo)
    track="frontend" → frontend-execution-plan.md + frontend-test-plan.md,
                        checked against packages/frontend-execution/ and
                        packages/frontend-test/ (frontend repo — natively
                        generated there, see WORKSPACE-ARCHITECTURE-
                        REFERENCE.md Section 11.3)
    """
    if base is None:
        base = get_module_version_path(mod, version)
    if frontend_base is None:
        frontend_base = get_frontend_module_version_path(mod, version)

    if track == "backend":
        exec_path = base / "P3_1" / "backend-execution-plan.md"
        test_path = base / "P3_5_BE" / "backend-test-plan.md"
        pkg_root = base / "packages"
        exec_label, test_label = "backend-execution-plan.md", "backend-test-plan.md"
        exec_pkg_dir, test_pkg_dir = "backend-execution", "backend-test"
        display_base = base
    elif track == "frontend":
        exec_path = frontend_base / "P3_2" / "frontend-execution-plan.md"
        test_path = frontend_base / "P3_5_FE" / "frontend-test-plan.md"
        pkg_root = frontend_base / "packages"
        exec_label, test_label = "frontend-execution-plan.md", "frontend-test-plan.md"
        exec_pkg_dir, test_pkg_dir = "frontend-execution", "frontend-test"
        display_base = frontend_base
    else:
        print(f"  ERROR: unknown track '{track}' — must be 'backend' or 'frontend'.")
        return False

    print()
    print("═" * 70)
    print(f"  STAGE 5 — Verify Completeness & Integrity")
    print(f"  Module : {mod}  (v{version})   Track: {track}")
    print("═" * 70)
    print()

    missing_issues = []
    hash_issues = []
    checked_count = 0

    def _find_marker_in_files(kind: str, marker_id: str, pkg_subroot: Path):
        pattern_start = f"<!-- {kind.upper()}:{marker_id}:START -->"
        for f in pkg_subroot.rglob("*.md"):
            if f.name == "index.md":
                continue
            text = f.read_text(encoding="utf-8")
            if pattern_start in text:
                sub_result = parse_file(f,  )
                matches = [
                    b for b in flatten(sub_result.root_blocks)
                    if b.kind == kind and b.marker_id == marker_id
                ]
                if matches:
                    return f, matches[0]
        return None, None

    def _verify_blocks(blocks, file_label, pkg_subroot):
        nonlocal checked_count
        for block in blocks:
            checked_count += 1
            pkg_file, pkg_block = _find_marker_in_files(block.kind, block.marker_id, pkg_subroot)

            if pkg_file is None:
                missing_issues.append(
                    f"{block.kind.upper()}:{block.marker_id} ({file_label}) — "
                    f"found in source but not embedded in any package file"
                )
                continue

            source_hash = _content_hash(block.content)
            pkg_hash = _content_hash(pkg_block.content)

            if source_hash != pkg_hash:
                hash_issues.append(
                    f"{block.kind.upper()}:{block.marker_id} ({file_label}) — "
                    f"content MISMATCH inside {pkg_file.relative_to(display_base)}\n"
                    f"      source hash : {source_hash[:16]}...\n"
                    f"      package hash: {pkg_hash[:16]}..."
                )

    if exec_path.exists():
        result = parse_file(exec_path)
        apis = find_by_kind(result.root_blocks, "api")
        xms = find_by_kind(result.root_blocks, "xm")

        _verify_blocks(apis, exec_label, pkg_root / exec_pkg_dir)
        _verify_blocks(xms, exec_label, pkg_root / exec_pkg_dir)

        print(f"  {exec_label} : {len(apis)} APIs, {len(xms)} XMs checked")

    if test_path.exists():
        result = parse_file(test_path)
        tcs = find_by_kind(result.root_blocks, "tc")

        _verify_blocks(tcs, test_label, pkg_root / test_pkg_dir)

        print(f"  {test_label}       : {len(tcs)} TCs checked")

    print(f"  Total atomic elements checked : {checked_count}")
    print()

    if missing_issues or hash_issues:
        if missing_issues:
            print(f"  ✗ {len(missing_issues)} MISSING file issue(s):")
            for i in missing_issues:
                print(f"    - {i}")
            print()
        if hash_issues:
            print(f"  ✗ {len(hash_issues)} CONTENT MISMATCH issue(s) — possible content drift:")
            for i in hash_issues:
                print(f"    - {i}")
            print()
        print("  This means a package file's content does NOT exactly match")
        print("  the corresponding block in the archived source artifact.")
        print(f"  Re-run Stage 2/3 (--track {track}) to regenerate, then Stage 5 again.")
        return False

    print("  ✓ All atomic elements (API/XM/TC) have matching package files.")
    print("  ✓ Content hash verified for every element — zero drift from archived source.")
    print("  ✓ No content loss detected.")
    mark_stage_complete(state, 5)
    save_state(mod, version, state, base)
    print()
    print(f"  ✓ Stage 5 complete — {track} splitting verified.")
    print()
    print(f"  Module [{mod}] v{version} ({track} track) fully packaged.")
    print(f"  Ready for downstream agents (Claude Code / Copilot / Codex).")
    print()
    return True


# ─────────────────────────────────────────────────────────────────────────────
# ORCHESTRATION
# ─────────────────────────────────────────────────────────────────────────────


# ─────────────────────────────────────────────────────────────────────────────
# ORCHESTRATION
# ─────────────────────────────────────────────────────────────────────────────

def run_stage(stage: int, mod: str, version: int, state: dict, plan: dict | None,
              base: Path = None, frontend_base: Path = None, dry_run: bool = False,
              track: str = None) -> tuple[bool, dict | None]:
    """
    Run a single stage. Returns (success, plan_for_next_stage).
    track: "backend" or "frontend" — required for every call (Stage 2/3
    need to know which artifact pair to split; Stage 4/5 need to know
    which repo's packages/ to operate on).
    """
    if stage == 1:
        result_plan = stage1_parse_and_plan(mod, version, state, track, base, frontend_base)
        return (result_plan is not None), result_plan
    elif stage == 2:
        ok = stage2_split_execution(mod, version, state, plan, track, base, frontend_base, dry_run=dry_run)
        return ok, plan
    elif stage == 3:
        ok = stage3_split_test(mod, version, state, plan, track, base, frontend_base, dry_run=dry_run)
        return ok, plan
    elif stage == 4:
        ok = stage4_generate_index(mod, version, state, track, base, frontend_base)
        return ok, plan
    elif stage == 5:
        ok = stage5_verify(mod, version, state, track, base, frontend_base)
        return ok, plan
    else:
        print(f"  Unknown stage: {stage}")
        return False, plan


def main():
    parser = argparse.ArgumentParser(description="Split execution-plan/test-plan artifacts into package files.")
    parser.add_argument("--module", "-m", required=True, help="Module code (e.g. ORG, FIN).")
    parser.add_argument("--track", choices=["backend", "frontend"], required=True,
                        help="Which artifact pair to split: 'backend' "
                             "(backend-execution-plan.md + backend-test-plan.md) or "
                             "'frontend' (frontend-execution-plan.md + frontend-test-plan.md).")
    parser.add_argument("--stage", "-s", type=int, choices=[1, 2, 3, 4, 5],
                        help="Run a single stage only.")
    parser.add_argument("--resume", "-r", action="store_true",
                        help="Resume from the next incomplete stage.")
    parser.add_argument("--status", action="store_true",
                        help="Show stage completion status and exit.")
    parser.add_argument("--dry-run", action="store_true",
                        help="Show what would be written without writing anything.")
    parser.add_argument("--output", "-o", help="Override the module's base path (advanced/testing use).")

    args = parser.parse_args()

    try:
        mod = validate_module(args.module)
    except ValueError as e:
        print(f"\n  ERROR: {e}\n")
        sys.exit(1)

    # Determine version
    registry = load_modules_registry()
    entry = registry.get("modules", {}).get(mod)
    version = (entry.get("current_version") if entry else None) or 1

    base = Path(args.output) if args.output else get_module_version_path(mod, version)
    frontend_base = get_frontend_module_version_path(mod, version)

    if args.status:
        print_status(mod, version, base, track=args.track)
        sys.exit(0)

    state = load_state(mod, version, base)

    if args.stage:
        plan = None
        if args.stage > 1 and (args.stage - 1) not in state.get("stages_completed", []):
            # Need Stage 1's plan for stages 2/3 — re-run it silently first
            ok, plan = run_stage(1, mod, version, state, None, base, frontend_base, track=args.track)
            if not ok:
                sys.exit(1)
        ok, _ = run_stage(args.stage, mod, version, state, plan, base,
                           frontend_base, dry_run=args.dry_run, track=args.track)
        sys.exit(0 if ok else 1)

    # Full run or resume
    if args.resume:
        stages_to_run = [s for s in range(1, 6) if s not in state.get("stages_completed", [])]
        if not stages_to_run:
            print("\n  All stages already complete. Nothing to resume.\n")
            sys.exit(0)
    else:
        stages_to_run = list(range(1, 6))

    plan = None
    for stage in stages_to_run:
        ok, plan = run_stage(stage, mod, version, state, plan, base,
                              frontend_base, dry_run=args.dry_run, track=args.track)
        if not ok:
            print(f"\n  Stopped at Stage {stage}. Fix the issue and re-run with --resume.\n")
            sys.exit(1)

    print("\n  ✓ All 5 stages complete.\n")


if __name__ == "__main__":
    main()
