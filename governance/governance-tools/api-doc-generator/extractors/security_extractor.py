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
  - erp-org / erp-finance-gl / erp-masterdata: on the *service* method
  - erp-security: directly on some *controller* methods

There is no springdoc customizer projecting this into the OpenAPI doc, so the
only way to discover it is to read the Java source: check the controller
method first, and if nothing is found there, resolve the service class the
controller method delegates to (by convention: `xxxService.methodName(...)`
inside the method body, matched back to a `private final X xxxService;`
field) and check the same-named method on that service class.

This is a heuristic over source text (regex-based), not a real Java parser.
It only works because this codebase consistently uses one delegate call per
controller method under the same method name. If it can't confidently
resolve a permission, it returns nothing — never guesses.
"""

import re
from pathlib import Path
from typing import Optional

PREAUTH_RE = re.compile(r'@(?:PreAuthorize|Secured)\s*\(\s*"([^"]*)"\s*\)')
PERMISSION_CONST_RE = re.compile(r"SecurityPermissions\)\.([A-Z0-9_]+)")
FIELD_DECL_TEMPLATE = r"private\s+final\s+(\w+)\s+{name}\s*;"

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


def _find_delegate_class(source: str, lines: list[str], decl_index: int, method_name: str) -> Optional[str]:
    start, end = _method_body_span(lines, decl_index)
    body = "\n".join(lines[start:end + 1])
    call_match = re.search(rf"\b(\w+)\.{re.escape(method_name)}\s*\(", body)
    if not call_match:
        return None
    var_name = call_match.group(1)
    field_match = re.search(FIELD_DECL_TEMPLATE.format(name=re.escape(var_name)), source)
    return field_match.group(1) if field_match else None


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


def find_delegate_class_name(source: str, method_name: str) -> Optional[str]:
    lines = source.splitlines()
    decl_index = _find_declaration_index(lines, method_name)
    if decl_index is None:
        return None
    return _find_delegate_class(source, lines, decl_index, method_name)


def extract_permission_constants(spel_expression: str) -> list[str]:
    return PERMISSION_CONST_RE.findall(spel_expression)


def resolve_permission(controller_source: str, method_name: str, source_root: Path) -> tuple[list[str], Optional[str]]:
    """Returns (permission_constants, source_label). source_label is
    "controller" or "service:<ClassName>". Empty list + None means
    not discoverable — caller must omit the field entirely, never guess."""
    expr = find_preauthorize(controller_source, method_name)
    if expr:
        return extract_permission_constants(expr), "controller"

    service_class = find_delegate_class_name(controller_source, method_name)
    if not service_class:
        return [], None

    matches = list(source_root.rglob(f"{service_class}.java"))
    if not matches:
        return [], None

    service_source = matches[0].read_text(encoding="utf-8")
    expr = find_preauthorize(service_source, method_name)
    if not expr:
        return [], None
    return extract_permission_constants(expr), f"service:{service_class}"
