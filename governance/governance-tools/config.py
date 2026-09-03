"""
ERP Governance Tools — Backend Configuration
==============================================
Single source of truth for the BACKEND toolset only.

This file has NO representation of "frontend" anywhere — not a
constant, not a path, not a conditional branch. The frontend has its
own completely separate copy of these five tools, in
frontend/governance/governance-tools/, which likewise has no
representation of "backend" beyond one sanctioned cross-repo read: the
published module registry (shared/modules-registry.json, written by
save_modules_registry below). Frontend does not read API Docs from
here either — those live in frontend's own modules/{MOD}/api-docs/.

There is no P4/P4_1 concept anywhere in this ecosystem — the
pre-implementation audit gate was removed entirely.

There is no --track flag anywhere in this toolset. Every tool here
does exactly one thing: backend.
"""

from pathlib import Path
import json
import re

# ─────────────────────────────────────────────
# REPO — Single root. Derived from this file's own location, not
# hardcoded, so the repo works regardless of which machine/user
# account it's checked out under.
# ─────────────────────────────────────────────

REPO_BASE_PATH = Path(__file__).resolve().parent.parent

# ─────────────────────────────────────────────
# MODULES
# ─────────────────────────────────────────────

KNOWN_MODULES = [
    # Add modules here as they're registered
]

MODULES_REGISTRY_FILE = REPO_BASE_PATH / "modules-registry.json"

# Published, read-only copy of the registry for other tracks (frontend, etc.)
# to consume. Lives outside both backend/ and frontend/ trees so no other
# track ever needs a path that reaches into backend's internals directly.
SHARED_REGISTRY_FILE = REPO_BASE_PATH.parent.parent / "shared" / "modules-registry.json"


def load_modules_registry() -> dict:
    """Load the dynamic modules registry from disk."""
    if MODULES_REGISTRY_FILE.exists():
        with open(MODULES_REGISTRY_FILE, "r", encoding="utf-8") as fh:
            return json.load(fh)
    return {"modules": {}}


def save_modules_registry(registry: dict):
    """Save the dynamic modules registry to disk, and publish a copy to
    SHARED_REGISTRY_FILE for other tracks to read — the only sanctioned
    way another track learns about registered modules."""
    MODULES_REGISTRY_FILE.parent.mkdir(parents=True, exist_ok=True)
    with open(MODULES_REGISTRY_FILE, "w", encoding="utf-8") as fh:
        json.dump(registry, fh, indent=2, ensure_ascii=False)

    SHARED_REGISTRY_FILE.parent.mkdir(parents=True, exist_ok=True)
    with open(SHARED_REGISTRY_FILE, "w", encoding="utf-8") as fh:
        json.dump(registry, fh, indent=2, ensure_ascii=False)


