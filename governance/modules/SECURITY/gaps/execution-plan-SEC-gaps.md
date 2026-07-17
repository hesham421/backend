# EXECUTION PLAN GOVERNANCE ENGINE — MODE 2 OUTPUT
## Module: Security (SEC) — Scope: GAP ITEMS ONLY (per srs-security-gaps.md)

```
Plan ID       : PLAN-SEC-002
Feature Code  : SEC-002 (DataScope Extension + Self-Service Auth)
Task Type     : 🆕 New Feature (12 new APIs, 4 new tables/entities, 1 altered
                column, JWT claim extension) — no existing SEC-001 phases reopened
Execution Plan Name : New Feature — DataScope (User Profile / Role-Branch) +
                       Self-Service Auth (Sign Up / Forgot Password) — Security Module
Consumes      : srs-security-full.md (v2.0, AUTHORITATIVE) +
                srs-security-gaps.md (delta, this session's governance decisions) +
                db-script-SEC-gaps.md (DBS-SEC-001, CONDITIONAL) +
                security-registry.md (v2.4.1, AS-IS runtime reference) +
                master-registry.md (v2.7.7)
Date          : 2026-07-09
```

---

## 0. MODE 2 ENTRY GATE

```
╔══════════════════════════════════════════════════════════════════╗
║                  MODE 2 — ENTRY GATE                             ║
╠════════════════════════════════╦═════════════════════════════════╣
║ SRS attached + feature code?    ║ Yes — srs-security-full.md +     ║
║                                  ║ srs-security-gaps.md (SEC-002)   ║
║ DB Script attached (DBS-ID)?    ║ Yes — DBS-SEC-001                ║
║ Gate DB passed?                 ║ ⚠ CONDITIONAL — technically      ║
║                                  ║ complete, EXECUTION BLOCKED       ║
║                                  ║ pending Conflict #19 sign-off     ║
║                                  ║ (carried forward per RULE-4 —     ║
║                                  ║ NOT a STOP for planning; IS a     ║
║                                  ║ STOP for MODE 3 agent execution)  ║
║ Entities in SRS = tables in DB? ║ 4/4 — ENTITY-SEC-009..012 =       ║
║                                  ║ SEC_USER_PROFILE, SEC_ROLE_BRANCH,║
║                                  ║ PASSWORD_RESET_TOKEN,             ║
║                                  ║ ACCOUNT_ACTIVATION_TOKEN ✓        ║
║ Registry loaded, no conflicts?  ║ ✗ — Conflict #19 OPEN (Security   ║
║                                  ║ "no code changes ever" exception  ║
║                                  ║ not yet extended a third time);   ║
║                                  ║ Conflict #20/BLK-SEC-002 CLOSED   ║
║                                  ║ this session (event-based fix)    ║
║ Naming: 3-way consistent?       ║ ✓ — SEC is an EXCEPTION module;   ║
║                                  ║ AS-IS column names used           ║
║                                  ║ throughout (no Pk/Fk/Fl           ║
║                                  ║ convention enforcement)           ║
╠════════════════════════════════╩═════════════════════════════════╣
║ P0 ARTIFACT CHECK:                                                 ║
║ module-registry-SEC.md loaded?  ║ Not uploaded — proceeding using  ║
║                                  ║ master-registry.md Section 4/15  ║
║                                  ║ directly (same posture as P2)    ║
║ EXCEPTION modules detected?     ║ Security (this module) — PERMANENT║
║                                  ║ EXCEPTION, AS-IS naming           ║
╠══════════════════════════════════════════════════════════════════╣
║ Extracted: 4 entities, 32 fields, 12 APIs, 12 rules (RULE-SEC-030 ║
║ ..041), 1 LOV, 2 XM-IDs (outbound, READY), 5 open OQs             ║
╠══════════════════════════════════════════════════════════════════╣
║ PROCEED? YES — produce execution-plan.md as a governed PLANNING   ║
║ ARTIFACT (Layer 3). This is NOT authorization to begin MODE 3      ║
║ (agent code execution) — see GOVERNANCE EXCEPTION box below.       ║
╚══════════════════════════════════════════════════════════════════╝
```

## ⚠ GOVERNANCE EXCEPTION — CARRIED FORWARD FROM DB LAYER — MUST READ FIRST

```
Per RULE-4 (Conflict Resolution Protocol) and CORE-1 (Truth Layer
Authority), this engine does not resolve Conflict #19 on the human
architecture authority's behalf, and does not silently treat a
CONDITIONAL Layer-2 (DB) gate as PASSED.

STATUS CARRIED FORWARD UNCHANGED FROM db-script-SEC-gaps.md:
  Conflict #20 / BLK-SEC-002  → CLOSED this session (event-based
                                 PasswordResetRequested /
                                 AccountActivationRequested integration
                                 removes the NotificationService
                                 build-cycle dependency)
  Conflict #19                → REMAINS OPEN. Security is a PERMANENT
                                 EXCEPTION module ("no code changes
                                 ever"), amended exactly twice to date
                                 (Conflict #17 — TENANT_ID removal;
                                 Conflict #18 — PK/FK renames). This
                                 gap package (4 new tables, 12 new
                                 APIs, 1 altered column on USERS,
                                 1 new JWT claim) is a THIRD, larger
                                 amendment and has NOT received
                                 architect sign-off.

GOVERNANCE POSITION TAKEN BY THIS ENGINE (P3):
  This execution-plan.md is produced as the EXECUTION TRUTH PLANNING
  ARTIFACT (Layer 3) — a complete, agent-ready specification — so
  that no time is lost once Conflict #19 is signed off. It is NOT
  authorization for MODE 3 (Claude Code / implementation agent) to
  write or run any code against the Security module.

  Gate Status for this plan: CONDITIONAL — PLAN COMPLETE, MODE 3
  EXECUTION BLOCKED pending Conflict #19 architecture-authority
  sign-off (same mechanism as Conflict #17/#18 — see
  master-registry.md Section 13).

  Additionally carried forward, non-blocking for planning:
    OQ-004 (PREFERRED_LANG domain undefined — defaulted VARCHAR(10) NULL)
    OQ-005 (EMPLOYEE_ID_FK — no HR module governed yet, unconstrained)
    OQ-001 (User/Admin login tab — undocumented, SCR-SEC-005, not in
            this gap package's build scope — UI element pre-exists)
    OQ-002 (Social login buttons — undocumented, likely template
            boilerplate — not in this gap package's build scope)
```

