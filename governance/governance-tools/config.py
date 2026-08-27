"""
ERP Governance Tools — Shared Configuration
============================================
Single source of truth for all agents.
To add a new module: add its code to KNOWN_MODULES.
To change the repo path: update REPO_BASE_PATH.

=====================================================================
Clean-slate baseline — single governance model, no legacy compatibility
layer. Every module uses the same folder/artifact/package structure:

  Backend  (this repo)      : P0, P0_5, P1, P2, P2_5, P3_1, P3_5_BE
  Frontend (sibling repo)   : P3_2, P3_5_FE, P4_2

  backend-execution-plan.md and frontend-execution-plan.md are two
  separate artifacts, generated at two different times — frontend is
  gated on real, implemented API Docs (GATE: BACKEND MODULE COMPLETE)
  and a real, implemented UI Shell (GATE: UI SHELL COMPLETE), never on
  a planned/pre-implementation contract. See CONTRACT-12 in
  shared-artifact-contracts.md.

  backend-test-plan.md and frontend-test-plan.md are two separate
  files (JUnit-only and Playwright-only respectively, by construction)
  — there is no MARK-level marker distinguishing tool sections inside
  one combined file; the file itself is the tool boundary.
=====================================================================
"""

from pathlib import Path
import json

# ─────────────────────────────────────────────
# REPO — Single root for everything
# ─────────────────────────────────────────────

REPO_BASE_PATH = Path("/Users/ezzat/my project/backend/governance")

# Frontend-native content (P3_2, P3_5_FE, P4_2, and their packages/) is a
# SEPARATE root, not derived from REPO_BASE_PATH, so the two repos can
# never accidentally collapse into one. Update by hand if this checkout
# ever moves.
FRONTEND_OUTPUT_BASE_PATH = Path("/Users/ezzat/my project/frontend/governance")

# ─────────────────────────────────────────────
# MODULES — All known module codes
# Add new modules here — agents pick them up automatically
# ─────────────────────────────────────────────

KNOWN_MODULES = [
    # Add modules here as they're registered, e.g. "ORG", "FIN", "HR"
    "SECURITY",
]

# ─────────────────────────────────────────────
# FRONTEND EXCLUSIONS — modules with no frontend track, ever
# ─────────────────────────────────────────────
# SECURITY is a backend-only module — no UI Shell, no frontend-execution-plan,
# no frontend track of any kind. A frontend-scoped operation against it is a
# governance error, not an ordinary "not built yet" gap, so agent1/2/3 reject
# it outright instead of silently creating empty frontend folders/state.
FRONTEND_EXCLUDED_MODULES = {"SECURITY"}

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


def register_module(mod: str, description: str = "") -> dict:
    """
    Register a new module or get existing registration.
    Returns the module registry entry.
    """
    registry = load_modules_registry()
    if mod not in registry["modules"]:
        registry["modules"][mod] = {
            "code": mod,
            "description": description,
            "registered_at": __import__("datetime").datetime.now().isoformat(),
            "versions": [],
            "current_version": None,
        }
        save_modules_registry(registry)
    return registry["modules"][mod]


def get_module_version_path(mod: str, version: "int | None" = None) -> Path:
    """
    Get path for a specific version of a module (backend-owned content).
    If version is None → returns current (latest) version path.
    Version 1 = modules/ORG/ (no suffix), version 2 = modules/ORG/v2/, etc.
    """
    registry = load_modules_registry()
    mod_entry = registry.get("modules", {}).get(mod)
    if not mod_entry:
        return get_module_path(mod)  # fallback for unregistered

    if version is None:
        version = mod_entry.get("current_version") or 1

    if version == 1:
        return REPO_BASE_PATH / "modules" / mod
    return REPO_BASE_PATH / "modules" / mod / f"v{version}"


def get_frontend_module_version_path(mod: str, version: "int | None" = None) -> Path:
    """
    Mirror of get_module_version_path(), rooted at
    FRONTEND_OUTPUT_BASE_PATH (frontend/governance/) instead of
    REPO_BASE_PATH (backend/governance/). Used for all frontend-native
    output — P3_2/P3_5_FE/P4_2 content and frontend-test-plan.md.

    Still reads the same modules-registry.json (registry stays
    backend-owned, single source of truth for version numbers) — only
    the destination root differs.
    """
    registry = load_modules_registry()
    mod_entry = registry.get("modules", {}).get(mod)
    if not mod_entry:
        return FRONTEND_OUTPUT_BASE_PATH / "modules" / mod

    if version is None:
        version = mod_entry.get("current_version") or 1

    if version == 1:
        return FRONTEND_OUTPUT_BASE_PATH / "modules" / mod
    return FRONTEND_OUTPUT_BASE_PATH / "modules" / mod / f"v{version}"

