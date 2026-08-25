# Project-Wide Resilient Failure-Handling Pattern — Design + Placement Report

Produced **2026-08-25**. Design-and-placement session only — **no code changes,
no skill-text changes** were made. This document is the output the task
required: a recommendation for the architect to approve before any
implementation or skill-text drafting begins.

Motivating example: `NotificationClient`, `SecurityUserClient`,
`SecUserProfileClient`. Scope: a general mechanism for **any** module's
cross-module `*Client` calls, not a one-off fix for those three.

---

## Part A — Project-wide survey

### A.1 Every cross-module `*Client` in the repo

Full-tree search confirms exactly **5** `*Client` classes exist — no others.
All five follow one convention, already codified in
`governance/.github/skills/backend/create-service/SKILL.md`'s
"Cross-Module Calls (XM)" section: a same-JVM REST self-call
(`http://localhost:${server.port}/api/...`) via a module-scoped
`RestTemplate` bean (3s connect / 5s read timeout), never a direct
cross-module bean injection.

| Client | Module | Calls | Failure handling today |
|---|---|---|---|
| `OrgBranchClient` | erp-security | erp-org (`GET /org/branches/{id}`) | **HARD** — catches, then rethrows as `LocalizedException`, blocking the caller's create/update flow |
| `MasterDataLookupClient` | erp-security | erp-masterdata (`GET /lookups/{code}`) | **HARD-by-accident** — no try/catch at all; raw `RestClientException` propagates unwrapped |
| `NotificationClient` | erp-security | erp-notification (`POST /notifications/send`) | **SOFT** — logs and swallows, "never fails the already-committed signup/reset flow" (deliberate) |
| `SecurityUserClient` | erp-notification | erp-security (`POST /users/search`) | **SOFT** — logs and returns `Optional.empty()` |
| `SecUserProfileClient` | erp-notification | erp-security (`GET /security/user-profiles/{id}`) | **SOFT** — logs and falls back to `DEFAULT_LANGUAGE="EN"` |

No `@Retryable`, `RetryTemplate`, or any resilience annotation exists
anywhere in the codebase today.

| Client | Would benefit from a generic retry/outbox mechanism? |
|---|---|
| `OrgBranchClient` | **Yes** — HARD failure currently blocks writes on a transient erp-org outage; a bounded retry-then-surface path is strictly better than blocking every time. |
| `MasterDataLookupClient` | **Yes**, and urgently — today an unhandled exception leaks raw; needs both proper HARD/SOFT classification *and* eventual-consistency retry. |
| `NotificationClient` | **Yes** — this is the direct motivating case: today a transient erp-notification outage silently loses the notification forever. |
| `SecurityUserClient` | **Yes** — a transient erp-security outage silently drops recipient resolution for a dispatch; retry converts "lost" into "delayed." |
| `SecUserProfileClient` | **Yes**, though lower priority — a wrong-language notification is a degraded UX, not a lost operation, but it's the same shape of failure and the mechanism should not special-case it out. |

All 5 are candidates. This confirms the mechanism must be genuinely
module-agnostic — it will be adopted by both erp-security and
erp-notification on day one, in both call directions.

### A.2 Shared/common module

`erp-common-utils` exists (`erp-common-utils/`) and **every** business module
(`erp-file`, `erp-finance-gl`, `erp-masterdata`, `erp-notification`,
`erp-org`, `erp-security`) already declares a Maven dependency on it. It
currently holds: base entity (`AuditableEntity`), the exception taxonomy
(`LocalizedException`/`BusinessException`/`CommonErrorCodes`), the response
envelope (`ApiResponse`/`ServiceResult`/`Status`), search/pagination
helpers, security context, audit, and web config/filters. It contains
**zero** retry/resilience/outbox infrastructure today.

**Is depending on it architecturally different from a module-to-module
dependency?** Yes, and this distinction is already explicitly honored in
this codebase, not just assumed:

- `create-service/SKILL.md`'s "SHARED LAYER MANDATE" section *requires*
  every service to depend on and reuse `erp-common-utils` classes via
  normal Maven/Spring injection — no REST indirection, no `*Client`.
- The same skill's "Cross-Module Calls (XM)" section forbids exactly that
  (direct injection) for another *business* module's `@Service`,
  `Repository`, or `@Entity`.
- `create-repository/SKILL.md` (line 50) restates the same split.
- The 3 `erp-security`/`erp-notification` `*Client` classes themselves
  prove the pattern in practice: `NotificationClient` imports
  `Status`, `LocalizedException`, `ApiResponse` from `erp-common-utils`
  directly (ordinary shared-library use), while still reaching
  erp-notification's business logic only over REST.

