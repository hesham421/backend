# HANDOFF — PHASE 8 (F2) — PLAN-SEC-002

Facades created: [list + paths + API-IDs bound]
- `UserProfileFacade` — `frontend/src/app/modules/security/user-profiles/facades/user-profile.facade.ts`
  - `applyGridStateAndLoad()` → API-SEC-033 (search), `getById()` → API-SEC-035, `create()` → API-SEC-032, `update()` → API-SEC-034
  - `loadActiveBranches()` → API-ORG-008 (XM-SEC-001 branch dropdown source)
  - Paired: `UserProfileApiService` (`services/user-profile-api.service.ts`)
- `RoleBranchFacade` — `frontend/src/app/modules/security/role-access/facades/role-branch.facade.ts`
  - `loadRoleBranches(roleId)` → API-SEC-037 (search, filtered to the role), `assign()` → API-SEC-036, `updateDataAccessLevel()` → API-SEC-038, `remove()` → API-SEC-039
  - `loadActiveBranches()` → API-ORG-008 (XM-SEC-002 branch dropdown source)
  - Paired: `RoleBranchApiService` (`services/role-branch-api.service.ts`)
  - Wired into the existing `role-access-form.component.ts`'s Branch Scope sub-tab (CORE-9: no new SCR-ID/route)
- `SignUpFacade` — `frontend/src/app/modules/security/authentication/facades/sign-up.facade.ts`
  - `submit()` → API-SEC-040, `activate(token)` → API-SEC-041
  - Paired: `SignUpApiService` (`services/sign-up-api.service.ts`)
- `ForgotPasswordFacade` — `frontend/src/app/modules/security/authentication/facades/forgot-password.facade.ts`
  - `requestReset()` → API-SEC-042, `reset(token)` → API-SEC-043
  - Paired: `ForgotPasswordApiService` (`services/forgot-password-api.service.ts`)

Pagination pattern used: [describe, confirm consistency]
- `UserProfileFacade` follows the canonical `create-facade` skill pattern exactly: single
  `lastSearchRequestSignal` as source of truth for page/size/sort, `currentFiltersSignal`
  independently tracked, `currentPage`/`pageSize` derived via `computed()` (never separate
  writable signals), `loading`/`error`/`saving`/`saveError` signals, `catchError → EMPTY`,
  `finalize` resets loading, `takeUntilDestroyed(this.destroyRef)` before every `.subscribe()`.
  Confirmed consistent with `RoleAccessFacade`/`MasterLookupFacade` precedent.
- `RoleBranchFacade` follows the HAS_CHILD child-list convention (S.4.8: page 0 / size 50,
  bounded list, no independent pagination UI) — local signal updates on assign (append),
  update (map in-place), remove (filter out), never a full reload. Matches
  `master-lookups`' `LookupDetail` child-facade precedent.
- `SignUpFacade`/`ForgotPasswordFacade` are non-list facades (single-action state only) — no
  pagination applicable.

Any undocumented endpoint usage (should be none): none. `UserProfileApiService.searchActiveBranches()`
and `RoleBranchApiService.searchActiveBranches()` call `POST /api/v1/org/branches/search`
(API-ORG-008), which is documented in `api-docs/endpoints/branch-management/API-ORG-008.md` and
explicitly named as the branch-dropdown source in execution-plan-SEC-gaps.md's XM-SEC-001/002
register entries.

Ready for Phase 9 (F3)? yes

---

## Deviations / judgment calls (not silent)

1. **STEP 0 CONTRACT ALIGNMENT GATE was force-overridden again by explicit user instruction**,
   same as Phase F1. At the time this phase started, `execution-state.json` had
   `current_phase: DOC` (PENDING) and `INT-C` also PENDING — the gate (plan Section 12) blocks
   F2 until both are COMPLETE. I printed the gate-failure block and asked; the user replied
   "yes F2", an explicit force-start instruction. `current_phase`/`current_sub` remain at `DOC`
   in `execution-state.json` (unchanged) — DOC/INT-C/INT-R are still owed and should be run and
   reconciled against F1/F2's frontend work before it's trusted as built on a stable, aligned
   contract, exactly as flagged in `HANDOFF-PHASE-7-F1.md`.

