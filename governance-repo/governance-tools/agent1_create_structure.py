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

Handles:
    - New known module        → creates v1 structure
    - Unknown module          → rejects unless --auto-register
    - --auto-register         → registers module and creates v1
    - --new-version           → creates v2/v3/... alongside existing
    - Existing module (same v)→ skips safely (idempotent)
"""

import argparse
import json
import sys
from pathlib import Path
from datetime import datetime

sys.path.insert(0, str(Path(__file__).parent))
from config import (
    REPO_BASE_PATH,
    KNOWN_MODULES,
    MODULE_STRUCTURE,
    PACKAGES_STRUCTURE,
    get_module_version_path,
    validate_module,
    build_manifest,
    get_next_version,
    set_current_version,
    register_module,
    load_modules_registry,
)

# ─────────────────────────────────────────────────────────────────────────────
# STRUCTURE BUILDER
# ─────────────────────────────────────────────────────────────────────────────

def plan_structure(mod: str, version: int) -> list[dict]:
    """Build a plan of all folders to create for a module version."""
    base = get_module_version_path(mod, version)
    folders = []

    # Stage folders
    for stage, name in MODULE_STRUCTURE.items():
        p = base / name
        folders.append({"path": p, "label": stage})

    # Packages — execution splits
    for sub in PACKAGES_STRUCTURE["execution"]:
        p = base / "packages" / "execution" / sub
        folders.append({"path": p, "label": f"packages/execution/{sub}"})

    # Packages — test splits
    for sub in PACKAGES_STRUCTURE["test"]:
        p = base / "packages" / "test" / sub
        folders.append({"path": p, "label": f"packages/test/{sub}"})

    for f in folders:
        f["exists"] = f["path"].exists()

    return folders


def print_plan(mod: str, version: int, folders: list[dict], dry_run: bool):
    """Print the creation plan."""
    base = get_module_version_path(mod, version)
    new_count  = sum(1 for f in folders if not f["exists"])
    skip_count = sum(1 for f in folders if f["exists"])

    print()
    print("═" * 62)
    print(f"  AGENT 1 — Structure Creator")
    print(f"  Module  : {mod}")
    print(f"  Version : v{version}")
    print(f"  Path    : {base.relative_to(REPO_BASE_PATH)}")
    print(f"  Mode    : {'DRY RUN (no changes)' if dry_run else 'LIVE'}")
    print("═" * 62)
    print()

    for f in folders:
        status = "EXISTS  ⚠ skip" if f["exists"] else "CREATE  ✓"
        try:
            rel = f["path"].relative_to(REPO_BASE_PATH)
        except ValueError:
            rel = f["path"]
        print(f"  [{status}]  {rel}")

    print()
    print(f"  Summary: {new_count} to create, {skip_count} already exist")
    print()


def create_structure(mod: str, version: int, folders: list[dict], dry_run: bool):
    """Create folders, manifest, and update modules registry."""
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

    # Write manifest.json at version root
    base = get_module_version_path(mod, version)
    manifest_path = base / "manifest.json"
    manifest = build_manifest(mod, version)
    manifest["created_at"] = datetime.now().isoformat()

    with open(manifest_path, "w", encoding="utf-8") as fh:
        json.dump(manifest, fh, indent=2, ensure_ascii=False)

    # Update modules registry
    set_current_version(mod, version)

    print("─" * 62)
    print(f"  ✓ Created  : {len(created)} folders")
    print(f"  ⚠ Skipped  : {len(skipped)} (already exist)")
    print(f"  ✓ Manifest : {manifest_path.relative_to(REPO_BASE_PATH)}")
    print(f"  ✓ Registry : modules-registry.json updated (v{version})")
    print("─" * 62)
    print()
    print(f"  Structure ready: [{mod}] v{version}")
    print(f"  Next step : python agent2_archive.py --module {mod}")
    print()


# ─────────────────────────────────────────────────────────────────────────────
# ENTRY POINT
# ─────────────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(
        description="Agent 1 — Create governance folder structure for a module."
    )
    parser.add_argument("--module", "-m", required=False, default=None,
                        help="Module code (e.g. ORG, FIN, HR)")
    parser.add_argument("--dry-run", "-d", action="store_true",
                        help="Preview without making changes.")
    parser.add_argument("--new-version", "-n", action="store_true",
                        help="Create a new version alongside existing (v2, v3...).")
    parser.add_argument("--auto-register", "-a", action="store_true",
                        help="Register unknown module automatically.")
    parser.add_argument("--description", default="",
                        help="Description for new module (used with --auto-register).")
    parser.add_argument("--list-modules", action="store_true",
                        help="List all known modules and exit.")

    args = parser.parse_args()

    # ── Manual required-check: --module is required UNLESS --list-modules ─────
    if not args.list_modules and not args.module:
        parser.error("the following arguments are required: --module/-m")

    # ── List modules ──────────────────────────────────────────────────────────
    if args.list_modules:
        registry = load_modules_registry()
        dyn = registry.get("modules", {})
        all_mods = list(dict.fromkeys(KNOWN_MODULES + list(dyn.keys())))
        print("\nRegistered modules:")
        for m in all_mods:
            base = get_module_version_path(m)
            status = "exists" if base.exists() else "not created"
            ver = dyn.get(m, {}).get("current_version", "—")
            desc = dyn.get(m, {}).get("description", "")
            print(f"  {m:<8} v{ver:<4} {status:<14} {desc}")
        print()
        sys.exit(0)

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

    if args.auto_register and args.module.upper() not in KNOWN_MODULES:
        print(f"\n  INFO: Module [{mod}] registered automatically.")

    # ── Determine version ─────────────────────────────────────────────────────
    if args.new_version:
        version = get_next_version(mod)
        print(f"\n  INFO: Creating new version v{version} for module [{mod}].")
    else:
        registry = load_modules_registry()
        existing = registry.get("modules", {}).get(mod, {}).get("current_version")
        version = existing if existing else 1

    # ── Build and show plan ───────────────────────────────────────────────────
    folders = plan_structure(mod, version)
    print_plan(mod, version, folders, args.dry_run)

    # ── Confirm if live run ───────────────────────────────────────────────────
    if not args.dry_run:
        confirm = input("  Proceed? [y/N]: ").strip().lower()
        if confirm != "y":
            print("\n  Cancelled — no changes made.\n")
            sys.exit(0)
        print()

    create_structure(mod, version, folders, args.dry_run)


if __name__ == "__main__":
    main()
