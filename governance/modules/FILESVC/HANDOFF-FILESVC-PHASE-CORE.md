# HANDOFF — PHASE CORE — FILESVC (File Service)

Gate status: CORE.md declares "MODE 2 Entry Gate PASSED" and "Gate This Phase: CORE PASSED" already, at the document level — this is a fresh module with 0 open OQ/AQ/XM items (module-registry-filesvc.md, srs.md OQ Log both confirm 0 open), so there is no Conflict-#19-style sign-off blocker to chase here.

AuditableEntity location: `backend/erp-common-utils/src/main/java/com/example/erp/common/domain/AuditableEntity.java` — matches CORE.md's declared entity base ("AuditableEntity, uniform, no tenant variant — both entities").

LocalizedException location: `backend/erp-common-utils/src/main/java/com/example/erp/common/exception/LocalizedException.java` — matches CORE.md's declared error-signaling policy ("LocalizedException — NotFoundException BANNED").

New Maven module does not exist yet: `backend/pom.xml`'s `<modules>` list currently has `erp-common-utils, erp-security, erp-finance-gl, erp-masterdata, erp-org, erp-main` (plus a commented-out `erp-rental-engine`, the existing precedent for "module not yet scaffolded"). File Service will need its own new module. Package-naming convention is **not uniform** across existing modules — `erp-org` and `erp-finance-gl` use `com.example.erp.<name>`, while `erp-security` and `erp-masterdata` use `com.example.<name>` (no `erp.` segment). Recommend `com.example.erp.file` (majority/newer pattern) — not decided here, this is DATAOM/SVCAPI's call when the module is actually scaffolded.

Reference Table precedent located: `OrgRegionType` (`backend/erp-org/src/main/java/com/example/erp/org/entity/OrgRegionType.java`) — extends `AuditableEntity`, no Domain companion object, plain `activate()`/`deactivate()` entity methods, per its own doc comment: "No RULE-IDs answer 'is this operation allowed?' for this entity, so it has no Domain companion object." This is the direct precedent CORE.md's FileCategory declaration follows (isActiveFl soft-deactivation, no domain/ package). FileDocument is a different shape, though — DATAOM's own spec gives it embedded `purgeContent()` / `resolveMaxSizeBytes()` methods, since it has real Status-Lifecycle transition logic (RULE-FILE-006/007), not a plain flag — same entity-embedded pattern as OrgRegionType, just non-trivial method bodies instead of a one-line flag flip.

No AES/GCM or URL-embedded-token security mechanism exists anywhere in this codebase today (searched all `.java`/`.properties` for `encrypt|AES|GCM` — zero hits). The Encrypted Token layer CORE.md mandates (module-local `security/` package, e.g. `FileTokenService`/`FileTokenFilter`, invoked before the controller layer) will be the **first-ever** implementation of this pattern in the platform — there is no existing filter/service to copy structurally, only the JWT filter chain as a loose analogy (different mechanism entirely, per POLICY-CLI-06's explicit "no JWT validation inside this module"). Flagging so SVCAPI doesn't go looking for a non-existent precedent.

Secret-injection precedent for `DOCUMENT_ENCRYPTION_SECRET` (business-policies-filesvc.md "Secrets management," AD-FILE-09): `JWT_SECRET` is the existing 3-file pattern to mirror — `application.properties` (`${JWT_SECRET:fallback-dev-secret-minimum-256-bits-long-replace-in-prod}`), `application-dev.properties` (hardcoded dev-only value), `application-prod.properties` (`${JWT_SECRET}`, no fallback, comment mandating the env var). SVCAPI should follow this exact shape for the new AES key property.

DB_TARGET confirmed: **same mismatch as previously found in PLAN-SEC-002's own Phase CORE.** The plan declares `DB_TARGET = POSTGRESQL_16` (business-policies-filesvc.md, db-script.md), but `deploy/docker-compose.yml` still pins `postgres:17`. db-script.md's DDL (BLOCK 1-8) uses no version-16-specific syntax, so non-blocking, but reported since it's a literal contradiction of the plan's stated target — not new to this module, a standing platform-wide discrepancy.

Secondary stale-artifact note (same family as the SEC Phase-CORE finding about `application-prod.properties`'s stale Oracle comment): `backend/.env.example` still documents `DB_URL=jdbc:oracle:thin:@//localhost:1521/FREEPDB1` even though every actual datasource config in the repo is Postgres. Cosmetic, pre-existing, unrelated to File Service — noted only because it's the kind of drift that could mislead a future session bootstrapping this module's own env template.

Binary-content policy (CORE.md's "BINARY CONTENT HANDLING" block) cross-checked against srs.md A3 and db-script.md DBF-0022 — all three agree: BYTEA, multipart-only accept, never in list/search DTOs, streamed on download, nulled at the application layer (not a DB trigger) on delete. No discrepancy found, nothing to flag here.

No code written this phase.

Ready for Phase DATA+DOM? yes
