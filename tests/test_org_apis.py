#!/usr/bin/env python3
"""
ERP Backend — Organization (ORG) Module API Test Suite
Logs in as admin, exercises the full create/search/get/update/activate/
deactivate flow for every ORG entity in FK dependency order, and generates
an HTML report.

Prerequisite: the admin user's role must actually hold the ORG permissions
(PERM_LEGAL_ENTITY_*, PERM_BRANCH_*, PERM_REGION_*, PERM_DEPARTMENT_*,
PERM_COST_CENTER_*, PERM_PROFIT_CENTER_*, PERM_LOCATION_SITE_*) — without
them every call below returns 403 regardless of whether the API itself works.

Usage:
    python3 test_org_apis.py
    python3 test_org_apis.py --base-url http://localhost:7273
    python3 test_org_apis.py --report org_api_test_report.html
"""

import argparse
import json
import sys
import time
from dataclasses import dataclass, field
from datetime import datetime
from pathlib import Path
from typing import Optional

try:
    import requests
except ImportError:
    print("requests library not found. Install it: pip install requests")
    sys.exit(1)


# ─── Configuration ────────────────────────────────────────────────────────────

BASE_URL = "http://localhost:7272"
TIMEOUT = 15  # seconds

ADMIN_USERNAME = "admin"
ADMIN_PASSWORD = "admin123"

# Region types are a real DB FK (org_region_type), seeded in dev: 1=GEOGRAPHIC, 2=SALES, 3=OPERATIONAL.
REGION_TYPE_ID = 1


# ─── Data classes ─────────────────────────────────────────────────────────────

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


# ─── HTTP helper ──────────────────────────────────────────────────────────────

class APIClient:
    def __init__(self, base_url: str):
        self.base_url = base_url.rstrip("/")
        self.session = requests.Session()
        self.session.headers.update({
            "Accept": "application/json",
            "Content-Type": "application/json",
        })

    def _make(
        self,
        method: str,
        path: str,
        token: Optional[str] = None,
        expected: Optional[list[int]] = None,
        **kwargs,
    ) -> TestResult:
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
                name="",
                method=method.upper(),
                url=url,
                status_code=resp.status_code,
                passed=resp.status_code in expected,
                expected_statuses=expected,
                duration_ms=round(duration, 1),
                response_body=body,
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


# ─── Test runner helpers ──────────────────────────────────────────────────────

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


def data_of(result: TestResult):
    """Every ORG response is wrapped in the standard envelope: {success, message, data, error, timestamp}."""
    if not result.response_body:
        return None
    try:
        return json.loads(result.response_body).get("data")
    except Exception:
        return None


def extract_token(result: TestResult) -> Optional[str]:
    d = data_of(result)
    return d.get("accessToken") if isinstance(d, dict) else None


def extract_id(result: TestResult) -> Optional[int]:
    d = data_of(result)
    return d.get("id") if isinstance(d, dict) else None


def extract_page_first_id(result: TestResult) -> Optional[int]:
    """Search endpoints return a Spring Page<T>: {..., content: [...]}."""
    d = data_of(result)
    if isinstance(d, dict):
        content = d.get("content")
        if isinstance(content, list) and content:
            return content[0].get("id")
    return None


# ─── Test suites ──────────────────────────────────────────────────────────────

def test_auth(client: APIClient) -> tuple[TestSuite, Optional[str]]:
    suite = TestSuite("Auth")
    print("\n[Auth]")

    r = run(suite, "Admin Login", client.post(
        "api/auth/login", expected=[200],
        json={"username": ADMIN_USERNAME, "password": ADMIN_PASSWORD},
    ))
    token = extract_token(r)
    if token:
        print("    → admin token obtained")
    else:
        print("    ⚠ No admin token — every subsequent test will fail")

    run(suite, "Login — Invalid Password", client.post(
        "api/auth/login", expected=[401],
        json={"username": ADMIN_USERNAME, "password": "wrong-password"},
    ))

    return suite, token


def test_auth_guard(client: APIClient) -> TestSuite:
    suite = TestSuite("Auth Guard")
    print("\n[Auth Guard]")
    run(suite, "Legal Entities — No Token → 401", client.get(
        "api/v1/org/legal-entities/1", expected=[401]
    ))
    return suite


