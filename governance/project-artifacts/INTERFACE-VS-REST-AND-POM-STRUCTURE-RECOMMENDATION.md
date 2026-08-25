# Interface-vs-REST-Loopback and Pom-Structure — Best-Practice Recommendation

Produced **2026-08-25**. Read-only investigation + written recommendation only —
**no code, pom.xml, or skill-text changes were made in this session.** This
document evaluates two structural questions on pure software-engineering
merit, deliberately setting aside `create-service/SKILL.md`'s current rules
and prior governance decisions as normative constraints (they are cited below
only as descriptive evidence of what exists today).

Scope: the deployment topology is fixed and given — `erp-security`,
`erp-notification`, `erp-org`, `erp-masterdata`, `erp-finance-gl`, `erp-file`,
and `erp-common-utils` are Maven modules assembled into one `erp-main`
Spring Boot deployable, one JVM, one process, one port, with no current or
plausible plan to run any of them independently.

---

## 1. Executive Summary

**Question 1 (communication):** For the specific pattern the 5 existing
`*Client` classes represent — a same-thread, permission-checked, read-style
lookup into another module that lives in the same JVM — **direct Spring
interface injection is the objectively simpler choice** and should replace
same-JVM loopback REST for this call shape. The investigation found no real
blocker: Spring's `@EnableMethodSecurity` already enforces `@PreAuthorize` at
the service-bean layer (not the controller), so authorization keeps working
correctly, and for synchronous callers it actually gets *simpler* (no more
manual `Authorization`-header forwarding). The two real costs — accidental
transaction-boundary fusion via default `REQUIRED` propagation, and the
temptation to leak a target module's domain types across the boundary — are
real but manageable with two explicit disciplines (call out propagation,
keep the existing narrow read-model DTOs). The retry/outbox mechanism just
built stops applying to these 5 call sites specifically (there is no
"network-shaped" failure left to retry against), which is a legitimate
simplification, not a loss — the generic outbox in `erp-common-utils` remains
valid for calls that reach genuinely flaky infrastructure (e.g. SMTP).

**Question 2 (packaging):** The current multi-module Maven layout is **not
buying what it appears to buy**, and should be evaluated for consolidation,
though this finding is less clear-cut than Question 1. The investigation
found a live, in-production counter-example — `erp-org`, `erp-notification`,
and `erp-masterdata` all reference `com.example.security.constants.
SecurityPermissions` through Spring-EL `T(...)` type expressions with **zero**
Maven dependency declared on `erp-security`. This is not a hypothetical risk:
it is the actual mechanism today, and it proves the "modules can't
accidentally reach each other" claim is already false in practice. Combined
with negligible measured reactor overhead (~4s for all 9 modules,
incrementally), zero exercised independent-versioning or independent-test
benefit (every module is pinned to `${project.version}`, and there are no
tests anywhere in the repository to run independently), multi-module Maven
is providing a soft, partial nudge — not the hard boundary it's assumed to
provide. A single pom is not a free win, however: it removes even that soft
nudge for plain `import` statements, and nothing in this codebase (no
ArchUnit, no Modulith) exists yet to replace it. Recommendation is
consolidation **conditioned on** adding a lightweight ArchUnit boundary test
in the same change — not consolidation on its own.

These two answers are **independent and can be adopted separately** — this
report is not proposing a single combined migration.

---

## 2. Question 1 — Findings

### 2.1 Transaction semantics

Concrete example from the codebase: [SecRoleBranchService.java](../../erp-security/src/main/java/com/example/security/service/SecRoleBranchService.java)`.create()`
is annotated `@Transactional` (line 60) and, before persisting anything, calls
`assertValidDataAccessLevel()` (line 143), which calls
`MasterDataLookupClient.assertValidDataAccessLevel()` (line 147) — today a
same-JVM **HTTP** call to `erp-masterdata`'s own `/api/lookups/{code}`
endpoint, which runs in **erp-masterdata's own, independent transaction**
(if any) and commits or fails on its own, entirely decoupled from
`SecRoleBranchService`'s eventual commit/rollback.

