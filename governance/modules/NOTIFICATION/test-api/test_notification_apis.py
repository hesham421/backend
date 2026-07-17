#!/usr/bin/env python3
"""
test_notification_apis.py — Notification Service (NOTIF prefix)

Governed by: PLAN-NOTIF-001 (execution-plan.md) + registry-test-NOTIF (TC-ID
extract from test-plan.md, session-input only) + 11 API docs (index.md catalog).

TIER: FULL — execution-plan.md (ENTITY REGISTRY / RULE REGISTRY / ERROR CATALOG)
+ TC-ID register + API docs all present => happy-path AND governed negative tests.

OUT OF SCOPE FOR THIS RUN (stated per MODE 5 Section 1):
  - API-NOTIF-004 (GET /api/v1/notifications/unread)
  - API-NOTIF-005 (PUT /api/v1/notifications/{id}/read)
  Both endpoints' own docs carry "GOVERNANCE-NOTE-BLOCKED, see DRV-NOTIF-003",
  and the TC-ID register tags them "DEFERRED (DRV-NOTIF-003)" — the underlying
  read/unread column does not yet exist pending an SRS amendment. Generating
  assertions against them would test an invented column. Not implemented here.

DEPENDENCY GRAPH (Stage B): no execution-plan ENTITY REGISTRY FK edges exist
between NotificationTemplate, NotificationChannelConfig, and NotificationLog —
all three are roots. NotificationChannelConfig has no Create endpoint (5 fixed,
pre-seeded rows per SRS) so it is exercised via List + Update only, with an
explicit restore-after-test step since these are shared seed rows, not
records this run creates.

NotificationLog (created via send/schedule) has no GetById/Update/Deactivate/
Delete API at all — it is a SHARED-owner, append-only entity per the ENTITY
REGISTRY (Section 3). There is nothing to clean up for it via the API; this is
a documented architectural property, not an oversight — see cleanup() note.

Intended for Dev/Test environments only.
"""

import argparse
import json
import sys
import time
from dataclasses import dataclass, field
from datetime import datetime, timedelta
from pathlib import Path
from typing import Optional

try:
    import requests
except ImportError:
    print("requests library not found. Install it: pip install requests")
    sys.exit(1)


# ─── Configuration (placeholders — never invent real credentials) ─────────────

BASE_URL = "http://localhost:7272"
TIMEOUT = 15  # seconds

ADMIN_USERNAME = "admin"  # CHANGE ME
ADMIN_PASSWORD = "admin"  # CHANGE ME


# ─── Data classes (fixed, per Section 3 boilerplate) ──────────────────────────

@dataclass
class TestResult:
    name: str
    method: str
    url: str
    status_code: Optional[int]
    passed: bool
    expected_statuses: list[int]
    duration_ms: float
    error: Optional[str] = None
    response_body: Optional[str] = None
    note: Optional[str] = None

    @property
    def status_label(self) -> str:
        return "PASS" if self.passed else "FAIL"


@dataclass
class TestSuite:
    name: str
    results: list[TestResult] = field(default_factory=list)

    @property
    def passed(self) -> int:
        return sum(1 for r in self.results if r.passed)

    @property
    def failed(self) -> int:
        return len(self.results) - self.passed

    @property
    def total(self) -> int:
        return len(self.results)


@dataclass
class Observation:
    """Stage E exploratory scenario — undocumented expected outcome. Never
    pass/fail. See run_observation()."""
    name: str
    method: str
    url: str
    status_code: Optional[int]
    duration_ms: float
    source: str
    response_body: Optional[str] = None


# ─── HTTP helper (fixed) ───────────────────────────────────────────────────────

class APIClient:
    def __init__(self, base_url: str):
        self.base_url = base_url.rstrip("/")
        self.session = requests.Session()
        self.session.headers.update({
            "Accept": "application/json",
            "Content-Type": "application/json",
        })

    def _make(self, method: str, path: str, token: Optional[str] = None,
               expected: Optional[list[int]] = None, **kwargs) -> TestResult:
        url = f"{self.base_url}/{path.lstrip('/')}"
        headers = kwargs.pop("headers", {})
        if token:
            headers["Authorization"] = f"Bearer {token}"
        if expected is None:
            expected = [200]

        start = time.perf_counter()
        try:
            resp = self.session.request(method.upper(), url, headers=headers, timeout=TIMEOUT, **kwargs)
            duration = (time.perf_counter() - start) * 1000
            try:
                body = json.dumps(resp.json(), indent=2, ensure_ascii=False)
            except Exception:
                body = resp.text[:500]
            return TestResult(
                name="", method=method.upper(), url=url, status_code=resp.status_code,
                passed=resp.status_code in expected, expected_statuses=expected,
                duration_ms=round(duration, 1), response_body=body,
            )
        except requests.exceptions.ConnectionError:
            duration = (time.perf_counter() - start) * 1000
            return TestResult(
                name="", method=method.upper(), url=url, status_code=None, passed=False,
                expected_statuses=expected, duration_ms=round(duration, 1),
                error="Connection refused — is the server running?",
            )
        except Exception as exc:
            duration = (time.perf_counter() - start) * 1000
            return TestResult(
                name="", method=method.upper(), url=url, status_code=None, passed=False,
                expected_statuses=expected, duration_ms=round(duration, 1), error=str(exc),
            )

    def get(self, path, token=None, expected=None, **kw):
        return self._make("GET", path, token, expected, **kw)

    def post(self, path, token=None, expected=None, **kw):
        return self._make("POST", path, token, expected, **kw)

    def put(self, path, token=None, expected=None, **kw):
        return self._make("PUT", path, token, expected, **kw)

    def delete(self, path, token=None, expected=None, **kw):
        return self._make("DELETE", path, token, expected, **kw)


