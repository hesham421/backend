"""
Markdown renderer: one index.md (module overview + shared sections) plus one
file per endpoint under endpoints/<group>/<slug>.md. Every subsection is
conditional on data actually being present — nothing here invents content;
absent data just means the subsection doesn't get written.
"""

import json
import re
from dataclasses import replace

from models.api_doc_model import (
    ApiDocument,
    Endpoint,
    ErrorCode,
    FieldSpec,
    PaginationConstraints,
    Parameter,
    ResponseEnvelope,
    StatusMapping,
)
from renderers.base import Renderer


def _slugify(text: str) -> str:
    text = re.sub(r"[^A-Za-z0-9]+", "-", text.strip()).strip("-").lower()
    return text or "root"


def _flatten_fields(fields: list[FieldSpec], prefix: str = "") -> list[FieldSpec]:
    """Flattens FieldSpec.nested (populated by dto_extractor's recursive
    schema expansion) into dotted-path rows -- parent.child, or parent[].child
    for array-of-object fields -- so a nested DTO like a search filter or a
    field-error item renders as real rows instead of an opaque type name.
    Self-referential schemas (FieldSpec.recursive_ref) get one explanatory
    row instead of expanding forever."""
    flat: list[FieldSpec] = []
    for f in fields:
        row = replace(f, name=f"{prefix}{f.name}") if prefix else f
        flat.append(row)
        if f.recursive_ref:
            flat.append(FieldSpec(
                name=f"{prefix}{f.name}[]" if f.is_array else f"{prefix}{f.name}",
                type=f"same shape as `{f.recursive_ref}` (recursive)",
                description="Recursive — repeats this field's own structure.",
            ))
        elif f.nested:
            child_prefix = f"{prefix}{f.name}[]." if f.is_array else f"{prefix}{f.name}."
            flat.extend(_flatten_fields(f.nested, child_prefix))
    return flat


def _constraints_text(f: FieldSpec) -> str:
    parts = []
    if f.max_length is not None:
        parts.append(f"maxLength: {f.max_length}")
    if f.min_length not in (None, 0):
        parts.append(f"minLength: {f.min_length}")
    if f.pattern:
        parts.append(f"pattern: `{f.pattern}`")
    if f.enum:
        parts.append(f"enum: {', '.join(f.enum)}")
    return "; ".join(parts)


def _field_table(fields: list[FieldSpec]) -> str:
    fields = _flatten_fields(fields)
    if not fields:
        return ""
    has_examples = any(f.example for f in fields)
    header = ["Field", "Type", "Required", "Constraints", "Description"]
    if has_examples:
        header.append("Example")
    lines = ["| " + " | ".join(header) + " |", "|" + "|".join(["---"] * len(header)) + "|"]
    for f in fields:
        row = [f.name, f.type, "Yes" if f.required else "No", _constraints_text(f), f.description or ""]
        if has_examples:
            row.append(f.example or "")
        lines.append("| " + " | ".join(c.replace("\n", " ") for c in row) + " |")
    return "\n".join(lines)


def _param_table(params: list[Parameter]) -> str:
    if not params:
        return ""
    lines = [
        "| Name | Type | Required | Description |",
        "|---|---|---|---|",
    ]
    for p in params:
        lines.append(f"| {p.name} | {p.type} | {'Yes' if p.required else 'No'} | {p.description or ''} |")
    return "\n".join(lines)


def _build_example_value(fields: list[FieldSpec]) -> tuple[dict, bool]:
    """Builds a JSON object from whatever real, backend-provided examples
    exist (recursing into FieldSpec.nested), and reports whether every field
    actually contributed one. Never invents a value for a field that has
    none -- that field is simply left out of the object, and `complete` comes
    back False so the caller can say so instead of implying full coverage."""
    obj: dict = {}
    complete = True
    for f in fields:
        if f.recursive_ref:
            complete = False
            continue
        if f.nested:
            nested_obj, nested_complete = _build_example_value(f.nested)
            if not nested_obj:
                complete = False
                continue
            obj[f.name] = [nested_obj] if f.is_array else nested_obj
            complete = complete and nested_complete
            continue
        if not f.example:
            complete = False
            continue
        try:
            obj[f.name] = json.loads(f.example)
        except (json.JSONDecodeError, TypeError):
            obj[f.name] = f.example
    return obj, complete


