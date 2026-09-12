# SYSTEM TEST — FULL CROSS-MODULE FLOW — ERP Platform (all six modules)
══════════════════════════════════════════════════════════════════
Status  : convenience document, NOT a governed test-gen artifact — it mints no `TC-*`
          id, owns no atom, is not split/verified/delivered, and is not read by any
          gate. Every scenario below is assembled from `TC-*` ids that already exist in
          a committed module test plan; **nothing here is invented**. That rule is what
          keeps this document safe to rewrite: if a claim below is wrong, the fix is to
          correct it against the plans, never to add a test obligation here.
Purpose : `system-test-index-erp.md` (the governed platform rollup) deliberately "never
          restates TC content — every row is an ID reference". This document is the
          missing narrative: what actually has to work, end to end, for the platform to
          be usable — and, since this revision, the **order to run the real mechanisms
          in** so that the whole system is exercised sequentially rather than
          module-by-module in isolation.
Sources : backend-test-plan-{sec,mdl,fin}.md · P3_5_BE/backend-test-plan.md for
          {CU,NOTIF,FILE} · system-test-index-erp.md · the delivered code, read directly
          where a claim below depends on it.
Revised : 2026-09-12 — scope widened from three modules to six, execution model added,
          and **six factual errors corrected**. See §0 before trusting any prior copy.
══════════════════════════════════════════════════════════════════

## §0 — What this revision changed, and why

Governance forbids deleting a claim to tidy a document: when a claim becomes false, state
what is true now and why it changed. The previous version was written against the platform
as it stood before the 2026-09-12 FIN work, and six of its statements no longer hold.

| # | Previous claim | Why it was wrong | What is true now |
|---|---|---|---|
| 1 | Flow 6: MDL unreachable yields "a defined `FIN-503`" | **`FIN-503` does not exist.** It is in no error catalog, no `FinErrorCodes` constant, and no api-doc. Nothing ever returned it. | The degraded read surfaces as **`FIN-400-INVALID-LOOKUP`** — verified live on 2026-09-12, and asserted by TC-FIN-047. |
| 2 | Flow 4: the accountant's denial surfaces as "**TC-FIN-038**'s `FIN-403-SOD-VIOLATION`" | `FIN-403-SOD-VIOLATION` has **no throw site left anywhere in `com.erp.fin`**. `FinSeparationOfDutiesService` and `FiscalPeriodDomain.assertCanHardClose` were both deleted by a recorded human decision; the constant and bundle rows survive, unreachable. | The denial is **`FIN-403-FORBIDDEN`**, raised by the `@PreAuthorize` gate and re-raised by `FinForbiddenAdvisor`. TC-FIN-038's own block was REVISED 2026-09-12 to say exactly this. |
| 3 | Flow 2 step 5 / Flow 4 step 3: the controller must hold the close permission "**never** the creation permission", and that exclusivity is a precondition | The global user-set disjointness check was deleted by a recorded human decision, and `V30__fin_sys_admin_close_approve_grant.sql` then deliberately granted **both** codes to SYS_ADMIN. The old exclusivity would fail today. | RULE-FIN-015 is satisfied by the **permission being distinct**, not by disjoint user sets. A caller holding both CAN close — that is now TC-FIN-061, kept as the inverted regression guard for the change. |
| 4 | Flow 5 step 4: reverse "the (now-VOID) original" a second time — **TC-FIN-030** | Two errors. Classic reversal leaves the original **POSTED**; there is no VOID path (ENT-FIN-004: "no VOID path"). And TC-FIN-030 is a different case — rejecting reversal of a *DRAFT* entry. | Double-reversal is **TC-FIN-048**. TC-FIN-030 belongs to the DRAFT case and is listed separately in Flow 5. |
| 5 | Flow 6: "the platform's only **two** cross-module reads" | There are **five** code-level cross-module edges, not two — see §2. The previous count silently equated "governed XM atoms" (of which there are indeed two) with "cross-module reads in the code" (of which there are five). | §2 states both numbers and keeps them distinct, because they answer different questions. |
| 6 | Scope: SEC → MDL → FIN only | CU, NOTIF and FILE are backend-complete and each carries a committed test plan (13 / 16 / 20 cases). Omitting them left ~24% of the platform's cases outside every system-level narrative. | All six modules are in scope — §1, and Flows 7-9. |