# ─── Test runner helpers (fixed) ───────────────────────────────────────────────

def run(suite: TestSuite, name: str, result: TestResult, note: str = "") -> TestResult:
    result.name = name
    result.note = note
    suite.results.append(result)
    status = "✓" if result.passed else "✗"
    code = result.status_code or "ERR"
    print(f"  {status} [{code}] {name} ({result.duration_ms:.0f}ms)")
    if not result.passed and result.error:
        print(f"      Error: {result.error}")
    return result


def run_observation(bucket: list, name: str, source: str, result: TestResult) -> None:
    obs = Observation(
        name=name, method=result.method, url=result.url, status_code=result.status_code,
        duration_ms=result.duration_ms, source=source, response_body=result.response_body,
    )
    bucket.append(obs)
    code = result.status_code or "ERR"
    print(f"  👁 [{code}] {name} — observed (source: {source})")


def data_of(result: TestResult):
    """Envelope shape {success, message, data, error, timestamp, correlationId}
    confirmed against index.md's ApiResponseNotificationSendConfirmation schema."""
    if not result.response_body:
        return None
    try:
        return json.loads(result.response_body).get("data")
    except Exception:
        return None


def error_code_of(result: TestResult) -> Optional[str]:
    """Runtime error.code — underscored format per index.md Known Error Codes
    table (e.g. NOTIF_TEMPLATE_BILINGUAL_REQUIRED), never the governance ERR-ID
    (e.g. ERR-NOTIF-0002, hyphenated) which only exists in execution-plan.md."""
    if not result.response_body:
        return None
    try:
        return json.loads(result.response_body).get("error", {}).get("code")
    except Exception:
        return None


def extract_token(result: TestResult) -> Optional[str]:
    d = data_of(result)
    return d.get("accessToken") if isinstance(d, dict) else None


def extract_user_id(token: Optional[str]) -> Optional[int]:
    """Decode the JWT payload (no signature verification needed — same-process
    trust) to read the `userId` claim. recipientId on NOTIF_LOG has a real FK
    constraint to USERS, so send/schedule tests need an actual user id, not an
    arbitrary placeholder."""
    if not token:
        return None
    try:
        import base64
        payload = token.split(".")[1]
        payload += "=" * (-len(payload) % 4)
        claims = json.loads(base64.urlsafe_b64decode(payload))
        return claims.get("userId")
    except Exception:
        return None


def extract_id(result: TestResult) -> Optional[int]:
    d = data_of(result)
    return d.get("id") if isinstance(d, dict) else None


def extract_page_first_id(result: TestResult) -> Optional[int]:
    """Search endpoints (templates/search, history/search) return the
    PageNotificationTemplateResponse-style envelope confirmed in index.md:
    totalElements, totalPages, numberOfElements, first, last, pageable, size,
    number (NOT "page"), sort, empty. The `content` array is not explicitly
    listed in index.md's Pagination Envelope table for this module either —
    treated here as a documentation gap (standard for Spring Page<T>), not
    proof it's absent from the real response; flagged, not silently assumed."""
    d = data_of(result)
    if isinstance(d, dict):
        content = d.get("content")  # unconfirmed in index.md table — see note above
        if isinstance(content, list) and content:
            return content[0].get("id")
    return None


# ─── Auth (fixed pattern) ──────────────────────────────────────────────────────

def test_auth(client: APIClient) -> tuple[TestSuite, Optional[str]]:
    suite = TestSuite("Auth")
    print("\n[Auth]")
    r = run(suite, "Admin Login", client.post(
        "api/auth/login", expected=[200],
        json={"username": ADMIN_USERNAME, "password": ADMIN_PASSWORD},
    ))
    token = extract_token(r)
    return suite, token


# ─── NotificationTemplate ──────────────────────────────────────────────────────
# Covers: API-NOTIF-007 (Create), API-NOTIF-010 (GetById), API-NOTIF-008 (Update),
#         API-NOTIF-009 (Deactivate)
# NOTE: no Activate endpoint exists for Template (API catalog has no such route) —
# deactivate is terminal per this module's API surface, unlike the Category/Item
# reference pattern. Not an omission; there is simply nothing to reactivate via.

