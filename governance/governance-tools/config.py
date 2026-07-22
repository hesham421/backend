"""
ERP Governance Tools — Shared Configuration
============================================
Single source of truth for all agents.
To add a new module: add its code to KNOWN_MODULES.
To change the repo path: update REPO_BASE_PATH.

=====================================================================
v2.1 UPDATE — DUAL GOVERNANCE MODEL SUPPORT
=====================================================================
This config now supports TWO governance models side by side:

  MODEL "v1" (legacy — existing modules: ORG, NOTIFICATION, FILESVC)
    - ONE execution-plan.md containing backend AND frontend phases
      together (CORE..ALIGN, F1..F4 combined)
    - ONE test-plan.md containing JUnit AND Playwright scenarios
      together (MARK:JUNIT / MARK:PLAYWRIGHT sections)
    - Folders: P0, P1, P2, P3, P3_5, P4
    - agent3_splitter.py runs ONCE per module

  MODEL "v2" (new — for modules registered from now on)
    - backend-execution-plan.md and frontend-execution-plan.md are
      TWO SEPARATE artifacts, generated at two different times
      (frontend gated on real API Docs — see GATE: BACKEND MODULE
      COMPLETE, CONTRACT-12)
    - backend-test-plan.md and frontend-test-plan.md are separate
      files — no MARK level needed (the file itself is the tool
      boundary)
    - Folders: P0, P0_5, P1, P2, P2_5, P3_1, P3_5_BE, P4_1, P3_2,
      P3_5_FE, P4_2
    - agent3_splitter.py runs TWICE per module (--stage backend,
      then later --stage frontend)

WHY BOTH: ORG, NOTIFICATION, and FILESVC were built entirely under
the v1 model — NOTIFICATION and FILESVC already have F1-F4 COMPLETE
under it. Retroactively splitting already-completed or in-progress
v1 work is out of scope for this update (high risk, no governance
benefit for work already done). v1 support is preserved AS-IS,
unchanged, indefinitely — this is not a deprecation path. v2 is
opt-in for modules registered going forward.

No existing module folder, file, or execution-state.json is touched
by this change. This is purely additive.
=====================================================================
"""

from pathlib import Path
import json

# ─────────────────────────────────────────────
# REPO — Single root for everything
# ─────────────────────────────────────────────

REPO_BASE_PATH = Path("/Users/ezzat/my project/backend/governance")

# PLAYWRIGHT test-phase content (v1 modules) / frontend-* content
# (v2 modules) is split out to the frontend repo by design
# (see the STRUCTURAL LAW section in backend/CLAUDE.md and frontend/CLAUDE.md —
# PLAYWRIGHT scenarios must never live under REPO_BASE_PATH/backend/governance/).
# This is a SEPARATE root, not derived from REPO_BASE_PATH, so the two can
# never accidentally collapse into one. Same hardcoded-absolute-path
# convention as REPO_BASE_PATH: update by hand if this checkout ever moves.
PLAYWRIGHT_OUTPUT_BASE_PATH = Path("/Users/ezzat/my project/frontend/governance")

# v2.1: identical alias, clearer name for v2-model code paths where
# "PLAYWRIGHT" is no longer an accurate name for what's being routed
# (F1-F4, SEC-FE, ALIGN-FE, and frontend-test-plan.md all route here too,
# not just Playwright scenarios). Both names point at the same path —
# use whichever reads clearer at the call site; do not treat them as
# two different roots.
FRONTEND_OUTPUT_BASE_PATH = PLAYWRIGHT_OUTPUT_BASE_PATH

# ─────────────────────────────────────────────
# MODULES — All known module codes
# Add new modules here — agents pick them up automatically
# ─────────────────────────────────────────────

KNOWN_MODULES = [
    "ORG",   # Organization
    "FIN",   # Finance
    "HR",    # Human Resources
    "PRC",   # Procurement
    "INV",   # Inventory
    "LGL",   # Legal
    "AST",   # Assets
    "BDG",   # Budget
    # Add more here as needed
]