So: **a shared/common module dependency is an already-sanctioned category,
distinct from and not constrained by the `*Client`+REST rule.** Placing new
shared infrastructure in `erp-common-utils` requires no new governance
decision — it's the same category as `ServiceResult` or
`LocalizedException`. This finding is load-bearing for Part B.

### A.3 Existing structural precedent

- `retry/`, `outbox/`, `job(s)/` packages: **none anywhere in the repo.**
- `scheduler/`: exactly one, `erp-security/.../scheduler/RefreshTokenCleanupJob.java`
  — a `@Scheduled` housekeeping cron (expired-token deletion), unrelated to
  cross-module retry; no attempt/status/payload fields.
- Spring Retry (`spring-retry` dependency, `@Retryable`, `RetryTemplate`):
  **zero matches** in any `pom.xml` or source file, in any module.
- Quartz / ShedLock: **zero matches.**
- `@Scheduled`: only the one file above.
- A "pending/failed items" table pattern (fields like `attemptCount`,
  `nextRetryAt`, `status=PENDING/FAILED`, `payload`): **none exists.**
  erp-notification's own `NOTIF_LOG` table records dispatch history but is
  never consulted by any `*Client` as a retry source — it's an audit log,
  not an outbox.

**Conclusion: this is greenfield.** There is no partial mechanism to
extend; whatever is chosen is a net-new addition to the codebase.

A prior session (`governance/project-artifacts/SKILL-ADAPTATION-ARCHITECTURE-CONFLICT-REPORT.md`,
2026-08-22) already rejected injecting a heavier microservices-style
proposal — a module facade interface plus a RabbitMQ 3-layer event-isolation
pipeline — because it contradicted the actual, deliberate single-JVM
modular-monolith architecture (no `XModule` interface, no message broker,
no Modulith anywhere). That report recommended formalizing the existing
`*Client`+REST and `ApplicationEventPublisher` patterns rather than
inventing new infrastructure that assumes a different target architecture
— which is now exactly what the "Cross-Module Calls (XM)" skill section
does. **The same caution applies here**: the retry/outbox design below is
scoped as an *extension* of the existing `*Client`+loopback-REST pattern,
not a replacement of it or a step toward message-broker infrastructure.

---

## Part B — Placement decision

### Generic vs. specific split

- **GENERIC mechanism** (the retry/outbox entity + generic scheduler that
  processes due retries, with no knowledge of any specific module's
  domain): `erp-common-utils`, alongside `AuditableEntity`,
  `LocalizedException`, `ServiceResult` — the same shelf, same dependency
  category, no new dependency edge introduced. New package suggestion (not
  binding — architect's call): `com.example.erp.common.resilience`.
- **SPECIFIC usage** (what erp-notification registers to plug its own
  `NotificationClient`/`SecurityUserClient`/`SecUserProfileClient` failures
  into the generic mechanism): stays inside the owning module, e.g.
  `erp-notification/.../client/` or a sibling package in that module —
  never copied into `erp-common-utils` or into the module it's calling.

### Does this require a new shared-module dependency that doesn't exist today?

**No.** Per A.2, every module that would use this (erp-security,
erp-notification, and any future adopter) already depends on
`erp-common-utils`. Adding retry/outbox classes there is additive content
in an existing, already-universal dependency — not a new edge in the
module dependency graph. This is the key placement finding: **no separate
governance decision is needed for the placement itself**, only for
introducing the pattern's existence (see Open Governance Items below,
regarding a possible new Maven dependency like `spring-retry` — Option B
in Part C).

---

## Part C — Design options

### Option A — Generic DB-backed outbox/retry table + generic `@Scheduled` processor in `erp-common-utils`

**Mechanism:** A generic `FailedCallRecord` entity (owning module,
target module, operation key, JSON payload, attempt count, `nextRetryAt`,
status: `PENDING`/`RETRYING`/`SUCCEEDED`/`FAILED_PERMANENT`) plus a generic
`@Scheduled` processor in `erp-common-utils`. The processor does **not**
know how to actually perform a notification send or a user lookup — it
invokes a callback registered by the owning module (e.g. a
`RetryableOperation` functional interface the module implements and
registers by operation key at startup). On failure it re-persists with a
backed-off `nextRetryAt`; after N attempts it flips to
`FAILED_PERMANENT` and stays queryable/visible.

- Reusable across modules: **yes** — the entity and processor carry no
  domain fields, only a payload blob and an operation key string.
- New dependency needed: none beyond what's already in `erp-common-utils`
  (JPA is already a transitive dependency via each module's own
  `spring-boot-starter-data-jpa`; `erp-common-utils` itself would need a
  JPA annotation dependency it may not currently have — verify before
  implementation).
