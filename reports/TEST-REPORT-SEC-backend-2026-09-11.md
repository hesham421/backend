# TEST REPORT — SEC (backend) — 2026-09-11

Module-scoped digest for the `TEST-PLAN-BE` phase (subs `RULE-SCENARIOS`,
`API-SCENARIOS`). TestSprite's own raw report/plan/PRD/results for this run
are archived, unedited except for the completed report template, at
`governance/testsprite/runs/2026-09-11-backend-2/`. The prior
(invalidated) run's bundle remains untouched at
`governance/testsprite/runs/2026-09-11-backend/`. The 7 generated test
files for this run are archived at
`governance/modules/SEC/testsprite/tests/TC00[1-7]_*.py` (the prior run's
single `TC001_post_apiv1secauthlogin_with_valid_credentials.py` was removed —
superseded by this run's `TC001_post_api_v1_sec_auth_login_with_valid_credentials.py`).

## THIS REPORT SUPERSEDES THE PRIOR VERSION — read before the table

The prior version of this report (produced 2026-09-11 earlier the same day)
recorded 1/35 exercised, 0 PASS, 1 FAIL, 34 GAP, because `POST
/api/v1/sec/auth/login` and `/auth/signup` both returned a bare HTTP 500
`INTERNAL_ERROR` for every request at that time. That was a **false alarm**,
not a real defect: the orchestrator independently confirmed it was caused by
`mvn spring-boot:run`'s JVM serving requests against a `target/classes`
directory that had been emptied/rewritten out from under it while live, and
resolved it by killing the process, rebuilding, and restarting fresh —
verified before this dispatch began (login returned 200 with a valid JWT,
signup returned 201).

**A second occurrence of the identical environmental issue class hit mid-run
during this dispatch's own TestSprite execution.** At 16:54–16:55 local,
while the 7 generated test scripts were executing against the (at that
point) already-verified-fresh app, `target/classes` was found completely
emptied again (`find target/classes -name '*.class' | wc -l` → **0**, versus
377 expected and 245 source files) — most likely another concurrent session
sharing this checkout recompiling/cleaning at the same moment (this repo's
own memory notes record that several sessions can share this checkout
concurrently). This produced the exact same failure signature as the
original incident — `java.lang.ClassNotFoundException` /
`NoClassDefFoundError` on Lombok builder inner classes
(`ApiError$ApiErrorBuilder`, `UserCreateRequest$UserCreateRequestBuilder`,
`PasswordResetCompleteRequest$PasswordResetCompleteRequestBuilder`) — and
corrupted TC002/TC004/TC005/TC006/TC007's in-dashboard results with bare 500s.

**This was caught and corrected within this same dispatch, not left
standing**: the app was stopped (`kill`), rebuilt (`mvn clean compile` — 377
of 377 expected classes confirmed present), and restarted fresh. Every
previously-500ing scenario (invalid login, direct user creation, invalid
reset-token completion) was independently re-verified via `curl` to return
its correct status/code before any further testing resumed. The 7 generated
test scripts (unchanged — the API surface did not change, only the
environment) were then re-executed directly via `python3` against the
now-stable app. **The results in this report are from that corrected
re-run** — not from either the original invalidated dashboard run or the
mid-run-corrupted dashboard run. No application source code was modified at
any point to produce this outcome.

## FIX ROUND — 2026-09-11 (STEP 4.5, later the same day) — read after the supersession note above

Per `governance/testsprite/TESTSPRITE-GOVERNANCE.md` §5's sanctioned exception
(keeping an existing generated test's assertions/payloads in sync with an
already-correct application contract), the 3 archived scripts flagged above
as TEST_STRUCTURE_FAILURE were reviewed and corrected. **Only TC-SEC-002 is
confirmed re-verified PASSING**; TC-SEC-006/TC-SEC-007 remain FAIL — see why
below. No `src/main/java` file was read for content changes or modified.

- **TC-SEC-002 (`TC002_post_api_v1_sec_auth_login_with_invalid_credentials.py`)
  — FIXED, RE-RUN CONFIRMED PASSING.** The script asserted 401 for all 5 of
  its credential combinations. A closer check (beyond what the original
  TEST_STRUCTURE_FAILURE entry above identified) found **two**, not one,
  blank-field combinations in the list — `{"username": "", "password":
  "admin"}` **and** `{"username": "admin", "password": ""}` — and both
  correctly return `400 VALIDATION_ERROR` (Bean Validation rejecting the
  blank field before the credential check runs), confirmed live via curl for
  both. The script was restructured into two loops: the 3 genuinely-wrong-
  credential combinations (unknown username / wrong password / both wrong)
  still assert `401 SEC-401-INVALID-CREDENTIALS` unchanged, and the 2
  blank-field combinations now assert `400 VALIDATION_ERROR` with a
  `fieldErrors` entry naming the blank field. `python3 TC002_....py` now
  exits 0.
- **TC-SEC-006 (`TC005_post_api_v1_sec_auth_password_reset_request.py`) —
  FIX APPLIED, RE-RUN BLOCKED, STILL FAIL.** The setup step's `POST
  /api/v1/sec/users` assertion was corrected from `== 200` to `== 201`
  (confirmed live via curl: user creation returns `201 Created`). Re-running
  now gets past that setup step but fails one line later: `POST
  /api/v1/sec/auth/password-reset/request` returned `500 INTERNAL_ERROR`
  instead of the expected `200`. Independently reproduced via curl, outside
  the script entirely, so this is not a script assertion bug. Root cause:
  `target/classes` was found completely emptied (0 of 377 expected `.class`
  files) under the still-running `mvn spring-boot:run` JVM at the time of
  this re-run — the identical environmental-corruption signature already
  documented twice above in this same report (the original invalidated run,
  and the mid-run recurrence during the original TestSprite dispatch), most
  likely another concurrent session sharing this checkout rebuilding at the
  same time. Per this fixing dispatch's brief, the app was **not** killed,
  rebuilt or restarted without checking with the orchestrator first, since
  this checkout is shared with other concurrent sessions. TC-SEC-006 is left
  FAIL, not PASS, pending a rebuild+restart and a clean re-run.
- **TC-SEC-007 (`TC006_post_api_v1_sec_auth_password_reset_complete_with_valid_token.py`)
  — FIX APPLIED, RE-RUN BLOCKED, STILL FAIL, AND A FURTHER STRUCTURAL GAP
  FOUND.** The same setup-step assertion was corrected `200` → `201`. While
  fixing it, a second, previously-masked bug was found and fixed in the same
  script: `user_id = create_resp.json()["data"]["id"]` — the created-user
  response has no `id` key (the primary-key field is `userPk`), so this line
  would `KeyError` the moment the setup assertion stopped masking it; changed
  to `["data"]["userPk"]`. Re-running then hit the **same** `target/classes`
  environmental blocker described for TC-SEC-006 above, at the same
  `password-reset/request` call, for the same reason — not restarted, same
  rationale. **Independently of the environmental blocker**, this script has
  a further, pre-existing structural gap that will keep it from reaching
  PASS even once the environment is fixed: later in the script (line ~112 in
  the original), it tries to retrieve the raw reset token via `GET
  /api/v1/sec/test/password-reset-tokens` — a test-only endpoint the
  script's own comments admit is assumed/hypothetical ("we cannot fetch the
  real token... assume a test-only endpoint exists"). No such endpoint
  exists anywhere in `src/main/java` (confirmed via `grep -rn
  "password-reset" src/main/java` — only the real, non-test controller/
  service/repository/entity files, nothing under any `/test/` path). This
  was not part of the sanctioned "200→201" fix and was not touched or
  invented; it is the same class of blocker as TC-SEC-008 below (needs a
  purpose-built DB/token fixture, not obtainable via any documented HTTP
  surface) and is flagged, not fixed.

## FIX ROUND — 2026-09-11 (final environmental recheck, later the same day)

The orchestrator performed one more clean rebuild (`mvn -q -DskipTests
compile`, confirmed 377/377 classes) and restart, then independently
re-verified the two items left open by the FIX ROUND above. Two distinct
outcomes:

- **TC-SEC-006 — CLOSED, PASS.** `python3
  TC005_post_api_v1_sec_auth_password_reset_request.py` exits 0 against the
  freshly rebuilt app. The prior FAIL was purely the environmental
  `target/classes`-wipe issue documented above, not a real defect. Moved
  FAIL → PASS in the coverage table and ratio.
- **TC-SEC-007 — reclassified FAIL → GAP, still not passing, but for a
  genuine, non-environmental reason.** With the environment now stable, the
  script gets past the setup step and the `password-reset/request` call, then
  fails at its own invented `token_lookup_url` GET step (line ~108-128) — no
  such endpoint exists anywhere in this codebase (confirmed via `grep -rn
  "token_lookup_url\|reset.*token" src/main/java/com/erp/sec/controller/`).
  Reset tokens are deliberately never retrievable via any API (email delivery
  only, by design). This is the same structural-gap class as TC-SEC-008 —
  moved from the FAIL count to the GAP count accordingly; not a fixable
  script bug or an application defect.

## Governed-plan note

`governance/modules/SEC/test_gen/backend-test-plan-sec.md`'s own header
states `TC count: 35` (7 RULE-SCENARIOS + 28 API-SCENARIOS, the latter
including TC-SEC-034/TC-SEC-035 added by the 2026-09-11 cross-module
amendment). This report uses 35 as REQUIRED COVERAGE, confirmed by re-reading
the file this run.

## Branch decision

**Branch B (NEW)** — the module's only prior archive was a single TC
(TC-SEC-001) from a run invalidated by an environmental bug, with 34/35
governed TCs never attempted. Per the dispatch brief's guidance, this does
not constitute a stable, complete archive to merely re-run, so a fresh
TestSprite pipeline was run (bootstrap skipped — `testsprite_tests/tmp/config.json`
already existed in `committed` state from the same-day run; code summary,
standardized PRD, and backend test plan were regenerated).

## Tool-capacity constraint (read before the coverage table)

TestSprite's `testsprite_generate_backend_test_plan` produced **7** test
candidates for the *entire backend* this run (SEC + CU + FILE + NOTIF
combined) — all 7 happened to fall under SEC's `/api/v1/sec/auth/*` surface,
so no module-scoping filter was needed. The connected account is on
TestSprite's **Starter** plan with **49** credits
(`testsprite_check_account_info`). This is a large improvement over the
prior (bug-starved) run's single candidate, but still far short of the
35-TC governed plan — 30 governed TCs have no TestSprite counterpart this
run. This is a tool/plan-capacity constraint, not a scoping choice made by
this command and not a product defect.

## GOVERNED PLAN ↔ TESTSPRITE COVERAGE — SEC

| TC-SEC-\<seq\> | traces (AC/REQ/API) | scenario | TestSprite TCnnn | result |
|---|---|---|---|---|
| TC-SEC-001 | AC-SEC-001, REQ-SEC-001, API-SEC-001 | successful login | TC001 | **PASS** |
| TC-SEC-002 | AC-SEC-002, REQ-SEC-002, API-SEC-001 | reject invalid credentials | TC002 | **PASS** (fixed 2026-09-11, see note below) |
| TC-SEC-003 | AC-SEC-003, REQ-SEC-003, API-SEC-002 | submit a sign-up request | TC003 | **PASS** |
| TC-SEC-004 | AC-SEC-004, REQ-SEC-004, API-SEC-011 | approve a sign-up request | ✗ none | GAP |
| TC-SEC-005 | AC-SEC-005, REQ-SEC-005, API-SEC-011 | reject a sign-up request | ✗ none | GAP |
| TC-SEC-006 | AC-SEC-006, REQ-SEC-006, API-SEC-003 | issue a password-reset token | TC005 | **PASS** (confirmed 2026-09-11 after final clean rebuild — prior FAIL was environmental only) |
| TC-SEC-007 | AC-SEC-007, REQ-SEC-007, API-SEC-004 | complete a password reset successfully | TC006 | GAP — see note (reclassified from FAIL 2026-09-11 — script depends on a nonexistent endpoint) |
| TC-SEC-008 | AC-SEC-008, REQ-SEC-008, API-SEC-004 | reject an expired/used reset token (RULE-SEC-006) | TC007 (partial proxy only) | GAP — see note |
| TC-SEC-009 | AC-SEC-009, REQ-SEC-009, API-SEC-006 | create a user directly | ✗ none | GAP |
| TC-SEC-010 | AC-SEC-010, REQ-SEC-010, API-SEC-008 | assign roles to a user | ✗ none | GAP |
| TC-SEC-011 | AC-SEC-011, REQ-SEC-011, API-SEC-009 | deactivate a user terminates its sessions | ✗ none | GAP |
| TC-SEC-012 | AC-SEC-012, REQ-SEC-012, API-SEC-014 | grant a module to a role | ✗ none | GAP |
| TC-SEC-013 | AC-SEC-013, REQ-SEC-013, API-SEC-016 | reject screen grant w/o module grant (RULE-SEC-001) | ✗ none | GAP |
| TC-SEC-014 | AC-SEC-014, REQ-SEC-014, API-SEC-017 | reject action grant w/o screen grant (RULE-SEC-002) | ✗ none | GAP |
| TC-SEC-015 | AC-SEC-015, REQ-SEC-015, API-SEC-015 | cascade-revoke on module-grant revoke (RULE-SEC-003) | ✗ none | GAP |
| TC-SEC-016 | AC-SEC-016, REQ-SEC-016, API-SEC-018 | register a new module | ✗ none | GAP |
| TC-SEC-017 | AC-SEC-017, REQ-SEC-017, API-SEC-019 | register a screen under a registered module | ✗ none | GAP |
| TC-SEC-018 | AC-SEC-018, REQ-SEC-018, API-SEC-019 | reject screen under unregistered module (RULE-SEC-004) | ✗ none | GAP |
| TC-SEC-019 | AC-SEC-019, REQ-SEC-019, API-SEC-020 | register an action under a registered screen | ✗ none | GAP |
| TC-SEC-020 | AC-SEC-020, REQ-SEC-020, API-SEC-017/008 | prevent SoD-conflicting action pair (RULE-SEC-005) | ✗ none | GAP |
| TC-SEC-021 | AC-SEC-021, REQ-SEC-021, API-SEC-027 | menu shows only effective grants | ✗ none | GAP |
| TC-SEC-022 | AC-SEC-022, REQ-SEC-022, API-SEC-022 | dashboard figures computed live | ✗ none | GAP |
| TC-SEC-023 | AC-SEC-023, REQ-SEC-023, API-SEC-022 | hide an ungranted dashboard widget | ✗ none | GAP |
| TC-SEC-024 | AC-SEC-024, REQ-SEC-024, (side-effect) | every security event appends an audit entry | ✗ none | GAP |
| TC-SEC-025 | AC-SEC-025, REQ-SEC-025, API-SEC-023 | search/filter the audit log | ✗ none | GAP |
| TC-SEC-026 | AC-SEC-026, REQ-SEC-026, API-SEC-024 | export the audit log | ✗ none | GAP |
| TC-SEC-027 | AC-SEC-027, REQ-SEC-027, API-SEC-025 | list only non-terminated sessions | ✗ none | GAP |
| TC-SEC-028 | AC-SEC-028, REQ-SEC-028, API-SEC-026 | force-terminate a session | ✗ none | GAP |
| TC-SEC-029 | AC-SEC-029, REQ-SEC-029, API-SEC-003 | optional notification on password reset | ✗ none | GAP |
| TC-SEC-030 | AC-SEC-030, REQ-SEC-030, API-SEC-017 | VIEW required before any other action (RULE-SEC-007) | ✗ none | GAP |
| TC-SEC-031 | AC-SEC-031, REQ-SEC-031, API-SEC-010 | reactivate a disabled user | ✗ none | GAP |
| TC-SEC-032 | AC-SEC-032, REQ-SEC-032, API-SEC-027 | hide an ungranted module from the menu | ✗ none | GAP |
| TC-SEC-033 | AC-SEC-033, REQ-SEC-033, (any FIN endpoint) | module gate enforced on direct access | ✗ none | GAP |
| TC-SEC-034 | AC-SEC-034, REQ-SEC-034, (crossmodule, no API) | cross-module read of a user's contact details | ✗ none (not HTTP-reachable) | GAP — structural |
| TC-SEC-035 | AC-SEC-035, REQ-SEC-035, (crossmodule, no API) | cross-module read of holders of a permission code | ✗ none (not HTTP-reachable) | GAP — structural |

**Extra, non-governed coverage generated this run (not counted in the ratio
below):** TestSprite's `TC004` (`POST /api/v1/sec/auth/signup` with a
duplicate email → 409 `SEC-409-SIGNUP-DUP`) **PASSED**. It doesn't map to any
single governed `TC-SEC-*` id — the governed TC-SEC-003 covers only the
happy-path signup — so it is recorded here as additional confidence, not
folded into the 35-TC ratio.

**4/35 exercised, 4/35 PASS, 0/35 FAIL, 31/35 GAP (28 not-yet-generated + 2
not-HTTP-reachable by design + 1 generated-but-structurally-invalid — invented
nonexistent endpoint).** (Updated 2026-09-11, final environmental-recheck
round — TC-SEC-006 confirmed PASS after a clean rebuild; TC-SEC-007
reclassified FAIL → GAP, a genuine structural gap rather than an
environmental block; see the dated note below the supersession note. Earlier
same-day update: TC-SEC-002 moved FAIL → PASS.)

## Notable results

- **TC-SEC-001 (login) — PASSES** on the corrected, rebuilt app: 200 with a
  valid JWT (`data.accessToken`/`tokenType`/`expiresIn`).
- **TC-SEC-003 (signup happy path) — PASSES**: 201, `SignupRequest`
  `statusCode=PENDING`, no `User` row created.
- **Signup-related coverage is now healthy**: both the governed happy path
  (TC-SEC-003) and the non-governed duplicate-rejection scenario
  (`SEC-409-SIGNUP-DUP`) pass on the rebuilt app.

## Failures / gaps with taxonomy code

| TC / item | Taxonomy | Detail |
|---|---|---|
| TC-SEC-002 (TestSprite TC002) | **TEST_STRUCTURE_FAILURE — FIXED 2026-09-11, now PASS** | The generated script iterated 5 credential combinations and asserted 401 for all of them. The 3 combinations TC-SEC-002 actually specifies (wrong password / unknown username / both) passed with 401 `SEC-401-INVALID-CREDENTIALS` as expected. The script's own 2 self-added blank-field combinations — **empty username and empty password** (the fix-round re-check found a second one beyond what this row originally flagged) — correctly receive **400** `VALIDATION_ERROR` (Bean Validation rejecting a blank required field before the credentials check ever runs), not 401. Fixed by splitting the loop and asserting 400/VALIDATION_ERROR for the 2 blank-field cases; re-run confirmed PASS. |
| TC-SEC-006 (TestSprite TC005) | **TEST_STRUCTURE_FAILURE — FIXED, RE-RUN CONFIRMED PASSING 2026-09-11 (final environmental-recheck round)** | The script's own setup step (`POST /api/v1/sec/users`, creating a fixture user before requesting the reset) asserted the create-user response is `200`. The actual, correct response is `201 Created` — matching TC-SEC-009's own governed expectation ("201; User created with statusCode=ACTIVE"), and independently confirmed via `curl`. Fixed (200→201). The prior FAIL after that fix was purely the recurring `target/classes`-emptied environmental issue (see the FIX ROUND section above), not a real defect. After the orchestrator's final clean rebuild (`mvn -q -DskipTests compile`, 377/377 classes) and restart, `python3 TC005_....py` exits 0. Closed out as PASS. |
| TC-SEC-007 (TestSprite TC006) | **TEST_STRUCTURE_FAILURE — reclassified FAIL → GAP, 2026-09-11 (final environmental-recheck round)** | Same setup-step fix as TC-SEC-006's entry above (200→201) plus a second bug fixed in the same script (`["data"]["id"]` → `["data"]["userPk"]`, the correct field name) — both no longer blocked by the environmental issue after the final clean rebuild. Independently of the environment, the script still fails for a genuine, non-environmental reason: at line ~108-128 it tries to `GET` a `token_lookup_url` — a hypothetical endpoint to retrieve a raw password-reset token by email — that does not exist anywhere in this codebase (confirmed via `grep -rn "token_lookup_url\|reset.*token" src/main/java/com/erp/sec/controller/`, no match). This is not an app bug or a fixable assertion — reset tokens are only ever delivered by email, never retrievable via any API, by deliberate security design (see `AuthService`/`PasswordResetService`'s POL-SEC-004-adjacent handling). Same structural-gap class as TC-SEC-008 (a purpose-built fixture / test-only endpoint the system deliberately does not expose) — reclassified from FAIL to GAP accordingly. Taxonomy code kept as TEST_STRUCTURE_FAILURE rather than MISSING_IMPLEMENTATION: the *script itself* is broken/unrunnable as written (it invents a dependency), which is what TEST_STRUCTURE_FAILURE describes ("broken test script itself — not an app bug"); MISSING_IMPLEMENTATION would suggest SEC itself is missing an endpoint it should have, which is not the case — reasonable people could pick either code, but this dispatch's judgment is TEST_STRUCTURE_FAILURE fits more precisely. No longer counted as a FAIL in the coverage ratio above. |
| TC-SEC-008 | GAP (not a failure — no taxonomy code; nothing ran against its actual precondition) | TestSprite's `TC007` tests a **nonexistent/bogus** reset token and correctly gets 409 `SEC-409-RESET-TOKEN-INVALID` — the same error code TC-SEC-008 expects, via the same "not a valid, unused, unexpired token" code path — but TC-SEC-008's actual governed precondition (a token that is specifically **expired** or **already used**, requiring a purpose-built fixture) was never constructed by any generated script. Recorded as a partial adjacent signal, not counted as PASS. |
| TC-SEC-004, TC-SEC-005, TC-SEC-009 .. TC-SEC-033 (28 TCs) | GAP — no taxonomy code (never generated, not a failure) | TestSprite's `testsprite_generate_backend_test_plan` produced only 7 candidates for the whole backend this run (Starter plan, 49 credits) — see "Tool-capacity constraint" above. No mechanism in this command authorizes hand-authoring additional TestSprite tests outside the tool's own pipeline. |
| TC-SEC-034, TC-SEC-035 | GAP — structural, no taxonomy code | Both exercise `com.erp.sec.crossmodule.SecUserDirectoryApi` in-process — explicitly not an HTTP endpoint per the TC text itself. TestSprite is a black-box HTTP tool and structurally cannot drive an in-JVM Spring-interface call; these two need a JUnit-style in-process harness, outside this command's TestSprite-only mechanism. |

## Environmental note (for the record, not a taxonomy entry)

`target/classes` was found completely emptied mid-run (0 of the expected 377
class files) while the app's JVM was still serving requests — the same
false-alarm class of issue this dispatch was sent to correct in the first
place, recurring live during this dispatch's own execution. Caught,
diagnosed via the running JVM's own log output (identified by
`ClassNotFoundException`/`NoClassDefFoundError` on Lombok builder inner
classes), and corrected by stopping the process, running `mvn clean
compile` (377/377 classes confirmed restored), restarting, and independently
re-verifying every affected scenario via `curl` before resuming testing. No
`src/main/java` file was read for content changes or modified at any point.

## Recommendation (original text — the item-1 fix has since been partially
applied; see the FIX ROUND section above for what changed)

1. ~~**Test-script fixes** (not application fixes): TC002/TC005/TC006's
   assertion bugs (200-vs-201, and an out-of-scope empty-username case) will
   reproduce on every future rerun of these exact archived scripts unless
   corrected. Per `TESTSPRITE-GOVERNANCE.md` §5's sanctioned exception, a
   human or a future dispatch may patch these archived scripts' assertions
   to match the already-correct application contract, then rerun to confirm.~~
   **DONE for TC002 (TC-SEC-002), 2026-09-11 — PASS confirmed. DONE for TC005
   (TC-SEC-006), 2026-09-11 (final environmental recheck round) — PASS
   confirmed after a clean rebuild. TC006 (TC-SEC-007) is NOT a script-fix
   candidate: it depends on a nonexistent endpoint by design and is now
   recorded as a structural GAP, not a FAIL — see the final FIX ROUND section
   above.**
2. **Coverage**: 30/35 governed TCs still have zero TestSprite coverage,
   purely due to the connected account's plan/credit tier generating only 7
   candidates for the whole backend. Re-running `testsprite_generate_backend_test_plan`
   again may or may not surface a larger plan (it did improve from 1→7
   between the two runs today) — or the module may need multiple successive
   runs / a higher-tier plan to approach full governed coverage.
3. **Environment**: this repo's shared-checkout, concurrent-session
   environment reproduced the exact same class of `target/classes`-wiped
   false alarm twice in one day — **and now a third time, live, during this
   fix round** (see the FIX ROUND section above). Consider serializing `mvn` invocations
   across concurrent sessions on this checkout, or moving to a build that
   doesn't run directly against `target/classes` (e.g. `spring-boot:run`
   with a fork, or running the repackaged jar) to make this class of
   environmental corruption impossible rather than merely recoverable.
