# HANDOFF — PHASE 9 (F3) — PLAN-SEC-002

Validators implemented: [list, mapped to RULE-IDs]

- **RULE-SEC-034** (`branchIdFk` required, active-branch-only dropdown source) —
  - Dropdown source: already active-branch-only, sourced by F2's `UserProfileFacade.loadActiveBranches()` /
    `RoleBranchFacade.loadActiveBranches()` (`searchActiveBranches()`) — no change needed.
  - `user-profile-entry.component.ts`: replaced `Validators.required` on `branchIdFk` with a new
    `requiredActiveBranchValidator` (same file) that sets a distinct error key `branchRequiredSec034`,
    so the field now shows the catalog-exact ERR-SEC-1034 text instead of the generic
    "This field is required" message.
  - `role-access-form.component.ts` (Branch Scope "Assign Branch" modal): already structurally
    enforced by the confirm button's `[disabled]="!selectedNewBranchId || ..."` guard (F2) — no new
    validator needed; added a code comment explaining why.
- **RULE-SEC-035** (`dataAccessLevel` required, LOV-SEC-002 dropdown) — already structurally
  guaranteed: the dropdown has exactly 3 fixed options (`BRANCH_ONLY` / `BRANCH_AND_CHILDREN` /
  `ALL`), no blank option, and always carries a non-null default (`DATA_ACCESS_LEVELS[0]`). No new
  client validator added (would be dead code); added a code comment recording this, and wired the
  backend error code (`SEC_ROLE_BRANCH_DATA_ACCESS_LEVEL_REQUIRED`) to the catalog-exact message
  defensively in case this constraint is ever relaxed on the UI side later.
