# SEC MODE 5 — Problems Report

Run ID: 136014

Only failures are listed below, categorized per Stage G of the MODE 5 spec. A PASS entry never appears here.

## 🔴 Likely Real Bugs (2)

- **[10. RoleActionGrant] SoD conflict guard via role assignment (RULE-SEC-005/TC-SEC-020) — known-unreachable in SEC v1** — HTTP 200, error.code=None (expected 409 SEC-409-SOD-CONFLICT; got 200 because SEC v1's conflictingCounterpartActions() unconditionally returns an empty set — see UserRoleService.java:150)
- **[11. ActiveSession] search active sessions via documented top-level userId field (REAL BUG — field ignored)** — HTTP 200, rows returned=5, all match u2=False, identical to unfiltered response=False (documented top-level userId field appears to be ignored server-side)

## 🟡 Test Assumption Mismatches (0)

None.

## ⚪ Infrastructure (0)

None.
