# Handover to frontend — FIN round 2

**Date:** 2026-09-19 · **Module:** FIN · **Repo:** `backend`
**Answers:** `BACKEND PROMPT — FIN round 2: what your delivery closed, and the four things still open`
**Prior:** `HANDOVER-FE-fin-blockers-2026-09-19.md` (round 1), `HANDOVER-FE-fin-plan-edits-2026-09-19.md`

---

## TL;DR

| Your ask | Answer |
|---|---|
| §1 operator handling | **`400`.** The api-docs are right, the handover you read was stale — corrected the same day |
| §2 layout | Confirmed, with a correction: the generator no longer hard-codes `tracks.backend.partition` either |
| §3 contract ids | Now recorded from the backend side too, on the line `origin/main` actually points at |
| §4(a) twelve page codes | **Verified on a fresh database** — 12 served, exact match, no extras |
| §4(b) eleven lookup keys | **Verified on a fresh database** — all seeded, with counts below |
| §4(c) MDL contract id | Still MDL's registry; recorded, unchanged |
| `periods` on a search row | Fixed — the field now says so itself |
| round-1 §7 | Answered in round 1 §7; restated below |

---

## 1. `parentAccountId` + `LIKE` is a `400`. The docs are right.

Run against the app the docs were generated from:

```
POST /api/v1/fin/accounts/search
{"filters":[{"field":"parentAccountId","operator":"LIKE","value":5}]}

400 {"error":{"code":"VALIDATION_ERROR","fieldErrors":[
      {"field":"parentAccountId",
       "message":"This search does not support the LIKE operator on \"parentAccountId\""}]}}
```

Build your error handling against "rejected". It is a promise.

**Why the contradiction existed, since you were right not to guess.** The handover sentence you
quote was true when it was written, and it understated a real defect. A code review the same day
found that discarding the operator was worse than "treated as `EQUALS`" in two cases:

- `NOT_EQUALS parentAccountId=1` returned the rows whose parent **is** 1 — the exact inverse of the
  request;
- `IN [1,4]` dropped the filter entirely, so a node's children pane would have rendered the whole
  chart of accounts.

Both now answer `400` carrying `UNSUPPORTED_FILTER_OPERATOR` on the offending field. The fix went
into the shared value extractor, so it also closes the same hole on the four lifted fields that
predate this work — `periodId`, `dimensionId`, `sourceAccountId` and the fiscal-period
`fiscalYearId` — none of which had ever been safe either. `EQUALS` is unaffected, and the published
field lists now say that an unsupported operator is refused as well as an unknown field.

The handover was corrected in the same commit rather than quietly edited. Your copy predates it.

## 2. Layout — confirmed, and a correction to what you expect of the generator

Yes: once the v7 tree is on `origin/main`, I regenerate and give you the commit id. Nothing from me
until you say it is pushed.

**But do not expect the fix you describe.** Your §2 says my `discovery.py` change makes
`default_output_dir` resolve against `tracks.backend.partition` instead of `paths.modules`, and
that this is exactly what the merged tree needs. That was the first repair and it was **also
wrong** — it just failed in the other direction. Hard-coding the partition sent FIN's docs to
`erp/modules/FIN/backend/api-docs/` on this profile, beside the real ones, which is the same defect
mirrored. Two empty-history copies in one day, from two opposite assumptions.

What it does now: it asks the checkout instead of assuming. Whichever candidate directory already
exists wins; if both exist it refuses and names them; if neither exists it refuses and names both
plus `--output`, rather than guessing. **On the merged v7 tree that resolves to
`backend/modules/FIN/api-docs/`** — the answer you want — because that directory is there, not
because the rule is hard-coded to the partition.

**The root cause is yours-and-the-factory's, and it will bite `api-verify` too.**
`profile-summary.json` publishes `paths.modules`, `tracks.backend.partition` and `module_dirs`, but
**no key declaring where api-docs belong**. The generator is the one consumer that writes them, so
it must infer. The three places that state the path in prose disagree, and one names a directory
absent from this profile:

| Source | Says | Exists on this profile |
|---|---|---|
| `platform/rules/api-verify-config.md:14` | `governance/shared/backend/modules/<MOD>/api-docs/` | **no** |
| backend `CLAUDE.md` ownership table | `$GOV/modules/<MOD>/api-docs/` | yes |
| the delivered tree | `erp/modules/<MOD>/api-docs/` | yes |

`api-verify` reads the same documents, so it inherits the same ambiguity. The real repair is a
declared key in `profile-summary.json` that the tool reads and fails on when absent — the way it
already fails on a missing `tracks.backend.partition` — and the matching correction to
`api-verify-config.md:14`. Both are the factory's; both are filed as `HUMAN`. Worth settling before
the merge rather than after, since the merge changes the answer.

## 3. Contract ids — now reported from this side as well

Agreed on all counts: six, `registry-exec-be-fin.md` is neither of ours, and it closes only when the
factory issues them. `API-FIN-038` for the fiscal-year search is a sensible suggestion and this
backend will bind whatever number arrives.

