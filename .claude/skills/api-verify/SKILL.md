---
name: api-verify
description: "API VERIFICATION (post-implementation). Holds a module's published api-docs to its API document (api-spec-<mod>.yaml, OpenAPI 3.1): generates one runnable script that exercises the real API in dependency order, plus a problems report, runs it, and records the api_verify row. Standalone, on demand, never a gate. Use after a backend module's endpoints are implemented and its api-docs are regenerated and published."
---
Every path below comes from the factory's v7 consumer contract through one tool —
read it, never type it:

```bash
python3 scripts/gov-module.py pin                    # exit 3 = pre-v7 project → STOP ("the factory owner runs gov.py upgrade-project")
python3 scripts/gov-module.py plan <MOD> --json      # api_spec · api_docs · test_plan · exec_units/test_units/integration (tests) · delivered_version · state_file
```

`<PART>` below is this track's partition, `governance/shared/backend/modules/<MOD>/`
(the folder holding `plan.state_file`).


# Skill: api-verify

## Description

**API VERIFICATION.** The API document (`plan.api_spec`) is the contract; the api-docs this
repo publishes (`plan.api_docs`) are the delivered surface. This skill holds the second to the
first: it turns them into one runnable script that calls the real API end-to-end in dependency
order, runs it, writes a report of what it found, and records the result as data the factory and
the frontend read (`api_verify` row). It designs no test and invents no rule, message,
dependency, or data value of its own — anything not traceable to an input document is skipped
and named as skipped.

This skill is **translation, not derivation**, and it is **module-agnostic** — it runs
identically for any `<MOD>` (SEC, FIN, MDL, FILE, CU, NOTIF, or any module registered later).
Nothing about a specific module is hard-coded here; every fact that can change — base path,
envelope shape, error format, languages, permission pattern, output location — is read from
[`governance/shared/platform/rules/api-verify-config.md`](../../../governance/shared/platform/rules/api-verify-config.md), never typed
into this file. If that file and this one ever disagree, `api-verify-config.md` wins — update
it, not this skill.

## When to Use

- A backend module's endpoints are implemented and its api-docs were just regenerated and
  published (`/generate-api-docs <MOD>` → `<PART>api-docs/`)
- Post-implementation verification against the real running API — not a code-review, not a
  substitute for `gov-validate-backend-feature`
- On demand, invoked explicitly for a given `<MOD>` — never automatically, never as a gate

## When NOT to Use

- Before the module's api-docs exist or are stale — regenerate them first
  (`/generate-api-docs`); a stale doc produces a script that tests the wrong surface
- Without an API document (`plan.api_spec` missing — a pre-v7 project): there is no contract to
  hold the docs to; STOP and say so
- As a substitute for `gov-enforce-backend-contract` / `gov-validate-backend-feature` — those
  review code and architecture; this skill exercises the live HTTP surface
- To design new test cases from scratch — the factory's P4 backend test plan
  (`plan.test_plan`) and the packages' `tests` define them; this skill only consumes what the
  API document, the test plan and the api-docs already established

## Responsibilities

- State the inputs read (API document, api-docs, test plan) at the start of every run
- Produce one script that creates, reads, updates, and deactivates every documented entity in
  dependency order, tears itself down, and reports failures — nothing else
- Produce a problems report bucketed into *likely real bug* / *test assumption mismatch* /
  *infrastructure*

## Constraints

- MUST NOT invent a business rule, error code, dependency edge, or payload value not present
  in the API document, the api-docs or the test plan
- MUST NOT ask questions — an unresolvable ambiguity (a dependency cycle, an ambiguous
  payload) is an ADR-worthy blocker: state it and stop, do not guess
- MUST NOT change any line artifact, mint any governance ID, or render a gate verdict — this
  runs entirely outside the governed pipeline; its one write into governance is the
  `api_verify` row (stage J), through `gov-module.py record`
- MUST NOT perform a hard delete unless the module's own api-docs document one
- MUST NOT write to the database outside the opt-in, transaction-scoped, run-created-ids-only
  path described in `api-verify-config.md` §4. "Write to the database" here means a *direct*
  write — a SQL statement the script issues itself. State changes made by calling the platform's
  own documented endpoints are not that: they are the thing being verified, and they are governed
  by stages F and I instead
- MUST NOT embed a literal credential in a generated script — credentials are run arguments or
  clearly-marked placeholders (`api-verify-config.md` §4), never typed into an artifact that
  gets committed
- MUST NOT mutate a pre-existing security record (role, grant, user) outside the single
  bounded exception in §3-I — and never at all against a non-Dev/Test target