2. **Branch dropdown built as a plain `avl-select` fed by a one-time bounded facade load
   (`loadActiveBranches()`, page 0 / size 50), NOT the shared `ErpLookupFieldComponent`.**
   `ErpLookupFieldComponent`/`LookupDataService` is the governance-sanctioned canonical pattern
   for entity lookups (per `enforce-ui-ux` skill UX.2.3), but it is a GET-based contract
   (`GET {endpoint}?search=&limit=` for quick mode, `GET {endpoint}?search=&page=&size=&sort=`
   for advanced mode). API-ORG-008 (the only documented branch lookup endpoint, per XM-SEC-001/002)
   is a `POST /api/v1/org/branches/search` endpoint using the `BaseSearchContractRequest` body
   contract — a different shape entirely. Forcing `ErpLookupFieldComponent` onto this endpoint
   isn't possible without either a new GET-based lookup endpoint on the Organization module (out
   of scope — no such API-ORG-ID exists in this plan) or a mismatched adapter. Used the same
   "one-time small load populates a plain `avl-select`" technique already established by
   `pages-registry`'s `loadActivePages()`/`parentId` dropdown instead. Flagging for a later
   phase/architecture decision: if the Organization module's branch count grows past ~50, this
   dropdown will silently truncate (page 0/size 50, no "load more") — either raise the size cap,
   add a real GET-based lookup endpoint on ORG, or adopt `ErpLookupFieldComponent` in `advanced`
   mode once ORG exposes one.

3. **`SEC_USER_PROFILE.userIdFk` (create-only) implemented as a plain numeric ID input, not a
   user search/picker.** The plan's Section 7.2 facade spec for `UserProfileFacade` does not
   mention a user-lookup dropdown (only the branch dropdown is called out, XM-SEC-001), and no
   frontend precedent for a "pick an existing system user" widget exists yet (`UserApiService`
   in `user-management/` is also POST-search-based, same GET/POST mismatch as branches — see
   #2). A numeric input is functionally correct (matches `CreateSecUserProfileRequest.userIdFk:
   Long`, immutable after creation, field disabled once `currentProfile` is loaded) but a
   real user-picker would be materially better UX. Flagging as a follow-up, not implemented here
   since it's beyond this facade's literally-specified scope.

4. **`SignUpComponent`'s form was rebuilt to match the backend contract exactly, not incrementally
   wired.** The F1 shell's form had `firstName`/`lastName` fields and no `username` field — but
   backend `SignupRequest` is `(username, email, password)` with no name fields at all (already
   flagged as a real gap in `HANDOFF-PHASE-7-F1.md`'s deviation #4, not newly discovered here).
   Fixed by replacing `firstName`/`lastName` with a `username` field (matching the 3–80 char /
   `@NotBlank` server-side constraint) and keeping `email`/`password`. This is a field removal,
   not an additive wiring — flagging explicitly since it changes the F1 shell's visible form
   rather than just adding a submit handler to it.

5. **SCR-SEC-008 (Sign Up) gained a second, activation state within its single route**, driven by
   a `token` query param — mirroring SCR-SEC-009's request/reset step-switching exactly (`toSignal`
   + `computed(() => !!token())`). The Section 7.1 screen table only explicitly calls SCR-SEC-009
   "2-step"; SCR-SEC-008 isn't labelled that way. However, Section 7.2's own `SignUpFacade` spec
   explicitly lists `activate(token) → API-SEC-041` and says the facade "Owns ... activation-link
   state (post-submit)" — and SCR-SEC-008 is the *only* screen in this plan that consumes
   API-SEC-041, so the emailed activation link has nowhere else to land. Implemented the
   activation view on the same `/security/sign-up` route (not a new route) — consistent with
   CORE-9-style route minimalism and the facade spec's explicit ownership statement.

6. **RULE-SEC-036 (client-side duplicate check before submit) implemented directly in
   `RoleAccessFormComponent.onAssignBranchConfirm()`**, not as a separate helper file — it's a
   3-line check against the already-loaded `roleBranches` signal, not complex enough to warrant
   a dedicated helper like `role-confirm-actions.ts`. Server remains authoritative
   (`ERR-SEC-1036`) per the plan; this is a UX-only pre-check.

7. **Verification:** `npx tsc -p tsconfig.app.json --noEmit` — clean, zero errors. Full
   `npx ng build --configuration=development` — clean, zero errors; one self-caught warning
   (unused `ErpBackButtonComponent` import in `UserProfileEntryComponent`, left over from
   switching to `erp-action-bar`'s built-in back button) fixed before this handoff was written.
   **Could not get a real browser to visually verify the screens** — same environment limitation
   as Phase F1 (`HANDOFF-PHASE-7-F1.md`): the only cached Playwright Chromium install is
   corrupted, and `npx playwright install chromium` cannot complete (no network access for
   browser binary downloads in this environment). Recommend a manual click-through before
   treating this phase as fully proven, particularly: the Branch Scope tab's assign/remove/
   inline-data-access-level-change flow, the Sign Up activation-link state transition, and the
   Forgot/Reset Password 2-step flow's generic-message behavior (RULE-SEC-038).