If this call were direct interface injection instead, Spring's default
`@Transactional` propagation is `REQUIRED`: the target method would join the
**caller's already-open transaction** on the same thread/`EntityManager`
rather than opening its own. For this specific example (a read-only lookup,
no write on the target side) that change is inert — there's nothing to fuse.
But the pattern generalizes badly: if any *future* cross-module call performs
a write on the target side (plausible — e.g. an audit-log write, a
usage-counter increment), direct injection would silently make that write
conditional on the *caller's* unrelated transaction succeeding, where today
it is unconditionally committed the moment the target's own transaction
commits. That is a real hazard, not a hypothetical one, because Spring's
default is invisible — nothing forces a developer to notice they've just
coupled two transaction boundaries that used to be independent.

This is also, properly read, a *benefit* in specific cases: if the intent
genuinely is "validate the lookup and only commit the role-branch row if
both succeed atomically," direct injection makes that trivial, where today
it's not achievable at all (two independent commits can never be made
atomic over HTTP without a distributed-transaction protocol this codebase
has zero infrastructure for).

**Conclusion:** direct injection doesn't have a uniformly correct default
here — it requires the call site to *decide* and state its propagation
explicitly (`REQUIRES_NEW` to preserve today's independence, or the default
`REQUIRED` to get atomicity). REST loopback made "independent" the only
option, silently; direct injection makes it a choice that must be made
correctly, which is a real cost in developer discipline but not a
structural blocker.

### 2.2 Authorization propagation

The 5 target endpoints are consistently **not** guarded at the controller
level. Confirmed directly in source comments:
[SecUserProfileController.java:27-30](../../erp-security/src/main/java/com/example/security/controller/SecUserProfileController.java)
and [SecRoleBranchController.java:28-32](../../erp-security/src/main/java/com/example/security/controller/SecRoleBranchController.java):
*"Thin controller — all logic, including `@PreAuthorize` permission gates
[...] — controllers never carry `@PreAuthorize`."* The actual gates are on
the `@Service` methods — e.g. `UserService.java` (7 `@PreAuthorize` lines),
`SecUserProfileService.java` (`getById`, line 108-110), `RegionService.java`,
`ProfitCenterService.java`, and 5 more `erp-org` services, all using the
identical `hasAuthority(T(com.example.security.constants.SecurityPermissions
).X)` SpEL pattern. `@EnableMethodSecurity` is declared once, globally, in
`erp-security/.../config/SecurityConfig.java:38` — since this is one Spring
`ApplicationContext` in one JVM, this method-security AOP interception
**already applies to every matching bean regardless of whether it's reached
via HTTP or via a direct method call.**

Consequence: switching to direct injection does **not** bypass authorization
for these methods — the same `@PreAuthorize` check fires either way, because
it's bound to the bean, not the controller or filter chain. For a
synchronous, same-thread caller, `SecurityContextHolder` already holds the
current `Authentication` — direct injection would actually be **simpler**
than today's pattern, which manually re-forwards the `Authorization` header
found via `RequestContextHolder`/`ServletRequestAttributes`
(`MasterDataLookupClient.forwardedAuthHeaders()`,
`OrgBranchClient.forwardedAuthHeaders()`, both identical) purely so the
target's own JWT filter chain can reconstruct the exact `SecurityContext`
that was already sitting on the same thread a moment earlier. That
header-forward-and-reparse round trip disappears entirely with direct
injection for the synchronous case.

Two genuine exceptions surfaced that are *not* simplified by direct
injection:

- **Async/background threads.** `EmailChannelSender`'s dispatch thread and
  the new `OutboxRetryProcessor`'s `@Scheduled` sweep thread have no live
  `HttpServletRequest`/`SecurityContext` at all — exactly why
  `DispatchAuthContext` (a `TaskDecorator`-populated thread-local) exists
  today. Direct injection needs the equivalent machinery
  (`DelegatingSecurityContextExecutor`/`Callable`, or manually pushing a
  reconstructed `Authentication` before the call) — same shape of problem,
  not eliminated, just renamed.