One thing needed fixing before your ask could actually be met. My gap entries were recorded on the
v7 tree — the same unpushed tree §2 is about — so from `origin/main` the backend side reported
nothing at all. They are now on the line `origin/main` actually points at:
`erp/modules/FIN/backend/execution-state.json`, 40 → 48 entries, the contract-id item among them and
corrected to six. So the gap is visible from both channels even if the v7 merge slips.

## 4. Runtime prerequisites — verified on a fresh database, not read off the migrations

Round 1 §6 did answer these, from `V24__fin_security_seed.sql` and `V26__fin_mdl_lookup_seed.sql`.
That was reading SQL, and you asked for a deployment. Reading a seed file does not prove a fresh
database comes up with it — ordering, a later migration undoing it, a permission the menu query
filters out. So this time it was actually run: a throwaway database, Flyway from empty, 29
migrations applied, the app booted against it, every answer below fetched over HTTP as `admin`.

**(a) The twelve page codes, from `GET /api/v1/sec/menu`:**

```
FIN_ACCOUNTS · FIN_ACCOUNT_LEDGER · FIN_ALLOCATION_RULES · FIN_BALANCE_SHEET
FIN_DIMENSIONS · FIN_DIMENSION_REPORTS · FIN_INCOME_STATEMENT · FIN_JOURNAL_ENTRIES
FIN_PERIODS · FIN_RECURRING_TEMPLATES · FIN_RULES · FIN_TRIAL_BALANCE
```

12 served. **Missing: none. Extra: none.** Exact match with your list, so your screen-level guard
will resolve every FIN route on a fresh deployment. `POST /fin/accounts/search`,
`/fin/fiscal-years/search` and `/fin/event-rules/search` all answered `200` on that same instance,
so the permission chain works end to end and not only in the menu payload.

**(b) The lookup keys, from `GET /api/v1/mdl/lookups?type=<key>` on the same fresh database:**

| Key | Values | | Key | Values |
|---|---|---|---|---|
| `ACCOUNT_TYPE` | 5 | | `ACCOUNT_DERIVATION_TYPE` | 3 |
| `DEBIT_CREDIT` | 2 | | `AMOUNT_SOURCE_TYPE` | 3 |
| `PERIOD_STATE` | 4 | | `DISTRIBUTION_TYPE` | 3 |
| `FISCAL_YEAR_STATUS` | 2 | | `RECURRING_SCHEDULE_TYPE` | 2 |
| `JOURNAL_TYPE` | 5 | | `RECURRING_FREQUENCY` | 4 |
| `JOURNAL_STATUS` | 3 | | | |

All eleven non-host keys seeded. `ACCOUNTING_EVENT_TYPE` → 0 and `PAYMENT_METHOD` → 0, both
legitimately empty as you said. No FIN screen will render an empty select on a required coded field.

One caveat on `JOURNAL_TYPE`: the five values are `EVENT_GENERATED`, `MANUAL`, `RECURRING`,
`ALLOCATION`, `REVERSAL`. `CLOSING` and `OPENING` are **not** seeded — they exist only as internal
constants for year-end close, so a caller cannot name them. A manual entry sent with
`journalTypeCode: "CLOSING"` answers `400 FIN-400-INVALID-LOOKUP`, not the `409` a reader of the
test plan might expect. Already recorded and escalated on our side.

**(c)** `GET /api/v1/mdl/lookups` carrying no contract id is MDL's registry. Unchanged, still
recorded, still not FIN's to issue.

## 5. `periods` on a fiscal-year search row — fixed in the field itself

You were right that the handover said it and the schema did not. The field now carries it:

> *"Generated periods. Populated by the create response only; ALWAYS EMPTY on a search row, where
> the period set is read through the fiscal-period search scoped by `fiscalYearId`"*

Regenerated, so it is in the api-docs. The next reader cannot wire a Detail pane to it by accident.

## 6. Round-1 §7 — the three unreachable catalog rows

These were answered in round 1 §7; restating so nothing is carried on a guess:

- **`FIN-503`** — confirmed dead. The platform's `Status` enum has no `SERVICE_UNAVAILABLE`, and
  cross-module calls here are in-process Spring injection with no network hop, so "MDL unreachable"
  has no producer. It was already recorded as struck on our side. Drop it.
- **`FIN-403-SOD-VIOLATION`** — confirmed unreachable. Drop it from your message catalog. We did not
  remove the registered code here: deleting a published code from the implementing side would be a
  silent contract change, and the catalog is the factory's.
- **`JOURNAL_STATUS.VOID`** — confirmed seeded (3 values, above) and unreachable through classic
  reversal, which leaves the original `POSTED`. Rendering it defensively is the right call.

So: three messages you can drop, none of which the backend can delete for you.

---

## What is open from this side

1. **§2 regeneration** — waiting on your push. Say the word and it is a few minutes.
2. **The profile's missing api-docs key** and `api-verify-config.md:14` — filed `HUMAN`, the
   factory's. Best settled before the merge.
3. **The six contract ids** — filed from both sides now.

Nothing else from round 2 is outstanding.
