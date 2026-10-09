# BRIEF — stage `test-gen` (Test Generation) · module FIN · v2 · profile `erp`

Lane `test-gen` · implementer claude:opus · effort high · round 1

## Rules that bind this run
- Questions: **forbidden**. A `[QUESTION]` block is refused. Ambiguity → ADR in `analysis/decisions/FIN/` (`ADR-{MOD}-{seq:03d}.md`): non-breaking → continue; breaking → status BLOCKED and stop.
- Owns IDs: TC — ID grammar `{prefix}-{MOD}-{seq}` (seq width 3); never re-number, never restart a sequence.
- Read only what this brief contains (generated current state); never open version folders yourself.
- Write exactly these files (complete files; this is a delta version, so only what changed):
- `governance-shared/analysis/modules/FIN/v2/test_gen/backend-test-plan-fin.md`
- `governance-shared/analysis/modules/FIN/v2/test_gen/frontend-test-plan-fin.md`
- `governance-shared/analysis/modules/FIN/v2/test_gen/test-execution-manifest-fin.md` (optional)
- `governance-shared/analysis/platform/system-test-index-erp.md` (optional)
- `governance-shared/analysis/modules/FIN/v2/change-manifest.md` — fill in its `## Per artifact` section for what you wrote (ADDED / MODIFIED / REMOVED / UNCHANGED); it is ONE file per version, at the version root, never in a stage folder.
- Respond with one `<<<FILE: <repo-relative path>>>> … <<<END FILE>>>` block per file when running through a command runner; when running as the operator, write the files directly.

## Contracts checked by `gov.py analyze` after this stage
- **C10** acceptance criteria → test generation (standalone): C10.1 traces {'from': 'TC', 'to': ['AC', 'XM', 'UXD'], 'min': 1, 'mode': 'any'} [CRITICAL]; C10.2 orphans {'kind': 'AC', 'referenced_by': ['TC'], 'min': 1} [MAJOR]; C10.3 markers {'artifact': 'backend-test-plan', 'track': 'backend', 'plan': 'test'} [CRITICAL]; C10.4 markers {'artifact': 'frontend-test-plan', 'track': 'frontend', 'plan': 'test'} [CRITICAL]; C10.5 ids-owned {'stage': 'test-gen'} [CRITICAL]; C10.6 exists {'artifact': 'test-execution-manifest', 'when': 'profile.stack.testing.manifest'} [MINOR]; C10.7 msg-bound {'plan': 'frontend-execution-plan', 'tests': ['frontend-test-plan'], 'spec': 'analyze.messages', 'format': 'stack.backend.api.error_code_format', 'rule_kind': 'RULE', 'kind': 'TC'} [MAJOR]; C10.8 tc-data {'srs': 'srs', 'tests': ['frontend-test-plan', 'backend-test-plan'], 'spec': 'analyze.catalogue', 'kind': 'TC'} [MAJOR]; C10.9 tc-consistency {'srs': 'srs', 'tests': ['frontend-test-plan', 'backend-test-plan'], 'spec': 'analyze.catalogue', 'kind': 'TC'} [MAJOR]

---
# ENGINE
```
ENGINE        : test-gen — Test Generation   (STANDALONE — outside the line, on demand)
LANE          : test-gen · questions forbidden · derives from `AC-*` (module) + `XM-*`/`UXD-*` (integration)
SCOPE         : module · modules FIN
MODULE        : FIN · v2 · profile erp (ERP Platform)
READS         : srs · backend-execution-plan? · frontend-execution-plan? · registry-srs · registry-db? · registry-exec-fe?   (from _state/ — "?" = optional — read for EACH module in scope)
PRODUCES      : backend-test-plan-fin.md · frontend-test-plan-fin.md · test-execution-manifest-fin.md (optional)
OWNS IDS      : TC
FRAMEWORK     : backend `agnostic` · frontend `agnostic` · manifest ON   (profile.stack.testing)
BOUNDARY      : analysis-only — test PLANS, never test code; never a gate for the core
```

# Test Generation — engine reference

## 0. Position

This engine runs **outside the governed line** (`factory.yaml → standalone`). No pass gates
on it, it gates nothing, and the pass gates never wait for it. It is invoked on demand
(`/test-gen`) once the
artifacts it consumes exist in `_state/`. It **invents nothing**: no rule, error,
endpoint, field, screen or cross-module flow — it only adds test cases and the indexes
derived from them.

Questions are `forbidden`; ambiguity → `factory.yaml → ambiguity` (ADR, then
`continue`; breaking → `BLOCKED`,
`stop`) — shared/GOVERNANCE-CORE.md.

Delta versions: read `_state/` as the baseline, emit only ADDED / MODIFIED / REMOVED
test cases, continue the `TC` sequence — shared/VERSIONING.md.

## 1. Inputs

| Input | Read from | Use |
|---|---|---|
| `srs` | `_state/current-srs.md` | **the derivation source**: every `REQ-*` with its `AC-*` (Given / When / Then), `RULE-*` messages, screens, permissions |
| `backend-execution-plan` (optional) | `_state/current-backend-execution-plan.md` | `API-*` (verb, path, request/response, catalog codes) to bind backend steps to endpoints; its `XM-*` blocks (target module, type HARD-FK/SOFT-READ, traces) are the **integration derivation source** on the backend track |
| `frontend-execution-plan` (optional) | `_state/current-frontend-execution-plan.md` | `SCR-*`, routes, F-blocks to bind frontend steps to screens; its `UXD-*` references (screen, foreign field, owner module) are the **integration derivation source** on the frontend track |
| `registry-srs` | `_state/current-registry-srs.md` | ID ranges, coverage of REQ by API/SCR |
| `registry-db` (optional) | `_state/current-registry-db.md` | the module's `XM-*` register (target module, type) — cross-checked against the `XM` blocks above, never restated |
| `registry-exec-fe` (optional) | `_state/current-registry-exec-fe.md` | the module's `UXD-*` register (screen, field, owner module, API used) — cross-checked against the `UXD-*` references above, never restated |

A missing optional plan → the corresponding test plan is written in **REDUCED** mode
(steps reference the AC only, no endpoint/screen binding) and says so in its header. A
missing `registry-db`/`registry-exec-fe` never blocks module-scope derivation — it only
narrows what integration-scope cross-checking can do.

## 2. Scope — a command flag, never prose

```
gov.py run-standalone test-gen --module MOD                 # this run: scope=module, mods=[MOD]
gov.py run-standalone test-gen --modules MOD-A,MOD-B,…       # this run: scope=modules, mods=[MOD-A, MOD-B, …]
gov.py run-standalone test-gen --scope project               # this run: scope=project, mods=every module with a committed version
```

This run: **scope = `module`**, modules = `FIN`.

| Scope | TC sources | Phases populated |
|---|---|---|
| `module` | `AC-*` of the one module (§3) | the module-scope phases only (`TEST-PLAN-BE` on backend; the equivalent on frontend) — **identical in shape to a single-module run today** |
| `modules` | `AC-*` per module (§3) **+** `XM-*` (§4) and `UXD-*` (§5) between the *selected* modules only | module-scope phases for every selected module, plus the integration phase(s) (`profile…phases[*].integration: true`) wherever a real linking atom pairs two selected modules |
| `project` | everything `modules` does, across **every** module the factory has ever produced a version for, **plus** a derived coverage rollup | the same as `modules`, plus `system-test-index-{profile}.md` (§9) — no new atom, no new gate |

Rules that hold at every scope:
1. a module never gets an integration TC for a module that is **not** in the current
   selection — running `--module FIN` alone never touches another module's `XM`/`UXD`;
2. an integration phase with no real linking atom among the selected modules stays **absent**
   from the output — never an empty `PHASE` block, never a guessed pairing;
3. `--module` output is produced exactly as it always was — no integration phase key, no
   `system-test-index`, no extra section — so a single-module run stays byte-identical in
   shape to before this scope model existed.

## 3. Derivation (module scope) — every `TC-*` comes from an `AC-*`

`TC-*` (`TC-FIN-{seq}`, 3-digit seq,
one continuous sequence across the module — both plans share it, so no TC id repeats) traces → AC + XM + UXD. The
module-scope derivation is **mechanical**:

| AC part | becomes |
|---|---|
| **Given** | preconditions — data state, role/permission, system state (bound to real entities/screens from the plans) |
| **When** | the step list — for backend: the endpoint call (`API-*`, verb, path, payload from the AC); for frontend: navigation + user actions on `SCR-*` |
| **Then** | expected result — status/response (per `LocalizedException → {code, messageAr, messageEn}` when a RULE fires) or UI state; message asserted in every language (ar, en) |

Rules:
1. one `TC-*` per `AC-*`, always — an AC without a TC is a coverage gap (✗), never skipped;
2. an AC whose Then names a `RULE-*` violation yields the **violation** TC; its happy-path
   twin exists only if another AC states it — do not fabricate happy paths;
3. a **boundary** TC is added only when the AC (or the RULE it cites) states a numeric limit;
4. every TC cites, besides its AC: the `REQ-*`, and the `API-*` (backend) or `SCR-*`
   (frontend) it exercises, plus the `RULE-*` / catalog code when a violation is expected;
5. never reword a rule, message or endpoint — reference by ID/code; message text is copied
   character-perfect from the SRS in every language. A case that asserts a refusal by its TEXT
   needs the frontend plan to bind that text (`text: <source>` on the row routing the code —
   `analyze` msg-bound); a plan that routes the code without its words is a plan gap to record in an
   ADR, never a message to paraphrase here;
6. **catalogue values are seeded or created** (`analyze` tc-data). The SRS lookup section
   (`A6`) says which values each key SEEDS and which are host data ("added as data" / "host-specific" / "host-defined").
   A case whose data names a value the section does not seed carries a `Host data` line creating it —
   the key, the value, the state the case needs (active / inactive) and the call the OWNER of the
   lookup publishes for adding a value, by verb and path. Host data is site data: it is never moved
   into a product seed, and a case never assumes it exists;
7. **no two cases demand opposite states of one catalogue value** (`analyze` tc-consistency). One
   case needing a value absent and another holding it inactive, or one asserting an option set that
   leaves out a value another case needs, cannot share a catalogue. Give each such case its OWN host
   value (`<VALUE>_<TC seq>`), created by its own `Host data` line — the one departure from `Test data`'s "the values
   named in the AC" this engine allows, and the case's `Test data` line says so;
8. over-engineering guard: if a track's TC count exceeds ~2× its AC count, review — the
   extra TCs are almost always fabricated variants; remove them.

Scenario tags (one per TC): `HAPPY | VIOLATION | BOUNDARY | PERMISSION | STATE | INTEGRATION`;
data class: `VALID | INVALID | BOUNDARY | EDGE | ATTACK`.
Permission ACs (no `VIEW` / no action permission) produce `PERMISSION` TCs on both tracks.

## 4. XM → TC derivation (integration scope, backend)

Runs only at `scope: modules|project`, and only for an `XM-*` whose **target module** is
also in `FIN` — an `XM-*` targeting a module outside the selection is
left alone (not this run's concern, not a gap either).

Source: the `XM-*` blocks of the **declaring** (consuming) module's `backend-execution-plan`
(cross-checked against its `registry-db`, never restated — [XM-PROTOCOL.md](../../../shared/XM-PROTOCOL.md)).
The **declaring module owns the resulting TC** — one continuous `TC-{MOD}-<seq>` sequence,
same rule as module scope (design decision: integration TC ownership follows declaration,
not the target).

| XM type | TC scenario |
|---|---|
| `HARD-FK` | one `EXISTS` TC (the referenced row is present — request/flow succeeds) **and** one `MISSING` TC (the referenced row is absent — the physical constraint is honoured: the documented rejection, never a silent pass) |
| `SOFT-READ` | one `GRACEFUL-DEGRADATION` TC — the target read fails or returns empty and the declaring module's flow still returns a defined result (never a 500 / unhandled state) |

Rules:
1. never invent the target entity's shape — bind by `ENT`/`DBF` ID only, as the `XM` block
   already does; the TC exercises the declaring module's own `API-*`, not the target's;
2. tag every such TC `INTEGRATION`, data class per the row above;
3. `traces=` carries the `XM-*` id plus the `REQ-*` the XM itself traces to (and the `API-*`
   exercised, when the plan binds one) — never an `AC-*` that does not exist for it;
4. one `XM-*` never yields more than the two/one TC(s) in the table above — no fabricated
   extra scenarios ("over-engineering guard" of §3 applies here too).

## 5. UXD → TC derivation (integration scope, frontend)

Runs only at `scope: modules|project`, and only for a `UXD-*` whose **owner module** (the
module that owns the displayed data) is also in `FIN`.

Source: the `UXD-*` references of the **displaying** module's `frontend-execution-plan`
F4 blocks (cross-checked against its `registry-exec-fe`, never restated). The **displaying
module owns the resulting TC** (it is the one whose screen renders the foreign field).

| UXD case | TC scenario |
|---|---|
| foreign field rendered | one TC: navigate to the `SCR-*`, the foreign-owned field renders the value the owner module's API returns |
| foreign field empty / owner API failure | one TC: the screen shows its declared empty/error state (§A.3 `States` of the ui-ux-spec) — never a blank crash, never invented copy |

Rules:
1. never invent the owner module's field shape or a new permission — the UXD block and the
   owner's real `API-*` (from api-docs, when present in state) are the only sources;
2. tag every such TC `INTEGRATION`; `traces=` carries the `UXD-*` id plus its `REQ-*`/`AC-*`
   and the `SCR-*` it renders on;
3. one `UXD-*` never yields more than the two TCs in the table above.

## 6. TC block — framework-agnostic form

```
<!-- TC:TC-FIN-<seq>:START traces=AC-FIN-<seq>,REQ-FIN-<seq>[,API-FIN-<seq>|SCR-FIN-<seq>|XM-FIN-<seq>|UXD-FIN-<seq>] -->
### TC-FIN-<seq> — <title>
Derived from : AC-FIN-<seq>  (REQ-FIN-<seq>)   |   XM-FIN-<seq> (REQ-FIN-<seq>)   |   UXD-FIN-<seq> (REQ-FIN-<seq>, AC-FIN-<seq>)
Exercises    : API-FIN-<seq> <verb path>   |   SCR-FIN-<seq> <route>
Rule / code  : RULE-FIN-<seq> → <catalog code> | —
Scenario     : <tag> · data class <class> · language <ar|en|ALL>
Preconditions: <from Given — concrete entities, role, state | for XM/UXD: the target/owner entity present or absent>
Host data    : <KEY VALUE — state — created by <verb path of the owner's add-value call>> | none
Steps        : 1. … 2. … (from When — one observable action per step)
Expected     : <from Then — status / body shape / message per language / UI state>
Test data    : <values named in the AC; placeholders marked, never invented business data>
<!-- TC:TC-FIN-<seq>:END -->
```
Framework: `profile.stack.testing` is **agnostic** on both tracks — the block above is the whole
contract; the consumer repo chooses its tool and turns each TC into a test. No framework
name, annotation or file layout is mentioned anywhere in the plan.

## 7. Test plans — organised by the profile's test phases

Each track with a `test` plan in the profile gets one file per module, wrapped in the
profile's test phases with `TC` atoms (kind `TC`, level 3, parents PHASE/SUB,
plans test). Test-plan SUB ids are **bare** labels
(`factory.markers.rules.sub_unqualified_exempt_plans` = test).
A phase flagged `integration: true` in the profile is populated **only** at `scope:
modules|project`, per §2/§4/§5 — at `scope: module` it is skipped entirely, so single-module
output carries the module-scope phases only, exactly as before this scope model existed.

### Track `backend` — one file per selected module: `backend-test-plan-fin.md`

| Phase key | Split rule | SUB labels |
|---|---|---|
| `TEST-PLAN-BE` | SUB when TC count > 12 — grouped RULE-SCENARIOS / API-SCENARIOS | `RULE-SCENARIOS`, `API-SCENARIOS` |
| `INT-XM` _(integration — `scope: modules|project` only)_ | SUB when TC count > 8 — grouped per target module | — |
Layout (per selected module):
```
<header>   sources (_state files + versions) · scope `module` · framework note (§3) · REDUCED? · open ADRs
<!-- PHASE:TEST-PLAN-BE:START traces=<union of the TCs' REQ/AC> -->
  <!-- SUB:RULE-SCENARIOS:START traces=… -->  …TC blocks…  <!-- SUB:RULE-SCENARIOS:END -->
  <!-- SUB:API-SCENARIOS:START traces=… -->  …TC blocks…  <!-- SUB:API-SCENARIOS:END -->
  (SUBs only when the threshold is met — decide WHILE writing, from the TC count)
<!-- PHASE:TEST-PLAN-BE:END -->
TC TRACEABILITY INDEX   AC → TC · REQ → TC · API → TC · RULE/code → TC
COVERAGE                AC covered <n>/<total> (a gap is ✗ and blocks the run) · REQ covered · API covered
```
Backend grouping hint: rule-driven ACs (violations, state transitions) vs endpoint-driven ACs
(happy paths, permission, paging/empty-result per `Page<T>`); integration TCs (§4) group by target module inside `INT-XM`.

### Track `frontend` — one file per selected module: `frontend-test-plan-fin.md`

| Phase key | Split rule | SUB labels |
|---|---|---|
| `TEST-PLAN-FE` | SUB when TC count > 8 — grouped UI-FLOWS / INT-FLOW | `UI-FLOWS`, `INT-FLOW` |
| `INT-UXD` _(integration — `scope: modules|project` only)_ | SUB when TC count > 8 — grouped per source module | — |
Layout (per selected module):
```
<header>   sources (_state files + versions) · scope `module` · framework note (§3) · REDUCED? · open ADRs
<!-- PHASE:TEST-PLAN-FE:START traces=<union of the TCs' REQ/AC> -->
  <!-- SUB:UI-FLOWS:START traces=… -->  …TC blocks…  <!-- SUB:UI-FLOWS:END -->
  <!-- SUB:INT-FLOW:START traces=… -->  …TC blocks…  <!-- SUB:INT-FLOW:END -->
  (SUBs only when the threshold is met — decide WHILE writing, from the TC count)
<!-- PHASE:TEST-PLAN-FE:END -->
TC TRACEABILITY INDEX   AC → TC · REQ → TC · SCR → TC · RULE/code → TC
COVERAGE                AC covered <n>/<total> (a gap is ✗ and blocks the run) · REQ covered · SCR covered
```
Frontend grouping hint: per-screen flows (search, create/edit, violation shown on screen,
permission-hidden affordance) vs the single module lifecycle flow (create → search → update →
deactivate → gone from active results); the composite-screen invariant (entry never rendered open by default) is one TC when an AC states it; integration TCs (§5) group by source (owner) module inside `INT-UXD`.

## 8. Test-execution manifest (ON — `profile.stack.testing.manifest`)

Emitted in the same run, after the backend test plan, as
`test-execution-manifest-fin.md` —
a **derived view** for the `api-verify` standalone (it introduces no ID), one per module:
```
DEPENDENCY ORDER      topological entity build order (from FK/XM relations in the backend plan and "must belong to" RULEs)
RULE → CODE → TC      RULE-* │ catalog code (runtime format per LocalizedException → {code, messageAr, messageEn}) │ TC-* │ HTTP │ API-*   (informational-only RULEs excluded)
ENTITY CRUD CHECKLIST ENT-* │ create │ read │ search │ update │ deactivate (soft) │ activate (each ✓ / —, from the API-* set)
```
Regenerate whenever the backend plan or test plan changes — a stale manifest is a defect.

## 9. System test index (`scope: project` only)

Emitted once per run, after every selected module's test plans, as
`system-test-index-erp.md` — a platform-level **derived view**
(introduces no ID, never a gate, `paths.platform` — same tier as `project-registry.md`):
```
SYSTEM TEST INDEX — ERP Platform — generated {scope: project}
AC → TC       per module: AC covered <n>/<total>, list of uncovered AC (✗)
XM → TC       every XM-* between two modules that both have a committed version: covered ✓/✗ (✗ = gap, never silently dropped)
UXD → TC      every UXD-* whose owner module also has a committed version: covered ✓/✗
COVERAGE %    per module: AC%, and — where the module both declares and is targeted by XM/UXD — integration%
CROSS-MODULE  matrix of module → module, one row per XM/UXD pair, TC id(s) covering it (— if none: gap ✗)
```
Never restates TC content — every row is an ID reference. A module the platform has never
produced a version for is out of scope (not a gap): the index only rolls up modules that
exist.

## 10. Split

The same toolkit splits test plans, with plan key `test`
(`factory.tracks.<track>.packages.test` → `backend-test`, `frontend-test`), **per module**:
```
gov.py split --track <track> --module <MOD> --version <v> --plan test --dry-run   # validate, non-zero exit = fix first
gov.py split --track <track> --module <MOD> --version <v> --plan test
```
Every TC atom is verified by content hash (`factory.markers.rules.verify` = sha256) after the split.
`system-test-index-erp.md` is a platform-level file — it is never split, never packaged, never delivered to a consumer repo.

## 11. Self-check before finishing

```
[ ] every AC-* in the SRS (of every selected module) has ≥1 TC-* (coverage ✗ = not done)
[ ] every TC-* carries traces= with its AC-*/XM-*/UXD-* source (+ the upstream ids the plan names) and the atom marker pair
[ ] every TC names one of AC/XM/UXD as its source; no reworded rule/message/endpoint; test data never invented
[ ] phases = the profile's test phases, in order; SUB labels bare; thresholds checked while writing
[ ] framework wording matches §6; manifest emitted iff profile.stack.testing.manifest (per module)
[ ] at scope module: no integration phase, no system-test-index — output unchanged from before
[ ] at scope modules|project: every XM-*/UXD-* between two SELECTED modules has ≥1 TC or is
    recorded as a gap (✗) — never silently dropped, never fabricated when absent
[ ] at scope project: system-test-index-erp.md rolls up every module with
    a committed version; every AC/XM/UXD gap listed is ✗, exactly like a module-scope AC gap
[ ] every host-data value a case names has a `Host data` line with the owner's add-value call;
    no two cases share a host value in opposite states (tc-data, tc-consistency)
[ ] every refusal a case asserts by its text is bound in the frontend plan (msg-bound) — or an ADR
    records the plan gap
[ ] ADRs written for every derivation choice that was not mechanical
```

## 12. Boundaries

| Owns | References (never redefines) | Never |
|---|---|---|
| `TC-*`, the test plans, the manifest | `REQ/AC/RULE` (P1), `API` (P3.1), `SCR/UXD` (P3.2 — `UXD` is `P3.2`'s, cited never redefined), `DBF/XM` (P2 — `XM` is `P2`'s, cited never redefined), catalog codes | test code, framework scaffolding, any edit to a line artifact, any gate or verdict, a cross-module TC for a module outside the current selection |


---
# INPUTS (generated current state)

<<<INPUT: srs>>>
# SRS — الحسابات العامة / Finance (General Ledger) (FIN) — DELTA v2
══════════════════════════════════════════════════════════════════
Module : FIN   Version : v2 (delta on v1 — change set CS-FIN-001)   Profile : erp
Inputs : prd, domain-profile, project-registry, change-manifest (PRD v2 approved 2026-09-23 by hesham421)
Counts : REQ 77 · AC 83 · ENT 16 · RULE 28 · SCR-REQ 13 · ADR 8 (this stage)   (current state, v1 + v2)
Delta  : ADDED — ENT 2 · REQ 31 · AC 37 · RULE 11 · SCR-REQ 1 · ADR 8
         MODIFIED — ENT-FIN-007, ENT-FIN-008 (A3 adaptation note only, pass-1) · ENT-FIN-010 · REQ-FIN-045 · AC-FIN-045 · RULE-FIN-004 · RULE-FIN-008 (wording only, pass-1) · SCR-REQ-FIN-003 · SCR-REQ-FIN-006
         REMOVED — none (the lookup value DIRECT leaves ACCOUNT_DERIVATION_TYPE; no ID is retired — ADR-FIN-010)
══════════════════════════════════════════════════════════════════

Delta reading note: this file re-emits only the ADDED and MODIFIED records. Each v1 record not
re-emitted here stays in force unchanged and is carried into `_state/current-srs.md` by `gov.py state`.
The sections that carry no ID (A1, A2, A3 preamble, A6, A7, A8, STANDALONE) replace their v1
counterparts in the folded state, so they are restated in full: v1 content unchanged, v2 content marked.

# PART A — MODULE FOUNDATION

## A1 — Document information
| Item | Value |
|---|---|
| Module | FIN — الحسابات العامة / Finance (General Ledger) |
| Feature code | FIN |
| Version | v2 (delta — CS-FIN-001, ADDITIVE) |
| Date | 2026-09-23 |
| Status | DRAFT (P1) |
| Prepared by | governance-factory (analysis lane) |
| Decisions applied count | 8 new ADRs (ADR-FIN-023 … ADR-FIN-027, ADR-FIN-037 … ADR-FIN-039 — the last three raised by this pass-1 revision) + 7 upstream v2 ADRs applied (ADR-FIN-010 … ADR-FIN-014, ADR-FIN-020, ADR-FIN-021) + 5 DEFAULTs (STANDALONE → Decisions applied) |

## A2 — Functional context

**In scope (v1, unchanged):** شجرة حسابات هرمية بأبعاد كبيانات، محرك قواعد يبني القيد من الحدث المحاسبي
القياسي، مصادر يومية أربعة (حدث/يدوي/متكرر-عكسي/توزيع) بدورة حياة موحدة (بناء → تحقق آلي →
ترحيل مباشر، لا اعتماد لكل قيد)، تصحيح بالعكس فقط، دورة حياة فترة محاسبية داخلية بالكامل
مع بوابة اعتماد واحدة عند الإغلاق، تقارير مالية مُشتقة حيًا مع تتبع نازل، تسجيل FIN في SEC
وMDL كبيانات.

**In scope (v2 — CS-FIN-001):** بناء قيد اليومية وترحيله من حدث محاسبي قياسي يصدره النظام القديم (Oracle)
كي يُوقَف الحساب في النظام القديم، في أربعة محاور لا يعيد أيٌّ منها فتح ثابت من ثوابت v1:
(1) اشتقاق الحساب بالربط — مخزن روابط حسابات تملكه FIN وتديره شاشة FIN كبيانات، يحوّل قيمة تجارية يحملها
الحدث (طريقة الدفع، رمز نوع المصروف) إلى حساب، مع حذف نمط DIRECT نهائيًا؛ (2) وسوم الأبعاد على سطور القاعدة —
قيمة كل بُعد إما ثابتة في القاعدة وإما مأخوذة من حقل تجاري مسمّى في الحدث، تخضع لتحقق الأبعاد القائم؛
(3) جواب الحدث المكرر دائمًا بخطأ الحدث المكرر؛ (4) رمز مميز موثَّق لرفض الفترة غير المفتوحة.

**Out of scope:** تعدد العملات، تعدد الدفاتر/الكيانات والقيود البينية، الحسابات الإحصائية،
التقويم متعدد الأنماط، المرفقات — استبعاد صريح [business-policies-fin.md → SCOPE
EXCEPTIONS]؛ الوحدة التجارية، مستهلك الأحداث، طبقة النقل — خارج النطاق تمامًا
[general-accounting-system-plan-en.md §15]. v2 يضيف: الطابور داخل قاعدة البيانات وخدمة المستهلك وكل ما داخل
Oracle القديم؛ تعبئة الروابط الفعلية (بيانات ترحيل)؛ مصدر أحداث ثانٍ أو وسيط رسائل؛ مصادقة المستدعي الآلي
(دلتا SEC منفصلة)؛ ترجمة رموز المؤسسة/الفرع القديمة إلى قيم أبعاد FIN (US-FIN-033 — DEFERRED، ADR-FIN-012).

**Module function (one paragraph):** FIN دفتر أستاذ عام قابل للتوصيل بأي نظام مضيف: يستقبل
حدثًا محاسبيًا قياسيًا فقط (لا قراءة ولا كتابة مباشرة لجداول المضيف)، يبني منه قيدًا متوازنًا
عبر قاعدة مُعرَّفة كبيانات، يرحّله مباشرة بعد تحقق آلي، ويشتق كل رصيد وتقرير من القيود
المُرحَّلة فقط — لا عمود رصيد مُخزَّن في أي مكان. في v2: الحدث يصرّح بوقائع تجارية فقط، وFIN وحدها تقرر
معناها المحاسبي (الحساب والأبعاد) — لا رمز حساب ولا مفهوم محاسبي يعبر داخل حمولة الحدث [POL-FIN-021].

**Detailed description (workflow narrative, roles):** حدث محاسبي يصل → المحرك يحدد قاعدته
عبر نوع الحدث → يبني سطور القيد (اشتقاق حساب + مصدر مبلغ + اتجاه لكل سطر، مع سطر باقٍ ضامن
عند التوزيع النسبي) → تحقق آلي (توازن، أوراق نشطة قابلة للترحيل، أبعاد صالحة، فترة مفتوحة)
→ ترحيل مباشر. محاسب يُدخل قيودًا يدوية بنفس المسار. مراقب مالي (منفصل عن منشئ القيود) يعتمد
إغلاق الفترة فقط — نقطة التحكم البشري الوحيدة. كل التقارير تُشتق حيًا من القيود المُرحَّلة.
إضافة v2 داخل خطوة بناء السطور: لسطر اشتقاقه MAPPING يقرأ المحرك قيمة الحقل التجاري المسمّى من الحدث ويبحث عن
الربط النشط لـ (نوع الحدث، الحقل التجاري، القيمة) فيأخذ حسابه؛ ولكل وسم بُعد على سطر القاعدة يضبط المحرك قيمة
البُعد من الثابت أو من الحقل التجاري؛ ثم يمر كل ذلك بالتحقق الآلي نفسه دون تجاوز. غياب الربط أو الحقل أو قيمة
البُعد يرفض الحدث كله ولا يُرحَّل أي قيد — لا قيمة افتراضية ولا تخمين. مسؤول المالية يدير الروابط عبر شاشة
روابط الحسابات. مشغّل تكامل الأحداث يقرأ رموز الرفض: المكرر = نجاح، الفترة غير المفتوحة = انتظار وإعادة
محاولة، وما سواهما = فشل يحتاج إصلاحًا.

**Current situation:** v1: لا نظام محاسبي سابق ضمن هذه الدفعة؛ FIN أول وحدة أعمال حقيقية تُبنى،
تستهلك SEC وMDL المكتملتين بالفعل. v2: النظام القديم Oracle ما زال يرحّل محاسبته بنفسه؛ يختار حساب النقد
ويستبدله بحساب البنك حين يكون الدفع بشيك (11 من 16 فرعًا حيًا)، ويختار حساب المصروف حسب نوع المصروف، ويختم
المؤسسة والفرع على كل قيد.

**Current difficulties:** الأنظمة القديمة المشابهة تعتمد على أعمدة رصيد مُخزَّنة تفقد تزامنها —
بالضبط ما يحله POL-FIN-009. في v2: محرك FIN v1 لا يعرف إلا CONSTANT (لا يتغير) وDIRECT (يُدخل رمز حساب في
الحمولة ويكسر الحد)، وMAPPING مُصرَّح به ويرفض التنفيذ؛ سطور القيد المبنية من الأحداث بلا أبعاد، فتتباعد
المقارنة المتوازية في كل صف وتفرغ تقارير الأبعاد؛ الحدث المكرر المتزامن يظهر خطأً عامًا من فئة 500 فيُعاد
إرساله وينتهي في طابور الرسائل الميتة.

**Proposed system and benefits:** محرك قواعد بيانات-محور يفصل منطق الحساب عن مصدر المبلغ،
دورة حياة قيد موحدة بلا اعتماد فردي يُسرّع التشغيل مع نقل الرقابة لمستوى الفترة، وأرصدة
مُشتقة دائمًا تضمن التطابق التام مع القيود الفعلية. v2: منطق اختيار الحساب القديم يصبح صفوف ربط صريحة
قابلة للتعديل دون إصدار، والقيود المبنية من الأحداث تحمل الإسناد نفسه الذي يحمله القيد اليدوي، والمستهلك يميّز
المكرر والفترة المغلقة عن الفشل الحقيقي — فيصبح التحقق في وضع الظل ذا معنى ويمكن إيقاف محاسبة النظام القديم.

**General notes (constraints, deferred items):** لا محرك سير عمل؛ لا رقم حساب داخل حمولة
الحدث؛ الالتزام بحدود idempotency عند الحد الخارجي مفترض لا مُعاد تنفيذه [§12.12]. v2: الحدث يسمّي كل واقعة
تجارية برمز من اللوكب FIN_EVENT_BUSINESS_FIELD (ADR-FIN-013)؛ ترجمة رموز الأبعاد مؤجلة (US-FIN-033)؛ تعبئة
الروابط بيانات ترحيل تُنتَج بعد تسليم المخزن؛ كل رفض جديد خطأ مميز يسمّي P3.1 رمزه، ويبقى رمزا الحدث المكرر
والفترة غير المفتوحة في v1 كما هما (ADR-FIN-027). كل مثال في هذا القسم يحمل الوقائع التجارية
داخل خريطة الحدث `fields`، مفتاحًا برمز FIN_EVENT_BUSINESS_FIELD (مثل `fields.PAYMENT_METHOD`)،
لا حقلاً مباشرًا باسم الواقعة نفسها — الناقل الذي يثبّته ADR-FIN-031 (ADR-FIN-037).

## A3 — Entities and fields

Standard fields per kind (profile.conventions.entity_defaults): master → nameAr, nameEn,
code, isActiveFl, createdBy, createdAt, updatedBy, updatedAt; transactional → docNo,
docDate, statusCode, fiscalYearId, periodId, createdBy, createdAt, updatedBy, updatedAt;
lookup → code, nameAr, nameEn, sortOrder, isActiveFl; config → key, valueAr, valueEn,
isActiveFl (adapted per entity below where the plan's own vocabulary differs, e.g. `code`
in place of a generic `key`, cited per field).

v2 changes to this section: ENT-FIN-015 and ENT-FIN-016 ADDED; ENT-FIN-010 MODIFIED (re-emitted below).
ENT-FIN-006 JournalLineDimension is unchanged in structure. In v2 the rules engine also writes it, for
event-built lines, in exactly the form a manual line uses (REQ-FIN-070). Its v1 record stands.

Adaptation of the master `entity_defaults` (ENT-FIN-007, ENT-FIN-008 — v1 debt, adapted here on the
folded state, ADR-FIN-038): FiscalYear's `code` (DBF-FIN-066) IS its label — a year is identified and
displayed by that one code, so it carries no separate `nameAr`/`nameEn`. FiscalPeriod's key is
`periodNo` together with its `fiscalYearId`, not a business `code`; its state lives in `statusCode`
(PERIOD_STATE), not in a master `isActiveFl`. Neither entity's schema changes — this records the
adaptation the profile's own defaults already allow, per entity, the way v2 records it for
ENT-FIN-015/016.

### ENT-FIN-010 — سطر القاعدة / RuleLine
| Kind | Ownership | Business number | Operations | Cross-module | Source |
|---|---|---|---|---|---|
| config | PRIVATE | No | create (with rule, with its dimension tags), read, update, delete | none | general-accounting-system-plan-en.md §6.2, §6.3; CS-FIN-001 §1, §2 |

MODIFIED in v2: `accountDerivationTypeCode` loses DIRECT (ADR-FIN-010); `accountDerivationValue` narrows to
the constant account code; `accountBusinessFieldCode` ADDED (ADR-FIN-023); dimension tags hang off the
line as ENT-FIN-016 (ADR-FIN-025). The update/delete named under Operations are v1's wording; the
as-built record at SCR-REQ-FIN-003 §B4 (no line update, no line delete) is unchanged.

| Field | Logical type | Required | Values / source | Notes | Label-ar | Label-en |
|---|---|---|---|---|---|---|
| ruleLineId | number | yes (system) | — | primary key | معرّف سطر القاعدة | Rule line id |
| eventTypeRuleId | reference | yes | ENT-FIN-009 | — | قاعدة الحدث | Event type rule |
| lineNo | number | yes | — | — | رقم السطر | Line number |
| accountDerivationTypeCode | lookup | yes | lookup key `ACCOUNT_DERIVATION_TYPE` (A6) — CONSTANT / MAPPING | DIRECT removed in v2 [RULE-FIN-018, ADR-FIN-010] | نوع اشتقاق الحساب | Account derivation type |
| accountDerivationValue | text | when CONSTANT | the constant account code; empty when MAPPING [RULE-FIN-019] | v1 "event field name / mapping-set key" readings dropped (ADR-FIN-023) | قيمة الاشتقاق (رمز الحساب الثابت) | Derivation value (constant account code) |
| accountBusinessFieldCode | lookup | when MAPPING | lookup key `FIN_EVENT_BUSINESS_FIELD` (A6); empty when CONSTANT [RULE-FIN-019] | ADDED v2 — the business field whose value in the event selects the account through ENT-FIN-015 | الحقل التجاري لربط الحساب | Account mapping business field |
| amountSourceTypeCode | lookup | yes | lookup key `AMOUNT_SOURCE_TYPE` (A6) | field / percentage / remainder [§6.2(b)] | نوع مصدر المبلغ | Amount source type |
| amountSourceValue | text | no | event field name or percentage figure | required unless amountSourceTypeCode=REMAINDER | قيمة مصدر المبلغ | Amount source value |
| directionCode | lookup | yes | lookup key `DEBIT_CREDIT` (A6) | — | الاتجاه | Direction |
| distributionTypeCode | lookup | yes | lookup key `DISTRIBUTION_TYPE` (A6) | fixed / percentage / remainder [§6.3] | نوع التوزيع | Distribution type |
| isRemainderFl | flag | yes | default false | exactly one true per rule when any line is PERCENTAGE [RULE-FIN-003] | سطر الباقي | Remainder line |
| createdAt | system | yes | — | — | — | — |

### ENT-FIN-015 — ربط الحساب / AccountMapping
| Kind | Ownership | Business number | Operations | Cross-module | Source |
|---|---|---|---|---|---|
| config | PRIVATE | No — a mapping is identified by its key (event type, business field, business value); nothing outside the system quotes it [§3.3 test] | create, read (search), update (account only), deactivate | lookup codes read from MDL (as every FIN lookup field) | CS-FIN-001 §1; module-registry-fin.md v2 ENTITIES OWNED — ADDED; POL-FIN-022, POL-FIN-025 |

Config defaults adapted: `key` → the natural key (eventTypeCode, businessFieldCode, businessValue), unique
among active rows [RULE-FIN-022]; `valueAr`/`valueEn` → not stored: the bilingual label shown for a
mapping is the referenced account's `nameAr`/`nameEn` (single source, ARCH-2); `isActiveFl` kept. Audit
fields per FIN's existing conventions (CS-FIN-001 §1).

| Field | Logical type | Required | Values / source | Notes | Label-ar | Label-en |
|---|---|---|---|---|---|---|
| accountMappingPk | number | yes (system) | — | primary key | معرّف الربط | Mapping id |
| eventTypeCode | lookup | yes | lookup key `ACCOUNTING_EVENT_TYPE` (A6) | the event type the mapping serves; fixed after create (ADR-FIN-024) | نوع الحدث | Event type |
| businessFieldCode | lookup | yes | lookup key `FIN_EVENT_BUSINESS_FIELD` (A6) | the business field being mapped; fixed after create | الحقل التجاري | Business field |
| businessValue | text | yes | when businessFieldCode=PAYMENT_METHOD: an active `PAYMENT_METHOD` code [RULE-FIN-024]; otherwise the host's code stored as-is | stored as a code, never a display text (module-registry AUTO-DECISION); compared by exact code, leading and trailing blanks trimmed on save and on event read (ADR-FIN-039); fixed after create | القيمة التجارية | Business value |
| accountId | reference | yes | ENT-FIN-001; active leaf on save [RULE-FIN-023] | the only field an update changes (ADR-FIN-024) | الحساب | Account |
| isActiveFl | flag | yes | default true | deactivated, never hard-deleted [KB:erp-domain-standards §6] | نشط | Active |
| createdBy, createdAt, updatedBy, updatedAt | system | yes | — | standard audit fields | — | — |

### ENT-FIN-016 — بُعد سطر القاعدة / RuleLineDimension
| Kind | Ownership | Business number | Operations | Cross-module | Source |
|---|---|---|---|---|---|
| config | PRIVATE | No | create (with its rule line), read (with its rule line) — ADR-FIN-025 | lookup codes read from MDL | CS-FIN-001 §2; module-registry-fin.md v2 ENTITIES OWNED — ADDED; POL-FIN-026 |

Config defaults adapted: `key` → (ruleLineId, dimensionId), one tag per dimension per line [RULE-FIN-025];
`valueAr`/`valueEn` → not stored, the labels come from ENT-FIN-002 / ENT-FIN-003; `isActiveFl` → not
carried, the tag lives and retires with its rule line (ADR-FIN-025, same as ENT-FIN-010).

| Field | Logical type | Required | Values / source | Notes | Label-ar | Label-en |
|---|---|---|---|---|---|---|
| ruleLineDimensionPk | number | yes (system) | — | primary key | معرّف وسم البُعد | Dimension tag id |
| ruleLineId | reference | yes | ENT-FIN-010 | — | سطر القاعدة | Rule line |
| dimensionId | reference | yes | ENT-FIN-002 | unique per ruleLineId [RULE-FIN-025] | البُعد | Dimension |
| valueSourceCode | lookup | yes | lookup key `FIN_DIMENSION_VALUE_SOURCE` (A6) — CONSTANT / BUSINESS_FIELD | ADR-FIN-010 (dimensions keep their own source list) | مصدر القيمة | Value source |
| dimensionValueId | reference | when CONSTANT | ENT-FIN-003; must belong to dimensionId; empty when BUSINESS_FIELD [RULE-FIN-026] | — | القيمة الثابتة | Constant value |
| businessFieldCode | lookup | when BUSINESS_FIELD | lookup key `FIN_EVENT_BUSINESS_FIELD` (A6); empty when CONSTANT [RULE-FIN-026] | the event's value resolves by `DimensionValue.code` within dimensionId (ADR-FIN-026) | الحقل التجاري | Business field |
| createdAt | system | yes | — | written with the rule line | — | — |

## A4 — Functional requirements (EARS) and acceptance criteria

### REQ-FIN-045 — تسجيل قوائم FIN في MDL / Register FIN's lookup types into the Lookup module
Pattern    : event
Statement  : When FIN is deployed, the system shall register its 15 lookup types and their seeded values as data into the Lookup module, naming FIN as owner.
Traces     : US-FIN-018, US-FIN-020, US-FIN-021, US-FIN-026
Entities   : ENT-FIN-001, ENT-FIN-004, ENT-FIN-007, ENT-FIN-008, ENT-FIN-009, ENT-FIN-010, ENT-FIN-011, ENT-FIN-013, ENT-FIN-015, ENT-FIN-016
Rationale  : POL-FIN-017; MODIFIED v2 — 13 → 15 types (FIN_EVENT_BUSINESS_FIELD, FIN_DIMENSION_VALUE_SOURCE), DIRECT leaves ACCOUNT_DERIVATION_TYPE, PAYMENT_METHOD gains CHEQUE
Source     : general-accounting-system-plan-en.md §5.2-§5.3; module-registry-fin.md v2 LOOKUPS OWNED; ADR-FIN-010, ADR-FIN-013
Priority   : HIGH
#### AC-FIN-045 — [REQ-FIN-045]
Given  : MDL is gated and reachable, and FIN v2 is deployed
When   : FIN's onboarding and its v2 migration have run
Then   : MDL records 15 LookupType rows owned by FIN; ACCOUNT_DERIVATION_TYPE holds exactly 2 active values, CONSTANT and MAPPING; FIN_EVENT_BUSINESS_FIELD holds 4 values; FIN_DIMENSION_VALUE_SOURCE holds 2 values; PAYMENT_METHOD holds the value CHEQUE

### REQ-FIN-047 — لا رمز حساب من حمولة الحدث / No account code from the event payload
Pattern    : ubiquitous
Statement  : The system shall take the account of every journal line it builds from a canonical accounting event from the rule line's constant account code or from an account mapping, never from an account code carried in the event payload.
Traces     : US-FIN-020
Entities   : ENT-FIN-005, ENT-FIN-010, ENT-FIN-015
Rationale  : POL-FIN-021 — the event states a business fact; FIN alone decides its accounting meaning
Source     : CS-FIN-001 Summary, §1, acceptance 1; domain-profile G6; ADR-FIN-010
Priority   : HIGH
#### AC-FIN-047 — [REQ-FIN-047]
Given  : an active rule for event type "PAYMENT_RECEIVED" whose debit line is MAPPING on PAYMENT_METHOD, an active mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102, and an event "EVT-3001" that carries fields.PAYMENT_METHOD = "CHEQUE" plus an extra payload field accountCode "1101"
When   : the event arrives
Then   : the system posts the entry with its debit line on account 1102, and no line of the entry carries account 1101

### REQ-FIN-048 — رفض نوع اشتقاق غير مدعوم / Reject an unsupported account derivation type
Pattern    : unwanted
Statement  : If a finance administrator adds a rule line whose account derivation type is not an active ACCOUNT_DERIVATION_TYPE value, then the system shall reject the rule line.
Traces     : US-FIN-020
Entities   : ENT-FIN-010
Rationale  : RULE-FIN-018; DIRECT is removed outright (ADR-FIN-010) — no path left for an account code to enter through the payload
Source     : CS-FIN-001 §1 "DIRECT … breaks the boundary"; POL-FIN-021
Priority   : HIGH
#### AC-FIN-048 — [REQ-FIN-048]
Given  : ACCOUNT_DERIVATION_TYPE holds CONSTANT and MAPPING only
When   : a finance administrator adds a rule line with accountDerivationTypeCode=DIRECT
Then   : the system rejects the line with ar: "نوع اشتقاق الحساب غير مدعوم؛ المسموح قيمة ثابتة أو ربط" · en: "Unsupported account derivation type; only constant or mapping is allowed", and no RuleLine row is created

### REQ-FIN-049 — سطر قاعدة يشتق حسابه بالربط / A rule line that derives its account by mapping
Pattern    : event
Statement  : When a finance administrator adds a rule line whose account derivation type is MAPPING, the system shall record the business field whose value in the event selects the line's account.
Traces     : US-FIN-021, US-FIN-022
Entities   : ENT-FIN-010
Rationale  : RULE-FIN-019; POL-FIN-022 — the line names the fact, the mapping store names the account
Source     : CS-FIN-001 §1; ADR-FIN-023
Priority   : HIGH
#### AC-FIN-049 — [REQ-FIN-049]
Given  : an active event-type rule for "EXPENSE_PAID"
When   : a finance administrator adds a line with accountDerivationTypeCode=MAPPING, accountBusinessFieldCode=EXPENSE_TYPE_CODE and no accountDerivationValue
Then   : the system creates the RuleLine with accountBusinessFieldCode=EXPENSE_TYPE_CODE

### REQ-FIN-050 — اشتقاق الحساب من الربط عند البناء / Resolve the account through the mapping at build time
Pattern    : event
Statement  : When the system builds a journal line from a rule line whose account derivation type is MAPPING, the system shall take the line's account from the active account mapping that matches the rule's event type, the rule line's business field and that field's value in the event.
Traces     : US-FIN-020, US-FIN-021, US-FIN-022
Entities   : ENT-FIN-005, ENT-FIN-009, ENT-FIN-010, ENT-FIN-015
Rationale  : RULE-FIN-020; POL-FIN-022 — cash versus bank by payment method, expense account by expense type
Source     : CS-FIN-001 §1, acceptance 1
Priority   : HIGH
#### AC-FIN-050 — [REQ-FIN-050]
Given  : active mappings (PAYMENT_RECEIVED, PAYMENT_METHOD, "CASH") → account 1101 (cash) and (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102 (bank), and a rule for PAYMENT_RECEIVED whose debit line is MAPPING on PAYMENT_METHOD
When   : event "EVT-3002" of type PAYMENT_RECEIVED arrives with fields.PAYMENT_METHOD = "CHEQUE" and amount 500.00
Then   : the system posts an entry whose debit line is 500.00 on account 1102
#### AC-FIN-051 — [REQ-FIN-050]
Given  : the same mappings and rule as AC-FIN-050
When   : event "EVT-3003" of type PAYMENT_RECEIVED arrives with fields.PAYMENT_METHOD = "CASH" and amount 500.00
Then   : the system posts an entry whose debit line is 500.00 on account 1101
#### AC-FIN-052 — [REQ-FIN-050]
Given  : active mappings (EXPENSE_PAID, EXPENSE_TYPE_CODE, "TRAVEL") → account 5210 and (EXPENSE_PAID, EXPENSE_TYPE_CODE, "RENT") → account 5220, and a rule for EXPENSE_PAID whose debit line is MAPPING on EXPENSE_TYPE_CODE
When   : event "EVT-3004" of type EXPENSE_PAID arrives with fields.EXPENSE_TYPE_CODE = "RENT" and amount 1200.00
Then   : the system posts an entry whose debit line is 1200.00 on account 5220

### REQ-FIN-051 — رفض حدث بلا الحقل التجاري المطلوب للحساب / Reject an event missing the business field its account needs
Pattern    : unwanted
Statement  : If a canonical accounting event does not carry the business field that a MAPPING rule line names, then the system shall reject the event and post no journal entry.
Traces     : US-FIN-021, US-FIN-022, US-FIN-024
Entities   : ENT-FIN-004, ENT-FIN-010
Rationale  : RULE-FIN-021; never a default, never a guess — a missing fact is not "cash"
Source     : CS-FIN-001 §1 "never a default, never a guess"; POL-FIN-024; ADR-FIN-027
Priority   : HIGH
#### AC-FIN-053 — [REQ-FIN-051]
Given  : the PAYMENT_RECEIVED rule of AC-FIN-050
When   : event "EVT-3005" of type PAYMENT_RECEIVED arrives with no fields.PAYMENT_METHOD key
Then   : the system rejects the event with the missing-business-field error, ar: "الحدث لا يحمل الحقل التجاري الذي تطلبه القاعدة" · en: "The event does not carry the business field the rule requires", and no JournalEntry exists with eventReference "EVT-3005"

### REQ-FIN-052 — رفض حساب مربوط غير مؤهَّل للترحيل / Reject a mapped account that cannot be posted to
Pattern    : unwanted
Statement  : If an account taken from an account mapping is not a leaf, not active, or not marked as accepting direct posting, then the system shall reject the event and post no journal entry.
Traces     : US-FIN-023
Entities   : ENT-FIN-001, ENT-FIN-005, ENT-FIN-015
Rationale  : RULE-FIN-007 applied unchanged to the mapped account; POL-FIN-023 — the new path opens no gap in the chart's integrity
Source     : CS-FIN-001 §1 "validates the resulting account exactly as it validates a CONSTANT one — leaf, active, postable"
Priority   : HIGH
#### AC-FIN-054 — [REQ-FIN-052]
Given  : mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102, and account 1102 has since received a child account (isLeafFl=false)
When   : event "EVT-3006" of type PAYMENT_RECEIVED arrives with fields.PAYMENT_METHOD = "CHEQUE"
Then   : the system rejects the event with ar: "الحساب المستهدف لا يقبل ترحيلاً مباشرًا" · en: "The target account does not accept direct posting", and no JournalEntry exists with eventReference "EVT-3006"
#### AC-FIN-055 — [REQ-FIN-052]
Given  : mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102, and account 1102 has since been deactivated (isActiveFl=false)
When   : event "EVT-3007" of type PAYMENT_RECEIVED arrives with fields.PAYMENT_METHOD = "CHEQUE"
Then   : the system rejects the event with the same message as AC-FIN-054, and no JournalEntry exists with eventReference "EVT-3007"

### REQ-FIN-053 — رفض القيمة التجارية غير المربوطة / Reject an unmapped business value
Pattern    : unwanted
Statement  : If the business value in a canonical accounting event has no active account mapping for the rule's event type and the rule line's business field, then the system shall reject the event with the dedicated unmapped-value error and post no journal entry.
Traces     : US-FIN-024
Entities   : ENT-FIN-004, ENT-FIN-010, ENT-FIN-015
Rationale  : RULE-FIN-020; POL-FIN-024 — never a default, never a guess; the missing mapping is found and completed rather than an entry posting to a wrong account
Source     : CS-FIN-001 §1, acceptance 2; ADR-FIN-027
Priority   : HIGH
#### AC-FIN-056 — [REQ-FIN-053]
Given  : the PAYMENT_RECEIVED rule and mappings of AC-FIN-050, and no mapping for the PAYMENT_METHOD value "TRANSFER"
When   : event "EVT-3008" of type PAYMENT_RECEIVED arrives with fields.PAYMENT_METHOD = "TRANSFER"
Then   : the system rejects the event with the unmapped-value error, ar: "لا يوجد ربط حساب نشط للقيمة التجارية في الحدث" · en: "No active account mapping exists for the business value in the event", and no JournalEntry exists with eventReference "EVT-3008"
#### AC-FIN-057 — [REQ-FIN-053]
Given  : mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CASH") → account 1101 with isActiveFl=false, and no other mapping for "CASH"
When   : event "EVT-3009" of type PAYMENT_RECEIVED arrives with fields.PAYMENT_METHOD = "CASH"
Then   : the system rejects the event with the unmapped-value error of AC-FIN-056, and no JournalEntry exists with eventReference "EVT-3009"

### REQ-FIN-054 — إنشاء ربط حساب / Create an account mapping
Pattern    : event
Statement  : When a finance administrator creates an account mapping, the system shall record its event type, business field, business value and account as an active mapping.
Traces     : US-FIN-025
Entities   : ENT-FIN-015
Rationale  : POL-FIN-025 — mappings are FIN-owned data managed through a FIN screen, never code
Source     : CS-FIN-001 §1, acceptance 3
Priority   : HIGH
#### AC-FIN-058 — [REQ-FIN-054]
Given  : no active mapping for (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE"), and account 1102 is an active leaf
When   : a finance administrator submits the mapping form with those three values and account 1102
Then   : the system creates an AccountMapping with isActiveFl=true and accountId = account 1102

### REQ-FIN-055 — بحث روابط الحسابات / Search account mappings
Pattern    : event
Statement  : When a finance administrator searches account mappings, the system shall list the mappings that match the given event type, business field, business value, account and active-flag filters.
Traces     : US-FIN-025
Entities   : ENT-FIN-015
Rationale  : the administrator finds the mapping to change or deactivate, and checks coverage before the migration data is loaded
Source     : CS-FIN-001 §1; [KB:erp-domain-standards §6] server-side search and paging
Priority   : HIGH
#### AC-FIN-059 — [REQ-FIN-055]
Given  : 3 mappings for PAYMENT_RECEIVED (CASH active, CHEQUE active, TRANSFER inactive) and 2 for EXPENSE_PAID
When   : the administrator searches with eventTypeCode=PAYMENT_RECEIVED and isActiveFl=true
Then   : the system returns exactly 2 rows, CASH and CHEQUE

### REQ-FIN-056 — تغيير حساب الربط / Change a mapping's account
Pattern    : event
Statement  : When a finance administrator changes the account of an account mapping, the system shall record the new account on that mapping.
Traces     : US-FIN-025
Entities   : ENT-FIN-015
Rationale  : POL-FIN-025 — logic evolves without a release; the key stays fixed (ADR-FIN-024)
Source     : CS-FIN-001 §1, acceptance 3
Priority   : HIGH
#### AC-FIN-060 — [REQ-FIN-056]
Given  : mapping (EXPENSE_PAID, EXPENSE_TYPE_CODE, "RENT") → account 5220, and account 5225 is an active leaf
When   : the administrator changes its account to 5225
Then   : the system updates the mapping to accountId = account 5225 and leaves its eventTypeCode, businessFieldCode and businessValue unchanged

### REQ-FIN-057 — إيقاف ربط حساب / Deactivate an account mapping
Pattern    : event
Statement  : When a finance administrator deactivates an account mapping, the system shall exclude it from every later account resolution.
Traces     : US-FIN-025
Entities   : ENT-FIN-015
Rationale  : soft delete [KB:erp-domain-standards §6]; a deactivated mapping resolves nothing, so an event needing it is rejected by REQ-FIN-053
Source     : CS-FIN-001 §1 "an active flag"; POL-FIN-025
Priority   : HIGH
#### AC-FIN-061 — [REQ-FIN-057]
Given  : an active mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CASH") → account 1101
When   : the administrator deactivates it
Then   : the system sets isActiveFl=false on the mapping, and the next PAYMENT_RECEIVED event with fields.PAYMENT_METHOD = "CASH" is rejected as in AC-FIN-057

### REQ-FIN-058 — تعديل الربط لا يمس القيود المُرحَّلة / A mapping change never alters a posted entry
Pattern    : event
Statement  : When a finance administrator changes or deactivates an account mapping, the system shall leave the account of every already-posted journal line unchanged.
Traces     : US-FIN-025
Entities   : ENT-FIN-005, ENT-FIN-015
Rationale  : POL-FIN-013 (immutable posted entries) and POL-FIN-009 (derived balances) hold on the new path; a mapping governs later derivations only (module-registry AUTO-DECISION, ADR-FIN-021)
Source     : CS-FIN-001 acceptance 7; US-FIN-025
Priority   : HIGH
#### AC-FIN-062 — [REQ-FIN-058]
Given  : entry "JV-2026-000500", posted from event "EVT-3010" with its debit line on account 5220 through mapping (EXPENSE_PAID, EXPENSE_TYPE_CODE, "RENT")
When   : the administrator changes that mapping's account to 5225
Then   : the debit line of "JV-2026-000500" still shows account 5220, and the account ledger of 5220 still contains that line

### REQ-FIN-059 — رفض ربط نشط مكرر / Reject a duplicate active mapping
Pattern    : unwanted
Statement  : If a finance administrator saves an account mapping whose event type, business field and business value already have an active mapping, then the system shall reject it.
Traces     : US-FIN-025
Entities   : ENT-FIN-015
Rationale  : RULE-FIN-022 — two candidate accounts for one fact would make the engine guess (module-registry AUTO-DECISION)
Source     : CS-FIN-001 §1 "never a guess"
Priority   : HIGH
#### AC-FIN-063 — [REQ-FIN-059]
Given  : an active mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102
When   : the administrator creates a second mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1103
Then   : the system rejects it with ar: "يوجد بالفعل ربط نشط لنوع الحدث والحقل التجاري والقيمة نفسها" · en: "An active mapping already exists for this event type, business field and value", and the count of active mappings for that key stays 1

### REQ-FIN-060 — رفض ربط بحساب غير ورقي أو غير نشط / Reject a mapping to a non-leaf or inactive account
Pattern    : unwanted
Statement  : If a finance administrator saves an account mapping whose account is not an active leaf account, then the system shall reject it.
Traces     : US-FIN-025, US-FIN-023
Entities   : ENT-FIN-001, ENT-FIN-015
Rationale  : RULE-FIN-023 — the gap is caught at configuration, not first discovered as a failed event; the post-time check (REQ-FIN-052) still stands
Source     : POL-FIN-023; ADR-FIN-024
Priority   : MEDIUM
#### AC-FIN-064 — [REQ-FIN-060]
Given  : account 1100 is a parent (rollup) account with isLeafFl=false
When   : the administrator saves a mapping whose account is 1100
Then   : the system rejects it with ar: "الحساب المربوط يجب أن يكون حسابًا ورقيًا نشطًا" · en: "The mapped account must be an active leaf account", and no AccountMapping row is written

### REQ-FIN-061 — رفض قيمة طريقة دفع غير معرَّفة / Reject an undefined payment-method value
Pattern    : unwanted
Statement  : If a finance administrator saves an account mapping for the PAYMENT_METHOD business field whose business value is not an active PAYMENT_METHOD code, then the system shall reject it.
Traces     : US-FIN-025, US-FIN-021
Entities   : ENT-FIN-015
Rationale  : RULE-FIN-024 — a misspelt payment method would never match an event and would silently leave the method unmapped
Source     : module-registry-fin.md v2 AUTO-DECISION (business value stored as a code; PAYMENT_METHOD → its lookup)
Priority   : MEDIUM
#### AC-FIN-065 — [REQ-FIN-061]
Given  : PAYMENT_METHOD holds CASH and CHEQUE as active values
When   : the administrator saves a mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQEU")
Then   : the system rejects it with ar: "القيمة ليست طريقة دفع معرَّفة" · en: "The value is not a defined payment method", and no AccountMapping row is written

### REQ-FIN-062 — وسوم الأبعاد على سطر القاعدة / Dimension tags on a rule line
Pattern    : event
Statement  : When a finance administrator adds a rule line with dimension tags, the system shall record for each tag its dimension, its value source and either its constant dimension value or its business field.
Traces     : US-FIN-026
Entities   : ENT-FIN-010, ENT-FIN-016
Rationale  : POL-FIN-026 — the same discipline as accounts: constant or named business field; RULE-FIN-025, RULE-FIN-026
Source     : CS-FIN-001 §2; ADR-FIN-025
Priority   : HIGH
#### AC-FIN-066 — [REQ-FIN-062]
Given  : dimensions ORGANISATION (value "ORG-1") and BRANCH, both active
When   : a finance administrator adds a line with tags {ORGANISATION: CONSTANT "ORG-1"} and {BRANCH: BUSINESS_FIELD BRANCH_CODE}
Then   : the system creates the RuleLine and 2 RuleLineDimension rows, one per tag, with those sources

### REQ-FIN-063 — ضبط الأبعاد على سطر القيد المبني من الحدث / Set the dimensions on an event-built journal line
Pattern    : optional
Statement  : Where a rule line carries a dimension tag, the system shall set that dimension on the journal line built from the rule line, with the tag's constant dimension value or with the dimension value whose code equals the named business field's value in the event.
Traces     : US-FIN-026
Entities   : ENT-FIN-003, ENT-FIN-005, ENT-FIN-006, ENT-FIN-016
Rationale  : POL-FIN-026 — organisation and branch attribution on every event-built line makes the parallel-run comparison meaningful; RULE-FIN-027; ADR-FIN-026
Source     : CS-FIN-001 §2, acceptance 4
Priority   : HIGH
#### AC-FIN-067 — [REQ-FIN-063]
Given  : the rule line of AC-FIN-066 on the PAYMENT_RECEIVED rule
When   : event "EVT-3011" arrives with fields.BRANCH_CODE = "BR-07"
Then   : the posted journal line built from that rule line carries ORGANISATION = "ORG-1"
#### AC-FIN-068 — [REQ-FIN-063]
Given  : the rule line of AC-FIN-066, and BRANCH holds an active value with code "BR-07"
When   : event "EVT-3011" arrives with fields.BRANCH_CODE = "BR-07"
Then   : the posted journal line built from that rule line carries BRANCH = the DimensionValue whose code is "BR-07"

### REQ-FIN-064 — رفض وسم البُعد نفسه مرتين / Reject two tags for the same dimension
Pattern    : unwanted
Statement  : If a rule line carries two dimension tags for the same dimension, then the system shall reject the rule line.
Traces     : US-FIN-026
Entities   : ENT-FIN-016
Rationale  : RULE-FIN-025 — a journal line holds one value per dimension (ENT-FIN-006, v1 shape)
Source     : CS-FIN-001 §2 "per dimension the value is either a constant or …"
Priority   : MEDIUM
#### AC-FIN-069 — [REQ-FIN-064]
Given  : an event-type rule
When   : the administrator adds a line tagged {BRANCH: CONSTANT "BR-01"} and {BRANCH: BUSINESS_FIELD BRANCH_CODE}
Then   : the system rejects the line with ar: "لا يجوز وسم البُعد نفسه مرتين على سطر القاعدة" · en: "A rule line cannot tag the same dimension twice", and no RuleLine row is created

### REQ-FIN-065 — رفض وسم لا يطابق مصدره / Reject a tag inconsistent with its source
Pattern    : unwanted
Statement  : If a dimension tag does not carry exactly the value its source requires, then the system shall reject the rule line.
Traces     : US-FIN-026
Entities   : ENT-FIN-016
Rationale  : RULE-FIN-026 — a CONSTANT tag needs a value of its own dimension, a BUSINESS_FIELD tag needs a business field, never both
Source     : CS-FIN-001 §2; ADR-FIN-010
Priority   : MEDIUM
#### AC-FIN-070 — [REQ-FIN-065]
Given  : dimensions BRANCH and ORGANISATION, and "ORG-1" is a value of ORGANISATION
When   : the administrator adds a line tagged {BRANCH: CONSTANT "ORG-1"}
Then   : the system rejects the line with ar: "وسم البُعد لا يطابق مصدر قيمته" · en: "The dimension tag does not match its value source", and no RuleLine row is created

### REQ-FIN-066 — تحقق قيمة البُعد المشتقة من الحدث / Validate an event-derived dimension value
Pattern    : event
Statement  : When the system resolves a dimension value for a journal line built from an event, the system shall validate that the value belongs to the tagged dimension and is active, with the same check it applies to a manually entered line.
Traces     : US-FIN-027
Entities   : ENT-FIN-003, ENT-FIN-006, ENT-FIN-016
Rationale  : RULE-FIN-009 reused unchanged; POL-FIN-027 — the existing dimension validation is not bypassed
Source     : CS-FIN-001 §2 "the existing validation is not bypassed"
Priority   : HIGH
#### AC-FIN-071 — [REQ-FIN-066]
Given  : the rule line of AC-FIN-066, and "ORG-1" and "BR-07" both active
When   : event "EVT-3012" arrives with fields.BRANCH_CODE = "BR-07"
Then   : the system validates both resolved values and posts the entry with 2 JournalLineDimension rows on that line

### REQ-FIN-067 — رفض قيمة بُعد غير نشطة من الحدث / Reject an inactive event-derived dimension value
Pattern    : unwanted
Statement  : If a dimension value resolved for an event-built journal line is inactive, then the system shall reject the event and post no journal entry.
Traces     : US-FIN-027, US-FIN-028
Entities   : ENT-FIN-003, ENT-FIN-006
Rationale  : RULE-FIN-009; POL-FIN-028 — an invalid value rejects the event, as a wrong account does
Source     : CS-FIN-001 §2; resolved decision v2-2 (ADR-FIN-011)
Priority   : HIGH
#### AC-FIN-072 — [REQ-FIN-067]
Given  : the rule line of AC-FIN-066, and BRANCH value "BR-09" is inactive
When   : event "EVT-3013" arrives with fields.BRANCH_CODE = "BR-09"
Then   : the system rejects the event with ar: "قيمة البُعد غير صالحة" · en: "The dimension value is invalid", and no JournalEntry exists with eventReference "EVT-3013"

### REQ-FIN-068 — رفض حدث بلا الحقل التجاري المطلوب للبُعد / Reject an event missing a tag's business field
Pattern    : unwanted
Statement  : If a canonical accounting event does not carry the business field that a dimension tag names, then the system shall reject the event and post no journal entry.
Traces     : US-FIN-028
Entities   : ENT-FIN-004, ENT-FIN-016
Rationale  : RULE-FIN-021; POL-FIN-028 — an entry without organisation or branch attribution corrupts the parallel-run comparison
Source     : CS-FIN-001 §1 "never a default", §2; ADR-FIN-011
Priority   : HIGH
#### AC-FIN-073 — [REQ-FIN-068]
Given  : the rule line of AC-FIN-066
When   : event "EVT-3014" arrives with no fields.BRANCH_CODE key
Then   : the system rejects the event with ar: "الحدث لا يحمل الحقل التجاري الذي تطلبه القاعدة" · en: "The event does not carry the business field the rule requires", and no JournalEntry exists with eventReference "EVT-3014"

### REQ-FIN-069 — رفض قيمة بُعد غير معرَّفة من الحدث / Reject an event dimension value that matches no defined value
Pattern    : unwanted
Statement  : If the value of a dimension tag's business field in the event matches no dimension value code of the tagged dimension, then the system shall reject the event and post no journal entry.
Traces     : US-FIN-028
Entities   : ENT-FIN-003, ENT-FIN-016
Rationale  : RULE-FIN-027; no translation, no guess (ADR-FIN-012, ADR-FIN-026)
Source     : CS-FIN-001 §2; POL-FIN-028
Priority   : HIGH
#### AC-FIN-074 — [REQ-FIN-069]
Given  : the rule line of AC-FIN-066, and BRANCH holds no value with code "BR-99"
When   : event "EVT-3015" arrives with fields.BRANCH_CODE = "BR-99"
Then   : the system rejects the event with ar: "قيمة البُعد في الحدث لا تطابق أي قيمة معرَّفة لهذا البُعد" · en: "The dimension value in the event matches no defined value of this dimension", and no JournalEntry exists with eventReference "EVT-3015"

### REQ-FIN-070 — شكل أبعاد واحد لكل السطور / One dimension shape for every line
Pattern    : ubiquitous
Statement  : The system shall record each dimension value of an event-built journal line as a journal line dimension of that line, in the same form as for a manually entered journal line.
Traces     : US-FIN-029
Entities   : ENT-FIN-005, ENT-FIN-006
Rationale  : POL-FIN-029 — ledger and dimension reports read one shape, whatever the source
Source     : CS-FIN-001 §2 "the same dimension shape"
Priority   : HIGH
#### AC-FIN-075 — [REQ-FIN-070]
Given  : entry A posted from event "EVT-3016" with BRANCH "BR-07" on its debit line, and manual entry B posted with BRANCH "BR-07" on its debit line
When   : both entries are read
Then   : each debit line holds exactly 1 JournalLineDimension row with dimension BRANCH and the same dimensionValueId

### REQ-FIN-071 — تقارير واحدة لسطور الأحداث واليدوية / One reporting treatment for event and manual lines
Pattern    : event
Statement  : When a financial controller opens the account ledger or a dimension report, the system shall group and filter event-built and manually entered journal lines by the same account-and-dimension combination regardless of the entry's journal type.
Traces     : US-FIN-029
Entities   : ENT-FIN-005, ENT-FIN-006
Rationale  : POL-FIN-029, POL-FIN-011 — dimension reports stop being empty for the entries that matter most; REQ-FIN-039 and REQ-FIN-043 unchanged, this states that they cover event lines
Source     : CS-FIN-001 §2 "ledger and dimension reports treat them identically"
Priority   : HIGH
#### AC-FIN-076 — [REQ-FIN-071]
Given  : the two entries of AC-FIN-075, each debiting account 5100 with 100.00 under BRANCH "BR-07"
When   : the controller opens the dimension report by BRANCH
Then   : the system shows 1 row for (5100, "BR-07") with a debit total of 200.00

### REQ-FIN-072 — الحدث المكرر يُجاب دائمًا بأنه مكرر / A duplicate event is always answered as a duplicate
Pattern    : unwanted
Statement  : If a canonical accounting event arrives with an event reference that is already recorded, then the system shall answer the duplicate-event error whether the pre-check or the store's uniqueness guarantee detects the repeat.
Traces     : US-FIN-030
Entities   : ENT-FIN-004
Rationale  : RULE-FIN-004 (MODIFIED v2); POL-FIN-030 — for the consumer a duplicate is success; a generic server error causes retries, a dead-letter queue and manual reconciliation
Source     : CS-FIN-001 §3, acceptance 5; ADR-FIN-027
Priority   : —
#### AC-FIN-077 — [REQ-FIN-072]
Given  : a POSTED entry with eventReference "EVT-4001"
When   : another event with reference "EVT-4001" arrives
Then   : the system answers the duplicate-event error with ar: "تم بالفعل ترحيل قيد لهذا المرجع" · en: "An entry for this event reference has already been posted", and exactly 1 JournalEntry exists with eventReference "EVT-4001"
#### AC-FIN-078 — [REQ-FIN-072]
Given  : no entry with eventReference "EVT-4002"
When   : two events with reference "EVT-4002" arrive at the same time, so both pass the pre-check and the store's uniqueness guarantee refuses the second
Then   : the system posts exactly 1 entry for "EVT-4002" and answers the other with the duplicate-event error of AC-FIN-077, never a generic server error

### REQ-FIN-073 — رمز مميز لرفض الفترة غير المفتوحة / A distinct code for a non-open period
Pattern    : unwanted
Statement  : If a canonical accounting event's posting targets an existing accounting period that is not Open, then the system shall reject it with the period-not-open error, distinct from every validation and configuration error.
Traces     : US-FIN-031
Entities   : ENT-FIN-004, ENT-FIN-008
Rationale  : RULE-FIN-008 (unchanged); POL-FIN-031 — an operational state that clears when the period is opened, so the consumer waits and retries; confirmed as the existing v1 code (ADR-FIN-027, ADR-FIN-014)
Source     : CS-FIN-001 §4, acceptance 6
Priority   : —
#### AC-FIN-079 — [REQ-FIN-073]
Given  : the period covering 2026-10-05 is SOFT_CLOSE
When   : event "EVT-4003" dated 2026-10-05 arrives
Then   : the system rejects it with the period-not-open error, ar: "الفترة المستهدفة غير مفتوحة" · en: "The target period is not open", whose code differs from every validation and configuration error code, and no JournalEntry exists with eventReference "EVT-4003"

### REQ-FIN-074 — تاريخ بلا فترة معرَّفة خطأ إعداد / A date with no defined period is a configuration error
Pattern    : unwanted
Statement  : If a canonical accounting event's date falls within no defined accounting period, then the system shall reject the event with a configuration error distinct from the period-not-open error.
Traces     : US-FIN-031
Entities   : ENT-FIN-004, ENT-FIN-008
Rationale  : RULE-FIN-028; a calendar setup gap is not cleared by opening a period, so it is never the retry signal (ADR-FIN-014)
Source     : CS-FIN-001 §4 "distinct from validation and configuration errors"; resolved decision v2-5
Priority   : —
#### AC-FIN-080 — [REQ-FIN-074]
Given  : no FiscalPeriod whose startDate…endDate covers 2027-01-15
When   : event "EVT-4004" dated 2027-01-15 arrives
Then   : the system rejects it with ar: "لا توجد فترة محاسبية معرَّفة تغطي تاريخ الحدث" · en: "No accounting period is defined for the event date", with a code different from the period-not-open code, and no JournalEntry exists with eventReference "EVT-4004"

### REQ-FIN-075 — توثيق رمز الفترة غير المفتوحة للمستهلك / Document the period-not-open code for the consumer
Pattern    : ubiquitous
Statement  : The system shall list the period-not-open error code in FIN's published error catalogue as the rejection that clears once the target period is opened.
Traces     : US-FIN-031
Entities   : ENT-FIN-008
Rationale  : POL-FIN-031 "catalogued and documented" — the consumer depends on the code, so it is published, not inferred
Source     : CS-FIN-001 §4 "the code is documented for the consumer to depend on"
Priority   : —
#### AC-FIN-081 — [REQ-FIN-075]
Given  : FIN's published API documentation
When   : the consumer's integrator reads the error catalogue
Then   : the catalogue lists the period-not-open code exactly once, with its ar/en message and the note that the rejection clears when the period is opened

### REQ-FIN-076 — ثوابت v1 على المسار الجديد عند الترحيل / v1 posting checks hold on the new path
Pattern    : unwanted
Statement  : If a journal entry built through account mapping or dimension tags fails any v1 posting check, then the system shall reject posting it exactly as it rejects an entry from any other source.
Traces     : US-FIN-032
Entities   : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006
Rationale  : POL-FIN-032 — RULE-FIN-006 (balance), RULE-FIN-007 (leaf and active), RULE-FIN-008 (period gate at post), RULE-FIN-009 (dimension validity), RULE-FIN-003 and RULE-FIN-010 (single remainder line, positive amounts with direction), RULE-FIN-004 (duplicate) apply unchanged; inherited, not reopened
Source     : CS-FIN-001 acceptance 7
Priority   : —
#### AC-FIN-082 — [REQ-FIN-076]
Given  : a rule distributing the event amount as 33% + 33% + remainder, whose remainder line is MAPPING on PAYMENT_METHOD, and an active mapping for "CHEQUE"
When   : event "EVT-4005" arrives with amount 100.00 and fields.PAYMENT_METHOD = "CHEQUE"
Then   : the two percentage lines total 66.00, the remainder line is exactly 34.00 on the mapped account, and the posted entry's debits equal its credits at 100.00

### REQ-FIN-077 — القيد المُرحَّل عبر المسار الجديد كغيره / A posted entry from the new path behaves as any other
Pattern    : ubiquitous
Statement  : The system shall apply the v1 post-posting invariants of lock, reversal-only correction and derived balances to a posted journal entry built through account mapping or dimension tags.
Traces     : US-FIN-032
Entities   : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006
Rationale  : POL-FIN-032 with POL-FIN-007, POL-FIN-009, POL-FIN-013 — RULE-FIN-016 and RULE-FIN-011 apply unchanged
Source     : CS-FIN-001 acceptance 7
Priority   : —
#### AC-FIN-083 — [REQ-FIN-077]
Given  : entry "JV-2026-000600" posted from event "EVT-4006" with a mapped debit account and BRANCH "BR-07" on its lines
When   : an accountant reverses it
Then   : the system posts a reversal whose lines carry the same accounts, amounts and dimension values with opposite direction, the original stays POSTED, and every account and BRANCH balance nets to 0.00

## A5 — Business rules

### RULE-FIN-004 — رفض مرجع حدث مكرر / Reject a duplicate event reference
Scope      : ENT-FIN-004
Trigger    : on create (event-sourced entry) — by the pre-check before building, and again when the store's uniqueness guarantee on eventReference refuses a concurrent second write
Statement  : The system shall reject building an entry for an event reference that has already produced a posted entry, and shall answer that rejection with this rule's duplicate-event error and message whichever of the two detections catches the repeat; a uniqueness refusal from the store is never surfaced as a generic data-integrity or server error.
Data source: ENT-FIN-004.eventReference, ENT-FIN-004.statusCode
Message    : ar: "تم بالفعل ترحيل قيد لهذا المرجع" · en: "An entry for this event reference has already been posted"
Traces     : REQ-FIN-011, REQ-FIN-072
Source     : general-accounting-system-plan-en.md §12.12; MODIFIED v2 — POL-FIN-030, CS-FIN-001 §3; ADR-FIN-027 (code unchanged)
Test-Hint  : the concurrent path is the one that used to leak a 500 — two simultaneous submissions of one reference, exactly one posts

### RULE-FIN-008 — بوابة الفترة عند الترحيل / Period gate at post time
Scope      : ENT-FIN-004, ENT-FIN-008
Trigger    : on post (any source)
Statement  : The system shall reject posting an entry whose period is not Open at that moment, except the year-end closing and opening entries generated by the year-end close (REQ-FIN-036, API-FIN-027), which are exempt from this gate: that close runs only after every period of the year is Hard Closed, so no Open period exists for its own entries by construction. The exemption is part of this rule and applies to no other entry and to no other rule — RULE-FIN-006, RULE-FIN-007 and RULE-FIN-009 apply to both generated entries in full.
Data source: ENT-FIN-004.periodId, ENT-FIN-008.statusCode
Message    : ar: "الفترة المستهدفة غير مفتوحة" · en: "The target period is not open"
Traces     : REQ-FIN-020, REQ-FIN-073
Source     : general-accounting-system-plan-en.md §12.4; MODIFIED v2 — wording only ("may only run once" → "runs only after"), no behaviour change (ADR-FIN-037)

### RULE-FIN-018 — نوعا اشتقاق الحساب المسموحان فقط / Only the two permitted account derivation types
Scope      : ENT-FIN-010
Trigger    : on create (rule line)
Statement  : The system shall reject a rule line whose account derivation type is not an active ACCOUNT_DERIVATION_TYPE value, so that CONSTANT and MAPPING are the only accepted derivations and no derivation reads an account code from the event.
Data source: ENT-FIN-010.accountDerivationTypeCode
Message    : ar: "نوع اشتقاق الحساب غير مدعوم؛ المسموح قيمة ثابتة أو ربط" · en: "Unsupported account derivation type; only constant or mapping is allowed"
Traces     : REQ-FIN-048
Source     : POL-FIN-021; ADR-FIN-010

### RULE-FIN-019 — اكتمال مواصفة الاشتقاق / Derivation spec matches its type
Scope      : ENT-FIN-010
Trigger    : on create (rule line)
Statement  : The system shall require a CONSTANT rule line to carry an account code in accountDerivationValue and no business field, and a MAPPING rule line to carry a business field in accountBusinessFieldCode and no account code.
Data source: ENT-FIN-010.accountDerivationTypeCode, ENT-FIN-010.accountDerivationValue, ENT-FIN-010.accountBusinessFieldCode
Message    : ar: "مواصفة اشتقاق الحساب لا تطابق نوعه" · en: "The account derivation spec does not match its type"
Traces     : REQ-FIN-049
Source     : POL-FIN-022; ADR-FIN-023

### RULE-FIN-020 — القيمة التجارية يجب أن تكون مربوطة / The business value must be mapped
Scope      : ENT-FIN-015, ENT-FIN-010
Trigger    : on build (event-sourced entry, per MAPPING rule line)
Statement  : The system shall reject the event with the dedicated unmapped-value error when no active account mapping exists for the rule's event type, the rule line's business field and the event's value for that field; it never substitutes a default account.
Data source: ENT-FIN-015.eventTypeCode, ENT-FIN-015.businessFieldCode, ENT-FIN-015.businessValue, ENT-FIN-015.isActiveFl, ENT-FIN-015.accountId, ENT-FIN-010.accountBusinessFieldCode, ENT-FIN-009.eventTypeCode
Message    : ar: "لا يوجد ربط حساب نشط للقيمة التجارية في الحدث" · en: "No active account mapping exists for the business value in the event"
Traces     : REQ-FIN-050, REQ-FIN-053
Source     : POL-FIN-022, POL-FIN-024; CS-FIN-001 §1; ADR-FIN-027
Test-Hint  : an inactive mapping counts as no mapping

### RULE-FIN-021 — الحدث يحمل الحقل التجاري المطلوب / The event carries the business field the rule names
Scope      : ENT-FIN-010, ENT-FIN-016
Trigger    : on build (event-sourced entry, per MAPPING rule line and per BUSINESS_FIELD dimension tag)
Statement  : The system shall reject the event with the missing-business-field error when the event does not carry the business field named by a MAPPING rule line or by a BUSINESS_FIELD dimension tag.
Data source: ENT-FIN-010.accountBusinessFieldCode, ENT-FIN-016.businessFieldCode, ENT-FIN-016.valueSourceCode
Message    : ar: "الحدث لا يحمل الحقل التجاري الذي تطلبه القاعدة" · en: "The event does not carry the business field the rule requires"
Traces     : REQ-FIN-051, REQ-FIN-068
Source     : CS-FIN-001 §1 "never a default, never a guess"; POL-FIN-024, POL-FIN-028; ADR-FIN-027

### RULE-FIN-022 — ربط نشط واحد لكل مفتاح / One active mapping per key
Scope      : ENT-FIN-015
Trigger    : on create (account mapping)
Statement  : The system shall reject an account mapping whose event type, business field and business value already have an active mapping.
Data source: ENT-FIN-015.eventTypeCode, ENT-FIN-015.businessFieldCode, ENT-FIN-015.businessValue, ENT-FIN-015.isActiveFl
Message    : ar: "يوجد بالفعل ربط نشط لنوع الحدث والحقل التجاري والقيمة نفسها" · en: "An active mapping already exists for this event type, business field and value"
Traces     : REQ-FIN-059
Source     : module-registry-fin.md v2 AUTO-DECISION (one active mapping per key); ADR-FIN-024

### RULE-FIN-023 — الحساب المربوط ورقي نشط عند الحفظ / A mapped account is an active leaf on save
Scope      : ENT-FIN-015
Trigger    : on create / update (account mapping)
Statement  : The system shall reject an account mapping whose account is not a leaf account or is inactive.
Data source: ENT-FIN-015.accountId, ENT-FIN-001.isLeafFl, ENT-FIN-001.isActiveFl
Message    : ar: "الحساب المربوط يجب أن يكون حسابًا ورقيًا نشطًا" · en: "The mapped account must be an active leaf account"
Traces     : REQ-FIN-060
Source     : POL-FIN-023; ADR-FIN-024
Test-Hint  : does not replace RULE-FIN-007 at post time — an account deactivated after the mapping was saved is still caught there

### RULE-FIN-024 — قيمة طريقة الدفع رمز معرَّف / A payment-method value is a defined code
Scope      : ENT-FIN-015
Trigger    : on create (account mapping)
Statement  : The system shall reject an account mapping for the PAYMENT_METHOD business field whose business value is not an active PAYMENT_METHOD code.
Data source: ENT-FIN-015.businessFieldCode, ENT-FIN-015.businessValue
Message    : ar: "القيمة ليست طريقة دفع معرَّفة" · en: "The value is not a defined payment method"
Traces     : REQ-FIN-061
Source     : module-registry-fin.md v2 AUTO-DECISION (business value stored as a code; PAYMENT_METHOD → its lookup)

### RULE-FIN-025 — وسم واحد لكل بُعد على سطر القاعدة / One tag per dimension on a rule line
Scope      : ENT-FIN-016
Trigger    : on create (rule line with its dimension tags)
Statement  : The system shall reject a rule line that carries more than one dimension tag for the same dimension.
Data source: ENT-FIN-016.ruleLineId, ENT-FIN-016.dimensionId
Message    : ar: "لا يجوز وسم البُعد نفسه مرتين على سطر القاعدة" · en: "A rule line cannot tag the same dimension twice"
Traces     : REQ-FIN-064, REQ-FIN-062
Source     : CS-FIN-001 §2; ENT-FIN-006 v1 shape (one value per dimension per line)

### RULE-FIN-026 — وسم البُعد يطابق مصدر قيمته / A dimension tag matches its value source
Scope      : ENT-FIN-016
Trigger    : on create (rule line with its dimension tags)
Statement  : The system shall require a CONSTANT dimension tag to carry a dimension value that belongs to the tagged dimension and no business field, and a BUSINESS_FIELD dimension tag to carry a business field and no dimension value.
Data source: ENT-FIN-016.valueSourceCode, ENT-FIN-016.dimensionId, ENT-FIN-016.dimensionValueId, ENT-FIN-016.businessFieldCode, ENT-FIN-003.dimensionId
Message    : ar: "وسم البُعد لا يطابق مصدر قيمته" · en: "The dimension tag does not match its value source"
Traces     : REQ-FIN-065, REQ-FIN-062
Source     : POL-FIN-026; ADR-FIN-010

### RULE-FIN-027 — قيمة البُعد من الحدث قابلة للتحديد / An event dimension value resolves to a defined value
Scope      : ENT-FIN-016, ENT-FIN-003
Trigger    : on build (event-sourced entry, per BUSINESS_FIELD dimension tag)
Statement  : The system shall reject the event with the unresolvable-dimension-value error when the event's value for a tag's business field equals the code of no dimension value of the tagged dimension.
Data source: ENT-FIN-016.dimensionId, ENT-FIN-016.businessFieldCode, ENT-FIN-003.code, ENT-FIN-003.dimensionId
Message    : ar: "قيمة البُعد في الحدث لا تطابق أي قيمة معرَّفة لهذا البُعد" · en: "The dimension value in the event matches no defined value of this dimension"
Traces     : REQ-FIN-069, REQ-FIN-063
Source     : POL-FIN-028; ADR-FIN-012, ADR-FIN-026, ADR-FIN-027
Test-Hint  : a matching but inactive value is RULE-FIN-009's rejection, not this one

### RULE-FIN-028 — تاريخ الحدث داخل فترة معرَّفة / The event date falls in a defined period
Scope      : ENT-FIN-008
Trigger    : on build (event-sourced entry)
Statement  : The system shall reject the event with the no-defined-period configuration error when no accounting period's start and end dates cover the event's date, and shall never answer this case with the period-not-open error.
Data source: ENT-FIN-008.startDate, ENT-FIN-008.endDate
Message    : ar: "لا توجد فترة محاسبية معرَّفة تغطي تاريخ الحدث" · en: "No accounting period is defined for the event date"
Traces     : REQ-FIN-074
Source     : POL-FIN-031 rationale; resolved decision v2-5; ADR-FIN-014, ADR-FIN-027

## A6 — Lookups

All 15 lookup types below are owned by FIN and registered into MDL (REQ-FIN-045). v1 had 13; v2 ADDS
2 and MODIFIES 2. module-registry-fin.md (LOOKUPS OWNED, v1 + v2 delta) carries the full source
citation per key; values are runtime-loaded from MDL, never hardcoded.

| Key | Values | v2 |
|---|---|---|
| ACCOUNT_TYPE | ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE | unchanged |
| DEBIT_CREDIT | DEBIT, CREDIT | unchanged |
| PERIOD_STATE | OPEN, SOFT_CLOSE, HARD_CLOSE, YEAR_END_CLOSE | unchanged |
| FISCAL_YEAR_STATUS | OPEN, CLOSED | unchanged |
| JOURNAL_TYPE | EVENT_GENERATED, MANUAL, RECURRING, ALLOCATION, REVERSAL | unchanged |
| JOURNAL_STATUS | DRAFT, POSTED, VOID (VOID is seeded in MDL by V26 and declared as `JournalEntry.STATUS_VOID`, but is now UNREACHABLE: classic reversal leaves the original POSTED and no code path writes VOID — see §A7) | unchanged |
| ACCOUNTING_EVENT_TYPE | none seeded — host-specific, added as data [§3, §6.4] | unchanged; now also used by ENT-FIN-015.eventTypeCode |
| PAYMENT_METHOD | CHEQUE (شيك / cheque) seeded; every other value host-specific, added as data | MODIFIED — CHEQUE named by the user; now used by ENT-FIN-015.businessValue [RULE-FIN-024] |
| ACCOUNT_DERIVATION_TYPE | CONSTANT, MAPPING | MODIFIED — DIRECT removed outright (ADR-FIN-010); MAPPING now implemented |
| AMOUNT_SOURCE_TYPE | FIELD, PERCENTAGE, REMAINDER | unchanged |
| DISTRIBUTION_TYPE | FIXED, PERCENTAGE, REMAINDER | unchanged |
| RECURRING_SCHEDULE_TYPE | RECURRING, REVERSING | unchanged |
| RECURRING_FREQUENCY | MONTHLY, QUARTERLY, ANNUALLY, WEEKLY | unchanged |
| FIN_EVENT_BUSINESS_FIELD | PAYMENT_METHOD, EXPENSE_TYPE_CODE, ORGANISATION_CODE, BRANCH_CODE | ADDED |
| FIN_DIMENSION_VALUE_SOURCE | CONSTANT, BUSINESS_FIELD | ADDED |

v2 lookups in the §6 block form:

```
FIN_EVENT_BUSINESS_FIELD — owner: FIN (ADR-FIN-013)
  used by   : ENT-FIN-010.accountBusinessFieldCode · ENT-FIN-015.businessFieldCode · ENT-FIN-016.businessFieldCode
  control   : lookup (fixed short list, grows as data when a new business fact is needed)
  values    : PAYMENT_METHOD    — طريقة الدفع / Payment method
              EXPENSE_TYPE_CODE — رمز نوع المصروف / Expense type code
              ORGANISATION_CODE — رمز المؤسسة / Organisation code
              BRANCH_CODE       — رمز الفرع / Branch code
  meaning   : the event names each business fact it carries by one of these codes; no code names an account
  source    : business-policies-fin.md v2 CUSTOM LOOKUP VALUES; CS-FIN-001 §1, §2

FIN_DIMENSION_VALUE_SOURCE — owner: FIN (ADR-FIN-010)
  used by   : ENT-FIN-016.valueSourceCode
  control   : lookup (fixed short list)
  values    : CONSTANT       — ثابت / Constant
              BUSINESS_FIELD — حقل تجاري / Business field
  source    : business-policies-fin.md v2 CUSTOM LOOKUP VALUES; CS-FIN-001 §2

ACCOUNT_DERIVATION_TYPE (MODIFIED) — owner: FIN
  used by   : ENT-FIN-010.accountDerivationTypeCode
  values    : CONSTANT — قيمة ثابتة / Constant
              MAPPING  — ربط / Mapping
  removed   : DIRECT (no data used it — v1 never delivered; ADR-FIN-010)

PAYMENT_METHOD (MODIFIED) — owner: FIN
  used by   : ENT-FIN-015.businessValue (when businessFieldCode=PAYMENT_METHOD)
  values    : CHEQUE — شيك / Cheque (named by the user); every other method host-defined, added as data
```

Consumed lookups: none — FIN owns every coded value list it uses.

## A7 — Status lifecycle

v2 adds no status field: ENT-FIN-015 carries only the binary `isActiveFl` and ENT-FIN-016 carries none
(not applicable). The v1 lifecycles below stand unchanged and are restated so the folded state keeps them.

**JournalEntry.statusCode — ENT-FIN-004 (JOURNAL_STATUS, 3 states)**
```
DRAFT  --(REQ-FIN-017, automatic validation passes)--> POSTED   (terminal)
POSTED --(REQ-FIN-028, reverse action)----------------> POSTED  (no transition — classic reversal: the original STAYS POSTED; its reversal is itself a new POSTED entry, and the two are linked through originalEntryId/reversalEntryId)
```
Classic reversal is the convention FIN follows: reversing an entry posts an equal, opposite
mirror entry and leaves the original POSTED and untouched, so the net effect on every account
balance is zero and both entries stay visible in the ledger and its audit trail. VOIDing the
original instead would remove it from the POSTED-only reports while the mirror remained,
netting to −(original). It also means RULE-FIN-016 (a POSTED row is never modified) holds
without exception — the reverse action's only write back to the original is the
reversalEntryId link.

DRAFT is transient (build → validate happens in one orchestration, RULE-FIN-016 locks
POSTED immediately); a DRAFT that fails validation is never saved (REQ-FIN-015/018-021
reject before any row is written) — not a stored intermediate state a user can browse. v2: the
same holds for every new event rejection (REQ-FIN-051, 052, 053, 067, 068, 069, 073, 074) — each is
raised before any row is written, so no entry and no line exists for the rejected event.

**FiscalPeriod.statusCode — ENT-FIN-008 (PERIOD_STATE, 4 states)**
```
OPEN        --(REQ-FIN-033, soft-close)--------> SOFT_CLOSE
SOFT_CLOSE  --(REQ-FIN-032, reopen)------------> OPEN
SOFT_CLOSE  --(REQ-FIN-034, hard-close)--------> HARD_CLOSE
HARD_CLOSE  --(REQ-FIN-036, year-end close, all periods of the year)--> YEAR_END_CLOSE
```
HARD_CLOSE and YEAR_END_CLOSE are terminal (RULE-FIN-014 — never reopened). v2: any state other than
OPEN answers an event posting with the period-not-open error (REQ-FIN-073); only SOFT_CLOSE can
later return to OPEN, so it is the case the consumer's retry resolves.

**FiscalYear.statusCode — ENT-FIN-007 (FISCAL_YEAR_STATUS, 2 states)** — binary
(OPEN/CLOSED, set by REQ-FIN-036) — not applicable for a diagram.

All other statuses in this module (isActiveFl flags) are binary — not applicable.

## A8 — Module dependencies

| Consumed entity | Owner ENT id | Owner module | HARD-FK / SOFT-READ | XM candidate (assigned by P2) |
|---|---|---|---|---|
| User (identity, principal string on audit columns) | ENT-SEC-001 | SEC | HARD-FK (per SEC's own precedent: principal string, not numeric FK — see AUTO-DECISIONS) | assigned by P2 |
| ModuleRegistry, ScreenRegistry, ActionRegistry | ENT-SEC-004, ENT-SEC-005, ENT-SEC-006 | SEC | HARD-FK (registration as data) — v2: + the Account mappings screen and its actions (SCR-REQ-FIN-013) | assigned by P2 |
| LookupType, LookupValue | ENT-MDL-001, ENT-MDL-002 | MDL | HARD-FK (registration + runtime read) — v2: + 2 lookup types, CHEQUE value, DIRECT value retired; the physical shape stays the v1 SOFT-READ lookup validation (ADR-FIN-001), now over 15 keys | assigned by P2 |

| External service | Purpose | Integration kind |
|---|---|---|
| Notifications (ready, external) | period-close-awaiting notice | SOFT / optional, per general-accounting-system-plan-en.md §2.3 |
| File Service (ready, external) | statement export | SOFT / optional, per general-accounting-system-plan-en.md §2.3 |
| Legacy Oracle system (host, v2) | emits the canonical accounting event carrying business facts only — never an account code | EVENT only, through the out-of-scope consumer; no table read or write in either direction |

# PART B — SCREEN REQUIREMENTS

v2: SCR-REQ-FIN-013 ADDED; SCR-REQ-FIN-003 and SCR-REQ-FIN-006 MODIFIED (re-emitted in full below);
the other ten screen requirements stand unchanged from v1.

## SCR-REQ-FIN-003 — قواعد المحرك / Engine rules
### B1 — Definition
Purpose      : إدارة قواعد ربط أنواع الأحداث بسطور القيد، ومنها سطور تشتق حسابها بالربط وتحمل وسوم الأبعاد.
Entities     : ENT-FIN-009, ENT-FIN-010, ENT-FIN-016
Operations   : search, create, deactivate (rule — API-FIN-034); create (line, with its dimension tags). As
               built there is no update and no by-id read on either, and no line delete — see B4
Users        : مسؤول مالي
Navigation   : FIN → Setup → Engine rules; to: SCR-REQ-FIN-013 (the mappings a MAPPING line resolves through)
Content shape: header + repeating lines (rule header + its lines), each line with its own repeating dimension tags
Traces       : REQ-FIN-007, REQ-FIN-008, REQ-FIN-009, REQ-FIN-048, REQ-FIN-049, REQ-FIN-062, REQ-FIN-064, REQ-FIN-065
Composite    : Master (rules) + Detail (lines, with their tags) = ONE screen requirement
MODIFIED v2  : MAPPING lines name a business field; DIRECT no longer offered; dimension tags per line
### B2 — Search / list
Filters: eventTypeCode(EXACT), isActiveFl(EXACT) — correspond to result columns.
### B3 — Input
Rule fields: eventTypeCode, nameAr, nameEn (ENT-FIN-009). Line fields: accountDerivationTypeCode,
accountDerivationValue (CONSTANT only), accountBusinessFieldCode (MAPPING only), amountSourceTypeCode,
amountSourceValue, directionCode, distributionTypeCode, isRemainderFl (ENT-FIN-010; RULE-FIN-003 blocks
saving until exactly one remainder line exists when a percentage line is present; RULE-FIN-018 admits
only CONSTANT and MAPPING; RULE-FIN-019 pairs the derivation value or business field with the type).
Dimension tags per line (repeating, ENT-FIN-016): dimensionId, valueSourceCode, dimensionValueId
(CONSTANT only), businessFieldCode (BUSINESS_FIELD only); RULE-FIN-025 one tag per dimension, RULE-FIN-026
tag matches its source.
### B4 — Access
Page code: FIN_RULES. Actions: VIEW, CREATE, UPDATE. Roles: مسؤول المالية / finance administrator holds all three.

**UPDATE covers two endpoints**, both gated by the single `PERM_FIN_RULES_UPDATE` that V24 already
seeds — no new permission constant and no migration were needed: API-FIN-011 (add a rule line) and
API-FIN-034, `PUT /api/v1/fin/event-rules/{id}/deactivate` (note the base path is
`/api/v1/fin/event-rules`, not `event-type-rules`). An unknown rule id answers the pre-existing
`FIN-404-RULE`. API-FIN-034 exists because until it landed no rule could ever be retired, which
left `FIN-404-NO-ACTIVE-RULE` (RULE-FIN-005, raised by API-FIN-020) unreachable. **Stated
limitation**, recorded in `EventTypeRuleService`'s own javadoc: deactivating a rule does NOT free
its event type for a replacement rule, because `EventTypeRuleService.create` guards uniqueness with
`existsByEventTypeCode`, which is not scoped to the active flag. No `activate` counterpart was
added, matching the delivered `AccountService.deactivate` precedent.

**Not built, and why.** `EventTypeRuleService` publishes exactly `create`, `deactivate` and
`search`; `RuleLineService` publishes `create` alone. So the rule has no update and no by-id read
endpoint, and the line has none of update, by-id read or delete. The rule-line *delete* named under
B1 Operations is a deliberate v1 exclusion rather than an oversight: ENT-FIN-010 carries no active
flag to soft-delete against, and FIN publishes no `DELETE` endpoint on any screen — V24 seeds no
`PERM_FIN_RULES_DELETE` row for this or any other FIN screen.

**v2 — dimension tags need no new action.** Tags travel inside the add-rule-line submission and follow
the line's lifecycle (ADR-FIN-025), so they are gated by the same UPDATE permission as the line itself;
no tag-level update or delete exists, inheriting the line's limitation stated above.
### B5 — API expectations
| Operation | Verb | Path | Inputs | Outputs | RULEs | Traces (REQ) |
|---|---|---|---|---|---|---|
| search rules | POST | /api/v1/fin/event-rules/search | filters, paging (request body) | Page\<EventTypeRule\> | — | REQ-FIN-007 |
| create rule | POST | /api/v1/fin/event-rules | eventTypeCode, nameAr, nameEn | EventTypeRule | — | REQ-FIN-007 |
| add rule line (v2: with accountBusinessFieldCode and dimension tags) | POST | /api/v1/fin/event-rules/{id}/lines | line fields + dimension tags [] | RuleLine with its tags | RULE-FIN-003, RULE-FIN-018, RULE-FIN-019, RULE-FIN-025, RULE-FIN-026 | REQ-FIN-008, REQ-FIN-009, REQ-FIN-048, REQ-FIN-049, REQ-FIN-062, REQ-FIN-064, REQ-FIN-065 |
| deactivate rule | PUT | /api/v1/fin/event-rules/{id}/deactivate | id | EventTypeRule (isActiveFl=false) | — | REQ-FIN-007 |

## SCR-REQ-FIN-006 — قيود اليومية / Journal entries (view + manual entry + reverse)
### B1 — Definition
Purpose      : عرض قيود اليومية بكل مصادرها، إدخال يدوي، وعكس قيد مُرحَّل.
Entities     : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006
Operations   : search, create (manual), read, reverse; system operation: build an entry from a canonical event (no screen affordance)
Users        : محاسب
Navigation   : FIN → Operations → Journal entries
Content shape: header + repeating lines with totals (debit/credit totals shown live while entering)
Traces       : REQ-FIN-010, REQ-FIN-011, REQ-FIN-012, REQ-FIN-013, REQ-FIN-014, REQ-FIN-015, REQ-FIN-016, REQ-FIN-017, REQ-FIN-018, REQ-FIN-019, REQ-FIN-020, REQ-FIN-021, REQ-FIN-027, REQ-FIN-028, REQ-FIN-029, REQ-FIN-030, REQ-FIN-047, REQ-FIN-050, REQ-FIN-051, REQ-FIN-052, REQ-FIN-053, REQ-FIN-063, REQ-FIN-066, REQ-FIN-067, REQ-FIN-068, REQ-FIN-069, REQ-FIN-070, REQ-FIN-072, REQ-FIN-073, REQ-FIN-074, REQ-FIN-076, REQ-FIN-077
Composite    : Search + Entry (with a "Reverse" action on a posted entry) = ONE screen requirement
MODIFIED v2  : the event-build operation resolves mapped accounts and dimension tags, and answers the
               duplicate, period-not-open and no-defined-period cases distinctly; the screen itself is unchanged —
               event-built lines show their dimension values exactly as manual lines do (REQ-FIN-070)
### B2 — Search / list
Filters: docNo(LIKE), docDate(DATE_RANGE), periodId(EXACT), statusCode(EXACT), journalTypeCode(EXACT) — correspond to result columns.
### B3 — Input
Header fields: docDate, periodId, descriptionAr, descriptionEn (ENT-FIN-004; docNo/statusCode/
journalTypeCode system-set). Line fields (repeating): accountId, amount, directionCode,
dimension value(s) per JournalLineDimension, descriptionAr, descriptionEn (ENT-FIN-005/006).
Buttons: "Post" → REQ-FIN-017 (validates REQ-FIN-018..021 first, REQ-FIN-015 shows failures);
"Reverse" (on a POSTED row) → REQ-FIN-028/029/030.
### B4 — Access
Page code: FIN_JOURNAL_ENTRIES. Actions: VIEW, CREATE (incl. Post), UPDATE (Reverse, modeled
as an update-class action per §7.1 custom-action convention — `PERM_FIN_JOURNAL_ENTRIES_REVERSE`).
v2 adds no action here: the event-build operation is a system call, and the machine caller's
authentication is a separate SEC change set (CS-FIN-001 Out of scope).
### B5 — API expectations
| Operation | Verb | Path | Inputs | Outputs | RULEs | Traces (REQ) |
|---|---|---|---|---|---|---|
| search entries | POST | /api/v1/fin/journal-entries/search | filters, paging (request body) | Page\<JournalEntry\> | — | REQ-FIN-027 |
| create manual entry | POST | /api/v1/fin/journal-entries | header + lines | JournalEntry (DRAFT then POSTED) | RULE-FIN-006, RULE-FIN-007, RULE-FIN-008, RULE-FIN-009, RULE-FIN-017 | REQ-FIN-014, REQ-FIN-015, REQ-FIN-017, REQ-FIN-018, REQ-FIN-019, REQ-FIN-020, REQ-FIN-021 |
| build event entry (system) | POST | /api/v1/fin/journal-entries/from-event | canonical event payload — business facts named by FIN_EVENT_BUSINESS_FIELD codes, never an account code | JournalEntry | RULE-FIN-004 (both detection paths), RULE-FIN-005, RULE-FIN-006..009, RULE-FIN-010, RULE-FIN-020, RULE-FIN-021, RULE-FIN-027, RULE-FIN-028 | REQ-FIN-010, REQ-FIN-011, REQ-FIN-012, REQ-FIN-013, REQ-FIN-017..021, REQ-FIN-047, REQ-FIN-050..053, REQ-FIN-063, REQ-FIN-066..070, REQ-FIN-072..074, REQ-FIN-076 |
| reverse entry | POST | /api/v1/fin/journal-entries/{id}/reverse | id | JournalEntry (the new reversal) | RULE-FIN-011, RULE-FIN-012, RULE-FIN-013 | REQ-FIN-028, REQ-FIN-029, REQ-FIN-030, REQ-FIN-077 |
| read entry | GET | /api/v1/fin/journal-entries/{id} | id | JournalEntry with lines | — | REQ-FIN-016, REQ-FIN-027 |

Rejection answers of the event-build operation, each distinct (ADR-FIN-027): duplicate event (RULE-FIN-004 —
the consumer acknowledges), period not open (RULE-FIN-008 — the consumer waits and retries; published per
REQ-FIN-075), and — each a failure needing a fix — unmapped value (RULE-FIN-020), missing business field
(RULE-FIN-021), unresolvable dimension value (RULE-FIN-027), no defined period (RULE-FIN-028), plus the v1
rejections. No rejection writes any row.

## SCR-REQ-FIN-013 — روابط الحسابات / Account mappings
### B1 — Definition
Purpose      : إدارة روابط الحسابات كبيانات: لكل نوع حدث وحقل تجاري وقيمة تجارية حسابٌ واحد نشط، يُضاف ويُعدَّل حسابه ويُوقف دون أي تعديل في الشيفرة.
Entities     : ENT-FIN-015
Operations   : search, create, update (account only), deactivate
Users        : مسؤول المالية / finance administrator
Navigation   : FIN → Setup → Account mappings; from: SCR-REQ-FIN-003 (a MAPPING rule line); to: SCR-REQ-FIN-001 (the mapped account)
Content shape: flat record
Traces       : REQ-FIN-054, REQ-FIN-055, REQ-FIN-056, REQ-FIN-057, REQ-FIN-058, REQ-FIN-059, REQ-FIN-060, REQ-FIN-061
Composite    : Search + Entry = ONE screen requirement
### B2 — Search / list
Filters: eventTypeCode(EXACT, lookup ACCOUNTING_EVENT_TYPE), businessFieldCode(EXACT, lookup
FIN_EVENT_BUSINESS_FIELD), businessValue(LIKE), accountId(EXACT), isActiveFl(EXACT) — correspond to the
result columns eventTypeCode, businessFieldCode, businessValue, account (code + nameAr/nameEn from
ENT-FIN-001), isActiveFl. Server-side paging [KB:erp-domain-standards §6].
### B3 — Input
Fields: eventTypeCode, businessFieldCode, businessValue, accountId (ENT-FIN-015). When businessFieldCode is
PAYMENT_METHOD, businessValue is chosen from the PAYMENT_METHOD lookup (RULE-FIN-024); otherwise it is the
host's code typed as-is. accountId offers active leaf accounts (RULE-FIN-023). eventTypeCode,
businessFieldCode and businessValue are read-only once saved (ADR-FIN-024).
Buttons: Save (new) → REQ-FIN-054, applies RULE-FIN-022, RULE-FIN-023, RULE-FIN-024; Save (existing) →
REQ-FIN-056 and REQ-FIN-058, applies RULE-FIN-023; Deactivate → REQ-FIN-057 and REQ-FIN-058.
### B4 — Access
Page code: FIN_ACCOUNT_MAPPINGS (one SEC_PAGES row, registered by REQ-FIN-044). Actions: VIEW (gateway),
CREATE (new mapping), UPDATE (change account, and deactivate — as FIN_ACCOUNTS models deactivate).
Roles: مسؤول المالية / finance administrator holds VIEW, CREATE, UPDATE. No DELETE: mappings are
deactivated, never hard-deleted.
### B5 — API expectations
| Operation | Verb | Path | Inputs | Outputs | RULEs | Traces (REQ) |
|---|---|---|---|---|---|---|
| search account mappings | POST | /api/v1/fin/account-mappings/search | filters (eventTypeCode, businessFieldCode, businessValue, accountId, isActiveFl), paging (request body) | Page\<AccountMapping\> | — | REQ-FIN-055 |
| create account mapping | POST | /api/v1/fin/account-mappings | eventTypeCode, businessFieldCode, businessValue, accountId | AccountMapping | RULE-FIN-022, RULE-FIN-023, RULE-FIN-024 | REQ-FIN-054, REQ-FIN-059, REQ-FIN-060, REQ-FIN-061 |
| update account mapping | PUT | /api/v1/fin/account-mappings/{id} | accountId | AccountMapping | RULE-FIN-023 | REQ-FIN-056, REQ-FIN-058, REQ-FIN-060 |
| deactivate account mapping | PUT | /api/v1/fin/account-mappings/{id}/deactivate | id | AccountMapping (isActiveFl=false) | — | REQ-FIN-057, REQ-FIN-058 |

Verb and path shapes follow FIN's v1 house style (POST `/search` with a request body; deactivate as
`PUT /{id}/deactivate` under UPDATE), not the generic DELETE-as-deactivate of engine §8, so the new
screen reads like its eleven siblings.

# STANDALONE

## Traceability matrix
Current state (v1 rows restated unchanged so the folded state keeps them; v2 rows ADDED; REQ-FIN-045 row MODIFIED).

| P0.5 | REQ | AC | RULE | ENT | SCR-REQ |
|---|---|---|---|---|---|
| US-FIN-001 | REQ-FIN-001, REQ-FIN-002, REQ-FIN-003 | AC-FIN-001…003 | RULE-FIN-001 | ENT-FIN-001 | SCR-REQ-FIN-001 |
| US-FIN-002 | REQ-FIN-004, REQ-FIN-005, REQ-FIN-006 | AC-FIN-004…006 | RULE-FIN-002 | ENT-FIN-002, ENT-FIN-003 | SCR-REQ-FIN-002 |
| US-FIN-003 | REQ-FIN-007, REQ-FIN-008, REQ-FIN-009 | AC-FIN-007…009 | RULE-FIN-003 | ENT-FIN-009, ENT-FIN-010 | SCR-REQ-FIN-003 |
| US-FIN-004 | REQ-FIN-010, REQ-FIN-011, REQ-FIN-012, REQ-FIN-013 | AC-FIN-010…013 | RULE-FIN-004, RULE-FIN-005, RULE-FIN-010 | ENT-FIN-004, ENT-FIN-005, ENT-FIN-009, ENT-FIN-010 | SCR-REQ-FIN-006 |
| US-FIN-005 | REQ-FIN-014, REQ-FIN-015 | AC-FIN-014, AC-FIN-015 | — | ENT-FIN-004, ENT-FIN-005 | SCR-REQ-FIN-006 |
| US-FIN-006 | REQ-FIN-022, REQ-FIN-023, REQ-FIN-024 | AC-FIN-022…024 | — | ENT-FIN-011, ENT-FIN-012 | SCR-REQ-FIN-004 |
| US-FIN-007 | REQ-FIN-025, REQ-FIN-026 | AC-FIN-025, AC-FIN-026 | RULE-FIN-010 | ENT-FIN-013, ENT-FIN-014 | SCR-REQ-FIN-005 |
| US-FIN-008 | REQ-FIN-016, REQ-FIN-027 | AC-FIN-016, AC-FIN-027 | RULE-FIN-016 | ENT-FIN-004 | SCR-REQ-FIN-006 |
| US-FIN-009 | REQ-FIN-028, REQ-FIN-029, REQ-FIN-030 | AC-FIN-028…030 | RULE-FIN-011, RULE-FIN-012, RULE-FIN-013 | ENT-FIN-004, ENT-FIN-005 | SCR-REQ-FIN-006 |
| US-FIN-010 | REQ-FIN-031, REQ-FIN-032, REQ-FIN-033, REQ-FIN-034, REQ-FIN-035, REQ-FIN-036 | AC-FIN-031…036 | RULE-FIN-014 | ENT-FIN-007, ENT-FIN-008 | SCR-REQ-FIN-007 |
| US-FIN-011 | REQ-FIN-037, REQ-FIN-038 | AC-FIN-037, AC-FIN-038 | RULE-FIN-015 | ENT-FIN-008 | SCR-REQ-FIN-007 |
| US-FIN-012 | REQ-FIN-039 | AC-FIN-039 | — | ENT-FIN-005 | SCR-REQ-FIN-008 |
| US-FIN-013 | REQ-FIN-040 | AC-FIN-040 | RULE-FIN-006 | ENT-FIN-005 | SCR-REQ-FIN-009 |
| US-FIN-014 | REQ-FIN-041 | AC-FIN-041 | — | ENT-FIN-005 | SCR-REQ-FIN-010 |
| US-FIN-015 | REQ-FIN-042 | AC-FIN-042 | — | ENT-FIN-005 | SCR-REQ-FIN-011 |
| US-FIN-016 | REQ-FIN-043 | AC-FIN-043 | — | ENT-FIN-005, ENT-FIN-006 | SCR-REQ-FIN-012 |
| US-FIN-017 | REQ-FIN-044 | AC-FIN-044 | — | ENT-FIN-004 | — (onboarding, no screen) |
| US-FIN-018 | REQ-FIN-045 (MODIFIED v2) | AC-FIN-045 (MODIFIED v2) | — | ENT-FIN-001, ENT-FIN-004, ENT-FIN-007, ENT-FIN-008, ENT-FIN-009, ENT-FIN-010, ENT-FIN-011, ENT-FIN-013, ENT-FIN-015, ENT-FIN-016 | — (onboarding, no screen) |
| US-FIN-019 | REQ-FIN-046 | AC-FIN-046 | — | ENT-FIN-004, ENT-FIN-005 | SCR-REQ-FIN-008, SCR-REQ-FIN-009, SCR-REQ-FIN-010 |
| US-FIN-020 | REQ-FIN-047, REQ-FIN-048, REQ-FIN-050, REQ-FIN-045 | AC-FIN-047, AC-FIN-048, AC-FIN-050…052, AC-FIN-045 | RULE-FIN-018, RULE-FIN-020 | ENT-FIN-005, ENT-FIN-010, ENT-FIN-015 | SCR-REQ-FIN-003, SCR-REQ-FIN-006 |
| US-FIN-021 | REQ-FIN-049, REQ-FIN-050, REQ-FIN-051, REQ-FIN-061, REQ-FIN-045 | AC-FIN-049, AC-FIN-050, AC-FIN-051, AC-FIN-053, AC-FIN-065, AC-FIN-045 | RULE-FIN-019, RULE-FIN-020, RULE-FIN-021, RULE-FIN-024 | ENT-FIN-010, ENT-FIN-015 | SCR-REQ-FIN-003, SCR-REQ-FIN-006, SCR-REQ-FIN-013 |
| US-FIN-022 | REQ-FIN-049, REQ-FIN-050, REQ-FIN-051 | AC-FIN-049, AC-FIN-052, AC-FIN-053 | RULE-FIN-019, RULE-FIN-020, RULE-FIN-021 | ENT-FIN-010, ENT-FIN-015 | SCR-REQ-FIN-003, SCR-REQ-FIN-006 |
| US-FIN-023 | REQ-FIN-052, REQ-FIN-060 | AC-FIN-054, AC-FIN-055, AC-FIN-064 | RULE-FIN-007, RULE-FIN-023 | ENT-FIN-001, ENT-FIN-015 | SCR-REQ-FIN-006, SCR-REQ-FIN-013 |
| US-FIN-024 | REQ-FIN-051, REQ-FIN-053 | AC-FIN-053, AC-FIN-056, AC-FIN-057 | RULE-FIN-020, RULE-FIN-021 | ENT-FIN-010, ENT-FIN-015 | SCR-REQ-FIN-006 |
| US-FIN-025 | REQ-FIN-054, REQ-FIN-055, REQ-FIN-056, REQ-FIN-057, REQ-FIN-058, REQ-FIN-059, REQ-FIN-060, REQ-FIN-061 | AC-FIN-058…065 | RULE-FIN-022, RULE-FIN-023, RULE-FIN-024 | ENT-FIN-015 | SCR-REQ-FIN-013 |
| US-FIN-026 | REQ-FIN-062, REQ-FIN-063, REQ-FIN-064, REQ-FIN-065, REQ-FIN-045 | AC-FIN-066…070, AC-FIN-045 | RULE-FIN-025, RULE-FIN-026, RULE-FIN-027 | ENT-FIN-006, ENT-FIN-016 | SCR-REQ-FIN-003, SCR-REQ-FIN-006 |
| US-FIN-027 | REQ-FIN-066, REQ-FIN-067 | AC-FIN-071, AC-FIN-072 | RULE-FIN-009 | ENT-FIN-003, ENT-FIN-006, ENT-FIN-016 | SCR-REQ-FIN-006 |
| US-FIN-028 | REQ-FIN-067, REQ-FIN-068, REQ-FIN-069 | AC-FIN-072, AC-FIN-073, AC-FIN-074 | RULE-FIN-009, RULE-FIN-021, RULE-FIN-027 | ENT-FIN-003, ENT-FIN-016 | SCR-REQ-FIN-006 |
| US-FIN-029 | REQ-FIN-070, REQ-FIN-071 | AC-FIN-075, AC-FIN-076 | — | ENT-FIN-005, ENT-FIN-006 | SCR-REQ-FIN-006, SCR-REQ-FIN-008, SCR-REQ-FIN-012 (the two report screens unchanged — they already aggregate every POSTED line) |
| US-FIN-030 | REQ-FIN-072 | AC-FIN-077, AC-FIN-078 | RULE-FIN-004 | ENT-FIN-004 | SCR-REQ-FIN-006 |
| US-FIN-031 | REQ-FIN-073, REQ-FIN-074, REQ-FIN-075 | AC-FIN-079, AC-FIN-080, AC-FIN-081 | RULE-FIN-008, RULE-FIN-028 | ENT-FIN-004, ENT-FIN-008 | SCR-REQ-FIN-006 |
| US-FIN-032 | REQ-FIN-076, REQ-FIN-077 | AC-FIN-082, AC-FIN-083 | RULE-FIN-003, RULE-FIN-004, RULE-FIN-006, RULE-FIN-007, RULE-FIN-008, RULE-FIN-009, RULE-FIN-010, RULE-FIN-011, RULE-FIN-016 | ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 | SCR-REQ-FIN-006 |
| US-FIN-033 | — DEFERRED (out of v2 scope, ADR-FIN-012) | — | — | — | — |

Plus REQ-FIN-017 through REQ-FIN-021 (unified validation, traced to US-FIN-004/005/006/007/009
collectively — see each REQ's own Traces line) and RULE-FIN-006 through RULE-FIN-009 (the four
post-time checks). Every non-deferred story traces to ≥1 REQ; every REQ traces to ≥1 story; every AC
to ≥1 REQ; every RULE to ≥1 REQ; every SCR-REQ to ≥1 REQ. US-FIN-033 is DEFERRED in the approved PRD
and deliberately has no REQ. No orphan, no dangling id.

## Decisions applied
| DEFAULT / ADR | What | Source | Override / status |
|---|---|---|---|
| DEFAULT (v1) | `amount` type `NUMERIC(18,4)` | [KB:erp-domain-standards §6] platform default for money | non-breaking |
| DEFAULT (v1) | RecurringTemplateLine/AllocationTarget carry a single optional dimension value, not the header's full multi-dimension model | v1 P1, applying §9.3.3 "as data" pragmatically | non-breaking; extend to a full JournalLineDimension-style child table if multi-dimension templates are needed later |
| DEFAULT (v2) | AccountMapping is soft-deleted through `isActiveFl`, never hard-deleted | [KB:erp-domain-standards §6]; module-registry v2 AUTO-DECISION | hard delete with audit record |
| DEFAULT (v2) | Account-mapping search is server-side with paging (page size 20, max 200) | [KB:erp-domain-standards §6] | client-side filtering |
| DEFAULT (v2) | The legacy "cash unless cheque" behaviour becomes one explicit mapping row per payment-method value; no default account exists anywhere | CS-FIN-001 §1 "never a default"; POL-FIN-024; module-registry v2 AUTO-DECISION | none — a default is forbidden by the change request |
| ADR-FIN-023 | MAPPING rule line names its business field in `accountBusinessFieldCode`; `accountDerivationValue` = constant account code only | this stage | ACCEPTED (non-breaking) |
| ADR-FIN-024 | AccountMapping references its account by id; key fixed after create; update changes the account only | this stage | ACCEPTED (non-breaking) |
| ADR-FIN-025 | Dimension tags are submitted with their rule line and follow its lifecycle | this stage | ACCEPTED (non-breaking) |
| ADR-FIN-026 | A business-field dimension value resolves by exact `DimensionValue.code` within the tagged dimension | this stage | ACCEPTED (non-breaking) |
| ADR-FIN-027 | Four new distinct rejection errors; FIN-409-DUPLICATE-EVENT and FIN-409-PERIOD-NOT-OPEN confirmed unchanged; the MAPPING-unsupported code retired | this stage | ACCEPTED (non-breaking) |
| ADR-FIN-010 (applied) | DIRECT removed outright; dimensions use FIN_DIMENSION_VALUE_SOURCE | P0 v2 dialogue | RESOLVED-IN-DIALOGUE; confirmed at prd-approval |
| ADR-FIN-011 (applied) | A missing or invalid dimension value rejects the event | P0 v2 dialogue | RESOLVED-IN-DIALOGUE |
| ADR-FIN-012 (applied) | Legacy dimension-code translation deferred (US-FIN-033) | P0 v2 dialogue | RESOLVED-IN-DIALOGUE; confirmed at prd-approval |
| ADR-FIN-013 (applied) | Business-field names come from FIN_EVENT_BUSINESS_FIELD | P0 v2 dialogue | RESOLVED-IN-DIALOGUE; confirmed at prd-approval |
| ADR-FIN-014 (applied) | One period-not-open code for every existing non-open period; a date with no period is a configuration error | P0 v2 dialogue | RESOLVED-IN-DIALOGUE |
| ADR-FIN-020 (applied) | v2 roles reuse v1 role names (finance administrator, financial controller); event integration operator is narrative only | P0.5 v2 dialogue | RESOLVED-IN-DIALOGUE |
| ADR-FIN-021 (applied) | A mapping change never alters a posted entry (REQ-FIN-058) | P0.5 v2 dialogue | RESOLVED-IN-DIALOGUE |
| ADR-FIN-037 | Event business fields travel in `fields.<FIN_EVENT_BUSINESS_FIELD code>` (e.g. `fields.PAYMENT_METHOD`), never as a bare field named after the fact itself; every affected AC re-emitted to match; RULE-FIN-008's wording changed from "may" to "runs only after" with no behaviour change | pass-1 review (merge-review-notes) | ACCEPTED (non-breaking) |
| ADR-FIN-038 | ERP-1's master `entity_defaults` are adapted for ENT-FIN-007 (code is its own label, no separate name) and ENT-FIN-008 (periodNo+fiscalYearId is the key, state lives in statusCode) — v1 debt, recorded rather than schema-changed | pass-1 review | ACCEPTED (non-breaking) |
| ADR-FIN-039 | AccountMapping.businessValue matching is exact-code with leading/trailing blanks trimmed on save and on event read, for every business field, not PAYMENT_METHOD alone | pass-1 review | ACCEPTED (non-breaking) |

No BLOCKED ADR: every v2 choice is additive and contradicts no approved story, policy, registry fact or
closed decision.

## Access summary
| Page code | Screen | VIEW | CREATE | UPDATE | DELETE | Custom |
|---|---|---|---|---|---|---|
| FIN_ACCOUNTS | Chart of accounts | role-granted | role-granted | role-granted (incl. deactivate) | — | — |
| FIN_DIMENSIONS | Dimensions | role-granted | role-granted | role-granted (dimension-VALUE deactivate only — API-FIN-035; `PERM_FIN_DIMENSIONS_UPDATE`, registered and granted by V28) | — | — |
| FIN_RULES | Engine rules | role-granted | role-granted | role-granted (add line — API-FIN-011, v2: with its dimension tags; and deactivate rule — API-FIN-034; one permission, `PERM_FIN_RULES_UPDATE`) | — | — |
| FIN_RECURRING_TEMPLATES | Recurring/reversing templates | role-granted | role-granted | role-granted (run — API-FIN-014; and deactivate template — API-FIN-036; one permission, `PERM_FIN_RECURRING_TEMPLATES_UPDATE`) | — | — |
| FIN_ALLOCATION_RULES | Allocation rules | role-granted | role-granted | role-granted (run — API-FIN-017; and deactivate rule — API-FIN-037; one permission, `PERM_FIN_ALLOCATION_RULES_UPDATE`) | — | — |
| FIN_JOURNAL_ENTRIES | Journal entries | role-granted | role-granted (incl. Post) | — | — | Reverse (`PERM_FIN_JOURNAL_ENTRIES_REVERSE`) |
| FIN_PERIODS | Fiscal periods & years | role-granted (gateway, and now also the read endpoint API-FIN-033) | role-granted (year) | role-granted (open/soft-close) | — | Close-approve (`PERM_FIN_PERIODS_CLOSE_APPROVE` — the distinct permission RULE-FIN-015 requires. Since V30 it is granted to `SYS_ADMIN`, so the bootstrap `admin` can close; V27's dedicated `FIN_CLOSE_APPROVER` role remains valid as a least-privilege alternative but is no longer required — see the SoD note below) |
| FIN_ACCOUNT_LEDGER | Account ledger | role-granted | — | — | — | — |
| FIN_TRIAL_BALANCE | Trial balance | role-granted | — | — | — | — |
| FIN_BALANCE_SHEET | Balance sheet | role-granted | — | — | — | — |
| FIN_INCOME_STATEMENT | Income statement | role-granted | — | — | — | — |
| FIN_DIMENSION_REPORTS | Dimension reports | role-granted | — | — | — | — |
| FIN_ACCOUNT_MAPPINGS (v2 ADDED) | Account mappings | finance administrator | finance administrator | finance administrator (change account, and deactivate) | — | — |
Every action beyond VIEW additionally requires VIEW on the same screen (platform gateway
convention, `profiles/erp.yaml → conventions.security_model.gateway_action`, enforced by
SEC's own mechanism — not restated as a FIN-owned RULE). The new page's permissions are derived by SEC
from its page code; this SRS enumerates no permission seed data for it.

**DELETE column, as built**: empty for every FIN screen. FIN publishes no `DELETE` endpoint;
deactivation, where it exists, is `PUT /{id}/deactivate` gated by the screen's UPDATE permission,
and V24 seeds no `PERM_FIN_*_DELETE` row (nor does V28, which adds only
`PERM_FIN_DIMENSIONS_UPDATE`). The DELETE ✓ marks above were pre-implementation and were corrected
at ALIGN-BE. v2 keeps this: FIN_ACCOUNT_MAPPINGS deactivates under UPDATE and has no DELETE.

**Deactivate endpoints, as built**: five — API-FIN-004 (account), API-FIN-034 (event-type rule), the
dimension-value one API-FIN-035, API-FIN-036 (recurring/reversing template) and API-FIN-037
(allocation rule). None has an `activate` counterpart, following the delivered
`AccountService.deactivate` precedent. Each is `PUT /{id}/deactivate` gated by its screen's UPDATE
permission; API-FIN-036 and API-FIN-037 reuse `PERM_FIN_RECURRING_TEMPLATES_UPDATE` and
`PERM_FIN_ALLOCATION_RULES_UPDATE`, both already seeded by V24 and already granted by V25's blanket
Tier-3 grant, so neither needed a new permission, a new error code or a migration. Deliberately NOT
built, and not pending: a deactivate on the parent `Dimension` (no REQ/AC/RULE requires it and
`Dimension.isActiveFl` drives no behaviour) and a rule-line delete (ENT-FIN-010 has no active-flag
column and FIN publishes no `DELETE` endpoint on any screen) — see SCR-REQ-FIN-002 §B4 and
SCR-REQ-FIN-003 §B4. v2 adds a sixth deactivate, on the account mapping (SCR-REQ-FIN-013), with no
`activate` counterpart either (ADR-FIN-024).

**DEFECT CLOSED 2026-09-12 — on those two screens, deactivating now DOES stop the run.** This
supersedes the entry that used to stand here reporting the run as ungated. `RecurringTemplateDomain
.assertCanRun()` (a new Domain companion) and `AllocationRuleDomain.assertCanRun()` are each called
first thing in their service's `run`, so a deactivated template or allocation rule is refused with
`FIN-409-NOT-ACTIVE` (409) and posts nothing. The UPDATE cells above cover run AND deactivate for
these two screens, and "deactivate" there now means a real gate, not only a record-keeping state.
**The gate is a recorded human decision, not stated requirement: no RULE-FIN-* asks for it**, and
closing the gap required inventing an error code, status and ar+en messages that no REQ, AC or RULE
provides — do not read `FIN-409-NOT-ACTIVE` back out of these tables as pre-existing spec. See
SCR-REQ-FIN-004 §B4 and SCR-REQ-FIN-005 §B4.

**SoD on FIN_PERIODS — what was removed, and what still holds (2026-09-12).** RULE-FIN-015 stands
and is enforced by the `PERM_FIN_PERIODS_CLOSE_APPROVE` gate alone, which is exactly what the rule
asks for. A stricter, unrequested check — FIN service code that refused the close for every caller
whenever any single user in the system held both that permission and
`PERM_FIN_JOURNAL_ENTRIES_CREATE` — was deleted by recorded human decision, together with the SEC
user-directory read behind it; `FIN-403-SOD-VIOLATION` is struck as unreachable. Migration V30 then
granted `PERM_FIN_PERIODS_CLOSE_APPROVE` to `SYS_ADMIN`, which V25 had deliberately withheld, so
the bootstrap `admin` can now close. Note that V27's header, being applied and immutable, still
describes the removed mechanism as live in both its numbered "OPERATIONAL PRECONDITIONS" — both are
now false; see the SEC-BE phase document for the correction in full.

**OPEN DEFECT — `update` is still missing on the same two screens.** This is now the only open half
of the original gap: a template or allocation rule created with wrong content can be retired — and,
since 2026-09-12, retiring it does stop it running — but it still cannot be corrected. Neither update was built because a
correct one must decide the fate of the aggregate's children — a template's existing lines, a
rule's existing targets and hence RULE-FIN-003's remainder-target set — which is a design question
no REQ, AC or RULE answers. Recorded as a known gap, not a reasoned exclusion. See SCR-REQ-FIN-004
§B4 and SCR-REQ-FIN-005 §B4.
══════════════════════════════════════════════════════════════════

<!-- carried from baseline v1 by gov.py state — not re-emitted in this delta -->
### ENT-FIN-001 — الحساب / Account
| Kind | Ownership | Business number | Operations | Cross-module | Source |
|---|---|---|---|---|---|
| master | PRIVATE | No — `code` is a client-defined chart-of-accounts code, not a platform-numbered sequence [§3.3 test] | create, read, search, update, deactivate | none | general-accounting-system-plan-en.md §4 |

| Field | Logical type | Required | Values / source | Notes | Label-ar | Label-en |
|---|---|---|---|---|---|---|
| accountPk | number | yes (system) | — | primary key | معرّف الحساب | Account id |
| code | text | yes | unique | chart-of-accounts code | رمز الحساب | Account code |
| nameAr | text | yes | — | — | اسم الحساب (عربي) | Account name (Arabic) |
| nameEn | text | yes | — | — | اسم الحساب (إنجليزي) | Account name (English) |
| accountTypeCode | lookup | yes | lookup key `ACCOUNT_TYPE` (A6) | asset/liability/equity/revenue/expense | نوع الحساب | Account type |
| natureCode | lookup | yes | lookup key `DEBIT_CREDIT` (A6) | normal balance side [POL-FIN-002] | الطبيعة | Nature |
| parentAccountId | reference | no | ENT-FIN-001 (self) | null = root | الحساب الأب | Parent account |
| isLeafFl | flag | yes | — | accepts direct posting only when true [RULE-FIN-001, POL-FIN-003] | ورقة (يقبل ترحيلاً مباشرًا) | Leaf (accepts direct posting) |
| isActiveFl | flag | yes | — | — | نشط | Active |
| createdBy, createdAt, updatedBy, updatedAt | system | yes | — | standard audit fields | — | — |


### ENT-FIN-002 — البُعد / Dimension
| Kind | Ownership | Business number | Operations | Cross-module | Source |
|---|---|---|---|---|---|
| config | PRIVATE | No | create, read, search, deactivate | none | general-accounting-system-plan-en.md §4.2 |

| Field | Logical type | Required | Values / source | Notes | Label-ar | Label-en |
|---|---|---|---|---|---|---|
| dimensionPk | number | yes (system) | — | primary key | معرّف البُعد | Dimension id |
| code | text | yes | unique | e.g. `PROJECT`, `INVESTOR` | رمز البُعد | Dimension code |
| nameAr | text | yes | — | — | الاسم (عربي) | Name (Arabic) |
| nameEn | text | yes | — | — | الاسم (إنجليزي) | Name (English) |
| isActiveFl | flag | yes | — | — | نشط | Active |
| createdBy, createdAt, updatedBy, updatedAt | system | yes | — | standard audit fields | — | — |


### ENT-FIN-003 — قيمة البُعد / DimensionValue
| Kind | Ownership | Business number | Operations | Cross-module | Source |
|---|---|---|---|---|---|
| lookup | PRIVATE | No | create, read, search, deactivate | none | general-accounting-system-plan-en.md §4.2 |

| Field | Logical type | Required | Values / source | Notes | Label-ar | Label-en |
|---|---|---|---|---|---|---|
| dimensionValuePk | number | yes (system) | — | primary key | معرّف قيمة البُعد | Dimension value id |
| dimensionId | reference | yes | ENT-FIN-002 | — | البُعد | Dimension |
| code | text | yes | unique within dimensionId [RULE-FIN-002] | — | الرمز | Code |
| nameAr | text | yes | — | — | الاسم (عربي) | Name (Arabic) |
| nameEn | text | yes | — | — | الاسم (إنجليزي) | Name (English) |
| sortOrder | number | yes | — | — | ترتيب العرض | Sort order |
| isActiveFl | flag | yes | — | — | نشط | Active |
| createdBy, createdAt, updatedBy, updatedAt | system | yes | — | standard audit fields | — | — |


### ENT-FIN-004 — رأس قيد اليومية / JournalEntry
| Kind | Ownership | Business number | Operations | Cross-module | Source |
|---|---|---|---|---|---|
| transactional | PRIVATE | **Yes** — `docNo` is exactly the numbered transactional document case [§3.3 test (c)]: system-generated on first save, read-only after, unique per fiscal year. Format `JV-{fiscalYearCode}-{NNNNNN}` (e.g. `JV-2026-000123`), counter scoped per `fiscalYearId` and restarting at `000001` each year, one counter across all journal types, produced by a FIN-local generator in `com.erp.fin` — NOT a "platform numbering engine", which does not exist in this platform and never did | create (4 sources), read, search, reverse | none | general-accounting-system-plan-en.md §7, §8, §9 |

| Field | Logical type | Required | Values / source | Notes | Label-ar | Label-en |
|---|---|---|---|---|---|---|
| journalEntryPk | number | yes (system) | — | primary key | معرّف القيد | Journal entry id |
| docNo | text | yes (system) | FIN-local generator (`com.erp.fin`), format `JV-{fiscalYearCode}-{NNNNNN}`; unique per fiscalYearId (UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO) | read-only after create | رقم المستند | Document number |
| docDate | date | yes | — | — | تاريخ المستند | Document date |
| fiscalYearId | reference | yes | ENT-FIN-007 | — | السنة المالية | Fiscal year |
| periodId | reference | yes | ENT-FIN-008; must be Open at post [RULE-FIN-008] | — | الفترة | Period |
| journalTypeCode | lookup | yes | lookup key `JOURNAL_TYPE` (A6) | classifies the source | نوع اليومية | Journal type |
| statusCode | lookup | yes | lookup key `JOURNAL_STATUS` (A6) | drives A7 lifecycle | الحالة | Status |
| eventReference | text | no | unique when not null [RULE-FIN-004] | idempotency key for event-sourced entries [POL-FIN-012] | مرجع الحدث | Event reference |
| originalEntryId | reference | no | ENT-FIN-004 (self) | set on a reversal entry [POL-FIN-007] | القيد الأصلي | Original entry |
| reversalEntryId | reference | no | ENT-FIN-004 (self) | set on the original once reversed (bidirectional link) | قيد العكس | Reversal entry |
| descriptionAr | text | no | — | — | الوصف (عربي) | Description (Arabic) |
| descriptionEn | text | no | — | — | الوصف (إنجليزي) | Description (English) |
| postedAt | date-time | no (system) | set at post time | — | تاريخ الترحيل | Posted at |
| createdBy, createdAt, updatedBy, updatedAt | system | yes | — | standard audit fields; immutable after posting [POL-FIN-013] | — | — |


### ENT-FIN-005 — سطر قيد اليومية / JournalLine
| Kind | Ownership | Business number | Operations | Cross-module | Source |
|---|---|---|---|---|---|
| transactional | PRIVATE | No | create (with header), read | none | general-accounting-system-plan-en.md §6, §8 |

| Field | Logical type | Required | Values / source | Notes | Label-ar | Label-en |
|---|---|---|---|---|---|---|
| journalLinePk | number | yes (system) | — | primary key | معرّف السطر | Line id |
| journalEntryId | reference | yes | ENT-FIN-004 | — | القيد | Journal entry |
| lineNo | number | yes | — | display/build order | رقم السطر | Line number |
| accountId | reference | yes | ENT-FIN-001; must be leaf+active [RULE-FIN-007] | — | الحساب | Account |
| amount | decimal | yes | always positive [RULE-FIN-006, POL-FIN-005] | `NUMERIC(18,4)` per [KB:erp-domain-standards §6] | المبلغ | Amount |
| directionCode | lookup | yes | lookup key `DEBIT_CREDIT` (A6) | carries the sign, never the amount [POL-FIN-005] | الاتجاه | Direction |
| isRemainderFl | flag | yes | default false | marks the POL-FIN-006 guarantor line, at most one true per compound distribution [RULE-FIN-010] | سطر الباقي | Remainder line |
| descriptionAr | text | no | — | — | الوصف (عربي) | Description (Arabic) |
| descriptionEn | text | no | — | — | الوصف (إنجليزي) | Description (English) |
| createdAt | system | yes | — | lines are written atomically with the header, never edited independently | — | — |


### ENT-FIN-006 — بُعد سطر القيد / JournalLineDimension
| Kind | Ownership | Business number | Operations | Cross-module | Source |
|---|---|---|---|---|---|
| transactional | PRIVATE | No | create (with line), read | none | general-accounting-system-plan-en.md §4.3, §12.11 |

| Field | Logical type | Required | Values / source | Notes | Label-ar | Label-en |
|---|---|---|---|---|---|---|
| journalLineDimensionPk | number | yes (system) | — | primary key | معرّف بُعد السطر | Line dimension id |
| journalLineId | reference | yes | ENT-FIN-005 | — | سطر القيد | Journal line |
| dimensionId | reference | yes | ENT-FIN-002 | — | البُعد | Dimension |
| dimensionValueId | reference | yes | ENT-FIN-003; must belong to dimensionId and be active [RULE-FIN-009] | — | قيمة البُعد | Dimension value |


### ENT-FIN-007 — السنة المالية / FiscalYear
| Kind | Ownership | Business number | Operations | Cross-module | Source |
|---|---|---|---|---|---|
| master | PRIVATE | No | create, read, search | none | general-accounting-system-plan-en.md §10 |

| Field | Logical type | Required | Values / source | Notes | Label-ar | Label-en |
|---|---|---|---|---|---|---|
| fiscalYearPk | number | yes (system) | — | primary key | معرّف السنة المالية | Fiscal year id |
| code | text | yes | unique, e.g. `2026` | — | رمز السنة | Year code |
| startDate | date | yes | — | — | تاريخ البداية | Start date |
| endDate | date | yes | — | — | تاريخ النهاية | End date |
| statusCode | lookup | yes | lookup key `FISCAL_YEAR_STATUS` (A6) | — | الحالة | Status |
| isActiveFl | flag | yes | — | — | نشط | Active |
| createdBy, createdAt, updatedBy, updatedAt | system | yes | — | standard audit fields | — | — |


### ENT-FIN-008 — الفترة المحاسبية / FiscalPeriod
| Kind | Ownership | Business number | Operations | Cross-module | Source |
|---|---|---|---|---|---|
| master | PRIVATE | No | create, read, search, open, soft-close, hard-close | none | general-accounting-system-plan-en.md §10 |

| Field | Logical type | Required | Values / source | Notes | Label-ar | Label-en |
|---|---|---|---|---|---|---|
| fiscalPeriodPk | number | yes (system) | — | primary key | معرّف الفترة | Period id |
| fiscalYearId | reference | yes | ENT-FIN-007 | — | السنة المالية | Fiscal year |
| periodNo | number | yes | — | 1..N within the year | رقم الفترة | Period number |
| nameAr | text | yes | — | — | الاسم (عربي) | Name (Arabic) |
| nameEn | text | yes | — | — | الاسم (إنجليزي) | Name (English) |
| startDate | date | yes | — | — | تاريخ البداية | Start date |
| endDate | date | yes | — | — | تاريخ النهاية | End date |
| statusCode | lookup | yes | lookup key `PERIOD_STATE` (A6) | drives A7 lifecycle | الحالة | Status |
| closedBy | text | no | — | set at hard-close, by the approver [POL-FIN-016] | أُغلقت بواسطة | Closed by |
| closedAt | date-time | no | — | — | تاريخ الإغلاق | Closed at |
| createdBy, createdAt, updatedBy, updatedAt | system | yes | — | standard audit fields | — | — |


### ENT-FIN-009 — قاعدة نوع الحدث / EventTypeRule
| Kind | Ownership | Business number | Operations | Cross-module | Source |
|---|---|---|---|---|---|
| config | PRIVATE | No | create, read, search, update, deactivate | none | general-accounting-system-plan-en.md §6 |

| Field | Logical type | Required | Values / source | Notes | Label-ar | Label-en |
|---|---|---|---|---|---|---|
| eventTypeRuleId | number | yes (system) | — | primary key | معرّف قاعدة الحدث | Event type rule id |
| eventTypeCode | lookup | yes | lookup key `ACCOUNTING_EVENT_TYPE` (A6); unique | one rule per event type [§6.4] | نوع الحدث | Event type |
| nameAr | text | yes | — | — | الاسم (عربي) | Name (Arabic) |
| nameEn | text | yes | — | — | الاسم (إنجليزي) | Name (English) |
| isActiveFl | flag | yes | — | — | نشط | Active |
| createdBy, createdAt, updatedBy, updatedAt | system | yes | — | standard audit fields | — | — |


### ENT-FIN-011 — قالب متكرر/عكسي / RecurringTemplate
| Kind | Ownership | Business number | Operations | Cross-module | Source |
|---|---|---|---|---|---|
| config | PRIVATE | No | create, read, search, update, deactivate | none | general-accounting-system-plan-en.md §7.3 |

| Field | Logical type | Required | Values / source | Notes | Label-ar | Label-en |
|---|---|---|---|---|---|---|
| recurringTemplateId | number | yes (system) | — | primary key | معرّف القالب | Template id |
| nameAr | text | yes | — | — | الاسم (عربي) | Name (Arabic) |
| nameEn | text | yes | — | — | الاسم (إنجليزي) | Name (English) |
| scheduleTypeCode | lookup | yes | lookup key `RECURRING_SCHEDULE_TYPE` (A6) | recurring / reversing | نوع الجدولة | Schedule type |
| frequencyCode | lookup | yes (when scheduleTypeCode=RECURRING) | lookup key `RECURRING_FREQUENCY` (A6) | not applicable to a pure reversing template | التكرار | Frequency |
| startDate | date | yes | — | — | تاريخ البدء | Start date |
| nextRunDate | date | yes (system-maintained) | — | advances after each run | تاريخ التشغيل القادم | Next run date |
| endDate | date | no | — | — | تاريخ الانتهاء | End date |
| isActiveFl | flag | yes | — | — | نشط | Active |
| createdBy, createdAt, updatedBy, updatedAt | system | yes | — | standard audit fields | — | — |


### ENT-FIN-012 — سطر القالب المتكرر / RecurringTemplateLine
| Kind | Ownership | Business number | Operations | Cross-module | Source |
|---|---|---|---|---|---|
| config | PRIVATE | No | create (with template), read, update, delete | none | general-accounting-system-plan-en.md §7.3 |

| Field | Logical type | Required | Values / source | Notes | Label-ar | Label-en |
|---|---|---|---|---|---|---|
| recurringTemplateLineId | number | yes (system) | — | primary key | معرّف سطر القالب | Template line id |
| recurringTemplateId | reference | yes | ENT-FIN-011 | — | القالب | Template |
| lineNo | number | yes | — | — | رقم السطر | Line number |
| accountId | reference | yes | ENT-FIN-001; must be leaf+active at run time [RULE-FIN-007, reused] | — | الحساب | Account |
| amount | decimal | yes | positive [RULE-FIN-006, reused] | — | المبلغ | Amount |
| directionCode | lookup | yes | lookup key `DEBIT_CREDIT` (A6) | — | الاتجاه | Direction |
| dimensionValueId | reference | no | ENT-FIN-003 | single optional dimension per line — a simplification vs the header's multi-dimension model (§9.3.3, AUTO-DECISION) | قيمة البُعد | Dimension value |
| createdAt | system | yes | — | — | — | — |


### ENT-FIN-013 — قاعدة توزيع / AllocationRule
| Kind | Ownership | Business number | Operations | Cross-module | Source |
|---|---|---|---|---|---|
| config | PRIVATE | No | create, read, search, update, deactivate | none | general-accounting-system-plan-en.md §7.4 |

| Field | Logical type | Required | Values / source | Notes | Label-ar | Label-en |
|---|---|---|---|---|---|---|
| allocationRuleId | number | yes (system) | — | primary key | معرّف قاعدة التوزيع | Allocation rule id |
| nameAr | text | yes | — | — | الاسم (عربي) | Name (Arabic) |
| nameEn | text | yes | — | — | الاسم (إنجليزي) | Name (English) |
| sourceAccountId | reference | yes | ENT-FIN-001 | the balance being distributed | الحساب المصدر | Source account |
| isActiveFl | flag | yes | — | — | نشط | Active |
| createdBy, createdAt, updatedBy, updatedAt | system | yes | — | standard audit fields | — | — |


### ENT-FIN-014 — هدف التوزيع / AllocationTarget
| Kind | Ownership | Business number | Operations | Cross-module | Source |
|---|---|---|---|---|---|
| config | PRIVATE | No | create (with rule), read, update, delete | none | general-accounting-system-plan-en.md §7.4 |

| Field | Logical type | Required | Values / source | Notes | Label-ar | Label-en |
|---|---|---|---|---|---|---|
| allocationTargetId | number | yes (system) | — | primary key | معرّف هدف التوزيع | Allocation target id |
| allocationRuleId | reference | yes | ENT-FIN-013 | — | قاعدة التوزيع | Allocation rule |
| lineNo | number | yes | — | — | رقم السطر | Line number |
| targetAccountId | reference | yes | ENT-FIN-001 | — | الحساب الهدف | Target account |
| dimensionValueId | reference | no | ENT-FIN-003 | single optional dimension (same simplification as ENT-FIN-012) | قيمة البُعد | Dimension value |
| distributionTypeCode | lookup | yes | lookup key `DISTRIBUTION_TYPE` (A6) | fixed / percentage / remainder [§6.3, reused for allocations §7.4] | نوع التوزيع | Distribution type |
| distributionValue | decimal | no | required unless distributionTypeCode=REMAINDER | — | قيمة التوزيع | Distribution value |
| isRemainderFl | flag | yes | default false | exactly one true per rule when any target is PERCENTAGE [RULE-FIN-003, reused] | هدف الباقي | Remainder target |


### REQ-FIN-001 — إنشاء حساب / Create an account
Pattern    : event
Statement  : When a finance administrator creates an account, the system shall record its code, bilingual name, type and nature within the chart hierarchy.
Traces     : US-FIN-001
Entities   : ENT-FIN-001
Rationale  : the reference structure that determines where values are recorded
Source     : general-accounting-system-plan-en.md §4.1-§4.2
Priority   : HIGH

#### AC-FIN-001 — [REQ-FIN-001]
Given a unique code, bilingual name, account type and nature, optionally a parent account
When a finance administrator submits the account form
Then the system creates the account


### REQ-FIN-002 — رفض ترحيل مباشر لحساب له أبناء / Reject direct-posting on an account with children
Pattern    : unwanted
Statement  : If an account has any child account, then the system shall prevent it from being marked as accepting direct posting.
Traces     : US-FIN-001
Entities   : ENT-FIN-001
Rationale  : RULE-FIN-001; POL-FIN-003 — only leaves accept postings
Source     : general-accounting-system-plan-en.md §4.2
Priority   : HIGH

#### AC-FIN-002 — [REQ-FIN-002]
Given an account with at least one child account
When an administrator attempts to mark it as accepting direct posting (isLeafFl=true)
Then the system rejects the change


### REQ-FIN-003 — تعطيل حساب / Deactivate an account
Pattern    : event
Statement  : When a finance administrator deactivates an account, the system shall block any future posting to it.
Traces     : US-FIN-001
Entities   : ENT-FIN-001
Rationale  : POL-FIN-003
Source     : general-accounting-system-plan-en.md §4.2
Priority   : MEDIUM

#### AC-FIN-003 — [REQ-FIN-003]
Given an active account
When an administrator deactivates it
Then the system sets isActiveFl=false and REQ-FIN-019 subsequently rejects any posting to it


### REQ-FIN-004 — إنشاء بُعد / Create a dimension
Pattern    : event
Statement  : When a finance administrator creates a dimension, the system shall record it as an active dimension available for account combinations.
Traces     : US-FIN-002
Entities   : ENT-FIN-002
Rationale  : dimensions defined as data, a governing design constraint
Source     : general-accounting-system-plan-en.md §4.2-§4.3
Priority   : HIGH

#### AC-FIN-004 — [REQ-FIN-004]
Given a unique code and bilingual name
When an administrator submits the dimension form
Then the system creates an active Dimension


### REQ-FIN-005 — إنشاء قيمة بُعد / Create a dimension value
Pattern    : event
Statement  : When a finance administrator creates a value under a dimension, the system shall record its code, bilingual name and sort order.
Traces     : US-FIN-002
Entities   : ENT-FIN-003
Rationale  : §4.2
Source     : general-accounting-system-plan-en.md §4.2
Priority   : HIGH

#### AC-FIN-005 — [REQ-FIN-005]
Given a selected dimension and a code not yet used under it
When an administrator submits the value form
Then the system creates the DimensionValue


### REQ-FIN-006 — رفض رمز مكرر ضمن البُعد / Reject a duplicate code within a dimension
Pattern    : unwanted
Statement  : If two dimension values under the same dimension share the same code, then the system shall reject the second.
Traces     : US-FIN-002
Entities   : ENT-FIN-003
Rationale  : RULE-FIN-002
Source     : general-accounting-system-plan-en.md §4.2
Priority   : MEDIUM

#### AC-FIN-006 — [REQ-FIN-006]
Given a dimension already holding a value with code "NORTH"
When a second value with code "NORTH" is created under the same dimension
Then the system rejects it


### REQ-FIN-007 — إنشاء قاعدة نوع حدث / Create an event-type rule
Pattern    : event
Statement  : When a finance administrator creates a rule for an event type, the system shall record it as the single active rule for that type.
Traces     : US-FIN-003
Entities   : ENT-FIN-009
Rationale  : §6.4 — one rule per event type
Source     : general-accounting-system-plan-en.md §6.1, §6.4
Priority   : HIGH

#### AC-FIN-007 — [REQ-FIN-007]
Given an event type registered in MDL with no existing rule
When an administrator creates a rule for it
Then the system creates an active EventTypeRule


### REQ-FIN-008 — إضافة سطر قاعدة / Add a rule line
Pattern    : event
Statement  : When a finance administrator adds a line to an event-type rule, the system shall record its account derivation, amount source and direction as three separate references.
Traces     : US-FIN-003
Entities   : ENT-FIN-010
Rationale  : POL-FIN-014; §6.2 separation of (a) and (b) is deliberate
Source     : general-accounting-system-plan-en.md §6.2
Priority   : HIGH

#### AC-FIN-008 — [REQ-FIN-008]
Given an event-type rule
When an administrator adds a line with an account-derivation spec, an amount-source spec and a direction
Then the system creates the RuleLine


### REQ-FIN-009 — رفض عدد غير صحيح من سطور الباقي / Reject an incorrect remainder-line count
Pattern    : unwanted
Statement  : If an event-type rule's lines include any percentage-distribution line, then the system shall require exactly one line marked as the remainder.
Traces     : US-FIN-003
Entities   : ENT-FIN-010
Rationale  : RULE-FIN-003; POL-FIN-006
Source     : general-accounting-system-plan-en.md §6.3, §12.6
Priority   : HIGH

#### AC-FIN-009 — [REQ-FIN-009]
Given a rule with two PERCENTAGE-distribution lines and zero lines marked remainder
When an administrator attempts to save the rule
Then the system rejects it until exactly one line is marked remainder


### REQ-FIN-010 — بناء قيد من حدث / Build an entry from an event
Pattern    : event
Statement  : When a canonical accounting event arrives, the system shall build a journal entry from it using its event type's active rule.
Traces     : US-FIN-004
Entities   : ENT-FIN-004, ENT-FIN-005, ENT-FIN-009, ENT-FIN-010
Rationale  : POL-FIN-014, POL-FIN-020 — no host-specific logic, only rule + event data
Source     : general-accounting-system-plan-en.md §7.1
Priority   : HIGH

#### AC-FIN-010 — [REQ-FIN-010]
Given a canonical event with a type that has an active rule
When the event arrives
Then the system builds a DRAFT journal entry whose lines follow the rule, then proceeds to REQ-FIN-017 (validate + post)


### REQ-FIN-011 — رفض مرجع حدث مكرر / Reject a duplicate event reference
Pattern    : unwanted
Statement  : If a canonical event's reference has already produced a posted entry, then the system shall reject a second entry for the same reference.
Traces     : US-FIN-004
Entities   : ENT-FIN-004
Rationale  : RULE-FIN-004; POL-FIN-012 — boundary idempotency is assumed, a repeat is a defect
Source     : general-accounting-system-plan-en.md §12.12
Priority   : HIGH

#### AC-FIN-011 — [REQ-FIN-011]
Given a POSTED entry with eventReference "EVT-1001"
When another event with reference "EVT-1001" arrives
Then the system rejects building a second entry for it


### REQ-FIN-012 — امتصاص فرق التقريب عبر سطر الباقي / Absorb the rounding difference via the remainder line
Pattern    : event
Statement  : When a rule's lines produce a compound or percentage distribution, the system shall compute the remainder line's amount as the original amount minus the sum of every other line, after each percentage line is rounded to the smallest currency unit.
Traces     : US-FIN-004
Entities   : ENT-FIN-005, ENT-FIN-010
Rationale  : RULE-FIN-010; POL-FIN-006
Source     : general-accounting-system-plan-en.md §6.3
Priority   : HIGH

#### AC-FIN-012 — [REQ-FIN-012]
Given a rule distributing 100.00 as 33% + 33% + remainder
When the entry is built
Then the two percentage lines total 66.00 (each rounded to the smallest currency unit) and the remainder line is exactly 34.00, so the entry balances exactly


### REQ-FIN-013 — رفض حدث بلا قاعدة نشطة / Reject an event with no active rule
Pattern    : unwanted
Statement  : If an event's type has no active event-type rule, then the system shall reject building an entry for it.
Traces     : US-FIN-004
Entities   : ENT-FIN-009
Rationale  : the engine applies rules, it never guesses
Source     : general-accounting-system-plan-en.md §6.1
Priority   : MEDIUM

#### AC-FIN-013 — [REQ-FIN-013]
Given an event type with no active EventTypeRule
When an event of that type arrives
Then the system rejects building an entry and records the failure for operator follow-up


### REQ-FIN-014 — إدخال يدوي / Create a manual journal entry
Pattern    : event
Statement  : When an accountant creates a manual journal entry with its lines, the system shall build it exactly as any other source, then proceed through the same validation and posting path.
Traces     : US-FIN-005
Entities   : ENT-FIN-004, ENT-FIN-005
Rationale  : POL-FIN-001, POL-FIN-018 — one unified rule for every source
Source     : general-accounting-system-plan-en.md §7.2
Priority   : HIGH

#### AC-FIN-014 — [REQ-FIN-014]
Given a fiscal period, at least two balanced lines with leaf active accounts
When an accountant submits the manual entry form
Then the system builds a DRAFT entry and proceeds to REQ-FIN-017


### REQ-FIN-015 — عرض تفاصيل الفشل قبل الترحيل / Show validation failures before posting
Pattern    : event
Statement  : When a manual entry fails automatic validation, the system shall return every failing check to the accountant without posting anything.
Traces     : US-FIN-005
Entities   : ENT-FIN-004
Rationale  : usable error feedback for the one path with a live human editor
Source     : general-accounting-system-plan-en.md §8.1
Priority   : MEDIUM

#### AC-FIN-015 — [REQ-FIN-015]
Given a manual entry with two failing validations
When the accountant submits it
Then the system returns both failures and posts nothing


### REQ-FIN-016 — إبقاء السجل دون حذف فعلي / Retain records without hard deletion
Pattern    : ubiquitous
Statement  : The system shall retain every posted journal entry's record permanently, never hard-deleting it.
Traces     : US-FIN-008
Entities   : ENT-FIN-004
Rationale  : POL-FIN-013
Source     : general-accounting-system-plan-en.md §12.13
Priority   : HIGH

#### AC-FIN-016 — [REQ-FIN-016]
Given a POSTED journal entry
When any attempt is made to delete it
Then the system rejects the delete — the only path that changes its effect is a reversal (REQ-FIN-028)


### REQ-FIN-017 — تحقق آلي وترحيل مباشر موحّد / Unified automatic validation and direct posting
Pattern    : event
Statement  : When any journal entry, regardless of source, passes automatic validation, the system shall post it directly with no separate human approval step.
Traces     : US-FIN-004, US-FIN-005, US-FIN-006, US-FIN-007, US-FIN-009
Entities   : ENT-FIN-004
Rationale  : POL-FIN-001, POL-FIN-003, POL-FIN-004, POL-FIN-018 — the single unified rule
Source     : general-accounting-system-plan-en.md §8.1
Priority   : HIGH

#### AC-FIN-017 — [REQ-FIN-017]
Given a DRAFT entry that passes every check in REQ-FIN-018..021
When validation completes
Then the system sets statusCode=POSTED, postedAt=now(), and the entry becomes fully locked (no edit, no delete — REQ-FIN-034-equivalent lock, POL-FIN-013)


### REQ-FIN-018 — رفض عدم توازن القيد / Reject an unbalanced entry
Pattern    : unwanted
Statement  : If a journal entry's total debits do not equal its total credits, then the system shall reject posting.
Traces     : US-FIN-004, US-FIN-005
Entities   : ENT-FIN-004, ENT-FIN-005
Rationale  : RULE-FIN-006; POL-FIN-001
Source     : general-accounting-system-plan-en.md §12.1
Priority   : HIGH

#### AC-FIN-018 — [REQ-FIN-018]
Given a DRAFT entry with debit lines totaling 100.00 and credit lines totaling 99.99
When the system validates it
Then it rejects posting with the imbalance amount shown


### REQ-FIN-019 — رفض حساب غير مؤهَّل للترحيل / Reject a non-postable account
Pattern    : unwanted
Statement  : If a journal line targets an account that is not a leaf, not active, or not marked as accepting direct posting, then the system shall reject posting.
Traces     : US-FIN-004, US-FIN-005
Entities   : ENT-FIN-001, ENT-FIN-005
Rationale  : RULE-FIN-007; POL-FIN-003
Source     : general-accounting-system-plan-en.md §12.3
Priority   : HIGH

#### AC-FIN-019 — [REQ-FIN-019]
Given a DRAFT entry with a line targeting a non-leaf (rollup) account
When the system validates it
Then it rejects posting, naming the offending line


### REQ-FIN-020 — رفض فترة مغلقة عند الترحيل / Reject a closed period at post time
Pattern    : unwanted
Statement  : If a journal entry's period is not Open at the moment of posting, then the system shall reject posting, even if it was Open when the entry was built.
Traces     : US-FIN-004, US-FIN-005
Entities   : ENT-FIN-004, ENT-FIN-008
Rationale  : RULE-FIN-008; POL-FIN-004 — checked at post time, not build time
Source     : general-accounting-system-plan-en.md §12.4
Priority   : HIGH

#### AC-FIN-020 — [REQ-FIN-020]
Given a DRAFT entry whose period was Open when built but is now Hard Closed
When the system attempts to post it
Then it rejects posting citing the period's current state


### REQ-FIN-021 — رفض قيمة بُعد غير صالحة / Reject an invalid dimension value
Pattern    : unwanted
Statement  : If a journal line cites a dimension value that does not belong to its stated dimension or is inactive, then the system shall reject posting.
Traces     : US-FIN-004, US-FIN-005
Entities   : ENT-FIN-006
Rationale  : RULE-FIN-009
Source     : general-accounting-system-plan-en.md §8.1 ("dimension valid")
Priority   : MEDIUM

#### AC-FIN-021 — [REQ-FIN-021]
Given a DRAFT entry line citing an inactive DimensionValue
When the system validates it
Then it rejects posting, naming the offending line and dimension


### REQ-FIN-022 — إنشاء قالب متكرر/عكسي / Create a recurring or reversing template
Pattern    : event
Statement  : When an accountant creates a recurring or reversing template with its lines, the system shall record its schedule and store it for future runs.
Traces     : US-FIN-006
Entities   : ENT-FIN-011, ENT-FIN-012
Rationale  : §7.3 — templates defined once as data
Source     : general-accounting-system-plan-en.md §7.3
Priority   : MEDIUM

#### AC-FIN-022 — [REQ-FIN-022]
Given a schedule type, a frequency (for recurring) and at least two balanced lines
When an accountant submits the template form
Then the system creates the active RecurringTemplate


### REQ-FIN-023 — تشغيل قالب متكرر / Run a recurring template on schedule
Pattern    : event
Statement  : When a recurring template's next run date arrives, the system shall generate and post a journal entry from it exactly as REQ-FIN-017 describes.
Traces     : US-FIN-006
Entities   : ENT-FIN-004, ENT-FIN-011
Rationale  : §7.3
Source     : general-accounting-system-plan-en.md §7.3
Priority   : MEDIUM

#### AC-FIN-023 — [REQ-FIN-023]
Given an active recurring template whose nextRunDate is today
When the scheduled run executes
Then the system builds and posts one entry from the template and advances nextRunDate per its frequency


### REQ-FIN-024 — عكس تلقائي في الفترة التالية / Automatic reversal in the next period
Pattern    : event
Statement  : When a reversing template's entry posts, the system shall automatically build and post its exact reversal in the next period.
Traces     : US-FIN-006
Entities   : ENT-FIN-004
Rationale  : §7.3; POL-FIN-007
Source     : general-accounting-system-plan-en.md §7.3
Priority   : MEDIUM

#### AC-FIN-024 — [REQ-FIN-024]
Given a reversing-type template entry posted in period N
When period N+1 opens (or immediately, per the template's own trigger)
Then the system posts a linked reversal entry in period N+1


### REQ-FIN-025 — إنشاء قاعدة توزيع / Create an allocation rule
Pattern    : event
Statement  : When an accountant creates an allocation rule with its targets, the system shall record how the source account's balance distributes across them.
Traces     : US-FIN-007
Entities   : ENT-FIN-013, ENT-FIN-014
Rationale  : §7.4
Source     : general-accounting-system-plan-en.md §7.4
Priority   : MEDIUM

#### AC-FIN-025 — [REQ-FIN-025]
Given a source account and at least one target with a distribution type
When an accountant submits the allocation rule form
Then the system creates the active AllocationRule


### REQ-FIN-026 — تشغيل قاعدة توزيع / Run an allocation rule
Pattern    : event
Statement  : When an allocation rule runs, the system shall distribute the source account's current balance across its targets per their distribution type, applying the same remainder guarantee as any compound distribution.
Traces     : US-FIN-007
Entities   : ENT-FIN-004, ENT-FIN-013, ENT-FIN-014
Rationale  : §7.4; RULE-FIN-010 reused
Source     : general-accounting-system-plan-en.md §7.4, §6.3
Priority   : MEDIUM

#### AC-FIN-026 — [REQ-FIN-026]
Given an allocation rule with two PERCENTAGE targets and one REMAINDER target, and a source balance of 1,000.00
When an accountant runs it
Then the system builds and posts one entry whose target lines sum exactly to 1,000.00, the remainder target absorbing the rounding difference


### REQ-FIN-027 — عرض/بحث قيود اليومية / Search and view journal entries
Pattern    : event
Statement  : When an accountant searches journal entries, the system shall return matching entries with their full status detail, unmodified.
Traces     : US-FIN-008
Entities   : ENT-FIN-004
Rationale  : POL-FIN-013
Source     : general-accounting-system-plan-en.md §8.4
Priority   : HIGH

#### AC-FIN-027 — [REQ-FIN-027]
Given entries across several statuses and periods
When an accountant filters by period and status
Then the system returns exactly the matching entries, unmodified


### REQ-FIN-028 — عكس قيد / Reverse a posted entry
Pattern    : event
Statement  : When an accountant reverses a posted entry, the system shall build and post a new entry mirroring it line-for-line with opposite directions and equal amounts, linked bidirectionally to the original.
Traces     : US-FIN-009
Entities   : ENT-FIN-004, ENT-FIN-005
Rationale  : RULE-FIN-011; POL-FIN-007
Source     : general-accounting-system-plan-en.md §9
Priority   : HIGH

#### AC-FIN-028 — [REQ-FIN-028]
Given a POSTED entry with three lines
When an accountant reverses it
Then the system posts a new entry with the same three lines' amounts and opposite directions, and both entries reference each other (originalEntryId/reversalEntryId)


### REQ-FIN-029 — ترحيل العكس في الفترة الحالية عند إغلاق الأصل / Post the reversal in the current period when the original's period is closed
Pattern    : event
Statement  : When the original entry's period is closed, the system shall post its reversal into the current open period instead.
Traces     : US-FIN-009
Entities   : ENT-FIN-004, ENT-FIN-008
Rationale  : RULE-FIN-012
Source     : general-accounting-system-plan-en.md §9
Priority   : MEDIUM

#### AC-FIN-029 — [REQ-FIN-029]
Given a POSTED entry whose period is now Hard Closed
When an accountant reverses it
Then the system posts the reversal into the current open period, not the closed one


### REQ-FIN-030 — رفض عكس قيد غير مُرحَّل / Reject reversing a non-posted entry
Pattern    : unwanted
Statement  : If an entry is not POSTED, then the system shall reject a reverse action on it.
Traces     : US-FIN-009
Entities   : ENT-FIN-004
Rationale  : RULE-FIN-013 — prevents double-reversal (an entry that already carries a reversal link) or reversing a DRAFT row
Source     : general-accounting-system-plan-en.md §9
Priority   : MEDIUM

#### AC-FIN-030 — [REQ-FIN-030]
Given an entry already reversed once (it stays POSTED and carries a reversalEntryId link to its reversal)
When an accountant attempts to reverse it again
Then the system rejects the action


### REQ-FIN-031 — إنشاء سنة مالية وفتراتها / Create a fiscal year with its periods
Pattern    : event
Statement  : When a finance administrator creates a fiscal year, the system shall generate its periods in the Open state per the chosen calendar. As built: a `periodCount` of twelve over a whole year generates the twelve calendar months, named from the JDK's CLDR month names in Arabic and English; any other count falls back to an even day split named "الفترة N" / "Period N".
Traces     : US-FIN-010
Entities   : ENT-FIN-007, ENT-FIN-008
Rationale  : §10.1-§10.2
Source     : general-accounting-system-plan-en.md §10.1, §10.4
Priority   : HIGH

#### AC-FIN-031 — [REQ-FIN-031]
Given a start date, end date and period count
When an administrator creates the fiscal year
Then the system creates the year and its periods, each initially Open


### REQ-FIN-032 — فتح فترة / Open a period
Pattern    : event
Statement  : When a finance administrator opens a period, the system shall allow normal posting into it.
Traces     : US-FIN-010
Entities   : ENT-FIN-008
Rationale  : §10.2
Source     : general-accounting-system-plan-en.md §10.2
Priority   : MEDIUM

#### AC-FIN-032 — [REQ-FIN-032]
Given a period not currently Open
When an administrator reopens it (soft-closed only, per §10.2 "re-openable")
Then the system sets statusCode=OPEN


### REQ-FIN-033 — إغلاق ناعم لفترة / Soft-close a period
Pattern    : event
Statement  : When a finance administrator soft-closes a period, the system shall block normal posting into it while still allowing authorized adjustments.
Traces     : US-FIN-010
Entities   : ENT-FIN-008
Rationale  : §10.2
Source     : general-accounting-system-plan-en.md §10.2
Priority   : MEDIUM

#### AC-FIN-033 — [REQ-FIN-033]
Given an Open period
When an administrator soft-closes it
Then the system sets statusCode=SOFT_CLOSE and REQ-FIN-020 rejects normal (non-adjustment) postings to it thereafter


### REQ-FIN-034 — إغلاق صارم لفترة / Hard-close a period
Pattern    : event
Statement  : When an authorized approver hard-closes a period, the system shall permanently block any posting into it.
Traces     : US-FIN-010, US-FIN-011
Entities   : ENT-FIN-008
Rationale  : §10.2-§10.3
Source     : general-accounting-system-plan-en.md §10.2, §10.3
Priority   : HIGH

#### AC-FIN-034 — [REQ-FIN-034]
Given a Soft Closed period
When an authorized approver hard-closes it
Then the system sets statusCode=HARD_CLOSE, closedBy and closedAt, and the period becomes permanently not re-openable


### REQ-FIN-035 — رفض إعادة فتح فترة مغلقة صارمًا / Reject reopening a hard-closed period
Pattern    : unwanted
Statement  : If a period is Hard Closed, then the system shall reject any attempt to reopen it.
Traces     : US-FIN-010
Entities   : ENT-FIN-008
Rationale  : RULE-FIN-014 — "not re-openable" per §10.2
Source     : general-accounting-system-plan-en.md §10.2
Priority   : MEDIUM

#### AC-FIN-035 — [REQ-FIN-035]
Given a Hard Closed period
When an administrator attempts to reopen it
Then the system rejects the action


### REQ-FIN-036 — تشغيل إقفال نهاية السنة / Run year-end close
Pattern    : event
Statement  : When an authorized approver runs year-end close for a fiscal year whose periods are all Hard Closed, the system shall generate a balanced closing entry and a balanced opening entry for the next year.
Traces     : US-FIN-010
Entities   : ENT-FIN-004, ENT-FIN-007
Rationale  : POL-FIN-010
Source     : general-accounting-system-plan-en.md §10.4
Priority   : HIGH

#### AC-FIN-036 — [REQ-FIN-036]
Given a fiscal year with every period Hard Closed
When an approver runs year-end close
Then the system posts a closing entry (result accounts → Retained Earnings) and generates the next year's opening entry from the resulting balance-sheet balances, both entries individually balanced


### REQ-FIN-037 — تسجيل اعتماد إغلاق الفترة / Record period-close approval distinctly
Pattern    : event
Statement  : When an authorized approver closes a period, the system shall record that approval as a distinct act from any entry creation in that period.
Traces     : US-FIN-011
Entities   : ENT-FIN-008
Rationale  : §8.2, §10.3 — the single human control point
Source     : general-accounting-system-plan-en.md §8.2, §10.3
Priority   : HIGH

#### AC-FIN-037 — [REQ-FIN-037]
Given a period ready to hard-close
When the approval is recorded
Then closedBy and closedAt are set to the approving principal and moment, distinct from any entry's createdBy in that period


### REQ-FIN-038 — فصل صلاحية الاعتماد عن صلاحية الإنشاء / Separate the approval permission from the creation permission
Pattern    : ubiquitous
Statement  : The system shall gate the period-close-approval action behind a permission distinct from the journal-entry-creation permission, enforced by the Security module.
Traces     : US-FIN-011
Entities   : ENT-FIN-008
Rationale  : POL-FIN-016; RULE-FIN-015
Source     : general-accounting-system-plan-en.md §2.2, §8.2, §10.3
Priority   : HIGH

#### AC-FIN-038 — [REQ-FIN-038]
Given a role holding only the entry-creation permission
When that role's user attempts the period-close-approval action
Then the system denies it (the CORE interceptor, per SEC's own mechanism)


### REQ-FIN-039 — دفتر الحساب المُشتق حيًا / Live-derived account ledger
Pattern    : event
Statement  : When an accountant opens the account ledger for an account and period range, the system shall compute it from POSTED lines at that moment.
Traces     : US-FIN-012
Entities   : ENT-FIN-005
Rationale  : POL-FIN-009, POL-FIN-011
Source     : general-accounting-system-plan-en.md §11
Priority   : HIGH

#### AC-FIN-039 — [REQ-FIN-039]
Given an account with posted lines across two periods
When the ledger is opened for that range
Then the system computes the running balance live from those lines, never from a stored column


### REQ-FIN-040 — ميزان مراجعة مُشتق حيًا ومتوازن دائمًا / Live-derived, always-balanced trial balance
Pattern    : event
Statement  : When a financial controller opens the trial balance for a period, the system shall compute it from POSTED lines such that total debit balances equal total credit balances.
Traces     : US-FIN-013
Entities   : ENT-FIN-005
Rationale  : POL-FIN-008, POL-FIN-009
Source     : general-accounting-system-plan-en.md §11
Priority   : HIGH

#### AC-FIN-040 — [REQ-FIN-040]
Given any set of POSTED entries in a period
When the trial balance is generated
Then its total debit balances equal its total credit balances exactly


### REQ-FIN-041 — ميزانية عمومية مع استمرارية / Balance sheet with continuity
Pattern    : event
Statement  : When a financial controller opens the balance sheet, the system shall compute balance-sheet account balances from POSTED lines, consistent with the prior year's closing balances as this year's opening balances.
Traces     : US-FIN-014
Entities   : ENT-FIN-005
Rationale  : POL-FIN-002, POL-FIN-009, POL-FIN-010
Source     : general-accounting-system-plan-en.md §11
Priority   : HIGH

#### AC-FIN-041 — [REQ-FIN-041]
Given a completed year-end close (REQ-FIN-036)
When the new year's balance sheet is opened
Then its opening balances equal the prior year's closing balances for every balance-sheet account


### REQ-FIN-042 — قائمة دخل تُفتح بصفر / Income statement opening at zero
Pattern    : event
Statement  : When a financial controller opens the income statement for a new fiscal year, the system shall compute result-account balances starting from zero for that year.
Traces     : US-FIN-015
Entities   : ENT-FIN-005
Rationale  : POL-FIN-002, POL-FIN-010
Source     : general-accounting-system-plan-en.md §11
Priority   : HIGH

#### AC-FIN-042 — [REQ-FIN-042]
Given a completed year-end close
When the new year's income statement is opened before any posting
Then every revenue/expense account shows a zero balance


### REQ-FIN-043 — تقارير أبعاد دون تكرار الحسابات / Dimension reports without duplicating accounts
Pattern    : event
Statement  : When a financial controller opens a dimension report, the system shall aggregate POSTED lines by the full account-and-dimension combination, never by the base account alone when a dimension is in play.
Traces     : US-FIN-016
Entities   : ENT-FIN-005, ENT-FIN-006
Rationale  : POL-FIN-011
Source     : general-accounting-system-plan-en.md §11, §12.11
Priority   : MEDIUM

#### AC-FIN-043 — [REQ-FIN-043]
Given postings to one account under two different PROJECT dimension values
When the dimension report is generated by PROJECT
Then the system shows two separate rows for that account, one per project, not one combined row


### REQ-FIN-044 — تسجيل FIN في وحدة الأمان / Register FIN into the Security module
Pattern    : event
Statement  : When FIN is deployed, the system shall register its module, screens and every action as data into the Security module.
Traces     : US-FIN-017
Entities   : ENT-FIN-004
Rationale  : POL-FIN-015
Source     : general-accounting-system-plan-en.md §2.2, §2.4
Priority   : HIGH

#### AC-FIN-044 — [REQ-FIN-044]
Given SEC v1 is gated and reachable
When FIN's onboarding runs
Then SEC records one ModuleRegistry row for FIN, one ScreenRegistry row per FIN screen, and one ActionRegistry row per action


### REQ-FIN-046 — تتبع نازل من التقرير إلى الحدث / Drill down from a report to its source event
Pattern    : event
Statement  : When a financial controller drills down from a financial-statement line, the system shall lead through the trial balance to the account ledger, to the original entry, to its source event reference.
Traces     : US-FIN-019
Entities   : ENT-FIN-004, ENT-FIN-005
Rationale  : POL-FIN-013
Source     : general-accounting-system-plan-en.md §11
Priority   : MEDIUM

#### AC-FIN-046 — [REQ-FIN-046]
Given a balance-sheet line
When a controller drills into it
Then the system navigates statement line → trial balance row → account ledger → the originating entry → its eventReference (or "manual", "recurring", "allocation" if not event-sourced)


### RULE-FIN-001 — منع ترحيل مباشر لحساب له أبناء / No direct posting on an account with children
Scope      : ENT-FIN-001
Trigger    : on update (isLeafFl)
Statement  : The system shall prevent an account with any child account from being marked as accepting direct posting.
Data source: ENT-FIN-001.parentAccountId, ENT-FIN-001.isLeafFl
Message    : ar: "لا يمكن لحساب له حسابات فرعية أن يقبل ترحيلاً مباشرًا" · en: "An account with sub-accounts cannot accept direct posting"
Traces     : REQ-FIN-002
Source     : general-accounting-system-plan-en.md §4.2


### RULE-FIN-002 — رفض رمز مكرر ضمن البُعد / Reject a duplicate code within a dimension
Scope      : ENT-FIN-003
Trigger    : on create (dimension value)
Statement  : The system shall reject a dimension value whose code already exists under the same dimension.
Data source: ENT-FIN-003.code, ENT-FIN-003.dimensionId
Message    : ar: "هذا الرمز مستخدم بالفعل ضمن هذا البُعد" · en: "This code is already used within this dimension"
Traces     : REQ-FIN-006
Source     : general-accounting-system-plan-en.md §4.2


### RULE-FIN-003 — سطر/هدف باقٍ واحد بالضبط عند التوزيع النسبي / Exactly one remainder line/target under percentage distribution
Scope      : ENT-FIN-010, ENT-FIN-014
Trigger    : on create/update (rule line or allocation target set)
Statement  : The system shall require exactly one line or target marked as the remainder whenever the set forms a compound or percentage distribution — that is, whenever any sibling line or target uses percentage distribution, or any line or target is already marked as the remainder (POL-FIN-006). The marker is `isRemainderFl`; a line or target whose marker disagrees with its own REMAINDER distribution/amount-source type code is rejected, since which line is the remainder decides every other line's amount under RULE-FIN-010.
Data source: ENT-FIN-010.isRemainderFl, ENT-FIN-010.distributionTypeCode, ENT-FIN-014.isRemainderFl, ENT-FIN-014.distributionTypeCode
Message    : ar: "يلزم تحديد سطر باقٍ واحد بالضبط عند وجود توزيع نسبي" · en: "Exactly one remainder line is required when any percentage distribution is present"
Traces     : REQ-FIN-009
Source     : general-accounting-system-plan-en.md §6.3, §12.6


### RULE-FIN-005 — رفض حدث بلا قاعدة نشطة / Reject an event with no active rule
Scope      : ENT-FIN-009
Trigger    : on create (event-sourced entry)
Statement  : The system shall reject building an entry for an event whose type has no active rule.
Data source: ENT-FIN-009.eventTypeCode, ENT-FIN-009.isActiveFl
Message    : ar: "لا توجد قاعدة نشطة لهذا النوع من الأحداث" · en: "No active rule exists for this event type"
Traces     : REQ-FIN-013
Source     : general-accounting-system-plan-en.md §6.1


### RULE-FIN-006 — ثبات تساوي المدين والدائن / Debit=Credit invariant
Scope      : ENT-FIN-004, ENT-FIN-005
Trigger    : on post (any source)
Statement  : The system shall reject posting an entry whose total debits do not equal its total credits to the smallest currency unit.
Data source: ENT-FIN-005.amount, ENT-FIN-005.directionCode
Message    : ar: "القيد غير متوازن — إجمالي المدين لا يساوي إجمالي الدائن" · en: "The entry is unbalanced — total debits do not equal total credits"
Traces     : REQ-FIN-018
Source     : general-accounting-system-plan-en.md §12.1


### RULE-FIN-007 — الترحيل للأوراق النشطة فقط / Posting only to leaf, active accounts
Scope      : ENT-FIN-001, ENT-FIN-005
Trigger    : on post (any source)
Statement  : The system shall reject posting a line targeting an account that is not a leaf, not active, or not marked as accepting direct posting.
Data source: ENT-FIN-001.isLeafFl, ENT-FIN-001.isActiveFl, ENT-FIN-005.accountId
Message    : ar: "الحساب المستهدف لا يقبل ترحيلاً مباشرًا" · en: "The target account does not accept direct posting"
Traces     : REQ-FIN-019
Source     : general-accounting-system-plan-en.md §12.3


### RULE-FIN-009 — صحة قيمة البُعد / Dimension value validity
Scope      : ENT-FIN-006
Trigger    : on post (any source)
Statement  : The system shall reject posting a line whose cited dimension value does not belong to its stated dimension or is inactive.
Data source: ENT-FIN-006.dimensionId, ENT-FIN-006.dimensionValueId, ENT-FIN-003.dimensionId, ENT-FIN-003.isActiveFl
Message    : ar: "قيمة البُعد غير صالحة" · en: "The dimension value is invalid"
Traces     : REQ-FIN-021
Source     : general-accounting-system-plan-en.md §8.1


### RULE-FIN-010 — سطر الباقي يمتص فرق التقريب / The remainder line absorbs the rounding difference
Scope      : ENT-FIN-005, ENT-FIN-010, ENT-FIN-014
Trigger    : on build (compound/percentage distribution — rule engine or allocation)
Statement  : The system shall compute the remainder line's amount per posting side — the total already carried by the opposing side minus the sum of every other line on the remainder line's own side — after every percentage line rounds to the smallest currency unit. The remainder line is never itself computed as a percentage, and its computed amount must be positive (POL-FIN-005): a distribution whose other lines already equal or exceed the opposing side leaves no residue to absorb and is rejected.
Data source: ENT-FIN-005.amount, ENT-FIN-005.isRemainderFl, ENT-FIN-010.distributionTypeCode, ENT-FIN-014.distributionValue
Message    : ar: "سطر الباقي يُحسب كفرق، لا كنسبة" · en: "The remainder line is computed as a difference, never as a percentage"
Traces     : REQ-FIN-012, REQ-FIN-026
Source     : general-accounting-system-plan-en.md §6.3, §12.6


### RULE-FIN-011 — العكس تام ومرتبط / Reversal is exact and linked
Scope      : ENT-FIN-004, ENT-FIN-005
Trigger    : on reverse
Statement  : The system shall build the reversal entry with the same lines as the original, each with the opposite direction and the same amount, and link both entries to each other.
Data source: ENT-FIN-005.amount, ENT-FIN-005.directionCode, ENT-FIN-004.originalEntryId, ENT-FIN-004.reversalEntryId
Message    : ar: "قيد العكس يطابق الأصل بعكس الاتجاه" · en: "The reversal entry mirrors the original with opposite direction"
Traces     : REQ-FIN-028
Source     : general-accounting-system-plan-en.md §12.7, §9


### RULE-FIN-012 — ترحيل العكس بالفترة الحالية عند إغلاق الأصل / Reversal posts to the current period when the original's is closed
Scope      : ENT-FIN-004, ENT-FIN-008
Trigger    : on reverse
Statement  : The system shall post the reversal into the current open period when the original entry's own period is no longer Open.
Data source: ENT-FIN-004.periodId, ENT-FIN-008.statusCode
Message    : ar: "سيُرحَّل قيد العكس في الفترة المفتوحة الحالية" · en: "The reversal will post into the current open period"
Traces     : REQ-FIN-029
Source     : general-accounting-system-plan-en.md §9


### RULE-FIN-013 — رفض عكس قيد غير مُرحَّل / Reject reversing a non-posted entry
Scope      : ENT-FIN-004
Trigger    : on reverse
Statement  : The system shall reject a reverse action on an entry that is not POSTED.
Data source: ENT-FIN-004.statusCode
Message    : ar: "لا يمكن عكس قيد غير مُرحَّل" · en: "A non-posted entry cannot be reversed"
Traces     : REQ-FIN-030
Source     : general-accounting-system-plan-en.md §9


### RULE-FIN-014 — رفض إعادة فتح فترة مغلقة صارمًا / Reject reopening a hard-closed period
Scope      : ENT-FIN-008
Trigger    : on update (period status)
Statement  : The system shall reject any attempt to reopen a Hard Closed period.
Data source: ENT-FIN-008.statusCode
Message    : ar: "الفترة مغلقة إغلاقًا صارمًا ولا يمكن إعادة فتحها" · en: "The period is hard-closed and cannot be reopened"
Traces     : REQ-FIN-035
Source     : general-accounting-system-plan-en.md §10.2


### RULE-FIN-015 — فصل صلاحية الاعتماد عن صلاحية الإنشاء / Separate the approval permission from the creation permission
Scope      : ENT-FIN-008
Trigger    : on evaluate (period-close-approval action)
Statement  : The system shall require the period-close-approval action to be gated by a permission distinct from the journal-entry-creation permission, enforced through the Security module.
Data source: DEFERRED — the permission matrix is the Security module's declaration surface; FIN declares no permission entity in this version, so the separation is enforced there and has no FIN-side field to read
Message    : ar: "صلاحية اعتماد الإغلاق منفصلة عن صلاحية إنشاء القيود" · en: "The close-approval permission is separate from the entry-creation permission"
Traces     : REQ-FIN-038
Source     : general-accounting-system-plan-en.md §2.2, §8.2, §10.3
Enforcement note (2026-09-12, does not change the Statement above): satisfied in full by the
             `@PreAuthorize(PERM_FIN_PERIODS_CLOSE_APPROVE)` gate on `FiscalPeriodService.hardClose`
             (API-FIN-026) and `FiscalYearService.yearEndClose` (API-FIN-027) — a permission code
             distinct from `PERM_FIN_JOURNAL_ENTRIES_CREATE`, enforced by SEC, exactly as the
             Statement and AC-FIN-038 describe. A STRICTER check once existed in FIN code and was
             removed by recorded human decision: `FinSeparationOfDutiesService` +
             `FiscalPeriodDomain.assertCanHardClose(...)` refused the close for EVERY caller
             whenever ANY single user in the system held both permissions — global user-set
             disjointness, which this rule does not ask for and which the `Data source` line above
             explicitly rules out by stating there is no FIN-side fact to read. `FIN-403-SOD-VIOLATION`
             is struck as unreachable; do not re-derive the removed behaviour from this rule.


### RULE-FIN-016 — قفل القيد بعد الترحيل / Lock an entry after posting
Scope      : ENT-FIN-004, ENT-FIN-005
Trigger    : on evaluate (any edit/delete attempt)
Statement  : The system shall reject any edit or delete attempt on a POSTED entry or its lines; correction is only through a reversal (REQ-FIN-028).
Data source: ENT-FIN-004.statusCode, ENT-FIN-005.journalEntryId
Message    : ar: "القيد المُرحَّل مقفل؛ التصحيح فقط عبر العكس" · en: "A posted entry is locked; correction is only through a reversal"
Traces     : REQ-FIN-016, REQ-FIN-017
Source     : general-accounting-system-plan-en.md §8.3, §12.13


### RULE-FIN-017 — تماسك السنة والفترة وتاريخ المستند / Fiscal year, period and document date must cohere
Scope      : ENT-FIN-004, ENT-FIN-008
Trigger    : on create (an entry whose fiscalYearId, periodId and docDate are all submitted)
Statement  : The system shall reject an entry whose submitted period does not belong to its submitted fiscal year, or whose document date falls outside that period's start/end dates. Entries the system itself generates derive the three from one another and are coherent by construction.
Data source: ENT-FIN-004.fiscalYearId, ENT-FIN-004.periodId, ENT-FIN-004.docDate, ENT-FIN-008.fiscalYearId, ENT-FIN-008.startDate, ENT-FIN-008.endDate
Message    : ar: "الفترة المحددة لا تتبع السنة المالية المحددة، أو تاريخ المستند خارج نطاقها" · en: "The selected period does not belong to the selected fiscal year, or the document date falls outside it"
Traces     : REQ-FIN-014, REQ-FIN-017
Source     : general-accounting-system-plan-en.md §8.1, §10.1


## SCR-REQ-FIN-001 — شجرة الحسابات / Chart of accounts
### B1 — Definition
Purpose      : إدارة شجرة الحسابات الهرمية.
Entities     : ENT-FIN-001
Operations   : search, create, update, deactivate. No by-id read — deliberate v1 exclusion, see
               the note under B5
Users        : مسؤول مالي
Navigation   : FIN → Setup → Chart of accounts
Content shape: true hierarchy (parent/child)
Traces       : REQ-FIN-001, REQ-FIN-002, REQ-FIN-003
Composite    : Search + Entry = ONE screen requirement
### B2 — Search / list
Filters: code(LIKE), nameAr/nameEn(LIKE), accountTypeCode(EXACT), isActiveFl(EXACT) — correspond to result columns.
### B3 — Input
Fields: code, nameAr, nameEn, accountTypeCode, natureCode, parentAccountId, isLeafFl (ENT-FIN-001; RULE-FIN-001 blocks isLeafFl=true on a parent). Button: Deactivate.
### B4 — Access
Page code: FIN_ACCOUNTS. Actions: VIEW, CREATE, UPDATE (covers deactivate — there is no DELETE
endpoint and no `PERM_FIN_ACCOUNTS_DELETE`; deactivate is `PUT /{id}/deactivate` gated by
`PERM_FIN_ACCOUNTS_UPDATE`).
### B5 — API expectations
| Operation | Verb | Path | Inputs | Outputs | RULEs | Traces (REQ) |
|---|---|---|---|---|---|---|
| search accounts | POST | /api/v1/fin/accounts/search | filters, paging (request body) | Page\<Account\> | — | REQ-FIN-001 |
| create account | POST | /api/v1/fin/accounts | account fields | Account | RULE-FIN-001 | REQ-FIN-001, REQ-FIN-002 |
| update account | PUT | /api/v1/fin/accounts/{id} | account fields | Account | RULE-FIN-001 | REQ-FIN-002 |
| deactivate account | PUT | /api/v1/fin/accounts/{id}/deactivate | id | Account | — | REQ-FIN-003 |

**No by-id read, and why.** `AccountController` publishes exactly these four endpoints — create,
update, deactivate and search (`AccountController.java:45, 52, 60, 66`) — and no `GET /{id}`. The
`read` this section's B1 Operations line used to name was never in B5 either, so the B1 line was
its only claim; it is narrowed here rather than deleted so the decision stays legible. A by-id read
is a deliberate v1 exclusion, not a pending endpoint: no REQ or AC asks for one (REQ-FIN-001/002/003
and AC-FIN-001…003 cover create, the leaf-flag rejection and deactivate), this is a Composite
"Search + Entry = ONE screen" requirement whose Entry form is populated from the search-result row,
and API-FIN-001 already returns the complete `Account` record, not a trimmed projection. Contrast
SCR-REQ-FIN-006, where a by-id read WAS built (API-FIN-022) because REQ-FIN-016/REQ-FIN-027 need
the entry's lines, which its search result does not carry.


## SCR-REQ-FIN-002 — تعريف الأبعاد وقيمها / Dimension definition & values
### B1 — Definition
Purpose      : إدارة الأبعاد وقيمها.
Entities     : ENT-FIN-002, ENT-FIN-003
Operations   : search, create (dimension — no deactivate, deliberate; see B4); search, create,
               deactivate (value — API-FIN-035)
Users        : مسؤول مالي
Navigation   : FIN → Setup → Dimensions
Content shape: header + repeating lines (master dimensions + detail values)
Traces       : REQ-FIN-004, REQ-FIN-005, REQ-FIN-006
Composite    : Master (dimensions) + Detail (values) = ONE screen requirement
### B2 — Search / list
Dimension filters: code(LIKE). Value filters: code(LIKE) — correspond to result columns.
### B3 — Input
Dimension fields: code, nameAr, nameEn. Value fields: code, nameAr, nameEn, sortOrder (ENT-FIN-002/003).
### B4 — Access
Page code: FIN_DIMENSIONS. Actions: VIEW, CREATE, UPDATE.

**UPDATE — the dimension-VALUE deactivate, and only that.** UPDATE covers exactly one endpoint:
API-FIN-035, `PUT /api/v1/fin/dimensions/values/{id}/deactivate`, published on
`DimensionController` (child endpoints live on the parent's controller) and delegating to
`DimensionValueService.deactivate`, gated by `PERM_FIN_DIMENSIONS_UPDATE`. That permission is
declared in `PermissionConstants` and is both registered and granted to `SYS_ADMIN` by migration
`V28__fin_dimensions_update_action.sql` — V24 had seeded VIEW and CREATE only, and V25's grant
statement (a `SELECT` over the registry) had already run, so V28 carries its own explicit grant
rather than relying on it. Deactivate is modelled as UPDATE and not DELETE, exactly as
FIN_ACCOUNTS models API-FIN-004. An unknown dimension-value id answers `FIN-404-DIMVALUE`, a new
code registered in `FinErrorCodes` and in both i18n bundles.

**The PARENT dimension deliberately has no deactivate.** No REQ, AC or RULE asks for one and
`Dimension.isActiveFl` (DBF-FIN-018) drives no behaviour, so that column stays unflippable through
the API by decision, not by oversight. The value-level flag (DBF-FIN-029) is the one RULE-FIN-009 /
REQ-FIN-021 read when rejecting a journal line that cites an INACTIVE dimension value —
`DimensionValueDomain.checkUsableOnLine` returns `FIN-409-INVALID-DIMENSION` off exactly this flag
— so before API-FIN-035 that branch was unreachable and untestable, and closing that gap is the
whole reason this half, and only this half, was built. No `activate` counterpart was added either,
matching the delivered `AccountService.deactivate` precedent.

**Previously recorded here, now superseded**: this section stated that FIN publishes no dimension
or dimension-value update/deactivate endpoint at all and that V24 seeds no
`PERM_FIN_DIMENSIONS_UPDATE`. That was an accurate description of the as-built state before
API-FIN-035 and V28; it is no longer true for the value half, and remains true for the parent half.

No DELETE: FIN publishes no `DELETE` endpoint on any screen, and neither V24 nor V28 seeds a
`PERM_FIN_DIMENSIONS_DELETE` row.
### B5 — API expectations
| Operation | Verb | Path | Inputs | Outputs | RULEs | Traces (REQ) |
|---|---|---|---|---|---|---|
| search dimensions | POST | /api/v1/fin/dimensions/search | filters, paging (request body) | Page\<Dimension\> | — | REQ-FIN-004 |
| create dimension | POST | /api/v1/fin/dimensions | code, nameAr, nameEn | Dimension | — | REQ-FIN-004 |
| create dimension value | POST | /api/v1/fin/dimensions/{id}/values | code, nameAr, nameEn, sortOrder | DimensionValue | RULE-FIN-002 | REQ-FIN-005, REQ-FIN-006 |
| search dimension values | POST | /api/v1/fin/dimensions/values/search | filters (incl. dimensionId), paging (request body) | Page\<DimensionValue\> | — | REQ-FIN-005 |
| deactivate dimension value | PUT | /api/v1/fin/dimensions/values/{id}/deactivate | id | DimensionValue (isActiveFl=false) | — | REQ-FIN-005 |


API-FIN-035 (the last row) is the endpoint that makes REQ-FIN-021 / RULE-FIN-009 reachable at all:
it is the only way to set `DimensionValue.isActiveFl` to false. There is no parent-dimension
counterpart and no `activate` — see B4.


## SCR-REQ-FIN-004 — قوالب متكررة/عكسية / Recurring / reversing templates
### B1 — Definition
Purpose      : إدارة القوالب المتكررة والعكسية.
Entities     : ENT-FIN-011, ENT-FIN-012
Operations   : search, create (template with its lines in one request), run, deactivate —
               API-FIN-012/013/014/036, which is everything `RecurringTemplateController`
               publishes. **`deactivate` (template) is DELIVERED as API-FIN-036 and, since
               2026-09-12, it DOES stop the template from running — that defect is CLOSED, see
               B4.** **`update` (template) is still NOT built and is now the ONLY open half of
               the original gap — also B4.** No by-id read and no line-level read/update/delete:
               deliberate v1 exclusions, also B4
Users        : محاسب
Navigation   : FIN → Setup → Recurring/reversing templates
Content shape: header + repeating lines
Traces       : REQ-FIN-022, REQ-FIN-023, REQ-FIN-024
Composite    : Master (templates) + Detail (lines) = ONE screen requirement
### B2 — Search / list
Filters: nameAr/nameEn(LIKE), scheduleTypeCode(EXACT), isActiveFl(EXACT).
### B3 — Input
Template fields: nameAr, nameEn, scheduleTypeCode, frequencyCode, startDate, endDate (ENT-FIN-011).
Line fields: accountId, amount, directionCode, dimensionValueId (ENT-FIN-012).
### B4 — Access
Page code: FIN_RECURRING_TEMPLATES. Actions: VIEW, CREATE, UPDATE (covers BOTH "run" —

API-FIN-014 — and "deactivate" — API-FIN-036; one permission, `PERM_FIN_RECURRING_TEMPLATES_UPDATE`,
already seeded by V24 and already granted by V25's blanket Tier-3 grant, so API-FIN-036 needed no
new constant, no new error code and no migration). No DELETE endpoint and no
`PERM_FIN_RECURRING_TEMPLATES_DELETE`.

**As built.** `RecurringTemplateController` publishes exactly four endpoints, each at its mapping
annotation: `POST` create (API-FIN-013, `RecurringTemplateController.java:51`), `POST /{id}/run`
(API-FIN-014, `:62`), `PUT /{id}/deactivate` (API-FIN-036, `:68`) and `POST /search` (API-FIN-012,
`:76`). The B1 Operations line originally promised `read, update, deactivate` on the template and
`create, read, update, delete` on the line; `deactivate` has since been built, but `read` and
`update` on the template and everything but `create` on the line have not.

**Deliberate v1 exclusions.** *By-id read*: API-FIN-012's search already returns the FULL aggregate
— `RecurringTemplateService.search` batch-loads each page's lines and maps them into
`RecurringTemplateResponse.lines` (`RecurringTemplateService.java:405-411`) — so a `GET /{id}`
would return nothing the search does not, and no REQ or AC asks for one. *Line delete*: FIN
publishes no `DELETE` endpoint on any screen and V24 seeds no `PERM_FIN_*_DELETE` row, the same
already-taken decision recorded at SCR-REQ-FIN-003 §B4. *No `activate` counterpart to API-FIN-036*:
FIN ships none for any entity, following the delivered `AccountService.deactivate` precedent.

**`deactivate` (template) — DELIVERED as API-FIN-036.** The half of the defect this section used
to report — that no endpoint could ever set `IS_ACTIVE_FL` to FALSE — is closed.
`RecurringTemplateService.deactivate` loads the template, calls the entity's own `deactivate()`
helper (`RecurringTemplate.java:99`, which until now had zero callers), persists, then re-reads the
template's lines and hands them to the mapper before returning — the re-read is required because
`RecurringTemplateResponse.lineCount` is derived from the list the mapper is given, so an empty
list would misreport the aggregate as having no lines. Unknown id answers the pre-existing
`FIN-404-TEMPLATE`. It is gated on the pre-existing `PERM_FIN_RECURRING_TEMPLATES_UPDATE`, the same
permission API-FIN-014's run uses. No migration, no new constant, no new error code.

**DEFECT CLOSED 2026-09-12 — deactivate now stops the run.** This paragraph used to report an OPEN
DEFECT: `RecurringTemplateService.run` (API-FIN-014) did not read `isActiveFl`, so a deactivated
template still ran and still posted, and setting the flag changed no behaviour whatever. It is now
closed. `RecurringTemplateDomain.assertCanRun()` — a NEW Domain companion for ENT-FIN-011, created
for this — is called by `RecurringTemplateService.run` immediately after the template is loaded and
before the run date, period or lines are resolved, and refuses a deactivated template with a new
`FIN-409-NOT-ACTIVE` (HTTP 409; ar "هذا التعريف غير نشط ولا يمكن تشغيله", en "This definition is
deactivated and cannot be run"). `FIN-404-TEMPLATE` was deliberately NOT reused, because the
template does exist. AC-FIN-023, written "Given an **active** recurring template", is now fully
satisfied: the inactive state is reachable AND behaves differently. The flag's other observable
effect is unchanged — B2's advertised `isActiveFl(EXACT)` filter discriminates
(`RecurringTemplateService`'s sort/filter whitelist honours it).

**READ THIS BEFORE CITING THE GATE AS A REQUIREMENT.** No RULE-FIN-* states it. It was closed on a
**recorded human decision**, because closing it required inventing an "exists but is inactive"
error code, its HTTP status, its ar+en messages and its Error Catalog row — none of which any REQ,
AC or RULE asks for. A later session must not cite `FIN-409-NOT-ACTIVE` as pre-existing spec, and
must not infer from it that other unstated gates may be added the same way without a fresh
decision.

**OPEN DEFECT — `update` (template) is still missing.** This is now the ONLY open half of the
original gap: with deactivate but no update, a template created with a wrong account or amount
cannot be corrected — it can only be retired. Since 2026-09-12 retiring it does at least stop it
(above), so the wrong entries stop arriving; entries already posted before the retirement remain
recoverable only by reversing them one by one. It was not built because a correct update must decide the fate of the template's existing child
lines (replace wholesale? merge? reject if any line changed?), which is a design question no REQ,
AC or RULE answers. It is recorded here as a known gap, not as a reasoned exclusion.
### B5 — API expectations
| Operation | Verb | Path | Inputs | Outputs | RULEs | Traces (REQ) |
|---|---|---|---|---|---|---|
| search templates | POST | /api/v1/fin/recurring-templates/search | filters, paging (request body) | Page\<RecurringTemplate\> | — | REQ-FIN-022 |
| create template | POST | /api/v1/fin/recurring-templates | template + lines | RecurringTemplate | RULE-FIN-006 (reused, balance check applied at run time not save time) | REQ-FIN-022 |
| run template (system/scheduled) | POST | /api/v1/fin/recurring-templates/{id}/run | — | JournalEntry | RULE-FIN-006, RULE-FIN-007, RULE-FIN-008, RULE-FIN-009, RULE-FIN-011 | REQ-FIN-023, REQ-FIN-024 |
| deactivate template (API-FIN-036) | PUT | /api/v1/fin/recurring-templates/{id}/deactivate | path `id`, no body | RecurringTemplate (isActiveFl=false, lines included) | — (none beyond existence; since 2026-09-12 it DOES gate the run — a subsequent API-FIN-014 answers `FIN-409-NOT-ACTIVE`, by decision not by RULE — see B4) | REQ-FIN-022 |


## SCR-REQ-FIN-005 — قواعد التوزيع / Allocation rules
### B1 — Definition
Purpose      : إدارة قواعد توزيع رصيد حساب مصدر على أهدافه.
Entities     : ENT-FIN-013, ENT-FIN-014
Operations   : search, create (rule with its targets in one request), run, deactivate —
               API-FIN-015/016/017/037, which is everything `AllocationRuleController` publishes.
               **`deactivate` (rule) is DELIVERED as API-FIN-037 and, since 2026-09-12, it DOES
               stop the rule from running — that defect is CLOSED, see B4.** **`update` (rule) is
               still NOT built and is now the ONLY open half of the original gap — also B4.** No
               by-id read and no target-level read/update/delete: deliberate v1 exclusions, also B4
Users        : محاسب
Navigation   : FIN → Setup → Allocation rules
Content shape: header + repeating lines
Traces       : REQ-FIN-025, REQ-FIN-026
Composite    : Master (rules) + Detail (targets) = ONE screen requirement
### B2 — Search / list
Filters: nameAr/nameEn(LIKE), sourceAccountId(EXACT), isActiveFl(EXACT).
### B3 — Input
Rule fields: nameAr, nameEn, sourceAccountId (ENT-FIN-013). Target fields: targetAccountId,
dimensionValueId, distributionTypeCode, distributionValue, isRemainderFl (ENT-FIN-014;
RULE-FIN-003 applies).
### B4 — Access
Page code: FIN_ALLOCATION_RULES. Actions: VIEW, CREATE, UPDATE (covers BOTH "run" — API-FIN-017 —
and "deactivate" — API-FIN-037; one permission, `PERM_FIN_ALLOCATION_RULES_UPDATE`, already seeded
by V24 and already granted by V25's blanket Tier-3 grant, so API-FIN-037 needed no new constant, no
new error code and no migration). No DELETE endpoint and no `PERM_FIN_ALLOCATION_RULES_DELETE`.

**As built.** `AllocationRuleController` publishes exactly four endpoints, each at its mapping
annotation: `POST` create (API-FIN-016, `AllocationRuleController.java:52`), `POST /{id}/run`
(API-FIN-017, `:63`), `PUT /{id}/deactivate` (API-FIN-037, `:69`) and `POST /search` (API-FIN-015,
`:76`). The B1 Operations line originally promised `read, update, deactivate` on the rule and
`create, read, update, delete` on the target; `deactivate` has since been built, but `read` and
`update` on the rule and everything but `create` on the target have not.

**Deliberate v1 exclusions.** *By-id read*: API-FIN-015's search returns the FULL aggregate,
targets included (`AllocationRuleResponse.targets`), so a `GET /{id}` would add nothing and no REQ
or AC asks for one — the same reasoning as SCR-REQ-FIN-004 §B4. *Target delete*: FIN publishes no
`DELETE` endpoint on any screen and V24 seeds no `PERM_FIN_*_DELETE` row. *No `activate`
counterpart to API-FIN-037*: FIN ships none for any entity, following the delivered
`AccountService.deactivate` precedent.

**`deactivate` (rule) — DELIVERED as API-FIN-037.** The half of the defect this section used to
report — that no endpoint could ever set `IS_ACTIVE_FL` to FALSE — is closed.
`AllocationRuleService.deactivate` loads the rule, calls the entity's own `deactivate()` helper
(`AllocationRule.java:91`, which until now had zero callers), persists, then re-reads the rule's
targets and hands them to the mapper before returning — the re-read is required because
`AllocationRuleResponse.targetCount` is derived from the list the mapper is given, so an empty list
would misreport the aggregate as having no targets. Unknown id answers the pre-existing
`FIN-404-ALLOCATION-RULE`. It is gated on the pre-existing `PERM_FIN_ALLOCATION_RULES_UPDATE`, the
same permission API-FIN-017's run uses. No migration, no new constant, no new error code. Nothing
is delegated to `AllocationRuleDomain` before the mutation: RULE-FIN-003, the one rule that Domain
owns, governs the remainder-target SET at create and run time and says nothing about the active
flag.

**DEFECT CLOSED 2026-09-12 — deactivate now stops the run.** This paragraph used to report an OPEN
DEFECT: `AllocationRuleService.run` (API-FIN-017) did not read `isActiveFl`, so a deactivated
allocation rule still ran and still posted, and the Domain accessor built for the flag was dead
code. It is now closed. `AllocationRuleDomain.assertCanRun()` is called by
`AllocationRuleService.run` immediately after the rule is loaded and before its targets are even
fetched, and refuses a deactivated rule with a new `FIN-409-NOT-ACTIVE` (HTTP 409; ar "هذا التعريف
غير نشط ولا يمكن تشغيله", en "This definition is deactivated and cannot be run").
`FIN-404-ALLOCATION-RULE` was deliberately NOT reused, because the rule does exist. B2's advertised
`isActiveFl(EXACT)` filter still discriminates as before.

**READ THIS BEFORE CITING THE GATE AS A REQUIREMENT.** No RULE-FIN-* states it — RULE-FIN-003, the
one rule `AllocationRuleDomain` owns, governs the remainder-target set and says nothing about the
active flag. The gate was added on a **recorded human decision**, since closing the gap required
inventing an error code, status and messages no REQ, AC or RULE asks for. Do not cite
`FIN-409-NOT-ACTIVE` as pre-existing spec. It closed on the same decision as SCR-REQ-FIN-004's.

**OPEN DEFECT — `update` (rule) is still missing.** This is now the ONLY open half of the original
gap: with deactivate but no update, a rule created with the wrong source account or target split
cannot be corrected — it can only be retired. Since 2026-09-12 retiring it does at least stop it
(above), so the wrong distributions stop arriving. It was not built because a correct update must decide the fate
of the rule's existing targets, and therefore of RULE-FIN-003's remainder-target set (replace
wholesale? merge? re-validate the remainder marker across the new set?), which is a design question
no REQ, AC or RULE answers. It is recorded here as a known gap, not as a reasoned exclusion.
### B5 — API expectations
| Operation | Verb | Path | Inputs | Outputs | RULEs | Traces (REQ) |
|---|---|---|---|---|---|---|
| search allocation rules | POST | /api/v1/fin/allocation-rules/search | filters, paging (request body) | Page\<AllocationRule\> | — | REQ-FIN-025 |
| create allocation rule | POST | /api/v1/fin/allocation-rules | rule + targets | AllocationRule | RULE-FIN-003 | REQ-FIN-025 |
| run allocation rule | POST | /api/v1/fin/allocation-rules/{id}/run | — | JournalEntry | RULE-FIN-006..011 | REQ-FIN-026 |
| deactivate allocation rule (API-FIN-037) | PUT | /api/v1/fin/allocation-rules/{id}/deactivate | path `id`, no body | AllocationRule (isActiveFl=false, targets included) | — (none beyond existence; since 2026-09-12 it DOES gate the run — a subsequent API-FIN-017 answers `FIN-409-NOT-ACTIVE`, by decision not by RULE — see B4) | REQ-FIN-025 |


## SCR-REQ-FIN-007 — الفترات والسنوات المالية / Fiscal periods & years + close approval + year-end close
### B1 — Definition
Purpose      : إدارة السنوات والفترات المالية، اعتماد الإغلاق، وتشغيل إقفال نهاية السنة.
Entities     : ENT-FIN-007, ENT-FIN-008
Operations   : create (year); search — PERIODS only (API-FIN-033); open, soft-close, hard-close
               (period); run year-end close. There is NO fiscal-YEAR search and no by-id read of
               either a year or a period — see the note under B5
Users        : مسؤول مالي (إدارة) / مراقب مالي (اعتماد الإغلاق — دور منفصل، POL-FIN-016)
Navigation   : FIN → Control → Fiscal periods & years
Content shape: header + repeating lines (year + its periods)
Traces       : REQ-FIN-031, REQ-FIN-032, REQ-FIN-033, REQ-FIN-034, REQ-FIN-035, REQ-FIN-036, REQ-FIN-037, REQ-FIN-038
Composite    : Master (years) + Detail (periods) = ONE screen requirement
### B2 — Search / list
Filters: fiscalYearId(EXACT), statusCode(EXACT) — both OPTIONAL. As delivered by API-FIN-033 these
are exactly the two filters implemented (`FiscalPeriodSearchRequest`), plus paging and sort.
`fiscalYearId` travels inside the body's `filters` list and narrows to one year's periods when
supplied; omitting it is a legitimate "all periods" request, deliberately unlike the dimension-value
child search (API-FIN-008), which rejects a missing parent id. The divergence is the point of the
endpoint: a client that did not create the fiscal year in the same session must still be able to
discover a period id.
### B3 — Input
Year fields: code, startDate, endDate, periodCount (ENT-FIN-007). Period actions per row:
Open, Soft-close, Hard-close (approval-gated). Year action: "Run year-end close" (enabled
only once every period is Hard Closed).
### B4 — Access
Page code: FIN_PERIODS. Actions: VIEW (`PERM_FIN_PERIODS_VIEW` — the screen's gateway, and since
API-FIN-033 also the permission behind a real read endpoint), CREATE (year), UPDATE
(open/soft-close, and a distinct custom action `PERM_FIN_PERIODS_CLOSE_APPROVE` for
hard-close/year-end-close — RULE-FIN-015).
### B5 — API expectations
| Operation | Verb | Path | Inputs | Outputs | RULEs | Traces (REQ) |
|---|---|---|---|---|---|---|
| search fiscal periods | POST | /api/v1/fin/fiscal-periods/search | filters (fiscalYearId?, statusCode?), paging (request body) | Page\<FiscalPeriod\> | — | REQ-FIN-031 |
| create fiscal year | POST | /api/v1/fin/fiscal-years | code, startDate, endDate, periodCount | FiscalYear + FiscalPeriod[] | — | REQ-FIN-031 |
| open period | PATCH | /api/v1/fin/fiscal-periods/{id}/open | id | FiscalPeriod | — | REQ-FIN-032 |
| soft-close period | PATCH | /api/v1/fin/fiscal-periods/{id}/soft-close | id | FiscalPeriod | — | REQ-FIN-033 |
| hard-close period (approval) | PATCH | /api/v1/fin/fiscal-periods/{id}/hard-close | id | FiscalPeriod | RULE-FIN-014, RULE-FIN-015 | REQ-FIN-034, REQ-FIN-035, REQ-FIN-037, REQ-FIN-038 |
| run year-end close | POST | /api/v1/fin/fiscal-years/{id}/year-end-close | id | closing + opening JournalEntry | RULE-FIN-006, RULE-FIN-007, RULE-FIN-009, RULE-FIN-015 (RULE-FIN-008 does NOT apply — the year-end CLOSING/OPENING entries are exempt from the period gate by RULE-FIN-008's own carve-out; they are precisely the entries a hard-closed period must still accept) | REQ-FIN-036 |

**A fiscal-period search API now exists: API-FIN-033**, `POST /api/v1/fin/fiscal-periods/search`
(`FiscalPeriodController.search` → `FiscalPeriodService.search`), serving the B2 filters directly
and gated by `PERM_FIN_PERIODS_VIEW` — which was already a registered V24 action row and already
granted by V25/V27, so no migration was needed; the endpoint only added the matching
`PermissionConstants` constant. Until it landed, the B2 filters were served solely by API-FIN-023's
response (the year with its generated periods), which is what this paragraph previously recorded,
and `PERM_FIN_PERIODS_VIEW` was a gateway row with no endpoint behind it. That is no longer the
case.

Why it was built: `JournalEntryCreateRequest` requires both `fiscalYearId` and `periodId`
(API-FIN-019, RULE-FIN-017), and API-FIN-029/030/031 require a period or year id of their own, yet
no API returned a fiscal period except the API-FIN-023 create response. A client that had not
created the year in the same session therefore could not post an entry or run a report at all.

**What API-FIN-033 is NOT, and what B1 no longer claims.** The B1 Operations line used to read
`create (year), search, read`. Measured against the controllers: `FiscalYearController` publishes
exactly two endpoints, `POST` create (API-FIN-023, `FiscalYearController.java:39`) and
`POST /{id}/year-end-close` (API-FIN-027, `:47`); `FiscalPeriodController` publishes the three
PATCH transitions plus `POST /search` (`FiscalPeriodController.java:49, 55, 61, 68`). So the
`search` B1 named is API-FIN-033 over fiscal PERIODS — **there is no fiscal-year search**, and
API-FIN-033 must not be read as one — and the `read` B1 named exists for neither resource: no
`GET /{id}` is published on a year or on a period.

*By-id read* is a deliberate v1 exclusion: API-FIN-023 returns the year together with its generated
periods, and API-FIN-033 returns the full `FiscalPeriodResponse` row, so neither a year nor a
period has a field a by-id read would newly expose. *Fiscal-year search* is recorded as a KNOWN GAP
rather than a reasoned exclusion — no REQ or AC asks for one (REQ-FIN-031 asks only that creating a
year generate its periods), and it is not blocking, because `FiscalPeriodResponse` carries
`fiscalYearId` (`FiscalPeriodResponse.java:31`), so an unfiltered API-FIN-033 call discovers year
ids indirectly. It is a gap and not a defect for that reason alone: the discovery path is awkward
(read a year id off any period row) but it exists, and nothing in the ledger can be corrupted by
its absence.


## SCR-REQ-FIN-008 — دفتر الحساب / Account ledger
### B1 — Definition
Purpose      : عرض دفتر حساب مُشتق حيًا مع تتبع نازل.
Entities     : ENT-FIN-005
Operations   : read
Users        : محاسب / مراقب مالي
Navigation   : FIN → Reports → Account ledger; to: SCR-REQ-FIN-006 (drill-down to entry)
Content shape: flat record (running balance list)
Traces       : REQ-FIN-039, REQ-FIN-046
Composite    : single screen (report; no entry)
### B2 — Search / list
Filters: accountId(EXACT), periodId or date range(DATE_RANGE), dimension value(s)(EXACT).
### B3 — Input
Not applicable — read-only report.
### B4 — Access
Page code: FIN_ACCOUNT_LEDGER. Action: VIEW.
### B5 — API expectations
| Operation | Verb | Path | Inputs | Outputs | RULEs | Traces (REQ) |
|---|---|---|---|---|---|---|
| account ledger | GET | /api/v1/fin/reports/account-ledger | accountId, date range, dimension filters | running-balance list, each row linking to its JournalEntry | — | REQ-FIN-039, REQ-FIN-046 |


## SCR-REQ-FIN-009 — ميزان المراجعة / Trial balance
### B1 — Definition
Purpose      : عرض ميزان مراجعة مُشتق حيًا ومتوازن دائمًا.
Entities     : ENT-FIN-005
Operations   : read
Users        : مراقب مالي
Navigation   : FIN → Reports → Trial balance; to: SCR-REQ-FIN-008 (drill-down per account)
Content shape: flat record (one row per account)
Traces       : REQ-FIN-040, REQ-FIN-046
Composite    : single screen (report; no entry)
### B2 — Search / list
Filters: periodId(EXACT), accountTypeCode(EXACT) — both OPTIONAL query parameters as built.
### B3 — Input
Not applicable.
### B4 — Access
Page code: FIN_TRIAL_BALANCE. Action: VIEW.
### B5 — API expectations
| Operation | Verb | Path | Inputs | Outputs | RULEs | Traces (REQ) |
|---|---|---|---|---|---|---|
| trial balance | GET | /api/v1/fin/reports/trial-balance | periodId?, accountTypeCode? | one row per account (debit/credit balance) | — | REQ-FIN-040, REQ-FIN-046 |

**404 on an unknown keying id, as built.** `periodId` stays an OPTIONAL narrowing: omitting it
means "no period narrowing" and remains a 200 (AC-FIN-040's happy path). When it IS supplied it
must resolve — an unknown id now answers the pre-existing `FIN-404-PERIOD` rather than a silent 200
carrying an all-zero report. This aligns the screen with FIN's existing house style, where a
keying entity id 404s (API-FIN-028 → `FIN-404-ACCOUNT`, API-FIN-032 → `FIN-404-DIMENSION`).


## SCR-REQ-FIN-010 — الميزانية العمومية / Balance sheet
### B1 — Definition
Purpose      : عرض ميزانية عمومية مُشتقة حيًا مع استمرارية الأرصدة الافتتاحية.
Entities     : ENT-FIN-005
Operations   : read
Users        : مراقب مالي
Navigation   : FIN → Reports → Balance sheet; to: SCR-REQ-FIN-009 (drill-down)
Content shape: header + repeating lines with totals (assets / liabilities / equity sections)
Traces       : REQ-FIN-041, REQ-FIN-046
Composite    : single screen (report; no entry)
### B2 — Search / list
Filters: fiscalYearId(EXACT, REQUIRED), asOfDate (optional cut-off).
### B3 — Input
Not applicable.
### B4 — Access
Page code: FIN_BALANCE_SHEET. Action: VIEW.
### B5 — API expectations
| Operation | Verb | Path | Inputs | Outputs | RULEs | Traces (REQ) |
|---|---|---|---|---|---|---|
| balance sheet | GET | /api/v1/fin/reports/balance-sheet | fiscalYearId (required), asOfDate? | grouped balance-sheet accounts with balances | — | REQ-FIN-041, REQ-FIN-046 |

**404 on an unknown fiscal year, as built.** `fiscalYearId` is the REQUIRED keying identifier and
is resolved first: an unknown id answers the pre-existing `FIN-404-YEAR`. Previously it returned a
200 carrying an all-zero statement that a caller could not tell apart from a genuinely dormant
year.


## SCR-REQ-FIN-011 — قائمة الدخل / Income statement
### B1 — Definition
Purpose      : عرض قائمة دخل مُشتقة حيًا تُفتح بصفر كل سنة.
Entities     : ENT-FIN-005
Operations   : read
Users        : مراقب مالي
Navigation   : FIN → Reports → Income statement; to: SCR-REQ-FIN-009 (drill-down)
Content shape: header + repeating lines with totals (revenue / expense sections)
Traces       : REQ-FIN-042, REQ-FIN-046
Composite    : single screen (report; no entry)
### B2 — Search / list
Filters: fiscalYearId(EXACT, REQUIRED); the period range is expressed as built by two optional
period ids, `fromPeriodId` and `toPeriodId`, translated into the `docDate` bounds those periods
span (DBF-FIN-080 / DBF-FIN-081).
### B3 — Input
Not applicable.
### B4 — Access
Page code: FIN_INCOME_STATEMENT. Action: VIEW.
### B5 — API expectations
| Operation | Verb | Path | Inputs | Outputs | RULEs | Traces (REQ) |
|---|---|---|---|---|---|---|
| income statement | GET | /api/v1/fin/reports/income-statement | fiscalYearId (required), fromPeriodId?, toPeriodId? | grouped revenue/expense accounts with balances | — | REQ-FIN-042, REQ-FIN-046 |

**404 on an unknown keying id, as built.** `fiscalYearId` is resolved first and raises the
pre-existing `FIN-404-YEAR` when it does not exist; `fromPeriodId` / `toPeriodId` stay OPTIONAL
narrowings and already raised `FIN-404-PERIOD` when supplied and unknown — that half is unchanged.
The year check closes an internal contradiction: the same request used to answer 404 on an unknown
period and 200 on an unknown year.


## SCR-REQ-FIN-012 — تقارير الأبعاد / Dimension reports
### B1 — Definition
Purpose      : عرض تقارير مجمّعة حسب البُعد دون تكرار الحسابات.
Entities     : ENT-FIN-005, ENT-FIN-006
Operations   : read
Users        : مراقب مالي
Navigation   : FIN → Reports → Dimension reports
Content shape: flat record (account × dimension-value rows)
Traces       : REQ-FIN-043
Composite    : single screen (report; no entry)
### B2 — Search / list
Filters: dimensionId(EXACT), dimensionValueId(EXACT), periodId(EXACT).
### B3 — Input
Not applicable.
### B4 — Access
Page code: FIN_DIMENSION_REPORTS. Action: VIEW.
### B5 — API expectations
| Operation | Verb | Path | Inputs | Outputs | RULEs | Traces (REQ) |
|---|---|---|---|---|---|---|
| dimension report | GET | /api/v1/fin/reports/dimension | dimensionId, filters | one row per account+dimension-value combination | — | REQ-FIN-043 |


API-FIN-035 (dimension value), API-FIN-036 (recurring/reversing template) and API-FIN-037
(allocation rule). None has an `activate` counterpart, following the delivered
`AccountService.deactivate` precedent. Each is `PUT /{id}/deactivate` gated by its screen's UPDATE
permission; API-FIN-036 and API-FIN-037 reuse `PERM_FIN_RECURRING_TEMPLATES_UPDATE` and
`PERM_FIN_ALLOCATION_RULES_UPDATE`, both already seeded by V24 and already granted by V25's blanket
Tier-3 grant, so neither needed a new permission, a new error code or a migration. Deliberately NOT
built, and not pending: a deactivate on the parent `Dimension` (no REQ/AC/RULE requires it and
`Dimension.isActiveFl` drives no behaviour) and a rule-line delete (ENT-FIN-010 has no active-flag
column and FIN publishes no `DELETE` endpoint on any screen) — see SCR-REQ-FIN-002 §B4 and
SCR-REQ-FIN-003 §B4.

**DEFECT CLOSED 2026-09-12 — on those two screens, deactivating now DOES stop the run.** This
supersedes the entry that used to stand here reporting the run as ungated. `RecurringTemplateDomain
.assertCanRun()` (a new Domain companion) and `AllocationRuleDomain.assertCanRun()` are each called
first thing in their service's `run`, so a deactivated template or allocation rule is refused with
`FIN-409-NOT-ACTIVE` (409) and posts nothing. The UPDATE cells above cover run AND deactivate for
these two screens, and "deactivate" there now means a real gate, not only a record-keeping state.
**The gate is a recorded human decision, not stated requirement: no RULE-FIN-* asks for it**, and
closing the gap required inventing an error code, status and ar+en messages that no REQ, AC or RULE
provides — do not read `FIN-409-NOT-ACTIVE` back out of these tables as pre-existing spec. See
SCR-REQ-FIN-004 §B4 and SCR-REQ-FIN-005 §B4.

**SoD on FIN_PERIODS — what was removed, and what still holds (2026-09-12).** RULE-FIN-015 stands
and is enforced by the `PERM_FIN_PERIODS_CLOSE_APPROVE` gate alone, which is exactly what the rule
asks for. A stricter, unrequested check — FIN service code that refused the close for every caller
whenever any single user in the system held both that permission and
`PERM_FIN_JOURNAL_ENTRIES_CREATE` — was deleted by recorded human decision, together with the SEC
user-directory read behind it; `FIN-403-SOD-VIOLATION` is struck as unreachable. Migration V30 then
granted `PERM_FIN_PERIODS_CLOSE_APPROVE` to `SYS_ADMIN`, which V25 had deliberately withheld, so
the bootstrap `admin` can now close. Note that V27's header, being applied and immutable, still
describes the removed mechanism as live in both its numbered "OPERATIONAL PRECONDITIONS" — both are
now false; see the SEC-BE phase document for the correction in full.

**OPEN DEFECT — `update` is still missing on the same two screens.** This is now the only open half
of the original gap: a template or allocation rule created with wrong content can be retired — and,
since 2026-09-12, retiring it does stop it running — but it still cannot be corrected. Neither update was built because a
correct one must decide the fate of the aggregate's children — a template's existing lines, a
rule's existing targets and hence RULE-FIN-003's remainder-target set — which is a design question
no REQ, AC or RULE answers. Recorded as a known gap, not a reasoned exclusion. See SCR-REQ-FIN-004
§B4 and SCR-REQ-FIN-005 §B4.
══════════════════════════════════════════════════════════════════

<<<END INPUT>>>

<<<INPUT: backend-execution-plan>>>
# BACKEND EXECUTION PLAN — الحسابات العامة / Finance (General Ledger) (FIN) — DELTA v2
══════════════════════════════════════════════════════════════════
Module : FIN   Version : v2 (delta on v1 — change set CS-FIN-001, ADDITIVE)   Profile : erp   Dialect : postgresql16
Framework : spring-boot-java (profile.stack.backend.framework)
Inputs : srs, db-script, registry-srs, registry-db (all v2 current state, `_state/`); change-manifest CS-FIN-001
Open ADRs : 6 raised by this stage, all ACCEPTED (non-breaking) — decisions/FIN/ADR-FIN-029.md,
            decisions/FIN/ADR-FIN-030.md, decisions/FIN/ADR-FIN-031.md,
            decisions/FIN/ADR-FIN-040.md, decisions/FIN/ADR-FIN-041.md, decisions/FIN/ADR-FIN-042.md. Carried: ADR-FIN-001. Applied from
            upstream v2: ADR-FIN-010, ADR-FIN-011, ADR-FIN-012, ADR-FIN-013, ADR-FIN-014, ADR-FIN-021,
            ADR-FIN-023, ADR-FIN-024, ADR-FIN-025, ADR-FIN-026, ADR-FIN-027, ADR-FIN-028. BLOCKED: none.
Delta  : ADDED — four account-mapping endpoints (API-FIN-038 … API-FIN-041), twelve queries (QR-FIN-050 …
         QR-FIN-061), the entity blocks of ENT-FIN-015 and ENT-FIN-016.
         MODIFIED — API-FIN-011 and API-FIN-020 in substance (rule-line tags; mapped accounts, event dimensions,
         distinct rejections); XM-FIN-001 (scope); QR-FIN-015,
         QR-FIN-025, QR-FIN-026; the Concurrency line of API-FIN-014, API-FIN-024, API-FIN-025, API-FIN-026 and
         API-FIN-027, each now naming a PESSIMISTIC_WRITE guard (ADR-FIN-041, pass-1); every other v1 endpoint
         and entity block in annotation only (ADR-FIN-029 — no behaviour change).
         REMOVED — none. `FIN-422-MAPPING-UNSUPPORTED` is struck in the Error Catalog; a code is not an id.
══════════════════════════════════════════════════════════════════

Delta reading note. This file is the complete current plan, not a fragment. `gov.py state` folds a delta plan
by carrying only a phase's DIRECT children, so a SUB re-emitted with its v2 atoms alone would lose every v1 atom
inside it, and nothing outside a marker is carried at all (index, manifest, catalog, self-check). Every phase is
therefore re-emitted in full. Records restated unchanged (the v1 query definitions, XM-FIN-002) are byte-identical
to v1; every record that differs is listed MODIFIED in the version's change manifest. ADR-FIN-029 records why the
v1 blocks were brought up to the current P3.1 template in the same pass: the contracts that check that template
were added after v1 passed its gate, and a v2 gate reads the whole folded plan.

## PRE-GENERATION EXTRACTION — FIN v2 (working set; not part of the plan proper)

```
── FROM srs ──────────────────────────────────────────────────────────────
ENTITIES      16 — ENT-FIN-001..016. v2 ADDED ENT-FIN-015 AccountMapping and ENT-FIN-016
              RuleLineDimension (both config); MODIFIED ENT-FIN-010 RuleLine (DIRECT gone,
              accountBusinessFieldCode added, dimension tags hang off the line)
REQUIREMENTS  77 — REQ-FIN-001..077 (v2 ADDED 047..077; MODIFIED 045 — 15 lookup types)
RULES         28 — RULE-FIN-001..028 (v2 ADDED 018..028; MODIFIED 004 — both detections answer
              the duplicate error). RULE-FIN-015 keeps its DEFERRED data source
SCREENS       13 — SCR-REQ-FIN-001..013 (ADDED 013 Account mappings; MODIFIED 003 and 006)
PERMISSIONS   13 page codes, pattern PERM_<PAGE_CODE>_<ACTION>, gateway VIEW. v2 adds page
              FIN_ACCOUNT_MAPPINGS with VIEW / CREATE / UPDATE (deactivate under UPDATE, no DELETE)
LOOKUPS       15 FIN-owned keys, runtime-loaded from MDL. v2 ADDED FIN_EVENT_BUSINESS_FIELD
              (PAYMENT_METHOD, EXPENSE_TYPE_CODE, ORGANISATION_CODE, BRANCH_CODE) and
              FIN_DIMENSION_VALUE_SOURCE (CONSTANT, BUSINESS_FIELD); MODIFIED ACCOUNT_DERIVATION_TYPE
              (CONSTANT, MAPPING — DIRECT soft-retired) and PAYMENT_METHOD (CHEQUE seeded)
BUSINESS CODE JournalEntry.docNo — `JV-{fiscalYearCode}-{NNNNNN}`, FIN-local generator, unchanged.
              AccountMapping and RuleLineDimension carry no business code (SRS A3)
── FROM db-script ────────────────────────────────────────────────────────
TABLES        16 — v2 ADDED FIN_ACCOUNT_MAPPING and FIN_RULE_LINE_DIM; FIN_RULE_LINE altered
PK GENERATION strategy `sequence`: one SEQ_{TABLE} per table, the PK a plain BIGINT filled by the
              application. The 14 v1 sequences exist from migration V22; v2 BLOCK 1 creates
              SEQ_FIN_ACCOUNT_MAPPING and SEQ_FIN_RULE_LINE_DIM
COLUMNS       165 DBF (v2 ADDED 148..165; MODIFIED 041 traces, 101 value set, 102 nullability)
CONSTRAINTS   v2: PK_FIN_ACCOUNT_MAPPING, PK_FIN_RULE_LINE_DIM, UQ_FIN_RULE_LINE_DIM_LINE_DIM,
              CHK_FIN_RULE_LINE_DERIVATION_SPEC, CHK_FIN_RULE_LINE_DIM_VALUE_SPEC,
              FK_ACCOUNT_MAPPING_ACCOUNT, FK_RULE_LINE_DIM_LINE, FK_RULE_LINE_DIM_DIMENSION,
              FK_RULE_LINE_DIM_VALUE. Indexes: UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY (partial unique,
              WHERE is_active_fl), IDX_FIN_ACCOUNT_MAPPING_EVT_FLD_VAL, IDX_FIN_ACCOUNT_MAPPING_FIELD,
              IDX_FIN_ACCOUNT_MAPPING_ACCOUNT, IDX_FIN_RULE_LINE_DIM_DIMENSION, IDX_FIN_RULE_LINE_DIM_VALUE.
              v1's UQ_FIN_JOURNAL_ENTRY_EVENT_REF is unchanged; only the answer to its refusal changes
XM            XM-FIN-001 SOFT-READ → MDL, ACTIVE, MODIFIED (6 more columns, 15 keys);
              XM-FIN-002 RETIRED 2026-09-12 (id burned)
── FROM registries ───────────────────────────────────────────────────────
SHARED ENTITIES CONSUMED   LookupType / LookupValue (MDL — ENT-MDL-001, ENT-MDL-002) via XM-FIN-001.
                           SEC identity and FIN's self-registration into SEC stay platform
                           integration, not an XM row (ADR-FIN-001)
EXISTING LOOKUP KEYS       the 13 v1 keys are reused; the 2 new keys are FIN-owned, no duplicate
ID RANGES already used     API-FIN-001..037 · QR-FIN-001..049 — v2 continues at API-FIN-038 and QR-FIN-050
──────────────────────────────────────────────────────────────────────────
No row required §2A.3 extraction-failure handling.
```

## EXECUTION PLAN INDEX — FIN v2 — backend-execution-plan-fin.md
Profile: erp · dialect: postgresql16 · framework: spring-boot-java
Open ADRs: 6 new (ADR-FIN-029, ADR-FIN-030, ADR-FIN-031, ADR-FIN-040, ADR-FIN-041, ADR-FIN-042), none BLOCKED — decisions/FIN/

**ENTITY REGISTRY**
| ENT | Name | Table | Business code | Operations (security actions — the endpoints are in each entity block) |
|---|---|---|---|---|
| ENT-FIN-001 | Account | FIN_ACCOUNT | none | VIEW · CREATE · UPDATE (update, deactivate) |
| ENT-FIN-002 | Dimension | FIN_DIMENSION | none | VIEW · CREATE |
| ENT-FIN-003 | DimensionValue | FIN_DIMENSION_VALUE | none | VIEW · CREATE · UPDATE (deactivate) |
| ENT-FIN-004 | JournalEntry | FIN_JOURNAL_ENTRY | **docNo** (`JV-{fiscalYearCode}-{NNNNNN}`, FIN-local generator) | VIEW · CREATE (manual, event, recurring, allocation, reversal, year-end) |
| ENT-FIN-005 | JournalLine | FIN_JOURNAL_LINE | none | VIEW · CREATE (with its header) |
| ENT-FIN-006 | JournalLineDimension | FIN_JOURNAL_LINE_DIM | none | VIEW · CREATE (with its line) |
| ENT-FIN-007 | FiscalYear | FIN_FISCAL_YEAR | none | CREATE · UPDATE (year-end close) |
| ENT-FIN-008 | FiscalPeriod | FIN_FISCAL_PERIOD | none | VIEW · CREATE (with its year) · UPDATE (open, soft-close, hard-close) |
| ENT-FIN-009 | EventTypeRule | FIN_EVENT_TYPE_RULE | none | VIEW · CREATE · UPDATE (deactivate) |
| ENT-FIN-010 | RuleLine | FIN_RULE_LINE | none | CREATE (with its dimension tags) |
| ENT-FIN-011 | RecurringTemplate | FIN_RECURRING_TEMPLATE | none | VIEW · CREATE · UPDATE (run, deactivate) |
| ENT-FIN-012 | RecurringTemplateLine | FIN_RECURRING_TEMPLATE_LINE | none | VIEW · CREATE (with its template) |
| ENT-FIN-013 | AllocationRule | FIN_ALLOCATION_RULE | none | VIEW · CREATE · UPDATE (run, deactivate) |
| ENT-FIN-014 | AllocationTarget | FIN_ALLOCATION_TARGET | none | VIEW · CREATE (with its rule) |
| ENT-FIN-015 | AccountMapping (v2) | FIN_ACCOUNT_MAPPING | none | VIEW · CREATE · UPDATE (change account, deactivate) |
| ENT-FIN-016 | RuleLineDimension (v2) | FIN_RULE_LINE_DIM | none | CREATE (with its rule line) |

**FIELD REGISTRY** — the DB Alignment Manifest below is the single binding (one row per DBF, property,
language type, XM). Read-only in every request: each PK, each audit column, docNo, postedAt, closedBy/closedAt,
and every `createdAt`-only child row. The v2 key columns of an account mapping (eventTypeCode, businessFieldCode,
businessValue) are writable on create and read-only afterwards (ADR-FIN-024).

**API REGISTRY**
| API | Operation | Verb | Path | ENT | Traces (REQ) |
|---|---|---|---|---|---|
| API-FIN-001 | search accounts | POST | /api/v1/fin/accounts/search | ENT-FIN-001 | REQ-FIN-001 |
| API-FIN-002 | create account | POST | /api/v1/fin/accounts | ENT-FIN-001 | REQ-FIN-001, REQ-FIN-002 |
| API-FIN-003 | update account | PUT | /api/v1/fin/accounts/{id} | ENT-FIN-001 | REQ-FIN-002 |
| API-FIN-004 | deactivate account | PUT | /api/v1/fin/accounts/{id}/deactivate | ENT-FIN-001 | REQ-FIN-003 |
| API-FIN-005 | search dimensions | POST | /api/v1/fin/dimensions/search | ENT-FIN-002 | REQ-FIN-004 |
| API-FIN-006 | create dimension | POST | /api/v1/fin/dimensions | ENT-FIN-002 | REQ-FIN-004 |
| API-FIN-007 | create dimension value | POST | /api/v1/fin/dimensions/{id}/values | ENT-FIN-003 | REQ-FIN-005, REQ-FIN-006 |
| API-FIN-008 | search dimension values | POST | /api/v1/fin/dimensions/values/search | ENT-FIN-003 | REQ-FIN-005 |
| API-FIN-009 | search event-type rules | POST | /api/v1/fin/event-rules/search | ENT-FIN-009 | REQ-FIN-007 |
| API-FIN-010 | create event-type rule | POST | /api/v1/fin/event-rules | ENT-FIN-009 | REQ-FIN-007 |
| API-FIN-011 | add rule line (v2: with its dimension tags) | POST | /api/v1/fin/event-rules/{id}/lines | ENT-FIN-010, ENT-FIN-016 | REQ-FIN-008, REQ-FIN-009, REQ-FIN-048, REQ-FIN-049, REQ-FIN-062, REQ-FIN-064, REQ-FIN-065 |
| API-FIN-012 | search templates | POST | /api/v1/fin/recurring-templates/search | ENT-FIN-011 | REQ-FIN-022 |
| API-FIN-013 | create template | POST | /api/v1/fin/recurring-templates | ENT-FIN-011, ENT-FIN-012 | REQ-FIN-022 |
| API-FIN-014 | run template | POST | /api/v1/fin/recurring-templates/{id}/run | ENT-FIN-011 | REQ-FIN-023, REQ-FIN-024 |
| API-FIN-015 | search allocation rules | POST | /api/v1/fin/allocation-rules/search | ENT-FIN-013 | REQ-FIN-025 |
| API-FIN-016 | create allocation rule | POST | /api/v1/fin/allocation-rules | ENT-FIN-013, ENT-FIN-014 | REQ-FIN-025 |
| API-FIN-017 | run allocation rule | POST | /api/v1/fin/allocation-rules/{id}/run | ENT-FIN-013 | REQ-FIN-026 |
| API-FIN-018 | search journal entries | POST | /api/v1/fin/journal-entries/search | ENT-FIN-004 | REQ-FIN-027 |
| API-FIN-019 | create manual entry | POST | /api/v1/fin/journal-entries | ENT-FIN-004 | REQ-FIN-014, REQ-FIN-015, REQ-FIN-017..021 |
| API-FIN-020 | build event entry (system; v2: mapped accounts, event dimensions, distinct rejections) | POST | /api/v1/fin/journal-entries/from-event | ENT-FIN-004 | REQ-FIN-010..013, REQ-FIN-017..021, REQ-FIN-047, REQ-FIN-050..053, REQ-FIN-063, REQ-FIN-066..070, REQ-FIN-072..076 |
| API-FIN-021 | reverse entry | POST | /api/v1/fin/journal-entries/{id}/reverse | ENT-FIN-004 | REQ-FIN-028, REQ-FIN-029, REQ-FIN-030, REQ-FIN-077 |
| API-FIN-022 | read entry | GET | /api/v1/fin/journal-entries/{id} | ENT-FIN-004 | REQ-FIN-016, REQ-FIN-027, REQ-FIN-070 |
| API-FIN-023 | create fiscal year | POST | /api/v1/fin/fiscal-years | ENT-FIN-007 | REQ-FIN-031 |
| API-FIN-024 | open period | PATCH | /api/v1/fin/fiscal-periods/{id}/open | ENT-FIN-008 | REQ-FIN-032 |
| API-FIN-025 | soft-close period | PATCH | /api/v1/fin/fiscal-periods/{id}/soft-close | ENT-FIN-008 | REQ-FIN-033 |
| API-FIN-026 | hard-close period (approval) | PATCH | /api/v1/fin/fiscal-periods/{id}/hard-close | ENT-FIN-008 | REQ-FIN-034, REQ-FIN-035, REQ-FIN-037, REQ-FIN-038 |
| API-FIN-027 | run year-end close | POST | /api/v1/fin/fiscal-years/{id}/year-end-close | ENT-FIN-007 | REQ-FIN-036 |
| API-FIN-028 | account ledger | GET | /api/v1/fin/reports/account-ledger | ENT-FIN-005 | REQ-FIN-039, REQ-FIN-046, REQ-FIN-071 |
| API-FIN-029 | trial balance | GET | /api/v1/fin/reports/trial-balance | ENT-FIN-005 | REQ-FIN-040, REQ-FIN-046 |
| API-FIN-030 | balance sheet | GET | /api/v1/fin/reports/balance-sheet | ENT-FIN-005 | REQ-FIN-041, REQ-FIN-046 |
| API-FIN-031 | income statement | GET | /api/v1/fin/reports/income-statement | ENT-FIN-005 | REQ-FIN-042, REQ-FIN-046 |
| API-FIN-032 | dimension report | GET | /api/v1/fin/reports/dimension | ENT-FIN-005 | REQ-FIN-043, REQ-FIN-071 |
| API-FIN-033 | search fiscal periods | POST | /api/v1/fin/fiscal-periods/search | ENT-FIN-008 | REQ-FIN-031 |
| API-FIN-034 | deactivate event-type rule | PUT | /api/v1/fin/event-rules/{id}/deactivate | ENT-FIN-009 | REQ-FIN-007 |
| API-FIN-035 | deactivate dimension value | PUT | /api/v1/fin/dimensions/values/{id}/deactivate | ENT-FIN-003 | REQ-FIN-005, REQ-FIN-021 |
| API-FIN-036 | deactivate template | PUT | /api/v1/fin/recurring-templates/{id}/deactivate | ENT-FIN-011 | REQ-FIN-022 |
| API-FIN-037 | deactivate allocation rule | PUT | /api/v1/fin/allocation-rules/{id}/deactivate | ENT-FIN-013 | REQ-FIN-025 |
| API-FIN-038 | search account mappings (v2) | POST | /api/v1/fin/account-mappings/search | ENT-FIN-015 | REQ-FIN-055 |
| API-FIN-039 | create account mapping (v2) | POST | /api/v1/fin/account-mappings | ENT-FIN-015 | REQ-FIN-054, REQ-FIN-059, REQ-FIN-060, REQ-FIN-061 |
| API-FIN-040 | update account mapping — the account only (v2) | PUT | /api/v1/fin/account-mappings/{id} | ENT-FIN-015 | REQ-FIN-056, REQ-FIN-058, REQ-FIN-060 |
| API-FIN-041 | deactivate account mapping (v2) | PUT | /api/v1/fin/account-mappings/{id}/deactivate | ENT-FIN-015 | REQ-FIN-057, REQ-FIN-058 |

**RULE REGISTRY** — scope, where each rule is enforced and the catalog row that carries its message. The ar/en
text of every v2 rule is inline in the endpoint that enforces it; every message is in the Error Catalog.
| RULE | Name | Scope | ar/en message | Enforced by | Catalog code |
|---|---|---|---|---|---|
| RULE-FIN-001 | no direct posting on a parent account | ENT-FIN-001 | ✓ / ✓ | API-FIN-002, API-FIN-003 | FIN-409-PARENT-NOT-LEAF-ELIGIBLE, FIN-409-HAS-CHILDREN |
| RULE-FIN-002 | unique code within a dimension | ENT-FIN-003 | ✓ / ✓ | API-FIN-007 | FIN-409-DIMVALUE-DUP |
| RULE-FIN-003 | exactly one remainder line / target | ENT-FIN-010, ENT-FIN-014 | ✓ / ✓ | API-FIN-011, API-FIN-016, API-FIN-017, API-FIN-020 | FIN-409-REMAINDER-COUNT, FIN-422-REMAINDER-MARKER |
| RULE-FIN-004 | duplicate event reference (MODIFIED v2 — both detections) | ENT-FIN-004 | ✓ / ✓ | API-FIN-020 | FIN-409-DUPLICATE-EVENT |
| RULE-FIN-005 | no active rule for the event type | ENT-FIN-009 | ✓ / ✓ | API-FIN-020 | FIN-404-NO-ACTIVE-RULE |
| RULE-FIN-006 | debits equal credits | ENT-FIN-004, ENT-FIN-005 | ✓ / ✓ | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021, API-FIN-027 | FIN-409-UNBALANCED |
| RULE-FIN-007 | post only to leaf, active accounts (v2: also the mapped one) | ENT-FIN-001, ENT-FIN-005 | ✓ / ✓ | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021, API-FIN-027 | FIN-409-NOT-POSTABLE-ACCOUNT |
| RULE-FIN-008 | period gate at post time | ENT-FIN-004, ENT-FIN-008 | ✓ / ✓ | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021 | FIN-409-PERIOD-NOT-OPEN |
| RULE-FIN-009 | dimension value validity (v2: also event-derived values) | ENT-FIN-006 | ✓ / ✓ | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021, API-FIN-027 | FIN-409-INVALID-DIMENSION |
| RULE-FIN-010 | the remainder line absorbs the rounding difference | ENT-FIN-005, ENT-FIN-010, ENT-FIN-014 | ✓ / ✓ | API-FIN-017, API-FIN-020 | FIN-422-REMAINDER-NOT-POSITIVE |
| RULE-FIN-011 | reversal is exact and linked | ENT-FIN-004, ENT-FIN-005 | ✓ / ✓ (informational — raises no rejection) | API-FIN-021 | — |
| RULE-FIN-012 | reversal posts to the current period when the original's is closed | ENT-FIN-004, ENT-FIN-008 | ✓ / ✓ (informational) | API-FIN-021 | — (no open period at all answers FIN-409-PERIOD-NOT-OPEN) |
| RULE-FIN-013 | no reversal of a non-posted or already-reversed entry | ENT-FIN-004 | ✓ / ✓ | API-FIN-021, API-FIN-014 | FIN-409-NOT-POSTED, FIN-409-ALREADY-REVERSED |
| RULE-FIN-014 | a hard-closed period is never reopened | ENT-FIN-008 | ✓ / ✓ | API-FIN-024, API-FIN-026 | FIN-409-NOT-REOPENABLE |
| RULE-FIN-015 | close approval is a distinct permission | ENT-FIN-008 | ✓ / ✓ | DEFERRED (no declaration surface in v2) — satisfied by SEC's permission gate on API-FIN-026 and API-FIN-027 | — (a denial is FIN-403-FORBIDDEN) |
| RULE-FIN-016 | a posted entry is locked | ENT-FIN-004, ENT-FIN-005 | ✓ / ✓ | by omission — no update or delete path exists | — |
| RULE-FIN-017 | fiscal year, period and document date cohere | ENT-FIN-004, ENT-FIN-008 | ✓ / ✓ | API-FIN-019 | FIN-400-PERIOD-NOT-IN-YEAR, FIN-400-DOCDATE-OUTSIDE-PERIOD |
| RULE-FIN-018 | only CONSTANT and MAPPING derivations | ENT-FIN-010 | ✓ / ✓ | API-FIN-011 | FIN-422-UNSUPPORTED-DERIVATION |
| RULE-FIN-019 | the derivation spec matches its type | ENT-FIN-010 | ✓ / ✓ | API-FIN-011 | FIN-422-DERIVATION-SPEC |
| RULE-FIN-020 | the business value must be mapped | ENT-FIN-015, ENT-FIN-010 | ✓ / ✓ | API-FIN-020 | FIN-422-UNMAPPED-VALUE |
| RULE-FIN-021 | the event carries the business field the rule names | ENT-FIN-010, ENT-FIN-016 | ✓ / ✓ | API-FIN-020 | FIN-422-MISSING-BUSINESS-FIELD |
| RULE-FIN-022 | one active mapping per key | ENT-FIN-015 | ✓ / ✓ | API-FIN-039 | FIN-409-MAPPING-DUP |
| RULE-FIN-023 | a mapped account is an active leaf on save | ENT-FIN-015 | ✓ / ✓ | API-FIN-039, API-FIN-040 | FIN-422-MAPPED-ACCOUNT-INELIGIBLE |
| RULE-FIN-024 | a payment-method value is a defined code | ENT-FIN-015 | ✓ / ✓ | API-FIN-039 | FIN-422-INVALID-PAYMENT-METHOD |
| RULE-FIN-025 | one tag per dimension on a rule line | ENT-FIN-016 | ✓ / ✓ | API-FIN-011 | FIN-422-DIMENSION-TAG-DUP |
| RULE-FIN-026 | a dimension tag matches its value source | ENT-FIN-016 | ✓ / ✓ | API-FIN-011 | FIN-422-DIMENSION-TAG-SOURCE |
| RULE-FIN-027 | an event dimension value resolves to a defined value | ENT-FIN-016, ENT-FIN-003 | ✓ / ✓ | API-FIN-020 | FIN-422-UNRESOLVED-DIMENSION-VALUE |
| RULE-FIN-028 | the event date falls in a defined period | ENT-FIN-008 | ✓ / ✓ | API-FIN-020 | FIN-422-NO-DEFINED-PERIOD |

**SCREEN REGISTRY** — one composite screen per SCR-REQ, one SEC_PAGES row each (SEC-BE).
| Screen | Page code | Type | ENT | Permission names |
|---|---|---|---|---|
| SCR-REQ-FIN-001 Chart of accounts | FIN_ACCOUNTS | Search + Entry | ENT-FIN-001 | PERM_FIN_ACCOUNTS_VIEW, PERM_FIN_ACCOUNTS_CREATE, PERM_FIN_ACCOUNTS_UPDATE |
| SCR-REQ-FIN-002 Dimensions | FIN_DIMENSIONS | Master + Detail | ENT-FIN-002, ENT-FIN-003 | PERM_FIN_DIMENSIONS_VIEW, PERM_FIN_DIMENSIONS_CREATE, PERM_FIN_DIMENSIONS_UPDATE |
| SCR-REQ-FIN-003 Engine rules (v2 MODIFIED) | FIN_RULES | Master + Detail | ENT-FIN-009, ENT-FIN-010, ENT-FIN-016 | PERM_FIN_RULES_VIEW, PERM_FIN_RULES_CREATE, PERM_FIN_RULES_UPDATE |
| SCR-REQ-FIN-004 Recurring templates | FIN_RECURRING_TEMPLATES | Master + Detail | ENT-FIN-011, ENT-FIN-012 | PERM_FIN_RECURRING_TEMPLATES_VIEW, PERM_FIN_RECURRING_TEMPLATES_CREATE, PERM_FIN_RECURRING_TEMPLATES_UPDATE |
| SCR-REQ-FIN-005 Allocation rules | FIN_ALLOCATION_RULES | Master + Detail | ENT-FIN-013, ENT-FIN-014 | PERM_FIN_ALLOCATION_RULES_VIEW, PERM_FIN_ALLOCATION_RULES_CREATE, PERM_FIN_ALLOCATION_RULES_UPDATE |
| SCR-REQ-FIN-006 Journal entries (v2 MODIFIED) | FIN_JOURNAL_ENTRIES | Search + Entry | ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 | PERM_FIN_JOURNAL_ENTRIES_VIEW, PERM_FIN_JOURNAL_ENTRIES_CREATE, PERM_FIN_JOURNAL_ENTRIES_REVERSE |
| SCR-REQ-FIN-007 Fiscal periods and years | FIN_PERIODS | Master + Detail | ENT-FIN-007, ENT-FIN-008 | PERM_FIN_PERIODS_VIEW, PERM_FIN_PERIODS_CREATE, PERM_FIN_PERIODS_UPDATE, PERM_FIN_PERIODS_CLOSE_APPROVE |
| SCR-REQ-FIN-008 Account ledger | FIN_ACCOUNT_LEDGER | Report | ENT-FIN-005 | PERM_FIN_ACCOUNT_LEDGER_VIEW |
| SCR-REQ-FIN-009 Trial balance | FIN_TRIAL_BALANCE | Report | ENT-FIN-005 | PERM_FIN_TRIAL_BALANCE_VIEW |
| SCR-REQ-FIN-010 Balance sheet | FIN_BALANCE_SHEET | Report | ENT-FIN-005 | PERM_FIN_BALANCE_SHEET_VIEW |
| SCR-REQ-FIN-011 Income statement | FIN_INCOME_STATEMENT | Report | ENT-FIN-005 | PERM_FIN_INCOME_STATEMENT_VIEW |
| SCR-REQ-FIN-012 Dimension reports | FIN_DIMENSION_REPORTS | Report | ENT-FIN-005, ENT-FIN-006 | PERM_FIN_DIMENSION_REPORTS_VIEW |
| SCR-REQ-FIN-013 Account mappings (v2 ADDED) | FIN_ACCOUNT_MAPPINGS | Search + Entry | ENT-FIN-015 | PERM_FIN_ACCOUNT_MAPPINGS_VIEW, PERM_FIN_ACCOUNT_MAPPINGS_CREATE, PERM_FIN_ACCOUNT_MAPPINGS_UPDATE |

**LOOKUP REGISTRY** — every value is runtime-loaded from MDL (XM-FIN-001); no API or field spec hardcodes one.
| Lookup key | Used in field (`DBF-*`) | `ENT-*` | Table owner module |
|---|---|---|---|
| ACCOUNT_TYPE | DBF-FIN-005 | ENT-FIN-001 | MDL |
| DEBIT_CREDIT | DBF-FIN-006, DBF-FIN-056, DBF-FIN-105, DBF-FIN-127 | ENT-FIN-001, ENT-FIN-005, ENT-FIN-010, ENT-FIN-012 | MDL |
| PERIOD_STATE | DBF-FIN-082 | ENT-FIN-008 | MDL |
| FISCAL_YEAR_STATUS | DBF-FIN-069 | ENT-FIN-007 | MDL |
| JOURNAL_TYPE | DBF-FIN-039 | ENT-FIN-004 | MDL |
| JOURNAL_STATUS | DBF-FIN-040 | ENT-FIN-004 | MDL |
| ACCOUNTING_EVENT_TYPE | DBF-FIN-090, DBF-FIN-150 | ENT-FIN-009, ENT-FIN-015 | MDL |
| PAYMENT_METHOD | DBF-FIN-152 (only when the business field is PAYMENT_METHOD) | ENT-FIN-015 | MDL |
| ACCOUNT_DERIVATION_TYPE | DBF-FIN-101 | ENT-FIN-010 | MDL |
| AMOUNT_SOURCE_TYPE | DBF-FIN-103 | ENT-FIN-010 | MDL |
| DISTRIBUTION_TYPE | DBF-FIN-106, DBF-FIN-144 | ENT-FIN-010, ENT-FIN-014 | MDL |
| RECURRING_SCHEDULE_TYPE | DBF-FIN-112 | ENT-FIN-011 | MDL |
| RECURRING_FREQUENCY | DBF-FIN-113 | ENT-FIN-011 | MDL |
| FIN_EVENT_BUSINESS_FIELD | DBF-FIN-148, DBF-FIN-151, DBF-FIN-161 | ENT-FIN-010, ENT-FIN-015, ENT-FIN-016 | MDL |
| FIN_DIMENSION_VALUE_SOURCE | DBF-FIN-160 | ENT-FIN-016 | MDL |

**QRC SUMMARY** — QR-FIN-001 … QR-FIN-061; v2 adds QR-FIN-050 … QR-FIN-061 (Query Reference Catalog below).

```
DB ALIGNMENT      the manifest below — aligned; v2 corrects three wrong DBF citations of v1 (ADR-FIN-029)
XM STATUS         0 deferred — XM-FIN-001 ACTIVE (scope widened in v2); XM-FIN-002 RETIRED
SECURITY          13 screens; grants to the SYS_ADMIN role plus the least-privilege FIN_CLOSE_APPROVER role
DBF ROWS          165
XM ROWS           2
API ROWS          41
QR ROWS           61
```

## DB Alignment Manifest — FIN v2
Canonical binding of plan properties to db-script fields. Column names, database types and SRS references are read
from the db-script by `DBF-*`, never repeated here. Status: ✓ aligned · ✗ type mismatch · ⏸ deferred XM. No row is
✗ or ⏸. XM-FIN-001 binds every lookup-backed column: the 16 of v1 plus 6 in v2 (DBF-FIN-148, DBF-FIN-150,
DBF-FIN-151, DBF-FIN-152, DBF-FIN-160, DBF-FIN-161). Every required column that no endpoint writes says so on its
row; the only one is DBF-FIN-147. Language types come from CORE's type-mapping table.

| DBF | ENT | plan property | plan type | XM | status |
|---|---|---|---|---|---|
| DBF-FIN-001 | ENT-FIN-001 | accountPk | Long | — | ✓ |
| DBF-FIN-002 | ENT-FIN-001 | code | String | — | ✓ |
| DBF-FIN-003 | ENT-FIN-001 | nameAr | String | — | ✓ |
| DBF-FIN-004 | ENT-FIN-001 | nameEn | String | — | ✓ |
| DBF-FIN-005 | ENT-FIN-001 | accountTypeCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-006 | ENT-FIN-001 | natureCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-007 | ENT-FIN-001 | parentAccountId | Long | — | ✓ |
| DBF-FIN-008 | ENT-FIN-001 | isLeafFl | Boolean | — | ✓ |
| DBF-FIN-009 | ENT-FIN-001 | isActiveFl | Boolean | — | ✓ |
| DBF-FIN-147 | ENT-FIN-001 | isRetainedEarningsFl | Boolean | — | ✓ seeded as data, never through an API (db-script §1) — see SEC-BE's bootstrap rows |
| DBF-FIN-010 | ENT-FIN-001 | createdBy | String | — | ✓ |
| DBF-FIN-011 | ENT-FIN-001 | createdAt | Instant | — | ✓ |
| DBF-FIN-012 | ENT-FIN-001 | updatedBy | String | — | ✓ |
| DBF-FIN-013 | ENT-FIN-001 | updatedAt | Instant | — | ✓ |
| DBF-FIN-014 | ENT-FIN-002 | dimensionPk | Long | — | ✓ |
| DBF-FIN-015 | ENT-FIN-002 | code | String | — | ✓ |
| DBF-FIN-016 | ENT-FIN-002 | nameAr | String | — | ✓ |
| DBF-FIN-017 | ENT-FIN-002 | nameEn | String | — | ✓ |
| DBF-FIN-018 | ENT-FIN-002 | isActiveFl | Boolean | — | ✓ |
| DBF-FIN-019 | ENT-FIN-002 | createdBy | String | — | ✓ |
| DBF-FIN-020 | ENT-FIN-002 | createdAt | Instant | — | ✓ |
| DBF-FIN-021 | ENT-FIN-002 | updatedBy | String | — | ✓ |
| DBF-FIN-022 | ENT-FIN-002 | updatedAt | Instant | — | ✓ |
| DBF-FIN-023 | ENT-FIN-003 | dimensionValuePk | Long | — | ✓ |
| DBF-FIN-024 | ENT-FIN-003 | dimensionId | Long | — | ✓ |
| DBF-FIN-025 | ENT-FIN-003 | code | String | — | ✓ |
| DBF-FIN-026 | ENT-FIN-003 | nameAr | String | — | ✓ |
| DBF-FIN-027 | ENT-FIN-003 | nameEn | String | — | ✓ |
| DBF-FIN-028 | ENT-FIN-003 | sortOrder | Integer | — | ✓ |
| DBF-FIN-029 | ENT-FIN-003 | isActiveFl | Boolean | — | ✓ |
| DBF-FIN-030 | ENT-FIN-003 | createdBy | String | — | ✓ |
| DBF-FIN-031 | ENT-FIN-003 | createdAt | Instant | — | ✓ |
| DBF-FIN-032 | ENT-FIN-003 | updatedBy | String | — | ✓ |
| DBF-FIN-033 | ENT-FIN-003 | updatedAt | Instant | — | ✓ |
| DBF-FIN-034 | ENT-FIN-004 | journalEntryPk | Long | — | ✓ |
| DBF-FIN-035 | ENT-FIN-004 | docNo | String | — | ✓ |
| DBF-FIN-036 | ENT-FIN-004 | docDate | LocalDate | — | ✓ |
| DBF-FIN-037 | ENT-FIN-004 | fiscalYearId | Long | — | ✓ |
| DBF-FIN-038 | ENT-FIN-004 | periodId | Long | — | ✓ |
| DBF-FIN-039 | ENT-FIN-004 | journalTypeCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-040 | ENT-FIN-004 | statusCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-041 | ENT-FIN-004 | eventReference | String | — | ✓ |
| DBF-FIN-042 | ENT-FIN-004 | originalEntryId | Long | — | ✓ |
| DBF-FIN-043 | ENT-FIN-004 | reversalEntryId | Long | — | ✓ |
| DBF-FIN-044 | ENT-FIN-004 | descriptionAr | String | — | ✓ |
| DBF-FIN-045 | ENT-FIN-004 | descriptionEn | String | — | ✓ |
| DBF-FIN-046 | ENT-FIN-004 | postedAt | Instant | — | ✓ |
| DBF-FIN-047 | ENT-FIN-004 | createdBy | String | — | ✓ |
| DBF-FIN-048 | ENT-FIN-004 | createdAt | Instant | — | ✓ |
| DBF-FIN-049 | ENT-FIN-004 | updatedBy | String | — | ✓ |
| DBF-FIN-050 | ENT-FIN-004 | updatedAt | Instant | — | ✓ |
| DBF-FIN-051 | ENT-FIN-005 | journalLinePk | Long | — | ✓ |
| DBF-FIN-052 | ENT-FIN-005 | journalEntryId | Long | — | ✓ |
| DBF-FIN-053 | ENT-FIN-005 | lineNo | Integer | — | ✓ |
| DBF-FIN-054 | ENT-FIN-005 | accountId | Long | — | ✓ |
| DBF-FIN-055 | ENT-FIN-005 | amount | BigDecimal | — | ✓ |
| DBF-FIN-056 | ENT-FIN-005 | directionCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-057 | ENT-FIN-005 | isRemainderFl | Boolean | — | ✓ |
| DBF-FIN-058 | ENT-FIN-005 | descriptionAr | String | — | ✓ |
| DBF-FIN-059 | ENT-FIN-005 | descriptionEn | String | — | ✓ |
| DBF-FIN-060 | ENT-FIN-005 | createdAt | Instant | — | ✓ |
| DBF-FIN-061 | ENT-FIN-006 | journalLineDimensionPk | Long | — | ✓ |
| DBF-FIN-062 | ENT-FIN-006 | journalLineId | Long | — | ✓ |
| DBF-FIN-063 | ENT-FIN-006 | dimensionId | Long | — | ✓ |
| DBF-FIN-064 | ENT-FIN-006 | dimensionValueId | Long | — | ✓ |
| DBF-FIN-065 | ENT-FIN-007 | fiscalYearPk | Long | — | ✓ |
| DBF-FIN-066 | ENT-FIN-007 | code | String | — | ✓ |
| DBF-FIN-067 | ENT-FIN-007 | startDate | LocalDate | — | ✓ |
| DBF-FIN-068 | ENT-FIN-007 | endDate | LocalDate | — | ✓ |
| DBF-FIN-069 | ENT-FIN-007 | statusCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-070 | ENT-FIN-007 | isActiveFl | Boolean | — | ✓ |
| DBF-FIN-071 | ENT-FIN-007 | createdBy | String | — | ✓ |
| DBF-FIN-072 | ENT-FIN-007 | createdAt | Instant | — | ✓ |
| DBF-FIN-073 | ENT-FIN-007 | updatedBy | String | — | ✓ |
| DBF-FIN-074 | ENT-FIN-007 | updatedAt | Instant | — | ✓ |
| DBF-FIN-075 | ENT-FIN-008 | fiscalPeriodPk | Long | — | ✓ |
| DBF-FIN-076 | ENT-FIN-008 | fiscalYearId | Long | — | ✓ |
| DBF-FIN-077 | ENT-FIN-008 | periodNo | Integer | — | ✓ |
| DBF-FIN-078 | ENT-FIN-008 | nameAr | String | — | ✓ |
| DBF-FIN-079 | ENT-FIN-008 | nameEn | String | — | ✓ |
| DBF-FIN-080 | ENT-FIN-008 | startDate | LocalDate | — | ✓ |
| DBF-FIN-081 | ENT-FIN-008 | endDate | LocalDate | — | ✓ |
| DBF-FIN-082 | ENT-FIN-008 | statusCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-083 | ENT-FIN-008 | closedBy | String | — | ✓ |
| DBF-FIN-084 | ENT-FIN-008 | closedAt | Instant | — | ✓ |
| DBF-FIN-085 | ENT-FIN-008 | createdBy | String | — | ✓ |
| DBF-FIN-086 | ENT-FIN-008 | createdAt | Instant | — | ✓ |
| DBF-FIN-087 | ENT-FIN-008 | updatedBy | String | — | ✓ |
| DBF-FIN-088 | ENT-FIN-008 | updatedAt | Instant | — | ✓ |
| DBF-FIN-089 | ENT-FIN-009 | eventTypeRulePk | Long | — | ✓ |
| DBF-FIN-090 | ENT-FIN-009 | eventTypeCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-091 | ENT-FIN-009 | nameAr | String | — | ✓ |
| DBF-FIN-092 | ENT-FIN-009 | nameEn | String | — | ✓ |
| DBF-FIN-093 | ENT-FIN-009 | isActiveFl | Boolean | — | ✓ |
| DBF-FIN-094 | ENT-FIN-009 | createdBy | String | — | ✓ |
| DBF-FIN-095 | ENT-FIN-009 | createdAt | Instant | — | ✓ |
| DBF-FIN-096 | ENT-FIN-009 | updatedBy | String | — | ✓ |
| DBF-FIN-097 | ENT-FIN-009 | updatedAt | Instant | — | ✓ |
| DBF-FIN-098 | ENT-FIN-010 | ruleLinePk | Long | — | ✓ |
| DBF-FIN-099 | ENT-FIN-010 | eventTypeRuleId | Long | — | ✓ |
| DBF-FIN-100 | ENT-FIN-010 | lineNo | Integer | — | ✓ |
| DBF-FIN-101 | ENT-FIN-010 | accountDerivationTypeCode | String | XM-FIN-001 | ✓ (v2: CONSTANT / MAPPING only) |
| DBF-FIN-102 | ENT-FIN-010 | accountDerivationValue | String | — | ✓ (v2: NULL-able, CONSTANT lines only) |
| DBF-FIN-148 | ENT-FIN-010 | accountBusinessFieldCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-103 | ENT-FIN-010 | amountSourceTypeCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-104 | ENT-FIN-010 | amountSourceValue | String | — | ✓ |
| DBF-FIN-105 | ENT-FIN-010 | directionCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-106 | ENT-FIN-010 | distributionTypeCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-107 | ENT-FIN-010 | isRemainderFl | Boolean | — | ✓ |
| DBF-FIN-108 | ENT-FIN-010 | createdAt | Instant | — | ✓ |
| DBF-FIN-109 | ENT-FIN-011 | recurringTemplatePk | Long | — | ✓ |
| DBF-FIN-110 | ENT-FIN-011 | nameAr | String | — | ✓ |
| DBF-FIN-111 | ENT-FIN-011 | nameEn | String | — | ✓ |
| DBF-FIN-112 | ENT-FIN-011 | scheduleTypeCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-113 | ENT-FIN-011 | frequencyCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-114 | ENT-FIN-011 | startDate | LocalDate | — | ✓ |
| DBF-FIN-115 | ENT-FIN-011 | nextRunDate | LocalDate | — | ✓ |
| DBF-FIN-116 | ENT-FIN-011 | endDate | LocalDate | — | ✓ |
| DBF-FIN-117 | ENT-FIN-011 | isActiveFl | Boolean | — | ✓ |
| DBF-FIN-118 | ENT-FIN-011 | createdBy | String | — | ✓ |
| DBF-FIN-119 | ENT-FIN-011 | createdAt | Instant | — | ✓ |
| DBF-FIN-120 | ENT-FIN-011 | updatedBy | String | — | ✓ |
| DBF-FIN-121 | ENT-FIN-011 | updatedAt | Instant | — | ✓ |
| DBF-FIN-122 | ENT-FIN-012 | recurringTemplateLinePk | Long | — | ✓ |
| DBF-FIN-123 | ENT-FIN-012 | recurringTemplateId | Long | — | ✓ |
| DBF-FIN-124 | ENT-FIN-012 | lineNo | Integer | — | ✓ |
| DBF-FIN-125 | ENT-FIN-012 | accountId | Long | — | ✓ |
| DBF-FIN-126 | ENT-FIN-012 | amount | BigDecimal | — | ✓ |
| DBF-FIN-127 | ENT-FIN-012 | directionCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-128 | ENT-FIN-012 | dimensionValueId | Long | — | ✓ |
| DBF-FIN-129 | ENT-FIN-012 | createdAt | Instant | — | ✓ |
| DBF-FIN-130 | ENT-FIN-013 | allocationRulePk | Long | — | ✓ |
| DBF-FIN-131 | ENT-FIN-013 | nameAr | String | — | ✓ |
| DBF-FIN-132 | ENT-FIN-013 | nameEn | String | — | ✓ |
| DBF-FIN-133 | ENT-FIN-013 | sourceAccountId | Long | — | ✓ |
| DBF-FIN-134 | ENT-FIN-013 | isActiveFl | Boolean | — | ✓ |
| DBF-FIN-135 | ENT-FIN-013 | createdBy | String | — | ✓ |
| DBF-FIN-136 | ENT-FIN-013 | createdAt | Instant | — | ✓ |
| DBF-FIN-137 | ENT-FIN-013 | updatedBy | String | — | ✓ |
| DBF-FIN-138 | ENT-FIN-013 | updatedAt | Instant | — | ✓ |
| DBF-FIN-139 | ENT-FIN-014 | allocationTargetPk | Long | — | ✓ |
| DBF-FIN-140 | ENT-FIN-014 | allocationRuleId | Long | — | ✓ |
| DBF-FIN-141 | ENT-FIN-014 | lineNo | Integer | — | ✓ |
| DBF-FIN-142 | ENT-FIN-014 | targetAccountId | Long | — | ✓ |
| DBF-FIN-143 | ENT-FIN-014 | dimensionValueId | Long | — | ✓ |
| DBF-FIN-144 | ENT-FIN-014 | distributionTypeCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-145 | ENT-FIN-014 | distributionValue | BigDecimal | — | ✓ |
| DBF-FIN-146 | ENT-FIN-014 | isRemainderFl | Boolean | — | ✓ |
| DBF-FIN-149 | ENT-FIN-015 | accountMappingPk | Long | — | ✓ |
| DBF-FIN-150 | ENT-FIN-015 | eventTypeCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-151 | ENT-FIN-015 | businessFieldCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-152 | ENT-FIN-015 | businessValue | String | XM-FIN-001 | ✓ (XM-FIN-001 applies only when businessFieldCode = PAYMENT_METHOD — RULE-FIN-024) |
| DBF-FIN-153 | ENT-FIN-015 | accountId | Long | — | ✓ |
| DBF-FIN-154 | ENT-FIN-015 | isActiveFl | Boolean | — | ✓ |
| DBF-FIN-155 | ENT-FIN-015 | createdBy | String | — | ✓ |
| DBF-FIN-156 | ENT-FIN-015 | createdAt | Instant | — | ✓ |
| DBF-FIN-157 | ENT-FIN-015 | updatedBy | String | — | ✓ |
| DBF-FIN-158 | ENT-FIN-015 | updatedAt | Instant | — | ✓ |
| DBF-FIN-159 | ENT-FIN-016 | ruleLineDimensionPk | Long | — | ✓ |
| DBF-FIN-160 | ENT-FIN-016 | valueSourceCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-161 | ENT-FIN-016 | businessFieldCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-162 | ENT-FIN-016 | ruleLineId | Long | — | ✓ |
| DBF-FIN-163 | ENT-FIN-016 | dimensionId | Long | — | ✓ |
| DBF-FIN-164 | ENT-FIN-016 | dimensionValueId | Long | — | ✓ |
| DBF-FIN-165 | ENT-FIN-016 | createdAt | Instant | — | ✓ |

## Query Reference Catalog (QR-FIN-*)

> Logical specification only, never executable code. The implementer rewrites every entry with the real entity
> classes and the project's query strategy. The v1 entries stand as delivered; the v2 entries and the three
> modified ones carry the full §5 form below the summary table.

| QR | Operation | API | Entity | Kind | Intent |
|---|---|---|---|---|---|
| QR-FIN-001 | FIND_BY_CRITERIA | API-FIN-001 | ENT-FIN-001 | search | search accounts |
| QR-FIN-002 | SAVE | API-FIN-002 | ENT-FIN-001 | create | create account |
| QR-FIN-003 | UPDATE | API-FIN-003 | ENT-FIN-001 | update | update account |
| QR-FIN-004 | UPDATE | API-FIN-004 | ENT-FIN-001 | deactivate | deactivate account |
| QR-FIN-005 | EXISTS | API-FIN-002 | ENT-FIN-001 | uniqueness | account code unique |
| QR-FIN-006 | EXISTS | API-FIN-002, API-FIN-003 | ENT-FIN-001 | RULE-FIN-001 | account has no children before marking leaf |
| QR-FIN-007 | FIND_BY_CRITERIA | API-FIN-005 | ENT-FIN-002 | search | search dimensions |
| QR-FIN-008 | SAVE | API-FIN-006 | ENT-FIN-002 | create | create dimension |
| QR-FIN-009 | SAVE | API-FIN-007 | ENT-FIN-003 | create | create dimension value |
| QR-FIN-010 | EXISTS | API-FIN-007 | ENT-FIN-003 | RULE-FIN-002 | dimension value code unique within dimension |
| QR-FIN-011 | FIND_BY_CRITERIA | API-FIN-008 | ENT-FIN-003 | search | search dimension values |
| QR-FIN-012 | FIND_BY_CRITERIA | API-FIN-009 | ENT-FIN-009 | search | search event-type rules |
| QR-FIN-013 | SAVE | API-FIN-010 | ENT-FIN-009 | create | create event-type rule |
| QR-FIN-014 | EXISTS | API-FIN-010 | ENT-FIN-009 | uniqueness | one active rule per event type |
| QR-FIN-015 | SAVE | API-FIN-011 | ENT-FIN-010 | create | add rule line — MODIFIED v2: also writes accountBusinessFieldCode; accountDerivationValue stays empty on a MAPPING line |
| QR-FIN-016 | EXISTS | API-FIN-011, API-FIN-016 | ENT-FIN-010, ENT-FIN-014 | RULE-FIN-003 | exactly one remainder line/target when a percentage distribution exists (scope widened here to match the id definition below and the code) |
| QR-FIN-017 | FIND_BY_CRITERIA | API-FIN-012 | ENT-FIN-011 | search | search templates |
| QR-FIN-018 | SAVE | API-FIN-013 | ENT-FIN-011, ENT-FIN-012 | create | create template with lines |
| QR-FIN-019 | FIND_ONE | API-FIN-014 | ENT-FIN-011 | run | load the template to run, by id (inherited `findById`; no due-date selection query exists — FIN exposes no scheduler endpoint) |
| QR-FIN-020 | FIND_BY_CRITERIA | API-FIN-015 | ENT-FIN-013 | search | search allocation rules |
| QR-FIN-021 | SAVE | API-FIN-016 | ENT-FIN-013, ENT-FIN-014 | create | create allocation rule with targets |
| QR-FIN-022 | FIND_ONE | API-FIN-017 | ENT-FIN-013 | run | load allocation rule + current source balance (via QR-FIN-043 — the shared balance aggregation; the earlier citation of QR-FIN-040, the hard-closed-period check, was wrong) |
| QR-FIN-023 | FIND_BY_CRITERIA | API-FIN-018 | ENT-FIN-004 | search | search journal entries |
| QR-FIN-024 | SAVE | API-FIN-019 | ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 | create | build manual entry (DRAFT) |
| QR-FIN-025 | SAVE | API-FIN-020 | ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 | create | build event entry from its rule — MODIFIED v2: each line's account is the constant or the mapped one, each resolved tag writes a JournalLineDimension row (REQ-FIN-070), the header insert is flushed (RULE-FIN-004) |
| QR-FIN-026 | EXISTS | API-FIN-020 | ENT-FIN-004 | RULE-FIN-004 | duplicate eventReference — MODIFIED v2: the friendly pre-check; UQ_FIN_JOURNAL_ENTRY_EVENT_REF is the guarantee |
| QR-FIN-027 | EXISTS | API-FIN-020 | ENT-FIN-009 | RULE-FIN-005 | active rule exists for event type |
| QR-FIN-028 | AGGREGATE | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017 | ENT-FIN-005 | RULE-FIN-010 | compute remainder-line amount for a compound/percentage distribution |
| QR-FIN-029 | EXISTS | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017 | ENT-FIN-005 | RULE-FIN-006 | debits = credits |
| QR-FIN-030 | EXISTS | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017 | ENT-FIN-001 | RULE-FIN-007 | every line's account is leaf + active |
| QR-FIN-031 | EXISTS | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017 | ENT-FIN-008 | RULE-FIN-008 | entry's period is Open at post time (not applied to API-FIN-027's CLOSING/OPENING entries — exempted by RULE-FIN-008 itself) |
| QR-FIN-032 | EXISTS | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017 | ENT-FIN-006 | RULE-FIN-009 | every dimension value valid + active |
| QR-FIN-033 | UPDATE | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017 | ENT-FIN-004 | post | flip DRAFT → POSTED, set postedAt (RULE-FIN-016 lock takes effect) |
| QR-FIN-034 | SAVE | API-FIN-021 | ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 | create | build reversal entry (RULE-FIN-011) |
| QR-FIN-035 | EXISTS | API-FIN-021, 014 | ENT-FIN-004 | RULE-FIN-013 | original entry is POSTED and not already reversed |
| QR-FIN-036 | FIND_ONE | API-FIN-021 | ENT-FIN-008 | RULE-FIN-012 | current open period, if original's is closed |
| QR-FIN-037 | FIND_ONE | API-FIN-022 | ENT-FIN-004 | read | read one entry with lines |
| QR-FIN-038 | SAVE | API-FIN-023 | ENT-FIN-007, ENT-FIN-008 | create | create fiscal year + periods |
| QR-FIN-039 | UPDATE | API-FIN-024, API-FIN-025, API-FIN-026 | ENT-FIN-008 | transition | open / soft-close / hard-close a period |
| QR-FIN-040 | EXISTS | API-FIN-026 | ENT-FIN-008 | RULE-FIN-014 | period not already Hard Closed before reopen attempts |
| QR-FIN-041 | AGGREGATE | API-FIN-027 | ENT-FIN-004, ENT-FIN-005 | year-end | compute closing balances, build closing + opening entries |
| QR-FIN-042 | FIND_BY_CRITERIA | API-FIN-028 | ENT-FIN-005 | report | account ledger — POSTED lines for one account, oldest first; the running balance is accumulated by the service, deliberately not returned by the query |
| QR-FIN-043 | AGGREGATE | API-FIN-029, API-FIN-030, API-FIN-031 | ENT-FIN-005 | report | trial balance / balance sheet / income statement account balances (shared aggregation, filtered per report by accountTypeCode) |
| QR-FIN-044 | AGGREGATE | API-FIN-032 | ENT-FIN-005, ENT-FIN-006 | report | dimension report (account × dimension value) |
| QR-FIN-045 | FIND_ONE | API-FIN-027 | ENT-FIN-001 | year-end | the single Retained Earnings account (DBF-FIN-147), lowest pk first |
| QR-FIN-046 | EXISTS | API-FIN-006 | ENT-FIN-002 | uniqueness | dimension code unique |
| QR-FIN-047 | EXISTS | API-FIN-023 | ENT-FIN-007 | uniqueness | fiscal year code unique |
| QR-FIN-048 | FIND_ONE | API-FIN-027 | ENT-FIN-007 | year-end | the next fiscal year, found by its startDate = this year's endDate + 1 day (the opening entry's target) |
| QR-FIN-049 | FIND_BY_CRITERIA | API-FIN-023, API-FIN-027, API-FIN-014 | ENT-FIN-008 | read | every period of one fiscal year, JOIN FETCH on the year, ordered by periodNo |
| QR-FIN-050 | FIND_BY_CRITERIA | API-FIN-038 | ENT-FIN-015 | search | search account mappings, with the account's display columns (ADR-FIN-031) |
| QR-FIN-051 | SAVE | API-FIN-039 | ENT-FIN-015 | create | create account mapping |
| QR-FIN-052 | EXISTS | API-FIN-039 | ENT-FIN-015 | RULE-FIN-022 | an active mapping already holds the key |
| QR-FIN-053 | UPDATE | API-FIN-040 | ENT-FIN-015 | update | change a mapping's account |
| QR-FIN-054 | UPDATE | API-FIN-041 | ENT-FIN-015 | deactivate | deactivate a mapping |
| QR-FIN-055 | FIND_ONE | API-FIN-020 | ENT-FIN-015 | RULE-FIN-020 | the active mapping for (event type, business field, value) |
| QR-FIN-056 | SAVE | API-FIN-011 | ENT-FIN-016 | create | a rule line's dimension tags, with the line |
| QR-FIN-057 | FIND_BY_CRITERIA | API-FIN-020 | ENT-FIN-016 | read | the tags of the active rule's lines |
| QR-FIN-058 | FIND_ONE | API-FIN-020 | ENT-FIN-003 | RULE-FIN-027 | the dimension value with a given code in a given dimension |
| QR-FIN-059 | FIND_ONE | API-FIN-020 | ENT-FIN-008 | RULE-FIN-028 | the fiscal period whose span covers a date |
| QR-FIN-060 | FIND_ONE | API-FIN-039, API-FIN-040 | ENT-FIN-001 | RULE-FIN-023 | the account a mapping names, with its leaf and active flags |
| QR-FIN-061 | FIND_ONE | API-FIN-011 | ENT-FIN-002, ENT-FIN-003 | RULE-FIN-026 | a tag's dimension and its CONSTANT value, by id |

#### QR-FIN-015 — add rule line (MODIFIED v2)
Phase        : DATA-DOM (ENT-FIN-010)
API          : API-FIN-011 — add rule line
Entity       : ENT-FIN-010
Operation    : SAVE
Intent       : insert one rule line; v2 also writes accountBusinessFieldCode, and accountDerivationValue stays empty on a MAPPING line
Logical spec : INSERT INTO FIN_RULE_LINE (rule_line_pk ← nextval SEQ_FIN_RULE_LINE, event_type_rule_id, line_no, account_derivation_type_code, account_derivation_value, account_business_field_code, amount_source_type_code, amount_source_value, direction_code, distribution_type_code, is_remainder_fl)
Join         : NONE
Transaction  : READ_WRITE — the same transaction as the line's tags (QR-FIN-056)
Locking      : the parent FIN_EVENT_TYPE_RULE row is loaded PESSIMISTIC_WRITE before line_no is read (API-FIN-011 Concurrency)
Pagination   : NO
Filters      : —
Result shape : full entity
Null handling: account_derivation_value NULL on MAPPING, account_business_field_code NULL on CONSTANT (CHK_FIN_RULE_LINE_DERIVATION_SPEC)

#### QR-FIN-025 — build event entry from its rule (MODIFIED v2)
Phase        : SVC-API (SVC-API-INT)
API          : API-FIN-020 — build event entry
Entity       : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006
Operation    : SAVE (read half: the rule's lines)
Intent       : read the active rule's lines in lineNo order, then insert the header, its lines and one JournalLineDimension row per resolved tag
Logical spec : SELECT … FROM FIN_RULE_LINE WHERE event_type_rule_id = :ruleId ORDER BY line_no; INSERT FIN_JOURNAL_ENTRY (flushed at once, so a duplicate event_reference is refused inside the transaction); INSERT FIN_JOURNAL_LINE per built line; INSERT FIN_JOURNAL_LINE_DIM per resolved tag
Join         : NONE
Transaction  : READ_WRITE — one transaction from the duplicate pre-check to the post (QR-FIN-033)
Locking      : the fiscal-year row lock guards the docNo series (as API-FIN-019); UQ_FIN_JOURNAL_ENTRY_EVENT_REF decides a concurrent duplicate (QR-FIN-026)
Pagination   : NO
Filters      : —
Result shape : full entity with lines and line dimensions
Null handling: event_reference is set on every event-built header; description_ar/description_en as sent

#### QR-FIN-026 — duplicate event reference (MODIFIED v2)
Phase        : SVC-API (SVC-API-INT)
API          : API-FIN-020 — build event entry
Entity       : ENT-FIN-004
Operation    : EXISTS
Intent       : has this event reference already produced an entry? The friendly half of RULE-FIN-004
Logical spec : SELECT EXISTS (SELECT 1 FROM FIN_JOURNAL_ENTRY WHERE event_reference = :eventReference)
Join         : NONE
Transaction  : READ_ONLY inside the build transaction
Locking      : none on the read. The read decides and the write follows, so two concurrent callers both pass it; UQ_FIN_JOURNAL_ENTRY_EVENT_REF refuses the second insert and FinPersistenceRefusalAdvisor answers FIN-409-DUPLICATE-EVENT (ADR-FIN-030)
Pagination   : NO
Filters      : event_reference EXACT
Result shape : boolean
Null handling: never called with a null reference — eventReference is required on the event request

#### QR-FIN-050 — search account mappings
Phase        : SVC-API (SVC-API-SEARCH)
API          : API-FIN-038 — search account mappings
Entity       : ENT-FIN-015
Operation    : FIND_BY_CRITERIA
Intent       : the mappings matching the screen's filters, each with the display data of its account
Logical spec : SELECT m.*, a.code, a.name_ar, a.name_en FROM FIN_ACCOUNT_MAPPING m JOIN FIN_ACCOUNT a ON a.account_pk = m.account_id WHERE [m.event_type_code = :eventTypeCode] AND [m.business_field_code = :businessFieldCode] AND [m.business_value LIKE :businessValue] AND [m.account_id = :accountId] AND [m.is_active_fl = :isActiveFl] ORDER BY :sort page :page size :size
Join         : required — FIN_ACCOUNT, for the account code and bilingual name the result columns show (ADR-FIN-031)
Transaction  : READ_ONLY
Locking      : NONE
Pagination   : YES (Page<T>) — default size 20, max 200
Filters      : eventTypeCode EXACT · businessFieldCode EXACT · businessValue LIKE · accountId EXACT · isActiveFl EXACT
Result shape : projection — the mapping's columns plus accountCode, accountNameAr, accountNameEn
Null handling: every filter optional; an empty page is success

#### QR-FIN-051 — create account mapping
Phase        : SVC-API (SVC-API-CRUD)
API          : API-FIN-039 — create account mapping
Entity       : ENT-FIN-015
Operation    : SAVE
Intent       : insert one active mapping
Logical spec : INSERT INTO FIN_ACCOUNT_MAPPING (account_mapping_pk ← nextval SEQ_FIN_ACCOUNT_MAPPING, event_type_code, business_field_code, business_value, account_id, is_active_fl = TRUE, created_by, created_at) — flushed at once
Join         : NONE
Transaction  : READ_WRITE
Locking      : UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY refuses a concurrent second active row for the same key; the refusal is answered as FIN-409-MAPPING-DUP (ADR-FIN-030)
Pagination   : NO
Filters      : —
Result shape : full entity
Null handling: no nullable business column; updated_by/updated_at NULL until the first change

#### QR-FIN-052 — an active mapping already holds the key
Phase        : SVC-API (SVC-API-CRUD)
API          : API-FIN-039 — create account mapping
Entity       : ENT-FIN-015
Operation    : EXISTS
Intent       : RULE-FIN-022 — is there an active mapping for this (event type, business field, business value)?
Logical spec : SELECT EXISTS (SELECT 1 FROM FIN_ACCOUNT_MAPPING WHERE event_type_code = :e AND business_field_code = :f AND business_value = :v AND is_active_fl)
Join         : NONE
Transaction  : READ_ONLY inside the create transaction
Locking      : none on the read; the partial unique index is the guarantee (QR-FIN-051)
Pagination   : NO
Filters      : three key columns EXACT, is_active_fl TRUE
Result shape : boolean
Null handling: all three key values are required on the request

#### QR-FIN-053 — change a mapping's account
Phase        : SVC-API (SVC-API-CRUD)
API          : API-FIN-040 — update account mapping
Entity       : ENT-FIN-015
Operation    : UPDATE
Intent       : replace the account of one mapping and nothing else (ADR-FIN-024)
Logical spec : UPDATE FIN_ACCOUNT_MAPPING SET account_id = :accountId, updated_by, updated_at WHERE account_mapping_pk = :id
Join         : NONE
Transaction  : READ_WRITE
Locking      : NONE — last writer wins between two concurrent changes, each validated against its own account
Pagination   : NO
Filters      : account_mapping_pk EXACT
Result shape : full entity
Null handling: —

#### QR-FIN-054 — deactivate a mapping
Phase        : SVC-API (SVC-API-CRUD)
API          : API-FIN-041 — deactivate account mapping
Entity       : ENT-FIN-015
Operation    : UPDATE
Intent       : soft-delete: the mapping stops resolving (RULE-FIN-020) and keeps its row for the audit trail
Logical spec : UPDATE FIN_ACCOUNT_MAPPING SET is_active_fl = FALSE, updated_by, updated_at WHERE account_mapping_pk = :id
Join         : NONE
Transaction  : READ_WRITE
Locking      : NONE
Pagination   : NO
Filters      : account_mapping_pk EXACT
Result shape : full entity
Null handling: —

#### QR-FIN-055 — the active mapping for an event's business value
Phase        : SVC-API (SVC-API-INT)
API          : API-FIN-020 — build event entry
Entity       : ENT-FIN-015
Operation    : FIND_ONE
Intent       : RULE-FIN-020 — the account a MAPPING line resolves to; none means the event is rejected, never a default
Logical spec : SELECT account_id FROM FIN_ACCOUNT_MAPPING WHERE event_type_code = :ruleEventType AND business_field_code = :lineField AND business_value = :eventValue AND is_active_fl
Join         : NONE — the account itself is then validated by QR-FIN-030 with every other line
Transaction  : READ_ONLY inside the build transaction
Locking      : NONE — a mapping changed after this read governs later events only (REQ-FIN-058)
Pagination   : NO
Filters      : three key columns EXACT, is_active_fl TRUE (served by UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY)
Result shape : projection account_id
Null handling: zero rows → FIN-422-UNMAPPED-VALUE; at most one row by the partial unique index

#### QR-FIN-056 — save a rule line's dimension tags
Phase        : SVC-API (SVC-API-CRUD)
API          : API-FIN-011 — add rule line
Entity       : ENT-FIN-016
Operation    : SAVE
Intent       : insert the submitted tags of the line just saved (ADR-FIN-025)
Logical spec : INSERT INTO FIN_RULE_LINE_DIM (rule_line_dimension_pk ← nextval SEQ_FIN_RULE_LINE_DIM, value_source_code, business_field_code, rule_line_id, dimension_id, dimension_value_id) per tag
Join         : NONE
Transaction  : READ_WRITE — the line's transaction (QR-FIN-015)
Locking      : NONE — the rows hang off a line no one else can see yet; UQ_FIN_RULE_LINE_DIM_LINE_DIM backs RULE-FIN-025
Pagination   : NO
Filters      : —
Result shape : full entities
Null handling: dimension_value_id NULL on BUSINESS_FIELD, business_field_code NULL on CONSTANT (CHK_FIN_RULE_LINE_DIM_VALUE_SPEC)

#### QR-FIN-057 — the tags of a rule's lines
Phase        : SVC-API (SVC-API-INT)
API          : API-FIN-020 — build event entry
Entity       : ENT-FIN-016
Operation    : FIND_BY_CRITERIA
Intent       : every dimension tag of the lines of the active rule, in one read
Logical spec : SELECT … FROM FIN_RULE_LINE_DIM WHERE rule_line_id IN (:lineIds)
Join         : NONE
Transaction  : READ_ONLY inside the build transaction
Locking      : NONE
Pagination   : NO
Filters      : rule_line_id SET
Result shape : full entities, grouped by rule line in the service
Null handling: a line with no tag builds a journal line with no dimension (v1 behaviour)

#### QR-FIN-058 — dimension value by code within a dimension
Phase        : SVC-API (SVC-API-INT)
API          : API-FIN-020 — build event entry
Entity       : ENT-FIN-003
Operation    : FIND_ONE
Intent       : RULE-FIN-027 — the DimensionValue whose code equals the event's value, inside the tagged dimension (ADR-FIN-026)
Logical spec : SELECT dimension_value_pk, is_active_fl FROM FIN_DIMENSION_VALUE WHERE dimension_id = :dimensionId AND code = :eventValue
Join         : NONE
Transaction  : READ_ONLY inside the build transaction
Locking      : NONE
Pagination   : NO
Filters      : dimension_id EXACT, code EXACT (served by UQ_FIN_DIMENSION_VALUE_DIM_CODE)
Result shape : projection
Null handling: zero rows → FIN-422-UNRESOLVED-DIMENSION-VALUE; an inactive match is left to RULE-FIN-009 (QR-FIN-032)

#### QR-FIN-059 — the fiscal period covering a date
Phase        : SVC-API (SVC-API-INT)
API          : API-FIN-020 — build event entry
Entity       : ENT-FIN-008
Operation    : FIND_ONE
Intent       : RULE-FIN-028 against RULE-FIN-008 — does any period cover the event date, and which one
Logical spec : SELECT fiscal_period_pk, fiscal_year_id, status_code FROM FIN_FISCAL_PERIOD WHERE :docDate BETWEEN start_date AND end_date
Join         : NONE
Transaction  : READ_ONLY inside the build transaction
Locking      : NONE here; the period state is read again at post by QR-FIN-031
Pagination   : NO
Filters      : docDate within [start_date, end_date]
Result shape : projection
Null handling: zero rows → FIN-422-NO-DEFINED-PERIOD (never FIN-409-PERIOD-NOT-OPEN). More than one row is possible: nothing stops two fiscal years from being created with overlapping spans, and this FIND_ONE then either fails or returns one non-deterministically. v1 defect, stated not hidden; CS-FIN-001 does not undertake it (ADR-FIN-040)

#### QR-FIN-060 — the account a mapping names
Phase        : SVC-API (SVC-API-CRUD)
API          : API-FIN-039 — create account mapping; API-FIN-040 — update account mapping
Entity       : ENT-FIN-001
Operation    : FIND_ONE
Intent       : RULE-FIN-023 — the account's leaf and active flags, read on save
Logical spec : SELECT account_pk, is_leaf_fl, is_active_fl FROM FIN_ACCOUNT WHERE account_pk = :accountId
Join         : NONE
Transaction  : READ_ONLY inside the write transaction
Locking      : NONE — an account deactivated after the save is still caught at post by RULE-FIN-007 (REQ-FIN-052)
Pagination   : NO
Filters      : account_pk EXACT
Result shape : projection
Null handling: zero rows → FIN-404-ACCOUNT

#### QR-FIN-061 — a tag's dimension and constant value, by id
Phase        : SVC-API (SVC-API-CRUD)
API          : API-FIN-011 — add rule line
Entity       : ENT-FIN-002, ENT-FIN-003
Operation    : FIND_ONE
Intent       : RULE-FIN-026 — the tagged dimension exists, and a CONSTANT tag's value belongs to it
Logical spec : SELECT dimension_pk FROM FIN_DIMENSION WHERE dimension_pk = :dimensionId; SELECT dimension_value_pk, dimension_id FROM FIN_DIMENSION_VALUE WHERE dimension_value_pk = :dimensionValueId
Join         : NONE
Transaction  : READ_ONLY inside the line's transaction
Locking      : NONE
Pagination   : NO
Filters      : primary keys EXACT
Result shape : projection
Null handling: unknown dimension → FIN-404-DIMENSION; unknown value → FIN-404-DIMVALUE; a value of another dimension → FIN-422-DIMENSION-TAG-SOURCE

Join governance. Every posting-pipeline query stays intra-module. The one cross-module read is the lookup
validation of XM-FIN-001, an in-process `MdlLookupApi` call and never a SQL join. No query joins to resolve a lookup
label: every lookup-backed column returns its code and the frontend labels it from MDL. v2 adds exactly one join
for parent data, QR-FIN-050's FIN_ACCOUNT display columns (ADR-FIN-031).

---

<!-- PHASE:CORE:START traces=REQ-FIN-017,REQ-FIN-045,REQ-FIN-047,REQ-FIN-072,REQ-FIN-075 -->
## PHASE 1 — CORE

**Layers**: controller → service → mapper → domain → repository (`profile.stack.backend.layers`).
Domain-behaviour placement: **domain classes** — one dedicated `<Entity>Domain` per entity — for every rule that
answers "is this operation allowed?" (`profile.conventions.domain_behaviour_placement: domain_classes`). The service
layer orchestrates only (load → delegate → persist → return) and holds no business-rule conditional of its own. How
many entities a rule reads is not a placement criterion: the service fetches those facts and passes them to the
domain class as plain arguments. The decision sits in the domain class; the entity keeps the plain state mutation
(`JournalEntryDomain.assertCanPost(...)` then `JournalEntry.post()`).

FIN domain classes and the rules each owns — v1, then the v2 additions:
- `AccountDomain` — RULE-FIN-001, RULE-FIN-007
- `DimensionValueDomain` — RULE-FIN-002, RULE-FIN-009; v2: RULE-FIN-027 (`assertResolvedByCode`)
- `EventTypeRuleDomain` — RULE-FIN-003, RULE-FIN-005, RULE-FIN-010; v2: RULE-FIN-018, RULE-FIN-019, RULE-FIN-021,
  RULE-FIN-025, RULE-FIN-026 (the rule line, its tags, and the event's business fields the line reads)
- `JournalEntryDomain` — RULE-FIN-004, RULE-FIN-006, RULE-FIN-008, RULE-FIN-011, RULE-FIN-012, RULE-FIN-013,
  RULE-FIN-016, RULE-FIN-017
- `FiscalPeriodDomain` — RULE-FIN-014; v2: RULE-FIN-028 (`assertDateCovered`)
- `FiscalYearDomain` — the year-end re-run guard (API-FIN-027)
- `AllocationRuleDomain`, `RecurringTemplateDomain` — RULE-FIN-003 and RULE-FIN-010 for allocation targets; the
  `assertCanRun()` gate of 2026-09-12 (a recorded human decision, not a RULE)
- v2 `AccountMappingDomain` (NEW, ENT-FIN-015) — RULE-FIN-020 (`assertResolved`), RULE-FIN-022 (`assertKeyFree`),
  and also RULE-FIN-023 (`assertAccountEligible`) and RULE-FIN-024 (`assertBusinessValueDefined`)

RULE-FIN-015 has no domain owner. Its data source is DEFERRED, and it is satisfied entirely by SEC's permission
gate: `@PreAuthorize(PERM_FIN_PERIODS_CLOSE_APPROVE)` on API-FIN-026 and API-FIN-027.

**Error signalling**: `LocalizedException → {code, messageAr, messageEn}`. Runtime code format, verbatim from
profile.stack.backend.api.error_code_format: `{MOD}-{http}[-{SLUG}]`, which for this module reads
`FIN-{http}[-{SLUG}]`. Every Error Catalog row is an instance of it, and its HTTP cell is one of the statuses the
platform emits (200, 201, 400, 401, 403, 404, 409, 413, 415, 422, 500).

**Store refusals (v2, ADR-FIN-030)**: a service never catches `DataIntegrityViolationException`. Two v2 rules need a
uniqueness refusal answered as a business error rather than as a server error: RULE-FIN-004 on
UQ_FIN_JOURNAL_ENTRY_EVENT_REF and RULE-FIN-022 on UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY. FIN therefore gains one
module-wide `FinPersistenceRefusalAdvisor`, placed exactly like the delivered `FinForbiddenAdvisor`: one
`DefaultPointcutAdvisor` over `com.erp.fin.service.*`, ordered outside the transaction advisor so a refusal raised at
flush or at commit passes through it, never a catch inside a service and never a feature `@ControllerAdvice`. It
reads the violated constraint's name and re-raises `LocalizedException(CONFLICT, …)` from a fixed registry:
UQ_FIN_JOURNAL_ENTRY_EVENT_REF → `FIN-409-DUPLICATE-EVENT`, UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY →
`FIN-409-MAPPING-DUP`. Any other constraint name is re-thrown unchanged. The two inserts concerned are flushed at
once, so the refusal happens inside the service call.

**Transaction scope**: `READ_ONLY` for every `FIND_*`/`EXISTS`/`AGGREGATE` query; `READ_WRITE` for every
`SAVE`/`UPDATE`. The build → validate → post sequence runs in ONE transaction per entry: either the whole entry
posts or nothing is written. That holds for every v2 event rejection too (SRS A7): no row exists for a rejected event.
Adding a rule line writes the line and its tags in one transaction.

**Search contract**: `{filters, page, size, sort}` in the request body, `Page<T>` in `ApiResponse<T>`, an empty
result is success. Allowed sort fields are the screen's own filter and result columns (SRS §B2), never widened.
Paging: server-side, size 20 by default, 200 at most [KB:erp-domain-standards §6].

**Audit fields**: `createdBy/createdAt/updatedBy/updatedAt` are filled by the platform, never by a request. On
FIN_FISCAL_PERIOD, `closedBy`/`closedAt` record the hard-close approver (API-FIN-026).

**Type mapping** (postgresql16 → Java, from profile.stack.db.syntax_map; column types only — PK generation is not a
type):

| postgresql16 | Java | Notes |
|---|---|---|
| BIGINT | Long | every PK — a plain column filled from its table's own `SEQ_{TABLE}` sequence, no identity clause — and every FK |
| VARCHAR(n) | String | every lookup-backed column holds the MDL code |
| TEXT | String | descriptions, rule-line values |
| BOOLEAN | Boolean | every `…Fl` flag |
| TIMESTAMPTZ | Instant | audit and system timestamps, stored UTC |
| DATE | LocalDate | document and period dates |
| NUMERIC(18,4) | BigDecimal | money [KB:erp-domain-standards §6] |
| NUMERIC | Integer | line, period and sort numbers |

**Lookup values**: every lookup-backed column is a plain `String` holding the code, validated in the service layer
against MDL (XM-FIN-001) before any write. v2: 15 FIN-owned keys. Two keys have a v2 rule of their own instead of the
generic `FIN-400-INVALID-LOOKUP`: ACCOUNT_DERIVATION_TYPE (RULE-FIN-018) and PAYMENT_METHOD when it is an account
mapping's business value (RULE-FIN-024).

**Event business fields (v2, ADR-FIN-031)**: the canonical event names each business fact by its
FIN_EVENT_BUSINESS_FIELD code, as a key of the request's existing `fields` map (for example
`fields.PAYMENT_METHOD = "CHEQUE"`, `fields.BRANCH_CODE = "BR-07"`). The rules engine reads a business field only
through that code: a MAPPING rule line names it in accountBusinessFieldCode, and a BUSINESS_FIELD tag names it in
businessFieldCode. Nothing in the payload is read as an account code (REQ-FIN-047).

**Numbering**: `docNo` format is `JV-{fiscalYearCode}-{NNNNNN}` — literal prefix `JV-`, the owning fiscal year's
`code` (DBF-FIN-066, VARCHAR(10)), `-`, then a zero-padded 6-digit counter starting at `000001`; e.g.
`JV-2026-000123`, at most 20 characters inside `doc_no VARCHAR(30)`. The counter is scoped per `fiscalYearId` and
restarts each year; one counter serves every journal type. `UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO (fiscal_year_id, doc_no)`
backs it in the store. A FIN-local generator in `com.erp.fin` produces it, deliberately not a shared `com.erp.common`
component. This is a recorded deviation from the profile's guidance that document numbers come from the platform
numbering engine: no such engine exists in this platform. The number is assigned once on create, never appears in a
request, and is present in every response. v2 adds no business code.

**Workflow engine**: forbidden — every lifecycle (JournalEntry, FiscalPeriod, FiscalYear) is a plain guarded
transition.

**Languages**: every name field and every catalog message exists in ar and en. v2 adds no stored name: an account
mapping is labelled by its account's `nameAr`/`nameEn`.

**Cross-module contract placement**: XM-FIN-001 (SOFT-READ → MDL) is FIN's service layer injecting MDL's published
in-process interface `com.erp.mdl.crossmodule.MdlLookupApi` and calling `readActiveValuesByKey(typeKey)`; it
consumes `LookupOptionView` (code, labelAr, labelEn, sortOrder), never an MDL entity. One deployable
(`profile.conventions.module_interface: in_process`): no HTTP client, no base path, no network failure path. FIN's
dependency on SEC for identity and authorization is the platform interceptor that SEC's and MDL's own plans declare,
cited here and not redeclared (ADR-FIN-001).

**Cross-cutting authorization**: every secured endpoint is gated before its method body runs by a `@PreAuthorize`
naming one `PERM_<PAGE_CODE>_<ACTION>` from `PermissionConstants`. A denial is re-raised as `FIN-403-FORBIDDEN` by
`FinForbiddenAdvisor`. RULE-FIN-015 is satisfied by the distinct `PERM_FIN_PERIODS_CLOSE_APPROVE` gate alone. The
earlier sentence here, which specified a FIN service-layer check that no single user holds both close-approval and
entry creation, was the origin of the SoD over-implementation removed on 2026-09-12; it is withdrawn and must not be
reinstated (SEC-BE).
<!-- PHASE:CORE:END -->

<!-- PHASE:DATA-DOM:START traces=REQ-FIN-001,REQ-FIN-010,REQ-FIN-014,REQ-FIN-031,REQ-FIN-049,REQ-FIN-054,REQ-FIN-062,REQ-FIN-070 -->
## PHASE 2 — DATA-DOM

Sixteen entities, grouped by role: `SUB:DATA-DOM-MASTER` (Account, FiscalYear, FiscalPeriod),
`SUB:DATA-DOM-TRANSACTIONAL` (JournalEntry, JournalLine, JournalLineDimension — the posting core) and
`SUB:DATA-DOM-LOOKUP` (FIN's own config and lookup-kind structural data, the two v2 entities among them). Every PK
is a plain BIGINT filled by the application from its table's own sequence (db-script header; profile
stack.db.pk_generation = `sequence`); v1's blocks said `GENERATED ALWAYS AS IDENTITY`, which migration V22 never built
and which the v2 db-script corrects — the bindings below name the sequence (ADR-FIN-029). Full RULE text for v1
rules is single-sourced in the SRS §A5 and in the Error Catalog; every v2 rule is stated in full in the block that
owns it and again in the endpoint that enforces it.

<!-- SUB:DATA-DOM-MASTER:START traces=REQ-FIN-001,REQ-FIN-031,REQ-FIN-036,REQ-FIN-074 -->
### SUB — DATA-DOM-MASTER

#### ENT-FIN-001 — Account      kind: master
BINDINGS       table `FIN_ACCOUNT` · PK `accountPk` (DBF-FIN-001) · PK generation `sequence` → `SEQ_FIN_ACCOUNT` (V22) · db-script v2
BUSINESS CODE  none — `code` is a client-chosen chart-of-accounts code, unique by UQ_FIN_ACCOUNT_CODE
FIELDS         DBF-FIN-001..013 and DBF-FIN-147 (`isRetainedEarningsFl`, seeded as data — see API-FIN-027 and SEC-BE's bootstrap rows); see the DB Alignment Manifest
DTO MEMBERSHIP create excludes {accountPk, isActiveFl, isRetainedEarningsFl, audit}; update excludes {accountPk, code, accountTypeCode, natureCode, isActiveFl, isRetainedEarningsFl, audit}; the response includes every field
LOOKUP FIELDS  accountTypeCode → `ACCOUNT_TYPE`; natureCode → `DEBIT_CREDIT` — codes validated through XM-FIN-001, never a numeric FK
DOMAIN RULES
- RULE-FIN-001 (SRS §A5) — on update of the leaf flag · application layer (QR-FIN-006) · `AccountDomain`
- RULE-FIN-007 (SRS §A5) — on post · application layer (QR-FIN-030) · `AccountDomain`; v2 applies it unchanged to an account taken from a mapping (REQ-FIN-052)
- v2: an account an active mapping names must be an active leaf when the mapping is saved (RULE-FIN-023) — owned by `AccountMappingDomain`, reading this entity's flags
STATE MACHINE  `isActiveFl` binary — not applicable
CROSS-MODULE   accountTypeCode and natureCode touch XM-FIN-001
OPERATIONS     VIEW · CREATE · UPDATE
REPOSITORY OPS → QR-FIN-001, QR-FIN-002, QR-FIN-003, QR-FIN-004, QR-FIN-005, QR-FIN-006, QR-FIN-045; v2 QR-FIN-060

#### ENT-FIN-007 — FiscalYear      kind: master
BINDINGS       table `FIN_FISCAL_YEAR` · PK `fiscalYearPk` (DBF-FIN-065) · PK generation `sequence` → `SEQ_FIN_FISCAL_YEAR` (V22) · db-script v2
BUSINESS CODE  none
FIELDS         DBF-FIN-065..074; statusCode lookup-backed (XM-FIN-001)
DTO MEMBERSHIP create `{code, startDate, endDate, periodCount}` — periodCount is a transient input that drives period generation (REQ-FIN-031), never persisted; the response carries the year and its generated periods
LOOKUP FIELDS  statusCode → `FISCAL_YEAR_STATUS`
DOMAIN RULES
- a CLOSED year is never closed again — the year-end re-run guard, `FiscalYearDomain` (FIN-409-INVALID-TRANSITION); the rest of year-end close is orchestrated in API-FIN-027
STATE MACHINE  statusCode OPEN → CLOSED, set once by API-FIN-027 — binary (SRS A7)
CROSS-MODULE   statusCode touches XM-FIN-001
OPERATIONS     CREATE · UPDATE
REPOSITORY OPS → QR-FIN-038, QR-FIN-041, QR-FIN-047, QR-FIN-048

#### ENT-FIN-008 — FiscalPeriod      kind: master
BINDINGS       table `FIN_FISCAL_PERIOD` · PK `fiscalPeriodPk` (DBF-FIN-075) · PK generation `sequence` → `SEQ_FIN_FISCAL_PERIOD` (V22) · db-script v2
BUSINESS CODE  none
FIELDS         DBF-FIN-075..088; statusCode lookup-backed (XM-FIN-001)
DTO MEMBERSHIP no direct create — generated with the year (API-FIN-023); the transition endpoints take only `{id}`; the response includes every field
LOOKUP FIELDS  statusCode → `PERIOD_STATE`
DOMAIN RULES
- RULE-FIN-008 (SRS §A5) — period Open at post time, the year-end closing and opening entries exempt · application layer (QR-FIN-031) · `JournalEntryDomain`. v2: for an event this is the retryable rejection, published for the consumer (REQ-FIN-073, REQ-FIN-075)
- RULE-FIN-014 (SRS §A5) — a hard-closed period is never reopened · application layer (QR-FIN-040) · `FiscalPeriodDomain`
- RULE-FIN-015 (SRS §A5) — DEFERRED (no declaration surface in v2): its data source is SEC's permission matrix; the separate `PERM_FIN_PERIODS_CLOSE_APPROVE` gate on API-FIN-026 and API-FIN-027 satisfies it, with no FIN catalog row and no FIN query
- RULE-FIN-028 (v2) — trigger: on build (event-sourced entry). Statement: the system shall reject the event with the no-defined-period configuration error when no accounting period's start and end dates cover the event's date, and shall never answer this case with the period-not-open error. Message: ar "لا توجد فترة محاسبية معرَّفة تغطي تاريخ الحدث" · en "No accounting period is defined for the event date". Scope: build · DB enforcement: application layer (QR-FIN-059 reads DBF-FIN-080 and DBF-FIN-081) · owner `FiscalPeriodDomain.assertDateCovered`
STATE MACHINE  statusCode (PERIOD_STATE) per SRS A7 — OPEN ⇄ SOFT_CLOSE → HARD_CLOSE → YEAR_END_CLOSE. v2: every state other than OPEN answers an event with FIN-409-PERIOD-NOT-OPEN; only SOFT_CLOSE can return to OPEN, so it is the case a consumer's retry resolves
CROSS-MODULE   statusCode touches XM-FIN-001
OPERATIONS     VIEW · CREATE · UPDATE
REPOSITORY OPS → QR-FIN-038, QR-FIN-039, QR-FIN-040, QR-FIN-049; v2 QR-FIN-059
<!-- SUB:DATA-DOM-MASTER:END -->

<!-- SUB:DATA-DOM-TRANSACTIONAL:START traces=REQ-FIN-010,REQ-FIN-014,REQ-FIN-017,REQ-FIN-028,REQ-FIN-070,REQ-FIN-072 -->
### SUB — DATA-DOM-TRANSACTIONAL

#### ENT-FIN-004 — JournalEntry      kind: transactional
BINDINGS       table `FIN_JOURNAL_ENTRY` · PK `journalEntryPk` (DBF-FIN-034) · PK generation `sequence` → `SEQ_FIN_JOURNAL_ENTRY` (V22) · db-script v2
BUSINESS CODE  **docNo** · column DBF-FIN-035 · format `JV-{fiscalYearCode}-{NNNNNN}` (e.g. `JV-2026-000123`), counter per fiscalYearId, one counter for every journal type · uniqueness UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO · generation source: the FIN-local generator in `com.erp.fin` (CORE — Numbering), invoked on create under the fiscal-year row lock; never in a request, always in a response
FIELDS         DBF-FIN-034..050; journalTypeCode and statusCode lookup-backed (XM-FIN-001)
DTO MEMBERSHIP manual create `{docDate, fiscalYearId, periodId, descriptionAr, descriptionEn, lines: [...]}`; event build = `EventEntryBuildRequest` (API-FIN-020 — eventReference, eventTypeCode, docDate, baseAmount, amounts, fields, descriptions); no update endpoint (RULE-FIN-016); the response carries every field with its lines
LOOKUP FIELDS  journalTypeCode → `JOURNAL_TYPE`; statusCode → `JOURNAL_STATUS`
DOMAIN RULES
- RULE-FIN-004 (MODIFIED v2) — trigger: on create (event-sourced entry), by the pre-check before building and again when the store's uniqueness guarantee on eventReference refuses a concurrent second write. Statement: the system shall reject building an entry for an event reference that has already produced a posted entry, and shall answer that rejection with the duplicate-event error whichever of the two detections catches the repeat; a uniqueness refusal from the store is never surfaced as a generic data-integrity or server error. Message: ar "تم بالفعل ترحيل قيد لهذا المرجع" · en "An entry for this event reference has already been posted". DB enforcement: UQ_FIN_JOURNAL_ENTRY_EVENT_REF, its refusal translated by FinPersistenceRefusalAdvisor (ADR-FIN-030) · owner `JournalEntryDomain` (pre-check, QR-FIN-026)
- RULE-FIN-005, RULE-FIN-006, RULE-FIN-008, RULE-FIN-011, RULE-FIN-012, RULE-FIN-013, RULE-FIN-016, RULE-FIN-017 (SRS §A5) — as in v1: QR-FIN-027, QR-FIN-029, QR-FIN-031, QR-FIN-034, QR-FIN-036, QR-FIN-035, repository omission, `JournalEntryDomain.assertHeaderCoherent`
- concurrency of RULE-FIN-013's already-reversed half — PESSIMISTIC_WRITE load of the original's header (API-FIN-021)
STATE MACHINE  statusCode (JOURNAL_STATUS) per SRS A7 — DRAFT → POSTED, POSTED terminal and locked at once; classic reversal leaves the original POSTED and links the mirror through originalEntryId / reversalEntryId (DBF-FIN-042, DBF-FIN-043); no path writes VOID. v2: every event rejection is raised before any row is written
CROSS-MODULE   journalTypeCode and statusCode touch XM-FIN-001
OPERATIONS     VIEW · CREATE
REPOSITORY OPS → QR-FIN-023 … QR-FIN-037 (the posting pipeline, reversal and read); v2 QR-FIN-025 and QR-FIN-026 modified

#### ENT-FIN-005 — JournalLine      kind: transactional
BINDINGS       table `FIN_JOURNAL_LINE` · PK `journalLinePk` (DBF-FIN-051) · PK generation `sequence` → `SEQ_FIN_JOURNAL_LINE` (V22) · db-script v2
FIELDS         DBF-FIN-051..060; directionCode lookup-backed (XM-FIN-001); amount positive by CHK_FIN_JOURNAL_LINE_AMOUNT_POSITIVE (POL-FIN-005)
DTO MEMBERSHIP written only nested in an entry's create or build request, never updated on its own (locked with its header)
DOMAIN RULES
- RULE-FIN-007, RULE-FIN-010 (SRS §A5) — QR-FIN-030, QR-FIN-028. v2: an event-built line's accountId is the rule line's constant account or the account its mapping resolves (REQ-FIN-047, REQ-FIN-050); it is STORED on the line, so a later mapping change cannot reach it (REQ-FIN-058)
CROSS-MODULE   directionCode touches XM-FIN-001
OPERATIONS     VIEW · CREATE
REPOSITORY OPS → written by QR-FIN-024, QR-FIN-025, QR-FIN-034; read by QR-FIN-037, QR-FIN-042, QR-FIN-043, QR-FIN-044

#### ENT-FIN-006 — JournalLineDimension      kind: transactional
BINDINGS       table `FIN_JOURNAL_LINE_DIM` · PK `journalLineDimensionPk` (DBF-FIN-061) · PK generation `sequence` → `SEQ_FIN_JOURNAL_LINE_DIM` (V22) · db-script v2
FIELDS         DBF-FIN-061..064
DTO MEMBERSHIP nested under each line of a manual entry (0..N per line); for an event-built line, one row per resolved dimension tag, written by the engine (v2)
DOMAIN RULES
- RULE-FIN-009 (SRS §A5) — QR-FIN-032. v2: applied unchanged to a value the engine resolved from the event (REQ-FIN-066, REQ-FIN-067)
- v2 REQ-FIN-070: an event-built line records each dimension value in exactly this shape, one row per dimension, the same as a manual line — no second shape, no separate table, so every report reads one form (REQ-FIN-071)
CROSS-MODULE   none
OPERATIONS     VIEW · CREATE
REPOSITORY OPS → written by QR-FIN-024, QR-FIN-025, QR-FIN-034; read by QR-FIN-037, QR-FIN-044
<!-- SUB:DATA-DOM-TRANSACTIONAL:END -->

<!-- SUB:DATA-DOM-LOOKUP:START traces=REQ-FIN-004,REQ-FIN-007,REQ-FIN-022,REQ-FIN-025,REQ-FIN-049,REQ-FIN-054,REQ-FIN-062 -->
### SUB — DATA-DOM-LOOKUP (FIN's own config and lookup-kind structural data)

#### ENT-FIN-002 — Dimension      kind: config
BINDINGS       table `FIN_DIMENSION` · PK `dimensionPk` (DBF-FIN-014) · PK generation `sequence` → `SEQ_FIN_DIMENSION` (V22) · db-script v2
FIELDS         DBF-FIN-014..022 · DTO: create `{code, nameAr, nameEn}`; no update and, by decision, no deactivate (SRS SCR-REQ-FIN-002 §B4)
DOMAIN RULES   none scoped alone; code unique by UQ_FIN_DIMENSION_CODE (QR-FIN-046)
OPERATIONS     VIEW · CREATE
REPOSITORY OPS → QR-FIN-007, QR-FIN-008, QR-FIN-046; v2 QR-FIN-061 (a tag's dimension)

#### ENT-FIN-003 — DimensionValue      kind: lookup
BINDINGS       table `FIN_DIMENSION_VALUE` · PK `dimensionValuePk` (DBF-FIN-023) · PK generation `sequence` → `SEQ_FIN_DIMENSION_VALUE` (V22) · db-script v2
FIELDS         DBF-FIN-023..033 — the profile's lookup defaults (code, nameAr, nameEn, sortOrder, isActiveFl) plus PK, FK and audit
DOMAIN RULES
- RULE-FIN-002 (SRS §A5) — QR-FIN-010 · UQ_FIN_DIMENSION_VALUE_DIM_CODE
- RULE-FIN-027 (v2) — trigger: on build (event-sourced entry, per BUSINESS_FIELD dimension tag). Statement: the system shall reject the event with the unresolvable-dimension-value error when the event's value for a tag's business field equals the code of no dimension value of the tagged dimension. Message: ar "قيمة البُعد في الحدث لا تطابق أي قيمة معرَّفة لهذا البُعد" · en "The dimension value in the event matches no defined value of this dimension". Resolution is by exact code within the dimension (ADR-FIN-026), served by UQ_FIN_DIMENSION_VALUE_DIM_CODE · application layer (QR-FIN-058) · owner `DimensionValueDomain.assertResolvedByCode`. A match that is inactive is RULE-FIN-009's rejection, not this one
OPERATIONS     VIEW · CREATE · UPDATE
REPOSITORY OPS → QR-FIN-009, QR-FIN-010, QR-FIN-011; v2 QR-FIN-058, QR-FIN-061

#### ENT-FIN-009 — EventTypeRule      kind: config
BINDINGS       table `FIN_EVENT_TYPE_RULE` · PK `eventTypeRulePk` (DBF-FIN-089) · PK generation `sequence` → `SEQ_FIN_EVENT_TYPE_RULE` (V22) · db-script v2
FIELDS         DBF-FIN-089..097; eventTypeCode lookup-backed (XM-FIN-001). v2: its eventTypeCode is the rule's half of every account-mapping key (RULE-FIN-020)
DOMAIN RULES   one rule per event type — UQ_FIN_EVENT_TYPE_RULE_CODE and QR-FIN-014; RULE-FIN-005 (QR-FIN-027)
OPERATIONS     VIEW · CREATE · UPDATE
REPOSITORY OPS → QR-FIN-012, QR-FIN-013, QR-FIN-014, QR-FIN-027

#### ENT-FIN-010 — RuleLine      kind: config   (MODIFIED v2)
BINDINGS       table `FIN_RULE_LINE` · PK `ruleLinePk` (DBF-FIN-098) · PK generation `sequence` → `SEQ_FIN_RULE_LINE` (V22) · db-script v2
FIELDS         DBF-FIN-098..108 and DBF-FIN-148. v2: accountDerivationTypeCode (DBF-FIN-101) is CONSTANT or MAPPING; accountDerivationValue (DBF-FIN-102) is NULL-able and holds only the constant account code; accountBusinessFieldCode (DBF-FIN-148) names the business field a MAPPING line reads
DTO MEMBERSHIP create request (API-FIN-011): every business field plus `dimensionTags[]` (ENT-FIN-016); excluded {ruleLinePk, lineNo — assigned, createdAt}; no update, no by-id read, no delete (SRS SCR-REQ-FIN-003 §B4); the response carries the line with its tags
LOOKUP FIELDS  accountDerivationTypeCode → `ACCOUNT_DERIVATION_TYPE`; accountBusinessFieldCode → `FIN_EVENT_BUSINESS_FIELD`; amountSourceTypeCode → `AMOUNT_SOURCE_TYPE`; directionCode → `DEBIT_CREDIT`; distributionTypeCode → `DISTRIBUTION_TYPE`
DOMAIN RULES
- RULE-FIN-003 (SRS §A5) — exactly one remainder line once the set is a compound or percentage distribution; `isRemainderFl` (DBF-FIN-107) is the single marker and must agree with the line's own REMAINDER type code · QR-FIN-016 · `EventTypeRuleDomain`
- RULE-FIN-018 (v2) — trigger: on create (rule line). Statement: the system shall reject a rule line whose account derivation type is not an active ACCOUNT_DERIVATION_TYPE value, so that CONSTANT and MAPPING are the only accepted derivations and no derivation reads an account code from the event. Message: ar "نوع اشتقاق الحساب غير مدعوم؛ المسموح قيمة ثابتة أو ربط" · en "Unsupported account derivation type; only constant or mapping is allowed". Scope CREATE · application layer, membership in MDL's active values (XM-FIN-001) · `EventTypeRuleDomain.assertDerivationSupported`
- RULE-FIN-019 (v2) — trigger: on create (rule line). Statement: the system shall require a CONSTANT rule line to carry an account code in accountDerivationValue and no business field, and a MAPPING rule line to carry a business field in accountBusinessFieldCode and no account code. Message: ar "مواصفة اشتقاق الحساب لا تطابق نوعه" · en "The account derivation spec does not match its type". Scope CREATE · DB: CHK_FIN_RULE_LINE_DERIVATION_SPEC (the exclusive-or), application: which half the type needs · `EventTypeRuleDomain.assertDerivationSpec`
- RULE-FIN-021 (v2) — the MAPPING half, checked when an event is built (full text in ENT-FIN-016)
STATE MACHINE  none — the line lives and retires with its rule
CROSS-MODULE   DBF-FIN-101, DBF-FIN-103, DBF-FIN-105, DBF-FIN-106 and v2 DBF-FIN-148 touch XM-FIN-001
OPERATIONS     CREATE
REPOSITORY OPS → QR-FIN-015 (MODIFIED), QR-FIN-016; read by QR-FIN-025 and QR-FIN-028 at build time

#### ENT-FIN-011 — RecurringTemplate      kind: config
BINDINGS       table `FIN_RECURRING_TEMPLATE` · PK `recurringTemplatePk` (DBF-FIN-109) · PK generation `sequence` → `SEQ_FIN_RECURRING_TEMPLATE` (V22) · db-script v2
FIELDS         DBF-FIN-109..121; scheduleTypeCode and frequencyCode lookup-backed (XM-FIN-001)
DOMAIN RULES   none scoped alone; its run posts through the shared pipeline (RULE-FIN-006 … RULE-FIN-010, cited on API-FIN-014) and `RecurringTemplateDomain.assertCanRun()` refuses a deactivated template (recorded human decision, not a RULE)
OPERATIONS     VIEW · CREATE · UPDATE
REPOSITORY OPS → QR-FIN-017, QR-FIN-018, QR-FIN-019

#### ENT-FIN-012 — RecurringTemplateLine      kind: config
BINDINGS       table `FIN_RECURRING_TEMPLATE_LINE` · PK `recurringTemplateLinePk` (DBF-FIN-122) · PK generation `sequence` → `SEQ_FIN_RECURRING_TEMPLATE_LINE` (V22) · db-script v2
FIELDS         DBF-FIN-122..129; directionCode lookup-backed (XM-FIN-001); amount positive by CHK_FIN_RECURRING_TPL_LINE_AMOUNT_POS
DOMAIN RULES   none scoped alone
OPERATIONS     VIEW · CREATE
REPOSITORY OPS → written by QR-FIN-018; read by QR-FIN-019 at run time

#### ENT-FIN-013 — AllocationRule      kind: config
BINDINGS       table `FIN_ALLOCATION_RULE` · PK `allocationRulePk` (DBF-FIN-130) · PK generation `sequence` → `SEQ_FIN_ALLOCATION_RULE` (V22) · db-script v2
FIELDS         DBF-FIN-130..138
DOMAIN RULES   none scoped alone; `AllocationRuleDomain.assertCanRun()` refuses a deactivated rule (recorded human decision, not a RULE)
OPERATIONS     VIEW · CREATE · UPDATE
REPOSITORY OPS → QR-FIN-020, QR-FIN-021, QR-FIN-022

#### ENT-FIN-014 — AllocationTarget      kind: config
BINDINGS       table `FIN_ALLOCATION_TARGET` · PK `allocationTargetPk` (DBF-FIN-139) · PK generation `sequence` → `SEQ_FIN_ALLOCATION_TARGET` (V22) · db-script v2
FIELDS         DBF-FIN-139..146; distributionTypeCode lookup-backed (XM-FIN-001)
DOMAIN RULES
- RULE-FIN-003 (SRS §A5, reused) — exactly one remainder target; `isRemainderFl` (DBF-FIN-146 — v1 cited DBF-FIN-141, the lineNo column, a slip corrected here) is the single marker the API-FIN-017 builder reads · QR-FIN-016 · `AllocationRuleDomain`
OPERATIONS     VIEW · CREATE
REPOSITORY OPS → written by QR-FIN-021; read by QR-FIN-022 at run time

#### ENT-FIN-015 — AccountMapping      kind: config   (ADDED v2)
BINDINGS       table `FIN_ACCOUNT_MAPPING` · PK `accountMappingPk` (DBF-FIN-149) · PK generation `sequence` → `SEQ_FIN_ACCOUNT_MAPPING` (db-script v2 BLOCK 1) · db-script v2
BUSINESS CODE  none — a mapping is identified by its key (eventTypeCode, businessFieldCode, businessValue); nothing outside the system quotes it
DEFAULT FIELDS config defaults adapted (SRS A3): `key` → the three-column natural key, unique among ACTIVE rows; `valueAr`/`valueEn` → not stored, the label is the referenced account's nameAr/nameEn; `isActiveFl` kept; four audit fields
FIELDS         DBF-FIN-149..158 — see the DB Alignment Manifest
DTO MEMBERSHIP create `{eventTypeCode, businessFieldCode, businessValue, accountId}`; update `{accountId}` only — the key is fixed after create (ADR-FIN-024); excluded everywhere {accountMappingPk, isActiveFl, audit}; the response carries every field plus the account's code, nameAr and nameEn (QR-FIN-050)
LOOKUP FIELDS  eventTypeCode → `ACCOUNTING_EVENT_TYPE`; businessFieldCode → `FIN_EVENT_BUSINESS_FIELD`; businessValue → `PAYMENT_METHOD` only when businessFieldCode = PAYMENT_METHOD, otherwise the host's code stored as-is — every one a stored code, never a numeric FK
DOMAIN RULES
- RULE-FIN-020 — trigger: on build (event-sourced entry, per MAPPING rule line). Statement: the system shall reject the event with the dedicated unmapped-value error when no active account mapping exists for the rule's event type, the rule line's business field and the event's value for that field; it never substitutes a default account. Message: ar "لا يوجد ربط حساب نشط للقيمة التجارية في الحدث" · en "No active account mapping exists for the business value in the event". Scope build · application layer (QR-FIN-055) · `AccountMappingDomain.assertResolved`. An inactive mapping counts as none
- RULE-FIN-022 — trigger: on create (account mapping). Statement: the system shall reject an account mapping whose event type, business field and business value already have an active mapping. Message: ar "يوجد بالفعل ربط نشط لنوع الحدث والحقل التجاري والقيمة نفسها" · en "An active mapping already exists for this event type, business field and value". Scope CREATE · DB: partial unique index UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY; application: QR-FIN-052 · `AccountMappingDomain.assertKeyFree`
- RULE-FIN-023 — trigger: on create / update (account mapping). Statement: the system shall reject an account mapping whose account is not a leaf account or is inactive. Message: ar "الحساب المربوط يجب أن يكون حسابًا ورقيًا نشطًا" · en "The mapped account must be an active leaf account". Scope CREATE, UPDATE · application layer (QR-FIN-060) · `AccountMappingDomain.assertAccountEligible`. Does not replace RULE-FIN-007 at post time
- RULE-FIN-024 — trigger: on create (account mapping). Statement: the system shall reject an account mapping for the PAYMENT_METHOD business field whose business value is not an active PAYMENT_METHOD code. Message: ar "القيمة ليست طريقة دفع معرَّفة" · en "The value is not a defined payment method". Scope CREATE · application layer, membership in MDL's active PAYMENT_METHOD values (XM-FIN-001) · `AccountMappingDomain.assertBusinessValueDefined`
STATE MACHINE  `isActiveFl` binary; soft deactivation, no reactivation — a deactivated key is restored by creating a new mapping (ADR-FIN-024)
CROSS-MODULE   eventTypeCode, businessFieldCode and (conditionally) businessValue touch XM-FIN-001
OPERATIONS     VIEW · CREATE · UPDATE
REPOSITORY OPS → QR-FIN-050, QR-FIN-051, QR-FIN-052, QR-FIN-053, QR-FIN-054, QR-FIN-055

#### ENT-FIN-016 — RuleLineDimension      kind: config   (ADDED v2)
BINDINGS       table `FIN_RULE_LINE_DIM` · PK `ruleLineDimensionPk` (DBF-FIN-159) · PK generation `sequence` → `SEQ_FIN_RULE_LINE_DIM` (db-script v2 BLOCK 1) · db-script v2
BUSINESS CODE  none
DEFAULT FIELDS config defaults adapted (SRS A3): `key` → (ruleLineId, dimensionId), one tag per dimension per line; no stored names (labels come from ENT-FIN-002 / ENT-FIN-003); no `isActiveFl` and no update audit — a tag lives and retires with its rule line (ADR-FIN-025); `createdAt` only
FIELDS         DBF-FIN-159..165 — see the DB Alignment Manifest
DTO MEMBERSHIP submitted only inside API-FIN-011 as `dimensionTags[]`: {dimensionId, valueSourceCode, dimensionValueId (CONSTANT), businessFieldCode (BUSINESS_FIELD)}; excluded {ruleLineDimensionPk, ruleLineId, createdAt}; no endpoint of its own
LOOKUP FIELDS  valueSourceCode → `FIN_DIMENSION_VALUE_SOURCE`; businessFieldCode → `FIN_EVENT_BUSINESS_FIELD`
DOMAIN RULES
- RULE-FIN-025 — trigger: on create (rule line with its dimension tags). Statement: the system shall reject a rule line that carries more than one dimension tag for the same dimension. Message: ar "لا يجوز وسم البُعد نفسه مرتين على سطر القاعدة" · en "A rule line cannot tag the same dimension twice". Scope CREATE · DB: UQ_FIN_RULE_LINE_DIM_LINE_DIM; application: over the submitted list · `EventTypeRuleDomain.assertTagsDistinct`
- RULE-FIN-026 — trigger: on create (rule line with its dimension tags). Statement: the system shall require a CONSTANT dimension tag to carry a dimension value that belongs to the tagged dimension and no business field, and a BUSINESS_FIELD dimension tag to carry a business field and no dimension value. Message: ar "وسم البُعد لا يطابق مصدر قيمته" · en "The dimension tag does not match its value source". Scope CREATE · DB: CHK_FIN_RULE_LINE_DIM_VALUE_SPEC (the exclusive-or); application: which half the source needs, and that a CONSTANT value belongs to the dimension (QR-FIN-061) · `EventTypeRuleDomain.assertTagMatchesSource`
- RULE-FIN-021 — trigger: on build (event-sourced entry, per MAPPING rule line and per BUSINESS_FIELD dimension tag). Statement: the system shall reject the event with the missing-business-field error when the event does not carry the business field named by a MAPPING rule line or by a BUSINESS_FIELD dimension tag. Message: ar "الحدث لا يحمل الحقل التجاري الذي تطلبه القاعدة" · en "The event does not carry the business field the rule requires". Scope build · application layer (reads DBF-FIN-148 or DBF-FIN-161 against the event's `fields`) · `EventTypeRuleDomain.assertFieldPresent`
- RULE-FIN-027 — resolution of a BUSINESS_FIELD tag's value (full text in ENT-FIN-003)
STATE MACHINE  none
CROSS-MODULE   valueSourceCode and businessFieldCode touch XM-FIN-001
OPERATIONS     CREATE
REPOSITORY OPS → QR-FIN-056, QR-FIN-057, QR-FIN-061
<!-- SUB:DATA-DOM-LOOKUP:END -->
<!-- PHASE:DATA-DOM:END -->

<!-- PHASE:SVC-API:START traces=REQ-FIN-001,REQ-FIN-010,REQ-FIN-014,REQ-FIN-017,REQ-FIN-028,REQ-FIN-036,REQ-FIN-050,REQ-FIN-054,REQ-FIN-062,REQ-FIN-072 -->
## PHASE 3 — SVC-API

API count = 41 (≥ 8) → split, grouped SEARCH / CRUD / INT. Every block names its entity and operation on its
`Entity` line, the columns it writes on its `Request` and `Orchestration` lines, and what stops two simultaneous
calls on its `Concurrency` line. For the 35 unchanged v1 endpoints those three lines are the v2 addition
(ADR-FIN-029); the rest of each block is v1 as delivered. API-FIN-011 and API-FIN-020 are rewritten, and
API-FIN-038 … API-FIN-041 are new.

<!-- SUB:SVC-API-SEARCH:START traces=REQ-FIN-001,REQ-FIN-004,REQ-FIN-007,REQ-FIN-022,REQ-FIN-027,REQ-FIN-031,REQ-FIN-039,REQ-FIN-040,REQ-FIN-041,REQ-FIN-042,REQ-FIN-043,REQ-FIN-055,REQ-FIN-071 -->
### SUB — SVC-API-SEARCH (read-only)

<!-- API:API-FIN-001:START traces=REQ-FIN-001,DBF-FIN-002,DBF-FIN-003,DBF-FIN-004,DBF-FIN-005 -->
### API-FIN-001 — search accounts
Entity       : ENT-FIN-001 · operation VIEW (search)
Endpoint: POST /api/v1/fin/accounts/search · Layers: `AccountController.search`→`AccountService.search`
Request: body `AccountSearchRequest` — `code`(LIKE), `nameAr/nameEn`(LIKE), `accountTypeCode`(EXACT), `isActiveFl`(EXACT), paging
Response: 200 · `Page<AccountResponse>` · `ApiResponse<...>`
Validations: none (read-only) · Errors: `FIN-500`
Orchestration: load (QR-FIN-001) → map → return · Repository: QR-FIN-001 · join NONE · READ_ONLY
Security: screen FIN_ACCOUNTS · `PERM_FIN_ACCOUNTS_VIEW` · Localization: nameAr/nameEn returned
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-001:END -->

<!-- API:API-FIN-005:START traces=REQ-FIN-004,DBF-FIN-015,DBF-FIN-016,DBF-FIN-017 -->
### API-FIN-005 — search dimensions
Entity       : ENT-FIN-002 · operation VIEW (search)
Endpoint: POST /api/v1/fin/dimensions/search · Layers: `DimensionController.search`→`DimensionService.search`
Request: body `DimensionSearchRequest` — `code`(LIKE), paging · Response: 200 · `Page<DimensionResponse>`
Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-007 → map → return · Repository: QR-FIN-007 · join NONE · READ_ONLY
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_VIEW` · Localization: nameAr/nameEn
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-005:END -->

<!-- API:API-FIN-008:START traces=REQ-FIN-005,DBF-FIN-024,DBF-FIN-025,DBF-FIN-026,DBF-FIN-027 -->
### API-FIN-008 — search dimension values
Entity       : ENT-FIN-003 · operation VIEW (search)
Endpoint: POST /api/v1/fin/dimensions/values/search · Layers: `DimensionController.searchDimensionValues`→`DimensionValueService.search`
Request: body `DimensionValueSearchRequest` — `dimensionId`(EXACT, required) carried in the body filters and read by the child parent-id extractor (never a path variable); `code`(LIKE), paging · Response: 200 · `Page<DimensionValueResponse>`
Validations: none · Errors: `FIN-404-DIMENSION`
Orchestration: QR-FIN-011 → map → return · Repository: QR-FIN-011 · join NONE · READ_ONLY
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_VIEW` · Localization: nameAr/nameEn
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-008:END -->

<!-- API:API-FIN-009:START traces=REQ-FIN-007,DBF-FIN-090,DBF-FIN-091,DBF-FIN-092 -->
### API-FIN-009 — search event-type rules
Entity       : ENT-FIN-009 · operation VIEW (search)
Endpoint: POST /api/v1/fin/event-rules/search · Layers: `EventTypeRuleController.search`→`EventTypeRuleService.search`
Request: body `EventTypeRuleSearchRequest` — `eventTypeCode`(EXACT), `isActiveFl`(EXACT), paging · Response: 200 · `Page<EventTypeRuleResponse>`
Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-012 → map → return · Repository: QR-FIN-012 · join NONE · READ_ONLY
Security: screen FIN_RULES · `PERM_FIN_RULES_VIEW` · Localization: nameAr/nameEn
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-009:END -->

<!-- API:API-FIN-012:START traces=REQ-FIN-022,DBF-FIN-110,DBF-FIN-111,DBF-FIN-112 -->
### API-FIN-012 — search templates
Entity       : ENT-FIN-011, ENT-FIN-012 · operation VIEW (search — each template is returned with its lines)
Endpoint: POST /api/v1/fin/recurring-templates/search · Layers: `RecurringTemplateController.search`→`RecurringTemplateService.search`
Request: body `RecurringTemplateSearchRequest` — `nameAr/nameEn`(LIKE), `scheduleTypeCode`(EXACT), `isActiveFl`(EXACT), paging
Response: 200 · `Page<RecurringTemplateResponse>` · Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-017 → map → return · Repository: QR-FIN-017 · join NONE · READ_ONLY
Security: screen FIN_RECURRING_TEMPLATES · `PERM_FIN_RECURRING_TEMPLATES_VIEW` · Localization: nameAr/nameEn
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-012:END -->

<!-- API:API-FIN-015:START traces=REQ-FIN-025,DBF-FIN-131,DBF-FIN-132,DBF-FIN-133 -->
### API-FIN-015 — search allocation rules
Entity       : ENT-FIN-013, ENT-FIN-014 · operation VIEW (search — each rule is returned with its targets)
Endpoint: POST /api/v1/fin/allocation-rules/search · Layers: `AllocationRuleController.search`→`AllocationRuleService.search`
Request: body `AllocationRuleSearchRequest` — `nameAr/nameEn`(LIKE), `sourceAccountId`(EXACT), `isActiveFl`(EXACT), paging
Response: 200 · `Page<AllocationRuleResponse>` · Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-020 → map → return · Repository: QR-FIN-020 · join NONE · READ_ONLY
Security: screen FIN_ALLOCATION_RULES · `PERM_FIN_ALLOCATION_RULES_VIEW` · Localization: nameAr/nameEn
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-015:END -->

<!-- API:API-FIN-018:START traces=REQ-FIN-027,DBF-FIN-035,DBF-FIN-036,DBF-FIN-040 -->
### API-FIN-018 — search journal entries
Entity       : ENT-FIN-004 · operation VIEW (search)
Endpoint: POST /api/v1/fin/journal-entries/search · Layers: `JournalEntryController.search`→`JournalEntryService.search`
Request: body `JournalEntrySearchRequest` — `docNo`(LIKE), `docDate`(DATE_RANGE), `periodId`(EXACT), `statusCode`(EXACT), `journalTypeCode`(EXACT), paging
Response: 200 · `Page<JournalEntryResponse>` · Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-023 → map → return (REQ-FIN-027: unmodified) · Repository: QR-FIN-023 · join NONE · READ_ONLY
Security: screen FIN_JOURNAL_ENTRIES · `PERM_FIN_JOURNAL_ENTRIES_VIEW` · Localization: descriptionAr/En
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-018:END -->

<!-- API:API-FIN-022:START traces=REQ-FIN-016,REQ-FIN-027,DBF-FIN-034,DBF-FIN-041,REQ-FIN-070 -->
### API-FIN-022 — read entry
Entity       : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 · operation VIEW (read one entry with its lines and their dimensions)
Endpoint: GET /api/v1/fin/journal-entries/{id} · Layers: `JournalEntryController.read`→`JournalEntryService.read`
Request: path `id` · Response: 200 · `JournalEntryResponse` with nested lines and their dimensions
Validations: none · Errors: `FIN-404-ENTRY`
Orchestration: QR-FIN-037 → map → return · Repository: QR-FIN-037 · join intra-module (entry→line→line-dim) · READ_ONLY
Security: screen FIN_JOURNAL_ENTRIES · `PERM_FIN_JOURNAL_ENTRIES_VIEW` · Localization: descriptionAr/En
Concurrency  : NONE — read-only; nothing is allocated or written
v2: an event-built line returns its dimension values as JournalLineDimension rows in exactly the form a manual line does (REQ-FIN-070, AC-FIN-075). Nothing changes in this endpoint, which already returns the rows it finds.
<!-- API:API-FIN-022:END -->

<!-- API:API-FIN-028:START traces=REQ-FIN-039,REQ-FIN-046,DBF-FIN-054,DBF-FIN-055,DBF-FIN-056,REQ-FIN-071 -->
### API-FIN-028 — account ledger
Entity       : ENT-FIN-005 · operation VIEW (read — account ledger report)
Endpoint: GET /api/v1/fin/reports/account-ledger · Layers: `ReportController.accountLedger`→`ReportService.accountLedger`
Request: `accountId`(EXACT), date range, dimension filters · Response: 200 · running-balance list, each row linking journalEntryId
Validations: none · Errors: `FIN-404-ACCOUNT`
Orchestration: (v2: event-built and manual lines are read alike — REQ-FIN-071) QR-FIN-042 (POSTED lines only, live) → compute running balance → return (POL-FIN-009)
Repository: QR-FIN-042 · join intra-module (line→entry for docDate/status) · READ_ONLY
Security: screen FIN_ACCOUNT_LEDGER · `PERM_FIN_ACCOUNT_LEDGER_VIEW` · Localization: n/a
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-028:END -->

<!-- API:API-FIN-029:START traces=REQ-FIN-040,REQ-FIN-046,DBF-FIN-005,DBF-FIN-006,DBF-FIN-055 -->
### API-FIN-029 — trial balance
Entity       : ENT-FIN-005 · operation VIEW (read — trial balance report)
Endpoint: GET /api/v1/fin/reports/trial-balance · Layers: `ReportController.trialBalance`→`ReportService.trialBalance`
Request: `periodId`(EXACT), `accountTypeCode`(EXACT) · Response: 200 · one row per account (debit/credit balance, sign per natureCode — POL-FIN-002)
Validations: RULE-FIN-006 restated as a report-level guarantee (POL-FIN-008: the sums always match because every contributing entry individually balanced — no separate check needed, an invariant by construction). `periodId` is an OPTIONAL narrowing, validated ONLY when supplied: omitting it means "no period narrowing" and stays a 200 (AC-FIN-040's happy path) — it is NOT mandatory
Errors: `FIN-404-PERIOD` (supplied `periodId` does not resolve), `FIN-500`
Orchestration: QR-FIN-043 (POSTED lines only, live, grouped by account) → apply nature sign → return
Repository: QR-FIN-043 · join intra-module · READ_ONLY
Security: screen FIN_TRIAL_BALANCE · `PERM_FIN_TRIAL_BALANCE_VIEW` · Localization: nameAr/nameEn per account
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-029:END -->

<!-- API:API-FIN-030:START traces=REQ-FIN-041,REQ-FIN-046,DBF-FIN-005,DBF-FIN-055 -->
### API-FIN-030 — balance sheet
Entity       : ENT-FIN-005 · operation VIEW (read — balance sheet report)
Endpoint: GET /api/v1/fin/reports/balance-sheet · Layers: `ReportController.balanceSheet`→`ReportService.balanceSheet`
Request: `fiscalYearId`(EXACT), `asOfDate` · Response: 200 · grouped ASSET/LIABILITY/EQUITY balances
Validations: none (continuity itself is guaranteed by REQ-FIN-036's opening-entry generation, not re-validated at read time). The REQUIRED `fiscalYearId` is resolved FIRST, before any aggregation
Errors: `FIN-404-YEAR` (unknown `fiscalYearId` — previously a silent 200 carrying an all-zero statement), `FIN-500`
Orchestration: QR-FIN-043 (accountTypeCode IN ASSET,LIABILITY,EQUITY) → group → return
Repository: QR-FIN-043 · join intra-module · READ_ONLY
Security: screen FIN_BALANCE_SHEET · `PERM_FIN_BALANCE_SHEET_VIEW` · Localization: nameAr/nameEn per account
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-030:END -->

<!-- API:API-FIN-031:START traces=REQ-FIN-042,REQ-FIN-046,DBF-FIN-005,DBF-FIN-055 -->
### API-FIN-031 — income statement
Entity       : ENT-FIN-005 · operation VIEW (read — income statement report)
Endpoint: GET /api/v1/fin/reports/income-statement · Layers: `ReportController.incomeStatement`→`ReportService.incomeStatement`
Request: `fiscalYearId`(EXACT), period range · Response: 200 · grouped REVENUE/EXPENSE balances
Validations: none (zero-opening is guaranteed by REQ-FIN-036 closing result accounts to Retained Earnings, not re-validated at read time). The REQUIRED `fiscalYearId` is resolved FIRST; the two OPTIONAL period bounds `fromPeriodId`/`toPeriodId` are validated only when supplied, as they already were
Errors: `FIN-404-YEAR` (unknown `fiscalYearId`), `FIN-404-PERIOD` (supplied period bound does not resolve — unchanged), `FIN-500`
Orchestration: QR-FIN-043 (accountTypeCode IN REVENUE,EXPENSE, scoped to the year/period range) → group → return
Repository: QR-FIN-043 · join intra-module · READ_ONLY
Security: screen FIN_INCOME_STATEMENT · `PERM_FIN_INCOME_STATEMENT_VIEW` · Localization: nameAr/nameEn per account
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-031:END -->

<!-- API:API-FIN-032:START traces=REQ-FIN-043,DBF-FIN-063,DBF-FIN-064,DBF-FIN-055,REQ-FIN-071 -->
### API-FIN-032 — dimension report
Entity       : ENT-FIN-005, ENT-FIN-006 · operation VIEW (read — dimension report)
Endpoint: GET /api/v1/fin/reports/dimension · Layers: `ReportController.dimensionReport`→`ReportService.dimensionReport`
Request: `dimensionId`(EXACT), `dimensionValueId`(EXACT), `periodId`(EXACT) · Response: 200 · one row per account+dimension-value combination (POL-FIN-011)
Validations: none · Errors: `FIN-404-DIMENSION`
Orchestration: (v2: event-built lines carry their JournalLineDimension rows in the v1 shape, so this grouping covers them unchanged — REQ-FIN-071, AC-FIN-076) QR-FIN-044 (group by account + dimension value, never base account alone) → return
Repository: QR-FIN-044 · join intra-module (line→line-dim→dimension-value) · READ_ONLY
Security: screen FIN_DIMENSION_REPORTS · `PERM_FIN_DIMENSION_REPORTS_VIEW` · Localization: nameAr/nameEn
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-032:END -->

<!-- API:API-FIN-033:START traces=REQ-FIN-031,DBF-FIN-076,DBF-FIN-077,DBF-FIN-080,DBF-FIN-081,DBF-FIN-082 -->
### API-FIN-033 — search fiscal periods
Entity       : ENT-FIN-008 · operation VIEW (search)
Endpoint: POST /api/v1/fin/fiscal-periods/search · Layers: `FiscalPeriodController.search`→`FiscalPeriodService.search`
Request: body `FiscalPeriodSearchRequest` — `fiscalYearId`(EXACT, **OPTIONAL**, DBF-FIN-076) carried in the body filters and read by the child parent-id extractor (never a path variable); `statusCode`(EXACT, DBF-FIN-082), paging/sort · Response: 200 · `Page<FiscalPeriodResponse>`
Validations: none (read-only) · Errors: `FIN-400-INVALID-SORT`, `FIN-500`
Orchestration: build the generic specification from the remaining filters → AND in an explicit join predicate on `fiscalYear.fiscalYearPk` ONLY when `fiscalYearId` is present → page → map → return
Repository: `FiscalPeriodRepository` via `JpaSpecificationExecutor.findAll(Specification, Pageable)` — no QR id is assigned; the Query Reference Catalog closes at QR-FIN-049 and extending it is a catalog-level change left to ALIGN · join intra-module (period→year, only when the parent filter is supplied) · READ_ONLY
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_VIEW` · Localization: nameAr/nameEn per period
Parent id OPTIONAL — deliberate divergence from API-FIN-008, which rejects a missing `dimensionId` with `FIN-404-DIMENSION`. SCR-REQ-FIN-007 §B2 makes both filters EXACT but neither mandatory, and the endpoint exists precisely so a client that did NOT create the fiscal year in the same session can discover a period id: requiring the year id first would leave that client with no way in. Same shape API-FIN-018 already uses for its optional `periodId`.
Why it exists: `JournalEntryCreateRequest` requires `fiscalYearId` + `periodId` (API-FIN-019) and API-FIN-029/030/031 require a period or year id, yet before this endpoint no API returned a fiscal period except API-FIN-023's create response. `PERM_FIN_PERIODS_VIEW` was already a V24 registry row and already granted by V25/V27, so **no migration was needed** — only the matching `PermissionConstants` constant was added.
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-033:END -->

<!-- API:API-FIN-038:START traces=REQ-FIN-055,DBF-FIN-150,DBF-FIN-151,DBF-FIN-152,DBF-FIN-153,DBF-FIN-154 -->
### API-FIN-038 — search account mappings
Entity       : ENT-FIN-015 · operation VIEW (search)
Endpoint     : POST /api/v1/fin/account-mappings/search   verb: POST — FIN's house style for a search with a request body (SRS SCR-REQ-FIN-013 §B5)
Layers       : `AccountMappingController.search` → `AccountMappingService.search`
Request      : body `AccountMappingSearchRequest` — filters eventTypeCode (EXACT), businessFieldCode (EXACT), businessValue (LIKE), accountId (EXACT), isActiveFl (EXACT), all optional; `page`, `size` (20 by default, 200 at most), `sort` limited to those five result columns (SRS §B2); nothing is written
Filters      : eventTypeCode → DBF-FIN-150 · businessFieldCode → DBF-FIN-151 · businessValue → DBF-FIN-152 · accountId → DBF-FIN-153 · isActiveFl → DBF-FIN-154
Response     : 200 · `ApiResponse<Page<AccountMappingResponse>>` — accountMappingPk, eventTypeCode, businessFieldCode, businessValue, accountId, accountCode, accountNameAr, accountNameEn, isActiveFl and the audit fields; an empty page is success, never "not found" (AC-FIN-059)
Validations  : the search contract only — a sort field outside the five → FIN-400-INVALID-SORT
Errors       : FIN-400-INVALID-SORT, FIN-403-FORBIDDEN, FIN-500
Orchestration: build the specification from the filters supplied → page (QR-FIN-050) → map; the account's code and names come from the joined account row (ADR-FIN-031) → return
Repository   : QR-FIN-050 · FIND_BY_CRITERIA · join FIN_ACCOUNT for the display columns (ADR-FIN-031) · READ_ONLY
Concurrency  : NONE — read-only
Security     : screen FIN_ACCOUNT_MAPPINGS · `PERM_FIN_ACCOUNT_MAPPINGS_VIEW` (the gateway), checked by `@PreAuthorize` before processing
Localization : the account's nameAr and nameEn are both returned; lookup values travel as codes and the frontend labels them from MDL
<!-- API:API-FIN-038:END -->

**404 on a keying id, as built (API-FIN-029/030/031).** FIN's house style already 404s on the id
that keys a report — API-FIN-028 answers `FIN-404-ACCOUNT`, API-FIN-032 answers
`FIN-404-DIMENSION`. API-FIN-030 and API-FIN-031 now do the same for their REQUIRED `fiscalYearId`
(`FIN-404-YEAR`), and API-FIN-029 for its `periodId` when — and only when — one is supplied
(`FIN-404-PERIOD`). API-FIN-031 was internally contradictory before: 404 on an unknown period bound
and 200 on an unknown year, in the same request. No new error code was introduced; both codes were
already registered in `FinErrorCodes` and both i18n bundles.
<!-- SUB:SVC-API-SEARCH:END -->

<!-- SUB:SVC-API-CRUD:START traces=REQ-FIN-001,REQ-FIN-002,REQ-FIN-004,REQ-FIN-005,REQ-FIN-006,REQ-FIN-007,REQ-FIN-008,REQ-FIN-009,REQ-FIN-014,REQ-FIN-022,REQ-FIN-025,REQ-FIN-048,REQ-FIN-049,REQ-FIN-054,REQ-FIN-056,REQ-FIN-057,REQ-FIN-062 -->
### SUB — SVC-API-CRUD

<!-- API:API-FIN-002:START traces=REQ-FIN-001,REQ-FIN-002,DBF-FIN-002,DBF-FIN-005,DBF-FIN-006,DBF-FIN-007,DBF-FIN-008 -->
### API-FIN-002 — create account
Entity       : ENT-FIN-001 · operation CREATE
Endpoint: POST /api/v1/fin/accounts · Layers: `AccountController.create`→`AccountService.create`
Request: (writes DBF-FIN-002 code · DBF-FIN-003 nameAr · DBF-FIN-004 nameEn · DBF-FIN-005 accountTypeCode · DBF-FIN-006 natureCode · DBF-FIN-007 parentAccountId · DBF-FIN-008 isLeafFl) `{code, nameAr, nameEn, accountTypeCode, natureCode, parentAccountId?, isLeafFl}`
Response: 201 · `AccountResponse`
Validations: RULE-FIN-001 (leaf requires no children — only relevant if `parentAccountId`
is later given children; at create this only matters when the same call sets a parent
that itself must remain non-leaf — QR-FIN-006 checks the *parent*, not the new row);
uniqueness of code (QR-FIN-005); accountTypeCode/natureCode validated via XM-FIN-001
Errors: `FIN-409-ACCOUNT-DUP`, `FIN-409-PARENT-NOT-LEAF-ELIGIBLE`, `FIN-400-INVALID-LOOKUP`
Orchestration: (sets DBF-FIN-009 isActiveFl=TRUE on insert; flips the parent's DBF-FIN-008 isLeafFl to FALSE) validate lookups (XM-FIN-001) → validate uniqueness (QR-FIN-005) → if
parentAccountId given, flip the parent's isLeafFl to false via RULE-FIN-001's inverse
effect (a parent gaining its first child can no longer itself be a leaf) → persist (QR-FIN-002) → return
Repository: QR-FIN-002, QR-FIN-005, QR-FIN-006 · join NONE · READ_WRITE
Security: screen FIN_ACCOUNTS · `PERM_FIN_ACCOUNTS_CREATE` · Localization: nameAr/nameEn required
Concurrency  : the pre-check is the friendly path; UQ_FIN_ACCOUNT_CODE is the guarantee. Two simultaneous requests can both pass the pre-check, and the store then refuses the second. UQ_FIN_ACCOUNT_CODE is not registered with FinPersistenceRefusalAdvisor (ADR-FIN-030 registers only the two v2 constraints), so that refusal still surfaces as the platform's generic error instead of FIN-409-ACCOUNT-DUP — v1 behaviour as built, recorded here and not changed by v2. Creating a child also flips the named parent's isLeafFl without a lock, racing a concurrent API-FIN-003 that is deciding RULE-FIN-001 over the same child set; the pair can leave an account both leaf and parent, which RULE-FIN-007 would then accept as postable. v1 as built; stated here, not changed by v2 (ADR-FIN-042)
<!-- API:API-FIN-002:END -->

<!-- API:API-FIN-003:START traces=REQ-FIN-002,DBF-FIN-003,DBF-FIN-004,DBF-FIN-008 -->
### API-FIN-003 — update account
Entity       : ENT-FIN-001 · operation UPDATE
Endpoint: PUT /api/v1/fin/accounts/{id} · Layers: `AccountController.update`→`AccountService.update`
Request: (writes DBF-FIN-003 nameAr · DBF-FIN-004 nameEn · DBF-FIN-008 isLeafFl) `{nameAr, nameEn, isLeafFl}` — excludes {accountPk, code, accountTypeCode, natureCode, isActiveFl, audit}
Response: 200 · `AccountResponse`
Validations: RULE-FIN-002-equivalent — RULE-FIN-001 (full text: DATA-DOM §ENT-FIN-001) —
`isLeafFl=true` rejected if the account has any child (QR-FIN-006)
Errors: `FIN-409-HAS-CHILDREN`, `FIN-404-ACCOUNT`
Orchestration: load → check RULE-FIN-001 if isLeafFl changing to true (QR-FIN-006) → update (QR-FIN-003) → return
Repository: QR-FIN-003, QR-FIN-006 · join NONE · READ_WRITE
Security: screen FIN_ACCOUNTS · `PERM_FIN_ACCOUNTS_UPDATE` · Localization: both name fields updatable
Concurrency  : RULE-FIN-001 is decided on the child set QR-FIN-006 reads, which a concurrent API-FIN-002 naming this account as parent can change; no lock is specified (v1 as built) — recorded, not changed by v2. The same race can leave the account both leaf and parent, a state RULE-FIN-007 accepts as postable (ADR-FIN-042)
<!-- API:API-FIN-003:END -->

<!-- API:API-FIN-004:START traces=REQ-FIN-003,DBF-FIN-009 -->
### API-FIN-004 — deactivate account
Entity       : ENT-FIN-001 · operation UPDATE (deactivate)
Endpoint: PUT /api/v1/fin/accounts/{id}/deactivate · Layers: `AccountController.deactivate`→`AccountService.deactivate`
Request: path `id`, no body · Response: 200 · `AccountResponse` (isActiveFl=false)
Validations: none beyond existence · Errors: `FIN-404-ACCOUNT`
Orchestration: (sets DBF-FIN-009 isActiveFl=FALSE) load → `Account.deactivate()` → persist (QR-FIN-004) → return (REQ-FIN-019
subsequently rejects any posting to it)
Repository: QR-FIN-004 · join NONE · READ_WRITE
Security: screen FIN_ACCOUNTS · `PERM_FIN_ACCOUNTS_UPDATE` · Localization: n/a
Concurrency  : NONE — flips one flag on the row it loads; nothing is allocated or decided on a read
<!-- API:API-FIN-004:END -->

<!-- API:API-FIN-006:START traces=REQ-FIN-004,DBF-FIN-015,DBF-FIN-016,DBF-FIN-017 -->
### API-FIN-006 — create dimension
Entity       : ENT-FIN-002 · operation CREATE
Endpoint: POST /api/v1/fin/dimensions · Layers: `DimensionController.create`→`DimensionService.create`
Request: (writes DBF-FIN-015 code · DBF-FIN-016 nameAr · DBF-FIN-017 nameEn) `{code, nameAr, nameEn}` · Response: 201 · `DimensionResponse`
Validations: uniqueness of code (QR-FIN-046, DB `UQ_FIN_DIMENSION_CODE` friendly-checked at service layer)
Errors: `FIN-409-DIMENSION-DUP`
Orchestration: (sets DBF-FIN-018 isActiveFl=TRUE on insert) validate → persist (QR-FIN-008) → return · Repository: QR-FIN-008 · join NONE · READ_WRITE
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_CREATE` · Localization: nameAr/nameEn required
Concurrency  : the pre-check is the friendly path; UQ_FIN_DIMENSION_CODE is the guarantee. Two simultaneous requests can both pass the pre-check, and the store then refuses the second. UQ_FIN_DIMENSION_CODE is not registered with FinPersistenceRefusalAdvisor (ADR-FIN-030 registers only the two v2 constraints), so that refusal still surfaces as the platform's generic error instead of FIN-409-DIMENSION-DUP — v1 behaviour as built, recorded here and not changed by v2
<!-- API:API-FIN-006:END -->

<!-- API:API-FIN-007:START traces=REQ-FIN-005,REQ-FIN-006,DBF-FIN-024,DBF-FIN-025,DBF-FIN-028 -->
### API-FIN-007 — create dimension value
Entity       : ENT-FIN-003 · operation CREATE
Endpoint: POST /api/v1/fin/dimensions/{id}/values · Layers: `DimensionController.createDimensionValue`→`DimensionValueService.create`
Request: (writes DBF-FIN-024 dimensionId from the path · DBF-FIN-025 code · DBF-FIN-026 nameAr · DBF-FIN-027 nameEn · DBF-FIN-028 sortOrder) path `id` (dimensionId); body `{code, nameAr, nameEn, sortOrder}`
Response: 201 · `DimensionValueResponse`
Validations: RULE-FIN-002 (full text: DATA-DOM §ENT-FIN-003) — code unique within the
dimension (QR-FIN-010)
Errors: `FIN-409-DIMVALUE-DUP`, `FIN-404-DIMENSION`
Orchestration: (sets DBF-FIN-029 isActiveFl=TRUE on insert) validate dimension exists → check RULE-FIN-002 (QR-FIN-010) → persist (QR-FIN-009) → return
Repository: QR-FIN-009, QR-FIN-010 · join NONE · READ_WRITE
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_CREATE` · Localization: nameAr/nameEn required
Concurrency  : the pre-check is the friendly path; UQ_FIN_DIMENSION_VALUE_DIM_CODE is the guarantee. Two simultaneous requests can both pass the pre-check, and the store then refuses the second. UQ_FIN_DIMENSION_VALUE_DIM_CODE is not registered with FinPersistenceRefusalAdvisor (ADR-FIN-030 registers only the two v2 constraints), so that refusal still surfaces as the platform's generic error instead of FIN-409-DIMVALUE-DUP — v1 behaviour as built, recorded here and not changed by v2
<!-- API:API-FIN-007:END -->

<!-- API:API-FIN-035:START traces=REQ-FIN-005,REQ-FIN-021,DBF-FIN-029 -->
### API-FIN-035 — deactivate dimension value
Entity       : ENT-FIN-003 · operation UPDATE (deactivate)
Endpoint: PUT /api/v1/fin/dimensions/values/{id}/deactivate · Layers: `DimensionController.deactivateDimensionValue`→`DimensionValueService.deactivate`
Request: path `id` (dimensionValuePk), no body · Response: 200 · `DimensionValueResponse` (isActiveFl=false)
Validations: none beyond existence · Errors: `FIN-404-DIMVALUE`
Orchestration: (sets DBF-FIN-029 isActiveFl=FALSE) load → `DimensionValue.deactivate()` (the entity's own helper, never a direct field assignment) → persist → return. Effect: `DimensionValueDomain.checkUsableOnLine` reads exactly this flag (DBF-FIN-029), so every posting path citing the value afterwards answers `FIN-409-INVALID-DIMENSION` (RULE-FIN-009 / REQ-FIN-021, via API-FIN-019, 020, 014, 017)
Repository: `DimensionValueRepository.findById` + `save` — no QR id is assigned; the Query Reference Catalog closes at QR-FIN-049 and extending it is a catalog-level change left to ALIGN · join NONE · READ_WRITE
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_UPDATE` · Localization: n/a
New in this delivery: `FIN-404-DIMVALUE` (`FinErrorCodes.FIN_404_DIMVALUE`, added to BOTH i18n bundles) and `PERM_FIN_DIMENSIONS_UPDATE`. This is the first UPDATE-class endpoint on FIN_DIMENSIONS, so migration `V28__fin_dimensions_update_action.sql` registers the `FIN_DIMENSIONS / UPDATE` action row AND explicitly grants it to `SYS_ADMIN` — V25 grants by a `SELECT` over the registry and has already run, so a later row would otherwise be registered-but-ungrantable.
Why it exists: RULE-FIN-009 / REQ-FIN-021 reject a journal line citing an INACTIVE dimension value and `DimensionValueDomain` implements that check, but nothing could set the flag false, so the branch was unreachable and untestable. Deliberately NOT built (a decision, not a backlog item): a deactivate on the PARENT `Dimension` — no REQ/AC/RULE requires one and `Dimension.isActiveFl` (DBF-FIN-018) drives no behaviour. No `activate` counterpart either, matching the delivered `AccountService.deactivate` precedent.
Concurrency  : NONE — flips one flag on the row it loads; nothing is allocated or decided on a read
<!-- API:API-FIN-035:END -->

<!-- API:API-FIN-010:START traces=REQ-FIN-007,REQ-FIN-044,REQ-FIN-045,DBF-FIN-090,DBF-FIN-091,DBF-FIN-092 -->
### API-FIN-010 — create event-type rule
Entity       : ENT-FIN-009 · operation CREATE
Endpoint: POST /api/v1/fin/event-rules · Layers: `EventTypeRuleController.create`→`EventTypeRuleService.create`
Request: (writes DBF-FIN-090 eventTypeCode · DBF-FIN-091 nameAr · DBF-FIN-092 nameEn) `{eventTypeCode, nameAr, nameEn}` · Response: 201 · `EventTypeRuleResponse`
Validations: eventTypeCode validated via XM-FIN-001; uniqueness of one-active-rule-per-type (QR-FIN-014)
Errors: `FIN-409-RULE-DUP`, `FIN-400-INVALID-LOOKUP`
Orchestration: (sets DBF-FIN-093 isActiveFl=TRUE on insert) validate lookup → check uniqueness (QR-FIN-014) → persist (QR-FIN-013) → return
Repository: QR-FIN-013, QR-FIN-014 · join NONE · READ_WRITE
Security: screen FIN_RULES · `PERM_FIN_RULES_CREATE` · Localization: nameAr/nameEn required
Precondition: this endpoint is only reachable once FIN's onboarding has completed —
FIN registered as a module with its screens/actions into SEC (REQ-FIN-044) and FIN's lookup types (13 in v1, 15 since v2),
including ACCOUNTING_EVENT_TYPE, registered into MDL (REQ-FIN-045); both run
once at deployment, not per request.
Concurrency  : the pre-check is the friendly path; UQ_FIN_EVENT_TYPE_RULE_CODE is the guarantee. Two simultaneous requests can both pass the pre-check, and the store then refuses the second. UQ_FIN_EVENT_TYPE_RULE_CODE is not registered with FinPersistenceRefusalAdvisor (ADR-FIN-030 registers only the two v2 constraints), so that refusal still surfaces as the platform's generic error instead of FIN-409-RULE-DUP — v1 behaviour as built, recorded here and not changed by v2
<!-- API:API-FIN-010:END -->

<!-- API:API-FIN-011:START traces=REQ-FIN-008,REQ-FIN-009,REQ-FIN-048,REQ-FIN-049,REQ-FIN-062,REQ-FIN-064,REQ-FIN-065,DBF-FIN-099,DBF-FIN-101,DBF-FIN-102,DBF-FIN-148,DBF-FIN-103,DBF-FIN-105,DBF-FIN-106,DBF-FIN-107,DBF-FIN-160,DBF-FIN-161,DBF-FIN-163,DBF-FIN-164 -->
### API-FIN-011 — add rule line (MODIFIED v2 — MAPPING business field, dimension tags)
Entity       : ENT-FIN-010, ENT-FIN-016 · operation CREATE (a line and its tags, gated as an update of the rule)
Endpoint     : POST /api/v1/fin/event-rules/{id}/lines   verb: POST
Layers       : `EventTypeRuleController.createRuleLine` → `RuleLineService.create`; every decision in `EventTypeRuleDomain`
Request      : path `id` → DBF-FIN-099 eventTypeRuleId; body `RuleLineCreateRequest` — `accountDerivationTypeCode` DBF-FIN-101 (String, required) · `accountDerivationValue` DBF-FIN-102 (String, CONSTANT only: the constant account code) · `accountBusinessFieldCode` DBF-FIN-148 (String, MAPPING only) · `amountSourceTypeCode` DBF-FIN-103 (required) · `amountSourceValue` DBF-FIN-104 (required unless REMAINDER) · `directionCode` DBF-FIN-105 (required) · `distributionTypeCode` DBF-FIN-106 (required) · `isRemainderFl` DBF-FIN-107 (Boolean, default false) · `dimensionTags[]`, each {`dimensionId` DBF-FIN-163 (required) · `valueSourceCode` DBF-FIN-160 (required) · `dimensionValueId` DBF-FIN-164 (CONSTANT only) · `businessFieldCode` DBF-FIN-161 (BUSINESS_FIELD only)}; excluded: ruleLinePk, lineNo, ruleLineDimensionPk, ruleLineId, createdAt
Response     : 201 · `ApiResponse<RuleLineResponse>` — the line with its `dimensionTags[]`
Validations  : in this order, inside one transaction; nothing is written if any fails
- lookup codes amountSourceTypeCode, directionCode, distributionTypeCode, every valueSourceCode and every business-field code (FIN_EVENT_BUSINESS_FIELD) through XM-FIN-001 → FIN-400-INVALID-LOOKUP
- RULE-FIN-018 — trigger: on create (rule line). Statement: reject a rule line whose account derivation type is not an active ACCOUNT_DERIVATION_TYPE value, so that CONSTANT and MAPPING are the only accepted derivations and no derivation reads an account code from the event. Message: ar "نوع اشتقاق الحساب غير مدعوم؛ المسموح قيمة ثابتة أو ربط" · en "Unsupported account derivation type; only constant or mapping is allowed" → FIN-422-UNSUPPORTED-DERIVATION. Membership is read from MDL (`readActiveValuesByKey("ACCOUNT_DERIVATION_TYPE")`, XM-FIN-001); DIRECT is deactivated there by db-script BLOCK 8, so a DIRECT line fails this rule and not the generic lookup check (AC-FIN-048)
- RULE-FIN-019 — trigger: on create (rule line). Statement: require a CONSTANT rule line to carry an account code in accountDerivationValue and no business field, and a MAPPING rule line to carry a business field in accountBusinessFieldCode and no account code. Message: ar "مواصفة اشتقاق الحساب لا تطابق نوعه" · en "The account derivation spec does not match its type" → FIN-422-DERIVATION-SPEC. CHK_FIN_RULE_LINE_DERIVATION_SPEC holds the exclusive-or in the store; the domain check answers first (AC-FIN-049)
- RULE-FIN-003 (v1, SRS §A5) — exactly one remainder line once the set is a compound or percentage distribution, and `isRemainderFl` (DBF-FIN-107) agrees with the line's own REMAINDER type code → FIN-409-REMAINDER-COUNT, FIN-422-REMAINDER-MARKER (QR-FIN-016). v1 cited the marker as DBF-FIN-103; that column is amountSourceTypeCode — corrected here
- RULE-FIN-025 — trigger: on create (rule line with its dimension tags). Statement: reject a rule line that carries more than one dimension tag for the same dimension. Message: ar "لا يجوز وسم البُعد نفسه مرتين على سطر القاعدة" · en "A rule line cannot tag the same dimension twice" → FIN-422-DIMENSION-TAG-DUP, checked over the submitted list (AC-FIN-069); UQ_FIN_RULE_LINE_DIM_LINE_DIM is the store's guarantee
- RULE-FIN-026 — trigger: on create (rule line with its dimension tags). Statement: require a CONSTANT dimension tag to carry a dimension value that belongs to the tagged dimension and no business field, and a BUSINESS_FIELD dimension tag to carry a business field and no dimension value. Message: ar "وسم البُعد لا يطابق مصدر قيمته" · en "The dimension tag does not match its value source" → FIN-422-DIMENSION-TAG-SOURCE (AC-FIN-070: a CONSTANT BRANCH tag citing an ORGANISATION value). An unknown dimension answers FIN-404-DIMENSION and an unknown value FIN-404-DIMVALUE (QR-FIN-061)
Errors       : FIN-404-RULE, FIN-400-INVALID-LOOKUP, FIN-422-UNSUPPORTED-DERIVATION, FIN-422-DERIVATION-SPEC, FIN-409-REMAINDER-COUNT, FIN-422-REMAINDER-MARKER, FIN-404-DIMENSION, FIN-404-DIMVALUE, FIN-422-DIMENSION-TAG-DUP, FIN-422-DIMENSION-TAG-SOURCE, FIN-403-FORBIDDEN, FIN-500
Orchestration: (sets DBF-FIN-100 lineNo = the rule's highest lineNo + 1 · each tag's DBF-FIN-162 ruleLineId from the saved line) load the rule under a row lock (FIN-404-RULE) → validate the lookup codes (XM-FIN-001) → `assertDerivationSupported` (RULE-FIN-018) → `assertDerivationSpec` (RULE-FIN-019) → RULE-FIN-003 over the rule's existing lines plus the new one (QR-FIN-016) → load each tag's dimension and CONSTANT value (QR-FIN-061) → `assertTagsDistinct` (RULE-FIN-025) → `assertTagMatchesSource` (RULE-FIN-026) → persist the line (QR-FIN-015) and its tags (QR-FIN-056) → return the line with its tags
Repository   : QR-FIN-016, QR-FIN-061, QR-FIN-015, QR-FIN-056 · join NONE · READ_WRITE (one transaction)
Concurrency  : lineNo is allocated from a read (the rule's highest lineNo + 1), and RULE-FIN-003 decides on the sibling set this call then extends. Two simultaneous additions to one rule are serialized by a PESSIMISTIC_WRITE load of the parent FIN_EVENT_TYPE_RULE row before either is read. FIN_RULE_LINE has no unique constraint on (event_type_rule_id, line_no), so that lock is the only guard
Security     : screen FIN_RULES · `PERM_FIN_RULES_UPDATE` — adding a line, with its tags, is an update of the rule (SRS SCR-REQ-FIN-003 §B4; tags add no action, ADR-FIN-025) — checked before processing
Localization : every message ar + en
<!-- API:API-FIN-011:END -->

<!-- API:API-FIN-034:START traces=REQ-FIN-007,DBF-FIN-093 -->
### API-FIN-034 — deactivate event-type rule
Entity       : ENT-FIN-009 · operation UPDATE (deactivate)
Endpoint: PUT /api/v1/fin/event-rules/{id}/deactivate · Layers: `EventTypeRuleController.deactivate`→`EventTypeRuleService.deactivate`
Request: path `id`, no body · Response: 200 · `EventTypeRuleResponse` (isActiveFl=false)
Validations: none beyond existence · Errors: `FIN-404-RULE`
Orchestration: (sets DBF-FIN-093 isActiveFl=FALSE) load → clear the active flag (DBF-FIN-093) → persist → return
Repository: `EventTypeRuleRepository.findById` + `save` — no QR id is assigned; the Query Reference Catalog closes at QR-FIN-049 and extending it is a catalog-level change left to ALIGN · join NONE · READ_WRITE
Security: screen FIN_RULES · `PERM_FIN_RULES_UPDATE` (pre-existing — no new constant, no migration) · Localization: n/a
Why it exists: until this endpoint landed no rule could ever be retired, so `FIN-404-NO-ACTIVE-RULE` (RULE-FIN-005, API-FIN-020) was unreachable. **Stated limitation**, recorded in the service's own javadoc: deactivating does NOT free the event type for a replacement rule, because `EventTypeRuleService.create` guards uniqueness with `existsByEventTypeCode`, which is not scoped to the active flag. No `activate` counterpart, and no rule-line delete — ENT-FIN-010 carries no active-flag column and FIN publishes no `DELETE` endpoint on any screen (a deliberate v1 exclusion, see srs-fin.md SCR-REQ-FIN-003 §B4).
Concurrency  : NONE — flips one flag on the row it loads; nothing is allocated or decided on a read
<!-- API:API-FIN-034:END -->

<!-- API:API-FIN-013:START traces=REQ-FIN-022,DBF-FIN-112,DBF-FIN-113,DBF-FIN-114,DBF-FIN-115 -->
### API-FIN-013 — create template
Entity       : ENT-FIN-011, ENT-FIN-012 · operation CREATE
Endpoint: POST /api/v1/fin/recurring-templates · Layers: `RecurringTemplateController.create`→`RecurringTemplateService.create`
Request: (writes DBF-FIN-110 nameAr · DBF-FIN-111 nameEn · DBF-FIN-112 scheduleTypeCode · DBF-FIN-113 frequencyCode · DBF-FIN-114 startDate · DBF-FIN-116 endDate; per line DBF-FIN-125 accountId · DBF-FIN-126 amount · DBF-FIN-127 directionCode · DBF-FIN-128 dimensionValueId) `{nameAr, nameEn, scheduleTypeCode, frequencyCode?, startDate, endDate?, lines: [...]}`
Response: 201 · `RecurringTemplateResponse`
Validations: scheduleTypeCode/frequencyCode/line directionCode validated via XM-FIN-001;
frequencyCode required unless scheduleTypeCode=REVERSING
Errors: `FIN-400-MISSING-FREQUENCY`, `FIN-400-INVALID-LOOKUP`
Orchestration: (sets DBF-FIN-115 nextRunDate=startDate · DBF-FIN-117 isActiveFl=TRUE · per line DBF-FIN-123 recurringTemplateId and DBF-FIN-124 lineNo in submission order) validate → set nextRunDate=startDate → persist template+lines (QR-FIN-018) → return
Repository: QR-FIN-018 · join NONE · READ_WRITE
Security: screen FIN_RECURRING_TEMPLATES · `PERM_FIN_RECURRING_TEMPLATES_CREATE` · Localization: nameAr/nameEn required
Concurrency  : NONE — nothing unique is allocated: every PK comes from its table's own sequence and lineNo is the position in this one request
<!-- API:API-FIN-013:END -->

<!-- API:API-FIN-036:START traces=REQ-FIN-022,DBF-FIN-117 -->
### API-FIN-036 — deactivate template
Entity       : ENT-FIN-011 · operation UPDATE (deactivate)
Endpoint: PUT /api/v1/fin/recurring-templates/{id}/deactivate · Layers: `RecurringTemplateController.deactivate`→`RecurringTemplateService.deactivate`
Request: path `id`, no body · Response: 200 · `RecurringTemplateResponse` (isActiveFl=false)
Validations: none beyond existence · Errors: `FIN-404-TEMPLATE`
Orchestration: (sets DBF-FIN-117 isActiveFl=FALSE) load → `RecurringTemplate.deactivate()` (the entity's own helper, never a direct field assignment) → persist → re-read the template's lines and hand them to the mapper → return. The line re-read is not decoration: `RecurringTemplateResponse.lineCount` is derived from the list the mapper is handed, so passing an empty list would misreport the aggregate as having no lines. It is orchestration (load → map), not a rule.
Repository: `RecurringTemplateRepository.findById` + `save`, plus `RecurringTemplateLineRepository.findByRecurringTemplatePk` for the response's lines — no QR id is assigned; the Query Reference Catalog closes at QR-FIN-049 and extending it is a catalog-level change left to ALIGN · join NONE · READ_WRITE
Security: screen FIN_RECURRING_TEMPLATES · `PERM_FIN_RECURRING_TEMPLATES_UPDATE` (pre-existing — V24 seeds the `FIN_RECURRING_TEMPLATES / UPDATE` action row and V25's blanket Tier-3 grant already covers it, so no new constant, no new error code and no migration) · Localization: n/a
**DEFECT CLOSED 2026-09-12 — deactivate now DOES stop the run.** This paragraph used to report an OPEN DEFECT: `RecurringTemplateService.run` (API-FIN-014) ignored `isActiveFl`, so a deactivated template still ran and still posted. That is no longer true. `RecurringTemplateDomain.assertCanRun()` — a NEW Domain companion for ENT-FIN-011 — is called by `RecurringTemplateService.run` immediately after the row is loaded and before anything else is resolved, and refuses a deactivated template with `FIN-409-NOT-ACTIVE` (`Status.CONFLICT` → HTTP 409; ar "هذا التعريف غير نشط ولا يمكن تشغيله", en "This definition is deactivated and cannot be run"). The screen's 404 was deliberately not reused: the template does exist. **This gate is a RECORDED HUMAN DECISION, not spec.** No RULE-FIN-* states it, and AC-FIN-023 is written "Given an active recurring template" without stating any outcome for an inactive one — a later reader must not mistake the gate for a requirement that was always there. What remains OPEN on this screen is only that a template cannot be UPDATED after creation (next paragraph). Recorded identically at srs-fin.md SCR-REQ-FIN-004 §B4 and §"Access summary".
Why it exists: srs-fin.md SCR-REQ-FIN-004 §B4 recorded the absence of a template deactivate as an OPEN DEFECT rather than a scope decision — `IS_ACTIVE_FL` is NOT NULL, the entity's `activate()`/`deactivate()` shipped with zero callers, B2 advertises an `isActiveFl(EXACT)` filter over a column nothing could set to FALSE, and AC-FIN-023 is written "Given an active recurring template", presupposing a state nothing could produce. Deliberately NOT built, and NOT taken by this endpoint: a template `update` — a correct update must decide the fate of the template's existing child lines, which is a design question no REQ, AC or RULE answers, so it remains the still-open half of the same gap. No `activate` counterpart either; FIN ships none for any entity.
Concurrency  : NONE — flips one flag on the row it loads; nothing is allocated or decided on a read
<!-- API:API-FIN-036:END -->

<!-- API:API-FIN-016:START traces=REQ-FIN-025,DBF-FIN-133,DBF-FIN-142,DBF-FIN-144 -->
### API-FIN-016 — create allocation rule
Entity       : ENT-FIN-013, ENT-FIN-014 · operation CREATE
Endpoint: POST /api/v1/fin/allocation-rules · Layers: `AllocationRuleController.create`→`AllocationRuleService.create`
Request: (writes DBF-FIN-131 nameAr · DBF-FIN-132 nameEn · DBF-FIN-133 sourceAccountId; per target DBF-FIN-142 targetAccountId · DBF-FIN-143 dimensionValueId · DBF-FIN-144 distributionTypeCode · DBF-FIN-145 distributionValue · DBF-FIN-146 isRemainderFl) `{nameAr, nameEn, sourceAccountId, targets: [...]}`
Response: 201 · `AllocationRuleResponse`
Validations: RULE-FIN-003 (reused) — exactly one remainder target when any sibling is
PERCENTAGE or any target is already marked remainder (QR-FIN-016, reused), and each target's
`isRemainderFl` (DBF-FIN-146) must agree with its own `distributionTypeCode`;
distributionTypeCode validated via XM-FIN-001
Errors: `FIN-409-REMAINDER-COUNT`, `FIN-422-REMAINDER-MARKER`, `FIN-404-ACCOUNT`, `FIN-400-INVALID-LOOKUP`
Orchestration: (sets DBF-FIN-134 isActiveFl=TRUE · per target DBF-FIN-140 allocationRuleId and DBF-FIN-141 lineNo in submission order) validate → check RULE-FIN-003 across targets (QR-FIN-016) → persist rule+targets (QR-FIN-021) → return
Repository: QR-FIN-021, QR-FIN-016 · join NONE · READ_WRITE
Security: screen FIN_ALLOCATION_RULES · `PERM_FIN_ALLOCATION_RULES_CREATE` · Localization: nameAr/nameEn required
Concurrency  : NONE — nothing unique is allocated: every PK comes from its table's own sequence and lineNo is the position in this one request
<!-- API:API-FIN-016:END -->

<!-- API:API-FIN-037:START traces=REQ-FIN-025,DBF-FIN-134 -->
### API-FIN-037 — deactivate allocation rule
Entity       : ENT-FIN-013 · operation UPDATE (deactivate)
Endpoint: PUT /api/v1/fin/allocation-rules/{id}/deactivate · Layers: `AllocationRuleController.deactivate`→`AllocationRuleService.deactivate`
Request: path `id`, no body · Response: 200 · `AllocationRuleResponse` (isActiveFl=false)
Validations: none beyond existence — RULE-FIN-003, the one rule `AllocationRuleDomain` owns, governs the remainder-target SET at create and run time and says nothing about the active flag · Errors: `FIN-404-ALLOCATION-RULE`
Orchestration: (sets DBF-FIN-134 isActiveFl=FALSE) load → `AllocationRule.deactivate()` (the entity's own helper, never a direct field assignment) → persist → re-read the rule's targets and hand them to the mapper → return. The target re-read is not decoration: `AllocationRuleResponse.targetCount` is derived from the list the mapper is handed, so passing an empty list would misreport the aggregate as having no targets. It is orchestration (load → map), not a rule.
Repository: `AllocationRuleRepository.findById` + `save`, plus `AllocationTargetRepository.findByAllocationRulePk` for the response's targets — no QR id is assigned; the Query Reference Catalog closes at QR-FIN-049 and extending it is a catalog-level change left to ALIGN · join NONE · READ_WRITE
Security: screen FIN_ALLOCATION_RULES · `PERM_FIN_ALLOCATION_RULES_UPDATE` (pre-existing — V24 seeds the `FIN_ALLOCATION_RULES / UPDATE` action row and V25's blanket Tier-3 grant already covers it, so no new constant, no new error code and no migration) · Localization: n/a
**DEFECT CLOSED 2026-09-12 — deactivate now DOES stop the run.** This paragraph used to report an OPEN DEFECT: `AllocationRuleService.run` (API-FIN-017) ignored `isActiveFl`, so a deactivated rule still ran and still posted, and `AllocationRuleDomain.isActive()` had no caller. That is no longer true. `AllocationRuleDomain.assertCanRun()` is called by `AllocationRuleService.run` immediately after the row is loaded and before the target set is even fetched, and refuses a deactivated rule with `FIN-409-NOT-ACTIVE` (`Status.CONFLICT` → HTTP 409; ar "هذا التعريف غير نشط ولا يمكن تشغيله", en "This definition is deactivated and cannot be run"). The screen's 404 was deliberately not reused: the rule does exist. **This gate is a RECORDED HUMAN DECISION, not spec** — no RULE-FIN-* states it, and a later reader must not mistake it for a requirement that was always there. What remains OPEN on this screen is only that a rule cannot be UPDATED after creation (next paragraph). Recorded identically at srs-fin.md SCR-REQ-FIN-005 §B4 and §"Access summary".
Why it exists: srs-fin.md SCR-REQ-FIN-005 §B4 recorded the absence of a rule deactivate as an OPEN DEFECT rather than a scope decision — `IS_ACTIVE_FL` is NOT NULL, the entity's `activate()`/`deactivate()` shipped with zero callers, `AllocationRuleDomain.isActive()` was dead code, and B2 advertises an `isActiveFl(EXACT)` filter over a column nothing could set to FALSE. Deliberately NOT built, and NOT taken by this endpoint: a rule `update` — a correct update must decide the fate of the rule's existing targets (and therefore of RULE-FIN-003's remainder-target set), which is a design question no REQ, AC or RULE answers, so it remains the still-open half of the same gap. No `activate` counterpart either; FIN ships none for any entity.
Concurrency  : NONE — flips one flag on the row it loads; nothing is allocated or decided on a read
<!-- API:API-FIN-037:END -->

<!-- API:API-FIN-019:START traces=REQ-FIN-014,REQ-FIN-015,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,DBF-FIN-036,DBF-FIN-037,DBF-FIN-038,DBF-FIN-044 -->
### API-FIN-019 — create manual entry
Entity       : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 · operation CREATE
Endpoint: POST /api/v1/fin/journal-entries · Layers: `JournalEntryController.createManual`→`JournalEntryService.createManual`
Request: (writes DBF-FIN-036 docDate · DBF-FIN-037 fiscalYearId · DBF-FIN-038 periodId · DBF-FIN-044 descriptionAr · DBF-FIN-045 descriptionEn; per line DBF-FIN-054 accountId · DBF-FIN-055 amount · DBF-FIN-056 directionCode · DBF-FIN-058 descriptionAr · DBF-FIN-059 descriptionEn; per line dimension DBF-FIN-063 dimensionId · DBF-FIN-064 dimensionValueId) `{docDate, fiscalYearId, periodId, journalTypeCode="MANUAL", descriptionAr,
descriptionEn, lines: [{accountId, amount, directionCode, descriptionAr?, descriptionEn?,
dimensions: [{dimensionId, dimensionValueId}]}]}` — excludes {journalEntryPk, docNo,
statusCode, postedAt, audit}
Response: 201 · `JournalEntryResponse` (statusCode=POSTED on success)
Validations: RULE-FIN-006 (debit=credit, QR-FIN-029), RULE-FIN-007 (leaf/active accounts,
QR-FIN-030), RULE-FIN-008 (period open, QR-FIN-031), RULE-FIN-009 (dimension valid,
QR-FIN-032) — every failure returned together (REQ-FIN-015), nothing posts if any fails.
Header coherence (RULE-FIN-017) runs FIRST and fail-fast: the submitted `periodId` must
belong to the submitted `fiscalYearId` (DBF-FIN-076) and `docDate` must fall inside that
period's [startDate, endDate] (DBF-FIN-080/081) — an incoherent triple makes RULE-FIN-008's
own period gate meaningless, lets the entry take a docNo from the wrong year's series, and
corrupts every period-scoped report and the year-end close. This is the only API that needs
it: every system-generated entry derives the three facts from one another
Errors: `FIN-400-PERIOD-NOT-IN-YEAR`, `FIN-400-DOCDATE-OUTSIDE-PERIOD`, `FIN-409-UNBALANCED`,
`FIN-409-NOT-POSTABLE-ACCOUNT`, `FIN-409-PERIOD-NOT-OPEN`, `FIN-409-INVALID-DIMENSION`
Numbering: `docNo` = `JV-{fiscalYearCode}-{NNNNNN}` — literal prefix `JV-`, the owning
fiscal year's `code` (DBF-FIN-066, VARCHAR(10)), `-`, then a zero-padded 6-digit counter
starting at `000001` (e.g. `JV-2026-000123`; worst case 20 chars, inside
`doc_no VARCHAR(30)`). Counter scoped per `fiscalYearId`, restarting at `000001` each
fiscal year — one counter for all journal types, never segmented by `journalTypeCode`;
`UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO (fiscal_year_id, doc_no)` backs uniqueness at the
database level. Produced by a FIN-local generator in `com.erp.fin`, deliberately NOT a
shared `com.erp.common` component (no platform numbering engine exists in this repo);
implemented in this phase. Assigned once on create, immutable thereafter — excluded from
every create/update request DTO, present only in responses.
Orchestration: (sets DBF-FIN-035 docNo · DBF-FIN-039 journalTypeCode=MANUAL · DBF-FIN-040 statusCode DRAFT→POSTED · DBF-FIN-046 postedAt · per line DBF-FIN-052 journalEntryId, DBF-FIN-053 lineNo and DBF-FIN-057 isRemainderFl=FALSE · per line dimension DBF-FIN-062 journalLineId) resolve fiscal year UNDER A ROW LOCK (`SELECT ... FOR UPDATE` on
FIN_FISCAL_YEAR, the docNo series' allocation lock) → resolve period → check RULE-FIN-017 →
generate docNo (FIN-local generator) → build DRAFT (QR-FIN-024) → validate
(QR-FIN-029..032) → on success: post (QR-FIN-033); on failure: discard the whole attempt
(one transaction, REQ-FIN-015) → return. The lock is what makes the per-fiscal-year counter
safe under concurrency: two simultaneous creates can no longer observe the same predecessor,
so `UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO` stays an unreachable backstop instead of surfacing as an
unlocalized data-integrity 409. A sequence was rejected (the counter restarts per year and the
schema declares none) and so was catch-and-retry (a service may not catch
DataIntegrityViolationException)
Repository: QR-FIN-024, QR-FIN-029, QR-FIN-030, QR-FIN-031, QR-FIN-032, QR-FIN-033 · join
NONE · READ_WRITE (one transaction, build-through-post)
Security: screen FIN_JOURNAL_ENTRIES · `PERM_FIN_JOURNAL_ENTRIES_CREATE` · Localization: descriptionAr/En
Concurrency  : the docNo series — allocated from a read of the fiscal year's last number and then written — is guarded by the fiscal-year row lock taken first in the Orchestration line; UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO stays an unreachable backstop
<!-- API:API-FIN-019:END -->

<!-- API:API-FIN-039:START traces=REQ-FIN-054,REQ-FIN-059,REQ-FIN-060,REQ-FIN-061,DBF-FIN-150,DBF-FIN-151,DBF-FIN-152,DBF-FIN-153,DBF-FIN-154 -->
### API-FIN-039 — create account mapping
Entity       : ENT-FIN-015 · operation CREATE
Endpoint     : POST /api/v1/fin/account-mappings   verb: POST
Layers       : `AccountMappingController.create` → `AccountMappingService.create`; every decision in `AccountMappingDomain`
Request      : body `AccountMappingCreateRequest` — `eventTypeCode` DBF-FIN-150 (String, required, ACCOUNTING_EVENT_TYPE) · `businessFieldCode` DBF-FIN-151 (String, required, FIN_EVENT_BUSINESS_FIELD) · `businessValue` DBF-FIN-152 (String, required, ≤ 100, a code stored as-is) · `accountId` DBF-FIN-153 (Long, required, FK_ACCOUNT_MAPPING_ACCOUNT); excluded: accountMappingPk (from SEQ_FIN_ACCOUNT_MAPPING), isActiveFl, audit fields
Response     : 201 · `ApiResponse<AccountMappingResponse>` (isActiveFl = true)
Validations  : in this order
- lookup codes eventTypeCode (ACCOUNTING_EVENT_TYPE) and businessFieldCode (FIN_EVENT_BUSINESS_FIELD) through XM-FIN-001 → FIN-400-INVALID-LOOKUP
- RULE-FIN-024 — trigger: on create (account mapping). Statement: reject an account mapping for the PAYMENT_METHOD business field whose business value is not an active PAYMENT_METHOD code. Message: ar "القيمة ليست طريقة دفع معرَّفة" · en "The value is not a defined payment method" → FIN-422-INVALID-PAYMENT-METHOD. Only when businessFieldCode = PAYMENT_METHOD; membership in `readActiveValuesByKey("PAYMENT_METHOD")` (XM-FIN-001; AC-FIN-065)
- RULE-FIN-023 — trigger: on create / update (account mapping). Statement: reject an account mapping whose account is not a leaf account or is inactive. Message: ar "الحساب المربوط يجب أن يكون حسابًا ورقيًا نشطًا" · en "The mapped account must be an active leaf account" → FIN-422-MAPPED-ACCOUNT-INELIGIBLE (QR-FIN-060; AC-FIN-064). An unknown accountId → FIN-404-ACCOUNT
- RULE-FIN-022 — trigger: on create (account mapping). Statement: reject an account mapping whose event type, business field and business value already have an active mapping. Message: ar "يوجد بالفعل ربط نشط لنوع الحدث والحقل التجاري والقيمة نفسها" · en "An active mapping already exists for this event type, business field and value" → FIN-409-MAPPING-DUP (QR-FIN-052; AC-FIN-063)
Errors       : FIN-400-INVALID-LOOKUP, FIN-422-INVALID-PAYMENT-METHOD, FIN-404-ACCOUNT, FIN-422-MAPPED-ACCOUNT-INELIGIBLE, FIN-409-MAPPING-DUP, FIN-403-FORBIDDEN, FIN-500
Orchestration: (sets DBF-FIN-154 isActiveFl=TRUE; the PK is drawn from SEQ_FIN_ACCOUNT_MAPPING) validate the two lookup codes (XM-FIN-001) → when businessFieldCode = PAYMENT_METHOD, hand MDL's active PAYMENT_METHOD codes to `assertBusinessValueDefined` (RULE-FIN-024) → load the account (QR-FIN-060) and hand its leaf and active flags to `assertAccountEligible` (RULE-FIN-023) → look for an active mapping on the key (QR-FIN-052) and hand the answer to `assertKeyFree` (RULE-FIN-022) → persist, flushed (QR-FIN-051) → return (AC-FIN-058)
Repository   : QR-FIN-060, QR-FIN-052, QR-FIN-051 · join NONE · READ_WRITE
Concurrency  : two simultaneous creates for one key can both pass QR-FIN-052. The partial unique index UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY (one ACTIVE row per event type, business field and value) makes the second impossible: its flush is refused and FinPersistenceRefusalAdvisor answers FIN-409-MAPPING-DUP, the same answer the pre-check gives (ADR-FIN-030). The pre-check is the friendly path and the index is the guarantee
Security     : screen FIN_ACCOUNT_MAPPINGS · `PERM_FIN_ACCOUNT_MAPPINGS_CREATE`, checked before processing (VIEW is the gateway)
Localization : every message ar + en; no name is stored — the mapping is labelled by its account's nameAr/nameEn
<!-- API:API-FIN-039:END -->

<!-- API:API-FIN-040:START traces=REQ-FIN-056,REQ-FIN-058,REQ-FIN-060,DBF-FIN-149,DBF-FIN-153 -->
### API-FIN-040 — update account mapping (the account only)
Entity       : ENT-FIN-015 · operation UPDATE (change the account)
Endpoint     : PUT /api/v1/fin/account-mappings/{id}   verb: PUT
Layers       : `AccountMappingController.update` → `AccountMappingService.update`
Request      : path `id` → DBF-FIN-149 accountMappingPk; body `AccountMappingUpdateRequest` — `accountId` DBF-FIN-153 (Long, required) and nothing else. eventTypeCode, businessFieldCode and businessValue are fixed after create and are not in the body (ADR-FIN-024); to move a mapping to another key, deactivate it and create a new one
Response     : 200 · `ApiResponse<AccountMappingResponse>`
Validations  :
- RULE-FIN-023 — trigger: on update (account mapping); statement and message as in API-FIN-039 → FIN-422-MAPPED-ACCOUNT-INELIGIBLE (QR-FIN-060); an unknown accountId → FIN-404-ACCOUNT
- no active-state gate: the SRS states none, and a deactivated mapping resolves nothing whatever its account (RULE-FIN-020)
Errors       : FIN-404-ACCOUNT-MAPPING, FIN-404-ACCOUNT, FIN-422-MAPPED-ACCOUNT-INELIGIBLE, FIN-403-FORBIDDEN, FIN-500
Orchestration: (sets DBF-FIN-153 accountId only; updatedBy and updatedAt are filled by the platform) load the mapping (FIN-404-ACCOUNT-MAPPING) → load the account (QR-FIN-060) → `AccountMappingDomain.assertAccountEligible` (RULE-FIN-023) → update (QR-FIN-053) → return (AC-FIN-060). REQ-FIN-058 holds by structure: a posted line stores its resolved accountId and no line references a mapping, so this write cannot reach a posted entry (AC-FIN-062)
Repository   : QR-FIN-060, QR-FIN-053 · join NONE · READ_WRITE
Concurrency  : NONE for the mapping — it sets one column from the request and allocates nothing; two concurrent changes end last-writer-wins, each validated against its own account. The account's eligibility is read and not locked, so an account deactivated a moment later is caught at post time by RULE-FIN-007 (REQ-FIN-052)
Security     : screen FIN_ACCOUNT_MAPPINGS · `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE`, checked before processing
Localization : every message ar + en
<!-- API:API-FIN-040:END -->

<!-- API:API-FIN-041:START traces=REQ-FIN-057,REQ-FIN-058,DBF-FIN-149,DBF-FIN-154 -->
### API-FIN-041 — deactivate account mapping
Entity       : ENT-FIN-015 · operation UPDATE (deactivate)
Endpoint     : PUT /api/v1/fin/account-mappings/{id}/deactivate   verb: PUT — FIN's house style: a soft deactivate under UPDATE, no DELETE endpoint (SRS SCR-REQ-FIN-013 §B4/§B5)
Layers       : `AccountMappingController.deactivate` → `AccountMappingService.deactivate`
Request      : path `id` → DBF-FIN-149 accountMappingPk, no body
Response     : 200 · `ApiResponse<AccountMappingResponse>` (isActiveFl = false)
Validations  : none beyond existence; deactivating a mapping that is already inactive is not an error (the API-FIN-004 precedent)
Errors       : FIN-404-ACCOUNT-MAPPING, FIN-403-FORBIDDEN, FIN-500
Orchestration: (sets DBF-FIN-154 isActiveFl=FALSE) load (FIN-404-ACCOUNT-MAPPING) → `AccountMapping.deactivate()` → persist (QR-FIN-054) → return. From the next event on QR-FIN-055 no longer finds the mapping, so an event that needs it answers FIN-422-UNMAPPED-VALUE (REQ-FIN-057, AC-FIN-061); posted lines keep their account (REQ-FIN-058). No `activate` counterpart (ADR-FIN-024)
Repository   : QR-FIN-054 · join NONE · READ_WRITE
Concurrency  : NONE — flips one flag; nothing is allocated or decided on a read
Security     : screen FIN_ACCOUNT_MAPPINGS · `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE`, checked before processing
Localization : n/a
<!-- API:API-FIN-041:END -->
<!-- SUB:SVC-API-CRUD:END -->

<!-- SUB:SVC-API-INT:START traces=REQ-FIN-010,REQ-FIN-023,REQ-FIN-024,REQ-FIN-026,REQ-FIN-028,REQ-FIN-031,REQ-FIN-034,REQ-FIN-036,REQ-FIN-037,REQ-FIN-050,REQ-FIN-063,REQ-FIN-072,REQ-FIN-073 -->
### SUB — SVC-API-INT (posting-pipeline orchestration and period-control actions)

<!-- API:API-FIN-020:START traces=REQ-FIN-010,REQ-FIN-011,REQ-FIN-012,REQ-FIN-013,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,REQ-FIN-047,REQ-FIN-050,REQ-FIN-051,REQ-FIN-052,REQ-FIN-053,REQ-FIN-063,REQ-FIN-066,REQ-FIN-067,REQ-FIN-068,REQ-FIN-069,REQ-FIN-070,REQ-FIN-072,REQ-FIN-073,REQ-FIN-074,REQ-FIN-075,REQ-FIN-076,DBF-FIN-041,DBF-FIN-090,DBF-FIN-102,DBF-FIN-148,DBF-FIN-153,DBF-FIN-160,DBF-FIN-161,DBF-FIN-164,DBF-FIN-063,DBF-FIN-064 -->
### API-FIN-020 — build event entry (system; MODIFIED v2 — mapped accounts, event dimensions, distinct rejections)
Entity       : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 · operation CREATE (build and post); reads ENT-FIN-009, ENT-FIN-010, ENT-FIN-015, ENT-FIN-016, ENT-FIN-003, ENT-FIN-008
Endpoint     : POST /api/v1/fin/journal-entries/from-event   verb: POST — a system call with no screen affordance (SRS SCR-REQ-FIN-006)
Layers       : `JournalEntryController.buildFromEvent` → `EventEntryService.build` → `JournalPostingService.buildValidateAndPost`
Request      : body `EventEntryBuildRequest` (as published in api-docs-fin.md) — `eventReference` → DBF-FIN-041 (required, ≤ 100) · `eventTypeCode` (required; selects the active rule by DBF-FIN-090) · `docDate` → DBF-FIN-036 (required; selects the period by DBF-FIN-080/081) · `baseAmount` and `amounts{}` (the amount sources the rule lines name) · `fields{}` — the event's business facts, keyed by FIN_EVENT_BUSINESS_FIELD code (PAYMENT_METHOD, EXPENSE_TYPE_CODE, ORGANISATION_CODE, BRANCH_CODE), string values (ADR-FIN-031) · `descriptionAr` → DBF-FIN-044 · `descriptionEn` → DBF-FIN-045. No payload value is ever read as an account code: an extra `fields.accountCode` is simply never looked at (REQ-FIN-047, AC-FIN-047)
Response     : 200 · `ApiResponse<JournalEntryResponse>` (statusCode POSTED, lines with their dimensions) — as published; a rejection writes no row (SRS A7)
Validations  : in build order; the first failure rejects the whole event and nothing is written
- RULE-FIN-004 (MODIFIED v2) — trigger: on create (event-sourced entry), by the pre-check before building and again when the store's uniqueness guarantee on eventReference refuses a concurrent second write. Statement: reject building an entry for an event reference that has already produced a posted entry, and answer with this rule's duplicate-event error whichever detection catches the repeat; a uniqueness refusal from the store is never surfaced as a generic data-integrity or server error. Message: ar "تم بالفعل ترحيل قيد لهذا المرجع" · en "An entry for this event reference has already been posted" → FIN-409-DUPLICATE-EVENT (pre-check QR-FIN-026; the store's refusal — see Concurrency)
- RULE-FIN-005 (v1, SRS §A5) — no active rule for the event type → FIN-404-NO-ACTIVE-RULE (QR-FIN-027)
- RULE-FIN-028 — trigger: on build (event-sourced entry). Statement: reject the event with the no-defined-period configuration error when no accounting period's start and end dates cover the event's date, and never answer this case with the period-not-open error. Message: ar "لا توجد فترة محاسبية معرَّفة تغطي تاريخ الحدث" · en "No accounting period is defined for the event date" → FIN-422-NO-DEFINED-PERIOD (QR-FIN-059 finds no covering period; AC-FIN-080)
- RULE-FIN-003 (v1, SRS §A5) — single remainder marker over the stored line set (QR-FIN-016) → FIN-409-REMAINDER-COUNT, FIN-422-REMAINDER-MARKER
- RULE-FIN-021 — trigger: on build (event-sourced entry, per MAPPING rule line and per BUSINESS_FIELD dimension tag). Statement: reject the event with the missing-business-field error when the event does not carry the business field named by a MAPPING rule line or by a BUSINESS_FIELD dimension tag. Message: ar "الحدث لا يحمل الحقل التجاري الذي تطلبه القاعدة" · en "The event does not carry the business field the rule requires" → FIN-422-MISSING-BUSINESS-FIELD (the named key is absent from `fields{}` or blank; AC-FIN-053, AC-FIN-073)
- RULE-FIN-020 — trigger: on build (event-sourced entry, per MAPPING rule line). Statement: reject the event with the dedicated unmapped-value error when no active account mapping exists for the rule's event type, the rule line's business field and the event's value for that field; never substitute a default account. Message: ar "لا يوجد ربط حساب نشط للقيمة التجارية في الحدث" · en "No active account mapping exists for the business value in the event" → FIN-422-UNMAPPED-VALUE (QR-FIN-055 finds no active row; an inactive mapping counts as none — AC-FIN-056, AC-FIN-057)
- RULE-FIN-027 — trigger: on build (event-sourced entry, per BUSINESS_FIELD dimension tag). Statement: reject the event with the unresolvable-dimension-value error when the event's value for a tag's business field equals the code of no dimension value of the tagged dimension. Message: ar "قيمة البُعد في الحدث لا تطابق أي قيمة معرَّفة لهذا البُعد" · en "The dimension value in the event matches no defined value of this dimension" → FIN-422-UNRESOLVED-DIMENSION-VALUE (QR-FIN-058, exact code within the dimension — ADR-FIN-026; AC-FIN-074). A match that is inactive is RULE-FIN-009's rejection, below
- RULE-FIN-010 (v1, SRS §A5) — the remainder line, computed last per posting side, must come out positive → FIN-422-REMAINDER-NOT-POSITIVE (QR-FIN-028); a MAPPING remainder line computes exactly like any other (AC-FIN-082)
- RULE-FIN-006, RULE-FIN-007, RULE-FIN-008, RULE-FIN-009 (v1, SRS §A5 — applied unchanged to the new path, REQ-FIN-076): debits equal credits (QR-FIN-029) → FIN-409-UNBALANCED; every line's account — constant or mapped — is a leaf, active and postable (QR-FIN-030) → FIN-409-NOT-POSTABLE-ACCOUNT, ar "الحساب المستهدف لا يقبل ترحيلاً مباشرًا" · en "The target account does not accept direct posting" (REQ-FIN-052, AC-FIN-054, AC-FIN-055); the covering period is Open (QR-FIN-031) → FIN-409-PERIOD-NOT-OPEN, ar "الفترة المستهدفة غير مفتوحة" · en "The target period is not open" — the retryable rejection (REQ-FIN-073, AC-FIN-079); every dimension value — the tag's constant one or the one resolved from the event — belongs to its dimension and is active (QR-FIN-032) → FIN-409-INVALID-DIMENSION, ar "قيمة البُعد غير صالحة" · en "The dimension value is invalid" (REQ-FIN-066, REQ-FIN-067, AC-FIN-072)
Errors       : FIN-409-DUPLICATE-EVENT, FIN-404-NO-ACTIVE-RULE, FIN-422-NO-DEFINED-PERIOD, FIN-409-REMAINDER-COUNT, FIN-422-REMAINDER-MARKER, FIN-422-MISSING-BUSINESS-FIELD, FIN-422-UNMAPPED-VALUE, FIN-422-UNRESOLVED-DIMENSION-VALUE, FIN-404-ACCOUNT (a CONSTANT line's account code resolves to no account), FIN-422-REMAINDER-NOT-POSITIVE, FIN-409-UNBALANCED, FIN-409-NOT-POSTABLE-ACCOUNT, FIN-409-PERIOD-NOT-OPEN, FIN-409-INVALID-DIMENSION, FIN-404-YEAR, FIN-403-FORBIDDEN, FIN-500. Retired: FIN-422-MAPPING-UNSUPPORTED — MAPPING is implemented (ADR-FIN-027). No longer raised here: FIN-404-PERIOD for a date no period covers, which is FIN-422-NO-DEFINED-PERIOD now (ADR-FIN-030)
Orchestration: (sets DBF-FIN-035 docNo · DBF-FIN-039 journalTypeCode=EVENT_GENERATED · DBF-FIN-040 statusCode DRAFT→POSTED · DBF-FIN-046 postedAt · DBF-FIN-037 fiscalYearId and DBF-FIN-038 periodId from the covering period · per line DBF-FIN-052, DBF-FIN-053, DBF-FIN-054 accountId (constant or mapped), DBF-FIN-055, DBF-FIN-056, DBF-FIN-057 · per resolved tag one JournalLineDimension row DBF-FIN-062, DBF-FIN-063, DBF-FIN-064 — the shape a manual line writes, REQ-FIN-070)
  1. pre-check RULE-FIN-004 (QR-FIN-026) → 2. resolve the active rule (QR-FIN-027) → 3. resolve the period covering docDate (QR-FIN-059); none → `FiscalPeriodDomain.assertDateCovered` answers RULE-FIN-028. This event path resolves its period itself, so the shared pipeline's FIN-404-PERIOD stays the answer only for API-FIN-014 and API-FIN-017 → 4. load the rule's lines (QR-FIN-025, read half) and their tags (QR-FIN-057); re-check RULE-FIN-003 (QR-FIN-016)
  5. per line, the account: CONSTANT → the account whose code is the line's accountDerivationValue; MAPPING → the value `fields[accountBusinessFieldCode]` (`EventTypeRuleDomain.assertFieldPresent`, RULE-FIN-021) → the active mapping for (the rule's eventTypeCode, the line's business field, that value) (QR-FIN-055, `AccountMappingDomain.assertResolved`, RULE-FIN-020) → its accountId
  6. per line, the amount from its amount source; the remainder line last, per side (QR-FIN-028) → 7. per tag, the dimension value: CONSTANT → the tag's dimensionValueId; BUSINESS_FIELD → the value `fields[businessFieldCode]` (RULE-FIN-021) → the DimensionValue with that code in the tagged dimension (QR-FIN-058, `DimensionValueDomain.assertResolvedByCode`, RULE-FIN-027)
  8. validate RULE-FIN-006 / 007 / 008 / 009 (QR-FIN-029 … QR-FIN-032) → 9. allocate docNo under the fiscal-year row lock (as API-FIN-019) → 10. persist header, lines and line dimensions with the header flushed (QR-FIN-025) → post (QR-FIN-033) → return
Repository   : QR-FIN-026, QR-FIN-027, QR-FIN-059, QR-FIN-025, QR-FIN-057, QR-FIN-016, QR-FIN-055, QR-FIN-028, QR-FIN-058, QR-FIN-029, QR-FIN-030, QR-FIN-031, QR-FIN-032, QR-FIN-033 · join NONE · READ_WRITE (one transaction, build through post; a rejection rolls back every row)
Concurrency  : two simultaneous events with one reference (AC-FIN-078) can both pass the QR-FIN-026 pre-check. UQ_FIN_JOURNAL_ENTRY_EVENT_REF is the guarantee: the header insert is flushed inside the transaction, the store refuses the second, and FinPersistenceRefusalAdvisor (CORE, ADR-FIN-030) translates a refusal naming that constraint into FIN-409-DUPLICATE-EVENT with RULE-FIN-004's message. Exactly one entry posts and the other caller gets the duplicate error, never FIN-500 (REQ-FIN-072). The docNo series stays guarded by the fiscal-year row lock
Security     : system-to-system call under the event consumer's own service principal, gated like any authenticated caller · `PERM_FIN_JOURNAL_ENTRIES_CREATE`. Machine-caller authentication is a separate SEC change set (CS-FIN-001, out of scope)
Localization : every rejection carries its ar + en message; descriptionAr/descriptionEn are stored as sent
<!-- API:API-FIN-020:END -->

<!-- API:API-FIN-014:START traces=REQ-FIN-023,REQ-FIN-024,DBF-FIN-115,DBF-FIN-112 -->
### API-FIN-014 — run template
Entity       : ENT-FIN-011, ENT-FIN-004, ENT-FIN-005 · operation UPDATE (run — posts a RECURRING entry)
Endpoint: POST /api/v1/fin/recurring-templates/{id}/run · Layers: `RecurringTemplateController.run`→`RecurringTemplateService.run`
Request: path `id` (or invoked by an internal scheduler with no path — the endpoint is the
same either way) · Response: 201 · `JournalEntryResponse`
Validations: the template must still be ACTIVE — `RecurringTemplateDomain.assertCanRun()`
(the NEW Domain companion for ENT-FIN-011) runs FIRST, immediately after the row is loaded and
before the period is resolved, and refuses a deactivated template with `FIN-409-NOT-ACTIVE`;
**no RULE-FIN-* states this gate — it rests on a recorded human decision of 2026-09-12, not on
stated requirement.** Then the same RULE-FIN-006/007/008/009 checks as API-FIN-019 (the
template's own lines were already balance-checked at API-FIN-013 create time, but re-validated
here since accounts/periods may have changed since)
Errors: same as API-FIN-019, plus `FIN-404-TEMPLATE` (unknown recurring template id) and
`FIN-409-NOT-ACTIVE` (the template exists but is deactivated)
Orchestration: (sets DBF-FIN-115 nextRunDate, advanced per frequency; the posted entry's DBF-FIN-035 docNo, DBF-FIN-039 journalTypeCode=RECURRING and DBF-FIN-040 statusCode as in API-FIN-019) load template (QR-FIN-019) → assert it is active → build entry from its lines (journalTypeCode=
RECURRING) → validate + post (QR-FIN-029..033) → advance nextRunDate per frequencyCode →
if scheduleTypeCode=REVERSING: also build and post the linked reversal in the next period
(REQ-FIN-024, reusing RULE-FIN-011/012 via QR-FIN-034/036) → return
Repository: QR-FIN-019, QR-FIN-024(-style build), QR-FIN-029..033, QR-FIN-034, QR-FIN-036
· join NONE · READ_WRITE
Security: screen FIN_RECURRING_TEMPLATES · `PERM_FIN_RECURRING_TEMPLATES_UPDATE` (running
a template is modeled as an update-class custom action) · Localization: n/a
Concurrency  : the template row is loaded PESSIMISTIC_WRITE before nextRunDate is read, so two concurrent runs of one template serialize instead of both reading the same predecessor date and both posting (ADR-FIN-041); the posted entry's docNo is separately allocated under the fiscal-year row lock as in API-FIN-019
<!-- API:API-FIN-014:END -->

<!-- API:API-FIN-017:START traces=REQ-FIN-026,DBF-FIN-133,DBF-FIN-145,DBF-FIN-146 -->
### API-FIN-017 — run allocation rule
Entity       : ENT-FIN-013, ENT-FIN-004, ENT-FIN-005 · operation UPDATE (run — posts an ALLOCATION entry)
Endpoint: POST /api/v1/fin/allocation-rules/{id}/run · Layers: `AllocationRuleController.run`→`AllocationRuleService.run`
Request: path `id` · Response: 201 · `JournalEntryResponse`
Validations: the rule must still be ACTIVE — `AllocationRuleDomain.assertCanRun()` runs FIRST,
immediately after the row is loaded and before the target set is even fetched, and refuses a
deactivated rule with `FIN-409-NOT-ACTIVE`; **no RULE-FIN-* states this gate — it rests on a
recorded human decision of 2026-09-12, not on stated requirement.** Then RULE-FIN-003 (single
remainder marker over the stored targets, QR-FIN-016) and RULE-FIN-010 (per-side remainder
guarantee, QR-FIN-028, reused) then RULE-FIN-006/007/008/009
Errors: same family as API-FIN-019, plus `FIN-404-ALLOCATION-RULE`, `FIN-409-NOT-ACTIVE`,
`FIN-409-REMAINDER-COUNT`, `FIN-422-REMAINDER-MARKER`, `FIN-422-REMAINDER-NOT-POSITIVE`
Orchestration: (sets the posted entry's DBF-FIN-035 docNo, DBF-FIN-039 journalTypeCode=ALLOCATION and DBF-FIN-040 statusCode as in API-FIN-019) load rule → assert it is active → load targets + current source-account balance (QR-FIN-022) →
distribute per target's distributionTypeCode, the target marked `isRemainderFl`
(DBF-FIN-146, the single marker — not `distributionTypeCode`) absorbing the rounding
difference between the source line's side and the targets' own side (QR-FIN-028) → build
entry (journalTypeCode=ALLOCATION) → validate + post (QR-FIN-029..033) → return
Repository: QR-FIN-022, QR-FIN-028, QR-FIN-029..033 · join NONE · READ_WRITE
Security: screen FIN_ALLOCATION_RULES · `PERM_FIN_ALLOCATION_RULES_UPDATE` · Localization: n/a
Concurrency  : the posted entry's docNo is allocated under the same fiscal-year row lock as API-FIN-019 (JournalPostingService); nothing else is allocated. The source balance this run reads (QR-FIN-022, QR-FIN-043) is read without a lock and the entry is posted for that amount, so two concurrent runs of one rule both read the same balance and both post — a source of 1,000.00 can end at −1,000.00. v1 as built; stated here, not changed by v2 (ADR-FIN-042)
<!-- API:API-FIN-017:END -->

<!-- API:API-FIN-021:START traces=REQ-FIN-028,REQ-FIN-029,REQ-FIN-030,DBF-FIN-042,DBF-FIN-043,REQ-FIN-077 -->
### API-FIN-021 — reverse entry
Entity       : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 · operation CREATE (reverse — posts the mirror entry; custom action)
Endpoint: POST /api/v1/fin/journal-entries/{id}/reverse · Layers: `JournalEntryController.reverse`→`JournalEntryService.reverse`
Request: path `id` · Response: 201 · `JournalEntryResponse` (the new reversal entry)
Validations: RULE-FIN-013 (must be POSTED and not already reversed, QR-FIN-035); RULE-FIN-012
(period substitution if original's is closed, QR-FIN-036)
Errors: `FIN-409-NOT-POSTED`, `FIN-409-ALREADY-REVERSED`, `FIN-404-ENTRY`
Orchestration: (sets DBF-FIN-042 originalEntryId on the reversal · DBF-FIN-043 reversalEntryId on the original · the reversal's DBF-FIN-039 journalTypeCode=REVERSAL) load original UNDER A ROW LOCK (`SELECT ... FOR UPDATE` on the
FIN_JOURNAL_ENTRY header — no FIN entity carries `@Version`, so without it two concurrent
reversals both read reversalEntryId as null, both post a mirror, and the second save
overwrites the first link, leaving one orphaned mirror and a net effect of −(original)) →
check RULE-FIN-013 (QR-FIN-035) → resolve posting period
(original's if Open, else the current open period per RULE-FIN-012, QR-FIN-036) → build
mirrored lines with opposite directions, same amounts (RULE-FIN-011, QR-FIN-034) →
validate + post (QR-FIN-029..033, journalTypeCode=REVERSAL) → set originalEntryId/
reversalEntryId on both rows (bidirectional link) → return. Classic reversal: the original
STAYS POSTED — the only write back to it is the reversalEntryId link (DBF-FIN-043); its
statusCode is never modified, so both entries are seen by the POSTED-only report queries and
the pair nets to zero (SRS A7). Because the original stays POSTED, the double-reversal half of
RULE-FIN-013 is an explicit guard on the reversal link: a second reverse on the same entry is
rejected with `FIN-409-ALREADY-REVERSED` (AC-FIN-030), which would otherwise post a second
mirror and leave a net effect of −(original).
Repository: QR-FIN-034, QR-FIN-035, QR-FIN-036, QR-FIN-029..033 · join NONE · READ_WRITE
Security: screen FIN_JOURNAL_ENTRIES · `PERM_FIN_JOURNAL_ENTRIES_REVERSE` (custom action) · Localization: n/a
Concurrency  : the already-reversed guard is a read-then-write, made atomic by the PESSIMISTIC_WRITE load of the original's header in the Orchestration line; the mirror's docNo is allocated under the fiscal-year row lock
v2: an entry posted through an account mapping or dimension tags reverses exactly like any other. The mirror copies each line's stored accountId and its JournalLineDimension rows with the opposite direction, the original stays POSTED, and every account and dimension balance nets to zero (REQ-FIN-077, AC-FIN-083). No mapping is consulted: a reversal reads the posted lines, never the rules or mappings that built them.
<!-- API:API-FIN-021:END -->

<!-- API:API-FIN-023:START traces=REQ-FIN-031,DBF-FIN-066,DBF-FIN-067,DBF-FIN-068,DBF-FIN-077 -->
### API-FIN-023 — create fiscal year
Entity       : ENT-FIN-007, ENT-FIN-008 · operation CREATE
Endpoint: POST /api/v1/fin/fiscal-years · Layers: `FiscalYearController.create`→`FiscalYearService.create`
Request: (writes DBF-FIN-066 code · DBF-FIN-067 startDate · DBF-FIN-068 endDate) `{code, startDate, endDate, periodCount}` · Response: 201 · `FiscalYearResponse` with its generated periods
Validations: uniqueness of code (QR-FIN-047)
Errors: `FIN-409-YEAR-DUP`
Orchestration: (sets DBF-FIN-069 statusCode=OPEN · DBF-FIN-070 isActiveFl=TRUE; per generated period DBF-FIN-076 fiscalYearId · DBF-FIN-077 periodNo · DBF-FIN-078 nameAr · DBF-FIN-079 nameEn · DBF-FIN-080 startDate · DBF-FIN-081 endDate · DBF-FIN-082 statusCode=OPEN) validate uniqueness → persist year (statusCode=OPEN) + generate periodCount periods,
each statusCode=OPEN (QR-FIN-038) → return
Period spans and names (governed, not implicit): when `periodCount` = 12 and
[startDate, endDate] spans one whole calendar year, each generated period IS a calendar
month — period N runs from the 1st to the last day of the Nth month of the span — and its
NAME_AR / NAME_EN (DBF-FIN-078/079, both NOT NULL) are that month's own name in Arabic and
English, taken from the platform's locale data (`java.time.Month` + CLDR), never a
hardcoded string table. For any other `periodCount`, or a span that is not a whole year,
the fallback is the even split: the days of [startDate, endDate] divided into periodCount
contiguous blocks, the first (totalDays mod periodCount) blocks one day longer, so the last
period always ends on the year's endDate and no day belongs to two periods; those periods
are named "الفترة N" / "Period N".
Repository: QR-FIN-038 · join NONE · READ_WRITE
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_CREATE` · Localization: n/a
Concurrency  : the code pre-check is the friendly path; UQ_FIN_FISCAL_YEAR_CODE is the guarantee. Two simultaneous requests can both pass the pre-check, and the store then refuses the second. UQ_FIN_FISCAL_YEAR_CODE is not registered with FinPersistenceRefusalAdvisor (ADR-FIN-030 registers only the two v2 constraints), so that refusal still surfaces as the platform's generic error instead of FIN-409-YEAR-DUP — v1 behaviour as built, recorded here and not changed by v2.
<!-- API:API-FIN-023:END -->

<!-- API:API-FIN-024:START traces=REQ-FIN-032,DBF-FIN-082 -->
### API-FIN-024 — open period
Entity       : ENT-FIN-008 · operation UPDATE (open)
Endpoint: PATCH /api/v1/fin/fiscal-periods/{id}/open · Layers: `FiscalPeriodController.open`→`FiscalPeriodService.open`
Request: path `id` · Response: 200 · `FiscalPeriodResponse`
Validations: current state must be SOFT_CLOSE (§10.2 "re-openable" — Hard Closed is not, RULE-FIN-014)
Errors: `FIN-409-NOT-REOPENABLE`, `FIN-404-PERIOD`
Orchestration: (sets DBF-FIN-082 statusCode=OPEN) load the period PESSIMISTIC_WRITE → check RULE-FIN-014 (QR-FIN-040) → transition (QR-FIN-039) → return
Repository: QR-FIN-039, QR-FIN-040 · join NONE · READ_WRITE
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_UPDATE` · Localization: n/a
Concurrency  : the period row is loaded PESSIMISTIC_WRITE before the current-status check, so two concurrent transitions of one period serialize instead of racing last-writer-wins (ADR-FIN-041)
<!-- API:API-FIN-024:END -->

<!-- API:API-FIN-025:START traces=REQ-FIN-033,DBF-FIN-082 -->
### API-FIN-025 — soft-close period
Entity       : ENT-FIN-008 · operation UPDATE (soft-close)
Endpoint: PATCH /api/v1/fin/fiscal-periods/{id}/soft-close · Layers: `FiscalPeriodController.softClose`→`FiscalPeriodService.softClose`
Request: path `id` · Response: 200 · `FiscalPeriodResponse`
Validations: current state must be OPEN · Errors: `FIN-409-INVALID-TRANSITION`, `FIN-404-PERIOD`
Orchestration: (sets DBF-FIN-082 statusCode=SOFT_CLOSE) load the period PESSIMISTIC_WRITE → transition (QR-FIN-039) → return · Repository: QR-FIN-039 · join NONE · READ_WRITE
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_UPDATE` · Localization: n/a
Concurrency  : the period row is loaded PESSIMISTIC_WRITE before the current-status check, so two concurrent transitions of one period serialize instead of racing last-writer-wins (ADR-FIN-041)
<!-- API:API-FIN-025:END -->

<!-- API:API-FIN-026:START traces=REQ-FIN-034,REQ-FIN-035,REQ-FIN-037,REQ-FIN-038,DBF-FIN-082,DBF-FIN-083,DBF-FIN-084 -->
### API-FIN-026 — hard-close period (approval)
Entity       : ENT-FIN-008 · operation UPDATE (hard-close — custom close-approve action)
Endpoint: PATCH /api/v1/fin/fiscal-periods/{id}/hard-close · Layers: `FiscalPeriodController.hardClose`→`FiscalPeriodService.hardClose`
Request: path `id` · Response: 200 · `FiscalPeriodResponse`
Validations: RULE-FIN-014 (not already Hard Closed, QR-FIN-040); RULE-FIN-015 (full text:
DATA-DOM §ENT-FIN-008) — caller must hold `PERM_FIN_PERIODS_CLOSE_APPROVE`, a permission
distinct from `PERM_FIN_JOURNAL_ENTRIES_CREATE`. **That gate is the WHOLE of the rule**: the
`@PreAuthorize` on `FiscalPeriodService.hardClose`, enforced by SEC's own mechanism, exactly as
RULE-FIN-015 states it and AC-FIN-038 describes it. There is NO service-layer SoD read — the
one that existed was deleted on 2026-09-12 as an over-implementation (it refused the close for
every caller whenever any single user held both codes); see the Error Catalog's struck
`FIN-403-SOD-VIOLATION` row and retired XM-FIN-002
Errors: `FIN-409-NOT-REOPENABLE`(reused message context), `FIN-404-PERIOD`
Orchestration: (sets DBF-FIN-082 statusCode=HARD_CLOSE · DBF-FIN-083 closedBy · DBF-FIN-084 closedAt) load the period PESSIMISTIC_WRITE → check RULE-FIN-014 (QR-FIN-040)
→ transition, set closedBy/closedAt to the approving principal (QR-FIN-039) → return
Repository: QR-FIN-039, QR-FIN-040 · join NONE · READ_WRITE
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_CLOSE_APPROVE` (custom — the distinct permission RULE-FIN-015 requires; since V30 it is also granted to SYS_ADMIN) · Localization: n/a
Concurrency  : the period row is loaded PESSIMISTIC_WRITE before the current-status check, so two concurrent transitions of one period serialize instead of racing last-writer-wins (ADR-FIN-041)
<!-- API:API-FIN-026:END -->

<!-- API:API-FIN-027:START traces=REQ-FIN-036,DBF-FIN-069,DBF-FIN-034 -->
### API-FIN-027 — run year-end close
Entity       : ENT-FIN-007, ENT-FIN-008, ENT-FIN-004 · operation UPDATE (year-end close; creates the CLOSING and OPENING entries)
Endpoint: POST /api/v1/fin/fiscal-years/{id}/year-end-close · Layers: `FiscalYearController.yearEndClose`→`FiscalYearService.yearEndClose`
Request: path `id` · Response: 201 · `{closingEntry: JournalEntryResponse, openingEntry: JournalEntryResponse}`
Validations, IN THIS ORDER (the order is part of the contract — a test asserting 409 on this
endpoint must not be defeated by a missing successor year or Retained Earnings
account): (1) the fiscal year's own statusCode must
still be OPEN — a re-run against an already-CLOSED year is rejected with
`FIN-409-INVALID-TRANSITION`, a stated rule on ENT-FIN-007's state machine rather than the
incidental `FIN-409-PERIODS-NOT-CLOSED` the first run's own YEAR_END_CLOSE transitions would
otherwise raise; (2) every period of the year must be HARD_CLOSE (§10.4 precondition), 409.
Only then are the successor year and the Retained Earnings account resolved.
RULE-FIN-015 is no longer a numbered step here: it is enforced entirely by this endpoint's
`@PreAuthorize(PERM_FIN_PERIODS_CLOSE_APPROVE)` gate, which rejects before the method body
runs. The step that used to come first — a service-layer SoD read answering 403 — was deleted
on 2026-09-12 as an over-implementation; see the Error Catalog's struck `FIN-403-SOD-VIOLATION`
row. **A test written against the old contract and asserting 403 from inside this method will
now fail, and should be retargeted at the `@PreAuthorize` gate.**
Errors: `FIN-409-INVALID-TRANSITION` (year already closed), `FIN-409-PERIODS-NOT-CLOSED`, `FIN-404-YEAR` (unknown year id, or no adjacent successor year), `FIN-404-ACCOUNT` (no account marked as Retained Earnings)
Orchestration: (sets DBF-FIN-069 statusCode=CLOSED · DBF-FIN-082 statusCode=YEAR_END_CLOSE on every period · the two entries' DBF-FIN-039 journalTypeCode=CLOSING/OPENING) load the fiscal year PESSIMISTIC_WRITE → verify the year is still OPEN → verify all periods Hard
Closed → resolve the successor year (QR-FIN-048) and the Retained Earnings account
(QR-FIN-045) → compute closing
balances (QR-FIN-041) →
build + post a closing entry into the year's LAST period (result accounts → Retained
Earnings, journalTypeCode=CLOSING). The Retained Earnings account is DERIVED BY THE SYSTEM
from FIN_ACCOUNT.is_retained_earnings_fl (DBF-FIN-147, at most one row TRUE — partial unique
index UQ_FIN_ACCOUNT_RETAINED_EARNINGS) and is never supplied by the caller; no marked
account is `FIN-404-ACCOUNT`
→ build + post the next year's opening entry from the resulting balance-sheet balances
(journalTypeCode=OPENING, QR-FIN-041, POL-FIN-010) — CLOSING/OPENING are two values added
to the JOURNAL_TYPE lookup this stage (see Decisions Applied — a data-only, non-breaking
extension, not a new REQ/RULE) → mark FiscalYear statusCode=CLOSED, periods
statusCode=YEAR_END_CLOSE → return
Repository: QR-FIN-041, QR-FIN-029, QR-FIN-030, QR-FIN-032, QR-FIN-033 (both generated
entries go through the same validated posting pipeline) · QR-FIN-031 is deliberately NOT
applied to either of them: RULE-FIN-008 itself exempts the year-end closing and opening
entries, since this API's own all-periods-Hard-Closed precondition leaves no Open period for
the closing entry to post into. RULE-FIN-006/007/009 apply to both in full · the successor
fiscal year receiving the opening entry is resolved by DATE ADJACENCY (the year whose
startDate is the day after this year's endDate) — a DERIVED decision, recorded as such in
execution-state.json because ENT-FIN-007 declares no successor column; no such year is
`FIN-404-YEAR` · join intra-module · READ_WRITE
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_CLOSE_APPROVE` · Localization: n/a
DORMANT YEAR — a reviewed decision, not an accident. For a fiscal year in which no result account
carries a non-zero balance, `FiscalYearService.closingLines` contributes no line at all (each
result account with `net().signum() == 0` is skipped, and the Retained Earnings absorbing line is
added only when the running `resultTotal` is itself non-zero), and symmetrically
`openingLines` contributes none when no balance-sheet account carries a non-zero balance. The run
therefore posts a CLOSING (and OPENING) journal entry with an EMPTY line set, and that entry still
consumes a `docNo`: `JournalPostingService.buildValidateAndPost` allocates the number from the
locked fiscal-year series before it validates, and writes the entry unconditionally.
This was reviewed and deliberately KEPT. `JournalEntryDomain.checkBalanced` sums debits and
credits and returns empty when they compare equal, so 0 = 0 passes — a lineless entry literally
satisfies AC-FIN-036's "both entries individually balanced". Omitting the entry would make
AC-FIN-036's "posts a closing entry" false, and refusing the close outright would invent a rule no
artifact states.
Reachable only from here. `JournalEntryCreateRequest.lines` carries `@NotEmpty`, so API-FIN-019
rejects a lineless entry at the DTO boundary. This shape exists solely on the internal year-end
path, which builds its lines itself and never passes through that DTO.
Concurrency  : the fiscal year row is loaded PESSIMISTIC_WRITE at the top of the transaction, before the OPEN-status check, so two concurrent runs of year-end close on one year serialize instead of both passing the OPEN check and each posting a CLOSING/OPENING pair (ADR-FIN-041); the same lock backs the docNo allocation later in the same method
<!-- API:API-FIN-027:END -->
<!-- SUB:SVC-API-INT:END -->
<!-- PHASE:SVC-API:END -->

<!-- PHASE:DOC:START traces=REQ-FIN-017,REQ-FIN-072,REQ-FIN-073,REQ-FIN-074,REQ-FIN-075 -->
## PHASE 4 — DOC

**API contract summary** (backend self-check only; the frontend binds to the real `api-docs-fin.md` published after
implementation): 41 endpoints under `/api/v1/fin`, one per API-FIN-001 … API-FIN-041, listed with verb and path in
the API Registry of the Plan Index — not restated here.

**DTO typing constraints**: every lookup-backed field is a `String` holding the code, never a Java enum —
accountTypeCode, natureCode, directionCode, journalTypeCode, statusCode, eventTypeCode, accountDerivationTypeCode,
amountSourceTypeCode, distributionTypeCode, scheduleTypeCode, frequencyCode, and in v2 accountBusinessFieldCode,
businessFieldCode, valueSourceCode and (for the PAYMENT_METHOD field) businessValue. `docNo` never appears in a
request body and always appears in a response.

**Pagination and filter standard**: `{filters, page, size, sort}` in the body, `Page<T>` in `ApiResponse<T>`, as
SEC's and MDL's own CORE phases define it.

**Event rejection contract for the consumer (REQ-FIN-075, ADR-FIN-027, ADR-FIN-030)**. The event consumer branches on
the full code of an API-FIN-020 rejection, never on the HTTP status alone: several unrelated rejections share 409.
Each code appears in the Error Catalog exactly once, with its ar/en message, and the published api-docs carry the
same three classes:

| Class | Code | HTTP | Consumer action |
|---|---|---|---|
| already posted | FIN-409-DUPLICATE-EVENT | 409 | acknowledge the message: the event posted the first time, whether the pre-check or the store's uniqueness guarantee caught the repeat (REQ-FIN-072) |
| retry later | FIN-409-PERIOD-NOT-OPEN | 409 | wait and retry: the rejection clears once the target period is opened. Only a SOFT_CLOSE period can return to OPEN; a HARD_CLOSE or YEAR_END_CLOSE period never will, so a retry policy needs an upper bound (REQ-FIN-073) |
| needs a fix | every other code API-FIN-020 lists — among them FIN-422-UNMAPPED-VALUE (add a mapping), FIN-422-MISSING-BUSINESS-FIELD (fix the producer), FIN-422-UNRESOLVED-DIMENSION-VALUE (define the dimension value), FIN-422-NO-DEFINED-PERIOD (define the calendar) | 404 / 409 / 422 | mark the message failed; each code names the party that fixes it (ADR-FIN-027) |

**Event payload** (ADR-FIN-031): business facts travel in the request's `fields{}` map, keyed by
FIN_EVENT_BUSINESS_FIELD codes (PAYMENT_METHOD, EXPENSE_TYPE_CODE, ORGANISATION_CODE, BRANCH_CODE); the key set
grows as MDL data, not as a new request field. An account code is never read from the payload.
<!-- PHASE:DOC:END -->

<!-- PHASE:INT-C:START traces=REQ-FIN-001,REQ-FIN-045,REQ-FIN-048,REQ-FIN-054,REQ-FIN-061,REQ-FIN-062 -->
## PHASE 5 — INT-C (cross-module consume)

One live `XM-*` row, below the split threshold (1 < 5), so no SUB. XM-FIN-002 stays below as a retired, struck
historical block; it is not a live dependency. v2 mints no XM: the six new lookup-backed columns widen XM-FIN-001,
and the new screen's registration into SEC is platform integration under ADR-FIN-001, like every v1 screen.

| XM | Classification | Target | Interface (in_process — one deployable) | What the target publishes | Status |
|---|---|---|---|---|---|
| XM-FIN-001 | SOFT-READ | MDL — ENT-MDL-001, ENT-MDL-002 | injected `com.erp.mdl.crossmodule.MdlLookupApi` | `readActiveValuesByKey(typeKey)` → `List<LookupOptionView>` | ACTIVE (MODIFIED v2 — 15 keys) |
| XM-FIN-002 | READ (historical) | SEC | none — the consuming service was deleted | — | RETIRED 2026-09-12 |

<!-- XM:XM-FIN-001:START traces=REQ-FIN-001,REQ-FIN-007,REQ-FIN-008,REQ-FIN-010,REQ-FIN-014,REQ-FIN-018,REQ-FIN-022,REQ-FIN-025,REQ-FIN-031,REQ-FIN-045,REQ-FIN-048,REQ-FIN-049,REQ-FIN-054,REQ-FIN-061,REQ-FIN-062 -->
### XM-FIN-001 — validate and read lookup-backed codes against MDL (MODIFIED v2 — 15 keys, 6 more columns)
Target        : MDL · ENT-MDL-001 (LookupType), ENT-MDL-002 (LookupValue) · classification SOFT-READ
Interface     : in-process (profile.conventions.module_interface = in_process) — FIN's services inject MDL's published
                `com.erp.mdl.crossmodule.MdlLookupApi` and call `readActiveValuesByKey(typeKey)`, consuming
                `LookupOptionView` (code, labelAr, labelEn, sortOrder) and never an MDL entity. One deployable: no
                HTTP client, no base path, no timeout, no network error path
Contract      : data required = the submitted code is an active value of the named FIN-owned key. The v1 columns
                are unchanged; v2 adds six: the rule line's accountBusinessFieldCode, the mapping's eventTypeCode,
                businessFieldCode and — only when businessFieldCode is PAYMENT_METHOD — businessValue, and the tag's
                valueSourceCode and businessFieldCode (DBF-FIN-148, DBF-FIN-150, DBF-FIN-151, DBF-FIN-152,
                DBF-FIN-160, DBF-FIN-161). Fallback if absent = reject with FIN-400-INVALID-LOOKUP, except
                ACCOUNT_DERIVATION_TYPE (RULE-FIN-018 → FIN-422-UNSUPPORTED-DERIVATION) and PAYMENT_METHOD as a
                business value (RULE-FIN-024 → FIN-422-INVALID-PAYMENT-METHOD). MDL's own not-found for an unknown
                key is caught and answered as FIN-400-INVALID-LOOKUP; MDL's code never leaks out of FIN's API.
                Retry: none (same process). Idempotency: read-only
Blocks        : none DEFERRED — MDL v1 is gated, the row is ACTIVE. v2 depends on db-script BLOCK 8 (2 new keys, 6
                values, CHEQUE, DIRECT retired) having run before API-FIN-011 or API-FIN-039 is first called —
                listed with its producer in SEC-BE's bootstrap rows. Accepted risk, unchanged: a value deactivated
                in MDL after a FIN row cites it is not retro-invalidated (db-script §2.1)
<!-- XM:XM-FIN-001:END -->

FIN's dependency on SEC for identity and authorization — the principal on every request, the `@PreAuthorize` gate,
and FIN's own registration of its module, screens and actions into SEC, the account-mappings screen included —
remains platform integration and not an `XM` row (ADR-FIN-001, carried from P2). Since 2026-09-12 it is the only
relationship FIN has with SEC: XM-FIN-002 was registered for FIN's read of SEC's data about other users, and that
read was deleted with the service that made it.
<!-- carried from baseline v1 by gov.py state — not re-emitted in this delta -->
<!-- XM:XM-FIN-002:START traces=REQ-FIN-037,REQ-FIN-038 -->
### ~~XM-FIN-002~~ — RETIRED 2026-09-12 (historical record, NOT a live dependency)
Status        : RETIRED. Kept so a reader reconstructing the decision can see what was
registered and why it went away. It binds nothing today and no code consumes it. The id is
burned, not reused.
What it was   : a READ of SEC's user→permission directory, classification READ, target SEC ·
SEC's ENT-SEC-001 (User) + its role/permission grant tables. `FinSeparationOfDutiesService`
injected `com.erp.sec.crossmodule.SecUserDirectoryApi` and called
`findUserIdsHoldingPermission(permissionCode)` twice, for `PERM_FIN_PERIODS_CLOSE_APPROVE` and
`PERM_FIN_JOURNAL_ENTRIES_CREATE`, on every API-FIN-026 hard-close and API-FIN-027 year-end
close (never HTTP); it derived `closeApprovePermissionHeld` and `entryCreatePermissionShared`
and handed them to `FiscalPeriodDomain.assertCanHardClose`, which threw
`FIN-403-SOD-VIOLATION`.
Why it is gone: that check enforced GLOBAL user-set disjointness — if ANY single user in the
system held both permissions, the close was refused for EVERY caller, including a perfectly
clean approver — and no REQ, AC or RULE requires it. RULE-FIN-015 (srs-fin.md:1026-1033)
requires only that the close-approval action be gated by a permission DISTINCT from the
journal-entry-creation permission, "enforced through the Security module", and its `Data
source` line reads "DEFERRED — ... has no FIN-side field to read"; REQ-FIN-038
(srs-fin.md:781-788) and AC-FIN-038 (:789-792) say the same, the latter describing an ordinary
interceptor denial. By recorded human decision on 2026-09-12 the over-implementation was
removed: `FinSeparationOfDutiesService` and `FiscalPeriodDomain.assertCanHardClose(...)` were
deleted. RULE-FIN-015 still stands, enforced by the delivered
`@PreAuthorize(PERM_FIN_PERIODS_CLOSE_APPROVE)` gate on `FiscalPeriodService.hardClose` and
`FiscalYearService.yearEndClose`.
Consequence   : that service was FIN's ONLY consumer of `com.erp.sec.crossmodule`, so **FIN's
cross-module dependency on SEC is gone entirely**. The only remaining `com.erp.sec` mentions
under `src/main/java/com/erp/fin/` are `@PreAuthorize` SpEL string literals naming
`PermissionConstants`, plus two javadoc references — neither is a structural dependency.
`FIN-403-SOD-VIOLATION` is correspondingly struck as unreachable in the Error Catalog.
<!-- XM:XM-FIN-002:END -->
<!-- PHASE:INT-C:END -->

<!-- PHASE:INT-R:START traces=REQ-FIN-001,REQ-FIN-045 -->
## PHASE 6 — INT-R (cross-module resolve)

| XM | Status | Workaround (if not READY / ACTIVE) |
|---|---|---|
| XM-FIN-001 | ACTIVE | not applicable — MDL is gated. The v2 widening needs no MDL change: MDL receives FIN's new keys and values as data (db-script BLOCK 8) |
| XM-FIN-002 | RETIRED 2026-09-12 | not applicable — historical; the service that consumed it was deleted |

No DEFERRED row exists. The next module to consume FIN is outside this batch; the inbound stub
`XM-INBOUND-STUB-3` names FIN as its eventual target, the formal id to be assigned by that module's own P2. The
legacy Oracle host of CS-FIN-001 reaches FIN only through the event endpoint API-FIN-020, via a consumer that is out
of scope; it has no table access in either direction and so no XM (db-script §2).
<!-- PHASE:INT-R:END -->

<!-- PHASE:SEC-BE:START traces=REQ-FIN-038,REQ-FIN-044,REQ-FIN-045,REQ-FIN-054 -->
## PHASE 7 — SEC-BE (security, backend half)

Permission matrix — one row per composite screen; a `✓` cell names the endpoints it serves and the permission for
that action. FIN publishes no `DELETE` endpoint, so the DELETE column is empty everywhere.

| Screen (page code) | ENT | VIEW | CREATE | UPDATE | DELETE | Custom |
|---|---|---|---|---|---|---|
| FIN_ACCOUNTS | ENT-FIN-001 | ✓ `PERM_FIN_ACCOUNTS_VIEW` (API-FIN-001) | ✓ `PERM_FIN_ACCOUNTS_CREATE` (API-FIN-002) | ✓ `PERM_FIN_ACCOUNTS_UPDATE` (API-FIN-003; API-FIN-004 deactivate) | — | — |
| FIN_DIMENSIONS | ENT-FIN-002, ENT-FIN-003 | ✓ `PERM_FIN_DIMENSIONS_VIEW` (API-FIN-005, API-FIN-008) | ✓ `PERM_FIN_DIMENSIONS_CREATE` (API-FIN-006, API-FIN-007) | ✓ `PERM_FIN_DIMENSIONS_UPDATE` (API-FIN-035, deactivate a dimension value — V28) | — | — |
| FIN_RULES | ENT-FIN-009, ENT-FIN-010, ENT-FIN-016 | ✓ `PERM_FIN_RULES_VIEW` (API-FIN-009) | ✓ `PERM_FIN_RULES_CREATE` (API-FIN-010) | ✓ `PERM_FIN_RULES_UPDATE` (API-FIN-011, add a line with its dimension tags; API-FIN-034, deactivate a rule) | — | — |
| FIN_RECURRING_TEMPLATES | ENT-FIN-011, ENT-FIN-012 | ✓ `PERM_FIN_RECURRING_TEMPLATES_VIEW` (API-FIN-012) | ✓ `PERM_FIN_RECURRING_TEMPLATES_CREATE` (API-FIN-013) | ✓ `PERM_FIN_RECURRING_TEMPLATES_UPDATE` (API-FIN-014, run; API-FIN-036, deactivate) | — | — |
| FIN_ALLOCATION_RULES | ENT-FIN-013, ENT-FIN-014 | ✓ `PERM_FIN_ALLOCATION_RULES_VIEW` (API-FIN-015) | ✓ `PERM_FIN_ALLOCATION_RULES_CREATE` (API-FIN-016) | ✓ `PERM_FIN_ALLOCATION_RULES_UPDATE` (API-FIN-017, run; API-FIN-037, deactivate) | — | — |
| FIN_JOURNAL_ENTRIES | ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 | ✓ `PERM_FIN_JOURNAL_ENTRIES_VIEW` (API-FIN-018, API-FIN-022) | ✓ `PERM_FIN_JOURNAL_ENTRIES_CREATE` (API-FIN-019, API-FIN-020) | — | — | Reverse `PERM_FIN_JOURNAL_ENTRIES_REVERSE` (API-FIN-021) |
| FIN_PERIODS | ENT-FIN-007, ENT-FIN-008 | ✓ `PERM_FIN_PERIODS_VIEW` (API-FIN-033, and the gateway) | ✓ `PERM_FIN_PERIODS_CREATE` (API-FIN-023) | ✓ `PERM_FIN_PERIODS_UPDATE` (API-FIN-024, API-FIN-025) | — | Close-approve `PERM_FIN_PERIODS_CLOSE_APPROVE` (API-FIN-026, API-FIN-027 — the distinct permission RULE-FIN-015 requires) |
| FIN_ACCOUNT_LEDGER | ENT-FIN-005 | ✓ `PERM_FIN_ACCOUNT_LEDGER_VIEW` (API-FIN-028) | — | — | — | — |
| FIN_TRIAL_BALANCE | ENT-FIN-005 | ✓ `PERM_FIN_TRIAL_BALANCE_VIEW` (API-FIN-029) | — | — | — | — |
| FIN_BALANCE_SHEET | ENT-FIN-005 | ✓ `PERM_FIN_BALANCE_SHEET_VIEW` (API-FIN-030) | — | — | — | — |
| FIN_INCOME_STATEMENT | ENT-FIN-005 | ✓ `PERM_FIN_INCOME_STATEMENT_VIEW` (API-FIN-031) | — | — | — | — |
| FIN_DIMENSION_REPORTS | ENT-FIN-005, ENT-FIN-006 | ✓ `PERM_FIN_DIMENSION_REPORTS_VIEW` (API-FIN-032) | — | — | — | — |
| FIN_ACCOUNT_MAPPINGS (v2) | ENT-FIN-015 | ✓ `PERM_FIN_ACCOUNT_MAPPINGS_VIEW` (API-FIN-038) | ✓ `PERM_FIN_ACCOUNT_MAPPINGS_CREATE` (API-FIN-039) | ✓ `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE` (API-FIN-040, change the account; API-FIN-041, deactivate) | — | — |

**Every API verifies its permission before processing.** Each service method carries `@PreAuthorize` naming the
cell's permission from `PermissionConstants`. Services hold no hard-coded role check. API-FIN-020 is a system call:
the event consumer's service principal must hold `PERM_FIN_JOURNAL_ENTRIES_CREATE`; how a machine caller
authenticates is SEC's separate change set (CS-FIN-001, out of scope).

**v2 — FIN_ACCOUNT_MAPPINGS.** One new SEC_PAGES row (page code FIN_ACCOUNT_MAPPINGS, parent FIN, name ar
"روابط الحسابات" / en "Account mappings") and three action rows, VIEW (the gateway), CREATE and UPDATE, following
`PERM_<PAGE_CODE>_<ACTION>`. `PermissionConstants` gains the three constants. Deactivate is modelled under UPDATE,
exactly as FIN_ACCOUNTS models API-FIN-004; there is no DELETE action for this page. They are delivered by a new
forward FIN migration — the next free Flyway version after V30 — that registers the page and its three actions AND
grants all three explicitly to SYS_ADMIN, following V28. The explicit grant is required: V25 granted SYS_ADMIN its FIN
actions with a `SELECT` over the action registry that has already run everywhere, so Flyway will never re-evaluate
it for a row inserted later. Registering without granting would leave every mapping endpoint answering
FIN-403-FORBIDDEN to every caller, the administrator included. The SRS role "finance administrator" is assigned
these permissions through SEC's role screens; it is not a FIN seed. `FIN_CLOSE_APPROVER` (V27) receives nothing new.

**v2 — FIN_RULES.** Dimension tags travel inside the add-rule-line submission and add no action (ADR-FIN-025):
API-FIN-011 keeps `PERM_FIN_RULES_UPDATE`, so no new constant, no new row and no migration.

**The DELETE column is empty by design.** Deactivation is `PUT /{id}/deactivate` under the screen's UPDATE
permission everywhere — API-FIN-004, 034, 035, 036, 037 and, in v2, API-FIN-041 — and no FIN screen has a
registered delete permission (V24, V28 and the v2 migration seed none). No deactivate has an `activate`
counterpart, following the delivered `AccountService.deactivate` precedent.

**FIN_DIMENSIONS / UPDATE — the one cell V24 did not seed** (as built). `PERM_FIN_DIMENSIONS_UPDATE` is registered by
`V28__fin_dimensions_update_action.sql`, which also grants it explicitly to SYS_ADMIN, for the V25 reason above. The
VIEW gateway on the same screen was already registered and granted by V24/V25.

**FIN_RULES, FIN_RECURRING_TEMPLATES and FIN_ALLOCATION_RULES / UPDATE — one permission, two endpoints each** (as
built). API-FIN-034, API-FIN-036 and API-FIN-037 reuse `PERM_FIN_RULES_UPDATE`,
`PERM_FIN_RECURRING_TEMPLATES_UPDATE` and `PERM_FIN_ALLOCATION_RULES_UPDATE`, seeded by V24 and granted by V25's
Tier-3 statement, so none needed a new constant, error code or migration.

**Deactivating a template or an allocation rule stops its run — closed 2026-09-12, by decision, not by rule.**
`RecurringTemplateDomain.assertCanRun()` (API-FIN-014) and `AllocationRuleDomain.assertCanRun()` (API-FIN-017) refuse
a deactivated definition with FIN-409-NOT-ACTIVE. No RULE-FIN-* states this gate; it rests on a recorded human
decision and must not be cited as pre-existing spec. What stays open on those two screens is only that neither can be
updated after creation (SRS SCR-REQ-FIN-004 §B4, SCR-REQ-FIN-005 §B4).

**FIN_PERIODS / VIEW — a gateway that also has an endpoint** (as built). `PERM_FIN_PERIODS_VIEW` is load-bearing:
`MenuService.effectiveAuthorityCodes()` keeps a granted permission only if the same screen also carries a granted
VIEW, so V27's `FIN_CLOSE_APPROVER` must hold it for `PERM_FIN_PERIODS_CLOSE_APPROVE` to reach the caller. Since
API-FIN-033 it also gates the fiscal-period search; the constant was added with that endpoint, and no migration was
needed.

**SoD enforcement (RULE-FIN-015, POL-FIN-016) — as rewritten 2026-09-12, unchanged by v2.** The requirement, read at
source: the period-close approval is gated by a permission distinct from journal-entry creation, enforced through the
Security module, and the rule's `Data source` is DEFERRED — FIN has no permission entity to read. The delivered
`@PreAuthorize(PermissionConstants.PERM_FIN_PERIODS_CLOSE_APPROVE)` on `FiscalPeriodService.hardClose` (API-FIN-026)
and `FiscalYearService.yearEndClose` (API-FIN-027) satisfies it in full: it is a different code from
`PERM_FIN_JOURNAL_ENTRIES_CREATE`, and SEC's mechanism enforces it (`JwtAuthenticationFilter` builds the caller's
authorities from `MenuService`, which reads the grant table). Keeping the two permissions on different roles remains
sound administration, and `FIN_CLOSE_APPROVER` (V27) is how a deployment expresses it — a guideline, not a check in FIN
code.

**What this section used to specify, and must not specify again.** Earlier revisions said FIN's service layer also
checks at close time that no single user holds both permissions. That global user-set disjointness rule is stronger
than anything the SRS states. It was implemented as `FinSeparationOfDutiesService` reading SEC's user directory and
`FiscalPeriodDomain.assertCanHardClose` throwing `FIN-403-SOD-VIOLATION` whenever any user in the system held both —
refusing the close for every caller. Both were deleted on 2026-09-12 by recorded human decision. **No FIN service
reads SEC's user directory, and none may be specified here.** XM-FIN-002 is retired and FIN-403-SOD-VIOLATION is
struck as unreachable in the Error Catalog.

**Who can close, as seeded.** V25 had withheld `PERM_FIN_PERIODS_CLOSE_APPROVE` from SYS_ADMIN only to avoid tripping
the removed check; `V30__fin_sys_admin_close_approve_grant.sql` grants it, so the bootstrap `admin` user can hard-close
a period and run a year-end close. V27's `FIN_CLOSE_APPROVER` role (the `PERM_FIN_PERIODS_VIEW` gateway plus
`PERM_FIN_PERIODS_CLOSE_APPROVE`, and by construction no entry creation) stays valid as the least-privilege option.
V27's header text and V25's describe the removed mechanism as live; both are applied and immutable, so this paragraph
is the correction: there is no `resolveFacts()`, no `assertCanHardClose` and no thrower of `FIN-403-SOD-VIOLATION`
anywhere.

**Gateway**: every non-VIEW permission requires VIEW on the same screen first — SEC's own interceptor convention, not
a FIN-owned rule.

**Forbidden responses**: a `@PreAuthorize` denial on any `com.erp.fin.service.*` method is caught by
`com.erp.fin.security.FinForbiddenAdvisor` (one `DefaultPointcutAdvisor` at `Ordered.HIGHEST_PRECEDENCE`, never a
feature `@ControllerAdvice`) and re-raised as `LocalizedException(Status.FORBIDDEN, FinErrorCodes.FIN_403_FORBIDDEN)`,
so FIN-403-FORBIDDEN reaches the wire with both ar and en messages. Two boundaries hold, both read in source: a
denial raised by the Spring Security filter chain before any FIN service is entered still answers the platform's own
forbidden code — unreachable for FIN today, because the platform `SecurityConfig` authorizes with
`.anyRequest().authenticated()` and declares no FIN URL rule; and FIN-403-SOD-VIOLATION never was an access denial
and now has no throw site. The v2 mapping endpoints inherit this unchanged.

### BOOTSTRAP DATA — FIN v2
Every row a fresh deployment needs before a FIN endpoint can succeed, each with who produces it. Lookup tables belong
to MDL, yet the keys are FIN's, so FIN seeds them. Registering a permission is not a grant: every permission names the
role it is granted to (`profile.conventions.security_model.grant_target` = role).

```
LOOKUP KEY  ACCOUNT_TYPE               │ seed source: FIN onboarding into MDL (REQ-FIN-045, delivered in v1) — 5 values
LOOKUP KEY  DEBIT_CREDIT               │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 2 values
LOOKUP KEY  PERIOD_STATE               │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 4 values
LOOKUP KEY  FISCAL_YEAR_STATUS         │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 2 values
LOOKUP KEY  JOURNAL_TYPE               │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 5 values, plus CLOSING and OPENING added as data at v1 ALIGN-BE
LOOKUP KEY  JOURNAL_STATUS             │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 3 values
LOOKUP KEY  ACCOUNTING_EVENT_TYPE      │ seed source: the type by FIN onboarding (v1); its values are host data, added in MDL by the deploying organization before its first rule or mapping (SRS A6 — none seeded)
LOOKUP KEY  PAYMENT_METHOD             │ seed source: the type by FIN onboarding (v1); CHEQUE by db-script v2 BLOCK 8 (idempotent SQL into MDL's tables, ADR-FIN-028); every other method is host data
LOOKUP KEY  ACCOUNT_DERIVATION_TYPE    │ seed source: the type, CONSTANT and MAPPING by FIN onboarding (v1); DIRECT deactivated by db-script v2 BLOCK 8
LOOKUP KEY  AMOUNT_SOURCE_TYPE         │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 3 values
LOOKUP KEY  DISTRIBUTION_TYPE          │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 3 values
LOOKUP KEY  RECURRING_SCHEDULE_TYPE    │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 2 values
LOOKUP KEY  RECURRING_FREQUENCY        │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 4 values
LOOKUP KEY  FIN_EVENT_BUSINESS_FIELD   │ seed source: db-script v2 BLOCK 8 — the type and 4 values (ADR-FIN-013)
LOOKUP KEY  FIN_DIMENSION_VALUE_SOURCE │ seed source: db-script v2 BLOCK 8 — the type and 2 values (ADR-FIN-010)
PERMISSION  PERM_FIN_ACCOUNTS_VIEW                │ grant target: role SYS_ADMIN — registered by V24, granted by V25
PERMISSION  PERM_FIN_ACCOUNTS_CREATE              │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_ACCOUNTS_UPDATE              │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_DIMENSIONS_VIEW              │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_DIMENSIONS_CREATE            │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_DIMENSIONS_UPDATE            │ grant target: role SYS_ADMIN — registered and granted by V28
PERMISSION  PERM_FIN_RULES_VIEW                   │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_RULES_CREATE                 │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_RULES_UPDATE                 │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_RECURRING_TEMPLATES_VIEW     │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_RECURRING_TEMPLATES_CREATE   │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_RECURRING_TEMPLATES_UPDATE   │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_ALLOCATION_RULES_VIEW        │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_ALLOCATION_RULES_CREATE      │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_ALLOCATION_RULES_UPDATE      │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_JOURNAL_ENTRIES_VIEW         │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_JOURNAL_ENTRIES_CREATE       │ grant target: role SYS_ADMIN — V24, V25; the event consumer's service principal needs a role holding it (SEC change set)
PERMISSION  PERM_FIN_JOURNAL_ENTRIES_REVERSE      │ grant target: role SYS_ADMIN — V24 (custom action), V25
PERMISSION  PERM_FIN_PERIODS_VIEW                 │ grant target: roles SYS_ADMIN (V25) and FIN_CLOSE_APPROVER (V27) — V24 registers it
PERMISSION  PERM_FIN_PERIODS_CREATE               │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_PERIODS_UPDATE               │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_PERIODS_CLOSE_APPROVE        │ grant target: roles SYS_ADMIN (V30) and FIN_CLOSE_APPROVER (V27) — V24 registers it
PERMISSION  PERM_FIN_ACCOUNT_LEDGER_VIEW          │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_TRIAL_BALANCE_VIEW           │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_BALANCE_SHEET_VIEW           │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_INCOME_STATEMENT_VIEW        │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_DIMENSION_REPORTS_VIEW       │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_ACCOUNT_MAPPINGS_VIEW        │ grant target: role SYS_ADMIN — registered and granted by the v2 FIN security migration (after V30)
PERMISSION  PERM_FIN_ACCOUNT_MAPPINGS_CREATE      │ grant target: role SYS_ADMIN — the v2 FIN security migration
PERMISSION  PERM_FIN_ACCOUNT_MAPPINGS_UPDATE      │ grant target: role SYS_ADMIN — the v2 FIN security migration
REQUIRED    FIN_ACCOUNT.is_retained_earnings_fl  │ seed source: marked on exactly one account as data by the deploying organization before the first year-end close (API-FIN-027 answers FIN-404-ACCOUNT otherwise); never written by an API
DATA        FIN_ACCOUNT_MAPPING rows            │ seed source: the migration-data load of CS-FIN-001, produced once the store exists (out of scope); until then every MAPPING line answers FIN-422-UNMAPPED-VALUE
```
<!-- PHASE:SEC-BE:END -->

<!-- PHASE:ALIGN-BE:START traces=REQ-FIN-017,REQ-FIN-076 -->
## PHASE 8 — ALIGN-BE

The self-check of this plan. Each row names the `gov.py analyze` check that backs it, and its mark is that check's
result in the stage report (`_state/analyze-stage-P3.1.md`), copied rather than judged. A clause that examined nothing
is written as such, never ✓. The RESULT line is written by the orchestrator.

The one ✗: C7.12 flags the id of SEC's User entity, cited in the retired XM-FIN-002 block of INT-C that
`gov.py state` carries unchanged from v1. The citation is correct history. It fails to resolve because
SEC's current version (v2) has no artifacts yet, so SEC publishes no id at all to resolve it against.

```
ALIGN — FIN v2
row               backing check        assertion                                                                  mark
TRACEABILITY      traces               every PHASE/SUB/atom block carries traces=, and every API traces to its     ✓ (C7.2: 57 blocks · C7.3: 41 API)
                                       REQ and its DBF
COVERED           orphans              every REQ is covered by ≥1 API or DBF                                      ✓ (C7.6: 77 REQ)
BINDING (§2A)     value-agreement      every DBF names the same physical column here as the db-script declares   ✓ (C7.10)
MANIFEST (§4)     count-agrees         every total this plan states equals the rows it heads                      ✓ (C7.18: 4 totals)
WRITERS           required-writer      every required column is written by an endpoint, or the row states why    ✓ (C7.19: 82 columns)
QRC (§5)          orphans              every catalogued query is reached by ≥1 API                                ✓ (C7.22: 61 QR)
API (R3)          code-format          every catalog code is an instance of the declared format and carries a    ✓ (C7.11)
                                       status the platform can emit
RULE INPUTS       data-source          every RULE enforced at runtime names where the data it READS comes from,  ✓ (C6.9)
                                       or is deferred
CROSS-MODULE      registry-agree       every registered XM is placed here, and every XM minted here is           ✓ (C7.5, C7.5b: 2 XM)
                                       back-registered
FOREIGN IDS       xref-resolve         every id of another module cited here is defined in that module's own     ✗ (C7.12: 1)
                                       registry
BOOTSTRAP DATA    bootstrap-complete   every lookup key and every permission has a row naming who produces it    ✓ (C7.21: 45 names)
SECURITY (R7)     operation-resolves   every declared entity operation resolves to an API, and every marked      ✓ (C7.20: 65)
                                       matrix cell names its API and its permission
DEMAND (SRS)      operation-resolves   every operation an SRS screen names is built by an API, or the plan       ✓ (C7.23: 30)
                                       states why it is not
DECISIONS         refs-exist           every ADR this plan cites exists on disk in analysis/decisions/FIN/        ✓ (C7.13)
PATHS             paths-resolve        every path the generated manifest and execution state emit resolves to    ✓ (C7.14)
                                       something that exists
COVERAGE          (the report)         clauses the report lists as having examined nothing: none
RESULT            PASSED ✓ — 0 findings
```

**Coverage — ENT/DBF → phases → QR → XM.** Every ENT-FIN-001 … ENT-FIN-016 has exactly one DATA-DOM block and at least
one SVC-API endpoint; all 165 DBF ids have one manifest row; XM-FIN-001 binds the 22 lookup-backed columns.
| ENT | DBF | Endpoints | Queries | XM |
|---|---|---|---|---|
| ENT-FIN-001 | DBF-FIN-001..013, DBF-FIN-147 | API-FIN-001 … 004 | QR-FIN-001 … 006, QR-FIN-045, QR-FIN-060 | XM-FIN-001 |
| ENT-FIN-002 | DBF-FIN-014..022 | API-FIN-005, 006 | QR-FIN-007, QR-FIN-008, QR-FIN-046, QR-FIN-061 | — |
| ENT-FIN-003 | DBF-FIN-023..033 | API-FIN-007, 008, 035 | QR-FIN-009 … 011, QR-FIN-058, QR-FIN-061 | — |
| ENT-FIN-004 | DBF-FIN-034..050 | API-FIN-018 … 022 | QR-FIN-023 … 037 | XM-FIN-001 |
| ENT-FIN-005 | DBF-FIN-051..060 | API-FIN-019, 020, 022, 028 … 032 | QR-FIN-024, 025, 028 … 030, 034, 037, 042 … 044 | XM-FIN-001 |
| ENT-FIN-006 | DBF-FIN-061..064 | API-FIN-019, 020, 022, 032 | QR-FIN-024, 025, 032, 034, 037, 044 | — |
| ENT-FIN-007 | DBF-FIN-065..074 | API-FIN-023, 027 | QR-FIN-038, 041, 047, 048 | XM-FIN-001 |
| ENT-FIN-008 | DBF-FIN-075..088 | API-FIN-023 … 026, 033 | QR-FIN-038 … 040, 049, QR-FIN-059 | XM-FIN-001 |
| ENT-FIN-009 | DBF-FIN-089..097 | API-FIN-009, 010, 034 | QR-FIN-012 … 014, 027 | XM-FIN-001 |
| ENT-FIN-010 | DBF-FIN-098..108, DBF-FIN-148 | API-FIN-011 | QR-FIN-015, 016 | XM-FIN-001 |
| ENT-FIN-011 | DBF-FIN-109..121 | API-FIN-012 … 014, 036 | QR-FIN-017 … 019 | XM-FIN-001 |
| ENT-FIN-012 | DBF-FIN-122..129 | API-FIN-012, 013 | QR-FIN-018, 019 | XM-FIN-001 |
| ENT-FIN-013 | DBF-FIN-130..138 | API-FIN-015 … 017, 037 | QR-FIN-020 … 022 | — |
| ENT-FIN-014 | DBF-FIN-139..146 | API-FIN-015, 016 | QR-FIN-016, 021, 022 | XM-FIN-001 |
| ENT-FIN-015 | DBF-FIN-149..158 | API-FIN-038 … 041; read by API-FIN-020 | QR-FIN-050 … 055 | XM-FIN-001 |
| ENT-FIN-016 | DBF-FIN-159..165 | API-FIN-011; read by API-FIN-020 | QR-FIN-056, 057, 061 | XM-FIN-001 |

**Coverage — RULE → API → catalog code**: the Rule Registry of the Plan Index carries it, one row per RULE-FIN-001 …
RULE-FIN-028; every rule that raises a user-facing rejection has exactly one Error Catalog row per code.

**Coverage — XM → status → blocks → workaround**: XM-FIN-001 → ACTIVE → blocks nothing → no workaround; XM-FIN-002 →
RETIRED → blocks nothing → not applicable.
<!-- PHASE:ALIGN-BE:END -->

## Error Catalog — FIN v2

Envelope: `LocalizedException → {code, messageAr, messageEn}`. Runtime code format: `FIN-{http}[-{SLUG}]`. The one
location of every code; downstream stages cite the code and never restate the text. v2 edits nine v1 rows (each says
what changed), strikes FIN-422-MAPPING-UNSUPPORTED and adds twelve rows at the end.

| code | RULE / PLATFORM-STD | API | HTTP | trigger | message-AR | message-EN |
|---|---|---|---|---|---|---|
| FIN-409-ACCOUNT-DUP | PLATFORM-STD | API-FIN-002 | 409 | duplicate account code | رمز الحساب مستخدم بالفعل | Account code already in use |
| FIN-409-PARENT-NOT-LEAF-ELIGIBLE | RULE-FIN-001 | API-FIN-002 | 409 | parent account cannot remain a leaf | لا يمكن لحساب له حسابات فرعية أن يقبل ترحيلاً مباشرًا | An account with sub-accounts cannot accept direct posting |
| FIN-400-INVALID-LOOKUP | PLATFORM-STD (XM-FIN-001) | many | 400 | submitted code not found in MDL | القيمة المُدخلة غير صالحة | The submitted value is not valid |
| FIN-409-HAS-CHILDREN | RULE-FIN-001 | API-FIN-003 | 409 | marking a parent as leaf | لا يمكن لحساب له حسابات فرعية أن يقبل ترحيلاً مباشرًا | An account with sub-accounts cannot accept direct posting |
| FIN-404-ACCOUNT | PLATFORM-STD | API-FIN-002, 003, 004, 013, 016, 019, 020, 027, 028, 039, 040 | 404 | unknown account id — every path that resolves an account FK raises it: API-FIN-002's `parentAccountId` (AccountService.java:168, reached only from `create`), a template line's `accountId` (API-FIN-013, RecurringTemplateService.java:304), an allocation rule's source and target accounts (API-FIN-016, AllocationRuleService.java:278), a manual entry line's `accountId` (API-FIN-019, JournalEntryService.java:349), the account code a CONSTANT rule line names (API-FIN-020, EventEntryService.java:193) and API-FIN-027's Retained Earnings account, where the reachable failure is "none marked" rather than an unknown id (FiscalYearService.java:347). v2: the `accountId` of an account mapping on create and on update (API-FIN-039, API-FIN-040, QR-FIN-060) | الحساب غير موجود | Account not found |
| FIN-409-DIMENSION-DUP | PLATFORM-STD | API-FIN-006 | 409 | duplicate dimension code | رمز البُعد مستخدم بالفعل | Dimension code already in use |
| FIN-409-DIMVALUE-DUP | RULE-FIN-002 | API-FIN-007 | 409 | duplicate code within dimension | هذا الرمز مستخدم بالفعل ضمن هذا البُعد | This code is already used within this dimension |
| FIN-404-DIMENSION | PLATFORM-STD | API-FIN-007, 008, 011, 032 | 404 | unknown dimension id — v2: also a dimension tag's `dimensionId` on API-FIN-011 (QR-FIN-061) | البُعد غير موجود | Dimension not found |
| FIN-404-DIMVALUE | PLATFORM-STD | API-FIN-011, 035 | 404 | unknown dimension-value id — v2: also a CONSTANT tag's `dimensionValueId` on API-FIN-011 (QR-FIN-061) | قيمة البُعد غير موجودة | Dimension value not found |
| FIN-409-RULE-DUP | PLATFORM-STD (§6.4) | API-FIN-010 | 409 | event type already has an active rule | يوجد بالفعل قاعدة نشطة لهذا النوع | An active rule already exists for this event type |
| FIN-409-REMAINDER-COUNT | RULE-FIN-003 | API-FIN-011, 016, 017, 020 | 409 | wrong remainder-line/target count — both guards are re-run on the consuming path, not only at create: the rule-line set is re-validated when an event entry is built (API-FIN-020, EventEntryService.java:104 → EventTypeRuleDomain.java:177) and the target set when an allocation rule is run (API-FIN-017, AllocationRuleService.java:179 → AllocationRuleDomain.java:122) | يلزم تحديد سطر باقٍ واحد بالضبط عند وجود توزيع نسبي | Exactly one remainder line is required when any percentage distribution is present |
| FIN-404-RULE | PLATFORM-STD | API-FIN-011, 034 | 404 | unknown event-type rule id | القاعدة غير موجودة | Rule not found |
| FIN-400-MISSING-FREQUENCY | PLATFORM-STD | API-FIN-013 | 400 | recurring template with no frequency | يلزم تحديد التكرار للقالب المتكرر | A frequency is required for a recurring template |
| FIN-404-TEMPLATE | PLATFORM-STD | API-FIN-014, 036 | 404 | unknown recurring template id — raised by `RecurringTemplateService.run` (API-FIN-014) and, since the template deactivate landed, by `RecurringTemplateService.deactivate` (API-FIN-036) resolving the same path id. API-FIN-036 raises it ONLY for an id that does not exist; deactivating an already-inactive template is not an error, and there is deliberately no "exists but is inactive" code — see the API-FIN-036 block in SVC-API-CRUD | القالب المتكرر غير موجود | Recurring template not found |
| FIN-404-ALLOCATION-RULE | PLATFORM-STD | API-FIN-017, 037 | 404 | unknown allocation rule id — raised by `AllocationRuleService.run` (API-FIN-017) and, since the rule deactivate landed, by `AllocationRuleService.deactivate` (API-FIN-037) resolving the same path id. API-FIN-037 raises it ONLY for an id that does not exist; deactivating an already-inactive rule is not an error, and there is deliberately no "exists but is inactive" code — see the API-FIN-037 block in SVC-API-CRUD | قاعدة التوزيع غير موجودة | Allocation rule not found |
| FIN-409-NOT-ACTIVE | (no RULE — recorded human decision, 2026-09-12) | API-FIN-014, 017 | 409 | running a DEACTIVATED recurring template (API-FIN-014) or a deactivated allocation rule (API-FIN-017). Thrown by `RecurringTemplateDomain.assertCanRun()` (a NEW Domain companion for ENT-FIN-011) and `AllocationRuleDomain.assertCanRun()`, each the first thing its service's `run` does after loading the row and before any period, line or target is resolved, so a retired definition posts nothing at all. `Status.CONFLICT` → HTTP 409, consistent with every other FIN refusal whose cause is the target row's own state (FIN-409-NOT-POSTED, FIN-409-NOT-REOPENABLE, FIN-409-PERIOD-NOT-OPEN, FIN-409-INVALID-TRANSITION); the screen's own 404 was NOT reused because the row does exist. **NO RULE-FIN-* STATES THIS GATE.** It closes the "deactivate does not stop the run" OPEN DEFECT by decision, not by implementing a requirement that was always there — do not cite it as pre-existing spec | هذا التعريف غير نشط ولا يمكن تشغيله | This definition is deactivated and cannot be run |
| FIN-409-UNBALANCED | RULE-FIN-006 | API-FIN-019, 020, 014, 017, 021, 027 | 409 | debits ≠ credits — checked once in JournalEntryService.createManual (API-FIN-019) and once in JournalPostingService.buildValidateAndPost (JournalPostingService.java:163), which is the single posting pipeline for API-FIN-014, 017, 020 AND for API-FIN-021's reversal and API-FIN-027's closing/opening pair; the last two were missing from this row | القيد غير متوازن — إجمالي المدين لا يساوي إجمالي الدائن | The entry is unbalanced — total debits do not equal total credits |
| FIN-409-NOT-POSTABLE-ACCOUNT | RULE-FIN-007 | API-FIN-019, 020, 014, 017, 021, 027 | 409 | non-leaf/inactive account — AccountDomain.checkPostable runs per line in JournalEntryService.createManual (API-FIN-019, :350) and in JournalPostingService.buildValidateAndPost (:141), so it also covers API-FIN-021's reversal and API-FIN-027's closing/opening pair. v2: on API-FIN-020 it applies unchanged to an account taken from an account mapping — a mapped account that has since gained a child or been deactivated rejects the event (REQ-FIN-052, AC-FIN-054, AC-FIN-055) | الحساب المستهدف لا يقبل ترحيلاً مباشرًا | The target account does not accept direct posting |
| FIN-409-PERIOD-NOT-OPEN | RULE-FIN-008 | API-FIN-019, 020, 014, 017, 021 | 409 | period not Open at post — API-FIN-021 raises it twice over: from the shared gate in JournalPostingService.buildValidateAndPost (:164) and, before that, when the original's period is closed and the ledger has no open period to receive the reversal (JournalEntryDomain.java:329). API-FIN-027 is deliberately NOT listed: RULE-FIN-008 exempts the year-end CLOSING/OPENING types (JournalEntryDomain.checkTargetPeriodOpenForGeneratedEntry, :236). v2 (REQ-FIN-073, REQ-FIN-075): on API-FIN-020 this is the answer for every EXISTING period whose state is not OPEN, distinct from every validation and configuration code, and it is the one rejection that clears once the target period is opened — published to the event consumer as its wait-and-retry signal (DOC phase). A date no period covers is FIN-422-NO-DEFINED-PERIOD, never this code | الفترة المستهدفة غير مفتوحة | The target period is not open |
| FIN-409-INVALID-DIMENSION | RULE-FIN-009 | API-FIN-013, 014, 016, 017, 019, 020, 021, 027 | 409 | invalid/inactive dimension value — RULE-FIN-009 (DimensionValueDomain.checkUsableOnLine) runs in JournalEntryService.createManual (API-FIN-019, :376) and in JournalPostingService.buildValidateAndPost (:144), so it also covers API-FIN-021 and API-FIN-027. API-FIN-013 and API-FIN-016 raise the SAME code for a different reason — plain FK resolution of a submitted `dimensionValueId` that does not exist (RecurringTemplateService.java:318, AllocationRuleService.java:291) — which is why they belong in this row even though no rule is evaluated there. v2: on API-FIN-020 it also covers a value resolved from the event or taken from a CONSTANT tag that is inactive or not of the tagged dimension (REQ-FIN-066, REQ-FIN-067, AC-FIN-072) | قيمة البُعد غير صالحة | The dimension value is invalid |
| FIN-409-DUPLICATE-EVENT | RULE-FIN-004 | API-FIN-020 | 409 | repeated eventReference — MODIFIED v2: raised by the pre-check (QR-FIN-026) AND by the store's refusal on UQ_FIN_JOURNAL_ENTRY_EVENT_REF when two events with one reference arrive together, translated by FinPersistenceRefusalAdvisor (ADR-FIN-030); a duplicate is never answered as a generic data-integrity or server error (REQ-FIN-072, AC-FIN-077, AC-FIN-078). Published to the event consumer as "already posted — acknowledge" (DOC phase) | تم بالفعل ترحيل قيد لهذا المرجع | An entry for this event reference has already been posted |
| FIN-404-NO-ACTIVE-RULE | RULE-FIN-005 | API-FIN-020 | 404 | no active rule for event type | لا توجد قاعدة نشطة لهذا النوع من الأحداث | No active rule exists for this event type |
| ~~FIN-422-MAPPING-UNSUPPORTED~~ STRUCK | — (retired v2) | — | — | RETIRED in v2 — MAPPING derivation is implemented (REQ-FIN-050, API-FIN-020) and DIRECT is removed (ADR-FIN-010), so the cause this code named no longer exists (ADR-FIN-027, ADR-FIN-030). Struck rather than deleted, following this table's precedent for unreachable codes. The constant `FinErrorCodes.FIN_422_MAPPING_UNSUPPORTED` and its two bundle entries are to be removed with the MAPPING implementation; no v2 path raises it | — | — |
| FIN-409-NOT-POSTED | RULE-FIN-013 | API-FIN-021, 014 | 409 | reversing a non-posted entry — raised by the same JournalEntryDomain.assertCanReverse guard as FIN-409-ALREADY-REVERSED (JournalEntryDomain.java:284), so a REVERSING template run reaches it too (API-FIN-014, RecurringTemplateService.java:249) | لا يمكن عكس قيد غير مُرحَّل | A non-posted entry cannot be reversed |
| FIN-409-ALREADY-REVERSED | RULE-FIN-013 | API-FIN-021, 014 | 409 | reversing an entry that already carries a reversal link | تم عكس هذا القيد بالفعل ولا يمكن عكسه مرة أخرى | This entry has already been reversed and cannot be reversed again |
| FIN-404-ENTRY | PLATFORM-STD | API-FIN-021, 022 | 404 | unknown entry id | القيد غير موجود | Entry not found |
| FIN-409-YEAR-DUP | PLATFORM-STD | API-FIN-023 | 409 | duplicate fiscal year code | رمز السنة المالية مستخدم بالفعل | Fiscal year code already in use |
| FIN-409-NOT-REOPENABLE | RULE-FIN-014 | API-FIN-024, 026 | 409 | period Hard Closed | الفترة مغلقة إغلاقًا صارمًا ولا يمكن إعادة فتحها | The period is hard-closed and cannot be reopened |
| FIN-409-INVALID-TRANSITION | PLATFORM-STD | API-FIN-025, 027 | 409 | period not in the expected state; fiscal year already CLOSED when year-end close is re-run | لا يمكن تنفيذ هذا الانتقال من الحالة الحالية | This transition is not allowed from the current status |
| ~~FIN-403-SOD-VIOLATION~~ STRUCK | RULE-FIN-015 | — | — | UNREACHABLE since 2026-09-12 — both throw sites are gone. It was raised when the close-approver's permission set was not globally disjoint from the entry-creation permission's: `FinSeparationOfDutiesService` read SEC's user directory (XM-FIN-002, now retired) and `FiscalPeriodDomain.assertCanHardClose(...)` threw. Both were DELETED by recorded human decision as an OVER-IMPLEMENTATION: they refused the close for EVERY caller whenever ANY single user in the system held both `PERM_FIN_PERIODS_CLOSE_APPROVE` and `PERM_FIN_JOURNAL_ENTRIES_CREATE`, and no REQ, AC or RULE states that. RULE-FIN-015 (srs-fin.md:1026-1033) asks only for a DISTINCT permission "enforced through the Security module" and its `Data source` line reads DEFERRED — no FIN-side fact to read — so the rule is satisfied in full by the delivered `@PreAuthorize(PERM_FIN_PERIODS_CLOSE_APPROVE)` gate on API-FIN-026 and API-FIN-027, which is untouched. STRUCK RATHER THAN DELETED, following the never-declared-service-unavailable row's precedent in this same table (an unreachable code is struck, kept visible, and given no API/HTTP cells). ONE HONEST DIFFERENCE FROM THAT PRECEDENT: that other row's code never existed in code at all, whereas the constant `FinErrorCodes.FIN_403_SOD_VIOLATION` and BOTH bundle entries (`messages.properties`, `messages_ar.properties`) were deliberately left in place by the code session, so the string is still in the build with no thrower — the message columns below are therefore the real, still-present bundle text, not a historical quotation. A session that removes the constant and the two bundle keys should delete this row's message cells with them | صلاحية اعتماد الإغلاق منفصلة عن صلاحية إنشاء القيود | The close-approval permission is separate from the entry-creation permission |
| FIN-404-PERIOD | PLATFORM-STD | API-FIN-014, 017, 019, 024, 025, 026, 027, 029, 031 | 404 | unknown period id — on API-FIN-029 only when the OPTIONAL `periodId` is actually supplied (omitting it stays a 200); API-FIN-031's `fromPeriodId`/`toPeriodId` raise it when supplied and unknown. The sites were verified in source and are NOT all "unknown id" failures: API-FIN-024/025/026 resolve the path id (FiscalPeriodService.java:204); API-FIN-019 resolves the submitted `periodId` (JournalEntryService.java:174); API-FIN-014 and 017 carry no period id and raise it when NO period's `[startDate, endDate]` covers the run date (JournalPostingService.java:208); API-FIN-014 also raises it when a REVERSING template's period has no successor inside the same fiscal year (RecurringTemplateService.java:284); API-FIN-027 raises it when the closing year has no last period or the successor year no first period (FiscalYearService.java:314, :322). v2: API-FIN-020 no longer raises it — an event date no period covers is FIN-422-NO-DEFINED-PERIOD (RULE-FIN-028, ADR-FIN-030) | الفترة غير موجودة | Period not found |
| FIN-409-PERIODS-NOT-CLOSED | PLATFORM-STD (§10.4 precondition) | API-FIN-027 | 409 | not every period Hard Closed | يجب إغلاق كل الفترات إغلاقًا صارمًا أولًا | Every period must be hard-closed first |
| FIN-404-YEAR | PLATFORM-STD | API-FIN-014, 017, 019, 020, 021, 027, 030, 031 | 404 | unknown fiscal year id — on API-FIN-030 and API-FIN-031 the REQUIRED `fiscalYearId` is resolved first and raises this instead of the silent all-zero 200 those reports previously returned. The five ids added here were verified in source: API-FIN-019 resolves the submitted `fiscalYearId` under the docNo row lock (JournalEntryService.java:171), and every system-generated posting re-resolves the year under that same lock inside JournalPostingService.buildValidateAndPost (:152) — the single pipeline behind API-FIN-014, 017, 020, 021 and 027. API-FIN-027 raises it from two further sites of its own: an absent adjacent successor year (FiscalYearService.java:364) and an unknown path id (:371). API-FIN-023 does NOT raise it — FiscalYearService.create resolves no year | السنة المالية غير موجودة | Fiscal year not found |
| FIN-422-REMAINDER-MARKER | RULE-FIN-003, RULE-FIN-010 | API-FIN-011, 016, 017, 020 | 422 | `isRemainderFl` (DBF-FIN-107 on a rule line, DBF-FIN-146 on an allocation target — v1 cited DBF-FIN-103 and DBF-FIN-141, the amount-source and lineNo columns, corrected in v2) disagrees with the line's/target's own REMAINDER type code, so which line is the remainder is ambiguous | علامة سطر الباقي لا تتفق مع نوع التوزيع أو مصدر المبلغ لنفس السطر | The remainder marker disagrees with the line's own distribution or amount-source type |
| FIN-422-REMAINDER-NOT-POSITIVE | RULE-FIN-010 | API-FIN-017, 020 | 422 | the remainder line's per-side difference is zero or negative — the other lines on its side already equal or exceed the opposing side (POL-FIN-005) | سطر الباقي يُحسب كفرق ويجب أن يكون موجبًا؛ السطور الأخرى تستهلك المبلغ بالكامل | The remainder line is computed as a difference and must be positive; the other lines already consume the full amount |
| FIN-400-PERIOD-NOT-IN-YEAR | RULE-FIN-017 | API-FIN-019 | 400 | submitted `periodId` belongs to a different fiscal year than the submitted `fiscalYearId` (DBF-FIN-076) | الفترة المحددة لا تتبع السنة المالية المحددة | The selected period does not belong to the selected fiscal year |
| FIN-400-DOCDATE-OUTSIDE-PERIOD | RULE-FIN-017 | API-FIN-019 | 400 | submitted `docDate` falls outside the submitted period's `[startDate, endDate]` span (DBF-FIN-080/081) | تاريخ المستند خارج نطاق الفترة المحددة | The document date falls outside the selected period |
| FIN-403-FORBIDDEN | PLATFORM-STD | every secured API (i.e. every `@PreAuthorize` on a `com.erp.fin.service.*` method — API-FIN-036 and API-FIN-037 included) | 403 | missing module/screen/action grant — **this code now REACHES THE WIRE as itself.** `com.erp.fin.security.FinForbiddenAdvisor` catches the `AccessDeniedException` raised by `@PreAuthorize` on any `com.erp.fin.service.*` method and re-raises it as `LocalizedException(Status.FORBIDDEN, FinErrorCodes.FIN_403_FORBIDDEN)`, which `GlobalExceptionHandler` renders like every other FIN error. It IS a `FinErrorCodes` constant (`FIN_403_FORBIDDEN`) and it IS in BOTH i18n bundles (`messages.properties`, `messages_ar.properties`). **Every earlier revision of this row said the opposite** — that FIN's 403 was rendered as the platform `ACCESS_DENIED` envelope, that this row was merely the catalog's *name* for that response, that the code was in no `FinErrorCodes` constant and in neither bundle, and that FIN had no `FIN-403-FORBIDDEN` on the wire. All of that is superseded and none of it is true any more. TWO BOUNDARIES, each read in source: (1) a denial raised by the Spring Security **filter chain**, before any FIN service is entered, never reaches an AOP proxy or `GlobalExceptionHandler` — it is written by the chain's `accessDeniedHandler` and still answers `SEC-403-FORBIDDEN`. That path is currently UNREACHABLE for FIN, because `SecurityConfig` (`com.erp.main.config`) authorizes with `.anyRequest().authenticated()` and declares no FIN authority rule, so every FIN permission denial is a `@PreAuthorize` denial on a FIN service and does pass through the advisor; adding a URL-level authority rule for a FIN path would change that. (2) `FIN-403-SOD-VIOLATION` is unaffected — it is a RULE-FIN-015 business failure thrown by hand as a `LocalizedException`, never an `AccessDeniedException`, so the advisor never sees it. Also note `CommonErrorCodes.ACCESS_DENIED` still exists and is still what `GlobalExceptionHandler.handleAccessDenied` emits for modules that have no forbidden advisor of their own; FIN is simply no longer one of them | لا تملك صلاحية المالية المطلوبة لهذه العملية | You do not hold the Finance permission required for this operation |
| FIN-400-INVALID-SORT | PLATFORM-STD | every search API | 400 | unrecognized sort field | حقل الترتيب غير معروف | Unrecognized sort field |
| ~~FIN service-unavailable (never declared)~~ STRUCK | PLATFORM-STD (MDL unreachable) | — | — | this row's code was originally shaped `{MOD}-{http}` with http 503 — never a declared platform status (`stack.backend.api.http_statuses` has no entry for it) and never a `FinErrorCodes` constant. Struck rather than spelled out here, so the row's own label no longer reads as a live catalog code. Genuinely unreachable: XM-FIN-001 is in-process `MdlLookupApi` injection, so there is no network hop to fail; an invalid code raises `FIN-400-INVALID-LOOKUP`, anything else falls through to `FIN-500` | — | — |
| FIN-500 | PLATFORM-STD (infrastructure) | any | 500 | unhandled server error | حدث خطأ في الخادم | A server error occurred |
| FIN-422-UNSUPPORTED-DERIVATION | RULE-FIN-018 | API-FIN-011 | 422 | ADDED v2 — a rule line's accountDerivationTypeCode is not an active ACCOUNT_DERIVATION_TYPE value (DIRECT included, retired in MDL) — AC-FIN-048 | نوع اشتقاق الحساب غير مدعوم؛ المسموح قيمة ثابتة أو ربط | Unsupported account derivation type; only constant or mapping is allowed |
| FIN-422-DERIVATION-SPEC | RULE-FIN-019 | API-FIN-011 | 422 | ADDED v2 — a CONSTANT line without an account code or with a business field, or a MAPPING line without a business field or with an account code | مواصفة اشتقاق الحساب لا تطابق نوعه | The account derivation spec does not match its type |
| FIN-422-UNMAPPED-VALUE | RULE-FIN-020 | API-FIN-020 | 422 | ADDED v2 — no ACTIVE account mapping for the rule's event type, the line's business field and the event's value; a configuration gap fixed by adding a mapping (AC-FIN-056, AC-FIN-057, AC-FIN-061) | لا يوجد ربط حساب نشط للقيمة التجارية في الحدث | No active account mapping exists for the business value in the event |
| FIN-422-MISSING-BUSINESS-FIELD | RULE-FIN-021 | API-FIN-020 | 422 | ADDED v2 — the event does not carry the business field a MAPPING line or a BUSINESS_FIELD tag names; a payload gap fixed at the producer (AC-FIN-053, AC-FIN-073) | الحدث لا يحمل الحقل التجاري الذي تطلبه القاعدة | The event does not carry the business field the rule requires |
| FIN-409-MAPPING-DUP | RULE-FIN-022 | API-FIN-039 | 409 | ADDED v2 — an active mapping already holds the (event type, business field, business value) key: by the pre-check QR-FIN-052, or by the store's refusal on UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY translated by FinPersistenceRefusalAdvisor (ADR-FIN-030) — AC-FIN-063 | يوجد بالفعل ربط نشط لنوع الحدث والحقل التجاري والقيمة نفسها | An active mapping already exists for this event type, business field and value |
| FIN-422-MAPPED-ACCOUNT-INELIGIBLE | RULE-FIN-023 | API-FIN-039, 040 | 422 | ADDED v2 — the mapping's account is not a leaf or is inactive when the mapping is saved (AC-FIN-064) | الحساب المربوط يجب أن يكون حسابًا ورقيًا نشطًا | The mapped account must be an active leaf account |
| FIN-422-INVALID-PAYMENT-METHOD | RULE-FIN-024 | API-FIN-039 | 422 | ADDED v2 — businessFieldCode is PAYMENT_METHOD and businessValue is not an active PAYMENT_METHOD code (AC-FIN-065) | القيمة ليست طريقة دفع معرَّفة | The value is not a defined payment method |
| FIN-422-DIMENSION-TAG-DUP | RULE-FIN-025 | API-FIN-011 | 422 | ADDED v2 — two tags of one submitted rule line name the same dimension (AC-FIN-069) | لا يجوز وسم البُعد نفسه مرتين على سطر القاعدة | A rule line cannot tag the same dimension twice |
| FIN-422-DIMENSION-TAG-SOURCE | RULE-FIN-026 | API-FIN-011 | 422 | ADDED v2 — a CONSTANT tag without a value of its own dimension or with a business field, or a BUSINESS_FIELD tag without a business field or with a value (AC-FIN-070) | وسم البُعد لا يطابق مصدر قيمته | The dimension tag does not match its value source |
| FIN-422-UNRESOLVED-DIMENSION-VALUE | RULE-FIN-027 | API-FIN-020 | 422 | ADDED v2 — the event's value for a BUSINESS_FIELD tag equals the code of no dimension value of the tagged dimension; fixed by defining the value (AC-FIN-074) | قيمة البُعد في الحدث لا تطابق أي قيمة معرَّفة لهذا البُعد | The dimension value in the event matches no defined value of this dimension |
| FIN-422-NO-DEFINED-PERIOD | RULE-FIN-028 | API-FIN-020 | 422 | ADDED v2 — no fiscal period's [startDate, endDate] covers the event's docDate; a calendar-setup error, never the retryable period-not-open answer (AC-FIN-080) | لا توجد فترة محاسبية معرَّفة تغطي تاريخ الحدث | No accounting period is defined for the event date |
| FIN-404-ACCOUNT-MAPPING | PLATFORM-STD (ADR-FIN-030) | API-FIN-040, 041 | 404 | ADDED v2 — unknown account-mapping id in the path | ربط الحساب غير موجود | Account mapping not found |

Every PLATFORM-STD row follows the platform's umbrella convention for infrastructure errors, which SEC established
at its own P3.1 and FIN cites without re-deriving. The v2 PLATFORM-STD row, FIN-404-ACCOUNT-MAPPING, is recorded in
ADR-FIN-030.

## QR id definitions — ADDED in v2
**QR-FIN-050** — FIND_BY_CRITERIA search account mappings, with the account's display columns [ENT-FIN-015, API-FIN-038]
**QR-FIN-051** — SAVE create account mapping [ENT-FIN-015, API-FIN-039]
**QR-FIN-052** — EXISTS an active mapping holds the key (RULE-FIN-022) [ENT-FIN-015, API-FIN-039]
**QR-FIN-053** — UPDATE change a mapping's account [ENT-FIN-015, API-FIN-040]
**QR-FIN-054** — UPDATE deactivate a mapping [ENT-FIN-015, API-FIN-041]
**QR-FIN-055** — FIND_ONE the active mapping for an event's business value (RULE-FIN-020) [ENT-FIN-015, API-FIN-020]
**QR-FIN-056** — SAVE a rule line's dimension tags [ENT-FIN-016, API-FIN-011]
**QR-FIN-057** — FIND_BY_CRITERIA the tags of the active rule's lines [ENT-FIN-016, API-FIN-020]
**QR-FIN-058** — FIND_ONE dimension value by code within a dimension (RULE-FIN-027) [ENT-FIN-003, API-FIN-020]
**QR-FIN-059** — FIND_ONE the fiscal period covering a date (RULE-FIN-028) [ENT-FIN-008, API-FIN-020]
**QR-FIN-060** — FIND_ONE the account a mapping names (RULE-FIN-023) [ENT-FIN-001, API-FIN-039, API-FIN-040]
**QR-FIN-061** — FIND_ONE a tag's dimension and CONSTANT value (RULE-FIN-026) [ENT-FIN-002, ENT-FIN-003, API-FIN-011]

## QR id definitions (cross-reference index — full detail in Query Reference Catalog above)
**QR-FIN-001** — FIND_BY_CRITERIA search accounts [ENT-FIN-001, API-FIN-001]
**QR-FIN-002** — SAVE create account [ENT-FIN-001, API-FIN-002]
**QR-FIN-003** — UPDATE update account [ENT-FIN-001, API-FIN-003]
**QR-FIN-004** — UPDATE deactivate account [ENT-FIN-001, API-FIN-004]
**QR-FIN-005** — EXISTS account code unique [ENT-FIN-001, API-FIN-002]
**QR-FIN-006** — EXISTS account has no children (RULE-FIN-001) [ENT-FIN-001, API-FIN-002, API-FIN-003]
**QR-FIN-007** — FIND_BY_CRITERIA search dimensions [ENT-FIN-002, API-FIN-005]
**QR-FIN-008** — SAVE create dimension [ENT-FIN-002, API-FIN-006]
**QR-FIN-009** — SAVE create dimension value [ENT-FIN-003, API-FIN-007]
**QR-FIN-010** — EXISTS dimension value code unique within dimension (RULE-FIN-002) [ENT-FIN-003, API-FIN-007]
**QR-FIN-011** — FIND_BY_CRITERIA search dimension values [ENT-FIN-003, API-FIN-008]
**QR-FIN-012** — FIND_BY_CRITERIA search event-type rules [ENT-FIN-009, API-FIN-009]
**QR-FIN-013** — SAVE create event-type rule [ENT-FIN-009, API-FIN-010]
**QR-FIN-014** — EXISTS one active rule per event type [ENT-FIN-009, API-FIN-010]
**QR-FIN-015** — SAVE add rule line; v2 also writes accountBusinessFieldCode [ENT-FIN-010, API-FIN-011]
**QR-FIN-016** — EXISTS remainder-line/target count (RULE-FIN-003) [ENT-FIN-010, ENT-FIN-014, API-FIN-011, API-FIN-016]
**QR-FIN-017** — FIND_BY_CRITERIA search templates [ENT-FIN-011, API-FIN-012]
**QR-FIN-018** — SAVE create template with lines [ENT-FIN-011, ENT-FIN-012, API-FIN-013]
**QR-FIN-019** — FIND_ONE load the template to run, by id [ENT-FIN-011, API-FIN-014]
**QR-FIN-020** — FIND_BY_CRITERIA search allocation rules [ENT-FIN-013, API-FIN-015]
**QR-FIN-021** — SAVE create allocation rule with targets [ENT-FIN-013, ENT-FIN-014, API-FIN-016]
**QR-FIN-022** — FIND_ONE load allocation rule + source balance [ENT-FIN-013, API-FIN-017]
**QR-FIN-023** — FIND_BY_CRITERIA search journal entries [ENT-FIN-004, API-FIN-018]
**QR-FIN-024** — SAVE build manual entry (DRAFT) [ENT-FIN-004, ENT-FIN-005, ENT-FIN-006, API-FIN-019]
**QR-FIN-025** — SAVE build event entry (DRAFT) from the rule; v2 lines carry the constant or mapped account and their resolved dimensions [ENT-FIN-004, ENT-FIN-005, ENT-FIN-006, API-FIN-020]
**QR-FIN-026** — EXISTS duplicate eventReference (RULE-FIN-004); v2 the friendly pre-check, UQ_FIN_JOURNAL_ENTRY_EVENT_REF the guarantee [ENT-FIN-004, API-FIN-020]
**QR-FIN-027** — EXISTS active rule for event type (RULE-FIN-005) [ENT-FIN-009, API-FIN-020]
**QR-FIN-028** — AGGREGATE compute remainder-line amount (RULE-FIN-010) [ENT-FIN-005, API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017]
**QR-FIN-029** — EXISTS debits=credits (RULE-FIN-006) [ENT-FIN-005, API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017]
**QR-FIN-030** — EXISTS every line account leaf+active (RULE-FIN-007) [ENT-FIN-001, API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017]
**QR-FIN-031** — EXISTS period open at post (RULE-FIN-008) [ENT-FIN-008, API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017]
**QR-FIN-032** — EXISTS every dimension value valid+active (RULE-FIN-009) [ENT-FIN-006, API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017]
**QR-FIN-033** — UPDATE post entry DRAFT→POSTED [ENT-FIN-004, API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017]
**QR-FIN-034** — SAVE build reversal entry (RULE-FIN-011) [ENT-FIN-004, ENT-FIN-005, ENT-FIN-006, API-FIN-021]
**QR-FIN-035** — EXISTS original entry is POSTED and not already reversed (RULE-FIN-013) [ENT-FIN-004, API-FIN-021, API-FIN-014]
**QR-FIN-036** — FIND_ONE current open period for reversal (RULE-FIN-012) [ENT-FIN-008, API-FIN-021]
**QR-FIN-037** — FIND_ONE read one entry with lines [ENT-FIN-004, API-FIN-022]
**QR-FIN-038** — SAVE create fiscal year + periods [ENT-FIN-007, ENT-FIN-008, API-FIN-023]
**QR-FIN-039** — UPDATE open/soft-close/hard-close period [ENT-FIN-008, API-FIN-024, API-FIN-025, API-FIN-026]
**QR-FIN-040** — EXISTS period not already Hard Closed (RULE-FIN-014) [ENT-FIN-008, API-FIN-024, API-FIN-026]
**QR-FIN-041** — AGGREGATE year-end closing/opening balances [ENT-FIN-004, ENT-FIN-005, API-FIN-027]
**QR-FIN-042** — FIND_BY_CRITERIA account ledger POSTED lines; running balance accumulated in the service [ENT-FIN-005, API-FIN-028]
**QR-FIN-043** — AGGREGATE trial balance / balance sheet / income statement account balances [ENT-FIN-005, API-FIN-029, API-FIN-030, API-FIN-031]
**QR-FIN-044** — AGGREGATE dimension report [ENT-FIN-005, ENT-FIN-006, API-FIN-032]
**QR-FIN-045** — FIND_ONE the Retained Earnings account (DBF-FIN-147) [ENT-FIN-001, API-FIN-027]
**QR-FIN-046** — EXISTS dimension code unique [ENT-FIN-002, API-FIN-006]
**QR-FIN-047** — EXISTS fiscal year code unique [ENT-FIN-007, API-FIN-023]
**QR-FIN-048** — FIND_ONE next fiscal year by startDate [ENT-FIN-007, API-FIN-027]
**QR-FIN-049** — FIND_BY_CRITERIA every period of one fiscal year [ENT-FIN-008, API-FIN-023, API-FIN-027, API-FIN-014]

## XM id definitions
**XM-FIN-001** — SOFT-READ every FIN lookup-backed column → MDL_LOOKUP_VALUE; MODIFIED v2: six more columns, 15 FIN-owned keys [REQ-FIN-001, REQ-FIN-007, REQ-FIN-008, REQ-FIN-010, REQ-FIN-014, REQ-FIN-018, REQ-FIN-022, REQ-FIN-025, REQ-FIN-031, REQ-FIN-045, REQ-FIN-048, REQ-FIN-049, REQ-FIN-054, REQ-FIN-061, REQ-FIN-062]
**~~XM-FIN-002~~** — RETIRED 2026-09-12, id burned and not reused. Was: READ SEC's user→permission directory (`SecUserDirectoryApi.findUserIdsHoldingPermission`) for a global user-set-disjointness check that RULE-FIN-015 does not require; retired when `FinSeparationOfDutiesService` was deleted [REQ-FIN-037, REQ-FIN-038]

## Registry content
See `registry-exec-be-fin.md`.
══════════════════════════════════════════════════════════════════

<<<END INPUT>>>

<<<INPUT: frontend-execution-plan>>>
# Frontend execution plan — FIN v2 (خطة تنفيذ الواجهة الأمامية — الحسابات العامة)

Stack : `react-ts-vite` · routing `react-router` · server-state `tanstack-query` · forms
`react-hook-form` · validation `zod` · state `zustand` · one lazy chunk per composite screen
(`profile.stack.frontend`).

Screens (ar/en) : شجرة الحسابات (Chart of accounts) · تعريف الأبعاد وقيمها (Dimensions) ·
قواعد المحرك (Engine rules) · قوالب متكررة/عكسية (Recurring/reversing templates) ·
قواعد التوزيع (Allocation rules) · قيود اليومية (Journal entries) ·
الفترات والسنوات المالية (Fiscal periods & years) · دفتر الحساب (Account ledger) ·
ميزان المراجعة (Trial balance) · الميزانية العمومية (Balance sheet) ·
قائمة الدخل (Income statement) · تقارير الأبعاد (Dimension reports) ·
روابط الحسابات (Account mappings — v2)

Delta v2 (CS-FIN-001): this plan is re-emitted complete. A marker plan folds only its blocks, and the API
surface and alignment content must carry the v2 binding, so every block is written out as current state.
Changes from v1:
- **New screen.** SCR-FIN-013, account mappings, has a SUB in F1 to F4 and a row in SEC-FE.
- **Rewritten screens.** SCR-FIN-003's blocks change for the MAPPING line, the dimension tags and the
  line-per-submit composition (ADR-FIN-062).
- **Bound to the published v2 surface.** The republished api-docs carry CS-FIN-001's endpoints, the v2 members
  of the add-rule-line request and response, and the v2 catalogue codes. Every binding the previous run of this
  stage left pending is now a published id, DTO and code (ADR-FIN-064, which supersedes the BLOCKED ADR-FIN-060).
- **Endpoint citations.** Every endpoint is cited by its published contract id. The one served endpoint the
  api-docs publish without an id, the fiscal-year search, is cited by verb and path (ADR-FIN-061).
- **Composition.** Every F4 block carries its `Composition` and `Saves` lines.
- **Consumer feedback from the frontend build (ADR-FIN-065).**
  - Five screens own a child collection: SCR-FIN-002, 003, 004, 005 and 007. Each becomes a list page, a `/new`
    page and a `/:id` record page. The record page hydrates through its published search filtered by
    `<entity>Pk EQUALS id`, so no by-id endpoint is invented.
  - F1 types every nullable response field as `T | null`.
  - F2 publishes SCR-FIN-006's year options and sends the income-statement bounds independently.
  - Every option read discloses `hasMore`.
  - Two SCR-FIN-003 409s and the journal-entry line refusals are bound.
  - No action affordance is gated client-side.
- **What a consumer must READ to build each screen (ADR-FIN-067).** The frontend built v2 end to end and met reads
  this plan never bound. Every block now carries them, and `analyze` checks each rule (C9.18–C9.22, C10.7):
  - an option read for every key a screen sends: the report parameters of SCR-FIN-008 … 012, and the account and
    dimension-value pickers of SCR-FIN-004 and 005;
  - a label read for every foreign id a screen renders, by `<entity>Pk IN [ids]` with no status filter, on a row
    of its own — never the filtered, capped picker;
  - every child option list scoped by its published parent (`fiscalYearId`, `dimensionId`);
  - the SCR-FIN-013 → SCR-FIN-001 drill-in by `accountPk EQUALS`;
  - SCR-FIN-006's lines as a pane beneath the header on its `/new` page, with a one-line Drawer;
  - the text of every routed refusal bound to its catalogue row.

## API SURFACE — FIN v2

Shapes come from `_inputs/api-docs-fin.md`, cited by `API-FIN-*` id and never restated. That file was fetched
from backend commit `4cbe044…` on 2026-09-23T20:36Z. Its catalog gives a contract id to 41 of the 42 served
endpoints, the whole P3.1 v2 registry range. The one exception is `POST /api/v1/fin/fiscal-years/search`,
reported as "undeclared" by the api-docs' own drift table; this plan cites it by verb and path (ADR-FIN-061).
The companion binding annex of ADR-FIN-002 is not a source of ids.

```
BINDING     REQ-FIN-001 → API-FIN-002 (create), API-FIN-003 (update, RULE-FIN-001)
            REQ-FIN-002 → API-FIN-002, API-FIN-003 (RULE-FIN-001)
            REQ-FIN-003 → API-FIN-004
            REQ-FIN-004 → API-FIN-006
            REQ-FIN-005 → API-FIN-007, API-FIN-008, API-FIN-035
            REQ-FIN-006 → API-FIN-007 (RULE-FIN-002)
            REQ-FIN-007 → API-FIN-009, API-FIN-010, API-FIN-034
            REQ-FIN-008 → API-FIN-011
            REQ-FIN-009 → API-FIN-011 (RULE-FIN-003)
            REQ-FIN-010 → API-FIN-020
            REQ-FIN-011 → API-FIN-020 (RULE-FIN-004)
            REQ-FIN-012 → API-FIN-020, API-FIN-014, API-FIN-017 (RULE-FIN-010)
            REQ-FIN-013 → API-FIN-020 (RULE-FIN-005)
            REQ-FIN-014 → API-FIN-019
            REQ-FIN-015 → API-FIN-019
            REQ-FIN-016 → API-FIN-022
            REQ-FIN-017 → API-FIN-019, API-FIN-020
            REQ-FIN-018 → API-FIN-019, API-FIN-020 (RULE-FIN-006)
            REQ-FIN-019 → API-FIN-019, API-FIN-020 (RULE-FIN-007)
            REQ-FIN-020 → API-FIN-019, API-FIN-020 (RULE-FIN-008)
            REQ-FIN-021 → API-FIN-019, API-FIN-020 (RULE-FIN-009)
            REQ-FIN-022 → API-FIN-012, API-FIN-013, API-FIN-036
            REQ-FIN-023 → API-FIN-014
            REQ-FIN-024 → API-FIN-014
            REQ-FIN-025 → API-FIN-015, API-FIN-016, API-FIN-037
            REQ-FIN-026 → API-FIN-017
            REQ-FIN-027 → API-FIN-018, API-FIN-022
            REQ-FIN-028 → API-FIN-021 (RULE-FIN-011)
            REQ-FIN-029 → API-FIN-021 (RULE-FIN-012)
            REQ-FIN-030 → API-FIN-021 (RULE-FIN-013)
            REQ-FIN-031 → API-FIN-023, API-FIN-033, POST /api/v1/fin/fiscal-years/search
            REQ-FIN-032 → API-FIN-024
            REQ-FIN-033 → API-FIN-025
            REQ-FIN-034 → API-FIN-026 (RULE-FIN-015)
            REQ-FIN-035 → API-FIN-024 (RULE-FIN-014)
            REQ-FIN-036 → API-FIN-027
            REQ-FIN-037 → API-FIN-026
            REQ-FIN-038 → API-FIN-026
            REQ-FIN-039 → API-FIN-028
            REQ-FIN-040 → API-FIN-029
            REQ-FIN-041 → API-FIN-030
            REQ-FIN-042 → API-FIN-031
            REQ-FIN-043 → API-FIN-032
            REQ-FIN-044 → (onboarding, no screen — traceability matrix)
            REQ-FIN-045 → (onboarding, no screen — traceability matrix; v2 adds two lookup keys, read through UXD-FIN-013/014)
            REQ-FIN-046 → API-FIN-028, API-FIN-029, API-FIN-030, API-FIN-031 (drill-down chain)
  v2        REQ-FIN-047 → API-FIN-020 (system caller, ADR-FIN-007)
            REQ-FIN-048 → API-FIN-011 (RULE-FIN-018)
            REQ-FIN-049 → API-FIN-011 (RULE-FIN-019 — the line's accountBusinessFieldCode member)
            REQ-FIN-050 → API-FIN-020 (RULE-FIN-020)
            REQ-FIN-051 → API-FIN-020 (RULE-FIN-021)
            REQ-FIN-052 → API-FIN-020 (RULE-FIN-007)
            REQ-FIN-053 → API-FIN-020 (RULE-FIN-020)
            REQ-FIN-054 → API-FIN-039 (RULE-FIN-022, RULE-FIN-023, RULE-FIN-024)
            REQ-FIN-055 → API-FIN-038
            REQ-FIN-056 → API-FIN-040 (RULE-FIN-023)
            REQ-FIN-057 → API-FIN-041
            REQ-FIN-058 → API-FIN-040, API-FIN-041 (change / deactivate), API-FIN-022 (a posted entry's lines read back unchanged)
            REQ-FIN-059 → API-FIN-039 (RULE-FIN-022)
            REQ-FIN-060 → API-FIN-039, API-FIN-040 (RULE-FIN-023); API-FIN-001 feeds the active-leaf picker
            REQ-FIN-061 → API-FIN-039 (RULE-FIN-024)
            REQ-FIN-062 → API-FIN-011 (RULE-FIN-025, RULE-FIN-026 — the line's dimensionTags member)
            REQ-FIN-063 → API-FIN-020
            REQ-FIN-064 → API-FIN-011 (RULE-FIN-025)
            REQ-FIN-065 → API-FIN-011 (RULE-FIN-026)
            REQ-FIN-066 → API-FIN-020 (RULE-FIN-009)
            REQ-FIN-067 → API-FIN-020 (RULE-FIN-009)
            REQ-FIN-068 → API-FIN-020 (RULE-FIN-021)
            REQ-FIN-069 → API-FIN-020 (RULE-FIN-027)
            REQ-FIN-070 → API-FIN-020 (writes), API-FIN-022 (reads lines[].dimensions — one shape for every journal type)
            REQ-FIN-071 → API-FIN-028, API-FIN-032
            REQ-FIN-072 → API-FIN-020 (RULE-FIN-004)
            REQ-FIN-073 → API-FIN-020 (RULE-FIN-008)
            REQ-FIN-074 → API-FIN-020 (RULE-FIN-028)
            REQ-FIN-075 → API-FIN-020 — the published error catalogue itself, no screen; it lists FIN-409-PERIOD-NOT-OPEN once, with its ar/en message and the note that the rejection clears once the period is opened (AC-FIN-081)
            REQ-FIN-076 → API-FIN-020
            REQ-FIN-077 → API-FIN-021, API-FIN-022
UNMAPPED    REQ needing an endpoint that has none: none
            published endpoint with no REQ: none
            published without a contract id: one, POST /api/v1/fin/fiscal-years/search, cited by path → ADR-FIN-061
            API-FIN-020 is bound to REQ-FIN-010..013, 047, 050..053, 063, 066..077 and drawn on no screen (ADR-FIN-007)
CODES       FIN-409-DIMVALUE-DUP → RULE-FIN-002 · FIN-409-RULE-DUP → REQ-FIN-007 (§6.4, one rule per type — no numbered RULE-FIN-*)
            FIN-409-REMAINDER-COUNT → RULE-FIN-003 · FIN-409-PARENT-NOT-LEAF-ELIGIBLE → RULE-FIN-001
            FIN-409-HAS-CHILDREN → RULE-FIN-001 · FIN-409-NOT-POSTABLE-ACCOUNT → RULE-FIN-007
            FIN-409-NOT-REOPENABLE → RULE-FIN-014 · FIN-403-SOD-VIOLATION → unreachable, removed by decision (SRS Access summary SoD note)
            FIN-409-UNBALANCED → RULE-FIN-006 · FIN-409-PERIOD-NOT-OPEN → RULE-FIN-008 (and REQ-FIN-073, the consumer's retry signal)
            FIN-409-INVALID-DIMENSION → RULE-FIN-009 · FIN-409-DUPLICATE-EVENT → RULE-FIN-004 (both detection paths, REQ-FIN-072)
            FIN-409-NOT-POSTED → RULE-FIN-013 · FIN-409-INVALID-TRANSITION → RULE-FIN-014
            FIN-404-ENTRY/ACCOUNT/DIMENSION/DIMVALUE/RULE/YEAR/PERIOD/ALLOCATION-RULE/TEMPLATE/ACCOUNT-MAPPING → not-found (data lookup, no RULE)
            FIN-400-INVALID-LOOKUP → validation (bad lookup code) · FIN-400-INVALID-SORT → validation (generic)
            FIN-409-ACCOUNT-DUP/DIMENSION-DUP/YEAR-DUP → uniqueness (data integrity, no numbered RULE-FIN-*)
            FIN-400-MISSING-FREQUENCY → ENT-FIN-011 field rule (frequencyCode required when RECURRING)
            FIN-404-NO-ACTIVE-RULE → RULE-FIN-005 · FIN-409-PERIODS-NOT-CLOSED → REQ-FIN-036 precondition
            FIN-409-ALREADY-REVERSED → RULE-FIN-013 · FIN-422-REMAINDER-MARKER → RULE-FIN-003
            FIN-422-REMAINDER-NOT-POSITIVE → RULE-FIN-010 · FIN-422-INVALID-PERCENTAGE-VALUE → RULE-FIN-010
            FIN-400-PERIOD-NOT-IN-YEAR → RULE-FIN-017 · FIN-400-DOCDATE-OUTSIDE-PERIOD → RULE-FIN-017
            FIN-403-FORBIDDEN → SEC-enforced permission denial (ADR-FIN-005, not a FIN RULE)
            FIN-409-NOT-ACTIVE → recorded decision, not a RULE-FIN-* (SRS §B4 "DEFECT CLOSED 2026-09-12")
  v2        screen-facing: FIN-422-UNSUPPORTED-DERIVATION → RULE-FIN-018 · FIN-422-DERIVATION-SPEC → RULE-FIN-019
            FIN-422-DIMENSION-TAG-DUP → RULE-FIN-025 · FIN-422-DIMENSION-TAG-SOURCE → RULE-FIN-026
            FIN-409-MAPPING-DUP → RULE-FIN-022 · FIN-422-MAPPED-ACCOUNT-INELIGIBLE → RULE-FIN-023
            FIN-422-INVALID-PAYMENT-METHOD → RULE-FIN-024
            event-path only (API-FIN-020, no screen): FIN-422-UNMAPPED-VALUE → RULE-FIN-020 · FIN-422-MISSING-BUSINESS-FIELD
            → RULE-FIN-021 · FIN-422-UNRESOLVED-DIMENSION-VALUE → RULE-FIN-027 · FIN-422-NO-DEFINED-PERIOD → RULE-FIN-028
            retired: the MAPPING-unsupported code no longer appears in the published catalogue (ADR-FIN-027)
```

<!-- PHASE:F1:START traces=SCR-FIN-001,SCR-FIN-002,SCR-FIN-003,SCR-FIN-004,SCR-FIN-005,SCR-FIN-006,SCR-FIN-007,SCR-FIN-008,SCR-FIN-009,SCR-FIN-010,SCR-FIN-011,SCR-FIN-012,SCR-FIN-013 -->
## PHASE F1 — Models & Types

**RF1 — Models & types.** Field/DTO binding: see `_inputs/api-docs-fin.md`. The published request and
response shapes are the source and are not restated here. Every lookup-coded field is typed as `string`
everywhere in the models (ADR-FIN-004), never as an enum or a union of literals, so a value that MDL has not
yet seeded is still assignable to the type. Validity is a runtime concern (§F3), not a type concern. The
same holds in v2, where ACCOUNT_DERIVATION_TYPE loses DIRECT: no type changes, because the option list is
loaded at runtime.

**Nullable response fields (plan-wide, ADR-FIN-065).** Some response fields are `Required: No` in the api-docs,
or are described there as null in some case. Every such field is typed `T | null`, because the wire sends JSON
`null`. It is never typed as optional (`?: T`, which means `T | undefined`). Presence tests on wire data use
`== null` / `!= null`, never `=== undefined` / `!== undefined`. A type that omits `null` hides every wrong branch
from the compiler and from any test that mocks its own fixtures. Request DTOs are unaffected: an absent optional
request member is simply not sent.

<!-- SUB:F1-SCR-FIN-001:START traces=SCR-FIN-001,API-FIN-001,API-FIN-002,API-FIN-003,API-FIN-004 -->
### F1-SCR-FIN-001 — Chart of accounts
Types : `Account` (`AccountResponse`), `AccountCreateRequest`, `AccountUpdateRequest`, `AccountSearchRequest` — api-docs binding API-FIN-001..004
<!-- SUB:F1-SCR-FIN-001:END -->

<!-- SUB:F1-SCR-FIN-002:START traces=SCR-FIN-002,API-FIN-005,API-FIN-006,API-FIN-007,API-FIN-008,API-FIN-035,REQ-FIN-005 -->
### F1-SCR-FIN-002 — Dimensions
Types : `Dimension` (`DimensionResponse`), `DimensionCreateRequest`, `DimensionValue` (`DimensionValueResponse`), `DimensionValueCreateRequest`, `DimensionSearchRequest`, `DimensionValueSearchRequest` — api-docs binding API-FIN-005..008; the value deactivate API-FIN-035 returns `DimensionValueResponse`
<!-- SUB:F1-SCR-FIN-002:END -->

<!-- SUB:F1-SCR-FIN-003:START traces=SCR-FIN-003,API-FIN-001,API-FIN-005,API-FIN-008,API-FIN-009,API-FIN-010,API-FIN-011,API-FIN-034,REQ-FIN-007,REQ-FIN-049,REQ-FIN-062,UXD-FIN-013,UXD-FIN-014 -->
### F1-SCR-FIN-003 — Engine rules
Types : `EventTypeRule` (`EventTypeRuleResponse`), `EventTypeRuleCreateRequest`, `RuleLine` (`RuleLineResponse`, embedded in the rule response), `RuleLineCreateRequest`, `EventTypeRuleSearchRequest` — api-docs binding API-FIN-009..011. The rule deactivate API-FIN-034 returns `EventTypeRuleResponse`. The pickers reuse `Account`, `Dimension` and `DimensionValue` (API-FIN-001, API-FIN-005, API-FIN-008).
v2    : the line's business field is the published `accountBusinessFieldCode` member, and its dimension tags are the published `dimensionTags` array — `RuleLineDimensionTagRequest` on the add-line request, `RuleLineDimensionTagResponse` on every rule and line response (API-FIN-011, and the rule responses of API-FIN-009, API-FIN-010, API-FIN-034). `accountDerivationValue` is optional in the published request, so a MAPPING line leaves it empty as RULE-FIN-019 requires.
Null  : the response members that one derivation or value source leaves unset are `T | null`, never `?: T` (PHASE F1 rule, ADR-FIN-065). That covers `accountDerivationValue` and `accountBusinessFieldCode` on a line, and `dimensionValueId` and `businessFieldCode` on a tag.
<!-- SUB:F1-SCR-FIN-003:END -->

<!-- SUB:F1-SCR-FIN-004:START traces=SCR-FIN-004,API-FIN-012,API-FIN-013,API-FIN-014,API-FIN-036,REQ-FIN-022 -->
### F1-SCR-FIN-004 — Recurring/reversing templates
Types : `RecurringTemplate` (`RecurringTemplateResponse`, lines included), `RecurringTemplateCreateRequest`, `RecurringTemplateSearchRequest` — api-docs binding API-FIN-012..014; the deactivate API-FIN-036 returns `RecurringTemplateResponse`
<!-- SUB:F1-SCR-FIN-004:END -->

<!-- SUB:F1-SCR-FIN-005:START traces=SCR-FIN-005,API-FIN-015,API-FIN-016,API-FIN-017,API-FIN-037,REQ-FIN-025 -->
### F1-SCR-FIN-005 — Allocation rules
Types : `AllocationRule` (`AllocationRuleResponse`, targets included), `AllocationRuleCreateRequest`, `AllocationRuleSearchRequest` — api-docs binding API-FIN-015..017; the deactivate API-FIN-037 returns `AllocationRuleResponse`
<!-- SUB:F1-SCR-FIN-005:END -->

<!-- SUB:F1-SCR-FIN-006:START traces=SCR-FIN-006,API-FIN-018,API-FIN-019,API-FIN-021,API-FIN-022,REQ-FIN-070 -->
### F1-SCR-FIN-006 — Journal entries
Types : `JournalEntry` (`JournalEntryResponse`, lines + line-dimensions included), `JournalEntryCreateRequest`, `JournalEntrySearchRequest` — api-docs binding API-FIN-018,019,021,022. `EventEntryBuildRequest`/API-FIN-020 is bound (ADR-FIN-007) but its type is not consumed by any form model — no screen constructs it.
v2    : no type change. An event-built entry's `lines[].dimensions` has the same published shape as a manual entry's (REQ-FIN-070), so one line model serves every journal type.
Null  : `originalEntryId`, `reversalEntryId`, `postedAt` and `eventReference` are `T | null` (PHASE F1 rule, ADR-FIN-065). "Is a reversal", "is reversed" and "can be reversed" test `!= null` / `== null`. An `=== undefined` test on them is always wrong on the wire: it marked every entry a reversal and disabled Reverse for good.
<!-- SUB:F1-SCR-FIN-006:END -->

<!-- SUB:F1-SCR-FIN-007:START traces=SCR-FIN-007,API-FIN-023,API-FIN-024,API-FIN-025,API-FIN-026,API-FIN-027,API-FIN-033,REQ-FIN-031 -->
### F1-SCR-FIN-007 — Fiscal periods & years
Types : `FiscalYear` (`FiscalYearResponse`, periods included on create only), `FiscalYearCreateRequest`, `FiscalYearSearchRequest`, `FiscalPeriod` (`FiscalPeriodResponse`), `FiscalPeriodSearchRequest` — api-docs binding API-FIN-023..027 and API-FIN-033 (the fiscal-period search); the fiscal-year search is `POST /api/v1/fin/fiscal-years/search` (ADR-FIN-061). A fiscal-year search row always carries an empty `periods` array (api-docs), so the model never reads periods from it.
<!-- SUB:F1-SCR-FIN-007:END -->

<!-- SUB:F1-SCR-FIN-008:START traces=SCR-FIN-008,API-FIN-028 -->
### F1-SCR-FIN-008 — Account ledger
Types : `AccountLedgerResponse` (rows embedded) — api-docs binding API-FIN-028
<!-- SUB:F1-SCR-FIN-008:END -->

<!-- SUB:F1-SCR-FIN-009:START traces=SCR-FIN-009,API-FIN-029 -->
### F1-SCR-FIN-009 — Trial balance
Types : `TrialBalanceResponse` (rows embedded) — api-docs binding API-FIN-029
<!-- SUB:F1-SCR-FIN-009:END -->

<!-- SUB:F1-SCR-FIN-010:START traces=SCR-FIN-010,API-FIN-030 -->
### F1-SCR-FIN-010 — Balance sheet
Types : `BalanceSheetResponse` (groups/rows embedded) — api-docs binding API-FIN-030
<!-- SUB:F1-SCR-FIN-010:END -->

<!-- SUB:F1-SCR-FIN-011:START traces=SCR-FIN-011,API-FIN-031 -->
### F1-SCR-FIN-011 — Income statement
Types : `IncomeStatementResponse` (groups/rows embedded) — api-docs binding API-FIN-031
<!-- SUB:F1-SCR-FIN-011:END -->

<!-- SUB:F1-SCR-FIN-012:START traces=SCR-FIN-012,API-FIN-032 -->
### F1-SCR-FIN-012 — Dimension reports
Types : `DimensionReportResponse` (rows embedded) — api-docs binding API-FIN-032
<!-- SUB:F1-SCR-FIN-012:END -->

<!-- SUB:F1-SCR-FIN-013:START traces=SCR-FIN-013,API-FIN-001,API-FIN-038,API-FIN-039,API-FIN-040,API-FIN-041,REQ-FIN-054,REQ-FIN-055,REQ-FIN-056,REQ-FIN-057,UXD-FIN-007,UXD-FIN-013,UXD-FIN-015 -->
### F1-SCR-FIN-013 — Account mappings
Types : `AccountMapping` (`AccountMappingResponse` — it carries the mapped account's code and bilingual name, the display columns of SRS SCR-REQ-FIN-013 B2), `AccountMappingCreateRequest`, `AccountMappingUpdateRequest` (the account alone — ADR-FIN-024), `AccountMappingSearchRequest` — api-docs binding API-FIN-038..041 (search, create, change account, deactivate); the page envelope is the api-docs' `Page<T>`.
Reused: `Account` (`AccountResponse`) and `AccountSearchRequest` — API-FIN-001, for the account picker.
Null  : every response member the api-docs leave not required (the audit fields among them) is `T | null`, never `?: T` (PHASE F1 rule, ADR-FIN-065).
<!-- SUB:F1-SCR-FIN-013:END -->
<!-- PHASE:F1:END -->

<!-- PHASE:F2:START traces=SCR-FIN-001,SCR-FIN-002,SCR-FIN-003,SCR-FIN-004,SCR-FIN-005,SCR-FIN-006,SCR-FIN-007,SCR-FIN-008,SCR-FIN-009,SCR-FIN-010,SCR-FIN-011,SCR-FIN-012,SCR-FIN-013 -->
## PHASE F2 — Data Hooks

**RF2 — Data hooks.** State rule: page and page size live inside the filter object that forms the cache key.
Components use the facade only, and the facade uses the declared queries only. Every picker (account,
dimension, dimension value) is a read query with its own key. A picker returns a value to its caller's form
state and never writes.

**Option reads (plan-wide, ADR-FIN-065).** Every option read sends page 0 and size 200, the api-docs' maximum
(Pagination Constraints). It publishes `hasMore = totalElements > options.length`. Every consumer of an option
read discloses `hasMore` when the set is capped, and never truncates silently. That covers the picker Drawers
and also every plain `<select>`, such as the year selects of SCR-FIN-006, SCR-FIN-010 and SCR-FIN-011 and the
selects of SCR-FIN-013.

**Record reads (ADR-FIN-065).** SCR-FIN-002, 003, 004, 005 and 007 own a child collection, so each has a
`/:id` record page. Each declares a RECORD-QUERY: the screen's published search, sent with exactly one filter,
`<entity>Pk EQUALS id`, at page 0 and size 1. It sits under the screen's existing query-key root, so every
invalidation this phase already declares for that root refreshes it. A zero-row answer is "not found": an
honest notice with a way back to the list, never an empty form. No by-id endpoint is invented (ADR-FIN-006, as
narrowed). After a create, the record page's id is taken from the CREATE RESPONSE, never re-found in the list.

**Option reads for every key a screen sends (ADR-FIN-067).** Every foreign key a screen sends — a report's
query parameter, a request field, a nested line's account or dimension value — has an OPTIONS or PICKER read of
its entity in that screen's table. A report page is not buildable from its report read alone.

**Label reads (ADR-FIN-067).** Every foreign id a screen renders, and that the answering response does not
already label, resolves through a LABELS row of its own. The row reads the entity's search with
`<entity>Pk IN [the ids on screen]`, at page 0 and a size equal to the id count. It carries no status filter,
so a record deactivated since still has its label. A picker read is filtered (active, leaf) and capped at 200.
Labels resolved through it rendered raw ids for deactivated records and past the cap (TC-FIN-136). Picker reads
stay filtered; label reads never are. A dimension value's search is scoped by its dimension, so its label read
names that scope. Where the published line carries no `dimensionId`, the read runs once per dimension, taken
from an unfiltered dimension read, and no value is guessed (ADR-FIN-067).

**Scoped option reads (ADR-FIN-067).** A search the api-docs scope by a parent is read with that parent:
`fiscalYearId EQUALS` for periods, `dimensionId EQUALS` for dimension values. The parent sits in the read and
in its cache key. An unscoped read narrowed client-side is cut at the 200-row cap, and every parent past it has
no options (TC-FIN-068).

**Refusal texts (ADR-FIN-067).** Every code routed to a control or to the generic surface carries its words, as
`text: catalogue <CODE>`. That is the ar and en `Message` of the code's row in the api-docs' Known Error Codes,
which is the text the server answers with. Where a RULE raises the code, that row is the SRS rule's own
message, so the implementer writes no text of their own.

**Screen gate.** SCREEN-INIT reads the screen's page code from the SEC effective menu (ADR-FIN-005). No
endpoint publishes the caller's action-level permissions. So no hook reads one, and no affordance is gated on
one. FIN-403-FORBIDDEN on a write is mapped to a forbidden rejection, shown on the screen with
`text: catalogue FIN-403-FORBIDDEN`.

<!-- SUB:F2-SCR-FIN-001:START traces=SCR-FIN-001,API-FIN-001,API-FIN-002,API-FIN-003,API-FIN-004,REQ-FIN-001,REQ-FIN-002,REQ-FIN-003 -->
### F2-SCR-FIN-001 — Chart of accounts
| Hook | Kind · API | Cache key | Errors | Loading | Invalidation |
|---|---|---|---|---|---|
| F2-SCR-FIN-001-SEARCH-QUERY | read · API-FIN-001 | [accounts, filters] (code/name/type/active, and `accountPk EQUALS` when a drill-in seeds it, + page/size) | server → generic | LOCAL | — |
| F2-SCR-FIN-001-PARENT-LABELS-QUERY | read · API-FIN-001, one filter `accountPk IN [the parentAccountIds on screen]`, no status filter (ADR-FIN-067) | [accounts, {accountPk: ids}] — under the [accounts] root | server → generic | LOCAL | refreshed by every [accounts] invalidation below |
| F2-SCR-FIN-001-CREATE-MUTATION | mutation · API-FIN-002 | — | RULE-FIN-001 (FIN-409-PARENT-NOT-LEAF-ELIGIBLE, FIN-409-HAS-CHILDREN) → inline on isLeafFl, text: catalogue FIN-409-PARENT-NOT-LEAF-ELIGIBLE / FIN-409-HAS-CHILDREN (the SRS RULE-FIN-001 message) · FIN-409-ACCOUNT-DUP → inline on code, text: catalogue FIN-409-ACCOUNT-DUP | LOCAL | invalidates [accounts] |
| F2-SCR-FIN-001-UPDATE-MUTATION | mutation · API-FIN-003 | — | RULE-FIN-001 (FIN-409-HAS-CHILDREN) → inline on isLeafFl, text: catalogue FIN-409-HAS-CHILDREN (the SRS RULE-FIN-001 message) | LOCAL | invalidates [accounts] |
| F2-SCR-FIN-001-DEACTIVATE-MUTATION | mutation · API-FIN-004 | — | FIN-404-ACCOUNT → generic, text: catalogue FIN-404-ACCOUNT · server → generic | LOCAL | invalidates [accounts] |
| F2-SCR-FIN-001-LOOKUP | UXD-FIN-001, UXD-FIN-002 | [lookup, ACCOUNT_TYPE] / [lookup, DEBIT_CREDIT] — long-lived | — | NONE | — |
| F2-SCR-FIN-001-SCREEN-INIT | SCR-FIN-001 | screen gate (page code in the SEC menu, ADR-FIN-005) + the two lookups; filters seeded from the route search params (a drill-in from SCR-FIN-013 arrives with `accountPk EQUALS` the mapped account — ADR-FIN-067) | — | GLOBAL (first paint) | — |
| F2-SCR-FIN-001-FACADE | SCR-FIN-001 | composes SEARCH-QUERY, PARENT-LABELS-QUERY (each row's parent shown by code and name, whether or not the parent is on the current page), CREATE/UPDATE/DEACTIVATE-MUTATION · state: list, selection, filters (incl. page/size), derived loading · deactivate first checks a usage confirmation (RULE-FIN-001 parent case is server-checked, not pre-empted client-side) |
<!-- SUB:F2-SCR-FIN-001:END -->

<!-- SUB:F2-SCR-FIN-002:START traces=SCR-FIN-002,API-FIN-005,API-FIN-006,API-FIN-007,API-FIN-008,API-FIN-035,REQ-FIN-004,REQ-FIN-005,REQ-FIN-006 -->
### F2-SCR-FIN-002 — Dimensions
| Hook | Kind · API | Cache key | Errors | Loading | Invalidation |
|---|---|---|---|---|---|
| F2-SCR-FIN-002-SEARCH-DIM-QUERY | read · API-FIN-005 | [dimensions, filters] | server → generic | LOCAL | — |
| F2-SCR-FIN-002-RECORD-QUERY | read · API-FIN-005, one filter `dimensionPk EQUALS id`, page 0 size 1 (ADR-FIN-065) | [dimensions, {dimensionPk, page: 0, size: 1}] — under the [dimensions] root | zero rows → not found · server → generic | LOCAL | refreshed by CREATE-DIM's invalidation of [dimensions] |
| F2-SCR-FIN-002-SEARCH-VALUE-QUERY | read · API-FIN-008 | [dimension-values, filters incl. dimensionId] | server → generic | LOCAL | — |
| F2-SCR-FIN-002-CREATE-DIM-MUTATION | mutation · API-FIN-006 | — | FIN-409-DIMENSION-DUP → inline on code, text: catalogue FIN-409-DIMENSION-DUP | LOCAL | invalidates [dimensions] |
| F2-SCR-FIN-002-CREATE-VALUE-MUTATION | mutation · API-FIN-007 | — | RULE-FIN-002 (FIN-409-DIMVALUE-DUP) → inline on code, text: catalogue FIN-409-DIMVALUE-DUP (the SRS RULE-FIN-002 message) | LOCAL | invalidates [dimension-values] |
| F2-SCR-FIN-002-DEACTIVATE-VALUE-MUTATION | mutation · API-FIN-035 | — | FIN-404-DIMVALUE → generic, text: catalogue FIN-404-DIMVALUE | LOCAL | invalidates [dimension-values] |
| F2-SCR-FIN-002-SCREEN-INIT | SCR-FIN-002 | screen gate (page code in the SEC menu, ADR-FIN-005) | — | GLOBAL | — |
| F2-SCR-FIN-002-FACADE | SCR-FIN-002 | composes both search queries, RECORD-QUERY + the three mutations · state: dimension list (list page), the record page's dimension (RECORD-QUERY — never found among the list's rows) and its values, filters · after create, hands the created dimension's id from the create response to the record page |
<!-- SUB:F2-SCR-FIN-002:END -->

<!-- SUB:F2-SCR-FIN-003:START traces=SCR-FIN-003,API-FIN-001,API-FIN-005,API-FIN-008,API-FIN-009,API-FIN-010,API-FIN-011,API-FIN-034,REQ-FIN-007,REQ-FIN-008,REQ-FIN-009,REQ-FIN-048,REQ-FIN-049,REQ-FIN-062,REQ-FIN-064,REQ-FIN-065,UXD-FIN-002,UXD-FIN-007,UXD-FIN-008,UXD-FIN-009,UXD-FIN-010,UXD-FIN-013,UXD-FIN-014 -->
### F2-SCR-FIN-003 — Engine rules
| Hook | Kind · API | Cache key | Errors | Loading | Invalidation |
|---|---|---|---|---|---|
| F2-SCR-FIN-003-SEARCH-QUERY | read · API-FIN-009 | [event-rules, filters] (eventTypeCode, isActiveFl + page/size) | server → generic | LOCAL | — |
| F2-SCR-FIN-003-RECORD-QUERY | read · API-FIN-009, one filter `eventTypeRulePk EQUALS id`, page 0 size 1 (ADR-FIN-065) | [event-rules, {eventTypeRulePk, page: 0, size: 1}] — under the [event-rules] root | zero rows → not found · server → generic | LOCAL | refreshed by every [event-rules] invalidation below |
| F2-SCR-FIN-003-CREATE-MUTATION | mutation · API-FIN-010 — the rule alone (ADR-FIN-062) | — | FIN-409-RULE-DUP (published on API-FIN-010's Business Responses — one active rule per event type) → inline on eventTypeCode, text: catalogue FIN-409-RULE-DUP (ADR-FIN-065) · FIN-400-INVALID-LOOKUP → inline on eventTypeCode, text: catalogue FIN-400-INVALID-LOOKUP | LOCAL | invalidates [event-rules] |
| F2-SCR-FIN-003-ADD-LINE-MUTATION | mutation · API-FIN-011 — one line with its `dimensionTags` per call (ADR-FIN-062) | — | RULE-FIN-003 → inline on the Line entry's isRemainderFl (FIN-409-REMAINDER-COUNT — published on API-FIN-011's Business Responses, ADR-FIN-065 — and FIN-422-REMAINDER-MARKER), text: catalogue FIN-409-REMAINDER-COUNT / FIN-422-REMAINDER-MARKER (the SRS RULE-FIN-003 message) · RULE-FIN-018 → inline on the derivation type (FIN-422-UNSUPPORTED-DERIVATION), text: catalogue FIN-422-UNSUPPORTED-DERIVATION (the SRS RULE-FIN-018 message) · RULE-FIN-019 → inline on the derivation fields (FIN-422-DERIVATION-SPEC), text: catalogue FIN-422-DERIVATION-SPEC (the SRS RULE-FIN-019 message) · RULE-FIN-025 → inline on the tags summary row (FIN-422-DIMENSION-TAG-DUP), text: catalogue FIN-422-DIMENSION-TAG-DUP (the SRS RULE-FIN-025 message) · RULE-FIN-026 → inline on the tags summary row (FIN-422-DIMENSION-TAG-SOURCE), text: catalogue FIN-422-DIMENSION-TAG-SOURCE (the SRS RULE-FIN-026 message) · FIN-404-DIMENSION / FIN-404-DIMVALUE → inline on the tags summary row, text: catalogue FIN-404-DIMENSION / FIN-404-DIMVALUE · FIN-400-INVALID-LOOKUP → inline, text: catalogue FIN-400-INVALID-LOOKUP · FIN-404-RULE → generic, text: catalogue FIN-404-RULE | LOCAL | invalidates [event-rules] |
| F2-SCR-FIN-003-DEACTIVATE-MUTATION | mutation · API-FIN-034 | — | FIN-404-RULE → generic, text: catalogue FIN-404-RULE | LOCAL | invalidates [event-rules] |
| F2-SCR-FIN-003-ACCOUNT-PICKER-QUERY | read · API-FIN-001 | [accounts, {isLeafFl: true, isActiveFl: true, code/name filter, page, size}] | server → generic | LOCAL | — |
| F2-SCR-FIN-003-DIMENSIONS-QUERY | read · API-FIN-005 | [dimensions, {isActiveFl: true, page, size}] | server → generic | LOCAL | — |
| F2-SCR-FIN-003-DIMENSION-VALUES-QUERY | read · API-FIN-008 | [dimension-values, {dimensionId, isActiveFl: true, code filter, page, size}] | server → generic | LOCAL | — |
| F2-SCR-FIN-003-TAG-DIMENSION-LABELS-QUERY | read · API-FIN-005, one filter `dimensionPk IN [the record's tag dimensionIds]`, no status filter (ADR-FIN-067) | [dimensions, {dimensionPk: ids}] | server → generic | LOCAL | — |
| F2-SCR-FIN-003-TAG-VALUE-LABELS-QUERY | read · API-FIN-008, per tag dimension: `dimensionId EQUALS <the tag's dimensionId>` (the parent scope) and `dimensionValuePk IN [that dimension's tag values]`, no status filter (ADR-FIN-067) | [dimension-values, {dimensionId, dimensionValuePk: ids}] | server → generic | LOCAL | — |
| F2-SCR-FIN-003-LOOKUP | UXD-FIN-007, UXD-FIN-008, UXD-FIN-009, UXD-FIN-002, UXD-FIN-010, UXD-FIN-013, UXD-FIN-014 | seven [lookup, <key>] entries — long-lived | — | NONE | — |
| F2-SCR-FIN-003-SCREEN-INIT | SCR-FIN-003 | screen gate + seven lookups | — | GLOBAL | — |
| F2-SCR-FIN-003-FACADE | SCR-FIN-003 | composes SEARCH-QUERY, RECORD-QUERY, the three mutations, the three picker queries and the two tag LABELS queries (a saved tag on a dimension or value deactivated since still shows its name and code, never a raw id — ADR-FIN-067) · state: rule list (list page), the record page's rule (RECORD-QUERY) and its lines (with their `dimensionTags`, read from the rule response), filters, the Line entry's draft (line fields + the tag list the tag editor returns) · operations: createRule, addLine (one call, never chained behind createRule), deactivateRule · derives the "mappings" link of a MAPPING line (rule eventTypeCode + line accountBusinessFieldCode) for SCR-FIN-013 |
<!-- SUB:F2-SCR-FIN-003:END -->

<!-- SUB:F2-SCR-FIN-004:START traces=SCR-FIN-004,API-FIN-012,API-FIN-013,API-FIN-014,API-FIN-036,REQ-FIN-022,REQ-FIN-023,REQ-FIN-024 -->
### F2-SCR-FIN-004 — Recurring/reversing templates
| Hook | Kind · API | Cache key | Errors | Loading | Invalidation |
|---|---|---|---|---|---|
| F2-SCR-FIN-004-SEARCH-QUERY | read · API-FIN-012 | [recurring-templates, filters] | server → generic | LOCAL | — |
| F2-SCR-FIN-004-RECORD-QUERY | read · API-FIN-012, one filter `recurringTemplatePk EQUALS id`, page 0 size 1 (ADR-FIN-065) | [recurring-templates, {recurringTemplatePk, page: 0, size: 1}] — under the [recurring-templates] root | zero rows → not found · server → generic | LOCAL | refreshed by every [recurring-templates] invalidation below |
| F2-SCR-FIN-004-CREATE-MUTATION | mutation · API-FIN-013 | — | FIN-400-MISSING-FREQUENCY → inline on frequencyCode, text: catalogue FIN-400-MISSING-FREQUENCY · FIN-404-ACCOUNT → inline on the line set, text: catalogue FIN-404-ACCOUNT | LOCAL | invalidates [recurring-templates] |
| F2-SCR-FIN-004-RUN-MUTATION | mutation · API-FIN-014 | — | FIN-409-NOT-ACTIVE → generic, text: catalogue FIN-409-NOT-ACTIVE · RULE-FIN-006/007/008/009/011 → generic (server-built entry), text: catalogue row of the code answered (FIN-409-UNBALANCED / FIN-409-NOT-POSTABLE-ACCOUNT / FIN-409-PERIOD-NOT-OPEN / FIN-409-INVALID-DIMENSION) | GLOBAL (posts a real entry) | invalidates [recurring-templates], [journal-entries] |
| F2-SCR-FIN-004-DEACTIVATE-MUTATION | mutation · API-FIN-036 | — | FIN-404-TEMPLATE → generic, text: catalogue FIN-404-TEMPLATE | LOCAL | invalidates [recurring-templates] |
| F2-SCR-FIN-004-ACCOUNT-PICKER-QUERY | read · API-FIN-001 — the line's account picker (ADR-FIN-067) | [accounts, {isLeafFl: true, isActiveFl: true, code/name filter, page, size}] | server → generic | LOCAL | — |
| F2-SCR-FIN-004-DIMENSIONS-QUERY | read · API-FIN-005 — the line's dimension, chosen before its value (ADR-FIN-067) | [dimensions, {isActiveFl: true, page, size}] | server → generic | LOCAL | — |
| F2-SCR-FIN-004-DIMENSION-VALUES-QUERY | read · API-FIN-008 — the line's value picker (ADR-FIN-067) | [dimension-values, {dimensionId, isActiveFl: true, code filter, page, size}] | server → generic | LOCAL | — |
| F2-SCR-FIN-004-ACCOUNT-LABELS-QUERY | read · API-FIN-001, one filter `accountPk IN [the lines' accountIds]`, no status filter (ADR-FIN-067) | [accounts, {accountPk: ids}] | server → generic | LOCAL | — |
| F2-SCR-FIN-004-VALUE-LABELS-QUERY | read · API-FIN-005 (every dimension, no status filter), then API-FIN-008 once per dimension: `dimensionId EQUALS <that dimension>` (the parent scope) and `dimensionValuePk IN [the lines' dimensionValueIds]`, no status filter — the published template line carries no dimensionId (ADR-FIN-067) | [dimension-values, {dimensionId, dimensionValuePk: ids}] | server → generic | LOCAL | — |
| F2-SCR-FIN-004-LOOKUP | UXD-FIN-011, UXD-FIN-012, UXD-FIN-002 | three [lookup, <key>] — long-lived | — | NONE | — |
| F2-SCR-FIN-004-SCREEN-INIT | SCR-FIN-004 | screen gate + three lookups | — | GLOBAL | — |
| F2-SCR-FIN-004-FACADE | SCR-FIN-004 | composes SEARCH-QUERY, RECORD-QUERY, the three mutations, the three picker reads (account; dimension, then value) and the two LABELS reads, so every line shows its account and dimension value by code and name (ADR-FIN-067) · state: template list (list page), the record page's template (RECORD-QUERY) and its lines, the New page's line draft (composed in the Line entry, sent in the one create), filters |
<!-- SUB:F2-SCR-FIN-004:END -->

<!-- SUB:F2-SCR-FIN-005:START traces=SCR-FIN-005,API-FIN-015,API-FIN-016,API-FIN-017,API-FIN-037,REQ-FIN-025,REQ-FIN-026 -->
### F2-SCR-FIN-005 — Allocation rules
| Hook | Kind · API | Cache key | Errors | Loading | Invalidation |
|---|---|---|---|---|---|
| F2-SCR-FIN-005-SEARCH-QUERY | read · API-FIN-015 | [allocation-rules, filters] | server → generic | LOCAL | — |
| F2-SCR-FIN-005-RECORD-QUERY | read · API-FIN-015, one filter `allocationRulePk EQUALS id`, page 0 size 1 (ADR-FIN-065) | [allocation-rules, {allocationRulePk, page: 0, size: 1}] — under the [allocation-rules] root | zero rows → not found · server → generic | LOCAL | refreshed by every [allocation-rules] invalidation below |
| F2-SCR-FIN-005-CREATE-MUTATION | mutation · API-FIN-016 | — | RULE-FIN-003 (FIN-409-REMAINDER-COUNT, FIN-422-REMAINDER-MARKER, FIN-422-REMAINDER-NOT-POSITIVE) → inline on the target set, text: catalogue FIN-409-REMAINDER-COUNT / FIN-422-REMAINDER-MARKER / FIN-422-REMAINDER-NOT-POSITIVE (the SRS RULE-FIN-003 message) · FIN-404-ACCOUNT → inline on the account it names, text: catalogue FIN-404-ACCOUNT | LOCAL | invalidates [allocation-rules] |
| F2-SCR-FIN-005-RUN-MUTATION | mutation · API-FIN-017 | — | FIN-409-NOT-ACTIVE → generic, text: catalogue FIN-409-NOT-ACTIVE · RULE-FIN-006..011 → generic, text: catalogue row of the code answered | GLOBAL (posts a real entry) | invalidates [allocation-rules], [journal-entries] |
| F2-SCR-FIN-005-DEACTIVATE-MUTATION | mutation · API-FIN-037 | — | FIN-404-ALLOCATION-RULE → generic, text: catalogue FIN-404-ALLOCATION-RULE | LOCAL | invalidates [allocation-rules] |
| F2-SCR-FIN-005-ACCOUNT-PICKER-QUERY | read · API-FIN-001 — the source and target account pickers (ADR-FIN-067) | [accounts, {isLeafFl: true, isActiveFl: true, code/name filter, page, size}] | server → generic | LOCAL | — |
| F2-SCR-FIN-005-DIMENSIONS-QUERY | read · API-FIN-005 — a target's dimension, chosen before its value (ADR-FIN-067) | [dimensions, {isActiveFl: true, page, size}] | server → generic | LOCAL | — |
| F2-SCR-FIN-005-DIMENSION-VALUES-QUERY | read · API-FIN-008 — a target's value picker (ADR-FIN-067) | [dimension-values, {dimensionId, isActiveFl: true, code filter, page, size}] | server → generic | LOCAL | — |
| F2-SCR-FIN-005-ACCOUNT-LABELS-QUERY | read · API-FIN-001, one filter `accountPk IN [the sourceAccountId and the targets' targetAccountIds]`, no status filter (ADR-FIN-067) | [accounts, {accountPk: ids}] | server → generic | LOCAL | — |
| F2-SCR-FIN-005-VALUE-LABELS-QUERY | read · API-FIN-005 (every dimension, no status filter), then API-FIN-008 once per dimension: `dimensionId EQUALS <that dimension>` (the parent scope) and `dimensionValuePk IN [the targets' dimensionValueIds]`, no status filter — the published target carries no dimensionId (ADR-FIN-067) | [dimension-values, {dimensionId, dimensionValuePk: ids}] | server → generic | LOCAL | — |
| F2-SCR-FIN-005-LOOKUP | UXD-FIN-010 | [lookup, DISTRIBUTION_TYPE] — long-lived | — | NONE | — |
| F2-SCR-FIN-005-SCREEN-INIT | SCR-FIN-005 | screen gate + the lookup | — | GLOBAL | — |
| F2-SCR-FIN-005-FACADE | SCR-FIN-005 | composes SEARCH-QUERY, RECORD-QUERY, the three mutations, the three picker reads and the two LABELS reads, so the source and every target show their account and dimension value by code and name (ADR-FIN-067) · state: rule list (list page), the record page's rule (RECORD-QUERY) and its targets, the New page's target draft (composed in the Target entry, sent in the one create), filters |
<!-- SUB:F2-SCR-FIN-005:END -->

<!-- SUB:F2-SCR-FIN-006:START traces=SCR-FIN-006,API-FIN-001,API-FIN-005,API-FIN-008,API-FIN-018,API-FIN-019,API-FIN-021,API-FIN-022,API-FIN-033,REQ-FIN-014,REQ-FIN-016,REQ-FIN-017,REQ-FIN-027,REQ-FIN-028,REQ-FIN-070,REQ-FIN-077 -->
### F2-SCR-FIN-006 — Journal entries
| Hook | Kind · API | Cache key | Errors | Loading | Invalidation |
|---|---|---|---|---|---|
| F2-SCR-FIN-006-SEARCH-QUERY | read · API-FIN-018 | [journal-entries, filters] (docNo/docDate/period/status/type + page/size) | server → generic | LOCAL | — |
| F2-SCR-FIN-006-READ-QUERY | read · API-FIN-022 | [journal-entries, id] | FIN-404-ENTRY → generic, text: catalogue FIN-404-ENTRY | LOCAL | — |
| F2-SCR-FIN-006-CREATE-MUTATION | mutation · API-FIN-019 | — | RULE-FIN-006 (FIN-409-UNBALANCED) → inline on the lines pane's totals, text: catalogue FIN-409-UNBALANCED (the SRS RULE-FIN-006 message) · RULE-FIN-008 (FIN-409-PERIOD-NOT-OPEN) → inline on periodId, text: catalogue FIN-409-PERIOD-NOT-OPEN (the SRS RULE-FIN-008 message) · RULE-FIN-017 (FIN-400-PERIOD-NOT-IN-YEAR / FIN-400-DOCDATE-OUTSIDE-PERIOD) → inline on periodId / docDate, text: catalogue FIN-400-PERIOD-NOT-IN-YEAR / FIN-400-DOCDATE-OUTSIDE-PERIOD · RULE-FIN-007 (FIN-409-NOT-POSTABLE-ACCOUNT) and RULE-FIN-009 (FIN-409-INVALID-DIMENSION) → the lines pane's refusal slot, announced once, text: catalogue FIN-409-NOT-POSTABLE-ACCOUNT / FIN-409-INVALID-DIMENSION (the SRS RULE-FIN-007 / RULE-FIN-009 messages) — the published error body names no line, so the message is never bound to a guessed line (known limitation, ADR-FIN-065) | LOCAL | invalidates [journal-entries] |
| F2-SCR-FIN-006-REVERSE-MUTATION | mutation · API-FIN-021 | — | RULE-FIN-013 (FIN-409-NOT-POSTED / FIN-409-ALREADY-REVERSED) → generic, text: catalogue FIN-409-NOT-POSTED / FIN-409-ALREADY-REVERSED — each code's own row: the SRS RULE-FIN-013 message is the not-posted one | LOCAL | invalidates [journal-entries] |
| F2-SCR-FIN-006-YEAR-OPTIONS-QUERY | read · `POST /api/v1/fin/fiscal-years/search` (ADR-FIN-061) — the manual entry's year select | [fiscal-years, {page: 0, size: 200}] — the SCR-FIN-007 root | server → generic | LOCAL | — |
| F2-SCR-FIN-006-PERIOD-OPTIONS-QUERY | read · API-FIN-033, `fiscalYearId EQUALS <the chosen year>` — the parent scope; never read unscoped and narrowed client-side (TC-FIN-068, ADR-FIN-067) | [fiscal-periods, {fiscalYearId, page: 0, size: 200}] — the SCR-FIN-007 root | server → generic | LOCAL | — |
| F2-SCR-FIN-006-ACCOUNT-PICKER-QUERY | read · API-FIN-001 | [accounts, {isLeafFl: true, isActiveFl: true, code/name filter, page, size}] | server → generic | LOCAL | — |
| F2-SCR-FIN-006-DIMENSIONS-QUERY | read · API-FIN-005, API-FIN-008 | [dimensions, {isActiveFl: true, …}] / [dimension-values, {dimensionId, isActiveFl: true, …}] — feeds the manual line's dimension and value pickers only; labels are the LABELS rows below (ADR-FIN-067) | server → generic | LOCAL | — |
| F2-SCR-FIN-006-YEAR-LABELS-QUERY | read · `POST /api/v1/fin/fiscal-years/search` (ADR-FIN-061), one filter `fiscalYearPk IN [the fiscalYearIds on screen]` (ADR-FIN-067) | [fiscal-years, {fiscalYearPk: ids}] | server → generic | LOCAL | — |
| F2-SCR-FIN-006-PERIOD-LABELS-QUERY | read · API-FIN-033, per year on screen: `fiscalYearId EQUALS <that year>` (the parent scope) and `fiscalPeriodPk IN [its periodIds]` (ADR-FIN-067) | [fiscal-periods, {fiscalYearId, fiscalPeriodPk: ids}] | server → generic | LOCAL | — |
| F2-SCR-FIN-006-ACCOUNT-LABELS-QUERY | read · API-FIN-001, one filter `accountPk IN [the lines' accountIds]`, no status filter — a posted line on an account deactivated since, or past the picker's cap, still shows code and name (TC-FIN-136, ADR-FIN-067) | [accounts, {accountPk: ids}] | server → generic | LOCAL | — |
| F2-SCR-FIN-006-DIMENSION-LABELS-QUERY | read · API-FIN-005 by `dimensionPk IN [the lines' dimensionIds]`, and API-FIN-008 per dimension: `dimensionId EQUALS <that dimension>` (the parent scope) and `dimensionValuePk IN [its values on the lines]`, no status filter (ADR-FIN-067) | [dimensions, {dimensionPk: ids}] / [dimension-values, {dimensionId, dimensionValuePk: ids}] | server → generic | LOCAL | — |
| F2-SCR-FIN-006-ENTRY-LINK-LABELS-QUERY | read · API-FIN-018, one filter `journalEntryPk IN [the originalEntryId and reversalEntryId on screen]` — each link shows the other entry's docNo (ADR-FIN-067) | [journal-entries, {journalEntryPk: ids}] | server → generic | LOCAL | — |
| F2-SCR-FIN-006-LOOKUP | UXD-FIN-005, UXD-FIN-006, UXD-FIN-002 | three [lookup, <key>] — long-lived | — | NONE | — |
| F2-SCR-FIN-006-SCREEN-INIT | SCR-FIN-006 | screen gate (no action-level read — Reverse renders for every holder of the screen, ADR-FIN-065) + three lookups + the fiscal-year option list; the period options load for the year chosen (RULE-FIN-017, ADR-FIN-067) | — | GLOBAL | — |
| F2-SCR-FIN-006-FACADE | SCR-FIN-006 | composes SEARCH-QUERY, READ-QUERY, CREATE/REVERSE-MUTATION, the option and picker reads, and the five LABELS reads · publishes the fiscal-year options (labelled by year code, with `hasMore`) for the manual-entry year select, and the chosen year's periods from PERIOD-OPTIONS-QUERY (scoped by `fiscalYearId EQUALS` on the server, never narrowed client-side — ADR-FIN-067); the server's FIN-400-PERIOD-NOT-IN-YEAR / FIN-400-DOCDATE-OUTSIDE-PERIOD stay the authority (ADR-FIN-065) · the list and the read view show the year, the period, every line's account and dimension value, and the reversal links by code, never as a raw id (ADR-FIN-067) · state: list, selected entry, filters, live debit/credit totals while entering (client-side echo of RULE-FIN-006, server remains authoritative) · v2: one line renderer for every journal type — an EVENT_GENERATED entry's dimensions display exactly as a manual entry's (REQ-FIN-070) |
<!-- SUB:F2-SCR-FIN-006:END -->

<!-- SUB:F2-SCR-FIN-007:START traces=SCR-FIN-007,API-FIN-023,API-FIN-024,API-FIN-025,API-FIN-026,API-FIN-027,API-FIN-033,REQ-FIN-031,REQ-FIN-034,REQ-FIN-036,REQ-FIN-037 -->
### F2-SCR-FIN-007 — Fiscal periods & years
| Hook | Kind · API | Cache key | Errors | Loading | Invalidation |
|---|---|---|---|---|---|
| F2-SCR-FIN-007-YEARS-QUERY | read · `POST /api/v1/fin/fiscal-years/search` (ADR-FIN-061) | [fiscal-years, filters] (code?, statusCode?) | server → generic | LOCAL | invalidated by CREATE-YEAR and YEAR-END-CLOSE |
| F2-SCR-FIN-007-RECORD-QUERY | read · `POST /api/v1/fin/fiscal-years/search` (ADR-FIN-061), one filter `fiscalYearPk EQUALS id`, page 0 size 1 (ADR-FIN-065) | [fiscal-years, {fiscalYearPk, page: 0, size: 1}] — under the [fiscal-years] root | zero rows → not found · server → generic | LOCAL | refreshed by CREATE-YEAR's and YEAR-END-CLOSE's invalidation of [fiscal-years] |
| F2-SCR-FIN-007-SEARCH-QUERY | read · API-FIN-033 | [fiscal-periods, filters] (fiscalYearId?, statusCode?) | server → generic | LOCAL | — |
| F2-SCR-FIN-007-CREATE-YEAR-MUTATION | mutation · API-FIN-023 | — | FIN-409-YEAR-DUP → inline on code, text: catalogue FIN-409-YEAR-DUP | LOCAL | invalidates [fiscal-periods], [fiscal-years] |
| F2-SCR-FIN-007-OPEN-MUTATION | mutation · API-FIN-024 | — | RULE-FIN-014 (FIN-409-NOT-REOPENABLE) → generic, text: catalogue FIN-409-NOT-REOPENABLE (the SRS RULE-FIN-014 message) | LOCAL | invalidates [fiscal-periods] |
| F2-SCR-FIN-007-SOFT-CLOSE-MUTATION | mutation · API-FIN-025 | — | FIN-409-INVALID-TRANSITION → generic, text: catalogue FIN-409-INVALID-TRANSITION | LOCAL | invalidates [fiscal-periods] |
| F2-SCR-FIN-007-HARD-CLOSE-MUTATION | mutation · API-FIN-026 | — | RULE-FIN-014 (FIN-409-NOT-REOPENABLE) → generic, text: catalogue FIN-409-NOT-REOPENABLE · RULE-FIN-015 → FIN-403-FORBIDDEN shown on the screen, text: catalogue FIN-403-FORBIDDEN — the separation of duty is the server's (ADR-FIN-005, ADR-FIN-065) | LOCAL | invalidates [fiscal-periods] |
| F2-SCR-FIN-007-YEAR-END-CLOSE-MUTATION | mutation · API-FIN-027 | — | FIN-409-PERIODS-NOT-CLOSED → inline on the year's close action, text: catalogue FIN-409-PERIODS-NOT-CLOSED · FIN-404-YEAR → generic, text: catalogue FIN-404-YEAR | GLOBAL (posts closing+opening entries) | invalidates [fiscal-periods], [fiscal-years], [journal-entries] |
| F2-SCR-FIN-007-LOOKUP | UXD-FIN-003, UXD-FIN-004 | two [lookup, <key>] — long-lived | — | NONE | — |
| F2-SCR-FIN-007-SCREEN-INIT | SCR-FIN-007 | screen gate (no Close-approve read — every transition renders for a holder of the screen, ADR-FIN-065) + two lookups | — | GLOBAL | — |
| F2-SCR-FIN-007-FACADE | SCR-FIN-007 | composes YEARS-QUERY, RECORD-QUERY, SEARCH-QUERY + the five mutations · state: year list (YEARS-QUERY, list page), the record page's year (RECORD-QUERY), its periods (SEARCH-QUERY scoped by `fiscalYearId`), filters · every transition and Run-year-end-close render enabled for a holder of the screen; a readiness hint shows while not every period reads HARD_CLOSE, and the server's FIN-409-PERIODS-NOT-CLOSED / FIN-409-NOT-REOPENABLE / FIN-403-FORBIDDEN decide (ADR-FIN-065) |
<!-- SUB:F2-SCR-FIN-007:END -->

<!-- SUB:F2-SCR-FIN-008:START traces=SCR-FIN-008,API-FIN-028,REQ-FIN-039,REQ-FIN-046,REQ-FIN-071 -->
### F2-SCR-FIN-008 — Account ledger
| Hook | Kind · API | Cache key | Errors | Loading | Invalidation |
|---|---|---|---|---|---|
| F2-SCR-FIN-008-REPORT-QUERY | read · API-FIN-028 | [account-ledger, accountId, fromDate, toDate, dimensionId, dimensionValueId] | FIN-404-ACCOUNT → generic, text: catalogue FIN-404-ACCOUNT | LOCAL | — |
| F2-SCR-FIN-008-ACCOUNT-OPTIONS-QUERY | read · API-FIN-001 — the required account: a picker by code/name with no status filter, since a deactivated account still has a ledger (ADR-FIN-067) | [accounts, {code/name filter, page, size}] | server → generic | LOCAL | — |
| F2-SCR-FIN-008-DIMENSION-OPTIONS-QUERY | read · API-FIN-005 — the optional dimension filter (ADR-FIN-067) | [dimensions, {page: 0, size: 200}] | server → generic | LOCAL | — |
| F2-SCR-FIN-008-VALUE-OPTIONS-QUERY | read · API-FIN-008, `dimensionId EQUALS <the chosen dimension>` — the parent scope (ADR-FIN-067) | [dimension-values, {dimensionId, page: 0, size: 200}] | server → generic | LOCAL | — |
| F2-SCR-FIN-008-LOOKUP | UXD-FIN-001, UXD-FIN-002, UXD-FIN-005 | three [lookup, <key>] — long-lived | — | NONE | — |
| F2-SCR-FIN-008-SCREEN-INIT | SCR-FIN-008 | screen gate + three lookups | — | GLOBAL | — |
| F2-SCR-FIN-008-FACADE | SCR-FIN-008 | composes REPORT-QUERY and the three option reads — the value select waits for a dimension · state: filters (incl. a drill-in's accountId, a query parameter), derived loading · each row opens its entry at `/fin/journal-entries/:id` by its journalEntryId (ADR-FIN-067) · v2: no journal-type filter is applied, so event-built and manual lines list together (REQ-FIN-071) |
<!-- SUB:F2-SCR-FIN-008:END -->

<!-- SUB:F2-SCR-FIN-009:START traces=SCR-FIN-009,API-FIN-029,REQ-FIN-040,REQ-FIN-046 -->
### F2-SCR-FIN-009 — Trial balance
| Hook | Kind · API | Cache key | Errors | Loading | Invalidation |
|---|---|---|---|---|---|
| F2-SCR-FIN-009-REPORT-QUERY | read · API-FIN-029 | [trial-balance, periodId, accountTypeCode] | FIN-404-PERIOD → generic, text: catalogue FIN-404-PERIOD | LOCAL | — |
| F2-SCR-FIN-009-YEAR-OPTIONS-QUERY | read · `POST /api/v1/fin/fiscal-years/search` (ADR-FIN-061) — the year that scopes the optional period filter (ADR-FIN-067) | [fiscal-years, {page: 0, size: 200}] | server → generic | LOCAL | — |
| F2-SCR-FIN-009-PERIOD-OPTIONS-QUERY | read · API-FIN-033, `fiscalYearId EQUALS <the chosen year>` — the parent scope (ADR-FIN-067) | [fiscal-periods, {fiscalYearId, page: 0, size: 200}] | server → generic | LOCAL | — |
| F2-SCR-FIN-009-LOOKUP | UXD-FIN-001, UXD-FIN-002 | two [lookup, <key>] — long-lived | — | NONE | — |
| F2-SCR-FIN-009-SCREEN-INIT | SCR-FIN-009 | screen gate + two lookups | — | GLOBAL | — |
| F2-SCR-FIN-009-FACADE | SCR-FIN-009 | composes REPORT-QUERY and the two option reads — the year select only scopes the period select and is never sent (API-FIN-029 publishes no year) · state: filters, derived loading |
<!-- SUB:F2-SCR-FIN-009:END -->

<!-- SUB:F2-SCR-FIN-010:START traces=SCR-FIN-010,API-FIN-030,REQ-FIN-041,REQ-FIN-046 -->
### F2-SCR-FIN-010 — Balance sheet
| Hook | Kind · API | Cache key | Errors | Loading | Invalidation |
|---|---|---|---|---|---|
| F2-SCR-FIN-010-REPORT-QUERY | read · API-FIN-030 | [balance-sheet, fiscalYearId, asOfDate] | FIN-404-YEAR → generic, text: catalogue FIN-404-YEAR | LOCAL | — |
| F2-SCR-FIN-010-YEAR-OPTIONS-QUERY | read · `POST /api/v1/fin/fiscal-years/search` (ADR-FIN-061) — the required year (ADR-FIN-067) | [fiscal-years, {page: 0, size: 200}] | server → generic | LOCAL | — |
| F2-SCR-FIN-010-LOOKUP | UXD-FIN-001, UXD-FIN-002 | two [lookup, <key>] — long-lived | — | NONE | — |
| F2-SCR-FIN-010-SCREEN-INIT | SCR-FIN-010 | screen gate + two lookups | — | GLOBAL | — |
| F2-SCR-FIN-010-FACADE | SCR-FIN-010 | composes REPORT-QUERY and YEAR-OPTIONS-QUERY · state: filters, derived loading · the fiscal-year option select discloses `hasMore` · derives each group's drill-down to SCR-FIN-009 from the group's `accountTypeCode` alone (ADR-FIN-065) |
<!-- SUB:F2-SCR-FIN-010:END -->

<!-- SUB:F2-SCR-FIN-011:START traces=SCR-FIN-011,API-FIN-031,REQ-FIN-042,REQ-FIN-046 -->
### F2-SCR-FIN-011 — Income statement
| Hook | Kind · API | Cache key | Errors | Loading | Invalidation |
|---|---|---|---|---|---|
| F2-SCR-FIN-011-REPORT-QUERY | read · API-FIN-031 | [income-statement, fiscalYearId, fromPeriodId, toPeriodId] | FIN-404-YEAR / FIN-404-PERIOD → generic, text: catalogue FIN-404-YEAR / FIN-404-PERIOD | LOCAL | — |
| F2-SCR-FIN-011-YEAR-OPTIONS-QUERY | read · `POST /api/v1/fin/fiscal-years/search` (ADR-FIN-061) — the required year (ADR-FIN-067) | [fiscal-years, {page: 0, size: 200}] | server → generic | LOCAL | — |
| F2-SCR-FIN-011-PERIOD-OPTIONS-QUERY | read · API-FIN-033, `fiscalYearId EQUALS <the chosen fiscalYearId>` — the parent scope; feeds both period bounds (ADR-FIN-067) | [fiscal-periods, {fiscalYearId, page: 0, size: 200}] | server → generic | LOCAL | — |
| F2-SCR-FIN-011-LOOKUP | UXD-FIN-001, UXD-FIN-002 | two [lookup, <key>] — long-lived | — | NONE | — |
| F2-SCR-FIN-011-SCREEN-INIT | SCR-FIN-011 | screen gate + two lookups | — | GLOBAL | — |
| F2-SCR-FIN-011-FACADE | SCR-FIN-011 | composes REPORT-QUERY · state: filters, derived loading · composes YEAR-OPTIONS-QUERY and PERIOD-OPTIONS-QUERY · sends `fromPeriodId` and `toPeriodId` each on its own when set — the api-docs publish both as independently optional with no cross-field rule, so the facade never couples them and never drops a half-specified range (ADR-FIN-065) · the fiscal-year option select discloses `hasMore` |
<!-- SUB:F2-SCR-FIN-011:END -->

<!-- SUB:F2-SCR-FIN-012:START traces=SCR-FIN-012,API-FIN-032,REQ-FIN-043,REQ-FIN-071 -->
### F2-SCR-FIN-012 — Dimension reports
| Hook | Kind · API | Cache key | Errors | Loading | Invalidation |
|---|---|---|---|---|---|
| F2-SCR-FIN-012-REPORT-QUERY | read · API-FIN-032 | [dimension-report, dimensionId, dimensionValueId, periodId] | FIN-404-DIMENSION → generic, text: catalogue FIN-404-DIMENSION | LOCAL | — |
| F2-SCR-FIN-012-DIMENSION-OPTIONS-QUERY | read · API-FIN-005 — the required dimension, with no status filter: a dimension deactivated since still reports (ADR-FIN-067) | [dimensions, {page: 0, size: 200}] | server → generic | LOCAL | — |
| F2-SCR-FIN-012-VALUE-OPTIONS-QUERY | read · API-FIN-008, `dimensionId EQUALS <the chosen dimension>` — the parent scope (ADR-FIN-067) | [dimension-values, {dimensionId, page: 0, size: 200}] | server → generic | LOCAL | — |
| F2-SCR-FIN-012-YEAR-OPTIONS-QUERY | read · `POST /api/v1/fin/fiscal-years/search` (ADR-FIN-061) — the year that scopes the optional period filter (ADR-FIN-067) | [fiscal-years, {page: 0, size: 200}] | server → generic | LOCAL | — |
| F2-SCR-FIN-012-PERIOD-OPTIONS-QUERY | read · API-FIN-033, `fiscalYearId EQUALS <the chosen year>` — the parent scope (ADR-FIN-067) | [fiscal-periods, {fiscalYearId, page: 0, size: 200}] | server → generic | LOCAL | — |
| F2-SCR-FIN-012-LOOKUP | UXD-FIN-002 | [lookup, DEBIT_CREDIT] — long-lived | — | NONE | — |
| F2-SCR-FIN-012-SCREEN-INIT | SCR-FIN-012 | screen gate + the lookup | — | GLOBAL | — |
| F2-SCR-FIN-012-FACADE | SCR-FIN-012 | composes REPORT-QUERY and the four option reads — the value select waits for the dimension, the period select for a year, and the year is never sent (API-FIN-032 publishes none) · state: filters, derived loading |
<!-- SUB:F2-SCR-FIN-012:END -->

<!-- SUB:F2-SCR-FIN-013:START traces=SCR-FIN-013,API-FIN-001,API-FIN-038,API-FIN-039,API-FIN-040,API-FIN-041,REQ-FIN-054,REQ-FIN-055,REQ-FIN-056,REQ-FIN-057,REQ-FIN-058,REQ-FIN-059,REQ-FIN-060,REQ-FIN-061,UXD-FIN-007,UXD-FIN-013,UXD-FIN-015 -->
### F2-SCR-FIN-013 — Account mappings
| Hook | Kind · API | Cache key | Errors | Loading | Invalidation |
|---|---|---|---|---|---|
| F2-SCR-FIN-013-SEARCH-QUERY | read · API-FIN-038 | [account-mappings, filters] (eventTypeCode, businessFieldCode, businessValue, accountId, isActiveFl + page/size) | FIN-400-INVALID-SORT → generic · server → generic | LOCAL | — |
| F2-SCR-FIN-013-CREATE-MUTATION | mutation · API-FIN-039 | — | RULE-FIN-022 → inline on the three key fields (FIN-409-MAPPING-DUP), text: catalogue FIN-409-MAPPING-DUP (the SRS RULE-FIN-022 message) · RULE-FIN-023 → inline on accountId (FIN-422-MAPPED-ACCOUNT-INELIGIBLE), text: catalogue FIN-422-MAPPED-ACCOUNT-INELIGIBLE (the SRS RULE-FIN-023 message) · RULE-FIN-024 → inline on businessValue (FIN-422-INVALID-PAYMENT-METHOD), text: catalogue FIN-422-INVALID-PAYMENT-METHOD (the SRS RULE-FIN-024 message) · FIN-400-INVALID-LOOKUP → inline, text: catalogue FIN-400-INVALID-LOOKUP · FIN-404-ACCOUNT → inline on accountId, text: catalogue FIN-404-ACCOUNT | LOCAL | invalidates [account-mappings] |
| F2-SCR-FIN-013-CHANGE-ACCOUNT-MUTATION | mutation · API-FIN-040 — the account only | — | RULE-FIN-023 → inline on accountId (FIN-422-MAPPED-ACCOUNT-INELIGIBLE), text: catalogue FIN-422-MAPPED-ACCOUNT-INELIGIBLE (the SRS RULE-FIN-023 message) · FIN-404-ACCOUNT → inline on accountId, text: catalogue FIN-404-ACCOUNT · FIN-404-ACCOUNT-MAPPING → generic, text: catalogue FIN-404-ACCOUNT-MAPPING | LOCAL | invalidates [account-mappings] |
| F2-SCR-FIN-013-DEACTIVATE-MUTATION | mutation · API-FIN-041 | — | FIN-404-ACCOUNT-MAPPING → generic, text: catalogue FIN-404-ACCOUNT-MAPPING | LOCAL | invalidates [account-mappings] |
| F2-SCR-FIN-013-ACCOUNT-PICKER-QUERY | read · API-FIN-001 | [accounts, {isLeafFl: true, isActiveFl: true, code/name filter, page, size}] | server → generic | LOCAL | — |
| F2-SCR-FIN-013-LOOKUP | UXD-FIN-007, UXD-FIN-013, UXD-FIN-015 | three [lookup, <key>] — long-lived; UXD-FIN-015 feeds the businessValue select only while businessFieldCode=PAYMENT_METHOD | — | NONE | — |
| F2-SCR-FIN-013-SCREEN-INIT | SCR-FIN-013 | screen gate (page code in the SEC menu, ADR-FIN-005) + three lookups; filters seeded from the route search params (a drill-in from SCR-FIN-003 arrives with eventTypeCode and businessFieldCode) | — | GLOBAL | — |
| F2-SCR-FIN-013-FACADE | SCR-FIN-013 | composes SEARCH-QUERY, the three mutations and ACCOUNT-PICKER-QUERY · state: list, selection, filters (incl. page/size, all in the key), derived loading · operations: create; changeAccount (sends the account alone, never the fixed key — ADR-FIN-024); deactivate (the confirmation carries the REQ-FIN-057/058 copy; no usage-check endpoint exists, so the confirmation is the check) · no mutation invalidates [journal-entries] — a mapping change never alters a posted entry (REQ-FIN-058) · the account cell's drill-in to SCR-FIN-001 seeds `accountPk EQUALS` the row's accountId (ADR-FIN-067) |
<!-- SUB:F2-SCR-FIN-013:END -->
<!-- PHASE:F2:END -->

<!-- PHASE:F3:START traces=SCR-FIN-001,SCR-FIN-002,SCR-FIN-003,SCR-FIN-004,SCR-FIN-005,SCR-FIN-006,SCR-FIN-007,SCR-FIN-008,SCR-FIN-009,SCR-FIN-010,SCR-FIN-011,SCR-FIN-012,SCR-FIN-013 -->
## PHASE F3 — Forms & Validators

No content role in `factory.yaml` names this phase (§3.2), so it is filled per the profile's own convention:
- one `zod` schema per entry or filter form;
- field constraints bound to the published request DTO (`_inputs/api-docs-fin.md`);
- a business rule that a form can usefully pre-empt is named against its `RULE-FIN-*`;
- everything else is left to the server.

<!-- SUB:F3-SCR-FIN-001:START traces=SCR-FIN-001,REQ-FIN-001,REQ-FIN-002,RULE-FIN-001 -->
### F3-SCR-FIN-001 — Chart of accounts
`accountFormSchema` : code (required, ≤30) · nameAr/nameEn (required, ≤200) · accountTypeCode/natureCode (required, member of the runtime-loaded UXD-FIN-001/002 option list) · parentAccountId (optional) · isLeafFl (boolean; disabled and forced false client-side once the account has ≥1 child in the loaded tree — a UX pre-empt of RULE-FIN-001, server remains authoritative)
<!-- SUB:F3-SCR-FIN-001:END -->

<!-- SUB:F3-SCR-FIN-002:START traces=SCR-FIN-002,REQ-FIN-004,REQ-FIN-005,RULE-FIN-002 -->
### F3-SCR-FIN-002 — Dimensions
`dimensionFormSchema` : code (required, ≤30) · nameAr/nameEn (required, ≤150)
`dimensionValueFormSchema` : code (required; client-side uniqueness pre-check against the loaded values of the record page's dimension — UX pre-empt of RULE-FIN-002) · nameAr/nameEn (required) · sortOrder (required, integer)
<!-- SUB:F3-SCR-FIN-002:END -->

<!-- SUB:F3-SCR-FIN-003:START traces=SCR-FIN-003,API-FIN-010,API-FIN-011,REQ-FIN-007,REQ-FIN-008,REQ-FIN-009,REQ-FIN-048,REQ-FIN-049,REQ-FIN-062,REQ-FIN-064,REQ-FIN-065,RULE-FIN-003,RULE-FIN-018,RULE-FIN-019,RULE-FIN-025,RULE-FIN-026,UXD-FIN-013,UXD-FIN-014 -->
### F3-SCR-FIN-003 — Engine rules
`eventTypeRuleFormSchema` : eventTypeCode (required, ≤50, member of UXD-FIN-007) · nameAr/nameEn (required, ≤150)
`ruleLineFormSchema` — one line together with its tags, one submit (ADR-FIN-062), mapped one to one onto the published `RuleLineCreateRequest` of API-FIN-011:
- **accountDerivationTypeCode.** Required, ≤20, and a member of UXD-FIN-008 as loaded. MDL holds only CONSTANT and MAPPING, so DIRECT is never offered and nothing is hardcoded (RULE-FIN-018).
- **accountDerivationValue.** Required when CONSTANT and empty when MAPPING. It is filled by the account picker, which returns the chosen leaf account's `code` (RULE-FIN-019 pre-empt).
- **accountBusinessFieldCode.** Required when MAPPING and empty when CONSTANT. ≤50, a member of UXD-FIN-013 (RULE-FIN-019 pre-empt).
- **amountSourceTypeCode.** Required, ≤20, a member of UXD-FIN-009.
- **amountSourceValue.** Required unless amountSourceTypeCode=REMAINDER.
- **directionCode.** Required, ≤10, a member of UXD-FIN-002.
- **distributionTypeCode.** Required, ≤15, a member of UXD-FIN-010.
- **isRemainderFl.** Boolean. Cross-field: at most one line of the rule, counting the loaded lines and this one, may carry isRemainderFl=true. That pre-empts a second remainder line under RULE-FIN-003. "Exactly one when any line is PERCENTAGE" stays server-checked, because lines arrive one at a time.
- **dimensionTags.** The list the tag editor returns, sent as the request's `dimensionTags` array.

`dimensionTagFormSchema`, one per tag, mapped onto the published `RuleLineDimensionTagRequest`:
- **dimensionId.** Required, and unique across the line's tags (RULE-FIN-025 pre-empt). The picker offers active dimensions.
- **valueSourceCode.** Required, ≤50, a member of UXD-FIN-014.
- **dimensionValueId.** Required when CONSTANT and empty when BUSINESS_FIELD. The picker is scoped to the tag's dimensionId and to active values (RULE-FIN-026 pre-empt).
- **businessFieldCode.** Required when BUSINESS_FIELD and empty when CONSTANT. ≤50, a member of UXD-FIN-013 (RULE-FIN-026 pre-empt).
<!-- SUB:F3-SCR-FIN-003:END -->

<!-- SUB:F3-SCR-FIN-004:START traces=SCR-FIN-004,REQ-FIN-022,RULE-FIN-006 -->
### F3-SCR-FIN-004 — Recurring/reversing templates
`recurringTemplateFormSchema` : nameAr/nameEn (required, ≤150) · scheduleTypeCode (required, member of UXD-FIN-011) · frequencyCode (required only when scheduleTypeCode=RECURRING, member of UXD-FIN-012 — cross-field, pre-empts FIN-400-MISSING-FREQUENCY) · startDate (required) · endDate (optional)
`recurringTemplateLineFormSchema` : accountId (required) · amount (required, > 0) · directionCode (required, member of UXD-FIN-002) · dimensionValueId (optional) — the set's debit=credit balance (RULE-FIN-006 reused) is checked before submit as a UX pre-empt; the server re-checks at run time
<!-- SUB:F3-SCR-FIN-004:END -->

<!-- SUB:F3-SCR-FIN-005:START traces=SCR-FIN-005,REQ-FIN-025,RULE-FIN-003 -->
### F3-SCR-FIN-005 — Allocation rules
`allocationRuleFormSchema` : nameAr/nameEn (required, ≤150) · sourceAccountId (required)
`allocationTargetFormSchema` : targetAccountId (required) · dimensionValueId (optional) · distributionTypeCode (required, member of UXD-FIN-010) · distributionValue (required unless distributionTypeCode=REMAINDER) · isRemainderFl (boolean) — cross-field across the whole target set before submit: exactly one target carries isRemainderFl=true when any target is PERCENTAGE (RULE-FIN-003; the set is submitted in one create call, so the whole set is known)
<!-- SUB:F3-SCR-FIN-005:END -->

<!-- SUB:F3-SCR-FIN-006:START traces=SCR-FIN-006,REQ-FIN-014,REQ-FIN-017,REQ-FIN-018,RULE-FIN-006,RULE-FIN-017 -->
### F3-SCR-FIN-006 — Journal entries
`manualEntryFormSchema` : docDate (required) · fiscalYearId (required; a member of the fiscal-year options the facade publishes — ADR-FIN-065) · periodId (required; a member of the chosen year's periods, read with `fiscalYearId EQUALS` the chosen year — UX pre-empt of RULE-FIN-017, ADR-FIN-067) · journalTypeCode (fixed `MANUAL`, not a validated input field — ADR-FIN-008) · descriptionAr/descriptionEn (optional) · lines (min 2 — a balanced entry needs at least a debit and a credit side)
`journalLineFormSchema` : accountId (required) · amount (required, > 0) · directionCode (required, member of UXD-FIN-002) · dimensions[] (dimensionId+dimensionValueId pairs, optional) · descriptionAr/descriptionEn (optional) — cross-field: Σdebit = Σcredit checked across `lines` before submit (UX pre-empt of RULE-FIN-006), server remains authoritative
`searchFilterSchema` : docNo (LIKE) · docDate range · periodId · statusCode (member of UXD-FIN-006) · journalTypeCode (member of UXD-FIN-005)
<!-- SUB:F3-SCR-FIN-006:END -->

<!-- SUB:F3-SCR-FIN-007:START traces=SCR-FIN-007,REQ-FIN-031,REQ-FIN-034 -->
### F3-SCR-FIN-007 — Fiscal periods & years
`fiscalYearFormSchema` : code (required, ≤10) · startDate/endDate (required, endDate > startDate) · periodCount (required, positive integer)
Row actions (open/soft-close/hard-close/year-end-close) take no form input beyond the row id — the affordance itself is the "form".
<!-- SUB:F3-SCR-FIN-007:END -->

<!-- SUB:F3-SCR-FIN-008:START traces=SCR-FIN-008,REQ-FIN-039 -->
### F3-SCR-FIN-008 — Account ledger
`ledgerFilterSchema` : accountId (required) · fromDate/toDate (optional range) · dimensionId/dimensionValueId (optional pair — dimensionValueId requires dimensionId)
<!-- SUB:F3-SCR-FIN-008:END -->

<!-- SUB:F3-SCR-FIN-009:START traces=SCR-FIN-009,REQ-FIN-040 -->
### F3-SCR-FIN-009 — Trial balance
`trialBalanceFilterSchema` : periodId (optional) · accountTypeCode (optional, member of UXD-FIN-001)
<!-- SUB:F3-SCR-FIN-009:END -->

<!-- SUB:F3-SCR-FIN-010:START traces=SCR-FIN-010,REQ-FIN-041 -->
### F3-SCR-FIN-010 — Balance sheet
`balanceSheetFilterSchema` : fiscalYearId (required) · asOfDate (optional)
<!-- SUB:F3-SCR-FIN-010:END -->

<!-- SUB:F3-SCR-FIN-011:START traces=SCR-FIN-011,REQ-FIN-042 -->
### F3-SCR-FIN-011 — Income statement
`incomeStatementFilterSchema` : fiscalYearId (required) · fromPeriodId (optional) · toPeriodId (optional) — each independently optional, no cross-field rule, as the api-docs publish them (ADR-FIN-065)
<!-- SUB:F3-SCR-FIN-011:END -->

<!-- SUB:F3-SCR-FIN-012:START traces=SCR-FIN-012,REQ-FIN-043 -->
### F3-SCR-FIN-012 — Dimension reports
`dimensionReportFilterSchema` : dimensionId (required) · dimensionValueId (optional) · periodId (optional)
<!-- SUB:F3-SCR-FIN-012:END -->

<!-- SUB:F3-SCR-FIN-013:START traces=SCR-FIN-013,API-FIN-038,API-FIN-039,API-FIN-040,REQ-FIN-054,REQ-FIN-055,REQ-FIN-056,REQ-FIN-059,REQ-FIN-060,REQ-FIN-061,RULE-FIN-022,RULE-FIN-023,RULE-FIN-024,UXD-FIN-007,UXD-FIN-013,UXD-FIN-015 -->
### F3-SCR-FIN-013 — Account mappings
`accountMappingFormSchema` (new mapping), bound to the published `AccountMappingCreateRequest` of API-FIN-039:
- **eventTypeCode.** Required, ≤50, a member of UXD-FIN-007.
- **businessFieldCode.** Required, ≤50, a member of UXD-FIN-013.
- **businessValue.** Required, ≤100, with leading and trailing blanks trimmed before submit (ADR-FIN-039); blank after trimming is refused. When businessFieldCode=PAYMENT_METHOD it must be a member of UXD-FIN-015 (RULE-FIN-024 pre-empt). Otherwise it is free text, because the host's code is stored as-is.
- **accountId.** Required, set only through the active-leaf account picker (RULE-FIN-023 pre-empt).

The one-active-mapping-per-key rule, RULE-FIN-022, is not pre-empted client-side, because the loaded page may
not hold the conflicting row. The server's FIN-409-MAPPING-DUP routes inline to the three key fields.

`changeAccountFormSchema` (existing mapping), bound to `AccountMappingUpdateRequest` of API-FIN-040: accountId
only, required and picked as above. The three key fields render read-only and are never submitted (ADR-FIN-024).

`accountMappingFilterSchema` (API-FIN-038): eventTypeCode (member of UXD-FIN-007) · businessFieldCode (member
of UXD-FIN-013) · businessValue (LIKE) · accountId · isActiveFl — all optional, each one a filter field the
published search request supports.
<!-- SUB:F3-SCR-FIN-013:END -->
<!-- PHASE:F3:END -->

<!-- PHASE:F4:START traces=SCR-FIN-001,SCR-FIN-002,SCR-FIN-003,SCR-FIN-004,SCR-FIN-005,SCR-FIN-006,SCR-FIN-007,SCR-FIN-008,SCR-FIN-009,SCR-FIN-010,SCR-FIN-011,SCR-FIN-012,SCR-FIN-013 -->
## PHASE F4 — Screens & Routes

**RF4 — Screens & routes.** Every route element is guarded by its page code being present in the effective
menu that the security module serves (ADR-FIN-005), never by a composed permission string read client-side.
No route is drawn for an operation with no published endpoint (ADR-FIN-006). No action affordance is gated on
the caller's action-level permissions: every affordance renders for a holder of the screen, and FIN-403-FORBIDDEN
is the authority (ADR-FIN-005, ADR-FIN-065).

**Record pages (ADR-FIN-065, erp-drawer-zustand §11).** SCR-FIN-002, 003, 004, 005 and 007 own a child
collection, and each has three routes:
- a list page;
- `/new`, registered before `/:id`, so that `/new` is never read as an id;
- a `/:id` record page, with the header in a collapsible section and the child collection beneath it.

The child line's add Drawer is on its own search param. After a save, `/new` moves to `/:id`, using the id of
the create response. The record page reads through the screen's RECORD-QUERY, so a cold deep link resolves. An
unresolvable id renders a "not found" notice with a way back.

Every second-level view is its own component and a sibling of the first level. It is opened from
navigation state (a route search param), never from a boolean the screen holds. Its value returns to the
screen's form state.

<!-- SUB:F4-SCR-FIN-001:START traces=SCR-FIN-001,REQ-FIN-001,REQ-FIN-002,REQ-FIN-003,API-FIN-001,API-FIN-002,API-FIN-003,API-FIN-004 -->
### F4-SCR-FIN-001-SCREEN — Chart of accounts
Routes       : `/fin/accounts` (search + inline entry — Composite screen, no `:id` route: entry hydrates from the search row, ADR-FIN-006); filter state in route search params, so a drill-in from SCR-FIN-013 lands filtered by `accountPk EQUALS` the mapped account (ADR-FIN-067); the typed code stays a free-text filter
Guard        : `FIN_ACCOUNTS` present in the effective menu (ADR-FIN-005)
Facade       : F2-SCR-FIN-001-FACADE · page never calls the queries/mutations directly
Cross-module : UXD-FIN-001, UXD-FIN-002 (lookup labels only) — no foreign business data displayed
Composition  : parent-account picker — its own component, a sibling of the Entry, opened from `?picker=parent`, returning the chosen parent to the Entry's form state
Saves        : ONE — create (API-FIN-002) or update (API-FIN-003), a single call each; Deactivate (API-FIN-004) is a confirmed action with its own single call, never on the form
<!-- SUB:F4-SCR-FIN-001:END -->

<!-- SUB:F4-SCR-FIN-002:START traces=SCR-FIN-002,REQ-FIN-004,REQ-FIN-005,REQ-FIN-006,API-FIN-005,API-FIN-006,API-FIN-007,API-FIN-008,API-FIN-035 -->
### F4-SCR-FIN-002-SCREEN — Dimensions
Routes       : `/fin/dimensions` (list) · `/fin/dimensions/new` (header; values locked with the reason stated) · `/fin/dimensions/:dimensionId` (record page: header in a collapsible section, values beneath; hydrated by F2-SCR-FIN-002-RECORD-QUERY — ADR-FIN-065) · navigation state: `?valueAction=create` (Value entry Drawer)
Guard        : `FIN_DIMENSIONS` present in the effective menu
Facade       : F2-SCR-FIN-002-FACADE
Cross-module : none
Composition  : none — neither form has secondary detail; the values collection is inline beneath the header on the record page
Saves        : ONE per form — create dimension (API-FIN-006, the header section's footer), create value (API-FIN-007, the Value entry Drawer); value deactivate (API-FIN-035) is a confirmed row action
<!-- SUB:F4-SCR-FIN-002:END -->

<!-- SUB:F4-SCR-FIN-003:START traces=SCR-FIN-003,REQ-FIN-007,REQ-FIN-008,REQ-FIN-009,REQ-FIN-049,REQ-FIN-062,API-FIN-009,API-FIN-010,API-FIN-011,API-FIN-034,UXD-FIN-002,UXD-FIN-007,UXD-FIN-008,UXD-FIN-009,UXD-FIN-010,UXD-FIN-013,UXD-FIN-014 -->
### F4-SCR-FIN-003-SCREEN — Engine rules
Routes       : `/fin/rules` (list) · `/fin/rules/new` (the rule header, saved alone; lines locked with the reason stated) · `/fin/rules/:ruleId` (record page: rule header read-only in a collapsible section, lines beneath; hydrated by F2-SCR-FIN-003-RECORD-QUERY — ADR-FIN-065; no line-delete affordance — ADR-FIN-006) · navigation state: `?lineAction=create` (Line entry Drawer), `?picker=tags` / `?picker=account` / `?picker=dimension-value` (second and third levels)
Guard        : `FIN_RULES` present in the effective menu; New rule, Add line and Deactivate render for every holder of the screen — FIN-403-FORBIDDEN is the authority (ADR-FIN-005, ADR-FIN-065)
Facade       : F2-SCR-FIN-003-FACADE
Cross-module : UXD-FIN-007, UXD-FIN-008, UXD-FIN-009, UXD-FIN-002, UXD-FIN-010, UXD-FIN-013, UXD-FIN-014
Composition  : rule Entry — no secondary detail · Line entry — a tags summary row (count + capped preview + edit trigger) whose tag editor is its own component, a sibling of the Line entry, returning the tag list · the constant-account picker (sibling of the Line entry) and the constant dimension-value picker (sibling of the tag editor) each return one value · a MAPPING line row shows a "mappings" link to `/fin/account-mappings?eventTypeCode=…&businessFieldCode=…` (SCR-FIN-013)
Saves        : ONE per sub-view — the rule Entry calls API-FIN-010 alone; the Line entry calls API-FIN-011 once with the line and its `dimensionTags`, whether the line is CONSTANT or MAPPING; the two are never chained (ADR-FIN-062). Deactivate (API-FIN-034) is a confirmed row action with its own single call
<!-- SUB:F4-SCR-FIN-003:END -->

<!-- SUB:F4-SCR-FIN-004:START traces=SCR-FIN-004,REQ-FIN-022,REQ-FIN-023,REQ-FIN-024,API-FIN-012,API-FIN-013,API-FIN-014,API-FIN-036 -->
### F4-SCR-FIN-004-SCREEN — Recurring/reversing templates
Routes       : `/fin/recurring-templates` (list) · `/fin/recurring-templates/new` (header + the lines composed before the one create) · `/fin/recurring-templates/:templateId` (record page: header and lines read-only, Run and Deactivate; hydrated by F2-SCR-FIN-004-RECORD-QUERY — ADR-FIN-065; no Edit affordance — update not published, ADR-FIN-006) · navigation state: `?lineAction=create` (Line entry Drawer on New), `?picker=account` / `?picker=dimensionValue`
Guard        : `FIN_RECURRING_TEMPLATES` present in the effective menu
Facade       : F2-SCR-FIN-004-FACADE
Cross-module : UXD-FIN-011, UXD-FIN-012, UXD-FIN-002
Composition  : line collection inline beneath the header, scrolling with the page body; on New one line is composed in the Line entry Drawer and returns to the line set · account and dimension-value pickers — sibling components opened from `?picker=…`, returning one value to the line being edited
Saves        : ONE — create (API-FIN-013), the template with its lines in one call; Run (API-FIN-014) and Deactivate (API-FIN-036) are confirmed row actions
<!-- SUB:F4-SCR-FIN-004:END -->

<!-- SUB:F4-SCR-FIN-005:START traces=SCR-FIN-005,REQ-FIN-025,REQ-FIN-026,API-FIN-015,API-FIN-016,API-FIN-017,API-FIN-037 -->
### F4-SCR-FIN-005-SCREEN — Allocation rules
Routes       : `/fin/allocation-rules` (list) · `/fin/allocation-rules/new` (header + the targets composed before the one create) · `/fin/allocation-rules/:allocationRuleId` (record page: header and targets read-only, Run and Deactivate; hydrated by F2-SCR-FIN-005-RECORD-QUERY — ADR-FIN-065; no Edit affordance — update not published, ADR-FIN-006) · navigation state: `?targetAction=create` (Target entry Drawer on New), `?picker=sourceAccount` / `?picker=targetAccount` / `?picker=dimensionValue`
Guard        : `FIN_ALLOCATION_RULES` present in the effective menu
Facade       : F2-SCR-FIN-005-FACADE
Cross-module : UXD-FIN-010
Composition  : target collection inline beneath the header, scrolling with the page body; on New one target is composed in the Target entry Drawer (a sibling component opened from `?targetAction=create`) and returns to the target set; the account and dimension-value pickers open from `?picker=…` the same way
Saves        : ONE — create (API-FIN-016), the rule with its targets in one call; Run (API-FIN-017) and Deactivate (API-FIN-037) are confirmed row actions
<!-- SUB:F4-SCR-FIN-005:END -->

<!-- SUB:F4-SCR-FIN-006:START traces=SCR-FIN-006,REQ-FIN-014,REQ-FIN-016,REQ-FIN-017,REQ-FIN-027,REQ-FIN-028,REQ-FIN-070,REQ-FIN-077,API-FIN-018,API-FIN-019,API-FIN-021,API-FIN-022 -->
### F4-SCR-FIN-006-SCREEN — Journal entries
Routes       : `/fin/journal-entries` (search) · `/fin/journal-entries/new` (manual entry) · `/fin/journal-entries/:id` (read + Reverse action on a POSTED row, for every journal type — event-built entries included, REQ-FIN-077)
Guard        : `FIN_JOURNAL_ENTRIES` present in the effective menu; `/new` and Reverse render for every holder of the screen — no endpoint publishes CREATE or `PERM_FIN_JOURNAL_ENTRIES_REVERSE` to the client, and FIN-403-FORBIDDEN is the authority (ADR-FIN-005, ADR-FIN-065)
Facade       : F2-SCR-FIN-006-FACADE
Cross-module : UXD-FIN-005, UXD-FIN-006, UXD-FIN-002
Composition  : `/new` is a record PAGE (CONTAINER CLAUSE, erp-drawer-zustand §11, ADR-FIN-067): the header form sits inline — its year select fed by the facade's fiscal-year options and its period select by PERIOD-OPTIONS-QUERY, the chosen year's periods (ADR-FIN-065, ADR-FIN-067) · the lines pane sits beneath the header, scrolling with the page, its footer carrying the line count and running debit/credit totals; it is the refusal slot where a not-postable-account or invalid-dimension refusal shows once · one line is composed in a one-line Drawer, a sibling component opened from `?lineAction=create`, returning that line to the pane and never posting; the account and dimension-value pickers inside it open from `?picker=…` · the read view `/:id` renders the same lines pane, read-only, for every journal type (REQ-FIN-070)
Saves        : ONE — Post on `/new` calls API-FIN-019 once; Reverse on `/:id` calls API-FIN-021 once after a confirmation, never from the entry form
<!-- SUB:F4-SCR-FIN-006:END -->

<!-- SUB:F4-SCR-FIN-007:START traces=SCR-FIN-007,REQ-FIN-031,REQ-FIN-034,REQ-FIN-036,REQ-FIN-037,API-FIN-023,API-FIN-024,API-FIN-025,API-FIN-026,API-FIN-027,API-FIN-033 -->
### F4-SCR-FIN-007-SCREEN — Fiscal periods & years
Routes       : `/fin/periods` (list of years from the published fiscal-year search, cited by path — ADR-FIN-061) · `/fin/periods/new` (create year) · `/fin/periods/:fiscalYearId` (record page: year header in a collapsible section, its periods from API-FIN-033 beneath with their row actions, and the year-end close; hydrated by F2-SCR-FIN-007-RECORD-QUERY — ADR-FIN-065)
Guard        : `FIN_PERIODS` present in the effective menu; every transition, hard-close and year-end-close render for every holder of the screen — no endpoint publishes the Close-approve grant to the client, and the server's FIN-403-FORBIDDEN enforces RULE-FIN-015 (ADR-FIN-005, ADR-FIN-065)
Facade       : F2-SCR-FIN-007-FACADE
Cross-module : UXD-FIN-003, UXD-FIN-004
Composition  : none — the create-year form has no secondary detail; the periods are listed inline beneath the year header
Saves        : ONE — create year (API-FIN-023); each period transition and the year-end close is a confirmed row action with its own single call
<!-- SUB:F4-SCR-FIN-007:END -->

<!-- SUB:F4-SCR-FIN-008:START traces=SCR-FIN-008,REQ-FIN-039,REQ-FIN-046,REQ-FIN-071,API-FIN-028 -->
### F4-SCR-FIN-008-SCREEN — Account ledger
Routes       : `/fin/reports/account-ledger` (FULL_PAGE, no entry sub-view — ADR-FIN-003); filter state mirrored into route search params so a filtered report and a drill-in from another screen are both linkable · each row opens its entry at `/fin/journal-entries/:id` by its journalEntryId (ADR-FIN-067)
Guard        : `FIN_ACCOUNT_LEDGER` present in the effective menu
Facade       : F2-SCR-FIN-008-FACADE
Cross-module : UXD-FIN-001, UXD-FIN-002, UXD-FIN-005
Composition  : none — a read-only report
Saves        : none — a filter change re-runs the query
<!-- SUB:F4-SCR-FIN-008:END -->

<!-- SUB:F4-SCR-FIN-009:START traces=SCR-FIN-009,REQ-FIN-040,REQ-FIN-046,API-FIN-029 -->
### F4-SCR-FIN-009-SCREEN — Trial balance
Routes       : `/fin/reports/trial-balance` (FULL_PAGE, no entry sub-view — ADR-FIN-003); filter state in route search params
Guard        : `FIN_TRIAL_BALANCE` present in the effective menu
Facade       : F2-SCR-FIN-009-FACADE
Cross-module : UXD-FIN-001, UXD-FIN-002
Composition  : none — a read-only report
Saves        : none
<!-- SUB:F4-SCR-FIN-009:END -->

<!-- SUB:F4-SCR-FIN-010:START traces=SCR-FIN-010,REQ-FIN-041,REQ-FIN-046,API-FIN-030 -->
### F4-SCR-FIN-010-SCREEN — Balance sheet
Routes       : `/fin/reports/balance-sheet` (FULL_PAGE, no entry sub-view — ADR-FIN-003); filter state in route search params · a group's drill-down navigates to `/fin/reports/trial-balance?accountTypeCode=<the group's accountTypeCode>` — the one carrier API-FIN-029 publishes that the statement holds (ADR-FIN-065)
Guard        : `FIN_BALANCE_SHEET` present in the effective menu
Facade       : F2-SCR-FIN-010-FACADE
Cross-module : UXD-FIN-001, UXD-FIN-002
Composition  : none — a read-only report
Saves        : none
<!-- SUB:F4-SCR-FIN-010:END -->

<!-- SUB:F4-SCR-FIN-011:START traces=SCR-FIN-011,REQ-FIN-042,REQ-FIN-046,API-FIN-031 -->
### F4-SCR-FIN-011-SCREEN — Income statement
Routes       : `/fin/reports/income-statement` (FULL_PAGE, no entry sub-view — ADR-FIN-003); filter state in route search params, each period bound written on its own · a group's drill-down navigates to `/fin/reports/trial-balance?accountTypeCode=<the group's accountTypeCode>` (ADR-FIN-065)
Guard        : `FIN_INCOME_STATEMENT` present in the effective menu
Facade       : F2-SCR-FIN-011-FACADE
Cross-module : UXD-FIN-001, UXD-FIN-002
Composition  : none — a read-only report
Saves        : none
<!-- SUB:F4-SCR-FIN-011:END -->

<!-- SUB:F4-SCR-FIN-012:START traces=SCR-FIN-012,REQ-FIN-043,REQ-FIN-071,API-FIN-032 -->
### F4-SCR-FIN-012-SCREEN — Dimension reports
Routes       : `/fin/reports/dimension` (FULL_PAGE, no entry sub-view — ADR-FIN-003); filter state in route search params
Guard        : `FIN_DIMENSION_REPORTS` present in the effective menu
Facade       : F2-SCR-FIN-012-FACADE
Cross-module : UXD-FIN-002
Composition  : none — a read-only report
Saves        : none
<!-- SUB:F4-SCR-FIN-012:END -->

<!-- SUB:F4-SCR-FIN-013:START traces=SCR-FIN-013,REQ-FIN-054,REQ-FIN-055,REQ-FIN-056,REQ-FIN-057,REQ-FIN-058,API-FIN-038,API-FIN-039,API-FIN-040,API-FIN-041,UXD-FIN-007,UXD-FIN-013,UXD-FIN-015 -->
### F4-SCR-FIN-013-SCREEN — Account mappings
Routes       : `/fin/account-mappings` (search + entry, Composite screen, one lazy chunk; no `:id` route — the SRS names no by-id read, the Entry hydrates from the search row) · navigation state: filters in route search params (a drill-in from SCR-FIN-003 lands filtered), `?entry=new` / `?entry=<row id>`, `?picker=account`
Guard        : `FIN_ACCOUNT_MAPPINGS` present in the effective menu (ADR-FIN-005); New, Save on an existing mapping and Deactivate render for every holder of the screen — FIN-403-FORBIDDEN is the authority (ADR-FIN-065); there is no Delete affordance
Facade       : F2-SCR-FIN-013-FACADE
Cross-module : UXD-FIN-007, UXD-FIN-013, UXD-FIN-015
Composition  : the account picker is its own component, a sibling of the Entry, opened from `?picker=account`, returning one active leaf account to the Entry's form state · businessValue renders as the UXD-FIN-015 select while businessFieldCode=PAYMENT_METHOD and as a text input otherwise · the account cell is a drill-in to SCR-FIN-001 that seeds `accountPk EQUALS` the row's accountId (`/fin/accounts?accountPk=<accountId>`, ADR-FIN-067)
Saves        : ONE — create (API-FIN-039) for a new mapping, change-account (API-FIN-040) for an existing one, a single call each; Deactivate (API-FIN-041) is a confirmed row action with its own single call; the list reads API-FIN-038
<!-- SUB:F4-SCR-FIN-013:END -->
<!-- PHASE:F4:END -->

<!-- PHASE:SEC-FE:START traces=SCR-FIN-001,SCR-FIN-002,SCR-FIN-003,SCR-FIN-004,SCR-FIN-005,SCR-FIN-006,SCR-FIN-007,SCR-FIN-008,SCR-FIN-009,SCR-FIN-010,SCR-FIN-011,SCR-FIN-012,SCR-FIN-013,RULE-FIN-015 -->
## PHASE SEC-FE — Security (frontend half)

**RF5 — Security.** For each `SCR-*`:
- **Navigation guard.** Without the screen's page code in the SEC effective menu, the user is redirected to
  unauthorized.
- **Action affordances.** No action is gated client-side (ADR-FIN-005, ADR-FIN-065). No published endpoint
  carries the caller's CREATE, UPDATE or custom grants: the SEC effective menu (ADR-FIN-005) carries page codes only. So every affordance
  renders for a caller who holds the screen. A client-side "does this caller hold CREATE" test would have to
  invent the state it reads.
- **Forbidden response.** `FIN-403-FORBIDDEN` is the authority on every write. It is shown as the localized
  catalogue message on the screen, never as an empty result.

Permission names are the backend/SEC registry's own and are never redeclared here (ADR-FIN-005).

| Screen | Navigation guard | Action affordances |
|---|---|---|
| SCR-FIN-001 | `FIN_ACCOUNTS` present → else unauthorized redirect | create, update and deactivate render for every holder of the screen; the server enforces CREATE/UPDATE and answers FIN-403-FORBIDDEN |
| SCR-FIN-002 | `FIN_DIMENSIONS` present (list, `/new` and `/:dimensionId` alike) | create dimension, create value and value-deactivate render for every holder; the server enforces CREATE/UPDATE; dimension itself has no deactivate (SRS §B4) |
| SCR-FIN-003 | `FIN_RULES` present (list, `/new` and `/:ruleId` alike) | new rule, add-line (with its dimension tags) and rule-deactivate render for every holder; the server enforces CREATE and UPDATE — one UPDATE permission covers both (SRS §B4); no line delete |
| SCR-FIN-004 | `FIN_RECURRING_TEMPLATES` present (list, `/new` and `/:templateId` alike) | create, run and deactivate render for every holder; the server enforces CREATE/UPDATE |
| SCR-FIN-005 | `FIN_ALLOCATION_RULES` present (list, `/new` and `/:allocationRuleId` alike) | create, run and deactivate render for every holder; the server enforces CREATE/UPDATE |
| SCR-FIN-006 | `FIN_JOURNAL_ENTRIES` present | create (incl. Post) and Reverse render for every holder; the server enforces CREATE and the custom `PERM_FIN_JOURNAL_ENTRIES_REVERSE`, for event-built entries as for any other |
| SCR-FIN-007 | `FIN_PERIODS` present (list, `/new` and `/:fiscalYearId` alike) | create year, open, soft-close, hard-close and year-end-close all render for every holder, whatever the period's state. RULE-FIN-015's separation of duty is the server's: `FIN-403-FORBIDDEN` answers a caller without Close-approve, and FIN-409-INVALID-TRANSITION / FIN-409-NOT-REOPENABLE / FIN-409-PERIODS-NOT-CLOSED answer on the merits. None of them is duplicated as a client-side check (ADR-FIN-065) |
| SCR-FIN-008 .. SCR-FIN-012 | respective page code present | read-only, VIEW only — no write affordance exists |
| SCR-FIN-013 | `FIN_ACCOUNT_MAPPINGS` present (v2 page, registered by REQ-FIN-044) | new, change-account and deactivate render for every holder; the server enforces the page's CREATE and UPDATE, as the api-docs publish; no Delete affordance exists — a mapping is only ever deactivated (SRS §B4) |

If the menu fails to load or comes back empty, no FIN entry is rendered and no FIN route is granted on any
screen. Access narrows and never widens (ADR-FIN-005).
<!-- PHASE:SEC-FE:END -->

<!-- PHASE:ALIGN-FE:START traces=SCR-FIN-001,SCR-FIN-002,SCR-FIN-003,SCR-FIN-004,SCR-FIN-005,SCR-FIN-006,SCR-FIN-007,SCR-FIN-008,SCR-FIN-009,SCR-FIN-010,SCR-FIN-011,SCR-FIN-012,SCR-FIN-013 -->
## PHASE ALIGN-FE — Alignment

**RF6 — Alignment.** Each row's mark is copied from the analyze report
(`_state/analyze-stage-P3.2.md`) for the clause that backs it.

```
ALIGN — FIN v2
row           backing check        assertion                                                                                   mark
SCREENS       orphans              every SCR is referenced by a plan block                                                     ✓ C9.7 — 13 examined
COMPOSITION   screen-composition   every SCR names where its secondary detail sits and that it saves once                      ✓ C9.17 — 13 examined
CONTAINER     composition-rule     every SCR names its container, and a child collection sits where that container puts it     ✓ C9.22 — 13 examined
OPTIONS       read-complete        every foreign key a screen sends has a read of its entity bound                             ✓ C9.18 — 25 examined
LABELS        label-read           every foreign id a screen renders resolves by pk IN, with no status filter                 ✓ C9.19 — 13 examined
SCOPE         scoped-option        every read of a parent-scoped search names its parent                                       ✓ C9.20 — 17 examined
DRILL         exact-drill          every cross-screen jump seeds an exact filter                                               ✓ C9.21 — 13 examined
UXD           orphans              every UXD is cited by a plan block — this is where a UX decision closes                     ✓ C9.6 — 15 examined
TRACES        traces               every PHASE/SUB carries traces=, every UXD traces to its REQ/AC, every SCR to its REQ/UXD    ✓ C9.2 — 58 · C9.3 — 15 · C9.4 — 13 examined
API           traces               every API this plan cites is defined in the fetched api-docs — never in the backend plan's contract draft   ✓ C9.5 — 41 examined
FOREIGN       xref-surface         every reference to another module's surface resolves in that module's own artifacts         ✓ C9.13 — 0 findings, no subject count reported
REGISTRY      registry-agree       every UXD and SCR defined here is in the stage registry, and nothing else is                ✓ C9.8 — 28 examined
LANGUAGES     languages            labels and messages in ar + en                                                              ✓ C9.14 — 0 findings, no subject count reported
MARKERS       markers              the parser reports no structural or semantic error for this track and plan                  ✓ C9.1 — 0 findings, no subject count reported
DECISIONS     refs-exist           every ADR this plan cites exists on disk in analysis/decisions/FIN/                         — not in the stage:P3.2 report (C7.13 runs at gate scope)
COVERAGE      (the report)         clauses listed as having examined nothing: none
RESULT    PASSED ✓ — 0 findings
```

Operations coverage table (operation │ API │ SCR action │ route │ status). Every write of the five record
screens lives on its `/new` or `/:id` route (ADR-FIN-065). Each of their searches also serves the record page's
cold-load read:

| Operation | API | SCR action | Route | Status |
|---|---|---|---|---|
| search accounts | API-FIN-001 | SCR-FIN-001 view | /fin/accounts | ✓ |
| create account | API-FIN-002 | SCR-FIN-001 create | /fin/accounts | ✓ |
| update account | API-FIN-003 | SCR-FIN-001 update | /fin/accounts | ✓ |
| deactivate account | API-FIN-004 | SCR-FIN-001 deactivate | /fin/accounts | ✓ |
| search dimensions (list; and the record page's cold load, `dimensionPk EQUALS`) | API-FIN-005 | SCR-FIN-002 view | /fin/dimensions · /fin/dimensions/:dimensionId | ✓ |
| create dimension | API-FIN-006 | SCR-FIN-002 create | /fin/dimensions/new | ✓ |
| create dimension value | API-FIN-007 | SCR-FIN-002 create | /fin/dimensions/:dimensionId | ✓ |
| search dimension values | API-FIN-008 | SCR-FIN-002 view | /fin/dimensions/:dimensionId | ✓ |
| deactivate dimension value | API-FIN-035 | SCR-FIN-002 update | /fin/dimensions/:dimensionId | ✓ |
| search event-type rules (list; and the record page's cold load, `eventTypeRulePk EQUALS`) | API-FIN-009 | SCR-FIN-003 view | /fin/rules · /fin/rules/:ruleId | ✓ |
| create event-type rule | API-FIN-010 | SCR-FIN-003 create | /fin/rules/new | ✓ |
| add rule line — CONSTANT or MAPPING, with its dimension tags | API-FIN-011 | SCR-FIN-003 update | /fin/rules/:ruleId | ✓ |
| deactivate event-type rule | API-FIN-034 | SCR-FIN-003 update | /fin/rules/:ruleId | ✓ |
| search recurring templates (list; and the record page's cold load, `recurringTemplatePk EQUALS`) | API-FIN-012 | SCR-FIN-004 view | /fin/recurring-templates · /fin/recurring-templates/:templateId | ✓ |
| create recurring template | API-FIN-013 | SCR-FIN-004 create | /fin/recurring-templates/new | ✓ |
| run recurring template | API-FIN-014 | SCR-FIN-004 update | /fin/recurring-templates/:templateId | ✓ |
| deactivate recurring template | API-FIN-036 | SCR-FIN-004 update | /fin/recurring-templates/:templateId | ✓ |
| search allocation rules (list; and the record page's cold load, `allocationRulePk EQUALS`) | API-FIN-015 | SCR-FIN-005 view | /fin/allocation-rules · /fin/allocation-rules/:allocationRuleId | ✓ |
| create allocation rule | API-FIN-016 | SCR-FIN-005 create | /fin/allocation-rules/new | ✓ |
| run allocation rule | API-FIN-017 | SCR-FIN-005 update | /fin/allocation-rules/:allocationRuleId | ✓ |
| deactivate allocation rule | API-FIN-037 | SCR-FIN-005 update | /fin/allocation-rules/:allocationRuleId | ✓ |
| search journal entries | API-FIN-018 | SCR-FIN-006 view | /fin/journal-entries | ✓ |
| create manual journal entry | API-FIN-019 | SCR-FIN-006 create | /fin/journal-entries/new | ✓ |
| build journal entry from event | API-FIN-020 | none (system caller, ADR-FIN-007) | — | ✗ (ADR-FIN-007) |
| reverse journal entry (every journal type) | API-FIN-021 | SCR-FIN-006 update | /fin/journal-entries/:id | ✓ |
| read journal entry | API-FIN-022 | SCR-FIN-006 view | /fin/journal-entries/:id | ✓ |
| create fiscal year | API-FIN-023 | SCR-FIN-007 create | /fin/periods/new | ✓ |
| open fiscal period | API-FIN-024 | SCR-FIN-007 update | /fin/periods/:fiscalYearId | ✓ |
| soft-close fiscal period | API-FIN-025 | SCR-FIN-007 update | /fin/periods/:fiscalYearId | ✓ |
| hard-close fiscal period | API-FIN-026 | SCR-FIN-007 close-approve | /fin/periods/:fiscalYearId | ✓ |
| run year-end close | API-FIN-027 | SCR-FIN-007 close-approve | /fin/periods/:fiscalYearId | ✓ |
| search fiscal periods | API-FIN-033 | SCR-FIN-007 view | /fin/periods/:fiscalYearId | ✓ |
| search fiscal years (list and option reads; and the record page's cold load, `fiscalYearPk EQUALS`) | `POST /api/v1/fin/fiscal-years/search` (ADR-FIN-061) | SCR-FIN-007 view · SCR-FIN-006, 010, 011 year selects | /fin/periods · /fin/periods/:fiscalYearId | ✓ |
| account ledger | API-FIN-028 | SCR-FIN-008 view | /fin/reports/account-ledger | ✓ |
| trial balance | API-FIN-029 | SCR-FIN-009 view | /fin/reports/trial-balance | ✓ |
| balance sheet | API-FIN-030 | SCR-FIN-010 view | /fin/reports/balance-sheet | ✓ |
| income statement | API-FIN-031 | SCR-FIN-011 view | /fin/reports/income-statement | ✓ |
| dimension report | API-FIN-032 | SCR-FIN-012 view | /fin/reports/dimension | ✓ |
| search account mappings | API-FIN-038 | SCR-FIN-013 view | /fin/account-mappings | ✓ |
| create account mapping | API-FIN-039 | SCR-FIN-013 create | /fin/account-mappings | ✓ |
| change a mapping's account | API-FIN-040 | SCR-FIN-013 update | /fin/account-mappings | ✓ |
| deactivate account mapping | API-FIN-041 | SCR-FIN-013 update | /fin/account-mappings | ✓ |
| update recurring template | (not published) | — | — | ✗ (ADR-FIN-006) |
| update allocation rule | (not published) | — | — | ✗ (ADR-FIN-006) |
| deactivate dimension (parent) | (not published) | — | — | ✗ (ADR-FIN-006) |
| delete rule line / template line / allocation target | (not published) | — | — | ✗ (ADR-FIN-006) |
<!-- PHASE:ALIGN-FE:END -->

<<<END INPUT>>>

<<<INPUT: registry-srs>>>
## REGISTRY — P1 — FIN v2
══════════════════════════════════════════════════════════════════
Change set : CS-FIN-001 (ADDITIVE) — delta on v1; this registry is the complete current P1 registry
             (v1 rows restated, v2 rows marked) so the merge into project-registry.md needs no v1 lookup.

Entities
| ENT id | Name (ar/en) | Kind | PRIVATE/SHARED | Status | v2 |
|---|---|---|---|---|---|
| ENT-FIN-001 | الحساب / Account | master | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-002 | البُعد / Dimension | config | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-003 | قيمة البُعد / DimensionValue | lookup | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-004 | رأس قيد اليومية / JournalEntry | transactional | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-005 | سطر قيد اليومية / JournalLine | transactional | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-006 | بُعد سطر القيد / JournalLineDimension | transactional | PRIVATE | REGISTERED | unchanged (now also written for event-built lines) |
| ENT-FIN-007 | السنة المالية / FiscalYear | master | PRIVATE | REGISTERED | MODIFIED v2 — label note only (ADR-FIN-038) |
| ENT-FIN-008 | الفترة المحاسبية / FiscalPeriod | master | PRIVATE | REGISTERED | MODIFIED v2 — label note only (ADR-FIN-038) |
| ENT-FIN-009 | قاعدة نوع الحدث / EventTypeRule | config | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-010 | سطر القاعدة / RuleLine | config | PRIVATE | REGISTERED | MODIFIED — DIRECT removed, accountBusinessFieldCode added |
| ENT-FIN-011 | قالب متكرر/عكسي / RecurringTemplate | config | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-012 | سطر القالب المتكرر / RecurringTemplateLine | config | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-013 | قاعدة توزيع / AllocationRule | config | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-014 | هدف التوزيع / AllocationTarget | config | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-015 | ربط الحساب / AccountMapping | config | PRIVATE | REGISTERED | ADDED |
| ENT-FIN-016 | بُعد سطر القاعدة / RuleLineDimension | config | PRIVATE | REGISTERED | ADDED |

Consumed
| Consumed entity | Owner ENT id | Owner module | HARD-FK / SOFT-READ | v2 |
|---|---|---|---|---|
| User | ENT-SEC-001 | SEC | HARD-FK (principal string) | unchanged |
| ModuleRegistry | ENT-SEC-004 | SEC | HARD-FK | unchanged |
| ScreenRegistry | ENT-SEC-005 | SEC | HARD-FK | + FIN_ACCOUNT_MAPPINGS screen row |
| ActionRegistry | ENT-SEC-006 | SEC | HARD-FK | + its VIEW / CREATE / UPDATE action rows |
| LookupType | ENT-MDL-001 | MDL | HARD-FK | + 2 types (15 FIN-owned) |
| LookupValue | ENT-MDL-002 | MDL | HARD-FK | + FIN_EVENT_BUSINESS_FIELD (4), FIN_DIMENSION_VALUE_SOURCE (2), PAYMENT_METHOD CHEQUE; ACCOUNT_DERIVATION_TYPE DIRECT retired |

Lookups owned
| Key | ENT | Values count | v2 |
|---|---|---|---|
| ACCOUNT_TYPE | ENT-FIN-001 | 5 | unchanged |
| DEBIT_CREDIT | ENT-FIN-001, ENT-FIN-005, ENT-FIN-010, ENT-FIN-012 | 2 | unchanged |
| PERIOD_STATE | ENT-FIN-008 | 4 | unchanged |
| FISCAL_YEAR_STATUS | ENT-FIN-007 | 2 | unchanged |
| JOURNAL_TYPE | ENT-FIN-004 | 5 | unchanged |
| JOURNAL_STATUS | ENT-FIN-004 | 3 | unchanged |
| ACCOUNTING_EVENT_TYPE | ENT-FIN-009, ENT-FIN-015 | 0 (host-defined) | now also used by ENT-FIN-015 |
| PAYMENT_METHOD | ENT-FIN-015 (businessValue when the field is PAYMENT_METHOD) | 1 seeded (CHEQUE) + host-defined | MODIFIED |
| ACCOUNT_DERIVATION_TYPE | ENT-FIN-010 | 2 (CONSTANT, MAPPING) | MODIFIED — DIRECT removed |
| AMOUNT_SOURCE_TYPE | ENT-FIN-010 | 3 | unchanged |
| DISTRIBUTION_TYPE | ENT-FIN-010, ENT-FIN-014 | 3 | unchanged |
| RECURRING_SCHEDULE_TYPE | ENT-FIN-011 | 2 | unchanged |
| RECURRING_FREQUENCY | ENT-FIN-011 | 4 | unchanged |
| FIN_EVENT_BUSINESS_FIELD | ENT-FIN-010, ENT-FIN-015, ENT-FIN-016 | 4 | ADDED |
| FIN_DIMENSION_VALUE_SOURCE | ENT-FIN-016 | 2 | ADDED |

Lookups consumed
none.

Screens
| SCR-REQ id | Name (ar/en) | Page code | v2 |
|---|---|---|---|
| SCR-REQ-FIN-001 | شجرة الحسابات / Chart of accounts | FIN_ACCOUNTS | unchanged |
| SCR-REQ-FIN-002 | تعريف الأبعاد وقيمها / Dimension definition & values | FIN_DIMENSIONS | unchanged |
| SCR-REQ-FIN-003 | قواعد المحرك / Engine rules | FIN_RULES | MODIFIED |
| SCR-REQ-FIN-004 | قوالب متكررة/عكسية / Recurring/reversing templates | FIN_RECURRING_TEMPLATES | unchanged |
| SCR-REQ-FIN-005 | قواعد التوزيع / Allocation rules | FIN_ALLOCATION_RULES | unchanged |
| SCR-REQ-FIN-006 | قيود اليومية / Journal entries | FIN_JOURNAL_ENTRIES | MODIFIED |
| SCR-REQ-FIN-007 | الفترات والسنوات المالية / Fiscal periods & years | FIN_PERIODS | unchanged |
| SCR-REQ-FIN-008 | دفتر الحساب / Account ledger | FIN_ACCOUNT_LEDGER | unchanged |
| SCR-REQ-FIN-009 | ميزان المراجعة / Trial balance | FIN_TRIAL_BALANCE | unchanged |
| SCR-REQ-FIN-010 | الميزانية العمومية / Balance sheet | FIN_BALANCE_SHEET | unchanged |
| SCR-REQ-FIN-011 | قائمة الدخل / Income statement | FIN_INCOME_STATEMENT | unchanged |
| SCR-REQ-FIN-012 | تقارير الأبعاد / Dimension reports | FIN_DIMENSION_REPORTS | unchanged |
| SCR-REQ-FIN-013 | روابط الحسابات / Account mappings | FIN_ACCOUNT_MAPPINGS | ADDED |

Requirements
REQ count: 77 · AC count: 83 · RULE count: 28 · ENT count: 16 · SCR-REQ count: 13
Last sequence per atom: REQ: 077 · AC: 083 · ENT: 016 · RULE: 028 · SCR-REQ: 013
v2 delta: ADDED REQ 31 · AC 37 · RULE 11 · ENT 2 · SCR-REQ 1; MODIFIED ENT 1 · REQ 1 · AC 1 · RULE 1 · SCR-REQ 2

REQ ids (full text in srs-fin.md → A4): REQ-FIN-001, REQ-FIN-002, REQ-FIN-003, REQ-FIN-004,
REQ-FIN-005, REQ-FIN-006, REQ-FIN-007, REQ-FIN-008, REQ-FIN-009, REQ-FIN-010, REQ-FIN-011,
REQ-FIN-012, REQ-FIN-013, REQ-FIN-014, REQ-FIN-015, REQ-FIN-016, REQ-FIN-017, REQ-FIN-018,
REQ-FIN-019, REQ-FIN-020, REQ-FIN-021, REQ-FIN-022, REQ-FIN-023, REQ-FIN-024, REQ-FIN-025,
REQ-FIN-026, REQ-FIN-027, REQ-FIN-028, REQ-FIN-029, REQ-FIN-030, REQ-FIN-031, REQ-FIN-032,
REQ-FIN-033, REQ-FIN-034, REQ-FIN-035, REQ-FIN-036, REQ-FIN-037, REQ-FIN-038, REQ-FIN-039,
REQ-FIN-040, REQ-FIN-041, REQ-FIN-042, REQ-FIN-043, REQ-FIN-044, REQ-FIN-045 (MODIFIED v2), REQ-FIN-046,
v2 ADDED: REQ-FIN-047, REQ-FIN-048, REQ-FIN-049, REQ-FIN-050, REQ-FIN-051, REQ-FIN-052, REQ-FIN-053,
REQ-FIN-054, REQ-FIN-055, REQ-FIN-056, REQ-FIN-057, REQ-FIN-058, REQ-FIN-059, REQ-FIN-060, REQ-FIN-061,
REQ-FIN-062, REQ-FIN-063, REQ-FIN-064, REQ-FIN-065, REQ-FIN-066, REQ-FIN-067, REQ-FIN-068, REQ-FIN-069,
REQ-FIN-070, REQ-FIN-071, REQ-FIN-072, REQ-FIN-073, REQ-FIN-074, REQ-FIN-075, REQ-FIN-076, REQ-FIN-077

AC ids (full text in srs-fin.md → A4): AC-FIN-001, AC-FIN-002, AC-FIN-003,
AC-FIN-004, AC-FIN-005, AC-FIN-006, AC-FIN-007, AC-FIN-008, AC-FIN-009, AC-FIN-010,
AC-FIN-011, AC-FIN-012, AC-FIN-013, AC-FIN-014, AC-FIN-015, AC-FIN-016, AC-FIN-017,
AC-FIN-018, AC-FIN-019, AC-FIN-020, AC-FIN-021, AC-FIN-022, AC-FIN-023, AC-FIN-024,
AC-FIN-025, AC-FIN-026, AC-FIN-027, AC-FIN-028, AC-FIN-029, AC-FIN-030, AC-FIN-031,
AC-FIN-032, AC-FIN-033, AC-FIN-034, AC-FIN-035, AC-FIN-036, AC-FIN-037, AC-FIN-038,
AC-FIN-039, AC-FIN-040, AC-FIN-041, AC-FIN-042, AC-FIN-043, AC-FIN-044, AC-FIN-045 (MODIFIED v2),
AC-FIN-046,
v2 ADDED: AC-FIN-047, AC-FIN-048, AC-FIN-049, AC-FIN-050, AC-FIN-051, AC-FIN-052, AC-FIN-053,
AC-FIN-054, AC-FIN-055, AC-FIN-056, AC-FIN-057, AC-FIN-058, AC-FIN-059, AC-FIN-060, AC-FIN-061,
AC-FIN-062, AC-FIN-063, AC-FIN-064, AC-FIN-065, AC-FIN-066, AC-FIN-067, AC-FIN-068, AC-FIN-069,
AC-FIN-070, AC-FIN-071, AC-FIN-072, AC-FIN-073, AC-FIN-074, AC-FIN-075, AC-FIN-076, AC-FIN-077,
AC-FIN-078, AC-FIN-079, AC-FIN-080, AC-FIN-081, AC-FIN-082, AC-FIN-083

RULE ids (full text in srs-fin.md → A5): RULE-FIN-001, RULE-FIN-002, RULE-FIN-003,
RULE-FIN-004 (MODIFIED v2), RULE-FIN-005, RULE-FIN-006, RULE-FIN-007, RULE-FIN-008 (MODIFIED v2 — wording,
ADR-FIN-037), RULE-FIN-009,
RULE-FIN-010, RULE-FIN-011, RULE-FIN-012, RULE-FIN-013, RULE-FIN-014, RULE-FIN-015,
RULE-FIN-016, RULE-FIN-017,
v2 ADDED: RULE-FIN-018, RULE-FIN-019, RULE-FIN-020, RULE-FIN-021, RULE-FIN-022, RULE-FIN-023,
RULE-FIN-024, RULE-FIN-025, RULE-FIN-026, RULE-FIN-027, RULE-FIN-028

Decisions
ADR ids raised by P1 v2: ADR-FIN-023, ADR-FIN-024, ADR-FIN-025, ADR-FIN-026, ADR-FIN-027 and, at the pass-1
revision, ADR-FIN-037, ADR-FIN-038, ADR-FIN-039 — eight, all ACCEPTED (non-breaking).
BLOCKED: none.

Event
"P1 completed: FIN v2 — +2 entities (16), +31 requirements (77), +37 acceptance criteria (83), +11 rules (28), +1 screen requirement (13), 5 ADRs; MODIFIED ENT-FIN-010, REQ-FIN-045, AC-FIN-045, RULE-FIN-004, SCR-REQ-FIN-003, SCR-REQ-FIN-006"
══════════════════════════════════════════════════════════════════

<!-- carried from baseline v1 by gov.py state — not re-emitted in this delta -->
AC-FIN-046

RULE ids (full text in srs-fin.md → A5): RULE-FIN-001, RULE-FIN-002, RULE-FIN-003,
RULE-FIN-004, RULE-FIN-005, RULE-FIN-006, RULE-FIN-007, RULE-FIN-008, RULE-FIN-009,
RULE-FIN-010, RULE-FIN-011, RULE-FIN-012, RULE-FIN-013, RULE-FIN-014, RULE-FIN-015,
RULE-FIN-016, RULE-FIN-017

Decisions
ADR ids: none.

Event
"P1 completed: FIN v1 — 14 entities, 46 requirements, 46 acceptance criteria, 16 rules, 12 screen requirements, 0 ADRs"
(RULE-FIN-017 — fiscal-year/period/docDate coherence — was added later, during SVC-API, and
carried back into srs-fin.md §A5; the counts above are the current 17, while this event line
records what P1 itself emitted.)
══════════════════════════════════════════════════════════════════

<<<END INPUT>>>

<<<INPUT: registry-db>>>
## REGISTRY — P2 — FIN v2
══════════════════════════════════════════════════════════════════
Change set : CS-FIN-001 (ADDITIVE), a delta on v1. This registry is the complete current P2
             registry: v1 rows are restated and v2 rows marked, so the merge into project-registry.md
             needs no v1 lookup.

Tables
| Table | ENT id | Kind | DBF range | v2 |
|---|---|---|---|---|
| FIN_ACCOUNT | ENT-FIN-001 | master | DBF-FIN-001 … DBF-FIN-013, DBF-FIN-147 | unchanged |
| FIN_DIMENSION | ENT-FIN-002 | config | DBF-FIN-014 … DBF-FIN-022 | unchanged |
| FIN_DIMENSION_VALUE | ENT-FIN-003 | lookup | DBF-FIN-023 … DBF-FIN-033 | unchanged |
| FIN_JOURNAL_ENTRY | ENT-FIN-004 | transactional | DBF-FIN-034 … DBF-FIN-050 | DBF-FIN-041 MODIFIED (traces only) |
| FIN_JOURNAL_LINE | ENT-FIN-005 | transactional | DBF-FIN-051 … DBF-FIN-060 | unchanged |
| FIN_JOURNAL_LINE_DIM | ENT-FIN-006 | transactional | DBF-FIN-061 … DBF-FIN-064 | unchanged (now also written for event-built lines) |
| FIN_FISCAL_YEAR | ENT-FIN-007 | master | DBF-FIN-065 … DBF-FIN-074 | unchanged |
| FIN_FISCAL_PERIOD | ENT-FIN-008 | master | DBF-FIN-075 … DBF-FIN-088 | unchanged |
| FIN_EVENT_TYPE_RULE | ENT-FIN-009 | config | DBF-FIN-089 … DBF-FIN-097 | unchanged |
| FIN_RULE_LINE | ENT-FIN-010 | config | DBF-FIN-098 … DBF-FIN-108, DBF-FIN-148 | MODIFIED — DBF-FIN-101, DBF-FIN-102 MODIFIED; DBF-FIN-148 ADDED; CHK_FIN_RULE_LINE_DERIVATION_SPEC |
| FIN_RECURRING_TEMPLATE | ENT-FIN-011 | config | DBF-FIN-109 … DBF-FIN-121 | unchanged |
| FIN_RECURRING_TEMPLATE_LINE | ENT-FIN-012 | config | DBF-FIN-122 … DBF-FIN-129 | unchanged |
| FIN_ALLOCATION_RULE | ENT-FIN-013 | config | DBF-FIN-130 … DBF-FIN-138 | unchanged |
| FIN_ALLOCATION_TARGET | ENT-FIN-014 | config | DBF-FIN-139 … DBF-FIN-146 | unchanged |
| FIN_ACCOUNT_MAPPING | ENT-FIN-015 | config | DBF-FIN-149 … DBF-FIN-158 | ADDED |
| FIN_RULE_LINE_DIM | ENT-FIN-016 | config | DBF-FIN-159 … DBF-FIN-165 | ADDED |

DBF ids (full detail in db-script-fin.md → §1): DBF-FIN-001, DBF-FIN-002, DBF-FIN-003,
DBF-FIN-004, DBF-FIN-005, DBF-FIN-006, DBF-FIN-007,
DBF-FIN-008, DBF-FIN-009, DBF-FIN-010, DBF-FIN-011, DBF-FIN-012, DBF-FIN-013, DBF-FIN-014,
DBF-FIN-015, DBF-FIN-016, DBF-FIN-017, DBF-FIN-018, DBF-FIN-019, DBF-FIN-020, DBF-FIN-021,
DBF-FIN-022, DBF-FIN-023, DBF-FIN-024, DBF-FIN-025, DBF-FIN-026, DBF-FIN-027, DBF-FIN-028,
DBF-FIN-029, DBF-FIN-030, DBF-FIN-031, DBF-FIN-032, DBF-FIN-033, DBF-FIN-034, DBF-FIN-035,
DBF-FIN-036, DBF-FIN-037, DBF-FIN-038, DBF-FIN-039, DBF-FIN-040, DBF-FIN-041, DBF-FIN-042,
DBF-FIN-043, DBF-FIN-044, DBF-FIN-045, DBF-FIN-046, DBF-FIN-047, DBF-FIN-048, DBF-FIN-049,
DBF-FIN-050, DBF-FIN-051, DBF-FIN-052, DBF-FIN-053, DBF-FIN-054, DBF-FIN-055, DBF-FIN-056,
DBF-FIN-057, DBF-FIN-058, DBF-FIN-059, DBF-FIN-060, DBF-FIN-061, DBF-FIN-062, DBF-FIN-063,
DBF-FIN-064, DBF-FIN-065, DBF-FIN-066, DBF-FIN-067, DBF-FIN-068, DBF-FIN-069, DBF-FIN-070,
DBF-FIN-071, DBF-FIN-072, DBF-FIN-073, DBF-FIN-074, DBF-FIN-075, DBF-FIN-076, DBF-FIN-077,
DBF-FIN-078, DBF-FIN-079, DBF-FIN-080, DBF-FIN-081, DBF-FIN-082, DBF-FIN-083, DBF-FIN-084,
DBF-FIN-085, DBF-FIN-086, DBF-FIN-087, DBF-FIN-088, DBF-FIN-089, DBF-FIN-090, DBF-FIN-091,
DBF-FIN-092, DBF-FIN-093, DBF-FIN-094, DBF-FIN-095, DBF-FIN-096, DBF-FIN-097, DBF-FIN-098,
DBF-FIN-099, DBF-FIN-100, DBF-FIN-101, DBF-FIN-102, DBF-FIN-103, DBF-FIN-104, DBF-FIN-105,
DBF-FIN-106, DBF-FIN-107, DBF-FIN-108, DBF-FIN-109, DBF-FIN-110, DBF-FIN-111, DBF-FIN-112,
DBF-FIN-113, DBF-FIN-114, DBF-FIN-115, DBF-FIN-116, DBF-FIN-117, DBF-FIN-118, DBF-FIN-119,
DBF-FIN-120, DBF-FIN-121, DBF-FIN-122, DBF-FIN-123, DBF-FIN-124, DBF-FIN-125, DBF-FIN-126,
DBF-FIN-127, DBF-FIN-128, DBF-FIN-129, DBF-FIN-130, DBF-FIN-131, DBF-FIN-132, DBF-FIN-133,
DBF-FIN-134, DBF-FIN-135, DBF-FIN-136, DBF-FIN-137, DBF-FIN-138, DBF-FIN-139, DBF-FIN-140,
DBF-FIN-141, DBF-FIN-142, DBF-FIN-143, DBF-FIN-144, DBF-FIN-145, DBF-FIN-146, DBF-FIN-147,
v2 ADDED: DBF-FIN-148, DBF-FIN-149, DBF-FIN-150, DBF-FIN-151, DBF-FIN-152, DBF-FIN-153, DBF-FIN-154,
DBF-FIN-155, DBF-FIN-156, DBF-FIN-157, DBF-FIN-158, DBF-FIN-159, DBF-FIN-160, DBF-FIN-161,
DBF-FIN-162, DBF-FIN-163, DBF-FIN-164, DBF-FIN-165
v2 MODIFIED: DBF-FIN-041, DBF-FIN-101, DBF-FIN-102

XM index
| XM id | Type | From | To | Status | v2 |
|---|---|---|---|---|---|
| XM-FIN-001 | SOFT-READ | FIN | MDL | ACTIVE | MODIFIED — scope widened to the 6 v2 lookup-backed columns and 15 keys; same target, same type |
| ~~XM-FIN-002~~ | READ | FIN | SEC | RETIRED 2026-09-12 (see db-script-fin.md §2) | unchanged |

Lookups
| Key | Seeded values count | Owner | v2 |
|---|---|---|---|
| ACCOUNT_TYPE | 5 | FIN | unchanged |
| DEBIT_CREDIT | 2 | FIN | unchanged |
| PERIOD_STATE | 4 | FIN | unchanged |
| FISCAL_YEAR_STATUS | 2 | FIN | unchanged |
| JOURNAL_TYPE | 5 | FIN | unchanged |
| JOURNAL_STATUS | 3 | FIN | unchanged |
| ACCOUNTING_EVENT_TYPE | 0 (host-defined) | FIN | unchanged; now also used by FIN_ACCOUNT_MAPPING.event_type_code |
| PAYMENT_METHOD | 1 (CHEQUE) + host-defined | FIN | MODIFIED — CHEQUE seeded (BLOCK 8) |
| ACCOUNT_DERIVATION_TYPE | 2 active (CONSTANT, MAPPING); DIRECT deactivated | FIN | MODIFIED — DIRECT soft-retired (BLOCK 8) |
| AMOUNT_SOURCE_TYPE | 3 | FIN | unchanged |
| DISTRIBUTION_TYPE | 3 | FIN | unchanged |
| RECURRING_SCHEDULE_TYPE | 2 | FIN | unchanged |
| RECURRING_FREQUENCY | 4 | FIN | unchanged |
| FIN_EVENT_BUSINESS_FIELD | 4 | FIN | ADDED (BLOCK 8) |
| FIN_DIMENSION_VALUE_SOURCE | 2 | FIN | ADDED (BLOCK 8) |
The 13 v1 keys were seeded through MDL's API at onboarding (REQ-FIN-045). The v2 changes also have
an idempotent SQL seed block against MDL_LOOKUP_TYPE / MDL_LOOKUP_VALUE (db-script BLOCK 8,
ADR-FIN-028). The tables are MDL's; the keys, and so the seed, are FIN's.

Sequences
Last DBF: DBF-FIN-165 · Last XM: XM-FIN-002 (retired 2026-09-12; the id is burned, not reused. The
live XM set is XM-FIN-001 alone.)
Physical sequences ADDED: SEQ_FIN_ACCOUNT_MAPPING, SEQ_FIN_RULE_LINE_DIM (16 per-table sequences in all)

Decisions
ADR-FIN-001 (ACCEPTED, non-breaking, v1, unchanged) — see analysis/decisions/FIN/ADR-FIN-001.md
ADR-FIN-028 (ACCEPTED, non-breaking, raised by P2 v2) — see analysis/decisions/FIN/ADR-FIN-028.md
BLOCKED: none.

Event
"P2 completed: FIN v2 — 16 tables (+2), 165 DBF (+18 ADDED, 3 MODIFIED), 2 XM ids (XM-FIN-001 MODIFIED; 1 live); migration on v1; 1 ADR"

Cascade
No registry XM row anywhere in the platform targets FIN with status DEFERRED, so there is nothing
to resolve. XM-FIN-001 stays ACTIVE, because MDL v1 is gated. Its v2 widening needs no MDL-side
change: MDL receives FIN's new keys as data. v2 adds no XM towards SEC. Per ADR-FIN-001, the
FIN_ACCOUNT_MAPPINGS screen and its actions are registered as data through SEC's onboarding, not
as an XM.
══════════════════════════════════════════════════════════════════

<<<END INPUT>>>

<<<INPUT: registry-exec-fe>>>
REGISTRY — P3.2 — FIN v2
══════════════════════════════════════════════════════════════════
Change set : CS-FIN-001 (ADDITIVE), a delta on v1. This registry is the complete current P3.2 registry: v1 rows
             are restated and v2 rows are marked, so the fold needs no v1 lookup.

ID RANGES     UXD-FIN-001..015 · SCR-FIN-001..013
              v2 ADDED: SCR-FIN-013 · UXD-FIN-013, UXD-FIN-014, UXD-FIN-015
              v2 MODIFIED: SCR-FIN-001 … SCR-FIN-012 (the `Composition` line on every screen; the v2 content on
              SCR-FIN-003 and SCR-FIN-006; the published endpoint each cites) · UXD-FIN-007 (now also on
              SCR-FIN-013). Nothing is renumbered and no sequence restarts.

SCREENS
| SCR | Name (ar/en) | Owning ENT | Permissions | v2 |
|---|---|---|---|---|
| SCR-FIN-001 | شجرة الحسابات / Chart of accounts | ENT-FIN-001 | PERM_FIN_ACCOUNTS_VIEW/CREATE/UPDATE | Composition |
| SCR-FIN-002 | تعريف الأبعاد وقيمها / Dimension definition & values | ENT-FIN-002, ENT-FIN-003 | PERM_FIN_DIMENSIONS_VIEW/CREATE/UPDATE | Composition; value deactivate cited as API-FIN-035 |
| SCR-FIN-003 | قواعد المحرك / Engine rules | ENT-FIN-009, ENT-FIN-010, ENT-FIN-016 | PERM_FIN_RULES_VIEW/CREATE/UPDATE | MODIFIED — MAPPING line, dimension tags, one line per submit (ADR-FIN-062); bound to the published v2 API-FIN-011 |
| SCR-FIN-004 | قوالب متكررة/عكسية / Recurring/reversing templates | ENT-FIN-011, ENT-FIN-012 | PERM_FIN_RECURRING_TEMPLATES_VIEW/CREATE/UPDATE | Composition; deactivate cited as API-FIN-036 |
| SCR-FIN-005 | قواعد التوزيع / Allocation rules | ENT-FIN-013, ENT-FIN-014 | PERM_FIN_ALLOCATION_RULES_VIEW/CREATE/UPDATE | Composition; deactivate cited as API-FIN-037 |
| SCR-FIN-006 | قيود اليومية / Journal entries | ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 | PERM_FIN_JOURNAL_ENTRIES_VIEW/CREATE, PERM_FIN_JOURNAL_ENTRIES_REVERSE | MODIFIED — event-built lines shown in the one dimension shape; `/new` takes the record-page shape and scoped period options (ADR-FIN-067) |
| SCR-FIN-007 | الفترات والسنوات المالية / Fiscal periods & years | ENT-FIN-007, ENT-FIN-008 | PERM_FIN_PERIODS_VIEW/CREATE/UPDATE, PERM_FIN_PERIODS_CLOSE_APPROVE | Composition; period search cited as API-FIN-033, year search by path (ADR-FIN-061) |
| SCR-FIN-008 | دفتر الحساب / Account ledger | ENT-FIN-005 | PERM_FIN_ACCOUNT_LEDGER_VIEW | Composition |
| SCR-FIN-009 | ميزان المراجعة / Trial balance | ENT-FIN-005 | PERM_FIN_TRIAL_BALANCE_VIEW | Composition |
| SCR-FIN-010 | الميزانية العمومية / Balance sheet | ENT-FIN-005 | PERM_FIN_BALANCE_SHEET_VIEW | Composition |
| SCR-FIN-011 | قائمة الدخل / Income statement | ENT-FIN-005 | PERM_FIN_INCOME_STATEMENT_VIEW | Composition |
| SCR-FIN-012 | تقارير الأبعاد / Dimension reports | ENT-FIN-005, ENT-FIN-006 | PERM_FIN_DIMENSION_REPORTS_VIEW | Composition |
| SCR-FIN-013 | روابط الحسابات / Account mappings | ENT-FIN-015 | PERM_FIN_ACCOUNT_MAPPINGS_VIEW/CREATE/UPDATE (as published on API-FIN-038 … API-FIN-041) | ADDED |

UXD INDEX
| UXD | Screen(s) | Field | Owner module · API used | v2 |
|---|---|---|---|---|
| UXD-FIN-001 | SCR-FIN-001, 008, 009, 010, 011 | accountTypeCode | MDL · API-MDL-011 | — |
| UXD-FIN-002 | SCR-FIN-001, 003, 004, 006, 008, 009, 010, 011, 012 | directionCode / natureCode (DEBIT_CREDIT) | MDL · API-MDL-011 | — |
| UXD-FIN-003 | SCR-FIN-007 | statusCode (PERIOD_STATE) | MDL · API-MDL-011 | — |
| UXD-FIN-004 | SCR-FIN-007 | statusCode (FISCAL_YEAR_STATUS) | MDL · API-MDL-011 | — |
| UXD-FIN-005 | SCR-FIN-006, 008 | journalTypeCode | MDL · API-MDL-011 | — |
| UXD-FIN-006 | SCR-FIN-006 | statusCode (JOURNAL_STATUS) | MDL · API-MDL-011 | — |
| UXD-FIN-007 | SCR-FIN-003, 013 | eventTypeCode (ACCOUNTING_EVENT_TYPE) | MDL · API-MDL-011 | MODIFIED — + SCR-FIN-013 |
| UXD-FIN-008 | SCR-FIN-003 | accountDerivationTypeCode | MDL · API-MDL-011 | — (DIRECT leaves MDL; nothing hardcoded) |
| UXD-FIN-009 | SCR-FIN-003 | amountSourceTypeCode | MDL · API-MDL-011 | — |
| UXD-FIN-010 | SCR-FIN-003, 005 | distributionTypeCode | MDL · API-MDL-011 | — |
| UXD-FIN-011 | SCR-FIN-004 | scheduleTypeCode | MDL · API-MDL-011 | — |
| UXD-FIN-012 | SCR-FIN-004 | frequencyCode | MDL · API-MDL-011 | — |
| UXD-FIN-013 | SCR-FIN-003, 013 | accountBusinessFieldCode, tag businessFieldCode, mapping businessFieldCode (FIN_EVENT_BUSINESS_FIELD) | MDL · API-MDL-011 | ADDED |
| UXD-FIN-014 | SCR-FIN-003 | tag valueSourceCode (FIN_DIMENSION_VALUE_SOURCE) | MDL · API-MDL-011 | ADDED |
| UXD-FIN-015 | SCR-FIN-013 | businessValue when businessFieldCode=PAYMENT_METHOD (PAYMENT_METHOD) | MDL · API-MDL-011 | ADDED |

API COVERAGE
Published and numbered in the fetched api-docs (backend commit 4cbe044…, fetched 2026-09-23T20:36Z): the full
P3.1 v2 registry range, API-FIN-001 … API-FIN-041 (41).
- **Used by a screen action:** 40, which includes the five v1 ALIGN-BE endpoints (API-FIN-033 fiscal-period
  search, API-FIN-034 rule deactivate, API-FIN-035 dimension-value deactivate, API-FIN-036 template deactivate,
  API-FIN-037 allocation-rule deactivate) and the four account-mapping endpoints (API-FIN-038 search,
  API-FIN-039 create, API-FIN-040 change account, API-FIN-041 deactivate) on SCR-FIN-013.
- **Bound but drawn on no screen:** API-FIN-020 (ADR-FIN-007). Its entries are searched, read and reversed on
  SCR-FIN-006 like any other entry. v2 binds REQ-FIN-047, 050…053, 063 and 066…077 to it.

Published without a contract id, cited by verb and path (ADR-FIN-061), and used:
| Published endpoint | P3.1 registry id | Used by |
|---|---|---|
| `POST /api/v1/fin/fiscal-years/search` | none — P3.1's gap | SCR-FIN-007, SCR-FIN-006 |

API-FIN-011 is bound with its published v2 members (the line's business field and its dimension tags), on both
the request and the rule and line responses.

Named by the SRS with no published endpoint: six operations, omitted rather than faked (ADR-FIN-006).
The v1 provisional id API-FIN-038 for the fiscal-year search stays withdrawn: that id is the account-mapping
search (ADR-FIN-061).

ALIGN      The verdict is stamped by the orchestrator from the analyze report into the ALIGN block of
           frontend-execution-plan-fin.md (PHASE ALIGN-FE). This registry carries no hand-counted verdict.
           Fixed in v2:
           - the six C9.5 CRITICAL clauses that the folded v1 plan carried (API-FIN-033 … 038 cited but not in
             the api-docs at the time); the five that the republished api-docs now number are cited by id, the
             fiscal-year search by path (ADR-FIN-061, ADR-FIN-064);
           - the twelve C9.17 MINOR clauses (no `Composition` line), with every screen block now declaring one;
           - the v1 ALIGN block's hand-written "PASSED ✓ — 0 findings" (C9.12), replaced by the stamp slot;
           - the bindings the previous run of this stage left pending on the unpublished v2 surface, now bound
             to the published ids, DTOs and catalogue codes (ADR-FIN-064);
           - what a consumer must READ (ADR-FIN-067): C9.18 option reads (16), C9.19 label reads (14), C9.20 scoped
             period read (1), C9.21 exact drill-in (2), C9.22 the SCR-FIN-006 page composition and every container, and
             C10.7 the bound text of every routed refusal.

ADRs       decisions/FIN/ADR-FIN-067.md (ACCEPTED — option, label and scoped reads per screen; accountPk drill-in; SCR-FIN-006
             record-page composition; bound refusal texts; backend asks: dimensionId on template lines and allocation
             targets, quoted string examples, a contract id for the fiscal-year search)
           decisions/FIN/ADR-FIN-065.md (ACCEPTED — consumer feedback from the frontend build: list + `/new` + `/:id` record pages for
             screens SCR-FIN-002/003/004/005/007, hydrated by the published `<entity>Pk EQUALS` search; nullable DTO fields `T | null`;
             option reads disclose `hasMore`; no client-side action gating; FB-3/4/5/10/11 bindings; narrows ADR-FIN-006)
           decisions/FIN/ADR-FIN-064.md (ACCEPTED — the republished api-docs carry the v2 surface; every pending binding resolved)
           decisions/FIN/ADR-FIN-060.md (SUPERSEDED by ADR-FIN-064 — it was BLOCKED on the unpublished v2 surface)
           decisions/FIN/ADR-FIN-061.md (ACCEPTED, narrowed by ADR-FIN-064 — only the fiscal-year search is still cited by path)
           decisions/FIN/ADR-FIN-062.md (ACCEPTED — engine-rule lines added one per submit, never behind the rule's save)
           Carried from v1 (ACCEPTED):
           - ADR-FIN-002: the api-docs binding annex, superseded as an id source by ADR-FIN-061.
           - ADR-FIN-003: FULL_PAGE reports.
           - ADR-FIN-004: one UXD per displayed lookup key.
           - ADR-FIN-005: gating through the SEC menu.
           - ADR-FIN-006: unpublished operations are omitted; its API-FIN-038 sentence is superseded by ADR-FIN-061; its
             "no by-id read" is narrowed to endpoints by ADR-FIN-065.
           - ADR-FIN-007: API-FIN-020 is drawn on no screen.
           - ADR-FIN-008: journalTypeCode is fixed to MANUAL.
           Applied from upstream: ADR-FIN-024, ADR-FIN-025, ADR-FIN-039.

TRACEABILITY  REQ covered by ≥1 SCR/F-block or by the plan's BINDING: 75/77.
              Covered through the screen blocks and F-blocks:
              - REQ-FIN-001 … 043, 046 (v1);
              - REQ-FIN-048, 049, 062, 064, 065 (SCR-FIN-003);
              - REQ-FIN-054 … 061 (SCR-FIN-013);
              - REQ-FIN-070, 077 (SCR-FIN-006);
              - REQ-FIN-071 (SCR-FIN-008, SCR-FIN-012).
              Covered through the BINDING on API-FIN-020 (the system caller, drawn on no screen — ADR-FIN-007):
              REQ-FIN-010 … 013, 047, 050 … 053, 063, 066 … 069, 072 … 076.
              Not a gap: REQ-FIN-044 and REQ-FIN-045 trace to onboarding, with no screen, by the P0.5 traceability
              matrix itself.
              Orphan REQ: none. Every screen-facing REQ is bound to a published endpoint.

Event
"P3.2 completed: FIN v2 — 13 SCR (+1), 15 UXD (+3), plan re-emitted complete (F1–F4 SUB per screen, SEC-FE, ALIGN-FE)
and bound to the republished v2 api-docs (API-FIN-001 … 041); 5 ADRs (ADR-FIN-061, ADR-FIN-062, ADR-FIN-064, ADR-FIN-065; ADR-FIN-060
superseded); revised by ADR-FIN-065 for the frontend build's consumer feedback (record pages, nullable types, option
disclosure, no client-side action gating); re-run by ADR-FIN-067 for what each screen must READ (option, label and
scoped reads, exact drill-in, record-page composition, bound refusal texts)"
══════════════════════════════════════════════════════════════════

<<<END INPUT>>>

<<<INPUT: backend-test-plan (baseline — continue it, never restart it)>>>
# BACKEND TEST PLAN — الحسابات العامة / Finance (General Ledger) (FIN)
══════════════════════════════════════════════════════════════════
Module : FIN   Version : v2 (delta on v1 — change set CS-FIN-001)   Profile : erp   Track : backend   Plan : test
Scope  : **module** — modules FIN (`gov.py run-standalone test-gen --module FIN`)
Sources: `_state/current-srs.md` (v2) · `_state/current-backend-execution-plan.md` (v2) ·
         `_state/current-frontend-execution-plan.md` (v2) · `_state/current-registry-srs.md` (v2) ·
         `_state/current-registry-db.md` (v2) · `_state/current-registry-exec-fe.md` (v2) ·
         baseline `_state/current-backend-test-plan.md` (v1)
Framework : **agnostic** (`profile.stack.testing`). Every block below is the framework-agnostic
            TC form; no tool, annotation, fixture or file layout is named anywhere in this plan.
            The consumer repo chooses its framework and turns each TC into a test.
REDUCED   : no — the backend-execution-plan is present, so every step binds to a real `API-*`,
            the four v2 account-mapping endpoints API-FIN-038 … API-FIN-041 included.
TC ids    : v1 TC-FIN-001 … TC-FIN-052 on this track, the frontend holding TC-FIN-053 … TC-FIN-092;
            v2 continues the one shared module sequence at TC-FIN-093 … TC-FIN-129 on this track,
            and `frontend-test-plan-fin.md` continues at TC-FIN-130. No id is reused, none is
            renumbered, and no id is reassigned to a different subject than the one it carried.
Open ADRs : 2 raised by test-gen v2 — ADR-FIN-063 (first run) and ADR-FIN-066 (this run), both
            ACCEPTED, non-breaking. Applied and not re-derived: ADR-FIN-009 (v1 test-gen),
            ADR-FIN-010 … ADR-FIN-014, ADR-FIN-021, ADR-FIN-023 … ADR-FIN-031, ADR-FIN-037,
            ADR-FIN-039, ADR-FIN-040 … ADR-FIN-042, ADR-FIN-064, ADR-FIN-065. The first run cited
            ADR-FIN-060 as BLOCKED upstream; it is superseded by ADR-FIN-064.
══════════════════════════════════════════════════════════════════

**Delta v2 — what this file is.** A complete current plan, not a fragment. `gov.py state` folds a
marker plan by phase and carries back only a phase's missing *direct* children, so a SUB
re-emitted with its v2 atoms alone would lose every v1 atom inside it. The phase is therefore
re-emitted whole (ADR-FIN-063). Every v1 case whose content v2 leaves alone is restated
byte-identical.

- **ADDED** — thirty-seven cases, TC-FIN-093 … TC-FIN-129, one per v2 acceptance criterion
  AC-FIN-047 … AC-FIN-083 (TC = AC + 46). Every v2 AC is backend-exercisable, so this track's
  denominator stays the whole module.
- **MODIFIED** — five v1 cases whose content v2 made stale. TC-FIN-045 follows AC-FIN-045
  itself, which v2 modified (fifteen lookup types, DIRECT gone, CHEQUE seeded). TC-FIN-044 now
  counts thirteen screens and thirty actions. TC-FIN-008 no longer offers DIRECT as test data.
  TC-FIN-010 and TC-FIN-012 assert the 200 the published build-from-event contract returns,
  where v1 wrote 201. TC-FIN-049's text is unchanged, but its record runs on to the next SUB's
  START marker, whose `traces=` grew, so the tools read it as re-defined and it is listed
  MODIFIED too (ADR-FIN-063). The re-run of ADR-FIN-066 changes two more: the rollup-account
  and inactive-dimension cases, TC-FIN-019 and TC-FIN-021, now state that the "offending line
  is named" half of their ACs fails against the published error body, which names no line.
- **REMOVED** — none.

The change manifest lists **all 52** re-emitted v1 cases as MODIFIED, not only the six above.
The version check reads each case's START marker as a record of its own and compares it with the
baseline's heading record. A byte-identical marker atom therefore always reads as re-defined —
the same reason the P3.1 v2 plan lists API-FIN-001 … API-FIN-037 as MODIFIED. The content
changes are exactly the seven named above; TC-FIN-049 and the other forty-four are word for word
v1.

**Id order is AC order, not document order.** In v1, `TC-FIN-<seq>` maps 1:1 onto
`AC-FIN-<seq>` for the forty-six v1 criteria, and TC-FIN-047 … TC-FIN-052 extend six of them.
In v2, TC-FIN-093 … TC-FIN-129 map onto AC-FIN-047 … AC-FIN-083 in order. The v2 cases **open**
each SUB and the v1 cases follow: the tools compare a v1 record's full text, and a v2 case placed
after the last v1 case would change that record. The traceability index below is the address.

**Where the TCs come from, and where they deliberately stop.** Every TC below is derived from an
`AC-FIN-*`. The v1 boundary of ADR-FIN-009 holds unchanged: behaviour delivered on a recorded
decision or as an as-built narrowing gets no TC of its own. v2 adds five such paths, none of
which any AC states:

- the store-refusal translation of FIN-409-MAPPING-DUP under two simultaneous creates;
- FIN-404-ACCOUNT-MAPPING for an unknown mapping id;
- the trimming of `businessValue` on save and on event read (ADR-FIN-039);
- a blank, rather than absent, business field on the event;
- the RULE-FIN-019 mismatch (FIN-422-DERIVATION-SPEC), whose only AC, AC-FIN-049, is the case
  that passes.

They are named in the "catalog rows no TC exercises" list below and in ADR-FIN-063. If anyone
wants them covered, that is a gap on the requirements side, not a test-generation one.

**What is expected to fail against the build.** Nothing waits on publication: the republished
api-docs carry API-FIN-038 … API-FIN-041, the v2 members of API-FIN-011 and API-FIN-020, and the
period-not-open note AC-FIN-081 asks for (ADR-FIN-064), so the catalogue case, TC-FIN-127, reads
a document that holds it. Two v1 cases carry a half that the published contract cannot satisfy
yet. The rollup-account case, TC-FIN-019, and the inactive-dimension case, TC-FIN-021, assert
that the refusal names the offending line (and, for the second, the dimension). The published
error body of API-FIN-019 names neither, so that half fails until the backend publishes them —
the follow-up ADR-FIN-065 records. It is stated in both cases, and not dropped, because the AC
says it (ADR-FIN-066).

<!-- PHASE:TEST-PLAN-BE:START traces=REQ-FIN-001,REQ-FIN-002,REQ-FIN-003,REQ-FIN-004,REQ-FIN-005,REQ-FIN-006,REQ-FIN-007,REQ-FIN-008,REQ-FIN-009,REQ-FIN-010,REQ-FIN-011,REQ-FIN-012,REQ-FIN-013,REQ-FIN-014,REQ-FIN-015,REQ-FIN-016,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,REQ-FIN-022,REQ-FIN-023,REQ-FIN-024,REQ-FIN-025,REQ-FIN-026,REQ-FIN-027,REQ-FIN-028,REQ-FIN-029,REQ-FIN-030,REQ-FIN-031,REQ-FIN-032,REQ-FIN-033,REQ-FIN-034,REQ-FIN-035,REQ-FIN-036,REQ-FIN-037,REQ-FIN-038,REQ-FIN-039,REQ-FIN-040,REQ-FIN-041,REQ-FIN-042,REQ-FIN-043,REQ-FIN-044,REQ-FIN-045,REQ-FIN-046,REQ-FIN-047,REQ-FIN-048,REQ-FIN-049,REQ-FIN-050,REQ-FIN-051,REQ-FIN-052,REQ-FIN-053,REQ-FIN-054,REQ-FIN-055,REQ-FIN-056,REQ-FIN-057,REQ-FIN-058,REQ-FIN-059,REQ-FIN-060,REQ-FIN-061,REQ-FIN-062,REQ-FIN-063,REQ-FIN-064,REQ-FIN-065,REQ-FIN-066,REQ-FIN-067,REQ-FIN-068,REQ-FIN-069,REQ-FIN-070,REQ-FIN-071,REQ-FIN-072,REQ-FIN-073,REQ-FIN-074,REQ-FIN-075,REQ-FIN-076,REQ-FIN-077,AC-FIN-001,AC-FIN-002,AC-FIN-003,AC-FIN-004,AC-FIN-005,AC-FIN-006,AC-FIN-007,AC-FIN-008,AC-FIN-009,AC-FIN-010,AC-FIN-011,AC-FIN-012,AC-FIN-013,AC-FIN-014,AC-FIN-015,AC-FIN-016,AC-FIN-017,AC-FIN-018,AC-FIN-019,AC-FIN-020,AC-FIN-021,AC-FIN-022,AC-FIN-023,AC-FIN-024,AC-FIN-025,AC-FIN-026,AC-FIN-027,AC-FIN-028,AC-FIN-029,AC-FIN-030,AC-FIN-031,AC-FIN-032,AC-FIN-033,AC-FIN-034,AC-FIN-035,AC-FIN-036,AC-FIN-037,AC-FIN-038,AC-FIN-039,AC-FIN-040,AC-FIN-041,AC-FIN-042,AC-FIN-043,AC-FIN-044,AC-FIN-045,AC-FIN-046,AC-FIN-047,AC-FIN-048,AC-FIN-049,AC-FIN-050,AC-FIN-051,AC-FIN-052,AC-FIN-053,AC-FIN-054,AC-FIN-055,AC-FIN-056,AC-FIN-057,AC-FIN-058,AC-FIN-059,AC-FIN-060,AC-FIN-061,AC-FIN-062,AC-FIN-063,AC-FIN-064,AC-FIN-065,AC-FIN-066,AC-FIN-067,AC-FIN-068,AC-FIN-069,AC-FIN-070,AC-FIN-071,AC-FIN-072,AC-FIN-073,AC-FIN-074,AC-FIN-075,AC-FIN-076,AC-FIN-077,AC-FIN-078,AC-FIN-079,AC-FIN-080,AC-FIN-081,AC-FIN-082,AC-FIN-083 -->
## PHASE — TEST-PLAN-BE

TC count is 52, far above the split threshold of 12, so this phase is split into the two groups
the engine names: rule-driven scenarios (violations and state transitions) and endpoint-driven
scenarios (happy paths, permission, paging and empty results). Every TC sits inside one of them.

<!-- SUB:RULE-SCENARIOS:START traces=REQ-FIN-002,REQ-FIN-003,REQ-FIN-006,REQ-FIN-009,REQ-FIN-011,REQ-FIN-012,REQ-FIN-013,REQ-FIN-014,REQ-FIN-015,REQ-FIN-016,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,REQ-FIN-026,REQ-FIN-029,REQ-FIN-030,REQ-FIN-032,REQ-FIN-033,REQ-FIN-034,REQ-FIN-035,REQ-FIN-048,REQ-FIN-051,REQ-FIN-052,REQ-FIN-053,REQ-FIN-057,REQ-FIN-058,REQ-FIN-059,REQ-FIN-060,REQ-FIN-061,REQ-FIN-064,REQ-FIN-065,REQ-FIN-067,REQ-FIN-068,REQ-FIN-069,REQ-FIN-072,REQ-FIN-073,REQ-FIN-074,REQ-FIN-076,AC-FIN-002,AC-FIN-003,AC-FIN-006,AC-FIN-009,AC-FIN-011,AC-FIN-012,AC-FIN-013,AC-FIN-014,AC-FIN-015,AC-FIN-016,AC-FIN-017,AC-FIN-018,AC-FIN-019,AC-FIN-020,AC-FIN-021,AC-FIN-026,AC-FIN-029,AC-FIN-030,AC-FIN-032,AC-FIN-033,AC-FIN-034,AC-FIN-035,AC-FIN-048,AC-FIN-053,AC-FIN-054,AC-FIN-055,AC-FIN-056,AC-FIN-057,AC-FIN-061,AC-FIN-062,AC-FIN-063,AC-FIN-064,AC-FIN-065,AC-FIN-069,AC-FIN-070,AC-FIN-072,AC-FIN-073,AC-FIN-074,AC-FIN-077,AC-FIN-078,AC-FIN-079,AC-FIN-080,AC-FIN-082 -->
### SUB — RULE-SCENARIOS

The ACs whose Then names a `RULE-*` violation or a state transition. Each one asserts the catalog
code and both message strings, or the stored state the rule governs.

**v2 cases open the SUB** (TC-FIN-094 … TC-FIN-128, the rule-driven half of CS-FIN-001), and the
v1 cases follow unchanged. Every API-FIN-020 case below is called by the event consumer's own
service principal holding `PERM_FIN_JOURNAL_ENTRIES_CREATE`. Each one asserts that a rejected
event leaves **no row at all**: no JournalEntry carrying its reference, no line, no line
dimension and no `docNo` drawn from the fiscal-year series (SRS A7). The search API-FIN-018 has
no `eventReference` filter, so that absence is read at the store (DBF-FIN-041).

<!-- TC:TC-FIN-094:START traces=AC-FIN-048,REQ-FIN-048,API-FIN-011 -->
### TC-FIN-094 — a DIRECT rule line is refused as an unsupported derivation
Derived from : AC-FIN-048  (REQ-FIN-048)
Exercises    : API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : RULE-FIN-018 → FIN-422-UNSUPPORTED-DERIVATION (422)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: `ACCOUNT_DERIVATION_TYPE` holds exactly two active values in MDL, CONSTANT and
               MAPPING, DIRECT having been deactivated by db-script v2 BLOCK 8; an EventTypeRule
               (ENT-FIN-009) exists; a finance administrator holding `PERM_FIN_RULES_UPDATE`
Steps        : 1. POST `/api/v1/fin/event-rules/{rule id}/lines` with
                  `accountDerivationTypeCode` = DIRECT and otherwise well-formed line fields
Expected     : 422 with code FIN-422-UNSUPPORTED-DERIVATION —
               ar: «نوع اشتقاق الحساب غير مدعوم؛ المسموح قيمة ثابتة أو ربط» ·
               en: "Unsupported account derivation type; only constant or mapping is allowed".
               The answer is this rule's own code, **not** the generic FIN-400-INVALID-LOOKUP:
               membership in MDL's active values is RULE-FIN-018's own check. No RuleLine row is
               created and the rule's line set is unchanged
Test data    : DIRECT — AC-FIN-048's own value; the other line fields are placeholders
<!-- TC:TC-FIN-094:END -->

<!-- TC:TC-FIN-099:START traces=AC-FIN-053,REQ-FIN-051,API-FIN-020 -->
### TC-FIN-099 — an event missing the business field its MAPPING line reads is refused
Derived from : AC-FIN-053  (REQ-FIN-051)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-021 → FIN-422-MISSING-BUSINESS-FIELD (422)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: the PAYMENT_RECEIVED rule of AC-FIN-050, whose debit line is MAPPING on
               PAYMENT_METHOD, with its mappings for "CASH" and "CHEQUE"; an Open period covering
               the event date
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3005`, type
                  PAYMENT_RECEIVED and a `fields` map that has **no** PAYMENT_METHOD key
Expected     : 422 with code FIN-422-MISSING-BUSINESS-FIELD —
               ar: «الحدث لا يحمل الحقل التجاري الذي تطلبه القاعدة» ·
               en: "The event does not carry the business field the rule requires".
               No default account is substituted: a missing fact is never read as "cash". No
               JournalEntry exists with eventReference `EVT-3005`
Test data    : `EVT-3005` — AC-FIN-053's own reference; the amount is a placeholder
<!-- TC:TC-FIN-099:END -->

<!-- TC:TC-FIN-100:START traces=AC-FIN-054,REQ-FIN-052,API-FIN-020 -->
### TC-FIN-100 — a mapped account that has since gained a child is refused at post time
Derived from : AC-FIN-054  (REQ-FIN-052)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event, after
               API-FIN-002 POST /api/v1/fin/accounts as setup
Rule / code  : RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT (409)
Scenario     : VIOLATION · data class EDGE · language ALL
Preconditions: the mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102 was
               saved while 1102 was an active leaf; a child account has **since** been created
               under 1102 through API-FIN-002, which sets 1102's `isLeafFl` (DBF-FIN-008) to
               false; the PAYMENT_RECEIVED rule of AC-FIN-050; an Open period
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3006` and
                  `fields.PAYMENT_METHOD` = "CHEQUE"
Expected     : 409 with code FIN-409-NOT-POSTABLE-ACCOUNT —
               ar: «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
               en: "The target account does not accept direct posting".
               The mapped account is validated exactly as a CONSTANT one: the save-time check of
               RULE-FIN-023 does not replace the post-time check. No JournalEntry exists with
               eventReference `EVT-3006`
Test data    : 1102 and `EVT-3006` — AC-FIN-054's own values; the child account is a placeholder
<!-- TC:TC-FIN-100:END -->

<!-- TC:TC-FIN-101:START traces=AC-FIN-055,REQ-FIN-052,API-FIN-020 -->
### TC-FIN-101 — a mapped account that has since been deactivated is refused at post time
Derived from : AC-FIN-055  (REQ-FIN-052)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event, after
               API-FIN-004 PUT /api/v1/fin/accounts/{id}/deactivate as setup
Rule / code  : RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT (409)
Scenario     : VIOLATION · data class EDGE · language ALL
Preconditions: the mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102, saved
               while 1102 was active; 1102 has **since** been deactivated through API-FIN-004
               (`isActiveFl` false, DBF-FIN-009); the PAYMENT_RECEIVED rule of AC-FIN-050; an Open
               period
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3007` and
                  `fields.PAYMENT_METHOD` = "CHEQUE"
Expected     : 409 with code FIN-409-NOT-POSTABLE-ACCOUNT and the same message as TC-FIN-100 —
               ar: «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
               en: "The target account does not accept direct posting".
               No JournalEntry exists with eventReference `EVT-3007`
Test data    : 1102 and `EVT-3007` — AC-FIN-055's own values
<!-- TC:TC-FIN-101:END -->

<!-- TC:TC-FIN-102:START traces=AC-FIN-056,REQ-FIN-053,API-FIN-020 -->
### TC-FIN-102 — a business value with no mapping is refused, never defaulted
Derived from : AC-FIN-056  (REQ-FIN-053)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-020 → FIN-422-UNMAPPED-VALUE (422)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: the PAYMENT_RECEIVED rule and mappings of AC-FIN-050 ("CASH" → 1101,
               "CHEQUE" → 1102), and **no** mapping of any state for the PAYMENT_METHOD value
               "TRANSFER"; an Open period
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3008` and
                  `fields.PAYMENT_METHOD` = "TRANSFER"
Expected     : 422 with code FIN-422-UNMAPPED-VALUE —
               ar: «لا يوجد ربط حساب نشط للقيمة التجارية في الحدث» ·
               en: "No active account mapping exists for the business value in the event".
               Neither 1101 nor 1102 is used in its place. No JournalEntry exists with
               eventReference `EVT-3008`
Test data    : "TRANSFER" and `EVT-3008` — AC-FIN-056's own values
<!-- TC:TC-FIN-102:END -->

<!-- TC:TC-FIN-103:START traces=AC-FIN-057,REQ-FIN-053,API-FIN-020 -->
### TC-FIN-103 — an inactive mapping counts as no mapping
Derived from : AC-FIN-057  (REQ-FIN-053)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event, after
               API-FIN-041 PUT /api/v1/fin/account-mappings/{id}/deactivate as setup
Rule / code  : RULE-FIN-020 → FIN-422-UNMAPPED-VALUE (422)
Scenario     : VIOLATION · data class EDGE · language ALL
Preconditions: the mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CASH") → account 1101 exists with
               `isActiveFl` false (DBF-FIN-154), and no other mapping for "CASH"; the
               PAYMENT_RECEIVED rule of AC-FIN-050; an Open period
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3009` and
                  `fields.PAYMENT_METHOD` = "CASH"
Expected     : 422 with code FIN-422-UNMAPPED-VALUE and the message of TC-FIN-102 —
               ar: «لا يوجد ربط حساب نشط للقيمة التجارية في الحدث» ·
               en: "No active account mapping exists for the business value in the event".
               The inactive row is not read as a fallback. No JournalEntry exists with
               eventReference `EVT-3009`
Test data    : "CASH", 1101 and `EVT-3009` — AC-FIN-057's own values
<!-- TC:TC-FIN-103:END -->

<!-- TC:TC-FIN-107:START traces=AC-FIN-061,REQ-FIN-057,API-FIN-041,API-FIN-020 -->
### TC-FIN-107 — a deactivated mapping stops resolving from the next event on
Derived from : AC-FIN-061  (REQ-FIN-057)
Exercises    : API-FIN-041 PUT /api/v1/fin/account-mappings/{id}/deactivate, then
               API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-020 → FIN-422-UNMAPPED-VALUE (422), on the event that follows
Scenario     : STATE · data class VALID · language ALL
Preconditions: an active mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CASH") → account 1101; the
               PAYMENT_RECEIVED rule of AC-FIN-050; an Open period; a finance administrator
               holding `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE`
Steps        : 1. PUT `/api/v1/fin/account-mappings/{id}/deactivate`
               2. POST `/api/v1/fin/journal-entries/from-event` with a new reference and
                  `fields.PAYMENT_METHOD` = "CASH"
Expected     : step 1 → 200 · the mapping with `isActiveFl` = false; the row is kept, never
                  hard-deleted.
               step 2 → 422 with code FIN-422-UNMAPPED-VALUE —
                  ar: «لا يوجد ربط حساب نشط للقيمة التجارية في الحدث» ·
                  en: "No active account mapping exists for the business value in the event" —
                  exactly as TC-FIN-103. Entries posted through the mapping before step 1 keep
                  their account (REQ-FIN-058)
Test data    : "CASH" and 1101 — AC-FIN-061's own values; the event reference is a placeholder
<!-- TC:TC-FIN-107:END -->

<!-- TC:TC-FIN-108:START traces=AC-FIN-062,REQ-FIN-058,API-FIN-040,API-FIN-022,API-FIN-028 -->
### TC-FIN-108 — changing a mapping never reaches an entry already posted through it
Derived from : AC-FIN-062  (REQ-FIN-058)
Exercises    : API-FIN-040 PUT /api/v1/fin/account-mappings/{id}, then
               API-FIN-022 GET /api/v1/fin/journal-entries/{id} and
               API-FIN-028 GET /api/v1/fin/reports/account-ledger
Rule / code  : — (no rule fires; the invariant holds by structure — a posted line stores its
               resolved accountId and references no mapping)
Scenario     : STATE · data class VALID · language ALL
Preconditions: entry `JV-2026-000500`, POSTED from event `EVT-3010`, whose debit line is on
               account 5220 through the mapping (EXPENSE_PAID, EXPENSE_TYPE_CODE, "RENT");
               account 5225 is an active leaf; a caller holding
               `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE`, `PERM_FIN_JOURNAL_ENTRIES_VIEW` and
               `PERM_FIN_ACCOUNT_LEDGER_VIEW`
Steps        : 1. PUT `/api/v1/fin/account-mappings/{RENT id}` with `accountId` = 5225
               2. GET `/api/v1/fin/journal-entries/{JV-2026-000500 id}`
               3. GET `/api/v1/fin/reports/account-ledger` for account 5220 over the entry's date
Expected     : step 1 → 200 with `accountId` = 5225.
               step 2 → the debit line of `JV-2026-000500` still shows account 5220.
               step 3 → the ledger of 5220 still contains that line, and the ledger of 5225 does
                  not. A mapping governs later derivations only
Test data    : `JV-2026-000500`, `EVT-3010`, 5220, 5225 and "RENT" — AC-FIN-062's own values
<!-- TC:TC-FIN-108:END -->

<!-- TC:TC-FIN-109:START traces=AC-FIN-063,REQ-FIN-059,API-FIN-039 -->
### TC-FIN-109 — a second active mapping for the same key is refused
Derived from : AC-FIN-063  (REQ-FIN-059)
Exercises    : API-FIN-039 POST /api/v1/fin/account-mappings
Rule / code  : RULE-FIN-022 → FIN-409-MAPPING-DUP (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an active mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102;
               account 1103 is an active leaf, so nothing but the key can fail; a finance
               administrator holding `PERM_FIN_ACCOUNT_MAPPINGS_CREATE`
Steps        : 1. POST `/api/v1/fin/account-mappings` with (PAYMENT_RECEIVED, PAYMENT_METHOD,
                  "CHEQUE") and `accountId` = 1103
               2. POST `/api/v1/fin/account-mappings/search` with those three key filters and
                  `isActiveFl` = true
Expected     : step 1 → 409 with code FIN-409-MAPPING-DUP —
                  ar: «يوجد بالفعل ربط نشط لنوع الحدث والحقل التجاري والقيمة نفسها» ·
                  en: "An active mapping already exists for this event type, business field and
                  value".
               step 2 → exactly 1 active row for that key, still on account 1102
Test data    : 1102, 1103 and "CHEQUE" — AC-FIN-063's own values
<!-- TC:TC-FIN-109:END -->

<!-- TC:TC-FIN-110:START traces=AC-FIN-064,REQ-FIN-060,API-FIN-039 -->
### TC-FIN-110 — a mapping to a parent account is refused when it is saved
Derived from : AC-FIN-064  (REQ-FIN-060)
Exercises    : API-FIN-039 POST /api/v1/fin/account-mappings
Rule / code  : RULE-FIN-023 → FIN-422-MAPPED-ACCOUNT-INELIGIBLE (422)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: account 1100 is a parent (rollup) account with `isLeafFl` = false; the mapping
               key submitted has no active mapping and its codes are valid, so only the account
               can fail; a finance administrator holding `PERM_FIN_ACCOUNT_MAPPINGS_CREATE`
Steps        : 1. POST `/api/v1/fin/account-mappings` with a valid key and `accountId` = 1100
Expected     : 422 with code FIN-422-MAPPED-ACCOUNT-INELIGIBLE —
               ar: «الحساب المربوط يجب أن يكون حسابًا ورقيًا نشطًا» ·
               en: "The mapped account must be an active leaf account".
               No AccountMapping row is written: the gap is caught at configuration, not first
               discovered as a failed event
Test data    : 1100 — AC-FIN-064's own value; the key is a placeholder
<!-- TC:TC-FIN-110:END -->

<!-- TC:TC-FIN-111:START traces=AC-FIN-065,REQ-FIN-061,API-FIN-039 -->
### TC-FIN-111 — a misspelt payment method is refused as a mapping value
Derived from : AC-FIN-065  (REQ-FIN-061)
Exercises    : API-FIN-039 POST /api/v1/fin/account-mappings
Rule / code  : RULE-FIN-024 → FIN-422-INVALID-PAYMENT-METHOD (422)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: PAYMENT_METHOD holds CASH and CHEQUE as active values in MDL (CHEQUE seeded by
               db-script v2 BLOCK 8, CASH added as host data); the account submitted is an active
               leaf; a finance administrator holding `PERM_FIN_ACCOUNT_MAPPINGS_CREATE`
Steps        : 1. POST `/api/v1/fin/account-mappings` with (PAYMENT_RECEIVED, PAYMENT_METHOD,
                  "CHEQEU") and that account
Expected     : 422 with code FIN-422-INVALID-PAYMENT-METHOD —
               ar: «القيمة ليست طريقة دفع معرَّفة» ·
               en: "The value is not a defined payment method".
               No AccountMapping row is written. The check applies only because the business
               field is PAYMENT_METHOD; for any other field the host's code is stored as-is
Test data    : "CHEQEU" — AC-FIN-065's own misspelling; the account is a placeholder
<!-- TC:TC-FIN-111:END -->

<!-- TC:TC-FIN-115:START traces=AC-FIN-069,REQ-FIN-064,API-FIN-011 -->
### TC-FIN-115 — a rule line tagging one dimension twice is refused
Derived from : AC-FIN-069  (REQ-FIN-064)
Exercises    : API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : RULE-FIN-025 → FIN-422-DIMENSION-TAG-DUP (422)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an EventTypeRule; the dimension BRANCH is active and holds a value with code
               "BR-01"; a finance administrator holding `PERM_FIN_RULES_UPDATE`
Steps        : 1. POST `/api/v1/fin/event-rules/{rule id}/lines` with an otherwise valid line
                  whose `dimensionTags` are {BRANCH: CONSTANT "BR-01"} and
                  {BRANCH: BUSINESS_FIELD BRANCH_CODE}
Expected     : 422 with code FIN-422-DIMENSION-TAG-DUP —
               ar: «لا يجوز وسم البُعد نفسه مرتين على سطر القاعدة» ·
               en: "A rule line cannot tag the same dimension twice".
               No RuleLine row is created and no RuleLineDimension row either: the line and its
               tags are written in one transaction or not at all
Test data    : BRANCH, "BR-01" and BRANCH_CODE — AC-FIN-069's own values
<!-- TC:TC-FIN-115:END -->

<!-- TC:TC-FIN-116:START traces=AC-FIN-070,REQ-FIN-065,API-FIN-011 -->
### TC-FIN-116 — a CONSTANT tag citing another dimension's value is refused
Derived from : AC-FIN-070  (REQ-FIN-065)
Exercises    : API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : RULE-FIN-026 → FIN-422-DIMENSION-TAG-SOURCE (422)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: the dimensions BRANCH and ORGANISATION are active, and "ORG-1" is a value of
               ORGANISATION; an EventTypeRule; a finance administrator holding
               `PERM_FIN_RULES_UPDATE`
Steps        : 1. POST `/api/v1/fin/event-rules/{rule id}/lines` with an otherwise valid line
                  tagged {BRANCH: CONSTANT "ORG-1"}
Expected     : 422 with code FIN-422-DIMENSION-TAG-SOURCE —
               ar: «وسم البُعد لا يطابق مصدر قيمته» ·
               en: "The dimension tag does not match its value source".
               No RuleLine row is created
Test data    : BRANCH, ORGANISATION and "ORG-1" — AC-FIN-070's own values
<!-- TC:TC-FIN-116:END -->

<!-- TC:TC-FIN-118:START traces=AC-FIN-072,REQ-FIN-067,API-FIN-020 -->
### TC-FIN-118 — an event resolving to an inactive dimension value is refused
Derived from : AC-FIN-072  (REQ-FIN-067)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event, after
               API-FIN-035 PUT /api/v1/fin/dimensions/values/{id}/deactivate as setup
Rule / code  : RULE-FIN-009 → FIN-409-INVALID-DIMENSION (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: the rule line of AC-FIN-066 on the PAYMENT_RECEIVED rule; BRANCH holds a value
               with code "BR-09" that has been deactivated through API-FIN-035; an Open period
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3013` and
                  `fields.BRANCH_CODE` = "BR-09"
Expected     : 409 with code FIN-409-INVALID-DIMENSION —
               ar: «قيمة البُعد غير صالحة» · en: "The dimension value is invalid".
               The value **matches** a defined code, so this is RULE-FIN-009's rejection and
               **not** FIN-422-UNRESOLVED-DIMENSION-VALUE (RULE-FIN-027's Test-Hint). The check is
               the same one a manual line receives. No JournalEntry exists with eventReference
               `EVT-3013`
Test data    : "BR-09" and `EVT-3013` — AC-FIN-072's own values
<!-- TC:TC-FIN-118:END -->

<!-- TC:TC-FIN-119:START traces=AC-FIN-073,REQ-FIN-068,API-FIN-020 -->
### TC-FIN-119 — an event missing the business field a dimension tag names is refused
Derived from : AC-FIN-073  (REQ-FIN-068)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-021 → FIN-422-MISSING-BUSINESS-FIELD (422)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: the rule line of AC-FIN-066 on the PAYMENT_RECEIVED rule, whose BRANCH tag is
               BUSINESS_FIELD BRANCH_CODE; an Open period
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3014` and a
                  `fields` map that has **no** BRANCH_CODE key
Expected     : 422 with code FIN-422-MISSING-BUSINESS-FIELD —
               ar: «الحدث لا يحمل الحقل التجاري الذي تطلبه القاعدة» ·
               en: "The event does not carry the business field the rule requires".
               The entry is never posted without its branch attribution. No JournalEntry exists
               with eventReference `EVT-3014`
Test data    : BRANCH_CODE and `EVT-3014` — AC-FIN-073's own values
<!-- TC:TC-FIN-119:END -->

<!-- TC:TC-FIN-120:START traces=AC-FIN-074,REQ-FIN-069,API-FIN-020 -->
### TC-FIN-120 — an event dimension value that matches no defined code is refused
Derived from : AC-FIN-074  (REQ-FIN-069)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-027 → FIN-422-UNRESOLVED-DIMENSION-VALUE (422)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: the rule line of AC-FIN-066 on the PAYMENT_RECEIVED rule; BRANCH holds **no**
               value with code "BR-99", active or not; an Open period
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3015` and
                  `fields.BRANCH_CODE` = "BR-99"
Expected     : 422 with code FIN-422-UNRESOLVED-DIMENSION-VALUE —
               ar: «قيمة البُعد في الحدث لا تطابق أي قيمة معرَّفة لهذا البُعد» ·
               en: "The dimension value in the event matches no defined value of this
               dimension".
               No translation and no guess is attempted. No JournalEntry exists with
               eventReference `EVT-3015`
Test data    : "BR-99" and `EVT-3015` — AC-FIN-074's own values
<!-- TC:TC-FIN-120:END -->

<!-- TC:TC-FIN-123:START traces=AC-FIN-077,REQ-FIN-072,API-FIN-020 -->
### TC-FIN-123 — a repeated event reference is answered as a duplicate
Derived from : AC-FIN-077  (REQ-FIN-072)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-004 → FIN-409-DUPLICATE-EVENT (409) — the pre-check detection
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a POSTED entry with eventReference `EVT-4001`; its event type still carries its
               active rule
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with another event whose
                  reference is `EVT-4001`
Expected     : 409 with code FIN-409-DUPLICATE-EVENT —
               ar: «تم بالفعل ترحيل قيد لهذا المرجع» ·
               en: "An entry for this event reference has already been posted".
               Exactly 1 JournalEntry exists with eventReference `EVT-4001`. For the consumer
               this code means "already posted — acknowledge" (DOC phase)
Test data    : `EVT-4001` — AC-FIN-077's own value
<!-- TC:TC-FIN-123:END -->

<!-- TC:TC-FIN-124:START traces=AC-FIN-078,REQ-FIN-072,API-FIN-020 -->
### TC-FIN-124 — two simultaneous events with one reference post once and never answer 500
Derived from : AC-FIN-078  (REQ-FIN-072)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event, twice concurrently
Rule / code  : RULE-FIN-004 → FIN-409-DUPLICATE-EVENT (409) — the store-refusal detection
Scenario     : VIOLATION · data class EDGE · language ALL
Preconditions: no entry with eventReference `EVT-4002`; an active rule for the event's type; an
               Open period; two callers under the consumer's service principal
Steps        : 1. submit two identical events with reference `EVT-4002` so that both pass the
                  duplicate pre-check before either commits — the harness holds both requests
                  past the pre-check and releases them together
               2. read back every entry carrying that reference
Expected     : step 1 → one call answers 200 with the POSTED entry; the other answers 409 with
                  code FIN-409-DUPLICATE-EVENT and the message of TC-FIN-123 —
                  ar: «تم بالفعل ترحيل قيد لهذا المرجع» ·
                  en: "An entry for this event reference has already been posted".
                  The losing call is **never** FIN-500 nor any generic data-integrity answer:
                  UQ_FIN_JOURNAL_ENTRY_EVENT_REF refuses the second insert and the refusal is
                  translated to this rule's code.
               step 2 → exactly 1 entry for `EVT-4002`
Test data    : `EVT-4002` — AC-FIN-078's own value
<!-- TC:TC-FIN-124:END -->

<!-- TC:TC-FIN-125:START traces=AC-FIN-079,REQ-FIN-073,API-FIN-020 -->
### TC-FIN-125 — an event into a soft-closed period gets the distinct period-not-open code
Derived from : AC-FIN-079  (REQ-FIN-073)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-008 → FIN-409-PERIOD-NOT-OPEN (409)
Scenario     : VIOLATION · data class EDGE · language ALL
Preconditions: the period covering 2026-10-05 is SOFT_CLOSE; an active rule for the event's
               type whose accounts are leaf and active, so the period is the only failing fact
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-4003` and
                  `docDate` 2026-10-05
Expected     : 409 with code FIN-409-PERIOD-NOT-OPEN —
               ar: «الفترة المستهدفة غير مفتوحة» · en: "The target period is not open".
               The code differs from every validation and configuration code API-FIN-020 lists —
               every FIN-400-*, every FIN-422-*, and FIN-422-NO-DEFINED-PERIOD in particular — so
               the consumer can wait and retry on it alone. No JournalEntry exists with
               eventReference `EVT-4003`
Test data    : 2026-10-05 and `EVT-4003` — AC-FIN-079's own values
<!-- TC:TC-FIN-125:END -->

<!-- TC:TC-FIN-126:START traces=AC-FIN-080,REQ-FIN-074,API-FIN-020 -->
### TC-FIN-126 — an event date no period covers is a configuration error, not a retry signal
Derived from : AC-FIN-080  (REQ-FIN-074)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-028 → FIN-422-NO-DEFINED-PERIOD (422)
Scenario     : VIOLATION · data class EDGE · language ALL
Preconditions: no FiscalPeriod whose `startDate` … `endDate` (DBF-FIN-080, DBF-FIN-081) covers
               2027-01-15; an active rule for the event's type
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-4004` and
                  `docDate` 2027-01-15
Expected     : 422 with code FIN-422-NO-DEFINED-PERIOD —
               ar: «لا توجد فترة محاسبية معرَّفة تغطي تاريخ الحدث» ·
               en: "No accounting period is defined for the event date".
               The code differs from FIN-409-PERIOD-NOT-OPEN, and the v1 answer FIN-404-PERIOD is
               no longer raised on this path. No JournalEntry exists with eventReference
               `EVT-4004`
Test data    : 2027-01-15 and `EVT-4004` — AC-FIN-080's own values
<!-- TC:TC-FIN-126:END -->

<!-- TC:TC-FIN-128:START traces=AC-FIN-082,REQ-FIN-076,API-FIN-020 -->
### TC-FIN-128 — a MAPPING remainder line still absorbs the rounding difference exactly
Derived from : AC-FIN-082  (REQ-FIN-076)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-010 → success path, no code · RULE-FIN-003 and RULE-FIN-006 pass
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an active rule distributing the event amount as 33% + 33% + remainder, whose one
               remainder line (`isRemainderFl` true) is MAPPING on PAYMENT_METHOD; an active
               mapping for "CHEQUE" to an active leaf account; an Open period
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-4005`,
                  amount 100.00 and `fields.PAYMENT_METHOD` = "CHEQUE"
Expected     : 200 · POSTED. The two percentage lines total 66.00, the remainder line is
               exactly 34.00 **on the mapped account**, and the entry's debits equal its credits
               at 100.00. A mapped remainder line computes exactly like any other: every v1
               posting check holds on the new path unchanged
Test data    : 100.00 · 33% · 33% · 66.00 · 34.00 · "CHEQUE" · `EVT-4005` — AC-FIN-082's own
<!-- TC:TC-FIN-128:END -->

<!-- TC:TC-FIN-002:START traces=AC-FIN-002,REQ-FIN-002,API-FIN-003 -->
### TC-FIN-002 — an account with children cannot be marked as accepting direct posting
Derived from : AC-FIN-002  (REQ-FIN-002)
Exercises    : API-FIN-003 PUT /api/v1/fin/accounts/{id}
Rule / code  : RULE-FIN-001 → FIN-409-HAS-CHILDREN (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a finance administrator holding `PERM_FIN_ACCOUNTS_UPDATE` (and the
               `PERM_FIN_ACCOUNTS_VIEW` gateway); an account `<parent>` (ENT-FIN-001) that has at
               least one child row whose `parentAccountId` (DBF-FIN-007) points at it
Steps        : 1. PUT `/api/v1/fin/accounts/{parent id}` with `isLeafFl` = true
Expected     : 409 · `LocalizedException → {code, messageAr, messageEn}` with code
               FIN-409-HAS-CHILDREN —
               ar: «لا يمكن لحساب له حسابات فرعية أن يقبل ترحيلاً مباشرًا» ·
               en: "An account with sub-accounts cannot accept direct posting".
               The stored `isLeafFl` (DBF-FIN-008) is unchanged; a subsequent API-FIN-001 search
               returns the parent still non-leaf
Test data    : `<parent>` and `<child>` are placeholders — AC-FIN-002 names no code; the child
               relation is the only fact the AC states
<!-- TC:TC-FIN-002:END -->

<!-- TC:TC-FIN-003:START traces=AC-FIN-003,REQ-FIN-003,API-FIN-004 -->
### TC-FIN-003 — deactivating an account blocks every later posting to it
Derived from : AC-FIN-003  (REQ-FIN-003)
Exercises    : API-FIN-004 PUT /api/v1/fin/accounts/{id}/deactivate, then
               API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT (409), on the second half
Scenario     : STATE · data class VALID · language ALL
Preconditions: an active leaf account `<A>`; an Open period whose `[startDate, endDate]` covers
               the document date; a caller holding `PERM_FIN_ACCOUNTS_UPDATE` and
               `PERM_FIN_JOURNAL_ENTRIES_CREATE`
Steps        : 1. PUT `/api/v1/fin/accounts/{A}/deactivate`
               2. POST `/api/v1/fin/journal-entries` with a balanced two-line entry, one line
                  targeting `<A>`
Expected     : step 1 → 200 · `AccountResponse` with `isActiveFl` = false (DBF-FIN-009).
               step 2 → 409 with code FIN-409-NOT-POSTABLE-ACCOUNT —
               ar: «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
               en: "The target account does not accept direct posting".
               Nothing is written: REQ-FIN-015's one-transaction rule means no DRAFT survives
Test data    : `<A>` is a placeholder; the AC names no code
<!-- TC:TC-FIN-003:END -->

<!-- TC:TC-FIN-006:START traces=AC-FIN-006,REQ-FIN-006,API-FIN-007 -->
### TC-FIN-006 — a duplicate code under the same dimension is refused
Derived from : AC-FIN-006  (REQ-FIN-006)
Exercises    : API-FIN-007 POST /api/v1/fin/dimensions/{id}/values
Rule / code  : RULE-FIN-002 → FIN-409-DIMVALUE-DUP (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a caller holding `PERM_FIN_DIMENSIONS_CREATE`; a dimension `PROJECT`
               (ENT-FIN-002) already holding a value with code `NORTH`
Steps        : 1. POST `/api/v1/fin/dimensions/{PROJECT id}/values` with code `NORTH`, both
                  names and a sortOrder
Expected     : 409 with code FIN-409-DIMVALUE-DUP —
               ar: «هذا الرمز مستخدم بالفعل ضمن هذا البُعد» ·
               en: "This code is already used within this dimension".
               No second row is stored: API-FIN-008 filtered by that dimension returns exactly
               one `NORTH` value
Test data    : `NORTH` — AC-FIN-006's own value; `PROJECT` — ENT-FIN-002's own example code
<!-- TC:TC-FIN-006:END -->

<!-- TC:TC-FIN-009:START traces=AC-FIN-009,REQ-FIN-009,API-FIN-011 -->
### TC-FIN-009 — a percentage line set with no remainder line is refused
Derived from : AC-FIN-009  (REQ-FIN-009)
Exercises    : API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : RULE-FIN-003 → FIN-409-REMAINDER-COUNT (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a caller holding `PERM_FIN_RULES_UPDATE`; an EventTypeRule (ENT-FIN-009) whose
               existing line set already carries one `PERCENTAGE`-distributed line and no line
               marked `isRemainderFl` (DBF-FIN-107)
Steps        : 1. POST `/api/v1/fin/event-rules/{rule id}/lines` with a second line whose
                  `distributionTypeCode` is `PERCENTAGE` and whose `isRemainderFl` is false
Expected     : 409 with code FIN-409-REMAINDER-COUNT —
               ar: «يلزم تحديد سطر باقٍ واحد بالضبط عند وجود توزيع نسبي» ·
               en: "Exactly one remainder line is required when any percentage distribution is
               present".
               The rule's stored line set is unchanged, and stays unchanged until exactly one
               line is marked remainder — the AC's own wording
Test data    : `PERCENTAGE` — a seeded `DISTRIBUTION_TYPE` value (SRS A6); the two-line count is
               AC-FIN-009's own
<!-- TC:TC-FIN-009:END -->

<!-- TC:TC-FIN-011:START traces=AC-FIN-011,REQ-FIN-011,API-FIN-020 -->
### TC-FIN-011 — a repeated event reference never produces a second entry
Derived from : AC-FIN-011  (REQ-FIN-011)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-004 → FIN-409-DUPLICATE-EVENT (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a POSTED JournalEntry (ENT-FIN-004) whose `eventReference` (DBF-FIN-041) is
               `EVT-1001`; the event type still carries its active rule; the caller is the Event
               consumer's own service principal holding `PERM_FIN_JOURNAL_ENTRIES_CREATE`
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with a canonical event whose
                  reference is `EVT-1001`
Expected     : 409 with code FIN-409-DUPLICATE-EVENT —
               ar: «تم بالفعل ترحيل قيد لهذا المرجع» ·
               en: "An entry for this event reference has already been posted".
               API-FIN-018 filtered on that reference still returns exactly one entry; no second
               `docNo` is consumed from the fiscal-year series
Test data    : `EVT-1001` — AC-FIN-011's own value
<!-- TC:TC-FIN-011:END -->

<!-- TC:TC-FIN-012:START traces=AC-FIN-012,REQ-FIN-012,API-FIN-020 -->
### TC-FIN-012 — the remainder line absorbs the rounding difference exactly
Derived from : AC-FIN-012  (REQ-FIN-012)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-010 → (success path; no code is raised when the residue is positive)
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an active EventTypeRule whose lines distribute the event amount as 33% + 33% +
               remainder, exactly one line carrying `isRemainderFl` = true (DBF-FIN-107) and
               agreeing with its own `REMAINDER` type code; an Open period covering the document
               date; every derived account leaf and active
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with an event whose amount is
                  100.00
Expected     : 200 · `JournalEntryResponse` with `statusCode` = POSTED. The two percentage lines
               total 66.00, each rounded to the smallest currency unit, and the remainder line is
               exactly 34.00 (DBF-FIN-055, `NUMERIC(18,4)`), so total debits equal total credits
               to the smallest currency unit. The remainder amount is a difference, never a
               percentage — RULE-FIN-010's own statement
Test data    : 100.00 · 33% · 33% · 66.00 · 34.00 — all AC-FIN-012's own values
<!-- TC:TC-FIN-012:END -->

<!-- TC:TC-FIN-013:START traces=AC-FIN-013,REQ-FIN-013,API-FIN-020 -->
### TC-FIN-013 — an event whose type has no active rule is refused
Derived from : AC-FIN-013  (REQ-FIN-013)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-005 → FIN-404-NO-ACTIVE-RULE (404)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an `ACCOUNTING_EVENT_TYPE` value registered in MDL for which FIN holds no active
               EventTypeRule row — reachable either because none was ever created or because the
               only one was retired through API-FIN-034
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with an event of that type
Expected     : 404 with code FIN-404-NO-ACTIVE-RULE —
               ar: «لا توجد قاعدة نشطة لهذا النوع من الأحداث» ·
               en: "No active rule exists for this event type".
               No entry is built and none is posted; the failure is recorded for operator
               follow-up, which is AC-FIN-013's own Then
Test data    : the event type is a placeholder — `ACCOUNTING_EVENT_TYPE` seeds no value (SRS A6:
               host-specific, added as data)
<!-- TC:TC-FIN-013:END -->

<!-- TC:TC-FIN-015:START traces=AC-FIN-015,REQ-FIN-015,API-FIN-019 -->
### TC-FIN-015 — two failing validations are both returned and nothing is posted
Derived from : AC-FIN-015  (REQ-FIN-015)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-006 and RULE-FIN-007 together → FIN-409-UNBALANCED,
               FIN-409-NOT-POSTABLE-ACCOUNT
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an accountant holding `PERM_FIN_JOURNAL_ENTRIES_CREATE`; an Open period; one
               non-leaf (rollup) account among the submitted lines; the submitted `fiscalYearId`,
               `periodId` and `docDate` are coherent, so RULE-FIN-017 does not fail first
Steps        : 1. POST `/api/v1/fin/journal-entries` with an entry that is both unbalanced and
                  targets the rollup account on one line
Expected     : the response carries **both** failures, not the first one only —
               FIN-409-UNBALANCED (ar: «القيد غير متوازن — إجمالي المدين لا يساوي إجمالي الدائن» ·
               en: "The entry is unbalanced — total debits do not equal total credits") and
               FIN-409-NOT-POSTABLE-ACCOUNT (ar: «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
               en: "The target account does not accept direct posting").
               Nothing is written at all: the whole build-through-post sequence is one
               transaction, so no DRAFT row and no `docNo` survive the failed attempt
Test data    : the two failing conditions are AC-FIN-015's own count; account ids are placeholders
<!-- TC:TC-FIN-015:END -->

<!-- TC:TC-FIN-016:START traces=AC-FIN-016,REQ-FIN-016,API-FIN-022 -->
### TC-FIN-016 — a POSTED entry cannot be deleted; only a reversal changes its effect
Derived from : AC-FIN-016  (REQ-FIN-016)
Exercises    : API-FIN-022 GET /api/v1/fin/journal-entries/{id} (the surface assertion) ·
               API-FIN-021 POST /api/v1/fin/journal-entries/{id}/reverse (the only mutation)
Rule / code  : RULE-FIN-016 → no catalog code: the rule is enforced by omission, there being no
               UPDATE and no DELETE mapping on a POSTED row anywhere in the module
Scenario     : STATE · data class EDGE · language ALL
Preconditions: a POSTED JournalEntry; a caller holding `PERM_FIN_JOURNAL_ENTRIES_VIEW` and
               `PERM_FIN_JOURNAL_ENTRIES_REVERSE`
Steps        : 1. enumerate the mappings published on `/api/v1/fin/journal-entries` and on
                  `/api/v1/fin/journal-entries/{id}`
               2. issue a DELETE against `/api/v1/fin/journal-entries/{id}`
               3. GET `/api/v1/fin/journal-entries/{id}`
Expected     : step 1 → the published set is search, create, from-event, reverse and read, and no
                  other; FIN publishes no DELETE endpoint on any screen.
               step 2 → refused by the framework's own routing before any FIN code runs, so **no
                  FIN catalog code is emitted** and none is asserted here.
               step 3 → the entry is byte-identical to before, still `statusCode` = POSTED.
               The only path that changes its effect is API-FIN-021 (REQ-FIN-028), whose own
               effect is a new linked entry, never a mutation of this row
Test data    : none beyond the existing POSTED entry
<!-- TC:TC-FIN-016:END -->

<!-- TC:TC-FIN-017:START traces=AC-FIN-017,REQ-FIN-017,API-FIN-019 -->
### TC-FIN-017 — a DRAFT that passes every check posts directly and locks
Derived from : AC-FIN-017  (REQ-FIN-017)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-016 → no code (the lock is enforced by omission) · RULE-FIN-006,
               RULE-FIN-007, RULE-FIN-008, RULE-FIN-009 all pass
Scenario     : STATE · data class VALID · language ALL
Preconditions: an Open period belonging to the submitted fiscal year and covering the document
               date; two or more balanced lines on leaf, active accounts; every cited dimension
               value active and belonging to its stated dimension
Steps        : 1. POST `/api/v1/fin/journal-entries` with that entry
               2. GET the created entry through API-FIN-022
Expected     : 201 · `statusCode` = POSTED and `postedAt` (DBF-FIN-046) set, with **no separate
               human approval step** between validation and posting — REQ-FIN-017's whole point.
               A `docNo` of the form `JV-{fiscalYearCode}-{NNNNNN}` is present in the response and
               was never present in the request. Step 2 returns the same POSTED row: the entry is
               fully locked, no edit and no delete mapping exists for it
Test data    : `JV-2026-000123` is the format example the SRS itself gives for fiscal year `2026`
<!-- TC:TC-FIN-017:END -->

<!-- TC:TC-FIN-018:START traces=AC-FIN-018,REQ-FIN-018,API-FIN-019 -->
### TC-FIN-018 — an entry out of balance by the smallest currency unit is refused
Derived from : AC-FIN-018  (REQ-FIN-018)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-006 → FIN-409-UNBALANCED (409)
Scenario     : VIOLATION · data class BOUNDARY · language ALL
Preconditions: an Open period; every line on a leaf, active account, so the imbalance is the only
               failing check
Steps        : 1. POST `/api/v1/fin/journal-entries` with debit lines totalling 100.00 and credit
                  lines totalling 99.99
Expected     : 409 with code FIN-409-UNBALANCED —
               ar: «القيد غير متوازن — إجمالي المدين لا يساوي إجمالي الدائن» ·
               en: "The entry is unbalanced — total debits do not equal total credits".
               The imbalance amount is shown, which is AC-FIN-018's own Then. Nothing is posted
               and nothing is stored
Test data    : 100.00 and 99.99 — AC-FIN-018's own values, one hundredth apart, which is the
               smallest currency unit RULE-FIN-006 measures to
<!-- TC:TC-FIN-018:END -->

<!-- TC:TC-FIN-019:START traces=AC-FIN-019,REQ-FIN-019,API-FIN-019 -->
### TC-FIN-019 — a line targeting a rollup account is refused, naming the line
Derived from : AC-FIN-019  (REQ-FIN-019)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an Open period; a balanced line set in which exactly one line targets a non-leaf
               (rollup) account — `isLeafFl` false (DBF-FIN-008) — every other line being leaf and
               active
Steps        : 1. POST `/api/v1/fin/journal-entries` with that entry
Expected     : 409 with code FIN-409-NOT-POSTABLE-ACCOUNT —
               ar: «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
               en: "The target account does not accept direct posting".
               The offending line is named, which is AC-FIN-019's own Then. Nothing is posted.
               The published error body of API-FIN-019 carries the code in
               `fieldErrors[].field` with no line index and no account id, so this half is
               expected to fail against the build until the backend publishes the line — the
               follow-up ADR-FIN-065 records (ADR-FIN-066)
Test data    : account ids are placeholders; the rollup relation is the fact the AC states
<!-- TC:TC-FIN-019:END -->

<!-- TC:TC-FIN-020:START traces=AC-FIN-020,REQ-FIN-020,API-FIN-019 -->
### TC-FIN-020 — the period gate is read at post time, not at build time
Derived from : AC-FIN-020  (REQ-FIN-020)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries, with API-FIN-026 in between
Rule / code  : RULE-FIN-008 → FIN-409-PERIOD-NOT-OPEN (409)
Scenario     : VIOLATION · data class EDGE · language ALL
Preconditions: a period that is OPEN when the entry is composed; an approver holding
               `PERM_FIN_PERIODS_CLOSE_APPROVE`, so the period can be moved to HARD_CLOSE between
               composition and submission (OPEN → SOFT_CLOSE → HARD_CLOSE, SRS A7)
Steps        : 1. compose a balanced entry against that period while it is still Open
               2. soft-close then hard-close the period (API-FIN-025, API-FIN-026)
               3. POST `/api/v1/fin/journal-entries` with the entry composed in step 1
Expected     : 409 with code FIN-409-PERIOD-NOT-OPEN —
               ar: «الفترة المستهدفة غير مفتوحة» ·
               en: "The target period is not open".
               The rejection cites the period's **current** state, which is AC-FIN-020's own Then,
               and holds even though the period was Open when the entry was built
Test data    : none invented; the state sequence is the SRS A7 lifecycle
<!-- TC:TC-FIN-020:END -->

<!-- TC:TC-FIN-021:START traces=AC-FIN-021,REQ-FIN-021,API-FIN-019 -->
### TC-FIN-021 — a line citing an inactive dimension value is refused
Derived from : AC-FIN-021  (REQ-FIN-021)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-009 → FIN-409-INVALID-DIMENSION (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an Open period and a balanced, leaf-account line set; one DimensionValue
               (ENT-FIN-003) whose `isActiveFl` (DBF-FIN-029) is false. That state is reachable
               only through API-FIN-035, the sole endpoint that clears the flag — the setup
               dependency ADR-FIN-009 records
Steps        : 1. PUT `/api/v1/fin/dimensions/values/{id}/deactivate`
               2. POST `/api/v1/fin/journal-entries` with a line citing that dimension value
Expected     : step 1 → 200 with `isActiveFl` = false.
               step 2 → 409 with code FIN-409-INVALID-DIMENSION —
               ar: «قيمة البُعد غير صالحة» · en: "The dimension value is invalid".
               The offending line and dimension are named, which is AC-FIN-021's own Then. The
               published error body names neither, so this half is expected to fail against the
               build until the backend publishes them — the follow-up ADR-FIN-065 records
               (ADR-FIN-066)
Test data    : a value under `PROJECT`; ids are placeholders
<!-- TC:TC-FIN-021:END -->

<!-- TC:TC-FIN-026:START traces=AC-FIN-026,REQ-FIN-026,API-FIN-017 -->
### TC-FIN-026 — an allocation run distributes the source balance to the last unit
Derived from : AC-FIN-026  (REQ-FIN-026)
Exercises    : API-FIN-017 POST /api/v1/fin/allocation-rules/{id}/run
Rule / code  : RULE-FIN-010 (reused for allocations) → success path, no code raised ·
               RULE-FIN-003 governs the target set
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an active AllocationRule (ENT-FIN-013) with two `PERCENTAGE` targets and one
               `REMAINDER` target, exactly one target carrying `isRemainderFl` = true
               (DBF-FIN-146) in agreement with its own `distributionTypeCode`; the source
               account's current balance is 1,000.00; an Open period; an accountant holding
               `PERM_FIN_ALLOCATION_RULES_UPDATE`
Steps        : 1. POST `/api/v1/fin/allocation-rules/{id}/run`
Expected     : 201 · one `JournalEntryResponse` with `journalTypeCode` = ALLOCATION and
               `statusCode` = POSTED, whose target lines sum **exactly** to 1,000.00, the
               remainder target absorbing the rounding difference. The entry balances, so
               RULE-FIN-006 does not fire
Test data    : 1,000.00 and the two-percentage-plus-one-remainder shape — AC-FIN-026's own
<!-- TC:TC-FIN-026:END -->

<!-- TC:TC-FIN-029:START traces=AC-FIN-029,REQ-FIN-029,API-FIN-021 -->
### TC-FIN-029 — a reversal of an entry in a closed period posts into the current open period
Derived from : AC-FIN-029  (REQ-FIN-029)
Exercises    : API-FIN-021 POST /api/v1/fin/journal-entries/{id}/reverse
Rule / code  : RULE-FIN-012 → success path, no distinct code (the period substitution is the
               rule's effect, not a refusal)
Scenario     : STATE · data class EDGE · language ALL
Preconditions: a POSTED entry whose own period is now HARD_CLOSE; a different period that is
               currently OPEN; a caller holding `PERM_FIN_JOURNAL_ENTRIES_REVERSE`
Steps        : 1. POST `/api/v1/fin/journal-entries/{id}/reverse`
Expected     : 201 · the reversal entry's `periodId` (DBF-FIN-038) is the **current open**
               period, not the original's closed one — AC-FIN-029's own Then. The mirrored lines
               carry the same amounts with opposite `directionCode`, and both rows are linked
               through `originalEntryId` / `reversalEntryId`. FIN-409-PERIOD-NOT-OPEN is **not**
               raised, because the reversal does not target the closed period
Test data    : none invented
<!-- TC:TC-FIN-029:END -->

<!-- TC:TC-FIN-030:START traces=AC-FIN-030,REQ-FIN-030,API-FIN-021 -->
### TC-FIN-030 — an entry that already carries a reversal cannot be reversed again
Derived from : AC-FIN-030  (REQ-FIN-030)
Exercises    : API-FIN-021 POST /api/v1/fin/journal-entries/{id}/reverse
Rule / code  : RULE-FIN-013 → FIN-409-ALREADY-REVERSED (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an entry reversed once already. Per SRS A7 it stays POSTED — classic reversal
               writes no VOID anywhere — and carries a `reversalEntryId` (DBF-FIN-043) link to
               its mirror
Steps        : 1. POST `/api/v1/fin/journal-entries/{id}/reverse` on the same original a second
                  time
Expected     : 409 with code FIN-409-ALREADY-REVERSED —
               ar: «تم عكس هذا القيد بالفعل ولا يمكن عكسه مرة أخرى» ·
               en: "This entry has already been reversed and cannot be reversed again".
               No second mirror is posted, so the pair's net ledger effect stays zero rather than
               becoming −(original)
Test data    : none invented
<!-- TC:TC-FIN-030:END -->

<!-- TC:TC-FIN-032:START traces=AC-FIN-032,REQ-FIN-032,API-FIN-024 -->
### TC-FIN-032 — a soft-closed period reopens
Derived from : AC-FIN-032  (REQ-FIN-032)
Exercises    : API-FIN-024 PATCH /api/v1/fin/fiscal-periods/{id}/open
Rule / code  : RULE-FIN-014 → not triggered here (the period is not Hard Closed)
Scenario     : STATE · data class VALID · language ALL
Preconditions: a period whose `statusCode` (DBF-FIN-082) is SOFT_CLOSE — the one state §10.2
               calls re-openable; an administrator holding `PERM_FIN_PERIODS_UPDATE`
Steps        : 1. PATCH `/api/v1/fin/fiscal-periods/{id}/open`
Expected     : 200 · `FiscalPeriodResponse` with `statusCode` = OPEN, the SOFT_CLOSE → OPEN
               transition of SRS A7. Normal posting into it succeeds afterwards, so RULE-FIN-008
               no longer refuses it
Test data    : the `PERIOD_STATE` values OPEN and SOFT_CLOSE — seeded lookup values (SRS A6)
<!-- TC:TC-FIN-032:END -->

<!-- TC:TC-FIN-033:START traces=AC-FIN-033,REQ-FIN-033,API-FIN-025 -->
### TC-FIN-033 — soft-closing a period blocks normal posting into it
Derived from : AC-FIN-033  (REQ-FIN-033)
Exercises    : API-FIN-025 PATCH /api/v1/fin/fiscal-periods/{id}/soft-close
Rule / code  : RULE-FIN-008 → FIN-409-PERIOD-NOT-OPEN (409), on the posting attempt that follows
Scenario     : STATE · data class VALID · language ALL
Preconditions: a period currently OPEN; an administrator holding `PERM_FIN_PERIODS_UPDATE`
Steps        : 1. PATCH `/api/v1/fin/fiscal-periods/{id}/soft-close`
               2. POST a balanced manual entry against that period (API-FIN-019)
Expected     : step 1 → 200 with `statusCode` = SOFT_CLOSE (OPEN → SOFT_CLOSE, SRS A7).
               step 2 → 409 with code FIN-409-PERIOD-NOT-OPEN —
               ar: «الفترة المستهدفة غير مفتوحة» · en: "The target period is not open" —
               which is AC-FIN-033's own "REQ-FIN-020 rejects normal postings thereafter"
Test data    : none invented
<!-- TC:TC-FIN-033:END -->

<!-- TC:TC-FIN-034:START traces=AC-FIN-034,REQ-FIN-034,API-FIN-026 -->
### TC-FIN-034 — hard-closing a period is permanent
Derived from : AC-FIN-034  (REQ-FIN-034)
Exercises    : API-FIN-026 PATCH /api/v1/fin/fiscal-periods/{id}/hard-close
Rule / code  : RULE-FIN-015 (the distinct permission gates the call) · RULE-FIN-014 (the
               terminal state it creates)
Scenario     : STATE · data class VALID · language ALL
Preconditions: a period whose `statusCode` is SOFT_CLOSE; an authorized approver holding
               `PERM_FIN_PERIODS_CLOSE_APPROVE` together with the `PERM_FIN_PERIODS_VIEW` gateway
Steps        : 1. PATCH `/api/v1/fin/fiscal-periods/{id}/hard-close`
Expected     : 200 · `statusCode` = HARD_CLOSE, with `closedBy` (DBF-FIN-083) and `closedAt`
               (DBF-FIN-084) set. The state is terminal: TC-FIN-035 shows the reopen refusal, and
               posting into it is refused by RULE-FIN-008
Test data    : none invented
<!-- TC:TC-FIN-034:END -->

<!-- TC:TC-FIN-035:START traces=AC-FIN-035,REQ-FIN-035,API-FIN-024 -->
### TC-FIN-035 — a hard-closed period never reopens
Derived from : AC-FIN-035  (REQ-FIN-035)
Exercises    : API-FIN-024 PATCH /api/v1/fin/fiscal-periods/{id}/open
Rule / code  : RULE-FIN-014 → FIN-409-NOT-REOPENABLE (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a period whose `statusCode` is HARD_CLOSE; an administrator holding
               `PERM_FIN_PERIODS_UPDATE`
Steps        : 1. PATCH `/api/v1/fin/fiscal-periods/{id}/open`
Expected     : 409 with code FIN-409-NOT-REOPENABLE —
               ar: «الفترة مغلقة إغلاقًا صارمًا ولا يمكن إعادة فتحها» ·
               en: "The period is hard-closed and cannot be reopened".
               The stored `statusCode` is unchanged
Test data    : none invented
<!-- TC:TC-FIN-035:END -->

<!-- TC:TC-FIN-047:START traces=AC-FIN-009,REQ-FIN-009,API-FIN-011 -->
### TC-FIN-047 — two remainder lines are refused as surely as none
Derived from : AC-FIN-009  (REQ-FIN-009) — the "exactly one" limit its RULE states
Exercises    : API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : RULE-FIN-003 → FIN-409-REMAINDER-COUNT (409)
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an EventTypeRule whose line set already carries one `PERCENTAGE` line and one line
               marked `isRemainderFl` = true; a caller holding `PERM_FIN_RULES_UPDATE`
Steps        : 1. POST `/api/v1/fin/event-rules/{rule id}/lines` with a **second** line marked
                  `isRemainderFl` = true
Expected     : 409 with code FIN-409-REMAINDER-COUNT —
               ar: «يلزم تحديد سطر باقٍ واحد بالضبط عند وجود توزيع نسبي» ·
               en: "Exactly one remainder line is required when any percentage distribution is
               present".
               This is the upper side of the same numeric limit TC-FIN-009 tests from below:
               RULE-FIN-003 requires exactly one, so zero and two fail alike. Which line is the
               remainder decides every other line's amount under RULE-FIN-010, so an ambiguous set
               must never be stored
Test data    : the line counts come from RULE-FIN-003's own "exactly one"
<!-- TC:TC-FIN-047:END -->

<!-- TC:TC-FIN-048:START traces=AC-FIN-012,REQ-FIN-012,API-FIN-020 -->
### TC-FIN-048 — a remainder that computes to zero or less is refused
Derived from : AC-FIN-012  (REQ-FIN-012) — the positivity limit its RULE states
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-010 → FIN-422-REMAINDER-NOT-POSITIVE (422)
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an active EventTypeRule whose non-remainder lines on the remainder line's own side
               already equal or exceed the total carried by the opposing side, leaving no residue
               for the remainder line to absorb; exactly one line marked `isRemainderFl`
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with an event of that type
Expected     : 422 with code FIN-422-REMAINDER-NOT-POSITIVE —
               ar: «سطر الباقي يُحسب كفرق ويجب أن يكون موجبًا؛ السطور الأخرى تستهلك المبلغ بالكامل» ·
               en: "The remainder line is computed as a difference and must be positive; the
               other lines already consume the full amount".
               Nothing is posted. The refusal happens in the rule engine, before
               `CHK_FIN_JOURNAL_LINE_AMOUNT_POSITIVE` could ever be reached — a negative amount is
               never offered to the database
Test data    : no figures are invented; the shape ("other lines already equal or exceed the
               opposing side") is RULE-FIN-010's own wording
<!-- TC:TC-FIN-048:END -->

<!-- TC:TC-FIN-049:START traces=AC-FIN-014,REQ-FIN-014,API-FIN-019 -->
### TC-FIN-049 — a manual entry with no lines never reaches the rules
Derived from : AC-FIN-014  (REQ-FIN-014) — the "at least two balanced lines" limit it states
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : — (no FIN catalog code: the refusal is the platform validation envelope, before
               any RULE-FIN-* is evaluated)
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an accountant holding `PERM_FIN_JOURNAL_ENTRIES_CREATE`; a coherent
               fiscalYearId / periodId / docDate triple, so nothing else can fail first
Steps        : 1. POST `/api/v1/fin/journal-entries` with a well-formed header and an **empty**
                  `lines` array
Expected     : the request is refused at the DTO boundary — `JournalEntryCreateRequest.lines`
               carries `@NotEmpty` — and answers the platform `VALIDATION_ERROR` envelope in the
               caller's language, not a FIN code. No entry row is written and no `docNo` is drawn
               from the fiscal-year series. This is the boundary the backend plan names when it
               says the lineless entry shape "exists solely on the internal year-end path"
               (API-FIN-027) and cannot be reached through this DTO
Test data    : the empty line set is the boundary of AC-FIN-014's "at least two"
<!-- TC:TC-FIN-049:END -->
<!-- SUB:RULE-SCENARIOS:END -->

<!-- SUB:API-SCENARIOS:START traces=REQ-FIN-001,REQ-FIN-004,REQ-FIN-005,REQ-FIN-007,REQ-FIN-008,REQ-FIN-010,REQ-FIN-014,REQ-FIN-022,REQ-FIN-023,REQ-FIN-024,REQ-FIN-025,REQ-FIN-027,REQ-FIN-028,REQ-FIN-031,REQ-FIN-036,REQ-FIN-037,REQ-FIN-038,REQ-FIN-039,REQ-FIN-040,REQ-FIN-041,REQ-FIN-042,REQ-FIN-043,REQ-FIN-044,REQ-FIN-045,REQ-FIN-046,REQ-FIN-047,REQ-FIN-049,REQ-FIN-050,REQ-FIN-054,REQ-FIN-055,REQ-FIN-056,REQ-FIN-062,REQ-FIN-063,REQ-FIN-066,REQ-FIN-070,REQ-FIN-071,REQ-FIN-075,REQ-FIN-077,AC-FIN-001,AC-FIN-004,AC-FIN-005,AC-FIN-007,AC-FIN-008,AC-FIN-010,AC-FIN-014,AC-FIN-022,AC-FIN-023,AC-FIN-024,AC-FIN-025,AC-FIN-027,AC-FIN-028,AC-FIN-031,AC-FIN-036,AC-FIN-037,AC-FIN-038,AC-FIN-039,AC-FIN-040,AC-FIN-041,AC-FIN-042,AC-FIN-043,AC-FIN-044,AC-FIN-045,AC-FIN-046,AC-FIN-047,AC-FIN-049,AC-FIN-050,AC-FIN-051,AC-FIN-052,AC-FIN-058,AC-FIN-059,AC-FIN-060,AC-FIN-066,AC-FIN-067,AC-FIN-068,AC-FIN-071,AC-FIN-075,AC-FIN-076,AC-FIN-081,AC-FIN-083 -->
### SUB — API-SCENARIOS

The ACs whose Then is an endpoint's own success shape: creations, runs, reports, paging and
empty results, and the two permission denials AC-FIN-038 states.

**v2 cases open the SUB** (TC-FIN-093 … TC-FIN-129, the endpoint-driven half of CS-FIN-001), and
the v1 cases follow unchanged. The mapping endpoints API-FIN-038 … API-FIN-041 are gated by the
three `PERM_FIN_ACCOUNT_MAPPINGS_*` permissions that the v2 FIN security migration registers and
grants to `SYS_ADMIN`; without that grant every mapping call answers FIN-403-FORBIDDEN, so the
grant is a precondition of every mapping case below. Where a mapping for a PAYMENT_METHOD value
other than CHEQUE is set up, that value must first exist as host data in MDL (RULE-FIN-024):
only CHEQUE is seeded.

<!-- TC:TC-FIN-093:START traces=AC-FIN-047,REQ-FIN-047,API-FIN-020 -->
### TC-FIN-093 — an account code carried in the event payload is never used
Derived from : AC-FIN-047  (REQ-FIN-047)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : — (success path; REQ-FIN-047 holds by construction, no code is raised)
Scenario     : HAPPY · data class ATTACK · language ALL
Preconditions: an active rule for event type PAYMENT_RECEIVED whose debit line is MAPPING on
               PAYMENT_METHOD and whose other line resolves a leaf, active account; an active
               mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102; accounts 1101
               and 1102 are both active leaves, so 1101 **could** be posted to; an Open period
               covering the event date; the caller is the event consumer's service principal
               holding `PERM_FIN_JOURNAL_ENTRIES_CREATE`
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3001`, type
                  PAYMENT_RECEIVED, `fields.PAYMENT_METHOD` = "CHEQUE" and an extra payload field
                  `accountCode` = "1101" — the backend plan names the `fields.accountCode` form
Expected     : 200 · the entry POSTED with its debit line on account 1102, and **no line of the
               entry carries account 1101**. The extra field is never read: nothing in the
               payload is treated as an account code
Test data    : `EVT-3001`, "CHEQUE", 1101 and 1102 — AC-FIN-047's own values; the amount is a
               placeholder
<!-- TC:TC-FIN-093:END -->

<!-- TC:TC-FIN-095:START traces=AC-FIN-049,REQ-FIN-049,API-FIN-011 -->
### TC-FIN-095 — a MAPPING rule line records the business field that selects its account
Derived from : AC-FIN-049  (REQ-FIN-049)
Exercises    : API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : RULE-FIN-019 → passes (the MAPPING half of the spec is complete)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an active EventTypeRule for "EXPENSE_PAID"; `FIN_EVENT_BUSINESS_FIELD` is seeded
               in MDL with its 4 values (db-script v2 BLOCK 8); a finance administrator holding
               `PERM_FIN_RULES_UPDATE`
Steps        : 1. POST `/api/v1/fin/event-rules/{rule id}/lines` with
                  `accountDerivationTypeCode` = MAPPING, `accountBusinessFieldCode` =
                  EXPENSE_TYPE_CODE, **no** `accountDerivationValue`, and valid amount-source,
                  direction and distribution fields
Expected     : 201 · `RuleLineResponse` with `accountBusinessFieldCode` = EXPENSE_TYPE_CODE
               (DBF-FIN-148) and `accountDerivationValue` empty (DBF-FIN-102): the line names the
               business fact and the mapping store names the account
Test data    : EXPENSE_PAID and EXPENSE_TYPE_CODE — AC-FIN-049's own values; `FIELD`, `DEBIT`
               and `FIXED` are seeded values (SRS A6)
<!-- TC:TC-FIN-095:END -->

<!-- TC:TC-FIN-096:START traces=AC-FIN-050,REQ-FIN-050,API-FIN-020 -->
### TC-FIN-096 — a cheque payment resolves to the bank account through its mapping
Derived from : AC-FIN-050  (REQ-FIN-050)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-020 → success path, no code
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: active mappings (PAYMENT_RECEIVED, PAYMENT_METHOD, "CASH") → account 1101 (cash)
               and (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102 (bank); a rule for
               PAYMENT_RECEIVED whose debit line is MAPPING on PAYMENT_METHOD and whose credit
               side resolves a leaf, active account; an Open period covering the event date
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3002`, type
                  PAYMENT_RECEIVED, `fields.PAYMENT_METHOD` = "CHEQUE" and amount 500.00
Expected     : 200 · a POSTED entry, `journalTypeCode` EVENT_GENERATED, whose debit line is
               500.00 on account 1102. The account is stored on the line (DBF-FIN-054), not
               looked up again when the entry is read
Test data    : `EVT-3002`, "CASH", "CHEQUE", 1101, 1102 and 500.00 — AC-FIN-050's own values
<!-- TC:TC-FIN-096:END -->

<!-- TC:TC-FIN-097:START traces=AC-FIN-051,REQ-FIN-050,API-FIN-020 -->
### TC-FIN-097 — a cash payment resolves to the cash account through its mapping
Derived from : AC-FIN-051  (REQ-FIN-050)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-020 → success path, no code
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the mappings and rule of AC-FIN-050; an Open period covering the event date
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3003`, type
                  PAYMENT_RECEIVED, `fields.PAYMENT_METHOD` = "CASH" and amount 500.00
Expected     : 200 · a POSTED entry whose debit line is 500.00 on account 1101. Together with
               TC-FIN-096 it shows one rule line taking two different accounts from two business
               values, with no code change between them
Test data    : `EVT-3003`, "CASH", 1101 and 500.00 — AC-FIN-051's own values
<!-- TC:TC-FIN-097:END -->

<!-- TC:TC-FIN-098:START traces=AC-FIN-052,REQ-FIN-050,API-FIN-020 -->
### TC-FIN-098 — an expense type resolves to its own expense account
Derived from : AC-FIN-052  (REQ-FIN-050)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-020 → success path, no code
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: active mappings (EXPENSE_PAID, EXPENSE_TYPE_CODE, "TRAVEL") → account 5210 and
               (EXPENSE_PAID, EXPENSE_TYPE_CODE, "RENT") → account 5220; a rule for EXPENSE_PAID
               whose debit line is MAPPING on EXPENSE_TYPE_CODE; an Open period
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3004`, type
                  EXPENSE_PAID, `fields.EXPENSE_TYPE_CODE` = "RENT" and amount 1200.00
Expected     : 200 · a POSTED entry whose debit line is 1200.00 on account 5220, and not on 5210
Test data    : `EVT-3004`, "TRAVEL", "RENT", 5210, 5220 and 1200.00 — AC-FIN-052's own values
<!-- TC:TC-FIN-098:END -->

<!-- TC:TC-FIN-104:START traces=AC-FIN-058,REQ-FIN-054,API-FIN-039 -->
### TC-FIN-104 — an account mapping is created active
Derived from : AC-FIN-058  (REQ-FIN-054)
Exercises    : API-FIN-039 POST /api/v1/fin/account-mappings
Rule / code  : RULE-FIN-022, RULE-FIN-023, RULE-FIN-024 all pass
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: no active mapping for (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE"); account 1102
               is an active leaf; PAYMENT_RECEIVED is an `ACCOUNTING_EVENT_TYPE` value in MDL and
               CHEQUE an active PAYMENT_METHOD value; a finance administrator holding
               `PERM_FIN_ACCOUNT_MAPPINGS_CREATE` and the `PERM_FIN_ACCOUNT_MAPPINGS_VIEW` gateway
Steps        : 1. POST `/api/v1/fin/account-mappings` with `eventTypeCode` = PAYMENT_RECEIVED,
                  `businessFieldCode` = PAYMENT_METHOD, `businessValue` = "CHEQUE" and
                  `accountId` = 1102
Expected     : 201 · `AccountMappingResponse` with `isActiveFl` = true and `accountId` = 1102,
               the three key fields stored as submitted **codes**, and the account's code and
               bilingual name returned beside it. No name is stored on the mapping itself
Test data    : PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE" and 1102 — AC-FIN-058's own values
<!-- TC:TC-FIN-104:END -->

<!-- TC:TC-FIN-105:START traces=AC-FIN-059,REQ-FIN-055,API-FIN-038 -->
### TC-FIN-105 — a mapping search returns exactly the rows its filters match
Derived from : AC-FIN-059  (REQ-FIN-055)
Exercises    : API-FIN-038 POST /api/v1/fin/account-mappings/search
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: 3 mappings for PAYMENT_RECEIVED — CASH active, CHEQUE active, TRANSFER inactive —
               and 2 for EXPENSE_PAID; CASH and TRANSFER exist as PAYMENT_METHOD host data so that
               their mappings could be saved; a caller holding `PERM_FIN_ACCOUNT_MAPPINGS_VIEW`
Steps        : 1. POST `/api/v1/fin/account-mappings/search` with filters `eventTypeCode` =
                  PAYMENT_RECEIVED and `isActiveFl` = true, plus page and size
Expected     : 200 · `Page<AccountMappingResponse>` with **exactly 2** rows, CASH and CHEQUE —
               neither the inactive TRANSFER row nor any EXPENSE_PAID row. Each row carries its
               account's code, `nameAr` and `nameEn`. Paging is server-side and the page
               metadata reflects the filtered total
Test data    : the 3 + 2 mappings and the filter pair — AC-FIN-059's own; accounts are
               placeholders
<!-- TC:TC-FIN-105:END -->

<!-- TC:TC-FIN-106:START traces=AC-FIN-060,REQ-FIN-056,API-FIN-040 -->
### TC-FIN-106 — changing a mapping's account leaves its key untouched
Derived from : AC-FIN-060  (REQ-FIN-056)
Exercises    : API-FIN-040 PUT /api/v1/fin/account-mappings/{id}
Rule / code  : RULE-FIN-023 → passes (the new account is an active leaf)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the mapping (EXPENSE_PAID, EXPENSE_TYPE_CODE, "RENT") → account 5220; account
               5225 is an active leaf; a finance administrator holding
               `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE`
Steps        : 1. PUT `/api/v1/fin/account-mappings/{RENT id}` with a body carrying `accountId` =
                  5225 and nothing else
Expected     : 200 · the mapping with `accountId` = 5225, and its `eventTypeCode`,
               `businessFieldCode` and `businessValue` unchanged; `updatedBy` and `updatedAt` are
               set by the platform. The key is not an input of this endpoint at all
Test data    : 5220, 5225 and "RENT" — AC-FIN-060's own values
<!-- TC:TC-FIN-106:END -->

<!-- TC:TC-FIN-112:START traces=AC-FIN-066,REQ-FIN-062,API-FIN-011 -->
### TC-FIN-112 — a rule line is saved with its two dimension tags
Derived from : AC-FIN-066  (REQ-FIN-062)
Exercises    : API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : RULE-FIN-025, RULE-FIN-026 → pass
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: dimensions ORGANISATION, holding value "ORG-1", and BRANCH, both active; an
               EventTypeRule — PAYMENT_RECEIVED in the event cases that reuse this line;
               `FIN_DIMENSION_VALUE_SOURCE` is seeded in MDL; a finance administrator holding
               `PERM_FIN_RULES_UPDATE`
Steps        : 1. POST `/api/v1/fin/event-rules/{rule id}/lines` with a valid line whose
                  `dimensionTags` are {ORGANISATION: CONSTANT "ORG-1"} and
                  {BRANCH: BUSINESS_FIELD BRANCH_CODE}
Expected     : 201 · the RuleLine and **2** RuleLineDimension rows, one per tag. The ORGANISATION
               tag carries `valueSourceCode` CONSTANT with the "ORG-1" value's id and no business
               field; the BRANCH tag carries BUSINESS_FIELD with `businessFieldCode` BRANCH_CODE
               and no value (DBF-FIN-160, DBF-FIN-161, DBF-FIN-164). The response returns the
               line with both tags
Test data    : ORGANISATION, "ORG-1", BRANCH and BRANCH_CODE — AC-FIN-066's own values
<!-- TC:TC-FIN-112:END -->

<!-- TC:TC-FIN-113:START traces=AC-FIN-067,REQ-FIN-063,API-FIN-020 -->
### TC-FIN-113 — a CONSTANT tag stamps its value on the event-built line
Derived from : AC-FIN-067  (REQ-FIN-063)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : — (success path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the rule line of AC-FIN-066 on the PAYMENT_RECEIVED rule, the rule balancing and
               its accounts leaf and active; BRANCH holds an active value "BR-07", without which
               RULE-FIN-027 would refuse the event (AC-FIN-068's own Given); an Open period
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3011` and
                  `fields.BRANCH_CODE` = "BR-07"
               2. GET the posted entry through API-FIN-022
Expected     : 200 · POSTED, and the journal line built from that rule line carries
               ORGANISATION = "ORG-1", taken from the tag and not from the event
Test data    : `EVT-3011`, "ORG-1" and "BR-07" — AC-FIN-067's own values. AC-FIN-068 names the
               same event, so on one dataset this TC and TC-FIN-114 read the same posting
<!-- TC:TC-FIN-113:END -->

<!-- TC:TC-FIN-114:START traces=AC-FIN-068,REQ-FIN-063,API-FIN-020 -->
### TC-FIN-114 — a BUSINESS_FIELD tag resolves its value by code from the event
Derived from : AC-FIN-068  (REQ-FIN-063)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-027 → success path, no code
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the rule line of AC-FIN-066; BRANCH holds an active value with code "BR-07"; an
               Open period
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3011` and
                  `fields.BRANCH_CODE` = "BR-07"
               2. GET the posted entry through API-FIN-022
Expected     : 200 · POSTED, and the journal line built from that rule line carries BRANCH = the
               DimensionValue whose code is "BR-07" — resolved by exact code **inside the BRANCH
               dimension**, never by a value of another dimension that happens to share the code
Test data    : `EVT-3011` and "BR-07" — AC-FIN-068's own values
<!-- TC:TC-FIN-114:END -->

<!-- TC:TC-FIN-117:START traces=AC-FIN-071,REQ-FIN-066,API-FIN-020 -->
### TC-FIN-117 — both resolved dimension values are validated and written on the line
Derived from : AC-FIN-071  (REQ-FIN-066)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-009 → passes for both values
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the rule line of AC-FIN-066; "ORG-1" and "BR-07" both active in their own
               dimensions; an Open period
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3012` and
                  `fields.BRANCH_CODE` = "BR-07"
               2. GET the posted entry through API-FIN-022
Expected     : 200 · the entry is POSTED with **2** JournalLineDimension rows on that line, one
               for ORGANISATION and one for BRANCH — the same validation a manual line receives,
               applied without a bypass
Test data    : `EVT-3012`, "ORG-1" and "BR-07" — AC-FIN-071's own values
<!-- TC:TC-FIN-117:END -->

<!-- TC:TC-FIN-121:START traces=AC-FIN-075,REQ-FIN-070,API-FIN-022 -->
### TC-FIN-121 — an event-built line and a manual line read back in one dimension shape
Derived from : AC-FIN-075  (REQ-FIN-070)
Exercises    : API-FIN-022 GET /api/v1/fin/journal-entries/{id}, with API-FIN-020 and
               API-FIN-019 as setup
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: entry A posted from event `EVT-3016` with BRANCH "BR-07" on its debit line
               (API-FIN-020), and manual entry B posted with BRANCH "BR-07" on its debit line
               (API-FIN-019); a caller holding `PERM_FIN_JOURNAL_ENTRIES_VIEW`
Steps        : 1. GET entry A
               2. GET entry B
Expected     : each debit line holds **exactly 1** JournalLineDimension row with dimension
               BRANCH and the same `dimensionValueId`. The two responses differ in
               `journalTypeCode` and `eventReference` only, never in the shape of their lines
Test data    : `EVT-3016` and "BR-07" — AC-FIN-075's own values
<!-- TC:TC-FIN-121:END -->

<!-- TC:TC-FIN-122:START traces=AC-FIN-076,REQ-FIN-071,API-FIN-032 -->
### TC-FIN-122 — the dimension report groups event-built and manual lines together
Derived from : AC-FIN-076  (REQ-FIN-071)
Exercises    : API-FIN-032 GET /api/v1/fin/reports/dimension
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the two entries of AC-FIN-075, each debiting account 5100 with 100.00 under
               BRANCH "BR-07"; a controller holding `PERM_FIN_DIMENSION_REPORTS_VIEW`
Steps        : 1. GET `/api/v1/fin/reports/dimension` with `dimensionId` = the BRANCH dimension
Expected     : 200 · **1** row for (5100, "BR-07") with a debit total of 200.00 — not two rows
               split by journal type. The dimension report is no longer empty for event-built
               entries
Test data    : 5100, 100.00, 200.00 and "BR-07" — AC-FIN-076's own values
<!-- TC:TC-FIN-122:END -->

<!-- TC:TC-FIN-127:START traces=AC-FIN-081,REQ-FIN-075,API-FIN-020 -->
### TC-FIN-127 — the published catalogue documents the period-not-open code once
Derived from : AC-FIN-081  (REQ-FIN-075)
Exercises    : the published API documentation of API-FIN-020 — its error catalogue, read as a
               document; no call is made
Rule / code  : RULE-FIN-008 → FIN-409-PERIOD-NOT-OPEN (409), the row under inspection
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: FIN's API documentation as the backend publishes it for v2
Steps        : 1. read the error catalogue of the published documentation
               2. count and read the rows naming FIN-409-PERIOD-NOT-OPEN
Expected     : the catalogue lists FIN-409-PERIOD-NOT-OPEN **exactly once**, with its message —
               ar: «الفترة المستهدفة غير مفتوحة» · en: "The target period is not open" — and the
               note that the rejection clears once the target period is opened. The copy
               republished for v2 carries both (ADR-FIN-064)
Test data    : none invented
<!-- TC:TC-FIN-127:END -->

<!-- TC:TC-FIN-129:START traces=AC-FIN-083,REQ-FIN-077,API-FIN-021,API-FIN-028,API-FIN-032 -->
### TC-FIN-129 — an entry posted through a mapping and tags reverses like any other
Derived from : AC-FIN-083  (REQ-FIN-077)
Exercises    : API-FIN-021 POST /api/v1/fin/journal-entries/{id}/reverse, then
               API-FIN-028 GET /api/v1/fin/reports/account-ledger and
               API-FIN-032 GET /api/v1/fin/reports/dimension
Rule / code  : RULE-FIN-011 → success path · RULE-FIN-016 holds (the original is never modified)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: entry `JV-2026-000600`, POSTED from event `EVT-4006`, with a mapped debit account
               and BRANCH "BR-07" on its lines; its period still Open; an accountant holding
               `PERM_FIN_JOURNAL_ENTRIES_REVERSE`
Steps        : 1. POST `/api/v1/fin/journal-entries/{JV-2026-000600 id}/reverse`
               2. read the ledger of each account the entry touches, and the dimension report by
                  BRANCH, over the entry's date
Expected     : step 1 → 201 · a POSTED reversal whose lines carry the **same accounts, amounts and
                  dimension values** with the opposite direction; the original stays POSTED and
                  the two are linked through `originalEntryId` / `reversalEntryId`. No mapping is
                  consulted — the reversal reads the posted lines, never the rules that built them.
               step 2 → every account balance and every BRANCH balance of the pair nets to 0.00
Test data    : `JV-2026-000600`, `EVT-4006` and "BR-07" — AC-FIN-083's own values
<!-- TC:TC-FIN-129:END -->

<!-- TC:TC-FIN-001:START traces=AC-FIN-001,REQ-FIN-001,API-FIN-002 -->
### TC-FIN-001 — an account is created inside the chart hierarchy
Derived from : AC-FIN-001  (REQ-FIN-001)
Exercises    : API-FIN-002 POST /api/v1/fin/accounts
Rule / code  : — (happy path; RULE-FIN-001 is not triggered)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a finance administrator holding `PERM_FIN_ACCOUNTS_CREATE` (and the
               `PERM_FIN_ACCOUNTS_VIEW` gateway); MDL holds the `ACCOUNT_TYPE` and `DEBIT_CREDIT`
               types with their seeded values, so XM-FIN-001 can validate the two codes; the code
               submitted is not yet used
Steps        : 1. POST `/api/v1/fin/accounts` with a unique `code`, `nameAr`, `nameEn`,
                  `accountTypeCode`, `natureCode`, an optional `parentAccountId` and `isLeafFl`
Expected     : 201 · `AccountResponse` carrying every submitted field plus the system-set
               `accountPk` and the four audit columns. `accountTypeCode` and `natureCode` are
               stored as the submitted **codes** (DBF-FIN-005, DBF-FIN-006), never as numeric
               keys. A following API-FIN-001 search on that code returns exactly the new row
Test data    : `ASSET` and `DEBIT` are seeded `ACCOUNT_TYPE` / `DEBIT_CREDIT` values (SRS A6);
               the chart code and the two names are placeholders — the AC names none
<!-- TC:TC-FIN-001:END -->

<!-- TC:TC-FIN-004:START traces=AC-FIN-004,REQ-FIN-004,API-FIN-006 -->
### TC-FIN-004 — a dimension is created and is active
Derived from : AC-FIN-004  (REQ-FIN-004)
Exercises    : API-FIN-006 POST /api/v1/fin/dimensions
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a caller holding `PERM_FIN_DIMENSIONS_CREATE`; the code is not yet used
Steps        : 1. POST `/api/v1/fin/dimensions` with code `PROJECT` and both names
Expected     : 201 · `DimensionResponse` with `isActiveFl` (DBF-FIN-018) true — AC-FIN-004's
               "creates an active Dimension". The dimension is immediately available to carry
               values and to be cited on a journal line's dimension pair
Test data    : `PROJECT` — ENT-FIN-002's own example code; the names are placeholders
<!-- TC:TC-FIN-004:END -->

<!-- TC:TC-FIN-005:START traces=AC-FIN-005,REQ-FIN-005,API-FIN-007 -->
### TC-FIN-005 — a value is created under its dimension
Derived from : AC-FIN-005  (REQ-FIN-005)
Exercises    : API-FIN-007 POST /api/v1/fin/dimensions/{id}/values
Rule / code  : — (happy path; RULE-FIN-002 passes)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the dimension `PROJECT` exists and holds no value with the submitted code; a
               caller holding `PERM_FIN_DIMENSIONS_CREATE`
Steps        : 1. POST `/api/v1/fin/dimensions/{PROJECT id}/values` with code `NORTH`, both names
                  and a `sortOrder`
Expected     : 201 · `DimensionValueResponse` whose `dimensionId` (DBF-FIN-024) is the path id
               and whose `isActiveFl` is true. API-FIN-008 filtered on that dimension returns it
Test data    : `NORTH` — the code AC-FIN-006 names for this dimension; `sortOrder` is a
               placeholder integer
<!-- TC:TC-FIN-005:END -->

<!-- TC:TC-FIN-007:START traces=AC-FIN-007,REQ-FIN-007,API-FIN-010 -->
### TC-FIN-007 — an event type gets its single active rule
Derived from : AC-FIN-007  (REQ-FIN-007)
Exercises    : API-FIN-010 POST /api/v1/fin/event-rules
Rule / code  : — (happy path; the one-rule-per-type uniqueness passes)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: FIN's onboarding has completed, so the `ACCOUNTING_EVENT_TYPE` type is registered
               in MDL (REQ-FIN-045) and FIN's screens and actions are registered in SEC
               (REQ-FIN-044); the event type carries no existing rule; a caller holding
               `PERM_FIN_RULES_CREATE`
Steps        : 1. POST `/api/v1/fin/event-rules` with `eventTypeCode`, `nameAr`, `nameEn`
Expected     : 201 · `EventTypeRuleResponse` with `isActiveFl` (DBF-FIN-093) true — the single
               active rule for that type, which §6.4 allows exactly one of. A second create for
               the same type answers FIN-409-RULE-DUP
Test data    : the event type code is a placeholder — `ACCOUNTING_EVENT_TYPE` seeds no value
<!-- TC:TC-FIN-007:END -->

<!-- TC:TC-FIN-008:START traces=AC-FIN-008,REQ-FIN-008,API-FIN-011 -->
### TC-FIN-008 — a rule line records its three references separately
Derived from : AC-FIN-008  (REQ-FIN-008)
Exercises    : API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : — (happy path; RULE-FIN-003 passes, no percentage line being present;
               RULE-FIN-018 and RULE-FIN-019 pass for a complete CONSTANT line)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an EventTypeRule exists; a caller holding `PERM_FIN_RULES_UPDATE`; MDL holds the
               four lookup types the line's codes belong to; the constant account code names a
               leaf, active account
Steps        : 1. POST `/api/v1/fin/event-rules/{rule id}/lines` with an account-derivation spec
                  (`accountDerivationTypeCode` = CONSTANT + `accountDerivationValue` = the
                  constant account code, and no `accountBusinessFieldCode`), an amount-source
                  spec (`amountSourceTypeCode` + `amountSourceValue`) and a `directionCode`
Expected     : 201 · `RuleLineResponse` in which the account derivation, the amount source and the
               direction are **three separate stored references** (DBF-FIN-101 … DBF-FIN-105), not
               one fused field — the deliberate §6.2 separation REQ-FIN-008 states. On this
               CONSTANT line `accountBusinessFieldCode` (DBF-FIN-148) is empty
Test data    : `CONSTANT` (`ACCOUNT_DERIVATION_TYPE` — since v2 its only other active value is
               MAPPING, and DIRECT is refused, TC-FIN-094), `FIELD` (`AMOUNT_SOURCE_TYPE`),
               `FIXED` (`DISTRIBUTION_TYPE`), `DEBIT` (`DEBIT_CREDIT`) — all seeded values (SRS
               A6); the account code and the source value are placeholders
<!-- TC:TC-FIN-008:END -->

<!-- TC:TC-FIN-010:START traces=AC-FIN-010,REQ-FIN-010,API-FIN-020 -->
### TC-FIN-010 — an arriving event is turned into an entry by its type's rule
Derived from : AC-FIN-010  (REQ-FIN-010)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : — (happy path; RULE-FIN-004 and RULE-FIN-005 both pass, then REQ-FIN-017's checks)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an event type with an active EventTypeRule whose lines derive leaf, active
               accounts and balance; an Open period covering the document date; the event's
               reference has produced no posted entry
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with a canonical event of that
                  type
Expected     : 200 · `ApiResponse<JournalEntryResponse>`, the status the published contract
               states. A DRAFT is built whose lines follow the rule — account derived per
               `accountDerivationTypeCode` (the constant code, or since v2 the account a mapping
               resolves), amount per `amountSourceTypeCode`, one `directionCode` per line — and
               the same call proceeds straight through validation to POSTED (REQ-FIN-017).
               `journalTypeCode` is EVENT_GENERATED and `eventReference` carries the event's own
               reference. No account number ever appears in the event payload: the rule derives
               it
Test data    : the event payload is opaque by POL-FIN-020; no field of it is invented here
<!-- TC:TC-FIN-010:END -->

<!-- TC:TC-FIN-014:START traces=AC-FIN-014,REQ-FIN-014,API-FIN-019 -->
### TC-FIN-014 — a manual entry follows exactly the same path as any other source
Derived from : AC-FIN-014  (REQ-FIN-014)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-017 passes (header coherent), then RULE-FIN-006 … RULE-FIN-009 pass
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a fiscal period that is Open and belongs to the submitted fiscal year, its
               `[startDate, endDate]` covering the document date; at least two balanced lines on
               leaf, active accounts; an accountant holding `PERM_FIN_JOURNAL_ENTRIES_CREATE`
Steps        : 1. POST `/api/v1/fin/journal-entries` with the header and its nested lines
Expected     : 201 · a DRAFT is built and proceeds to REQ-FIN-017 in the same call, so the
               response carries `statusCode` = POSTED. No separate approval step exists for the
               entry, and `docNo`, `statusCode` and `postedAt` are absent from the request and
               present in the response
Test data    : two lines — AC-FIN-014's own "at least two"; accounts and amounts are placeholders
<!-- TC:TC-FIN-014:END -->

<!-- TC:TC-FIN-022:START traces=AC-FIN-022,REQ-FIN-022,API-FIN-013 -->
### TC-FIN-022 — a recurring or reversing template is stored with its schedule
Derived from : AC-FIN-022  (REQ-FIN-022)
Exercises    : API-FIN-013 POST /api/v1/fin/recurring-templates
Rule / code  : — (happy path; the frequency-required condition is satisfied)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a caller holding `PERM_FIN_RECURRING_TEMPLATES_CREATE`; MDL holds
               `RECURRING_SCHEDULE_TYPE` and `RECURRING_FREQUENCY`
Steps        : 1. POST `/api/v1/fin/recurring-templates` with `scheduleTypeCode` = `RECURRING`,
                  a `frequencyCode`, a `startDate` and at least two balanced lines
Expected     : 201 · `RecurringTemplateResponse` with `isActiveFl` (DBF-FIN-117) true and
               `nextRunDate` (DBF-FIN-115) set to the submitted `startDate`, its lines stored with
               it in one request. A `RECURRING` template without a `frequencyCode` would answer
               FIN-400-MISSING-FREQUENCY; a `REVERSING` one needs none
Test data    : `RECURRING`, `REVERSING`, `MONTHLY` — seeded values (SRS A6); dates and amounts are
               placeholders
<!-- TC:TC-FIN-022:END -->

<!-- TC:TC-FIN-023:START traces=AC-FIN-023,REQ-FIN-023,API-FIN-014 -->
### TC-FIN-023 — a due recurring template posts one entry and advances its next run date
Derived from : AC-FIN-023  (REQ-FIN-023)
Exercises    : API-FIN-014 POST /api/v1/fin/recurring-templates/{id}/run
Rule / code  : RULE-FIN-006 … RULE-FIN-009 re-checked at run time
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an **active** recurring template whose `nextRunDate` is today — AC-FIN-023's own
               Given; an Open period covering that date; every template line's account still leaf
               and active; a caller holding `PERM_FIN_RECURRING_TEMPLATES_UPDATE`
Steps        : 1. POST `/api/v1/fin/recurring-templates/{id}/run`
Expected     : 201 · exactly **one** `JournalEntryResponse`, `journalTypeCode` = RECURRING and
               `statusCode` = POSTED, and the template's `nextRunDate` advanced per its
               `frequencyCode`. The lines are re-validated at run time even though they were
               checked at create time, because accounts and periods may have changed since
Test data    : none invented; "today" is AC-FIN-023's own
<!-- TC:TC-FIN-023:END -->

<!-- TC:TC-FIN-024:START traces=AC-FIN-024,REQ-FIN-024,API-FIN-014 -->
### TC-FIN-024 — a reversing template's entry is mirrored in the next period
Derived from : AC-FIN-024  (REQ-FIN-024)
Exercises    : API-FIN-014 POST /api/v1/fin/recurring-templates/{id}/run
Rule / code  : RULE-FIN-011 (the mirror) and RULE-FIN-012 (its period) — success path, no code
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an active template whose `scheduleTypeCode` is `REVERSING`; period N is Open and
               period N+1 exists inside the same fiscal year
Steps        : 1. POST `/api/v1/fin/recurring-templates/{id}/run`
Expected     : 201 · the template's own entry posts in period N, and a **linked reversal** posts
               in period N+1 — same line amounts, opposite `directionCode`, the two rows carrying
               each other's ids in `originalEntryId` / `reversalEntryId`. Both entries are
               individually balanced, so the pair nets to zero across the two periods
Test data    : none invented; "period N / N+1" is AC-FIN-024's own wording
<!-- TC:TC-FIN-024:END -->

<!-- TC:TC-FIN-025:START traces=AC-FIN-025,REQ-FIN-025,API-FIN-016 -->
### TC-FIN-025 — an allocation rule is stored with its targets
Derived from : AC-FIN-025  (REQ-FIN-025)
Exercises    : API-FIN-016 POST /api/v1/fin/allocation-rules
Rule / code  : RULE-FIN-003 (reused over the target set) passes
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a source account exists; a caller holding `PERM_FIN_ALLOCATION_RULES_CREATE`; MDL
               holds `DISTRIBUTION_TYPE`
Steps        : 1. POST `/api/v1/fin/allocation-rules` with a `sourceAccountId` and at least one
                  target carrying a `distributionTypeCode`
Expected     : 201 · `AllocationRuleResponse` with `isActiveFl` (DBF-FIN-134) true and its targets
               stored in the same request, each recording how the source account's balance
               distributes to it
Test data    : `FIXED`, `PERCENTAGE`, `REMAINDER` — seeded `DISTRIBUTION_TYPE` values (SRS A6);
               account ids are placeholders
<!-- TC:TC-FIN-025:END -->

<!-- TC:TC-FIN-027:START traces=AC-FIN-027,REQ-FIN-027,API-FIN-018 -->
### TC-FIN-027 — a filtered search returns exactly the matching entries, unmodified
Derived from : AC-FIN-027  (REQ-FIN-027)
Exercises    : API-FIN-018 POST /api/v1/fin/journal-entries/search
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: entries exist across several `statusCode` values and several periods; a caller
               holding `PERM_FIN_JOURNAL_ENTRIES_VIEW`
Steps        : 1. POST `/api/v1/fin/journal-entries/search` with `periodId` and `statusCode` in
                  the body filters, plus page and size
Expected     : 200 · `Page<JournalEntryResponse>` holding **exactly** the entries matching both
               filters and no others, each returned unmodified — no field recomputed, no status
               changed by the read. Paging is server-side and the page metadata reflects the
               filtered total, not the table total
Test data    : the filter pair is AC-FIN-027's own ("filters by period and status"); ids are
               placeholders
<!-- TC:TC-FIN-027:END -->

<!-- TC:TC-FIN-028:START traces=AC-FIN-028,REQ-FIN-028,API-FIN-021 -->
### TC-FIN-028 — a reversal mirrors the original line for line and links both ways
Derived from : AC-FIN-028  (REQ-FIN-028)
Exercises    : API-FIN-021 POST /api/v1/fin/journal-entries/{id}/reverse
Rule / code  : RULE-FIN-011 → success path, no distinct code
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a POSTED entry with exactly three lines, whose own period is still Open; a caller
               holding `PERM_FIN_JOURNAL_ENTRIES_REVERSE`
Steps        : 1. POST `/api/v1/fin/journal-entries/{id}/reverse`
               2. GET both rows through API-FIN-022
Expected     : 201 · a new POSTED entry with `journalTypeCode` = REVERSAL carrying the **same
               three line amounts with opposite directions**. Both entries reference each other:
               the reversal's `originalEntryId` (DBF-FIN-042) and the original's `reversalEntryId`
               (DBF-FIN-043). The original **stays POSTED** — classic reversal, SRS A7 — so both
               rows remain visible to the POSTED-only reports and net to zero
Test data    : three lines — AC-FIN-028's own count
<!-- TC:TC-FIN-028:END -->

<!-- TC:TC-FIN-031:START traces=AC-FIN-031,REQ-FIN-031,API-FIN-023 -->
### TC-FIN-031 — a fiscal year is created with its periods, each Open
Derived from : AC-FIN-031  (REQ-FIN-031)
Exercises    : API-FIN-023 POST /api/v1/fin/fiscal-years
Rule / code  : — (happy path; the year-code uniqueness check passes)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a finance administrator holding `PERM_FIN_PERIODS_CREATE`; the year code is not
               yet used
Steps        : 1. POST `/api/v1/fin/fiscal-years` with code `2026`, a start date, an end date
                  spanning that whole calendar year, and `periodCount` = 12
Expected     : 201 · `FiscalYearResponse` with `statusCode` = OPEN **and its generated periods**,
               twelve of them, each `statusCode` = OPEN — AC-FIN-031's "each initially Open".
               Because the count is twelve over a whole year, each period is one calendar month
               and its `nameAr` / `nameEn` (DBF-FIN-078, DBF-FIN-079) are that month's own name in
               Arabic and English from the platform's locale data. `periodCount` is a transient
               input and is not a stored column
Test data    : `2026` — the fiscal year code the `docNo` example in the SRS itself uses; twelve is
               REQ-FIN-031's own stated count
<!-- TC:TC-FIN-031:END -->

<!-- TC:TC-FIN-036:START traces=AC-FIN-036,REQ-FIN-036,API-FIN-027 -->
### TC-FIN-036 — year-end close posts a balanced closing entry and the next year's opening entry
Derived from : AC-FIN-036  (REQ-FIN-036)
Exercises    : API-FIN-027 POST /api/v1/fin/fiscal-years/{id}/year-end-close
Rule / code  : RULE-FIN-006, RULE-FIN-007, RULE-FIN-009 apply to both generated entries;
               RULE-FIN-008 does **not** — the rule exempts these two by its own carve-out
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a fiscal year still `statusCode` = OPEN whose every period is HARD_CLOSE; exactly
               one account marked as Retained Earnings (DBF-FIN-147); a successor fiscal year
               whose `startDate` is the day after this year's `endDate`; an approver holding
               `PERM_FIN_PERIODS_CLOSE_APPROVE`
Steps        : 1. POST `/api/v1/fin/fiscal-years/{id}/year-end-close`
Expected     : 201 · both entries returned. The closing entry posts into the year's **last**
               period with result accounts closed to Retained Earnings; the opening entry is
               generated for the next year from the resulting balance-sheet balances. **Each is
               individually balanced** — AC-FIN-036's own Then. The fiscal year moves to CLOSED
               and its periods to YEAR_END_CLOSE. FIN-409-PERIOD-NOT-OPEN is never raised, and the
               Retained Earnings account is derived by the system, never supplied by the caller
Test data    : none invented; the two entries' composition is AC-FIN-036's own
<!-- TC:TC-FIN-036:END -->

<!-- TC:TC-FIN-037:START traces=AC-FIN-037,REQ-FIN-037,API-FIN-026 -->
### TC-FIN-037 — the close approval is recorded as an act of its own
Derived from : AC-FIN-037  (REQ-FIN-037)
Exercises    : API-FIN-026 PATCH /api/v1/fin/fiscal-periods/{id}/hard-close
Rule / code  : — (happy path; RULE-FIN-015's gate has already admitted the caller)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a period ready to hard-close (SOFT_CLOSE); entries exist inside that period whose
               `createdBy` (DBF-FIN-047) is some other principal; the approver holds
               `PERM_FIN_PERIODS_CLOSE_APPROVE`
Steps        : 1. PATCH `/api/v1/fin/fiscal-periods/{id}/hard-close`
               2. read back the period and any entry of that period
Expected     : `closedBy` (DBF-FIN-083) and `closedAt` (DBF-FIN-084) hold the approving principal
               and the moment of approval, and `closedBy` is **distinct from every entry's
               `createdBy` in that period** — AC-FIN-037's own Then. The approval is therefore
               legible as a separate act, not inferred from any entry's audit columns
Test data    : principals are placeholders; the distinctness is the fact the AC states
<!-- TC:TC-FIN-037:END -->

<!-- TC:TC-FIN-038:START traces=AC-FIN-038,REQ-FIN-038,API-FIN-026 -->
### TC-FIN-038 — entry-creation permission alone does not buy a period close
Derived from : AC-FIN-038  (REQ-FIN-038)
Exercises    : API-FIN-026 PATCH /api/v1/fin/fiscal-periods/{id}/hard-close
Rule / code  : RULE-FIN-015 → FIN-403-FORBIDDEN (403)
Scenario     : PERMISSION · data class ATTACK · language ALL
Preconditions: a role holding `PERM_FIN_JOURNAL_ENTRIES_CREATE` and **not**
               `PERM_FIN_PERIODS_CLOSE_APPROVE`; a period in SOFT_CLOSE, so nothing but the
               permission can refuse the call
Steps        : 1. PATCH `/api/v1/fin/fiscal-periods/{id}/hard-close` as that role's user
Expected     : 403 with code FIN-403-FORBIDDEN —
               ar: «لا تملك صلاحية المالية المطلوبة لهذه العملية» ·
               en: "You do not hold the Finance permission required for this operation".
               The denial comes from the permission gate — SEC's own mechanism, as AC-FIN-038
               words it — and the period's `statusCode` is unchanged.
               **Assert nothing else.** There is no global user-set-disjointness check: a caller
               who does hold `PERM_FIN_PERIODS_CLOSE_APPROVE` succeeds even when some other user
               in the system also holds the creation permission, and `FIN-403-SOD-VIOLATION` is
               struck as unreachable. A test asserting that older behaviour fails by design
Test data    : the two permission codes are the registry's own
<!-- TC:TC-FIN-038:END -->

<!-- TC:TC-FIN-039:START traces=AC-FIN-039,REQ-FIN-039,API-FIN-028 -->
### TC-FIN-039 — the account ledger is computed live from posted lines
Derived from : AC-FIN-039  (REQ-FIN-039)
Exercises    : API-FIN-028 GET /api/v1/fin/reports/account-ledger
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: one account with POSTED lines across two periods; a caller holding
               `PERM_FIN_ACCOUNT_LEDGER_VIEW`
Steps        : 1. GET `/api/v1/fin/reports/account-ledger` with that `accountId` and a date range
                  spanning both periods
               2. post one further entry touching the same account, then repeat step 1
Expected     : a running-balance list built from those POSTED lines at the moment of the call,
               each row linking to its `journalEntryId`. Step 2's repeat reflects the new line
               immediately — **no stored balance column exists anywhere** to fall out of sync,
               which is AC-FIN-039's own "never from a stored column". Lines of entries that are
               not POSTED never appear
Test data    : two periods — AC-FIN-039's own; ids are placeholders
<!-- TC:TC-FIN-039:END -->

<!-- TC:TC-FIN-040:START traces=AC-FIN-040,REQ-FIN-040,API-FIN-029 -->
### TC-FIN-040 — the trial balance always balances
Derived from : AC-FIN-040  (REQ-FIN-040)
Exercises    : API-FIN-029 GET /api/v1/fin/reports/trial-balance
Rule / code  : RULE-FIN-006 restated as a report-level guarantee — an invariant by construction,
               not a separate check
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: any set of POSTED entries in a period; a controller holding
               `PERM_FIN_TRIAL_BALANCE_VIEW`
Steps        : 1. GET `/api/v1/fin/reports/trial-balance` with that `periodId`
               2. GET it again with `periodId` omitted
Expected     : 200 · one row per account, and **total debit balances equal total credit balances
               exactly** in both calls — AC-FIN-040's own Then. The sums match because every
               contributing entry individually balanced, not because the report corrects
               anything. Omitting `periodId` is a legitimate "no period narrowing" request and
               stays a 200
Test data    : none invented
<!-- TC:TC-FIN-040:END -->

<!-- TC:TC-FIN-041:START traces=AC-FIN-041,REQ-FIN-041,API-FIN-030 -->
### TC-FIN-041 — the new year's opening balances equal the prior year's closing balances
Derived from : AC-FIN-041  (REQ-FIN-041)
Exercises    : API-FIN-030 GET /api/v1/fin/reports/balance-sheet
Rule / code  : — (read-only; continuity is guaranteed by REQ-FIN-036's opening entry, not
               re-validated at read time)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a completed year-end close (the TC-FIN-036 outcome); a controller holding
               `PERM_FIN_BALANCE_SHEET_VIEW`
Steps        : 1. GET `/api/v1/fin/reports/balance-sheet` for the closed year
               2. GET it for the new year
Expected     : for **every** balance-sheet account, the new year's opening balance equals the
               prior year's closing balance — AC-FIN-041's own Then. Balances are grouped
               ASSET / LIABILITY / EQUITY and computed live from POSTED lines, the sign following
               each account's `natureCode`
Test data    : none invented
<!-- TC:TC-FIN-041:END -->

<!-- TC:TC-FIN-042:START traces=AC-FIN-042,REQ-FIN-042,API-FIN-031 -->
### TC-FIN-042 — the income statement opens a new year at zero
Derived from : AC-FIN-042  (REQ-FIN-042)
Exercises    : API-FIN-031 GET /api/v1/fin/reports/income-statement
Rule / code  : — (read-only; the zero opening is guaranteed by REQ-FIN-036 closing result
               accounts to Retained Earnings)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a completed year-end close, and **no posting yet** in the new year; a controller
               holding `PERM_FIN_INCOME_STATEMENT_VIEW`
Steps        : 1. GET `/api/v1/fin/reports/income-statement` for the new fiscal year
Expected     : 200 · **every** revenue and expense account shows a zero balance — AC-FIN-042's
               own Then — while the balance-sheet accounts of TC-FIN-041 carry their brought-
               forward balances. The contrast between the two reports is the point: result
               accounts restart, balance-sheet accounts continue
Test data    : none invented
<!-- TC:TC-FIN-042:END -->

<!-- TC:TC-FIN-043:START traces=AC-FIN-043,REQ-FIN-043,API-FIN-032 -->
### TC-FIN-043 — a dimension report never merges two dimension values into one account row
Derived from : AC-FIN-043  (REQ-FIN-043)
Exercises    : API-FIN-032 GET /api/v1/fin/reports/dimension
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: POSTED lines on **one** account carrying two different `PROJECT` dimension values;
               a controller holding `PERM_FIN_DIMENSION_REPORTS_VIEW`
Steps        : 1. GET `/api/v1/fin/reports/dimension` with `dimensionId` = the `PROJECT` dimension
Expected     : 200 · **two separate rows** for that account, one per project value, not one
               combined row — AC-FIN-043's own Then. The aggregation key is the full
               account-and-dimension-value combination, never the base account alone when a
               dimension is in play
Test data    : the `PROJECT` dimension and two of its values — AC-FIN-043's own shape
<!-- TC:TC-FIN-043:END -->

<!-- TC:TC-FIN-044:START traces=AC-FIN-044,REQ-FIN-044 -->
### TC-FIN-044 — FIN registers itself into SEC as data
Derived from : AC-FIN-044  (REQ-FIN-044)
Exercises    : — deployment-time onboarding; FIN publishes no endpoint for it, so no `API-*` is
               cited (ADR-FIN-009)
Rule / code  : — (registration, no rule)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: SEC v1 is gated and reachable — AC-FIN-044's own Given
Steps        : 1. run FIN's onboarding once against a database that has none of its registry
                  rows, together with FIN's forward security migrations up to and including the
                  v2 one that registers FIN_ACCOUNT_MAPPINGS
               2. read back SEC's module, screen and action registries
Expected     : SEC holds **one** ModuleRegistry row for FIN (ENT-SEC-004), **one** ScreenRegistry
               row per FIN screen (ENT-SEC-005) — the thirteen page codes FIN_ACCOUNTS …
               FIN_DIMENSION_REPORTS and, since v2, FIN_ACCOUNT_MAPPINGS — and **one**
               ActionRegistry row per action (ENT-SEC-006), thirty in total, each named
               `PERM_<PAGE_CODE>_<ACTION>`. They include the two custom actions
               `PERM_FIN_JOURNAL_ENTRIES_REVERSE` and `PERM_FIN_PERIODS_CLOSE_APPROVE`, and the
               three v2 actions VIEW, CREATE and UPDATE of FIN_ACCOUNT_MAPPINGS, which are also
               granted to `SYS_ADMIN`; no FIN page has a DELETE action. Onboarding is idempotent
               at the deployment boundary: it runs once, not per request
Test data    : the thirteen page codes and the thirty actions are the backend plan's own
               bootstrap rows, not invented
<!-- TC:TC-FIN-044:END -->

<!-- TC:TC-FIN-045:START traces=AC-FIN-045,REQ-FIN-045 -->
### TC-FIN-045 — FIN registers its fifteen lookup types into MDL
Derived from : AC-FIN-045  (REQ-FIN-045) — MODIFIED in v2
Exercises    : — deployment-time onboarding and the v2 lookup migration (db-script v2 BLOCK 8);
               no `API-*` publishes either (ADR-FIN-009). The registration is made into MDL,
               which this module does not own
Rule / code  : — (registration, no rule)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: MDL is gated and reachable, and FIN v2 is deployed — AC-FIN-045's own Given
Steps        : 1. run FIN's onboarding and its v2 migration
               2. read MDL's lookup types filtered by owner module `FIN`, with their values
Expected     : MDL holds exactly **15** LookupType rows owned by FIN — the thirteen of v1
               (ACCOUNT_TYPE, DEBIT_CREDIT, PERIOD_STATE, FISCAL_YEAR_STATUS, JOURNAL_TYPE,
               JOURNAL_STATUS, ACCOUNTING_EVENT_TYPE, PAYMENT_METHOD, ACCOUNT_DERIVATION_TYPE,
               AMOUNT_SOURCE_TYPE, DISTRIBUTION_TYPE, RECURRING_SCHEDULE_TYPE, RECURRING_FREQUENCY)
               plus FIN_EVENT_BUSINESS_FIELD and FIN_DIMENSION_VALUE_SOURCE. Within them:
               ACCOUNT_DERIVATION_TYPE holds exactly **2** active values, CONSTANT and MAPPING;
               FIN_EVENT_BUSINESS_FIELD holds **4** values (PAYMENT_METHOD, EXPENSE_TYPE_CODE,
               ORGANISATION_CODE, BRANCH_CODE); FIN_DIMENSION_VALUE_SOURCE holds **2** values
               (CONSTANT, BUSINESS_FIELD); PAYMENT_METHOD holds the value CHEQUE. FIN consumes no
               lookup it does not own. ACCOUNTING_EVENT_TYPE still seeds no value — host-defined
               — and that is the registered state, not an incomplete one. Running the migration
               a second time changes nothing: the seed is idempotent
Test data    : the fifteen keys and the value counts are AC-FIN-045's own and SRS A6's own list
<!-- TC:TC-FIN-045:END -->

<!-- TC:TC-FIN-046:START traces=AC-FIN-046,REQ-FIN-046,API-FIN-030,API-FIN-029,API-FIN-028,API-FIN-022 -->
### TC-FIN-046 — a statement line drills all the way down to its source event
Derived from : AC-FIN-046  (REQ-FIN-046)
Exercises    : API-FIN-030 → API-FIN-029 → API-FIN-028 → API-FIN-022, in that order
Rule / code  : — (read-only chain)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: at least one balance-sheet line whose balance comes from an event-sourced entry;
               a controller holding the VIEW permission of each of the four report and entry
               screens
Steps        : 1. GET the balance sheet and take one statement line
               2. GET the trial balance and find that line's account row
               3. GET the account ledger for that account
               4. GET the originating entry by the id the ledger row carries
Expected     : each step's output carries the key the next step needs, so the chain
               statement line → trial balance row → account ledger → originating entry →
               `eventReference` completes without a gap. For an entry that is not event-sourced
               the last hop reads `manual`, `recurring` or `allocation` instead of a reference —
               AC-FIN-046's own alternative
Test data    : none invented; the chain is AC-FIN-046's own
<!-- TC:TC-FIN-046:END -->

<!-- TC:TC-FIN-050:START traces=AC-FIN-027,REQ-FIN-027,API-FIN-018 -->
### TC-FIN-050 — a filter matching nothing is a successful empty page
Derived from : AC-FIN-027  (REQ-FIN-027) — its "exactly the matching entries" read at zero
Exercises    : API-FIN-018 POST /api/v1/fin/journal-entries/search
Rule / code  : — (read-only)
Scenario     : BOUNDARY · data class EDGE · language ALL
Preconditions: entries exist, but none in the combination of period and status the filter names;
               a caller holding `PERM_FIN_JOURNAL_ENTRIES_VIEW`
Steps        : 1. POST `/api/v1/fin/journal-entries/search` with that filter pair
Expected     : 200 · an **empty** `Page<JournalEntryResponse>`. An empty result is success, not a
               404 and not an error — the module's own search contract. "Exactly the matching
               entries" is honoured when the matching set is empty, and no entry of a neighbouring
               period or status leaks into the page
Test data    : none invented
<!-- TC:TC-FIN-050:END -->

<!-- TC:TC-FIN-051:START traces=AC-FIN-031,REQ-FIN-031,API-FIN-023 -->
### TC-FIN-051 — a period count other than twelve falls back to an even day split
Derived from : AC-FIN-031  (REQ-FIN-031) — the twelve-period limit REQ-FIN-031 states
Exercises    : API-FIN-023 POST /api/v1/fin/fiscal-years
Rule / code  : — (happy path, the documented fallback branch)
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: a finance administrator holding `PERM_FIN_PERIODS_CREATE`; a year code not yet used
Steps        : 1. POST `/api/v1/fin/fiscal-years` with a `periodCount` **other than 12**, or with
                  twelve over a span that is not a whole calendar year
Expected     : 201 · that many periods, each `statusCode` = OPEN, produced by the even day split:
               the days of the span divided into contiguous blocks, the first
               (totalDays mod periodCount) blocks one day longer, so **the last period ends on the
               year's end date and no day belongs to two periods**. Their names are «الفترة N» and
               "Period N", not month names — the calendar-month naming of TC-FIN-031 applies only
               at exactly twelve over a whole year
Test data    : the two naming patterns are REQ-FIN-031's own strings; the count is a placeholder
               on the other side of the stated limit
<!-- TC:TC-FIN-051:END -->

<!-- TC:TC-FIN-052:START traces=AC-FIN-038,REQ-FIN-038,API-FIN-027 -->
### TC-FIN-052 — the same permission gates the year-end close
Derived from : AC-FIN-038  (REQ-FIN-038) — its "period-close-approval action", on the second
               endpoint that carries it
Exercises    : API-FIN-027 POST /api/v1/fin/fiscal-years/{id}/year-end-close
Rule / code  : RULE-FIN-015 → FIN-403-FORBIDDEN (403)
Scenario     : PERMISSION · data class ATTACK · language ALL
Preconditions: a role holding `PERM_FIN_JOURNAL_ENTRIES_CREATE` and not
               `PERM_FIN_PERIODS_CLOSE_APPROVE`; a fiscal year that is OPEN with every period
               HARD_CLOSE, so the call would otherwise succeed
Steps        : 1. POST `/api/v1/fin/fiscal-years/{id}/year-end-close` as that role's user
Expected     : 403 with code FIN-403-FORBIDDEN —
               ar: «لا تملك صلاحية المالية المطلوبة لهذه العملية» ·
               en: "You do not hold the Finance permission required for this operation".
               The denial happens **before the method body runs**, so neither the successor year
               nor the Retained Earnings account is resolved and no entry is posted; the fiscal
               year stays OPEN. Assert the 403 at the gate, never from inside the service
Test data    : the two permission codes are the registry's own
<!-- TC:TC-FIN-052:END -->
<!-- SUB:API-SCENARIOS:END -->
<!-- PHASE:TEST-PLAN-BE:END -->

## TC TRACEABILITY INDEX

**AC → TC**

| AC | TC | AC | TC |
|---|---|---|---|
| AC-FIN-001 | TC-FIN-001 | AC-FIN-043 | TC-FIN-043 |
| AC-FIN-002 | TC-FIN-002 | AC-FIN-044 | TC-FIN-044 |
| AC-FIN-003 | TC-FIN-003 | AC-FIN-045 | TC-FIN-045 |
| AC-FIN-004 | TC-FIN-004 | AC-FIN-046 | TC-FIN-046 |
| AC-FIN-005 | TC-FIN-005 | AC-FIN-047 | TC-FIN-093 |
| AC-FIN-006 | TC-FIN-006 | AC-FIN-048 | TC-FIN-094 |
| AC-FIN-007 | TC-FIN-007 | AC-FIN-049 | TC-FIN-095 |
| AC-FIN-008 | TC-FIN-008 | AC-FIN-050 | TC-FIN-096 |
| AC-FIN-009 | TC-FIN-009, TC-FIN-047 | AC-FIN-051 | TC-FIN-097 |
| AC-FIN-010 | TC-FIN-010 | AC-FIN-052 | TC-FIN-098 |
| AC-FIN-011 | TC-FIN-011 | AC-FIN-053 | TC-FIN-099 |
| AC-FIN-012 | TC-FIN-012, TC-FIN-048 | AC-FIN-054 | TC-FIN-100 |
| AC-FIN-013 | TC-FIN-013 | AC-FIN-055 | TC-FIN-101 |
| AC-FIN-014 | TC-FIN-014, TC-FIN-049 | AC-FIN-056 | TC-FIN-102 |
| AC-FIN-015 | TC-FIN-015 | AC-FIN-057 | TC-FIN-103 |
| AC-FIN-016 | TC-FIN-016 | AC-FIN-058 | TC-FIN-104 |
| AC-FIN-017 | TC-FIN-017 | AC-FIN-059 | TC-FIN-105 |
| AC-FIN-018 | TC-FIN-018 | AC-FIN-060 | TC-FIN-106 |
| AC-FIN-019 | TC-FIN-019 | AC-FIN-061 | TC-FIN-107 |
| AC-FIN-020 | TC-FIN-020 | AC-FIN-062 | TC-FIN-108 |
| AC-FIN-021 | TC-FIN-021 | AC-FIN-063 | TC-FIN-109 |
| AC-FIN-022 | TC-FIN-022 | AC-FIN-064 | TC-FIN-110 |
| AC-FIN-023 | TC-FIN-023 | AC-FIN-065 | TC-FIN-111 |
| AC-FIN-024 | TC-FIN-024 | AC-FIN-066 | TC-FIN-112 |
| AC-FIN-025 | TC-FIN-025 | AC-FIN-067 | TC-FIN-113 |
| AC-FIN-026 | TC-FIN-026 | AC-FIN-068 | TC-FIN-114 |
| AC-FIN-027 | TC-FIN-027, TC-FIN-050 | AC-FIN-069 | TC-FIN-115 |
| AC-FIN-028 | TC-FIN-028 | AC-FIN-070 | TC-FIN-116 |
| AC-FIN-029 | TC-FIN-029 | AC-FIN-071 | TC-FIN-117 |
| AC-FIN-030 | TC-FIN-030 | AC-FIN-072 | TC-FIN-118 |
| AC-FIN-031 | TC-FIN-031, TC-FIN-051 | AC-FIN-073 | TC-FIN-119 |
| AC-FIN-032 | TC-FIN-032 | AC-FIN-074 | TC-FIN-120 |
| AC-FIN-033 | TC-FIN-033 | AC-FIN-075 | TC-FIN-121 |
| AC-FIN-034 | TC-FIN-034 | AC-FIN-076 | TC-FIN-122 |
| AC-FIN-035 | TC-FIN-035 | AC-FIN-077 | TC-FIN-123 |
| AC-FIN-036 | TC-FIN-036 | AC-FIN-078 | TC-FIN-124 |
| AC-FIN-037 | TC-FIN-037 | AC-FIN-079 | TC-FIN-125 |
| AC-FIN-038 | TC-FIN-038, TC-FIN-052 | AC-FIN-080 | TC-FIN-126 |
| AC-FIN-039 | TC-FIN-039 | AC-FIN-081 | TC-FIN-127 |
| AC-FIN-040 | TC-FIN-040 | AC-FIN-082 | TC-FIN-128 |
| AC-FIN-041 | TC-FIN-041 | AC-FIN-083 | TC-FIN-129 |
| AC-FIN-042 | TC-FIN-042 | — | — |

**REQ → TC** — for REQ-FIN-001 … REQ-FIN-046 each `REQ-FIN-<n>` carries the TC of its own
`AC-FIN-<n>`, plus the extensions in the first table. v2 requirements are not 1:1 with their
criteria, so the second table lists every one of them.

| REQ | TC |
|---|---|
| REQ-FIN-009 | TC-FIN-009, TC-FIN-047 |
| REQ-FIN-012 | TC-FIN-012, TC-FIN-048 |
| REQ-FIN-014 | TC-FIN-014, TC-FIN-049 |
| REQ-FIN-027 | TC-FIN-027, TC-FIN-050 |
| REQ-FIN-031 | TC-FIN-031, TC-FIN-051 |
| REQ-FIN-038 | TC-FIN-038, TC-FIN-052 |
| REQ-FIN-003 | TC-FIN-003 (and RULE-FIN-007's effect, asserted there) |
| REQ-FIN-017 | TC-FIN-017 (the transition) · TC-FIN-010, TC-FIN-014, TC-FIN-023, TC-FIN-026, TC-FIN-036 (the same unified path from every other source) |
| REQ-FIN-046 | TC-FIN-046 (the chain) · TC-FIN-039, TC-FIN-040, TC-FIN-041, TC-FIN-042 (each hop's own report) |

| REQ (v2) | AC | TC |
|---|---|---|
| REQ-FIN-047 | AC-FIN-047 | TC-FIN-093 |
| REQ-FIN-048 | AC-FIN-048 | TC-FIN-094 |
| REQ-FIN-049 | AC-FIN-049 | TC-FIN-095 |
| REQ-FIN-050 | AC-FIN-050, AC-FIN-051, AC-FIN-052 | TC-FIN-096, TC-FIN-097, TC-FIN-098 |
| REQ-FIN-051 | AC-FIN-053 | TC-FIN-099 |
| REQ-FIN-052 | AC-FIN-054, AC-FIN-055 | TC-FIN-100, TC-FIN-101 |
| REQ-FIN-053 | AC-FIN-056, AC-FIN-057 | TC-FIN-102, TC-FIN-103 |
| REQ-FIN-054 | AC-FIN-058 | TC-FIN-104 |
| REQ-FIN-055 | AC-FIN-059 | TC-FIN-105 |
| REQ-FIN-056 | AC-FIN-060 | TC-FIN-106 |
| REQ-FIN-057 | AC-FIN-061 | TC-FIN-107 |
| REQ-FIN-058 | AC-FIN-062 | TC-FIN-108 |
| REQ-FIN-059 | AC-FIN-063 | TC-FIN-109 |
| REQ-FIN-060 | AC-FIN-064 | TC-FIN-110 |
| REQ-FIN-061 | AC-FIN-065 | TC-FIN-111 |
| REQ-FIN-062 | AC-FIN-066 | TC-FIN-112 |
| REQ-FIN-063 | AC-FIN-067, AC-FIN-068 | TC-FIN-113, TC-FIN-114 |
| REQ-FIN-064 | AC-FIN-069 | TC-FIN-115 |
| REQ-FIN-065 | AC-FIN-070 | TC-FIN-116 |
| REQ-FIN-066 | AC-FIN-071 | TC-FIN-117 |
| REQ-FIN-067 | AC-FIN-072 | TC-FIN-118 |
| REQ-FIN-068 | AC-FIN-073 | TC-FIN-119 |
| REQ-FIN-069 | AC-FIN-074 | TC-FIN-120 |
| REQ-FIN-070 | AC-FIN-075 | TC-FIN-121 |
| REQ-FIN-071 | AC-FIN-076 | TC-FIN-122 |
| REQ-FIN-072 | AC-FIN-077, AC-FIN-078 | TC-FIN-123, TC-FIN-124 |
| REQ-FIN-073 | AC-FIN-079 | TC-FIN-125 |
| REQ-FIN-074 | AC-FIN-080 | TC-FIN-126 |
| REQ-FIN-075 | AC-FIN-081 | TC-FIN-127 |
| REQ-FIN-076 | AC-FIN-082 | TC-FIN-128 |
| REQ-FIN-077 | AC-FIN-083 | TC-FIN-129 |

**API → TC**

| API | TC |
|---|---|
| API-FIN-001 | TC-FIN-001, TC-FIN-002 (the unchanged-state assertion) |
| API-FIN-002 | TC-FIN-001 · TC-FIN-100 (setup: the child that makes a mapped account a parent) |
| API-FIN-003 | TC-FIN-002 |
| API-FIN-004 | TC-FIN-003 · TC-FIN-101 (setup: the mapped account deactivated) |
| API-FIN-005 | — no TC: no `AC-*` states a dimension search (ADR-FIN-009) |
| API-FIN-006 | TC-FIN-004 |
| API-FIN-007 | TC-FIN-005, TC-FIN-006 |
| API-FIN-008 | TC-FIN-005, TC-FIN-006 (the one-row assertion) |
| API-FIN-009 | — no TC: no `AC-*` states a rule search (ADR-FIN-009) |
| API-FIN-010 | TC-FIN-007 |
| API-FIN-011 | TC-FIN-008, TC-FIN-009, TC-FIN-047 · v2: TC-FIN-094, TC-FIN-095, TC-FIN-112, TC-FIN-115, TC-FIN-116 |
| API-FIN-012 | — no TC: no `AC-*` states a template search (ADR-FIN-009) |
| API-FIN-013 | TC-FIN-022 |
| API-FIN-014 | TC-FIN-023, TC-FIN-024 |
| API-FIN-015 | — no TC: no `AC-*` states an allocation-rule search (ADR-FIN-009) |
| API-FIN-016 | TC-FIN-025 |
| API-FIN-017 | TC-FIN-026 |
| API-FIN-018 | TC-FIN-027, TC-FIN-050, TC-FIN-011 (the one-entry assertion) |
| API-FIN-019 | TC-FIN-003, TC-FIN-014, TC-FIN-015, TC-FIN-017, TC-FIN-018, TC-FIN-019, TC-FIN-020, TC-FIN-021, TC-FIN-033, TC-FIN-049 · TC-FIN-121 (setup: manual entry B) |
| API-FIN-020 | TC-FIN-010, TC-FIN-011, TC-FIN-012, TC-FIN-013, TC-FIN-048 · v2: TC-FIN-093, TC-FIN-096 … TC-FIN-103, TC-FIN-107, TC-FIN-113, TC-FIN-114, TC-FIN-117 … TC-FIN-120, TC-FIN-123 … TC-FIN-128 |
| API-FIN-021 | TC-FIN-016, TC-FIN-028, TC-FIN-029, TC-FIN-030 · TC-FIN-129 |
| API-FIN-022 | TC-FIN-016, TC-FIN-017, TC-FIN-028, TC-FIN-046 · TC-FIN-108, TC-FIN-113, TC-FIN-114, TC-FIN-117, TC-FIN-121 |
| API-FIN-023 | TC-FIN-031, TC-FIN-051 |
| API-FIN-024 | TC-FIN-032, TC-FIN-035 |
| API-FIN-025 | TC-FIN-020, TC-FIN-033 |
| API-FIN-026 | TC-FIN-020, TC-FIN-034, TC-FIN-037, TC-FIN-038 |
| API-FIN-027 | TC-FIN-036, TC-FIN-052 |
| API-FIN-028 | TC-FIN-039, TC-FIN-046 · TC-FIN-108, TC-FIN-129 |
| API-FIN-029 | TC-FIN-040, TC-FIN-046 |
| API-FIN-030 | TC-FIN-041, TC-FIN-046 |
| API-FIN-031 | TC-FIN-042 |
| API-FIN-032 | TC-FIN-043 · TC-FIN-122, TC-FIN-129 |
| API-FIN-033 | — no TC: no `AC-*` states a fiscal-period search (ADR-FIN-009); it is cited only as setup |
| API-FIN-034 | — no TC of its own; cited as TC-FIN-013's alternative precondition (ADR-FIN-009) |
| API-FIN-035 | — no TC of its own; cited as the only precondition of TC-FIN-021 and TC-FIN-118 (ADR-FIN-009) |
| API-FIN-036 | — no TC: no `AC-*` states a template deactivate (ADR-FIN-009) |
| API-FIN-037 | — no TC: no `AC-*` states an allocation-rule deactivate (ADR-FIN-009) |
| API-FIN-038 | TC-FIN-105 · TC-FIN-109 (the one-active-row assertion) |
| API-FIN-039 | TC-FIN-104, TC-FIN-109, TC-FIN-110, TC-FIN-111 |
| API-FIN-040 | TC-FIN-106, TC-FIN-108 |
| API-FIN-041 | TC-FIN-107 · TC-FIN-103 (setup: the inactive mapping) |

**RULE / catalog code → TC**

| RULE | code | TC |
|---|---|---|
| RULE-FIN-001 | FIN-409-HAS-CHILDREN (409) | TC-FIN-002 |
| RULE-FIN-001 | FIN-409-PARENT-NOT-LEAF-ELIGIBLE (409) | — no TC: no `AC-*` states the create-path variant |
| RULE-FIN-002 | FIN-409-DIMVALUE-DUP (409) | TC-FIN-006 |
| RULE-FIN-003 | FIN-409-REMAINDER-COUNT (409) | TC-FIN-009, TC-FIN-047 |
| RULE-FIN-003 | FIN-422-REMAINDER-MARKER (422) | — no TC: no `AC-*` states marker disagreement |
| RULE-FIN-004 | FIN-409-DUPLICATE-EVENT (409) | TC-FIN-011 · v2: TC-FIN-123 (the pre-check), TC-FIN-124 (the store's refusal) |
| RULE-FIN-005 | FIN-404-NO-ACTIVE-RULE (404) | TC-FIN-013 |
| RULE-FIN-006 | FIN-409-UNBALANCED (409) | TC-FIN-015, TC-FIN-018 · TC-FIN-040 (the report invariant) |
| RULE-FIN-007 | FIN-409-NOT-POSTABLE-ACCOUNT (409) | TC-FIN-003, TC-FIN-015, TC-FIN-019 · v2: TC-FIN-100, TC-FIN-101 (a mapped account) |
| RULE-FIN-008 | FIN-409-PERIOD-NOT-OPEN (409) | TC-FIN-020, TC-FIN-033 · TC-FIN-036 (the carve-out, asserted as not raised) · v2: TC-FIN-125 (the event path), TC-FIN-127 (its published row) |
| RULE-FIN-009 | FIN-409-INVALID-DIMENSION (409) | TC-FIN-021 · v2: TC-FIN-118 (an event-derived value) · TC-FIN-117 (passes) |
| RULE-FIN-010 | — (success path) | TC-FIN-012, TC-FIN-026 · v2: TC-FIN-128 (a MAPPING remainder line) |
| RULE-FIN-010 | FIN-422-REMAINDER-NOT-POSITIVE (422) | TC-FIN-048 |
| RULE-FIN-011 | — (success path, no distinct code) | TC-FIN-028, TC-FIN-024 · v2: TC-FIN-129 |
| RULE-FIN-012 | — (success path, no distinct code) | TC-FIN-029, TC-FIN-024 |
| RULE-FIN-013 | FIN-409-ALREADY-REVERSED (409) | TC-FIN-030 |
| RULE-FIN-013 | FIN-409-NOT-POSTED (409) | — no TC: AC-FIN-030's Given is the already-reversed entry, not a DRAFT, and DRAFT is never a stored browsable state (SRS A7) |
| RULE-FIN-014 | FIN-409-NOT-REOPENABLE (409) | TC-FIN-035 · TC-FIN-032, TC-FIN-034 (the states around it) |
| RULE-FIN-015 | FIN-403-FORBIDDEN (403) | TC-FIN-038, TC-FIN-052 |
| RULE-FIN-016 | — (enforced by omission, no code) | TC-FIN-016, TC-FIN-017 · v2: TC-FIN-129 (the original of a new-path entry stays untouched) |
| RULE-FIN-017 | FIN-400-PERIOD-NOT-IN-YEAR, FIN-400-DOCDATE-OUTSIDE-PERIOD (400) | — no TC: no `AC-*` states the incoherent triple; RULE-FIN-017 was added at SVC-API and carries no AC of its own (ADR-FIN-009) |
| RULE-FIN-018 | FIN-422-UNSUPPORTED-DERIVATION (422) | TC-FIN-094 |
| RULE-FIN-019 | FIN-422-DERIVATION-SPEC (422) | — no TC raises it: AC-FIN-049 is the passing case, TC-FIN-095, and no `AC-*` states a mismatched spec (ADR-FIN-063) |
| RULE-FIN-020 | FIN-422-UNMAPPED-VALUE (422) | TC-FIN-102, TC-FIN-103, TC-FIN-107 · TC-FIN-096, TC-FIN-097, TC-FIN-098 (resolves) |
| RULE-FIN-021 | FIN-422-MISSING-BUSINESS-FIELD (422) | TC-FIN-099 (a MAPPING line's field), TC-FIN-119 (a tag's field) |
| RULE-FIN-022 | FIN-409-MAPPING-DUP (409) | TC-FIN-109 |
| RULE-FIN-023 | FIN-422-MAPPED-ACCOUNT-INELIGIBLE (422) | TC-FIN-110 |
| RULE-FIN-024 | FIN-422-INVALID-PAYMENT-METHOD (422) | TC-FIN-111 |
| RULE-FIN-025 | FIN-422-DIMENSION-TAG-DUP (422) | TC-FIN-115 |
| RULE-FIN-026 | FIN-422-DIMENSION-TAG-SOURCE (422) | TC-FIN-116 |
| RULE-FIN-027 | FIN-422-UNRESOLVED-DIMENSION-VALUE (422) | TC-FIN-120 · TC-FIN-114 (resolves) |
| RULE-FIN-028 | FIN-422-NO-DEFINED-PERIOD (422) | TC-FIN-126 |

**Catalog rows no TC exercises**, stated rather than left unsaid: FIN-409-ACCOUNT-DUP,
FIN-409-DIMENSION-DUP, FIN-409-RULE-DUP, FIN-409-YEAR-DUP, FIN-400-INVALID-LOOKUP,
FIN-400-MISSING-FREQUENCY, FIN-400-INVALID-SORT, FIN-422-REMAINDER-MARKER,
FIN-422-DERIVATION-SPEC, FIN-409-NOT-ACTIVE, FIN-409-INVALID-TRANSITION,
FIN-409-PERIODS-NOT-CLOSED, FIN-400-PERIOD-NOT-IN-YEAR, FIN-400-DOCDATE-OUTSIDE-PERIOD,
FIN-404-ACCOUNT-MAPPING, the `FIN-404-*` not-found rows raised outside an AC's own path, and
FIN-500. No `AC-FIN-*` states any of them, and this engine derives from the ACs alone. Writing a
TC for a code no acceptance criterion asserts is the over-engineering §3 rule 6 forbids. They are
a requirements-side gap if anyone wants them covered, not a test-generation one — ADR-FIN-009 and
ADR-FIN-063 record the boundary. Two rows are not gaps and must not be tested: FIN-403-SOD-VIOLATION
is struck as unreachable, and TC-FIN-038 says so; FIN-422-MAPPING-UNSUPPORTED is struck in v2,
MAPPING being implemented (ADR-FIN-027), and no path raises it.

## COVERAGE

```
AC  covered   83/83   ✓ 0 gaps   (every AC-FIN-001…083 carries ≥1 TC on this track; v2 adds 37,
                        AC-FIN-047…083, and AC-FIN-045 keeps TC-FIN-045, modified to its v2 text)
REQ covered   77/77   ✓ 0 gaps   (REQ-FIN-047…077 are new in v2)
API covered   32/41   — 9 endpoints carry no TC, each named in the API → TC index with the reason;
                        none is an AC left uncovered (ADR-FIN-009). The four v2 endpoints
                        API-FIN-038…041 are all covered. (v1 printed 26/37 over a table that
                        counts 28/37; the figure here is the table's own.)
TC count      89      ✓ 1.07× the AC count (52 v1 + 37 v2) — well under the ~2× guard
Integration   n/a     scope = module: no INT-XM phase is emitted and none is owed (§2 rule 3).
                      The one live XM, XM-FIN-001 (SOFT-READ → MDL, widened in v2 to 15 keys),
                      is cited only as a precondition on the lookup-validated writes, never
                      asserted as a cross-module scenario
```
══════════════════════════════════════════════════════════════════

<<<END INPUT>>>

<<<INPUT: frontend-test-plan (baseline — continue it, never restart it)>>>
# FRONTEND TEST PLAN — الحسابات العامة / Finance (General Ledger) (FIN)
══════════════════════════════════════════════════════════════════
Module : FIN   Version : v2 (delta on v1 — change set CS-FIN-001)   Profile : erp   Track : frontend   Plan : test
Scope  : **module** — modules FIN (`gov.py run-standalone test-gen --module FIN`)
Sources: `_state/current-srs.md` (v2) · `_state/current-frontend-execution-plan.md` (v2) ·
         `_state/current-backend-execution-plan.md` (v2) · `_state/current-registry-srs.md` (v2) ·
         `_state/current-registry-exec-fe.md` (v2) · baseline `_state/current-frontend-test-plan.md` (v1)
Framework : **agnostic** (`profile.stack.testing`). Every block below is the framework-agnostic
            TC form; no tool, annotation, fixture, selector or file layout is named anywhere in
            this plan. The consumer repo chooses its framework and turns each TC into a test.
REDUCED   : no — the frontend-execution-plan is present, so every step binds to a real `SCR-*`
            and its declared route, the v2 screen SCR-FIN-013 included. Since ADR-FIN-064 every
            call a case makes is a published endpoint: the account-mapping calls and the v2
            members of the add-rule-line call no longer wait on publication.
TC ids    : v1 TC-FIN-053 … TC-FIN-092 on this track; v2 continues the one shared module
            sequence at TC-FIN-130 … TC-FIN-145, after `backend-test-plan-fin.md`'s
            TC-FIN-093 … TC-FIN-129. No id is reused, none is renumbered, and no id is reassigned
            to a different subject than the one it carried.
Open ADRs : 2 raised by test-gen v2 — ADR-FIN-063 (first run) and ADR-FIN-066 (this run, which
            realigns the cases with the revised frontend plan), both ACCEPTED, non-breaking.
            Applied and not re-derived: ADR-FIN-009 (v1 test-gen), ADR-FIN-001 … ADR-FIN-008,
            ADR-FIN-024, ADR-FIN-025, ADR-FIN-039, ADR-FIN-061, ADR-FIN-062, ADR-FIN-064,
            ADR-FIN-065. The first run cited ADR-FIN-060 as BLOCKED; it is superseded by
            ADR-FIN-064.
══════════════════════════════════════════════════════════════════

**Delta v2 — what this file is.** A complete current plan, not a fragment. The reason is the one
the backend plan states: a marker plan folds by phase, so a SUB re-emitted with its v2 atoms
alone would lose its v1 atoms (ADR-FIN-063). Every v1 case whose content v2 leaves alone is
restated byte-identical.

- **ADDED** — sixteen cases, TC-FIN-130 … TC-FIN-145, one per v2 acceptance criterion a screen
  can exercise.
- **MODIFIED** — the v1 cases the v2 frontend plan made stale. The first v2 run changed four:
  the engine-rules cases TC-FIN-059, TC-FIN-060 and TC-FIN-061 for the one-line-per-submit form
  (ADR-FIN-062), and the fiscal-year case TC-FIN-079 for the fiscal-year search (ADR-FIN-061).
  This run (ADR-FIN-066) realigns the plan with the frontend plan as ADR-FIN-064 and
  ADR-FIN-065 revised it:
  - record pages: the five screens that own a child collection are now a list, a `/new` page
    and a `/:id` record page, so every case on SCR-FIN-002, 003, 004, 005 and 007 names its
    new route;
  - no client-side action gating: every affordance renders for a holder of the screen and the
    server's 403 decides, so each "holds the … affordance" precondition becomes the permission
    the server checks. The cases on hard-close, year-end close and the entry creator's close
    (TC-FIN-082, TC-FIN-084, TC-FIN-086) asserted a gate that no longer exists and are
    re-derived;
  - journal-entry line refusals: the published error body names no line, so the refusals of
    TC-FIN-055, TC-FIN-063, TC-FIN-067 and TC-FIN-069 are asserted in the lines pane's single
    refusal slot, never against a line;
  - the income statement sends its two period bounds independently (TC-FIN-090), and the
    balance sheet drills down from a group by its account type (TC-FIN-092).

  Unchanged in content: TC-FIN-065, TC-FIN-066, TC-FIN-075, TC-FIN-077, TC-FIN-078,
  TC-FIN-087, TC-FIN-088, TC-FIN-089 and TC-FIN-091. TC-FIN-091's record still runs on to the
  INT-FLOW START marker, whose `traces=` grew in the first v2 run, so it stays listed MODIFIED.
- **REMOVED** — none.

The change manifest lists **all 40** re-emitted v1 cases as MODIFIED, for the reason the backend
plan gives: the version check compares a marker atom's START line with the baseline's heading
record, so it reads a byte-identical case as re-defined. The cases whose content changed are the
ones named above; the nine listed as unchanged are word for word v1.

**Id order is AC order within this track.** In v1, TC-FIN-053 is AC-FIN-001's frontend case and
TC-FIN-092 is AC-FIN-046's. In v2, TC-FIN-130 … TC-FIN-145 follow the in-track v2 criteria in AC
order. The v2 cases **open** each SUB and the v1 cases follow, for the same record-text reason
the backend plan gives. The traceability index is the address.

**Twenty-seven ACs are out of this track, and none of them is a gap.** Every `AC-FIN-*` carries ≥1
TC across the module. This track's denominator is the ACs a screen can exercise: 40 of the 46 in
v1, and 16 of the 37 added in v2, so 56 of 83.

| AC | why it is not a frontend case | covered on the backend track by |
|---|---|---|
| AC-FIN-010, AC-FIN-011, AC-FIN-012, AC-FIN-013 | their When is "a canonical event arrives". API-FIN-020 is bound but drawn on no screen and constructed by no form — ADR-FIN-007, restated in F1-SCR-FIN-006 | TC-FIN-010, TC-FIN-011, TC-FIN-012, TC-FIN-013 |
| AC-FIN-044, AC-FIN-045 | onboarding, no screen — the P0.5 traceability matrix itself says so ("— (onboarding, no screen)"). AC-FIN-045 is MODIFIED in v2 and stays onboarding | TC-FIN-044, TC-FIN-045 |
| AC-FIN-047, AC-FIN-050 … AC-FIN-057, AC-FIN-067, AC-FIN-068, AC-FIN-071 … AC-FIN-074, AC-FIN-077 … AC-FIN-080, AC-FIN-082 (v2) | the same event path: the account a mapping resolves, the dimension a tag resolves, and every event rejection happen inside API-FIN-020, which no screen calls (ADR-FIN-007) | TC-FIN-093, TC-FIN-096 … TC-FIN-103, TC-FIN-113, TC-FIN-114, TC-FIN-117 … TC-FIN-120, TC-FIN-123 … TC-FIN-126, TC-FIN-128 |
| AC-FIN-081 (v2) | it asks what the published error catalogue says. That is a document the backend publishes, not a screen | TC-FIN-127 |

Entries built from an event are still **seen and reversed** on SCR-FIN-006 like any other source
(TC-FIN-143, TC-FIN-145). What no screen does is construct one. ADR-FIN-009 and ADR-FIN-063 record
this denominator, so that no later reader takes 56/56 for a silent narrowing of 83.

**What this track asserts and what it leaves to the server.** Where the frontend plan declares a
client-side pre-empt of a rule (F3), the TC asserts the pre-empt **and** the server's own refusal:
the pre-empt is a convenience, and the server stays authoritative. RULE-FIN-022 is the one v2
rule the plan explicitly does **not** pre-empt, and TC-FIN-137 asserts that too. Where the plan
routes an error to a generic surface rather than a field, the TC says generic and does not invent
a field binding. No UI copy is invented anywhere: the only strings asserted are the SRS's own
rule messages and the Error Catalog's own rows, in both languages. The deactivate confirmation on
SCR-FIN-013 has no stated text, so none is asserted. No case asserts an affordance hidden or
disabled for want of a grant: the plan gates none on the client, every affordance renders for a
holder of the screen, and the server's 403 is the control (ADR-FIN-065).

<!-- PHASE:TEST-PLAN-FE:START traces=REQ-FIN-001,REQ-FIN-002,REQ-FIN-003,REQ-FIN-004,REQ-FIN-005,REQ-FIN-006,REQ-FIN-007,REQ-FIN-008,REQ-FIN-009,REQ-FIN-014,REQ-FIN-015,REQ-FIN-016,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,REQ-FIN-022,REQ-FIN-023,REQ-FIN-024,REQ-FIN-025,REQ-FIN-026,REQ-FIN-027,REQ-FIN-028,REQ-FIN-029,REQ-FIN-030,REQ-FIN-031,REQ-FIN-032,REQ-FIN-033,REQ-FIN-034,REQ-FIN-035,REQ-FIN-036,REQ-FIN-037,REQ-FIN-038,REQ-FIN-039,REQ-FIN-040,REQ-FIN-041,REQ-FIN-042,REQ-FIN-043,REQ-FIN-046,REQ-FIN-048,REQ-FIN-049,REQ-FIN-054,REQ-FIN-055,REQ-FIN-056,REQ-FIN-057,REQ-FIN-058,REQ-FIN-059,REQ-FIN-060,REQ-FIN-061,REQ-FIN-062,REQ-FIN-064,REQ-FIN-065,REQ-FIN-070,REQ-FIN-071,REQ-FIN-077,AC-FIN-001,AC-FIN-002,AC-FIN-003,AC-FIN-004,AC-FIN-005,AC-FIN-006,AC-FIN-007,AC-FIN-008,AC-FIN-009,AC-FIN-014,AC-FIN-015,AC-FIN-016,AC-FIN-017,AC-FIN-018,AC-FIN-019,AC-FIN-020,AC-FIN-021,AC-FIN-022,AC-FIN-023,AC-FIN-024,AC-FIN-025,AC-FIN-026,AC-FIN-027,AC-FIN-028,AC-FIN-029,AC-FIN-030,AC-FIN-031,AC-FIN-032,AC-FIN-033,AC-FIN-034,AC-FIN-035,AC-FIN-036,AC-FIN-037,AC-FIN-038,AC-FIN-039,AC-FIN-040,AC-FIN-041,AC-FIN-042,AC-FIN-043,AC-FIN-046,AC-FIN-048,AC-FIN-049,AC-FIN-058,AC-FIN-059,AC-FIN-060,AC-FIN-061,AC-FIN-062,AC-FIN-063,AC-FIN-064,AC-FIN-065,AC-FIN-066,AC-FIN-069,AC-FIN-070,AC-FIN-075,AC-FIN-076,AC-FIN-083 -->
## PHASE — TEST-PLAN-FE

TC count is 40, above the split threshold of 8, so this phase is split into the two groups the
engine names: per-screen flows, and the flows that only exist across screens.

<!-- SUB:UI-FLOWS:START traces=REQ-FIN-001,REQ-FIN-002,REQ-FIN-004,REQ-FIN-005,REQ-FIN-006,REQ-FIN-007,REQ-FIN-008,REQ-FIN-009,REQ-FIN-014,REQ-FIN-015,REQ-FIN-016,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,REQ-FIN-022,REQ-FIN-025,REQ-FIN-027,REQ-FIN-028,REQ-FIN-029,REQ-FIN-030,REQ-FIN-031,REQ-FIN-032,REQ-FIN-033,REQ-FIN-034,REQ-FIN-035,REQ-FIN-036,REQ-FIN-037,REQ-FIN-038,REQ-FIN-039,REQ-FIN-040,REQ-FIN-041,REQ-FIN-042,REQ-FIN-043,REQ-FIN-048,REQ-FIN-049,REQ-FIN-054,REQ-FIN-055,REQ-FIN-056,REQ-FIN-057,REQ-FIN-059,REQ-FIN-060,REQ-FIN-061,REQ-FIN-062,REQ-FIN-064,REQ-FIN-065,REQ-FIN-070,REQ-FIN-071,REQ-FIN-077,AC-FIN-001,AC-FIN-002,AC-FIN-004,AC-FIN-005,AC-FIN-006,AC-FIN-007,AC-FIN-008,AC-FIN-009,AC-FIN-014,AC-FIN-015,AC-FIN-016,AC-FIN-017,AC-FIN-018,AC-FIN-019,AC-FIN-020,AC-FIN-021,AC-FIN-022,AC-FIN-025,AC-FIN-027,AC-FIN-028,AC-FIN-029,AC-FIN-030,AC-FIN-031,AC-FIN-032,AC-FIN-033,AC-FIN-034,AC-FIN-035,AC-FIN-036,AC-FIN-037,AC-FIN-038,AC-FIN-039,AC-FIN-040,AC-FIN-041,AC-FIN-042,AC-FIN-043,AC-FIN-048,AC-FIN-049,AC-FIN-058,AC-FIN-059,AC-FIN-060,AC-FIN-061,AC-FIN-063,AC-FIN-064,AC-FIN-065,AC-FIN-066,AC-FIN-069,AC-FIN-070,AC-FIN-075,AC-FIN-076,AC-FIN-083 -->
### SUB — UI-FLOWS

One screen, one flow: search, create, the violation shown on the screen that caused it, the
state a row moves to, and the refusal the server gives a caller who lacks the grant.

**v2 cases open the SUB** (TC-FIN-130 … TC-FIN-145, the screen half of CS-FIN-001), and the v1
cases follow. The account-mapping screen SCR-FIN-013 and the v2 half of the engine-rules Line
entry on SCR-FIN-003 call endpoints the republished api-docs carry: the four account-mapping
endpoints API-FIN-038 … API-FIN-041, the v2 members of API-FIN-011, and all twelve v2
catalogue codes (ADR-FIN-064). No case waits on publication any longer. The messages asserted
are the SRS's own rule messages, in both languages.

<!-- TC:TC-FIN-130:START traces=AC-FIN-048,REQ-FIN-048,SCR-FIN-003 -->
### TC-FIN-130 — the Line entry never offers DIRECT, and the server refuses it anyway
Derived from : AC-FIN-048  (REQ-FIN-048)
Exercises    : SCR-FIN-003 `/fin/rules/:ruleId` — the Line entry Drawer, `?lineAction=create`
Rule / code  : RULE-FIN-018 → FIN-422-UNSUPPORTED-DERIVATION (422), shown inline on the derivation
               type
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: `FIN_RULES` is in the caller's effective menu and the caller's role holds
               `PERM_FIN_RULES_UPDATE`, which the server checks; a rule's record page is open; MDL
               holds CONSTANT and MAPPING as the only active `ACCOUNT_DERIVATION_TYPE` values
               (UXD-FIN-008)
Steps        : 1. open the Line entry and open the account-derivation select
               2. submit a line whose derivation type is DIRECT, bypassing the select
Expected     : step 1 → the select offers exactly the list MDL returns, CONSTANT and MAPPING;
                  DIRECT is not offered and no value is hardcoded in the screen.
               step 2 → the server's refusal is shown inline on the derivation type —
                  ar: «نوع اشتقاق الحساب غير مدعوم؛ المسموح قيمة ثابتة أو ربط» ·
                  en: "Unsupported account derivation type; only constant or mapping is allowed".
                  No line is added to the rule
Test data    : DIRECT — AC-FIN-048's own value
<!-- TC:TC-FIN-130:END -->

<!-- TC:TC-FIN-131:START traces=AC-FIN-049,REQ-FIN-049,SCR-FIN-003 -->
### TC-FIN-131 — a MAPPING line is entered with a business field and no account
Derived from : AC-FIN-049  (REQ-FIN-049)
Exercises    : SCR-FIN-003 `/fin/rules/:ruleId` — the Line entry Drawer, `?lineAction=create`
Rule / code  : RULE-FIN-019 → pre-empted on the form (the derivation spec matches its type)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the record page of an active rule for "EXPENSE_PAID" is open; the
               `FIN_EVENT_BUSINESS_FIELD` option list has loaded (UXD-FIN-013); the caller's role
               holds `PERM_FIN_RULES_UPDATE`
Steps        : 1. open the Line entry and choose derivation type MAPPING
               2. observe the account and business-field controls
               3. choose business field EXPENSE_TYPE_CODE, fill the amount-source, direction and
                  distribution fields, and save
Expected     : step 2 → the business-field control becomes required and the constant-account
                  control empty and not required: a MAPPING line carries a business field and no
                  account code, the RULE-FIN-019 pre-empt.
               step 3 → **one** call adds the line, which appears among the rule's lines with
                  business field EXPENSE_TYPE_CODE shown by its label in the caller's language
Test data    : EXPENSE_PAID and EXPENSE_TYPE_CODE — AC-FIN-049's own values
<!-- TC:TC-FIN-131:END -->

<!-- TC:TC-FIN-132:START traces=AC-FIN-058,REQ-FIN-054,SCR-FIN-013 -->
### TC-FIN-132 — an account mapping is created from the account-mappings screen
Derived from : AC-FIN-058  (REQ-FIN-054)
Exercises    : SCR-FIN-013 `/fin/account-mappings` — the Entry, `?entry=new`
Rule / code  : — (happy path; RULE-FIN-023 and RULE-FIN-024 are pre-empted by the account picker
               and the payment-method select)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: `FIN_ACCOUNT_MAPPINGS` is in the caller's effective menu, and the caller's role
               holds `PERM_FIN_ACCOUNT_MAPPINGS_CREATE`, which the server checks; the
               three option lists have loaded (UXD-FIN-007, UXD-FIN-013, UXD-FIN-015); no active
               mapping holds (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE"); account 1102 is an
               active leaf
Steps        : 1. navigate to `/fin/account-mappings` and open a new entry
               2. choose event type PAYMENT_RECEIVED and business field PAYMENT_METHOD, and
                  observe the business-value control
               3. choose CHEQUE; open the account picker (`?picker=account`) and pick 1102
               4. save
Expected     : step 2 → while the business field is PAYMENT_METHOD, the business value is a
                  select over the `PAYMENT_METHOD` option list, not free text.
               step 3 → the picker is its own view, a sibling of the Entry, offering active leaf
                  accounts only; it returns 1102 to the Entry's form.
               step 4 → **one** call creates the mapping, and the list refreshes to show it
                  active with the account's code and its name in the caller's language
Test data    : PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE" and 1102 — AC-FIN-058's own values
<!-- TC:TC-FIN-132:END -->

<!-- TC:TC-FIN-133:START traces=AC-FIN-059,REQ-FIN-055,SCR-FIN-013 -->
### TC-FIN-133 — filtering the mapping list returns exactly the matching rows
Derived from : AC-FIN-059  (REQ-FIN-055)
Exercises    : SCR-FIN-013 `/fin/account-mappings`
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: 3 mappings for PAYMENT_RECEIVED (CASH active, CHEQUE active, TRANSFER inactive)
               and 2 for EXPENSE_PAID; `FIN_ACCOUNT_MAPPINGS` is in the caller's effective menu
Steps        : 1. filter by event type PAYMENT_RECEIVED and active only
               2. copy the resulting URL, open it fresh, and compare
Expected     : step 1 → exactly 2 rows, CASH and CHEQUE, each showing its account's code and name.
               step 2 → the same two rows: the filters live in the route's search parameters and
                  the page and page size are part of the filter that keys the list, so paging
                  re-queries the server
Test data    : the 3 + 2 mappings and the filter pair — AC-FIN-059's own
<!-- TC:TC-FIN-133:END -->

<!-- TC:TC-FIN-134:START traces=AC-FIN-060,REQ-FIN-056,SCR-FIN-013 -->
### TC-FIN-134 — only the account of an existing mapping can be changed
Derived from : AC-FIN-060  (REQ-FIN-056)
Exercises    : SCR-FIN-013 `/fin/account-mappings` — the Entry, `?entry=<row id>`
Rule / code  : RULE-FIN-023 → pre-empted by the account picker
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the mapping (EXPENSE_PAID, EXPENSE_TYPE_CODE, "RENT") → account 5220; account 5225
               is an active leaf; the caller's role holds `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE`
Steps        : 1. open that mapping's entry from its row
               2. observe the event type, business field and business value controls
               3. pick 5225 in the account picker and save
Expected     : step 2 → the three key fields render **read-only**.
               step 3 → one call sends the account alone, never the fixed key; the row then
                  shows account 5225 with its event type, business field and business value
                  unchanged
Test data    : 5220, 5225 and "RENT" — AC-FIN-060's own values
<!-- TC:TC-FIN-134:END -->

<!-- TC:TC-FIN-135:START traces=AC-FIN-061,REQ-FIN-057,SCR-FIN-013 -->
### TC-FIN-135 — a deactivated mapping leaves the active list
Derived from : AC-FIN-061  (REQ-FIN-057)
Exercises    : SCR-FIN-013 `/fin/account-mappings`
Rule / code  : — (state change; the event half of AC-FIN-061 is asserted by TC-FIN-107, no
               screen building an event)
Scenario     : STATE · data class VALID · language ALL
Preconditions: an active mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CASH") → account 1101; the
               caller's role holds `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE`
Steps        : 1. invoke Deactivate on that row and confirm
               2. filter the list by active only, then by inactive only
Expected     : step 1 → Deactivate is a confirmed row action with its own single call, never a
                  field of the Entry; the row reads inactive. There is **no Delete affordance**
                  anywhere on the screen: a mapping is only ever deactivated.
               step 2 → the mapping is gone from the active results and present in the inactive
                  ones
Test data    : "CASH" and 1101 — AC-FIN-061's own values
<!-- TC:TC-FIN-135:END -->

<!-- TC:TC-FIN-137:START traces=AC-FIN-063,REQ-FIN-059,SCR-FIN-013 -->
### TC-FIN-137 — a duplicate active mapping is refused on the key fields
Derived from : AC-FIN-063  (REQ-FIN-059)
Exercises    : SCR-FIN-013 `/fin/account-mappings` — the Entry, `?entry=new`
Rule / code  : RULE-FIN-022 → FIN-409-MAPPING-DUP (409), shown inline on the three key fields
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an active mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102;
               account 1103 is an active leaf; the caller's role holds
               `PERM_FIN_ACCOUNT_MAPPINGS_CREATE`
Steps        : 1. create a second mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → 1103 and
                  save
Expected     : the form sends the request — RULE-FIN-022 is **not** pre-empted client-side, the
               loaded page possibly not holding the conflicting row — and the server's refusal is
               shown inline on the event type, business field and business value —
               ar: «يوجد بالفعل ربط نشط لنوع الحدث والحقل التجاري والقيمة نفسها» ·
               en: "An active mapping already exists for this event type, business field and
               value".
               The list still shows exactly one active mapping for that key
Test data    : 1102, 1103 and "CHEQUE" — AC-FIN-063's own values
<!-- TC:TC-FIN-137:END -->

<!-- TC:TC-FIN-138:START traces=AC-FIN-064,REQ-FIN-060,SCR-FIN-013 -->
### TC-FIN-138 — a parent account is neither offered nor accepted for a mapping
Derived from : AC-FIN-064  (REQ-FIN-060)
Exercises    : SCR-FIN-013 `/fin/account-mappings` — the account picker, `?picker=account`
Rule / code  : RULE-FIN-023 → FIN-422-MAPPED-ACCOUNT-INELIGIBLE (422), shown inline on the account
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: account 1100 is a parent (rollup) account with `isLeafFl` false; the caller's role
               holds `PERM_FIN_ACCOUNT_MAPPINGS_CREATE`
Steps        : 1. open a new entry and open the account picker, searching for 1100
               2. submit a mapping whose account is 1100, bypassing the picker
Expected     : step 1 → 1100 is not offered: the picker lists active leaf accounts only.
               step 2 → the server's refusal is shown inline on the account field —
                  ar: «الحساب المربوط يجب أن يكون حسابًا ورقيًا نشطًا» ·
                  en: "The mapped account must be an active leaf account" —
                  and no row is added to the list
Test data    : 1100 — AC-FIN-064's own value; the key is a placeholder
<!-- TC:TC-FIN-138:END -->

<!-- TC:TC-FIN-139:START traces=AC-FIN-065,REQ-FIN-061,SCR-FIN-013 -->
### TC-FIN-139 — a misspelt payment method cannot be entered, and is refused if sent
Derived from : AC-FIN-065  (REQ-FIN-061)
Exercises    : SCR-FIN-013 `/fin/account-mappings` — the Entry, `?entry=new`
Rule / code  : RULE-FIN-024 → FIN-422-INVALID-PAYMENT-METHOD (422), shown inline on the business
               value
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: PAYMENT_METHOD holds CASH and CHEQUE as active values (UXD-FIN-015); the caller's
               role holds `PERM_FIN_ACCOUNT_MAPPINGS_CREATE`
Steps        : 1. choose business field PAYMENT_METHOD and try to type "CHEQEU" as the value
               2. submit (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQEU"), bypassing the select
Expected     : step 1 → the value is a select over CASH and CHEQUE; free text is not accepted.
               step 2 → the server's refusal is shown inline on the business value —
                  ar: «القيمة ليست طريقة دفع معرَّفة» ·
                  en: "The value is not a defined payment method" —
                  and no row is added
Test data    : "CHEQEU" — AC-FIN-065's own misspelling
<!-- TC:TC-FIN-139:END -->

<!-- TC:TC-FIN-140:START traces=AC-FIN-066,REQ-FIN-062,SCR-FIN-003 -->
### TC-FIN-140 — a rule line is saved with its dimension tags in one submit
Derived from : AC-FIN-066  (REQ-FIN-062)
Exercises    : SCR-FIN-003 `/fin/rules/:ruleId` — the Line entry `?lineAction=create` and its tag
               editor `?picker=tags`
Rule / code  : RULE-FIN-025, RULE-FIN-026 → pre-empted in the tag editor
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: dimensions ORGANISATION (value "ORG-1") and BRANCH, both active; the
               `FIN_DIMENSION_VALUE_SOURCE` and `FIN_EVENT_BUSINESS_FIELD` option lists have
               loaded (UXD-FIN-014, UXD-FIN-013); a rule's record page is open; the caller's role
               holds `PERM_FIN_RULES_UPDATE`
Steps        : 1. fill a line, then open its tag editor from the tags summary row
               2. add {ORGANISATION: CONSTANT "ORG-1"} — picking the value in the dimension-value
                  picker — and {BRANCH: BUSINESS_FIELD BRANCH_CODE}, and return to the line
               3. save the line
Expected     : step 2 → the tag editor is its own view, a sibling of the Line entry, and returns
                  the tag list to it; the summary row reads two tags. A CONSTANT tag asks for a
                  value and no business field, a BUSINESS_FIELD tag the reverse.
               step 3 → **one** call carries the line together with both tags, never a line
                  save followed by a tag save. The line appears among the rule's lines with
                  its two tags
Test data    : ORGANISATION, "ORG-1", BRANCH and BRANCH_CODE — AC-FIN-066's own values
<!-- TC:TC-FIN-140:END -->

<!-- TC:TC-FIN-141:START traces=AC-FIN-069,REQ-FIN-064,SCR-FIN-003 -->
### TC-FIN-141 — a second tag for the same dimension is stopped in the tag editor
Derived from : AC-FIN-069  (REQ-FIN-064)
Exercises    : SCR-FIN-003 `/fin/rules/:ruleId` — the tag editor, `?picker=tags`
Rule / code  : RULE-FIN-025 → FIN-422-DIMENSION-TAG-DUP (422), shown inline on the tags summary row
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a rule's record page is open; BRANCH holds "BR-01"; the caller's role holds
               `PERM_FIN_RULES_UPDATE`
Steps        : 1. in the tag editor add {BRANCH: CONSTANT "BR-01"}, then a second tag on BRANCH
               2. submit the line with both tags, bypassing the editor's check
Expected     : step 1 → the editor refuses the second BRANCH tag: the dimension is unique across
                  a line's tags, the RULE-FIN-025 pre-empt.
               step 2 → the server's refusal is shown inline on the tags summary row —
                  ar: «لا يجوز وسم البُعد نفسه مرتين على سطر القاعدة» ·
                  en: "A rule line cannot tag the same dimension twice" —
                  and no line is added
Test data    : BRANCH, "BR-01" and BRANCH_CODE — AC-FIN-069's own values
<!-- TC:TC-FIN-141:END -->

<!-- TC:TC-FIN-142:START traces=AC-FIN-070,REQ-FIN-065,SCR-FIN-003 -->
### TC-FIN-142 — a CONSTANT tag can only take a value of its own dimension
Derived from : AC-FIN-070  (REQ-FIN-065)
Exercises    : SCR-FIN-003 `/fin/rules/:ruleId` — the tag editor and its dimension-value picker
               `?picker=dimension-value`
Rule / code  : RULE-FIN-026 → FIN-422-DIMENSION-TAG-SOURCE (422), shown inline on the tags summary
               row
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: dimensions BRANCH and ORGANISATION, "ORG-1" being a value of ORGANISATION; the
               caller's role holds `PERM_FIN_RULES_UPDATE`; a rule's record page is open
Steps        : 1. add a CONSTANT tag on BRANCH and open its value picker, looking for "ORG-1"
               2. submit the line tagged {BRANCH: CONSTANT "ORG-1"}, bypassing the picker
Expected     : step 1 → "ORG-1" is not offered: the picker is scoped to the tag's own dimension
                  and to active values.
               step 2 → the server's refusal is shown inline on the tags summary row —
                  ar: «وسم البُعد لا يطابق مصدر قيمته» ·
                  en: "The dimension tag does not match its value source" —
                  and no line is added
Test data    : BRANCH, ORGANISATION and "ORG-1" — AC-FIN-070's own values
<!-- TC:TC-FIN-142:END -->

<!-- TC:TC-FIN-143:START traces=AC-FIN-075,REQ-FIN-070,SCR-FIN-006 -->
### TC-FIN-143 — an event-built entry's dimensions display exactly as a manual entry's
Derived from : AC-FIN-075  (REQ-FIN-070)
Exercises    : SCR-FIN-006 `/fin/journal-entries/:id`
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: entry A posted from event `EVT-3016` with BRANCH "BR-07" on its debit line, and
               manual entry B posted with BRANCH "BR-07" on its debit line; the caller holds
               `FIN_JOURNAL_ENTRIES` VIEW
Steps        : 1. open entry A's read route
               2. open entry B's read route
Expected     : both debit lines show exactly one dimension, BRANCH, with the value "BR-07" by its
               name and code, drawn by the same line component. The entries differ on screen only
               in their journal type — EVENT_GENERATED against MANUAL, labelled from UXD-FIN-005 —
               and in A's event reference
Test data    : `EVT-3016` and "BR-07" — AC-FIN-075's own values
<!-- TC:TC-FIN-143:END -->

<!-- TC:TC-FIN-144:START traces=AC-FIN-076,REQ-FIN-071,SCR-FIN-012 -->
### TC-FIN-144 — the dimension report screen shows one row for both entries
Derived from : AC-FIN-076  (REQ-FIN-071)
Exercises    : SCR-FIN-012 `/fin/reports/dimension`
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the two entries of AC-FIN-075, each debiting account 5100 with 100.00 under
               BRANCH "BR-07"; the caller holds `FIN_DIMENSION_REPORTS`
Steps        : 1. run the report by the BRANCH dimension
Expected     : one row for (5100, "BR-07") with a debit total of 200.00. The screen applies no
               journal-type filter, so event-built and manual lines are counted together
Test data    : 5100, 100.00, 200.00 and "BR-07" — AC-FIN-076's own values
<!-- TC:TC-FIN-144:END -->

<!-- TC:TC-FIN-145:START traces=AC-FIN-083,REQ-FIN-077,SCR-FIN-006 -->
### TC-FIN-145 — an event-built entry is reversed from the journal screen like any other
Derived from : AC-FIN-083  (REQ-FIN-077)
Exercises    : SCR-FIN-006 `/fin/journal-entries/:id`
Rule / code  : RULE-FIN-011 → success path, no code
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: entry `JV-2026-000600`, posted from event `EVT-4006` with a mapped debit account
               and BRANCH "BR-07" on its lines; the caller's role holds the custom
               `PERM_FIN_JOURNAL_ENTRIES_REVERSE`
Steps        : 1. open the entry and invoke Reverse, confirming
               2. open the created reversal
Expected     : step 1 → Reverse is offered on this EVENT_GENERATED entry exactly as on a manual
                  one; one call posts the reversal.
               step 2 → its lines show the same accounts, amounts and BRANCH value with the
                  opposite direction, each row showing the other's link, and the original still
                  reads POSTED
Test data    : `JV-2026-000600`, `EVT-4006` and "BR-07" — AC-FIN-083's own values
<!-- TC:TC-FIN-145:END -->

<!-- TC:TC-FIN-053:START traces=AC-FIN-001,REQ-FIN-001,SCR-FIN-001 -->
### TC-FIN-053 — an account is created from the chart screen
Derived from : AC-FIN-001  (REQ-FIN-001)
Exercises    : SCR-FIN-001 `/fin/accounts`
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the effective menu the security module serves carries `FIN_ACCOUNTS`, and the
               caller's role holds `PERM_FIN_ACCOUNTS_CREATE`, which the server checks — the
               affordance renders for every holder of the screen (ADR-FIN-065); the
               `ACCOUNT_TYPE` and `DEBIT_CREDIT` option lists have loaded (UXD-FIN-001, UXD-FIN-002)
Steps        : 1. navigate to `/fin/accounts`
               2. open the entry side of the composite screen and fill code, both names, account
                  type, nature, optionally a parent through the parent-account picker
                  (`?picker=parent`), and the leaf flag
               3. submit
Expected     : the row is created and the list refreshes to include it — the screen's list cache
               is invalidated by the create, so no manual reload is needed. The type and nature
               selects offer the option lists MDL returns and submit **codes**, never labels.
               There is no `:id` route: the entry form hydrates from the search row (ADR-FIN-006)
Test data    : `ASSET` and `DEBIT` are seeded values; the code and names are placeholders
<!-- TC:TC-FIN-053:END -->

<!-- TC:TC-FIN-054:START traces=AC-FIN-002,REQ-FIN-002,SCR-FIN-001 -->
### TC-FIN-054 — the leaf flag is refused on an account that has children
Derived from : AC-FIN-002  (REQ-FIN-002)
Exercises    : SCR-FIN-001 `/fin/accounts`
Rule / code  : RULE-FIN-001 → FIN-409-HAS-CHILDREN (409), shown inline
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an account with at least one child, visible in the loaded tree; the caller's role
               holds `PERM_FIN_ACCOUNTS_UPDATE`
Steps        : 1. navigate to `/fin/accounts` and select the parent account
               2. observe the leaf-flag control
               3. submit the update with the leaf flag set, bypassing the client pre-empt
Expected     : step 2 → the leaf control is **disabled and forced false** while the account has
               ≥1 child in the loaded tree — the declared client pre-empt of RULE-FIN-001.
               step 3 → the server's refusal is shown **inline on the leaf-flag field**, carrying
               the catalog message in the caller's language —
               ar: «لا يمكن لحساب له حسابات فرعية أن يقبل ترحيلاً مباشرًا» ·
               en: "An account with sub-accounts cannot accept direct posting".
               The pre-empt is a convenience; the server stays authoritative and the row is
               unchanged either way
Test data    : parent and child are placeholders
<!-- TC:TC-FIN-054:END -->

<!-- TC:TC-FIN-056:START traces=AC-FIN-004,REQ-FIN-004,SCR-FIN-002 -->
### TC-FIN-056 — a dimension is created on its `/new` page and opens as a record page
Derived from : AC-FIN-004  (REQ-FIN-004)
Exercises    : SCR-FIN-002 `/fin/dimensions/new` → `/fin/dimensions/:dimensionId`
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: `FIN_DIMENSIONS` is in the caller's effective menu, and the caller's role holds
               `PERM_FIN_DIMENSIONS_CREATE`, which the server checks
Steps        : 1. navigate to `/fin/dimensions/new` and observe the values section
               2. create a dimension with code `PROJECT` and both names
               3. open `/fin/dimensions`, then open the record page's URL in a fresh session
Expected     : step 1 → the values section is locked and says why: values are added under a
                  saved dimension.
               step 2 → one call creates the dimension, and the page moves to
                  `/fin/dimensions/:dimensionId` with the id from the create response: the header
                  in a collapsible section, the (empty) values beneath it.
               step 3 → the list shows the dimension as active, and the cold deep link renders
                  the same record page. It reads through the published dimension search filtered
                  by `dimensionPk`, never through an invented by-id endpoint (ADR-FIN-065)
Test data    : `PROJECT` — ENT-FIN-002's own example code
<!-- TC:TC-FIN-056:END -->

<!-- TC:TC-FIN-057:START traces=AC-FIN-005,REQ-FIN-005,SCR-FIN-002 -->
### TC-FIN-057 — a value is added under the dimension whose record page is open
Derived from : AC-FIN-005  (REQ-FIN-005)
Exercises    : SCR-FIN-002 `/fin/dimensions/:dimensionId` — the Value entry Drawer,
               `?valueAction=create`
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the dimension `PROJECT` exists and its record page is open; it holds no value with
               the code about to be used; the caller's role holds `PERM_FIN_DIMENSIONS_CREATE`
Steps        : 1. open the Value entry and add a value with code `NORTH`, both names and a sort
                  order
Expected     : the value appears among that dimension's values and under no other dimension —
               the values query is keyed by the record page's dimension id, so another
               dimension's record page shows a different set. The sort order is a required
               integer
Test data    : `NORTH` — the code AC-FIN-006 names; the sort order is a placeholder integer
<!-- TC:TC-FIN-057:END -->

<!-- TC:TC-FIN-058:START traces=AC-FIN-006,REQ-FIN-006,SCR-FIN-002 -->
### TC-FIN-058 — a duplicate value code is refused on the screen that entered it
Derived from : AC-FIN-006  (REQ-FIN-006)
Exercises    : SCR-FIN-002 `/fin/dimensions/:dimensionId` — the Value entry Drawer,
               `?valueAction=create`
Rule / code  : RULE-FIN-002 → FIN-409-DIMVALUE-DUP (409), shown inline on the code field
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: the dimension `PROJECT` already holds a value with code `NORTH`; its record page
               is open, so its values are loaded
Steps        : 1. add a second value with code `NORTH` under the same dimension
               2. submit past the client-side uniqueness pre-check
Expected     : step 1 → the client-side uniqueness pre-check against the loaded values marks the
               code field before any request is sent.
               step 2 → the server answers and the message is shown **inline on the code field** —
               ar: «هذا الرمز مستخدم بالفعل ضمن هذا البُعد» ·
               en: "This code is already used within this dimension".
               The detail list still shows exactly one `NORTH`
Test data    : `NORTH` and `PROJECT` — the AC's and the entity's own values
<!-- TC:TC-FIN-058:END -->

<!-- TC:TC-FIN-059:START traces=AC-FIN-007,REQ-FIN-007,SCR-FIN-003 -->
### TC-FIN-059 — an event-type rule is created on its `/new` page, header alone
Derived from : AC-FIN-007  (REQ-FIN-007)
Exercises    : SCR-FIN-003 `/fin/rules/new` → `/fin/rules/:ruleId`
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: `FIN_RULES` is in the caller's effective menu and the caller's role holds
               `PERM_FIN_RULES_CREATE`; the seven option lists the screen needs have loaded
               (UXD-FIN-007, UXD-FIN-008, UXD-FIN-009, UXD-FIN-002, UXD-FIN-010 and, since v2,
               the two of UXD-FIN-013 and UXD-FIN-014); the chosen event type has no rule yet
Steps        : 1. navigate to `/fin/rules/new` and observe the lines section
               2. create a rule with an event type and both names
Expected     : step 1 → the lines section is locked and says why: lines are added to a saved
                  rule.
               step 2 → the rule header is saved **alone**, in one call, and the page moves to
                  `/fin/rules/:ruleId` with the id from the create response; the rule reads
                  active and has no lines yet. Lines are added afterwards one per submit, never
                  chained behind the rule's save (ADR-FIN-062). The event-type select offers the
                  `ACCOUNTING_EVENT_TYPE` options MDL returns — which may legitimately be an
                  empty list, the type being host-defined and seeding no value
Test data    : the event type code is a placeholder
<!-- TC:TC-FIN-059:END -->

<!-- TC:TC-FIN-060:START traces=AC-FIN-008,REQ-FIN-008,SCR-FIN-003 -->
### TC-FIN-060 — a rule line captures its three references as three controls
Derived from : AC-FIN-008  (REQ-FIN-008)
Exercises    : SCR-FIN-003 `/fin/rules/:ruleId` — the Line entry Drawer, `?lineAction=create`
Rule / code  : — (happy path; RULE-FIN-019 is pre-empted on the form)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a rule's record page is open; the caller's role holds `PERM_FIN_RULES_UPDATE`,
               the permission adding a line needs
Steps        : 1. open the Line entry and choose derivation type CONSTANT
               2. pick the constant account in the account picker (`?picker=account`)
               3. set the amount-source type and value, the direction and the distribution type,
                  and save
Expected     : the line is added to the rule's lines by **one** call. The account derivation, the
               amount source and the direction are **three separate controls**, never fused. The
               picker is a sibling view offering active leaf accounts and returns the chosen
               account's code as the derivation value; the business-field control stays empty
               and not required on a CONSTANT line. The amount-source value is required unless
               the amount-source type is `REMAINDER`. There is no line-delete affordance — none
               is published (ADR-FIN-006)
Test data    : `CONSTANT`, `FIELD`, `FIXED`, `DEBIT`, `REMAINDER` — seeded values (SRS A6)
<!-- TC:TC-FIN-060:END -->

<!-- TC:TC-FIN-061:START traces=AC-FIN-009,REQ-FIN-009,SCR-FIN-003 -->
### TC-FIN-061 — the remainder-line count is enforced across the whole line set
Derived from : AC-FIN-009  (REQ-FIN-009)
Exercises    : SCR-FIN-003 `/fin/rules/:ruleId` — the Line entry Drawer, `?lineAction=create`
Rule / code  : RULE-FIN-003 → FIN-409-REMAINDER-COUNT (409), shown inline on the Line entry's
               remainder flag
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a rule's record page whose loaded lines already hold one `PERCENTAGE` line and no
               line marked as the remainder; the caller's role holds `PERM_FIN_RULES_UPDATE`
Steps        : 1. add a second `PERCENTAGE` line, not marked as the remainder, and save
               2. on a rule whose loaded lines already include one marked as the remainder, try
                  to add a further line also marked as the remainder
Expected     : step 1 → lines arrive one per submit (ADR-FIN-062), so "exactly one remainder
                  once any line is PERCENTAGE" is the server's check: its message is shown
                  inline on the remainder flag —
                  ar: «يلزم تحديد سطر باقٍ واحد بالضبط عند وجود توزيع نسبي» ·
                  en: "Exactly one remainder line is required when any percentage distribution is
                  present" —
                  and the line is not added.
               step 2 → the form's cross-field check, counting the loaded lines and this one,
                  refuses a second remainder marker before any request is sent.
               The rule stays unsaved in that shape until exactly one line is marked remainder,
               which is AC-FIN-009's own Then
Test data    : two percentage lines and zero remainder — AC-FIN-009's own counts
<!-- TC:TC-FIN-061:END -->

<!-- TC:TC-FIN-062:START traces=AC-FIN-014,REQ-FIN-014,SCR-FIN-006 -->
### TC-FIN-062 — a manual entry is composed and posted from its own route
Derived from : AC-FIN-014  (REQ-FIN-014)
Exercises    : SCR-FIN-006 `/fin/journal-entries/new`
Rule / code  : — (happy path; RULE-FIN-017's pairing is pre-empted by the year-filtered period
               select)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: `FIN_JOURNAL_ENTRIES` is in the caller's effective menu and the caller's role holds
               `PERM_FIN_JOURNAL_ENTRIES_CREATE`; the fiscal-year and fiscal-period option lists
               have loaded; at least two leaf active accounts and an Open period exist
Steps        : 1. navigate to `/fin/journal-entries/new`
               2. pick a fiscal year from the year select, then a period from the select
                  **narrowed to that year**
               3. open the lines Drawer (`?panel=lines`), add two balanced lines through its
                  account picker, return to the entry and post
Expected     : the entry is created in one call and the search list is invalidated so the new row
               appears. The year select lists the fiscal years the facade publishes, by year
               code, and says so when that list is capped at the option read's maximum rather
               than cutting it silently. The period options are narrowed to the chosen year —
               the client pre-empt of RULE-FIN-017, the server staying the authority. The lines
               summary row shows the line count and the running debit and credit totals. The
               journal type is a fixed, non-editable `MANUAL` and is not offered as a select
               (ADR-FIN-008); the document number is never an input and appears only on the
               created row
Test data    : `MANUAL` — a seeded `JOURNAL_TYPE` value; two lines — AC-FIN-014's own minimum
<!-- TC:TC-FIN-062:END -->

<!-- TC:TC-FIN-063:START traces=AC-FIN-015,REQ-FIN-015,SCR-FIN-006 -->
### TC-FIN-063 — both validation failures are shown and nothing is posted
Derived from : AC-FIN-015  (REQ-FIN-015)
Exercises    : SCR-FIN-006 `/fin/journal-entries/new`
Rule / code  : RULE-FIN-006 → FIN-409-UNBALANCED · RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a non-leaf (rollup) account exists; the year, period and document date are
               coherent so nothing else fails first; the caller's role holds
               `PERM_FIN_JOURNAL_ENTRIES_CREATE`
Steps        : 1. on `/fin/journal-entries/new`, submit an entry that is both unbalanced and
                  targets the rollup account on one line, bypassing the account picker, which
                  offers leaf accounts only
Expected     : **both** failures are surfaced together, not the first alone. The imbalance is
               shown inline on the header field it concerns —
               ar «القيد غير متوازن — إجمالي المدين لا يساوي إجمالي الدائن» ·
               en "The entry is unbalanced — total debits do not equal total credits".
               The account refusal is shown once, in the lines pane's refusal slot, bound to no
               line because the published error body names none (ADR-FIN-065) —
               ar «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
               en "The target account does not accept direct posting".
               The form keeps the accountant's input, and the search list gains no row: nothing
               was posted
Test data    : the two failing conditions are AC-FIN-015's own count
<!-- TC:TC-FIN-063:END -->

<!-- TC:TC-FIN-064:START traces=AC-FIN-016,REQ-FIN-016,SCR-FIN-006 -->
### TC-FIN-064 — a posted entry offers no delete, only a reverse
Derived from : AC-FIN-016  (REQ-FIN-016)
Exercises    : SCR-FIN-006 `/fin/journal-entries/:id`
Rule / code  : RULE-FIN-016 → no code: enforced by omission, and mirrored on the screen by the
               absence of the affordance
Scenario     : STATE · data class EDGE · language ALL
Preconditions: a POSTED entry; `FIN_JOURNAL_ENTRIES` is in the caller's effective menu — Reverse
               renders for every holder of the screen, the server checking
               `PERM_FIN_JOURNAL_ENTRIES_REVERSE` (ADR-FIN-065)
Steps        : 1. open `/fin/journal-entries/:id` for that entry
               2. enumerate the actions the row offers
Expected     : the row offers **Reverse and nothing that deletes or edits it**. No delete
               affordance exists anywhere on the screen for any row, in any permission
               combination — FIN publishes no delete endpoint, so drawing one would be an
               affordance with nothing behind it (ADR-FIN-006). The correction path a user is
               given is the reversal, which is AC-FIN-016's own Then
Test data    : none beyond the existing entry
<!-- TC:TC-FIN-064:END -->

<!-- TC:TC-FIN-065:START traces=AC-FIN-017,REQ-FIN-017,SCR-FIN-006 -->
### TC-FIN-065 — a posted entry is read-only and shows its posted state
Derived from : AC-FIN-017  (REQ-FIN-017)
Exercises    : SCR-FIN-006 `/fin/journal-entries/:id`
Rule / code  : RULE-FIN-016 → no code (the lock is a fact about the surface, not a message)
Scenario     : STATE · data class VALID · language ALL
Preconditions: an entry just posted through TC-FIN-062's flow
Steps        : 1. open the entry's read route
               2. attempt to change any header or line field
Expected     : the status reads POSTED and the posted-at moment is shown; the header and the lines
               are rendered **read-only**, with no editable control and no save affordance, since
               there is no update endpoint to receive one. The status label resolves through the
               `JOURNAL_STATUS` option list (UXD-FIN-006) and is displayed in the caller's
               language, the code itself never being shown raw
Test data    : none invented
<!-- TC:TC-FIN-065:END -->

<!-- TC:TC-FIN-066:START traces=AC-FIN-018,REQ-FIN-018,SCR-FIN-006 -->
### TC-FIN-066 — live totals warn, and the server refuses, an entry a hundredth out
Derived from : AC-FIN-018  (REQ-FIN-018)
Exercises    : SCR-FIN-006 `/fin/journal-entries/new`
Rule / code  : RULE-FIN-006 → FIN-409-UNBALANCED (409), shown inline
Scenario     : VIOLATION · data class BOUNDARY · language ALL
Preconditions: an entry form with every line on a leaf, active account and an Open period, so the
               imbalance is the only failing check
Steps        : 1. enter debit lines totalling 100.00 and credit lines totalling 99.99
               2. observe the live debit/credit totals the screen keeps while entering
               3. submit
Expected     : step 2 → the totals are shown side by side and do not match, the screen's own echo
               of RULE-FIN-006.
               step 3 → the server refuses and the message is shown inline —
               ar: «القيد غير متوازن — إجمالي المدين لا يساوي إجمالي الدائن» ·
               en: "The entry is unbalanced — total debits do not equal total credits" —
               with the imbalance amount. The client echo never substitutes for the server check:
               submitting with the echo bypassed still fails
Test data    : 100.00 and 99.99 — AC-FIN-018's own values
<!-- TC:TC-FIN-066:END -->

<!-- TC:TC-FIN-067:START traces=AC-FIN-019,REQ-FIN-019,SCR-FIN-006 -->
### TC-FIN-067 — a rollup account on a line is refused, in the lines pane
Derived from : AC-FIN-019  (REQ-FIN-019)
Exercises    : SCR-FIN-006 `/fin/journal-entries/new`
Rule / code  : RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT (409), shown in the lines pane's
               refusal slot
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a non-leaf (rollup) account exists; an Open period; the caller's role holds
               `PERM_FIN_JOURNAL_ENTRIES_CREATE`
Steps        : 1. in the lines Drawer, open the account picker and search for the rollup account
               2. submit a balanced entry in which exactly one line targets the rollup account,
                  bypassing the picker
Expected     : step 1 → the rollup account is not offered: the picker lists active leaf accounts
                  only.
               step 2 → the refusal is shown **once, in the lines pane's refusal slot** —
                  ar: «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
                  en: "The target account does not accept direct posting".
                  It is bound to no line: the published error body names none, and the screen
                  never guesses one. The "naming the offending line" half of AC-FIN-019 is
                  asserted on the backend track, by TC-FIN-019, as a known limitation of the
                  published contract (ADR-FIN-065, ADR-FIN-066). The lines keep their values
                  and nothing was posted
Test data    : account ids are placeholders; the rollup relation is the AC's own fact
<!-- TC:TC-FIN-067:END -->

<!-- TC:TC-FIN-068:START traces=AC-FIN-020,REQ-FIN-020,SCR-FIN-006 -->
### TC-FIN-068 — an entry composed against a period that has since closed is refused
Derived from : AC-FIN-020  (REQ-FIN-020)
Exercises    : SCR-FIN-006 `/fin/journal-entries/new`
Rule / code  : RULE-FIN-008 → FIN-409-PERIOD-NOT-OPEN (409), shown inline
Scenario     : VIOLATION · data class EDGE · language ALL
Preconditions: a period Open when the form is filled, and hard-closed on SCR-FIN-007 by an
               approver before the form is submitted
Steps        : 1. fill a balanced entry against that period and leave the form open
               2. in another session, soft-close then hard-close the period on its year's
                  record page, `/fin/periods/:fiscalYearId`
               3. submit the form
Expected     : the refusal names the period's **current** state —
               ar: «الفترة المستهدفة غير مفتوحة» · en: "The target period is not open" —
               even though the period was Open when the form was filled. A stale option list is
               not an authority: the check is the server's, at post time
Test data    : none invented
<!-- TC:TC-FIN-068:END -->

<!-- TC:TC-FIN-069:START traces=AC-FIN-021,REQ-FIN-021,SCR-FIN-006 -->
### TC-FIN-069 — a line citing an inactive dimension value is refused
Derived from : AC-FIN-021  (REQ-FIN-021)
Exercises    : SCR-FIN-006 `/fin/journal-entries/new`, with SCR-FIN-002 as setup
Rule / code  : RULE-FIN-009 → FIN-409-INVALID-DIMENSION (409), shown in the lines pane's refusal
               slot
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a dimension value deactivated on its dimension's record page — the only way the
               inactive state is reachable at all (ADR-FIN-009); the caller's role holds
               `PERM_FIN_DIMENSIONS_UPDATE` and `PERM_FIN_JOURNAL_ENTRIES_CREATE`
Steps        : 1. on `/fin/dimensions/:dimensionId` for `PROJECT`, deactivate one of its values
               2. on `/fin/journal-entries/new`, add a line citing that dimension value and submit
Expected     : step 1 → the value reads inactive. The deactivate row action renders for every
                  holder of the screen, and the parent dimension itself offers no deactivate —
                  none is published.
               step 2 → the refusal is shown once, in the lines pane's refusal slot —
                  ar: «قيمة البُعد غير صالحة» · en: "The dimension value is invalid" —
                  bound to no line and naming no dimension, because the published error body
                  carries neither. The "naming the offending line and dimension" half of
                  AC-FIN-021 is asserted on the backend track, by TC-FIN-021 (ADR-FIN-065,
                  ADR-FIN-066). Nothing is posted
Test data    : `PROJECT` and one of its values
<!-- TC:TC-FIN-069:END -->

<!-- TC:TC-FIN-070:START traces=AC-FIN-022,REQ-FIN-022,SCR-FIN-004 -->
### TC-FIN-070 — a template is created with its schedule and lines in one submit
Derived from : AC-FIN-022  (REQ-FIN-022)
Exercises    : SCR-FIN-004 `/fin/recurring-templates/new` → `/fin/recurring-templates/:templateId`
Rule / code  : — (happy path; the frequency conditional is pre-empted on the form)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: `FIN_RECURRING_TEMPLATES` is in the caller's effective menu and the caller's role
               holds `PERM_FIN_RECURRING_TEMPLATES_CREATE`; the schedule-type and frequency option
               lists have loaded (UXD-FIN-011, UXD-FIN-012)
Steps        : 1. on `/fin/recurring-templates/new`, choose schedule type `RECURRING` and observe
                  the frequency control
               2. choose `REVERSING` and observe it again
               3. return to `RECURRING`, give a frequency and a start date, compose at least two
                  balanced lines one at a time in the Line entry Drawer (`?lineAction=create`),
                  and submit
Expected     : the frequency is **required only when the schedule type is `RECURRING`** — the
               declared cross-field rule, which pre-empts FIN-400-MISSING-FREQUENCY — and is not
               required for `REVERSING`. The line set's debit = credit balance is checked before
               submit as a convenience, the server re-checking at run time. **One** call creates
               the template with its lines, and the page moves to the template's record page with
               the id from the create response, header and lines read-only. The record page
               offers Run and Deactivate and **no Edit affordance**: no update endpoint is
               published (ADR-FIN-006)
Test data    : `RECURRING`, `REVERSING`, `MONTHLY` — seeded values (SRS A6)
<!-- TC:TC-FIN-070:END -->

<!-- TC:TC-FIN-073:START traces=AC-FIN-025,REQ-FIN-025,SCR-FIN-005 -->
### TC-FIN-073 — an allocation rule is created with its targets
Derived from : AC-FIN-025  (REQ-FIN-025)
Exercises    : SCR-FIN-005 `/fin/allocation-rules/new` → `/fin/allocation-rules/:allocationRuleId`
Rule / code  : RULE-FIN-003 → pre-empted across the target set; FIN-409-REMAINDER-COUNT (409) if
               the pre-empt is bypassed
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: `FIN_ALLOCATION_RULES` is in the caller's effective menu and the caller's role
               holds `PERM_FIN_ALLOCATION_RULES_CREATE`; the `DISTRIBUTION_TYPE` option list has
               loaded (UXD-FIN-010)
Steps        : 1. on `/fin/allocation-rules/new`, give a source account and compose targets one
                  at a time in the Target entry Drawer (`?targetAction=create`)
               2. with two `PERCENTAGE` targets and none marked as the remainder, try to submit
               3. mark exactly one target as the remainder and submit
               4. submit the step 2 set with the form's check bypassed
Expected     : step 2 → the form's cross-field check refuses the set before any request: exactly
                  one target must carry the remainder mark when any target is `PERCENTAGE`, and
                  the whole set is known because it travels in the one create.
               step 3 → **one** call stores the rule with its targets, and the page moves to the
                  rule's record page with the targets read-only; there is no Edit affordance, no
                  update endpoint being published.
               step 4 → the server's refusal is shown inline —
                  ar: «يلزم تحديد سطر باقٍ واحد بالضبط عند وجود توزيع نسبي» ·
                  en: "Exactly one remainder line is required when any percentage distribution is
                  present".
               A target's distribution value is required unless its type is `REMAINDER`
Test data    : `PERCENTAGE`, `REMAINDER`, `FIXED` — seeded values
<!-- TC:TC-FIN-073:END -->

<!-- TC:TC-FIN-075:START traces=AC-FIN-027,REQ-FIN-027,SCR-FIN-006 -->
### TC-FIN-075 — filtering the journal list returns exactly the matching rows
Derived from : AC-FIN-027  (REQ-FIN-027)
Exercises    : SCR-FIN-006 `/fin/journal-entries`
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: entries exist across several statuses and periods; the caller holds
               `FIN_JOURNAL_ENTRIES` VIEW
Steps        : 1. navigate to `/fin/journal-entries`
               2. filter by period and by status
               3. page through the result
Expected     : exactly the matching entries are listed, unmodified, and the page and page size
               are part of the filter that keys the list — so paging re-queries the server rather
               than slicing a client-side array, and returning to a previous page does not
               re-order or re-shape the rows. The status and journal-type filters offer the
               `JOURNAL_STATUS` and `JOURNAL_TYPE` option lists (UXD-FIN-006, UXD-FIN-005)
Test data    : the filter pair is AC-FIN-027's own
<!-- TC:TC-FIN-075:END -->

<!-- TC:TC-FIN-076:START traces=AC-FIN-028,REQ-FIN-028,SCR-FIN-006 -->
### TC-FIN-076 — Reverse posts a mirrored entry and both rows point at each other
Derived from : AC-FIN-028  (REQ-FIN-028)
Exercises    : SCR-FIN-006 `/fin/journal-entries/:id`
Rule / code  : RULE-FIN-011 → success path, no code
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a POSTED entry with three lines; the caller's role holds the custom
               `PERM_FIN_JOURNAL_ENTRIES_REVERSE`, which is independent of CREATE
Steps        : 1. open the entry and invoke Reverse
               2. open the created reversal, then return to the original
Expected     : a new entry is posted with the same three line amounts and opposite directions, and
               each row shows the other's link. **The original still reads POSTED** — it is not
               voided — so both appear in the list and the pair nets to zero. The list is
               invalidated, so the reversal appears without a manual reload
Test data    : three lines — AC-FIN-028's own count
<!-- TC:TC-FIN-076:END -->

<!-- TC:TC-FIN-077:START traces=AC-FIN-029,REQ-FIN-029,SCR-FIN-006 -->
### TC-FIN-077 — reversing an entry of a closed period lands in the current open period
Derived from : AC-FIN-029  (REQ-FIN-029)
Exercises    : SCR-FIN-006 `/fin/journal-entries/:id`
Rule / code  : RULE-FIN-012 → success path, no code
Scenario     : STATE · data class EDGE · language ALL
Preconditions: a POSTED entry whose own period is now hard-closed, and a different period that is
               currently Open
Steps        : 1. open the entry and invoke Reverse
Expected     : the reversal is created and its period, shown on the created row, is the **current
               open** period rather than the original's closed one. The action is not blocked and
               no period-not-open message appears: the substitution is the rule's effect, not a
               refusal, and the user is never asked to choose the period
Test data    : none invented
<!-- TC:TC-FIN-077:END -->

<!-- TC:TC-FIN-078:START traces=AC-FIN-030,REQ-FIN-030,SCR-FIN-006 -->
### TC-FIN-078 — a second Reverse on the same entry is refused
Derived from : AC-FIN-030  (REQ-FIN-030)
Exercises    : SCR-FIN-006 `/fin/journal-entries/:id`
Rule / code  : RULE-FIN-013 → FIN-409-ALREADY-REVERSED (409), shown on the screen's generic
               surface (the plan routes reverse failures there, not to a field)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an entry already reversed once, still POSTED and carrying its reversal link
Steps        : 1. open that entry and invoke Reverse again
Expected     : the action is refused and the message is shown —
               ar: «تم عكس هذا القيد بالفعل ولا يمكن عكسه مرة أخرى» ·
               en: "This entry has already been reversed and cannot be reversed again".
               No second mirror appears in the list. The message is shown generically rather than
               bound to a field, there being no field to bind it to
Test data    : none invented
<!-- TC:TC-FIN-078:END -->

<!-- TC:TC-FIN-079:START traces=AC-FIN-031,REQ-FIN-031,SCR-FIN-007 -->
### TC-FIN-079 — creating a year opens its record page with its periods, each Open
Derived from : AC-FIN-031  (REQ-FIN-031)
Exercises    : SCR-FIN-007 `/fin/periods/new` → `/fin/periods/:fiscalYearId`
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: `FIN_PERIODS` is in the caller's effective menu and the caller's role holds
               `PERM_FIN_PERIODS_CREATE`; the `PERIOD_STATE` and `FISCAL_YEAR_STATUS` option lists
               have loaded (UXD-FIN-003, UXD-FIN-004)
Steps        : 1. navigate to `/fin/periods/new`
               2. create a year with a code, a start date, an end date after it, and a period
                  count
               3. return to `/fin/periods`
Expected     : step 2 → one call creates the year and the page moves to
                  `/fin/periods/:fiscalYearId` with the id from the create response: the year
                  header in a collapsible section, its generated periods beneath it, **every
                  period reading OPEN** — AC-FIN-031's own Then. The end date must be later than
                  the start date and the period count must be a positive integer, both checked on
                  the form.
               step 3 → the year is listed. The list and the record page's header come from the
                  published fiscal-year search, cited by path (ADR-FIN-061); the periods come
                  from API-FIN-033 scoped to the year. The creation invalidates both, so no
                  reload is needed
Test data    : `2026` and a twelve-period year; the dates are placeholders
<!-- TC:TC-FIN-079:END -->

<!-- TC:TC-FIN-080:START traces=AC-FIN-032,REQ-FIN-032,SCR-FIN-007 -->
### TC-FIN-080 — a soft-closed period is reopened from its row
Derived from : AC-FIN-032  (REQ-FIN-032)
Exercises    : SCR-FIN-007 `/fin/periods/:fiscalYearId`
Rule / code  : — (happy path)
Scenario     : STATE · data class VALID · language ALL
Preconditions: a period row reading SOFT_CLOSE on its year's record page; the caller's role holds
               `PERM_FIN_PERIODS_UPDATE`
Steps        : 1. invoke Open on that row
Expected     : the row's state becomes OPEN and the period list is invalidated so the change is
               visible at once. The row action takes no input beyond the row itself — the
               affordance is the whole form
Test data    : none invented
<!-- TC:TC-FIN-080:END -->

<!-- TC:TC-FIN-081:START traces=AC-FIN-033,REQ-FIN-033,SCR-FIN-007 -->
### TC-FIN-081 — soft-closing a period is visible on its row
Derived from : AC-FIN-033  (REQ-FIN-033)
Exercises    : SCR-FIN-007 `/fin/periods/:fiscalYearId`
Rule / code  : FIN-409-INVALID-TRANSITION (409) when the row is not Open
Scenario     : STATE · data class VALID · language ALL
Preconditions: a period row reading OPEN on its year's record page; the caller's role holds
               `PERM_FIN_PERIODS_UPDATE`
Steps        : 1. invoke Soft-close on that row
               2. invoke Soft-close again on the now soft-closed row
Expected     : step 1 → the row reads SOFT_CLOSE.
               step 2 → the server refuses the second transition —
                  ar: «لا يمكن تنفيذ هذا الانتقال من الحالة الحالية» ·
                  en: "This transition is not allowed from the current status" —
                  shown on the screen's generic surface. Posting into the period is refused from
                  SCR-FIN-006 thereafter, which is AC-FIN-033's own consequence
Test data    : none invented
<!-- TC:TC-FIN-081:END -->

<!-- TC:TC-FIN-082:START traces=AC-FIN-034,REQ-FIN-034,SCR-FIN-007 -->
### TC-FIN-082 — hard-close is the approver's action and is permanent
Derived from : AC-FIN-034  (REQ-FIN-034)
Exercises    : SCR-FIN-007 `/fin/periods/:fiscalYearId`
Rule / code  : RULE-FIN-015 (the server's gate) · RULE-FIN-014 (the terminal state)
Scenario     : STATE · data class VALID · language ALL
Preconditions: a period row reading SOFT_CLOSE on its year's record page; the caller's role holds
               `PERM_FIN_PERIODS_CLOSE_APPROVE`
Steps        : 1. invoke Hard-close on that row and confirm
               2. observe the row's actions afterwards
Expected     : step 1 → the row reads HARD_CLOSE and shows who closed it and when.
               step 2 → the transitions still render, as every affordance does for a holder of
                  the screen whatever the period's state, but the state is terminal: invoking
                  Open is refused by the server (TC-FIN-083). No affordance is rendered from a
                  close-approve flag or any other grant read on the client; the server alone
                  enforces RULE-FIN-015 (ADR-FIN-065)
Test data    : none invented
<!-- TC:TC-FIN-082:END -->

<!-- TC:TC-FIN-083:START traces=AC-FIN-035,REQ-FIN-035,SCR-FIN-007 -->
### TC-FIN-083 — reopening a hard-closed period is refused on the screen
Derived from : AC-FIN-035  (REQ-FIN-035)
Exercises    : SCR-FIN-007 `/fin/periods/:fiscalYearId`
Rule / code  : RULE-FIN-014 → FIN-409-NOT-REOPENABLE (409), shown on the generic surface
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a period row reading HARD_CLOSE on its year's record page; the caller's role holds
               `PERM_FIN_PERIODS_UPDATE`
Steps        : 1. invoke Open on that row
Expected     : refused, with —
               ar: «الفترة مغلقة إغلاقًا صارمًا ولا يمكن إعادة فتحها» ·
               en: "The period is hard-closed and cannot be reopened".
               The row's state is unchanged. The Open action is neither hidden nor disabled on a
               hard-closed row: the server is the authority, and this message is what a user
               sees (ADR-FIN-065)
Test data    : none invented
<!-- TC:TC-FIN-083:END -->

<!-- TC:TC-FIN-084:START traces=AC-FIN-036,REQ-FIN-036,SCR-FIN-007 -->
### TC-FIN-084 — year-end close is refused until every period is hard-closed, then runs
Derived from : AC-FIN-036  (REQ-FIN-036)
Exercises    : SCR-FIN-007 `/fin/periods/:fiscalYearId`
Rule / code  : FIN-409-PERIODS-NOT-CLOSED (409), shown inline, when it is invoked too early
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a year's record page is open; the caller's role holds
               `PERM_FIN_PERIODS_CLOSE_APPROVE`
Steps        : 1. with at least one period not HARD_CLOSE, observe Run-year-end-close, then
                  invoke it
               2. hard-close the remaining periods, then invoke it again
Expected     : step 1 → the action renders enabled, with a readiness hint while not every period
                  reads HARD_CLOSE; the server decides, refusing with —
                  ar: «يجب إغلاق كل الفترات إغلاقًا صارمًا أولًا» ·
                  en: "Every period must be hard-closed first" —
                  shown inline, and nothing is posted (ADR-FIN-065).
               step 2 → the close runs and posts the closing and opening entries, so the journal
                  list is invalidated too and both new entries are visible on SCR-FIN-006
Test data    : none invented
<!-- TC:TC-FIN-084:END -->

<!-- TC:TC-FIN-085:START traces=AC-FIN-037,REQ-FIN-037,SCR-FIN-007 -->
### TC-FIN-085 — the approving principal and moment are shown on the closed row
Derived from : AC-FIN-037  (REQ-FIN-037)
Exercises    : SCR-FIN-007 `/fin/periods/:fiscalYearId`
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a period just hard-closed by an approver, containing entries created by a
               different principal
Steps        : 1. read the closed period's row
Expected     : the row shows **who** closed it and **when**, distinct from the creator recorded on
               any entry of that period. The approval is legible on the screen as an act of its
               own rather than inferred from an entry's audit data — AC-FIN-037's own point
Test data    : principals are placeholders
<!-- TC:TC-FIN-085:END -->

<!-- TC:TC-FIN-086:START traces=AC-FIN-038,REQ-FIN-038,SCR-FIN-007 -->
### TC-FIN-086 — an entry creator's hard-close is refused by the server
Derived from : AC-FIN-038  (REQ-FIN-038)
Exercises    : SCR-FIN-007 `/fin/periods/:fiscalYearId`
Rule / code  : RULE-FIN-015 → FIN-403-FORBIDDEN (403), shown as the catalogue message on the screen
Scenario     : PERMISSION · data class ATTACK · language ALL
Preconditions: a user whose role holds the journal-entry-creation permission and **not** the
               close-approve one; `FIN_PERIODS` is in the effective menu with VIEW, so the screen
               is reachable; a period row reads SOFT_CLOSE
Steps        : 1. open the year's record page and inspect the period row's actions
               2. invoke Hard-close on that row
Expected     : step 1 → Hard-close and Run-year-end-close render like every other action: no
                  endpoint publishes the close-approve grant to the client, so the screen cannot
                  hide them and does not (ADR-FIN-065).
               step 2 → the server denies the call, and its message is shown on the screen in
                  the caller's language —
                  ar: «لا تملك صلاحية المالية المطلوبة لهذه العملية» ·
                  en: "You do not hold the Finance permission required for this operation" —
                  never as an empty result. The row still reads SOFT_CLOSE. The separation of
                  duty is **server-enforced**; the client runs no separation-of-duty check and no
                  permission check of its own
Test data    : the two permission codes are the registry's own
<!-- TC:TC-FIN-086:END -->

<!-- TC:TC-FIN-087:START traces=AC-FIN-039,REQ-FIN-039,SCR-FIN-008 -->
### TC-FIN-087 — the account ledger screen shows a live running balance
Derived from : AC-FIN-039  (REQ-FIN-039)
Exercises    : SCR-FIN-008 `/fin/reports/account-ledger`
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an account with posted lines across two periods; the caller holds
               `FIN_ACCOUNT_LEDGER` in the effective menu
Steps        : 1. navigate to `/fin/reports/account-ledger`
               2. set the account and a date range covering both periods
               3. copy the resulting URL, open it fresh, and compare
Expected     : the rows carry a running balance computed at request time, each linking to its
               entry. The filter state is mirrored into the route's search parameters, so step 3
               reproduces the same filtered report from the URL alone — the screen is a full page
               with no entry sub-view (ADR-FIN-003). The account filter is required; a date range
               and a dimension pair are optional, the dimension value requiring its dimension
Test data    : two periods — AC-FIN-039's own; ids are placeholders
<!-- TC:TC-FIN-087:END -->

<!-- TC:TC-FIN-088:START traces=AC-FIN-040,REQ-FIN-040,SCR-FIN-009 -->
### TC-FIN-088 — the trial balance screen shows equal debit and credit totals
Derived from : AC-FIN-040  (REQ-FIN-040)
Exercises    : SCR-FIN-009 `/fin/reports/trial-balance`
Rule / code  : — (read-only; the equality is an invariant by construction)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: posted entries exist in a period; the caller holds `FIN_TRIAL_BALANCE`
Steps        : 1. navigate to `/fin/reports/trial-balance`
               2. run it with a period, then with the period filter cleared
Expected     : one row per account, and the **total debit balances equal the total credit
               balances** in both runs. Both filters are optional, so clearing the period is a
               legitimate request and not an error state. Account names are shown in the caller's
               language, and the account-type filter offers the `ACCOUNT_TYPE` option list
               (UXD-FIN-001)
Test data    : none invented
<!-- TC:TC-FIN-088:END -->

<!-- TC:TC-FIN-089:START traces=AC-FIN-041,REQ-FIN-041,SCR-FIN-010 -->
### TC-FIN-089 — the balance sheet carries the prior year's closing balances forward
Derived from : AC-FIN-041  (REQ-FIN-041)
Exercises    : SCR-FIN-010 `/fin/reports/balance-sheet`
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a completed year-end close; the caller holds `FIN_BALANCE_SHEET`
Steps        : 1. run the report for the closed year
               2. run it for the new year and compare, account by account
Expected     : the new year's opening balances equal the prior year's closing balances for every
               balance-sheet account, the figures grouped into asset, liability and equity
               sections with their totals. The fiscal year is a **required** filter, so the screen
               does not issue a request until one is chosen, and an as-of date narrows it
               optionally
Test data    : none invented
<!-- TC:TC-FIN-089:END -->

<!-- TC:TC-FIN-090:START traces=AC-FIN-042,REQ-FIN-042,SCR-FIN-011 -->
### TC-FIN-090 — the income statement opens the new year at zero
Derived from : AC-FIN-042  (REQ-FIN-042)
Exercises    : SCR-FIN-011 `/fin/reports/income-statement`
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a completed year-end close and **no posting yet** in the new year; the caller holds
               `FIN_INCOME_STATEMENT`
Steps        : 1. run the report for the new fiscal year, before any posting
Expected     : every revenue and expense account shows a zero balance, grouped into revenue and
               expense sections with their totals — while TC-FIN-089's balance sheet, run for the
               same year, carries its brought-forward balances. The fiscal year is required. The
               two period bounds are each optional on their own: one set without the other is a
               legal open-ended range, sent as it stands and never coupled to the other
               (ADR-FIN-065)
Test data    : none invented
<!-- TC:TC-FIN-090:END -->

<!-- TC:TC-FIN-091:START traces=AC-FIN-043,REQ-FIN-043,SCR-FIN-012 -->
### TC-FIN-091 — the dimension report shows one row per account and dimension value
Derived from : AC-FIN-043  (REQ-FIN-043)
Exercises    : SCR-FIN-012 `/fin/reports/dimension`
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: postings on one account under two different `PROJECT` dimension values; the caller
               holds `FIN_DIMENSION_REPORTS`
Steps        : 1. navigate to `/fin/reports/dimension` and run it by the `PROJECT` dimension
Expected     : **two separate rows** for that account, one per project value, never one combined
               row — AC-FIN-043's own Then. The dimension filter is required; a dimension value
               and a period narrow it optionally. Filter state is mirrored into the route's search
               parameters, so the run is linkable
Test data    : `PROJECT` and two of its values — AC-FIN-043's own shape
<!-- TC:TC-FIN-091:END -->
<!-- SUB:UI-FLOWS:END -->

<!-- SUB:INT-FLOW:START traces=REQ-FIN-003,REQ-FIN-023,REQ-FIN-024,REQ-FIN-026,REQ-FIN-046,REQ-FIN-058,AC-FIN-003,AC-FIN-023,AC-FIN-024,AC-FIN-026,AC-FIN-046,AC-FIN-062 -->
### SUB — INT-FLOW

The flows that do not fit on one screen: a definition retired here changes what is accepted
there, a run here posts an entry that is read there, and a report drills into the entry behind it.

The v2 case opens the SUB (TC-FIN-136) and the v1 cases follow unchanged.

<!-- TC:TC-FIN-136:START traces=AC-FIN-062,REQ-FIN-058,SCR-FIN-013,SCR-FIN-006,SCR-FIN-008 -->
### TC-FIN-136 — changing a mapping's account leaves posted entries as they were
Derived from : AC-FIN-062  (REQ-FIN-058)
Exercises    : SCR-FIN-013 `/fin/account-mappings` → SCR-FIN-006 `/fin/journal-entries/:id` →
               SCR-FIN-008 `/fin/reports/account-ledger`
Rule / code  : — (no rule fires; a posted line stores its account)
Scenario     : STATE · data class VALID · language ALL
Preconditions: entry `JV-2026-000500`, posted from event `EVT-3010` with its debit line on
               account 5220 through the mapping (EXPENSE_PAID, EXPENSE_TYPE_CODE, "RENT");
               account 5225 is an active leaf; the caller's menu carries the three page codes,
               and the caller's role holds `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE`
Steps        : 1. on `/fin/account-mappings`, change the RENT mapping's account to 5225
               2. open `JV-2026-000500` on `/fin/journal-entries/:id`
               3. open the account ledger for 5220 over the entry's date
Expected     : step 1 → the mapping row shows 5225. The change invalidates the mapping list and
                  nothing else — no journal cache is touched, because a mapping change never
                  alters a posted entry.
               step 2 → the debit line still shows account 5220.
               step 3 → the ledger of 5220 still lists that line
Test data    : `JV-2026-000500`, `EVT-3010`, 5220, 5225 and "RENT" — AC-FIN-062's own values
<!-- TC:TC-FIN-136:END -->

<!-- TC:TC-FIN-055:START traces=AC-FIN-003,REQ-FIN-003,SCR-FIN-001,SCR-FIN-006 -->
### TC-FIN-055 — deactivating an account on one screen closes it on another
Derived from : AC-FIN-003  (REQ-FIN-003)
Exercises    : SCR-FIN-001 `/fin/accounts` → SCR-FIN-006 `/fin/journal-entries/new`
Rule / code  : RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT (409), on the second screen, shown in
               the lines pane's refusal slot
Scenario     : STATE · data class VALID · language ALL
Preconditions: an active leaf account used on entries today; an Open period; the caller's role
               holds `PERM_FIN_ACCOUNTS_UPDATE` and `PERM_FIN_JOURNAL_ENTRIES_CREATE`
Steps        : 1. on `/fin/accounts`, deactivate the account, confirming the usage prompt the
                  screen raises before the call
               2. filter the chart list by active only, and look for the account
               3. on `/fin/journal-entries/new`, open the lines Drawer's account picker and
                  search for the account
               4. submit a balanced entry with a line on it, bypassing the picker
Expected     : step 1 → the row is deactivated; the confirmation is a prompt, not a rule check —
                  the parent-child case is the server's to refuse and is never pre-empted here.
               step 2 → the account is gone from the active results.
               step 3 → the account is not offered: the picker lists active leaf accounts only.
               step 4 → the post is refused, and the message is shown once, in the lines pane's
                  refusal slot —
                  ar: «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
                  en: "The target account does not accept direct posting".
                  The published error body names no line, so the screen binds the message to
                  none (ADR-FIN-065).
               This is the full lifecycle AC-FIN-003 describes: create, search, deactivate, gone
               from active results, and rejected by REQ-FIN-019 thereafter
Test data    : the account is a placeholder
<!-- TC:TC-FIN-055:END -->

<!-- TC:TC-FIN-071:START traces=AC-FIN-023,REQ-FIN-023,SCR-FIN-004,SCR-FIN-006 -->
### TC-FIN-071 — running a template posts one entry, visible on the journal screen
Derived from : AC-FIN-023  (REQ-FIN-023)
Exercises    : SCR-FIN-004 `/fin/recurring-templates/:templateId` → SCR-FIN-006
               `/fin/journal-entries`
Rule / code  : RULE-FIN-006 … RULE-FIN-009 re-checked at run time, surfaced generically
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an **active** recurring template whose next run date is today; an Open period; the
               caller's role holds `PERM_FIN_RECURRING_TEMPLATES_UPDATE`, the permission Run needs
Steps        : 1. on the template's record page, invoke Run and confirm
               2. navigate to `/fin/journal-entries` and filter for the new row
Expected     : step 1 → exactly **one** entry is posted and the template's next run date advances
                  per its frequency. The run is a global-loading action, not a local one: it posts
                  a real entry, and both the template list and the journal list are invalidated.
               step 2 → the entry is listed with journal type `RECURRING` and status POSTED.
               Run-time failures (an account since deactivated, a period since closed) surface on
               the generic error surface of the template screen, the entry being server-built with
               no form field to bind a message to
Test data    : none invented; "today" is AC-FIN-023's own
<!-- TC:TC-FIN-071:END -->

<!-- TC:TC-FIN-072:START traces=AC-FIN-024,REQ-FIN-024,SCR-FIN-004,SCR-FIN-006 -->
### TC-FIN-072 — a reversing template's pair is visible across the two periods
Derived from : AC-FIN-024  (REQ-FIN-024)
Exercises    : SCR-FIN-004 `/fin/recurring-templates/:templateId` → SCR-FIN-006
               `/fin/journal-entries`
Rule / code  : RULE-FIN-011, RULE-FIN-012 → success path, no code
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an active template whose schedule type is `REVERSING`; period N Open and period
               N+1 existing in the same year
Steps        : 1. on the template's record page, invoke Run and confirm
               2. on `/fin/journal-entries`, filter by period N, then by period N+1
Expected     : period N shows the template's own entry and period N+1 shows its **linked
               reversal**, equal amounts with opposite directions, each row showing the other's
               link. Opening either from the list reaches the same pair, so the relationship is
               navigable from the screen and not only implied by the amounts
Test data    : `REVERSING` — a seeded value; "period N / N+1" is AC-FIN-024's own wording
<!-- TC:TC-FIN-072:END -->

<!-- TC:TC-FIN-074:START traces=AC-FIN-026,REQ-FIN-026,SCR-FIN-005,SCR-FIN-006 -->
### TC-FIN-074 — an allocation run posts one entry that sums to the source balance
Derived from : AC-FIN-026  (REQ-FIN-026)
Exercises    : SCR-FIN-005 `/fin/allocation-rules/:allocationRuleId` → SCR-FIN-006
               `/fin/journal-entries`
Rule / code  : RULE-FIN-010 → success path, no code; RULE-FIN-003 governs the target set
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an active allocation rule with two `PERCENTAGE` targets and one `REMAINDER` target,
               its source account balance 1,000.00; an Open period; the caller's role holds
               `PERM_FIN_ALLOCATION_RULES_UPDATE`, the permission Run needs
Steps        : 1. on the rule's record page, invoke Run and confirm
               2. on `/fin/journal-entries`, open the created entry
Expected     : step 1 → the run is a global-loading action and invalidates both the rule list and
                  the journal list.
               step 2 → one entry with journal type `ALLOCATION` and status POSTED, whose target
                  lines sum **exactly** to 1,000.00, the remainder target carrying the rounding
                  difference. The screen displays the amounts as returned and computes no
                  distribution of its own
Test data    : 1,000.00 and the two-percentage-plus-one-remainder shape — AC-FIN-026's own
<!-- TC:TC-FIN-074:END -->

<!-- TC:TC-FIN-092:START traces=AC-FIN-046,REQ-FIN-046,SCR-FIN-010,SCR-FIN-009,SCR-FIN-008,SCR-FIN-006 -->
### TC-FIN-092 — a statement line is followed down to the entry behind it
Derived from : AC-FIN-046  (REQ-FIN-046)
Exercises    : SCR-FIN-010 `/fin/reports/balance-sheet` → SCR-FIN-009
               `/fin/reports/trial-balance` → SCR-FIN-008 `/fin/reports/account-ledger` →
               SCR-FIN-006 `/fin/journal-entries/:id`
Rule / code  : — (read-only chain)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a balance-sheet line whose balance comes from an event-sourced entry; the caller's
               effective menu carries all four page codes
Steps        : 1. open the balance sheet and drill down from the group that holds the line
               2. on the trial balance reached, find the line's account row and drill into it
               3. from the ledger row reached, open the originating entry
Expected     : step 1 → the trial balance opens filtered by the group's account type, carried as
                  `accountTypeCode` in its route search parameters — the one filter the trial
                  balance accepts that the statement holds; no account and no period is carried
                  (ADR-FIN-065).
               step 2 → the account ledger opens already filtered by that account.
               step 3 → the entry shows its lines and its event reference, or reads `manual`,
                  `recurring` or `allocation` where the entry is not event-sourced.
               Every hop's filter lives in the destination's route search parameters, so every
               intermediate step is linkable and back navigation returns to the filtered view
               rather than a blank report. A user whose menu lacks one of the four page codes is
               redirected to unauthorized at that hop rather than shown an empty screen
Test data    : none invented; the chain is AC-FIN-046's own
<!-- TC:TC-FIN-092:END -->
<!-- SUB:INT-FLOW:END -->
<!-- PHASE:TEST-PLAN-FE:END -->

## TC TRACEABILITY INDEX

**AC → TC** (in-track criteria only; the 27 others are in the header table)

| AC | TC | AC | TC |
|---|---|---|---|
| AC-FIN-001 | TC-FIN-053 | AC-FIN-033 | TC-FIN-081 |
| AC-FIN-002 | TC-FIN-054 | AC-FIN-034 | TC-FIN-082 |
| AC-FIN-003 | TC-FIN-055 | AC-FIN-035 | TC-FIN-083 |
| AC-FIN-004 | TC-FIN-056 | AC-FIN-036 | TC-FIN-084 |
| AC-FIN-005 | TC-FIN-057 | AC-FIN-037 | TC-FIN-085 |
| AC-FIN-006 | TC-FIN-058 | AC-FIN-038 | TC-FIN-086 |
| AC-FIN-007 | TC-FIN-059 | AC-FIN-039 | TC-FIN-087 |
| AC-FIN-008 | TC-FIN-060 | AC-FIN-040 | TC-FIN-088 |
| AC-FIN-009 | TC-FIN-061 | AC-FIN-041 | TC-FIN-089 |
| AC-FIN-014 | TC-FIN-062 | AC-FIN-042 | TC-FIN-090 |
| AC-FIN-015 | TC-FIN-063 | AC-FIN-043 | TC-FIN-091 |
| AC-FIN-016 | TC-FIN-064 | AC-FIN-046 | TC-FIN-092 |
| AC-FIN-017 | TC-FIN-065 | AC-FIN-048 | TC-FIN-130 |
| AC-FIN-018 | TC-FIN-066 | AC-FIN-049 | TC-FIN-131 |
| AC-FIN-019 | TC-FIN-067 | AC-FIN-058 | TC-FIN-132 |
| AC-FIN-020 | TC-FIN-068 | AC-FIN-059 | TC-FIN-133 |
| AC-FIN-021 | TC-FIN-069 | AC-FIN-060 | TC-FIN-134 |
| AC-FIN-022 | TC-FIN-070 | AC-FIN-061 | TC-FIN-135 |
| AC-FIN-023 | TC-FIN-071 | AC-FIN-062 | TC-FIN-136 |
| AC-FIN-024 | TC-FIN-072 | AC-FIN-063 | TC-FIN-137 |
| AC-FIN-025 | TC-FIN-073 | AC-FIN-064 | TC-FIN-138 |
| AC-FIN-026 | TC-FIN-074 | AC-FIN-065 | TC-FIN-139 |
| AC-FIN-027 | TC-FIN-075 | AC-FIN-066 | TC-FIN-140 |
| AC-FIN-028 | TC-FIN-076 | AC-FIN-069 | TC-FIN-141 |
| AC-FIN-029 | TC-FIN-077 | AC-FIN-070 | TC-FIN-142 |
| AC-FIN-030 | TC-FIN-078 | AC-FIN-075 | TC-FIN-143 |
| AC-FIN-031 | TC-FIN-079 | AC-FIN-076 | TC-FIN-144 |
| AC-FIN-032 | TC-FIN-080 | AC-FIN-083 | TC-FIN-145 |

**REQ → TC** — for the v1 requirements each `REQ-FIN-<n>` carries the TC of its own `AC-FIN-<n>`,
plus the extensions in the first table. The v2 in-track requirements are listed in full in the
second.

| REQ | TC |
|---|---|
| REQ-FIN-017 | TC-FIN-065 (the locked, posted surface) · TC-FIN-062, TC-FIN-071, TC-FIN-074 (the same unified path seen from three screens) |
| REQ-FIN-019 | TC-FIN-067 · TC-FIN-055 (the same refusal reached through the chart screen) |
| REQ-FIN-021 | TC-FIN-069 (which needs SCR-FIN-002's deactivate as its only setup) |
| REQ-FIN-036 | TC-FIN-084 · TC-FIN-089, TC-FIN-090 (the two reports that read its result) |
| REQ-FIN-046 | TC-FIN-092 (the chain) · TC-FIN-087, TC-FIN-088, TC-FIN-089 (each hop's own screen) |

| REQ (v2) | AC | TC |
|---|---|---|
| REQ-FIN-048 | AC-FIN-048 | TC-FIN-130 |
| REQ-FIN-049 | AC-FIN-049 | TC-FIN-131 |
| REQ-FIN-054 | AC-FIN-058 | TC-FIN-132 |
| REQ-FIN-055 | AC-FIN-059 | TC-FIN-133 |
| REQ-FIN-056 | AC-FIN-060 | TC-FIN-134 |
| REQ-FIN-057 | AC-FIN-061 | TC-FIN-135 |
| REQ-FIN-058 | AC-FIN-062 | TC-FIN-136 |
| REQ-FIN-059 | AC-FIN-063 | TC-FIN-137 |
| REQ-FIN-060 | AC-FIN-064 | TC-FIN-138 |
| REQ-FIN-061 | AC-FIN-065 | TC-FIN-139 |
| REQ-FIN-062 | AC-FIN-066 | TC-FIN-140 |
| REQ-FIN-064 | AC-FIN-069 | TC-FIN-141 |
| REQ-FIN-065 | AC-FIN-070 | TC-FIN-142 |
| REQ-FIN-070 | AC-FIN-075 | TC-FIN-143 |
| REQ-FIN-071 | AC-FIN-076 | TC-FIN-144 |
| REQ-FIN-077 | AC-FIN-083 | TC-FIN-145 |

**SCR → TC**

| SCR | TC |
|---|---|
| SCR-FIN-001 | TC-FIN-053, TC-FIN-054, TC-FIN-055 |
| SCR-FIN-002 | TC-FIN-056, TC-FIN-057, TC-FIN-058, TC-FIN-069 (setup) |
| SCR-FIN-003 | TC-FIN-059, TC-FIN-060, TC-FIN-061 · v2: TC-FIN-130, TC-FIN-131, TC-FIN-140, TC-FIN-141, TC-FIN-142 |
| SCR-FIN-004 | TC-FIN-070, TC-FIN-071, TC-FIN-072 |
| SCR-FIN-005 | TC-FIN-073, TC-FIN-074 |
| SCR-FIN-006 | TC-FIN-062, TC-FIN-063, TC-FIN-064, TC-FIN-065, TC-FIN-066, TC-FIN-067, TC-FIN-068, TC-FIN-069, TC-FIN-075, TC-FIN-076, TC-FIN-077, TC-FIN-078, TC-FIN-055, TC-FIN-071, TC-FIN-072, TC-FIN-074, TC-FIN-092 · v2: TC-FIN-136, TC-FIN-143, TC-FIN-145 |
| SCR-FIN-007 | TC-FIN-079, TC-FIN-080, TC-FIN-081, TC-FIN-082, TC-FIN-083, TC-FIN-084, TC-FIN-085, TC-FIN-086, TC-FIN-068 (setup) |
| SCR-FIN-008 | TC-FIN-087, TC-FIN-092 · v2: TC-FIN-136 |
| SCR-FIN-009 | TC-FIN-088, TC-FIN-092 |
| SCR-FIN-010 | TC-FIN-089, TC-FIN-092 |
| SCR-FIN-011 | TC-FIN-090 |
| SCR-FIN-012 | TC-FIN-091 · v2: TC-FIN-144 |
| SCR-FIN-013 | TC-FIN-132, TC-FIN-133, TC-FIN-134, TC-FIN-135, TC-FIN-136, TC-FIN-137, TC-FIN-138, TC-FIN-139 (v2) |

**RULE / catalog code → TC**

| RULE | code | TC |
|---|---|---|
| RULE-FIN-001 | FIN-409-HAS-CHILDREN (409) | TC-FIN-054 |
| RULE-FIN-002 | FIN-409-DIMVALUE-DUP (409) | TC-FIN-058 |
| RULE-FIN-003 | FIN-409-REMAINDER-COUNT (409) | TC-FIN-061, TC-FIN-073 |
| RULE-FIN-006 | FIN-409-UNBALANCED (409) | TC-FIN-063, TC-FIN-066 |
| RULE-FIN-007 | FIN-409-NOT-POSTABLE-ACCOUNT (409) | TC-FIN-055, TC-FIN-063, TC-FIN-067 |
| RULE-FIN-008 | FIN-409-PERIOD-NOT-OPEN (409) | TC-FIN-068 |
| RULE-FIN-009 | FIN-409-INVALID-DIMENSION (409) | TC-FIN-069 |
| RULE-FIN-010 | — (success path) | TC-FIN-074 |
| RULE-FIN-011, RULE-FIN-012 | — (success path) | TC-FIN-072, TC-FIN-076, TC-FIN-077 · v2: TC-FIN-145 |
| RULE-FIN-013 | FIN-409-ALREADY-REVERSED (409) | TC-FIN-078 |
| RULE-FIN-014 | FIN-409-NOT-REOPENABLE (409) | TC-FIN-083 |
| RULE-FIN-015 | FIN-403-FORBIDDEN (403) | TC-FIN-086 |
| RULE-FIN-016 | — (enforced by omission; on screen, by the absent affordance) | TC-FIN-064, TC-FIN-065 |
| RULE-FIN-017 | — (pre-empted by the year-filtered period select; the 400s are the server's) | TC-FIN-062 |
| RULE-FIN-018 | FIN-422-UNSUPPORTED-DERIVATION (422) | TC-FIN-130 |
| RULE-FIN-019 | — (pre-empted on the Line entry; FIN-422-DERIVATION-SPEC is the server's) | TC-FIN-131, TC-FIN-060 |
| RULE-FIN-022 | FIN-409-MAPPING-DUP (409) — not pre-empted | TC-FIN-137 |
| RULE-FIN-023 | FIN-422-MAPPED-ACCOUNT-INELIGIBLE (422) | TC-FIN-138 · TC-FIN-132, TC-FIN-134 (the picker pre-empt) |
| RULE-FIN-024 | FIN-422-INVALID-PAYMENT-METHOD (422) | TC-FIN-139 · TC-FIN-132 (the select pre-empt) |
| RULE-FIN-025 | FIN-422-DIMENSION-TAG-DUP (422) | TC-FIN-141 · TC-FIN-140 (passes) |
| RULE-FIN-026 | FIN-422-DIMENSION-TAG-SOURCE (422) | TC-FIN-142 · TC-FIN-140 (passes) |
| — (not a rule) | FIN-409-INVALID-TRANSITION (409) | TC-FIN-081 |
| — (not a rule) | FIN-409-PERIODS-NOT-CLOSED (409) | TC-FIN-084 |
| — (not a rule) | FIN-400-MISSING-FREQUENCY, pre-empted on the form | TC-FIN-070 |

**Catalog rows no TC on this track exercises**, stated rather than left unsaid: the uniqueness
rows FIN-409-ACCOUNT-DUP, FIN-409-DIMENSION-DUP, FIN-409-RULE-DUP and FIN-409-YEAR-DUP, the
lookup and sort validation rows, FIN-422-REMAINDER-MARKER, FIN-422-REMAINDER-NOT-POSITIVE,
FIN-422-INVALID-PERCENTAGE-VALUE, FIN-409-NOT-ACTIVE, the `FIN-404-*` not-found rows
(FIN-404-ACCOUNT-MAPPING among them), FIN-409-NOT-POSTED, the two RULE-FIN-017 400s,
FIN-422-DERIVATION-SPEC and FIN-500. The event-path codes of RULE-FIN-020, RULE-FIN-021,
RULE-FIN-027 and RULE-FIN-028 are not on this track at all, no screen calling API-FIN-020; the
backend track covers each. No in-track `AC-FIN-*` states any of these rows. Writing a screen case
for a code no acceptance criterion asserts is the over-engineering §3 rule 6 forbids, and
ADR-FIN-009 and ADR-FIN-063 record the boundary. Two rows must not be tested:
FIN-403-SOD-VIOLATION is struck as unreachable and no client-side separation-of-duty check exists
to assert; FIN-422-MAPPING-UNSUPPORTED is retired in v2 and gone from the published catalogue
(ADR-FIN-027, ADR-FIN-064), so no screen can reach it.

## COVERAGE

```
AC  covered   56/56   ✓ 0 gaps on this track's denominator — the ACs a screen can exercise
                      (40 v1 + 16 v2). The 27 outside it are not gaps: each is covered on the
                      backend track, and the header table names the TC that covers it.
                      Module-wide the figure is 83/83.
REQ covered   56/56   ✓ 0 gaps on the same denominator (REQ-FIN-010…013, 044, 045, 047,
                      050…053, 063, 066…069, 072…076 excluded — the same event and onboarding
                      paths)
SCR covered   13/13   ✓ 0 gaps — every SCR-FIN-001…013 is named by ≥1 TC's Exercises line
TC count      56      ✓ 1.00× the in-track AC count — well under the ~2× over-engineering guard
Waiting       0/56    ADR-FIN-064 bound every v2 call, member and code to the republished
                      api-docs, so no case waits on publication. TC-FIN-143 … TC-FIN-145 still
                      need fixtures that only the v2 build-from-event path produces
Integration   n/a     scope = module: no INT-UXD phase is emitted and none is owed (§2 rule 3).
                      UXD-FIN-001 … UXD-FIN-015 are cited as the option lists a control loads,
                      never asserted as cross-module scenarios — every one of them is owned by
                      MDL, which is not in this run's selection
```
══════════════════════════════════════════════════════════════════

<<<END INPUT>>>

<<<INPUT: test-execution-manifest (baseline — continue it, never restart it)>>>
# TEST EXECUTION MANIFEST — الحسابات العامة / Finance (General Ledger) (FIN)
══════════════════════════════════════════════════════════════════
Module : FIN   Version : v2 (delta on v1 — change set CS-FIN-001)   Profile : erp   Scope : module
Emitted because `profile.stack.testing.manifest` is ON. A **derived view** for the `api-verify`
standalone: it introduces no id, defines nothing, gates nothing, and every cell below is an
`ID` or a value one of the cited artifacts already states.
Derived from : `backend-test-plan-fin.md` v2 (this run) · `_state/current-backend-execution-plan.md`
               (v2) · `_state/current-srs.md` (v2) · `_state/current-registry-db.md` (v2)
Regenerate whenever the backend execution plan or the backend test plan changes — a stale
manifest is a defect. v2 regenerates it in full: it carries no id to fold by, so a partial
manifest would drop every v1 row.
══════════════════════════════════════════════════════════════════

## DEPENDENCY ORDER

The order in which entities must be built for any TC of this module to run. It is read off the FK
and XM relations of the backend plan and the "must belong to" rules, and nothing else.

```
0.  MDL · LookupType + LookupValue for FIN's 15 owned keys      (ENT-MDL-001/002, XM-FIN-001)
      ── not this module's data. Every lookup-backed column is validated by membership in the
         list MDL returns, so no FIN row carrying a code can be written before the keys exist.
         REQ-FIN-045 / TC-FIN-045 is the registration: the 13 v1 keys by onboarding, then
         db-script v2 BLOCK 8 adds FIN_EVENT_BUSINESS_FIELD (4 values) and
         FIN_DIMENSION_VALUE_SOURCE (2), seeds CHEQUE into PAYMENT_METHOD and deactivates
         DIRECT in ACCOUNT_DERIVATION_TYPE. BLOCK 8 must run before API-FIN-011 or API-FIN-039 is
         first called. Host data comes on top: ACCOUNTING_EVENT_TYPE values (PAYMENT_RECEIVED,
         EXPENSE_PAID in the v2 cases) and every PAYMENT_METHOD value but CHEQUE (CASH,
         TRANSFER) are added in MDL by the deploying organisation — a mapping for a
         PAYMENT_METHOD value that MDL lacks is refused by RULE-FIN-024.
1.  SEC · ModuleRegistry, ScreenRegistry, ActionRegistry rows for FIN   (ENT-SEC-004/005/006)
      ── every endpoint below is gated, so no TC has a caller until FIN's 13 screens and 30
         actions are registered and granted. REQ-FIN-044 / TC-FIN-044 is that registration.
         The three FIN_ACCOUNT_MAPPINGS actions come from the v2 FIN security migration, which
         also grants them to SYS_ADMIN; without that grant every mapping case answers
         FIN-403-FORBIDDEN. The event consumer's service principal needs a role holding
         PERM_FIN_JOURNAL_ENTRIES_CREATE for every API-FIN-020 case.
2.  ENT-FIN-001 · Account            (FIN_ACCOUNT)              ← API-FIN-002
      ── self-referencing through parentAccountId (DBF-FIN-007): roots before children, and a
         parent stops being a leaf as soon as it gains one (RULE-FIN-001). At most one row
         carries isRetainedEarningsFl (DBF-FIN-147), which API-FIN-027 derives rather than
         receives — TC-FIN-036 needs it present.
3.  ENT-FIN-002 · Dimension          (FIN_DIMENSION)            ← API-FIN-006
4.  ENT-FIN-003 · DimensionValue     (FIN_DIMENSION_VALUE)      ← API-FIN-007
      ── depends on 3: the code must be unique within its dimension (RULE-FIN-002), and the
         parent id arrives as the path id. Only API-FIN-035 can clear its active flag, which is
         the one setup step of TC-FIN-021, TC-FIN-069 and TC-FIN-118. The v2 event cases resolve
         a value by its code within its dimension (ADR-FIN-026), so ORGANISATION "ORG-1" and
         BRANCH "BR-07" must exist before any tagged event posts.
5.  ENT-FIN-007 · FiscalYear         (FIN_FISCAL_YEAR)          ← API-FIN-023
      ── TC-FIN-036 additionally needs a SUCCESSOR year whose startDate is the day after this
         year's endDate: the adjacency is derived, ENT-FIN-007 declaring no successor column.
6.  ENT-FIN-008 · FiscalPeriod       (FIN_FISCAL_PERIOD)        ← API-FIN-023 (generated with 5)
      ── never created directly; generated with its year, each period OPEN. Transitions run
         OPEN ⇄ SOFT_CLOSE → HARD_CLOSE → YEAR_END_CLOSE (API-FIN-024/025/026, then 027).
         TC-FIN-126 needs a date that NO period covers; TC-FIN-125 a covering period in
         SOFT_CLOSE.
7.  ENT-FIN-009 · EventTypeRule      (FIN_EVENT_TYPE_RULE)      ← API-FIN-010
8.  ENT-FIN-010 · RuleLine           (FIN_RULE_LINE)            ← API-FIN-011
      ── depends on 7 (path id) and on 2 for any CONSTANT-derived account. RULE-FIN-003 is
         evaluated over the whole sibling set, so lines must be added in an order that never
         leaves a percentage set without exactly one remainder line at save time. v2: a line is
         CONSTANT (an account code, no business field) or MAPPING (a business field, no code),
         RULE-FIN-018 and RULE-FIN-019.
9.  ENT-FIN-016 · RuleLineDimension  (FIN_RULE_LINE_DIM)        ← API-FIN-011 (same request, v2)
      ── written in one transaction with its line, never alone; depends on 8, on 3 (the tagged
         dimension) and, for a CONSTANT tag, on 4 (a value of that same dimension,
         RULE-FIN-026). One tag per dimension per line (RULE-FIN-025).
10. ENT-FIN-015 · AccountMapping     (FIN_ACCOUNT_MAPPING)      ← API-FIN-039 (v2)
      ── depends on 2 (an active leaf on save, RULE-FIN-023) and on step 0 (the event type,
         the business field and, for PAYMENT_METHOD, the value, RULE-FIN-024). It is
         independent of 7–9 in the store — no FK ties a mapping to a rule — but an event whose
         MAPPING line reads it posts only once both exist. One active row per key
         (UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY). An account's later loss of leaf or active status
         is caught at post (TC-FIN-100, TC-FIN-101), not re-checked on the mapping.
11. ENT-FIN-011 · RecurringTemplate  (FIN_RECURRING_TEMPLATE)   ← API-FIN-013
12. ENT-FIN-012 · RecurringTemplateLine                         ← API-FIN-013 (same request)
      ── depends on 11, on 2 (accountId) and optionally on 4 (dimensionValueId).
13. ENT-FIN-013 · AllocationRule     (FIN_ALLOCATION_RULE)      ← API-FIN-016
14. ENT-FIN-014 · AllocationTarget   (FIN_ALLOCATION_TARGET)    ← API-FIN-016 (same request)
      ── depends on 13 and on 2 (sourceAccountId, targetAccountId); RULE-FIN-003 again.
15. ENT-FIN-004 · JournalEntry       (FIN_JOURNAL_ENTRY)        ← API-FIN-019, 020, and generated
                                                                   by 014, 017, 021, 027
16. ENT-FIN-005 · JournalLine        (FIN_JOURNAL_LINE)         ← written with 15
17. ENT-FIN-006 · JournalLineDimension                          ← written with 16
      ── 15–17 are one atomic write and depend on everything above: leaf active accounts (2),
         an Open period belonging to the submitted year (5, 6), valid active dimension values
         (3, 4), and — for an event-built entry — an active rule with its lines and tags
         (7, 8, 9) plus an active mapping for every MAPPING line (10). An event-built line
         writes its dimensions in exactly the manual shape (REQ-FIN-070) and stores the account
         it resolved, so a later mapping change never reaches it (TC-FIN-108). The docNo
         counter is scoped per fiscalYearId and restarts at 000001 each year, so a test
         asserting a document number must know which year's series it is drawing from. A
         rejected event writes nothing at all (SRS A7).
```

Nothing is seeded outside these steps: every required column is written by an endpoint or by the
platform. Steps 0 and 1 are the only rows FIN does not create through an endpoint of its own.
CS-FIN-001's own migration data — the real account mappings — is out of scope; until it is
loaded, every MAPPING line answers FIN-422-UNMAPPED-VALUE, and each v2 case sets up the mappings
it needs.

## RULE → CODE → TC

Runtime code format `FIN-{http}[-{SLUG}]`; the envelope is
`LocalizedException → {code, messageAr, messageEn}`. Rows are the rules that raise a catalog code;
the rules whose effect is a success path or an omission are listed after the table rather than
given empty cells. Frontend cases are marked.

| RULE | catalog code | TC | HTTP | API |
|---|---|---|---|---|
| RULE-FIN-001 | FIN-409-HAS-CHILDREN | TC-FIN-002 · TC-FIN-054 (frontend) | 409 | API-FIN-003 |
| RULE-FIN-001 | FIN-409-PARENT-NOT-LEAF-ELIGIBLE | — no TC (no AC states the create-path variant) | 409 | API-FIN-002 |
| RULE-FIN-002 | FIN-409-DIMVALUE-DUP | TC-FIN-006 · TC-FIN-058 (frontend) | 409 | API-FIN-007 |
| RULE-FIN-003 | FIN-409-REMAINDER-COUNT | TC-FIN-009, TC-FIN-047 · TC-FIN-061, TC-FIN-073 (frontend) | 409 | API-FIN-011, API-FIN-016, API-FIN-017, API-FIN-020 |
| RULE-FIN-003 | FIN-422-REMAINDER-MARKER | — no TC (no AC states marker disagreement) | 422 | API-FIN-011, API-FIN-016, API-FIN-017, API-FIN-020 |
| RULE-FIN-004 | FIN-409-DUPLICATE-EVENT | TC-FIN-011, TC-FIN-123 (the pre-check), TC-FIN-124 (the store's refusal under concurrency) | 409 | API-FIN-020 |
| RULE-FIN-005 | FIN-404-NO-ACTIVE-RULE | TC-FIN-013 | 404 | API-FIN-020 |
| RULE-FIN-006 | FIN-409-UNBALANCED | TC-FIN-015, TC-FIN-018 · TC-FIN-063, TC-FIN-066 (frontend) | 409 | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021, API-FIN-027 |
| RULE-FIN-007 | FIN-409-NOT-POSTABLE-ACCOUNT | TC-FIN-003, TC-FIN-015, TC-FIN-019, TC-FIN-100, TC-FIN-101 (a mapped account) · TC-FIN-055, TC-FIN-063, TC-FIN-067 (frontend) | 409 | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021, API-FIN-027 |
| RULE-FIN-008 | FIN-409-PERIOD-NOT-OPEN | TC-FIN-020, TC-FIN-033, TC-FIN-125 (the event path — the consumer's retry signal), TC-FIN-127 (its published row) · TC-FIN-068 (frontend) | 409 | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021 |
| RULE-FIN-008 | — (carve-out: the year-end CLOSING/OPENING entries are exempt, asserted as **not** raised) | TC-FIN-036 | — | API-FIN-027 |
| RULE-FIN-009 | FIN-409-INVALID-DIMENSION | TC-FIN-021, TC-FIN-118 (an event-derived value) · TC-FIN-069 (frontend) | 409 | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021, API-FIN-027 |
| RULE-FIN-010 | FIN-422-REMAINDER-NOT-POSITIVE | TC-FIN-048 | 422 | API-FIN-017, API-FIN-020 |
| RULE-FIN-013 | FIN-409-ALREADY-REVERSED | TC-FIN-030 · TC-FIN-078 (frontend) | 409 | API-FIN-021 |
| RULE-FIN-013 | FIN-409-NOT-POSTED | — no TC (AC-FIN-030's Given is the already-reversed entry; DRAFT is never a stored browsable state) | 409 | API-FIN-021, API-FIN-014 |
| RULE-FIN-014 | FIN-409-NOT-REOPENABLE | TC-FIN-035 · TC-FIN-083 (frontend) | 409 | API-FIN-024, API-FIN-026 |
| RULE-FIN-015 | FIN-403-FORBIDDEN | TC-FIN-038, TC-FIN-052 · TC-FIN-086 (frontend) | 403 | API-FIN-026, API-FIN-027 |
| RULE-FIN-017 | FIN-400-PERIOD-NOT-IN-YEAR, FIN-400-DOCDATE-OUTSIDE-PERIOD | — no TC (the rule was added at SVC-API and carries no AC of its own) | 400 | API-FIN-019 |
| RULE-FIN-018 | FIN-422-UNSUPPORTED-DERIVATION | TC-FIN-094 · TC-FIN-130 (frontend) | 422 | API-FIN-011 |
| RULE-FIN-019 | FIN-422-DERIVATION-SPEC | — no TC raises it (AC-FIN-049 is the passing case, TC-FIN-095; TC-FIN-131 the form pre-empt) | 422 | API-FIN-011 |
| RULE-FIN-020 | FIN-422-UNMAPPED-VALUE | TC-FIN-102, TC-FIN-103, TC-FIN-107 | 422 | API-FIN-020 |
| RULE-FIN-021 | FIN-422-MISSING-BUSINESS-FIELD | TC-FIN-099 (a MAPPING line), TC-FIN-119 (a dimension tag) | 422 | API-FIN-020 |
| RULE-FIN-022 | FIN-409-MAPPING-DUP | TC-FIN-109 · TC-FIN-137 (frontend) | 409 | API-FIN-039 |
| RULE-FIN-023 | FIN-422-MAPPED-ACCOUNT-INELIGIBLE | TC-FIN-110 · TC-FIN-138 (frontend) | 422 | API-FIN-039, API-FIN-040 |
| RULE-FIN-024 | FIN-422-INVALID-PAYMENT-METHOD | TC-FIN-111 · TC-FIN-139 (frontend) | 422 | API-FIN-039 |
| RULE-FIN-025 | FIN-422-DIMENSION-TAG-DUP | TC-FIN-115 · TC-FIN-141 (frontend) | 422 | API-FIN-011 |
| RULE-FIN-026 | FIN-422-DIMENSION-TAG-SOURCE | TC-FIN-116 · TC-FIN-142 (frontend) | 422 | API-FIN-011 |
| RULE-FIN-027 | FIN-422-UNRESOLVED-DIMENSION-VALUE | TC-FIN-120 | 422 | API-FIN-020 |
| RULE-FIN-028 | FIN-422-NO-DEFINED-PERIOD | TC-FIN-126 | 422 | API-FIN-020 |

**Rules that raise no code, and why** — they are enforced, not informational, so they are named
rather than dropped: RULE-FIN-010's own computation (the per-side remainder difference) is a
success path, asserted by TC-FIN-012, TC-FIN-026 and, on a MAPPING remainder line, TC-FIN-128;
the exact mirrored reversal of RULE-FIN-011 and its period substitution under RULE-FIN-012 are
success paths, asserted by TC-FIN-028, TC-FIN-029, TC-FIN-024 and, for a new-path entry, TC-FIN-129;
RULE-FIN-016 is enforced by omission — no update and no delete mapping exists on a POSTED row
anywhere in the module — and is asserted by TC-FIN-016 and TC-FIN-017; RULE-FIN-020 and
RULE-FIN-027 also have success paths, the resolved mapping of TC-FIN-096 … TC-FIN-098 and the
resolved dimension value of TC-FIN-114. The three messages the SRS gives RULE-FIN-010,
RULE-FIN-011 and RULE-FIN-012 are the only genuinely informational strings in the module and no
TC asserts them as a response body.

**The event consumer's three classes** (backend plan, DOC phase), for `api-verify`: already
posted — FIN-409-DUPLICATE-EVENT, acknowledge (TC-FIN-123, TC-FIN-124); retry later —
FIN-409-PERIOD-NOT-OPEN, wait and retry (TC-FIN-125); needs a fix — every other API-FIN-020 code,
the four v2 ones among them (TC-FIN-099, TC-FIN-102, TC-FIN-120, TC-FIN-126). TC-FIN-125 and
TC-FIN-126 together assert that a date with no period never answers the retry code.

**Catalog rows no TC exercises**, listed so `api-verify` does not read this table as complete
coverage of the catalog: FIN-409-ACCOUNT-DUP, FIN-409-DIMENSION-DUP, FIN-409-RULE-DUP,
FIN-409-YEAR-DUP, FIN-400-INVALID-LOOKUP, FIN-400-MISSING-FREQUENCY, FIN-400-INVALID-SORT,
FIN-422-INVALID-PERCENTAGE-VALUE, FIN-409-NOT-ACTIVE, FIN-409-INVALID-TRANSITION,
FIN-409-PERIODS-NOT-CLOSED, FIN-422-DERIVATION-SPEC, FIN-404-ACCOUNT-MAPPING, the `FIN-404-*`
not-found rows raised outside an AC's own path, and FIN-500. No `AC-FIN-*` states any of them;
the absence is a requirements-side gap, not a test-generation one — ADR-FIN-009, ADR-FIN-063.

**Two rows are not gaps and must not be tested**: `FIN-403-SOD-VIOLATION`, struck as unreachable
— its two throw sites were deleted and the global user-set-disjointness behaviour it signalled is
not what RULE-FIN-015 asks for, so TC-FIN-038 asserts the permission gate and explicitly nothing
about disjointness; and `FIN-422-MAPPING-UNSUPPORTED`, struck in v2 because MAPPING is
implemented (ADR-FIN-027) — no path raises it.

## ENTITY CRUD CHECKLIST

Each cell is ✓ only where an `API-*` of the backend plan serves it; a `—` carries its reason.
`deactivate` is the soft semantics the profile declares: no endpoint of this module removes a row.

| ENT | create | read (by id) | search | update | deactivate (soft) | activate |
|---|---|---|---|---|---|---|
| ENT-FIN-001 · Account | ✓ API-FIN-002 | — deliberate v1 exclusion: the Entry form hydrates from the search row | ✓ API-FIN-001 | ✓ API-FIN-003 | ✓ API-FIN-004 | — none in FIN, following the delivered deactivate precedent |
| ENT-FIN-002 · Dimension | ✓ API-FIN-006 | — none published | ✓ API-FIN-005 | — none published | — deliberate: no REQ/AC/RULE needs it and the flag drives no behaviour | — none in FIN |
| ENT-FIN-003 · DimensionValue | ✓ API-FIN-007 | — none published | ✓ API-FIN-008 | — none published | ✓ API-FIN-035 | — none in FIN |
| ENT-FIN-004 · JournalEntry | ✓ API-FIN-019, API-FIN-020 (and generated by API-FIN-014, 017, 021, 027) | ✓ API-FIN-022 | ✓ API-FIN-018 | — RULE-FIN-016: a POSTED entry is locked; correction is a reversal | — no soft-delete flag on this entity | — n/a |
| ENT-FIN-005 · JournalLine | ✓ with its header | ✓ nested in API-FIN-022; read by API-FIN-028, 029, 030, 031 | — no standalone line search | — never edited independently of its header | — n/a | — n/a |
| ENT-FIN-006 · JournalLineDimension | ✓ with its line — for an event-built line, one row per resolved tag (v2) | ✓ nested in API-FIN-022; read by API-FIN-032 | — none | — none | — n/a | — n/a |
| ENT-FIN-007 · FiscalYear | ✓ API-FIN-023 | — none published: the create response already carries the year with its periods | — **KNOWN GAP** in the backend plan: its API registry holds no fiscal-year search. The fetched api-docs publish `POST /api/v1/fin/fiscal-years/search` without a contract id, and the frontend cites it by path (ADR-FIN-061). No backend TC exercises it, no AC stating a search; on the frontend it feeds the year list and record page TC-FIN-079 walks through, and the year selects | ✓ statusCode only, set once by API-FIN-027 | — no soft-delete flag | — n/a |
| ENT-FIN-008 · FiscalPeriod | ✓ generated with its year by API-FIN-023 | — none published: API-FIN-033 returns the full row | ✓ API-FIN-033 | ✓ API-FIN-024, API-FIN-025, API-FIN-026 (the three state transitions) | — no soft-delete flag; HARD_CLOSE is the terminal state instead | — RULE-FIN-014: a hard-closed period never reopens |
| ENT-FIN-009 · EventTypeRule | ✓ API-FIN-010 | — none published | ✓ API-FIN-009 | — none published | ✓ API-FIN-034 | — none in FIN |
| ENT-FIN-010 · RuleLine | ✓ API-FIN-011 (v2: with its tags) | ✓ embedded in the rule's own response | — none | — none published | — the entity carries no active flag | — n/a |
| ENT-FIN-011 · RecurringTemplate | ✓ API-FIN-013 | ✓ the search response carries the full aggregate, lines included | ✓ API-FIN-012 | — **OPEN GAP**: no update endpoint; a wrong template can only be retired and replaced | ✓ API-FIN-036 | — none in FIN |
| ENT-FIN-012 · RecurringTemplateLine | ✓ with its template | ✓ in the aggregate | — none | — none | — n/a | — n/a |
| ENT-FIN-013 · AllocationRule | ✓ API-FIN-016 | ✓ the search response carries the full aggregate, targets included | ✓ API-FIN-015 | — **OPEN GAP**: no update endpoint; same shape as ENT-FIN-011 | ✓ API-FIN-037 | — none in FIN |
| ENT-FIN-014 · AllocationTarget | ✓ with its rule | ✓ in the aggregate | — none | — none | — n/a | — n/a |
| ENT-FIN-015 · AccountMapping (v2) | ✓ API-FIN-039 | — none: the SRS names no by-id read and the Entry hydrates from the search row | ✓ API-FIN-038 | ✓ API-FIN-040 — the account only; the key is fixed after create (ADR-FIN-024) | ✓ API-FIN-041 | — none: a deactivated key is restored by creating a new mapping (ADR-FIN-024) |
| ENT-FIN-016 · RuleLineDimension (v2) | ✓ with its rule line, API-FIN-011 | ✓ embedded in the line's response | — none | — none: a tag lives and retires with its line (ADR-FIN-025) | — no active flag | — n/a |

**Custom actions, which no CRUD column holds**: reverse an entry (API-FIN-021, gated by its own
`PERM_FIN_JOURNAL_ENTRIES_REVERSE`, and reversing an event-built entry exactly like any other);
run a template (API-FIN-014) and run an allocation rule (API-FIN-017), both modelled as
update-class actions; hard-close a period (API-FIN-026) and run the year-end close (API-FIN-027),
both gated by `PERM_FIN_PERIODS_CLOSE_APPROVE` — the distinct permission RULE-FIN-015 requires;
build an entry from an event (API-FIN-020), a system call under the consumer's service principal.

**No DELETE column appears above** because FIN publishes no `DELETE` endpoint on any screen and
no `PERM_FIN_*_DELETE` row is registered — v2's FIN_ACCOUNT_MAPPINGS included. Every `—` in the
table is a recorded decision or a recorded gap, not an untested endpoint.
══════════════════════════════════════════════════════════════════

<<<END INPUT>>>

<<<INPUT: change-manifest>>>
# CHANGE MANIFEST — CS-FIN-001
Module       : FIN      Version: v2      Baseline: v1
Change type  : ADDITIVE
Summary      : Make FIN able to derive and post a journal entry from a business event emitted by the legacy Oracle system, so that legacy accounting can be retired. Four deltas on v1, none of which reopens a v1 invariant. (1) MAPPING account derivation: a FIN-owned, screen-managed mapping store that turns a business value carried by the event into an account code, replacing the unimplemented MAPPING mode; an unmapped value is a hard error and the entry does not post. (2) Dimension tags on rule-driven lines: a rule line carries dimension values derived from the event, each either a constant or a named business field, validated by FIN's existing dimension rules, so event-built and manually-built lines have the same dimension shape. (3) A duplicate event reference must always answer the duplicate-event error, whether the pre-check or the unique constraint caught it. (4) A posting attempt into a non-open period must answer its own distinct, catalogued and documented error code. The governing boundary for all four: no account code and no accounting concept of any kind may travel inside an event payload; the event states a business fact and FIN alone decides what it means in accounting terms.

## Scope detail (the raised request, restated for this change set)

### 1. MAPPING account derivation — blocking

A rule line derives its account today by CONSTANT (the value is the account code, fixed per line) or
by DIRECT (the value names an event field carrying the account code). MAPPING is declared but throws
an unsupported error. CONSTANT cannot vary; DIRECT breaks the boundary because it puts an account
code in the payload. Two legacy behaviours choose the account by a business value and fit neither:

- Cash versus bank. Legacy posts to the cash account and swaps it for the bank account when the
  payment was by cheque. The event carries the payment method as a business fact and FIN must decide
  which account that means. Eleven of the sixteen live legacy branches need this.
- Expense account per expense type. The event carries an expense type code and the account depends
  on it.

Requested: a FIN-owned mapping store, managed as data through a FIN screen like every other
configuration in the module, holding per entry the event type it serves, the business field being
mapped (payment method, expense type code), the business source value, the account code it resolves
to, an active flag and audit fields per FIN's existing conventions. Behaviour: when a rule line's
derivation is MAPPING, the engine reads the named business field from the event, resolves it through
the store, and validates the resulting account exactly as it validates a CONSTANT one — leaf, active,
postable. An unmapped value is an error and no entry posts; never a default, never a guess. A
dedicated error code is needed and FIN's analysis owns its name.

Also to settle in this change set: DIRECT is the only remaining way an account code can enter through
a payload, and once MAPPING exists it has no legitimate use in this architecture. Review it
explicitly and record the decision as an ADR — remove, deprecate, or restrict. Leaving it unexamined
is not acceptable.

### 2. Dimension tags on rule-driven lines — blocking

FIN v1 has dimensions on the chart of accounts and reports by dimension, but lines built by the rules
engine from an event carry no dimension values at all. Legacy stamps organisation and branch on every
journal entry. Without an equivalent, every event-sourced entry loses its branch and organisation
attribution, the parallel-run comparison between legacy and FIN diverges on every row (which makes
shadow-mode verification worthless), and FIN's own dimension reports are empty for exactly the
entries that matter most.

Requested: a rule line must be able to carry dimension values derived from the event, using the same
discipline as accounts — per dimension the value is either a constant or the content of a named
business field in the event (organisation code, branch code). The resolved value is validated against
FIN's existing dimension rules (belongs to that dimension, active); the existing validation is not
bypassed. Event-built and manually-built lines must end up with the same dimension shape so that
ledger and dimension reports treat them identically.

### 3. Return the duplicate-event error reliably — small

FIN checks the event reference for a duplicate and then inserts: check-then-act. When the same event
arrives twice at once the unique constraint catches the second, but it surfaces as a generic
data-integrity failure in the 500 class rather than as the duplicate-event error. For the consumer a
duplicate is success, not failure — it means the event was already posted and the message can be
acknowledged. A generic 500 looks like an outage: the consumer retries, the message lands in the
dead-letter queue, and someone reconciles by hand an event that posted correctly the first time.

Requested: a duplicate event reference must always answer the duplicate-event error, whether it was
caught by the pre-check or by the unique constraint.

### 4. Confirm the closed-period response — small

The consumer must treat "the period is closed" differently from every other rejection: it is an
operational state that resolves itself when someone opens the period, so the event should wait and be
retried rather than be marked failed.

Requested: confirm FIN answers a posting attempt into a non-open period with its own distinct,
catalogued error code, distinct from validation and configuration errors, and that the code is
documented for the consumer to depend on. If it does not exist, add it.

## Acceptance for this change set

1. A rule line resolves its account from a business value in the event, with no account code anywhere
   in the payload.
2. An unmapped business value produces a clear error and no posted entry.
3. Mappings are added and changed as data through a FIN screen, never in code.
4. Rule-driven lines carry validated dimension values derived from event fields.
5. A duplicate event reference always answers the duplicate-event error.
6. A closed-period rejection has its own distinct, documented code.
7. Every FIN v1 invariant still holds for entries produced through the new path — debits equal
   credits, leaf and active accounts only, the period gate at post time, positive amounts with
   direction, the single remainder line, duplicate rejection, derived balances, immutable audit
   trail. These are inherited, not reopened.

## Out of scope for this change set

- The in-database queue, the consumer service, and everything inside the legacy Oracle system.
- Seeding the actual account mappings for the migration: that is migration data, produced once the
  mapping store exists.
- A second event source or a broker: the consumer is built so adding one later is an added adapter,
  not a rebuild.
- The v1 exclusions stand unchanged — multi-currency, multi-ledger and multi-entity, intercompany,
  statistical accounts, multi-pattern calendar, attachments.
- The machine-caller authentication needed by the consumer is a SEC delta, tracked separately, not
  part of this change set.

## Per artifact

srs:
  ADDED    : ENT-FIN-015, ENT-FIN-016, REQ-FIN-047, REQ-FIN-048, REQ-FIN-049, REQ-FIN-050, REQ-FIN-051, REQ-FIN-052, REQ-FIN-053, REQ-FIN-054, REQ-FIN-055, REQ-FIN-056, REQ-FIN-057, REQ-FIN-058, REQ-FIN-059, REQ-FIN-060, REQ-FIN-061, REQ-FIN-062, REQ-FIN-063, REQ-FIN-064, REQ-FIN-065, REQ-FIN-066, REQ-FIN-067, REQ-FIN-068, REQ-FIN-069, REQ-FIN-070, REQ-FIN-071, REQ-FIN-072, REQ-FIN-073, REQ-FIN-074, REQ-FIN-075, REQ-FIN-076, REQ-FIN-077, AC-FIN-047, AC-FIN-048, AC-FIN-049, AC-FIN-050, AC-FIN-051, AC-FIN-052, AC-FIN-053, AC-FIN-054, AC-FIN-055, AC-FIN-056, AC-FIN-057, AC-FIN-058, AC-FIN-059, AC-FIN-060, AC-FIN-061, AC-FIN-062, AC-FIN-063, AC-FIN-064, AC-FIN-065, AC-FIN-066, AC-FIN-067, AC-FIN-068, AC-FIN-069, AC-FIN-070, AC-FIN-071, AC-FIN-072, AC-FIN-073, AC-FIN-074, AC-FIN-075, AC-FIN-076, AC-FIN-077, AC-FIN-078, AC-FIN-079, AC-FIN-080, AC-FIN-081, AC-FIN-082, AC-FIN-083, RULE-FIN-018, RULE-FIN-019, RULE-FIN-020, RULE-FIN-021, RULE-FIN-022, RULE-FIN-023, RULE-FIN-024, RULE-FIN-025, RULE-FIN-026, RULE-FIN-027, RULE-FIN-028, SCR-REQ-FIN-013
  MODIFIED : ENT-FIN-010, REQ-FIN-045, AC-FIN-045, RULE-FIN-004, SCR-REQ-FIN-003, SCR-REQ-FIN-006, RULE-FIN-008
  REMOVED  : none
  UNCHANGED: every other v1 ENT / REQ / AC / RULE / SCR-REQ record (carried from v1 by gov.py state); A1, A2, A3 preamble, A6, A7, A8 and STANDALONE are restated in full (they carry no id to fold by)
registry-srs:
  MODIFIED : all
db-script:
  ADDED    : DBF-FIN-148, DBF-FIN-149, DBF-FIN-150, DBF-FIN-151, DBF-FIN-152, DBF-FIN-153, DBF-FIN-154, DBF-FIN-155, DBF-FIN-156, DBF-FIN-157, DBF-FIN-158, DBF-FIN-159, DBF-FIN-160, DBF-FIN-161, DBF-FIN-162, DBF-FIN-163, DBF-FIN-164, DBF-FIN-165
  MODIFIED : DBF-FIN-041, DBF-FIN-101, DBF-FIN-102, XM-FIN-001
  REMOVED  : none
  UNCHANGED: every other v1 DBF / XM definition (carried from v1 by gov.py state); the field matrix (§1) and the XM register (§2) are restated in full as current state; the SQL (§3) is a migration on v1
registry-db:
  MODIFIED : all
backend-execution-plan:
  ADDED    : API-FIN-038, API-FIN-039, API-FIN-040, API-FIN-041, QR-FIN-050, QR-FIN-051, QR-FIN-052, QR-FIN-053, QR-FIN-054, QR-FIN-055, QR-FIN-056, QR-FIN-057, QR-FIN-058, QR-FIN-059, QR-FIN-060, QR-FIN-061, ENT-FIN-015, ENT-FIN-016
  MODIFIED : API-FIN-001, API-FIN-002, API-FIN-003, API-FIN-004, API-FIN-005, API-FIN-006, API-FIN-007, API-FIN-008, API-FIN-009, API-FIN-010, API-FIN-011, API-FIN-012, API-FIN-013, API-FIN-014, API-FIN-015, API-FIN-016, API-FIN-017, API-FIN-018, API-FIN-019, API-FIN-020, API-FIN-021, API-FIN-022, API-FIN-023, API-FIN-024, API-FIN-025, API-FIN-026, API-FIN-027, API-FIN-028, API-FIN-029, API-FIN-030, API-FIN-031, API-FIN-032, API-FIN-033, API-FIN-034, API-FIN-035, API-FIN-036, API-FIN-037, QR-FIN-015, QR-FIN-025, QR-FIN-026, ENT-FIN-001, ENT-FIN-002, ENT-FIN-003, ENT-FIN-004, ENT-FIN-005, ENT-FIN-006, ENT-FIN-007, ENT-FIN-008, ENT-FIN-009, ENT-FIN-010, ENT-FIN-011, ENT-FIN-012, ENT-FIN-013, ENT-FIN-014, XM-FIN-001
  REMOVED  : none
  UNCHANGED: QR-FIN-001, QR-FIN-002, QR-FIN-003, QR-FIN-004, QR-FIN-005, QR-FIN-006, QR-FIN-007, QR-FIN-008, QR-FIN-009, QR-FIN-010, QR-FIN-011, QR-FIN-012, QR-FIN-013, QR-FIN-014, QR-FIN-016, QR-FIN-017, QR-FIN-018, QR-FIN-019, QR-FIN-020, QR-FIN-021, QR-FIN-022, QR-FIN-023, QR-FIN-024, QR-FIN-027, QR-FIN-028, QR-FIN-029, QR-FIN-030, QR-FIN-031, QR-FIN-032, QR-FIN-033, QR-FIN-034, QR-FIN-035, QR-FIN-036, QR-FIN-037, QR-FIN-038, QR-FIN-039, QR-FIN-040, QR-FIN-041, QR-FIN-042, QR-FIN-043, QR-FIN-044, QR-FIN-045, QR-FIN-046, QR-FIN-047, QR-FIN-048, QR-FIN-049 (restated byte-identical); XM-FIN-002 (carried by gov.py state)
registry-exec-be:
  MODIFIED : all
flow-diagram:
  MODIFIED : all
  REMOVED  : none
ui-ux-spec:
  ADDED    : SCR-FIN-013, UXD-FIN-013, UXD-FIN-014, UXD-FIN-015
  MODIFIED : SCR-FIN-001, SCR-FIN-002, SCR-FIN-003, SCR-FIN-004, SCR-FIN-005, SCR-FIN-006, SCR-FIN-007, SCR-FIN-008, SCR-FIN-009, SCR-FIN-010, SCR-FIN-011, SCR-FIN-012, UXD-FIN-007
  REMOVED  : none
  UNCHANGED: UXD-FIN-001, UXD-FIN-002, UXD-FIN-003, UXD-FIN-004, UXD-FIN-005, UXD-FIN-006, UXD-FIN-008, UXD-FIN-009, UXD-FIN-010, UXD-FIN-011, UXD-FIN-012 (restated byte-identical)
  NOTE     : re-run 2026-09-24 (ADR-FIN-067) — every Composition line names its container; SCR-FIN-006 takes the record-page shape and scoped period options; SCR-FIN-001/013 navigate by accountPk; SCR-FIN-008's journalEntryId moves to its Navigation line
frontend-execution-plan:
  MODIFIED : all
  REMOVED  : none
  NOTE     : re-run 2026-09-24 (ADR-FIN-067) — option, label and scoped reads on every screen that needs them; SCR-FIN-006 record-page composition; every routed refusal bound to its catalogue text
registry-exec-fe:
  MODIFIED : all
backend-test-plan:
  ADDED    : TC-FIN-093, TC-FIN-094, TC-FIN-095, TC-FIN-096, TC-FIN-097, TC-FIN-098, TC-FIN-099, TC-FIN-100, TC-FIN-101, TC-FIN-102, TC-FIN-103, TC-FIN-104, TC-FIN-105, TC-FIN-106, TC-FIN-107, TC-FIN-108, TC-FIN-109, TC-FIN-110, TC-FIN-111, TC-FIN-112, TC-FIN-113, TC-FIN-114, TC-FIN-115, TC-FIN-116, TC-FIN-117, TC-FIN-118, TC-FIN-119, TC-FIN-120, TC-FIN-121, TC-FIN-122, TC-FIN-123, TC-FIN-124, TC-FIN-125, TC-FIN-126, TC-FIN-127, TC-FIN-128, TC-FIN-129
  MODIFIED : TC-FIN-001, TC-FIN-002, TC-FIN-003, TC-FIN-004, TC-FIN-005, TC-FIN-006, TC-FIN-007, TC-FIN-008, TC-FIN-009, TC-FIN-010, TC-FIN-011, TC-FIN-012, TC-FIN-013, TC-FIN-014, TC-FIN-015, TC-FIN-016, TC-FIN-017, TC-FIN-018, TC-FIN-019, TC-FIN-020, TC-FIN-021, TC-FIN-022, TC-FIN-023, TC-FIN-024, TC-FIN-025, TC-FIN-026, TC-FIN-027, TC-FIN-028, TC-FIN-029, TC-FIN-030, TC-FIN-031, TC-FIN-032, TC-FIN-033, TC-FIN-034, TC-FIN-035, TC-FIN-036, TC-FIN-037, TC-FIN-038, TC-FIN-039, TC-FIN-040, TC-FIN-041, TC-FIN-042, TC-FIN-043, TC-FIN-044, TC-FIN-045, TC-FIN-046, TC-FIN-047, TC-FIN-048, TC-FIN-049, TC-FIN-050, TC-FIN-051, TC-FIN-052
  REMOVED  : none
  NOTE     : content changed in TC-FIN-008, TC-FIN-010, TC-FIN-012, TC-FIN-044 and TC-FIN-045 (first run, ADR-FIN-063) and in TC-FIN-019 and TC-FIN-021 (re-run, ADR-FIN-066: the line-naming half of their ACs is stated as failing against the published error body); TC-FIN-049 changes at its record boundary (the next SUB's START traces grew); the other v1 cases are restated word for word and listed MODIFIED because ids-continue compares each START-marker record with the baseline heading record (ADR-FIN-063); of the ADDED cases the re-run changed TC-FIN-127, whose catalogue is now republished (ADR-FIN-064)
frontend-test-plan:
  ADDED    : TC-FIN-130, TC-FIN-131, TC-FIN-132, TC-FIN-133, TC-FIN-134, TC-FIN-135, TC-FIN-136, TC-FIN-137, TC-FIN-138, TC-FIN-139, TC-FIN-140, TC-FIN-141, TC-FIN-142, TC-FIN-143, TC-FIN-144, TC-FIN-145
  MODIFIED : TC-FIN-053, TC-FIN-054, TC-FIN-055, TC-FIN-056, TC-FIN-057, TC-FIN-058, TC-FIN-059, TC-FIN-060, TC-FIN-061, TC-FIN-062, TC-FIN-063, TC-FIN-064, TC-FIN-065, TC-FIN-066, TC-FIN-067, TC-FIN-068, TC-FIN-069, TC-FIN-070, TC-FIN-071, TC-FIN-072, TC-FIN-073, TC-FIN-074, TC-FIN-075, TC-FIN-076, TC-FIN-077, TC-FIN-078, TC-FIN-079, TC-FIN-080, TC-FIN-081, TC-FIN-082, TC-FIN-083, TC-FIN-084, TC-FIN-085, TC-FIN-086, TC-FIN-087, TC-FIN-088, TC-FIN-089, TC-FIN-090, TC-FIN-091, TC-FIN-092
  REMOVED  : none
  NOTE     : content unchanged only in TC-FIN-065, TC-FIN-066, TC-FIN-075, TC-FIN-077, TC-FIN-078, TC-FIN-087, TC-FIN-088, TC-FIN-089 and TC-FIN-091 (TC-FIN-091 changes at its record boundary, the INT-FLOW START traces having grown); every other v1 case changed — TC-FIN-059, TC-FIN-060, TC-FIN-061 and TC-FIN-079 in the first run (ADR-FIN-063), the rest in the re-run that aligns the cases with the frontend plan as ADR-FIN-064 and ADR-FIN-065 revised it (record pages, no client-side action gating, line refusals in the lines pane, independent income-statement bounds, group drill-down — ADR-FIN-066); the ADDED cases TC-FIN-130 … TC-FIN-142 and TC-FIN-145 no longer wait on publication
test-execution-manifest:
  MODIFIED : all
  REMOVED  : none
api-verify-script:
  ADDED    : all
  REMOVED  : none
  NOTE     : first api-verify output of FIN (standalone, outside the line); full tier (api-docs + test-execution-manifest); mints and redefines no id
api-verify-report:
  ADDED    : all
  REMOVED  : none
  NOTE     : written by the script's --plan-only mode (no base URL or credentials were supplied to the stage run, so no call was made); a real run regenerates it with results

<<<END INPUT>>>

---
# KNOWLEDGE (profile primary sources — cite as [KB:<file> §n])

<<<KB: profiles/erp/knowledge/erp-domain-standards.md>>>
# ERP Domain Standards — knowledge base for the `erp` profile

```
Profile   : erp            (profiles/erp.yaml → knowledge.files)
Role      : PRIMARY SOURCE the engines may cite (domain-profile, P0, P1, P2, P3.x)
            when they resolve an ambiguity themselves (factory.yaml → ambiguity).
Replaces  : the former "platform-standards.md Section M" that engines referenced
            but that never existed in the factory.
Rule      : a citation to this file is written as [KB:erp-domain-standards §n].
```

## §1 Module tiers
| Tier | Purpose | Typical modules |
|---|---|---|
| Tier 0 — Foundation | must exist before any business module | Organization (ORG), Security (SEC), Master Data Lookup (MDL) |
| Tier 1 — Core business | first revenue/cost flows | Procurement (PRC), Finance (FIN), Inventory (INV) |
| Tier 2 — Extended business | depends on Tier 1 | Sales (SLS), Contracts (CTR), Human Resources (HR) |

A module may only declare a HARD-FK XM towards a module of the same or a lower tier.

## §2 Entity kinds and defaults
Entity kinds and their default fields are declared in `profiles/erp.yaml → conventions.entity_defaults`.
Rules the engines apply on top:
1. Every master entity is bilingual (`nameAr`, `nameEn`) and soft-deletable (`isActiveFl`).
2. Transactional documents are period-bound (`fiscalYearId`, `periodId`) and status-driven (`statusCode` from a lookup).
3. Lookups are owned by MDL; a module never stores a lookup's display text, only its code.
4. No entity generates its own document numbers — the platform numbering engine does.

## §3 Business-policy conventions (P0 → POL-*)
- A policy is a single, testable sentence in EARS form (see factory.yaml → ids.ears).
- Policies that cross modules are declared once, in the owning (lower-tier) module, and referenced by code elsewhere.
- Fiscal policies (period locking, posting rules) belong to FIN; approval-limit policies belong to the module that owns the document.

## §4 Screens, security and permissions
- Composite screens: Search + Entry (or Master + Detail, Wizard) = ONE `SCR-*` and ONE `SEC_PAGES` row.
- Permission pattern and gateway action: `profiles/erp.yaml → conventions.security_model`.
- Backend: one controller per composite screen; authorization per method (gateway action on reads; CREATE/UPDATE/DELETE on mutations).
- Frontend: one lazily-loaded chunk per composite screen; Search↔Entry via route params.

## §5 Cross-module dependencies (XM)
- `HARD-FK`: a physical foreign key to another module's table — allowed only downward in tier.
- `SOFT-READ`: a read-only lookup by code — allowed in any direction.
- Every XM cites the `REQ-*` that needs it; the consuming module owns the XM record.

## §6 Defaults an engine may assume without asking (after PRD approval)
| Question | Default |
|---|---|
| Soft delete vs hard delete | soft (`isActiveFl`) |
| Audit trail | the four audit fields on every table |
| Paging | server-side, page size 20, max 200 |
| Search | server-side filter on code/name (both languages) |
| Money | `NUMERIC(18,4)`, currency code from MDL |
| Dates | `TIMESTAMPTZ`, stored UTC, displayed in tenant timezone |

Anything not covered here becomes an ADR (`decisions/<MOD>/`) per the ambiguity rule.

<<<END KB>>>