def test_legal_entity(client: APIClient, token: Optional[str]) -> tuple[TestSuite, dict]:
    suite = TestSuite("Legal Entity")
    ids: dict = {}
    print("\n[Legal Entity]")

    if not token:
        print("  ⚠ Skipping — no admin token available")
        return suite, ids

    ts = int(time.time())
    name_ar = f"شركة اختبار {ts}"
    name_en = f"Test Legal Entity {ts}"

    r = run(suite, "Create Legal Entity", client.post(
        "api/v1/org/legal-entities", token=token, expected=[201],
        json={"nameAr": name_ar, "nameEn": name_en, "entityTypeId": "HEAD_OFFICE", "notes": "Created by test_org_apis.py"},
    ))
    entity_id = extract_id(r)
    ids["id"] = entity_id

    run(suite, "Create Legal Entity — Missing Required Field", client.post(
        "api/v1/org/legal-entities", token=token, expected=[400],
        json={"nameEn": "Missing Arabic Name", "entityTypeId": "HEAD_OFFICE"},
    ))

    run(suite, "Create Legal Entity — Duplicate Name", client.post(
        "api/v1/org/legal-entities", token=token, expected=[409],
        json={"nameAr": name_ar, "nameEn": name_en, "entityTypeId": "HEAD_OFFICE"},
    ), note="RULE-ORG-015 — name uniqueness is global scope")

    run(suite, "Search Legal Entities", client.post(
        "api/v1/org/legal-entities/search", token=token, expected=[200],
        json={"filters": [{"field": "nameEn", "operator": "CONTAINS", "value": "Test Legal Entity"}],
              "sorts": [{"field": "nameEn", "direction": "ASC"}], "page": 0, "size": 20},
    ))

    if entity_id:
        run(suite, "Get Legal Entity by ID", client.get(
            f"api/v1/org/legal-entities/{entity_id}", token=token, expected=[200]
        ))
    run(suite, "Get Legal Entity by ID — Not Found", client.get(
        "api/v1/org/legal-entities/999999999", token=token, expected=[404]
    ))

    if entity_id:
        run(suite, "Update Legal Entity", client.put(
            f"api/v1/org/legal-entities/{entity_id}", token=token, expected=[200],
            json={"notes": "Updated by test_org_apis.py"},
        ))
        run(suite, "Deactivate Legal Entity", client.put(
            f"api/v1/org/legal-entities/{entity_id}/deactivate", token=token, expected=[200]
        ))
        run(suite, "Activate Legal Entity", client.put(
            f"api/v1/org/legal-entities/{entity_id}/activate", token=token, expected=[200]
        ), note="Left active so downstream entities (Branch/Region/ProfitCenter) can be created under it")

    return suite, ids


def test_branch(client: APIClient, token: Optional[str], legal_entity_id: Optional[int]) -> tuple[TestSuite, dict]:
    suite = TestSuite("Branch")
    ids: dict = {}
    print("\n[Branch]")

    if not token or not legal_entity_id:
        print("  ⚠ Skipping — no admin token or no Legal Entity to attach to")
        return suite, ids

    ts = int(time.time())
    name_en = f"Test Branch {ts}"

    r = run(suite, "Create Branch", client.post(
        "api/v1/org/branches", token=token, expected=[201],
        json={"legalEntityFk": legal_entity_id, "nameAr": f"فرع اختبار {ts}", "nameEn": name_en,
              "branchTypeId": "MAIN_BRANCH", "notes": "Created by test_org_apis.py"},
    ))
    branch_id = extract_id(r)
    ids["id"] = branch_id

    run(suite, "Create Branch — Missing Required Field", client.post(
        "api/v1/org/branches", token=token, expected=[400],
        json={"legalEntityFk": legal_entity_id, "nameEn": "Missing Arabic Name", "branchTypeId": "MAIN_BRANCH"},
    ))

    run(suite, "Create Branch — Unknown Legal Entity", client.post(
        "api/v1/org/branches", token=token, expected=[404, 422, 400],
        json={"legalEntityFk": 999999999, "nameAr": "فرع", "nameEn": "Orphan Branch", "branchTypeId": "MAIN_BRANCH"},
    ))

    run(suite, "Search Branches", client.post(
        "api/v1/org/branches/search", token=token, expected=[200],
        json={"filters": [{"field": "nameEn", "operator": "CONTAINS", "value": "Test Branch"}], "page": 0, "size": 20},
    ))

    if branch_id:
        run(suite, "Get Branch by ID", client.get(f"api/v1/org/branches/{branch_id}", token=token, expected=[200]))
    run(suite, "Get Branch by ID — Not Found", client.get("api/v1/org/branches/999999999", token=token, expected=[404]))

    if branch_id:
        run(suite, "Update Branch", client.put(
            f"api/v1/org/branches/{branch_id}", token=token, expected=[200], json={"notes": "Updated by test_org_apis.py"}
        ))
        run(suite, "Deactivate Branch", client.put(f"api/v1/org/branches/{branch_id}/deactivate", token=token, expected=[200]))
        run(suite, "Activate Branch", client.put(
            f"api/v1/org/branches/{branch_id}/activate", token=token, expected=[200]
        ), note="Left active so Department/CostCenter/LocationSite can be created under it")

    return suite, ids


