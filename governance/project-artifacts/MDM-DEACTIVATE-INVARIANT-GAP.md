# MDM — Active-value-under-inactive-type invariant gap (governance decision needed)

**Status:** OPEN — awaiting a governance/spec decision. No code change has been made,
because the current behavior is explicitly derived from **DRV-004 / QR-MDM-0011**
("parent existence only, no active-check") and reversing it unilaterally would be a
silent governance deviation.

**Raised:** 2026-09-05, during a code review of the new MDM module.
**Scope:** `com.erp.mdm` — `MdmLookupValueService.create`, `MdmLookupTypeService.deactivate`.

---

## The rule pair in tension

- **RULE-MDM-006 (DRV-008):** a `LookupType` cannot be soft-deactivated while it still has
  active `LookupValue` children (`MdmLookupTypeService.deactivate` → `countActiveValuesByType`
  → `LookupTypeDomain.assertCanDeactivate` → 409 CONFLICT). Intent: an inactive type must never
  own active values.
- **DRV-004 / QR-MDM-0011:** value `create` validates parent **existence only**, deliberately
  *not* the parent's active state (`MdmLookupValueService.create`,
  [service create — parent existence, no active-check]).

These two rules are individually satisfied but jointly leave the target invariant
("an inactive type has no active values") **enforceable in one direction only**.

## How the invariant is violated (no race required)

1. Type `T` has zero active values → `DELETE /lookup-types/{T}` succeeds (RULE-MDM-006 passes).
   `T` is now inactive.
2. `POST /lookup-types/{T}/values` — `create` finds `T` (exists), skips the active-check per
   DRV-004, and persists a value with `isActive = TRUE` (entity default).
3. Result: an **inactive type owning an active value** — precisely the state RULE-MDM-006 exists
   to prevent.

There is also a **TOCTOU race** variant even for a type that is currently active: `deactivate`
reads `countActiveValuesByType == 0` and, before it commits, a concurrent `create` commits a new
active value under the same type; no lock or DB constraint backs the count check.

## Why this is hard to recover from

The module exposes **no reactivation endpoint** (update DTOs omit `isActiveFl`; `activate()` on
both entities is currently unreachable). So a stranded active value:
- never surfaces via `GET /api/v1/mdm/lookups/{typeCode}` (that query requires `lt.isActive = true`), and
- cannot be corrected through the API.

Recovery today is a manual DB fix only.

## Options for the decision-maker

1. **Accept as-is (document the carve-out).** Keep DRV-004 existence-only; formally record that
   the "inactive type ⇒ no active values" invariant is a *deactivate-time* guard only, not a
   *create-time* one. Cheapest; leaves the manual-recovery risk.
2. **Extend create to require an active parent.** Add a parent active-check in
   `MdmLookupValueService.create` (reuse the "requireActiveModule" pattern from `PageService`),
   returning a suitable 4xx. This changes DRV-004 and needs a spec amendment.
3. **Backstop the race + close the reverse gap at the DB/tx layer.** e.g. a lock on the parent
   row during `create`/`deactivate`, or a DB-level guard. Strongest guarantee, most work.

**Recommendation:** treat this as a spec question first (Option 1 vs 2). Do **not** patch the
code ahead of that decision — the existence-only check is a stated derived requirement.

---

## Other MDM review items intentionally left unchanged (same "documented decision" reason)

- **`LookupValue.notes` has no create/update write path** yet is exposed on the response —
  documented as sanctioned by **build-create-dto A.3.7** (response may carry fields the create omits).
- **`findActiveByTypeCode` gated by `@PreAuthorize("isAuthenticated()")`** instead of a permission
  constant — documented spec deviation **DRV-006 / srs-MDM §B5** (platform-wide provider endpoint).
- **Search filters key on entity name `isActive`, not the response's `isActiveFl`** — a repo-wide
  convention (identical to `FileCategoryService`), not an MDM-specific defect.

Code-level bugs found in the same review (PUT partial-update null-out, nondeterministic
consumption ordering) and cleanups (duplicated active-filter/normalize logic, dead domain state)
were fixed directly, as they carried no governance implication.