- **`NotificationClient.sendAccountActivation`/`sendPasswordReset`** — the
  one case where the caller is *genuinely unauthenticated* (self-registration,
  forgot-password), yet the target (`NotificationEventProcessor.send`,
  `@PreAuthorize("isAuthenticated()")`, line 61) requires an authenticated
  principal. Today this is solved by minting a **real, verifiable JWT** for a
  seeded service account (`svc-notification`) via `JwtService.generateAccess`
  — the same code path a real login uses — and sending it as a normal Bearer
  token through the standard filter chain. Under direct injection, satisfying
  `isAuthenticated()` requires manually constructing an `Authentication`
  object and pushing it into `SecurityContextHolder` for the call's duration
  — asserting the service account's authorities without ever validating
  anything through a real credential-check surface. This is **architecturally
  weaker**, not stronger, than today's approach, and is the one place this
  investigation recommends keeping the existing minted-JWT pattern rather
  than converting to direct injection outright (see §3, accepted tradeoffs).

### 2.3 Retry-mechanism relevance

The newly built `NotificationRetryGateway`/`SecurityUserRetryGateway`/
`SecUserProfileRetryGateway`/`MasterDataLookupRetryGateway` all retry
narrowly on `ResourceAccessException`/`HttpServerErrorException` — failures
that, by construction, can only arise from an actual TCP/HTTP round trip
(connection refused, timeout, 5xx). A direct Java method call between two
beans in the same JVM cannot fail this way; it returns normally, throws a
declared business exception (`LocalizedException`, etc.), or — rarely —
leaks a genuine bug (`NullPointerException`, a constraint violation). "Retry
on transient failure" has no referent left for these specific call sites.

**Honest assessment, not a hedge:** adopting direct injection at these 5 call
sites would make all 4 `*RetryGateway` classes dead code, and they should be
deleted, not kept "just in case." This is a point **in favor** of direct
injection — it doesn't lose real resilience, it removes a layer that exists
to compensate for a self-imposed network hop that direct injection removes
at the root rather than papering over. The prior design report's own A.1
survey found the actual production risk was "a transient erp-notification
outage silently loses the notification forever" — but that risk is a
property of the loopback-HTTP design itself; removing the HTTP hop removes
the failure mode it was retrying against, it doesn't leave the same risk
unretried.

The **generic outbox** (`FailedCallRecordBase` / `OutboxRetryProcessor` /
`RetryableOperationRegistry` in `erp-common-utils`) is a different matter and
should **not** be deleted: it durably records "this operation failed and
needs a later retry" independent of *why* it failed, and several of the real
downstream dependencies in this codebase are genuinely flaky regardless of
in-process-vs-REST (e.g. `erp-notification`'s actual SMTP relay via
`spring-boot-starter-mail`, or any future outbound webhook/gateway call).
The mechanism stays valuable there — it just becomes unnecessary
*specifically for the erp-security↔erp-notification/masterdata/org bridging*
that motivated it, because that bridging disappears as a distinct failure
category once it's a direct call.

### 2.4 Testability

No test files exist anywhere in the repository today — confirmed by an
exhaustive `**/src/test/**/*.java` search returning zero matches, despite
every module declaring `spring-boot-starter-test`, `spring-security-test`,
and (in `erp-finance-gl`) `h2` as test dependencies. This means the
comparison is necessarily theoretical, not a measurement against this
codebase's actual test suite (there isn't one).