def test_region(client: APIClient, token: Optional[str], legal_entity_id: Optional[int]) -> tuple[TestSuite, dict]:
    suite = TestSuite("Region")
    ids: dict = {}
    print("\n[Region]")

    if not token or not legal_entity_id:
        print("  ⚠ Skipping — no admin token or no Legal Entity to attach to")
        return suite, ids

    ts = int(time.time())
    r = run(suite, "Create Region", client.post(
        "api/v1/org/regions", token=token, expected=[201],
        json={"legalEntityFk": legal_entity_id, "regionTypeIdFk": REGION_TYPE_ID,
              "nameAr": f"منطقة اختبار {ts}", "nameEn": f"Test Region {ts}", "notes": "Created by test_org_apis.py"},
    ))
    region_id = extract_id(r)
    ids["id"] = region_id

    run(suite, "Create Region — Unknown Region Type", client.post(
        "api/v1/org/regions", token=token, expected=[404, 422, 400],
        json={"legalEntityFk": legal_entity_id, "regionTypeIdFk": 999999, "nameAr": "منطقة", "nameEn": "Orphan Region"},
    ))

    run(suite, "Search Regions", client.post(
        "api/v1/org/regions/search", token=token, expected=[200],
        json={"filters": [{"field": "nameEn", "operator": "CONTAINS", "value": "Test Region"}], "page": 0, "size": 20},
    ))

    if region_id:
        run(suite, "Get Region by ID", client.get(f"api/v1/org/regions/{region_id}", token=token, expected=[200]))
    run(suite, "Get Region by ID — Not Found", client.get("api/v1/org/regions/999999999", token=token, expected=[404]))

    if region_id:
        run(suite, "Update Region", client.put(
            f"api/v1/org/regions/{region_id}", token=token, expected=[200], json={"notes": "Updated by test_org_apis.py"}
        ))
        run(suite, "Deactivate Region", client.put(f"api/v1/org/regions/{region_id}/deactivate", token=token, expected=[200]))
        run(suite, "Activate Region", client.put(f"api/v1/org/regions/{region_id}/activate", token=token, expected=[200]))

    return suite, ids


def test_profit_center(client: APIClient, token: Optional[str], legal_entity_id: Optional[int]) -> tuple[TestSuite, dict]:
    suite = TestSuite("Profit Center")
    ids: dict = {}
    print("\n[Profit Center]")

    if not token or not legal_entity_id:
        print("  ⚠ Skipping — no admin token or no Legal Entity to attach to")
        return suite, ids

    ts = int(time.time())
    r = run(suite, "Create Profit Center", client.post(
        "api/v1/org/profit-centers", token=token, expected=[201],
        json={"legalEntityFk": legal_entity_id, "nameAr": f"مركز ربح اختبار {ts}", "nameEn": f"Test Profit Center {ts}",
              "notes": "Created by test_org_apis.py"},
    ))
    pc_id = extract_id(r)
    ids["id"] = pc_id

    run(suite, "Search Profit Centers", client.post(
        "api/v1/org/profit-centers/search", token=token, expected=[200],
        json={"filters": [{"field": "nameEn", "operator": "CONTAINS", "value": "Test Profit Center"}], "page": 0, "size": 20},
    ))

    if pc_id:
        run(suite, "Get Profit Center by ID", client.get(f"api/v1/org/profit-centers/{pc_id}", token=token, expected=[200]))
    run(suite, "Get Profit Center by ID — Not Found", client.get("api/v1/org/profit-centers/999999999", token=token, expected=[404]))

    if pc_id:
        run(suite, "Update Profit Center", client.put(
            f"api/v1/org/profit-centers/{pc_id}", token=token, expected=[200], json={"notes": "Updated by test_org_apis.py"}
        ))
        run(suite, "Deactivate Profit Center", client.put(f"api/v1/org/profit-centers/{pc_id}/deactivate", token=token, expected=[200]))
        run(suite, "Activate Profit Center", client.put(f"api/v1/org/profit-centers/{pc_id}/activate", token=token, expected=[200]))

    return suite, ids