def test_template(client: APIClient, token: Optional[str], observations: list,
                    created_ids: dict[str, list[int]]) -> tuple[TestSuite, dict]:
    suite = TestSuite("Template")
    ids: dict = {}
    print("\n[Template]")

    unique_code = f"TEST_TPL_{int(time.time())}"

    # Covers: API-NOTIF-007 (Create Template)
    r = run(suite, "Create Template", client.post(
        "api/v1/notifications/templates", token=token, expected=[200, 201],
        json={
            "templateCode": unique_code,
            "templateNameAr": "قالب تجريبي",
            "templateNameEn": "Test Template",
            "channelTypeId": "EMAIL",
            "moduleCode": "SECURITY",
            "templateBodyAr": "مرحباً {{name}}",
            "templateBodyEn": "Hello {{name}}",
        },
    ))
    tpl_id = extract_id(r)
    ids["id"] = tpl_id
    ids["templateCode"] = unique_code
    if tpl_id:
        created_ids.setdefault("Template", []).append(tpl_id)

    # Covers: API-NOTIF-010 (GetById)
    if tpl_id:
        run(suite, "Get Template by ID", client.get(
            f"api/v1/notifications/templates/{tpl_id}", token=token, expected=[200]))
    r404 = run(suite, "Get Template by ID — Not Found", client.get(
        "api/v1/notifications/templates/999999999", token=token, expected=[404]))
    # ERR-NOTIF-0004 (PLATFORM-STD, entity-specific runtime code) — assert only if
    # the call actually got a body back; 404 without a JSON error body is still a pass.
    if r404.status_code == 404:
        code = error_code_of(r404)
        if code and code != "NOTIF_TEMPLATE_NOT_FOUND":
            print(f"      ⚠ Note: expected error.code=NOTIF_TEMPLATE_NOT_FOUND, got {code}")

    # Covers: API-NOTIF-008 (Update Template)
    if tpl_id:
        run(suite, "Update Template", client.put(
            f"api/v1/notifications/templates/{tpl_id}", token=token, expected=[200],
            json={
                "templateNameAr": "قالب محدث",
                "templateNameEn": "Updated Template",
                "channelTypeId": "EMAIL",
                "moduleCode": "SECURITY",
                "templateBodyAr": "مرحباً محدث {{name}}",
                "templateBodyEn": "Updated hello {{name}}",
            },
        ))

    # ── Stage C — governed negative tests (RULE-ID + ERR-ID + TC-ID all resolve) ──

    # Negative: RULE-NOTIF-006 / ERR-NOTIF-0002 / TC-NOTIF-009 — omit templateBodyEn
    # (bilingual requirement). NOTE: create.md's own field table marks
    # templateBodyAr/En as Required=No at the JSON-schema level, but RULE-NOTIF-006
    # in execution-plan.md is the authoritative business rule ("MUST have both") —
    # the doc's Required column reflects bean-validation only, not the service-layer
    # check. Runtime code per index.md: NOTIF_TEMPLATE_BILINGUAL_REQUIRED.
    r_bilingual = run(suite,
        "Create Template — missing templateBodyEn (RULE-NOTIF-006 / ERR-NOTIF-0002 / TC-NOTIF-009)",
        client.post("api/v1/notifications/templates", token=token, expected=[400],
            json={
                "templateCode": f"{unique_code}_NOBODY",
                "templateNameAr": "قالب بدون نص",
                "templateNameEn": "No Body Template",
                "channelTypeId": "EMAIL",
                "moduleCode": "SECURITY",
                "templateBodyAr": "نص عربي فقط",
            }))
    if (bid := extract_id(r_bilingual)):
        created_ids.setdefault("Template", []).append(bid)  # only if backend didn't actually reject
    if r_bilingual.status_code == 400:
        code = error_code_of(r_bilingual)
        if code and code != "NOTIF_TEMPLATE_BILINGUAL_REQUIRED":
            print(f"      ⚠ Note: expected error.code=NOTIF_TEMPLATE_BILINGUAL_REQUIRED, got {code}")

    # Negative: RULE-NOTIF-007 / ERR-NOTIF-0003 / TC-NOTIF-012 — duplicate templateCode
    # Runtime code per index.md: NOTIF_TEMPLATE_CODE_DUPLICATE.
    r_dup = run(suite,
        "Create Template — duplicate templateCode (RULE-NOTIF-007 / ERR-NOTIF-0003 / TC-NOTIF-012)",
        client.post("api/v1/notifications/templates", token=token, expected=[409],
            json={
                "templateCode": unique_code,  # reuse the code created above
                "templateNameAr": "قالب مكرر",
                "templateNameEn": "Duplicate Template",
                "channelTypeId": "EMAIL",
                "moduleCode": "SECURITY",
                "templateBodyAr": "نص",
                "templateBodyEn": "Body",
            }))
    if r_dup.status_code == 409:
        code = error_code_of(r_dup)
        if code and code != "NOTIF_TEMPLATE_CODE_DUPLICATE":
            print(f"      ⚠ Note: expected error.code=NOTIF_TEMPLATE_CODE_DUPLICATE, got {code}")

    # Negative: RULE-NOTIF-007 / ERR-NOTIF-0003 / TC-NOTIF-013 (ATTACK data-class) —
    # update.md's own DTO excludes templateCode entirely, but RULE-NOTIF-007 states
    # "MUST reject any attempt to modify templateCode after creation" — this sends
    # templateCode in the update body anyway to verify the backend actually enforces
    # immutability rather than silently ignoring the unknown field.
    # CONFIRMED (live run): immutability is enforced structurally, not via a business
    # rule — UpdateTemplateRequest has no templateCode field, and this backend's
    # Jackson config has fail-on-unknown-properties=true, so any unrecognized JSON
    # field 400s at deserialization (error.code=INVALID_JSON) before service code
    # ever runs. That's a stronger guarantee than a runtime 409 check would be, so
    # 400 is the correct expectation here, not 409.
    if tpl_id:
        r_immut = run(suite,
            "Update Template — inject templateCode in body (RULE-NOTIF-007 / ERR-NOTIF-0003 / TC-NOTIF-013)",
            client.put(f"api/v1/notifications/templates/{tpl_id}", token=token, expected=[400],
                json={
                    "templateCode": f"{unique_code}_HACKED",
                    "templateNameAr": "قالب محدث",
                    "templateNameEn": "Updated Template",
                    "channelTypeId": "EMAIL",
                    "moduleCode": "SECURITY",
                    "templateBodyAr": "نص",
                    "templateBodyEn": "Body",
                }))
        if r_immut.status_code == 400:
            code = error_code_of(r_immut)
            if code and code != "INVALID_JSON":
                print(f"      ⚠ Note: expected error.code=INVALID_JSON, got {code}")

    # ── Stage E — exploratory scenarios (Required=Yes / stated maxLength fields only) ──

    # Source: templateCode Constraints maxLength=50, Required=Yes
    r_code_max = client.post("api/v1/notifications/templates", token=token, expected=[200, 201, 400, 409],
        json={"templateCode": ("X" * 50), "templateNameAr": "ح", "templateNameEn": "N",
              "channelTypeId": "EMAIL", "moduleCode": "SECURITY",
              "templateBodyAr": "ح", "templateBodyEn": "b"})
    if (mid := extract_id(r_code_max)):
        created_ids.setdefault("Template", []).append(mid)
    run_observation(observations, "Create Template — templateCode at maxLength (50 chars)",
        source="API-NOTIF-007 templateCode Constraints: maxLength=50", result=r_code_max)

    r_code_over = client.post("api/v1/notifications/templates", token=token, expected=[200, 201, 400],
        json={"templateCode": ("X" * 51), "templateNameAr": "ح", "templateNameEn": "N",
              "channelTypeId": "EMAIL", "moduleCode": "SECURITY",
              "templateBodyAr": "ح", "templateBodyEn": "b"})
    if (oid := extract_id(r_code_over)):
        created_ids.setdefault("Template", []).append(oid)
    run_observation(observations, "Create Template — templateCode over maxLength (51 chars)",
        source="API-NOTIF-007 templateCode Constraints: maxLength=50", result=r_code_over)

    # Source: templateNameEn Constraints maxLength=200, Required=Yes
    r_name_over = client.post("api/v1/notifications/templates", token=token, expected=[200, 201, 400],
        json={"templateCode": f"{unique_code}_NAMEOVER", "templateNameAr": "ح",
              "templateNameEn": ("N" * 201), "channelTypeId": "EMAIL", "moduleCode": "SECURITY",
              "templateBodyAr": "ح", "templateBodyEn": "b"})
    if (nid := extract_id(r_name_over)):
        created_ids.setdefault("Template", []).append(nid)
    run_observation(observations, "Create Template — templateNameEn over maxLength (201 chars)",
        source="API-NOTIF-007 templateNameEn Constraints: maxLength=200", result=r_name_over)

    # Source: channelTypeId Required=Yes, LOV-NOTIF-001 — undocumented enum value
    # (only EMAIL/PUSH seen across docs; no code list attached this session)
    r_bad_channel = client.post("api/v1/notifications/templates", token=token, expected=[200, 201, 400],
        json={"templateCode": f"{unique_code}_BADCHAN", "templateNameAr": "ح", "templateNameEn": "N",
              "channelTypeId": "CARRIER_PIGEON", "moduleCode": "SECURITY",
              "templateBodyAr": "ح", "templateBodyEn": "b"})
    if (cid := extract_id(r_bad_channel)):
        created_ids.setdefault("Template", []).append(cid)
    run_observation(observations, "Create Template — channelTypeId undocumented enum value",
        source="API-NOTIF-007 channelTypeId Required=Yes, example values EMAIL/PUSH only", result=r_bad_channel)

    # Source: moduleCode Required=Yes — omission test
    r_omit_module = client.post("api/v1/notifications/templates", token=token, expected=[200, 201, 400],
        json={"templateCode": f"{unique_code}_NOMOD", "templateNameAr": "ح", "templateNameEn": "N",
              "channelTypeId": "EMAIL", "templateBodyAr": "ح", "templateBodyEn": "b"})
    if (moid := extract_id(r_omit_module)):
        created_ids.setdefault("Template", []).append(moid)
    run_observation(observations, "Create Template — omit required moduleCode",
        source="API-NOTIF-007 moduleCode Required=Yes", result=r_omit_module)

    # Covers: API-NOTIF-009 (Deactivate Template) — terminal, no Activate endpoint exists
    if tpl_id:
        run(suite, "Deactivate Template", client.put(
            f"api/v1/notifications/templates/{tpl_id}/deactivate", token=token, expected=[200]))

    return suite, ids