---

## 1. PRE-GENERATION EXTRACTION TABLE

### 1.1 Entities (from srs-security-full.md, ENTITY-IDs — RECEIVED, not invented)

| ENTITY-ID | Entity Name | Table (AS-IS, EXCEPTION naming) | Type |
|---|---|---|---|
| ENTITY-SEC-009 | SEC_USER_PROFILE (ملف بيانات المستخدم) | SEC_USER_PROFILE | PRIVATE (1:1 extension of USERS) |
| ENTITY-SEC-010 | SEC_ROLE_BRANCH (نطاق فروع الدور) | SEC_ROLE_BRANCH | PRIVATE (join: ROLES × ORG_BRANCH) |
| ENTITY-SEC-011 | PASSWORD_RESET_TOKEN | PASSWORD_RESET_TOKEN | PRIVATE (infrastructure) |
| ENTITY-SEC-012 | ACCOUNT_ACTIVATION_TOKEN | ACCOUNT_ACTIVATION_TOKEN | PRIVATE (infrastructure) |

No new Business Code is required for any of the four entities — none is a
document/master record with a human-facing code; all four are either a
1:1 profile extension, a join table, or a token table (source: SRS — no
Business Code field present in ENTITY-SEC-009..012 field lists).

### 1.2 LOVs

| LOV-ID | LOOKUP_KEY | Values (code / AR / EN) | Usage |
|---|---|---|---|
| LOV-SEC-002 | DATA_ACCESS_LEVEL | BRANCH_ONLY / الفرع فقط / Branch Only — BRANCH_AND_CHILDREN / الفرع وفروعه التابعة / Branch and Children — ALL / كل الفروع / All | SEC_ROLE_BRANCH.DATA_ACCESS_LEVEL |

Seeded via MD_MASTER_LOOKUP/MD_LOOKUP_DETAIL (BLOCK 8 of db-script-SEC-gaps.md) — MasterData Lookup is itself a PERMANENT EXCEPTION feature; no schema change needed there, seed rows only.

### 1.3 Rules (full extraction — RULE-SEC-030..041, RECEIVED from srs-security-full.md)

| RULE-ID | Scope | Statement | Message-AR | Client Policy? |
|---|---|---|---|---|
| RULE-SEC-030 | ENTITY-SEC-001 | Self-registered accounts MUST be created with ENABLED=0 | يجب أن يُنشئ النظام الحسابات المسجَّلة ذاتياً بحالة غير مفعَّلة | Yes |
| RULE-SEC-031 | ENTITY-SEC-011/012 | MUST publish a domain event (PasswordResetRequested / AccountActivationRequested) instead of calling NotificationService directly | يجب أن يُصدر النظام حدثاً بدلاً من استدعاء خدمة الإشعارات مباشرة | Yes — event-based integration |
| RULE-SEC-032 | ENTITY-SEC-001/012 | MUST require a valid, unused, non-expired ACCOUNT_ACTIVATION_TOKEN before flipping USERS.ENABLED to 1 | يجب ألا يُفعَّل الحساب إلا برمز تفعيل صالح وغير مستخدَم وغير منتهي | — |
| RULE-SEC-033 | ENTITY-SEC-011/012 | MUST reject an already-used/expired token; MUST mark it used immediately on success | يجب أن يرفض النظام رمزاً مستخدَماً أو منتهياً، ويُعلِّمه كمستخدَم فور نجاح الاستخدام | — |
| RULE-SEC-034 | ENTITY-SEC-009 | MUST require an active ORG_BRANCH reference (BRANCH_ID_FK) for every SEC_USER_PROFILE record | يجب أن يتضمَّن كل ملف بيانات مستخدم فرعاً فعّالاً مرتبطاً به | — |
| RULE-SEC-035 | ENTITY-SEC-010 | MUST require DATA_ACCESS_LEVEL for every SEC_ROLE_BRANCH record | يجب تحديد مستوى نطاق الوصول للبيانات عند كل إسناد فرع لدور | — |
| RULE-SEC-036 | ENTITY-SEC-010 | MUST prevent duplicate (ROLE_ID_FK, BRANCH_ID_FK) combinations | يجب أن يمنع النظام تكرار إسناد نفس الفرع لنفس الدور | — |
| RULE-SEC-037 | ENTITY-SEC-001/010 | MUST populate `allowedBranches[]` claim in the access token, derived from active SEC_ROLE_BRANCH assignments | يجب أن يتضمَّن رمز الدخول قائمة الفروع المسموحة | — |
| RULE-SEC-038 | ENTITY-SEC-001/011 | MUST respond with a generic success message on Forgot Password regardless of EMAIL existence (anti-enumeration) | يجب أن يُظهر النظام رسالة نجاح عامة بغضّ النظر عن وجود البريد | ERP-DEFAULT |
| RULE-SEC-039 | ENTITY-SEC-011 | MUST invalidate any prior unexpired PASSWORD_RESET_TOKEN for the same user when issuing a new one | يجب أن يُبطل النظام أي رمز استعادة سابق غير منتهٍ | ERP-DEFAULT |
| RULE-SEC-040 | ENTITY-SEC-001 | Sign Up MUST enforce the same global username-uniqueness as RULE-SEC-006 | يجب أن يخضع التسجيل الذاتي لنفس قاعدة منع تكرار اسم المستخدم | — |
| RULE-SEC-041 | ENTITY-SEC-001 | MUST enforce global uniqueness on USERS.EMAIL | يجب أن يفرض النظام تفرد البريد الإلكتروني على مستوى النظام | Yes (AQ-008) |

