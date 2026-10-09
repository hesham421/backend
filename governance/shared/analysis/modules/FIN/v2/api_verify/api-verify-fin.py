#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# Intended for Dev/Test environments only.
"""
api-verify-fin.py — API verification for module FIN · v2 (change set CS-FIN-001) · profile erp.

Stage `api-verify` (standalone, post-implementation). A TRANSLATION of the governed inputs into
one runnable script — it designs no test and invents no rule, message, dependency or data value:

  * api-docs                 (`_inputs/api-docs-fin.md`, 11 files, 42 served endpoints)
  * test-execution-manifest  (`_state/current-test-execution-manifest.md`, v2)
  → tier FULL: happy-path CRUD per entity AND negative (rule-violation) tests; the dependency order
    and the RULE → code → TC triples are read from the manifest, never re-derived.

Usage (stdlib only, Python ≥ 3.9):

    python api-verify-fin.py --username <user> --password <pass>            # localhost:7272
    python api-verify-fin.py --token <jwt> --event-type <unused ACCOUNTING_EVENT_TYPE code>
    python api-verify-fin.py --plan-only                                    # report, no calls

Exit code: 0 when every ASSERTED call passed, 1 on any asserted failure, 2 when the run could not
start (no credentials, non-local target, preflight refused). Observations never affect it.

Every value that is not an api-docs example or an id threaded from a parent's response is a
MARKED PLACEHOLDER (search for `PLACEHOLDER`). Credentials are never invented.
"""
from __future__ import annotations

import argparse
import json
import sys
import threading
import time
import urllib.error
import urllib.parse
import urllib.request
from dataclasses import dataclass, field
from datetime import date, datetime, timedelta, timezone
from pathlib import Path
from typing import Any, Callable

# ══════════════════════════════════════════════════════════════════════════════════════════
# config
# ══════════════════════════════════════════════════════════════════════════════════════════

MODULE = "FIN"
VERSION = "v2"
TIER = "FULL"                                   # api-docs + test-execution-manifest both present
DEFAULT_BASE_URL = "http://localhost:7272"      # api-docs index.md → Servers
AUTH_ENDPOINT = "/api/v1/sec/auth/login"        # api-docs index.md → Authentication (bearerAuth)
LOCAL_HOSTS = {"localhost", "127.0.0.1", "::1"}
PLACEHOLDER_USERNAME = "<PLACEHOLDER_USERNAME>"  # PLACEHOLDER — never invented; pass --username
PLACEHOLDER_PASSWORD = "<PLACEHOLDER_PASSWORD>"  # PLACEHOLDER — never invented; pass --password
# PLACEHOLDER — the SEC login request/response bodies are not in FIN's api-docs; override the body
# with --login-body and the token is looked up under these keys (extract_token).
LOGIN_BODY_KEYS = ("username", "password")
TOKEN_KEYS = ("accessToken", "token", "access_token", "jwt")

# api-docs examples (used verbatim unless a column is unique on ALL rows — then the example gets
# the run tag appended, see unique_code()).
EX_EVENT_TYPE = "SALES_INVOICE"                 # API-FIN-010 example eventTypeCode
EX_YEAR_START, EX_YEAR_END, EX_PERIOD_COUNT = "2026-01-01", "2026-12-31", 12   # API-FIN-023 example
# PLACEHOLDER — RULE-FIN-028 needs a docDate that NO period covers (manifest step 6, TC-FIN-126);
# no input states one. Pass --uncovered-date when this date is covered on the target.
PLACEHOLDER_UNCOVERED_DATE = "1900-01-31"
# PLACEHOLDER — RULE-FIN-024 needs a businessValue that is NOT a PAYMENT_METHOD code (TC-FIN-111).
PLACEHOLDER_NOT_A_PAYMENT_METHOD = "NOT_A_PAYMENT_METHOD"
# PLACEHOLDER — an undocumented lookup value for the stage-E "undocumented lookup" observation.
PLACEHOLDER_NOT_A_LOOKUP = "NOT_A_LOOKUP"
# PLACEHOLDER — an id no row carries, for stage-E not-found / invalid-FK observations.
PLACEHOLDER_MISSING_ID = 9_000_000_000_000_000

PAGE_SIZE = 20                                  # api-docs Pagination Constraints → default size

SHARED_CODES = {"VALIDATION_ERROR", "FIN-403-FORBIDDEN", "SEC-401-INVALID-CREDENTIALS"}


def _now() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="seconds")


def _f(*slugs: str) -> set:
    return {s if s == "VALIDATION_ERROR" else f"FIN-{s}" for s in slugs}


SORT = ("400-INVALID-SORT", "VALIDATION_ERROR")
# Business Responses per endpoint, read from each endpoint's own api-docs table. Used only to
# bucket failures (a documented rejection vs an unexplained one) — never to derive a test.
DOC_CODES: dict[str, set] = {
    "API-FIN-001": _f(*SORT),
    "API-FIN-002": _f("400-INVALID-LOOKUP", "404-ACCOUNT", "409-ACCOUNT-DUP", "409-PARENT-NOT-LEAF-ELIGIBLE"),
    "API-FIN-003": _f("404-ACCOUNT", "409-HAS-CHILDREN"),
    "API-FIN-004": _f("404-ACCOUNT"),
    "API-FIN-005": _f(*SORT),
    "API-FIN-006": _f("409-DIMENSION-DUP"),
    "API-FIN-007": _f("404-DIMENSION", "409-DIMVALUE-DUP"),
    "API-FIN-008": _f(*SORT, "404-DIMENSION"),
    "API-FIN-009": _f(*SORT),
    "API-FIN-010": _f("400-INVALID-LOOKUP", "409-RULE-DUP"),
    "API-FIN-011": _f("400-INVALID-LOOKUP", "404-DIMENSION", "404-DIMVALUE", "404-RULE", "409-REMAINDER-COUNT",
                      "422-DERIVATION-SPEC", "422-DIMENSION-TAG-DUP", "422-DIMENSION-TAG-SOURCE",
                      "422-REMAINDER-MARKER", "422-UNSUPPORTED-DERIVATION"),
    "API-FIN-012": _f(*SORT),
    "API-FIN-013": _f("400-INVALID-LOOKUP", "400-MISSING-FREQUENCY", "404-ACCOUNT", "409-INVALID-DIMENSION"),
    "API-FIN-014": _f("404-PERIOD", "404-TEMPLATE", "404-YEAR", "409-ALREADY-REVERSED", "409-DUPLICATE-EVENT",
                      "409-NOT-ACTIVE", "409-NOT-POSTED", "409-PERIOD-NOT-OPEN", "409-UNBALANCED"),
    "API-FIN-015": _f(*SORT),
    "API-FIN-016": _f("400-INVALID-LOOKUP", "404-ACCOUNT", "409-INVALID-DIMENSION", "409-REMAINDER-COUNT",
                      "422-REMAINDER-MARKER"),
    "API-FIN-017": _f("404-ALLOCATION-RULE", "404-PERIOD", "404-YEAR", "409-DUPLICATE-EVENT", "409-NOT-ACTIVE",
                      "409-PERIOD-NOT-OPEN", "409-REMAINDER-COUNT", "409-UNBALANCED", "422-REMAINDER-MARKER",
                      "422-REMAINDER-NOT-POSITIVE"),
    "API-FIN-018": _f(*SORT),
    "API-FIN-019": _f("400-DOCDATE-OUTSIDE-PERIOD", "400-INVALID-LOOKUP", "400-PERIOD-NOT-IN-YEAR", "404-ACCOUNT",
                      "404-PERIOD", "404-YEAR", "409-DUPLICATE-EVENT", "409-INVALID-DIMENSION",
                      "409-NOT-POSTABLE-ACCOUNT", "409-PERIOD-NOT-OPEN", "409-UNBALANCED"),
    "API-FIN-020": _f("404-ACCOUNT", "404-NO-ACTIVE-RULE", "404-YEAR", "409-DUPLICATE-EVENT", "409-PERIOD-NOT-OPEN",
                      "409-REMAINDER-COUNT", "409-UNBALANCED", "422-INVALID-PERCENTAGE-VALUE",
                      "422-MISSING-BUSINESS-FIELD", "422-NO-DEFINED-PERIOD", "422-REMAINDER-MARKER",
                      "422-REMAINDER-NOT-POSITIVE", "422-UNMAPPED-VALUE", "422-UNRESOLVED-DIMENSION-VALUE"),
    "API-FIN-021": _f("404-ENTRY", "404-YEAR", "409-ALREADY-REVERSED", "409-DUPLICATE-EVENT", "409-NOT-POSTED",
                      "409-PERIOD-NOT-OPEN", "409-UNBALANCED"),
    "API-FIN-022": _f("404-ENTRY"),
    "API-FIN-023": _f("409-YEAR-DUP"),
    "FY-SEARCH": _f(*SORT),       # POST /api/v1/fin/fiscal-years/search — served, no contract id
    "API-FIN-024": _f("404-PERIOD", "409-NOT-REOPENABLE"),
    "API-FIN-025": _f("404-PERIOD", "409-INVALID-TRANSITION"),
    "API-FIN-026": _f("404-PERIOD", "409-NOT-REOPENABLE"),
    "API-FIN-027": _f("404-ACCOUNT", "404-PERIOD", "404-YEAR", "409-DUPLICATE-EVENT", "409-INVALID-TRANSITION",
                      "409-PERIODS-NOT-CLOSED", "409-PERIOD-NOT-OPEN", "409-UNBALANCED"),
    "API-FIN-028": _f("404-ACCOUNT"),
    "API-FIN-029": _f("404-PERIOD"),
    "API-FIN-030": _f("404-YEAR"),
    "API-FIN-031": _f("404-PERIOD", "404-YEAR"),
    "API-FIN-032": _f("404-DIMENSION"),
    "API-FIN-033": _f(*SORT),
    "API-FIN-034": _f("404-RULE"),
    "API-FIN-035": _f("404-DIMVALUE"),
    "API-FIN-036": _f("404-TEMPLATE"),
    "API-FIN-037": _f("404-ALLOCATION-RULE"),
    "API-FIN-038": _f(*SORT),
    "API-FIN-039": _f("400-INVALID-LOOKUP", "404-ACCOUNT", "409-MAPPING-DUP", "422-INVALID-PAYMENT-METHOD",
                      "422-MAPPED-ACCOUNT-INELIGIBLE"),
    "API-FIN-040": _f("404-ACCOUNT", "404-ACCOUNT-MAPPING", "422-MAPPED-ACCOUNT-INELIGIBLE"),
    "API-FIN-041": _f("404-ACCOUNT-MAPPING"),
}

# Stage-B order: the manifest's DEPENDENCY ORDER, read as-is (steps 0 and 1 are MDL / SEC data this
# module does not create; they are verified indirectly by the preflight, see preflight()).
DEPENDENCY_ORDER = [
    ("0", "MDL lookup keys (external)", "—", "precondition — FIN-400-INVALID-LOOKUP on any create means a key is missing"),
    ("1", "SEC module/screen/action registry (external)", "—", "precondition — preflight() stops on 401/403"),
    ("2", "ENT-FIN-001 Account", "API-FIN-002", "test_account"),
    ("3", "ENT-FIN-002 Dimension", "API-FIN-006", "test_dimension"),
    ("4", "ENT-FIN-003 DimensionValue", "API-FIN-007", "test_dimension_value"),
    ("5", "ENT-FIN-007 FiscalYear", "API-FIN-023", "test_fiscal_year"),
    ("6", "ENT-FIN-008 FiscalPeriod", "API-FIN-023 (generated)", "test_fiscal_period"),
    ("7", "ENT-FIN-009 EventTypeRule", "API-FIN-010", "test_event_type_rule"),
    ("8–9", "ENT-FIN-010 RuleLine + ENT-FIN-016 RuleLineDimension", "API-FIN-011", "test_rule_line"),
    ("10", "ENT-FIN-015 AccountMapping", "API-FIN-039", "test_account_mapping"),
    ("11–12", "ENT-FIN-011 RecurringTemplate + ENT-FIN-012 lines", "API-FIN-013", "test_recurring_template"),
    ("13–14", "ENT-FIN-013 AllocationRule + ENT-FIN-014 targets", "API-FIN-016", "test_allocation_rule"),
    ("15–17", "ENT-FIN-004 JournalEntry + ENT-FIN-005 lines + ENT-FIN-006 line dimensions",
     "API-FIN-019, API-FIN-020", "test_journal_entry · test_journal_entry_from_event"),
]

# Stage C — the manifest's RULE → CODE → TC rows that carry a backend TC, and where each is asserted.
NEGATIVES = [
    ("RULE-FIN-001", "FIN-409-HAS-CHILDREN", "TC-FIN-002", 409, "API-FIN-003", "test_account"),
    ("RULE-FIN-002", "FIN-409-DIMVALUE-DUP", "TC-FIN-006", 409, "API-FIN-007", "test_dimension_value"),
    ("RULE-FIN-003", "FIN-409-REMAINDER-COUNT", "TC-FIN-009, TC-FIN-047", 409, "API-FIN-016", "test_allocation_rule"),
    ("RULE-FIN-003", "FIN-409-REMAINDER-COUNT", "TC-FIN-009, TC-FIN-047", 409, "API-FIN-011", "test_rule_line_negatives"),
    ("RULE-FIN-004", "FIN-409-DUPLICATE-EVENT", "TC-FIN-011, TC-FIN-123", 409, "API-FIN-020", "test_journal_entry_from_event"),
    ("RULE-FIN-004", "FIN-409-DUPLICATE-EVENT", "TC-FIN-124 (concurrent)", 409, "API-FIN-020", "test_journal_entry_from_event"),
    ("RULE-FIN-005", "FIN-404-NO-ACTIVE-RULE", "TC-FIN-013", 404, "API-FIN-020", "test_event_type_rule_deactivate"),
    ("RULE-FIN-006", "FIN-409-UNBALANCED", "TC-FIN-015, TC-FIN-018", 409, "API-FIN-019", "test_journal_entry"),
    ("RULE-FIN-006", "FIN-409-UNBALANCED", "TC-FIN-015, TC-FIN-018", 409, "API-FIN-014", "test_recurring_template"),
    ("RULE-FIN-007", "FIN-409-NOT-POSTABLE-ACCOUNT", "TC-FIN-003, TC-FIN-015, TC-FIN-019", 409, "API-FIN-019", "test_journal_entry"),
    ("RULE-FIN-007", "FIN-409-NOT-POSTABLE-ACCOUNT", "TC-FIN-100, TC-FIN-101", 409, "API-FIN-020", "test_journal_entry_from_event"),
    ("RULE-FIN-008", "FIN-409-PERIOD-NOT-OPEN", "TC-FIN-020, TC-FIN-033", 409, "API-FIN-019", "test_journal_entry"),
    ("RULE-FIN-008", "FIN-409-PERIOD-NOT-OPEN", "TC-FIN-125, TC-FIN-127", 409, "API-FIN-020", "test_journal_entry_from_event"),
    ("RULE-FIN-009", "FIN-409-INVALID-DIMENSION", "TC-FIN-021", 409, "API-FIN-019", "test_journal_entry"),
    ("RULE-FIN-009", "FIN-409-INVALID-DIMENSION", "TC-FIN-118", 409, "API-FIN-020", "test_journal_entry_from_event"),
    ("RULE-FIN-013", "FIN-409-ALREADY-REVERSED", "TC-FIN-030", 409, "API-FIN-021", "test_journal_entry"),
    ("RULE-FIN-014", "FIN-409-NOT-REOPENABLE", "TC-FIN-035", 409, "API-FIN-024", "test_fiscal_period"),
    ("RULE-FIN-015", "FIN-403-FORBIDDEN", "TC-FIN-038, TC-FIN-052", 403, "API-FIN-026, API-FIN-027",
     "test_close_approval_permission (needs --limited-* credentials)"),
    ("RULE-FIN-018", "FIN-422-UNSUPPORTED-DERIVATION", "TC-FIN-094", 422, "API-FIN-011", "test_rule_line_negatives"),
    ("RULE-FIN-020", "FIN-422-UNMAPPED-VALUE", "TC-FIN-102, TC-FIN-103", 422, "API-FIN-020", "test_journal_entry_from_event"),
    ("RULE-FIN-020", "FIN-422-UNMAPPED-VALUE", "TC-FIN-107", 422, "API-FIN-020", "test_account_mapping_deactivate"),
    ("RULE-FIN-021", "FIN-422-MISSING-BUSINESS-FIELD", "TC-FIN-099 (MAPPING line)", 422, "API-FIN-020", "test_journal_entry_from_event"),
    ("RULE-FIN-021", "FIN-422-MISSING-BUSINESS-FIELD", "TC-FIN-119 (dimension tag)", 422, "API-FIN-020", "test_journal_entry_from_event"),
    ("RULE-FIN-022", "FIN-409-MAPPING-DUP", "TC-FIN-109", 409, "API-FIN-039", "test_account_mapping"),
    ("RULE-FIN-023", "FIN-422-MAPPED-ACCOUNT-INELIGIBLE", "TC-FIN-110", 422, "API-FIN-040", "test_account_mapping"),
    ("RULE-FIN-024", "FIN-422-INVALID-PAYMENT-METHOD", "TC-FIN-111", 422, "API-FIN-039", "test_account_mapping"),
    ("RULE-FIN-025", "FIN-422-DIMENSION-TAG-DUP", "TC-FIN-115", 422, "API-FIN-011", "test_rule_line_negatives"),
    ("RULE-FIN-026", "FIN-422-DIMENSION-TAG-SOURCE", "TC-FIN-116", 422, "API-FIN-011", "test_rule_line_negatives"),
    ("RULE-FIN-027", "FIN-422-UNRESOLVED-DIMENSION-VALUE", "TC-FIN-120", 422, "API-FIN-020", "test_journal_entry_from_event"),
    ("RULE-FIN-028", "FIN-422-NO-DEFINED-PERIOD", "TC-FIN-126", 422, "API-FIN-020", "test_journal_entry_from_event"),
]