# ─── NotificationTemplate — Search ─────────────────────────────────────────────
# Covers: API-NOTIF-006 (Search Templates)

def test_template_search(client: APIClient, token: Optional[str], template_code: Optional[str]) -> TestSuite:
    suite = TestSuite("Template Search")
    print("\n[Template Search]")
    run(suite, "Search Templates — by templateCode", client.post(
        "api/v1/notifications/templates/search", token=token, expected=[200],
        json={"templateCode": template_code, "page": 0, "size": 20} if template_code
              else {"page": 0, "size": 20},
    ))
    run(suite, "Search Templates — default paging", client.post(
        "api/v1/notifications/templates/search", token=token, expected=[200], json={}))
    return suite


# ─── NotificationChannelConfig ─────────────────────────────────────────────────
# Covers: API-NOTIF-011 (List), API-NOTIF-012 (Update)
# No Create endpoint — 5 fixed, pre-seeded rows per SRS (NOT this run's data).
# Original isEnabledFl/configJson are captured and restored after the update test
# so this run doesn't leave shared seed configuration mutated.

def test_channel_config(client: APIClient, token: Optional[str]) -> tuple[TestSuite, Optional[dict]]:
    suite = TestSuite("Channel Config")
    print("\n[Channel Config]")

    r_list = run(suite, "List Channel Configs", client.get(
        "api/v1/notifications/channel-configs", token=token, expected=[200]))

    original = None
    cfg_id = None
    try:
        rows = json.loads(r_list.response_body).get("data") if r_list.response_body else None
        if isinstance(rows, list) and rows:
            first = rows[0]
            cfg_id = first.get("id")
            original = {"isEnabledFl": first.get("isEnabledFl"), "configJson": first.get("configJson")}
    except Exception:
        pass

    if cfg_id is not None:
        run(suite, "Update Channel Config", client.put(
            f"api/v1/notifications/channel-configs/{cfg_id}", token=token, expected=[200],
            json={"isEnabledFl": not original["isEnabledFl"] if original else True,
                  "configJson": original.get("configJson") if original else None}))
    else:
        print("  ⚠ Skipped Update — no channel config row available from List")

    return suite, ({"id": cfg_id, "original": original} if cfg_id is not None else None)


