"""
ERP Governance Tools — Agent 3: Backend Artifact Splitter
============================================================
Reads Marker Protocol (PROJECT-3-REGISTRY.md Section 5.7) from
backend-execution-plan.md and backend-test-plan.md, then splits them
into addressable package files.

Usage:
    python agent3_splitter.py --module MODCODE
    python agent3_splitter.py --module MODCODE --stage 1
    python agent3_splitter.py --module MODCODE --resume
    python agent3_splitter.py --module MODCODE --status

This tool has no representation of "frontend" anywhere, and no
--track flag — it does exactly one job: split backend-execution-plan.md
and backend-test-plan.md. There is no P4/audit concept anywhere.

Stages:
    1. Parse & Plan          — read markers, validate structure, show plan
    2. Split execution-plan  — write PHASE/SUB/API/XM package files
    3. Split test-plan       — write PHASE/SUB/TC package files (no
                                MARK level — this file is JUnit-only by
                                construction)
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
    get_module_version_path,
    validate_module,
    load_modules_registry,
)
from marker_parser import parse_file, flatten, find_by_kind, MarkerBlock, ParseResult

STAGE_NAMES = {
    1: "Parse & Plan",
    2: "Split backend-execution-plan.md",
    3: "Split backend-test-plan.md",
    4: "Generate Index Files",
    5: "Verify Completeness",
}

PHASE_FOLDER_MAP = {
    "CORE":      "CORE",
    "DATA-DOM":  "DATA-DOM",
    "SVC-API":   "SVC-API",
    "DOC":       "DOC",
    "INT-C":     "INT-C",
    "INT-R":     "INT-R",
    "SEC-BE":    "SEC-BE",
    "ALIGN-BE":  "ALIGN-BE",
}

# ─────────────────────────────────────────────────────────────────────────────
# STATE
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


def print_status(mod: str, version: int, base: Path = None):
    state = load_state(mod, version, base)
    print()
    print("═" * 62)
    print(f"  AGENT 3 — Status")
    print(f"  Module  : {mod}  (v{version})")
    print("═" * 62)
    for stage_num, name in STAGE_NAMES.items():
        done = stage_num in state.get("stages_completed", [])
        status = "✓ DONE" if done else "— pending"
        print(f"  Stage {stage_num} — {name:<32} {status}")
    if state.get("stages", {}).get("5"):
        print()
        print(f"  Last run: {state['stages']['5']['completed_at']}")
    print()


def confirm(prompt: str = "  Proceed?") -> bool:
    answer = input(f"{prompt} [y/N]: ").strip().lower()
    return answer == "y"


# ─────────────────────────────────────────────────────────────────────────────
# WRITE HELPERS
# ─────────────────────────────────────────────────────────────────────────────

def _write_block(path: Path, block: "MarkerBlock", header: str = ""):
    path.parent.mkdir(parents=True, exist_ok=True)
    text = (header + "\n\n") if header else ""
    text += block.content
    path.write_text(text, encoding="utf-8")


def _write_content(path: Path, content: str, header: str = ""):
    path.parent.mkdir(parents=True, exist_ok=True)
    text = (header + "\n\n") if header else ""
    text += content
    path.write_text(text, encoding="utf-8")


def _execute_write_plan(write_plan: list[dict]):
    for w in write_plan:
        if "block" in w:
            _write_block(w["dest"], w["block"], w.get("header", ""))
        else:
            _write_content(w["dest"], w["content"], w.get("header", ""))


def _safe_filename(marker_id: str) -> str:
    return marker_id.strip().replace(" ", "-") + ".md"


def _preamble_content(block: "MarkerBlock", raw_lines: list[str]) -> str:
    """
    Extract content between a container's START marker and its first
    child SUB — the 'preamble' that belongs to the container but is
    outside any SUB. Returns empty string if no preamble exists.
    """
    children_with_sub = [c for c in block.children if c.kind == "sub"]
    if not children_with_sub:
        return ""
    first_child_start = children_with_sub[0].start_line
    preamble_lines = raw_lines[block.start_line: first_child_start - 1]
    return "".join(preamble_lines).strip()


# ─────────────────────────────────────────────────────────────────────────────
# STAGE 1 — Parse & Plan
# ─────────────────────────────────────────────────────────────────────────────

def stage1_parse_and_plan(mod: str, version: int, state: dict, base: Path = None) -> dict | None:
    """
    Parses backend-execution-plan.md + backend-test-plan.md, validates
    marker structure, and shows a generation plan before anything is written.
    """
    if base is None:
        base = get_module_version_path(mod, version)

    exec_path = base / "P3_1" / "backend-execution-plan.md"
    test_path = base / "P3_5_BE" / "backend-test-plan.md"

    print()
    print("═" * 70)
    print(f"  STAGE 1 — Parse & Plan")
    print(f"  Module : {mod}  (v{version})")
    print("═" * 70)
    print()

    exec_result: ParseResult | None = None
    test_result: ParseResult | None = None

    if exec_path.exists():
        exec_result = parse_file(exec_path)
        print(f"  ✓ Read backend-execution-plan.md  ({exec_result.total_lines} lines)")
    else:
        print(f"  ⚠ backend-execution-plan.md not found at {exec_path}")
        print(f"    Run agent2_archive.py first.")

    if test_path.exists():
        test_result = parse_file(test_path)
        print(f"  ✓ Read backend-test-plan.md      ({test_result.total_lines} lines)")
    else:
        print(f"  — backend-test-plan.md not found — will skip Stage 3 (acceptable if not generated yet)")

    if not exec_result and not test_result:
        print()
        print(f"  ERROR: Neither backend-execution-plan.md nor backend-test-plan.md found. Nothing to split.")
        return None

    all_errors = []
    if exec_result:
        all_errors += [("backend-execution-plan.md", e) for e in exec_result.errors]
    if test_result:
        all_errors += [("backend-test-plan.md", e) for e in test_result.errors]

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
        # Relative to REPO_BASE_PATH, never absolute — see config.py's
        # build_manifest() note; this persists into _agent3-state.json.
        "exec_path": str(exec_path.relative_to(REPO_BASE_PATH)) if exec_result else None,
        "test_path": str(test_path.relative_to(REPO_BASE_PATH)) if test_result else None,
        "exec_summary": {},
        "test_summary": {},
    }

    if exec_result:
        phases = find_by_kind(exec_result.root_blocks, "phase")
        apis = find_by_kind(exec_result.root_blocks, "api")
        xms = find_by_kind(exec_result.root_blocks, "xm")
        subs = find_by_kind(exec_result.root_blocks, "sub")

        print()
        print(f"  ── backend-execution-plan.md plan ──────────────────────")
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

        plan["exec_summary"] = {"phases": len(phases), "apis": len(apis), "xms": len(xms), "subs": len(subs)}

    if test_result:
        tcs = find_by_kind(test_result.root_blocks, "tc")
        subs_t = find_by_kind(test_result.root_blocks, "sub")
        phases_t = find_by_kind(test_result.root_blocks, "phase")

        print()
        print(f"  ── backend-test-plan.md plan ───────────────────────────")
        for p in phases_t:
            sub_count = len([s for s in p.children if s.kind == "sub"])
            tc_count = len([t for t in flatten([p]) if t.kind == "tc"])
            extra = f", {sub_count} sub-section(s)" if sub_count else " (no SUB — below threshold)"
            print(f"      - PHASE:{p.marker_id:<12} → {tc_count} TC(s){extra}")
        print(f"    Total TC atomic files : {len(tcs)}")

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
            for phase_id, orphans in orphan_warnings:
                ids = ", ".join(t.marker_id for t in orphans)
                print(f"    PHASE:{phase_id} → {len(orphans)} orphan TC(s): {ids}")
            print()

        plan["test_summary"] = {"phases": len(phases_t), "tcs": len(tcs), "subs": len(subs_t)}

    total_files = (
        plan["exec_summary"].get("apis", 0) + plan["exec_summary"].get("xms", 0)
        + plan["exec_summary"].get("phases", 0)
        + plan["test_summary"].get("tcs", 0) + plan["test_summary"].get("phases", 0)
    )
    print()
    print(f"  Estimated package files to generate: ~{total_files}")
    print()

    if not confirm("  Approve Stage 1 plan and proceed?"):
        print("\n  Stage 1 cancelled — no files written.\n")
        return None

    state["exec_plan_path"] = plan["exec_path"]
    state["test_plan_path"] = plan["test_path"]
    mark_stage_complete(state, 1)
    save_state(mod, version, state, base)

    print("  ✓ Stage 1 complete.\n")
    return plan


# ─────────────────────────────────────────────────────────────────────────────
# STAGE 2 — Split backend-execution-plan.md
# ─────────────────────────────────────────────────────────────────────────────

def stage2_split_execution(mod: str, version: int, state: dict, plan: dict | None,
                             base: Path = None, dry_run: bool = False) -> bool:
    if base is None:
        base = get_module_version_path(mod, version)

    src_path = base / "P3_1" / "backend-execution-plan.md"
    pkg_root = base / "packages" / "backend-execution"

    print()
    print("═" * 70)
    print(f"  STAGE 2 — Split backend-execution-plan.md")
    print(f"  Module : {mod}  (v{version})")
    print("═" * 70)
    print()

    if not src_path.exists():
        print(f"  — backend-execution-plan.md not found at {src_path}. Skipping Stage 2.\n")
        if not dry_run:
            mark_stage_complete(state, 2)
            save_state(mod, version, state, base)
        return True

    result = parse_file(src_path)
    if result.errors:
        print("  ✗ Structural errors present:")
        for e in result.errors[:10]:
            print(f"    [{e.severity}] line {e.line}: {e.message}")
        print()
        return False

    phases = find_by_kind(result.root_blocks, "phase")
    write_plan = []

    for phase in phases:
        folder_name = PHASE_FOLDER_MAP.get(phase.marker_id)
        if folder_name is None:
            print(f"  ⚠ PHASE:{phase.marker_id} not in the backend phase map — skipped.")
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
                    f"<!-- Context: see {header_filename} for phase-level strategy, registry table, and intro -->"
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

    print(f"\n  ✓ {len(write_plan)} files written to packages/backend-execution/")
    mark_stage_complete(state, 2)
    save_state(mod, version, state, base)
    print("  ✓ Stage 2 complete.\n")
    return True


# ─────────────────────────────────────────────────────────────────────────────
# STAGE 3 — Split backend-test-plan.md
# ─────────────────────────────────────────────────────────────────────────────

def stage3_split_test(mod: str, version: int, state: dict, plan: dict | None,
                        base: Path = None, dry_run: bool = False) -> bool:
    if base is None:
        base = get_module_version_path(mod, version)

    src_path = base / "P3_5_BE" / "backend-test-plan.md"
    pkg_root = base / "packages" / "backend-test"
    expected_phase_key = "TEST-PLAN-BE"

    print()
    print("═" * 70)
    print(f"  STAGE 3 — Split backend-test-plan.md")
    print(f"  Module : {mod}  (v{version})")
    print("═" * 70)
    print()

    if not src_path.exists():
        print(f"  — backend-test-plan.md not found at {src_path}. Skipping Stage 3.\n")
        if not dry_run:
            mark_stage_complete(state, 3)
            save_state(mod, version, state, base)
        return True

    result = parse_file(src_path)
    if result.errors:
        print("  ✗ Structural errors present:")
        for e in result.errors[:10]:
            print(f"    [{e.severity}] line {e.line}: {e.message}")
        print()
        return False

    phases = find_by_kind(result.root_blocks, "phase")
    write_plan = []

    for phase in phases:
        if phase.marker_id != expected_phase_key:
            print(f"  ⚠ PHASE:{phase.marker_id} does not match the expected "
                  f"'{expected_phase_key}' — processing anyway, but this may "
                  f"indicate a generation-time naming issue.")
        folder = pkg_root

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
                    f"<!-- Context: see {header_filename} for phase-level intro and mandatory scenarios -->"
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
        print(f"    {w['dest'].relative_to(base)}{extra}")
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

    print(f"\n  ✓ {len(write_plan)} files written to packages/backend-test/")
    mark_stage_complete(state, 3)
    save_state(mod, version, state, base)
    print("  ✓ Stage 3 complete.\n")
    return True


# ─────────────────────────────────────────────────────────────────────────────
# STAGE 4 — Generate Index Files
# ─────────────────────────────────────────────────────────────────────────────

def stage4_generate_index(mod: str, version: int, state: dict, base: Path = None, dry_run: bool = False) -> bool:
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

    if dry_run:
        print("  — DRY RUN: no files written, no state changed.\n")
        return True

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
    import hashlib
    return hashlib.sha256(text.strip().encode("utf-8")).hexdigest()


def stage5_verify(mod: str, version: int, state: dict, base: Path = None) -> bool:
    if base is None:
        base = get_module_version_path(mod, version)

    exec_path = base / "P3_1" / "backend-execution-plan.md"
    test_path = base / "P3_5_BE" / "backend-test-plan.md"
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
        pattern_start = f"<!-- {kind.upper()}:{marker_id}:START -->"
        for f in pkg_subroot.rglob("*.md"):
            if f.name == "index.md":
                continue
            text = f.read_text(encoding="utf-8")
            if pattern_start in text:
                sub_result = parse_file(f)
                matches = [b for b in flatten(sub_result.root_blocks) if b.kind == kind and b.marker_id == marker_id]
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
                    f"content MISMATCH inside {pkg_file.relative_to(base)}"
                )

    if exec_path.exists():
        result = parse_file(exec_path)
        apis = find_by_kind(result.root_blocks, "api")
        xms = find_by_kind(result.root_blocks, "xm")
        _verify_blocks(apis, "backend-execution-plan.md", pkg_root / "backend-execution")
        _verify_blocks(xms, "backend-execution-plan.md", pkg_root / "backend-execution")
        print(f"  backend-execution-plan.md : {len(apis)} APIs, {len(xms)} XMs checked")

    if test_path.exists():
        result = parse_file(test_path)
        tcs = find_by_kind(result.root_blocks, "tc")
        _verify_blocks(tcs, "backend-test-plan.md", pkg_root / "backend-test")
        print(f"  backend-test-plan.md       : {len(tcs)} TCs checked")

    print(f"  Total atomic elements checked : {checked_count}")
    print()

    if missing_issues or hash_issues:
        if missing_issues:
            print(f"  ✗ {len(missing_issues)} MISSING file issue(s):")
            for i in missing_issues:
                print(f"    - {i}")
            print()
        if hash_issues:
            print(f"  ✗ {len(hash_issues)} CONTENT MISMATCH issue(s):")
            for i in hash_issues:
                print(f"    - {i}")
            print()
        print("  Re-run Stage 2/3 to regenerate, then Stage 5 again.")
        return False

    print("  ✓ All atomic elements (API/XM/TC) have matching package files.")
    print("  ✓ Content hash verified for every element — zero drift from archived source.")
    mark_stage_complete(state, 5)
    save_state(mod, version, state, base)
    print()
    print(f"  ✓ Stage 5 complete — backend splitting verified.")
    print(f"  Module [{mod}] v{version} fully packaged.")
    print()
    return True


# ─────────────────────────────────────────────────────────────────────────────
# ORCHESTRATION
# ─────────────────────────────────────────────────────────────────────────────

def run_stage(stage: int, mod: str, version: int, state: dict, plan: dict | None,
              base: Path = None, dry_run: bool = False) -> tuple[bool, dict | None]:
    if stage == 1:
        result_plan = stage1_parse_and_plan(mod, version, state, base)
        return (result_plan is not None), result_plan
    elif stage == 2:
        ok = stage2_split_execution(mod, version, state, plan, base, dry_run=dry_run)
        return ok, plan
    elif stage == 3:
        ok = stage3_split_test(mod, version, state, plan, base, dry_run=dry_run)
        return ok, plan
    elif stage == 4:
        ok = stage4_generate_index(mod, version, state, base, dry_run=dry_run)
        return ok, plan
    elif stage == 5:
        ok = stage5_verify(mod, version, state, base)
        return ok, plan
    else:
        print(f"  Unknown stage: {stage}")
        return False, plan


def main():
    parser = argparse.ArgumentParser(description="Split backend-execution-plan.md/backend-test-plan.md into package files.")
    parser.add_argument("--module", "-m", help="Module code (module code).")
    parser.add_argument("--stage", "-s", type=int, choices=[1, 2, 3, 4, 5], help="Run a single stage only.")
    parser.add_argument("--resume", "-r", action="store_true", help="Resume from the next incomplete stage.")
    parser.add_argument("--status", action="store_true", help="Show stage completion status and exit.")
    parser.add_argument("--dry-run", action="store_true", help="Show what would be written without writing anything.")
    parser.add_argument("--output", "-o", help="Override the module's base path (advanced/testing use).")
    parser.add_argument("--validate-markers", action="store_true",
                         help="Parse and validate marker structure only — no writes, no state changes, "
                              "no module registration required if used with --file.")
    parser.add_argument("--file", help="With --validate-markers: validate this specific file directly "
                                        "(e.g. before it has been archived for any module).")

    args = parser.parse_args()

    if args.validate_markers and args.file:
        target = Path(args.file)
        if not target.exists():
            print(f"\n  ERROR: file not found: {target}\n")
            sys.exit(1)
        result = parse_file(target)
        if result.errors:
            print(f"\n  ✗ STRUCTURAL ERRORS in {target.name}:\n")
            for e in result.errors:
                print(f"    [{e.severity}] line {e.line}: {e.message}")
            print()
            sys.exit(1)
        phases = find_by_kind(result.root_blocks, "phase")
        apis = find_by_kind(result.root_blocks, "api")
        xms = find_by_kind(result.root_blocks, "xm")
        tcs = find_by_kind(result.root_blocks, "tc")
        print(f"\n  ✓ {target.name}: marker structure valid — "
              f"{len(phases)} PHASE, {len(apis)} API, {len(xms)} XM, {len(tcs)} TC block(s).\n")
        sys.exit(0)

    if not args.module:
        print("\n  ERROR: --module is required (unless using --validate-markers --file).\n")
        sys.exit(1)

    try:
        mod = validate_module(args.module)
    except ValueError as e:
        print(f"\n  ERROR: {e}\n")
        sys.exit(1)

    registry = load_modules_registry()
    entry = registry.get("modules", {}).get(mod)
    version = (entry.get("current_version") if entry else None) or 1

    base = Path(args.output) if args.output else get_module_version_path(mod, version)

    if args.validate_markers:
        targets = [
            ("backend-execution-plan.md", base / "P3_1" / "backend-execution-plan.md"),
            ("backend-test-plan.md", base / "P3_5_BE" / "backend-test-plan.md"),
        ]
        checked = 0
        exit_code = 0
        for label, p in targets:
            if not p.exists():
                continue
            checked += 1
            result = parse_file(p)
            if result.errors:
                exit_code = 1
                print(f"\n  ✗ STRUCTURAL ERRORS in {label}:")
                for e in result.errors:
                    print(f"    [{e.severity}] line {e.line}: {e.message}")
            else:
                phases = find_by_kind(result.root_blocks, "phase")
                apis = find_by_kind(result.root_blocks, "api")
                xms = find_by_kind(result.root_blocks, "xm")
                tcs = find_by_kind(result.root_blocks, "tc")
                print(f"\n  ✓ {label}: marker structure valid — "
                      f"{len(phases)} PHASE, {len(apis)} API, {len(xms)} XM, {len(tcs)} TC block(s).")
        if checked == 0:
            print(f"\n  Nothing to validate — neither file found for module {mod} (v{version}).")
        print()
        sys.exit(exit_code)

    if args.status:
        print_status(mod, version, base)
        sys.exit(0)

    state = load_state(mod, version, base)

    if args.stage:
        plan = None
        if args.stage > 1 and (args.stage - 1) not in state.get("stages_completed", []):
            ok, plan = run_stage(1, mod, version, state, None, base)
            if not ok:
                sys.exit(1)
        ok, _ = run_stage(args.stage, mod, version, state, plan, base, dry_run=args.dry_run)
        sys.exit(0 if ok else 1)

    if args.resume:
        stages_to_run = [s for s in range(1, 6) if s not in state.get("stages_completed", [])]
        if not stages_to_run:
            print("\n  All stages already complete. Nothing to resume.\n")
            sys.exit(0)
    else:
        stages_to_run = list(range(1, 6))

    plan = None
    for stage in stages_to_run:
        ok, plan = run_stage(stage, mod, version, state, plan, base, dry_run=args.dry_run)
        if not ok:
            print(f"\n  Stopped at Stage {stage}. Fix the issue and re-run with --resume.\n")
            sys.exit(1)

    print("\n  ✓ All 5 stages complete.\n")


if __name__ == "__main__":
    main()
