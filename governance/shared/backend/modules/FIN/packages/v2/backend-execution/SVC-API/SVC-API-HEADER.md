<!-- source: PHASE:SVC-API — preamble before the first SUB -->
<!-- traces: REQ-FIN-001, REQ-FIN-010, REQ-FIN-014, REQ-FIN-017, REQ-FIN-028, REQ-FIN-036, REQ-FIN-050, REQ-FIN-054, REQ-FIN-062, REQ-FIN-072 -->
## PHASE 3 — SVC-API

API count = 41 (≥ 8) → split, grouped SEARCH / CRUD / INT. Every block names its entity and operation on its
`Entity` line, the columns it writes on its `Request` and `Orchestration` lines, and what stops two simultaneous
calls on its `Concurrency` line. For the 35 unchanged v1 endpoints those three lines are the v2 addition
(ADR-FIN-029); the rest of each block is v1 as delivered. API-FIN-011 and API-FIN-020 are rewritten, and
API-FIN-038 … API-FIN-041 are new.
