"""
ERP Governance Tools — Agent 2: Artifact Archiver
==================================================
Copies generated artifacts from their source locations
into the canonical governance repository structure.

Usage (v1 modules — ORG, NOTIFICATION, FILESVC — UNCHANGED):
    python agent2_archive.py --module ORG --source ~/Desktop/ORG-artifacts
    python agent2_archive.py --module ORG --source ~/Desktop/ORG-artifacts --dry-run

Usage (v2 modules — any module registered from now on):
    python agent2_archive.py --module FIN --track backend --source ~/Desktop/FIN-backend-files
      (run again later, after real implementation + API docs + GATE:
      BACKEND MODULE COMPLETE)
    python agent2_archive.py --module FIN --track frontend --source ~/Desktop/FIN-frontend-files

What it does:
    1. Reads manifest.json for the module
    2. Scans source folder for known artifact filenames (per the
       module's governance_model — and, for v2, per --track)
    3. Shows a plan of what will be copied where
    4. Waits for approval
    5. Copies files to correct stage folders
    6. Updates manifest.json (archived flag — see v2.1 note below)

Handles:
    - Missing files        → warns but continues (partial archive)
    - Already archived     → asks before overwriting
    - Unknown module       → rejects with clear message
    - master-registry.md  → copied to repo root (shared)

=====================================================================
v2.1 UPDATE — DUAL GOVERNANCE MODEL SUPPORT
=====================================================================
--track is REQUIRED for v2-model modules, REJECTED (hard error, exit 1)
for v1 (same convention as agent1_create_structure.py's --frontend-only
and agent3_splitter.py's --track flag — see their docstrings).

--track backend  : scans/archives P0, P0_5, P1, P2, P2_5, P3_1,
                    P3_5_BE, P4_1 artifacts (this repo only)
--track frontend : scans/archives P3_2, P3_5_FE, P4_2 artifacts.
                    Under v2 these are natively frontend-generated
                    (see WORKSPACE-ARCHITECTURE-REFERENCE.md 11.3) —
                    this script's destination paths already resolve
                    there automatically via config.get_stage_path()'s
                    model-aware routing, same mechanism used by
                    agent3_splitter.py.

manifest.json's archived flag: v1 keeps the single "archived" boolean,
unchanged. v2 uses "archived_backend"/"archived_frontend" (see
config.build_manifest's v2 branch) — this script sets the correct one
based on --track.
=====================================================================
"""

import argparse
import json
import shutil
import sys
from pathlib import Path
from datetime import datetime

# ── Import shared config ──────────────────────────────────────────────────────
sys.path.insert(0, str(Path(__file__).parent))
from config import (
    REPO_BASE_PATH,
    KNOWN_MODULES,
    ARTIFACT_FILES,
    SHARED_FILES,
    GOVERNANCE_MODEL_V1,
    GOVERNANCE_MODEL_V2,
    get_governance_model,
    get_artifact_files,
    get_module_path,
    get_stage_path,
    validate_module,
    resolve_filename,
)

# ─────────────────────────────────────────────────────────────────────────────
# SCAN — Find artifacts in source folder
# ─────────────────────────────────────────────────────────────────────────────

def scan_source(mod: str, source_path: Path, track: str = None) -> list[dict]:
    """
    Scan source folder for known artifact files.
    Returns list of copy operations with status.

    track: None for v1 modules. "backend" or "frontend" for v2
      modules — selects which stage subset to scan (see module
      docstring's v2.1 UPDATE section).
    """
    operations = []
    model = get_governance_model(mod)
    artifact_files = get_artifact_files(mod)

    if model == GOVERNANCE_MODEL_V2:
        backend_stages = {"P0", "P0_5", "P1", "P2", "P2_5", "P3_1", "P3_5_BE", "P4_1"}
        frontend_stages = {"P3_2", "P3_5_FE", "P4_2"}
        stages_to_scan = backend_stages if track == "backend" else frontend_stages
    else:
        stages_to_scan = set(artifact_files.keys())

    # Per-stage artifacts
    for stage, templates in artifact_files.items():
        if stage not in stages_to_scan:
            continue
        dest_dir = get_stage_path(mod, stage)
        for template in templates:
            filename = resolve_filename(template, mod)
            src = source_path / filename
            dst = dest_dir / filename
            operations.append({
                "stage":    stage,
                "filename": filename,
                "src":      src,
                "dst":      dst,
                "found":    src.exists(),
                "exists":   dst.exists(),
                "shared":   False,
            })

    # Shared files → repo root (backend repo only, both models — v2's
    # frontend track never scans for master-registry.md, which is
    # backend-owned regardless of governance model)
    if model == GOVERNANCE_MODEL_V1 or track == "backend":
        for filename in SHARED_FILES:
            src = source_path / filename
            dst = REPO_BASE_PATH / filename
            operations.append({
                "stage":    "SHARED",
                "filename": filename,
                "src":      src,
                "dst":      dst,
                "found":    src.exists(),
                "exists":   dst.exists(),
                "shared":   True,
            })

    return operations


