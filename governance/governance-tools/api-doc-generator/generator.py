"""
generator.py — the API documentation pipeline: extract -> render -> sync.
════════════════════════════════════════════════════════════════════
Generates frontend-ready API documentation directly from an implemented
Spring Boot module. The ONLY source of truth is the compiled application's
OpenAPI document (springdoc-openapi) plus, optionally, its Java source (the
module's own, and any shared/common modules it depends on) for sections that
have no OpenAPI representation at all (permissions, error codes, error/HTTP
status mapping, globally-applied headers).

This module has no CLI of its own — it's imported by generate.py, which is
responsible for turning "--module ORG --function update" into a
discovery.RepositoryContext (via discovery.py's repository auto-discovery,
with explicit overrides only where discovery genuinely can't be
authoritative).

build_document() is the ONLY place a RepositoryContext gets unpacked. Every
individual extractor below still takes its own narrow, explicit input (a
Path, a dict, ...), never the context object itself — so no extractor module
depends on discovery.py, or needs to know it's running inside a "resolved
repository" at all. That keeps repository knowledge confined to one
boundary, exactly as it was before the context object existed; the context
just replaces what used to be several loose, independently-threaded
parameters (module, openapi_source, source_root, common_source_roots) with
one object discovery.py hands over as a unit.

Design principle, unchanged: every extractor either finds real metadata or
leaves the field empty. The renderer never prints a placeholder for missing
data — it omits the subsection. Nothing here is ever invented.
"""

import sync
from discovery import RepositoryContext
from extractors import (
    common_headers_extractor,
    dto_extractor,
    error_mapping_extractor,
    exception_extractor,
    openapi_extractor,
    pagination_extractor,
    response_model_extractor,
    security_extractor,
)
from models.api_doc_model import ResponseEnvelope
from renderers.markdown_renderer import MarkdownRenderer


def build_document(context: RepositoryContext):
    openapi = openapi_extractor.load_openapi(context.openapi_source)

    document = openapi_extractor.build_document(openapi, context.module)
    document.response_envelope = response_model_extractor.find_envelope(openapi)

    page_schema_name, page_fields = dto_extractor.find_page_envelope(openapi)
    if page_schema_name:
        document.pagination_envelope = ResponseEnvelope(schema_name=page_schema_name, fields=page_fields)

    source_root = context.source_root
    if source_root is not None:
        document.error_codes = exception_extractor.find_error_codes(source_root)

        for ep in document.endpoints:
            controller_file, method_name = security_extractor.find_controller_for_endpoint(source_root, ep.method, ep.path)
            if not controller_file or not method_name:
                continue
            controller_source = controller_file.read_text(encoding="utf-8")
            permission, source_label = security_extractor.resolve_permission(controller_source, method_name, source_root)
            ep.permission = permission
            ep.permission_source = source_label

    if context.common_source_roots:
        document.error_codes, document.status_mappings = error_mapping_extractor.enrich_error_codes(
            document.error_codes, source_root, context.common_source_roots
        )
        document.common_headers = common_headers_extractor.find_common_headers(context.common_source_roots)
        error_mapping_extractor.attach_endpoint_error_codes(document)
        document.pagination_constraints = pagination_extractor.find_pagination_constraints(
            source_root, context.common_source_roots
        )

    return document


def run(context: RepositoryContext, mode: str) -> str:
    """Runs the full pipeline and returns the human-readable report text.
    Raises on load failure — callers decide how to surface it (CLI exit code,
    etc.)."""
    document = build_document(context)
    files = MarkdownRenderer().render(document)

    lines = [f"Module      : {context.module}"]

    if mode == "generate":
        sync.write_all(context.output, files)
        lines.append("Mode        : Generate")
        lines.append(f"Endpoints   : {len(document.endpoints)}")
        lines.append(f"Groups      : {len(document.groups())}")
        lines.append(f"Error codes : {len(document.error_codes)}")
        lines.append(f"Output      : {context.output}")
        return "\n".join(lines)

    existing = sync.read_existing(context.output)
    report = sync.compare(existing, files, document.endpoints, mode=mode)

    if mode == "update":
        written, deleted = sync.apply(context.output, existing, files, report)
        lines.append(sync.format_report(report))
        lines.append(f"Files written: {len(written)}, deleted: {len(deleted)}")
        lines.append(f"Output      : {context.output}")
    else:  # review — no filesystem writes
        lines.append(sync.format_report(report))

    return "\n".join(lines)