# Manifest rows NOT asserted by this script, with the reason — named, never silently dropped.
SKIPPED_NEGATIVES = [
    ("RULE-FIN-001", "FIN-409-PARENT-NOT-LEAF-ELIGIBLE", "the manifest cites no TC (no AC states the create-path variant) — executed as an observation only"),
    ("RULE-FIN-003", "FIN-422-REMAINDER-MARKER", "the manifest cites no TC — not asserted"),
    ("RULE-FIN-003", "FIN-409-REMAINDER-COUNT on API-FIN-017 / API-FIN-020", "an invalid remainder set cannot be stored through the API (API-FIN-011 / API-FIN-016 refuse it first, and those are asserted)"),
    ("RULE-FIN-006", "FIN-409-UNBALANCED on API-FIN-020 / 017 / 021 / 027", "no API input can make a rule, an allocation, a reversal or a year-end close produce an unbalanced entry; asserted on API-FIN-019 and API-FIN-014"),
    ("RULE-FIN-007 / 008 / 009", "on API-FIN-014 / 017 / 021 / 027", "the same guard is asserted on API-FIN-019 and API-FIN-020; the generated-entry paths share it (JournalPostingService.buildValidateAndPost)"),
    ("RULE-FIN-010", "FIN-422-REMAINDER-NOT-POSITIVE / TC-FIN-048", "its precondition (non-remainder lines that already consume the whole amount) needs percentages or amounts no input states for this run"),
    ("RULE-FIN-013", "FIN-409-NOT-POSTED", "the manifest cites no TC (DRAFT is never a stored, browsable state)"),
    ("RULE-FIN-015", "FIN-403-FORBIDDEN / TC-FIN-038, TC-FIN-052", "needs a second caller WITHOUT PERM_FIN_PERIODS_CLOSE_APPROVE — runs only when --limited-username/--limited-password or --limited-token is given"),
    ("RULE-FIN-017", "FIN-400-PERIOD-NOT-IN-YEAR · FIN-400-DOCDATE-OUTSIDE-PERIOD", "the manifest cites no TC (the rule was added at SVC-API)"),
    ("RULE-FIN-019", "FIN-422-DERIVATION-SPEC", "no TC raises it (TC-FIN-095 is the passing case) — executed as an observation only"),
    ("RULE-FIN-008 carve-out", "year-end CLOSING/OPENING entries / TC-FIN-036", "a success path with terminal side effects (every period of the run's year hard-closed) — runs only with --with-year-end"),
    ("—", "FIN-403-SOD-VIOLATION · FIN-422-MAPPING-UNSUPPORTED", "struck codes the manifest says must NOT be tested"),
    ("—", "frontend TCs (TC-FIN-054, 058, 061, 063, …, 142)", "frontend cases; out of scope for an API run"),
]

# Documentation gaps found while translating the api-docs (flagged, never asserted).
DOC_GAPS = [
    "`Page<T>` (index.md → Pagination Envelope) documents no `content` field. The script reads search rows from `content` "
    "(the profile's paging convention) and asserts only documented page fields (`totalElements`, `number`, `size`).",
    "The error envelope is documented as `error.code / error.message / error.fieldErrors[]`, not as the profile's "
    "`{code, messageAr, messageEn}`; there is one message per response. The script asserts `error.message` presence in the "
    "language it requests (`Accept-Language: ar`, the primary language) and records the text as an observation.",
    "FIN-409-UNBALANCED, FIN-409-NOT-POSTABLE-ACCOUNT, FIN-409-INVALID-DIMENSION and FIN-409-PERIOD-NOT-OPEN are documented as "
    "surfacing \"as a `fieldErrors[]` entry\" on several endpoints, without stating what the top-level `error.code` is then. "
    "The script accepts the code in either slot and records which slot answered — for API-FIN-020 the consumer \"branches on "
    "the full error code\", so a retry code that arrives only in `fieldErrors[]` would be a contract problem.",
    "Known Error Codes: FIN-409-NOT-POSTABLE-ACCOUNT, FIN-409-UNBALANCED and FIN-403-SOD-VIOLATION have empty `Status` / "
    "`HTTP Status` cells (the consumer notes say 409); FIN-409-NOT-POSTABLE-ACCOUNT reads \"Raised by: 1 endpoint(s)\" "
    "while its note lists four APIs.",
    "API-FIN-020's Business Responses omit FIN-409-NOT-POSTABLE-ACCOUNT and FIN-409-INVALID-DIMENSION, which the catalog notes "
    "attribute to API-FIN-020 and the manifest asserts on the event path (TC-FIN-100, TC-FIN-101, TC-FIN-118).",
    "The API-FIN-011 request example is not a valid request: it pairs CONSTANT with `accountBusinessFieldCode` (RULE-FIN-019) "
    "and a CONSTANT dimension tag with `businessFieldCode` (RULE-FIN-026). The script builds each shape from the field "
    "descriptions (\"CONSTANT only\" / \"MAPPING only\" / \"BUSINESS_FIELD only\").",
    "Request and response examples render string-typed codes as JSON numbers (`\"code\": 1101`, `\"code\": 2026`, "
    "`\"accountDerivationValue\": 1101`, `\"accountCode\": 1101`); the field tables type them `string`. The script sends strings "
    "and compares codes as strings.",
    "Examples carry literal FK ids (`parentAccountId: 10`, `accountId: 12`, `dimensionId: 1`, `dimensionValueId: 5 / 7`, "
    "`sourceAccountId: 31`, `targetAccountId: 21 / 22`, `fiscalYearId: 1`, `periodId: 1`); the script threads real ids instead.",
    "API-FIN-014 and API-FIN-017 take no body: the date by which they pick a period (`resolvePeriodContaining`) is not documented.",
    "API-FIN-020 `amounts` has no documented key set or example; the script keys it by the FIELD source value `netAmount` "
    "(API-FIN-011 example) with the documented `baseAmount` example 1000.0.",
    "The SEC login request and response bodies are not in FIN's api-docs; the login body keys and token keys are marked placeholders.",
    "`POST /api/v1/fin/fiscal-years/search` is served without a contract id (index.md → Drift); the script cites it by path.",
    "Codes, event-type rules and event references are unique on ALL rows and FIN has no activate endpoint, so verbatim "
    "example codes collide with existing data and with every rerun. The script appends a run tag to unique-keyed example "
    "values (fiscal-year code ≤ 10 characters), and takes the rule's event type as an argument: one rule per event type, ever.",
    "No uniqueness is documented on fiscal-year dates. Event, template and allocation runs resolve a period by date; when "
    "another year already covers the run's dates, which period answers is not documented — pass --year-start/--year-end.",
    "Lookup value lists are not published in the api-docs; the script uses only values seen in the inputs "
    "(ASSET, DEBIT, CREDIT, MANUAL, CONSTANT, MAPPING, DIRECT, BUSINESS_FIELD, FIELD, FIXED, PERCENTAGE, REMAINDER, "
    "RECURRING, MONTHLY, PAYMENT_METHOD, BRANCH_CODE, CHEQUE, CASH, OPEN, SOFT_CLOSE, HARD_CLOSE).",
]

# ══════════════════════════════════════════════════════════════════════════════════════════
# client — thin HTTP wrapper that unwraps ApiResponse<T>
# ══════════════════════════════════════════════════════════════════════════════════════════


class Resp:
    def __init__(self, method: str, path: str, status: int | None, body: Any, text: str,
                 error: str | None = None, at: str = ""):
        self.method, self.path, self.status, self.body, self.text = method, path, status, body, text
        self.transport_error, self.at = error, at

    @property
    def data(self) -> Any:
        return self.body.get("data") if isinstance(self.body, dict) else None

    @property
    def err(self) -> dict:
        e = self.body.get("error") if isinstance(self.body, dict) else None
        return e if isinstance(e, dict) else {}

    @property
    def code(self) -> str | None:
        return self.err.get("code")

    @property
    def message(self) -> str | None:
        return self.err.get("message")

    @property
    def field_codes(self) -> list:
        return [fe.get("field") for fe in (self.err.get("fieldErrors") or []) if isinstance(fe, dict)]

    def has_code(self, code: str) -> bool:
        return code == self.code or code in self.field_codes

    def code_slot(self, code: str) -> str:
        if code == self.code:
            return "error.code"
        return "error.fieldErrors[].field" if code in self.field_codes else "—"


class Client:
    def __init__(self, base_url: str, token: str | None = None, lang: str = "ar", timeout: float = 30.0):
        self.base, self.token, self.lang, self.timeout = base_url.rstrip("/"), token, lang, timeout

    def call(self, method: str, path: str, body: Any = None, params: dict | None = None) -> Resp:
        url = self.base + path
        if params:
            url += "?" + urllib.parse.urlencode({k: v for k, v in params.items() if v is not None})
        data = json.dumps(body, ensure_ascii=False).encode("utf-8") if body is not None else None
        req = urllib.request.Request(url, data=data, method=method)
        req.add_header("Accept", "application/json")
        req.add_header("Accept-Language", self.lang)
        if data is not None:
            req.add_header("Content-Type", "application/json; charset=utf-8")
        if self.token:
            req.add_header("Authorization", f"Bearer {self.token}")
        at = _now()
        try:
            with urllib.request.urlopen(req, timeout=self.timeout) as r:
                status, raw = r.status, r.read()
        except urllib.error.HTTPError as e:
            status, raw = e.code, e.read()
        except (urllib.error.URLError, TimeoutError, OSError) as e:
            return Resp(method, path, None, None, "", error=f"{type(e).__name__}: {e}", at=at)
        text = raw.decode("utf-8", errors="replace")
        try:
            parsed = json.loads(text) if text else None
        except ValueError:
            parsed = None
        return Resp(method, path, status, parsed, text, at=at)

    def get(self, path, params=None):
        return self.call("GET", path, params=params)

    def post(self, path, body=None):
        return self.call("POST", path, body)

    def put(self, path, body=None):
        return self.call("PUT", path, body)

    def patch(self, path, body=None):
        return self.call("PATCH", path, body)


# ══════════════════════════════════════════════════════════════════════════════════════════
# results
# ══════════════════════════════════════════════════════════════════════════════════════════

BUCKET_BUG, BUCKET_ASSUMPTION, BUCKET_INFRA = "likely real bug", "test assumption mismatch", "infrastructure"


@dataclass
class TestResult:
    suite: str
    name: str
    api: str
    covers: str
    negative: str
    method: str
    path: str
    expected: str
    status: int | None
    code: str | None
    message: str | None
    passed: bool | None            # None = observation / skipped
    kind: str = "assert"           # assert | observation | skipped
    bucket: str | None = None
    detail: str = ""
    at: str = ""


@dataclass
class TestSuite:
    name: str
    results: list = field(default_factory=list)


class Run:
    def __init__(self):
        self.suites: list[TestSuite] = []
        self.current: TestSuite | None = None
        self.started = _now()
        self.aborted: str | None = None
        self.teardown: list[str] = []
        self.remaining: list[str] = []

    def begin(self, name: str) -> None:
        self.current = TestSuite(name)
        self.suites.append(self.current)
        print(f"\n━━ {name}")

    def add(self, r: TestResult) -> None:
        self.current.results.append(r)
        mark = {"assert": "PASS" if r.passed else "FAIL", "observation": "OBS ", "skipped": "SKIP"}[r.kind]
        tail = f"{r.status} {r.code or ''}".strip() if r.kind != "skipped" else r.detail
        print(f"  [{mark}] {r.name} — {tail}" + (f"  ({r.detail})" if r.kind == "assert" and not r.passed else ""))

    def all(self, kind: str) -> list:
        return [r for s in self.suites for r in s.results if r.kind == kind]


RUN = Run()
CLIENT: Client
ARGS: argparse.Namespace


def classify(api: str, expect_status: int, resp: Resp) -> str:
    """Stage G buckets. Ambiguous → likely real bug."""
    if resp.status is None:
        return BUCKET_INFRA
    documented = DOC_CODES.get(api, set()) | SHARED_CODES
    if expect_status >= 400:
        if 200 <= resp.status < 300 or resp.status >= 500:
            return BUCKET_BUG                       # a documented rejection did not happen / unexplained
        return BUCKET_ASSUMPTION                    # rejected, with a status/code the manifest did not state
    if resp.status < 500 and resp.code in documented:
        return BUCKET_ASSUMPTION                    # a documented rejection answered a setup call — precondition
    return BUCKET_BUG


def run(name: str, api: str, call: Callable[[], Resp], expect_status: int, expect_code: str | None = None,
        covers: str = "", negative: str = "", check: Callable[[Resp], str | None] | None = None) -> Resp:
    """An ASSERTED call: HTTP status (+ runtime code and message presence for a negative)."""
    resp = call()
    reasons = []
    if resp.status != expect_status:
        reasons.append(f"expected HTTP {expect_status}, got {resp.status}"
                       + (f" {resp.code}" if resp.code else "") + (f" [{resp.transport_error}]" if resp.transport_error else ""))
    if expect_code and resp.status is not None:
        if not resp.has_code(expect_code):
            reasons.append(f"expected code {expect_code}, got {resp.code} fieldErrors={resp.field_codes}")
        if not (resp.message or "").strip():
            reasons.append("error.message empty (message presence, primary language)")
    if not reasons and check:
        try:
            msg = check(resp)
        except Exception as e:                      # a malformed body fails the assertion, never the run
            msg = f"check raised {type(e).__name__}: {e}"
        if msg:
            reasons.append(msg)
    ok = not reasons
    detail = "; ".join(reasons)
    if ok and expect_code:
        detail = f"code in {resp.code_slot(expect_code)}"
    RUN.add(TestResult(RUN.current.name, name, api, covers, negative, resp.method, resp.path,
                       f"{expect_status}" + (f" {expect_code}" if expect_code else ""), resp.status, resp.code,
                       resp.message, ok, "assert", None if ok else classify(api, expect_status, resp), detail, resp.at))
    return resp


def run_observation(name: str, api: str, call: Callable[[], Resp], expected: str, covers: str = "") -> Resp:
    """Stage E — executed and recorded, never pass/fail, never in totals or the exit code."""
    resp = call()
    extra = f"fieldErrors={resp.field_codes}" if resp.field_codes else ""
    if resp.transport_error:
        extra = resp.transport_error
    RUN.add(TestResult(RUN.current.name, name, api, covers, "", resp.method, resp.path, expected, resp.status,
                       resp.code, resp.message, None, "observation", None, extra, resp.at))
    return resp


def skip(name: str, reason: str, api: str = "") -> None:
    RUN.add(TestResult(RUN.current.name, name, api, "", "", "", "", "", None, None, None, None, "skipped", None, reason))


# ══════════════════════════════════════════════════════════════════════════════════════════
# helpers
# ══════════════════════════════════════════════════════════════════════════════════════════

CREATED: dict[str, list] = {}          # entity → [ids, in creation order]
DEACTIVATED: set = set()               # (entity, id) already deactivated by a test step
CTX: dict[str, Any] = {}               # threaded ids and values — never literals
_SEQ = [0]
_SEQ_LOCK = threading.Lock()


def _run_tag() -> str:
    n, digits, out = int(time.time()), "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ", ""
    while n:
        n, r = divmod(n, 36)
        out = digits[r] + out
    return out[-6:]


