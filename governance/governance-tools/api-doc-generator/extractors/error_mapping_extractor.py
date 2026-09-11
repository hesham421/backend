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

# Two shapes of the same shared table are recognised, because this platform
# has used both: an explicit statusMappings.put(...) table in a helper class,
# and -- currently -- the Status enum carrying its own HttpStatus in its
# constructor (NOT_FOUND(HttpStatus.NOT_FOUND), ...). Only matching the first
# meant the whole Status -> HTTP mapping silently came back empty, so every
# error code documented its business Status with a blank HTTP status beside it.
STATUS_MAPPING_RE = re.compile(r"statusMappings\.put\(\s*Status\.(\w+)\s*,\s*HttpStatus\.(\w+)\s*\)")
STATUS_ENUM_CONSTANT_RE = re.compile(r"^\s*([A-Z][A-Z0-9_]*)\s*\(\s*HttpStatus\.(\w+)\s*\)", re.MULTILINE)
STATUS_ENUM_DECL_RE = re.compile(r"\benum\s+Status\b")

# Framework handlers are matched in both the helper form (createError("X"))
# and the builder form this codebase uses (ApiError.builder().code("X")),
# paired with the HTTP status of the same @ExceptionHandler method -- written
# either as ResponseEntity.status(HttpStatus.X) or one of ResponseEntity's
# named shortcuts (badRequest()/notFound()/...).
CREATE_ERROR_CODE_RE = re.compile(r'(?:createError|\.code)\(\s*"([A-Z0-9_]+)"')
RESPONSE_STATUS_RE = re.compile(r"ResponseEntity\.status\(\s*HttpStatus\.(\w+)\s*\)")
RESPONSE_SHORTCUT_RE = re.compile(r"ResponseEntity\.(badRequest|notFound|unprocessableEntity)\s*\(")
RESPONSE_SHORTCUT_STATUS = {
    "badRequest": "BAD_REQUEST",
    "notFound": "NOT_FOUND",
    "unprocessableEntity": "UNPROCESSABLE_ENTITY",
}

# Spring's own HttpStatus constant -> numeric code. A test asserting on a
# status needs the number, and the constant name alone never carries it.
# Only constants this platform's Status enum / GlobalExceptionHandler can
# actually produce are listed; an unlisted one renders as its bare name
# rather than a guessed number.
HTTP_STATUS_CODES = {
    "OK": 200, "CREATED": 201, "ACCEPTED": 202, "NO_CONTENT": 204,
    "BAD_REQUEST": 400, "UNAUTHORIZED": 401, "FORBIDDEN": 403, "NOT_FOUND": 404,
    "METHOD_NOT_ALLOWED": 405, "NOT_ACCEPTABLE": 406, "CONFLICT": 409, "GONE": 410,
    "PAYLOAD_TOO_LARGE": 413, "CONTENT_TOO_LARGE": 413, "URI_TOO_LONG": 414,
    "UNSUPPORTED_MEDIA_TYPE": 415, "UNPROCESSABLE_ENTITY": 422,
    "UNPROCESSABLE_CONTENT": 422, "TOO_MANY_REQUESTS": 429,
    "INTERNAL_SERVER_ERROR": 500, "NOT_IMPLEMENTED": 501, "BAD_GATEWAY": 502,
    "SERVICE_UNAVAILABLE": 503, "GATEWAY_TIMEOUT": 504,
}


def http_status_label(constant: Optional[str]) -> Optional[str]:
    """"CONFLICT" -> "409 CONFLICT". Left as the bare constant when its
    numeric code isn't known -- never guessed."""
    if not constant:
        return constant
    code = HTTP_STATUS_CODES.get(constant)
    return f"{code} {constant}" if code else constant
THROW_RE = re.compile(r"new\s+(?:BusinessException|LocalizedException)\(\s*Status\.(\w+)\s*,\s*\w+\.(\w+)")


def find_status_http_mapping(common_source_roots: list[Path]) -> dict[str, str]:
    return find_status_http_mapping_with_source(common_source_roots)[0]


