# ADR-FIN-009 — Test-generation derives from the ACs alone: the frontend denominator, the uncovered as-built paths, and the two ACs with no endpoint

Module  : FIN     Version : v1     Stage raised : test-gen (Test Generation, standalone)
Status  : ACCEPTED (non-breaking)

## Context

The test-gen engine derives one `TC-*` per `AC-*` and forbids questions, so three ambiguities in
FIN v1 had to be settled here rather than asked about. All three arise from the same fact: FIN's
as-built artifacts describe more behaviour than its forty-six acceptance criteria state.

**(a) Not every AC can be exercised by a screen.** The coverage rule is that every `AC-FIN-*`
carries ≥1 TC, and each track's plan reports its own `AC covered n/total`. Six ACs have no screen
at all: AC-FIN-010 … AC-FIN-013 are event-driven, and API-FIN-020 is bound but drawn on no form
(ADR-FIN-007); AC-FIN-044 and AC-FIN-045 are deployment-time onboarding, which the P0.5
traceability matrix itself records as "— (onboarding, no screen)". Reading the frontend
denominator as 46 would mark six permanent ✗ gaps and block a run that has no defect in it.

**(b) FIN ships behaviour no AC, REQ or RULE states.** The `FIN-409-NOT-ACTIVE` run gate on
API-FIN-014 / API-FIN-017 was closed on a recorded human decision and the Error Catalog says so in
its own RULE column; API-FIN-033 (fiscal-period search), API-FIN-034 … API-FIN-037 (the four
deactivates), the keying-id 404s on the three report endpoints and the dormant-year lineless
closing entry are all as-built additions or narrowings. A test-generation engine that wrote TCs
for them would be inventing the acceptance criteria it claims to derive from — and the FIN
artifacts warn twice, in as many words, that a later session "must not cite `FIN-409-NOT-ACTIVE`
as pre-existing spec".

**(c) Two ACs name no endpoint.** §3 rule 4 requires every backend TC to cite the `API-*` it
exercises. AC-FIN-044 and AC-FIN-045 are satisfied by onboarding and by migrations, and FIN
publishes no endpoint for either.

## Decision

1. **The frontend track's denominator is the ACs a screen can exercise — 40 of 46.** The six
   outside it are listed in the frontend plan's header, each beside the backend `TC-*` that does
   cover it, and the coverage block states the module-wide figure of 46/46 alongside the in-track
   40/40. They are recorded as out-of-track, never as ✗. The backend track's denominator stays the
   full 46, and covers all of them.

2. **No TC is written for behaviour no AC states.** Those paths are cited only where an AC's own
   Given needs them as setup: API-FIN-035 is the only way AC-FIN-021's inactive dimension value is
   reachable, and API-FIN-034 is one of two ways AC-FIN-013's ruleless event type is reached. Both
   test plans carry an explicit "catalog rows no TC exercises" list naming every such code, so the
   absence is legible rather than silent. `FIN-403-SOD-VIOLATION` is treated differently again: it
   is not merely uncovered but **struck**, and TC-FIN-038 asserts the permission gate while
   explicitly asserting nothing about global user-set disjointness.

3. **AC-FIN-044 and AC-FIN-045 get a TC with an empty `Exercises` binding**, reading
   "— deployment-time onboarding; FIN publishes no endpoint for it", rather than being bound to a
   foreign module's endpoint or skipped. Their `traces=` carries the AC and the REQ, which is what
   the contract requires; the `API-*` half of §3 rule 4 has nothing to cite.

4. **Where an AC's REQ maps to several endpoints, the TC binds to the one the AC's own When
   describes**, and names the others only when the AC's Then reaches them — AC-FIN-003 exercises
   API-FIN-004 and then API-FIN-019, because its Then says "REQ-FIN-019 subsequently rejects any
   posting to it".

## Consequences

- `AC covered` reads 40/40 on the frontend track and 46/46 on the backend track; no ✗ appears in
  either, and the module-wide claim of 46/46 is true rather than arithmetic sleight of hand.
- `API covered` reads 26/37 on the backend track. The eleven endpoints without a TC are each named
  in the API → TC index with their reason. That number is expected to stay below 37 until a
  requirement exists for the as-built additions; a later reader should not treat it as a defect in
  this plan.
- Nothing in FIN changes: no REQ, AC, RULE, API, screen or catalog row is added, removed or
  reworded by this decision. It records what the test plans do and do not claim.
- The honest gap this exposes is upstream, not here: `FIN-409-NOT-ACTIVE`, RULE-FIN-017's two 400s
  and the deactivate endpoints are delivered behaviour with no acceptance criterion. Writing those
  ACs is a P1 act; until then this engine cannot cover them without fabricating requirements.