# ─────────────────────────────────────────────
# MODULE FOLDER STRUCTURE
# ─────────────────────────────────────────────

MODULE_STRUCTURE = {
    "P0":       "P0",        # Platform Inception outputs
    "P0_5":     "P0_5",      # PRD Engine output: prd-{mod}.md
    "P1":       "P1",        # SRS outputs
    "P2":       "P2",        # DB Script outputs
    "P2_5":     "P2_5",      # UI/UX Design Engine: flow-diagram.md, ui-ux-spec.md
                              # (visual-mockups/ lives in the frontend repo instead)
    "P3_1":     "P3_1",      # Backend Execution Plan outputs
    "P3_5_BE":  "P3_5_BE",   # Backend Test Plan + test-execution-manifest.md
    "P3_2":     "P3_2",      # Frontend Execution Plan outputs — NOTE:
                              # natively generated in the FRONTEND repo.
                              # Listed here for path-resolution
                              # completeness only — get_stage_path()
                              # routes P3_2/P3_5_FE/P4_2 reads to
                              # FRONTEND_OUTPUT_BASE_PATH automatically.
    "P3_5_FE":  "P3_5_FE",   # Frontend Test Plan outputs — frontend repo
    "P4_2":     "P4_2",      # Frontend Audit Report — frontend repo
    "packages": "packages",  # Split artifacts (Agent 3 output) — backend
                              # repo for backend-*, frontend repo for frontend-*
}

# ─────────────────────────────────────────────
# ARTIFACT FILENAMES — Canonical names per stage
# ─────────────────────────────────────────────