def test_department(client: APIClient, token: Optional[str], branch_id: Optional[int]) -> tuple[TestSuite, dict]:
    suite = TestSuite("Department")
    ids: dict = {}
    print("\n[Department]")

    if not token or not branch_id:
        print("  ⚠ Skipping — no admin token or no Branch to attach to")
        return suite, ids

    ts = int(time.time())
    r = run(suite, "Create Department", client.post(
        "api/v1/org/departments", token=token, expected=[201],
        json={"branchFk": branch_id, "nameAr": f"قسم اختبار {ts}", "nameEn": f"Test Department {ts}",
              "nodeTypeId": "DETAIL", "notes": "Created by test_org_apis.py"},
    ))
    dept_id = extract_id(r)
    ids["id"] = dept_id

    run(suite, "Search Departments", client.post(
        "api/v1/org/departments/search", token=token, expected=[200],
        json={"filters": [{"field": "nameEn", "operator": "CONTAINS", "value": "Test Department"}], "page": 0, "size": 20},
    ))

    run(suite, "Get Department Tree", client.get(
        f"api/v1/org/departments/tree?branchFk={branch_id}", token=token, expected=[200]
    ))

    if dept_id:
        run(suite, "Get Department by ID", client.get(f"api/v1/org/departments/{dept_id}", token=token, expected=[200]))
    run(suite, "Get Department by ID — Not Found", client.get("api/v1/org/departments/999999999", token=token, expected=[404]))

    if dept_id:
        run(suite, "Update Department", client.put(
            f"api/v1/org/departments/{dept_id}", token=token, expected=[200], json={"notes": "Updated by test_org_apis.py"}
        ))
        run(suite, "Deactivate Department", client.put(f"api/v1/org/departments/{dept_id}/deactivate", token=token, expected=[200]))
        run(suite, "Activate Department", client.put(f"api/v1/org/departments/{dept_id}/activate", token=token, expected=[200]))

    return suite, ids


def test_cost_center(client: APIClient, token: Optional[str], branch_id: Optional[int]) -> tuple[TestSuite, dict]:
    suite = TestSuite("Cost Center")
    ids: dict = {}
    print("\n[Cost Center]")

    if not token or not branch_id:
        print("  ⚠ Skipping — no admin token or no Branch to attach to")
        return suite, ids

    ts = int(time.time())
    r = run(suite, "Create Cost Center", client.post(
        "api/v1/org/cost-centers", token=token, expected=[201],
        json={"branchFk": branch_id, "nameAr": f"مركز تكلفة اختبار {ts}", "nameEn": f"Test Cost Center {ts}",
              "nodeTypeId": "DETAIL", "costCenterTypeId": "DIRECT", "notes": "Created by test_org_apis.py"},
    ))
    cc_id = extract_id(r)
    ids["id"] = cc_id

    run(suite, "Search Cost Centers", client.post(
        "api/v1/org/cost-centers/search", token=token, expected=[200],
        json={"filters": [{"field": "nameEn", "operator": "CONTAINS", "value": "Test Cost Center"}], "page": 0, "size": 20},
    ))

    run(suite, "Get Cost Center Tree", client.get(
        f"api/v1/org/cost-centers/tree?branchFk={branch_id}", token=token, expected=[200]
    ))

    if cc_id:
        run(suite, "Get Cost Center by ID", client.get(f"api/v1/org/cost-centers/{cc_id}", token=token, expected=[200]))
    run(suite, "Get Cost Center by ID — Not Found", client.get("api/v1/org/cost-centers/999999999", token=token, expected=[404]))

    if cc_id:
        run(suite, "Update Cost Center", client.put(
            f"api/v1/org/cost-centers/{cc_id}", token=token, expected=[200], json={"notes": "Updated by test_org_apis.py"}
        ))
        run(suite, "Deactivate Cost Center", client.put(f"api/v1/org/cost-centers/{cc_id}/deactivate", token=token, expected=[200]))
        run(suite, "Activate Cost Center", client.put(f"api/v1/org/cost-centers/{cc_id}/activate", token=token, expected=[200]))

    return suite, ids