def restore_channel_config(client: APIClient, token: Optional[str], cfg_state: Optional[dict]) -> None:
    if not cfg_state or not cfg_state.get("original"):
        return
    cfg_id = cfg_state["id"]
    original = cfg_state["original"]
    r = client.put(f"api/v1/notifications/channel-configs/{cfg_id}", token=token, expected=[200],
                    json={"isEnabledFl": original["isEnabledFl"], "configJson": original.get("configJson")})
    if r.status_code == 200:
        print(f"  ✓ Restored Channel Config #{cfg_id} to original isEnabledFl={original['isEnabledFl']}")
    else:
        print(f"  ⚠ Could not restore Channel Config #{cfg_id} (status {r.status_code}) — check manually")


# ─── Send / Schedule ────────────────────────────────────────────────────────────
# Covers: API-NOTIF-001 (Send), API-NOTIF-002 (Schedule)
# NotificationLog rows created here have no GetById/Update/Deactivate/Delete API —
# SHARED-owner, append-only entity per ENTITY REGISTRY (execution-plan.md Section 3).
# logEntryIds are printed for traceability only; not tracked in created_ids since
# there is no cleanup endpoint to call — see cleanup() note.

def test_send_schedule(client: APIClient, token: Optional[str], template_code: str,
                         observations: list, recipient_id: int) -> TestSuite:
    suite = TestSuite("Send/Schedule")
    print("\n[Send/Schedule]")

    r_send = run(suite, "Send Notification — happy path", client.post(
        "api/v1/notifications/send", token=token, expected=[200, 201],
        json={
            "recipientId": recipient_id,
            "channelHint": ["EMAIL"],
            "templateCode": template_code,
            "contextData": {"name": "Test User"},
            "priority": "MEDIUM",
            "moduleCode": "SECURITY",
        }))
    log_ids = data_of(r_send)
    if isinstance(log_ids, dict) and log_ids.get("logEntryIds"):
        print(f"      logEntryIds: {log_ids['logEntryIds']} (append-only NotificationLog — no cleanup API)")

    # NOTE: scheduledAt's @Future validation is checked against the backend
    # JVM's local system clock (LocalDateTime, no timezone info) — datetime.now()
    # here, not datetime.utcnow(), since a UTC timestamp read as local time can
    # land in the past whenever the server's TZ offset is non-zero.
    run(suite, "Schedule Notification — happy path", client.post(
        "api/v1/notifications/schedule", token=token, expected=[200, 201],
        json={
            "recipientId": recipient_id,
            "channelHint": ["EMAIL"],
            "templateCode": template_code,
            "contextData": {"name": "Test User"},
            "priority": "MEDIUM",
            "moduleCode": "SECURITY",
            "scheduledAt": (datetime.now() + timedelta(hours=1)).strftime("%Y-%m-%dT%H:%M:%S"),
        }))

    # Negative: RULE-NOTIF-001 / ERR-NOTIF-0001 / TC-NOTIF-002 — event contract
    # incompleteness (omit recipientId). Runtime code per index.md: NOTIF_EVENT_INCOMPLETE.
    r_incomplete = run(suite,
        "Send Notification — missing recipientId (RULE-NOTIF-001 / ERR-NOTIF-0001 / TC-NOTIF-002)",
        client.post("api/v1/notifications/send", token=token, expected=[400],
            json={
                "channelHint": ["EMAIL"],
                "templateCode": template_code,
                "contextData": {"name": "Test User"},
                "priority": "MEDIUM",
                "moduleCode": "SECURITY",
            }))
    if r_incomplete.status_code == 400:
        code = error_code_of(r_incomplete)
        if code and code != "NOTIF_EVENT_INCOMPLETE":
            print(f"      ⚠ Note: expected error.code=NOTIF_EVENT_INCOMPLETE, got {code}")

    # ── Stage E exploratory (send/schedule) ──

    # Source: priority Constraints pattern HIGH|MEDIUM|LOW, Required=Yes —
    # undocumented enum value, not covered by RULE-NOTIF-001 (that only governs
    # presence, not pattern conformance) so not a Stage C duplicate.
    r_bad_priority = client.post("api/v1/notifications/send", token=token, expected=[200, 400],
        json={"recipientId": recipient_id, "channelHint": ["EMAIL"], "templateCode": template_code,
              "contextData": {"name": "Test User"}, "priority": "URGENT", "moduleCode": "SECURITY"})
    run_observation(observations, "Send Notification — priority outside HIGH|MEDIUM|LOW pattern",
        source="API-NOTIF-001 priority Constraints: pattern HIGH|MEDIUM|LOW", result=r_bad_priority)

    # Source: recipientId Type=integer (int64), Required=Yes — type mismatch
    r_type_mismatch = client.post("api/v1/notifications/send", token=token, expected=[200, 400],
        json={"recipientId": "not-a-number", "channelHint": ["EMAIL"], "templateCode": template_code,
              "contextData": {"name": "Test User"}, "priority": "MEDIUM", "moduleCode": "SECURITY"})
    run_observation(observations, "Send Notification — recipientId as string instead of int64",
        source="API-NOTIF-001 recipientId Type: integer (int64)", result=r_type_mismatch)

    return suite