ARTIFACT_FILES = {
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
        # frontend repo
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

# Shared files — copied to repo root (not per-module)
SHARED_FILES = [
    "master-registry.md",
]

# ─────────────────────────────────────────────
# PACKAGES STRUCTURE — Agent 3 output folders
# ─────────────────────────────────────────────

PACKAGES_STRUCTURE = {
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
    # generated there — this splitter output stays in the same repo
    # the source file lives in, no cross-repo routing needed)
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

# ─────────────────────────────────────────────
# MARKER PATTERNS — Used by Agent 3
# No MARK level — each test-plan file is single-tool by construction
# (backend-test-plan.md is JUnit-only, frontend-test-plan.md is
# Playwright-only), so there is nothing to distinguish via marker
# inside either file. TC blocks nest directly under PHASE or SUB.
# ─────────────────────────────────────────────

import re

MARKERS = {
    "phase":  re.compile(r"<!--\s*PHASE:(\w[\w-]*):(START|END)\s*-->"),
    "sub":    re.compile(r"<!--\s*SUB:([\w-]+):(START|END)\s*-->"),
    "api":    re.compile(r"<!--\s*API:(API-[\w-]+):(START|END)\s*-->"),
    "xm":     re.compile(r"<!--\s*XM:(XM-[\w-]+):(START|END)\s*-->"),
    "tc":     re.compile(r"<!--\s*TC:(TC-[\w-]+):(START|END)\s*-->"),
}

# Allowed nesting hierarchy: PHASE → [SUB] → ATOM, for every file type.
# TC atomic markers nest directly under PHASE or SUB — same rule as
# API/XM, no separate tool-boundary level.
ALLOWED_PARENTS = {
    "phase": [None],                  # top level only
    "sub":   ["phase"],               # SUB inside PHASE only
    "api":   ["phase", "sub"],        # API inside PHASE or SUB
    "xm":    ["phase", "sub"],        # XM inside PHASE or SUB
    "tc":    ["phase", "sub"],        # TC inside PHASE or SUB directly
}

# ─────────────────────────────────────────────
# HELPERS
# ─────────────────────────────────────────────

def get_module_path(mod: str) -> Path:
    """Return the root path for a module (backend-owned content)."""
    mod = mod.upper()
    if mod not in KNOWN_MODULES:
        registry = load_modules_registry()
        if mod not in registry.get("modules", {}):
            raise ValueError(f"Unknown module: {mod}. Add it to KNOWN_MODULES in config.py")
    return REPO_BASE_PATH / "modules" / mod


def get_stage_path(mod: str, stage: str) -> Path:
    """
    Return the path for a specific stage inside a module.
    P3_2/P3_5_FE/P4_2 resolve under FRONTEND_OUTPUT_BASE_PATH (frontend
    repo) automatically, since those stages are natively frontend-
    generated — not a backend-repo folder like the rest.
    """
    if stage not in MODULE_STRUCTURE:
        raise ValueError(f"Unknown stage: {stage}. Valid stages: {list(MODULE_STRUCTURE.keys())}")

    frontend_native_stages = {"P3_2", "P3_5_FE", "P4_2"}
    if stage in frontend_native_stages:
        mod_upper = mod.upper()
        return FRONTEND_OUTPUT_BASE_PATH / "modules" / mod_upper / MODULE_STRUCTURE[stage]

    return get_module_path(mod) / MODULE_STRUCTURE[stage]


def get_packages_path(mod: str, artifact: str, sub: str = "") -> Path:
    """
    Return the packages path for a split artifact.
    "frontend-execution" and "frontend-test" artifacts resolve under
    FRONTEND_OUTPUT_BASE_PATH (frontend repo); "backend-execution" and
    "backend-test" stay in this repo.
    """
    frontend_native_artifacts = {"frontend-execution", "frontend-test"}
    if artifact in frontend_native_artifacts:
        mod_upper = mod.upper()
        base = FRONTEND_OUTPUT_BASE_PATH / "modules" / mod_upper / "packages" / artifact
        return base / sub if sub else base

    base = get_module_path(mod) / "packages" / artifact
    return base / sub if sub else base


def resolve_filename(template: str, mod: str) -> str:
    """Replace {mod} placeholder with actual module code."""
    return template.replace("{mod}", mod.lower())


def validate_module(mod: str, auto_register: bool = False, description: str = "") -> str:
    """
    Validate and normalize module code.
    If auto_register=True → unknown modules are registered automatically.
    If auto_register=False → unknown modules raise ValueError.
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
        register_module(mod, description)
        return mod

    raise ValueError(
        f"Module '{mod}' is not registered.\n"
        f"Static modules : {', '.join(KNOWN_MODULES) or '(none yet)'}\n"
        f"To register a new module automatically, use --auto-register flag.\n"
        f"Or add '{mod}' to KNOWN_MODULES in config.py."
    )


# ─────────────────────────────────────────────
# MANIFEST SCHEMA
# ─────────────────────────────────────────────

def build_manifest(mod: str, version: int = 1) -> dict:
    """Build empty manifest structure for a module version."""
    base = get_module_version_path(mod, version)
    frontend_base = get_frontend_module_version_path(mod, version)

    return {
        "module":  mod,
        "version": version,
        "status": {
            "archived_backend":  False,
            "split_backend":     False,
            "backend_module_complete": False,  # CONTRACT-12 gate
            "ui_shell_complete": False,   # CONTRACT-12 gate (v2.1)
            "archived_frontend": False,
            "split_frontend":    False,
            "audited_frontend":  False,   # P4.2
        },
        "artifacts": {
            "p0":      str(base / MODULE_STRUCTURE["P0"]),
            "p0_5":    str(base / MODULE_STRUCTURE["P0_5"]),
            "p1":      str(base / MODULE_STRUCTURE["P1"]),
            "p2":      str(base / MODULE_STRUCTURE["P2"]),
            "p2_5":    str(base / MODULE_STRUCTURE["P2_5"]),
            "p3_1":    str(base / MODULE_STRUCTURE["P3_1"]),
            "p3_5_be": str(base / MODULE_STRUCTURE["P3_5_BE"]),
            # Frontend-native stages resolve in the FRONTEND repo, not here:
            "p3_2":    str(frontend_base / MODULE_STRUCTURE["P3_2"]),
            "p3_5_fe": str(frontend_base / MODULE_STRUCTURE["P3_5_FE"]),
            "p4_2":    str(frontend_base / MODULE_STRUCTURE["P4_2"]),
        },
        "registries": {
            "srs":       str(base / MODULE_STRUCTURE["P1"] / f"registry-srs-{mod.lower()}.md"),
            "db":        str(base / MODULE_STRUCTURE["P2"] / f"registry-db-{mod.lower()}.md"),
            "exec_be":   str(base / MODULE_STRUCTURE["P3_1"] / f"registry-exec-be-{mod.lower()}.md"),
            "test_be":   str(base / MODULE_STRUCTURE["P3_5_BE"] / f"registry-test-be-{mod.lower()}.md"),
            "exec_fe":   str(frontend_base / MODULE_STRUCTURE["P3_2"] / f"registry-exec-fe-{mod.lower()}.md"),
            "test_fe":   str(frontend_base / MODULE_STRUCTURE["P3_5_FE"] / f"registry-test-fe-{mod.lower()}.md"),
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
