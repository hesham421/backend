<!-- Source: PHASE:DERIVATION-LOG -->

# SECTION 4 — DERIVATION LOG

| DRV-ID | Phase | Element | Derivation | Criterion |
|---|---|---|---|---|
| DRV-ORG-001 | DATA+DOM | Entity base class | All 8 entities extend `AuditableEntity` (project-standard, TenantAuditableEntity retired 2026-06-21) | Project-standard rule |
| DRV-ORG-002 | DATA+DOM | Error signaling | `LocalizedException` used uniformly; `NotFoundException` banned per project standard | Project-standard rule |
| DRV-ORG-003 | SVC+API | Audit fields | Never set in Mapper/Service — populated exclusively by `AuditEntityListener` per RULE-ORG-016 | CRITERION-2 (SRS "MUST") |
| DRV-ORG-004 | SVC+API | Optimistic locking | Not applied — project does not use version columns (confirmed: no VERSION column in dbs-org-001.md) | CRITERION-1 |
| DRV-ORG-005 | SVC+API | Search/Pagination | `BaseSearchContractRequest` + `ALLOWED_SORT_FIELDS` allow-list per project convention; JPA `Page<T>` used directly — no custom PagedResult wrapper | Project-standard rule |
| DRV-ORG-006 | SVC+API | Deactivation semantics | `isActiveFl = false` via dedicated activate/deactivate endpoints (API-ORG-004/005, 010/011, 016/017, 023/024, 030/031, 036/037, 042/043) — never hard delete, consistent across all 7 business entities | CRITERION-2 |
| DRV-ORG-007 | DATA+DOM | Tree entities | Department and CostCenter use self-referencing nullable FK (parent_department_fk / parent_cost_center_fk) — recursive structure validated via RULE-ORG-007/008 cycle-prevention at service layer (no DB-level recursive constraint in PostgreSQL) | CRITERION-1 + DBS confirmation |
| DRV-ORG-008 | SVC+API | NumberingEngine integration | All Business-Code-bearing entities (001,002,003,004,005,006,007) call `POST /api/numbering/generate` synchronously inside the Create transaction per RULE-ORG-013 | CRITERION-2 |
| DRV-ORG-009 | SECTION D | TC-ID reconciliation | SECTION D TC Coverage Matrix references TC-IDs from test-plan-org-001.md (TC-ORG-001..061) — reconciled in prior MODE 2.5 session | Retroactive correction — prior ALIGN session |
| DRV-ORG-010 | F2 | Service class naming | F2-SERVICE blocks use `[Entity]Service` (Angular injectable) distinct from backend `[Entity]Service` (Spring) — disambiguated by layer context only, no naming collision risk (different modules/packages) | Project-standard rule |
| DRV-ORG-011 | DATA+DOM/F2 | LOV canonical contract | All 6 LOVs (LOV-ORG-001..006) consumed via platform-shared `GET /api/lookups/{lookupKey}?active=true` — corrected from an earlier module-local assumption | CRITERION-2 + master-registry Section authoritative contract |
| DRV-ORG-012 | SVC+API | Repository strategy | Standard `JpaRepository<Entity, Long>` + custom `@Query`/Specification for tree retrieval (API-ORG-020, API-ORG-027) — no native SQL required; PostgreSQL recursive CTE expressed as QR-ID reference only (agent implements) | CRITERION-3 |
| DRV-ORG-013 | F2 | Observable typing | All F2-SERVICE HTTP calls typed `Observable<T>` (Angular HttpClient) per Angular project convention | Project-standard rule |
| DRV-ORG-014 | ALIGN | Retroactive SECTION D / MANDATORY-J-2 closure | MANDATORY-J-2 (empty search → HTTP 200 not 404) explicitly scoped to all 7 search endpoints (API-ORG-002,008,014,021,028,034,040) | Retroactive correction — prior 4A audit round |
| DRV-ORG-015 | PLAN-INDEX | RegionType (ENTITY-ORG-008) API scope | No dedicated API-ORG-IDs or SCR-ID assigned to RegionType in SRS PART B — confirmed maintained via platform generic Admin Reference Table screen, not a module-specific screen. Documented here, not re-derived. | CRITERION-1 (SRS PART A note: "Reference Table مستقل") |
| DRV-ORG-016 | SECTION D (test-plan) | Over-Engineering Guard reduction rationale | Raw per-API/per-screen TC derivation (100+) reduced per Section 16.5 guard — see test-plan-org-001.md header note | Guard rule, Section 16.5 |
| DRV-ORG-017 | SECTION D (test-plan) | MANDATORY-J-6 / MANDATORY-P-4 restoration | 4A-005-003 remediation — both mandatory scenarios restored as named, traceable TC-IDs (TC-ORG-060, TC-ORG-061) after being silently dropped in the initial guard pass | 4A-005-003 audit finding |
| DRV-ORG-018 | SECTION 2 | DB Alignment Manifest rebuild — CONTRACT-1 compliance | Manifest rebuilt to canonical 5-column form (FIELD-ID, DBF-ID, Plan Type, FK/XM-ID, Match Status); prior format duplicated Column Name and raw DB Type from the DB Field Traceability Matrix, violating CONTRACT-1 | 4A-005-006 audit finding |
| DRV-ORG-019 | PHASE SVC+API | Per-API REPOSITORY STRATEGY blocks | Single module-level prose note replaced with a per-API-ID 5-field table (DB Operation, Join strategy, Transaction boundary, Fetch strategy, Bulk operation flag) for all 44 APIs per HR-2 | 4A-005-005 audit finding |
| DRV-ORG-020 | PHASE F2 | F2-SERVICE structured field blocks | All 7 F2-SERVICE blocks rebuilt with labeled fields (Service class, Observable type, Error handling, Loading state, Caching, XM-ID impact) per HR-3, replacing condensed narrative prose | 4A-005-004 audit finding |

Sequence confirmed contiguous DRV-ORG-001..020. No gaps.