def unique_code(example: str, max_len: int) -> str:
    """PLACEHOLDER — an api-docs example + the run tag (+ a counter), for columns unique on ALL rows."""
    with _SEQ_LOCK:
        _SEQ[0] += 1
        n = _SEQ[0]
    suffix = f"{ARGS.run_tag}{n:02d}"
    return (example + suffix)[:max_len] if len(example) + len(suffix) <= max_len else (example[: max_len - len(suffix)] + suffix)


def extract_token(resp: Resp) -> str | None:
    src = resp.data if isinstance(resp.data, dict) else resp.body
    if isinstance(src, str):
        return src
    if isinstance(src, dict):
        for k in TOKEN_KEYS:
            if isinstance(src.get(k), str):
                return src[k]
    return None


def extract_id(data: Any, key: str) -> Any:
    return data.get(key) if isinstance(data, dict) else None


def page_rows(data: Any) -> list:
    """Rows of a Page<T>. `content` is NOT in the documented Page<T> table — a flagged documentation gap."""
    rows = data.get("content") if isinstance(data, dict) else None
    return rows if isinstance(rows, list) else []


def first_id_of_page(data: Any, key: str) -> Any:
    rows = page_rows(data)
    return rows[0].get(key) if rows and isinstance(rows[0], dict) else None


def track(entity: str, pk: Any) -> None:
    if pk is not None:
        CREATED.setdefault(entity, []).append(pk)


def track_if_created(entity: str, key: str, resp: Resp) -> None:
    if resp.status in (200, 201):
        track(entity, extract_id(resp.data, key))


def same(a: Any, b: Any) -> bool:
    if isinstance(b, bool) or isinstance(a, bool):
        return a is b
    if isinstance(b, (int, float)) and isinstance(a, (int, float)):
        return float(a) == float(b)
    return str(a) == str(b)


def expect_fields(obj: Any, expected: dict) -> str | None:
    if not isinstance(obj, dict):
        return "response data is not an object"
    bad = [f"{k}={obj.get(k)!r} (expected {v!r})" for k, v in expected.items() if not same(obj.get(k), v)]
    return "; ".join(bad) or None


def check_page(r: Resp, total: int | None = None) -> str | None:
    d = r.data
    if not isinstance(d, dict):
        return "data is not a Page<T>"
    missing = [k for k in ("totalElements", "number", "size") if k not in d]
    if missing:
        return f"documented page fields missing: {missing}"
    if total is not None and d.get("totalElements") != total:
        return f"totalElements={d.get('totalElements')} (expected {total})"
    return None


def search_by(label: str, api: str, path: str, field_name: str, value: Any, pk_key: str, pk: Any,
              expected: dict | None = None, extra_filters: list | None = None, covers: str = "") -> Resp:
    """The `read` step for entities without a get-by-id: a search filtered on the id."""
    body = {"filters": (extra_filters or []) + [{"field": field_name, "operator": "EQUALS", "value": value}],
            "page": 0, "size": PAGE_SIZE}

    def check(r: Resp) -> str | None:
        msg = check_page(r, 1)
        if msg:
            return msg
        row = next((x for x in page_rows(r.data) if isinstance(x, dict) and same(x.get(pk_key), pk)), None)
        return expect_fields(row, expected) if (row and expected) else None

    return run(f"read {label} (search {field_name} EQUALS)", api, lambda: CLIENT.post(path, body), 200,
               covers=covers or api, check=check)


def deactivate(entity: str, api: str, path_tpl: str, pk: Any, label: str, key: str = "isActiveFl") -> Resp:
    r = run(f"deactivate {label}", api, lambda: CLIENT.put(path_tpl.format(id=pk)), 200, covers=api,
            check=lambda x: expect_fields(x.data, {key: False}))
    if r.status == 200:
        DEACTIVATED.add((entity, pk))
    return r


def explore(api: str, method: str, path: str, make_payload: Callable[[], dict], fields: list,
            entity: str | None = None, pk_key: str | None = None, mismatch: tuple | None = None) -> None:
    """Stage E, bounded to required / length-constrained fields: omission, at-limit, one-over, type mismatch.
    fields = [(name, required, maxLength or None, at_limit_allowed)]. Outcomes are OBSERVATIONS.
    An at-limit call that succeeds creates a row — it is tracked for teardown."""
    for name, required, max_len, at_limit in fields:
        if required:
            p = make_payload()
            p.pop(name, None)
            r = run_observation(f"omit required `{name}`", api, lambda p=p: CLIENT.call(method, path, p),
                                "400 VALIDATION_ERROR (shared handler — no RULE)")
            if entity:
                track_if_created(entity, pk_key, r)
        if max_len:
            if at_limit:
                p = make_payload()
                p[name] = (str(p.get(name) or "") + "X" * max_len)[:max_len]
                r = run_observation(f"`{name}` at maxLength {max_len}", api, lambda p=p: CLIENT.call(method, path, p),
                                    "2xx (within the documented constraint)")
                if entity:
                    track_if_created(entity, pk_key, r)
            p = make_payload()
            p[name] = (str(p.get(name) or "") + "X" * (max_len + 1))[: max_len + 1]
            r = run_observation(f"`{name}` one over maxLength ({max_len + 1})", api,
                                lambda p=p: CLIENT.call(method, path, p), "400 VALIDATION_ERROR (no RULE)")
            if entity:
                track_if_created(entity, pk_key, r)
    if mismatch:
        name, bad = mismatch
        p = make_payload()
        p[name] = bad
        r = run_observation(f"type mismatch on `{name}` ({bad!r})", api, lambda p=p: CLIENT.call(method, path, p),
                            "400 VALIDATION_ERROR (malformed body — no RULE)")
        if entity:
            track_if_created(entity, pk_key, r)


def need(*keys: str) -> str | None:
    missing = [k for k in keys if CTX.get(k) is None]
    return f"dependency not created: {', '.join(missing)}" if missing else None


# ══════════════════════════════════════════════════════════════════════════════════════════
# payloads — api-docs examples verbatim; FK ids threaded; unique codes run-tagged (PLACEHOLDER)
# ══════════════════════════════════════════════════════════════════════════════════════════

def account_payload(code: str, parent_id: Any = None, is_leaf: bool = True) -> dict:
    p = {"code": code, "nameAr": "النقدية بالصندوق", "nameEn": "Cash on hand",
         "accountTypeCode": "ASSET", "natureCode": "DEBIT", "isLeafFl": is_leaf}
    if parent_id is not None:
        p["parentAccountId"] = parent_id
    return p


def dimension_payload() -> dict:
    return {"code": unique_code("COST_CENTER_", 30), "nameAr": "مركز التكلفة", "nameEn": "Cost centre"}


def dimension_value_payload(code: str = "CC-100") -> dict:
    # value codes are unique only WITHIN the (freshly created) dimension, so the examples stay verbatim
    return {"code": code, "nameAr": "الإدارة المالية", "nameEn": "Finance department", "sortOrder": 10}


def rule_payload() -> dict:
    return {"eventTypeCode": ARGS.event_type, "nameAr": "فاتورة مبيعات", "nameEn": "Sales invoice"}


def constant_line(direction: str, account_code: str, distribution: str = "FIXED", tags: list | None = None) -> dict:
    # CONSTANT: an account code, no business field (RULE-FIN-019; the API-FIN-011 example pairs both — a doc gap)
    p = {"accountDerivationTypeCode": "CONSTANT", "accountDerivationValue": account_code,
         "amountSourceTypeCode": "FIELD", "amountSourceValue": "netAmount",
         "directionCode": direction, "distributionTypeCode": distribution, "isRemainderFl": False}
    if tags is not None:
        p["dimensionTags"] = tags
    return p


def mapping_line(direction: str, tags: list | None = None) -> dict:
    # MAPPING: a business field, no account code (RULE-FIN-019)
    p = {"accountDerivationTypeCode": "MAPPING", "accountBusinessFieldCode": "PAYMENT_METHOD",
         "amountSourceTypeCode": "FIELD", "amountSourceValue": "netAmount",
         "directionCode": direction, "distributionTypeCode": "FIXED", "isRemainderFl": False}
    if tags is not None:
        p["dimensionTags"] = tags
    return p


def constant_tag(dimension_id: Any, value_id: Any) -> dict:
    return {"dimensionId": dimension_id, "valueSourceCode": "CONSTANT", "dimensionValueId": value_id}


def business_field_tag(dimension_id: Any) -> dict:
    return {"dimensionId": dimension_id, "valueSourceCode": "BUSINESS_FIELD", "businessFieldCode": "BRANCH_CODE"}


def mapping_payload(account_id: Any, value: str = "CHEQUE") -> dict:
    return {"eventTypeCode": ARGS.event_type, "businessFieldCode": "PAYMENT_METHOD",
            "businessValue": value, "accountId": account_id}


def event_payload(fields: dict | None = None, doc_date: str | None = None) -> dict:
    return {"eventReference": unique_code("SALES-INV-2026-000811-", 100), "eventTypeCode": ARGS.event_type,
            "docDate": doc_date or CTX["doc_date"], "baseAmount": 1000.0, "amounts": {"netAmount": 1000.0},
            "fields": {"PAYMENT_METHOD": "CHEQUE", "BRANCH_CODE": "CC-100"} if fields is None else fields,
            "descriptionAr": "فاتورة مبيعات", "descriptionEn": "Sales invoice"}


def je_line(account_id: Any, direction: str, dims: list | None = None) -> dict:
    p = {"accountId": account_id, "amount": 1500.0, "directionCode": direction,
         "descriptionAr": "إهلاك", "descriptionEn": "Depreciation"}
    if dims:
        p["dimensions"] = dims
    return p


def je_payload(lines: list, period_id: Any = None) -> dict:
    return {"docDate": CTX["doc_date"], "fiscalYearId": CTX["year_id"], "periodId": period_id or CTX["period1_id"],
            "journalTypeCode": "MANUAL", "descriptionAr": "قيد إهلاك يناير",
            "descriptionEn": "January depreciation entry", "lines": lines}


def balanced_je(debit_account: Any = None, dims: list | None = None) -> dict:
    return je_payload([je_line(debit_account or CTX["acc_debit"], "DEBIT", dims),
                       je_line(CTX["acc_credit"], "CREDIT")])


def template_payload(lines: list, frequency: bool = True) -> dict:
    p = {"nameAr": "إهلاك شهري", "nameEn": "Monthly depreciation", "scheduleTypeCode": "RECURRING",
         "startDate": CTX["doc_date"], "endDate": CTX["year_end"], "lines": lines}
    if frequency:
        p["frequencyCode"] = "MONTHLY"
    return p


def allocation_payload(targets: list) -> dict:
    return {"nameAr": "توزيع مصاريف الإدارة", "nameEn": "Administrative expense allocation",
            "sourceAccountId": CTX["acc_debit"], "targets": targets}


def pct_target(account_id: Any, with_dimension: bool = True) -> dict:
    t = {"targetAccountId": account_id, "distributionTypeCode": "PERCENTAGE", "distributionValue": 25.0,
         "isRemainderFl": False}
    if with_dimension:
        t["dimensionValueId"] = CTX["dv_main"]
    return t


def remainder_target(account_id: Any) -> dict:
    return {"targetAccountId": account_id, "distributionTypeCode": "REMAINDER", "isRemainderFl": True}


# ══════════════════════════════════════════════════════════════════════════════════════════
# per entity — stage-B order
# ══════════════════════════════════════════════════════════════════════════════════════════

ACC = "/api/v1/fin/accounts"


def create_account(label: str, parent_id: Any = None, is_leaf: bool = True) -> dict | None:
    code = unique_code("1101", 30)
    r = run(f"create account [{label}]", "API-FIN-002", lambda: CLIENT.post(ACC, account_payload(code, parent_id, is_leaf)),
            201, covers="API-FIN-002",
            check=lambda x: expect_fields(x.data, {"code": code, "isLeafFl": is_leaf, "isActiveFl": True}))
    pk = extract_id(r.data, "accountPk")
    track("account", pk)
    return {"id": pk, "code": code} if pk is not None else None


def test_account() -> None:
    """Covers: API-FIN-002, API-FIN-001, API-FIN-003, API-FIN-004 ; ENT-FIN-001
    Negative: RULE-FIN-001 / FIN-409-HAS-CHILDREN / TC-FIN-002"""
    RUN.begin("account — ENT-FIN-001")
    parent = create_account("parent, not a leaf", is_leaf=False)
    if not parent:
        skip("account suite", "parent account not created — every dependent suite will skip", "API-FIN-002")
        return
    CTX["acc_parent"] = parent["id"]
    for key, label in (("acc_debit", "leaf D — debit side"), ("acc_credit", "leaf C — credit side"),
                       ("acc_mapped", "leaf M — the mapping's account"), ("acc_target", "leaf T — allocation remainder target"),
                       ("acc_spare", "leaf S — deactivate step")):
        a = create_account(label, parent_id=parent["id"])
        CTX[key] = a["id"] if a else None
        CTX[key + "_code"] = a["code"] if a else None

    search_by("parent account", "API-FIN-001", f"{ACC}/search", "accountPk", parent["id"], "accountPk", parent["id"],
              {"code": parent["code"], "isLeafFl": False})
    run("search accounts (unfiltered page)", "API-FIN-001",
        lambda: CLIENT.post(f"{ACC}/search", {"page": 0, "size": PAGE_SIZE}), 200, check=check_page)
    skip("read account by id", "no get-by-id is published (manifest CRUD checklist: deliberate v1 exclusion)")

    if CTX.get("acc_debit"):
        run("update leaf D (names, isLeafFl stays true)", "API-FIN-003",
            lambda: CLIENT.put(f"{ACC}/{CTX['acc_debit']}", {"nameAr": "النقدية بالصندوق", "nameEn": "Cash on hand", "isLeafFl": True}),
            200, check=lambda x: expect_fields(x.data, {"accountPk": CTX["acc_debit"], "isLeafFl": True}))

    # Negative: RULE-FIN-001 / FIN-409-HAS-CHILDREN / TC-FIN-002 — the parent already has sub-accounts
    run("mark parent-with-children as leaf", "API-FIN-003",
        lambda: CLIENT.put(f"{ACC}/{parent['id']}", {"nameAr": "النقدية بالصندوق", "nameEn": "Cash on hand", "isLeafFl": True}),
        409, "FIN-409-HAS-CHILDREN", negative="RULE-FIN-001 / FIN-409-HAS-CHILDREN / TC-FIN-002")

    if CTX.get("acc_spare"):
        deactivate("account", "API-FIN-004", ACC + "/{id}/deactivate", CTX["acc_spare"], "leaf S")
        run_observation("deactivate leaf S twice (idempotency)", "API-FIN-004",
                        lambda: CLIENT.put(f"{ACC}/{CTX['acc_spare']}/deactivate"), "undocumented — 200 or 409")
    skip("activate account", "no activate endpoint in FIN (manifest CRUD checklist)")

    # Stage E — observations
    if CTX.get("acc_debit"):
        r = run_observation("create sub-account under a LEAF parent (RULE-FIN-001 create path, no TC)", "API-FIN-002",
                            lambda: CLIENT.post(ACC, account_payload(unique_code("1101", 30), CTX["acc_debit"])),
                            "409 FIN-409-PARENT-NOT-LEAF-ELIGIBLE (documented; the manifest cites no TC)")
        track_if_created("account", "accountPk", r)
    r = run_observation("duplicate account code", "API-FIN-002", lambda: CLIENT.post(ACC, account_payload(parent["code"], is_leaf=False)),
                        "409 FIN-409-ACCOUNT-DUP (documented; no TC)")
    track_if_created("account", "accountPk", r)
    p = account_payload(unique_code("1101", 30), is_leaf=False)
    p["accountTypeCode"] = PLACEHOLDER_NOT_A_LOOKUP
    r = run_observation("undocumented accountTypeCode lookup value", "API-FIN-002", lambda: CLIENT.post(ACC, p),
                        "400 FIN-400-INVALID-LOOKUP (documented; no TC)")
    track_if_created("account", "accountPk", r)
    r = run_observation("invalid FK parentAccountId", "API-FIN-002",
                        lambda: CLIENT.post(ACC, account_payload(unique_code("1101", 30), PLACEHOLDER_MISSING_ID)),
                        "404 FIN-404-ACCOUNT (documented; no TC)")
    track_if_created("account", "accountPk", r)
    run_observation("deactivate a missing account id", "API-FIN-004",
                    lambda: CLIENT.put(f"{ACC}/{PLACEHOLDER_MISSING_ID}/deactivate"), "404 FIN-404-ACCOUNT (documented; no TC)")
    explore("API-FIN-002", "POST", ACC, lambda: account_payload(unique_code("1101", 30), is_leaf=False),
            [("code", True, 30, True), ("nameAr", True, 200, True), ("nameEn", True, 200, True),
             ("accountTypeCode", True, 20, False), ("natureCode", True, 10, False)],
            "account", "accountPk", mismatch=("parentAccountId", "not-a-number"))