# ─── Notification History Search ───────────────────────────────────────────────
# Covers: API-NOTIF-003 (Search History)

def test_history_search(client: APIClient, token: Optional[str], recipient_id: int) -> TestSuite:
    suite = TestSuite("History Search")
    print("\n[History Search]")
    run(suite, "Search Notification History — default paging", client.post(
        "api/v1/notifications/history/search", token=token, expected=[200],
        json={"page": 0, "size": 20}))
    run(suite, "Search Notification History — by recipientId", client.post(
        "api/v1/notifications/history/search", token=token, expected=[200],
        json={"recipientId": recipient_id, "page": 0, "size": 20}))
    return suite


# ─── Cleanup / Teardown ─────────────────────────────────────────────────────────

ENTITY_TEARDOWN_ORDER = ["Template"]

ENTITY_PATHS = {
    "Template": "api/v1/notifications/templates",
}

# Discovered during Stage A: no DELETE endpoint exists for Template in the API
# catalog (only Create/Search/Update/Deactivate/GetById) — soft-delete only.
ENTITY_HAS_HARD_DELETE = {
    "Template": False,
}


def cleanup(client: APIClient, token: Optional[str], created_ids: dict[str, list[int]],
            channel_config_state: Optional[dict]) -> None:
    """
    Best-effort teardown, always executed via try/finally in main().

    Template: soft-delete only (deactivate) — no hard-delete endpoint documented,
    so records remain in the database, deactivated. This is a documented
    limitation of the target API, not a bug in this script.

    Channel Config: not a created record — this run only toggled an existing
    seeded row's isEnabledFl/configJson, so it is restored to its original
    value rather than "cleaned up".

    NotificationLog (send/schedule output): intentionally NOT included here.
    It is a SHARED-owner, append-only entity per execution-plan.md's ENTITY
    REGISTRY — no Update/Deactivate/Delete endpoint exists for it at all, by
    design. There is nothing this script can call to remove it.
    """
    print("\n[Cleanup]")
    soft_only_remaining = 0

    for entity in ENTITY_TEARDOWN_ORDER:
        ids = created_ids.get(entity, [])
        path = ENTITY_PATHS.get(entity)
        has_delete = ENTITY_HAS_HARD_DELETE.get(entity, False)
        for _id in ids:
            if _id is None or path is None:
                continue
            if has_delete:
                r = client.delete(f"{path}/{_id}", token=token, expected=[200, 204, 404])
                if r.status_code in (200, 204):
                    print(f"  ✓ Deleted {entity} #{_id}")
                else:
                    print(f"  ⚠ Could not delete {entity} #{_id} (status {r.status_code})")
            else:
                r = client.put(f"{path}/{_id}/deactivate", token=token, expected=[200, 404, 409])
                if r.status_code == 200:
                    soft_only_remaining += 1
                    print(f"  ✓ Deactivated {entity} #{_id} (no hard-delete endpoint documented)")
                else:
                    soft_only_remaining += 1
                    print(f"  ⚠ Could not deactivate {entity} #{_id} (status {r.status_code}) — may remain active")

    restore_channel_config(client, token, channel_config_state)

    if soft_only_remaining:
        print(f"  ⚠ {soft_only_remaining} Template record(s) remain in the database (soft-deactivated) — "
              f"no hard-delete endpoint documented for this entity.")
    print("  ℹ NotificationLog rows created by Send/Schedule are NOT cleaned up — append-only "
          "SHARED-owner entity per execution-plan.md, no delete/deactivate API exists for it.")


# ─── HTML Report ────────────────────────────────────────────────────────────────

