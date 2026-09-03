## MODULE REGISTRY — COMMON UTILS
══════════════════════════════════════════════════════════════════
Module Name    : Common Utils
Module Code    : CU
Layer          : L1
Type           : Cross-Cutting Foundation (library — not a business module)
Execution Tier : T1 (ROOT — built first, depended-on by all)
P0 Date        : 2026-09-01
Readiness      : READY
Domain KB      : none supplied — derived from domain-profile-ERP.md
Source         : NEW
══════════════════════════════════════════════════════════════════

SCOPE NOTE
──────────────────────────────────────────────────────────────────
Per domain-profile-ERP.md, Common Utils is ONE analytical cross-cutting
component — a shared library, not a standalone business module. It groups
five capabilities: Specification/Filtering, Global Exceptions, Bundle,
Configuration, and Events. Its outputs are reusable utilities consumed by
every other module; it owns almost no persistent data. Governing mandate:
medium complexity — the simplest solution that satisfies the requirement.
No FK from CU to any module (it is ROOT); other modules depend on CU.

RESPONSIBILITIES (capabilities — code-level unless noted)
──────────────────────────────────────────────────────────────────
Specification / Filtering │ dynamic query + filter mechanism (predicate
                          │ builder over entity queries). Pure code — no entity.
Global Exceptions         │ base exception hierarchy + centralized handler +
                          │ standard error-response shape. Pure code — no entity.
                          │ NOTE: the governed Error Catalog / ERR-IDs are a P3
                          │ artifact — CU provides the exception INFRASTRUCTURE,
                          │ not the catalog.
Bundle (i18n)             │ AR/EN message resolution. Default: resource bundles
                          │ (messages_ar / messages_en) — file-based, no entity.
Configuration             │ platform "Configurable" goal — lightweight persisted
                          │ key/value settings store (see ENTITIES OWNED).
Events                    │ in-process domain events (publisher/listener). Default:
                          │ synchronous in-process bus — no entity, no broker.
──────────────────────────────────────────────────────────────────

ENTITIES OWNED
──────────────────────────────────────────────────────────────────
AppConfiguration │ Config / Master │ PRIVATE
──────────────────────────────────────────────────────────────────
Note: names only — ENTITY-IDs assigned by P1, not here.
This is the ONLY persisted entity CU owns by default. It backs the
"Configurable" design goal with runtime-adjustable key/value settings.
All other CU capabilities are code mechanisms with no table.

LOVs OWNED
──────────────────────────────────────────────────────────────────
(none by default)
──────────────────────────────────────────────────────────────────
Note: any config value-type / scope enumeration, if needed, is a P1
LOV decision — not pre-declared here to avoid over-engineering.

LOVs CONSUMED (from other modules)
──────────────────────────────────────────────────────────────────
(none — CU is ROOT)
──────────────────────────────────────────────────────────────────

SHARED ENTITIES CONSUMED
──────────────────────────────────────────────────────────────────
(none — CU is ROOT)
──────────────────────────────────────────────────────────────────

DEPENDENCIES
──────────────────────────────────────────────────────────────────
(none)
──────────────────────────────────────────────────────────────────
ROOT: YES — no external deps. Depended-on by SEC, FILE, NOTIF.

AUTO-DECISIONS
──────────────────────────────────────────────────────────────────
AUTO: Configuration is a persisted key/value store (1 entity: AppConfiguration).
FROM: domain-profile "Configurable" design goal + medium-complexity rule.
IF WRONG: if configuration should be file/env-only (Spring properties), drop
          the entity — CU then owns zero persisted entities.

AUTO: Bundle (i18n) uses file-based resource bundles (messages_ar/_en), no entity.
FROM: Step 4 default (simplest solution) — standard Spring i18n.
IF WRONG: if translations must be runtime-editable by admins, add a
          MessageCatalog entity (P1 decision).

AUTO: Events use a synchronous in-process publisher (Spring ApplicationEvent),
      no broker, no outbox table.
FROM: Modular Monolith + medium-complexity rule (no RabbitMQ/Kafka).
IF WRONG: if durable/async cross-module delivery is required later, add an
          event-outbox entity + async dispatch (opened as a new decision then).

AUTO: Specification/Filtering and Global Exceptions are pure code mechanisms
      (no entities, no tables).
FROM: their nature — query helpers and error handling.
IF WRONG: n/a — these are not data-owning by definition.

INF-IDs
──────────────────────────────────────────────────────────────────
(none — all decisions traced to domain-profile + medium-complexity rule
 via AUTO-DECISIONS above; no unresolved gap)
──────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════