- **RULE-SEC-036** (client-side duplicate (role, branch) pre-check) — the ad hoc check already
  built in F2 (`role-access-form.component.ts#onAssignBranchConfirm`) was kept as-is (still a
  UX-only pre-check, server remains authoritative via ERR-SEC-1036); corrected its message text
  (`ROLE_ACCESS.BRANCH_ALREADY_ASSIGNED`) to the catalog-exact wording (previous text said "to the
  role" not "to this role", and the Arabic used "مخصص" not the catalog's "مُسنَد").
- **RULE-SEC-040/041** (username/email format + required, no client uniqueness pre-check) — format
  and required validators were already correctly implemented in F2 (`Validators.required` +
  `minLength`/`maxLength` for username, `Validators.required` + `Validators.email` for email); no
  "check availability" endpoint invented. Fixed a real display bug found while verifying this task:
  `sign-up.component.html`/`password-recovery.component.html` hard-coded `VALIDATION.REQUIRED` as
  the error text for every invalid state, so an invalid-but-non-empty email (format violation) or a
  too-short password (minlength violation) incorrectly showed "This field is required" instead of
  the correct message. Fixed by adding a `fieldErrorKey()` helper (both components) that delegates
  to the shared `getFormFieldError()` resolver already used by `erp-form-field` elsewhere in the
  codebase, so the right message now shows for each violated validator.
- **RULE-SEC-038** (Forgot Password identical generic message) — verified, no code change needed
  (see verification note below).

Message-text cross-check vs Section 4.2: confirmed exact / discrepancies fixed

- Added catalog-exact bilingual (EN/AR) strings for every RULE-ID in this phase's scope:
  - `USER_PROFILES.BRANCH_REQUIRED_ACTIVE` (ERR-SEC-1034)
  - `ROLE_ACCESS.DATA_ACCESS_LEVEL_REQUIRED` (ERR-SEC-1035)
  - `ROLE_ACCESS.BRANCH_ALREADY_ASSIGNED` (ERR-SEC-1036) — **corrected**, was previously
    paraphrased in both EN ("to the role" → "to this role") and AR ("مخصص" → "مُسنَد")
  - `AUTH.USERNAME_ALREADY_EXISTS` (ERR-SEC-1040), `AUTH.EMAIL_ALREADY_EXISTS` (ERR-SEC-1041) — new
- `api-docs`' auto-generated endpoint pages for all five affected endpoints (`create.md` under
  `security-datascope-user-profiles/`, `create_1.md` under `security-datascope-role-branches/`,
  `signup.md` under `authentication/`) only document the generic `UNAUTHORIZED`/`INVALID_JSON`
  responses, not these business error codes — **MISSING_IN_DOCS**, resolved from backend source
  per STEP 1.5 (see `api_doc_gaps[]` below). Backend codes found in `erp-security`'s
  `SecurityErrorCodes.java` (lines 76–88):
  - `SEC_USER_PROFILE_BRANCH_INACTIVE` → ERR-SEC-1034 (note: the *missing* `branchIdFk` case is a
    plain Bean Validation `@NotNull`, not this code — this code covers the "branch present but
    inactive/unresolvable" case only)
  - `SEC_ROLE_BRANCH_DATA_ACCESS_LEVEL_REQUIRED` → ERR-SEC-1035
  - `SEC_ROLE_BRANCH_DUPLICATE_ASSIGNMENT` → ERR-SEC-1036
  - `SIGNUP_USERNAME_ALREADY_EXISTS` → ERR-SEC-1040 (deliberately a *different* code from
    RULE-SEC-006's `USERNAME_ALREADY_EXISTS`, per a comment in the backend source — do not merge)
  - `SIGNUP_EMAIL_ALREADY_EXISTS` → ERR-SEC-1041
  - All five wired into `ErpErrorMapperService.errorMappings` so that a real server-side rejection
    (not just the client pre-check) also renders the catalog-exact text in the save-error banner,
    not the generic `ERRORS.OPERATION_FAILED` fallback.
- RULE-SEC-032/033/043 (token invalid/expired/already-used) are **out of this phase's scope** —
  Section 7.3 does not list them under F3's validator set, even though Section 4.2's catalog
  includes their ERR-IDs. Not touched, to avoid scope creep; `SignUpFacade.activate()` /
  `ForgotPasswordFacade.reset()` error banners still fall back to generic `ERRORS.OPERATION_FAILED`
  for these — flagging as a possible follow-up for Phase SEC or ALIGN if a full audit is wanted.

RULE-SEC-038 UI-level verification: [describe test performed]

- Read `ForgotPasswordFacade.requestReset()` (`forgot-password.facade.ts:41-57`): the RxJS `tap()`
  on success calls `this.requestSuccessSignal.set(true)` unconditionally — it never inspects the
  HTTP response body, so there is no code path where the signal's value depends on whether the
  backend found a matching email. `catchError` only fires on a genuine transport/HTTP error (never
  triggered by the "email not found" case, since the backend always returns 200 for this endpoint
  per RULE-SEC-038).
- Read `password-recovery.component.html:10-17`: the success view is gated purely on
  `facade.requestSuccess()` and renders two fixed translation keys
  (`AUTH.RESET_LINK_SENT`/`AUTH.RESET_LINK_SENT_HINT`) with no interpolation of any response data
  and no secondary `@if`/ternary that could vary the message — confirmed no UI-layer branch exists
  that could leak whether the email existed.
- This is a static-analysis verification (code read + confirmed no branching exists), not a live
  two-email-comparison test — no working browser was available in this environment (see
  Verification note below). A manual click-through comparing a known-existing vs known-nonexistent
  email is still recommended before treating this as end-to-end proven.

Ready for Phase 10 (SEC)? yes — with the same caveat carried forward from Phases F1/F2: DOC/INT-C/
INT-R remain PENDING and should be run to reconcile the whole F1/F2/F3 frontend build against a
confirmed-stable contract before SEC's permissions/route-guard work is layered on top.

---

## Deviations / judgment calls (not silent)

1. **STEP 0 CONTRACT ALIGNMENT GATE was force-overridden a third time by explicit user
   instruction**, same as Phases F1 and F2. At the time this phase started,
   `execution-state.json` had `current_phase: DOC` (PENDING) and `INT-C` also PENDING — the gate
   (plan Section 12) blocks F3 until both are COMPLETE. I printed the gate-failure block and
   asked; the user chose "Force-start F3 anyway", an explicit override. `current_phase`/
   `current_sub` remain at `DOC` in `execution-state.json` (unchanged) — DOC/INT-C/INT-R are still
   owed and should be run and reconciled against F1/F2/F3's frontend work before it's trusted as
   built on a stable, aligned contract, exactly as flagged in `HANDOFF-PHASE-7-F1.md` and
   `HANDOFF-PHASE-8-F2.md`.

2. **`api_doc_gaps[]` additions (5 entries, all MISSING_IN_DOCS)** — the business error codes for
   RULE-SEC-034/035/036/040/041 are not present in any `api-docs/endpoints/**/*.md` file (only the
   generic `UNAUTHORIZED`/`INVALID_JSON` framework-level responses are auto-documented). Resolved
   each from `erp-security`'s `SecurityErrorCodes.java` directly, per STEP 1.5's fallback rule. See
   the message-text cross-check section above for the specific codes and files.

3. **Two pre-existing message-text bugs fixed, not just new validators added:**
   - `ROLE_ACCESS.BRANCH_ALREADY_ASSIGNED` (built in F2) was a paraphrase of the catalog text in
     both languages, not a verbatim match — corrected in `en.json`/`ar.json` since this phase's own
     Definition of Done requires verbatim Section 4.2 text.
   - `sign-up.component.html`/`password-recovery.component.html` (both built in F2) always showed
     `VALIDATION.REQUIRED` for any invalid control state, regardless of which validator actually
     failed — a real bug (not cosmetic): a user typing a malformed-but-non-empty email, or a
     too-short password, saw "This field is required" instead of the actually-correct message.
     Fixed via a shared `fieldErrorKey()` helper delegating to the existing `getFormFieldError()`
     utility (`form-error-resolver.ts`) — the same utility `erp-form-field` already uses elsewhere,
     so this brings the auth pages in line with the rest of the codebase's error-display
     convention instead of introducing a new one.

4. **No new Reactive FormGroup added to the Branch Scope "Assign Branch" modal.** RULE-SEC-034 and
   RULE-SEC-035 are both already structurally enforced there (disabled confirm button / no-blank
   dropdown with a default, respectively) — converting the modal's plain component-state bindings
   to a full `FormGroup` + `Validators` would be validator-shaped code with no behavior it doesn't
   already have. Documented this reasoning inline instead (`role-access-form.component.ts`) so a
   future reader doesn't wonder why F3 skipped it.

5. **RULE-SEC-032/033/043 deliberately left untouched** — Section 7.3 (this phase's own source of
   truth) does not list them under F3's validator scope, even though Section 4.2's catalog defines
   their ERR-IDs. Implementing message-mapping for them here would be scope creep beyond what F3
   asked for; flagging as a candidate for Phase SEC or ALIGN instead.

6. **Verification:** `npx tsc -p tsconfig.app.json --noEmit` — clean, zero errors (one import
   fix needed mid-session: `password-recovery.component.ts` was missing the `AbstractControl`
   import needed by the new `fieldErrorKey()` helper — added and re-verified). Full
   `npx ng build --configuration=production` — clean, zero errors; only pre-existing warnings in
   unrelated files (`MasterLookupEntryComponent`, `AdminLayout`, `NavRightComponent`). i18n JSON
   files (`en.json`/`ar.json`) validated as well-formed via `json.load()`.
   **Could not get a real browser to visually verify the screens** — same environment limitation
   as Phases F1/F2 (`HANDOFF-PHASE-7-F1.md`, `HANDOFF-PHASE-8-F2.md`): the only cached Playwright
   Chromium install is corrupted, and `npx playwright install chromium` cannot complete (no network
   access for browser binary downloads in this environment). A manual click-through is recommended
   before treating this phase as fully proven, particularly: the user-profile branch-required error
   message, the sign-up format-vs-required message distinction (try an invalid-format email, a
   too-short password), and RULE-SEC-038's live behavior with a real existing vs. non-existing
   email address.
