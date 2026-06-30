# /project:execute

Execute the current phase for the specified module.

## Usage
/project:execute [MODULE]

## Execution Steps

1. Read `governance-repo/modules/$ARGUMENTS/execution-state.json`
2. Identify `current_phase` and `current_sub`
3. Check how many subs in `current_phase` have status `PENDING` (including `current_sub`)
4. **If more than one sub is PENDING in this phase**, ask the user before
   executing anything:
   ```
   Phase [current_phase] has [N] pending subs: [list sub IDs in order]
   How do you want to execute them?
     1) Run all of them now, automatically, one after another, without stopping
     2) Run only [current_sub] now, then stop and wait for the next /project:execute
   ```
   Wait for the user's choice (1 or 2) before proceeding. Do not assume.
   - If the user picks (1): set an internal flag `auto_continue = true` for
     this run and proceed through all remaining PENDING subs in this phase
     without re-asking after each one.
   - If the user picks (2): proceed with `current_sub` only, as in the
     single-sub flow below, and stop after it completes.
   - If only one sub is PENDING in the phase, skip this question entirely —
     proceed directly to step 5.
5. Build the file path:
   - If current_sub exists:
     `governance-repo/modules/$ARGUMENTS/packages/execution/[current_phase]/[current_sub].md`
   - If no sub (phase has only index.md):
     `governance-repo/modules/$ARGUMENTS/packages/execution/[current_phase]/index.md`
6. Read the file completely before writing any code
7. Follow the Phase Execution Protocol in CLAUDE.md
8. After completing the sub:
   - Mark sub as COMPLETE in execution-state.json
   - Advance current_sub to next PENDING sub in same phase
   - If no more subs → mark phase COMPLETE, advance current_phase
9. **If `auto_continue = true` and another PENDING sub exists in the same
   phase**, go back to step 5 immediately for the next sub — do not stop,
   do not re-ask. Repeat until no PENDING subs remain in this phase, then
   proceed to step 10.
   **If `auto_continue` is not set** (single-sub flow, or user picked option 2),
   stop here after this one sub.
10. Print completion report (format defined in CLAUDE.md). If multiple subs
    were run via `auto_continue`, list all of them in the report, not just
    the last one.

## Phase Map for Module: ORG

```
CORE        → CORE
DATA-DOM    → DATA-DOM-CORE-ENTITIES
SVC-API     → SVC-API-CRUD, SVC-API-TREE
DOC         → DOC
INT-C       → INT-C
INT-R       → INT-R
F1          → F1-SCR-ORG-001, F1-SCR-ORG-002, F1-SCR-ORG-003, F1-SCR-ORG-004, F1-SCR-ORG-005, F1-SCR-ORG-006, F1-SCR-ORG-007
F2          → F2-SCR-ORG-001, F2-SCR-ORG-002, F2-SCR-ORG-003, F2-SCR-ORG-004, F2-SCR-ORG-005, F2-SCR-ORG-006, F2-SCR-ORG-007
F3          → F3-SCR-ORG-001, F3-SCR-ORG-002, F3-SCR-ORG-003, F3-SCR-ORG-004, F3-SCR-ORG-005, F3-SCR-ORG-006, F3-SCR-ORG-007
SEC         → SEC
ALIGN       → ALIGN
```

## Constraints
- Never execute a phase not listed in execution-state.json
- Never skip a sub — every PENDING sub must eventually run, in order
- When a phase has multiple PENDING subs, always ask the execution-mode
  question (step 4) before running anything — never assume auto-run or
  single-sub by default
- Never advance phase without explicit user instruction
- Always update execution-state.json after every sub, even mid auto_continue
- auto_continue applies only within the current phase — never carries over
  to the next phase automatically