- MUST NOT report a run as clean while records it created survive: every surviving id is
  listed in the report (§3-F), never summarised as "cleaned up"

## Output

- `<PART>test-api/test_<mod>_apis.py` (or the language the run targets)
- `<PART>test-api/<mod>_problems_report.md`
- one `api_verify` row in `plan.state_file`:
  `python3 scripts/gov-module.py record <MOD> api_verify --version <plan.delivered_version> --result PASS|FAIL`

---

## 1. Inputs

State them at the start of the run. There is one tier: the API document always exists for a
v7 module, so negatives are never self-derived and never skipped for want of a manifest (the
test-execution-manifest and `test_gen/` are retired).

| Input | Read from | Role |
|---|---|---|
| API document | `plan.api_spec` (`api-spec-<mod>.yaml`, OpenAPI 3.1) | **the contract** — every operation by `x-api-id`: method, path, parameters, request/response schemas, each error response with its catalog codes (`x-error-codes`), paging (`x-paginated`), traces (`x-traces`), the security scheme |
| api-docs | `plan.api_docs` (`<PART>api-docs/`: `index.md`, then `**/*.md`) | **the delivered surface** — endpoints as the built backend documents them: verbs, paths, request/response field tables, examples, contract ids |
| test plan | `plan.test_plan` (P4) and the `tests` of `plan.exec_units` / `plan.integration` | the `TC-*` ids a negative or happy path is tagged with — the same id space as the packages' acceptance tests |
| stack conventions | `governance/shared/platform/rules/api-verify-config.md` when the project publishes it; otherwise the API document's own `servers`, `security`, schemas and error responses | base path, envelopes, error format, permissions (§2) |

Pair operations and documented endpoints by method + path (trailing-segment match when the
served path carries a base prefix). An operation with no delivered counterpart, or a delivered
endpoint with no operation, is listed in the report under *contract mismatch*; the script does
not invent a call for it.

Multi-batch api-docs (a large module documented across several endpoint files): reconcile
received endpoints against the API document's operations, report entity-level coverage briefly,
never ask — list every operation without a delivered counterpart as *not yet served*. One
function per entity lets a later batch append without a rewrite.

## 2. Stack conventions

Base path, verb→operation mapping, response/paging/error envelopes, error-code format,
`DELETE` semantics, permission pattern, languages: the API document states what it covers
(`servers`, `security`, schemas, error responses, `x-paginated`); the rest is read from
`governance/shared/platform/rules/api-verify-config.md` §3 when the project publishes it. Do
not restate or override those values here or in the generated script; if a module's actual
behavior disagrees with them, that is either a contract mismatch (stage G) or a convention that
changed (fix the source, not this run). Where `api-verify-config.md` is absent, its §4 safety
rules below still hold as written: Dev/Test target only, DB writes opt-in and off by default.

## 3. Processing pipeline

**A0 — Preconditions (before any suite runs).** Every payload value that references a row the
run does not itself create — a foreign/registry code, an owner-module code, a lookup key, a
parent id taken from an example rather than threaded — is *verified to exist and be active*
first, through a documented read endpoint. A missing one is reported once, up front, as a
precondition failure naming the exact value and the document that supplied it, and its
dependent suites are marked blocked-on-precondition. Never let it surface instead as a wall of
downstream assertion failures: an example value in a governed document is a claim about the
environment, and a claim that is false is a finding about the document or the seed data, not
about the endpoint under test.

A0 runs **after** stage I, not before it, whenever the run needs a grant at all: the read
endpoints A0 checks through are themselves permission-gated, so a run that verifies
preconditions before granting itself access reports a wall of 403s as "missing preconditions"
and hides the real state of the data. Order is grants → preconditions → suites.

**A — Inventory.** Parse every operation of the API document (`x-api-id`, method, path, entity
from the path, operation type from verb + path suffix, request/response schemas, error responses
with their codes) and every documented endpoint of the api-docs (verb, path, request field table
— name, type, required, constraints, example — response shape). Pair them (§1).

**B — Dependency order.** Infer edges from foreign-key request fields (the API document's
schemas, `DBF`-typed references in `x-traces`) and the api-docs' "parent …" descriptions, then
sort topologically; roots first. A cycle or an unresolvable edge → stop and report it as a
blocker, do not guess.