# ─────────────────────────────────────────────
# GOVERNANCE MODEL — v1 (legacy) vs v2 (split)
# ─────────────────────────────────────────────

GOVERNANCE_MODEL_V1 = "v1"
GOVERNANCE_MODEL_V2 = "v2"
DEFAULT_MODEL_FOR_NEW_MODULES = GOVERNANCE_MODEL_V2

# Modules already known to be v1 as of this update — explicit, not
# inferred, so a future registry read never has to guess. Any module
# NOT in this set is treated as v2 by default (see get_governance_model).
KNOWN_V1_MODULES = {"ORG", "NOTIFICATION", "FILESVC"}

# ─────────────────────────────────────────────
# MODULES REGISTRY FILE
# Auto-updated when new modules are registered
# ─────────────────────────────────────────────

MODULES_REGISTRY_FILE = REPO_BASE_PATH / "modules-registry.json"


def load_modules_registry() -> dict:
    """Load the dynamic modules registry from disk."""
    if MODULES_REGISTRY_FILE.exists():
        with open(MODULES_REGISTRY_FILE, "r", encoding="utf-8") as fh:
            return json.load(fh)
    return {"modules": {}}


def save_modules_registry(registry: dict):
    """Save the dynamic modules registry to disk."""
    MODULES_REGISTRY_FILE.parent.mkdir(parents=True, exist_ok=True)
    with open(MODULES_REGISTRY_FILE, "w", encoding="utf-8") as fh:
        json.dump(registry, fh, indent=2, ensure_ascii=False)


def get_governance_model(mod: str) -> str:
    """
    Return "v1" or "v2" for a module.
    Priority: explicit "governance_model" field in modules-registry.json
              > KNOWN_V1_MODULES hardcoded set
              > DEFAULT_MODEL_FOR_NEW_MODULES ("v2")
    This is the single function every other helper in this file calls
    to decide which folder/filename/package scheme to use — never
    duplicate this decision logic elsewhere.
    """
    mod = mod.upper().strip()
    registry = load_modules_registry()
    entry = registry.get("modules", {}).get(mod)
    if entry and entry.get("governance_model") in (GOVERNANCE_MODEL_V1, GOVERNANCE_MODEL_V2):
        return entry["governance_model"]
    if mod in KNOWN_V1_MODULES:
        return GOVERNANCE_MODEL_V1
    return DEFAULT_MODEL_FOR_NEW_MODULES


def register_module(mod: str, description: str = "", governance_model: "str | None" = None) -> dict:
    """
    Register a new module or get existing registration.
    governance_model: "v1" or "v2" — if None, defaults to
      DEFAULT_MODEL_FOR_NEW_MODULES ("v2") unless mod is in
      KNOWN_V1_MODULES (then "v1").
    Returns the module registry entry.
    """
    registry = load_modules_registry()
    if mod not in registry["modules"]:
        model = governance_model or (
            GOVERNANCE_MODEL_V1 if mod in KNOWN_V1_MODULES else DEFAULT_MODEL_FOR_NEW_MODULES
        )
        registry["modules"][mod] = {
            "code": mod,
            "description": description,
            "governance_model": model,
            "registered_at": __import__("datetime").datetime.now().isoformat(),
            "versions": [],
            "current_version": None,
        }
        save_modules_registry(registry)
    return registry["modules"][mod]


def get_module_version_path(mod: str, version: "int | None" = None) -> Path:
    """
    Get path for a specific version of a module.
    If version is None → returns current (latest) version path.
    Version 1 = modules/ORG/v1/ , Version 2 = modules/ORG/v2/ etc.
    (This "version" concept is unrelated to the "v1"/"v2" governance
    model string — unfortunate naming collision inherited from the
    original file, kept as-is to avoid a wider rename. Governance
    model is about WHICH SCHEMA a module uses; this version is about
    WHICH REVISION of a module's artifacts you're looking at.)
    """
    registry = load_modules_registry()
    mod_entry = registry.get("modules", {}).get(mod)
    if not mod_entry:
        return get_module_path(mod)  # fallback for unregistered

    if version is None:
        version = mod_entry.get("current_version") or 1

    # v1 lives at modules/ORG/ (no suffix) for backward compat
    if version == 1:
        return REPO_BASE_PATH / "modules" / mod
    return REPO_BASE_PATH / "modules" / mod / f"v{version}"

