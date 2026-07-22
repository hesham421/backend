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

    v2.1 — new module, explicit governance model:
    python agent1_create_structure.py --module FIN --auto-register --governance-model v2

Handles:
    - New known module        → creates structure per its governance_model
                                  (v1: P0..P4/packages, unchanged — or
                                   v2: P0,P0_5,P1,P2,P2_5,P3_1,P3_5_BE,P4_1
                                   in THIS repo + a companion call needed
                                   in the frontend repo for P3_2/P3_5_FE/P4_2)
    - Unknown module          → rejects unless --auto-register
    - --auto-register         → registers module (v1 or v2 per
                                  --governance-model, defaults to v2 for
                                  brand-new modules — see config.py
                                  DEFAULT_MODEL_FOR_NEW_MODULES) and
                                  creates its structure
    - --new-version           → creates v2/v3/... alongside existing
                                  (this is the unrelated "module content
                                  version" number, not the governance
                                  model string — see config.py's note
                                  on that naming collision)
    - Existing module (same v)→ skips safely (idempotent)

=====================================================================
v2.1 UPDATE — DUAL GOVERNANCE MODEL SUPPORT
=====================================================================
This script now creates the correct folder set automatically based on
each module's governance_model (config.get_governance_model(mod)):

  v1 modules (ORG, NOTIFICATION, FILESVC) — UNCHANGED behavior, exact
    same folders as before: P0, P1, P2, P3, P3_5, P4, packages/execution/*,
    packages/test/{JUNIT,PLAYWRIGHT}.

  v2 modules (any new module) — creates BACKEND-repo folders only from
    this script: P0, P0_5, P1, P2, P2_5, P3_1, P3_5_BE, P4_1,
    packages/backend-execution/*, packages/backend-test/*.
    Frontend-native folders (P3_2, P3_5_FE, P4_2,
    packages/frontend-execution/*, packages/frontend-test/*) are NOT
    created by this script even for v2 modules — they belong in the
    FRONTEND repo (see WORKSPACE-ARCHITECTURE-REFERENCE.md Section 11.3,
    "frontend/ generates its own source now"). Run THIS SAME SCRIPT
    again from within the frontend repo's own governance-tools/ copy,
    pointed at the frontend repo's REPO_BASE_PATH, to create those —
    see the --frontend-only flag added below.
=====================================================================
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
    GOVERNANCE_MODEL_V1,
    GOVERNANCE_MODEL_V2,
    get_governance_model,
    get_module_structure,
    get_packages_structure,
    get_module_version_path,
    get_playwright_module_version_path,
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

def plan_structure(mod: str, version: int, frontend_only: bool = False) -> list[dict]:
    """
    Build a plan of all folders to create for a module version.

    frontend_only: v2 modules only. When True, plans ONLY the
      frontend-native folders (P3_2, P3_5_FE, P4_2,
      packages/frontend-execution/*, packages/frontend-test/*) rooted
      under FRONTEND_OUTPUT_BASE_PATH — meant to be run from the
      frontend repo's own copy of this script. Ignored (must be False)
      for v1 modules, which have no frontend-native concept.
    """
    model = get_governance_model(mod)
    structure = get_module_structure(mod)
    packages = get_packages_structure(mod)
    folders = []

    if frontend_only:
        if model != GOVERNANCE_MODEL_V2:
            raise ValueError(
                f"--frontend-only was passed but module '{mod}' is governance_model="
                f"'{model}' — only v2 modules have a frontend-native folder set. "
                f"v1 modules use the single combined structure created by the "
                f"backend-repo run (no separate frontend-only step)."
            )
        frontend_base = get_playwright_module_version_path(mod, version)
        for stage in ("P3_2", "P3_5_FE", "P4_2"):
            p = frontend_base / structure[stage]
            folders.append({"path": p, "label": stage})
        for sub in packages.get("frontend-execution", []):
            p = frontend_base / "packages" / "frontend-execution" / sub
            folders.append({"path": p, "label": f"packages/frontend-execution/{sub}"})
        for sub in packages.get("frontend-test", []):
            p = frontend_base / "packages" / "frontend-test" / sub
            folders.append({"path": p, "label": f"packages/frontend-test/{sub}"})
        for f in folders:
            f["exists"] = f["path"].exists()
        return folders

    base = get_module_version_path(mod, version)

    if model == GOVERNANCE_MODEL_V1:
        # Stage folders — unchanged from before
        for stage, name in structure.items():
            if stage == "packages":
                continue
            p = base / name
            folders.append({"path": p, "label": stage})
        for sub in packages["execution"]:
            p = base / "packages" / "execution" / sub
            folders.append({"path": p, "label": f"packages/execution/{sub}"})
        for sub in packages["test"]:
            p = base / "packages" / "test" / sub
            folders.append({"path": p, "label": f"packages/test/{sub}"})
    else:
        # v2 — this script creates BACKEND-native folders only.
        # P3_2/P3_5_FE/P4_2 and their packages are frontend-only (see above).
        backend_stages = ("P0", "P0_5", "P1", "P2", "P2_5", "P3_1", "P3_5_BE", "P4_1")
        for stage in backend_stages:
            p = base / structure[stage]
            folders.append({"path": p, "label": stage})
        for sub in packages.get("backend-execution", []):
            p = base / "packages" / "backend-execution" / sub
            folders.append({"path": p, "label": f"packages/backend-execution/{sub}"})
        for sub in packages.get("backend-test", []):
            p = base / "packages" / "backend-test" / sub
            folders.append({"path": p, "label": f"packages/backend-test/{sub}"})

    for f in folders:
        f["exists"] = f["path"].exists()

    return folders


def print_plan(mod: str, version: int, folders: list[dict], dry_run: bool, frontend_only: bool = False):
    """Print the creation plan."""
    model = get_governance_model(mod)
    if frontend_only:
        base = get_playwright_module_version_path(mod, version)
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
    print(f"  Model   : governance_model={model}" + (" (frontend-native folders)" if frontend_only else ""))
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
      manifest.json or touch modules-registry.json. Per STRUCTURAL LAW
      (WORKSPACE-ARCHITECTURE-REFERENCE.md), the registry and manifest
      stay backend-owned always — even for v2 modules' frontend-native
      folders. Run the backend (non-frontend-only) pass first; it
      writes the manifest/registry entries that already point at the
      frontend paths (see config.build_manifest's v2 branch).
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
    model = get_governance_model(mod)
    if model == GOVERNANCE_MODEL_V2:
        print(f"  Next step (backend)  : python agent2_archive.py --module {mod} --track backend")
        print(f"  Next step (frontend) : run THIS script with --frontend-only from the")
        print(f"                         frontend repo's governance-tools/ copy, once")
        print(f"                         GATE: BACKEND MODULE COMPLETE is confirmed")
    else:
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
    parser.add_argument("--governance-model", choices=["v1", "v2"], default=None,
                        help="Explicit governance model for a NEW module registration "
                             "(only used together with --auto-register on a module's "
                             "first-ever registration). Defaults to 'v2' for brand-new "
                             "modules — pass 'v1' explicitly only if you have a specific "
                             "reason to use the legacy combined model.")
    parser.add_argument("--frontend-only", action="store_true",
                        help="v2 modules only. Create ONLY the frontend-native folders "
                             "(P3_2, P3_5_FE, P4_2, packages/frontend-execution/*, "
                             "packages/frontend-test/*) rooted in the frontend repo. "
                             "Run this from the frontend repo's own governance-tools/ "
                             "copy — see FRONTEND_OUTPUT_BASE_PATH in config.py. Does "
                             "NOT write manifest.json or modules-registry.json (those "
                             "stay backend-owned — run the normal backend pass first).")

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
            governance_model=args.governance_model,
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
    try:
        folders = plan_structure(mod, version, frontend_only=args.frontend_only)
    except ValueError as e:
        print(f"\n  ERROR: {e}\n")
        sys.exit(1)
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
