"""
Best-effort permission discovery from Java source.

Global auth requirement (is this endpoint behind the security filter chain at
all, and which scheme) is already fully discoverable straight from the
OpenAPI JSON — see openapi_extractor._operation_auth — because Spring Security
applies globally and OpenApiConfig declares one Bearer scheme applied to every
operation. That part needs no source access and lives in openapi_extractor.

What is NOT visible in the OpenAPI JSON is the specific business permission
(e.g. "BRANCH_CREATE") required to call an endpoint, because this codebase
puts `@PreAuthorize` in two different places depending on the module:
  - most modules: on the *service* method
  - some modules: directly on the *controller* method

There is no springdoc customizer projecting this into the OpenAPI doc, so the
only way to discover it is to read the Java source: check the controller
method first, and if nothing is found there, resolve the service class the
controller method delegates to (by convention: `xxxService.methodName(...)`
inside the method body, matched back to a `private final X xxxService;`
field) and check the same-named method on that service class.

This is a heuristic over source text (regex-based), not a real Java parser.
The common case is one delegate call per controller method under the same
method name (e.g. `MasterLookupController.create` -> `MasterLookupService.
create`), tried first. Some controllers in this codebase legitimately use a
differently-named delegate instead — e.g. `MasterLookupController.
createDetail` calls `LookupDetailService.create`, not `.createDetail` — so a
same-name-only match would silently drop the permission for every one of
those endpoints. As a fallback, when exactly one call is made against one of
the controller's own `private final XxxService` fields (matched by type name
ending in "Service", the convention every service field in this codebase
follows), that call is treated as the delegate regardless of its method name.
If more than one such call exists (genuine ambiguity) or none does, it
returns nothing — never guesses.
"""

import re
from pathlib import Path
from typing import Optional

PREAUTH_RE = re.compile(r'@(?:PreAuthorize|Secured)\s*\(\s*"([^"]*)"\s*\)')
# The SpEL form is hasAuthority(T(<fully.qualified.Holder>).CONSTANT). The
# holder class name is a per-project choice (this platform uses
# PermissionConstants; an earlier one used SecurityPermissions), so it is
# matched structurally -- any T(...) type reference followed by a SCREAMING_CASE
# constant -- rather than by a hardcoded class name that silently yields zero
# permissions the moment the class is renamed.
PERMISSION_CONST_RE = re.compile(r"T\(\s*[\w.]+\s*\)\s*\.\s*([A-Z][A-Z0-9_]*)")
# The literal form, hasAuthority('PERM_X') / hasRole("ADMIN").
PERMISSION_LITERAL_RE = re.compile(r"has(?:Authority|Role)\(\s*['\"]([A-Za-z0-9_]+)['\"]\s*\)")
FIELD_DECL_TEMPLATE = r"private\s+final\s+(\w+)\s+{name}\s*;"
SERVICE_FIELD_DECL_RE = re.compile(r"private\s+final\s+(\w+Service)\s+(\w+)\s*;")

CLASS_REQUEST_MAPPING_RE = re.compile(r'@RequestMapping\(\s*"([^"]*)"\s*\)')
MAPPING_ANNOTATION_RE = re.compile(
    r'@(GetMapping|PostMapping|PutMapping|PatchMapping|DeleteMapping)(?:\(\s*"([^"]*)"\s*\))?'
)
MAPPING_VERB = {
    "GetMapping": "GET", "PostMapping": "POST", "PutMapping": "PUT",
    "PatchMapping": "PATCH", "DeleteMapping": "DELETE",
}
METHOD_NAME_AFTER_ANNOTATION_RE = re.compile(r"\b(?:public|private|protected)\b[^;{]*?\b(\w+)\s*\(")


def _combine_path(base: str, sub: str) -> str:
    base = (base or "").rstrip("/")
    sub = sub or ""
    if sub and not sub.startswith("/"):
        sub = "/" + sub
    return (base + sub) or "/"


def find_controller_for_endpoint(source_root: Path, http_method: str, path: str) -> tuple[Optional[Path], Optional[str]]:
    """Matches an Endpoint back to its controller source file + Java method
    name by re-deriving each controller method's full route (class-level
    @RequestMapping + method-level @XxxMapping) and comparing verb+path —
    more reliable than guessing by method name, since method names like
    "create"/"search" repeat across every controller in this codebase."""
    for controller_file in sorted(source_root.rglob("*Controller.java")):
        text = controller_file.read_text(encoding="utf-8")
        class_match = CLASS_REQUEST_MAPPING_RE.search(text)
        base_path = class_match.group(1) if class_match else ""

        for m in MAPPING_ANNOTATION_RE.finditer(text):
            verb = MAPPING_VERB[m.group(1)]
            if verb != http_method.upper():
                continue
            full_path = _combine_path(base_path, m.group(2) or "")
            if full_path != path:
                continue
            name_match = METHOD_NAME_AFTER_ANNOTATION_RE.search(text[m.end():])
            if name_match:
                return controller_file, name_match.group(1)

    return None, None