Also recorded here, not silently fixed, because it is a finding rather than an editing error:
**TC-SEC-033 is no longer blocked.** `SecCoverageIntegrationTest` states it cannot be covered
"because `com.erp.fin` does not exist anywhere under `src/main/java`". That was true when
written; it is false now — FIN ships 9 controllers and 37 live operations. TC-SEC-033 is
therefore a live, newly-unblocked gap, and Flow 2 step 7 depends on it. See §6.

---

## §1 — Scope: the whole platform, and what this document can honestly claim

Six modules, all backend-complete, **207 test-case ids** committed across them:

| Module | Plan | Id form | Ids | Governed rollup? |
|---|---|---|---|---|
| SEC | `test_gen/backend-test-plan-sec.md` | `TC-SEC-nnn` | 35 | yes |
| MDL | `test_gen/backend-test-plan-mdl.md` | `TC-MDL-nnn` | 14 | yes |
| FIN | `test_gen/backend-test-plan-fin.md` | `TC-FIN-nnn` | 109 (108 in force; TC-FIN-091 RETIRED) | yes |
| CU | `P3_5_BE/backend-test-plan.md` | `TC-BE-CU-nnn` | 13 | **no** |
| NOTIF | `P3_5_BE/backend-test-plan.md` | `TC-BE-NOTIF-nnn` | 16 | **no** |
| FILE | `P3_5_BE/backend-test-plan.md` | `TC-BE-FILE-nnn` | 20 | **no** |
| | | **Total** | **207 (206 in force)** | |

Two things follow, and both are stated plainly rather than glossed:

**The id form differs by path.** CU/NOTIF/FILE are Legacy Path modules and use `TC-BE-<MOD>-nnn`,
not `TC-<MOD>-nnn`. A search for `TC-CU-*` finds nothing and must not be read as "CU has no
tests". Their plans also live at `P3_5_BE/backend-test-plan.md`, not `test_gen/`.

**`system-test-index-erp.md` rolls up only SEC, MDL and FIN** — "every module with a committed
version". CU/NOTIF/FILE are outside it. So the governed coverage percentages there describe
three modules, not the platform. This document is the only place the other three appear in a
system-level view, which is precisely why the omission mattered.

### What a flow document can and cannot do

A narrative cannot meaningfully walk 206 cases, and claiming otherwise would be the kind of
green-looking dishonesty this platform's own test governance exists to prevent. So the
document is split by what each part can actually support:

- **Part A (§3)** — the sequential run order over the mechanisms that really exist. This is
  what covers every module's own case set, because each mechanism already does.
- **Part B (§4)** — the cross-module flows. These are the journeys **no per-module test can
  catch**, because each one spans two or more modules' state. This is where the value is.
- **Part C (§5)** — the ledger: which mechanism owns which module's cases, and which specific
  ids the flows touch. Anything not covered is named in §6, not omitted.

---

## §2 — The real dependency graph (verified in code, 2026-09-12)

Two different questions get confused here, so both answers are given.

**Governed XM atoms — 2.** Only `XM-MDL-001` (MDL → SEC) and `XM-FIN-001` (FIN → MDL) are
declared XM atoms with TC coverage in the governed rollup. `XM-FIN-002` (FIN → SEC) was
**removed**: `FinSeparationOfDutiesService`, FIN's only consumer of `SecUserDirectoryApi`, was
deleted. The single remaining mention of `com.erp.sec.crossmodule` under `com.erp.fin` is a
**comment** in `FiscalPeriodDomain.java:117` recording that removal — not an import. A grep
that counts it as a dependency is reading a tombstone as a live edge.

**Code-level cross-module reads — 5.** Verified by which packages import another module's
`crossmodule`:

| Consumer | Provider | Where | Governed XM atom? |
|---|---|---|---|
| MDL | SEC | owner-module validation on every lookup-type write | ✓ `XM-MDL-001` |
| FIN | MDL | lookup validation on every lookup-backed field | ✓ `XM-FIN-001` |
| SEC | NOTIF | `PasswordResetService` sends the reset notification | ✗ legacy — no XM atom |
| NOTIF | MDL | `NotificationLookupService` | ✗ legacy — no XM atom |
| FILE | MDL | `FileLookupService` | ✗ legacy — no XM atom |
| CU | — | consumes no other module | n/a |

