# REGISTRY EXTRACT — registry-srs-NOTIF
══════════════════════════════════════════════════════════════════
Module          : Notification Service (NOTIF)
Source artifact : srs-NOTIF.md (v1.2)
Extracted by    : P-REG (mechanical extraction — not a governance artifact)
Status          : SESSION INPUT ONLY — not loaded as Project Instruction,
                  not a Truth Layer artifact, not subject to P4.1/P4.2 audit
══════════════════════════════════════════════════════════════════

## HEADER
Module name : Notification Service (خدمة الإشعارات)
Module Prefix : NOTIF
OQ count : 1 (OQ-NOTIF-001 — RESOLVED)

## ENTITIES (PART A — A3)
| ENTITY-ID | Entity Name | Type |
|---|---|---|
| ENTITY-NOTIF-001 | NotificationLog | PRIVATE |
| ENTITY-NOTIF-002 | NotificationTemplate | PRIVATE |
| ENTITY-NOTIF-003 | NotificationChannelConfig | PRIVATE |

## RULES (PART A — A4)
| RULE-ID | Short Title | Test-Hint |
|---|---|---|
| RULE-NOTIF-001 | Fan out one log per channel | — |
| RULE-NOTIF-002 | Retry ≤5 then FAILED | — |
| RULE-NOTIF-003 | Disabled channel, no retry | — |
| RULE-NOTIF-004 | Bilingual templates required | — |
| RULE-NOTIF-005 | Delegate auth to Security filter | — |
| RULE-NOTIF-006 | Unique template/channel codes | — |
| RULE-NOTIF-007 | No dispatch to inactive recipient | — |

## LOVs (PART A — A5)
| LOV-ID | LOV Name |
|---|---|
| LOV-NOTIF-001 | NotificationChannel (NOTIF_CHANNEL) |
| LOV-NOTIF-002 | NotificationStatus (NOTIF_STATUS) |

## LIFECYCLE STATES (PART A — A6)
NotificationLog: PENDING → SENT | PENDING → FAILED (after retries) | PENDING → CHANNEL_DISABLED

## DEPENDENCIES (PART A — A7)
| Type | Target ENTITY-ID | Target Module | XM candidate |
|---|---|---|---|
| SOFT-READ | ENTITY-SEC-001 (UserAccount) | SEC | Yes |
| SOFT/service | FileDocument (via FileService, file_id) | FILE | Yes |
Note: CU (Events/config/exceptions) is USES (library) — not a governed dependency type.

## SCREENS (PART B)
| SCR-ID | page_code | Screen Name | Pattern |
|---|---|---|---|
| SCR-NOTIF-001 | NOTIF_TEMPLATES | Notification Templates | PATTERN-2 (SIDE_DRAWER) |
| SCR-NOTIF-002 | NOTIF_CHANNELS | Channel Configuration | PATTERN-2 (SIDE_DRAWER) |
| SCR-NOTIF-003 | NOTIF_LOG | Notification Log (read-only) | PATTERN-2 (SIDE_DRAWER) |

## APIs (PART B — B5)
| API-ID | Method | Endpoint | Owning SCR-ID |
|---|---|---|---|
| API-NOTIF-001 | POST | /api/v1/notifications/dispatch | — (event/service endpoint) |
| API-NOTIF-002 | GET | /api/v1/notifications/logs | SCR-NOTIF-003 |
| API-NOTIF-003 | GET | /api/v1/notifications/logs/{id} | SCR-NOTIF-003 |
| API-NOTIF-004 | POST/GET/PUT/DELETE | /api/v1/notifications/templates | SCR-NOTIF-001 |
| API-NOTIF-005 | POST/GET/PUT/DELETE | /api/v1/notifications/channels | SCR-NOTIF-002 |
| API-NOTIF-006 | GET | /api/v1/notifications/lookups/{lookupKey} | (cross-screen) |

## PERMISSIONS (Permissions Summary)
| PERM Name | Linked SCR-ID(s) |
|---|---|
| PERM_NOTIF_TEMPLATES_{VIEW,CREATE,UPDATE,DELETE} | SCR-NOTIF-001 |
| PERM_NOTIF_CHANNELS_{VIEW,CREATE,UPDATE,DELETE} | SCR-NOTIF-002 |
| PERM_NOTIF_LOG_VIEW (VIEW only) | SCR-NOTIF-003 |
All granted to NOTIF_ADMIN per srs-NOTIF Permissions Summary.

## OQ LOG STATUS
| OQ-ID | Status | One-line topic | Escalation |
|---|---|---|---|
| OQ-NOTIF-001 | RESOLVED | Actual provider per channel (SMS/WhatsApp/Push) | P3-TECH |

---
*End of registry-srs-NOTIF.md*
