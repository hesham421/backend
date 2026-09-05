# PRD — Master Data (MDM)
══════════════════════════════════════════════════════════════════
Module          : Master Data (MDM prefix)
Source artifacts: platform-summary.md, module-registry-MDM.md,
                  business-policies-MDM.md
Status          : DRAFT — awaiting Reconciliation Gate (Project 2.5)
Date            : 2026-09-04
══════════════════════════════════════════════════════════════════

## USER STORIES

US-MDM-001
  Story    : As a catalog administrator, I need to define and maintain the
             categories of shared value lists (lookup types) used across the
             platform, so that shared reference data is organized under
             clear, named categories.
  Priority : —
  Success metric : —
  Source   : module-registry-MDM.md §ENTITIES OWNED (LookupType)
  Status   : DRAFT

US-MDM-002
  Story    : As a catalog administrator, I need to add and maintain the
             member values that belong to each lookup category, so that
             every shared value list has its correct, up-to-date set of
             options.
  Priority : —
  Success metric : —
  Source   : module-registry-MDM.md §ENTITIES OWNED (LookupValue)
  Status   : DRAFT

US-MDM-003
  Story    : As a module on the platform (current or future) that needs a
             shared value list, I need to obtain that list from the central
             Master Data catalog, so that the same reference data is not
             duplicated as a separate local list inside my own module.
  Priority : —
  Success metric : —
  Source   : business-policies-MDM.md §CLIENT-SPECIFIC POLICIES (POLICY-CLI-01);
             platform-summary.md §MODULES (row 1.5 — MDM as the platform-wide
             mandatory reference)
  Status   : DRAFT

## OPEN ITEMS (ambiguous, not yet a story)

  ? Management surface — the Project 0 inputs do not state whether lookup
    categories and values are maintained through a runtime administrator
    screen or seeded / managed at deployment. A P1 / P2.5 decision; flagged
    here, not written into a story.

  ? Initial catalog content — seeding MDM with the value lists that currently
    live locally in FILE (FileType / FileStatus) and NOTIF
    (NotificationChannel / NotificationStatus) is explicitly OUT of MDM's own
    scope (recorded as INTEGRATION CANDIDATES in module-registry-MDM.md). The
    consumer-side migration stories belong to those separate, governed
    amendment sessions — not to this PRD.

══════════════════════════════════════════════════════════════════
*End of prd-MDM.md*
*Next stage: Project 2.5 (UI/UX Design Engine) — requires this file AND
 srs.md together (CONTRACT-11). Does not gate Project 1.*
══════════════════════════════════════════════════════════════════