# ─────────────────────────────────────────────────────────────────────────────
# PLAN — Display what will happen
# ─────────────────────────────────────────────────────────────────────────────

def print_plan(mod: str, source_path: Path, operations: list[dict], dry_run: bool, track: str = None):
    """Print the archive plan."""

    found     = [o for o in operations if o["found"]]
    missing   = [o for o in operations if not o["found"]]
    overwrite = [o for o in found if o["exists"]]
    model = get_governance_model(mod)

    print()
    print("═" * 65)
    print(f"  AGENT 2 — Artifact Archiver")
    print(f"  Module  : {mod}")
    print(f"  Model   : governance_model={model}" + (f", track={track}" if track else ""))
    print(f"  Source  : {source_path}")
    print(f"  Repo    : {REPO_BASE_PATH}")
    print(f"  Mode    : {'DRY RUN (no changes)' if dry_run else 'LIVE'}")
    print("═" * 65)
    print()

    def _rel(p: Path) -> str:
        # Destination may resolve under REPO_BASE_PATH (backend) or
        # FRONTEND_OUTPUT_BASE_PATH (frontend, v2 frontend track) —
        # try both roots, fall back to absolute if neither matches.
        for root in (REPO_BASE_PATH,):
            try:
                return str(p.relative_to(root))
            except ValueError:
                continue
        try:
            from config import FRONTEND_OUTPUT_BASE_PATH
            return str(p.relative_to(FRONTEND_OUTPUT_BASE_PATH))
        except ValueError:
            return str(p)

    # Group by stage
    stages = {}
    for op in operations:
        stages.setdefault(op["stage"], []).append(op)

    for stage, ops in stages.items():
        print(f"  [{stage}]")
        for op in ops:
            if not op["found"]:
                status = "NOT FOUND  ✗ skip"
            elif op["exists"]:
                status = "OVERWRITE  ⚠"
            else:
                status = "COPY       ✓"
            rel_dst = _rel(op["dst"])
            print(f"    {status:<18} {op['filename']:<35} → {rel_dst}")
        print()

    print("─" * 65)
    print(f"  To copy    : {len(found)}")
    print(f"  To skip    : {len(missing)} (not found in source)")
    print(f"  Overwrites : {len(overwrite)}")
    if missing:
        print()
        print("  Missing files (will be skipped):")
        for op in missing:
            print(f"    ✗ {op['filename']}")
    print()


# ─────────────────────────────────────────────────────────────────────────────
# EXECUTE — Copy files
# ─────────────────────────────────────────────────────────────────────────────

def execute_archive(mod: str, operations: list[dict], dry_run: bool, track: str = None):
    """Copy artifact files to their destinations."""

    if dry_run:
        print("  DRY RUN — no files copied.")
        return

    copied  = []
    skipped = []
    errors  = []

    for op in operations:
        if not op["found"]:
            skipped.append(op["filename"])
            continue
        try:
            op["dst"].parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(op["src"], op["dst"])
            copied.append(op["filename"])
        except Exception as e:
            errors.append(f"{op['filename']}: {e}")

    # Update manifest (always lives backend-side — get_module_path is
    # backend-only regardless of governance model or track, per
    # STRUCTURAL LAW / config.py's manifest-stays-backend-owned rule)
    model = get_governance_model(mod)
    manifest_path = get_module_path(mod) / "manifest.json"
    if manifest_path.exists():
        with open(manifest_path, "r", encoding="utf-8") as fh:
            manifest = json.load(fh)

        if model == GOVERNANCE_MODEL_V2:
            flag_key = "archived_backend" if track == "backend" else "archived_frontend"
            manifest.setdefault("status", {})[flag_key] = True
            manifest[f"{track}_archived_at"] = datetime.now().isoformat()
            manifest.setdefault(f"{track}_archived_files", [])
            manifest[f"{track}_archived_files"] = copied
            manifest.setdefault(f"{track}_skipped_files", [])
            manifest[f"{track}_skipped_files"] = skipped
        else:
            manifest["status"]["archived"] = True
            manifest["archived_at"] = datetime.now().isoformat()
            manifest["archived_files"] = copied
            manifest["skipped_files"] = skipped

        with open(manifest_path, "w", encoding="utf-8") as fh:
            json.dump(manifest, fh, indent=2, ensure_ascii=False)

    # Report
    print("─" * 65)
    print(f"  ✓ Copied   : {len(copied)} files")
    print(f"  ⚠ Skipped  : {len(skipped)} files (not found)")
    if errors:
        print(f"  ✗ Errors   : {len(errors)}")
        for err in errors:
            print(f"    {err}")
    manifest_flag_desc = f"{track}_archived: true" if (model == GOVERNANCE_MODEL_V2 and track) else "archived: true"
    print(f"  ✓ Manifest : updated ({manifest_flag_desc})")
    print("─" * 65)
    print()

    if skipped:
        print("  NOTE: Missing files can be added later by re-running")
        track_flag = f" --track {track}" if track else ""
        print(f"  agent2_archive.py --module {mod}{track_flag} --source <path>")
        print("  Existing files will not be overwritten unless --force is used.")
        print()

    if not errors:
        print(f"  Archive complete for module [{mod}]" + (f" (track: {track})" if track else "") + ".")
        track_flag = f" --track {track}" if track else ""
        print(f"  Next step : Run agent3_splitter.py --module {mod}{track_flag}")
    print()


