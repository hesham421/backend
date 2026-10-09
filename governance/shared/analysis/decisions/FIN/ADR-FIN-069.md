# ADR-FIN-069 — What the factory publishes about FIN v2: the registry names v2 and where it is delivered, the profile summary points at it, and a versioned sparse checkout reaches v2
Status      : ACCEPTED (non-breaking)
Stage       : publication (factory tooling)        Module: FIN        Version: v2
Lane        : —
Decided     : 2026-09-24
traces      : SCR-FIN-001

## Context

The frontend recorded three items that no stage of FIN owns. Each one was a fact the factory published, or
failed to publish:

- `platform/modules-registry.json` still read `FIN: versions [1], current_version 1`, last published on
  2026-09-17. By then FIN v2 had been created, split and gate-approved (pass-2, `28ef61a`). Nothing
  re-published after those steps, and no check compared the file with what the factory derives.
- `platform/profile-summary.json` gives `tracks.frontend.delivery = frontend/modules/{MOD}/packages`. That
  path names the partition, not a module's current delivery. v2 is delivered at `packages/v2/`, so a
  consumer that resolved its packages from this key read the superseded v1 packages.
- The consumer's sparse patterns (`analysis/modules/*/P3_2/**`) matched only one path segment. The v2
  plan, spec and gate records were committed but absent from disk. The consumer patched its own
  `.governance-scope`.

## Decision

1. The registry publishes each module's delivered version and package paths, per track:
   `modules.<MOD>.delivery.<track> = {version, exec, test}`. For FIN this is
   `frontend/modules/FIN/packages/v2/frontend-execution`. The profile summary names that key under
   `tracks.<track>.delivery_current`, and names the version folder under `delivery_version_folder`, so no
   consumer composes a delivery path. Published at `e8cdd17`: FIN `versions [1, 2]`, `current_version 2`,
   delivered at v2 on both tracks.
2. Creating a version, splitting and approving a gate each republish, in `gov.py`. A new check, `analyze`
   C9.23 publication-fresh, fails whenever a published copy differs from what the factory derives now.
3. `gov.py sparse --track <t>` emits every module-level pattern twice: once for the base, once under
   `analysis/modules/*/v*/`, plus the version root's change manifest. The consumer's patch becomes the
   factory's own output.

## Consequences

- The frontend's two `HUMAN` layout items and its sparse `LAYOUT` item are answered by the factory. The
  consumer's `generate-frontend-module-setup` and `orchestrate-module` can both resolve `$PKGS` from
  `modules.FIN.delivery.frontend` and stop disagreeing between flat `packages/` and `packages/vN/`.