### 1.4 FIELD-ID Register (DB Alignment Manifest — FIELD-ID ⟷ DBF-ID, 1:1 binding)

EXCEPTION MODULE NOTE: Security uses AS-IS column names (no Pk/Fk/Fl/Id
convention enforcement) — Java field names mirror the DB column names in
lowerCamelCase per existing Security entity convention (e.g. `USER_ID_FK`
→ `userIdFk`, consistent with USER_ID_FK / ROLE_ID_FK / PAGE_ID_FK already
in production per master-registry.md Section 4).

| FIELD-ID | DBF-ID | Java Field | Plan Type | FK / XM-ID | Match |
|---|---|---|---|---|---|
| FIELD-SEC-0001 | DBF-0001 | userIdFk | Long | → USERS (shared PK) | ✓ |
| FIELD-SEC-0002 | DBF-0002 | branchIdFk | Long | XM-SEC-001 | ✓ |
| FIELD-SEC-0003 | DBF-0003 | fullNameAr | String(200) | — | ✓ |
| FIELD-SEC-0004 | DBF-0004 | fullNameEn | String(100) | — | ✓ |
| FIELD-SEC-0005 | DBF-0005 | preferredLang | String(10), nullable | — | ✓ (OQ-004 open) |
| FIELD-SEC-0006 | DBF-0006 | employeeIdFk | Long, nullable, unconstrained | — | ✓ (OQ-005 open, no XM-ID) |
| FIELD-SEC-0007 | DBF-0007 | isActiveFl | Boolean | — | ✓ |
| FIELD-SEC-0008..0011 | DBF-0008..0011 | createdBy/createdAt/updatedBy/updatedAt | audit std | — | ✓ |
| FIELD-SEC-0012 | DBF-0012 | roleIdFk | Long | composite PK part 1 | ✓ |
| FIELD-SEC-0013 | DBF-0013 | branchIdFk | Long | XM-SEC-002, composite PK part 2 | ✓ |
| FIELD-SEC-0014 | DBF-0014 | dataAccessLevel | String(30), LOV-SEC-002 | — | ✓ |
| FIELD-SEC-0015 | DBF-0015 | isActiveFl | Boolean | — | ✓ |
| FIELD-SEC-0016..0019 | DBF-0016..0019 | audit std | audit std | — | ✓ |
| FIELD-SEC-0020 | DBF-0020 | tokenPk | Long | PK | ✓ |
| FIELD-SEC-0021 | DBF-0021 | token | String(64) | unique, indexed | ✓ |
| FIELD-SEC-0022 | DBF-0022 | userIdFk | Long | → USERS | ✓ |
| FIELD-SEC-0023 | DBF-0023 | createdAt | LocalDateTime | — | ✓ |
| FIELD-SEC-0024 | DBF-0024 | expiresAt | LocalDateTime | — | ✓ |
| FIELD-SEC-0025 | DBF-0025 | usedFl | Boolean | — | ✓ |
| FIELD-SEC-0026 | DBF-0026 | tokenPk | Long | PK | ✓ |
| FIELD-SEC-0027 | DBF-0027 | token | String(64) | unique, indexed | ✓ |
| FIELD-SEC-0028 | DBF-0028 | userIdFk | Long | → USERS | ✓ |
| FIELD-SEC-0029 | DBF-0029 | createdAt | LocalDateTime | — | ✓ |
| FIELD-SEC-0030 | DBF-0030 | expiresAt | LocalDateTime | — | ✓ |
| FIELD-SEC-0031 | DBF-0031 | usedFl | Boolean | — | ✓ |
| FIELD-SEC-0032 | DBF-0032 | email (existing) | String(150) | UK_USERS_EMAIL added | ✓ (blocked — Conflict #19) |

Total: 32 FIELD-IDs, all DBF-bound, all Match Status ✓ (structurally); FIELD-SEC-0032 is execution-blocked pending Conflict #19.

---

## 2. PHASE CORE

```
Module          : Security (SEC)
Screens in scope: SCR-SEC-006 (new), SCR-SEC-002 (extended — Branch Scope
                   sub-tab), SCR-SEC-008 (new), SCR-SEC-009 (new)
Entity base     : AuditableEntity (PROJECT-STANDARD — uniform, no
                   TenantAuditableEntity — consistent with Conflict #17)
Error signaling : LocalizedException — NotFoundException BANNED
Audit fields    : Populated via AuditEntityListener — never set in
                   Mapper or Service
Optimistic lock : NOT used (project standard — no VERSION column added)
Deactivation    : isActiveFl = false (NOT physical delete) for
                   SEC_USER_PROFILE and SEC_ROLE_BRANCH; token tables
                   use usedFl / natural expiry, never soft-deleted
Search/Pagination: BaseSearchContractRequest + ALLOWED_SORT_FIELDS
                   (userIdFk, branchIdFk, isActiveFl for user-profiles;
                   roleIdFk, branchIdFk, dataAccessLevel for role-branches)
```

---

## 3. PHASE DATA+DOM — Domain Model Specifications

```
SEC_USER_PROFILE (ENTITY-SEC-009)
  — 1:1 extension of USERS via userIdFk (shared PK pattern: the FK IS
    the PK — @MapsId against USERS, consistent with how SEC treats
    other 1:1 extensions). Do NOT invent a separate surrogate PK;
    db-script-SEC-gaps.md's DBF-0001 has no separate PK column listed.
  — branchIdFk → ORG_BRANCH (XM-SEC-001, HARD-FK, READY) — RULE-SEC-034
    enforced at Service layer: reject if referenced ORG_BRANCH.isActiveFl
    = false (LocalizedException, not NotFoundException, per project
    standard) → ERR-SEC-1034.
  — employeeIdFk: unconstrained BIGINT, nullable, NO @JoinColumn FK
    annotation (OQ-005 — no HR entity exists to bind to). Store as a
    plain Long field with a code comment referencing OQ-005; do NOT
    fabricate an Employee JPA relationship.
  — preferredLang: nullable String(10), no LOV binding yet (OQ-004).

SEC_ROLE_BRANCH (ENTITY-SEC-010)
  — Composite key: (roleIdFk, branchIdFk) — @EmbeddedId or
    @IdClass, consistent with existing join-table pattern
    (USER_ROLES / ROLE_PERMISSIONS use composite keys per
    security-registry.md §7).
  — dataAccessLevel: String(30), validated against LOV-SEC-002 codes
    (BRANCH_ONLY / BRANCH_AND_CHILDREN / ALL) at Service layer via
    MasterData Lookup service (PERMANENT EXCEPTION feature, AS-IS).
  — RULE-SEC-036 (no duplicate role+branch): enforced by the composite
    PK itself at DB level (UNIQUE by construction) — Service layer
    additionally pre-checks for a clean LocalizedException message
    (ERR-SEC-1036) rather than relying on a raw constraint-violation
    stack trace reaching the client.

PASSWORD_RESET_TOKEN / ACCOUNT_ACTIVATION_TOKEN (ENTITY-SEC-011/012)
  — Both infrastructure tables, no soft-delete — usedFl flips true on
    consumption (RULE-SEC-033); no isActiveFl (out of scope per SRS
    field list).
  — token column: opaque, cryptographically random 64-char string
    (generation mechanism is an agent implementation detail — not
    specified by SRS; use existing project token-generation utility if
    one exists in erp-common-utils, else a SecureRandom-backed
    generator — no invented library name).

JWT allowedBranches[] claim (RULE-SEC-037)
  — Extends existing JwtService / JwtProperties (security-registry.md
    §5.3) — access token claims currently: sub, authorities[], userId.
    ADD: allowedBranches[] — array of branchIdFk values derived from
    the user's active (isActiveFl=true) SEC_ROLE_BRANCH rows across all
    of the user's active roles.
  — Special case: if any active SEC_ROLE_BRANCH row for the user has
    dataAccessLevel = ALL, the claim MAY be represented as a sentinel
    (e.g. a single "ALL" marker) rather than enumerating every branch —
    this is an agent-level optimization decision, not an SRS mandate;
    document the chosen representation in the Derivation Log (Section
    9) when MODE 3 begins.
  — No SRS rule requires refresh-token claim changes — refresh token
    keeps sub + jti only (security-registry.md §5.3), unchanged.
```

---

## 4. PHASE SVC+API — API Contracts + Error Catalog

### 4.1 API Register (API-SEC-032..043 — RECEIVED SCR-ID/RULE-ID bindings from srs-security-full.md)

| API-ID | Method | Endpoint | Owning SCR-ID | RULE-IDs |
|---|---|---|---|---|
| API-SEC-032 | POST | /api/v1/security/user-profiles | SCR-SEC-006 | RULE-SEC-034 |
| API-SEC-033 | GET | /api/v1/security/user-profiles | SCR-SEC-006 | — |
| API-SEC-034 | PUT | /api/v1/security/user-profiles/{id} | SCR-SEC-006 | RULE-SEC-034 |
| API-SEC-035 | GET | /api/v1/security/user-profiles/{id} | SCR-SEC-006 | — |
| API-SEC-036 | POST | /api/v1/security/role-branches | SCR-SEC-002 (sub-tab) | RULE-SEC-035, RULE-SEC-036 |
| API-SEC-037 | GET | /api/v1/security/role-branches | SCR-SEC-002 (sub-tab) | — |
| API-SEC-038 | PUT | /api/v1/security/role-branches/{id} | SCR-SEC-002 (sub-tab) | RULE-SEC-035 |
| API-SEC-039 | DELETE | /api/v1/security/role-branches/{id} | SCR-SEC-002 (sub-tab) | — |
| API-SEC-040 | POST | /api/auth/signup | SCR-SEC-008 | RULE-SEC-030, RULE-SEC-031, RULE-SEC-040, RULE-SEC-041, RULE-SEC-029 |
| API-SEC-041 | POST | /api/auth/signup/activate | SCR-SEC-008 | RULE-SEC-032, RULE-SEC-033 |
| API-SEC-042 | POST | /api/auth/forgot-password | SCR-SEC-009 | RULE-SEC-031, RULE-SEC-038, RULE-SEC-039 |
| API-SEC-043 | POST | /api/auth/reset-password | SCR-SEC-009 | RULE-SEC-032, RULE-SEC-033 |

```
CONTRACT NOTES (API CONTRACT COMPLETENESS — Section 8.3 of this engine):
- API-SEC-040/041/042/043 sit under /api/auth/** — already a PUBLIC
  endpoint prefix per security-registry.md §5.2 (no auth required) and
  already covered generically by LoginRateLimitFilter's path matching
  for /api/auth/signup (§5.8 — "no code change needed there"). Confirm
  /api/auth/forgot-password and /api/auth/reset-password are added to
  the SAME rate-limit key pattern (ip|identifier) — this is a NEW
  filter-config item not yet covered by the existing generic match,
  since LoginRateLimitFilter today only path-matches login + signup.
  → Flagged as a required SEC-phase task (Section 6 below), not a
  DB or SRS gap — implementation-detail addition to an existing filter.
- API-SEC-032..039 sit under /api/v1/security/** (authenticated,
  permission-gated — see Section 6 Permissions Matrix).
- ERR-carry rule: every RULE-ID above must have exactly one bound
  ERR-ID (Section 4.2). API-SEC-033/035/037 (GET/search/fetch) carry
  no RULE-ID → no dedicated ERR-ID (standard 200/404 platform handling
  applies; 404 via LocalizedException per project standard, NEVER
  NotFoundException).
```

### 4.2 Error Catalog (Arabic + English — new ERR-IDs, this module)

| ERR-ID | Source RULE-ID | HTTP Status | Message-AR | Message-EN |
|---|---|---|---|---|
| ERR-SEC-1030 | RULE-SEC-030 | — (internal state, not user-facing) | — | — |
| ERR-SEC-1032 | RULE-SEC-032 | 400 | رمز التفعيل غير صالح أو منتهي الصلاحية | Activation token is invalid or expired |
| ERR-SEC-1033 | RULE-SEC-033 | 400 | رمز التفعيل مستخدَم مسبقاً | Activation/reset token already used |
| ERR-SEC-1034 | RULE-SEC-034 | 400 | يجب اختيار فرع فعّال لملف المستخدم | An active branch is required for the user profile |
| ERR-SEC-1035 | RULE-SEC-035 | 400 | يجب تحديد مستوى نطاق الوصول للبيانات | Data access level is required |
| ERR-SEC-1036 | RULE-SEC-036 | 409 | هذا الفرع مُسنَد بالفعل لهذا الدور | This branch is already assigned to this role |
| ERR-SEC-1039 | RULE-SEC-039 | — (internal, silent invalidation) | — | — |
| ERR-SEC-1040 | RULE-SEC-040 | 409 | اسم المستخدم مستخدَم بالفعل | Username already in use |
| ERR-SEC-1041 | RULE-SEC-041 | 409 | البريد الإلكتروني مستخدَم بالفعل | Email address already in use |
| ERR-SEC-1043 | RULE-SEC-033 (reset variant) | 400 | رمز الاستعادة غير صالح أو منتهي الصلاحية | Reset token is invalid or expired |

```
Note: RULE-SEC-038 (anti-enumeration) deliberately has NO ERR-ID — its
entire point is that /api/auth/forgot-password returns HTTP 200 with a
generic success body regardless of outcome; raising a distinguishable
error would violate the rule itself.
```

---

## 5. PHASE DOC — Contract Documentation

```
- Swagger/OpenAPI groups: add "Security — DataScope" (API-SEC-032..039)
  and extend existing "Security — Auth" group (API-SEC-040..043) —
  group names are an agent/documentation-tool decision, not SRS-mandated.
- DOC ✓ gate condition: every API-ID above has a request/response schema
  bound to its FIELD-ID list (Section 1.4) — no additional fields, no
  omitted fields.
```

---

## 6. PHASE INT-C / INT-R — Cross-Module Contracts

### 6.1 Outbound XM Register (RECEIVED from db-script-SEC-gaps.md — P3 extends status only, never assigns new XM-IDs)

| XM-ID | Type | This Table | Target Table | Target Module | Status | INT-C Contract |
|---|---|---|---|---|---|---|
| XM-SEC-001 | HARD-FK | SEC_USER_PROFILE.branchIdFk | ORG_BRANCH | Organization | READY | Consume ORG_BRANCH via existing `/api/v1/org/branches/{id}` (GET, API-ORG-012) for active-branch validation (RULE-SEC-034); FK enforced physically at DB per db-script-SEC-gaps.md BLOCK 5e |
| XM-SEC-002 | HARD-FK | SEC_ROLE_BRANCH.branchIdFk | ORG_BRANCH | Organization | READY | Same contract as XM-SEC-001; branch dropdown in F1 sources active branches via API-ORG-008 (search) |

```
Both XM-IDs are READY (Organization module GOVERNED ✓, DBS-ORG-001) —
no INT-R deferral or workaround required.

Registry gap (informational, non-blocking): registry-exec-ORG.md's XM
Inbound Stub list (registry-exec-ORG.md, INT SUMMARY section) does not
list Security as an anticipated consumer of Branch, despite this
HARD-FK now existing. This is a P2(ORG)-side traceability gap, already
flagged in master-registry.md Section 13 — recommend the Registry
Maintainer add Security to Organization's XM Inbound Stub list. Not a
blocker for Security's own execution plan.
```

### 6.2 Event-Based Integration (Conflict #20 resolution — RULE-SEC-031)

```
Replaces a rejected HARD-FK design (Security → NotificationService)
that would have created a build-cycle conflict with Section 7 of
master-registry.md (NotificationService already depends on
Organization/Security/FileService).

Pattern    : Security PUBLISHES a domain event; NotificationService
             SUBSCRIBES and sends the email. No direct service call,
             no compile-time dependency from Security → Notification.
Events     : PasswordResetRequested (payload: userIdFk, token, expiresAt)
             AccountActivationRequested (payload: userIdFk, token, expiresAt)
Emitted by : API-SEC-040 (signup) → AccountActivationRequested
             API-SEC-042 (forgot-password) → PasswordResetRequested
Mechanism  : Use the project's existing event-publishing mechanism if
             one exists elsewhere in the codebase (e.g. Spring
             ApplicationEventPublisher, or a message-broker abstraction
             already wired for AuditService/IntegrationService); this
             engine does NOT invent a new messaging library — agent
             confirms the existing pattern during MODE 3 and records
             the choice in the Derivation Log.
Status     : Conflict #20 CLOSED — no XM-ID needed (no FK/table
             dependency — event contract only, not a Truth Layer
             cross-module dependency in the XM sense). BLK-SEC-002
             CLOSED as a consequence — Sign Up / Forgot Password are no
             longer build-blocked on NotificationService's own delivery
             timeline.
```

---

## 7. PHASE F1/F2/F3 — Screens, Facades, Validators

### 7.1 F1 — Screen Specifications

| SCR-ID | Screen Name | Pattern | Route Guard (PERM) | Notes |
|---|---|---|---|---|
| SCR-SEC-006 | ملفات المستخدمين / نطاق البيانات (User Profiles) | PATTERN-1 — Search + Entry | PERM_USER_PROFILE_VIEW (+CREATE/UPDATE) | New sidebar item under "الأمان" group, per srs-security-gaps.md §3.1; also add a 6th "وصول سريع" quick-access card |
| SCR-SEC-002 (extended) | إدارة الأدوار والصلاحيات — Branch Scope sub-tab | Composite (existing SCR-ID, CORE-9 — no new SCR-ID/SEC_PAGES row for the sub-tab) | PERM_ROLE_VIEW (+UPDATE for branch assignment) | Sub-tab, NOT a new sidebar item — per srs-security-gaps.md §3.1 recommendation and CORE-9 (no sub-screen gets its own SCR-ID) |
| SCR-SEC-008 | التسجيل الذاتي (Sign Up) | PATTERN-1 (simplified, public) | PUBLIC — no permission gate (unauthenticated) | Reached from "ليس لديك حساب؟" link on SCR-SEC-005 |
| SCR-SEC-009 | نسيان كلمة المرور واستعادتها (Forgot/Reset Password) | PATTERN-1 (simplified, public, 2-step: request → reset) | PUBLIC — no permission gate | Reached from "نسيت كلمة المرور؟" link on SCR-SEC-005 |

```
CORE-9 compliance: SCR-SEC-002's Branch Scope sub-tab does NOT receive
its own SEC_PAGES row — one Controller class continues to serve the
whole Role composite screen; PERM_ROLE_VIEW remains the gateway
permission for the sub-tab as for the rest of SCR-SEC-002.
```

### 7.2 F2 — Facade / State Ownership

```
UserProfileFacade  (backs SCR-SEC-006)
  Owns  : selected user-profile record, search filters/results, branch
          dropdown (active ORG_BRANCH list, sourced via API-ORG-008)
  Ops   : search() → API-SEC-033 | load(id) → API-SEC-035
          create() → API-SEC-032 | update(id) → API-SEC-034

RoleBranchFacade  (backs SCR-SEC-002 sub-tab)
  Owns  : role-branch grid for the currently open Role, branch dropdown
  Ops   : search(roleId) → API-SEC-037 | assign() → API-SEC-036
          update(id) → API-SEC-038 | remove(id) → API-SEC-039

SignUpFacade  (backs SCR-SEC-008)
  Owns  : signup form state, activation-link state (post-submit)
  Ops   : submit() → API-SEC-040 | activate(token) → API-SEC-041

ForgotPasswordFacade  (backs SCR-SEC-009)
  Owns  : 2-step state — request form, then reset form (token from URL)
  Ops   : requestReset() → API-SEC-042 | reset(token) → API-SEC-043
```

### 7.3 F3 — Validators (client-side, mirrored server-side per RULE-ID)

```
RULE-SEC-034 → branchIdFk required, active-branch-only dropdown source
RULE-SEC-035 → dataAccessLevel required (LOV-SEC-002 dropdown)
RULE-SEC-036 → client-side duplicate check before submit (UX only —
               server remains source of truth per ERR-SEC-1036)
RULE-SEC-040/041 → username/email format + required, server-authoritative
               uniqueness (no client-side "is available" pre-check
               endpoint specified by SRS — do not invent one)
RULE-SEC-038 → Forgot Password form shows the SAME generic success
               message client-side regardless of API response content
               (front end must not branch UI on whether the email
               existed — this would defeat RULE-SEC-038 at the UI layer)
All validators : Arabic + English message pairs per Error Catalog
               (Section 4.2) — MANDATORY-P-1 (Playwright) will verify
               Arabic message visibility in test-plan.md
```

---

## 8. PHASE SEC — Permissions, Route Guards, Seed Data

### 8.1 Permissions Matrix

| SCR-ID | VIEW | CREATE | UPDATE | DELETE |
|---|---|---|---|---|
| SCR-SEC-006 (User Profiles) | ✓ | ✓ | ✓ | — (no delete per SRS field/API list — profiles deactivate via isActiveFl through UPDATE, not DELETE) |
| SCR-SEC-002 sub-tab (Role Branch) | ✓ (existing PERM_ROLE_VIEW) | ✓ (existing PERM_ROLE_CREATE reused for assign) | ✓ (existing PERM_ROLE_UPDATE) | ✓ (existing PERM_ROLE_DELETE reused for API-SEC-039) |
| SCR-SEC-008 (Sign Up) | PUBLIC | PUBLIC | — | — |
| SCR-SEC-009 (Forgot/Reset) | PUBLIC | PUBLIC | — | — |

```
New PERM_ constants (auto-generated from PAGE_CODE per SEC-3 /
CORE-9 — not hand-written INSERTs):
  PERM_USER_PROFILE_VIEW / _CREATE / _UPDATE  (SCR-SEC-006)
No new PERM_ constants for the Role Branch sub-tab (CORE-9 — reuses
PERM_ROLE_* since it is not a separate SEC_PAGES row).
```

### 8.2 Security Seed Data Task

```
- Seed 3 new SEC_PAGES row: USER_PROFILE (pageCode), route
  /security/user-profiles, parent = existing "الأمان" group page.
- Auto-generate 3 permission records (VIEW/CREATE/UPDATE — no DELETE,
  per RULE-SEC-012 pattern, scoped to only the operations this screen
  actually exposes) via the existing SEC_PAGES→PERMISSIONS trigger
  mechanism (RULE-SEC-012).
- Seed DATA_ACCESS_LEVEL lookup values — already specified in
  db-script-SEC-gaps.md BLOCK 8 (MD_MASTER_LOOKUP/MD_LOOKUP_DETAIL
  INSERTs) — P3 does not duplicate; references that block as
  authoritative.
```

### 8.3 Rate-Limit Filter Extension (flagged in Section 4.1)

```
Extend LoginRateLimitFilter's path-matching (security-registry.md §5.8)
to also cover:
  POST /api/auth/forgot-password
  POST /api/auth/reset-password
Same key pattern: "<ip>|<identifier-lowercased>" (email, in this case,
not username). This is an addition to an EXISTING filter's configured
path list — not a new filter class, not a new column, not a Security
"code change" in the Conflict #19 sense of new tables/entities, but IS
still a change to Security module code and therefore still falls under
the same Conflict #19 gate.
```

---

## 9. DERIVATION LOG (inferences beyond direct source traceability)

| DRV-ID | Item | Inference | Justification |
|---|---|---|---|
| DRV-SEC-001 | SEC_USER_PROFILE PK | Shared PK with USERS via @MapsId, no separate surrogate PK | db-script-SEC-gaps.md DBF list has no separate PK column for this table; RULE-SEC-034 frames it as "every SEC_USER_PROFILE record" (1:1), consistent with a shared-PK extension pattern already common in this codebase style |
| DRV-SEC-002 | SEC_ROLE_BRANCH PK | Composite (roleIdFk, branchIdFk) | Mirrors existing USER_ROLES/ROLE_PERMISSIONS composite-key join tables (security-registry.md §7); RULE-SEC-036's uniqueness requirement maps naturally onto a composite PK |
| DRV-SEC-003 | Token generation mechanism | Not specified — left to agent, use existing project utility if present | SRS does not mandate a specific algorithm/library; inventing one would exceed traceable scope |
| DRV-SEC-004 | allowedBranches[] "ALL" sentinel | Optional optimization, not mandated | RULE-SEC-037 only requires the claim to be "derived from" assignments — representation format is an implementation detail |
| DRV-SEC-005 | Event bus mechanism for RULE-SEC-031 | Use existing project event/messaging pattern, not invented | Avoids NO-COLUMN-INVENTION-equivalent violation for infrastructure choices; agent confirms against actual codebase in MODE 3 |

---

## 10. OQ LOG (carried forward — not resolved by this engine)

| OQ-ID | Topic | Status | Blocks this plan? |
|---|---|---|---|
| OQ-001 | User/Admin login tabs undocumented (SCR-SEC-005) | OPEN | No — SCR-SEC-005 itself is out of this gap package's build scope (pre-existing screen; only its two link targets, SCR-SEC-008/009, are in scope) |
| OQ-002 | Social login buttons undocumented (SCR-SEC-005) | OPEN | No — same reasoning as OQ-001 |
| OQ-003 | SEC_PAGES SHARED entity ARCH-8 auto-raise | DEFERRED | No — orthogonal to this gap package |
| OQ-004 | PREFERRED_LANG domain undefined | OPEN | No — defaulted VARCHAR(10) NULL, non-blocking per db-script-SEC-gaps.md |
| OQ-005 | EMPLOYEE_ID_FK target (HR ungoverned) | OPEN | No — unconstrained column, no XM-ID, non-blocking |

---

## 11. TC COVERAGE MATRIX — SUMMARY (full Given/When/Then in test-plan.md, MODE 2.5)

| Coverage Type | Total | Covered (planned) | Gap |
|---|---|---|---|
| RULE-IDs (SEC-030..041) | 12 | 12 | 0 |
| API-IDs (SEC-032..043) | 12 | 12 | 0 |
| SCR-IDs (006, 002-ext, 008, 009) | 4 | 4 | 0 |
| ERR-IDs | 9 | 9 | 0 |
| XM-IDs | 2 | 2 | 0 |

```
TC Coverage Gate: PASSED ✓ (planned — no GAP ✗ entries). Full TC blocks
(Given/When/Then, TC-SEC-[N] IDs) are generated in test-plan.md under
MODE 2.5, AFTER this ALIGN gate is confirmed — per this engine's
lifecycle (CORE→...→ALIGN→test-plan.md). Not duplicated here.
```

---

## 12. GATE ALIGN

```
╔══════════════╦══════════════════════════════════════════════════════╗
║ CORE ✓       ║ Scope, standards, task type declared                  ║
║ DATA+DOM ✓   ║ 4 entities, 32 FIELD-IDs, all DBF-bound, no invented   ║
║              ║ columns, EXCEPTION naming respected                    ║
║ SVC+API ✓    ║ 12 API-IDs contracted, ERR-carry rule satisfied        ║
║ DOC ✓        ║ Contract docs mapped to FIELD-ID lists                 ║
║ INT-C ✓      ║ XM-SEC-001/002 contracts specified; event contract     ║
║              ║ (RULE-SEC-031) specified                               ║
║ INT-R ✓      ║ Both XM-IDs READY — no deferral/workaround needed      ║
║ F1 ✓         ║ 4 SCR-IDs specified; CORE-9 respected (no new SCR-ID   ║
║              ║ for Branch Scope sub-tab)                              ║
║ F2 ✓         ║ 4 Facades specified, API-IDs bound, no undocumented    ║
║              ║ endpoint usage                                          ║
║ F3 ✓         ║ All 12 RULE-IDs have validator specs aligned to ERR-IDs║
║ SEC ✓        ║ Permissions Matrix complete, seed-data task specified, ║
║              ║ rate-limit filter extension flagged                     ║
║ TEST ✓       ║ TC Coverage Matrix Summary present, 0 gaps             ║
╠══════════════╬══════════════════════════════════════════════════════╣
║ ALIGN ✓      ║ Internally self-consistent — PLAN LAYER COMPLETE       ║
╚══════════════╩══════════════════════════════════════════════════════╝

⚠ OVERALL GATE STATUS: CONDITIONAL
  Plan (Layer 3 — Execution Truth) is COMPLETE and internally ALIGNED.
  MODE 3 (agent code execution against the Security module) remains
  BLOCKED pending Conflict #19 architecture-authority sign-off —
  identical posture to db-script-SEC-gaps.md (Layer 2). This engine
  does not override, weaken, or bypass that block (RULE-4, Principle-4,
  Principle-11).
```

---

## 13. REGISTRY UPDATE BLOCK

```
────────────────────────────────────────────────────────────────
Source Mode    : MODE 2
Feature Code   : SEC-002
DBS-ID (input) : DBS-SEC-001 (CONDITIONAL)
Plan ID        : PLAN-SEC-002 — ALIGN GATE PASSED ✓ (planning layer)
────────────────────────────────────────────────────────────────
FIELD-IDs      : FIELD-SEC-0001..0032 (32, fully DBF-bound)
API-IDs        : API-SEC-032..043 (12)
ERR-IDs        : ERR-SEC-1030,1032,1033,1034,1035,1036,1039,1040,1041,1043 (9)
SCR-IDs used   : SCR-SEC-006 (new), SCR-SEC-002 (extended, no new ID),
                 SCR-SEC-008 (new), SCR-SEC-009 (new)
RULE-IDs used  : RULE-SEC-030..041 (12, all RECEIVED from SRS)
XM-IDs         : XM-SEC-001, XM-SEC-002 (both RECEIVED, READY, extended
                 with INT-C contract only — no new XM-ID assigned)
DRV-IDs        : DRV-SEC-001..005
OQ-IDs carried : OQ-001, OQ-002, OQ-003 (DEFERRED), OQ-004, OQ-005
Gate Status    : CONDITIONAL — PLAN COMPLETE; MODE 3 EXECUTION BLOCKED
                 pending Conflict #19 architecture-authority sign-off
Next Action    : (1) Route Conflict #19 to architecture authority
                 (same mechanism as Conflict #17/#18 — master-registry
                 Section 13). (2) Once signed off: MODE 2.5 — generate
                 test-plan.md from this execution-plan.md (TC Coverage
                 Matrix already summarized in Section 11 above).
                 (3) MODE 4A — pre-flight governance audit before any
                 MODE 3 agent execution begins.
────────────────────────────────────────────────────────────────
```

---
*End of execution-plan-SEC-gaps.md — MODE 2 output, Execution Plan Governance Engine (Project 3)*
*Consumed: srs-security-full.md + srs-security-gaps.md + db-script-SEC-gaps.md + security-registry.md + master-registry.md*
*Carries forward, unresolved: Conflict #19 (execution-blocking), OQ-001/002/003/004/005 (non-blocking)*
