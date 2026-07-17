<!-- Source: PHASE:DATA-DOM / SUB:CORE-ENTITIES -->
<!-- Context: see DATA-DOM-HEADER.md for phase-level strategy, registry table, and intro -->

  ## ENTITY-ORG-001 — LegalEntity
  Table: ORG_LEGAL_ENTITY · Sequence: ORG_LEGAL_ENTITY_SEQ · Business Code: `LE-NNNNN` (global scope)
  Fields: see SECTION 2 (FIELD-0001..0011). entity_type_id bound to LOV-ORG-001.
  Domain rules enforced at entity/service layer: RULE-ORG-011 (code immutable), RULE-ORG-012 (code uniqueness global), RULE-ORG-013 (NumberingEngine only), RULE-ORG-014 (code blocked on Update), RULE-ORG-015 (name uniqueness — global scope, no parent), RULE-ORG-016 (audit fields blocked).
  Deactivation guard: RULE-ORG-001 (active Branches), RULE-ORG-002 (active ProfitCenters) — both checked in deactivate service method before `isActiveFl=0` write.
  QR-ORG-001: SELECT EXISTS(SELECT 1 FROM ORG_BRANCH WHERE LEGAL_ENTITY_FK=:pk AND IS_ACTIVE_FL=1) — RULE-ORG-001 guard.
  QR-ORG-002: SELECT EXISTS(SELECT 1 FROM ORG_PROFIT_CENTER WHERE LEGAL_ENTITY_FK=:pk AND IS_ACTIVE_FL=1) — RULE-ORG-002 guard.
  QR-ORG-003: SELECT * FROM ORG_LEGAL_ENTITY WHERE (:code IS NULL OR LEGAL_ENTITY_CODE=:code) AND (:nameAr IS NULL OR NAME_AR ILIKE %:nameAr%) AND ... ORDER BY :sort PAGE :page SIZE :size — search (API-ORG-002).

  ## ENTITY-ORG-002 — Branch
  Table: ORG_BRANCH · Sequence: ORG_BRANCH_SEQ · Business Code: `BR-[LE_CODE]-NNNNN` (scope: per LegalEntity)
  Fields: FIELD-0012..0023. branch_type_id bound to LOV-ORG-002. legal_entity_fk NOT NULL RESTRICT.
  Domain rules: RULE-ORG-011..016 (standard code/name/audit set, scope: per LegalEntity for name+code uniqueness), RULE-ORG-018 (parent LegalEntity must be active at create time).
  Deactivation guard: RULE-ORG-003 (active Departments), RULE-ORG-004 (active CostCenters), RULE-ORG-005 (active LocationSites).
  QR-ORG-004: SELECT IS_ACTIVE_FL FROM ORG_LEGAL_ENTITY WHERE LEGAL_ENTITY_PK=:legalEntityFk — RULE-ORG-018 guard at create.
  QR-ORG-005..007: existence checks against ORG_DEPARTMENT / ORG_COST_CENTER / ORG_LOCATION_SITE WHERE BRANCH_FK=:pk AND IS_ACTIVE_FL=1 — RULE-ORG-003/004/005 guards.
  QR-ORG-008: paginated search by branch_code/name_ar/legal_entity_fk/branch_type_id/is_active_fl (API-ORG-008).

  ## ENTITY-ORG-008 — RegionType (PRIVATE Reference Table)
  Table: ORG_REGION_TYPE · Sequence: ORG_REGION_TYPE_SEQ · No Business Code.
  Fields: FIELD-0024..0032. Admin-only Create/Read/Update (no Deactivate API in this plan — DRV-ORG-015).
  QR-ORG-009: SELECT * FROM ORG_REGION_TYPE WHERE IS_ACTIVE_FL=1 ORDER BY NAME_EN — feeds Region entry-form dropdown (region_type_id_fk).

  ## ENTITY-ORG-003 — Region
  Table: ORG_REGION · Sequence: ORG_REGION_SEQ · Business Code: `RG-[LE_CODE]-NNNNN` (scope: per LegalEntity)
  Fields: FIELD-0033..0044. region_type_id_fk NOT NULL RESTRICT → ORG_REGION_TYPE. legal_entity_fk NOT NULL RESTRICT.
  Domain rules: RULE-ORG-011..016 standard set; RULE-ORG-017 (SOFT-READ consumer warning surfaced on deactivate — non-blocking, ⏸ OQ-001).
  Deactivation guard: RULE-ORG-006 (active Branches referencing this Region) — note Test-Hint: only `is_active_fl=1` Branches counted.
  QR-ORG-010: SELECT EXISTS(SELECT 1 FROM ORG_BRANCH WHERE REGION_FK=:pk AND IS_ACTIVE_FL=1) — ⚠ see note below.
  ⚠ NOTE: dbs-org-001.md DBF matrix shows NO `region_fk` column on ORG_BRANCH (ORG_BRANCH FK set = legal_entity_fk only). RULE-ORG-017's "region_fk" reference and RULE-ORG-006's "Branches reference Region" trace to a relationship not materialized as a direct FK in the current DBS. Resolution: this binding is logged as INBOUND-STUB-style internal gap, not blocking — RULE-ORG-006/017 guard logic is deferred to whichever consuming linkage is confirmed at MODE 1.5 amendment; QR-ORG-010 marked ⚠ AGENT REFERENCE — verify FK existence in db-script before implementing. Carried forward unresolved per OQ-001 since the same root cause (no direct Region↔Branch FK in DBS) underlies both. No new OQ raised — already covered by OQ-001 scope per DRV continuity.
  QR-ORG-011: paginated search by region_code/name_ar/legal_entity_fk/region_type_id_fk/is_active_fl (API-ORG-014).

  ## ENTITY-ORG-004 — Department (tree)
  Table: ORG_DEPARTMENT · Sequence: ORG_DEPARTMENT_SEQ · Business Code: `DEP-[BR_CODE]-NNNNN` (scope: per Branch)
  Fields: FIELD-0045..0057. branch_fk NOT NULL RESTRICT. parent_department_fk NULLABLE RESTRICT (self). node_type_id bound to LOV-ORG-003 (SUMMARY/DETAIL).
  Domain rules: RULE-ORG-007 (cycle prevention on parent assignment), RULE-ORG-009 (SUMMARY blocked on transactional records — enforced in consuming modules, not here — informational only), RULE-ORG-011..016 standard set (code/name scope: per Branch), RULE-ORG-019 (must be under active Branch at create), RULE-ORG-020 (node_type_id immutable after save).
  QR-ORG-012: recursive CTE — `WITH RECURSIVE dept_tree AS (SELECT * FROM ORG_DEPARTMENT WHERE PARENT_DEPARTMENT_FK IS NULL AND BRANCH_FK=:branchFk UNION ALL SELECT d.* FROM ORG_DEPARTMENT d JOIN dept_tree t ON d.PARENT_DEPARTMENT_FK=t.DEPARTMENT_PK) SELECT * FROM dept_tree` — API-ORG-020 tree retrieval. ⚠ agent rewrites per ORM capability (native query or recursive Specification).
  QR-ORG-013: cycle check — walk ancestor chain of proposed parent_department_fk upward; if target descendant PK encountered → reject (RULE-ORG-007).
  QR-ORG-014: paginated flat search by branch_fk/name_ar/node_type_id/is_active_fl (API-ORG-021).

  ## ENTITY-ORG-005 — CostCenter (tree)
  Table: ORG_COST_CENTER · Sequence: ORG_COST_CENTER_SEQ · Business Code: `CC-[BR_CODE]-NNNNN` (scope: per Branch)
  Fields: FIELD-0058..0071. branch_fk NOT NULL RESTRICT. parent_cost_center_fk NULLABLE RESTRICT (self). node_type_id → LOV-ORG-004. cost_center_type_id → LOV-ORG-005.
  Domain rules: RULE-ORG-008 (cycle prevention, mirrors RULE-ORG-007), RULE-ORG-010 (SUMMARY blocked on transactional records — informational), RULE-ORG-011..016 standard set, RULE-ORG-019 (active Branch required), RULE-ORG-020 (node_type_id immutable).
  QR-ORG-015: recursive CTE mirroring QR-ORG-012, table=ORG_COST_CENTER — API-ORG-027.
  QR-ORG-016: cycle check mirroring QR-ORG-013 — RULE-ORG-008.
  QR-ORG-017: paginated search by branch_fk/name_ar/node_type_id/cost_center_type_id/is_active_fl (API-ORG-028).

  ## ENTITY-ORG-006 — ProfitCenter
  Table: ORG_PROFIT_CENTER · Sequence: ORG_PROFIT_CENTER_SEQ · Business Code: `PC-[LE_CODE]-NNNNN` (scope: per LegalEntity)
  Fields: FIELD-0072..0082. legal_entity_fk NOT NULL RESTRICT. No internal dependency guard on deactivate (per SRS A6 lifecycle table) — only the standard RULE-ORG-011..016 set applies.
  QR-ORG-018: paginated search by profit_center_code/name_ar/legal_entity_fk/is_active_fl (API-ORG-034).

  ## ENTITY-ORG-007 — LocationSite
  Table: ORG_LOCATION_SITE · Sequence: ORG_LOCATION_SITE_SEQ · Business Code: `LS-[BR_CODE]-NNNNN` (scope: per Branch)
  Fields: FIELD-0083..0094. branch_fk NOT NULL RESTRICT. site_type_id → LOV-ORG-006.
  Domain rules: RULE-ORG-011..016 standard set, RULE-ORG-019 (active Branch required). No internal dependency guard on deactivate (per SRS A6) — though API-ORG-042 contract table lists RULE-ORG-005 as cross-reference (Branch-side guard, not a LocationSite-side guard); retained as documentation cross-link only, not a new validation on this entity.
  QR-ORG-019: paginated search by location_site_code/name_ar/branch_fk/site_type_id/is_active_fl (API-ORG-040).
