"""
discovery.py — resolves everything generate.py needs from just --module.
════════════════════════════════════════════════════════════════════
This is neither an extractor, a renderer, nor the sync layer — it's the
plumbing that runs *before* extraction, turning "ORG" into the concrete
inputs (an OpenAPI source, a module source root, zero or more shared
common-source roots, an output directory) the existing pipeline already
knows how to consume. Nothing downstream needs to change to accept
discovered inputs instead of hand-typed ones — they're the same shapes
generator.run() already accepted.

Everything here is derived from real, versioned repository artifacts that
this platform already depends on for other reasons, not from guessing a
module's name:

  - governance's location inside this backend/ checkout (governance/ is a
    subfolder of backend/ as of the backend/frontend governance split;
    see backend/governance/governance-tools/README.md)
  - the Maven descriptors (root pom.xml; its <modules> reactor list when one
    exists, each module's own <dependencies> otherwise) for module source +
    shared/common source roots. This platform currently builds as a single
    consolidated POM, which find_common_source_roots handles explicitly.
  - the GroupedOpenApi bean declarations for the springdoc group id,
    package(s), and display name that identify "which module is ORG"
  - application*.properties' own server.port / springdoc.api-docs.path /
    server.servlet.context-path for the dev server's OpenAPI URL

Every discovery step degrades to "not found" rather than guessing, exactly
like the existing best-effort extractors (security_extractor.py,
exception_extractor.py) already do. Callers (generate.py) turn a "not found"
into a clear message naming the explicit override flag to pass instead.
"""

import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Optional

GENERATOR_ROOT = Path(__file__).resolve().parent           # .../backend/governance/governance-tools/api-doc-generator
GOVERNANCE_ROOT = GENERATOR_ROOT.parent.parent              # .../backend/governance
BACKEND_ROOT = GOVERNANCE_ROOT.parent                       # .../backend — governance now lives INSIDE backend/, not as its sibling


class DiscoveryError(Exception):
    """Raised when a required input can't be reliably discovered. Always
    carries the name of the explicit override flag the caller should use."""


@dataclass
class OpenApiGroupInfo:
    method_name: str
    group_id: str
    display_name: Optional[str]
    packages: list[str] = field(default_factory=list)


@dataclass
class RepositoryContext:
    """Everything the extraction pipeline needs, already resolved. This is
    the ONLY object that carries repository-shaped knowledge (locations,
    which module, which shared resources) across the discovery -> pipeline
    boundary. generator.build_document() is the only consumer that unpacks
    it; individual extractors keep taking their own narrow, explicit inputs
    (a Path, a dict, ...) rather than this object itself, so an extractor's
    signature always says exactly what data it needs and stays usable
    outside of this discovery flow (e.g. from a test) without depending on
    discovery.py at all."""
    module: str
    openapi_source: str
    output: Path
    source_root: Optional[Path] = None
    common_source_roots: list[Path] = field(default_factory=list)


def default_backend_root() -> Path:
    return BACKEND_ROOT


def default_output_dir(module: str) -> Path:
    return GOVERNANCE_ROOT / "modules" / module / "api-docs"


def _normalize(s: str) -> str:
    return re.sub(r"[^a-z0-9]", "", s.lower())


# ---------------------------------------------------------------------------
# GroupedOpenApi discovery (springdoc group id, packages, display name)
# ---------------------------------------------------------------------------

_METHOD_RE = re.compile(r"\bGroupedOpenApi\s+(\w+)\s*\(\s*\)\s*\{")
_GROUP_ID_RE = re.compile(r'\.group\(\s*"([^"]+)"\s*\)')
_DISPLAY_NAME_RE = re.compile(r'\.displayName\(\s*"([^"]+)"\s*\)')
_PACKAGES_RE = re.compile(r'\.packagesToScan\(\s*((?:"[^"]*"\s*,?\s*)+)\)', re.DOTALL)
_QUOTED_RE = re.compile(r'"([^"]*)"')


