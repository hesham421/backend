<!-- Source: PHASE:DATA-DOM / SUB:DATA-DOM-RBAC -->
<!-- Context: see DATA-DOM-HEADER.md for phase-level strategy, registry table, and intro -->

### ENTITY-SEC-002 — Role
  Table SEC_ROLE | PK ID | SEQ_SEC_ROLE. FIELDS 0012-0016 (roleCode UNIQUE, nameAr, nameEn, isActiveFl).
  Rules: RULE-SEC-010 (unique roleCode). BC: none (roleCode natural key).
  **(v1.3) Tier-1 op AssignModules → writes SEC_ROLE_MODULE (ENTITY-SEC-011) via API-SEC-017/018; Tier-2 grants (SEC_ROLE_PERMISSION) gated by RULE-SEC-014.** QR-SEC-0007..0010.
### ENTITY-SEC-010 — Module (Registry) ⟵ v1.3  [Tier-1 grantable unit + dashboard display unit]
  Table SEC_MODULE | PK ID | SEQ_SEC_MODULE. FIELDS 0050-0054 → DBF-0050..0054 (ID, moduleCode UNIQUE VARCHAR(50), nameAr, nameEn, isActiveFl).
  BUSINESS CODE: NONE (moduleCode natural key — e.g. SEC/FILE/NOTIF/CU). BC-RULE-0 not applied.
  DTO: ModuleCreateRequest{moduleCode(create-only, immutable after),nameAr,nameEn,isActiveFl}; ModuleUpdateRequest{nameAr,nameEn,isActiveFl}; ModuleResponse{modulePk,moduleCode,nameAr,nameEn,isActiveFl}.
  Rules: RULE-SEC-010 (unique moduleCode). Module is the Tier-1 grantable unit (RULE-SEC-013). QR-SEC-0023..0025.
### ENTITY-SEC-011 — RoleModule (join, Tier-1 grant) ⟵ v1.3
  Table SEC_ROLE_MODULE — composite PK (ROLE_FK, MODULE_FK), no surrogate id, no audit. FIELDS 0055-0056 → DBF-0055..0056.
  Semantics: presence of a (role,module) row = the role is granted the module (RULE-SEC-013 → dashboard visibility + prerequisite for Tier-2). QR-SEC-0026 (grant/revoke), QR-SEC-0027 (EXISTS check).
### ENTITY-SEC-004 — Page (Screen Registry — CORE-9 owner)
  Table SEC_PAGE | PK ID | SEQ_SEC_PAGE. FIELDS 0024-0029 (pageCode UNIQUE, nameAr, nameEn, isActiveFl, parentPageFk self-FK) **+ FIELD-0049 moduleFk (DBF-0049, BIGINT FK→SEC_MODULE, NOT NULL) ⟵ v1.3**.
  DTO: PageCreate/UpdateRequest gains **moduleFk (required)**. moduleFk is the basis of the RULE-SEC-014 derivation.
  Rules: RULE-SEC-010 (unique pageCode), RULE-SEC-011 (registering a page auto-generates 4 permissions), **RULE-SEC-014 (page's module gates who may hold its screen permissions)**. QR-SEC-0011..0014.
### ENTITY-SEC-003 — Permission (auto-generated per Page, CORE-9)
  Table SEC_PERMISSION | PK ID | SEQ_SEC_PERMISSION. FIELDS 0017-0023 (permissionCode UNIQUE, permissionType CHECK VIEW/CREATE/UPDATE/DELETE, nameAr, nameEn, isActiveFl, pageFk→SEC_PAGE).
  Rules: RULE-SEC-010 (unique permissionCode), RULE-SEC-011 (generation). Never client-created directly. **(v1.3) The permission's owning module = SEC_PAGE(pageFk).moduleFk — used by RULE-SEC-014.** QR-SEC-0015..0016.
### ENTITY-SEC-008 — UserRole (join) Table SEC_USER_ROLE — composite PK (USER_ACCOUNT_FK, ROLE_FK), no audit. QR-SEC-0017 (assign/remove).
### ENTITY-SEC-009 — RolePermission (join, Tier-2) Table SEC_ROLE_PERMISSION — composite PK (ROLE_FK, PERMISSION_FK), no audit. **Grant subject to RULE-SEC-014 (no orphan screen permission).** QR-SEC-0018 (grant/revoke), QR-SEC-0029 (dependents check for revoke-module).
