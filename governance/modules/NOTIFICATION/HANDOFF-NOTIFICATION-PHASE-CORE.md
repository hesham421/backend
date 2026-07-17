# HANDOFF — PHASE CORE — NOTIFICATION

Gate status: CORE.md declares "MODE 2 Entry Gate PASSED" and "Gate This Phase: CORE PASSED" at the document level — execution-state.json confirms `blocked: []` and no open OQ items block this module's CORE phase.

AuditableEntity location: `backend/erp-common-utils/src/main/java/com/example/erp/common/domain/AuditableEntity.java` — matches CORE.md's declared entity base ("AuditableEntity — all three entities").

LocalizedException location: `backend/erp-common-utils/src/main/java/com/example/erp/common/exception/LocalizedException.java` — matches CORE.md's error-signaling policy ("LocalizedException — NotFoundException BANNED").

AuditEntityListener location: `backend/erp-common-utils/src/main/java/com/example/erp/common/audit/AuditEntityListener.java` — matches CORE.md's "Audit fields — AuditEntityListener — never accepted in Create/UpdateRequest."

BaseSearchContractRequest location: `backend/erp-common-utils/src/main/java/com/example/erp/common/dto/BaseSearchContractRequest.java` — matches CORE.md's Search/Pagination declaration for API-NOTIF-003/006.

New Maven module does not exist yet: `backend/pom.xml`'s `<modules>` list is currently `erp-common-utils, erp-security, erp-finance-gl, erp-masterdata, erp-org, erp-file, erp-main` (plus commented-out `erp-rental-engine`). `erp-file` (File Service) is the newest scaffolded module since the last CORE handoff — its package root is `com.example.erp.file`, extending the majority/newer `com.example.erp.<name>` convention also used by `erp-org`/`erp-finance-gl` (vs. the older `com.example.<name>` used by `erp-security`/`erp-masterdata`). Recommend `com.example.erp.notification` for the new module, following the same majority convention — not decided here, this is DATAOM/SVCAPI's call when the module is actually scaffolded. Parent POM coordinates to mirror: `groupId com.erp`, `artifactId erp-system`, `version 1.0.0-SNAPSHOT` (copied from `erp-file/pom.xml`).

Spring Events ingress precedent found: `AuthService.java` (`backend/erp-security/.../service/AuthService.java`) uses `ApplicationEventPublisher.publishEvent(...)` with dedicated event classes in a sibling `event/` package (`PasswordResetRequestedEvent`, `AccountActivationRequestedEvent`). This is the direct structural precedent for CORE.md's "Spring Events (same-process, same-transaction)" ingress path — SVCAPI should mirror this same-package-sibling `event/` layout for NOTIFICATION's event classes.

RabbitMQ ingress — infra exists, zero application-code precedent: `docker-compose.yml` (repo root, not under `deploy/`) already defines a running `rabbitmq:3-management` container (ports 5672/15672, default creds `admin`/`admin123`) — but there is **no** `spring-boot-starter-amqp` dependency in any `pom.xml`, no `@RabbitListener`/`RabbitTemplate` usage, and no `spring.rabbitmq.*` property anywhere in the backend. This means the container is provisioned but never wired into Spring — NOTIFICATION's `erp.notification.exchange`/`erp.notification.queue` (routing key `notification.send`) will be the **first-ever** RabbitMQ integration in this codebase, not a pattern-match against existing code. SVCAPI should budget for adding the starter dependency and connection properties (host/port/user/pass) itself — mirror the JWT_SECRET-style externalized-property pattern (`application.properties` default + `application-dev.properties` explicit + `application-prod.properties` env-var-only) for the AMQP credentials, consistent with FileSvc's CORE handoff precedent for secret injection.

Firebase Admin SDK / Apache Camel — confirmed zero precedent anywhere in the repo (`pom.xml` search across all modules: no hits for either). Matches CORE.md's own framing that channel dispatch adapters live in a new module-local `channel/` package outside the 6-layer CANONICAL ARCHITECTURE — these will be first-time dependencies added at SVCAPI, same category of "no existing code to copy structurally" as FileSvc's Encrypted Token layer in its own CORE handoff.

`@Lob` / TEXT-mapped fields — confirmed zero precedent anywhere in the repo (no existing `@Lob` usage found). NOTIFICATION's `templateBodyAr`/`templateBodyEn`/`configJson` (TEXT → `String` + `@Lob`) will be this codebase's first use of that mapping — nothing to structurally copy, DATAOM should apply CORE-8's standard mapping directly from the type-mapping table.

`Short`-typed count field (SMALLINT → `Short`, not `_FL` Boolean) — confirmed zero precedent anywhere in the repo for a `Short`-typed entity field. `RETRY_COUNT` will be the first such field; DATAOM should not go looking for an existing example to mirror.

Soft-deactivation (`isActiveFl`/`isEnabledFl`) precedent located: `erp-org`'s repositories (`CostCenterRepository`, `DepartmentRepository`, `BranchRepository`, `LocationSiteRepository`, `ProfitCenterRepository`) all filter on an `isActiveFl`-style flag — confirms the pattern CORE.md declares for `NotificationTemplate.isActiveFl` and `NotificationChannelConfig.isEnabledFl` is a well-worn convention in this codebase, not a new one.

DB_TARGET confirmed: **same standing mismatch as every prior CORE handoff (SEC-002, FileSvc).** Declared `DB_TARGET = POSTGRESQL_16`, but the repo's `docker-compose.yml` (root-level, not `deploy/docker-compose.yml` — that path doesn't exist in this repo layout) pins `postgres:17`. Both are Postgres; no version-16-specific syntax expected in NOTIFICATION's DDL, so non-blocking, reported per standing instruction rather than silently reconciled.

No code written this phase.

Ready for Phase DATAOM? yes