def _brace_span(text: str, open_brace_index: int) -> int:
    """Returns the index just past the matching closing brace for the '{' at
    open_brace_index."""
    depth = 0
    for i in range(open_brace_index, len(text)):
        if text[i] == "{":
            depth += 1
        elif text[i] == "}":
            depth -= 1
            if depth == 0:
                return i + 1
    return len(text)


def _parse_groups_from_source(text: str) -> list[OpenApiGroupInfo]:
    groups: list[OpenApiGroupInfo] = []
    for m in _METHOD_RE.finditer(text):
        body_start = text.index("{", m.end() - 1)
        body_end = _brace_span(text, body_start)
        body = text[body_start:body_end]

        group_m = _GROUP_ID_RE.search(body)
        if not group_m:
            continue
        display_m = _DISPLAY_NAME_RE.search(body)
        packages_m = _PACKAGES_RE.search(body)
        packages = _QUOTED_RE.findall(packages_m.group(1)) if packages_m else []

        groups.append(OpenApiGroupInfo(
            method_name=m.group(1),
            group_id=group_m.group(1),
            display_name=display_m.group(1) if display_m else None,
            packages=packages,
        ))
    return groups


def find_openapi_groups(backend_root: Path) -> list[OpenApiGroupInfo]:
    groups: list[OpenApiGroupInfo] = []
    for path in sorted(backend_root.rglob("*.java")):
        text = path.read_text(encoding="utf-8", errors="ignore")
        if "GroupedOpenApi.builder()" not in text:
            continue
        groups.extend(_parse_groups_from_source(text))
    return groups


def match_openapi_group(groups: list[OpenApiGroupInfo], module: str) -> Optional[OpenApiGroupInfo]:
    """Matches --module against method name / group id / display name first
    (specific, intentional identifiers); only falls back to packagesToScan if
    nothing matched there, then breaks ties by preferring the group that
    scans the fewest packages -- so a combined "all modules" group (which by
    construction lists every module's package) never wins over a genuine
    single-module group. Returns None on no match or genuine ambiguity --
    callers must fall back to an explicit override, never guess."""
    norm_module = _normalize(module)
    if not norm_module:
        return None

    tier1 = [
        g for g in groups
        if norm_module in _normalize(g.method_name)
        or norm_module in _normalize(g.group_id)
        or norm_module in _normalize(g.display_name or "")
    ]
    if len(tier1) == 1:
        return tier1[0]
    if len(tier1) > 1:
        return None  # genuinely ambiguous among specific identifiers

    tier2 = [g for g in groups if any(norm_module in _normalize(p) for p in g.packages)]
    if not tier2:
        return None
    fewest = min(len(g.packages) for g in tier2)
    narrowed = [g for g in tier2 if len(g.packages) == fewest]
    return narrowed[0] if len(narrowed) == 1 else None


# ---------------------------------------------------------------------------
# Server / springdoc URL discovery
# ---------------------------------------------------------------------------
# The OpenAPI document's URL is NOT "localhost:8080/api-docs/<group>" by
# assumption -- all three of its parts are configuration this backend already
# declares in its own application*.properties, and all three move
# independently:
#
#   server.port                  -- this platform runs on 7272, not Spring's 8080 default
#   springdoc.api-docs.path      -- springdoc's own default is /v3/api-docs, and a group is
#                                   served at <that path>/<groupId>; /api-docs is NOT a
#                                   valid fallback, it is simply a different path
#   server.servlet.context-path  -- prefixes everything when set
#
# So they are read from the property files themselves. The @Value("${server.port:NNNN}")
# Java form is still honoured as a fallback for a backend that declares the
# default in code instead, and only then does a literal default apply.

_PORT_VALUE_ANNOTATION_RE = re.compile(r'@Value\(\s*"\$\{server\.port:(\d+)\}"\s*\)')

_PROPERTY_RE_CACHE: dict[str, re.Pattern] = {}

SPRINGDOC_DEFAULT_API_DOCS_PATH = "/v3/api-docs"   # springdoc-openapi's own documented default


