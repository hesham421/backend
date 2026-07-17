# HANDOFF — PHASE 2 (DATA+DOM) — PLAN-SEC-002

Entities created:
- `SecUserProfile` — `backend/erp-security/src/main/java/com/example/security/entity/SecUserProfile.java` (SEC_USER_PROFILE, ENTITY-SEC-009; shared PK via `@MapsId`/`@OneToOne` to `UserAccount`)
- `SecRoleBranch` — `backend/erp-security/src/main/java/com/example/security/entity/SecRoleBranch.java` (SEC_ROLE_BRANCH, ENTITY-SEC-010; composite key via `@IdClass`)
- `SecRoleBranchId` — `backend/erp-security/src/main/java/com/example/security/entity/SecRoleBranchId.java` (composite PK class for SecRoleBranch)
- `PasswordResetToken` — `backend/erp-security/src/main/java/com/example/security/entity/PasswordResetToken.java` (PASSWORD_RESET_TOKEN, ENTITY-SEC-011; modeled on `RefreshToken`'s plain-audit style)
- `AccountActivationToken` — `backend/erp-security/src/main/java/com/example/security/entity/AccountActivationToken.java` (ACCOUNT_ACTIVATION_TOKEN, ENTITY-SEC-012; modeled on `RefreshToken`'s plain-audit style)

Migration file(s): `backend/erp-security/src/main/resources/db/scripts/002_datascope_selfservice_auth_schema.sql` (sibling to `001_security_schema_migration_and_seed.sql`, same manual-DBA / idempotent convention)

Migration verified applying cleanly: **yes** — executed directly against the live local Postgres dev DB (`erp_db`, via the project's Postgres MCP connection) inside a single `BEGIN...COMMIT` transaction. No errors on the second run (first attempt surfaced two real discrepancies, both fixed and re-verified — see Deviations). Post-commit checks confirmed: all 4 tables exist with their PK/UK/CHECK/FK constraints (`pg_constraint`), all 7 indexes exist (`pg_indexes`), and the entities compile (`mvn -o -pl erp-security -am clean compile` → `BUILD SUCCESS`, 83 source files).

UK_USERS_EMAIL applied: **yes** — `uk_users_email UNIQUE (email)` confirmed present in `pg_constraint` (Postgres folds unquoted identifiers to lowercase, so it does not show as `UK_USERS_EMAIL`).

Lookup seed applied: **yes** — `DATA_ACCESS_LEVEL` MD_MASTER_LOOKUP row + 3 MD_LOOKUP_DETAIL rows (BRANCH_ONLY / BRANCH_AND_CHILDREN / ALL) confirmed present, count = 3.

Deviations from db-script-SEC-gaps.md (all found by actually running the script against the live schema, not guessed in advance; first two blocked the migration until fixed):

1. **USERS.EMAIL did not exist.** `execution-plan-SEC-gaps.md` (FIELD-SEC-0032, "email (existing)") and `db-script-SEC-gaps.md` BLOCK 5b both assume `USERS.EMAIL` already exists and only add a `UNIQUE` constraint on it — the live `USERS` table had no such column, and the governed DDL never contains an `ADD COLUMN EMAIL` anywhere in BLOCK 1–11. I stopped and flagged this before touching the DB; the operator explicitly instructed me to add the missing column and commit. Added `ALTER TABLE USERS ADD COLUMN EMAIL VARCHAR(150)` (nullable — the existing bootstrap admin row has no email to backfill) immediately before the `UK_USERS_EMAIL` constraint in the migration script, and added the corresponding `email` field to `UserAccount.java` (not originally in this phase's "4 new entities" scope, but left unmapped would have been an inconsistent half-state now that the column physically exists).
2. **Oracle-dialect seed syntax.** `db-script-SEC-gaps.md` BLOCK 8 uses `SEQ.NEXTVAL` (Oracle syntax), which is invalid on Postgres. Translated to `nextval('SEQ')`, matching `001_security_schema_migration_and_seed.sql`'s existing `SEC_PAGES_SEQ` usage. Confirmed via `HANDOFF-PHASE-1-CORE.md` that the live DB_TARGET is Postgres, not Oracle. No column/table/constraint name changed.
3. **Missing NOT NULL audit columns in BLOCK 8 seed INSERTs.** `MD_MASTER_LOOKUP`/`MD_LOOKUP_DETAIL` both require `CREATED_AT`/`CREATED_BY` (NOT NULL, `AuditableEntity`) — the governed INSERT statements omit them and failed on first run (`null value in column "created_at"`). Added them following the `SYSTEM`-seed convention already used in `001_security_schema_migration_and_seed.sql`.
4. `branchIdFk` (on `SecUserProfile` and `SecRoleBranch`) and `roleIdFk` (on `SecRoleBranch`) are plain `Long` scalars, not JPA associations — matches FIELD-ID register's literal "Plan Type: Long", and `erp-security` has no Maven dependency on `erp-org` (RULE-SEC-034's active-branch check is a Service-layer HTTP call to ORG's API per XM-SEC-001, Section 6.1 — not a shared JPA object graph). Referential integrity is enforced at the DB level only (FK_SEC_USER_PROFILE_BRANCH, FK_SEC_ROLE_BRANCH_ROLE, FK_SEC_ROLE_BRANCH_BRANCH).
5. `PasswordResetToken.user` / `AccountActivationToken.user` are `@ManyToOne UserAccount` associations (matching `RefreshToken`'s existing pattern, which the plan explicitly cites as the model for these two tables), not plain `Long userIdFk` scalars as FIELD-SEC-0022/0028's "Plan Type: Long" literally states — same-module association, no dependency concern.
6. `SecRoleBranch` uses `@IdClass(SecRoleBranchId.class)` (plan allowed `@EmbeddedId` OR `@IdClass`) — no existing composite-key precedent in this codebase either way; chose `@IdClass` to keep `roleIdFk`/`branchIdFk` as flat fields consistent with FIELD-ID register naming.

No repository/service/controller code written this phase.

Ready for Phase 3 (SVC+API)? yes
