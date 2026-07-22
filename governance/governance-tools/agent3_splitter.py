"""
ERP Governance Tools — Agent 3: Artifact Splitter
====================================================
Reads Marker Protocol (PROJECT-3-REGISTRY.md Section 5.7 — moved here
in v2.1 from the old "P3 Section 6.7") from execution-plan.md and
test-plan.md, then splits them into addressable package files.

Staged execution — each stage requires explicit approval before proceeding.
Stages are independently resumable: if Stage 3 fails, Stage 1/2 results
are preserved and Stage 3 alone can be re-run.

Usage (v1 modules — ORG, NOTIFICATION, FILESVC — UNCHANGED):
    python agent3_splitter.py --module ORG
    python agent3_splitter.py --module ORG --stage 1
    python agent3_splitter.py --module ORG --resume
    python agent3_splitter.py --module ORG --status

Usage (v2 modules — any module registered from now on):
    python agent3_splitter.py --module FIN --track backend
      (run again later, after real implementation + API docs + GATE:
      BACKEND MODULE COMPLETE)
    python agent3_splitter.py --module FIN --track frontend

Stages (v1 — unchanged names, still apply as-is):
    1. Parse & Plan          — read markers, validate structure, show plan
    2. Split execution-plan  — write PHASE/SUB/API/XM package files
    3. Split test-plan       — write MARK/SUB/TC package files
    4. Generate Index Files  — index.md per package folder
    5. Verify Completeness   — line-count + marker-count cross-check

=====================================================================
v2.1 UPDATE — DUAL GOVERNANCE MODEL SUPPORT (--track flag)
=====================================================================
--track is REQUIRED for v2 modules (config.get_governance_model(mod)
== "v2") and REJECTED (hard error, exit 1) for v1 modules — v1 has no
backend/frontend split, it always runs the single combined stage2/stage3
exactly as before, so passing --track for one is a usage mistake, not a
no-op (matches agent1_create_structure.py's --frontend-only behavior).

--track backend : splits backend-execution-plan.md (P3_1/) and
                   backend-test-plan.md (P3_5_BE/) — output stays
                   entirely in THIS repo (packages/backend-execution/,
                   packages/backend-test/). No PLAYWRIGHT-style
                   cross-repo routing needed for this track — see
                   WORKSPACE-ARCHITECTURE-REFERENCE.md Section 11.3.

--track frontend : splits frontend-execution-plan.md (P3_2/) and
                    frontend-test-plan.md (P3_5_FE/). Under v2, these
                    source files are NATIVELY frontend-generated (they
                    live in the frontend repo's own P3_2/P3_5_FE
                    folders, not routed from this backend repo) — so
                    this track is normally invoked with THIS SCRIPT'S
                    COPY living in (or --output pointed at) the
                    frontend repo, not this one. Included here for
                    completeness / local testing only.

This is intentionally a SEPARATE flag from --stage (which still means
1-5, the five internal pipeline steps, unchanged). --track picks WHICH
artifact pair gets split; --stage picks WHICH step of that split to
run. The two compose: --track backend --stage 2 is valid and means
"run just the split-execution step, for the backend track."

STATUS: all previously-PENDING items are now closed as of this pass:
  ✓ stage1_parse_and_plan_v2() added — passes governance_model="v2"
    into parse_file(), so a stray MARK token in a v2 file is now
    correctly caught as a CRITICAL error at Stage 1 (not silently
    tolerated as it briefly was between the agent3-only pass and this one).
  ✓ stage5_verify_v2() added — same completeness + content-hash
    integrity check as v1, scoped per track.
  ✓ agent1_create_structure.py — model-aware (creates the right
    folder set automatically per module), plus --frontend-only for
    the frontend repo's own P3_2/P3_5_FE/P4_2 folders.
  ✓ agent2_archive.py — model/track-aware scan+archive, correct
    manifest flag per track (archived_backend/archived_frontend for
    v2, single archived flag unchanged for v1).
=====================================================================
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
    PLAYWRIGHT_OUTPUT_BASE_PATH,
    FRONTEND_OUTPUT_BASE_PATH,
    GOVERNANCE_MODEL_V1,
    GOVERNANCE_MODEL_V2,
    get_governance_model,
    get_module_version_path,
    get_playwright_module_version_path,
    get_module_structure,
    get_artifact_files,
    resolve_filename,
    validate_module,
    load_modules_registry,
)
from marker_parser import (
    parse_file, flatten, find_by_kind, MarkerBlock, ParseResult,
)

STAGE_NAMES = {
    1: "Parse & Plan",
    2: "Split execution-plan.md",
    3: "Split test-plan.md",
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
    path = _state_path(mod, version, base)
    if path.exists():
        with open(path, "r", encoding="utf-8") as fh:
            return json.load(fh)
    return {
        "module": mod,
        "version": version,
        "stages_completed": [],
        "last_run": None,
        "exec_plan_path": None,
        "test_plan_path": None,
    }


def save_state(mod: str, version: int, state: dict, base: Path = None):
    path = _state_path(mod, version, base)
    path.parent.mkdir(parents=True, exist_ok=True)
    state["last_run"] = datetime.now().isoformat()
    with open(path, "w", encoding="utf-8") as fh:
        json.dump(state, fh, indent=2, ensure_ascii=False)


def mark_stage_complete(state: dict, stage: int):
    if stage not in state["stages_completed"]:
        state["stages_completed"].append(stage)
        state["stages_completed"].sort()


def print_status(mod: str, version: int, base: Path = None):
    state = load_state(mod, version, base)
    print()
    print("═" * 60)
    print(f"  AGENT 3 — Status")
    print(f"  Module  : {mod}  (v{version})")
    print("═" * 60)
    for s in range(1, 6):
        done = s in state["stages_completed"]
        mark = "✓ DONE" if done else "— pending"
        print(f"  Stage {s} — {STAGE_NAMES[s]:<28} {mark}")
    if state["last_run"]:
        print()
        print(f"  Last run: {state['last_run']}")
    print()


# ─────────────────────────────────────────────────────────────────────────────
# APPROVAL GATE
# ─────────────────────────────────────────────────────────────────────────────

def confirm(prompt: str = "  Proceed?") -> bool:
    answer = input(f"{prompt} [y/N]: ").strip().lower()
    return answer == "y"


# ─────────────────────────────────────────────────────────────────────────────
# STAGE 1 — Parse & Plan
# ─────────────────────────────────────────────────────────────────────────────

def stage1_parse_and_plan(mod: str, version: int, state: dict, base: Path = None) -> dict | None:
    """
    Parse execution-plan.md and test-plan.md.
    Validate marker structure.
    Show a plan of what Stage 2/3 will produce.
    Returns a plan dict if approved, None if cancelled or blocked by errors.
    """
    if base is None:
        base = get_module_version_path(mod, version)
    exec_path = base / "P3" / "execution-plan.md"
    test_path = base / "P3_5" / "test-plan.md"

    print()
    print("═" * 70)
    print(f"  STAGE 1 — Parse & Plan")
    print(f"  Module : {mod}  (v{version})")
    print("═" * 70)
    print()

    exec_result: ParseResult | None = None
    test_result: ParseResult | None = None

    # ── Parse execution-plan.md ───────────────────────────────────────────────
    if exec_path.exists():
        exec_result = parse_file(exec_path)
        print(f"  ✓ Read execution-plan.md  ({exec_result.total_lines} lines)")
    else:
        print(f"  ⚠ execution-plan.md not found at {exec_path}")
        print(f"    Run agent2_archive.py first to populate P3/.")

    # ── Parse test-plan.md ────────────────────────────────────────────────────
    if test_path.exists():
        test_result = parse_file(test_path)
        print(f"  ✓ Read test-plan.md      ({test_result.total_lines} lines)")
    else:
        print(f"  — test-plan.md not found — will skip Stage 3 (acceptable if not generated yet)")

    if not exec_result and not test_result:
        print()
        print("  ERROR: Neither execution-plan.md nor test-plan.md found. Nothing to split.")
        return None

    # ── Report structural errors ──────────────────────────────────────────────
    all_errors = []
    if exec_result:
        all_errors += [("execution-plan.md", e) for e in exec_result.errors]
    if test_result:
        all_errors += [("test-plan.md", e) for e in test_result.errors]

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

    # ── Build plan summary ────────────────────────────────────────────────────
    plan = {
        "exec_path": str(exec_path) if exec_result else None,
        "test_path": str(test_path) if test_result else None,
        "exec_summary": {},
        "test_summary": {},
    }

    if exec_result:
        phases = find_by_kind(exec_result.root_blocks, "phase")
        apis = find_by_kind(exec_result.root_blocks, "api")
        xms = find_by_kind(exec_result.root_blocks, "xm")
        subs = find_by_kind(exec_result.root_blocks, "sub")

        print()
        print("  ── execution-plan.md plan ──────────────────────────────────")
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
        print(f"    Total XM atomic files  : {len(xms)}")

        plan["exec_summary"] = {
            "phases": len(phases), "apis": len(apis),
            "xms": len(xms), "subs": len(subs),
        }

    if test_result:
        marks = find_by_kind(test_result.root_blocks, "mark")
        tcs = find_by_kind(test_result.root_blocks, "tc")
        subs_t = find_by_kind(test_result.root_blocks, "sub")

        print()
        print("  ── test-plan.md plan ───────────────────────────────────────")
        for m in marks:
            sub_count = len([s for s in m.children if s.kind == "sub"])
            tc_count = len([t for t in flatten([m]) if t.kind == "tc"])
            extra = f", {sub_count} sub-section(s)" if sub_count else " (no SUB — below threshold)"
            print(f"      - MARK:{m.marker_id:<12} → {tc_count} TC(s){extra}")
        print(f"    Total TC atomic files : {len(tcs)}")

        # Detect orphan TCs: inside a MARK that HAS subs, but not inside any sub
        orphan_warnings = []
        for m in marks:
            sub_blocks = [c for c in m.children if c.kind == "sub"]
            if not sub_blocks:
                continue  # no SUBs -> all TCs go directly under MARK (acceptable)
            tcs_in_subs = {t.marker_id for sub in sub_blocks for t in flatten([sub]) if t.kind == "tc"}
            all_tcs_in_mark = [t for t in flatten([m]) if t.kind == "tc"]
            orphans = [t for t in all_tcs_in_mark if t.marker_id not in tcs_in_subs]
            if orphans:
                orphan_warnings.append((m.marker_id, orphans))

        if orphan_warnings:
            print()
            print("  ⚠ WARNING — Orphan TCs (inside MARK but outside any SUB block):")
            print("    Stage 3 will NOT write these TCs to any package file.")
            print("    Wrap them in <!-- SUB:...:START/END --> before continuing.")
            for mark_id, orphans in orphan_warnings:
                ids = ", ".join(t.marker_id for t in orphans)
                print(f"    MARK:{mark_id} → {len(orphans)} orphan TC(s): {ids}")
            print()

        plan["test_summary"] = {
            "marks": len(marks), "tcs": len(tcs), "subs": len(subs_t),
        }

    total_files = (
        plan["exec_summary"].get("apis", 0)
        + plan["exec_summary"].get("xms", 0)
        + plan["exec_summary"].get("phases", 0)
        + plan["test_summary"].get("tcs", 0)
        + plan["test_summary"].get("marks", 0)
    )
    print()
    print(f"  Estimated package files to generate: ~{total_files}")
    print()

    if not confirm("  Approve Stage 1 plan and proceed?"):
        print("\n  Stage 1 cancelled — no files written.\n")
        return None

    # Persist parsed results for stage 2/3 to reuse (re-parse is cheap, but
    # we store paths so stage 2/3 can run independently if resumed later)
    state["exec_plan_path"] = plan["exec_path"]
    state["test_plan_path"] = plan["test_path"]
    mark_stage_complete(state, 1)
    save_state(mod, version, state, base)

    print("  ✓ Stage 1 complete.\n")
    return plan


def _v2_artifact_path(mod: str, base: Path, frontend_base: Path, stage: str) -> Path:
    """
    Resolve a v2-model artifact's file path from config.py's single source
    of truth (get_module_structure()/get_artifact_files()/resolve_filename()),
    joined onto the ALREADY version-resolved `base`/`frontend_base` passed in.

    Deliberately does NOT call config.get_stage_path() — that helper has no
    `version` parameter and always resolves via get_module_path() (the
    unversioned/"version 1" layout), so calling it directly here would
    silently ignore a non-default --version. Routing the stage-folder name
    and artifact filename through config.py (instead of hardcoding them)
    while still joining onto the caller's version-aware base preserves
    both: single source of truth AND correct multi-version behavior.
    """
    structure = get_module_structure(mod)
    artifacts = get_artifact_files(mod)
    frontend_native_stages_v2 = {"P3_2", "P3_5_FE", "P4_2"}
    root = frontend_base if stage in frontend_native_stages_v2 else base
    filename = resolve_filename(artifacts[stage][0], mod)
    return root / structure[stage] / filename


def stage1_parse_and_plan_v2(mod: str, version: int, state: dict, track: str,
                               base: Path = None, frontend_base: Path = None) -> dict | None:
    """
    v2 model equivalent of stage1_parse_and_plan — parses
    backend-execution-plan.md + backend-test-plan.md (track="backend")
    or frontend-execution-plan.md + frontend-test-plan.md
    (track="frontend"), validates marker structure using
    governance_model="v2" (so a stray MARK token is correctly flagged
    as a CRITICAL error — this is the exact gap flagged as PENDING in
    the module docstring's v2.1 UPDATE section, now closed).

    No MARK-level reporting here (unlike stage1_parse_and_plan's
    test-plan.md section) — v2 test-plans have TC directly under
    PHASE/SUB, so the plan summary reports PHASE/SUB/TC counts only.
    """
    if base is None:
        base = get_module_version_path(mod, version)
    if frontend_base is None:
        frontend_base = get_playwright_module_version_path(mod, version)

    if track == "backend":
        exec_path = _v2_artifact_path(mod, base, frontend_base, "P3_1")
        test_path = _v2_artifact_path(mod, base, frontend_base, "P3_5_BE")
        exec_label, test_label = exec_path.name, test_path.name
    elif track == "frontend":
        exec_path = _v2_artifact_path(mod, base, frontend_base, "P3_2")
        test_path = _v2_artifact_path(mod, base, frontend_base, "P3_5_FE")
        exec_label, test_label = exec_path.name, test_path.name
    else:
        print(f"  ERROR: unknown track '{track}' — must be 'backend' or 'frontend'.")
        return None

    print()
    print("═" * 70)
    print(f"  STAGE 1 (v2) — Parse & Plan")
    print(f"  Module : {mod}  (v{version})   Track: {track}")
    print("═" * 70)
    print()

    exec_result: ParseResult | None = None
    test_result: ParseResult | None = None

    if exec_path.exists():
        exec_result = parse_file(exec_path, governance_model=GOVERNANCE_MODEL_V2)
        print(f"  ✓ Read {exec_label}  ({exec_result.total_lines} lines)")
    else:
        print(f"  ⚠ {exec_label} not found at {exec_path}")
        print(f"    Run agent2_archive.py --track {track} first.")

    if test_path.exists():
        test_result = parse_file(test_path, governance_model=GOVERNANCE_MODEL_V2)
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
    print("  ✓ No structural errors — marker hierarchy is valid (v2: no MARK level).")

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
        # v2: no MARK level — TCs sit directly under PHASE or SUB.
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

    if not confirm("  Approve Stage 1 (v2) plan and proceed?"):
        print("\n  Stage 1 cancelled — no files written.\n")
        return None

    state["exec_plan_path"] = plan["exec_path"]
    state["test_plan_path"] = plan["test_path"]
    state["track"] = track
    mark_stage_complete(state, 1)
    save_state(mod, version, state, base)

    print("  ✓ Stage 1 (v2) complete.\n")
    return plan


# ─────────────────────────────────────────────────────────────────────────────
# FILE WRITER HELPERS
# ─────────────────────────────────────────────────────────────────────────────

def _write_block(path: Path, block: "MarkerBlock", header: str = ""):
    """Write a single MarkerBlock's content to a file — copy/paste only."""
    path.parent.mkdir(parents=True, exist_ok=True)
    text = (header + "\n\n") if header else ""
    text += block.content
    path.write_text(text, encoding="utf-8")


def _write_content(path: Path, content: str, header: str = ""):
    """Write raw text content to a file (used for preamble/header files)."""
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


def _guard_playwright_path(path: Path):
    """
    Refuse to write PLAYWRIGHT test-phase content anywhere inside
    backend/governance/ (REPO_BASE_PATH) — forbidden per the STRUCTURAL LAW
    section in backend/CLAUDE.md and frontend/CLAUDE.md. This is a permanent
    guardrail: it fires regardless of whether PLAYWRIGHT_OUTPUT_BASE_PATH or
    a --playwright-output override is ever misconfigured back into
    backend/governance/, so this specific bug cannot silently recur.
    """
    resolved = path.resolve()
    forbidden_root = REPO_BASE_PATH.resolve()
    if resolved == forbidden_root or forbidden_root in resolved.parents:
        raise RuntimeError(
            f"REFUSING TO WRITE PLAYWRIGHT CONTENT — resolved path\n"
            f"    {resolved}\n"
            f"  is inside backend/governance/ ({forbidden_root}).\n"
            f"  PLAYWRIGHT content must never live in backend/governance/ — see the\n"
            f"  STRUCTURAL LAW section in backend/CLAUDE.md and frontend/CLAUDE.md.\n"
            f"  Check config.py's PLAYWRIGHT_OUTPUT_BASE_PATH and/or the\n"
            f"  --playwright-output CLI argument."
        )


def _display_dest(dest: Path, base: Path, playwright_base: Path) -> str:
    """
    Show a write-plan destination relative to whichever root it actually
    belongs under, tagging frontend-rooted (PLAYWRIGHT) paths explicitly so
    a printed plan never silently implies everything lands under one tree.
    """
    try:
        return str(dest.relative_to(base))
    except ValueError:
        pass
    try:
        return f"[frontend/governance] {dest.relative_to(playwright_base)}"
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
PHASE_FOLDER_MAP = {
    "CORE":      "CORE",
    "DATA-DOM":  "DATA-DOM",
    "SVC-API":   "SVC-API",
    "DOC":       "DOC",
    "INT-C":     "INT-C",
    "INT-R":     "INT-R",
    "F1":        "F1",
    "F2":        "F2",
    "F3":        "F3",
    "F4":        "F4",
    "SEC":       "SEC",
    "ALIGN":     "ALIGN",
}

# v2 — split phase keys, one map per track (PROJECT-3-REGISTRY.md
# Section 2 — Phase Ownership Index). SEC/ALIGN are gone as bare keys;
# they are SEC-BE/ALIGN-BE (backend) or SEC-FE/ALIGN-FE (frontend) now.
PHASE_FOLDER_MAP_V2_BACKEND = {
    "CORE":      "CORE",
    "DATA-DOM":  "DATA-DOM",
    "SVC-API":   "SVC-API",
    "DOC":       "DOC",
    "INT-C":     "INT-C",
    "INT-R":     "INT-R",
    "SEC-BE":    "SEC-BE",
    "ALIGN-BE":  "ALIGN-BE",
}

PHASE_FOLDER_MAP_V2_FRONTEND = {
    "F1":        "F1",
    "F2":        "F2",
    "F3":        "F3",
    "F4":        "F4",
    "SEC-FE":    "SEC-FE",
    "ALIGN-FE":  "ALIGN-FE",
}


def stage2_split_execution(mod: str, version: int, state: dict, plan: dict | None, base: Path = None, dry_run: bool = False) -> bool:
    """Split execution-plan.md into PHASE / SUB / API / XM package files."""
    if base is None:
        base = get_module_version_path(mod, version)
    exec_path = base / "P3" / "execution-plan.md"
    pkg_root = base / "packages" / "execution"

    print()
    print("═" * 70)
    print(f"  STAGE 2 — Split execution-plan.md")
    print(f"  Module : {mod}  (v{version})")
    print("═" * 70)
    print()

    if not exec_path.exists():
        print("  — execution-plan.md not found. Skipping Stage 2.\n")
        return True  # not a failure — just nothing to do

    result = parse_file(exec_path)
    if result.errors:
        print("  ✗ Structural errors present. Re-run Stage 1 to see details.\n")
        return False

    phases = find_by_kind(result.root_blocks, "phase")
    write_plan = []

    for phase in phases:
        folder_name = PHASE_FOLDER_MAP.get(phase.marker_id, phase.marker_id)
        folder = pkg_root / folder_name

        sub_blocks = [c for c in phase.children if c.kind == "sub"]
        api_count = len([a for a in flatten([phase]) if a.kind == "api"])
        xm_count  = len([x for x in flatten([phase]) if x.kind == "xm"])

        if sub_blocks:
            # ── Preamble: content between PHASE:START and first SUB:START ──
            # This content (intro text, tables, strategy notes) belongs to
            # the Phase but sits outside any SUB — must NOT be lost.
            preamble = _preamble_content(phase, result.raw_lines)
            header_filename = _safe_filename(f"{phase.marker_id}-HEADER") if preamble else None

            if preamble:
                write_plan.append({
                    "dest": folder / header_filename,
                    "content": preamble,
                    "header": f"<!-- Source: PHASE:{phase.marker_id} / PREAMBLE (before first SUB) -->",
                    "note": "phase-level content (tables, strategy, intro)",
                })

            # ── One file per SUB — with context reference to HEADER ──
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
            # Whole phase as one file — no SUBs present
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
        print(f"    {w['dest'].relative_to(base)}{extra}")
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

    print(f"\n  ✓ {len(write_plan)} files written to packages/execution/")
    mark_stage_complete(state, 2)
    save_state(mod, version, state, base)
    print("  ✓ Stage 2 complete.\n")
    return True


# ─────────────────────────────────────────────────────────────────────────────
# STAGE 3 — Split test-plan.md
# ─────────────────────────────────────────────────────────────────────────────

def stage3_split_test(mod: str, version: int, state: dict, plan: dict | None,
                       base: Path = None, playwright_base: Path = None,
                       dry_run: bool = False) -> bool:
    """
    Split test-plan.md into MARK / SUB / TC package files.

    JUNIT-type MARK content is written under `base` (backend/governance/),
    same as always. PLAYWRIGHT-type MARK content is written under a SEPARATE
    root, `playwright_base` — defaults to
    config.get_playwright_module_version_path(mod, version), i.e.
    frontend/governance/modules/<MOD>/ — and is NEVER written under `base`.
    See STRUCTURAL LAW in backend/CLAUDE.md / frontend/CLAUDE.md.
    """
    if base is None:
        base = get_module_version_path(mod, version)
    if playwright_base is None:
        playwright_base = get_playwright_module_version_path(mod, version)
    test_path = base / "P3_5" / "test-plan.md"
    pkg_root = base / "packages" / "test"
    playwright_pkg_root = playwright_base / "packages" / "test"

    # Guard early, before any parsing/printing — refuse to proceed at all if
    # the PLAYWRIGHT destination has been misconfigured back into backend/governance/.
    _guard_playwright_path(playwright_pkg_root)

    print()
    print("═" * 70)
    print(f"  STAGE 3 — Split test-plan.md")
    print(f"  Module : {mod}  (v{version})")
    print(f"  JUNIT output      : {pkg_root}")
    print(f"  PLAYWRIGHT output : {playwright_pkg_root}")
    print("═" * 70)
    print()

    if not test_path.exists():
        print("  — test-plan.md not found. Skipping Stage 3.\n")
        return True

    result = parse_file(test_path)
    if result.errors:
        print("  ✗ Structural errors present. Re-run Stage 1 to see details.\n")
        return False

    marks = find_by_kind(result.root_blocks, "mark")
    write_plan = []

    for mark in marks:
        is_playwright = (mark.marker_id == "PLAYWRIGHT")
        mark_pkg_root = playwright_pkg_root if is_playwright else pkg_root
        folder = mark_pkg_root / mark.marker_id

        sub_blocks = [c for c in mark.children if c.kind == "sub"]
        tc_count_mark = len([t for t in flatten([mark]) if t.kind == "tc"])

        if sub_blocks:
            # ── Preamble: any content before first SUB inside this MARK ──
            preamble = _preamble_content(mark, result.raw_lines)
            header_filename = _safe_filename(f"{mark.marker_id}-HEADER") if preamble else None

            if preamble:
                write_plan.append({
                    "dest": folder / header_filename,
                    "content": preamble,
                    "header": f"<!-- Source: MARK:{mark.marker_id} / PREAMBLE (before first SUB) -->",
                    "note": "mark-level content before first SUB",
                })

            for sub in sub_blocks:
                fname = _safe_filename(sub.marker_id)
                sub_tc_count = len([t for t in flatten([sub]) if t.kind == "tc"])
                context_ref = (
                    f"<!-- Context: see {header_filename} for mark-level "
                    f"intro and mandatory scenarios -->"
                    if header_filename else ""
                )
                header_line = f"<!-- Source: MARK:{mark.marker_id} / SUB:{sub.marker_id} -->"
                if context_ref:
                    header_line += f"\n{context_ref}"
                write_plan.append({
                    "dest": folder / fname,
                    "block": sub,
                    "header": header_line,
                    "note": f"{sub_tc_count} TC(s) embedded",
                })
        else:
            fname = _safe_filename(mark.marker_id)
            write_plan.append({
                "dest": folder / fname,
                "block": mark,
                "header": f"<!-- Source: MARK:{mark.marker_id} -->",
                "note": f"{tc_count_mark} TC(s) embedded",
            })

    print(f"  Files to write: {len(write_plan)}")
    for w in write_plan[:15]:
        extra = f"  ({w['note']})" if w.get("note") else ""
        print(f"    {_display_dest(w['dest'], base, playwright_base)}{extra}")
    if len(write_plan) > 15:
        print(f"    ... and {len(write_plan) - 15} more")
    print()

    if dry_run:
        print("  — DRY RUN: no files written, no state changed.\n")
        return True

    if not confirm("  Approve Stage 3 — write these files?"):
        print("\n  Stage 3 cancelled — no files written.\n")
        return False

    # Re-guard immediately before writing, in case anything upstream (a
    # future code change, a bad --playwright-output value) let a PLAYWRIGHT
    # destination slip through — belt-and-suspenders around REPO_BASE_PATH.
    for w in write_plan:
        if playwright_pkg_root in w["dest"].parents or w["dest"].parent == playwright_pkg_root:
            _guard_playwright_path(w["dest"])

    _execute_write_plan(write_plan)

    print(f"\n  ✓ {len(write_plan)} files written "
          f"({pkg_root} for JUNIT, {playwright_pkg_root} for PLAYWRIGHT)")
    mark_stage_complete(state, 3)
    save_state(mod, version, state, base)
    print("  ✓ Stage 3 complete.\n")
    return True


# ─────────────────────────────────────────────────────────────────────────────
# STAGE 2/3 — v2 MODEL — Split backend-/frontend-execution-plan.md
# and backend-/frontend-test-plan.md (no MARK level)
# ─────────────────────────────────────────────────────────────────────────────

def stage2_split_execution_v2(mod: str, version: int, state: dict, plan: dict | None,
                                track: str, base: Path = None,
                                frontend_base: Path = None, dry_run: bool = False) -> bool:
    """
    v2 model equivalent of stage2_split_execution — splits
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
        frontend_base = get_playwright_module_version_path(mod, version)

    if track == "backend":
        src_path = _v2_artifact_path(mod, base, frontend_base, "P3_1")
        pkg_root = base / "packages" / "backend-execution"
        phase_map = PHASE_FOLDER_MAP_V2_BACKEND
        artifact_label = src_path.name
    elif track == "frontend":
        src_path = _v2_artifact_path(mod, base, frontend_base, "P3_2")
        pkg_root = frontend_base / "packages" / "frontend-execution"
        phase_map = PHASE_FOLDER_MAP_V2_FRONTEND
        artifact_label = src_path.name
    else:
        print(f"  ERROR: unknown track '{track}' — must be 'backend' or 'frontend'.")
        return False

    print()
    print("═" * 70)
    print(f"  STAGE 2 (v2) — Split {artifact_label}")
    print(f"  Module : {mod}  (v{version})   Track: {track}")
    print("═" * 70)
    print()

    if not src_path.exists():
        print(f"  — {artifact_label} not found at {src_path}. Skipping Stage 2.\n")
        return True  # not a failure — just nothing to do yet

    result = parse_file(src_path, governance_model=GOVERNANCE_MODEL_V2)
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
            print(f"  ⚠ PHASE:{phase.marker_id} not in the {track} phase map for v2 — "
                  f"skipped. (Expected keys: {list(phase_map.keys())}. If this phase "
                  f"legitimately belongs to the OTHER track, that's a generation-time "
                  f"bug in the source file, not a splitter bug.)")
            continue
        folder = pkg_root / folder_name

        sub_blocks = [c for c in phase.children if c.kind == "sub"]
        api_count = len([a for a in flatten([phase]) if a.kind == "api"])
        xm_count  = len([x for x in flatten([phase]) if x.kind == "xm"])
        if track == "frontend" and xm_count:
            print(f"  ⚠ PHASE:{phase.marker_id} has {xm_count} XM marker(s) (backend track only) — "
                  f"XM-IDs are backend-only (CORE-5 RULE-6); found while splitting the frontend "
                  f"track. Writing anyway — this likely indicates a generation-time bug in the "
                  f"source file, not a splitter bug.")

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
                if track == "frontend" and sub_xm_count:
                    print(f"  ⚠ PHASE:{phase.marker_id} / SUB:{sub.marker_id} has {sub_xm_count} "
                          f"XM marker(s) (backend track only) — same CORE-5 RULE-6 concern as above.")
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

    if not confirm("  Approve Stage 2 (v2) — write these files?"):
        print("\n  Stage 2 cancelled — no files written.\n")
        return False

    _execute_write_plan(write_plan)

    print(f"\n  ✓ {len(write_plan)} files written to packages/{track}-execution/")
    mark_stage_complete(state, 2)
    save_state(mod, version, state, base)
    print("  ✓ Stage 2 (v2) complete.\n")
    return True


def stage3_split_test_v2(mod: str, version: int, state: dict, plan: dict | None,
                           track: str, base: Path = None,
                           frontend_base: Path = None, dry_run: bool = False) -> bool:
    """
    v2 model equivalent of stage3_split_test — splits
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
        frontend_base = get_playwright_module_version_path(mod, version)

    if track == "backend":
        src_path = _v2_artifact_path(mod, base, frontend_base, "P3_5_BE")
        pkg_root = base / "packages" / "backend-test"
        expected_phase_key = "TEST-PLAN-BE"
        artifact_label = src_path.name
    elif track == "frontend":
        src_path = _v2_artifact_path(mod, base, frontend_base, "P3_5_FE")
        pkg_root = frontend_base / "packages" / "frontend-test"
        expected_phase_key = "TEST-PLAN-FE"
        artifact_label = src_path.name
    else:
        print(f"  ERROR: unknown track '{track}' — must be 'backend' or 'frontend'.")
        return False

    print()
    print("═" * 70)
    print(f"  STAGE 3 (v2) — Split {artifact_label}")
    print(f"  Module : {mod}  (v{version})   Track: {track}")
    print("═" * 70)
    print()

    if not src_path.exists():
        print(f"  — {artifact_label} not found at {src_path}. Skipping Stage 3.\n")
        return True

    result = parse_file(src_path, governance_model=GOVERNANCE_MODEL_V2)
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
        folder = pkg_root  # v2 test-plan has exactly one top-level PHASE per file

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

    if not confirm("  Approve Stage 3 (v2) — write these files?"):
        print("\n  Stage 3 cancelled — no files written.\n")
        return False

    _execute_write_plan(write_plan)

    print(f"\n  ✓ {len(write_plan)} files written to packages/{track}-test/")
    mark_stage_complete(state, 3)
    save_state(mod, version, state, base)
    print("  ✓ Stage 3 (v2) complete.\n")
    return True


# ─────────────────────────────────────────────────────────────────────────────
# STAGE 4 — Generate Index Files
# ─────────────────────────────────────────────────────────────────────────────

def stage4_generate_index(mod: str, version: int, state: dict, base: Path = None) -> bool:
    """Generate index.md in every package folder listing its contents."""
    if base is None:
        base = get_module_version_path(mod, version)
    pkg_root = base / "packages"

    print()
    print("═" * 70)
    print(f"  STAGE 4 — Generate Index Files")
    print(f"  Module : {mod}  (v{version})")
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
        print(f"    {folder.relative_to(base)}  ({len(files)} file(s))")
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
        # header line + following blank line
        rest = lines[1:]
        if rest and rest[0].strip() == "":
            rest = rest[1:]
        return "\n".join(rest)
    return file_text


def stage5_verify(mod: str, version: int, state: dict, base: Path = None) -> bool:
    """
    Cross-check completeness AND content integrity:
      1. Every atomic marker (API/XM/TC) in source is found EMBEDDED inside
         some package file (grouped at SUB/PHASE/MARK level — not a 1:1
         file mapping, per Section 6.7.5).
      2. Every atomic block's content, as it appears inside its package
         file, hashes identically to the same block in the archived source
         — guarantees copy/paste only, zero drift, even after grouping.
    """
    if base is None:
        base = get_module_version_path(mod, version)
    exec_path = base / "P3" / "execution-plan.md"
    test_path = base / "P3_5" / "test-plan.md"
    pkg_root = base / "packages"

    print()
    print("═" * 70)
    print(f"  STAGE 5 — Verify Completeness & Integrity")
    print(f"  Module : {mod}  (v{version})")
    print("═" * 70)
    print()

    missing_issues = []
    hash_issues = []
    checked_count = 0

    def _find_marker_in_files(kind: str, marker_id: str, pkg_subroot: Path):
        """
        Search all package .md files under pkg_subroot for a given
        atomic marker, and re-parse that file to extract the matching
        block's content for hash comparison. Returns (file, block) or
        (None, None) if not found in any package file.
        """
        from config import MARKERS
        pattern_start = f"<!-- {kind.upper()}:{marker_id}:START -->"
        for f in pkg_subroot.rglob("*.md"):
            if f.name == "index.md":
                continue
            text = f.read_text(encoding="utf-8")
            if pattern_start in text:
                # Re-parse this package file to extract the exact block content
                sub_result = parse_file(f)
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
                    f"content MISMATCH inside {pkg_file.relative_to(base)}\n"
                    f"      source hash : {source_hash[:16]}...\n"
                    f"      package hash: {pkg_hash[:16]}..."
                )

    # ── Verify execution-plan.md ──────────────────────────────────────────────
    if exec_path.exists():
        result = parse_file(exec_path)
        apis = find_by_kind(result.root_blocks, "api")
        xms = find_by_kind(result.root_blocks, "xm")

        _verify_blocks(apis, "execution-plan.md", pkg_root / "execution")
        _verify_blocks(xms, "execution-plan.md", pkg_root / "execution")

        print(f"  execution-plan.md : {len(apis)} APIs, {len(xms)} XMs checked")

    # ── Verify test-plan.md ───────────────────────────────────────────────────
    if test_path.exists():
        result = parse_file(test_path)
        tcs = find_by_kind(result.root_blocks, "tc")

        _verify_blocks(tcs, "test-plan.md", pkg_root / "test")

        print(f"  test-plan.md       : {len(tcs)} TCs checked")

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
        print("  Re-run Stage 2/3 to regenerate, then Stage 5 again.")
        return False

    print("  ✓ All atomic elements (API/XM/TC) have matching package files.")
    print("  ✓ Content hash verified for every element — zero drift from archived source.")
    print("  ✓ No content loss detected.")
    mark_stage_complete(state, 5)
    save_state(mod, version, state, base)
    print()
    print("  ✓ Stage 5 complete — splitting verified.")
    print()
    print(f"  Module [{mod}] v{version} fully packaged. Ready for downstream agents")
    print(f"  (Claude Code / Copilot / Codex) to consume individual package files.")
    print()
    return True