DIM = "/api/v1/fin/dimensions"


def test_dimension() -> None:
    """Covers: API-FIN-006, API-FIN-005 ; ENT-FIN-002"""
    RUN.begin("dimension — ENT-FIN-002")
    p = dimension_payload()
    r = run("create dimension", "API-FIN-006", lambda: CLIENT.post(DIM, p), 201,
            check=lambda x: expect_fields(x.data, {"code": p["code"], "isActiveFl": True}))
    CTX["dimension"] = extract_id(r.data, "dimensionPk")
    track("dimension", CTX["dimension"])
    if CTX["dimension"] is None:
        return
    search_by("dimension", "API-FIN-005", f"{DIM}/search", "dimensionPk", CTX["dimension"], "dimensionPk",
              CTX["dimension"], {"code": p["code"]})
    skip("read dimension by id / update / deactivate / activate",
         "none published (manifest CRUD checklist: deliberate — no REQ/AC/RULE needs them)")
    r = run_observation("duplicate dimension code", "API-FIN-006", lambda: CLIENT.post(DIM, dict(p)),
                        "409 FIN-409-DIMENSION-DUP (documented; no TC)")
    track_if_created("dimension", "dimensionPk", r)
    explore("API-FIN-006", "POST", DIM, dimension_payload,
            [("code", True, 30, True), ("nameAr", True, 150, True), ("nameEn", True, 150, True)], "dimension", "dimensionPk")


def test_dimension_value() -> None:
    """Covers: API-FIN-007, API-FIN-008, API-FIN-035 ; ENT-FIN-003
    Negative: RULE-FIN-002 / FIN-409-DIMVALUE-DUP / TC-FIN-006"""
    RUN.begin("dimension value — ENT-FIN-003")
    why = need("dimension")
    if why:
        skip("dimension value suite", why)
        return
    path = f"{DIM}/{CTX['dimension']}/values"
    for key, code in (("dv_main", "CC-100"), ("dv_spare", "PRJ-A")):   # API-FIN-007 / API-FIN-032 examples
        r = run(f"create dimension value {code}", "API-FIN-007", lambda code=code: CLIENT.post(path, dimension_value_payload(code)),
                201, check=lambda x, code=code: expect_fields(x.data, {"code": code, "dimensionId": CTX["dimension"], "isActiveFl": True}))
        CTX[key] = extract_id(r.data, "dimensionValuePk")
        track("dimension_value", CTX[key])
    if CTX.get("dv_main") is not None:
        search_by("dimension value CC-100", "API-FIN-008", f"{DIM}/values/search", "dimensionValuePk", CTX["dv_main"],
                  "dimensionValuePk", CTX["dv_main"], {"code": "CC-100"},
                  extra_filters=[{"field": "dimensionId", "operator": "EQUALS", "value": CTX["dimension"]}])
    skip("read dimension value by id / update / activate", "none published (manifest CRUD checklist)")
    # Negative: RULE-FIN-002 / FIN-409-DIMVALUE-DUP / TC-FIN-006 — same code, same dimension
    r = run("duplicate value code within its dimension", "API-FIN-007", lambda: CLIENT.post(path, dimension_value_payload("CC-100")),
            409, "FIN-409-DIMVALUE-DUP", negative="RULE-FIN-002 / FIN-409-DIMVALUE-DUP / TC-FIN-006")
    track_if_created("dimension_value", "dimensionValuePk", r)
    # PRJ-A is the one value this run deactivates — the setup of TC-FIN-021 / TC-FIN-118 (manifest step 4)
    if CTX.get("dv_spare") is not None:
        deactivate("dimension_value", "API-FIN-035", DIM + "/values/{id}/deactivate", CTX["dv_spare"], "value PRJ-A")
        run_observation("deactivate value PRJ-A twice (idempotency)", "API-FIN-035",
                        lambda: CLIENT.put(f"{DIM}/values/{CTX['dv_spare']}/deactivate"), "undocumented — 200 or 409")
    r = run_observation("create value under a missing dimension", "API-FIN-007",
                        lambda: CLIENT.post(f"{DIM}/{PLACEHOLDER_MISSING_ID}/values", dimension_value_payload("CC-100")),
                        "404 FIN-404-DIMENSION (documented; no TC)")
    track_if_created("dimension_value", "dimensionValuePk", r)
    run_observation("deactivate a missing value id", "API-FIN-035",
                    lambda: CLIENT.put(f"{DIM}/values/{PLACEHOLDER_MISSING_ID}/deactivate"), "404 FIN-404-DIMVALUE (documented; no TC)")
    explore("API-FIN-007", "POST", path, lambda: dimension_value_payload(unique_code("CC-100-", 30)),
            [("code", True, 30, True), ("nameAr", True, 150, True), ("nameEn", True, 150, True), ("sortOrder", True, None, False)],
            "dimension_value", "dimensionValuePk", mismatch=("sortOrder", "ten"))


FY = "/api/v1/fin/fiscal-years"
FP = "/api/v1/fin/fiscal-periods"


def test_fiscal_year() -> None:
    """Covers: API-FIN-023, POST /api/v1/fin/fiscal-years/search (no contract id) ; ENT-FIN-007, ENT-FIN-008 (generated)"""
    RUN.begin("fiscal year — ENT-FIN-007")
    code = (ARGS.year_start[:4] + ARGS.run_tag)[:10]      # example "2026" + run tag, maxLength 10 — PLACEHOLDER
    p = {"code": code, "startDate": ARGS.year_start, "endDate": ARGS.year_end, "periodCount": ARGS.period_count}

    def check(x: Resp) -> str | None:
        msg = expect_fields(x.data, {"code": code, "startDate": ARGS.year_start, "endDate": ARGS.year_end})
        periods = (x.data or {}).get("periods") or []
        return msg or (None if len(periods) == ARGS.period_count else f"{len(periods)} periods generated (expected {ARGS.period_count})")

    r = run("create fiscal year with its periods", "API-FIN-023", lambda: CLIENT.post(FY, p), 201, check=check)
    CTX["year_id"] = extract_id(r.data, "fiscalYearPk")
    CTX["year_code"] = code
    track("fiscal_year", CTX["year_id"])
    periods = sorted(((r.data or {}).get("periods") or []), key=lambda x: x.get("periodNo") or 0)
    if CTX["year_id"] is None or not periods:
        return
    CTX["periods"] = [x.get("fiscalPeriodPk") for x in periods]
    CTX["period1_id"] = periods[0].get("fiscalPeriodPk")
    CTX["doc_date"] = periods[0].get("endDate")        # the example docDate 2026-01-31 IS period 1's endDate — threaded
    CTX["year_end"] = ARGS.year_end
    CTX["period_last_id"] = periods[-1].get("fiscalPeriodPk")
    CTX["period2_id"] = periods[1].get("fiscalPeriodPk") if len(periods) > 1 else None
    search_by("fiscal year", "FY-SEARCH", f"{FY}/search", "fiscalYearPk", CTX["year_id"], "fiscalYearPk", CTX["year_id"],
              {"code": code}, covers="POST /api/v1/fin/fiscal-years/search")
    skip("read year by id / deactivate / activate", "none published (manifest CRUD checklist: create response carries the year)")
    r = run_observation("duplicate fiscal-year code", "API-FIN-023", lambda: CLIENT.post(FY, dict(p)),
                        "409 FIN-409-YEAR-DUP (documented; no TC)")
    track_if_created("fiscal_year", "fiscalYearPk", r)
    # at-limit is NOT exercised here: a second accepted year would overlap the run's dates (see DOC_GAPS)
    explore("API-FIN-023", "POST", FY, lambda: dict(p, code=unique_code("", 10)),
            [("code", True, 10, False), ("startDate", True, None, False), ("endDate", True, None, False),
             ("periodCount", True, None, False)], "fiscal_year", "fiscalYearPk", mismatch=("periodCount", "twelve"))


def test_fiscal_period() -> None:
    """Covers: API-FIN-033, API-FIN-025, API-FIN-024, API-FIN-026 ; ENT-FIN-008
    Negative: RULE-FIN-014 / FIN-409-NOT-REOPENABLE / TC-FIN-035"""
    RUN.begin("fiscal period — ENT-FIN-008")
    why = need("year_id", "period_last_id")
    if why:
        skip("fiscal period suite", why)
        return
    run("search periods of the run's year", "API-FIN-033",
        lambda: CLIENT.post(f"{FP}/search", {"filters": [{"field": "fiscalYearId", "operator": "EQUALS", "value": CTX["year_id"]}],
                                             "page": 0, "size": PAGE_SIZE}),
        200, check=lambda x: check_page(x, ARGS.period_count))
    if CTX.get("period2_id"):
        run("soft-close period 2", "API-FIN-025", lambda: CLIENT.patch(f"{FP}/{CTX['period2_id']}/soft-close"), 200,
            check=lambda x: expect_fields(x.data, {"statusCode": "SOFT_CLOSE"}))
        run_observation("soft-close period 2 twice", "API-FIN-025", lambda: CLIENT.patch(f"{FP}/{CTX['period2_id']}/soft-close"),
                        "409 FIN-409-INVALID-TRANSITION (documented; no TC)")
        run("re-open period 2 (SOFT_CLOSE → OPEN)", "API-FIN-024", lambda: CLIENT.patch(f"{FP}/{CTX['period2_id']}/open"), 200,
            check=lambda x: expect_fields(x.data, {"statusCode": "OPEN"}))
    last = CTX["period_last_id"]
    run("soft-close the last period", "API-FIN-025", lambda: CLIENT.patch(f"{FP}/{last}/soft-close"), 200,
        check=lambda x: expect_fields(x.data, {"statusCode": "SOFT_CLOSE"}))
    run("hard-close the last period (approval)", "API-FIN-026", lambda: CLIENT.patch(f"{FP}/{last}/hard-close"), 200,
        check=lambda x: expect_fields(x.data, {"statusCode": "HARD_CLOSE"}))
    # Negative: RULE-FIN-014 / FIN-409-NOT-REOPENABLE / TC-FIN-035 — a hard-closed period never reopens
    run("re-open a hard-closed period", "API-FIN-024", lambda: CLIENT.patch(f"{FP}/{last}/open"), 409, "FIN-409-NOT-REOPENABLE",
        negative="RULE-FIN-014 / FIN-409-NOT-REOPENABLE / TC-FIN-035")
    run_observation("open a missing period id", "API-FIN-024", lambda: CLIENT.patch(f"{FP}/{PLACEHOLDER_MISSING_ID}/open"),
                    "404 FIN-404-PERIOD (documented; no TC)")


RULES = "/api/v1/fin/event-rules"


def test_event_type_rule() -> None:
    """Covers: API-FIN-010, API-FIN-009 ; ENT-FIN-009 (API-FIN-034 runs in test_event_type_rule_deactivate)"""
    RUN.begin("event-type rule — ENT-FIN-009")
    p = rule_payload()
    r = run(f"create event-type rule for {ARGS.event_type}", "API-FIN-010", lambda: CLIENT.post(RULES, p), 201,
            check=lambda x: expect_fields(x.data, {"eventTypeCode": ARGS.event_type, "isActiveFl": True}))
    CTX["rule"] = extract_id(r.data, "eventTypeRulePk")
    track("event_type_rule", CTX["rule"])
    if CTX["rule"] is None:
        if r.code == "FIN-409-RULE-DUP":
            skip("event path", f"{ARGS.event_type} already holds a rule (unique on ALL rows, no activate) — "
                               "rerun with --event-type <an ACCOUNTING_EVENT_TYPE value with no rule>")
        return
    search_by("event-type rule", "API-FIN-009", f"{RULES}/search", "eventTypeRulePk", CTX["rule"], "eventTypeRulePk",
              CTX["rule"], {"eventTypeCode": ARGS.event_type})
    skip("read rule by id / update / activate", "none published (manifest CRUD checklist)")
    skip("deactivate rule", "deferred to test_event_type_rule_deactivate — dependents (rule lines, events) need it active")
    r = run_observation("second rule for the same event type", "API-FIN-010", lambda: CLIENT.post(RULES, dict(p)),
                        "409 FIN-409-RULE-DUP (documented; no TC)")
    track_if_created("event_type_rule", "eventTypeRulePk", r)
    p2 = dict(p, eventTypeCode=PLACEHOLDER_NOT_A_LOOKUP)
    r = run_observation("undocumented eventTypeCode lookup value", "API-FIN-010", lambda: CLIENT.post(RULES, p2),
                        "400 FIN-400-INVALID-LOOKUP (documented; no TC)")
    track_if_created("event_type_rule", "eventTypeRulePk", r)
    # at-limit would consume another ACCOUNTING_EVENT_TYPE value (one rule per type, ever) — one-over / omission only
    explore("API-FIN-010", "POST", RULES, rule_payload,
            [("eventTypeCode", True, 50, False), ("nameAr", True, 150, False), ("nameEn", True, 150, False)],
            "event_type_rule", "eventTypeRulePk")


def test_rule_line() -> None:
    """Covers: API-FIN-011 ; ENT-FIN-010, ENT-FIN-016 (tags are written with their line)
    Success paths: a MAPPING line with a BUSINESS_FIELD tag, a CONSTANT line with a CONSTANT tag (TC-FIN-095)"""
    RUN.begin("rule line + dimension tags — ENT-FIN-010 / ENT-FIN-016")
    why = need("rule", "acc_credit_code", "dimension", "dv_main")
    if why:
        skip("rule line suite", why)
        return
    path = f"{RULES}/{CTX['rule']}/lines"
    lines = [("MAPPING debit line, BUSINESS_FIELD tag BRANCH_CODE", mapping_line("DEBIT", [business_field_tag(CTX["dimension"])])),
             ("CONSTANT credit line, CONSTANT tag CC-100",
              constant_line("CREDIT", CTX["acc_credit_code"], tags=[constant_tag(CTX["dimension"], CTX["dv_main"])]))]
    CTX["rule_lines"] = []
    for label, body in lines:
        r = run(f"add rule line — {label}", "API-FIN-011", lambda body=body: CLIENT.post(path, body), 201,
                check=lambda x, body=body: expect_fields(x.data, {"eventTypeRuleId": CTX["rule"],
                                                                  "accountDerivationTypeCode": body["accountDerivationTypeCode"]})
                or (None if len((x.data or {}).get("dimensionTags") or []) == 1 else "dimensionTags not echoed"))
        CTX["rule_lines"].append(extract_id(r.data, "ruleLinePk"))
        track("rule_line", CTX["rule_lines"][-1])
    skip("read/update/deactivate a rule line", "embedded in the rule; no update; no active flag (manifest CRUD checklist)")
    run("rule search shows both lines", "API-FIN-009",
        lambda: CLIENT.post(f"{RULES}/search", {"filters": [{"field": "eventTypeRulePk", "operator": "EQUALS", "value": CTX["rule"]}],
                                                "page": 0, "size": PAGE_SIZE}), 200,
        check=lambda x: check_page(x, 1) or next((f"lineCount={row.get('lineCount')}" for row in page_rows(x.data)
                                                   if isinstance(row, dict) and row.get("lineCount") != 2), None))