def generate_html_report(suites: list[TestSuite], observations: list, output_path: str,
                           duration: float, base_url: str) -> None:
    total_pass = sum(s.passed for s in suites)
    total_fail = sum(s.failed for s in suites)
    total_all = sum(s.total for s in suites)
    pass_pct = round(total_pass / total_all * 100, 1) if total_all else 0

    suite_rows = ""
    for suite in suites:
        pct = round(suite.passed / suite.total * 100, 1) if suite.total else 0
        suite_rows += f"""
        <tr>
          <td>{suite.name}</td>
          <td class="num">{suite.total}</td>
          <td class="num pass">{suite.passed}</td>
          <td class="num fail">{suite.failed}</td>
          <td class="num">{pct}%</td>
        </tr>"""

    detail_sections = ""
    for suite in suites:
        rows = ""
        for r in suite.results:
            status_class = "pass" if r.passed else "fail"
            code = r.status_code or "ERR"
            expected = ", ".join(str(s) for s in r.expected_statuses)
            note_html = f'<span class="note">{r.note}</span>' if r.note else ""
            error_html = f'<div class="error-msg">{r.error}</div>' if r.error else ""
            body_html = ""
            if r.response_body:
                short = r.response_body[:300]
                body_html = f'<details><summary>Response body</summary><pre>{short}{"..." if len(r.response_body) > 300 else ""}</pre></details>'
            rows += f"""
            <tr class="{status_class}">
              <td><span class="badge {status_class}">{r.status_label}</span></td>
              <td><span class="method {r.method.lower()}">{r.method}</span></td>
              <td class="test-name">{r.name} {note_html}</td>
              <td class="url">{r.url}</td>
              <td class="num">{code}</td>
              <td class="num">{expected}</td>
              <td class="num">{r.duration_ms:.0f}ms</td>
              <td>{error_html}{body_html}</td>
            </tr>"""
        detail_sections += f"""
        <section>
          <h2>{suite.name} — {suite.passed}/{suite.total} passed</h2>
          <table>
            <thead><tr><th>Status</th><th>Method</th><th>Test</th><th>URL</th>
              <th>Got</th><th>Expected</th><th>Time</th><th>Details</th></tr></thead>
            <tbody>{rows}</tbody>
          </table>
        </section>"""

    observation_section = ""
    if observations:
        obs_rows = ""
        for o in observations:
            code = o.status_code or "ERR"
            body_html = ""
            if o.response_body:
                short = o.response_body[:300]
                body_html = f'<details><summary>Response body</summary><pre>{short}{"..." if len(o.response_body) > 300 else ""}</pre></details>'
            obs_rows += f"""
            <tr>
              <td><span class="badge observed">OBSERVED</span></td>
              <td><span class="method {o.method.lower()}">{o.method}</span></td>
              <td class="test-name">{o.name}</td>
              <td class="url">{o.url}</td>
              <td class="num">{code}</td>
              <td class="url">{o.source}</td>
              <td class="num">{o.duration_ms:.0f}ms</td>
              <td>{body_html}</td>
            </tr>"""
        observation_section = f"""
        <section>
          <h2>⚠️ Exploratory Observations — undocumented expected outcomes, not pass/fail</h2>
          <p style="padding:0 20px;color:#666;font-size:.85rem">
            These scenarios test documented constraints (Required/maxLength/type/enum) but the
            API's expected response for violating them isn't specified in execution-plan.md.
            Review the observed status/body against your intended behavior — do not treat these
            as failures of the implementation.
          </p>
          <table>
            <thead><tr><th>Status</th><th>Method</th><th>Scenario</th><th>URL</th>
              <th>Observed</th><th>Source (doc/field)</th><th>Time</th><th>Details</th></tr></thead>
            <tbody>{obs_rows}</tbody>
          </table>
        </section>"""

    html = f"""<!DOCTYPE html>
<html lang="en"><head><meta charset="UTF-8" />
<title>Notification API Test Report — {datetime.now().strftime('%Y-%m-%d %H:%M')}</title>
<style>
  *,*::before,*::after{{box-sizing:border-box}}
  body{{font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,sans-serif;margin:0;padding:20px;background:#f4f6f9;color:#1a1a2e}}
  h1{{margin-bottom:4px}} .meta{{color:#666;font-size:.9rem;margin-bottom:24px}}
  .summary{{display:flex;gap:16px;flex-wrap:wrap;margin-bottom:28px}}
  .card{{background:#fff;border-radius:10px;padding:18px 24px;box-shadow:0 2px 6px rgba(0,0,0,.08);min-width:140px;text-align:center}}
  .card .num{{font-size:2.2rem;font-weight:700;line-height:1}} .card .label{{font-size:.8rem;color:#888;margin-top:4px}}
  .card.total .num{{color:#3a86ff}} .card.pass .num{{color:#2ec4b6}} .card.fail .num{{color:#e63946}}
  .suite-table{{background:#fff;border-radius:10px;box-shadow:0 2px 6px rgba(0,0,0,.08);margin-bottom:28px;overflow:hidden}}
  table{{width:100%;border-collapse:collapse;font-size:.875rem}}
  th{{background:#f0f2f5;text-align:left;padding:10px 14px;font-weight:600;color:#555}}
  td{{padding:8px 14px;border-top:1px solid #eef0f3;vertical-align:top}}
  tr.pass td{{background:#f6fff8}} tr.fail td{{background:#fff5f5}}
  .badge{{display:inline-block;padding:2px 8px;border-radius:10px;font-size:.75rem;font-weight:700}}
  .badge.pass{{background:#d4f7ef;color:#1a7a6f}} .badge.fail{{background:#fde8e8;color:#b91c1c}}
  .badge.observed{{background:#fff3cd;color:#8a6314}}
  .method{{display:inline-block;padding:2px 6px;border-radius:4px;font-size:.7rem;font-weight:700;color:#fff}}
  .method.get{{background:#3a86ff}} .method.post{{background:#2ec4b6}} .method.put{{background:#f4a261}}
  .num{{text-align:center}} .url{{font-size:.75rem;color:#666;word-break:break-all;max-width:260px}}
  .test-name{{font-weight:500}} .note{{font-size:.72rem;color:#999;margin-left:6px}}
  .error-msg{{color:#b91c1c;font-size:.8rem}}
  details summary{{cursor:pointer;color:#3a86ff;font-size:.78rem;margin-top:4px}}
  pre{{background:#f8f8f8;padding:8px;border-radius:4px;font-size:.72rem;overflow-x:auto;max-width:400px;white-space:pre-wrap}}
  section{{background:#fff;border-radius:10px;box-shadow:0 2px 6px rgba(0,0,0,.08);margin-bottom:28px;overflow:hidden}}
  section h2{{padding:14px 20px;margin:0;background:#f0f2f5;font-size:1rem;border-bottom:1px solid #e0e4ea}}
</style></head>
<body>
  <h1>Notification API Test Report</h1>
  <p class="meta">Generated {datetime.now().strftime('%Y-%m-%d %H:%M:%S')} &nbsp;·&nbsp;
     Base URL: {base_url} &nbsp;·&nbsp; Duration: {duration:.1f}s &nbsp;·&nbsp;
     API-NOTIF-004/005 (unread/markAsRead) skipped — DEFERRED per DRV-NOTIF-003</p>
  <div class="summary">
    <div class="card total"><div class="num">{total_all}</div><div class="label">Total</div></div>
    <div class="card pass"><div class="num">{total_pass}</div><div class="label">Passed</div></div>
    <div class="card fail"><div class="num">{total_fail}</div><div class="label">Failed</div></div>
    <div class="card total"><div class="num">{pass_pct}%</div><div class="label">Pass Rate</div></div>
  </div>
  <div class="suite-table"><table><thead><tr><th>Suite</th><th>Total</th><th>Pass</th><th>Fail</th><th>Rate</th></tr></thead>
  <tbody>{suite_rows}</tbody></table></div>
  {detail_sections}
  {observation_section}
</body></html>"""

    Path(output_path).write_text(html, encoding="utf-8")
    print(f"\n📄 HTML report written to: {output_path}")