def test_location_site(client: APIClient, token: Optional[str], branch_id: Optional[int]) -> tuple[TestSuite, dict]:
    suite = TestSuite("Location Site")
    ids: dict = {}
    print("\n[Location Site]")

    if not token or not branch_id:
        print("  ⚠ Skipping — no admin token or no Branch to attach to")
        return suite, ids

    ts = int(time.time())
    r = run(suite, "Create Location Site", client.post(
        "api/v1/org/location-sites", token=token, expected=[201],
        json={"branchFk": branch_id, "nameAr": f"موقع اختبار {ts}", "nameEn": f"Test Location Site {ts}",
              "siteTypeId": "WAREHOUSE", "notes": "Created by test_org_apis.py"},
    ))
    site_id = extract_id(r)
    ids["id"] = site_id

    run(suite, "Search Location Sites", client.post(
        "api/v1/org/location-sites/search", token=token, expected=[200],
        json={"filters": [{"field": "nameEn", "operator": "CONTAINS", "value": "Test Location Site"}], "page": 0, "size": 20},
    ))

    if site_id:
        run(suite, "Get Location Site by ID", client.get(f"api/v1/org/location-sites/{site_id}", token=token, expected=[200]))
    run(suite, "Get Location Site by ID — Not Found", client.get("api/v1/org/location-sites/999999999", token=token, expected=[404]))

    if site_id:
        run(suite, "Update Location Site", client.put(
            f"api/v1/org/location-sites/{site_id}", token=token, expected=[200], json={"notes": "Updated by test_org_apis.py"}
        ))
        run(suite, "Deactivate Location Site", client.put(f"api/v1/org/location-sites/{site_id}/deactivate", token=token, expected=[200]))
        run(suite, "Activate Location Site", client.put(f"api/v1/org/location-sites/{site_id}/activate", token=token, expected=[200]))

    return suite, ids


# ─── HTML Report ──────────────────────────────────────────────────────────────

