"""
Best-effort module-level error catalog discovery.

No controller anywhere in this codebase declares per-endpoint
`@ApiResponse`/`@ApiResponses` (springdoc annotations), so per-endpoint error
responses are not discoverable and must not be fabricated.

What IS consistent across every module is a `*ErrorCodes.java` class holding
`public static final String NAME = "VALUE";` constants (OrgErrorCodes,
SecurityErrorCodes, MasterDataErrorCodes, GlErrorCodes, CommonErrorCodes).
This is surfaced as one module-level "Known Error Codes" appendix — never
claimed to apply to any specific endpoint.
"""

import re
from pathlib import Path

from models.api_doc_model import ErrorCode

CONST_RE = re.compile(r'public\s+static\s+final\s+String\s+(\w+)\s*=\s*"([^"]+)"\s*;')


def find_error_codes(source_root: Path) -> list[ErrorCode]:
    codes: list[ErrorCode] = []
    for path in sorted(source_root.rglob("*ErrorCodes.java")):
        text = path.read_text(encoding="utf-8")
        try:
            rel = str(path.relative_to(source_root))
        except ValueError:
            rel = path.name
        for name, value in CONST_RE.findall(text):
            codes.append(ErrorCode(name=name, value=value, source_file=rel))
    return codes