def _find_declaration_index(lines: list[str], method_name: str) -> Optional[int]:
    pattern = re.compile(rf"\b(public|private|protected)\b.*\b{re.escape(method_name)}\s*\(")
    for i, line in enumerate(lines):
        if pattern.search(line):
            return i
    return None


def _collect_annotations_above(lines: list[str], decl_index: int) -> list[str]:
    """Walks upward collecting annotation lines directly above the method
    declaration. Tracks paren-depth so a multi-line annotation like
    `@Operation(\n    summary = "...",\n    description = "..."\n)` gets
    skipped as one unit instead of being mistaken for the end of the block
    on its first (non-"@"-prefixed) continuation line."""
    annotations: list[str] = []
    depth = 0
    i = decl_index - 1
    while i >= 0:
        line = lines[i]
        net = line.count("(") - line.count(")")
        depth -= net
        if depth > 0:
            i -= 1
            continue
        depth = 0

        stripped = line.strip()
        if not stripped:
            i -= 1
            continue
        if stripped.startswith("*") or stripped.startswith("/**") or stripped.endswith("*/"):
            i -= 1
            continue
        if stripped.startswith("@"):
            annotations.append(stripped)
            i -= 1
            continue
        break
    return annotations


def _method_body_span(lines: list[str], decl_index: int) -> tuple[int, int]:
    """Returns (start, end) line indices covering the method body, found by
    brace-balance counting starting from the first '{' at/after decl_index."""
    depth = 0
    started = False
    start = decl_index
    for i in range(decl_index, len(lines)):
        for ch in lines[i]:
            if ch == "{":
                depth += 1
                started = True
            elif ch == "}":
                depth -= 1
        if started and depth <= 0:
            return start, i
    return start, len(lines) - 1


def _find_delegate(source: str, lines: list[str], decl_index: int, method_name: str) -> tuple[Optional[str], Optional[str]]:
    """Returns (service_class_name, service_method_name). Tries the same-name
    call first, then falls back to the single call made against a known
    `XxxService`-typed field, whatever its method name — see module
    docstring for why the fallback is needed."""
    start, end = _method_body_span(lines, decl_index)
    body = "\n".join(lines[start:end + 1])

    same_name_match = re.search(rf"\b(\w+)\.{re.escape(method_name)}\s*\(", body)
    if same_name_match:
        var_name = same_name_match.group(1)
        field_match = re.search(FIELD_DECL_TEMPLATE.format(name=re.escape(var_name)), source)
        if field_match:
            return field_match.group(1), method_name

    service_fields = SERVICE_FIELD_DECL_RE.findall(source)
    candidates = []
    for field_type, field_name in service_fields:
        for m in re.finditer(rf"\b{re.escape(field_name)}\.(\w+)\s*\(", body):
            candidates.append((field_type, m.group(1)))
    if len(candidates) == 1:
        return candidates[0]
    return None, None


def find_preauthorize(source: str, method_name: str) -> Optional[str]:
    lines = source.splitlines()
    decl_index = _find_declaration_index(lines, method_name)
    if decl_index is None:
        return None
    for annotation in _collect_annotations_above(lines, decl_index):
        m = PREAUTH_RE.search(annotation)
        if m:
            return m.group(1)
    return None


def find_delegate(source: str, method_name: str) -> tuple[Optional[str], Optional[str]]:
    lines = source.splitlines()
    decl_index = _find_declaration_index(lines, method_name)
    if decl_index is None:
        return None, None
    return _find_delegate(source, lines, decl_index, method_name)


def extract_permission_constants(spel_expression: str) -> list[str]:
    found = PERMISSION_CONST_RE.findall(spel_expression)
    found += [c for c in PERMISSION_LITERAL_RE.findall(spel_expression) if c not in found]
    return found


def resolve_permission(
    controller_source: str, method_name: str, source_root: Path
) -> tuple[list[str], Optional[str], Optional[str]]:
    """Returns (permission_constants, source_label, raw_expression).
    source_label is "controller" or "service:<ClassName>".

    The raw expression is returned alongside the constants because a real
    authorization rule does not always name a permission constant -- e.g.
    `@PreAuthorize("isAuthenticated()")`. Returning only constants made such
    an endpoint indistinguishable from one with no check at all, which reads
    as "unprotected" to anyone building a client or a test against these docs.
    (None, None, None) still means genuinely not discoverable -- never a guess."""
    expr = find_preauthorize(controller_source, method_name)
    if expr:
        return extract_permission_constants(expr), "controller", expr

    service_class, service_method = find_delegate(controller_source, method_name)
    if not service_class or not service_method:
        return [], None, None

    matches = list(source_root.rglob(f"{service_class}.java"))
    if not matches:
        return [], None, None

    service_source = matches[0].read_text(encoding="utf-8")
    expr = find_preauthorize(service_source, service_method)
    if not expr:
        return [], None, None
    return extract_permission_constants(expr), f"service:{service_class}", expr