def get_playwright_module_version_path(mod: str, version: "int | None" = None) -> Path:
    """
    Mirror of get_module_version_path(), but rooted at
    PLAYWRIGHT_OUTPUT_BASE_PATH / FRONTEND_OUTPUT_BASE_PATH
    (frontend/governance/) instead of REPO_BASE_PATH (backend/governance/).

    v1 modules: used for PLAYWRIGHT test-phase output only (unchanged).
    v2 modules: used for ALL frontend-native output — F1-F4/SEC-FE/
    ALIGN-FE package content AND frontend-test-plan.md content — since
    under v2 these are natively frontend-generated (see
    WORKSPACE-ARCHITECTURE-REFERENCE.md Section 11.3), not just a
    routed split of backend-owned content.

    Still reads the same modules-registry.json (registry stays backend-owned,
    single source of truth for version numbers per STRUCTURAL LAW) — only the
    destination root differs.
    """
    registry = load_modules_registry()
    mod_entry = registry.get("modules", {}).get(mod)
    if not mod_entry:
        return PLAYWRIGHT_OUTPUT_BASE_PATH / "modules" / mod

    if version is None:
        version = mod_entry.get("current_version") or 1

    if version == 1:
        return PLAYWRIGHT_OUTPUT_BASE_PATH / "modules" / mod
    return PLAYWRIGHT_OUTPUT_BASE_PATH / "modules" / mod / f"v{version}"

# ─────────────────────────────────────────────
# MODULE FOLDER STRUCTURE
# v1: legacy, unchanged. v2: new split model.
# ─────────────────────────────────────────────

MODULE_STRUCTURE_V1 = {
    "P0":       "P0",        # Platform Inception outputs
    "P1":       "P1",        # SRS outputs
    "P2":       "P2",        # DB Script outputs
    "P3":       "P3",        # Execution Plan outputs (backend+frontend combined)
    "P3_5":     "P3_5",      # Test Plan outputs (JUnit+Playwright combined)
    "P4":       "P4",        # Audit Report outputs
    "packages": "packages",  # Split artifacts (Agent 3 output)
}

MODULE_STRUCTURE_V2 = {
    "P0":       "P0",        # Platform Inception outputs (unchanged)
    "P0_5":     "P0_5",      # PRD Engine output: prd-{mod}.md
    "P1":       "P1",        # SRS outputs (unchanged)
    "P2":       "P2",        # DB Script outputs (unchanged)
    "P2_5":     "P2_5",      # UI/UX Design Engine: flow-diagram.md, ui-ux-spec.md
                              # (visual-mockups/ lives in the frontend repo instead
                              # — see WORKSPACE-ARCHITECTURE-REFERENCE.md 11.4)
    "P3_1":     "P3_1",      # Backend Execution Plan outputs (PASS 1)
    "P3_5_BE":  "P3_5_BE",   # Backend Test Plan + test-execution-manifest.md
    "P4_1":     "P4_1",      # Backend Audit Report
    "P3_2":     "P3_2",      # Frontend Execution Plan outputs (PASS 2) —
                              # NOTE: natively generated in the FRONTEND repo,
                              # not this one. Listed here for path-resolution
                              # completeness only — see get_stage_path's model
                              # check, which routes P3_2/P3_5_FE/P4_2 reads to
                              # FRONTEND_OUTPUT_BASE_PATH automatically.
    "P3_5_FE":  "P3_5_FE",   # Frontend Test Plan outputs — frontend repo
    "P4_2":     "P4_2",      # Frontend Audit Report — frontend repo
    "packages": "packages",  # Split artifacts (Agent 3 output) — backend repo
                              # for backend-*, frontend repo for frontend-*
}

