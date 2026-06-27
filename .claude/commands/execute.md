# /project:execute

Execute the current phase for the specified module.

## Usage
/project:execute [MODULE]

## Execution Steps

1. Read `governance-repo/modules/$ARGUMENTS/execution-state.json`
2. Identify `current_phase` and `current_sub`
3. Build the file path:
   - If current_sub exists:
     `governance-repo/modules/$ARGUMENTS/packages/execution/[current_phase]/[current_sub].md`
   - If no sub (phase has only index.md):
     `governance-repo/modules/$ARGUMENTS/packages/execution/[current_phase]/index.md`
4. Read the file completely before writing any code
5. Follow the Phase Execution Protocol in CLAUDE.md
6. After completing the sub:
   - Mark sub as COMPLETE in execution-state.json
   - Advance current_sub to next PENDING sub in same phase
   - If no more subs → mark phase COMPLETE, advance current_phase
7. Print completion report (format defined in CLAUDE.md)

## Phase Map for Module: erp-org

```
CORE        → CORE
DATA-DOM    → DATA-DOM-MASTER, DATA-DOM-REFERENCE
SVC-API     → SVC-API-CRUD, SVC-API-INT
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
- Never skip a sub — execute all subs in order
- Never advance phase without explicit user instruction
- Always update execution-state.json after every sub