def generate_html_report(suites: list[TestSuite], output_path: str, duration: float, base_url: str) -> None:
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
            <thead>
              <tr>
                <th>Status</th><th>Method</th><th>Test</th><th>URL</th>
                <th>Got</th><th>Expected</th><th>Time</th><th>Details</th>
              </tr>
            </thead>
            <tbody>{rows}</tbody>
          </table>
        </section>"""

    html = f"""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>ORG API Test Report — {datetime.now().strftime('%Y-%m-%d %H:%M')}</title>
  <style>
    *, *::before, *::after {{ box-sizing: border-box; }}
    body {{ font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            margin: 0; padding: 20px; background: #f4f6f9; color: #1a1a2e; }}
    h1 {{ margin-bottom: 4px; }}
    .meta {{ color: #666; font-size: .9rem; margin-bottom: 24px; }}
    .summary {{ display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 28px; }}
    .card {{ background: #fff; border-radius: 10px; padding: 18px 24px;
             box-shadow: 0 2px 6px rgba(0,0,0,.08); min-width: 140px; text-align: center; }}
    .card .num {{ font-size: 2.2rem; font-weight: 700; line-height: 1; }}
    .card .label {{ font-size: .8rem; color: #888; margin-top: 4px; }}
    .card.total .num {{ color: #3a86ff; }}
    .card.pass .num {{ color: #2ec4b6; }}
    .card.fail .num {{ color: #e63946; }}
    .suite-table {{ background: #fff; border-radius: 10px; padding: 0;
                    box-shadow: 0 2px 6px rgba(0,0,0,.08); margin-bottom: 28px;
                    overflow: hidden; }}
    table {{ width: 100%; border-collapse: collapse; font-size: .875rem; }}
    th {{ background: #f0f2f5; text-align: left; padding: 10px 14px;
          font-weight: 600; color: #555; }}
    td {{ padding: 8px 14px; border-top: 1px solid #eef0f3; vertical-align: top; }}
    tr.pass td {{ background: #f6fff8; }}
    tr.fail td {{ background: #fff5f5; }}
    .badge {{ display: inline-block; padding: 2px 8px; border-radius: 10px;
              font-size: .75rem; font-weight: 700; }}
    .badge.pass {{ background: #d4f7ef; color: #1a7a6f; }}
    .badge.fail {{ background: #fde8e8; color: #b91c1c; }}
    .method {{ display: inline-block; padding: 2px 6px; border-radius: 4px;
               font-size: .7rem; font-weight: 700; color: #fff; }}
    .method.get  {{ background: #3a86ff; }}
    .method.post {{ background: #2ec4b6; }}
    .method.put  {{ background: #f4a261; }}
    .num {{ text-align: center; }}
    .url {{ font-size: .75rem; color: #666; word-break: break-all; max-width: 260px; }}
    .test-name {{ font-weight: 500; }}
    .note {{ font-size: .72rem; color: #999; margin-left: 6px; }}
    .error-msg {{ color: #b91c1c; font-size: .8rem; }}
    details summary {{ cursor: pointer; color: #3a86ff; font-size: .78rem; margin-top: 4px; }}
    pre {{ background: #f8f8f8; padding: 8px; border-radius: 4px;
           font-size: .72rem; overflow-x: auto; max-width: 400px; white-space: pre-wrap; }}
    section {{ background: #fff; border-radius: 10px;
               box-shadow: 0 2px 6px rgba(0,0,0,.08); margin-bottom: 28px;
               overflow: hidden; }}
    section h2 {{ padding: 14px 20px; margin: 0; background: #f0f2f5;
                  font-size: 1rem; border-bottom: 1px solid #e0e4ea; }}
  </style>
</head>
<body>
  <h1>ORG Module API Test Report</h1>
  <p class="meta">Generated {datetime.now().strftime('%Y-%m-%d %H:%M:%S')} &nbsp;·&nbsp;
     Base URL: {base_url} &nbsp;·&nbsp; Duration: {duration:.1f}s</p>

  <div class="summary">
    <div class="card total"><div class="num">{total_all}</div><div class="label">Total</div></div>
    <div class="card pass"><div class="num">{total_pass}</div><div class="label">Passed</div></div>
    <div class="card fail"><div class="num">{total_fail}</div><div class="label">Failed</div></div>
    <div class="card total"><div class="num">{pass_pct}%</div><div class="label">Pass Rate</div></div>
  </div>

  <div class="suite-table">
    <table>
      <thead>
        <tr><th>Suite</th><th>Total</th><th>Pass</th><th>Fail</th><th>Rate</th></tr>
      </thead>
      <tbody>{suite_rows}</tbody>
    </table>
  </div>

  {detail_sections}
</body>
</html>"""

    Path(output_path).write_text(html, encoding="utf-8")
    print(f"\n📄 HTML report written to: {output_path}")


# ─── Entry point ──────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description="ORG module API test suite")
    parser.add_argument("--base-url", default=BASE_URL, help="Base API URL")
    parser.add_argument("--report", default="org_api_test_report.html", help="Output HTML file")
    args = parser.parse_args()

    client = APIClient(args.base_url)
    start_time = time.perf_counter()

    print("=" * 60)
    print("  ERP Backend — Organization (ORG) Module API Test Suite")
    print(f"  Base URL: {args.base_url}")
    print("=" * 60)

    auth_suite, token = test_auth(client)
    guard_suite = test_auth_guard(client)

    legal_entity_suite, le_ids = test_legal_entity(client, token)
    branch_suite, branch_ids = test_branch(client, token, le_ids.get("id"))
    region_suite, region_ids = test_region(client, token, le_ids.get("id"))
    profit_center_suite, pc_ids = test_profit_center(client, token, le_ids.get("id"))
    department_suite, dept_ids = test_department(client, token, branch_ids.get("id"))
    cost_center_suite, cc_ids = test_cost_center(client, token, branch_ids.get("id"))
    location_site_suite, site_ids = test_location_site(client, token, branch_ids.get("id"))

    duration = time.perf_counter() - start_time
    suites = [
        auth_suite, guard_suite, legal_entity_suite, branch_suite, region_suite,
        profit_center_suite, department_suite, cost_center_suite, location_site_suite,
    ]

    print("\n" + "=" * 60)
    print("  SUMMARY")
    print("=" * 60)
    total_pass = sum(s.passed for s in suites)
    total_all = sum(s.total for s in suites)
    for s in suites:
        bar = "█" * s.passed + "░" * s.failed
        print(f"  {s.name:<20} {s.passed:>3}/{s.total:<3}  {bar}")
    print(f"\n  Total: {total_pass}/{total_all} passed  ({duration:.1f}s)")

    generate_html_report(suites, args.report, duration, args.base_url)

    sys.exit(0 if total_pass == total_all else 1)


if __name__ == "__main__":
    main()