# Backward-compatible alias — old code that imports MODULE_STRUCTURE
# directly (not through get_stage_path) keeps working unchanged,
# always resolving to the v1 (legacy) structure. New code should call
# get_module_structure(mod) instead, which is model-aware.
MODULE_STRUCTURE = MODULE_STRUCTURE_V1


def get_module_structure(mod: str) -> dict:
    """Model-aware replacement for the bare MODULE_STRUCTURE constant."""
    return MODULE_STRUCTURE_V2 if get_governance_model(mod) == GOVERNANCE_MODEL_V2 else MODULE_STRUCTURE_V1

# ─────────────────────────────────────────────
# ARTIFACT FILENAMES — Canonical names per stage
# v1: legacy, unchanged. v2: new split model.
# ─────────────────────────────────────────────

ARTIFACT_FILES_V1 = {
    "P0": [
        "platform-summary.md",
        "module-registry-{mod}.md",
        "business-policies-{mod}.md",
    ],
    "P1": [
        "srs.md",
        "registry-srs-{mod}.md",     # P-REG output
    ],
    "P2": [
        "db-script.md",
        "registry-db-{mod}.md",      # P-REG output
    ],
    "P3": [
        "execution-plan.md",
        "registry-exec-{mod}.md",    # P-REG output
    ],
    "P3_5": [
        "test-plan.md",
        "registry-test-{mod}.md",    # P-REG output
    ],
    "P4": [
        "audit-report.md",
    ],
}

ARTIFACT_FILES_V2 = {
    "P0": [
        "platform-summary.md",
        "module-registry-{mod}.md",
        "business-policies-{mod}.md",
    ],
    "P0_5": [
        "prd-{mod}.md",
    ],
    "P1": [
        "srs.md",
        "registry-srs-{mod}.md",       # P-REG output
    ],
    "P2": [
        "db-script.md",
        "registry-db-{mod}.md",        # P-REG output
    ],
    "P2_5": [
        "flow-diagram.md",
        "ui-ux-spec.md",
        # visual-mockups/ is a directory, not a file — lives in the
        # frontend repo (see WORKSPACE-ARCHITECTURE-REFERENCE.md 11.4)
    ],
    "P3_1": [
        "backend-execution-plan.md",
        "registry-exec-be-{mod}.md",   # P-REG output
    ],
    "P3_5_BE": [
        "backend-test-plan.md",
        "test-execution-manifest.md",
        "registry-test-be-{mod}.md",   # P-REG output
    ],
    "P4_1": [
        "P4.1-audit-report.md",
    ],
    "P3_2": [
        "frontend-execution-plan.md",
        "registry-exec-fe-{mod}.md",   # P-REG output
    ],
    "P3_5_FE": [
        "frontend-test-plan.md",
        "registry-test-fe-{mod}.md",   # P-REG output
    ],
    "P4_2": [
        "P4.2-audit-report.md",
    ],
}

# Backward-compatible alias, same rationale as MODULE_STRUCTURE above.
ARTIFACT_FILES = ARTIFACT_FILES_V1


def get_artifact_files(mod: str) -> dict:
    """Model-aware replacement for the bare ARTIFACT_FILES constant."""
    return ARTIFACT_FILES_V2 if get_governance_model(mod) == GOVERNANCE_MODEL_V2 else ARTIFACT_FILES_V1

# Shared files — copied to repo root (not per-module)
SHARED_FILES = [
    "master-registry.md",
]

# ─────────────────────────────────────────────
# PACKAGES STRUCTURE — Agent 3 output folders
# v1: legacy, unchanged. v2: new split model.
# ─────────────────────────────────────────────

PACKAGES_STRUCTURE_V1 = {
    # execution-plan.md splits (backend+frontend combined)
    "execution": [
        "CORE",
        "DATA-DOM",
        "SVC-API",
        "DOC",
        "INT-C",
        "INT-R",
        "F1",
        "F2",
        "F3",
        "SEC",
        "ALIGN",
        "SECTIONS",   # SECTION A/B/C/D
    ],
    # test-plan.md splits (JUnit+Playwright combined via MARK)
    "test": [
        "JUNIT",
        "PLAYWRIGHT",
    ],
}