**C — Negative mapping.** For every error response of every operation (`responses.<status>`
with `x-error-codes`): set up the precondition the catalog row describes (the `RULE-*` the
backend plan's `error-catalog` block cites for that code — cite it, do not restate it), call the
endpoint that must be blocked, assert the HTTP status and the runtime code (never the
hyphenated governance RULE id), and tag the function with the code and the `TC-*` of the test
plan that names the same operation and code. No such TC → tag the code only and list the
operation+code as *untraced negative* in the report.

**D — Assembly.** One fixed structure (§4), one `test_<entity>()` per entity in stage-B order,
FK ids threaded as parameters from the parent's own return value — never hard-coded.

**E — Exploratory scenarios (bounded).** Only for fields with `required = yes` or a stated
constraint (length, numeric range): at-limit / one-over, omission, type mismatch, an
undocumented lookup value, CRUD idempotency (deactivate twice, activate an already-active
record), invalid FK reference. **Assert vs observe:** an outcome governed by a RULE + code is
stage C's; any other expected status is undocumented → executed as an *observation* (recorded,
never pass/fail, listed apart in the report). No documented source value → skip the case.

**F — Teardown.** Every created id is tracked per entity; cleanup runs in `finally`, in reverse
dependency order; a hard-delete endpoint is used only when the api-docs document one, otherwise
the deactivate endpoint is called and the report states that records remain (deactivated) —
never a silent "cleaned up". That statement is a **list, not a sentence**: a `Surviving records`
section naming every id/natural key left behind and why (no documented hard delete, or a failed
cleanup call), so a human can purge them. Rows the run adds to a *registry or reference table
another module's rules read* (a module registry, a status/type catalogue) are called out
separately as **permanent residue**, with suggested cleanup SQL per stage H — such a row keeps
influencing other modules' behaviour long after the run ends, which an ordinary deactivated
fixture does not. Where that table exposes *any* documented way to retire a row (deactivate,
archive, soft-delete), teardown uses it rather than leaving the row active: an abandoned active
registry entry is not inert test data, it is a value another module's rule may now match on.
Codes written into such a table carry the run's own namespace so residue is always attributable.