- Visibility: a queryable table — an admin/ops query or endpoint can list
  `PENDING`/`FAILED_PERMANENT` rows per module.
- Failure surfacing: rows sitting in `FAILED_PERMANENT` are inherently
  visible via query; alerting on that status is a follow-on, not required
  for the base mechanism.
- Fits `*Client` + REST module-boundary rule: **yes** — nothing here
  changes how modules call each other; it wraps around the existing
  `*Client` call, doesn't replace it, and callbacks are registered
  in-module, not injected across modules.

### Option B — Spring Retry (`@Retryable`) as first line of defense, falling through to Option A's outbox only after retries are exhausted

**Mechanism:** Each `*Client` method gets `@Retryable` (a handful of
fast, bounded, in-process retries with backoff — no persistence, no
scheduler) for transient blips (a dropped connection, a momentary 503).
Only when retries are exhausted does the call register a
`FailedCallRecord` via the Option A outbox, for durable retry across a
sustained outage or an app restart.

- Reusable across modules: **yes** — `@Retryable` is applied per-method in
  each module's own `*Client`, no cross-module coupling; the durable
  fallback reuses the same generic Option A infrastructure.
- New dependency needed: **yes** — `spring-retry` (+ `spring-aspects` for
  AOP-based `@Retryable`) added to `erp-common-utils`'s dependency
  management so every module that already depends on it gets it
  transitively. This is a genuinely new third-party dependency the project
  doesn't currently have anywhere — flag explicitly (see Open Governance
  Items).
- Visibility / failure surfacing: same as Option A once a call reaches the
  outbox; fast in-process retries that succeed are invisible (by design —
  that's the point of the first line of defense).
- Fits module boundary rule: **yes**, same reasoning as Option A — purely
  additive around the existing `*Client` call sites.

### Option C — Outbox only, no in-process fast-retry layer (Option A alone, no Spring Retry)

Included for completeness since Option B's dependency addition is itself
an open question. Same generic mechanism as Option A, but every failure —
even a one-off transient blip — goes straight to the durable outbox and
waits for the next scheduled sweep (e.g. every 30–60s) rather than being
retried in-process within the same request.

- Reusable: yes, identical to Option A.
- New dependency: **none** — no `spring-retry` needed.
- Visibility / failure surfacing: identical to Option A.
- Trade-off vs. Option B: a purely transient failure (network blip) incurs
  a full scheduler-cycle delay instead of resolving within the same
  request/second, but the design stays dependency-minimal.
- Fits module boundary rule: yes.

---

## Recommended option

**Option B** (Spring Retry as first line of defense, Option A's generic
outbox as the durable fallback), with the explicit caveat that adding
`spring-retry` is flagged below as its own governance decision point and
the architect may reasonably choose **Option C** instead to avoid the new
dependency entirely and start simpler. Reasoning: most of today's actual
failures observed in the 5 `*Client`s (per A.1) read as transient —
same-JVM loopback calls timing out or connection-refused during a brief
window — which Spring Retry resolves cheaply without ever touching the
database; the outbox exists for the sustained-outage case, which is
strictly rarer but has real consequences (a lost notification with no
record of the attempt). Option A/C's generic outbox is the load-bearing
piece regardless of which option is chosen; the only open question is
whether the fast in-process layer is worth a new dependency. Final choice
remains the architect's.

---

## Part D — Motivating-example walkthrough (Notification's 3 clients)

**One retry unit or three?** **Three separate retry units**, not one
bundled per notification dispatch. Justification: `SecurityUserClient`
(resolve recipient), `SecUserProfileClient` (resolve language), and the
eventual `NotificationClient`-style outbound send (currently the reverse
direction, erp-security → erp-notification, but the same shape would apply
if erp-notification itself made an outbound send call) each fail
independently and have different consequences — a language-resolution
failure should not block or duplicate-retry a recipient-resolution retry
that already succeeded. Each `*Client` call site registers its own
`FailedCallRecord` keyed by its own operation, so a partial failure in one
doesn't force replaying the others.

**What erp-notification needs to add/change (conceptually, no code yet):**
1. Wrap each `*Client` call's failure path (today: catch → log → soft
   fallback) with: on exhaustion of the fast in-process retries (if
   Option B), persist a `FailedCallRecord` via the generic
   `erp-common-utils` API instead of just logging and returning the
   fallback value immediately.
2. Register a callback (implementing the generic `RetryableOperation`
   contract) that knows how to re-invoke `SecurityUserClient.resolve...`
   or `SecUserProfileClient.resolvePreferredLanguage` given the persisted
   payload, so the generic scheduler can retry it later without knowing
   what a "user profile" or "language" is.