# ─────────────────────────────────────────────────────────────────────────────
# ENTRY POINT
# ─────────────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(
        description="Agent 2 — Archive governance artifacts into the repository."
    )
    parser.add_argument(
        "--module", "-m",
        required=True,
        help=f"Module code. Known: {', '.join(KNOWN_MODULES)}"
    )
    parser.add_argument(
        "--source", "-s",
        required=True,
        help="Path to folder containing the generated artifact files."
    )
    parser.add_argument(
        "--dry-run", "-d",
        action="store_true",
        help="Preview what would be copied without making any changes."
    )
    parser.add_argument(
        "--force", "-f",
        action="store_true",
        help="Overwrite existing files without asking."
    )
    parser.add_argument(
        "--track", choices=["backend", "frontend"], default=None,
        help="REQUIRED for v2-model modules — selects which stage subset "
             "to scan/archive ('backend': P0,P0_5,P1,P2,P2_5,P3_1,P3_5_BE,P4_1 "
             "/ 'frontend': P3_2,P3_5_FE,P4_2). REJECTED (hard error) "
             "for v1-model modules."
    )

    args = parser.parse_args()

    # ── Validate module ───────────────────────────────────────────────────────
    try:
        mod = validate_module(args.module)
    except ValueError as e:
        print(f"\n  ERROR: {e}\n")
        sys.exit(1)

    model = get_governance_model(mod)
    if model == GOVERNANCE_MODEL_V2 and args.track is None:
        print(f"\n  ERROR: module '{mod}' uses governance_model='v2' — "
              f"--track backend|frontend is required.\n")
        sys.exit(1)
    if model == GOVERNANCE_MODEL_V1 and args.track is not None:
        print(f"\n  ERROR: --track '{args.track}' was passed but module '{mod}' is "
              f"governance_model='v1' — v1 has no backend/frontend track split; it "
              f"always runs the single combined archive (no separate --track step).\n")
        sys.exit(1)

    # ── Validate source path ──────────────────────────────────────────────────
    source_path = Path(args.source).expanduser().resolve()
    if not source_path.exists():
        print(f"\n  ERROR: Source folder not found: {source_path}\n")
        sys.exit(1)

    # ── Validate module structure exists ──────────────────────────────────────
    module_path = get_module_path(mod)
    if not module_path.exists():
        print(f"\n  ERROR: Module structure not found: {module_path}")
        print(f"  Run agent1_create_structure.py --module {mod} first.\n")
        sys.exit(1)

    # ── Check if already archived ─────────────────────────────────────────────
    manifest_path = module_path / "manifest.json"
    if manifest_path.exists():
        with open(manifest_path, "r", encoding="utf-8") as fh:
            manifest = json.load(fh)
        if model == GOVERNANCE_MODEL_V2:
            flag_key = "archived_backend" if args.track == "backend" else "archived_frontend"
            already = manifest.get("status", {}).get(flag_key)
        else:
            already = manifest.get("status", {}).get("archived")
        if already and not args.force:
            print(f"\n  WARNING: Module [{mod}]" + (f" (track: {args.track})" if args.track else "") + " was already archived.")
            print(f"  Use --force to overwrite existing files.")
            confirm = input("  Continue anyway? [y/N]: ").strip().lower()
            if confirm != "y":
                print("\n  Cancelled — no changes made.\n")
                sys.exit(0)
            print()

    # ── Scan and plan ─────────────────────────────────────────────────────────
    operations = scan_source(mod, source_path, track=args.track)
    print_plan(mod, source_path, operations, args.dry_run, track=args.track)

    # ── Confirm if live run ───────────────────────────────────────────────────
    if not args.dry_run:
        confirm = input("  Proceed? [y/N]: ").strip().lower()
        if confirm != "y":
            print("\n  Cancelled — no changes made.\n")
            sys.exit(0)
        print()

    # ── Execute ───────────────────────────────────────────────────────────────
    execute_archive(mod, operations, args.dry_run, track=args.track)


if __name__ == "__main__":
    main()
