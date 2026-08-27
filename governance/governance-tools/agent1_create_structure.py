"""
ERP Governance Tools — Agent 1: Structure Creator
==================================================
Creates the canonical folder structure for a module.

Usage:
    python agent1_create_structure.py --module ORG
    python agent1_create_structure.py --module ORG --dry-run
    python agent1_create_structure.py --module NEW --auto-register --description "New Module"
    python agent1_create_structure.py --module ORG --new-version
    python agent1_create_structure.py --list-modules

    Frontend-native folders (run from the frontend repo's own copy of
    this script, once GATE: BACKEND MODULE COMPLETE + GATE: UI SHELL
    COMPLETE are confirmed for the module):
    python agent1_create_structure.py --module ORG --frontend-only

Handles:
    - New known module        → creates its full folder structure
                                  (backend-repo run: P0, P0_5, P1, P2,
                                  P2_5, P3_1, P3_5_BE — frontend-
                                  native folders are NOT created by a
                                  backend-repo run; see --frontend-only)
    - Unknown module          → rejects unless --auto-register
    - --auto-register         → registers module and creates its structure
    - --new-version           → creates v2/v3/... alongside existing
    - Existing module (same v)→ skips safely (idempotent)
    - --frontend-only         → creates ONLY the frontend-native subset
                                  (P3_2, P3_5_FE, P4_2) — run this from
                                  the frontend repo's own copy of the
                                  script, never from the backend repo
"""

import argparse
import json
import sys
from pathlib import Path
from datetime import datetime

sys.path.insert(0, str(Path(__file__).parent))
from config import (
    REPO_BASE_PATH,
    FRONTEND_OUTPUT_BASE_PATH,
    KNOWN_MODULES,
    FRONTEND_EXCLUDED_MODULES,
    MODULE_STRUCTURE,
    PACKAGES_STRUCTURE,
    get_module_version_path,
    get_frontend_module_version_path,
    validate_module,
    build_manifest,
    get_next_version,
    set_current_version,
    register_module,
    load_modules_registry,
)

# Stages created by a normal (backend-repo) run
BACKEND_STAGES = ("P0", "P0_5", "P1", "P2", "P2_5", "P3_1", "P3_5_BE")
# Stages created by a --frontend-only (frontend-repo) run
FRONTEND_STAGES = ("P3_2", "P3_5_FE", "P4_2")


def plan_structure(mod: str, version: int, frontend_only: bool = False) -> list[dict]:
    """
    Build a plan of all folders to create for a module version.

    frontend_only: when True, plans ONLY the frontend-native folders
      (P3_2, P3_5_FE, P4_2, packages/frontend-execution/*,
      packages/frontend-test/*) rooted under FRONTEND_OUTPUT_BASE_PATH —
      meant to be run from the frontend repo's own copy of this script.
      When False (default), plans the backend-repo subset.
    """
    folders = []

    if frontend_only:
        frontend_base = get_frontend_module_version_path(mod, version)
        for stage in FRONTEND_STAGES:
            p = frontend_base / MODULE_STRUCTURE[stage]
            folders.append({"path": p, "label": stage})
        for sub in PACKAGES_STRUCTURE.get("frontend-execution", []):
            p = frontend_base / "packages" / "frontend-execution" / sub
            folders.append({"path": p, "label": f"packages/frontend-execution/{sub}"})
        for sub in PACKAGES_STRUCTURE.get("frontend-test", []):
            p = frontend_base / "packages" / "frontend-test" / sub
            folders.append({"path": p, "label": f"packages/frontend-test/{sub}"})
    else:
        base = get_module_version_path(mod, version)
        for stage in BACKEND_STAGES:
            p = base / MODULE_STRUCTURE[stage]
            folders.append({"path": p, "label": stage})
        for sub in PACKAGES_STRUCTURE.get("backend-execution", []):
            p = base / "packages" / "backend-execution" / sub
            folders.append({"path": p, "label": f"packages/backend-execution/{sub}"})
        for sub in PACKAGES_STRUCTURE.get("backend-test", []):
            p = base / "packages" / "backend-test" / sub
            folders.append({"path": p, "label": f"packages/backend-test/{sub}"})

    for f in folders:
        f["exists"] = f["path"].exists()

    return folders


def print_plan(mod: str, version: int, folders: list[dict], dry_run: bool, frontend_only: bool = False):
    """Print the creation plan."""
    if frontend_only:
        base = get_frontend_module_version_path(mod, version)
        rel_root = FRONTEND_OUTPUT_BASE_PATH
    else:
        base = get_module_version_path(mod, version)
        rel_root = REPO_BASE_PATH
    new_count  = sum(1 for f in folders if not f["exists"])
    skip_count = sum(1 for f in folders if f["exists"])

    print()
    print("═" * 62)
    print(f"  AGENT 1 — Structure Creator")
    print(f"  Module  : {mod}")
    print(f"  Scope   : {'frontend-native folders' if frontend_only else 'backend folders'}")
    print(f"  Version : v{version}")
    try:
        print(f"  Path    : {base.relative_to(rel_root)}")
    except ValueError:
        print(f"  Path    : {base}")
    print(f"  Mode    : {'DRY RUN (no changes)' if dry_run else 'LIVE'}")
    print("═" * 62)
    print()

    for f in folders:
        status = "EXISTS  ⚠ skip" if f["exists"] else "CREATE  ✓"
        try:
            rel = f["path"].relative_to(rel_root)
        except ValueError:
            rel = f["path"]
        print(f"  [{status}]  {rel}")

    print()
    print(f"  Summary: {new_count} to create, {skip_count} already exist")
    print()