3. Decide (module-owned policy, not generic-mechanism policy) what happens
   to the *original* notification dispatch while the resolution is
   pending — e.g. does the dispatch retry too, or proceed with today's
   fallback (`DEFAULT_LANGUAGE`) while the resolution retries in the
   background purely to fix future dispatches? This is exactly the kind
   of domain decision that must stay in erp-notification's specific-usage
   code, not leak into the generic mechanism.

**Confirmed generic:** none of this requires the generic mechanism to know
anything about "notification," "user," or "language" — it only needs an
operation key (opaque string), a payload (opaque JSON blob), and a
callback reference. If a future Sales→Inventory retry needed the exact
same three fields, it would reuse the identical entity and scheduler with
zero modification. The design does not need reworking on this front.

---

## Output summary (per requested format)

**PROJECT-WIDE CLIENT SURVEY:**
- `OrgBranchClient` — erp-security — HARD (rethrows) — benefit: yes
- `MasterDataLookupClient` — erp-security — HARD-by-accident (unhandled) — benefit: yes
- `NotificationClient` — erp-security — SOFT (swallowed) — benefit: yes
- `SecurityUserClient` — erp-notification — SOFT (swallowed) — benefit: yes
- `SecUserProfileClient` — erp-notification — SOFT (swallowed) — benefit: yes

**SHARED MODULE / PLACEMENT FINDING:**
- Exists: yes — `erp-common-utils/` — Architecturally distinct from
  module-to-module dependency: yes (already-honored split, see A.2;
  every business module already depends on it for exactly this category
  of cross-cutting infrastructure).

**GENERIC VS. SPECIFIC SPLIT (recommended):**
- Generic mechanism location: `erp-common-utils` (suggested package
  `com.example.erp.common.resilience` — architect's call on exact name)
- Per-module usage location: owning module's own `client/` package (e.g.
  `erp-notification/.../client/`), using Notification's 3 clients as the
  example

**DESIGN OPTIONS:**
- Option A — generic outbox + scheduler only — Reusable: yes — New dep: none — Visibility: queryable table — Failure surfacing: `FAILED_PERMANENT` status — Fits boundary: yes
- Option B — Spring Retry first line + Option A fallback — Reusable: yes — New dep: `spring-retry`+`spring-aspects` (flagged) — Visibility: same as A — Failure surfacing: same as A — Fits boundary: yes
- Option C — Option A alone, no fast-retry layer — Reusable: yes — New dep: none — Visibility: same as A — Failure surfacing: same as A — Fits boundary: yes

**RECOMMENDED OPTION:** Option B, with Option C as the dependency-free
fallback choice if the architect prefers not to add `spring-retry` yet.
Final choice remains the architect's.

**MOTIVATING-EXAMPLE WALKTHROUGH:** See Part D — 3 independent retry
units (one per `*Client` call site), erp-notification registers a callback
per operation, the original dispatch's degraded-fallback behavior (default
language, etc.) stays a module-owned policy decision, not part of the
generic mechanism.

**SKILL-TEXT IMPLICATION:** `create-service/SKILL.md`'s "Cross-Module
Calls (XM)" section would need a new subsection — name only, not drafted
— e.g. "Retry-with-eventual-consistency for XM calls," documenting when a
`*Client` failure should register with the generic outbox instead of
(or in addition to) today's HARD-rethrow / SOFT-swallow choice, and how to
implement the per-operation callback contract.

**OPEN GOVERNANCE ITEMS:**
1. Whether to add `spring-retry` (+ `spring-aspects`) as a new
   project-wide dependency (needed only for Option B) — this is a new
   third-party dependency the project doesn't have anywhere today; flagged
   for explicit approval, not decided here.
2. Whether `erp-common-utils` currently has (or needs adding) a JPA
   annotation dependency sufficient to define a new `@Entity` there
   directly, or whether the generic entity should instead be defined via
   an interface/mapped-superclass in `erp-common-utils` with the actual
   `@Entity`+table living in each consuming module's own schema (avoids
   `erp-common-utils` owning a live DB table). This is an implementation
   detail but affects the "GENERIC mechanism location" placement claim
   above and should be resolved before implementation starts.
3. HARD vs. SOFT vs. "retry-then-decide" is currently inconsistent even
   among the 3 motivating clients (see A.1) — adopting this mechanism is
   a good opportunity to also fix `MasterDataLookupClient`'s unhandled
   exception, but that is a separate, smaller fix the architect may want
   to schedule independently of the larger retry/outbox rollout.

---

**STOP** — this document ends the design session. No implementation or
skill-text drafting proceeds until the architect reviews and decides among
the above.