def find_status_http_mapping_with_source(
    common_source_roots: list[Path],
) -> tuple[dict[str, str], Optional[str]]:
    """(Status constant name -> HttpStatus constant name, the file it was read
    from). The source file is returned rather than assumed, since the table
    has lived in more than one place on this platform."""
    for root in common_source_roots:
        for path in sorted(root.rglob("OperationCodeImpl.java")):
            text = path.read_text(encoding="utf-8")
            mapping = {m.group(1): m.group(2) for m in STATUS_MAPPING_RE.finditer(text)}
            if mapping:
                return mapping, path.name

    for root in common_source_roots:
        for path in sorted(root.rglob("Status.java")):
            text = path.read_text(encoding="utf-8")
            if not STATUS_ENUM_DECL_RE.search(text):
                continue
            mapping = {m.group(1): m.group(2) for m in STATUS_ENUM_CONSTANT_RE.finditer(text)}
            if mapping:
                return mapping, path.name
    return {}, None


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
                if not code_m:
                    continue
                status_m = RESPONSE_STATUS_RE.search(chunk)
                if status_m:
                    http = status_m.group(1)
                else:
                    shortcut_m = RESPONSE_SHORTCUT_RE.search(chunk)
                    if not shortcut_m:
                        # e.g. the LocalizedException handler, whose status is
                        # ex.getStatus().getHttpStatus() -- per-throw-site, not
                        # a fixed framework status. Nothing to record here.
                        continue
                    http = RESPONSE_SHORTCUT_STATUS[shortcut_m.group(1)]
                code = code_m.group(1)
                if code in seen:
                    continue
                seen.add(code)
                codes.append(ErrorCode(name=code, value=code, source_file=rel, http_status=http))
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
    status_http, table_source = find_status_http_mapping_with_source(common_source_roots)
    status_mappings = [
        StatusMapping(name=name, http_status=http_status_label(http), source_file=table_source)
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
                code.http_status = http_status_label(http)

    framework_codes = find_framework_error_codes(common_source_roots)
    for code in framework_codes:
        code.http_status = http_status_label(code.http_status)
    return module_error_codes + framework_codes, status_mappings


def attach_endpoint_error_codes(document: ApiDocument) -> None:
    """Attaches framework-level errors to individual endpoints, but only
    where BOTH halves of the claim are independently verified real facts:
    (a) a per-endpoint property already established by another extractor
    (requires_auth from the OpenAPI security requirement, permission from a
    real @PreAuthorize/@Secured found by security_extractor, request_body
    from the OpenAPI request body), and (b) a framework error code this run
    actually found in GlobalExceptionHandler.

    Codes are looked up by the HTTP STATUS the handler really returns, not by
    a hardcoded code name: a code's name is a project naming choice that
    changes (this platform returns ACCESS_DENIED, not "FORBIDDEN", and has no
    "INVALID_JSON" at all), whereas "the handler that answers 403" is the
    fact being asserted. If no handler for that status was found, nothing is
    claimed -- as before, silence rather than a stale assertion.

    Deliberately narrow: three rules, each unconditionally true of the
    Spring MVC/Security stack this platform runs on, never a guess about
    business logic:
      - requires_auth       -> the 401 handler, when one exists
      - permission found    -> the 403 handler (a real @PreAuthorize/@Secured
                                expression was found for this exact endpoint;
                                AccessDeniedException is handled globally)
      - has a request body  -> the 400 handler (any @RequestBody is
                                deserialized by Jackson before the controller
                                method runs, unconditionally; the framework
                                catches malformed JSON globally)
    """
    by_status: dict[str, ErrorCode] = {}
    for code in document.error_codes:
        if not code.http_status or code.status:
            # code.status set => a module business code enriched from its own
            # throw site, not a framework handler. Only framework-level codes
            # (which carry an HTTP status and no business Status) qualify.
            continue
        constant = code.http_status.split()[-1]
        by_status.setdefault(constant, code)

    def _attach(ep, status_constant: str, reason: str) -> None:
        code = by_status.get(status_constant)
        if not code:
            return
        ep.possible_errors.append(PossibleError(code=code.name, http_status=code.http_status, reason=reason))

    for ep in document.endpoints:
        if ep.requires_auth:
            _attach(
                ep, "UNAUTHORIZED",
                "Endpoint requires authentication (global security requirement); "
                "an unauthenticated call is rejected before the controller method runs.",
            )
        if ep.permission or ep.permission_expression:
            _attach(
                ep, "FORBIDDEN",
                "An authorization check was found for this endpoint "
                "(@PreAuthorize/@Secured); GlobalExceptionHandler maps AccessDeniedException to this status.",
            )
        if ep.request_body is not None:
            _attach(
                ep, "BAD_REQUEST",
                "Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or "
                "invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status.",
            )
