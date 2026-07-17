"""
Data model for the API documentation generator.

Every field below is Optional (or an empty list/dict by default). Absence of
a value means "not discoverable from the implemented backend" — extractors
must never fabricate a value to fill a gap, and renderers must skip a
subsection entirely rather than print a placeholder for missing data.
"""

from dataclasses import dataclass, field
from typing import Optional


@dataclass
class FieldSpec:
    name: str
    type: str
    required: bool = False
    description: Optional[str] = None
    example: Optional[str] = None
    min_length: Optional[int] = None
    max_length: Optional[int] = None
    pattern: Optional[str] = None
    format: Optional[str] = None
    enum: list[str] = field(default_factory=list)
    is_array: bool = False
    item_type: Optional[str] = None
    # Populated when this field's own type is an object (or array-of-object)
    # schema with properties of its own — the field's row still renders as
    # before, but renderers may also expand this into a nested sub-table.
    nested: list["FieldSpec"] = field(default_factory=list)
    # Set instead of `nested` when the referenced schema is already being
    # expanded higher up the same chain (self-referential / recursive
    # schemas, e.g. a tree node's own child list) — names the schema so the
    # renderer can say "same shape as X" instead of expanding forever.
    recursive_ref: Optional[str] = None


@dataclass
class Parameter:
    name: str
    location: str  # "path" | "query" | "header"
    type: str = "string"
    required: bool = False
    description: Optional[str] = None
    example: Optional[str] = None


@dataclass
class RequestBody:
    content_type: str = "application/json"
    schema_name: Optional[str] = None
    fields: list[FieldSpec] = field(default_factory=list)


@dataclass
class ResponseVariant:
    status_code: str
    description: Optional[str] = None
    content_type: Optional[str] = None
    schema_name: Optional[str] = None
    fields: list[FieldSpec] = field(default_factory=list)
    is_array: bool = False
    is_page: bool = False


@dataclass
class Endpoint:
    method: str
    path: str
    group: Optional[str] = None          # OpenAPI tag, used to bucket endpoints
    operation_id: Optional[str] = None
    summary: Optional[str] = None
    description: Optional[str] = None
    path_params: list[Parameter] = field(default_factory=list)
    query_params: list[Parameter] = field(default_factory=list)
    header_params: list[Parameter] = field(default_factory=list)
    request_body: Optional[RequestBody] = None
    responses: list[ResponseVariant] = field(default_factory=list)
    requires_auth: Optional[bool] = None      # None = undetermined, else True/False
    security_schemes: list[str] = field(default_factory=list)
    # Populated only by security_extractor.py, only when --source is given.
    permission: list[str] = field(default_factory=list)
    permission_source: Optional[str] = None   # "controller" | "service:<ClassName>"
    # Populated only by error_mapping_extractor.attach_endpoint_error_codes(),
    # only when common source roots are available -- see PossibleError.
    possible_errors: list[PossibleError] = field(default_factory=list)

    def slug(self) -> str:
        if self.operation_id:
            return self.operation_id
        cleaned = self.path.strip("/").replace("/", "-").replace("{", "").replace("}", "")
        return f"{self.method.lower()}-{cleaned}" if cleaned else self.method.lower()


@dataclass
class PossibleError:
    """One framework-level error this specific endpoint can structurally
    produce, attached only when both (a) a real per-endpoint fact (requires
    auth / has a found permission check / accepts a request body) and (b) a
    real, already-extracted framework error-code+HTTP-status pairing exist --
    never attached by convention or guesswork. See
    error_mapping_extractor.attach_endpoint_error_codes."""
    code: str
    http_status: Optional[str]
    reason: str


@dataclass
class ErrorCode:
    name: str
    value: str
    source_file: str
    # Populated only by error_mapping_extractor.py, only when a shared
    # common-source root was discovered/given. status is the business Status
    # enum constant found at this code's throw site (e.g. "CONFLICT");
    # http_status is that Status resolved through the shared Status->HttpStatus
    # table (e.g. "409 CONFLICT"). Either may be absent independently.
    status: Optional[str] = None
    http_status: Optional[str] = None


@dataclass
class StatusMapping:
    """One row of the shared, module-independent Status/exception -> HTTP
    status table (see error_mapping_extractor.py). Not tied to any module's
    own error codes — this is the platform-wide framework table."""
    name: str
    http_status: str
    category: Optional[str] = None
    source_file: Optional[str] = None


@dataclass
class PaginationConstraints:
    """Request-side pagination limits/defaults -- distinct from
    ResponseEnvelope (which describes the shape of a paginated *response*).
    Populated only by pagination_extractor.py, only when discoverable and
    unambiguously attributable to the module being documented."""
    default_page: Optional[int] = None
    default_size: Optional[int] = None
    min_size: Optional[int] = None
    max_size: Optional[int] = None
    max_page_number: Optional[int] = None
    source_file: Optional[str] = None


@dataclass
class ResponseEnvelope:
    schema_name: str
    fields: list[FieldSpec] = field(default_factory=list)


@dataclass
class SecurityScheme:
    name: str
    type: Optional[str] = None
    scheme: Optional[str] = None
    bearer_format: Optional[str] = None
    description: Optional[str] = None


@dataclass
class ApiDocument:
    module: str
    title: Optional[str] = None
    description: Optional[str] = None
    version: Optional[str] = None
    servers: list[str] = field(default_factory=list)
    security_schemes: list[SecurityScheme] = field(default_factory=list)
    endpoints: list[Endpoint] = field(default_factory=list)
    error_codes: list[ErrorCode] = field(default_factory=list)
    response_envelope: Optional[ResponseEnvelope] = None
    pagination_envelope: Optional[ResponseEnvelope] = None
    pagination_constraints: Optional[PaginationConstraints] = None
    # Populated only by error_mapping_extractor.py, only when a shared
    # common-source root was discovered/given (see 3.6 in the enhancement report).
    status_mappings: list[StatusMapping] = field(default_factory=list)
    # Populated only by common_headers_extractor.py, only when a shared
    # common-source root was discovered/given (see 3.12).
    common_headers: list[Parameter] = field(default_factory=list)

    def groups(self) -> list[str]:
        seen: list[str] = []
        for ep in self.endpoints:
            g = ep.group or "Ungrouped"
            if g not in seen:
                seen.append(g)
        return seen