def create_structure(mod: str, version: int, folders: list[dict], dry_run: bool, frontend_only: bool = False):
    """
    Create folders, manifest, and update modules registry.

    frontend_only: when True, only creates folders — does NOT write
      manifest.json or touch modules-registry.json. The registry and
      manifest stay backend-owned always. Run the backend (non-
      frontend-only) pass first; it writes the manifest/registry
      entries that already point at the frontend paths.
    """
    if dry_run:
        print("  DRY RUN — no folders created.")
        return

    created = []
    skipped = []

    for f in folders:
        if f["exists"]:
            skipped.append(f["path"])
        else:
            f["path"].mkdir(parents=True, exist_ok=True)
            (f["path"] / ".gitkeep").touch()
            created.append(f["path"])

    print("─" * 62)
    print(f"  ✓ Created  : {len(created)} folders")
    print(f"  ⚠ Skipped  : {len(skipped)} (already exist)")

    if frontend_only:
        print(f"  — Manifest/registry NOT touched (frontend-only run — those")
        print(f"    stay backend-owned; run the backend pass to write them).")
        print("─" * 62)
        print()
        print(f"  Frontend-native structure ready: [{mod}] v{version}")
        print()
        return

    # Write manifest.json at version root
    base = get_module_version_path(mod, version)
    manifest_path = base / "manifest.json"
    manifest = build_manifest(mod, version)
    manifest["created_at"] = datetime.now().isoformat()

    with open(manifest_path, "w", encoding="utf-8") as fh:
        json.dump(manifest, fh, indent=2, ensure_ascii=False)

    # Update modules registry
    set_current_version(mod, version)

    print(f"  ✓ Manifest : {manifest_path.relative_to(REPO_BASE_PATH)}")
    print(f"  ✓ Registry : modules-registry.json updated (v{version})")
    print("─" * 62)
    print()
    print(f"  Structure ready: [{mod}] v{version}")
    print(f"  Next step (backend)  : python agent2_archive.py --module {mod}")
    print(f"  Next step (frontend) : run this script with --frontend-only from")
    print(f"                         the frontend repo's copy, once GATE: BACKEND")
    print(f"                         MODULE COMPLETE + GATE: UI SHELL COMPLETE")
    print(f"                         are confirmed for this module")
    print()


def list_modules():
    """List all known + registered modules."""
    registry = load_modules_registry()
    all_mods = set(KNOWN_MODULES) | set(registry.get("modules", {}).keys())

    print()
    print("═" * 62)
    print("  KNOWN MODULES")
    print("═" * 62)
    if not all_mods:
        print("  (none registered yet)")
    for mod in sorted(all_mods):
        entry = registry.get("modules", {}).get(mod, {})
        version = entry.get("current_version", "—")
        desc = entry.get("description", "")
        static_tag = " [static]" if mod in KNOWN_MODULES else ""
        print(f"  {mod:<12} v{version}{static_tag}  {desc}")
    print("═" * 62)
    print()


def main():
    parser = argparse.ArgumentParser(description="Create governance folder structure for a module.")
    parser.add_argument("--module", "-m", help="Module code (e.g. ORG, FIN).")
    parser.add_argument("--dry-run", action="store_true", help="Show plan without creating anything.")
    parser.add_argument("--auto-register", action="store_true",
                        help="Automatically register an unknown module.")
    parser.add_argument("--description", default="", help="Description for --auto-register.")
    parser.add_argument("--new-version", action="store_true",
                        help="Create the next version for an existing module.")
    parser.add_argument("--list-modules", action="store_true",
                        help="List all known modules and exit.")
    parser.add_argument("--frontend-only", action="store_true",
                        help="Create ONLY the frontend-native folders (P3_2, P3_5_FE, "
                             "P4_2, packages/frontend-execution/*, packages/frontend-test/*) "
                             "rooted in the frontend repo. Run this from the frontend "
                             "repo's own governance-tools/ copy. Does NOT write "
                             "manifest.json or modules-registry.json (those stay "
                             "backend-owned — run the normal backend pass first).")

    args = parser.parse_args()

    if args.list_modules:
        list_modules()
        sys.exit(0)

    if not args.module:
        print("\n  ERROR: --module is required (or use --list-modules).\n")
        sys.exit(1)

    # ── Validate / register module ────────────────────────────────────────────
    try:
        mod = validate_module(
            args.module,
            auto_register=args.auto_register,
            description=args.description,
        )
    except ValueError as e:
        print(f"\n  ERROR: {e}\n")
        sys.exit(1)

    # ── Reject frontend-scoped ops on frontend-excluded modules ────────────────
    if args.frontend_only and mod in FRONTEND_EXCLUDED_MODULES:
        print(f"\n  ERROR: Module '{mod}' is frontend-excluded — it has no frontend "
              f"track (see FRONTEND_EXCLUDED_MODULES in config.py).")
        print(f"  --frontend-only is not valid for this module.\n")
        sys.exit(1)

    # ── Determine version ─────────────────────────────────────────────────────
    if args.new_version:
        version = get_next_version(mod)
    else:
        registry = load_modules_registry()
        entry = registry.get("modules", {}).get(mod)
        version = (entry.get("current_version") if entry else None) or 1

    # ── Build and show plan ───────────────────────────────────────────────────
    folders = plan_structure(mod, version, frontend_only=args.frontend_only)
    print_plan(mod, version, folders, args.dry_run, frontend_only=args.frontend_only)

    # ── Confirm if live run ───────────────────────────────────────────────────
    if not args.dry_run:
        confirm = input("  Proceed? [y/N]: ").strip().lower()
        if confirm != "y":
            print("\n  Cancelled — no changes made.\n")
            sys.exit(0)
        print()

    create_structure(mod, version, folders, args.dry_run, frontend_only=args.frontend_only)


if __name__ == "__main__":
    main()