PACKAGES_STRUCTURE_V2 = {
    # backend-execution-plan.md splits — backend repo
    "backend-execution": [
        "CORE",
        "DATA-DOM",
        "SVC-API",
        "DOC",
        "INT-C",
        "INT-R",
        "SEC-BE",
        "ALIGN-BE",
        "SECTIONS",   # SECTION A/B/C/D
    ],
    # frontend-execution-plan.md splits — frontend repo (natively
    # generated there, see 11.3 — this splitter output stays in the
    # same repo the source file lives in, no cross-repo routing needed)
    "frontend-execution": [
        "F1",
        "F2",
        "F3",
        "F4",
        "SEC-FE",
        "ALIGN-FE",
    ],
    # backend-test-plan.md splits — backend repo, no MARK subfolder
    "backend-test": [
        "RULE-SCENARIOS",
        "API-SCENARIOS",
    ],
    # frontend-test-plan.md splits — frontend repo, no MARK subfolder
    "frontend-test": [
        "UI-FLOWS",
        "INT-FLOW",
    ],
}

# Backward-compatible alias, same rationale as above.
PACKAGES_STRUCTURE = PACKAGES_STRUCTURE_V1


def get_packages_structure(mod: str) -> dict:
    """Model-aware replacement for the bare PACKAGES_STRUCTURE constant."""
    return PACKAGES_STRUCTURE_V2 if get_governance_model(mod) == GOVERNANCE_MODEL_V2 else PACKAGES_STRUCTURE_V1

# ─────────────────────────────────────────────
# MARKER PATTERNS — Used by Agent 3
# Unchanged for v1. v2 files simply never contain a MARK tag — the
# regex staying registered is harmless (it just never matches); see
# marker_parser.py for the hierarchy-validation change that makes
# MARK optional-and-then-forbidden per model, rather than removing
# the pattern here and risking a v1 parse regression.
# ─────────────────────────────────────────────

import re

MARKERS = {
    "phase":  re.compile(r"<!--\s*PHASE:(\w[\w-]*):(START|END)\s*-->"),
    "sub":    re.compile(r"<!--\s*SUB:([\w-]+):(START|END)\s*-->"),
    "mark":   re.compile(r"<!--\s*MARK:(JUNIT|PLAYWRIGHT):(START|END)\s*-->"),
    "api":    re.compile(r"<!--\s*API:(API-[\w-]+):(START|END)\s*-->"),
    "xm":     re.compile(r"<!--\s*XM:(XM-[\w-]+):(START|END)\s*-->"),
    "tc":     re.compile(r"<!--\s*TC:(TC-[\w-]+):(START|END)\s*-->"),
}

# ─────────────────────────────────────────────
# HELPERS
# ─────────────────────────────────────────────

def get_module_path(mod: str) -> Path:
    """Return the root path for a module."""
    mod = mod.upper()
    if mod not in KNOWN_MODULES:
        registry = load_modules_registry()
        if mod not in registry.get("modules", {}):
            raise ValueError(f"Unknown module: {mod}. Add it to KNOWN_MODULES in config.py")
    return REPO_BASE_PATH / "modules" / mod


def get_stage_path(mod: str, stage: str) -> Path:
    """
    Return the path for a specific stage inside a module.
    Model-aware (v2.1): for v2 modules, P3_2/P3_5_FE/P4_2 resolve
    under FRONTEND_OUTPUT_BASE_PATH (frontend repo) automatically,
    since those stages are natively frontend-generated under the v2
    model — not a backend-repo folder like their v1 P3/P3_5
    counterparts were.
    """
    structure = get_module_structure(mod)
    if stage not in structure:
        raise ValueError(f"Unknown stage: {stage}. Valid for this module's model: {list(structure.keys())}")

    frontend_native_stages_v2 = {"P3_2", "P3_5_FE", "P4_2"}
    if get_governance_model(mod) == GOVERNANCE_MODEL_V2 and stage in frontend_native_stages_v2:
        mod_upper = mod.upper()
        return FRONTEND_OUTPUT_BASE_PATH / "modules" / mod_upper / structure[stage]

    return get_module_path(mod) / structure[stage]


