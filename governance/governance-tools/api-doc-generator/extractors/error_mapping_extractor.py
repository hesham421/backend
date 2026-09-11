"""
Best-effort discovery of error-code -> HTTP-status information that has no
OpenAPI representation at all (no controller anywhere declares
@ApiResponse/@ApiResponses, so springdoc never sees this).

Two distinct, shared, module-independent files carry this information across
every module of this ERP platform's backend architecture:

  - OperationCodeImpl's static Status -> HttpStatus table is the ONLY place
    that mapping exists (its own doc comment says so). It's shared and
    identical for every module, so it's read once from wherever the caller's
    discovered common-source roots point, never per module.

  - GlobalExceptionHandler's @ExceptionHandler methods hardcode the HTTP
    status for framework-level exceptions (validation, malformed JSON, method
    not allowed, ...) that every endpoint in every module can produce. These
    are read the same way, once, from the shared common-source roots.

A third, per-module piece -- which business Status a specific module's own
error code (e.g. a name-duplicate or cycle-detection code) was thrown with --
is NOT centralized; it only exists at each module's own throw sites
(`new BusinessException(Status.X, SomeErrorCodes.Y, ...)` /
`new LocalizedException(Status.X, SomeErrorCodes.Y, ...)`). That part is
read from the module's own --source, one module at a time, and only recorded
when a throw site literally names both together -- never guessed from a
code's name or value.
"""

import re
from pathlib import Path
from typing import Optional

from models.api_doc_model import ApiDocument, ErrorCode, PossibleError, StatusMapping

STATUS_MAPPING_RE = re.compile(r"statusMappings\.put\(\s*Status\.(\w+)\s*,\s*HttpStatus\.(\w+)\s*\)")
CREATE_ERROR_CODE_RE = re.compile(r'createError\(\s*"([A-Z0-9_]+)"')
RESPONSE_STATUS_RE = re.compile(r"ResponseEntity\.status\(\s*HttpStatus\.(\w+)\s*\)")
THROW_RE = re.compile(r"new\s+(?:BusinessException|LocalizedException)\(\s*Status\.(\w+)\s*,\s*\w+\.(\w+)")


def find_status_http_mapping(common_source_roots: list[Path]) -> dict[str, str]:
    """Status enum constant name -> HttpStatus constant name, parsed from the
    shared OperationCodeImpl's static mapping table."""
    for root in common_source_roots:
        for path in sorted(root.rglob("OperationCodeImpl.java")):
            text = path.read_text(encoding="utf-8")
            mapping = {m.group(1): m.group(2) for m in STATUS_MAPPING_RE.finditer(text)}
            if mapping:
                return mapping
    return {}


def _split_handler_methods(text: str) -> list[str]:
    parts = re.split(r"(?=@ExceptionHandler)", text)
    return parts[1:]


def find_framework_error_codes(common_source_roots: list[Path]) -> list[ErrorCode]:
    """Framework-level error codes (apply to any endpoint, any module) with
    their real HTTP status, parsed from the shared GlobalExceptionHandler."""
    codes: list[ErrorCode] = []
    seen: set[str] = set()
    for root in common_source_roots:
        for path in sorted(root.rglob("GlobalExceptionHandler.java")):
            text = path.read_text(encoding="utf-8")
            try:
                rel = str(path.relative_to(root))
            except ValueError:
                rel = path.name
            for chunk in _split_handler_methods(text):
                code_m = CREATE_ERROR_CODE_RE.search(chunk)
                status_m = RESPONSE_STATUS_RE.search(chunk)
                if not code_m or not status_m:
                    continue
                code = code_m.group(1)
                if code in seen:
                    continue
                seen.add(code)
                codes.append(ErrorCode(name=code, value=code, source_file=rel, http_status=status_m.group(1)))
    return codes


def find_business_status_associations(source_root: Path) -> dict[str, str]:
    """Best-effort: error-code constant name -> Status constant name, parsed
    from throw sites in the module's own source. Only records a pairing that
    is literally spelled out at a throw site -- never inferred from the
    code's own name or value."""
    associations: dict[str, str] = {}
    for path in sorted(source_root.rglob("*.java")):
        text = path.read_text(encoding="utf-8")
        for m in THROW_RE.finditer(text):
            status_name, const_name = m.groups()
            associations.setdefault(const_name, status_name)
    return associations