def test_rule_line_negatives() -> None:
    """Covers: API-FIN-011 ; runs AFTER the event path so a wrongly accepted probe line cannot mask it.
    Negative: RULE-FIN-018 / FIN-422-UNSUPPORTED-DERIVATION / TC-FIN-094
    Negative: RULE-FIN-025 / FIN-422-DIMENSION-TAG-DUP / TC-FIN-115
    Negative: RULE-FIN-026 / FIN-422-DIMENSION-TAG-SOURCE / TC-FIN-116
    Negative: RULE-FIN-003 / FIN-409-REMAINDER-COUNT / TC-FIN-009, TC-FIN-047"""
    RUN.begin("rule line — rule violations")
    why = need("rule", "acc_credit_code", "dimension", "dv_main")
    if why:
        skip("rule line negatives", why)
        return
    path = f"{RULES}/{CTX['rule']}/lines"
    direct = constant_line("DEBIT", CTX["acc_credit_code"])
    direct["accountDerivationTypeCode"] = "DIRECT"             # retired in MDL (manifest step 0)
    run("DIRECT account derivation", "API-FIN-011", lambda: CLIENT.post(path, direct), 422, "FIN-422-UNSUPPORTED-DERIVATION",
        negative="RULE-FIN-018 / FIN-422-UNSUPPORTED-DERIVATION / TC-FIN-094")
    dup = constant_line("DEBIT", CTX["acc_credit_code"],
                        tags=[constant_tag(CTX["dimension"], CTX["dv_main"]), constant_tag(CTX["dimension"], CTX["dv_main"])])
    run("two tags on the same dimension", "API-FIN-011", lambda: CLIENT.post(path, dup), 422, "FIN-422-DIMENSION-TAG-DUP",
        negative="RULE-FIN-025 / FIN-422-DIMENSION-TAG-DUP / TC-FIN-115")
    bad_tag = dict(constant_tag(CTX["dimension"], CTX["dv_main"]), businessFieldCode="BRANCH_CODE")   # the API-FIN-011 example's tag shape
    run("CONSTANT tag that also names a business field", "API-FIN-011",
        lambda: CLIENT.post(path, constant_line("DEBIT", CTX["acc_credit_code"], tags=[bad_tag])), 422,
        "FIN-422-DIMENSION-TAG-SOURCE", negative="RULE-FIN-026 / FIN-422-DIMENSION-TAG-SOURCE / TC-FIN-116")
    run("PERCENTAGE line with no remainder sibling", "API-FIN-011",
        lambda: CLIENT.post(path, constant_line("DEBIT", CTX["acc_credit_code"], distribution="PERCENTAGE")), 409,
        "FIN-409-REMAINDER-COUNT", negative="RULE-FIN-003 / FIN-409-REMAINDER-COUNT / TC-FIN-009, TC-FIN-047")
    spec = constant_line("DEBIT", CTX["acc_credit_code"])
    spec["accountBusinessFieldCode"] = "PAYMENT_METHOD"         # the API-FIN-011 example's line shape
    run_observation("CONSTANT line that also names a business field (RULE-FIN-019, no TC)", "API-FIN-011",
                    lambda: CLIENT.post(path, spec), "422 FIN-422-DERIVATION-SPEC (documented; no TC raises it)")
    run_observation("add a line to a missing rule", "API-FIN-011",
                    lambda: CLIENT.post(f"{RULES}/{PLACEHOLDER_MISSING_ID}/lines", constant_line("DEBIT", CTX["acc_credit_code"])),
                    "404 FIN-404-RULE (documented; no TC)")
    run_observation("tag an unknown dimension", "API-FIN-011",
                    lambda: CLIENT.post(path, constant_line("DEBIT", CTX["acc_credit_code"],
                                                            tags=[constant_tag(PLACEHOLDER_MISSING_ID, CTX["dv_main"])])),
                    "404 FIN-404-DIMENSION (documented; no TC)")
    explore("API-FIN-011", "POST", path, lambda: constant_line("DEBIT", CTX["acc_credit_code"]),
            [("accountDerivationTypeCode", True, 20, False), ("amountSourceTypeCode", True, 20, False),
             ("directionCode", True, 10, False), ("distributionTypeCode", True, 15, False)])


MAP = "/api/v1/fin/account-mappings"


def test_account_mapping() -> None:
    """Covers: API-FIN-039, API-FIN-038, API-FIN-040 ; ENT-FIN-015 (API-FIN-041 runs in test_account_mapping_deactivate)
    Negative: RULE-FIN-022 / FIN-409-MAPPING-DUP / TC-FIN-109
    Negative: RULE-FIN-023 / FIN-422-MAPPED-ACCOUNT-INELIGIBLE / TC-FIN-110
    Negative: RULE-FIN-024 / FIN-422-INVALID-PAYMENT-METHOD / TC-FIN-111"""
    RUN.begin("account mapping — ENT-FIN-015")
    why = need("acc_mapped", "acc_parent")
    if why:
        skip("account mapping suite", why)
        return
    p = mapping_payload(CTX["acc_mapped"])
    r = run("create mapping PAYMENT_METHOD=CHEQUE → leaf M", "API-FIN-039", lambda: CLIENT.post(MAP, p), 201,
            check=lambda x: expect_fields(x.data, {"eventTypeCode": ARGS.event_type, "businessValue": "CHEQUE",
                                                   "accountId": CTX["acc_mapped"], "isActiveFl": True}))
    CTX["mapping"] = extract_id(r.data, "accountMappingPk")
    track("account_mapping", CTX["mapping"])
    if CTX["mapping"] is None:
        return
    search_by("mapping", "API-FIN-038", f"{MAP}/search", "accountMappingPk", CTX["mapping"], "accountMappingPk",
              CTX["mapping"], {"businessValue": "CHEQUE"})
    skip("read mapping by id / activate", "none (manifest CRUD checklist: search row hydrates; restore = new mapping)")
    run("update mapping (the account only)", "API-FIN-040", lambda: CLIENT.put(f"{MAP}/{CTX['mapping']}", {"accountId": CTX["acc_mapped"]}),
        200, check=lambda x: expect_fields(x.data, {"accountId": CTX["acc_mapped"]}))
    # Negative: RULE-FIN-022 — the active key is already held
    r = run("second active mapping for the same key", "API-FIN-039", lambda: CLIENT.post(MAP, dict(p)), 409, "FIN-409-MAPPING-DUP",
            negative="RULE-FIN-022 / FIN-409-MAPPING-DUP / TC-FIN-109")
    track_if_created("account_mapping", "accountMappingPk", r)
    # Negative: RULE-FIN-023 — the parent has sub-accounts, so it is not a leaf
    run("re-point the mapping at a non-leaf account", "API-FIN-040",
        lambda: CLIENT.put(f"{MAP}/{CTX['mapping']}", {"accountId": CTX["acc_parent"]}), 422, "FIN-422-MAPPED-ACCOUNT-INELIGIBLE",
        negative="RULE-FIN-023 / FIN-422-MAPPED-ACCOUNT-INELIGIBLE / TC-FIN-110")
    # Negative: RULE-FIN-024 — businessFieldCode PAYMENT_METHOD with a value that is no PAYMENT_METHOD code
    r = run("PAYMENT_METHOD mapping for an undefined payment method", "API-FIN-039",
            lambda: CLIENT.post(MAP, mapping_payload(CTX["acc_mapped"], PLACEHOLDER_NOT_A_PAYMENT_METHOD)), 422,
            "FIN-422-INVALID-PAYMENT-METHOD", negative="RULE-FIN-024 / FIN-422-INVALID-PAYMENT-METHOD / TC-FIN-111")
    track_if_created("account_mapping", "accountMappingPk", r)
    run_observation("update a missing mapping id", "API-FIN-040",
                    lambda: CLIENT.put(f"{MAP}/{PLACEHOLDER_MISSING_ID}", {"accountId": CTX["acc_mapped"]}),
                    "404 FIN-404-ACCOUNT-MAPPING (documented; no TC)")
    run_observation("update to a missing account id", "API-FIN-040",
                    lambda: CLIENT.put(f"{MAP}/{CTX['mapping']}", {"accountId": PLACEHOLDER_MISSING_ID}),
                    "404 FIN-404-ACCOUNT (documented; no TC)")
    explore("API-FIN-039", "POST", MAP, lambda: mapping_payload(CTX["acc_mapped"], "CASH"),
            [("eventTypeCode", True, 50, False), ("businessFieldCode", True, 50, False), ("businessValue", True, 100, False),
             ("accountId", True, None, False)], "account_mapping", "accountMappingPk", mismatch=("accountId", "twelve"))


RT = "/api/v1/fin/recurring-templates"


def test_recurring_template() -> None:
    """Covers: API-FIN-013, API-FIN-012, API-FIN-014, API-FIN-036 ; ENT-FIN-011, ENT-FIN-012
    Negative: RULE-FIN-006 / FIN-409-UNBALANCED / TC-FIN-015, TC-FIN-018 (on API-FIN-014)"""
    RUN.begin("recurring template — ENT-FIN-011 / ENT-FIN-012")
    why = need("acc_debit", "acc_credit", "dv_main", "doc_date")
    if why:
        skip("recurring template suite", why)
        return
    lines = [{"accountId": CTX["acc_debit"], "amount": 1500.0, "directionCode": "DEBIT", "dimensionValueId": CTX["dv_main"]},
             {"accountId": CTX["acc_credit"], "amount": 1500.0, "directionCode": "CREDIT"}]
    r = run("create a balanced monthly template", "API-FIN-013", lambda: CLIENT.post(RT, template_payload(lines)), 201,
            check=lambda x: expect_fields(x.data, {"scheduleTypeCode": "RECURRING", "frequencyCode": "MONTHLY", "isActiveFl": True,
                                                   "lineCount": 2}))
    CTX["template"] = extract_id(r.data, "recurringTemplatePk")
    track("recurring_template", CTX["template"])
    if CTX["template"] is not None:
        search_by("template", "API-FIN-012", f"{RT}/search", "recurringTemplatePk", CTX["template"], "recurringTemplatePk",
                  CTX["template"], {"lineCount": 2})
        skip("update template", "OPEN GAP in the manifest CRUD checklist: no update endpoint")
        r = run("run the template (posts an entry)", "API-FIN-014", lambda: CLIENT.post(f"{RT}/{CTX['template']}/run"), 201,
                check=lambda x: expect_fields(x.data, {"statusCode": "POSTED"}))
        track("journal_entry", extract_id(r.data, "journalEntryPk"))
    # Negative: RULE-FIN-006 — the API-FIN-013 example itself is one DEBIT line, i.e. unbalanced
    r = run("create a one-line (unbalanced) template", "API-FIN-013",
            lambda: CLIENT.post(RT, template_payload([lines[0]])), 201)
    unbalanced = extract_id(r.data, "recurringTemplatePk")
    track("recurring_template", unbalanced)
    if unbalanced is not None:
        run("run an unbalanced template", "API-FIN-014", lambda: CLIENT.post(f"{RT}/{unbalanced}/run"), 409, "FIN-409-UNBALANCED",
            negative="RULE-FIN-006 / FIN-409-UNBALANCED / TC-FIN-015, TC-FIN-018")
        deactivate("recurring_template", "API-FIN-036", RT + "/{id}/deactivate", unbalanced, "the unbalanced template")
        run_observation("run a deactivated template", "API-FIN-014", lambda: CLIENT.post(f"{RT}/{unbalanced}/run"),
                        "409 FIN-409-NOT-ACTIVE (recorded human decision; no TC)")
    skip("activate template", "none in FIN (manifest CRUD checklist)")
    r = run_observation("RECURRING template without frequencyCode", "API-FIN-013",
                        lambda: CLIENT.post(RT, template_payload(lines, frequency=False)), "400 FIN-400-MISSING-FREQUENCY (documented; no TC)")
    track_if_created("recurring_template", "recurringTemplatePk", r)
    r = run_observation("template line on a missing account", "API-FIN-013",
                        lambda: CLIENT.post(RT, template_payload([dict(lines[0], accountId=PLACEHOLDER_MISSING_ID), lines[1]])),
                        "404 FIN-404-ACCOUNT (documented; no TC)")
    track_if_created("recurring_template", "recurringTemplatePk", r)
    run_observation("run a missing template id", "API-FIN-014", lambda: CLIENT.post(f"{RT}/{PLACEHOLDER_MISSING_ID}/run"),
                    "404 FIN-404-TEMPLATE (documented; no TC)")
    explore("API-FIN-013", "POST", RT, lambda: template_payload(lines),
            [("nameAr", True, 150, True), ("nameEn", True, 150, True), ("scheduleTypeCode", True, 15, False),
             ("frequencyCode", False, 15, False), ("startDate", True, None, False), ("lines", True, None, False)],
            "recurring_template", "recurringTemplatePk", mismatch=("startDate", "not-a-date"))


AR = "/api/v1/fin/allocation-rules"


def test_allocation_rule() -> None:
    """Covers: API-FIN-016, API-FIN-015 ; ENT-FIN-013, ENT-FIN-014 (API-FIN-017 / 037 run in test_allocation_rule_run)
    Negative: RULE-FIN-003 / FIN-409-REMAINDER-COUNT / TC-FIN-009, TC-FIN-047 (on API-FIN-016)"""
    RUN.begin("allocation rule — ENT-FIN-013 / ENT-FIN-014")
    why = need("acc_debit", "acc_credit", "acc_target", "dv_main")
    if why:
        skip("allocation rule suite", why)
        return
    body = allocation_payload([pct_target(CTX["acc_credit"]), remainder_target(CTX["acc_target"])])
    r = run("create a PERCENTAGE + REMAINDER allocation rule", "API-FIN-016", lambda: CLIENT.post(AR, body), 201,
            check=lambda x: expect_fields(x.data, {"sourceAccountId": CTX["acc_debit"], "isActiveFl": True, "targetCount": 2}))
    CTX["allocation"] = extract_id(r.data, "allocationRulePk")
    track("allocation_rule", CTX["allocation"])
    if CTX["allocation"] is not None:
        search_by("allocation rule", "API-FIN-015", f"{AR}/search", "allocationRulePk", CTX["allocation"], "allocationRulePk",
                  CTX["allocation"], {"targetCount": 2})
    skip("update allocation rule / activate", "OPEN GAP (no update endpoint) / none in FIN (manifest CRUD checklist)")
    # Negative: RULE-FIN-003 — percentage targets without exactly one remainder target
    r = run("PERCENTAGE targets with no remainder target", "API-FIN-016",
            lambda: CLIENT.post(AR, allocation_payload([pct_target(CTX["acc_credit"]), pct_target(CTX["acc_target"], False)])),
            409, "FIN-409-REMAINDER-COUNT", negative="RULE-FIN-003 / FIN-409-REMAINDER-COUNT / TC-FIN-009, TC-FIN-047")
    track_if_created("allocation_rule", "allocationRulePk", r)
    marker = dict(remainder_target(CTX["acc_target"]), isRemainderFl=False)
    r = run_observation("REMAINDER target not marked isRemainderFl (RULE-FIN-003 marker, no TC)", "API-FIN-016",
                        lambda: CLIENT.post(AR, allocation_payload([pct_target(CTX["acc_credit"]), marker])),
                        "422 FIN-422-REMAINDER-MARKER (documented; no TC)")
    track_if_created("allocation_rule", "allocationRulePk", r)
    r = run_observation("target on an inactive dimension value", "API-FIN-016",
                        lambda: CLIENT.post(AR, allocation_payload([dict(pct_target(CTX["acc_credit"]), dimensionValueId=CTX.get("dv_spare")),
                                                                    remainder_target(CTX["acc_target"])])),
                        "409 FIN-409-INVALID-DIMENSION (documented; no TC on this API)")
    track_if_created("allocation_rule", "allocationRulePk", r)
    explore("API-FIN-016", "POST", AR, lambda: allocation_payload([pct_target(CTX["acc_credit"]), remainder_target(CTX["acc_target"])]),
            [("nameAr", True, 150, True), ("nameEn", True, 150, True), ("sourceAccountId", True, None, False),
             ("targets", True, None, False)], "allocation_rule", "allocationRulePk", mismatch=("sourceAccountId", "thirty-one"))


JE = "/api/v1/fin/journal-entries"


def soft_close_period1() -> bool:
    r = run("soft-close period 1 (setup)", "API-FIN-025", lambda: CLIENT.patch(f"{FP}/{CTX['period1_id']}/soft-close"), 200,
            check=lambda x: expect_fields(x.data, {"statusCode": "SOFT_CLOSE"}))
    return r.status == 200


def reopen_period1() -> None:
    run("re-open period 1 (setup)", "API-FIN-024", lambda: CLIENT.patch(f"{FP}/{CTX['period1_id']}/open"), 200,
        check=lambda x: expect_fields(x.data, {"statusCode": "OPEN"}))


