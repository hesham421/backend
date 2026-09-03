# /SEC/execute-backend

Execute the current phase for SEC — with context safety check.

## Usage
/SEC/execute-backend [PHASE]

---

## STEP 0 — Context Safety Assessment (MANDATORY)

### 0.1 — Read state, identify PENDING subs in the requested phase
### 0.2 — Look up each sub's weight from the Weight Map below
### 0.3 — Classify and decide chunking

| Total weight in phase | Action |
|---|---|
| All LIGHT/MEDIUM | Execute the whole phase in one pass |
| Any HEAVY present | Chunk — one sub (or a few LIGHT subs) per pass |
| Any XL present | That sub alone is one full pass |

### 0.4 — Print assessment, wait for confirmation
```
══════════════════════════════════════════════════════
PHASE ASSESSMENT — SEC / [PHASE]
══════════════════════════════════════════════════════
Subs pending : [list, weight + task count each]
Plan         : [one pass / chunked — list chunks]
══════════════════════════════════════════════════════
Proceed? [waits for confirmation]
```

---

## STEP 1 — Execution (after confirmation)

### 1.0 — Read shared context once (before the per-sub loop)
- The phase's `[PHASE]-HEADER.md` under `packages/backend-execution/[PHASE]/`
  if present — phase-level strategy, tables, and intro that the SUB files
  reference but don't repeat. SEC has `DATA-DOM-HEADER.md` and
  `SVC-API-HEADER.md` — read once at the start of each of those phases,
  never treated as a sub.
- `packages/backend-execution/_SECTIONS.md` if present — plan-level content
  that lives OUTSIDE every phase (Plan Index, DB Alignment Manifest, Error
  Catalog, Agent Handoff Summary). Read once for orientation; it is context,
  not a sub.

### Per sub:
1. Read `packages/backend-execution/[PHASE]/[SUB].md` completely
   (the SUB file is named by its phase-qualified label, e.g. `SVC-API-AUTH.md`)
2. Identify all tasks
3. Map each task to the skill routing table in `GOVERNANCE-RULES.md`
4. Read required skills from `.claude/skills/` (`build-*` to generate, `gov-*` to validate)
5. Execute all tasks in order
6. Run the phase's validation skill after the last task
7. Mark sub COMPLETE in `execution-state.json`

### Blocked items — OQ
OQ-blocked task → skip, add to `blocked[]`, mark in code:
`// TODO: OQ-[ID] — pending resolution`. Continue remaining tasks.

---

## STEP 2 — Session Report

Print phase/sub completed, tasks executed, blocked items, any
api_doc_gaps entries added.

---

## Weight Map — SEC

| Phase | Sub | Weight | Notes |
|---|---|---|---|
| CORE | CORE | LIGHT | Architecture/policy context only — no code artifact of its own; establishes domain-service placement (AuthDomainService, PermissionGenerationDomainService, AuthorizationGrantDomainService) for later phases to build. |
| DATA-DOM | DATA-DOM-IDENTITY | MEDIUM | 1 entity (ENTITY-SEC-001 UserAccount, 11 fields), single layer, but a state machine (PENDING_ACTIVATION→ACTIVE⇄INACTIVE) and 7 domain rules (RULE-SEC-001/002/003/004/005/009/012). |
| DATA-DOM | DATA-DOM-RBAC | HEAVY | 7 entities (Role, Module, RoleModule, Page, Permission, UserRole, RolePermission) — multi-entity, multi-layer (entity + join tables + auto-generation domain logic for RULE-SEC-011/013/014). |
| DATA-DOM | DATA-DOM-TOKENS | MEDIUM | 3 entities (RefreshToken, PasswordResetToken, AccountActivationToken), single layer, uniform shape (token/expiresAt/used-or-revoked/userAccountFk) each with its own TTL rule. |
| SVC-API | SVC-API-AUTH | HEAVY | 6 APIs (login/refresh/logout/forgot-password/reset-password/activate) — multi-layer, complex orchestration (JWT issuance, token rotation, lockout, CU event publishing). |
| SVC-API | SVC-API-LOOKUP | LIGHT | 1 API (API-SEC-016), read-only, single layer. |
| SVC-API | SVC-API-MODULES | HEAVY | 4 APIs (Module CRUD + assign/revoke Tier-1 grant + dashboard modules) — multi-layer, RULE-SEC-013/014 derivation logic (AuthorizationGrantDomainService). |
| SVC-API | SVC-API-RBAC | HEAVY | 5 APIs across 3 entities (Roles CRUD, Pages CRUD incl. auto-permission-generation, Permissions list, Tier-2 grant/revoke) — multi-layer, multi-entity. |
| SVC-API | SVC-API-USERS | HEAVY | 5 APIs (create/search/update/deactivate/activate user) — multi-layer, includes activation-token issuance orchestration. |
| DOC | DOC | LIGHT | Internal contract-stabilization summary only — no code. |
| INT-C | INT-C | LIGHT | Zero outbound XM (SEC is the identity ROOT); inbound-stub notation only — no-op gate. |
| INT-R | INT-R | LIGHT | No outbound runtime dependencies — no-op gate. |
| SEC-BE | SEC-BE | MEDIUM | Single layer (security annotations + seed data), but spans 4 SCR pages, Tier-1 module seed, and 16 permission→role seed grants. |
| ALIGN-BE | ALIGN-BE | LIGHT | Traceability/consistency gate check only — no code. |

## Phase Map — SEC

```
CORE → DATA-DOM → SVC-API → DOC → INT-C → INT-R → SEC-BE → ALIGN-BE
```

| Phase | Subs (filesystem order) |
|---|---|
| CORE | CORE |
| DATA-DOM | DATA-DOM-IDENTITY, DATA-DOM-RBAC, DATA-DOM-TOKENS |
| SVC-API | SVC-API-AUTH, SVC-API-LOOKUP, SVC-API-MODULES, SVC-API-RBAC, SVC-API-USERS |
| DOC | DOC |
| INT-C | INT-C |
| INT-R | INT-R |
| SEC-BE | SEC-BE |
| ALIGN-BE | ALIGN-BE |

---

## Constraints (NON-NEGOTIABLE)

- NEVER skip STEP 0
- NEVER execute without confirmation after assessment
- NEVER invent field/column/route names — always look up db-script.md
- NEVER implement a blocked OQ item — mark and skip only
- NEVER advance phase without explicit instruction
- ALWAYS update execution-state.json after every sub