**G — Problems report.** `<mod>_problems_report.md` lists only failures, bucketed: *contract
mismatch* (the delivered surface departs from the API document — the backend's to fix, or a
gap recorded for the factory: `gov-module.py record <MOD> api_doc_gaps --key "<METHOD> <path>"
--resolution "OPEN — …"`), *likely real bug* (a documented rejection did not happen, or an
unexplained status), *test assumption mismatch* (rejected, but with a status the API document
did not state — the document is the factory's to fix, through a gap, not the backend),
*infrastructure* (connection / timeout — rerun). Ambiguous → *likely real bug*.

**H — Log correlation and database access (opt-in).** Log excerpts around a failing call are
attached as *approximate* unless a trace id exists. Database writes are two-tier: deleting rows
this run created (tracked ids only) is allowed for teardown; any other data fix is emitted as
*suggested SQL* for a human — never executed. All DB writes sit behind an explicit opt-in flag
(default off = print only), in a transaction, with separate credentials — see
`api-verify-config.md` §4.

**I — Permission preconditions (the one sanctioned exception to §6's "records it did not
create").** A module's endpoints are permission-gated, and the run's own account may legitimately
lack those permissions — a module registers its screens/actions without granting them to anyone,
by design. The run may therefore grant itself the module's documented permissions through the
platform's own documented grant endpoints, under **all** of these, or not at all:

- **Dev/Test target only** — verified against `api-verify-config.md` §4.1 before the first grant
  call; a target that is not demonstrably Dev/Test aborts the run rather than escalating.
- **Only what the documented endpoints under test require** — never a broader role, never a
  permission outside `<MOD>`'s own set.
- **Journal before granting, not after.** Append the intended grant to the on-disk journal named
  in `api-verify-config.md` §4.2 *before* the call that creates it. `finally` does not survive
  `SIGKILL`, a container stop, or a lost machine — the journal is what makes an interrupted run
  auditable instead of an invisible standing privilege.
- **Revoke only what this run newly created**, in `finally`; a grant that already existed is
  left untouched and reported as pre-existing. Treat a duplicate-grant rejection as "already
  present", never as a failure.
- **Disclose in the report either way** — what was granted, what was revoked, and anything the
  revoke failed to remove, named explicitly as standing privilege needing manual removal.
- **Assume you are not alone.** "Already granted" may mean a concurrent run granted it seconds
  ago and is about to revoke it underneath you. Before treating a pre-existing grant as usable,
  check the journal (§4.2) for an unmatched `GRANT` from another `RUN_ID`; if one is there, stop
  and say so rather than racing — two runs sharing one role will fail each other in ways that
  read like authorization bugs in the module under test.

Anything beyond this — creating a role, elevating a human user, touching a grant the run did not
make — is out of scope and stays out.

**J — The result reaches the factory as data.** After the script runs, record one row:

```bash
python3 scripts/gov-module.py record <MOD> api_verify --version <plan.delivered_version> --result PASS   # only when the script exits zero
python3 scripts/gov-module.py record <MOD> api_verify --version <plan.delivered_version> --result FAIL   # otherwise
```

The frontend's delivery of that version stays OPEN until this row is PASS. Then `python3 scripts/gov-module.py validate <MOD>`
and `./scripts/governance push` publish the row and the `test-api/` output.

## 4. Script structure

```
# Intended for Dev/Test environments only.
config      : BASE_URL (argument or localhost default) · credentials (placeholders unless given)
              · auth endpoint · DB opt-in flag
client      : thin HTTP wrapper (get/post/put/patch/delete) that unwraps the response envelope
results     : TestResult / TestSuite records · run() for asserted calls · run_observation() for
              stage-E observations (separate bucket, never in totals)
helpers     : extract_token · extract_id · first id of a page (per the paging envelope)
grants()    : stage I, only if the run needs permissions it lacks — assert a Dev/Test target
              first, then journal-append → grant, recording which grants this run newly
              created (skipped entirely when unneeded)
preflight() : stage A0 — assert every externally-owned referenced value exists and is active;
              a miss blocks its dependent suites with a precondition failure, never a cascade
per entity  : test_<entity>(parent_ids…) → create → get-by-id (ok) → get-by-id (missing id →
              not found) → update → [stage C negatives] → [stage E observations] → deactivate →
              activate ; appends created ids
cleanup()   : stage F, then stage I's revoke of self-created grants (journal-append after each)
report      : Markdown (+ optional HTML) with pass/fail suites, an "observations" section, the
              problems buckets, a "Surviving records" list and a "Privileges" line →
              <mod>_problems_report.md ; never persists an auth response or a token
main()      : grants() → preflight() → suites in stage-B order, all inside try/finally; exit
              non-zero on any asserted failure (observations never affect the exit code)
```

Every test function carries a traceability comment: `Covers: API-… ; Negative: RULE-… / <code> / TC-…`.

Payload rules: use the api-docs' `example` values verbatim; a required field without an example
gets a clearly marked placeholder of the right type; lookup/enum values only as seen in the
docs; FK ids threaded, never literal.

## 5. Generation gate (silent; only failures are reported)

```
[ ] every operation of the API document has a test function, or is listed as not yet served
    (contract mismatch)
[ ] every entity in the api-docs has a test_<entity>() function
[ ] main() order = stage-B order — no forward FK reference
[ ] every payload value comes from an example or a marked placeholder
[ ] negatives only where the API document states status + code — none self-derived; tagged
    with the test plan's TC where one names that operation and code
[ ] exploratory scenarios only on required/constrained fields; undocumented outcomes use
    run_observation()
[ ] every create appends its id; cleanup() in finally, reverse order; hard-delete used only if
    documented
[ ] runtime error codes in the format api-verify-config.md states; page fields as documented
[ ] no invented credentials, business codes, or lookup values; traceability comment on every
    function
[ ] every externally-owned value a payload references (registry/owner/lookup code, example-
    sourced parent id) is precondition-checked by preflight() before the suites that need it
[ ] the report writer emits a Surviving records list from the tracked-id set, with permanent
    registry/reference residue in its own section — not a hardcoded "cleaned up" string
[ ] if the script grants anything: preflight asserts a Dev/Test target, the journal append
    precedes every grant call, revoke is in finally and guarded on newly-created-by-this-run,
    and the report writer emits the Privileges line unconditionally
[ ] no literal credential anywhere in the script — argument or marked placeholder only
[ ] no auth response, token, or Authorization header value reaches the report or the journal
```

## 6. Boundaries

| Consumes (read-only) | Produces | Never |
|---|---|---|
| the API document, the api-docs, the P4 test plan, run arguments, `api-verify-config.md` (when published) | `test_<mod>_apis.py`, `<mod>_problems_report.md` under `<PART>test-api/`, the stage-I grant journal, one `api_verify` row (stage J) | a governance ID of any kind, a change to any line artifact, a gate verdict, a data fix against records it did not create — **one exception, and only one**: the bounded, journalled, self-revoked permission grant of stage I |

## Related Skills

| Skill | Purpose |
|-------|---------|
| [`gov-validate-backend-feature`](../gov-validate-backend-feature/SKILL.md) | Static/architectural validation before this skill's live run |
| [`gov-enforce-backend-contract`](../gov-enforce-backend-contract/SKILL.md) | The 85-rule contract this skill's target code was already required to pass |