def _example_json(fields: list[FieldSpec]) -> str:
    obj, complete = _build_example_value(fields)
    if not obj:
        return ""
    code = "```json\n" + json.dumps(obj, indent=2, ensure_ascii=False) + "\n```"
    if complete:
        return code
    return "_(partial — only fields with a documented example are shown)_\n\n" + code


def _envelope_section(title: str, envelope: ResponseEnvelope | None) -> str:
    if not envelope:
        return ""
    parts = [f"## {title}", ""]
    if envelope.schema_name:
        parts.append(f"Schema: `{envelope.schema_name}`")
        parts.append("")
    table = _field_table(envelope.fields)
    if table:
        parts.append(table)
        parts.append("")
    return "\n".join(parts)


def _error_codes_section(codes: list[ErrorCode]) -> str:
    if not codes:
        return ""
    has_status = any(c.status or c.http_status for c in codes)
    header = ["Code", "Value", "Source"]
    if has_status:
        header += ["Status", "HTTP Status"]
    lines = ["## Known Error Codes", "", "| " + " | ".join(header) + " |", "|" + "|".join(["---"] * len(header)) + "|"]
    for c in codes:
        row = [c.name, f"`{c.value}`", c.source_file]
        if has_status:
            row += [c.status or "", c.http_status or ""]
        lines.append("| " + " | ".join(row) + " |")
    lines.append("")
    return "\n".join(lines)


def _status_mappings_section(mappings: list[StatusMapping]) -> str:
    if not mappings:
        return ""
    lines = [
        "## Status -> HTTP Status Reference",
        "",
        "Shared, module-independent mapping every business error code's `Status` "
        "resolves through (see each error code's own Status column above, when known).",
        "",
        "| Status | HTTP Status | Category |",
        "|---|---|---|",
    ]
    for m in mappings:
        lines.append(f"| {m.name} | {m.http_status} | {m.category or ''} |")
    lines.append("")
    return "\n".join(lines)


def _pagination_constraints_section(c: PaginationConstraints | None) -> str:
    if not c:
        return ""
    rows = [
        ("Default page", c.default_page),
        ("Default size", c.default_size),
        ("Minimum size", c.min_size),
        ("Maximum size", c.max_size),
        ("Maximum page number", c.max_page_number),
    ]
    present = [(label, value) for label, value in rows if value is not None]
    if not present:
        return ""
    lines = ["## Pagination Constraints", ""]
    if c.source_file:
        lines.append(f"Source: `{c.source_file}`")
        lines.append("")
    lines += ["| Constraint | Value |", "|---|---|"]
    lines += [f"| {label} | {value} |" for label, value in present]
    lines.append("")
    return "\n".join(lines)


def _common_headers_section(headers: list[Parameter]) -> str:
    if not headers:
        return ""
    lines = ["## Common Headers", "", "Applied globally by shared middleware — not endpoint-specific.", ""]
    lines.append(_param_table(headers))
    lines.append("")
    return "\n".join(lines)


def _auth_section(ep: Endpoint) -> str:
    lines = ["**Authentication**", ""]
    if ep.requires_auth is None:
        lines.append("Not determined from the OpenAPI document.")
    elif ep.requires_auth:
        schemes = ", ".join(ep.security_schemes) if ep.security_schemes else "unspecified scheme"
        lines.append(f"Required ({schemes}).")
    else:
        lines.append("Not required.")
    if ep.permission:
        source = f" (found on {ep.permission_source})" if ep.permission_source else ""
        lines.append("")
        lines.append(f"**Required permission(s)**: {', '.join(ep.permission)}{source}")
    lines.append("")
    return "\n".join(lines)


