# PHASE 3 — SVC+API
## Plan: PLAN-SEC-002 | Source: execution-plan-SEC-gaps.md → Section 4

---
## SESSION PROMPT — PASTE THIS FIRST

You are executing **Phase SVC+API** of PLAN-SEC-002 for the Security
module, in this repository, using Claude Code.

FIRST: read `HANDOFF-PHASE-2-DATA-DOM.md`. If missing or "Ready for
Phase 3: no" — STOP and ask me.

**Source of truth: `execution-plan-SEC-gaps.md` Section 4** (4.1 API
Register, 4.2 Error Catalog) in full. Also re-read Section 1.3 (Rules)
for the exact RULE-ID statements each endpoint must enforce, and Section
6.2 (event-based integration for RULE-SEC-031) — you WILL need it for
API-SEC-040/042, even though INT-C is nominally the next phase: the
publish-side of the event must exist here for the endpoint to compile
and behave correctly. Reference `security-registry.md` §5.8 for the
existing `LoginRateLimitFilter` pattern you are extending (Section 8.3
of the plan) — do NOT touch the filter's path-matching yet, that is
Phase SEC's job; here you only need the two new endpoints to exist.

TASK — implement repositories, services, mappers, controllers for:
- API-SEC-032..035 (user-profiles CRUD/search)
- API-SEC-036..039 (role-branches CRUD/search)
- API-SEC-040/041 (signup, activate)
- API-SEC-042/043 (forgot-password, reset-password)

Requirements (non-negotiable, per plan + project standard):
1. Every RULE-ID in the API Register's "RULE-IDs" column must be
   enforced in the Service layer, throwing `LocalizedException` with
   the EXACT ERR-ID + Arabic/English message pair from Section 4.2 —
   never `NotFoundException`, never a paraphrased message.
2. RULE-SEC-038 (anti-enumeration): forgot-password ALWAYS returns the
   same generic success response — do not let a try/catch leak whether
   the email existed.
3. RULE-SEC-031: publish `PasswordResetRequested` / 
   `AccountActivationRequested` domain events — do NOT call any
   NotificationService class/method directly. If no event-publishing
   mechanism exists yet in this codebase, use Spring's
   `ApplicationEventPublisher` and report that choice in the handoff
   (per DRV-SEC-005 in the plan) — do not invent a message broker.
4. Controllers are thin (per security-registry.md §5.2) — logic in
   Service layer, `@PreAuthorize` deferred to Phase SEC (do not add
   permission annotations yet — Phase SEC owns that).
5. Search/pagination uses `BaseSearchContractRequest` +
   `ALLOWED_SORT_FIELDS` per Section 2 (PHASE CORE) of the plan.

DEFINITION OF DONE:
- [ ] All 12 API-IDs implemented and reachable
- [ ] All ERR-IDs in Section 4.2 produced verbatim on their trigger condition
- [ ] RULE-SEC-038 verified to not leak email existence
- [ ] Events published for RULE-SEC-031, no direct NotificationService call
- [ ] No @PreAuthorize added yet (Phase SEC's scope)

Write `HANDOFF-PHASE-3-SVC-API.md` using the template below.

---
## HANDOFF REPORT TEMPLATE

```
# HANDOFF — PHASE 3 (SVC+API) — PLAN-SEC-002
Controllers/Services/Repos created: [list + paths]
Event mechanism used for RULE-SEC-031: [class/pattern + path]
ERR-IDs verified against Section 4.2 (list each, confirm exact match): 
RULE-SEC-038 anti-enumeration verified how: [describe test/check]
Endpoints NOT yet permission-gated (expected — Phase SEC): confirmed
Deviations from plan Section 4 (should be none — list or "none"):
Ready for Phase 4 (DOC)? [yes/no]
```