def register_module(mod: str, description: str = "") -> dict:
    """Register a new module or return its existing registration."""
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
    Path for a specific version of a module.
    Version 1 = modules/[MODCODE]/ (no suffix), version 2 = modules/[MODCODE]/v2/, etc.
    """
    registry = load_modules_registry()
    mod_entry = registry.get("modules", {}).get(mod)
    if not mod_entry:
        return get_module_path(mod)

    if version is None:
        version = mod_entry.get("current_version") or 1

    if version == 1:
        return REPO_BASE_PATH / "modules" / mod
    return REPO_BASE_PATH / "modules" / mod / f"v{version}"

# ─────────────────────────────────────────────
# MODULE FOLDER STRUCTURE — backend stages only
# ─────────────────────────────────────────────

MODULE_STRUCTURE = {
    "P0":       "P0",        # Platform Inception outputs
    "P0_5":     "P0_5",      # PRD Engine: prd-{mod}.md
    "P1":       "P1",        # SRS outputs
    "P2":       "P2",        # DB Script outputs
    "P2_5":     "P2_5",      # UI/UX Design Engine text outputs:
                              # flow-diagram.md, ui-ux-spec.md
                              # (visual-mockups/ lives in the frontend repo)
    "P3_1":     "P3_1",      # Backend Execution Plan
    "P3_5_BE":  "P3_5_BE",   # Backend Test Plan + test-execution-manifest.md
    "packages": "packages",
}

BACKEND_STAGES = ("P0", "P0_5", "P1", "P2", "P2_5", "P3_1", "P3_5_BE")

# ─────────────────────────────────────────────
# ARTIFACT FILENAMES — exact names produced by the real governance
# engines (verified directly against PRD-ENGINE.md,
# PROJECT-1-SRS-GOVERNANCE-ENGINE.md, PROJECT-2-DATABASE-GOVERNANCE-
# ENGINE.md, UI-UX-DESIGN-ENGINE.md, PROJECT-3-BACKEND-ENGINE.md)
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
        # visual-mockups/ is a directory, lives in the frontend repo
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
}

# Files archived once at the governance root rather than per-module.
# Empty since master-registry.md was removed — agent2_archive.py handles an
# empty list fine (it simply produces no SHARED operations).
SHARED_FILES: list[str] = []

# ─────────────────────────────────────────────
# PACKAGES STRUCTURE — backend-execution-plan.md / backend-test-plan.md splits
# ─────────────────────────────────────────────

# Each entry maps an artifact package to the set of sub-folders that are
# PRE-CREATED for it. Agent 3 writes into these folders.
#
#   backend-execution : one folder per canonical PHASE key. Agent 3 Stage 2
#                       writes each phase's package file(s) INTO its phase
#                       folder. These are the eight canonical backend
#                       execution phases (PROJECT-3-REGISTRY.md Section 2 /
#                       PROJECT-3-BACKEND-ENGINE.md) — no more, no less.
#                       (The old dead "SECTIONS" folder was removed —
#                       nothing ever wrote into it. Unmarked top-level
#                       content is now captured by Agent 3 as a flat
#                       _SECTIONS.md file instead — see agent3_splitter.py.)
#
#   backend-test      : NO pre-created sub-folders. backend-test-plan.md
#                       splits into flat files written DIRECTLY inside
#                       packages/backend-test/ (RULE-SCENARIOS.md,
#                       API-SCENARIOS.md, or one whole-phase file below
#                       threshold). Pre-creating RULE-SCENARIOS/ and
#                       API-SCENARIOS/ as *folders* while Agent 3 wrote
#                       them as *files* was FINDING-19 — the folders were
#                       always dead. The container packages/backend-test/
#                       itself is still created (see ensure_module_structure).
PACKAGES_STRUCTURE = {
    "backend-execution": [
        "CORE",
        "DATA-DOM",
        "SVC-API",
        "DOC",
        "INT-C",
        "INT-R",
        "SEC-BE",
        "ALIGN-BE",
    ],
    "backend-test": [],
}

# ─────────────────────────────────────────────
# CANONICAL PHASE KEYS — per artifact file identity. Used by the semantic
# validator (marker_parser.validate_semantics) to reject any PHASE marker
# whose key is not one of the file's canonical keys BEFORE splitting — so a
# typo like <!-- PHASE:SVC+API:START --> (+ instead of -) is caught as a
# blocking error instead of being silently skipped by Agent 3 Stage 2.
# Verified against PROJECT-3-REGISTRY.md Section 2 (Phase Ownership Index)
# and PROJECT-3-BACKEND-ENGINE.md.
# ─────────────────────────────────────────────

CANONICAL_PHASE_KEYS = {
    "backend-execution-plan.md": [
        "CORE", "DATA-DOM", "SVC-API", "DOC",
        "INT-C", "INT-R", "SEC-BE", "ALIGN-BE",
    ],
    "backend-test-plan.md": [
        "TEST-PLAN-BE",
    ],
}

# Files whose SUB labels are EXEMPT from the phase-qualification rule
# (AMEND-P3-N). A test-plan file contains exactly one phase, so its two
# possible SUB labels (RULE-SCENARIOS / API-SCENARIOS) can never collide
# across phases and are deliberately left un-prefixed. Every OTHER file
# must phase-qualify every SUB: SUB:{PHASE-KEY}-{LABEL}.
SUB_QUALIFICATION_EXEMPT = {
    "backend-test-plan.md",
}


def classify_artifact(filename: str) -> str:
    """Return the canonical artifact filename key for a path/name, or ''.

    File identity drives the semantic checks (canonical phase keys, SUB
    qualification). Matching is on the basename so an absolute or relative
    path both resolve correctly.
    """
    name = Path(filename).name
    if name in CANONICAL_PHASE_KEYS:
        return name
    return ""


# ─────────────────────────────────────────────
# SPLIT THRESHOLDS — data-driven, AUTO-checked by the splitter.
#
# When a phase's countable element (an atomic marker kind) reaches its
# threshold, PROJECT-3-REGISTRY.md Section 5.7.4 expects the generating engine
# to have introduced SUB blocks. Agent 3 verifies this AUTOMATICALLY. The check
# is deliberately FLEXIBLE:
#   • advisory by default (a warning, never blocks) — because the split
#     decision is ultimately semantic, and a phase legitimately at the trigger
#     count with no meaningful sub-grouping is a judgment call, not an error;
#   • escalated to BLOCKING only when the caller passes --strict-thresholds;
#   • only phases whose trigger is countable FROM MARKERS are listed here.
#     DATA-DOM's "Entities ≥ 5" trigger is intentionally omitted: entities are
#     not a marker kind, so it cannot be counted structurally and is left to
#     the generating engine's own self-check. "Methods ≥ 6" for SVC-API is
#     likewise not marker-countable; API count is used as the structural proxy.
#
# To change a threshold, edit one row here — nothing else.
# Each rule: kind = atomic marker to count; count/op = the trigger comparison.
# ─────────────────────────────────────────────

PHASE_SPLIT_THRESHOLDS = {
    "SVC-API":      {"kind": "api", "count": 8,  "op": ">=", "grouping": "CRUD / SEARCH / INT"},
    "INT-C":        {"kind": "xm",  "count": 5,  "op": ">=", "grouping": "per target module"},
    "INT-R":        {"kind": "xm",  "count": 5,  "op": ">=", "grouping": "per target module"},
    "TEST-PLAN-BE": {"kind": "tc",  "count": 12, "op": ">",  "grouping": "RULE-SCENARIOS / API-SCENARIOS"},
}

# Phases that must never carry SUB blocks (Section 5.7.4 "Never splits"). If one
# does, it is flagged with the same advisory/flexible severity model.
NEVER_SPLIT_PHASES = {"CORE", "DOC", "SEC-BE", "ALIGN-BE"}

# ─────────────────────────────────────────────
# MARKER PATTERNS — neutral, no backend/frontend distinction in the
# syntax itself (see marker_parser.py)
# ─────────────────────────────────────────────

MARKERS = {
    "phase":  re.compile(r"<!--\s*PHASE:(\w[\w-]*):(START|END)\s*-->"),
    "sub":    re.compile(r"<!--\s*SUB:([\w-]+):(START|END)\s*-->"),
    "api":    re.compile(r"<!--\s*API:(API-[\w-]+):(START|END)\s*-->"),
    "xm":     re.compile(r"<!--\s*XM:(XM-[\w-]+):(START|END)\s*-->"),
    "tc":     re.compile(r"<!--\s*TC:(TC-[\w-]+):(START|END)\s*-->"),
}

ALLOWED_PARENTS = {
    "phase": [None],
    "sub":   ["phase"],
    "api":   ["phase", "sub"],
    "xm":    ["phase", "sub"],
    "tc":    ["phase", "sub"],
}

# ─────────────────────────────────────────────
# HELPERS
# ─────────────────────────────────────────────

def get_module_path(mod: str) -> Path:
    """Root path for a module. Auto-creates nothing — pure path resolution."""
    return REPO_BASE_PATH / "modules" / mod.upper()


def get_stage_path(mod: str, stage: str) -> Path:
    if stage not in MODULE_STRUCTURE:
        raise ValueError(f"Unknown backend stage: {stage}. Valid: {list(MODULE_STRUCTURE.keys())}")
    return get_module_path(mod) / MODULE_STRUCTURE[stage]


def get_packages_path(mod: str, artifact: str, sub: str = "") -> Path:
    base = get_module_path(mod) / "packages" / artifact
    return base / sub if sub else base


def resolve_filename(template: str, mod: str) -> str:
    return template.replace("{mod}", mod.lower())


def validate_module(mod: str, auto_register: bool = False, description: str = "") -> str:
    """
    Validate and normalize a module code.
    auto_register=True registers unknown modules automatically.
    """
    mod = mod.upper().strip()

    if mod in KNOWN_MODULES:
        return mod

    registry = load_modules_registry()
    if mod in registry.get("modules", {}):
        return mod

    if auto_register:
        register_module(mod, description)
        return mod

    raise ValueError(
        f"Module '{mod}' is not registered.\n"
        f"Static modules : {', '.join(KNOWN_MODULES) or '(none yet)'}\n"
        f"Use --auto-register to register it automatically, or add it "
        f"to KNOWN_MODULES in config.py."
    )


def ensure_module_structure(mod: str) -> list[Path]:
    """
    Create every backend stage folder + packages subfolder for a module
    if missing — idempotent, safe to call from any tool (agent1
    explicitly, or agent2 automatically when the structure doesn't
    exist yet). Returns the list of paths that were newly created.
    """
    created = []
    for stage in BACKEND_STAGES:
        p = get_stage_path(mod, stage)
        if not p.exists():
            p.mkdir(parents=True, exist_ok=True)
            (p / ".gitkeep").touch()
            created.append(p)
    for artifact, subs in PACKAGES_STRUCTURE.items():
        # Always create the artifact container itself — even when it has no
        # pre-created sub-folders (backend-test), Agent 3 writes flat files
        # directly into this container.
        container = get_packages_path(mod, artifact)
        if not container.exists():
            container.mkdir(parents=True, exist_ok=True)
            (container / ".gitkeep").touch()
            created.append(container)
        for sub in subs:
            p = get_packages_path(mod, artifact, sub)
            if not p.exists():
                p.mkdir(parents=True, exist_ok=True)
                (p / ".gitkeep").touch()
                created.append(p)
    return created


# ─────────────────────────────────────────────
# MANIFEST SCHEMA
# ─────────────────────────────────────────────

def build_manifest(mod: str, version: int = 1) -> dict:
    base = get_module_version_path(mod, version)

    # Paths are stored relative to REPO_BASE_PATH, never absolute — an
    # absolute Path here would bake in the checking-out machine's home
    # directory and this repo's on-disk folder name at generation time,
    # both of which are guaranteed to differ across machines and to
    # drift the moment the repo/folder is renamed or moved.
    def rel(p: Path) -> str:
        return str(p.relative_to(REPO_BASE_PATH))

    return {
        "module":  mod,
        "version": version,
        "status": {
            "archived":  False,
            "split":     False,
            "backend_module_complete": False,  # gate for frontend readiness
            "ui_shell_complete": False,
        },
        "artifacts": {
            "p0":      rel(base / MODULE_STRUCTURE["P0"]),
            "p0_5":    rel(base / MODULE_STRUCTURE["P0_5"]),
            "p1":      rel(base / MODULE_STRUCTURE["P1"]),
            "p2":      rel(base / MODULE_STRUCTURE["P2"]),
            "p2_5":    rel(base / MODULE_STRUCTURE["P2_5"]),
            "p3_1":    rel(base / MODULE_STRUCTURE["P3_1"]),
            "p3_5_be": rel(base / MODULE_STRUCTURE["P3_5_BE"]),
        },
        "registries": {
            "srs":     rel(base / MODULE_STRUCTURE["P1"] / f"registry-srs-{mod.lower()}.md"),
            "db":      rel(base / MODULE_STRUCTURE["P2"] / f"registry-db-{mod.lower()}.md"),
            "exec_be": rel(base / MODULE_STRUCTURE["P3_1"] / f"registry-exec-be-{mod.lower()}.md"),
            "test_be": rel(base / MODULE_STRUCTURE["P3_5_BE"] / f"registry-test-be-{mod.lower()}.md"),
        },
        "packages": {
            "backend_execution": rel(base / "packages" / "backend-execution"),
            "backend_test":      rel(base / "packages" / "backend-test"),
        },
    }


def get_next_version(mod: str) -> int:
    registry = load_modules_registry()
    entry = registry.get("modules", {}).get(mod)
    if not entry or not entry.get("versions"):
        return 1
    return max(entry["versions"]) + 1


def set_current_version(mod: str, version: int):
    registry = load_modules_registry()
    if mod not in registry["modules"]:
        register_module(mod)
        registry = load_modules_registry()
    entry = registry["modules"][mod]
    if version not in entry["versions"]:
        entry["versions"].append(version)
    entry["current_version"] = version
    save_modules_registry(registry)
