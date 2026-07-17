# REGISTRY EXTRACT — registry-srs-FILE
══════════════════════════════════════════════════════════════════
Module          : File Service (FILE prefix)
Source artifact : srs.md
Extracted by    : P-REG (mechanical extraction — not a governance artifact)
Status          : SESSION INPUT ONLY — not loaded as Project Instruction,
                  not a Truth Layer artifact, not subject to P4 audit
══════════════════════════════════════════════════════════════════

## HEADER
| Field | Value |
|---|---|
| Module Name | File Service |
| Module Prefix | FILE |
| Feature Code | FILE-001 |
| OQ Count | 0 open (OQ-001 RESOLVED — MODE 1) |

## ENTITIES
| ENTITY-ID | Entity Name | Type |
|---|---|---|
| ENTITY-FILE-001 | FileDocument | SHARED (owner) |
| ENTITY-FILE-002 | FileCategory | SHARED (owner) |

## RULES
| RULE-ID | Short Title | Test-Hint |
|---|---|---|
| RULE-FILE-001 | File content size limit | تحقّق من رفض ملف > 5MB بـ 400؛ وتطبيق maxSizeBytesOverride عند وجوده |
| RULE-FILE-002 | Operation link (token) TTL expiry | طلب بعد انتهاء TTL → 401 |
| RULE-FILE-003 | Reject tampered/mismatched token | رمز صادر لـ download يُستخدَم على endpoint الحذف → 403؛ رمز مُركَّب يدوياً → 401 |
| RULE-FILE-004 | Single-use token | إعادة استخدام رمز رفع مُستهلَك سابقاً → رفض |
| RULE-FILE-005 | File type detection from content only | رفع ملف بترويسة Content-Type مزوَّرة → يُصنَّف حسب المحتوى الفعلي |
| RULE-FILE-006 | Deletion finality | — |
| RULE-FILE-007 | Delete restricted to owner or Admin | محاولة حذف ملف من entity/موديول مختلف عن المالك (بدون Admin) → 403 |

## LOVs
| LOV-ID | LOV Name |
|---|---|
| LOV-FILE-001 | FileType |
| LOV-FILE-002 | FileStatus |

## LIFECYCLE STATES
FileDocument: ACTIVE → ARCHIVED (archival) / ACTIVE or ARCHIVED → DELETED (final deletion, no return) — DELETED retains metadata/audit trail after binary content purge

## DEPENDENCIES
| Type | Target ENTITY-ID | Target Module | XM candidate |
|---|---|---|---|
| — | — | — | لا يوجد — File Service لا تستهلك أي كيان SHARED من موديول آخر |

## SCREENS
| SCR-ID | page_code | Screen Name | Pattern |
|---|---|---|---|
| SCR-FILE-001 | FILE_ATTACHMENT | لوحة إدارة المرفقات | PATTERN-2 — Inline / Modal |

## APIs
| API-ID | Method | Endpoint | Owning SCR-ID |
|---|---|---|---|
| API-FILE-001 | POST | /api/v1/files/upload-token | SCR-FILE-001 |
| API-FILE-002 | POST | /upload/{encryptedToken} | SCR-FILE-001 |
| API-FILE-003 | GET | /download/{encryptedToken} | SCR-FILE-001 |
| API-FILE-004 | DELETE | /{encryptedToken} | SCR-FILE-001 |
| API-FILE-005 | GET | /api/v1/files/{ownerId} | SCR-FILE-001 |

## PERMISSIONS
| PERM Name | Linked SCR-ID(s) |
|---|---|
| PERM_FILE_ATTACHMENT_VIEW/CREATE/UPDATE/DELETE | SCR-FILE-001 |

## OQ LOG STATUS
| OQ-ID | Status | One-line topic | Escalation |
|---|---|---|---|
| OQ-001 | RESOLVED | Impact of FileDocument deletion on HARD-FK consumer modules | LOCAL |