def test_journal_entry() -> None:
    """Covers: API-FIN-019, API-FIN-022, API-FIN-018, API-FIN-021 ; ENT-FIN-004, ENT-FIN-005, ENT-FIN-006
    Negative: RULE-FIN-006 / FIN-409-UNBALANCED / TC-FIN-015, TC-FIN-018
    Negative: RULE-FIN-007 / FIN-409-NOT-POSTABLE-ACCOUNT / TC-FIN-003, TC-FIN-015, TC-FIN-019
    Negative: RULE-FIN-009 / FIN-409-INVALID-DIMENSION / TC-FIN-021
    Negative: RULE-FIN-013 / FIN-409-ALREADY-REVERSED / TC-FIN-030
    Negative: RULE-FIN-008 / FIN-409-PERIOD-NOT-OPEN / TC-FIN-020, TC-FIN-033"""
    RUN.begin("journal entry (manual) — ENT-FIN-004 / 005 / 006")
    why = need("acc_debit", "acc_credit", "year_id", "period1_id", "dimension", "dv_main")
    if why:
        skip("journal entry suite", why)
        return
    dims = [{"dimensionId": CTX["dimension"], "dimensionValueId": CTX["dv_main"]}]
    r = run("post a balanced manual entry with a dimension tag", "API-FIN-019", lambda: CLIENT.post(JE, balanced_je(dims=dims)), 201,
            check=lambda x: expect_fields(x.data, {"statusCode": "POSTED", "fiscalYearId": CTX["year_id"],
                                                   "periodId": CTX["period1_id"], "lineCount": 2}))
    entry = extract_id(r.data, "journalEntryPk")
    track("journal_entry", entry)
    if entry is not None:
        run("read the entry with its lines", "API-FIN-022", lambda: CLIENT.get(f"{JE}/{entry}"), 200,
            check=lambda x: expect_fields(x.data, {"journalEntryPk": entry, "statusCode": "POSTED"})
            or (None if len((x.data or {}).get("lines") or []) == 2 else "lines not returned"))
        run("read a missing entry id", "API-FIN-022", lambda: CLIENT.get(f"{JE}/{PLACEHOLDER_MISSING_ID}"), 404, "FIN-404-ENTRY",
            covers="API-FIN-022 (skeleton: get-by-id missing → not found)")
        search_by("entry", "API-FIN-018", f"{JE}/search", "journalEntryPk", entry, "journalEntryPk", entry, {"statusCode": "POSTED"})
        run_observation("PUT on a posted entry (RULE-FIN-016 is enforced by omission — TC-FIN-016, TC-FIN-017)", "API-FIN-022",
                        lambda: CLIENT.put(f"{JE}/{entry}", balanced_je()), "405 METHOD_NOT_ALLOWED (shared handler) — no update mapping")
        r2 = run("reverse the posted entry", "API-FIN-021", lambda: CLIENT.post(f"{JE}/{entry}/reverse"), 201,
                 check=lambda x: expect_fields(x.data, {"originalEntryId": entry, "statusCode": "POSTED"}))
        track("journal_entry", extract_id(r2.data, "journalEntryPk"))
        run("reverse the same entry again", "API-FIN-021", lambda: CLIENT.post(f"{JE}/{entry}/reverse"), 409, "FIN-409-ALREADY-REVERSED",
            negative="RULE-FIN-013 / FIN-409-ALREADY-REVERSED / TC-FIN-030")
    skip("deactivate / activate entry", "no soft-delete flag on ENT-FIN-004; a posted entry is corrected by reversal")

    # Negative: RULE-FIN-006 — the API-FIN-019 example is exactly one DEBIT line
    run("post an unbalanced entry", "API-FIN-019", lambda: CLIENT.post(JE, je_payload([je_line(CTX["acc_debit"], "DEBIT")])), 409,
        "FIN-409-UNBALANCED", negative="RULE-FIN-006 / FIN-409-UNBALANCED / TC-FIN-015, TC-FIN-018")
    # Negative: RULE-FIN-007 — a non-leaf, then an inactive leaf
    run("post to an account with sub-accounts", "API-FIN-019", lambda: CLIENT.post(JE, balanced_je(CTX["acc_parent"])), 409,
        "FIN-409-NOT-POSTABLE-ACCOUNT", negative="RULE-FIN-007 / FIN-409-NOT-POSTABLE-ACCOUNT / TC-FIN-003, TC-FIN-015, TC-FIN-019")
    if ("account", CTX.get("acc_spare")) in DEACTIVATED:
        run("post to a deactivated leaf", "API-FIN-019", lambda: CLIENT.post(JE, balanced_je(CTX["acc_spare"])), 409,
            "FIN-409-NOT-POSTABLE-ACCOUNT", negative="RULE-FIN-007 / FIN-409-NOT-POSTABLE-ACCOUNT / TC-FIN-003, TC-FIN-015, TC-FIN-019")
    # Negative: RULE-FIN-009 — an inactive dimension value on a line
    if ("dimension_value", CTX.get("dv_spare")) in DEACTIVATED:
        run("post with a deactivated dimension value", "API-FIN-019",
            lambda: CLIENT.post(JE, balanced_je(dims=[{"dimensionId": CTX["dimension"], "dimensionValueId": CTX["dv_spare"]}])), 409,
            "FIN-409-INVALID-DIMENSION", negative="RULE-FIN-009 / FIN-409-INVALID-DIMENSION / TC-FIN-021")
    # Negative: RULE-FIN-008 — the target period is not Open at post time
    if soft_close_period1():
        run("post into a soft-closed period", "API-FIN-019", lambda: CLIENT.post(JE, balanced_je()), 409, "FIN-409-PERIOD-NOT-OPEN",
            negative="RULE-FIN-008 / FIN-409-PERIOD-NOT-OPEN / TC-FIN-020, TC-FIN-033")
        reopen_period1()
    if CTX.get("period2_id"):
        run_observation("period of the year, docDate outside it (RULE-FIN-017, no TC)", "API-FIN-019",
                        lambda: CLIENT.post(JE, je_payload([je_line(CTX["acc_debit"], "DEBIT"), je_line(CTX["acc_credit"], "CREDIT")],
                                                           CTX["period2_id"])),
                        "400 FIN-400-DOCDATE-OUTSIDE-PERIOD (documented; no TC)")
    run_observation("line on a missing account", "API-FIN-019", lambda: CLIENT.post(JE, balanced_je(PLACEHOLDER_MISSING_ID)),
                    "404 FIN-404-ACCOUNT (documented; no TC)")
    run_observation("reverse a missing entry id", "API-FIN-021", lambda: CLIENT.post(f"{JE}/{PLACEHOLDER_MISSING_ID}/reverse"),
                    "404 FIN-404-ENTRY (documented; no TC)")
    explore("API-FIN-019", "POST", JE, balanced_je,
            [("docDate", True, None, False), ("fiscalYearId", True, None, False), ("periodId", True, None, False),
             ("journalTypeCode", True, 20, False), ("lines", True, None, False)],
            "journal_entry", "journalEntryPk", mismatch=("fiscalYearId", "one"))


def test_journal_entry_from_event() -> None:
    """Covers: API-FIN-020 ; ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 built from an event (MAPPING + dimension tags)
    Negative: RULE-FIN-004 / FIN-409-DUPLICATE-EVENT / TC-FIN-011, TC-FIN-123, TC-FIN-124
    Negative: RULE-FIN-021 / FIN-422-MISSING-BUSINESS-FIELD / TC-FIN-099, TC-FIN-119
    Negative: RULE-FIN-020 / FIN-422-UNMAPPED-VALUE / TC-FIN-102, TC-FIN-103
    Negative: RULE-FIN-027 / FIN-422-UNRESOLVED-DIMENSION-VALUE / TC-FIN-120
    Negative: RULE-FIN-009 / FIN-409-INVALID-DIMENSION / TC-FIN-118
    Negative: RULE-FIN-028 / FIN-422-NO-DEFINED-PERIOD / TC-FIN-126
    Negative: RULE-FIN-008 / FIN-409-PERIOD-NOT-OPEN / TC-FIN-125, TC-FIN-127
    Negative: RULE-FIN-007 / FIN-409-NOT-POSTABLE-ACCOUNT / TC-FIN-100, TC-FIN-101"""
    RUN.begin("journal entry from event — API-FIN-020")
    why = need("rule_lines", "mapping", "doc_date", "dv_main")
    if why or len([x for x in CTX["rule_lines"] if x is not None]) != 2:
        skip("event path suite", why or "the rule does not carry both lines")
        return
    ev = event_payload()
    r = run("post an event (MAPPING line + BUSINESS_FIELD tag)", "API-FIN-020", lambda: CLIENT.post(f"{JE}/from-event", ev), 201,
            check=lambda x: expect_fields(x.data, {"eventReference": ev["eventReference"], "statusCode": "POSTED", "lineCount": 2})
            or next((f"debit line account {ln.get('accountId')} (expected the mapped leaf M)"
                     for ln in (x.data or {}).get("lines") or [] if ln.get("directionCode") == "DEBIT"
                     and not same(ln.get("accountId"), CTX["acc_mapped"])), None)
            or next((f"line {ln.get('lineNo')} carries {len(ln.get('dimensions') or [])} dimensions (expected 1)"
                     for ln in (x.data or {}).get("lines") or [] if len(ln.get("dimensions") or []) != 1), None))
    track("journal_entry", extract_id(r.data, "journalEntryPk"))
    # Negative: RULE-FIN-004 — the pre-check, then the store's refusal under concurrency
    run("post the same event reference again", "API-FIN-020", lambda: CLIENT.post(f"{JE}/from-event", ev), 409,
        "FIN-409-DUPLICATE-EVENT", negative="RULE-FIN-004 / FIN-409-DUPLICATE-EVENT / TC-FIN-011, TC-FIN-123")
    concurrent_duplicate()
    # Negatives on the event's business facts — each rejected event writes nothing (SRS A7)
    cases = [
        ("event without PAYMENT_METHOD (MAPPING line)", {"BRANCH_CODE": "CC-100"}, 422, "FIN-422-MISSING-BUSINESS-FIELD",
         "RULE-FIN-021 / FIN-422-MISSING-BUSINESS-FIELD / TC-FIN-099"),
        ("event without BRANCH_CODE (dimension tag)", {"PAYMENT_METHOD": "CHEQUE"}, 422, "FIN-422-MISSING-BUSINESS-FIELD",
         "RULE-FIN-021 / FIN-422-MISSING-BUSINESS-FIELD / TC-FIN-119"),
        ("PAYMENT_METHOD value with no active mapping (CASH)", {"PAYMENT_METHOD": "CASH", "BRANCH_CODE": "CC-100"}, 422,
         "FIN-422-UNMAPPED-VALUE", "RULE-FIN-020 / FIN-422-UNMAPPED-VALUE / TC-FIN-102, TC-FIN-103"),
        ("BRANCH_CODE naming no value of the dimension (BR-07)", {"PAYMENT_METHOD": "CHEQUE", "BRANCH_CODE": "BR-07"}, 422,
         "FIN-422-UNRESOLVED-DIMENSION-VALUE", "RULE-FIN-027 / FIN-422-UNRESOLVED-DIMENSION-VALUE / TC-FIN-120"),
    ]
    if ("dimension_value", CTX.get("dv_spare")) in DEACTIVATED:
        cases.append(("BRANCH_CODE resolving to a deactivated value (PRJ-A)", {"PAYMENT_METHOD": "CHEQUE", "BRANCH_CODE": "PRJ-A"}, 409,
                      "FIN-409-INVALID-DIMENSION", "RULE-FIN-009 / FIN-409-INVALID-DIMENSION / TC-FIN-118"))
    for label, fields, status, code, neg in cases:
        run(label, "API-FIN-020", lambda fields=fields: CLIENT.post(f"{JE}/from-event", event_payload(fields)), status, code, negative=neg)
    run("event dated where no period is defined", "API-FIN-020",
        lambda: CLIENT.post(f"{JE}/from-event", event_payload(doc_date=ARGS.uncovered_date)), 422, "FIN-422-NO-DEFINED-PERIOD",
        negative="RULE-FIN-028 / FIN-422-NO-DEFINED-PERIOD / TC-FIN-126")
    if soft_close_period1():
        run("event into a soft-closed period (the consumer's retry signal)", "API-FIN-020",
            lambda: CLIENT.post(f"{JE}/from-event", event_payload()), 409, "FIN-409-PERIOD-NOT-OPEN",
            negative="RULE-FIN-008 / FIN-409-PERIOD-NOT-OPEN / TC-FIN-125, TC-FIN-127")
        reopen_period1()
    # last: the mapped account loses its active status after the mapping was saved — caught at post
    if deactivate("account", "API-FIN-004", ACC + "/{id}/deactivate", CTX["acc_mapped"], "leaf M (setup)").status == 200:
        run("event whose mapped account is now inactive", "API-FIN-020", lambda: CLIENT.post(f"{JE}/from-event", event_payload()), 409,
            "FIN-409-NOT-POSTABLE-ACCOUNT", negative="RULE-FIN-007 / FIN-409-NOT-POSTABLE-ACCOUNT / TC-FIN-100, TC-FIN-101")
    explore("API-FIN-020", "POST", f"{JE}/from-event", event_payload,
            [("eventReference", True, 100, False), ("eventTypeCode", True, 50, False), ("docDate", True, None, False),
             ("baseAmount", True, None, False)], "journal_entry", "journalEntryPk", mismatch=("baseAmount", "one thousand"))


def concurrent_duplicate() -> None:
    """Negative: RULE-FIN-004 / FIN-409-DUPLICATE-EVENT / TC-FIN-124 — the same new event twice at once: one 201,
    one 409 FIN-409-DUPLICATE-EVENT, never a DATA_INTEGRITY_VIOLATION / 500 (ADR-FIN-030)."""
    ev = event_payload()
    out: list = [None, None]
    barrier = threading.Barrier(2)

    def fire(i: int) -> None:
        barrier.wait()
        out[i] = CLIENT.post(f"{JE}/from-event", ev)

    threads = [threading.Thread(target=fire, args=(i,)) for i in range(2)]
    for t in threads:
        t.start()
    for t in threads:
        t.join()
    for x in out:
        if x is not None and x.status == 201:
            track("journal_entry", extract_id(x.data, "journalEntryPk"))
    loser = next((x for x in out if x is not None and x.status != 201), None) \
        or Resp("POST", f"{JE}/from-event", None, None, "", error="no second response (thread failed)", at=_now())
    statuses = sorted(x.status or 0 for x in out if x is not None)
    run("same new event twice, concurrently", "API-FIN-020", lambda: loser, 409, "FIN-409-DUPLICATE-EVENT",
        negative="RULE-FIN-004 / FIN-409-DUPLICATE-EVENT / TC-FIN-124",
        check=lambda _x: None if statuses == [201, 409] else f"statuses {statuses} (expected one 201 and one 409)")


def test_allocation_rule_run() -> None:
    """Covers: API-FIN-017, API-FIN-037 — after the entries above gave the source account a balance"""
    RUN.begin("allocation rule — run / deactivate")
    why = need("allocation")
    if why:
        skip("allocation run", why)
        return
    r = run("run the allocation rule (posts an entry)", "API-FIN-017", lambda: CLIENT.post(f"{AR}/{CTX['allocation']}/run"), 201,
            check=lambda x: expect_fields(x.data, {"statusCode": "POSTED"}))
    track("journal_entry", extract_id(r.data, "journalEntryPk"))
    deactivate("allocation_rule", "API-FIN-037", AR + "/{id}/deactivate", CTX["allocation"], "the allocation rule")
    run_observation("run a deactivated allocation rule", "API-FIN-017", lambda: CLIENT.post(f"{AR}/{CTX['allocation']}/run"),
                    "409 FIN-409-NOT-ACTIVE (recorded human decision; no TC)")
    run_observation("run a missing allocation rule", "API-FIN-017", lambda: CLIENT.post(f"{AR}/{PLACEHOLDER_MISSING_ID}/run"),
                    "404 FIN-404-ALLOCATION-RULE (documented; no TC)")


REP = "/api/v1/fin/reports"


def test_reports() -> None:
    """Covers: API-FIN-028, API-FIN-029, API-FIN-030, API-FIN-031, API-FIN-032 ; reads of ENT-FIN-005 / ENT-FIN-006"""
    RUN.begin("financial reports (read-only)")
    if CTX.get("acc_debit") is not None:
        run("account ledger of leaf D", "API-FIN-028", lambda: CLIENT.get(f"{REP}/account-ledger", {"accountId": CTX["acc_debit"]}), 200,
            check=lambda x: expect_fields(x.data, {"accountId": CTX["acc_debit"]}))
        run_observation("account ledger of a missing account", "API-FIN-028",
                        lambda: CLIENT.get(f"{REP}/account-ledger", {"accountId": PLACEHOLDER_MISSING_ID}), "404 FIN-404-ACCOUNT (documented; no TC)")
    if CTX.get("period1_id") is not None:
        r = run("trial balance of period 1", "API-FIN-029", lambda: CLIENT.get(f"{REP}/trial-balance", {"periodId": CTX["period1_id"]}), 200,
                check=lambda x: None if isinstance(x.data, dict) and "rows" in x.data else "rows missing")
        run_observation("trial balance `balanced` (POL-FIN-008, an invariant by construction)", "API-FIN-029", lambda: r,
                        f"balanced=true (observed {(r.data or {}).get('balanced') if isinstance(r.data, dict) else None})")
    if CTX.get("year_id") is not None:
        run("balance sheet of the run's year", "API-FIN-030", lambda: CLIENT.get(f"{REP}/balance-sheet", {"fiscalYearId": CTX["year_id"]}), 200,
            check=lambda x: expect_fields(x.data, {"fiscalYearId": CTX["year_id"]}))
        run("income statement of the run's year", "API-FIN-031",
            lambda: CLIENT.get(f"{REP}/income-statement", {"fiscalYearId": CTX["year_id"]}), 200,
            check=lambda x: expect_fields(x.data, {"fiscalYearId": CTX["year_id"]}))
        run_observation("balance sheet of a missing year", "API-FIN-030",
                        lambda: CLIENT.get(f"{REP}/balance-sheet", {"fiscalYearId": PLACEHOLDER_MISSING_ID}), "404 FIN-404-YEAR (documented; no TC)")
    if CTX.get("dimension") is not None:
        run("dimension report of the run's dimension", "API-FIN-032",
            lambda: CLIENT.get(f"{REP}/dimension", {"dimensionId": CTX["dimension"]}), 200,
            check=lambda x: expect_fields(x.data, {"dimensionId": CTX["dimension"]}))
    run_observation("trial balance without filters", "API-FIN-029", lambda: CLIENT.get(f"{REP}/trial-balance"), "200 (both params optional)")


