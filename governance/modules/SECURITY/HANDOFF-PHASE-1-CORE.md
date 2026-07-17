# HANDOFF — PHASE 1 (CORE) — PLAN-SEC-002

Conflict #19 sign-off confirmed: yes — explicitly confirmed by the human operator in this session (2026-07-09). NOTE: `master-registry.md` (v2.7.7) Section 13 still lists Conflict #19 as **OPEN** with "no architect sign-off recorded" as of this reading — the registry document itself has not yet been updated to reflect this session's sign-off. This is a registry-lag discrepancy, not a contradiction of the operator's confirmation; recommend the Registry Maintainer update Section 13 / Section 15 / AQ log before or alongside Phase 2 so the governed artifact matches reality.

AuditableEntity location: `backend/erp-common-utils/src/main/java/com/example/erp/common/domain/AuditableEntity.java` (`@MappedSuperclass`, line 29; audit fields populated via `AuditEntityListener`, `@EntityListeners` line 30)

Existing Security entities locations:
- UserAccount (USERS): `backend/erp-security/src/main/java/com/example/security/entity/UserAccount.java` (`@Table(name="USERS")` line 12, extends AuditableEntity line 19; `@ManyToMany` → Role via `@JoinTable(name="USER_ROLES")` lines 52-59 — no separate UserRole entity class)
- Role (ROLES): `backend/erp-security/src/main/java/com/example/security/entity/Role.java` (`@Table(name="ROLES")` line 22, extends AuditableEntity line 31; `@ManyToMany` → Permission via `@JoinTable(name="ROLE_PERMISSIONS")` lines 68-75 — no separate RolePermission entity class)
- Permission (PERMISSIONS): `backend/erp-security/src/main/java/com/example/security/entity/Permission.java` (`@Table(name="PERMISSIONS")` line 20, extends AuditableEntity line 28)
- Page (SEC_PAGES): `backend/erp-security/src/main/java/com/example/security/entity/Page.java` (`@Table(name="SEC_PAGES")` line 17, extends AuditableEntity line 27)
- RefreshToken (REFRESH_TOKENS): `backend/erp-security/src/main/java/com/example/security/entity/RefreshToken.java` (`@Table(name="REFRESH_TOKENS")` line 11) — ⚠️ does NOT extend AuditableEntity (plain `@Data @Builder`, own `@CreationTimestamp createdAt`, line 36-38); pre-existing legacy pattern, out of scope for this gap package but noted since PASSWORD_RESET_TOKEN/ACCOUNT_ACTIVATION_TOKEN are modeled on it per the plan
- USER_ROLES / ROLE_PERMISSIONS: confirmed no dedicated entity classes exist — both are pure `@JoinTable` join tables on UserAccount.roles / Role.permissions respectively

JwtService / JwtProperties:
- `backend/erp-security/src/main/java/com/example/security/security/JwtService.java` (`@Service`, line 24)
- `backend/erp-security/src/main/java/com/example/security/config/properties/JwtProperties.java` (`@ConfigurationProperties(prefix = "erp.security.jwt")` record, line 21-22)

LoginRateLimitFilter: `backend/erp-security/src/main/java/com/example/security/security/LoginRateLimitFilter.java` (`@Component`, extends OncePerRequestFilter, line 38)

LocalizedException: `backend/erp-common-utils/src/main/java/com/example/erp/common/exception/LocalizedException.java` (`extends RuntimeException`, line 30)

Migration mechanism in this repo: Hibernate `ddl-auto` + a standalone hand-written SQL script — NOT Flyway. `spring.flyway.enabled=false` (`erp-main/src/main/resources/application.properties` line 94). `ddl-auto=none` at base and prod (`application.properties` line 42, `application-prod.properties` line 25); `ddl-auto=update` in dev only (`application-dev.properties` line 20). Existing Security migration script lives at `backend/erp-security/src/main/resources/db/scripts/001_security_schema_migration_and_seed.sql` (a `db/scripts` folder, not the Flyway-convention `db/migration`) — this is the pattern `db-script-SEC-gaps.md`'s FULL_DATABASE_SCRIPT should follow (a new sibling script), pending Conflict #19-gated DBA execution, consistent with how the Conflict #17/#18 scripts were handled. Note: a companion script `001_rename_pk_fk_to_standard.sql` is referenced in code comments (`UserAccount.java` line 26, `Role.java` line 38) but could not be located anywhere in the repo via search — flagging rather than guessing its path.

DB_TARGET confirmed: **NO — mismatch found.** The plan (`execution-plan-SEC-gaps.md` / `db-script-SEC-gaps.md`) declares `DB_TARGET = POSTGRESQL_16`. The actual repo config: base/dev/prod datasource all use `org.postgresql.Driver` (Postgres confirmed as the real driver), but `deploy/docker-compose.yml` (lines 15-16) pins the Postgres container image to **`postgres:17`**, not 16. Both are Postgres and the gap package's DDL (BLOCK 1–8 in `db-script-SEC-gaps.md`) uses no version-16-specific syntax I could find, so this is likely non-blocking, but it is a literal contradiction of the plan's stated DB_TARGET and is reported per instructions rather than silently reconciled. Also noted: `application-prod.properties` has a stale header comment claiming "Production Database - Oracle on same host" that contradicts the Postgres driver config actually in effect — `deploy/docker-compose.yml` does separately define an unused `oracle: gvenzl/oracle-free:latest` service, which likely explains the stale comment. Cosmetic, not blocking.

Open questions / discrepancies found vs. execution-plan-SEC-gaps.md:
1. `db-script-SEC-gaps.md` was initially absent from `governance-repo/modules/SECURITY/gaps/` at session start; the user added it mid-session. Confirmed present and read before any inventory work began.
2. Conflict #19 registry-document lag — see note above (operator confirmed sign-off verbally; `master-registry.md` Section 13/15 and AQ log not yet updated to reflect it).
3. DB_TARGET mismatch — see above (POSTGRESQL_16 declared vs. `postgres:17` actually deployed).
4. Companion migration script `001_rename_pk_fk_to_standard.sql` referenced in code comments but not found in repo.
5. `RefreshToken` entity (pre-existing, AS-IS) does not extend `AuditableEntity`, unlike the other Security entities and unlike what Phase DATA+DOM will need for the two new token tables (`PASSWORD_RESET_TOKEN`/`ACCOUNT_ACTIVATION_TOKEN`) — Phase 2 should confirm whether the new token entities should mirror `RefreshToken`'s plain-audit-field style or use `AuditableEntity`, since `db-script-SEC-gaps.md` BLOCK 3 gives `PASSWORD_RESET_TOKEN`/`ACCOUNT_ACTIVATION_TOKEN` only `CREATED_AT`/`EXPIRES_AT` columns (no `CREATED_BY`/`UPDATED_AT`/`UPDATED_BY`), consistent with `RefreshToken`'s pattern, not `AuditableEntity`'s.

No code written this phase.

Ready for Phase 2 (DATA+DOM)? yes