The three unlabelled edges are real behaviour with no governed XM atom, because their consumer
or provider predates the current model. They are exercised by Flows 7-9 below. **This is not a
licence to mint XM atoms for them** — that would be inventing governance content. It is a
statement that a system test must cover them even though the rollup does not know about them.

Deployment order follows the graph: **SEC → MDL → {CU, NOTIF, FILE} → FIN**. CU is orderless
(no dependencies) and can be provisioned at any point.

---

## §3 — Part A: sequential execution model

The mechanisms that exist today, in the order to run them. Each step's artifacts are real
files; none is aspirational.

| # | Step | Mechanism | Covers |
|---|---|---|---|
| 0 | Provision | `docker compose --env-file .env -f docker/docker-compose.yml up -d`, then `mvn spring-boot:run` (JDK 25 at `$JAVA_HOME/Contents/Home`; `set -a && . ./.env && set +a` first). Flyway applies every migration at startup. | the bootstrap itself — a migration failure stops everything downstream |
| 1 | Health + group discovery | `GET /actuator/health` = 200, and `/v3/api-docs/swagger-config` lists **six** groups (`sec, cu, notif, file, mdl, fin`). | catches a module whose springdoc group was never registered — exactly the FIN defect found and fixed on 2026-09-12 |
| 2 | Regenerate api-docs | `governance-tools/api-doc-generator`, `--function generate`, per module | a stale doc silently tests the wrong contract; regenerate before asserting against it |
| 3 | **FIN** — api-verify | `python3 governance/modules/FIN/test-api/test_fin_apis.py` | 124 assertions, 100 FIN cases |
| 4 | **FIN** — JUnit | `mvn -Dtest='Fin*CoverageIntegrationTest' test` | 10 tests, the 8 FIN cases no HTTP client can reach |
| 5 | **SEC** — api-verify | `python3 governance/modules/SEC/test-api/test_sec_apis.py` | SEC's HTTP surface |
| 6 | **SEC** — JUnit | `mvn -Dtest=SecCoverageIntegrationTest test` | the SEC cases needing in-process access |
| 7 | **MDL** | `governance/modules/MDL/test-api/test_mdl_apis.py` | MDL's surface, including XM-MDL-001 |
| 8 | **CU / NOTIF / FILE** | no api-verify script exists yet — see §6 | — |
| 9 | Cross-module flows | Part B, §4 | the journeys no single module owns |
| 10 | Architecture | `mvn -Dtest='*ArchTest' test` | `CrossModuleBoundaryArchTest` — that §2's graph is the *only* graph |

**Why FIN runs before SEC** (steps 3-4 before 5-6), which looks backwards against the
deployment order: the FIN suites are the ones that create and tear down the most state, and
they assume the SEC bootstrap `admin` exists but nothing else. Running SEC's suites first
leaves users, roles and grants behind (SEC has almost no hard-delete endpoints), which changes
what FIN's own permission assertions see. If a step-ordering change is ever needed, this is the
constraint to preserve, not the deployment order.

**Isolation.** Steps 3-7 are not idempotent in the same way: the JUnit suites roll back
(class-level `@Transactional`) and leave nothing; the api-verify scripts **cannot**, because FIN
and SEC publish no hard-delete endpoints, so their rows survive as deactivated records. Each
script's own problems report carries a `Surviving records` list. Treat a full system run as
**additive to the dev database** and size expectations accordingly — a fresh environment is the
only way to get a clean before/after comparison.

---

## §4 — Part B: the cross-module flows

### Flow 1 — Platform bootstrap (module & lookup onboarding)
The order the modules must be deployed in, and what each registers as it arrives.

1. **SEC deploys first** — it has no upstream dependency (ROOT).
2. **MDL deploys second** and, at its first write, validates its owner-module references
   against SEC — **TC-MDL-014** (graceful degradation when SEC is unreachable at that moment;
   the happy path is implicit in every other MDL case naming a real owner module, e.g.
   **TC-MDL-001**).
3. **CU, NOTIF and FILE** may deploy next. NOTIF and FILE each validate their own lookup types
   against MDL from this point on (§2); CU depends on nothing.
