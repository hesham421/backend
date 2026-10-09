# ADR-FIN-029 — The v2 backend plan is restated in full, and the v1 blocks are brought to the current P3.1 template

Module  : FIN     Version : v2     Stage raised : P3.1 (Backend Execution Plan)
Status  : ACCEPTED (non-breaking)
traces  : REQ-FIN-001, REQ-FIN-045, REQ-FIN-050, REQ-FIN-054, REQ-FIN-062, REQ-FIN-072, API-FIN-011, API-FIN-020

## Context
A delta stage should emit only what changed. Two facts make that unsafe for this plan.

1. `gov.py state` folds a delta plan by carrying a baseline block into the delta only when it is a direct child of a
   phase the delta re-emits. CS-FIN-001 changes blocks inside every SVC-API group (API-FIN-011 in CRUD, API-FIN-020 in
   INT, the new search in SEARCH) and inside two DATA-DOM groups. A group re-emitted with only its v2 blocks would lose
   every v1 block in it from the folded state. Nothing outside a marker is carried at all — the index, the DB Alignment
   Manifest, the Error Catalog and the self-check would vanish.
2. The v1 plan passed its gate (2026-09-10) before the contracts that now check the P3.1 template existed. Read against
   today's checks it lacks an `Entity` line on 29 of 37 endpoint blocks, cites almost no DBF on its Request and
   Orchestration lines (72 `required-writer` findings), has no bootstrap section, and names a PK generation
   (`GENERATED ALWAYS AS IDENTITY`) that migration V22 never built and that the v2 db-script corrects to per-table
   sequences. It also cites three wrong DBF ids: the remainder marker as DBF-FIN-103 (a rule line's amountSourceTypeCode)
   and as DBF-FIN-141 (an allocation target's lineNo), instead of DBF-FIN-107 and DBF-FIN-146. A v2 gate reads the whole
   folded plan, so this debt would block it whatever the delta did.

## Decision
- The v2 plan re-emits every phase. Records it restates unchanged — the v1 query definitions — are byte-identical to
  v1. XM-FIN-002 is left for the fold to carry.
- Every v1 endpoint block keeps its v1 text and gains three lines: `Entity` (the entity and the operation it answers),
  DBF ids on its `Request` and `Orchestration` lines (what it writes), and `Concurrency`. Where the v1 behaviour has an
  unguarded race, the Concurrency line records it as v1-as-built and does not change it. Every v1 entity block is
  rewritten to the R2 form: sequence name in BINDINGS, an OPERATIONS line, rules as a list.
- The three wrong DBF citations are corrected, in the blocks and in the Error Catalog row FIN-422-REMAINDER-MARKER.
- Every re-emitted block that differs from v1 is listed MODIFIED in the version's change manifest.

## Consequences
- No endpoint, query, rule or response changes behaviour through this ADR. Only API-FIN-011, API-FIN-020 and the
  queries QR-FIN-015, QR-FIN-025 and QR-FIN-026 change in substance, and that is CS-FIN-001's scope.
- The change manifest lists every v1 API and entity block as MODIFIED. A downstream stage that re-runs by modified id
  will revisit them. That is the cost of making the plan complete.
- The fold's one-level carry is a tool limitation. It is reported with this run and not changed here.
- Override: emit only the CS-FIN-001 blocks, once the fold carries the grandchildren of a re-emitted phase, and accept
  the inherited findings on the v1 blocks.