def _property_files(backend_root: Path) -> list[Path]:
    """application.properties first (the un-profiled base every profile
    inherits), then any profile-specific file, so a base declaration wins
    over a profile override this tool has no way to know is active."""
    resources = backend_root / "src" / "main" / "resources"
    if not resources.is_dir():
        return []
    base = resources / "application.properties"
    files = [base] if base.exists() else []
    files += sorted(p for p in resources.glob("application-*.properties") if p != base)
    return files


def _read_property(backend_root: Path, key: str) -> Optional[str]:
    pattern = _PROPERTY_RE_CACHE.get(key)
    if pattern is None:
        pattern = re.compile(rf"^\s*{re.escape(key)}\s*[=:]\s*(\S+)\s*$", re.MULTILINE)
        _PROPERTY_RE_CACHE[key] = pattern
    for path in _property_files(backend_root):
        m = pattern.search(path.read_text(encoding="utf-8", errors="ignore"))
        if m:
            return m.group(1)
    return None


def find_server_port(backend_root: Path, default: str = "8080") -> str:
    declared = _read_property(backend_root, "server.port")
    if declared and declared.isdigit():
        return declared
    for path in sorted(backend_root.rglob("*.java")):
        text = path.read_text(encoding="utf-8", errors="ignore")
        m = _PORT_VALUE_ANNOTATION_RE.search(text)
        if m:
            return m.group(1)
    return default


def find_api_docs_path(backend_root: Path) -> str:
    path = _read_property(backend_root, "springdoc.api-docs.path") or SPRINGDOC_DEFAULT_API_DOCS_PATH
    return "/" + path.strip("/")


def find_context_path(backend_root: Path) -> str:
    path = _read_property(backend_root, "server.servlet.context-path")
    if not path or path.strip("/") == "":
        return ""
    return "/" + path.strip("/")


def build_openapi_url(backend_root: Path, group_id: str) -> str:
    """The live URL springdoc actually serves this group at, assembled from
    the backend's own declared configuration rather than assumed."""
    port = find_server_port(backend_root)
    context = find_context_path(backend_root)
    api_docs = find_api_docs_path(backend_root)
    return f"http://localhost:{port}{context}{api_docs}/{group_id}"


# ---------------------------------------------------------------------------
# Module source root discovery (from a matched group's packages)
# ---------------------------------------------------------------------------

def find_module_source_root(backend_root: Path, packages: list[str]) -> Optional[Path]:
    """Given the controller package(s) a matched OpenAPI group scans, finds
    which Maven module's src/main/java actually contains that package, then
    returns the module's own domain-root directory rather than the whole
    src/main/java tree -- so module-own best-effort extractors (error codes,
    throw-site Status associations, controller/service lookup) only ever see
    this module's own code, never another module's. This matters most under a
    single consolidated POM layout (src/main/java directly under
    backend_root, e.g. this platform's own erp-system, as opposed to one
    Maven module directory per domain) where every module's code shares one
    physical src/main/java tree and would otherwise all be visible at once.

    Every GroupedOpenApi bean on this platform scans a "<domain>.controller"
    leaf package (e.g. "com.erp.security.controller"), not the module's
    whole domain package -- but the domain's other code (service/,
    exception/, dto/, ...) lives one level up, as siblings of controller/,
    not inside it. So when the matched package's last segment is literally
    "controller", scope to its parent (the domain root) instead of the
    controller subpackage itself; otherwise scope to the matched package
    directory as-is, in case a future module doesn't follow that convention."""
    candidate_roots = [backend_root / "src" / "main" / "java"]
    candidate_roots += sorted(backend_root.glob("*/src/main/java"))
    for java_root in candidate_roots:
        for package in packages:
            package_dir = java_root / Path(*package.split("."))
            if package_dir.is_dir():
                return package_dir.parent if package_dir.name == "controller" else package_dir
    return None


# ---------------------------------------------------------------------------
# Shared/common source discovery (Maven reactor dependency graph)
# ---------------------------------------------------------------------------

_ARTIFACT_ID_RE = re.compile(r"<artifactId>([^<]+)</artifactId>")
_MODULE_RE = re.compile(r"<module>([^<]+)</module>")
_DEPENDENCY_BLOCK_RE = re.compile(r"<dependency>(.*?)</dependency>", re.DOTALL)