4. **FIN deploys last**:
   - registers its module + **12 screens** + **27 actions** into SEC — **TC-FIN-044**;
   - registers its **13 lookup types** into MDL — **TC-FIN-045**, two of them deliberately
     seeded empty (`ACCOUNTING_EVENT_TYPE`, `PAYMENT_METHOD` — host-specific by design, not an
     omission; an event type must be added through MDL before any FIN event rule can exist);
   - every subsequent FIN write touching a lookup-backed field validates against MDL —
     **TC-FIN-047** covers the degraded case.

**Pass/fail**: TC-MDL-014, TC-FIN-044, TC-FIN-045, TC-FIN-047 all pass, in this order, against a
freshly provisioned environment. The screen/action counts in step 4 are exact and asserted by
identity, not merely by count.

### Flow 2 — Identity, RBAC and first FIN access
Ties SEC's authorization model to FIN actually becoming reachable.

1. A prospective user signs up — **TC-SEC-003**.
2. An administrator approves them — **TC-SEC-004** — a real `User` now exists.
3. The administrator creates the two roles this platform's close model uses:
   `FIN Accountant` and `FIN Controller` (role creation is a precondition, not its own case).
4. `FIN Accountant` is granted the FIN module, the Journal Entries screen, and CREATE —
   **TC-SEC-012**, with **TC-SEC-013** and **TC-SEC-014** as the out-of-order violations, and
   **TC-SEC-030** (VIEW must exist before any other action takes effect).
5. `FIN Controller` is granted the FIN module, the Periods screen, and
   `PERM_FIN_PERIODS_CLOSE_APPROVE`. **It is no longer required that this role lack
   `PERM_FIN_JOURNAL_ENTRIES_CREATE`** — see §0 item 3. Exclusivity is not the invariant; the
   permission being *distinct* is.
6. The accountant logs in — **TC-SEC-001** — and their menu shows exactly FIN → Journal
   Entries — **TC-SEC-021**, **TC-SEC-032**.
7. The accountant opens the Periods screen directly by URL — denied regardless of the menu —
   **TC-SEC-033**. ⚠ This case is currently **uncovered**; see §6.

### Flow 3 — Chart setup → event-driven posting → live reporting
The accounting spine, configuration to a number on a report.

1. Two accounts (an ASSET leaf, a REVENUE leaf) and a dimension with two values —
   **TC-FIN-001**, **TC-FIN-004**, **TC-FIN-005**.
2. An event-type rule with a percentage/remainder distribution — **TC-FIN-007**,
   **TC-FIN-008**; the remainder-count guard refuses a bad set — **TC-FIN-009**.
   *Ordering note*: the remainder line must be added **first**. The guard runs over the
   prospective line set at every add, so a percentage line added before any remainder line is
   refused — correctly.
3. A canonical event arrives; the engine builds the entry from the rule — **TC-FIN-010** — with
   the remainder computed exactly, **per side** — **TC-FIN-012**, **TC-FIN-054**.
4. The same event reference is replayed — rejected as a duplicate, not double-posted —
   **TC-FIN-011**. This is the platform's real idempotency guarantee, not a unit assertion.
5. Validation runs and the entry posts directly, no approval hop — **TC-FIN-017** — and is
   immediately immutable — **TC-FIN-016**.
6. The account ledger shows the posting immediately, computed live — **TC-FIN-039**.
7. The trial balance balances by construction — **TC-FIN-040**.
8. Drill from a statement line down to the entry and its event reference — **TC-FIN-046**.

**Pass/fail**: every case above passes **in sequence against the same entities** created in
steps 1-2 — not fresh fixtures per step. That is what proves the pieces compose.

### Flow 4 — Separation of duties at period close
The scenario that most depends on SEC, FIN and Flow 2's role setup being right together.

1. The accountant posts entries during the month via Flow 3's mechanics.
2. The accountant attempts to hard-close the period — denied: they hold no
   `PERM_FIN_PERIODS_CLOSE_APPROVE`. The denial is **`FIN-403-FORBIDDEN`** (not
   `FIN-403-SOD-VIOLATION`, see §0 item 2) — **TC-FIN-038**.