def get_packages_path(mod: str, artifact: str, sub: str = "") -> Path:
    """
    Return the packages path for a split artifact.
    Model-aware (v2.1): for v2 modules, "frontend-execution" and
    "frontend-test" artifacts resolve under FRONTEND_OUTPUT_BASE_PATH
    (frontend repo); "backend-execution" and "backend-test" stay in
    this repo. v1 modules are unaffected — "execution" and "test"
    both stay in this repo, exactly as before.
    """
    frontend_native_artifacts_v2 = {"frontend-execution", "frontend-test"}
    if get_governance_model(mod) == GOVERNANCE_MODEL_V2 and artifact in frontend_native_artifacts_v2:
        mod_upper = mod.upper()
        base = FRONTEND_OUTPUT_BASE_PATH / "modules" / mod_upper / "packages" / artifact
        return base / sub if sub else base

    base = get_module_path(mod) / "packages" / artifact
    return base / sub if sub else base


def resolve_filename(template: str, mod: str) -> str:
    """Replace {mod} placeholder with actual module code."""
    return template.replace("{mod}", mod.lower())


def validate_module(mod: str, auto_register: bool = False, description: str = "",
                     governance_model: "str | None" = None) -> str:
    """
    Validate and normalize module code.
    If auto_register=True → unknown modules are registered automatically.
    If auto_register=False → unknown modules raise ValueError.
    governance_model: only used if auto_register triggers a NEW
      registration — ignored for already-known/registered modules
      (their model is fixed at first registration, never silently
      changed on a later validate_module call).
    """
    mod = mod.upper().strip()

    # Known in static list → always valid
    if mod in KNOWN_MODULES:
        return mod

    # Check dynamic registry
    registry = load_modules_registry()
    if mod in registry.get("modules", {}):
        return mod

    # Unknown module
    if auto_register:
        register_module(mod, description, governance_model=governance_model)
        return mod

    raise ValueError(
        f"Module '{mod}' is not registered.\n"
        f"Static modules : {', '.join(KNOWN_MODULES)}\n"
        f"To register a new module automatically, use --auto-register flag.\n"
        f"Or add '{mod}' to KNOWN_MODULES in config.py.\n"
        f"New modules default to governance_model='{DEFAULT_MODEL_FOR_NEW_MODULES}' "
        f"unless --governance-model is passed explicitly."
    )


# ─────────────────────────────────────────────
# MANIFEST SCHEMA
# ─────────────────────────────────────────────

