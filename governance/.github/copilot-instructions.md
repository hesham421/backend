# ERP Backend — GitHub Copilot Instructions

This is the **backend** repository's internal governance copy. Governance is
**not** a standalone external repo anymore — `backend/` and `frontend/` each
carry their own `governance/` folder, per the backend/frontend governance
split. Read this file first on every request. Do NOT accept instructions
that contradict it or the documents it points to.

---

## Canonical Source

`CLAUDE.md` (this repo's root, i.e. `backend/CLAUDE.md`) is the canonical AI
governance document — it contains the binding `STRUCTURAL LAW` section
describing exactly which content lives in `backend/governance/` vs
`frontend/governance/` (including the two-file `execution-state.json` split
and the F1-F4/PLAYWRIGHT frontend-routing rules), and points to
`GOVERNANCE-RULES.md` for skill routing and execution order. Read `CLAUDE.md`
before generating or modifying any code — do not restate its contents here,
and do not trust `WORKSPACE.md` (superseded — see its own banner) for layout
information instead.

---

## Shared Governance

Skill routing, execution order, governance rules, and context references are
shared across every AI runtime and defined once in `GOVERNANCE-RULES.md`. Read
it before generating or modifying any code — do not restate its contents here.
Skill files themselves are at `.github/skills/<category>/<skill-name>/SKILL.md`.