def _endpoint_markdown(ep: Endpoint) -> str:
    parts = [f"# {ep.method} {ep.path}", ""]
    if ep.summary:
        parts.append(f"**{ep.summary}**")
        parts.append("")
    if ep.description:
        parts.append(ep.description)
        parts.append("")
    if ep.operation_id:
        parts.append(f"Operation ID: `{ep.operation_id}`")
        parts.append("")

    parts.append(_auth_section(ep))

    path_table = _param_table(ep.path_params)
    if path_table:
        parts += ["## Path Parameters", "", path_table, ""]

    query_table = _param_table(ep.query_params)
    if query_table:
        parts += ["## Query Parameters", "", query_table, ""]

    header_table = _param_table(ep.header_params)
    if header_table:
        parts += ["## Headers", "", header_table, ""]

    if ep.request_body:
        parts.append("## Request Body")
        parts.append("")
        if ep.request_body.schema_name:
            parts.append(f"Schema: `{ep.request_body.schema_name}` ({ep.request_body.content_type})")
            parts.append("")
        table = _field_table(ep.request_body.fields)
        if table:
            parts.append(table)
            parts.append("")
        example = _example_json(ep.request_body.fields)
        if example:
            parts.append("**Request Example**")
            parts.append("")
            parts.append(example)
            parts.append("")

    for resp in ep.responses:
        parts.append(f"## Response `{resp.status_code}`" + (f" — {resp.description}" if resp.description else ""))
        parts.append("")
        if resp.schema_name:
            shape = resp.schema_name
            if resp.is_array:
                shape = f"array of {shape}"
            elif resp.is_page:
                shape = f"paginated list of {shape} (see Pagination Envelope in index.md)"
            parts.append(f"Shape: `{shape}`")
            parts.append("")
        table = _field_table(resp.fields)
        if table:
            parts.append(table)
            parts.append("")
        example = _example_json(resp.fields)
        if example:
            parts.append("**Response Example**")
            parts.append("")
            parts.append(example)
            parts.append("")

    if ep.possible_errors:
        parts.append("## Other Possible Responses")
        parts.append("")
        parts.append("Structurally guaranteed by this endpoint's own shape (auth requirement, "
                      "permission check, request body) combined with the shared framework's "
                      "exception handling — not specific business errors.")
        parts.append("")
        parts.append("| HTTP Status | Code | Why |")
        parts.append("|---|---|---|")
        for perr in ep.possible_errors:
            parts.append(f"| {perr.http_status} | {perr.code} | {perr.reason} |")
        parts.append("")

    return "\n".join(parts).rstrip() + "\n"


def _catalog_table(endpoints: list[Endpoint], group: str) -> str:
    lines = ["| Method | Path | Summary | Doc |", "|---|---|---|---|"]
    for ep in endpoints:
        if (ep.group or "Ungrouped") != group:
            continue
        link = f"endpoints/{_slugify(group)}/{ep.slug()}.md"
        lines.append(f"| {ep.method} | `{ep.path}` | {ep.summary or ''} | [{ep.slug()}]({link}) |")
    return "\n".join(lines)


def _index_markdown(doc: ApiDocument) -> str:
    parts = [f"# {doc.module} API Documentation", ""]
    if doc.title:
        parts.append(f"_{doc.title}_")
        parts.append("")
    if doc.description:
        parts.append(doc.description)
        parts.append("")
    if doc.version:
        parts.append(f"API version: `{doc.version}`")
        parts.append("")

    if doc.servers:
        parts.append("## Servers")
        parts.append("")
        for s in doc.servers:
            parts.append(f"- {s}")
        parts.append("")

    if doc.security_schemes:
        parts.append("## Authentication")
        parts.append("")
        for s in doc.security_schemes:
            desc = f" — {s.description}" if s.description else ""
            kind = s.scheme or s.type or ""
            fmt = f" ({s.bearer_format})" if s.bearer_format else ""
            parts.append(f"- **{s.name}**: {kind}{fmt}{desc}")
        parts.append("")

    parts.append(_common_headers_section(doc.common_headers))
    parts.append(_envelope_section("Common Response Envelope", doc.response_envelope))
    parts.append(_envelope_section("Pagination Envelope", doc.pagination_envelope))
    parts.append(_pagination_constraints_section(doc.pagination_constraints))
    parts.append(_error_codes_section(doc.error_codes))
    parts.append(_status_mappings_section(doc.status_mappings))

    parts.append("## API Catalog")
    parts.append("")
    for group in doc.groups():
        parts.append(f"### {group}")
        parts.append("")
        parts.append(_catalog_table(doc.endpoints, group))
        parts.append("")

    return "\n".join(p for p in parts if p is not None).strip() + "\n"


class MarkdownRenderer(Renderer):

    def render(self, document: ApiDocument) -> dict[str, str]:
        files: dict[str, str] = {"index.md": _index_markdown(document)}
        for ep in document.endpoints:
            group_slug = _slugify(ep.group or "Ungrouped")
            files[f"endpoints/{group_slug}/{ep.slug()}.md"] = _endpoint_markdown(ep)
        return files
