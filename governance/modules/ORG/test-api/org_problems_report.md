# ORG Module API — Problems Report

Generated 2026-07-04 09:24:19 · Base URL: http://localhost:7272

**3 failure(s)** across all suites — 🔴 3 likely real bugs, 🟡 0 test assumption mismatches, ⚪ 0 infrastructure issues.

## 🔴 Likely Real Bugs (3)

A call that should have been rejected per a documented RULE/ERR succeeded instead, or failed with a status not explained by any documented rule. Ambiguous failures default here rather than being silently treated as benign.

### [Region] Create Region
- **POST http://localhost:7272/api/v1/org/regions**
- Got `404`, expected `200, 201`
- Response: `{
  "correlationId": "9fd928f0-fe86-4d44-9179-df4ffdce1183",
  "error": {
    "code": "ERR_ORG_0004",
    "details": "Record not found",
    "path": "/api/v1/org/regions",
    "timestamp": "2026-07-04T05:24:18.913792Z"
  },
  "message": "Record not found",
  "success": false,
  "timestamp": "2026-07-04T05:24:18.913794Z"
}`

### [Deactivation Rules (RULE-ORG-001..005)] Deactivate Branch (happy path, API-ORG-010)
- **PUT http://localhost:7272/api/v1/org/branches/21/deactivate**
- Got `409`, expected `200`
- Response: `{
  "correlationId": "a601935f-85e9-45ea-ad00-d1d7931e072f",
  "error": {
    "code": "ERR_ORG_0007",
    "details": "Cannot deactivate Branch: active departments exist",
    "path": "/api/v1/org/branches/21/deactivate",
    "timestamp": "2026-07-04T05:24:19.523401Z"
  },
  "message": "Cannot deactivate Branch: active departments exist",
  "success": false,
  "timestamp": "2026-07-04T05:24:19.5234...`

### [Deactivation Rules (RULE-ORG-001..005)] Deactivate LegalEntity (happy path, API-ORG-004)
- **PUT http://localhost:7272/api/v1/org/legal-entities/18/deactivate**
- Got `409`, expected `200`
- Response: `{
  "correlationId": "8012746b-67e2-42ec-bea9-f599a0e04613",
  "error": {
    "code": "ERR_ORG_0005",
    "details": "Cannot deactivate Legal Entity: active branches exist",
    "path": "/api/v1/org/legal-entities/18/deactivate",
    "timestamp": "2026-07-04T05:24:19.550783Z"
  },
  "message": "Cannot deactivate Legal Entity: active branches exist",
  "success": false,
  "timestamp": "2026-07-04T0...`

## 🟡 Test Assumption Mismatches (0)

The backend correctly rejected the request, but with a different status code than this test asserted. Likely the test's expected-status list needs correcting against execution-plan.md — not a backend defect.

_None._

## ⚪ Infrastructure (0)

Connection/timeout failures. Rerun — these carry no signal about the API itself.

_None._