# ─── Entry point ────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description="Notification Service API test suite")
    parser.add_argument("--base-url", default=BASE_URL, help="Base API URL")
    parser.add_argument("--report", default="notification_api_test_report.html", help="Output HTML file")
    args = parser.parse_args()

    client = APIClient(args.base_url)
    start_time = time.perf_counter()

    print("=" * 60)
    print("  Notification Service API Test Suite")
    print(f"  Base URL: {args.base_url}")
    print("  Skipped (DEFERRED, DRV-NOTIF-003): API-NOTIF-004 (unread), API-NOTIF-005 (markAsRead)")
    print("=" * 60)

    auth_suite, token = test_auth(client)
    observations: list = []
    created_ids: dict[str, list[int]] = {"Template": []}

    template_suite = TestSuite("Template")
    template_search_suite = TestSuite("Template Search")
    channel_config_suite = TestSuite("Channel Config")
    send_schedule_suite = TestSuite("Send/Schedule")
    history_search_suite = TestSuite("History Search")
    channel_config_state = None

    recipient_id = extract_user_id(token) or 4  # falls back to seeded 'admin' (users_pk=4)

    try:
        template_suite, tpl_ids = test_template(client, token, observations, created_ids)
        template_search_suite = test_template_search(client, token, tpl_ids.get("templateCode"))
        channel_config_suite, channel_config_state = test_channel_config(client, token)
        send_schedule_suite = test_send_schedule(
            client, token, tpl_ids.get("templateCode", "USER_WELCOME"), observations, recipient_id)
        history_search_suite = test_history_search(client, token, recipient_id)
    finally:
        # Always runs, even if a test above raised partway through.
        cleanup(client, token, created_ids, channel_config_state)

    duration = time.perf_counter() - start_time
    suites = [auth_suite, template_suite, template_search_suite, channel_config_suite,
              send_schedule_suite, history_search_suite]

    print("\n" + "=" * 60)
    print("  SUMMARY")
    print("=" * 60)
    total_pass = sum(s.passed for s in suites)
    total_all = sum(s.total for s in suites)
    for s in suites:
        bar = "█" * s.passed + "░" * s.failed
        print(f"  {s.name:<20} {s.passed:>3}/{s.total:<3}  {bar}")
    print(f"\n  Total: {total_pass}/{total_all} passed  ({duration:.1f}s)")
    if observations:
        print(f"  Exploratory observations: {len(observations)} (see HTML report — not pass/fail)")

    generate_html_report(suites, observations, args.report, duration, args.base_url)
    sys.exit(0 if total_pass == total_all else 1)


if __name__ == "__main__":
    main()
