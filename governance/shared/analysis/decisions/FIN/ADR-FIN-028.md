# ADR-FIN-028 — Physical shape of the v2 mapping store, dimension tags and lookup changes

Module  : FIN     Version : v2     Stage raised : P2 (Database)
Status  : ACCEPTED (non-breaking)
traces  : ENT-FIN-010, ENT-FIN-015, ENT-FIN-016, REQ-FIN-045, REQ-FIN-049, REQ-FIN-050, REQ-FIN-054, REQ-FIN-055, REQ-FIN-059, REQ-FIN-062, REQ-FIN-064, REQ-FIN-065, REQ-FIN-072, RULE-FIN-004, RULE-FIN-019, RULE-FIN-022, RULE-FIN-025, RULE-FIN-026, DBF-FIN-041, DBF-FIN-101, DBF-FIN-102, DBF-FIN-148, DBF-FIN-152, XM-FIN-001

## Context
The v2 SRS (CS-FIN-001) adds two entities (AccountMapping, RuleLineDimension), one RuleLine field
(`accountBusinessFieldCode`), and changes to four FIN-owned lookup keys. It fixes the fields and
rules. It does not fix these structural points, which P2 has to settle:

1. Table names for the two new entities.
2. Lengths for the new code columns and for `businessValue`. The SRS gives only "lookup" and "text".
3. Whether RULE-FIN-019 and RULE-FIN-026 (the derivation spec or tag matches its source) become
   database CHECKs. v1's DEFAULT keeps every lookup value set out of local CHECK constraints.
4. How RULE-FIN-022 ("one ACTIVE mapping per key") becomes a constraint. A plain UNIQUE would
   also block a replacement row once the old one is deactivated.
5. How the v2 lookup changes reach MDL. The engine (§4.2) requires a seed block against the owning
   module's table for every key this module owns. v1 left all 13 keys to FIN's onboarding through
   MDL's API (REQ-FIN-045).
6. Whether existing `FIN_RULE_LINE` rows need converting now that DIRECT is gone and
   `account_derivation_value` is only the constant account code.

## Decision
1. **Names.** `FIN_ACCOUNT_MAPPING` (ENT-FIN-015) and `FIN_RULE_LINE_DIM` (ENT-FIN-016), following
   v1's `FIN_JOURNAL_LINE_DIM`. Columns use the script's one transformation (camelCase to
   snake_case), so the PK is `rule_line_dimension_pk`. This is the transformation the header
   states. v1's `journal_line_dim_pk` is an applied name and stays as it is.
2. **Lengths.** Every new column that stores an MDL lookup code is `VARCHAR(50)`, the width of
   `MDL_LOOKUP_VALUE.code`, so any code MDL accepts fits. `business_value` is `VARCHAR(100)`, the
   width v1 gave `event_reference`, the other host-supplied code. It holds a host code as-is, or
   a PAYMENT_METHOD code (≤ 50) when the field is PAYMENT_METHOD.
3. **Exclusive-or CHECKs that name no lookup code.** `CHK_FIN_RULE_LINE_DERIVATION_SPEC` requires
   exactly one of `account_derivation_value` and `account_business_field_code` to be non-null.
   `CHK_FIN_RULE_LINE_DIM_VALUE_SPEC` does the same for `dimension_value_id` and
   `business_field_code`. Which one each type needs (CONSTANT → value, MAPPING / BUSINESS_FIELD →
   field) stays in the application layer, validated against MDL (XM-FIN-001). The v1 DEFAULT
   holds: no local CHECK lists a lookup code. Every existing v1 rule line satisfies the new CHECK:
   its value is NOT NULL and the new column is NULL.
4. **Partial unique index.** `UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY` is UNIQUE on (event_type_code,
   business_field_code, business_value) WHERE is_active_fl. It follows the v1 precedent
   `UQ_FIN_ACCOUNT_RETAINED_EARNINGS` and lives in BLOCK 7. A deactivated mapping keeps its row,
   which is the soft-delete default, and does not block a new active mapping for the same key.
   RULE-FIN-025 is a plain `UQ_FIN_RULE_LINE_DIM_LINE_DIM` UNIQUE (rule_line_id, dimension_id). A
   tag has no active flag.
5. **Seed block against MDL, idempotent.** BLOCK 8 writes the v2 lookup changes into
   `MDL_LOOKUP_TYPE` / `MDL_LOOKUP_VALUE`, which MDL owns. It inserts 2 types, their 6 values and
   PAYMENT_METHOD's CHEQUE. It also deactivates ACCOUNT_DERIVATION_TYPE's DIRECT, a soft retire, so
   AC-FIN-045's "exactly 2 active values" holds and the id is not destroyed. Every statement is
   guarded by `NOT EXISTS` on MDL's own unique keys (`UQ_MDL_LOOKUP_TYPE_KEY`,
   `UQ_MDL_LOOKUP_VALUE_TYPE_CODE`), so it can run before or after FIN's API onboarding
   (REQ-FIN-045), in either order, any number of times. The 11 unchanged v1 keys are not
   re-seeded. This is a migration.
6. **No data conversion.** ADR-FIN-010 records that no data ever used DIRECT, since v1 never
   delivered it. MAPPING raised "unsupported" in v1, so no v1 rule line can depend on it. The
   migration only relaxes `account_derivation_value` to NULL-able and adds the new column.

## Consequences
- P3.1 validates the per-type pairing (RULE-FIN-019, RULE-FIN-026), tag-value-belongs-to-dimension
  (RULE-FIN-026) and PAYMENT_METHOD membership (RULE-FIN-024) in the application layer. The database
  guarantees only the exclusive-or and the uniqueness.
- P3.1 maps a unique violation on `UQ_FIN_JOURNAL_ENTRY_EVENT_REF` to the duplicate-event error
  (RULE-FIN-004 as modified; REQ-FIN-072). The constraint is unchanged. In the PostgreSQL catalogue
  its name is lower-case (`uq_fin_journal_entry_event_ref`) because it was created unquoted.
- P3.1 keeps FIN's lookup onboarding idempotent, so it tolerates the rows BLOCK 8 may already have
  written.
- Override: move the per-type pairing into CHECKs that name 'CONSTANT' / 'MAPPING' /
  'BUSINESS_FIELD' if the lookup-in-MDL DEFAULT is dropped. Otherwise use a full UNIQUE instead of
  the partial index if hard delete ever replaces deactivation, which the DEFAULT forbids.
