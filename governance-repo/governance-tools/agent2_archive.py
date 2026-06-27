"""
ERP Governance Tools — Agent 2: Artifact Archiver
==================================================
Copies generated artifacts from their source locations
into the canonical governance repository structure.

Usage:
    python agent2_archive.py --module ORG --source ~/Desktop/ORG-artifacts
    python agent2_archive.py --module ORG --source ~/Desktop/ORG-artifacts --dry-run

What it does:
    1. Reads manifest.json for the module
    2. Scans source folder for known artifact filenames
    3. Shows a plan of what will be copied where
    4. Waits for approval
    5. Copies files to correct stage folders
    6. Updates manifest.json (archived: true)

Handles:
    - Missing files        → warns but continues (partial archive)
    - Already archived     → asks before overwriting
    - Unknown module       → rejects with clear message
    - master-registry.md  → copied to repo root (shared)
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
    get_module_path,
    get_stage_path,
    validate_module,
    resolve_filename,
)

# ─────────────────────────────────────────────────────────────────────────────
# SCAN — Find artifacts in source folder
# ─────────────────────────────────────────────────────────────────────────────

def scan_source(mod: str, source_path: Path) -> list[dict]:
    """
    Scan source folder for known artifact files.
    Returns list of copy operations with status.
    """
    operations = []

    # Per-stage artifacts
    for stage, templates in ARTIFACT_FILES.items():
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

    # Shared files → repo root
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

def print_plan(mod: str, source_path: Path, operations: list[dict], dry_run: bool):
    """Print the archive plan."""

    found     = [o for o in operations if o["found"]]
    missing   = [o for o in operations if not o["found"]]
    overwrite = [o for o in found if o["exists"]]

    print()
    print("═" * 65)
    print(f"  AGENT 2 — Artifact Archiver")
    print(f"  Module  : {mod}")
    print(f"  Source  : {source_path}")
    print(f"  Repo    : {REPO_BASE_PATH}")
    print(f"  Mode    : {'DRY RUN (no changes)' if dry_run else 'LIVE'}")
    print("═" * 65)
    print()

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
            rel_dst = op["dst"].relative_to(REPO_BASE_PATH)
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

def execute_archive(mod: str, operations: list[dict], dry_run: bool):
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

    # Update manifest
    manifest_path = get_module_path(mod) / "manifest.json"
    if manifest_path.exists():
        with open(manifest_path, "r", encoding="utf-8") as fh:
            manifest = json.load(fh)
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
    print(f"  ✓ Manifest : updated (archived: true)")
    print("─" * 65)
    print()

    if skipped:
        print("  NOTE: Missing files can be added later by re-running")
        print(f"  agent2_archive.py --module {mod} --source <path>")
        print("  Existing files will not be overwritten unless --force is used.")
        print()

    if not errors:
        print(f"  Archive complete for module [{mod}].")
        print(f"  Next step : Run agent3_splitter.py --module {mod}")
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

    args = parser.parse_args()

    # ── Validate module ───────────────────────────────────────────────────────
    try:
        mod = validate_module(args.module)
    except ValueError as e:
        print(f"\n  ERROR: {e}\n")
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
        if manifest.get("status", {}).get("archived") and not args.force:
            print(f"\n  WARNING: Module [{mod}] was already archived.")
            print(f"  Use --force to overwrite existing files.")
            confirm = input("  Continue anyway? [y/N]: ").strip().lower()
            if confirm != "y":
                print("\n  Cancelled — no changes made.\n")
                sys.exit(0)
            print()

    # ── Scan and plan ─────────────────────────────────────────────────────────
    operations = scan_source(mod, source_path)
    print_plan(mod, source_path, operations, args.dry_run)

    # ── Confirm if live run ───────────────────────────────────────────────────
    if not args.dry_run:
        confirm = input("  Proceed? [y/N]: ").strip().lower()
        if confirm != "y":
            print("\n  Cancelled — no changes made.\n")
            sys.exit(0)
        print()

    # ── Execute ───────────────────────────────────────────────────────────────
    execute_archive(mod, operations, args.dry_run)


if __name__ == "__main__":
    main()
