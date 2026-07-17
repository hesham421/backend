# PHASE 10 — SEC (Permissions, Seed Data, Route Guards)
## Plan: PLAN-SEC-002 | Source: execution-plan-SEC-gaps.md → Section 8

---
## SESSION PROMPT — PASTE THIS FIRST

You are executing **Phase SEC** of PLAN-SEC-002 for the Security module,
in this repository, using Claude Code.

FIRST: read `HANDOFF-PHASE-9-F3.md`. If missing or "Ready for Phase 10:
no" — STOP and ask me.

**Source of truth: `execution-plan-SEC-gaps.md` Section 8** in full
(8.1 Permissions Matrix, 8.2 Security Seed Data Task, 8.3 Rate-Limit
Filter Extension). Also re-read Section 6.2 for RULE-SEC-037
(`allowedBranches[]` JWT claim — implemented in this phase, not earlier,
since it touches shared JWT infrastructure).

TASK:
1. **Permissions**: add SEC_PAGES row for SCR-SEC-006
   (pageCode `USER_PROFILE`, route `/security/user-profiles`, parent =
   existing "الأمان" group page) and let the existing RULE-SEC-012
   auto-generation mechanism create PERM_USER_PROFILE_VIEW/CREATE/UPDATE
   (NO DELETE — per Section 8.1, this screen deactivates via UPDATE,
   never deletes). Do NOT add a new SEC_PAGES row for the Role→Branch
   sub-tab — it reuses existing PERM_ROLE_* (CORE-9).
2. **Route guards / @PreAuthorize**: now wire the actual permission
   checks on the Phase 3 controllers (API-SEC-032..039 per the matrix
   in Section 8.1) and on the Phase 7 Angular routes/buttons (hide
   Create/Update actions per permission, per MANDATORY-P-3-style UI
   enforcement). API-SEC-040..043 (auth endpoints) stay PUBLIC.
3. **JWT `allowedBranches[]` claim** (RULE-SEC-037): extend
   `JwtService`/`JwtProperties` to populate this claim on login, derived
   from the user's active SEC_ROLE_BRANCH rows across active roles. If
   you use the "ALL" sentinel optimization from the plan's DRV-SEC-004,
   document that choice explicitly in the handoff.
4. **Rate-limit filter extension** (Section 8.3): extend
   `LoginRateLimitFilter`'s path-matching to also cover
   `POST /api/auth/forgot-password` and `POST /api/auth/reset-password`,
   using email (not username) as the identifier in the
   `<ip>|<identifier>` key. Verify this does not break the existing
   login/signup rate-limiting behavior.
5. Seed DATA_ACCESS_LEVEL lookup — confirm Phase 2's migration (BLOCK 8)
   already did this; do not re-seed.

DEFINITION OF DONE:
- [ ] SEC_PAGES + 3 auto-generated PERM_USER_PROFILE_* permissions confirmed
- [ ] @PreAuthorize wired on all authenticated endpoints per Section 8.1 matrix
- [ ] Angular UI hides Create/Update actions per permission
- [ ] allowedBranches[] claim present on a real login response — verify
      by actually logging in and decoding the token
- [ ] Rate-limit filter extended, existing login/signup rate-limiting
      still passes its existing tests

Write `HANDOFF-PHASE-10-SEC.md` using the template below.

---
## HANDOFF REPORT TEMPLATE

```
# HANDOFF — PHASE 10 (SEC) — PLAN-SEC-002
SEC_PAGES row + generated permissions confirmed: [list PERM_ constants]
@PreAuthorize wiring confirmed per endpoint: [list API-ID → permission]
UI permission-hiding confirmed: [yes/no, how verified]
allowedBranches[] claim verified in an actual token: [yes/no — paste decoded claim shape, redact real values]
"ALL" sentinel used?: [yes/no]
Rate-limit filter extension verified (existing tests still pass): [yes/no]
Ready for Phase 11 (ALIGN)? [yes/no]
```
