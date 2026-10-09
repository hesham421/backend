<!-- source: PHASE:ALIGN-BE -->
<!-- traces: REQ-FIN-017, REQ-FIN-076 -->
<!-- PHASE:ALIGN-BE:START traces=REQ-FIN-017,REQ-FIN-076 -->
## PHASE 8 — ALIGN-BE

The self-check of this plan. Each row names the `gov.py analyze` check that backs it, and its mark is that check's
result in the stage report (`_state/analyze-stage-P3.1.md`), copied rather than judged. A clause that examined nothing
is written as such, never ✓. The RESULT line is written by the orchestrator.

The one ✗: C7.12 flags the id of SEC's User entity, cited in the retired XM-FIN-002 block of INT-C that
`gov.py state` carries unchanged from v1. The citation is correct history. It fails to resolve because
SEC's current version (v2) has no artifacts yet, so SEC publishes no id at all to resolve it against.

```
ALIGN — FIN v2
row               backing check        assertion                                                                  mark
TRACEABILITY      traces               every PHASE/SUB/atom block carries traces=, and every API traces to its     ✓ (C7.2: 57 blocks · C7.3: 41 API)
                                       REQ and its DBF
COVERED           orphans              every REQ is covered by ≥1 API or DBF                                      ✓ (C7.6: 77 REQ)
BINDING (§2A)     value-agreement      every DBF names the same physical column here as the db-script declares   ✓ (C7.10)
MANIFEST (§4)     count-agrees         every total this plan states equals the rows it heads                      ✓ (C7.18: 4 totals)
WRITERS           required-writer      every required column is written by an endpoint, or the row states why    ✓ (C7.19: 82 columns)
QRC (§5)          orphans              every catalogued query is reached by ≥1 API                                ✓ (C7.22: 61 QR)
API (R3)          code-format          every catalog code is an instance of the declared format and carries a    ✓ (C7.11)
                                       status the platform can emit
RULE INPUTS       data-source          every RULE enforced at runtime names where the data it READS comes from,  ✓ (C6.9)
                                       or is deferred
CROSS-MODULE      registry-agree       every registered XM is placed here, and every XM minted here is           ✓ (C7.5, C7.5b: 2 XM)
                                       back-registered
FOREIGN IDS       xref-resolve         every id of another module cited here is defined in that module's own     ✗ (C7.12: 1)
                                       registry
BOOTSTRAP DATA    bootstrap-complete   every lookup key and every permission has a row naming who produces it    ✓ (C7.21: 45 names)
SECURITY (R7)     operation-resolves   every declared entity operation resolves to an API, and every marked      ✓ (C7.20: 65)
                                       matrix cell names its API and its permission
DEMAND (SRS)      operation-resolves   every operation an SRS screen names is built by an API, or the plan       ✓ (C7.23: 30)
                                       states why it is not
DECISIONS         refs-exist           every ADR this plan cites exists on disk in analysis/decisions/FIN/        ✓ (C7.13)
PATHS             paths-resolve        every path the generated manifest and execution state emit resolves to    ✓ (C7.14)
                                       something that exists
COVERAGE          (the report)         clauses the report lists as having examined nothing: none
RESULT            PASSED ✓ — 0 findings
```

**Coverage — ENT/DBF → phases → QR → XM.** Every ENT-FIN-001 … ENT-FIN-016 has exactly one DATA-DOM block and at least
one SVC-API endpoint; all 165 DBF ids have one manifest row; XM-FIN-001 binds the 22 lookup-backed columns.
| ENT | DBF | Endpoints | Queries | XM |
|---|---|---|---|---|
| ENT-FIN-001 | DBF-FIN-001..013, DBF-FIN-147 | API-FIN-001 … 004 | QR-FIN-001 … 006, QR-FIN-045, QR-FIN-060 | XM-FIN-001 |
| ENT-FIN-002 | DBF-FIN-014..022 | API-FIN-005, 006 | QR-FIN-007, QR-FIN-008, QR-FIN-046, QR-FIN-061 | — |
| ENT-FIN-003 | DBF-FIN-023..033 | API-FIN-007, 008, 035 | QR-FIN-009 … 011, QR-FIN-058, QR-FIN-061 | — |
| ENT-FIN-004 | DBF-FIN-034..050 | API-FIN-018 … 022 | QR-FIN-023 … 037 | XM-FIN-001 |
| ENT-FIN-005 | DBF-FIN-051..060 | API-FIN-019, 020, 022, 028 … 032 | QR-FIN-024, 025, 028 … 030, 034, 037, 042 … 044 | XM-FIN-001 |
| ENT-FIN-006 | DBF-FIN-061..064 | API-FIN-019, 020, 022, 032 | QR-FIN-024, 025, 032, 034, 037, 044 | — |
| ENT-FIN-007 | DBF-FIN-065..074 | API-FIN-023, 027 | QR-FIN-038, 041, 047, 048 | XM-FIN-001 |
| ENT-FIN-008 | DBF-FIN-075..088 | API-FIN-023 … 026, 033 | QR-FIN-038 … 040, 049, QR-FIN-059 | XM-FIN-001 |
| ENT-FIN-009 | DBF-FIN-089..097 | API-FIN-009, 010, 034 | QR-FIN-012 … 014, 027 | XM-FIN-001 |
| ENT-FIN-010 | DBF-FIN-098..108, DBF-FIN-148 | API-FIN-011 | QR-FIN-015, 016 | XM-FIN-001 |
| ENT-FIN-011 | DBF-FIN-109..121 | API-FIN-012 … 014, 036 | QR-FIN-017 … 019 | XM-FIN-001 |
| ENT-FIN-012 | DBF-FIN-122..129 | API-FIN-012, 013 | QR-FIN-018, 019 | XM-FIN-001 |
| ENT-FIN-013 | DBF-FIN-130..138 | API-FIN-015 … 017, 037 | QR-FIN-020 … 022 | — |
| ENT-FIN-014 | DBF-FIN-139..146 | API-FIN-015, 016 | QR-FIN-016, 021, 022 | XM-FIN-001 |
| ENT-FIN-015 | DBF-FIN-149..158 | API-FIN-038 … 041; read by API-FIN-020 | QR-FIN-050 … 055 | XM-FIN-001 |
| ENT-FIN-016 | DBF-FIN-159..165 | API-FIN-011; read by API-FIN-020 | QR-FIN-056, 057, 061 | XM-FIN-001 |

**Coverage — RULE → API → catalog code**: the Rule Registry of the Plan Index carries it, one row per RULE-FIN-001 …
RULE-FIN-028; every rule that raises a user-facing rejection has exactly one Error Catalog row per code.

**Coverage — XM → status → blocks → workaround**: XM-FIN-001 → ACTIVE → blocks nothing → no workaround; XM-FIN-002 →
RETIRED → blocks nothing → not applicable.
<!-- PHASE:ALIGN-BE:END -->