def find_common_source_roots(backend_root: Path, module_source_root: Path) -> list[Path]:
    """Resolves every OTHER reactor module that module_source_root's own
    module actually declares as a Maven dependency -- the real, versioned,
    build-enforced link to shared/common code, as opposed to guessing a name
    like "common-utils". Works for any number of shared modules, not just one.

    module_source_root is now package-scoped (see find_module_source_root),
    so its owning Maven module directory (the one with the pom.xml) is found
    by walking upward until a pom.xml turns up, rather than assuming a fixed
    number of parent hops -- that walk naturally lands on backend_root itself
    under a single consolidated POM layout (this platform's own erp-system),
    where no nearer pom.xml exists between the package directory and the
    repo root."""
    root_pom = backend_root / "pom.xml"
    if not root_pom.exists():
        return []
    reactor_modules = set(_MODULE_RE.findall(root_pom.read_text(encoding="utf-8")))

    module_root = module_source_root
    while module_root != backend_root and not (module_root / "pom.xml").exists():
        module_root = module_root.parent

    if module_root == backend_root:
        # Single consolidated POM (no <modules> reactor split) -- there is no
        # separate shared module to resolve, since shared/common code
        # (com.erp.common, GlobalExceptionHandler, OperationCodeImpl, ...)
        # already lives inside this same src/main/java tree. Search that
        # whole tree rather than just this module's own package.
        return [backend_root / "src" / "main" / "java"]

    module_pom = module_root / "pom.xml"
    if not module_pom.exists():
        return []
    module_text = module_pom.read_text(encoding="utf-8")

    dep_artifacts = set()
    for dep_block in _DEPENDENCY_BLOCK_RE.findall(module_text):
        m = _ARTIFACT_ID_RE.search(dep_block)
        if m:
            dep_artifacts.add(m.group(1))

    roots = []
    for artifact in sorted(dep_artifacts & reactor_modules):
        candidate = backend_root / artifact / "src" / "main" / "java"
        if candidate.is_dir():
            roots.append(candidate)
    return roots


# ---------------------------------------------------------------------------
# Top-level resolve()
# ---------------------------------------------------------------------------

def resolve(
    module: str,
    *,
    backend_root: Optional[Path] = None,
    openapi_override: Optional[str] = None,
    source_override: Optional[Path] = None,
    common_source_overrides: Optional[list[Path]] = None,
    output_override: Optional[Path] = None,
) -> RepositoryContext:
    backend_root = backend_root or default_backend_root()
    output = output_override or default_output_dir(module)

    if openapi_override and source_override:
        # Every input explicitly given -- no repository discovery needed at all.
        return RepositoryContext(
            module=module,
            openapi_source=openapi_override,
            output=output,
            source_root=source_override,
            common_source_roots=common_source_overrides or [],
        )

    if not backend_root.exists():
        if not openapi_override:
            raise DiscoveryError(
                f"Backend repository not found at expected path '{backend_root}'. "
                f"Pass --openapi explicitly (and --source, if permission/error-code sections are wanted)."
            )
        return RepositoryContext(module=module, openapi_source=openapi_override, output=output,
                               source_root=source_override, common_source_roots=common_source_overrides or [])

    groups = find_openapi_groups(backend_root)
    matched = match_openapi_group(groups, module)

    openapi_source = openapi_override
    source_root = source_override
    common_source_roots = list(common_source_overrides or [])

    if openapi_source is None:
        if matched is None:
            raise DiscoveryError(
                f"Could not identify a unique springdoc GroupedOpenApi group for module '{module}' "
                f"under '{backend_root}'. Pass --openapi explicitly (a live URL or a saved JSON file)."
            )
        openapi_source = build_openapi_url(backend_root, matched.group_id)

    if source_root is None and matched is not None:
        source_root = find_module_source_root(backend_root, matched.packages)

    if not common_source_overrides and source_root is not None:
        common_source_roots = find_common_source_roots(backend_root, source_root)

    return RepositoryContext(
        module=module,
        openapi_source=openapi_source,
        output=output,
        source_root=source_root,
        common_source_roots=common_source_roots,
    )