In the abstract: a directly-injected interface is trivially mockable with
Mockito, no HTTP layer involved. But the gap is smaller than it first
appears — the very reason `NotificationRetryGateway`/etc. were extracted
into their own `@Component` (see the javadoc on
`NotificationRetryGateway.java:14-19`: *"Spring AOP proxies can't intercept
a private method or a self-invoked call"*) is that `@Retryable` needed a
separate injectable seam. That extraction incidentally already gives today's
design an equally mockable boundary (mock the `*RetryGateway` bean directly,
skip `MockRestServiceServer`/WireMock entirely) — so this is a real but
mild point in favor of direct injection, not a decisive one.

### 2.5 Hidden coupling risk

- **`RequestContextHolder`/`ThreadLocal` fragility.** Direct injection does
  not fix the async-thread problem described in §2.2 — it relocates it.
  Today's `DispatchAuthContext` (header propagation across a `TaskDecorator`
  boundary) has a direct structural analogue under direct injection
  (`SecurityContext` propagation across the same boundary). Net: no
  improvement, no regression — a wash, worth calling out only so nobody
  believes direct injection removes this class of fragility.
- **Domain-type leakage.** Today's `*Client` classes return module-local,
  hand-rolled read models — `UserLookup`, `SecUserProfileLookup`,
  `OrgBranchLookup`, `LookupValueLookup` — never the target module's real
  entities or DTOs. This is a deliberate (if accidental-by-construction)
  anti-corruption layer: going over JSON forces a translation boundary.
  Direct injection removes that forcing function — nothing stops a developer
  from injecting `SecUserProfileService` directly and passing its real
  `SecUserProfileDto` (or, worse, a JPA entity) straight into
  `erp-notification`'s code, creating real, growing, compile-time coupling
  to `erp-security`'s internal domain shape. This is a **genuine,
  codebase-specific risk**, not generic microservices FUD: this repo has no
  ArchUnit/Modulith tests (confirmed absent — see §4.2) that would catch that
  drift, and the one place a real compile-time module-to-module dependency
  already exists (`erp-finance-gl` → `erp-masterdata`, see §4.4) has no
  precedent of discipline to point to since `erp-finance-gl` has no source
  code yet (pom-only module). Mitigation is available (keep returning the
  same narrow read-model records the `*Client` classes already define,
  exposed via a small interface) but it must be a stated convention, not an
  automatic consequence of the change.

### 2.6 Future flexibility

An exhaustive governance-wide search for "microservice," "independently
deploy," "scale independently," and similar phrasing turned up exactly two
hits — [SKILL-ADAPTATION-ARCHITECTURE-CONFLICT-REPORT.md](SKILL-ADAPTATION-ARCHITECTURE-CONFLICT-REPORT.md)
and [RESILIENT-FAILURE-HANDLING-DESIGN-REPORT.md](RESILIENT-FAILURE-HANDLING-DESIGN-REPORT.md)
— and both are prior sessions **rejecting** a microservices-shaped proposal
as contradicting this project's actual, deliberate single-JVM modular
monolith. The one structural artifact that could be read as forward-looking —
`<!-- <module>erp-rental-engine</module> -->`, commented out in the root
`pom.xml` — has zero mentions anywhere in `master-registry.md` or
`modules-registry.json`; nothing describes it as a planned extraction
candidate, and reading it as evidence of a microservices intent would be
inflating a hypothetical this project shows no real sign of wanting. Weight
this concern low: there is no plausible near-term reason to keep the REST
seam "just in case."

---

## 3. Question 1 — Recommendation and Accepted Tradeoffs

**Recommendation:** Replace the 5 existing `*Client`+loopback-REST call sites
with direct Spring interface injection, through a small, explicitly-defined
interface per producing module (e.g. `SecurityUserLookup`,
`SecUserProfileLookup`, `MasterDataLookupApi`) rather than injecting the
concrete `@Service` classes directly — this keeps a visible contract surface
even without a compile-time module boundary. Delete the 4 `*RetryGateway`
classes as part of the same change (§2.3). Keep the one asymmetric case —
`NotificationClient`'s anonymous-flow, service-account JWT minting — exactly
as it is; do not force it through direct injection.

**Accepted tradeoffs, stated explicitly, not hidden:**

1. Every converted call site must **explicitly** declare its transaction
   propagation intent (`REQUIRES_NEW` to preserve today's independent-commit
   behavior, or accept `REQUIRED` only when atomicity is actually wanted).
   Silently accepting Spring's default is not acceptable given §2.1's
   analysis — this must be a per-call-site decision.
