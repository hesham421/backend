# ADR-FIN-062 — Engine-rule lines are added one per submit from the Detail pane, never batched behind the rule's save
Status      : ACCEPTED
Stage       : P3.2        Module: FIN        Version: v2
Lane        : analysis
Decided     : 2026-09-23
traces      : REQ-FIN-007, REQ-FIN-008, REQ-FIN-049, REQ-FIN-062, SCR-FIN-003

## Context

In v1, SCR-FIN-003's ui-ux-spec put the rule's line collection behind the rule Entry, as a summary row that opens a
second-level Drawer. It never said how many calls the Entry's single save makes.

The published surface makes that save a batch. API-FIN-010 creates a rule with no lines. API-FIN-011 adds exactly one
line per call. The SRS (SCR-REQ-FIN-003 §B4) records that a line has no update and no delete. v2 adds dimension tags
that travel inside the add-line submission and share the line's lifecycle (ADR-FIN-025).

A "save rule with N lines" would therefore be 1 + N ordered calls. If call k fails, the rule already holds lines
1…k−1, and the screen has no way to correct or remove them. This is the partial save that P3.2 engine §A.4 forbids:
"a rejected update cannot leave the record carrying children never saved with it".

## Decision

Each surface on SCR-FIN-003 creates one record and submits once:
- **Rule Entry.** Takes eventTypeCode, nameAr and nameEn. It saves the rule and nothing else.
- **Line entry.** Opened from the Detail pane of an already-saved, selected rule. It saves one line together with its
  dimension tags, which is one API-FIN-011 call. The tags are a summary row that opens a second-level editor. That
  editor returns the tag list to the line's form state and never saves.

A new rule is therefore created first and then given its lines one at a time. The RULE-FIN-003 remainder check stays
server-authoritative. The client pre-empts only what it can know, which is that no second line of the rule carries
the remainder marker.

## Consequences

- SCR-FIN-003's `Composition` line and the F2, F3 and F4 blocks follow this decision. The v1 composition, with
  lines behind the rule Entry, is superseded.
- If the backend ever publishes a rule-with-lines create in one call, this ADR is superseded, and the lines can
  return behind the rule's save as one call.
- The change is non-breaking. It narrows how the screen uses published endpoints and removes no operation.
