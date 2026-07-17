"""
Turns one already-resolved OpenAPI schema property into a FieldSpec.

springdoc-openapi already translates Bean Validation annotations into plain
JSON Schema keywords by the time they reach the OpenAPI document:
  @NotBlank / @NotNull  -> listed in the parent schema's "required" array
  @Size(min, max)        -> "minLength" / "maxLength"
  @Pattern(regexp)        -> "pattern"
  @Schema(description=…, example=…) -> "description" / "example"

This module does not talk to Java source at all — everything here comes
from the OpenAPI JSON that's already been resolved by dto_extractor.
"""

from typing import Optional

from models.api_doc_model import FieldSpec


def _type_label(resolved: dict, ref_name: Optional[str], is_array: bool, item_type: Optional[str]) -> str:
    if is_array:
        return f"array<{item_type or 'object'}>"
    if ref_name:
        return ref_name
    t = resolved.get("type", "object")
    fmt = resolved.get("format")
    return f"{t} ({fmt})" if fmt else t


def _stringify(value) -> Optional[str]:
    if value is None:
        return None
    return str(value)


def build_field_spec(name: str, prop: dict, required: bool, resolve_ref) -> FieldSpec:
    """resolve_ref: callable(schema_dict) -> (resolved_dict, ref_name_or_None), supplied by
    dto_extractor so this module never has to know how $ref dereferencing works."""
    resolved, ref_name = resolve_ref(prop)

    raw_type = resolved.get("type", "object" if ref_name else "string")
    is_array = raw_type == "array"
    item_type = None
    if is_array:
        items = resolved.get("items", {})
        item_resolved, item_ref = resolve_ref(items)
        item_type = item_ref or item_resolved.get("type", "object")

    return FieldSpec(
        name=name,
        type=_type_label(resolved, ref_name, is_array, item_type),
        required=required,
        description=resolved.get("description"),
        example=_stringify(resolved.get("example")),
        min_length=resolved.get("minLength"),
        max_length=resolved.get("maxLength"),
        pattern=resolved.get("pattern"),
        format=resolved.get("format"),
        enum=list(resolved.get("enum") or []),
        is_array=is_array,
        item_type=item_type,
    )