def stage5_verify_v2(mod: str, version: int, state: dict, track: str,
                       base: Path = None, frontend_base: Path = None) -> bool:
    """
    v2 model equivalent of stage5_verify — same completeness + content-hash
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
        frontend_base = get_playwright_module_version_path(mod, version)

    if track == "backend":
        exec_path = _v2_artifact_path(mod, base, frontend_base, "P3_1")
        test_path = _v2_artifact_path(mod, base, frontend_base, "P3_5_BE")
        pkg_root = base / "packages"
        exec_label, test_label = exec_path.name, test_path.name
        exec_pkg_dir, test_pkg_dir = "backend-execution", "backend-test"
        display_base = base
    elif track == "frontend":
        exec_path = _v2_artifact_path(mod, base, frontend_base, "P3_2")
        test_path = _v2_artifact_path(mod, base, frontend_base, "P3_5_FE")
        pkg_root = frontend_base / "packages"
        exec_label, test_label = exec_path.name, test_path.name
        exec_pkg_dir, test_pkg_dir = "frontend-execution", "frontend-test"
        display_base = frontend_base
    else:
        print(f"  ERROR: unknown track '{track}' — must be 'backend' or 'frontend'.")
        return False

    print()
    print("═" * 70)
    print(f"  STAGE 5 (v2) — Verify Completeness & Integrity")
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
                sub_result = parse_file(f, governance_model=GOVERNANCE_MODEL_V2)
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
        result = parse_file(exec_path, governance_model=GOVERNANCE_MODEL_V2)
        apis = find_by_kind(result.root_blocks, "api")
        xms = find_by_kind(result.root_blocks, "xm")

        _verify_blocks(apis, exec_label, pkg_root / exec_pkg_dir)
        _verify_blocks(xms, exec_label, pkg_root / exec_pkg_dir)

        print(f"  {exec_label} : {len(apis)} APIs, {len(xms)} XMs checked")

    if test_path.exists():
        result = parse_file(test_path, governance_model=GOVERNANCE_MODEL_V2)
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
    print(f"  ✓ Stage 5 (v2) complete — {track} splitting verified.")
    print()
    print(f"  Module [{mod}] v{version} ({track} track) fully packaged.")
    print(f"  Ready for downstream agents (Claude Code / Copilot / Codex).")
    print()
    return True


# ─────────────────────────────────────────────────────────────────────────────
# ORCHESTRATION
# ─────────────────────────────────────────────────────────────────────────────

def run_stage(stage: int, mod: str, version: int, state: dict, plan: dict | None,
              base: Path = None, playwright_base: Path = None, dry_run: bool = False,
              track: str = None) -> tuple[bool, dict | None]:
    """
    Run a single stage. Returns (success, plan_for_next_stage).

    track: None for v1 modules (ignored — v1 has no track concept).
      "backend" or "frontend" for v2 modules — REQUIRED for v2, since
      Stage 2/3 need to know which artifact pair to split. See the
      module docstring's v2.1 UPDATE section.
    """
    model = get_governance_model(mod)

    if stage == 1:
        if model == GOVERNANCE_MODEL_V2:
            result_plan = stage1_parse_and_plan_v2(mod, version, state, track, base, playwright_base)
        else:
            result_plan = stage1_parse_and_plan(mod, version, state, base)
        return (result_plan is not None), result_plan
    elif stage == 2:
        if model == GOVERNANCE_MODEL_V2:
            ok = stage2_split_execution_v2(mod, version, state, plan, track, base, playwright_base, dry_run=dry_run)
        else:
            ok = stage2_split_execution(mod, version, state, plan, base, dry_run=dry_run)
        return ok, plan
    elif stage == 3:
        if model == GOVERNANCE_MODEL_V2:
            ok = stage3_split_test_v2(mod, version, state, plan, track, base, playwright_base, dry_run=dry_run)
        else:
            ok = stage3_split_test(mod, version, state, plan, base, playwright_base, dry_run=dry_run)
        return ok, plan
    elif stage == 4:
        ok = stage4_generate_index(mod, version, state, base)
        return ok, plan
    elif stage == 5:
        if model == GOVERNANCE_MODEL_V2:
            ok = stage5_verify_v2(mod, version, state, track, base, playwright_base)
        else:
            ok = stage5_verify(mod, version, state, base)
        return ok, plan
    else:
        print(f"  Unknown stage: {stage}")
        return False, plan


def main():
    parser = argparse.ArgumentParser(
        description="Agent 3 — Split governance artifacts using Marker Protocol (staged, approve-gated)."
    )
    parser.add_argument("--module", "-m", required=True, help="Module code (e.g. ORG)")
    parser.add_argument("--version", "-v", type=int, default=None,
                        help="Module version (default: current version from registry)")
    parser.add_argument("--output", "-o", default=None,
                        help="Override output base path for the module (e.g. /path/to/modules/ORG). "
                             "Affects execution/ output and JUNIT test output. Does NOT affect "
                             "PLAYWRIGHT output — use --playwright-output for that, separately.")
    parser.add_argument("--playwright-output", default=None,
                        help="Override output base path specifically for PLAYWRIGHT test-phase "
                             "content (e.g. /path/to/frontend/governance/modules/ORG). Independent "
                             "of --output. Defaults to config.PLAYWRIGHT_OUTPUT_BASE_PATH's module "
                             "path (frontend/governance/modules/<MOD>/) — never backend/governance/.")
    parser.add_argument("--stage", "-s", type=int, choices=[1, 2, 3, 4, 5],
                        help="Run a single stage only.")
    parser.add_argument("--track", choices=["backend", "frontend"], default=None,
                        help="REQUIRED for v2-model modules (any module registered "
                             "from now on) — picks which artifact pair Stage 2/3 "
                             "split: 'backend' (backend-execution-plan.md + "
                             "backend-test-plan.md) or 'frontend' "
                             "(frontend-execution-plan.md + frontend-test-plan.md). "
                             "REJECTED (hard error) for v1-model modules "
                             "(ORG, NOTIFICATION, FILESVC), which have no track split.")
    parser.add_argument("--resume", "-r", action="store_true",
                        help="Resume from the next incomplete stage.")
    parser.add_argument("--status", action="store_true",
                        help="Show stage completion status and exit.")
    parser.add_argument("--dry-run", action="store_true",
                        help="Print the resolved write plan for Stage 2/3 (including which repo "
                             "root each file would land under) without writing anything, "
                             "prompting, or marking any stage complete. Safe to run any time.")

    args = parser.parse_args()

    try:
        mod = validate_module(args.module)
    except ValueError as e:
        print(f"\n  ERROR: {e}\n")
        sys.exit(1)

    model = get_governance_model(mod)
    if model == GOVERNANCE_MODEL_V2 and args.track is None:
        print(f"\n  ERROR: module '{mod}' uses governance_model='v2' — "
              f"--track backend|frontend is required.\n"
              f"  (v1 modules like ORG/NOTIFICATION/FILESVC don't need it — "
              f"only new modules registered under v2 do.)\n")
        sys.exit(1)
    if model == GOVERNANCE_MODEL_V1 and args.track is not None:
        print(f"\n  ERROR: --track '{args.track}' was passed but module '{mod}' is "
              f"governance_model='v1' — v1 has no backend/frontend track split; it "
              f"always runs the single combined stage2/stage3 (no separate --track step).\n")
        sys.exit(1)

    # Determine version
    if args.version:
        version = args.version
    else:
        registry = load_modules_registry()
        version = registry.get("modules", {}).get(mod, {}).get("current_version") or 1

    override_base = Path(args.output).resolve() if args.output else None
    playwright_override_base = Path(args.playwright_output).resolve() if args.playwright_output else None

    if args.status:
        print_status(mod, version, override_base)
        sys.exit(0)

    base = override_base if override_base else get_module_version_path(mod, version)
    if not base.exists():
        print(f"\n  ERROR: Module structure not found: {base}")
        print(f"  Run agent1_create_structure.py --module {mod} first, or check --output path.\n")
        sys.exit(1)

    state = load_state(mod, version, base)

    # ── Single stage mode ─────────────────────────────────────────────────────
    if args.stage:
        plan = None
        if args.stage > 1 and 1 not in state["stages_completed"]:
            print(f"\n  WARNING: Stage 1 has not been completed yet for this module.")
            if not confirm("  Run Stage 1 first?"):
                print("\n  Cancelled.\n")
                sys.exit(0)
            ok, plan = run_stage(1, mod, version, state, None, base, track=args.track)
            if not ok:
                sys.exit(1)
        ok, _ = run_stage(args.stage, mod, version, state, plan, base,
                           playwright_override_base, dry_run=args.dry_run, track=args.track)
        sys.exit(0 if ok else 1)

    # ── Resume mode ───────────────────────────────────────────────────────────
    if args.resume:
        start_stage = 1
        for s in range(1, 6):
            if s in state["stages_completed"]:
                start_stage = s + 1
        if start_stage > 5:
            print(f"\n  All stages already complete for [{mod}] v{version}.")
            print_status(mod, version, base)
            sys.exit(0)
        print(f"\n  Resuming from Stage {start_stage}.\n")
        stages_to_run = range(start_stage, 6)
    else:
        # ── Full run mode — all 5 stages in sequence ──────────────────────────
        stages_to_run = range(1, 6)

    plan = None
    for stage in stages_to_run:
        ok, plan = run_stage(stage, mod, version, state, plan, base,
                              playwright_override_base, dry_run=args.dry_run, track=args.track)
        if not ok:
            print(f"  Stopped at Stage {stage}. Re-run with --resume to continue once fixed.\n")
            sys.exit(1)

    print("═" * 70)
    print(f"  ALL STAGES COMPLETE — Module [{mod}] v{version}")
    print("═" * 70)
    print()


if __name__ == "__main__":
    main()