def test_account_mapping_deactivate() -> None:
    """Covers: API-FIN-041 ; Negative: RULE-FIN-020 / FIN-422-UNMAPPED-VALUE / TC-FIN-107 (the key's mapping is deactivated)"""
    RUN.begin("account mapping — deactivate")
    why = need("mapping")
    if why:
        skip("mapping deactivate", why)
        return
    if deactivate("account_mapping", "API-FIN-041", MAP + "/{id}/deactivate", CTX["mapping"], "the CHEQUE mapping").status == 200 \
            and CTX.get("rule_lines"):
        run("event whose only mapping is deactivated", "API-FIN-020", lambda: CLIENT.post(f"{JE}/from-event", event_payload()), 422,
            "FIN-422-UNMAPPED-VALUE", negative="RULE-FIN-020 / FIN-422-UNMAPPED-VALUE / TC-FIN-107")
    run_observation("deactivate a missing mapping id", "API-FIN-041", lambda: CLIENT.put(f"{MAP}/{PLACEHOLDER_MISSING_ID}/deactivate"),
                    "404 FIN-404-ACCOUNT-MAPPING (documented; no TC)")


def test_event_type_rule_deactivate() -> None:
    """Covers: API-FIN-034 ; Negative: RULE-FIN-005 / FIN-404-NO-ACTIVE-RULE / TC-FIN-013"""
    RUN.begin("event-type rule — deactivate")
    why = need("rule")
    if why:
        skip("rule deactivate", why)
        return
    if deactivate("event_type_rule", "API-FIN-034", RULES + "/{id}/deactivate", CTX["rule"], "the event-type rule").status == 200 \
            and CTX.get("doc_date"):
        run("event whose type has no active rule", "API-FIN-020", lambda: CLIENT.post(f"{JE}/from-event", event_payload()), 404,
            "FIN-404-NO-ACTIVE-RULE", negative="RULE-FIN-005 / FIN-404-NO-ACTIVE-RULE / TC-FIN-013")
    run_observation("deactivate a missing rule id", "API-FIN-034", lambda: CLIENT.put(f"{RULES}/{PLACEHOLDER_MISSING_ID}/deactivate"),
                    "404 FIN-404-RULE (documented; no TC)")


def test_close_approval_permission(limited: Client | None) -> None:
    """Covers: API-FIN-026, API-FIN-027 ; Negative: RULE-FIN-015 / FIN-403-FORBIDDEN / TC-FIN-038, TC-FIN-052"""
    RUN.begin("close approval — permission gate (RULE-FIN-015)")
    if limited is None:
        skip("forbidden close approval", "no --limited-* credentials (a caller WITHOUT PERM_FIN_PERIODS_CLOSE_APPROVE) were supplied")
        return
    why = need("period2_id", "year_id")
    if why:
        skip("forbidden close approval", why)
        return
    run("hard-close without PERM_FIN_PERIODS_CLOSE_APPROVE", "API-FIN-026",
        lambda: limited.patch(f"{FP}/{CTX['period2_id']}/hard-close"), 403, "FIN-403-FORBIDDEN",
        negative="RULE-FIN-015 / FIN-403-FORBIDDEN / TC-FIN-038, TC-FIN-052")
    run("year-end close without PERM_FIN_PERIODS_CLOSE_APPROVE", "API-FIN-027",
        lambda: limited.post(f"{FY}/{CTX['year_id']}/year-end-close"), 403, "FIN-403-FORBIDDEN",
        negative="RULE-FIN-015 / FIN-403-FORBIDDEN / TC-FIN-038, TC-FIN-052")


def test_year_end_close() -> None:
    """Covers: API-FIN-023 (successor year), API-FIN-025, API-FIN-026, API-FIN-027
    Carve-out: RULE-FIN-008 is NOT raised for the CLOSING/OPENING entries / TC-FIN-036 (opt-in: --with-year-end)"""
    RUN.begin("year-end close (opt-in)")
    if not ARGS.with_year_end:
        skip("year-end close", "terminal side effects (every period of the run's year hard-closed) — pass --with-year-end")
        return
    why = need("year_id", "periods")
    if why:
        skip("year-end close", why)
        return
    ys, ye = date.fromisoformat(ARGS.year_start), date.fromisoformat(ARGS.year_end)
    s_start = ye + timedelta(days=1)                 # the successor is DERIVED by adjacency (manifest step 5)
    s_end = s_start + (ye - ys)
    body = {"code": (str(s_start.year) + ARGS.run_tag)[:10], "startDate": s_start.isoformat(), "endDate": s_end.isoformat(),
            "periodCount": ARGS.period_count}
    r = run("create the successor year", "API-FIN-023", lambda: CLIENT.post(FY, body), 201)
    track("fiscal_year", extract_id(r.data, "fiscalYearPk"))
    for pk in CTX["periods"]:
        if pk == CTX.get("period_last_id"):
            continue                                  # already HARD_CLOSE (test_fiscal_period)
        run(f"soft-close period {pk}", "API-FIN-025", lambda pk=pk: CLIENT.patch(f"{FP}/{pk}/soft-close"), 200)
        run(f"hard-close period {pk}", "API-FIN-026", lambda pk=pk: CLIENT.patch(f"{FP}/{pk}/hard-close"), 200)
    r = run("run year-end close", "API-FIN-027", lambda: CLIENT.post(f"{FY}/{CTX['year_id']}/year-end-close"), 201,
            check=lambda x: None if isinstance(x.data, dict) and x.data.get("closingEntry") and x.data.get("openingEntry")
            else "closingEntry / openingEntry missing")
    for k in ("closingEntry", "openingEntry"):
        track("journal_entry", extract_id((r.data or {}).get(k) if isinstance(r.data, dict) else None, "journalEntryPk"))


# ══════════════════════════════════════════════════════════════════════════════════════════
# cleanup — stage F: reverse dependency order, soft semantics, tracked ids only
# ══════════════════════════════════════════════════════════════════════════════════════════

TEARDOWN = [   # reverse of stage-B order; (entity, deactivate path or None, api, reason when none)
    ("journal_entry", None, "", "posted entries are immutable (RULE-FIN-016) and FIN has no delete — they remain POSTED"),
    ("allocation_rule", AR + "/{id}/deactivate", "API-FIN-037", ""),
    ("recurring_template", RT + "/{id}/deactivate", "API-FIN-036", ""),
    ("account_mapping", MAP + "/{id}/deactivate", "API-FIN-041", ""),
    ("rule_line", None, "", "no deactivate and no active flag — a line and its tags retire with their rule (API-FIN-034)"),
    ("event_type_rule", RULES + "/{id}/deactivate", "API-FIN-034", ""),
    ("fiscal_year", None, "", "no deactivate endpoint — the year and its periods remain (periods left OPEN except the hard-closed last one)"),
    ("dimension_value", DIM + "/values/{id}/deactivate", "API-FIN-035", ""),
    ("dimension", None, "", "no deactivate endpoint (manifest CRUD checklist) — dimensions remain active"),
    ("account", ACC + "/{id}/deactivate", "API-FIN-004", ""),
]


def cleanup() -> None:
    print("\n━━ cleanup (deactivate, reverse dependency order)")
    for entity, tpl, api, reason in TEARDOWN:
        ids = [x for x in CREATED.get(entity, []) if x is not None]
        if not ids:
            continue
        if tpl is None:
            RUN.remaining.append(f"{entity}: {len(ids)} row(s) {ids} — {reason}")
            continue
        done, failed = [], []
        for pk in reversed(ids):
            if (entity, pk) in DEACTIVATED:
                done.append(pk)
                continue
            r = CLIENT.put(tpl.format(id=pk))
            (done if r.status == 200 else failed).append(pk if r.status == 200 else f"{pk} ({r.status} {r.code})")
        RUN.teardown.append(f"{entity} via {api}: deactivated {len(done)} {done}" + (f"; FAILED {failed}" if failed else ""))
        RUN.remaining.append(f"{entity}: {len(done)} row(s) remain, deactivated (soft semantics — no hard delete is documented)")
    if ARGS.db_opt_in:
        print_suggested_sql()


TABLES = {"account": "FIN_ACCOUNT", "dimension": "FIN_DIMENSION", "dimension_value": "FIN_DIMENSION_VALUE",
          "fiscal_year": "FIN_FISCAL_YEAR", "event_type_rule": "FIN_EVENT_TYPE_RULE", "account_mapping": "FIN_ACCOUNT_MAPPING",
          "journal_entry": "FIN_JOURNAL_ENTRY"}   # api-docs index.md → Uniqueness Invariants (table names only)


def print_suggested_sql() -> None:
    """Stage H — opt-in, PRINT ONLY. The api-docs state table names but not primary-key columns, so the SQL is a
    suggestion for a human (`<PK_COLUMN>` to fill in), scoped to ids this run created; it is never executed here."""
    print("\n-- SUGGESTED SQL (not executed) — rows created by run", ARGS.run_tag, "— review, then run in ONE transaction")
    print("BEGIN;")
    for entity, tpl, _api, _r in TEARDOWN:
        ids = [x for x in CREATED.get(entity, []) if x is not None]
        if ids and entity in TABLES:
            print(f"DELETE FROM {TABLES[entity]} WHERE <PK_COLUMN> IN ({', '.join(str(i) for i in ids)});")
    print("ROLLBACK;  -- replace with COMMIT only after review")


# ══════════════════════════════════════════════════════════════════════════════════════════
# report — api-verify-report-fin.md (Arabic primary, English beside it)
# ══════════════════════════════════════════════════════════════════════════════════════════

COVERAGE = [   # every served endpoint → the function that exercises it
    ("API-FIN-001", "POST /accounts/search", "test_account"), ("API-FIN-002", "POST /accounts", "test_account"),
    ("API-FIN-003", "PUT /accounts/{id}", "test_account"), ("API-FIN-004", "PUT /accounts/{id}/deactivate", "test_account"),
    ("API-FIN-005", "POST /dimensions/search", "test_dimension"), ("API-FIN-006", "POST /dimensions", "test_dimension"),
    ("API-FIN-007", "POST /dimensions/{id}/values", "test_dimension_value"),
    ("API-FIN-008", "POST /dimensions/values/search", "test_dimension_value"),
    ("API-FIN-009", "POST /event-rules/search", "test_event_type_rule"), ("API-FIN-010", "POST /event-rules", "test_event_type_rule"),
    ("API-FIN-011", "POST /event-rules/{id}/lines", "test_rule_line · test_rule_line_negatives"),
    ("API-FIN-012", "POST /recurring-templates/search", "test_recurring_template"),
    ("API-FIN-013", "POST /recurring-templates", "test_recurring_template"),
    ("API-FIN-014", "POST /recurring-templates/{id}/run", "test_recurring_template"),
    ("API-FIN-015", "POST /allocation-rules/search", "test_allocation_rule"),
    ("API-FIN-016", "POST /allocation-rules", "test_allocation_rule"),
    ("API-FIN-017", "POST /allocation-rules/{id}/run", "test_allocation_rule_run"),
    ("API-FIN-018", "POST /journal-entries/search", "test_journal_entry"),
    ("API-FIN-019", "POST /journal-entries", "test_journal_entry"),
    ("API-FIN-020", "POST /journal-entries/from-event", "test_journal_entry_from_event (+ the two deactivate suites)"),
    ("API-FIN-021", "POST /journal-entries/{id}/reverse", "test_journal_entry"),
    ("API-FIN-022", "GET /journal-entries/{id}", "test_journal_entry"),
    ("API-FIN-023", "POST /fiscal-years", "test_fiscal_year"),
    ("— (no contract id)", "POST /fiscal-years/search", "test_fiscal_year"),
    ("API-FIN-024", "PATCH /fiscal-periods/{id}/open", "test_fiscal_period"),
    ("API-FIN-025", "PATCH /fiscal-periods/{id}/soft-close", "test_fiscal_period"),
    ("API-FIN-026", "PATCH /fiscal-periods/{id}/hard-close", "test_fiscal_period · test_close_approval_permission"),
    ("API-FIN-027", "POST /fiscal-years/{id}/year-end-close", "test_close_approval_permission · test_year_end_close (opt-in)"),
    ("API-FIN-028", "GET /reports/account-ledger", "test_reports"), ("API-FIN-029", "GET /reports/trial-balance", "test_reports"),
    ("API-FIN-030", "GET /reports/balance-sheet", "test_reports"), ("API-FIN-031", "GET /reports/income-statement", "test_reports"),
    ("API-FIN-032", "GET /reports/dimension", "test_reports"),
    ("API-FIN-033", "POST /fiscal-periods/search", "test_fiscal_period"),
    ("API-FIN-034", "PUT /event-rules/{id}/deactivate", "test_event_type_rule_deactivate"),
    ("API-FIN-035", "PUT /dimensions/values/{id}/deactivate", "test_dimension_value"),
    ("API-FIN-036", "PUT /recurring-templates/{id}/deactivate", "test_recurring_template"),
    ("API-FIN-037", "PUT /allocation-rules/{id}/deactivate", "test_allocation_rule_run"),
    ("API-FIN-038", "POST /account-mappings/search", "test_account_mapping"),
    ("API-FIN-039", "POST /account-mappings", "test_account_mapping"),
    ("API-FIN-040", "PUT /account-mappings/{id}", "test_account_mapping"),
    ("API-FIN-041", "PUT /account-mappings/{id}/deactivate", "test_account_mapping_deactivate"),
]


def _cell(s: Any) -> str:
    return str(s if s is not None else "—").replace("|", "\\|").replace("\n", " ")