def enrich_error_codes(
    module_error_codes: list[ErrorCode],
    source_root: Optional[Path],
    common_source_roots: list[Path],
) -> tuple[list[ErrorCode], list[StatusMapping]]:
    """Fills in .status/.http_status on the module's own error codes where a
    throw site names both, appends the shared framework-level codes, and
    returns the shared Status->HttpStatus table for rendering once. Every
    piece here is best-effort and silently omitted when not found -- never
    fabricated. No-ops entirely if no common_source_roots were discovered."""
    status_http = find_status_http_mapping(common_source_roots)
    status_mappings = [
        StatusMapping(name=name, http_status=http, source_file="OperationCodeImpl.java")
        for name, http in sorted(status_http.items())
    ]

    if source_root is not None:
        associations = find_business_status_associations(source_root)
        for code in module_error_codes:
            status_name = associations.get(code.name)
            if not status_name:
                continue
            code.status = status_name
            http = status_http.get(status_name)
            if http:
                code.http_status = http

    framework_codes = find_framework_error_codes(common_source_roots)
    return module_error_codes + framework_codes, status_mappings


def attach_endpoint_error_codes(document: ApiDocument) -> None:
    """Attaches framework-level errors to individual endpoints, but only
    where BOTH halves of the claim are independently verified real facts:
    (a) a per-endpoint property already established by another extractor
    (requires_auth from the OpenAPI security requirement, permission from a
    real @PreAuthorize/@Secured found by security_extractor, request_body
    from the OpenAPI request body), and (b) a framework error code this run
    actually found in GlobalExceptionHandler, with its real HTTP status --
    looked up by name, never hardcoded, so if a future GlobalExceptionHandler
    stops declaring one of these handlers, this silently stops claiming it
    rather than asserting a stale fact.

    Deliberately narrow: three rules, each unconditionally true of the
    Spring MVC/Security stack this platform runs on, never a guess about
    business logic:
      - requires_auth       -> UNAUTHORIZED (global security filter runs
                                before every authenticated endpoint;
                                AuthenticationException is handled globally)
      - permission found    -> FORBIDDEN (a real @PreAuthorize/@Secured
                                expression was found for this exact endpoint;
                                AccessDeniedException is handled globally)
      - has a request body  -> INVALID_JSON (any @RequestBody is deserialized
                                by Jackson before the controller method runs,
                                unconditionally, independent of whether Bean
                                Validation is also wired up; the framework
                                catches malformed JSON globally)
    VALIDATION_ERROR is deliberately NOT attached here: it would require
    confirming @Valid is present on this specific parameter, which isn't
    verified by anything currently extracted -- attaching it from "has a
    request body" alone would be a convention-based guess, not a proof.
    """
    by_name = {c.name: c for c in document.error_codes if c.http_status}

    def _attach(ep, code_name: str, reason: str) -> None:
        code = by_name.get(code_name)
        if not code:
            return
        ep.possible_errors.append(PossibleError(code=code.name, http_status=code.http_status, reason=reason))

    for ep in document.endpoints:
        if ep.requires_auth:
            _attach(
                ep, "UNAUTHORIZED",
                "Endpoint requires authentication (global security requirement); "
                "GlobalExceptionHandler maps AuthenticationException to this status for every such endpoint.",
            )
        if ep.permission:
            _attach(
                ep, "FORBIDDEN",
                "A specific permission check was found for this endpoint "
                "(@PreAuthorize/@Secured); GlobalExceptionHandler maps AccessDeniedException to this status.",
            )
        if ep.request_body is not None:
            _attach(
                ep, "INVALID_JSON",
                "Endpoint accepts a JSON request body; GlobalExceptionHandler maps "
                "HttpMessageNotReadableException (malformed JSON) to this status for any @RequestBody, unconditionally.",
            )