2. Async/scheduled callers need an explicit `SecurityContext`-propagation
   helper (mirroring today's `DispatchAuthContext`) — this is new code to
   write, not something direct injection provides for free.
3. The return-type discipline in §2.5 (module-local read models, never the
   target's real entities/DTOs) must be a stated convention going forward,
   since nothing in this codebase currently enforces it automatically.
4. The generic outbox mechanism in `erp-common-utils` (`FailedCallRecordBase`
   /`OutboxRetryProcessor`) is retained for genuinely flaky downstream
   dependencies (e.g. SMTP) — it is not removed by this recommendation, only
   un-wired from these 5 specific call sites.

---

## 4. Question 2 — Findings

### 4.1 What the current structure actually buys, checked one by one

| Claimed benefit | Verified? | Evidence |
|---|---|---|
| Enforced compile-time module boundary (no accidental cross-module import) | **Partially false** | See §4.2 — a live, in-production counter-example exists |
| Independent module versioning | **Unused** | All 8 internal `<dependency>` declarations pin `${project.version}` (root `pom.xml` `<dependencyManagement>`); no module has ever diverged |
| Independent test execution per module | **Unused** | Zero test files anywhere in the repo (§2.4) — there is nothing to run independently |
| Parallel Maven builds | Not evaluated | No CI configuration was in scope for this investigation; not confirmed as exercised |
| Faster incremental builds via reactor module skipping | **Real, but small at this scale** | See §4.3 |

### 4.2 Compile-time enforcement — the actual evidence

`erp-org`, `erp-notification`, and `erp-masterdata` source code all contains
`@PreAuthorize` expressions of the exact shape
`hasAuthority(T(com.example.security.constants.SecurityPermissions).X)` —
for example `RegionService.java:59`, `UserService.java` is the *owning*
module so that one's expected, but `erp-org`'s `RegionService.java`,
`ProfitCenterService.java`, `BranchService.java`, `CostCenterService.java`,
`DepartmentService.java`, `LegalEntityService.java`, `LocationSiteService.java`
and `erp-notification`'s `NotificationTemplateService.java`,
`NotificationLogQueryService.java`, `NotificationChannelConfigService.java`,
and `erp-masterdata`'s `MasterLookupService.java`, `LookupDetailService.java`
all reference `com.example.security.constants.SecurityPermissions` this way.

Checked directly: **none of `erp-org/pom.xml`, `erp-notification/pom.xml`, or
`erp-masterdata/pom.xml` declares a Maven dependency on `erp-security`.**
This compiles and runs correctly today only because Spring-EL's `T(...)`
operator resolves the class name **reflectively at runtime**, and at
runtime `erp-main` has already assembled every module's classes onto one
classpath — Maven's compile-time dependency graph is never consulted for
this reference. A plain `import com.example.security.constants.
SecurityPermissions;` in the same files *would* fail to compile without the
declared dependency — so the multi-module structure does block the naive
case. But it provides **zero** protection against the SpEL-string form, and
this isn't a contrived edge case: it is the actual mechanism used
project-wide, in 12 confirmed files, for permission checks in every business
module that isn't `erp-security` itself. Anyone reaching for
`Class.forName(...)`, a `@Value` string reference, reflection, or any other
runtime-resolved indirection gets the identical bypass, invisibly, with no
Maven or compiler diagnostic ever firing.

**Conclusion:** the "modules literally cannot accidentally import another
module's internals" claim is not true of this codebase today. It is true
only for direct, static `import` statements — a real but partial guarantee,
already circumvented by the codebase's own permission-check convention.

### 4.3 Build time

Measured directly in this environment (`mvn -o compile`, all modules already
built, i.e. reactor/dependency-resolution overhead with no actual
compilation work): **9-module reactor total: 4.3s wall-clock**, with
`erp-common-utils` at 1.7s (largest, first in the graph) and every other
module under 1s, several under 0.2s. A true from-scratch `mvn clean compile`
timing could not be captured in this session — the available local Maven
toolchain doesn't support `--release 21` (an unrelated, pre-existing
environment/JDK mismatch, not a property of the module count) — but the
incremental number already shows reactor bootstrap and inter-module
dependency resolution are not a real cost at this codebase's current size
(9 modules, no module with a large source tree). A from-scratch build's
total time would be dominated by `javac` compiling the same total lines of
source regardless of whether that source is split across 9 poms or
concatenated into 1 — consolidating poms would not measurably change total
compile time at this scale, only remove reactor-graph bookkeeping that is
already cheap.

### 4.4 Overhead the current structure does carry

- **8 near-identical `pom.xml` files.** Each of `erp-security`,
  `erp-notification`, `erp-org`, `erp-masterdata`, `erp-file`,
  `erp-finance-gl` independently redeclares its own
  `spring-boot-starter-web`/`-test`, `lombok`, `springdoc-openapi`,
  `postgresql` (runtime), etc. Versions are centralized via the parent's
  `<dependencyManagement>`, so this isn't version drift — but it is real,
  literal duplication of dependency *presence* declarations across 8 files
  that a single pom would collapse to one list.
- **One real cross-business-module compile dependency already exists:**
  `erp-finance-gl/pom.xml` declares a direct `<dependency>` on
  `erp-masterdata` (comment: *"Masterdata Module for Lookup Services (Rule
  23)"*) — this is not the common-utils category, it is one business module
  depending directly on another's jar, the exact thing the `*Client`+REST
  rule exists to prevent everywhere else. In practice this is not yet a
  precedent either way: `erp-finance-gl` has **no source code at all**
  (`erp-finance-gl/**/*.java` returns zero files) — it's a scaffolded,
  not-yet-implemented module. Whether it ends up injecting `erp-masterdata`
  beans directly or going the REST-loopback route is an open question this
  module will eventually have to answer, and it's worth the architect
  noting that the pom itself already anticipates direct injection as the
  intended answer for this one pairing.

---

## 5. Question 2 — Recommendation and Accepted Tradeoffs

**Recommendation:** Consolidation into a single pom (package-by-feature
folders replacing pom-per-module) is justified by the evidence in §4 — the
claimed benefits are largely unrealized or already circumvented, and the
real overhead (8 duplicated dependency lists) is a genuine, if modest, tax.
This is a weaker, more conditional recommendation than Question 1's, and it
should **not** be adopted on its own: consolidating removes even the soft,
partial `import`-level guard that exists today, and this codebase has
nothing to replace it with. **Adopt consolidation only paired with adding an
ArchUnit test suite** (confirmed absent today — zero matches for
`ArchUnit`/`Modulith` anywhere in the repo outside the two prior governance
reports discussing them) that encodes the same boundary rules the pom
structure was assumed to enforce (e.g. "no class in package
`com.example.erp.org` may reference a class in `com.example.security`
except via the designated interface package") — this is strictly cheaper to
add than it sounds, a few `ArchRuleDefinition` classes in a single test
module, and it would be a **strictly stronger** guarantee than what
multi-module poms give today, since it can also catch the SpEL/reflection
bypass in §4.2 by pattern-matching the string literal, not just `import`
statements.

**Accepted tradeoffs:**

1. Losing the (already partial) `import`-level compiler guard the instant
   the ArchUnit suite is not yet in place — the two changes should land
   together, not sequentially with a gap.
2. `erp-finance-gl` → `erp-masterdata`'s real dependency, and any future
   pairing like it, becomes package-visibility-only enforced instead of
   Maven-artifact-boundary enforced — ArchUnit rules must explicitly cover
   this pairing too, not just the security/notification/org/masterdata set
   this report focused on.
3. Losing the theoretical (if currently unused) option of ever running
   `mvn test -pl erp-security` in isolation once tests exist — a single pom
   can still filter by package/class pattern with Surefire, but that's a
   different mechanism than module-scoped test execution and should be
   verified to work acceptably before committing to consolidation.
4. This is the larger, more disruptive of the two changes (touches every
   module's directory layout and every existing import path) — it should be
   sequenced *after* Question 1's change, not concurrently, to keep the
   change surface reviewable.

---

## 6. Deeper Observations (Part B)

- **PERMANENT EXCEPTION modules (Security, MasterData Lookup) are the ones
  most affected by both changes.** `erp-security` and `erp-masterdata` are
  called out in governance as carve-outs the project has committed not to
  restructure internally. Neither recommendation here asks to change either
  module's *internals* — Question 1 only changes how *other* modules reach
  them (call mechanism at the boundary), and Question 2 only changes where
  their `pom.xml`/directory lives, not their code. Worth the architect
  double-checking this reading holds if "PERMANENT EXCEPTION" was intended
  to freeze more than internals.
- **Database transaction isolation risk from §2.1 generalizes beyond the 5
  call sites evaluated.** If direct injection is adopted, every *future*
  cross-module call (not just these 5) inherits the same "must declare
  propagation explicitly" obligation. This should be written into whatever
  successor guidance replaces the "Cross-Module Calls (XM)" skill section
  (see §7) as a first-class rule, not an implicit expectation.
- **The retry/outbox design's own placement analysis already anticipated
  this tension.** The prior design report (§A.3) explicitly flagged that
  Option A/B/C's outbox mechanism is scoped as "an extension of the existing
  `*Client`+loopback-REST pattern, not a replacement of it." This report's
  Question 1 finding effectively revisits that boundary from first
  principles and reaches a different, unconstrained answer for these 5 call
  sites specifically — the two documents are not in conflict, they were
  scoped differently on purpose (one assumed REST loopback as fixed, this
  one didn't).
- **`erp-common-utils`'s existing JPA dependency (`jakarta.persistence-api`,
  `hibernate-core`, both `provided` scope, confirmed in its `pom.xml`)
  already resolves Open Governance Item #2** from the prior design report —
  it did not need a new dependency to define `FailedCallRecordBase` as a
  `@MappedSuperclass`; this was already possible with what was there. Worth
  closing that open item explicitly since this investigation happened to
  verify it.
- **No CI pipeline was reviewed in this session** — if one exists and
  currently parallelizes or scopes work by Maven module, that's a concrete
  data point Question 2's build-time analysis in §4.3 did not have access
  to, and it should be checked before finalizing the pom-consolidation
  decision.

---

## 7. If Adopted — What Would Need to Change (Named Only)

This section names artifacts, not a migration plan or code.

**If Question 1 is adopted:**
- `governance/.github/skills/backend/create-service/SKILL.md`'s
  "Cross-Module Calls (XM)" section
- The 5 `*Client` classes and their 4 `*RetryGateway` companions
- `SecFailedCallRecordStore`/`NotificationFailedCallRecordStore` and their
  registered `RetryableOperation` callbacks (only the registrations tied to
  these 5 call sites — not the generic mechanism itself)
- `DispatchAuthContext` (or its direct-injection successor for
  `SecurityContext` propagation across async boundaries)
- `create-repository/SKILL.md` line 50 (restates the same split being
  revisited)

**If Question 2 is adopted:**
- Root `pom.xml` and all 8 child `pom.xml` files
- `governance/master-registry.md` and `modules-registry.json` (module
  references)
- A new ArchUnit test module/suite (does not exist today)
- `governance/.github/skills/backend/create-service/SKILL.md` and
  `create-repository/SKILL.md` (any module-layout-specific instructions)
- `governance/governance-tools/` scripts, if any hardcode per-module paths
  (not checked in this session — flag for verification before implementation)

---

## 8. Non-Self-Executing

**This recommendation is not self-executing.** No code, `pom.xml`, or
skill-text file was modified in this session. Both questions require
separate, explicit architect approval, and — if approved — a separate
implementation session for each (they are independent changes and need not
be approved or implemented together). This document's job ends here.