def write_report(path: Path, plan_only: bool) -> None:
    L: list[str] = []
    a = L.append
    asserted, obs, skipped = RUN.all("assert"), RUN.all("observation"), RUN.all("skipped")
    failed = [r for r in asserted if not r.passed]
    a(f"# تقرير التحقق من واجهات البرمجة — {MODULE} {VERSION} · API verification report — {MODULE} {VERSION}")
    a("")
    a("> مُولَّد بواسطة `api-verify-fin.py` — لا يُحرَّر يدويًا. · Generated by `api-verify-fin.py` — do not edit by hand;")
    a("> rerun the script to refresh it. Stage `api-verify` (standalone, post-implementation) — it gates nothing and changes no line artifact.")
    a("")
    a("## 1. ملخص التشغيل · Run summary")
    a("")
    a("| البند · Item | القيمة · Value |")
    a("|---|---|")
    a(f"| المستوى · Tier | **{TIER}** — api-docs + test-execution-manifest (negatives from the manifest's RULE → code → TC rows; zero self-derivation) |")
    a(f"| المصادر · Inputs | api-docs (11 files, 42 served endpoints, 41 with a contract id) · test-execution-manifest v2 (CS-FIN-001) |")
    if plan_only:
        a("| الحالة · Status | **لم يُنفَّذ بعد · generated, not executed** — no base URL or credentials were supplied to the "
          "stage run, so no call was made; every result section below fills in when the script runs |")
        a(f"| الهدف الافتراضي · Default target | `{DEFAULT_BASE_URL}` (api-docs → Servers); Dev/Test only |")
    else:
        a(f"| الحالة · Status | {'aborted: ' + RUN.aborted if RUN.aborted else 'executed'} |")
        a(f"| الهدف · Target | `{ARGS.base_url}` |")
        a(f"| البدء · Started (UTC) | {RUN.started} |")
        a(f"| وسم التشغيل · Run tag | `{ARGS.run_tag}` (appended to unique-keyed example codes) |")
        a(f"| نوع الحدث · Event type | `{ARGS.event_type}` |")
        a(f"| السنة · Fiscal year | `{CTX.get('year_code', '—')}` {ARGS.year_start} → {ARGS.year_end} |")
        a(f"| مُؤكَّدة · Asserted | {len(asserted)} — ناجحة · passed {len(asserted) - len(failed)} · فاشلة · failed {len(failed)} |")
        a(f"| ملاحظات · Observations | {len(obs)} (never pass/fail, never in the exit code) |")
        a(f"| متروكة · Skipped | {len(skipped)} |")
    a("")

    a("## 2. المشكلات · Problems (asserted failures only)")
    a("")
    if plan_only:
        a("_لا نتائج قبل التشغيل · No results before a run._ The script buckets every asserted failure into: "
          "**likely real bug** (a documented rejection did not happen, or an unexplained status — ambiguous cases land here), "
          "**test assumption mismatch** (rejected, but with a status/code the manifest did not state; fix the expectation, "
          "not the backend), **infrastructure** (connection / timeout — rerun).")
    else:
        for bucket, ar in ((BUCKET_BUG, "خلل فعلي مرجَّح"), (BUCKET_ASSUMPTION, "عدم تطابق افتراض الاختبار"), (BUCKET_INFRA, "البنية التحتية")):
            rows = [r for r in failed if r.bucket == bucket]
            a(f"### {ar} · {bucket} ({len(rows)})")
            a("")
            if not rows:
                a("_لا شيء · none_")
                a("")
                continue
            a("| Suite | Check | API | Traceability | Expected | Actual | error.message | Detail |")
            a("|---|---|---|---|---|---|---|---|")
            for r in rows:
                a(f"| {_cell(r.suite)} | {_cell(r.name)} | {_cell(r.api)} | {_cell(r.negative or r.covers)} | {_cell(r.expected)} | "
                  f"{_cell(r.status)} {_cell(r.code)} | {_cell(r.message)} | {_cell(r.detail)} |")
            a("")
            if ARGS.log_file:
                for r in rows:
                    ex = log_excerpt(r)
                    if ex:
                        a(f"<details><summary>{_cell(r.name)} — log excerpt (<b>approximate</b>: no trace id is documented, "
                          f"matched by path)</summary>\n\n```\n{ex}\n```\n</details>\n")
    a("")

    a("## 3. النتائج حسب المجموعة · Results by suite (stage-B order)")
    a("")
    if plan_only:
        a("| # | الكيان · Entity | Created by | Suite |")
        a("|---|---|---|---|")
        for step, ent, api, fn in DEPENDENCY_ORDER:
            a(f"| {step} | {ent} | {api} | {fn} |")
        a("")
        a("Then, after every creator above: `test_rule_line_negatives` · `test_allocation_rule_run` · `test_reports` · "
          "`test_account_mapping_deactivate` · `test_event_type_rule_deactivate` · `test_close_approval_permission` · "
          "`test_year_end_close` (opt-in) — the deferred steps that would break a dependent if they ran earlier. "
          "FK ids are threaded from each parent's response, never hard-coded.")
    else:
        a("| Suite | Passed | Failed | Observations | Skipped |")
        a("|---|---|---|---|---|")
        for s in RUN.suites:
            rs = s.results
            a(f"| {_cell(s.name)} | {sum(1 for r in rs if r.kind == 'assert' and r.passed)} | "
              f"{sum(1 for r in rs if r.kind == 'assert' and not r.passed)} | {sum(1 for r in rs if r.kind == 'observation')} | "
              f"{sum(1 for r in rs if r.kind == 'skipped')} |")
        a("")
        a("<details><summary>كل الاستدعاءات المؤكَّدة · every asserted call</summary>\n")
        a("| Suite | Check | API | Traceability | Expected | Actual | Result |")
        a("|---|---|---|---|---|---|---|")
        for r in asserted:
            a(f"| {_cell(r.suite)} | {_cell(r.name)} | {_cell(r.api)} | {_cell(r.negative or r.covers)} | {_cell(r.expected)} | "
              f"{_cell(r.status)} {_cell(r.code)} | {'PASS' if r.passed else 'FAIL'} {_cell(r.detail) if r.detail else ''} |")
        a("\n</details>")
    a("")

    a("## 4. الحالات السلبية المؤكَّدة · Negative tests (stage C — manifest triples)")
    a("")
    a("HTTP status + runtime code (`FIN-{http}[-{SLUG}]`, never the governance id) + `error.message` presence.")
    a("")
    a("| RULE | Runtime code | TC | HTTP | Endpoint | Function |")
    a("|---|---|---|---|---|---|")
    for rule, code, tc, http, api, fn in NEGATIVES:
        a(f"| {rule} | `{code}` | {tc} | {http} | {api} | `{fn}` |")
    a("")
    a("### حالات لم تُؤكَّد · Manifest rows not asserted, and why")
    a("")
    a("| RULE | Code / case | Reason |")
    a("|---|---|---|")
    for rule, code, why in SKIPPED_NEGATIVES:
        a(f"| {rule} | {code} | {why} |")
    a("")

    a("## 5. الملاحظات الاستكشافية · Observations (stage E — recorded, never pass/fail)")
    a("")
    if plan_only:
        a("Bounded to required or length-constrained fields and to documented outcomes that no RULE + TC governs: "
          "omission of each required field, at-limit / one-over on every `maxLength` (at-limit skipped where a success would "
          "consume an event type or overlap the run's fiscal year), one type mismatch per create, undocumented lookup values, "
          "invalid FK / missing ids (documented `FIN-404-*`), deactivate-twice idempotency, duplicate codes without a TC "
          "(`FIN-409-ACCOUNT-DUP`, `-DIMENSION-DUP`, `-RULE-DUP`, `-YEAR-DUP`), `FIN-400-MISSING-FREQUENCY`, "
          "`FIN-409-NOT-ACTIVE`, `FIN-409-INVALID-TRANSITION`, `FIN-422-DERIVATION-SPEC`, `FIN-422-REMAINDER-MARKER`, "
          "`FIN-409-PARENT-NOT-LEAF-ELIGIBLE`, `FIN-400-DOCDATE-OUTSIDE-PERIOD`, a PUT on a posted entry (RULE-FIN-016, "
          "enforced by omission — TC-FIN-016, TC-FIN-017) and the trial balance's `balanced` flag (POL-FIN-008).")
    else:
        a("| Suite | Probe | API | Expected (undocumented or no TC) | Observed | error.message | Note |")
        a("|---|---|---|---|---|---|---|")
        for r in obs:
            a(f"| {_cell(r.suite)} | {_cell(r.name)} | {_cell(r.api)} | {_cell(r.expected)} | {_cell(r.status)} {_cell(r.code)} | "
              f"{_cell(r.message)} | {_cell(r.detail)} |")
        if skipped:
            a("")
            a("### متروكة · Skipped steps")
            a("")
            a("| Suite | Step | Reason |")
            a("|---|---|---|")
            for r in skipped:
                a(f"| {_cell(r.suite)} | {_cell(r.name)} | {_cell(r.detail)} |")
    a("")

    a("## 6. التنظيف · Teardown (stage F)")
    a("")
    if plan_only:
        a("`cleanup()` runs in `finally`, in reverse dependency order, and touches only ids this run created. No hard-delete "
          "endpoint is documented, so rows are **deactivated, not removed**: allocation rules (API-FIN-037), recurring templates "
          "(API-FIN-036), account mappings (API-FIN-041), the event-type rule (API-FIN-034), dimension values (API-FIN-035), "
          "accounts (API-FIN-004). **Records remain** where no deactivate exists: dimensions, the fiscal year and its periods, "
          "and every journal entry (posted, immutable — RULE-FIN-016). `--db-opt-in` only PRINTS suggested SQL for a human; "
          "it never executes (the api-docs name the tables but not their primary-key columns).")
    else:
        for line in RUN.teardown or ["nothing was created"]:
            a(f"- {line}")
        a("")
        a("**السجلات المتبقية · Records that remain:**")
        a("")
        for line in RUN.remaining or ["none"]:
            a(f"- {line}")
    a("")

    a("## 7. تغطية نقاط النهاية · Endpoint coverage (42 served)")
    a("")
    a("| API | Endpoint (`/api/v1/fin` …) | Function |")
    a("|---|---|---|")
    for api, ep, fn in COVERAGE:
        a(f"| {api} | `{ep}` | `{fn}` |")
    a("")
    a("CRUD cells the manifest marks `—` (no get-by-id outside journal entries, no activate anywhere, no update on "
      "dimensions, dimension values, rules, templates or allocation rules, no delete anywhere) are recorded as skipped "
      "steps with the checklist's own reason; the `read` step uses a search filtered on the entity's id.")
    a("")

    a("## 8. فجوات التوثيق · Documentation gaps flagged during translation")
    a("")
    for i, g in enumerate(DOC_GAPS, 1):
        a(f"{i}. {g}")
    a("")

    a("## 9. التشغيل · How to run")
    a("")
    a("```")
    a("python api-verify-fin.py --username <user> --password <pass>             # Dev/Test, localhost:7272")
    a("  [--base-url http://localhost:<port>] [--allow-nonlocal <env name>]     # non-local targets need the flag")
    a("  [--token <jwt>] [--login-body '<json>']                                # SEC login body is undocumented here")
    a("  [--event-type <ACCOUNTING_EVENT_TYPE with no rule yet>]                # default SALES_INVOICE (example)")
    a("  [--year-start 2026-01-01 --year-end 2026-12-31 --period-count 12]     # api-docs example year")
    a("  [--uncovered-date <a date no period covers>]                           # TC-FIN-126")
    a("  [--limited-username <u> --limited-password <p> | --limited-token <jwt>] # TC-FIN-038, TC-FIN-052")
    a("  [--with-year-end] [--db-opt-in] [--log-file <backend log>] [--report <path>]")
    a("```")
    a("")
    a("Preconditions the script cannot create (manifest steps 0–1): FIN's MDL lookup keys including the v2 BLOCK 8 values "
      "(CHEQUE in PAYMENT_METHOD, DIRECT retired, FIN_EVENT_BUSINESS_FIELD, FIN_DIMENSION_VALUE_SOURCE) and host values "
      "(the chosen ACCOUNTING_EVENT_TYPE, CASH); SEC registration of FIN's screens/actions with the calling role granted "
      "every FIN permission, the three FIN_ACCOUNT_MAPPINGS actions included; the seeded retained-earnings account for "
      "`--with-year-end`. `preflight()` stops the run on 401/403 before any row is written.")
    a("")
    path.write_text("\n".join(L) + "\n", encoding="utf-8")
    print(f"\nreport → {path}")


def log_excerpt(r: TestResult, window: int = 2000, around: int = 6) -> str:
    """Stage H (opt-in) — APPROXIMATE: lines of the backend log's tail that mention the failing path."""
    try:
        lines = Path(ARGS.log_file).read_text(encoding="utf-8", errors="replace").splitlines()[-window:]
    except OSError:
        return ""
    key = r.path.split("?")[0]
    hits = [i for i, ln in enumerate(lines) if key and key in ln]
    if not hits:
        return ""
    i = hits[-1]
    return "\n".join(lines[max(0, i - around): i + around + 1])


# ══════════════════════════════════════════════════════════════════════════════════════════
# main
# ══════════════════════════════════════════════════════════════════════════════════════════

def login(username: str, password: str, token: str | None) -> str | None:
    if token:
        return token
    if ARGS.login_body:
        body = json.loads(ARGS.login_body)
    else:
        body = {LOGIN_BODY_KEYS[0]: username, LOGIN_BODY_KEYS[1]: password}
    r = Client(ARGS.base_url, lang=ARGS.lang, timeout=ARGS.timeout).post(AUTH_ENDPOINT, body)
    run("login (bearerAuth)", "—", lambda: r, 200, covers=f"POST {AUTH_ENDPOINT} (SEC)",
        check=lambda x: None if extract_token(x) else f"no token under {TOKEN_KEYS}")
    return extract_token(r)


def preflight() -> str | None:
    """Steps 0–1 of the dependency order are MDL / SEC data FIN does not create: a 401/403 here means the caller is
    not registered or not granted — stop before any row is written (a 403 later would read like a FIN defect)."""
    r = run("preflight — search accounts (gateway VIEW)", "API-FIN-001",
            lambda: CLIENT.post(f"{ACC}/search", {"page": 0, "size": 1}), 200, covers="API-FIN-001 (preflight)", check=check_page)
    if r.status is None:
        return f"preflight: target unreachable ({r.transport_error}) — no row was written"
    if r.status in (401, 403):
        return f"preflight refused {r.status} {r.code} (steps 0–1: SEC registration / grants) — no row was written"
    return None if r.status == 200 else f"preflight answered {r.status} {r.code} — no row was written"


def parse_args(argv: list[str]) -> argparse.Namespace:
    ap = argparse.ArgumentParser(description="FIN v2 API verification (Dev/Test only).")
    ap.add_argument("--base-url", default=DEFAULT_BASE_URL)
    ap.add_argument("--allow-nonlocal", metavar="ENV", help="name the Dev/Test environment of a non-local target")
    ap.add_argument("--username", default=PLACEHOLDER_USERNAME)
    ap.add_argument("--password", default=PLACEHOLDER_PASSWORD)
    ap.add_argument("--token")
    ap.add_argument("--login-body", help="raw JSON body for the SEC login (its shape is not in FIN's api-docs)")
    ap.add_argument("--limited-username")
    ap.add_argument("--limited-password")
    ap.add_argument("--limited-token")
    ap.add_argument("--event-type", default=EX_EVENT_TYPE)
    ap.add_argument("--year-start", default=EX_YEAR_START)
    ap.add_argument("--year-end", default=EX_YEAR_END)
    ap.add_argument("--period-count", type=int, default=EX_PERIOD_COUNT)
    ap.add_argument("--uncovered-date", default=PLACEHOLDER_UNCOVERED_DATE)
    ap.add_argument("--run-tag", default=_run_tag())
    ap.add_argument("--with-year-end", action="store_true")
    ap.add_argument("--db-opt-in", action="store_true", help="print suggested teardown SQL (never executed)")
    ap.add_argument("--log-file")
    ap.add_argument("--lang", default="ar", help="Accept-Language (primary ar)")
    ap.add_argument("--timeout", type=float, default=30.0)
    ap.add_argument("--report", default=str(Path(__file__).with_name("api-verify-report-fin.md")))
    ap.add_argument("--plan-only", action="store_true", help="write the report skeleton without calling the API")
    return ap.parse_args(argv)


def main(argv: list[str]) -> int:
    global ARGS, CLIENT
    ARGS = parse_args(argv)
    report = Path(ARGS.report)
    if ARGS.plan_only:
        write_report(report, plan_only=True)
        return 0
    host = urllib.parse.urlparse(ARGS.base_url).hostname or ""
    if host not in LOCAL_HOSTS and not ARGS.allow_nonlocal:
        print(f"refused: {host} is not local — pass --allow-nonlocal <Dev/Test environment name>", file=sys.stderr)
        return 2
    if not ARGS.token and PLACEHOLDER_USERNAME in (ARGS.username,) and not ARGS.login_body:
        print("refused: no credentials — pass --username/--password, --login-body or --token (none are invented)", file=sys.stderr)
        return 2

    RUN.begin("preconditions")
    CLIENT = Client(ARGS.base_url, lang=ARGS.lang, timeout=ARGS.timeout)
    token = login(ARGS.username, ARGS.password, ARGS.token)
    limited = None
    if ARGS.limited_token or (ARGS.limited_username and ARGS.limited_password):
        lt = ARGS.limited_token or login(ARGS.limited_username, ARGS.limited_password, None)
        limited = Client(ARGS.base_url, lt, ARGS.lang, ARGS.timeout) if lt else None
    if not token:
        RUN.aborted = "login failed — no bearer token"
        write_report(report, plan_only=False)
        return 2
    CLIENT.token = token
    refused = preflight()
    if refused:
        RUN.aborted = refused
        write_report(report, plan_only=False)
        return 2

    try:
        test_account()
        test_dimension()
        test_dimension_value()
        test_fiscal_year()
        test_fiscal_period()
        test_event_type_rule()
        test_rule_line()
        test_account_mapping()
        test_recurring_template()
        test_allocation_rule()
        test_journal_entry()
        test_journal_entry_from_event()
        test_rule_line_negatives()
        test_allocation_rule_run()
        test_reports()
        test_account_mapping_deactivate()
        test_event_type_rule_deactivate()
        test_close_approval_permission(limited)
        test_year_end_close()
    except KeyboardInterrupt:
        RUN.aborted = "interrupted"
    finally:
        cleanup()
        write_report(report, plan_only=False)
    return 1 if any(not r.passed for r in RUN.all("assert")) else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