def build_manifest(mod: str, version: int = 1) -> dict:
    """
    Build empty manifest structure for a module version.
    Model-aware (v2.1): shape differs between v1 and v2 — see the
    two branches below. Callers that only ever handled the v1 shape
    (manifest["artifacts"]["p3"], etc.) will KeyError on a v2
    manifest — this is intentional; check manifest["governance_model"]
    before assuming which shape you have.
    """
    model = get_governance_model(mod)
    base = get_module_version_path(mod, version)

    if model == GOVERNANCE_MODEL_V1:
        return {
            "module":  mod,
            "version": version,
            "governance_model": GOVERNANCE_MODEL_V1,
            "status": {
                "archived": False,
                "split":    False,
                "audited":  False,
            },
            "artifacts": {
                "p0":   str(base / MODULE_STRUCTURE_V1["P0"]),
                "p1":   str(base / MODULE_STRUCTURE_V1["P1"]),
                "p2":   str(base / MODULE_STRUCTURE_V1["P2"]),
                "p3":   str(base / MODULE_STRUCTURE_V1["P3"]),
                "p3_5": str(base / MODULE_STRUCTURE_V1["P3_5"]),
                "p4":   str(base / MODULE_STRUCTURE_V1["P4"]),
            },
            "registries": {
                "srs":  str(base / MODULE_STRUCTURE_V1["P1"]  / f"registry-srs-{mod.lower()}.md"),
                "db":   str(base / MODULE_STRUCTURE_V1["P2"]  / f"registry-db-{mod.lower()}.md"),
                "exec": str(base / MODULE_STRUCTURE_V1["P3"]  / f"registry-exec-{mod.lower()}.md"),
                "test": str(base / MODULE_STRUCTURE_V1["P3_5"] / f"registry-test-{mod.lower()}.md"),
            },
            "packages": {
                "execution": str(base / "packages" / "execution"),
                "test":      str(base / "packages" / "test"),
            },
        }

    # GOVERNANCE_MODEL_V2
    frontend_base = get_playwright_module_version_path(mod, version)  # == FRONTEND_OUTPUT_BASE_PATH branch
    return {
        "module":  mod,
        "version": version,
        "governance_model": GOVERNANCE_MODEL_V2,
        "status": {
            "archived_backend":  False,
            "split_backend":     False,
            "audited_backend":   False,   # P4.1
            "backend_module_complete": False,  # CONTRACT-12 gate
            "archived_frontend": False,
            "split_frontend":    False,
            "audited_frontend":  False,   # P4.2
        },
        "artifacts": {
            "p0":      str(base / MODULE_STRUCTURE_V2["P0"]),
            "p0_5":    str(base / MODULE_STRUCTURE_V2["P0_5"]),
            "p1":      str(base / MODULE_STRUCTURE_V2["P1"]),
            "p2":      str(base / MODULE_STRUCTURE_V2["P2"]),
            "p2_5":    str(base / MODULE_STRUCTURE_V2["P2_5"]),
            "p3_1":    str(base / MODULE_STRUCTURE_V2["P3_1"]),
            "p3_5_be": str(base / MODULE_STRUCTURE_V2["P3_5_BE"]),
            "p4_1":    str(base / MODULE_STRUCTURE_V2["P4_1"]),
            "p3_2":    str(frontend_base / MODULE_STRUCTURE_V2["P3_2"]),
            "p3_5_fe": str(frontend_base / MODULE_STRUCTURE_V2["P3_5_FE"]),
            "p4_2":    str(frontend_base / MODULE_STRUCTURE_V2["P4_2"]),
        },
        "registries": {
            "srs":       str(base / MODULE_STRUCTURE_V2["P1"] / f"registry-srs-{mod.lower()}.md"),
            "db":        str(base / MODULE_STRUCTURE_V2["P2"] / f"registry-db-{mod.lower()}.md"),
            "exec_be":   str(base / MODULE_STRUCTURE_V2["P3_1"] / f"registry-exec-be-{mod.lower()}.md"),
            "test_be":   str(base / MODULE_STRUCTURE_V2["P3_5_BE"] / f"registry-test-be-{mod.lower()}.md"),
            "exec_fe":   str(frontend_base / MODULE_STRUCTURE_V2["P3_2"] / f"registry-exec-fe-{mod.lower()}.md"),
            "test_fe":   str(frontend_base / MODULE_STRUCTURE_V2["P3_5_FE"] / f"registry-test-fe-{mod.lower()}.md"),
        },
        "packages": {
            "backend_execution":  str(base / "packages" / "backend-execution"),
            "backend_test":       str(base / "packages" / "backend-test"),
            "frontend_execution": str(frontend_base / "packages" / "frontend-execution"),
            "frontend_test":      str(frontend_base / "packages" / "frontend-test"),
        },
    }


def get_next_version(mod: str) -> int:
    """Return the next version number for a module."""
    registry = load_modules_registry()
    entry = registry.get("modules", {}).get(mod)
    if not entry or not entry.get("versions"):
        return 1
    return max(entry["versions"]) + 1


def set_current_version(mod: str, version: int):
    """Update the current version in the modules registry."""
    registry = load_modules_registry()
    if mod not in registry["modules"]:
        register_module(mod)
        registry = load_modules_registry()
    entry = registry["modules"][mod]
    if version not in entry["versions"]:
        entry["versions"].append(version)
    entry["current_version"] = version
    save_modules_registry(registry)
