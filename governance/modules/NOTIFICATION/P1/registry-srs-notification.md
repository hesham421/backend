# REGISTRY EXTRACT — registry-srs-NOTIF
══════════════════════════════════════════════════════════════════
Module          : Notification Service (NOTIF prefix)
Source artifact : srs.md
Extracted by    : P-REG (mechanical extraction — not a governance artifact)
Status          : SESSION INPUT ONLY — not loaded as Project Instruction,
                  not a Truth Layer artifact, not subject to P4 audit
══════════════════════════════════════════════════════════════════

## HEADER
| Field | Value |
|---|---|
| Module Name | Notification Service |
| Module Prefix | NOTIF |
| Feature Code | NOTIF-001 |
| OQ Count | 0 open |

## ENTITIES
| ENTITY-ID | Entity Name | Type |
|---|---|---|
| ENTITY-NOTIF-001 | NotificationLog | SHARED (owner) |
| ENTITY-NOTIF-002 | NotificationTemplate | PRIVATE (Phase 1) |
| ENTITY-NOTIF-003 | NotificationChannelConfig | PRIVATE (Configuration) |

## RULES
| RULE-ID | Short Title | Test-Hint |
|---|---|---|
| RULE-NOTIF-001 | Event contract completeness | نشر event بـ templateCode غير موجود → رسالة خطأ عربية؛ نشر event بدون channelHint → رفض |
| RULE-NOTIF-002 | No internal channel inference | لا جدول/إعداد داخل Notification يربط module_code بقناة معينة ضمنياً — تحقّق غيابه |
| RULE-NOTIF-003 | Per-channel fan-out independence | channelHint=['SMS','WHATSAPP'] وSMS معطَّلة → WhatsApp يُرسَل بنجاح، SMS يُسجَّل CHANNEL_DISABLED |
| RULE-NOTIF-004 | Retry policy on failure | محاكاة فشل SMTP → التحقق من retryCount يصل 5 قبل FAILED |
| RULE-NOTIF-005 | Disabled channel handling | تعطيل قناة SMS ثم نشر حدث بها → CHANNEL_DISABLED، لا استثناء للناشر |
| RULE-NOTIF-006 | Template bilingual requirement + fallback | طلب قالب برمز غير موجود → استخدام قالب افتراضي بدل فشل الإرسال |
| RULE-NOTIF-007 | Template code uniqueness + immutability | محاولة تعديل templateCode على قالب موجود → رفض |
| RULE-NOTIF-008 | Template storage strategy + retrieval fallback | بعد الترقية، انقطاع File Service مؤقت → استخدام templateBodyAr/En بدل فشل الإرسال |

## LOVs
| LOV-ID | LOV Name |
|---|---|
| LOV-NOTIF-001 | NotificationChannel |
| LOV-NOTIF-002 | NotificationStatus |

## LIFECYCLE STATES
NotificationLog: PENDING → SENT (final) / FAILED (final) / CHANNEL_DISABLED (final) — no return to PENDING from any final state

## DEPENDENCIES
| Type | Target ENTITY-ID | Target Module | XM candidate |
|---|---|---|---|
| HARD-FK (DEFERRED) | ENTITY-FILE-001 (FileDocument) | File Service | Yes → XM-NOTIF-[N] in MODE 1.5, activated at RXE-NOTIF-[SEQ] |
| HARD-FK | USERS (PERMANENT EXCEPTION, no governed ENTITY-ID) | Security | No — Security EXCEPTION |

## SCREENS
| SCR-ID | page_code | Screen Name | Pattern |
|---|---|---|---|
| SCR-NOTIF-001 | NOTIFICATION_INBOX | لوحة إشعاراتي (Notification Bell + History) | PATTERN-3 — Specialized |
| SCR-NOTIF-002 | NOTIFICATION_TEMPLATE | إدارة قوالب الإشعارات | PATTERN-1 — Search + Entry |
| SCR-NOTIF-003 | NOTIFICATION_CHANNEL_CONFIG | إعدادات قنوات الإشعار | PATTERN-2 — Inline / Modal |

## APIs
| API-ID | Method | Endpoint | Owning SCR-ID |
|---|---|---|---|
| API-NOTIF-001 | POST | /api/v1/notifications/send | SCR-NOTIF-001 |
| API-NOTIF-002 | POST | /api/v1/notifications/schedule | SCR-NOTIF-001 |
| API-NOTIF-003 | GET | /api/v1/notifications/history | SCR-NOTIF-001 |
| API-NOTIF-004 | GET | /api/v1/notifications/unread | SCR-NOTIF-001 |
| API-NOTIF-005 | PUT | /api/v1/notifications/{id}/read | SCR-NOTIF-001 |
| API-NOTIF-006 | GET | /api/v1/notifications/templates | SCR-NOTIF-002 |
| API-NOTIF-007 | POST | /api/v1/notifications/templates | SCR-NOTIF-002 |
| API-NOTIF-008 | PUT | /api/v1/notifications/templates/{id} | SCR-NOTIF-002 |
| API-NOTIF-009 | PUT | /api/v1/notifications/templates/{id}/deactivate | SCR-NOTIF-002 |
| API-NOTIF-010 | GET | /api/v1/notifications/templates/{id} | SCR-NOTIF-002 |
| API-NOTIF-011 | GET | /api/v1/notifications/channel-configs | SCR-NOTIF-003 |
| API-NOTIF-012 | PUT | /api/v1/notifications/channel-configs/{id} | SCR-NOTIF-003 |

## PERMISSIONS
| PERM Name | Linked SCR-ID(s) |
|---|---|
| PERM_NOTIFICATION_INBOX_VIEW/CREATE/UPDATE/DELETE | SCR-NOTIF-001 |
| PERM_NOTIFICATION_TEMPLATE_VIEW/CREATE/UPDATE/DELETE | SCR-NOTIF-002 |
| PERM_NOTIFICATION_CHANNEL_CONFIG_VIEW/CREATE/UPDATE/DELETE | SCR-NOTIF-003 |

## OQ LOG STATUS
| OQ-ID | Status | One-line topic | Escalation |
|---|---|---|---|
| — | — | None | — |
