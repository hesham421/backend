"""
ERP Governance Tools — Shared Configuration
============================================
Single source of truth for all agents.
To add a new module: add its code to KNOWN_MODULES.
To change the repo path: update REPO_BASE_PATH.
"""

from pathlib import Path
import json

# ─────────────────────────────────────────────
# REPO — Single root for everything
# ─────────────────────────────────────────────

REPO_BASE_PATH = Path("/Users/ezzat/my project/governance-repo")

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
    Get path for a specific version of a module.
    If version is None → returns current (latest) version path.
    Version 1 = modules/ORG/v1/ , Version 2 = modules/ORG/v2/ etc.
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

# ─────────────────────────────────────────────
# MODULE FOLDER STRUCTURE
# Same for every module — no exceptions
# ─────────────────────────────────────────────

MODULE_STRUCTURE = {
    "P0":       "P0",        # Platform Inception outputs
    "P1":       "P1",        # SRS outputs
    "P2":       "P2",        # DB Script outputs
    "P3":       "P3",        # Execution Plan outputs
    "P3_5":     "P3_5",      # Test Plan outputs
    "P4":       "P4",        # Audit Report outputs
    "packages": "packages",  # Split artifacts (Agent 3 output)
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

# Shared files — copied to repo root (not per-module)
SHARED_FILES = [
    "master-registry.md",
]

# ─────────────────────────────────────────────
# PACKAGES STRUCTURE — Agent 3 output folders
# ─────────────────────────────────────────────

PACKAGES_STRUCTURE = {
    # execution-plan.md splits
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
    # test-plan.md splits
    "test": [
        "JUNIT",
        "PLAYWRIGHT",
    ],
}

# ─────────────────────────────────────────────
# MARKER PATTERNS — Used by Agent 3
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
    """Return the path for a specific stage inside a module."""
    if stage not in MODULE_STRUCTURE:
        raise ValueError(f"Unknown stage: {stage}. Valid: {list(MODULE_STRUCTURE.keys())}")
    return get_module_path(mod) / MODULE_STRUCTURE[stage]


def get_packages_path(mod: str, artifact: str, sub: str = "") -> Path:
    """Return the packages path for a split artifact."""
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
        f"Static modules : {', '.join(KNOWN_MODULES)}\n"
        f"To register a new module automatically, use --auto-register flag.\n"
        f"Or add '{mod}' to KNOWN_MODULES in config.py."
    )


# ─────────────────────────────────────────────
# MANIFEST SCHEMA
# ─────────────────────────────────────────────

def build_manifest(mod: str, version: int = 1) -> dict:
    """Build empty manifest structure for a module version."""
    base = get_module_version_path(mod, version)
    return {
        "module":  mod,
        "version": version,
        "status": {
            "archived": False,
            "split":    False,
            "audited":  False,
        },
        "artifacts": {
            "p0":   str(base / MODULE_STRUCTURE["P0"]),
            "p1":   str(base / MODULE_STRUCTURE["P1"]),
            "p2":   str(base / MODULE_STRUCTURE["P2"]),
            "p3":   str(base / MODULE_STRUCTURE["P3"]),
            "p3_5": str(base / MODULE_STRUCTURE["P3_5"]),
            "p4":   str(base / MODULE_STRUCTURE["P4"]),
        },
        "registries": {
            "srs":  str(base / MODULE_STRUCTURE["P1"]  / f"registry-srs-{mod.lower()}.md"),
            "db":   str(base / MODULE_STRUCTURE["P2"]  / f"registry-db-{mod.lower()}.md"),
            "exec": str(base / MODULE_STRUCTURE["P3"]  / f"registry-exec-{mod.lower()}.md"),
            "test": str(base / MODULE_STRUCTURE["P3_5"] / f"registry-test-{mod.lower()}.md"),
        },
        "packages": {
            "execution": str(base / "packages" / "execution"),
            "test":      str(base / "packages" / "test"),
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