3. The controller hard-closes the period — **TC-FIN-034**, **TC-FIN-059** (any holder of the
   close permission may do so), **TC-FIN-062** (the two-word `CLOSE_APPROVE` action resolves),
   **TC-FIN-037** (the approval is recorded distinctly from every entry's `createdBy`).
4. A late entry against the now-closed period is rejected even though the period was Open when
   the form was opened — **TC-FIN-020**.
5. Year-end close, once every period is hard-closed — **TC-FIN-036** — then the new year's
   balance sheet (opening balances match the prior year's closing — **TC-FIN-041**) and income
   statement (opens at zero — **TC-FIN-042**).
6. The same close attempted by the accountant on the *year-end* endpoint is denied identically
   — **TC-FIN-060** — and a caller holding **both** permissions succeeds — **TC-FIN-061**.

**Pass/fail**: this is the highest-value regression in the platform. If SEC's role model,
FIN's permission gate (`RULE-FIN-015`) or the year-end arithmetic ever drift apart, this flow
catches it before any narrower per-module test does.

### Flow 5 — Correction without loss (reversal, not deletion)
1. Post a manual entry — **TC-FIN-014**.
2. Attempt to delete it — no route exists; it is refused **405** — **TC-FIN-016**.
3. Reverse it — a new, linked, exactly mirrored entry posts, and **the original stays POSTED**
   (there is no VOID path) — **TC-FIN-028**.
4. Attempt to reverse it a second time — refused — **TC-FIN-048** (not TC-FIN-030; see §0
   item 4).
5. Attempt to reverse a **DRAFT** entry — refused — **TC-FIN-030**.
6. Reverse an entry whose own period has since been hard-closed — the reversal lands in an open
   period, not the closed one — **TC-FIN-029**.

### Flow 6 — Degraded-dependency resilience
Every cross-module read must fail *safely*, never opaquely. §2 lists five edges; three of them
have no governed XM atom but still must not leak a stack trace.

1. MDL unreachable, create a FIN account (needs `ACCOUNT_TYPE`) → **`FIN-400-INVALID-LOOKUP`**,
   a defined FIN code, never an MDL code and never a stack trace — **TC-FIN-047**.
   (§0 item 1: this is *not* `FIN-503`, which does not exist.)
2. SEC unreachable, register a new MDL lookup type (needs owner-module validation) → a defined
   error — **TC-MDL-014**.
3. MDL unreachable, exercise a NOTIF lookup — **TC-BE-NOTIF-016**; and a FILE lookup —
   **TC-BE-FILE-020**. Ungoverned edges, real behaviour.
4. NOTIF unreachable, trigger a SEC password reset (`PasswordResetService` dispatches through
   NOTIF) — the reset itself must not fail because the notification did. ⚠ No committed case
   asserts this; see §6.
5. *(Architecture, not a test failure)* SEC has no upstream to degrade against. If SEC is down
   nothing authenticates — the by-design single point of trust for identity.

### Flow 7 — Notification delivery (NOTIF)
1. Create a bilingual template — **TC-BE-NOTIF-005**; a missing bilingual body is refused —
   **TC-BE-NOTIF-006**.
2. Channels are registered with unique codes — **TC-BE-NOTIF-008**, duplicates refused —
   **TC-BE-NOTIF-009**.
3. Dispatch fans out exactly one log per channel — **TC-BE-NOTIF-001**, **TC-BE-NOTIF-011**.
4. A transient provider failure retries and succeeds — **TC-BE-NOTIF-002**; exhausted retries
   land in FAILED with the error recorded — **TC-BE-NOTIF-003**.
5. A disabled channel short-circuits: no retry, no provider call — **TC-BE-NOTIF-004**,
   **TC-BE-NOTIF-015**; an inactive recipient is skipped — **TC-BE-NOTIF-010**.
6. Logs are queryable, empty results return 200 not 404 — **TC-BE-NOTIF-012**,
   **TC-BE-NOTIF-013**.

### Flow 8 — Document lifecycle (FILE)
1. Upload within the size limit — **TC-BE-FILE-001**, **TC-BE-FILE-013**; oversized refused —
   **TC-BE-FILE-002**; disallowed content type refused — **TC-BE-FILE-004**.
2. Ownership fields are mandatory — **TC-BE-FILE-008**, **TC-BE-FILE-009** — which is what makes
   a file attributable to a business record at all.
3. A single-use access token is issued — **TC-BE-FILE-014** — download consumes it —
   **TC-BE-FILE-005**, **TC-BE-FILE-015** — and a reused or expired token is refused —
   **TC-BE-FILE-006**.
4. Metadata never carries the bytes — **TC-BE-FILE-016**.
5. Archive is a soft delete: the bytes are retained — **TC-BE-FILE-010**, **TC-BE-FILE-018**.

### Flow 9 — Platform configuration (CU)
CU consumes no other module, so it is a spine of its own rather than a cross-module flow — it is
listed to keep the platform view complete.
1. Create a configuration — **TC-BE-CU-001**; duplicate key refused — **TC-BE-CU-002**.
2. The key is immutable; the value is not — **TC-BE-CU-005**, **TC-BE-CU-006**,
   **TC-BE-CU-008**.
3. Read by key — **TC-BE-CU-010**; unknown key 404 — **TC-BE-CU-011**; empty search 200 not 404
   — **TC-BE-CU-012**.
4. Injection resistance on the search surface — **TC-BE-CU-013**.

---

## §5 — Part C: coverage ledger

| Module | Cases | Mechanism that owns them | Result (run 2026-09-12) |
|---|---|---|---|
| FIN | 108 in force | `test_fin_apis.py` (100) + `Fin*CoverageIntegrationTest` (8) | **124 + 10 assertions, 0 failed** — 108/108 covered |
| SEC | 35 | `test_sec_apis.py` + `SecCoverageIntegrationTest` | **67 passed, 2 failed** — both pre-existing, §6.1 / §6.7 |
| MDL | 14 | `test_mdl_apis.py` | **30 passed, 0 failed** |
| CU | 13 | `test_cu_apis.py` — **generated 2026-09-12** | **19 passed, 3 failed** — 13/13 covered |
| NOTIF | 16 | `test_notif_apis.py` — **generated 2026-09-12** | **38 passed, 2 failed** |
| FILE | 20 | `test_file_apis.py` — **generated 2026-09-12** | **41 passed, 1 failed** — 20/20 exercised, 2 partial |
| | | **Platform total** | **319 api-verify assertions + 10 JUnit tests · 8 failures** |

The three missing runners named in the previous revision's §6 now exist, so every module in the
platform has an automated mechanism in the §3 order. All three were generated at **Full tier** —
each of CU, NOTIF and FILE carries a `P3_5_BE/test-execution-manifest.md`, so their negatives are
stage-C mapped from real RULE→code→TC triples rather than self-derived.

**Ids referenced by the flows in §4**: **83 distinct cases** across all six modules — FIN 34,
FILE 14, NOTIF 14, SEC 10, CU 9, MDL 2. That is ~40% of the 206 in force, and the proportions are
not a ranking of importance: FIN simply has 109 cases to the others' 13-35, while CU/NOTIF/FILE
appear almost in full because their plans are small enough to walk end to end.

The flows are deliberately a *spine*, not a re-listing. The remaining ~123 cases are owned by
their module's own mechanism (the table above), which is where they belong — a case appearing in
no flow is **not** uncovered. A case in no flow **and** no mechanism is, and every one of those is
named in §6.

---

## §6 — Open items

Each is a real gap or a real question, not a placeholder. None is invented, and none is closed
here — this document owns no atom and may not resolve them.

1. **TC-SEC-033 is uncovered and no longer blocked.** `SecCoverageIntegrationTest` excludes it
   on the stated grounds that `com.erp.fin` does not exist. FIN now ships 9 controllers and 37
   operations, so the premise is stale. Flow 2 step 7 and Flow 4 step 2 both lean on it. It is
   the platform's only assertion that the module gate holds on **direct URL access**, independent
   of the menu — the menu-hiding cases (TC-SEC-021, TC-SEC-032) do not substitute for it.
2. ~~**CU, NOTIF and FILE have no api-verify script**~~ — **CLOSED 2026-09-12.** All three were
   generated at Full tier and run: CU 19/3, NOTIF 38/2, FILE 41/1. Every module in the platform
   now has an automated mechanism in the §3 order. What that first run exposed is recorded as
   items 7-10 below — the runners were missing, and so was the knowledge of what they would find.
3. **Flow 6 step 4 has no committed case.** SEC → NOTIF is a real code edge
   (`PasswordResetService`), and a password reset should not fail because a notification did.
   No `TC-*` asserts it. Writing one is a test-gen act for SEC's or NOTIF's plan — not
   something this document may mint.
4. **Three cross-module edges have no governed XM atom** (SEC→NOTIF, NOTIF→MDL, FILE→MDL, §2).
   Whether the Legacy Path modules should retroactively declare XM atoms is a governance
   question with a standing answer — Legacy Path modules keep their structure untouched — so
   this is recorded as a known asymmetry, not a defect to fix.
5. **`system-test-index-erp.md` covers three modules of six.** Its percentages are therefore
   platform-shaped but not platform-wide. Widening it is a governed generation step, not an
   edit to this file.
7. **Four governed error codes are unreachable over HTTP — one root cause, three modules.**
   `APP_CONFIGURATION_FIELDS_REQUIRED` (CU), `NOTIF_TEMPLATE_BILINGUAL_REQUIRED` (NOTIF, two
   cases) and `FILE_DOCUMENT_OWNERSHIP_REQUIRED` (FILE) are each published in their module's
   api-docs "Known Error Codes" table and named in a manifest RULE→code→TC triple, but no caller
   can ever receive them: the DTO's own `@NotBlank` bean validation rejects at the controller
   boundary before the domain rule can throw, so the response is always the generic
   `VALIDATION_ERROR` envelope. The HTTP **status** is right in every case (400) and the field is
   named in `fieldErrors`; only the governed code is unreachable. Whether the fix is to relax the
   bean validation so the domain rule owns the refusal, or to strike those codes from the
   catalogs as structurally unreachable, is a decision for a human — four TC assertions currently
   fail against it and were deliberately left failing.

8. **CU, NOTIF and FILE enforce no permissions at all.** Every
   `@PreAuthorize(hasAuthority(...))` in those three modules is commented out behind
   `TODO: SEC-PENDING — re-add ... once the new SEC module ships PermissionConstants`. That
   blocker is **half-lifted and nobody noticed**: `com.erp.sec.permission.PermissionConstants`
   ships 43 constants, 27 of them `PERM_FIN_*` — and **zero** for `CONFIG_*`, `PERM_NOTIF_*` or
   `PERM_FILE_*`. FIN's gates were restored; the other three were not. Consequence, verified by
   three independent runs: **any authenticated user can create, update and delete platform
   configuration, notification templates and channels, and files.** Anonymous access is correctly
   401. This is the platform's largest open security gap and it is invisible to per-module
   testing, because a module with no permission to check has nothing to fail.

9. **A likely platform-wide response defect: update endpoints return a stale `updatedAt`.**
   Reproduced independently on CU over three successive updates — the PUT response carries the
   *pre-update* timestamp while the row holds the new one, because `mapper.toResponse(saved)`
   reads the entity before `AuditEntityListener`'s `@PreUpdate` stamps at flush. The persisted
   value is always correct. Map-before-commit is the `build-create-service` house pattern, so
   every module's update endpoint is worth checking against this, not just CU's.

10. **Two silent-acceptance behaviours, same shape as item 9's dishonesty.** CU accepts a
   `configKey` change and answers 200 while ignoring it — the client is told a rename succeeded
   that did not happen (the key genuinely is immutable, which is the point: the invariant holds,
   the report of it lies). And an unknown search *filter* field is silently ignored platform-wide
   while an unknown *sort* field is properly rejected — already recorded against FIN, confirmed
   again on CU and MDL.

11. **Document location.** This file sits in `governance/modules/`, beside the governed
   `system-test-index-erp.md`. `CLAUDE.md` routes purely informational markdown to
   `governance/project-artifacts/`. This revision makes the document *drive* a run order (§3),
   which is an argument for keeping it here — but it is a judgement call a human should confirm
   rather than one settled silently by whoever edited last.

══════════════════════════════════════════════════════════════════
Every `TC-*` referenced above exists, verbatim, in its module's committed plan. This document
adds no test obligation — it shows the order that makes the existing ones tell the platform's
actual story, and names what that story cannot yet tell.
