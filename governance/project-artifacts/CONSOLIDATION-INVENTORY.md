# Consolidation Inventory — Non-Functional `.md` / `.sql` Artifacts

Working record of the `frontend` + `backend` → `governance-repo/project-artifacts/`
consolidation performed on **2026-07-06**. This file is a permanent record of the
reasoning, not a temporary scratch doc — do not delete it.

Scope excluded: `node_modules/`, `dist/`, `.git/`, `build/`, `.angular/`, `target/`,
`backend/tests/.venv/` (untracked Python venv — confirmed via `git ls-files`, not
part of either repo), and anything under `.github/skills/` (out of scope per task
definition regardless of extension).

**Totals**: 49 files scanned (45 frontend, 4 backend) → **5 KEEP**, **19 MOVE**, **25 AMBIGUOUS**.

---

## Frontend (`frontend/`) — 45 files

### `.md` — KEEP (functional)

| # | Path | Verdict | Evidence |
|---|------|---------|----------|
| 1 | `frontend/CLAUDE.md` | KEEP | Live AI-governance entry point, explicitly out-of-scope for this task per the core rule. Points to `governance-repo/CLAUDE.md`. |
| 2 | `frontend/.github/copilot-instructions.md` | KEEP | Live Copilot governance entry point, parallel to `CLAUDE.md`; content confirmed current (references `governance-repo/.github/skills/frontend/`, matches today's `GOVERNANCE-RULES.md` content map). |

### `.md` — MOVE (artifact) → `migration-audits`

All 13 below are AVELYNQ/design-system Phase 1–4 migration audit/verification/map
documents sitting loose in `frontend/design-system/`. For each, searched the whole
frontend repo (`grep -rl <filename-stem>` across `*.ts .html .json .js .mjs .md`)
for real references. Zero hits in application code (`.ts`/`.html`) for any of them.
The only hits found were other design-system audit docs cross-linking each other —
per the guardrail, "a link from an already-obsolete audit doc to another obsolete
doc does NOT count as a reason to keep either."

| # | Path | Evidence (search performed → result) |
|---|------|---------------------------------------|
| 3 | `design-system/AG-GRID-THEMING-AUDIT.md` | No references found anywhere in repo. |
| 4 | `design-system/AVL-INPUT-NUMBER-COERCION-VERIFICATION.md` | No references found anywhere in repo. |
| 5 | `design-system/FINANCE-MODULE-AUDIT.md` | Referenced only from `design-system/TABLER-VERSION-AUDIT.md` (another audit doc, itself MOVE) — doesn't count. |
| 6 | `design-system/FORM-SUBMIT-AUDIT.md` | Referenced only from `MASTER-DATA-MODULE-AUDIT.md` and `MIGRATION-COMPLETE-SUMMARY.md` (both MOVE) — doesn't count. |
| 7 | `design-system/ICON-MIGRATION-MAP.md` | No references found anywhere in repo. |
| 8 | `design-system/MASTER-DATA-MODULE-AUDIT.md` | No inbound references found anywhere in repo. |
| 9 | `design-system/MIGRATION-COMPLETE-SUMMARY.md` | No inbound references found anywhere in repo (it references others, nothing references it). |
| 10 | `design-system/NGB-USAGE-INVENTORY.md` | Cited in *prose comments only* in `src/app/core/services/confirm-dialog.service.ts:18` and `src/app/theme/shared/shared.module.ts:21` — both describe a past finding ("no consumers found... as of Phase 3's inventory"), not a build/runtime read. Confirmed via direct read of both call sites: plain `// NOTE` / `//` comments, not imports. Per the core rule, a prose mention doesn't count. |
| 11 | `design-system/PHASE-1-EXCEPTIONS.md` | No references found anywhere in repo. |
| 12 | `design-system/SECURITY-MODULE-AUDIT.md` | No references found anywhere in repo. |
| 13 | `design-system/SHARED-COMPONENT-AUDIT.md` | Referenced only from `AVELYNQ-EXTENSIONS-PROPOSAL.md` (MOVE) — doesn't count. |
| 14 | `design-system/TABLER-VERSION-AUDIT.md` | No inbound references found anywhere in repo. |
| 15 | `design-system/TOKEN-MIGRATION-MAP.md` | Referenced only from `MIGRATION-COMPLETE-SUMMARY.md` (MOVE) — doesn't count. |

### `.md` — MOVE (artifact) → `design-decisions`

| # | Path | Evidence |
|---|------|----------|
| 16 | `design-system/AVELYNQ-EXTENSIONS-PROPOSAL.md` | Self-labeled "Status: proposed, pending human design review." Searched whole repo for the filename stem: zero hits in `.ts`/`.html`/`.scss`; only cross-linked from `SHARED-COMPONENT-AUDIT.md` (also MOVE). Distinct from the per-component `.prompt.md` specs it proposes (see AMBIGUOUS section) — this master proposal doc itself is not cited anywhere live. |
| 17 | `src/app/shared/ag-grid/ACTIVE-FILTER-GUIDE.md` | Documents an already-implemented filtering convention. Searched repo for `ACTIVE-FILTER-GUIDE`: zero hits in `.ts`/`.html`/`.json`. Not imported, not read at build/runtime — pure reference documentation sitting in the source tree. |

### `.md` — MOVE (artifact) → `investigation-reports`

| # | Path | Evidence |
|---|------|----------|
| 18 | `src/app/modules/security/role-access/BLUEPRINT-COMPLIANCE-SUMMARY.md` | Self-audit against an external "Blueprint Level 2" spec (`frontend.rules.md`, not present in this repo). Only cross-referenced by the other two role-access docs (also MOVE). Zero references in `.ts`/`.html`. |
| 19 | `src/app/modules/security/role-access/FILE-TUNING-BLUEPRINT-LEVEL-2.md` | Same module; only cross-referenced by the other two role-access docs (also MOVE). Zero references in `.ts`/`.html`. |
| 20 | `src/app/modules/security/role-access/QUICK-REFERENCE.md` | Same module; zero inbound references of any kind found. |

### `.md` — AMBIGUOUS (needs human decision) — AVELYNQ design-system spec bundle

25 files under `design-system/avelynq-source/` and `design-system/avelynq-extensions/`.
See `## Needs Your Decision` at the end of this document for the full reasoning —
summary: these are actively cited **by path, in doc comments, from dozens of
currently-shipped production component/directive files** (e.g.
`avl-button.component.ts:8`, `dialog.service.ts:16`, `overlays.scss:190/242/260`)
as their design spec of record, and `avelynq-source/SKILL.md` self-declares
`user-invocable: true` (an Agent Skill manifest) — but none of it is wired into
any build tool, bundler, CI step, or `.claude/skills/`-discoverable location
(no such directory exists in this repo). Genuinely ambiguous under the letter
of the core rule vs. its spirit.

| # | Path |
|---|------|
| 21 | `design-system/avelynq-extensions/Dropdown.prompt.md` |
| 22 | `design-system/avelynq-extensions/Pagination.prompt.md` |
| 23 | `design-system/avelynq-extensions/Toast.prompt.md` |
| 24 | `design-system/avelynq-extensions/Tooltip.prompt.md` |
| 25 | `design-system/avelynq-extensions/Typeahead.prompt.md` |
| 26 | `design-system/avelynq-source/SKILL.md` |
| 27 | `design-system/avelynq-source/readme.md` |
| 28 | `design-system/avelynq-source/ui_kits/erp/README.md` |
| 29 | `design-system/avelynq-source/uploads/AVELON-Brand-Concept-Summary.md` |
| 30–45 | `design-system/avelynq-source/components/**/*.prompt.md` (16 files: buttons/{Button,IconButton}, data-display/{Avatar,Badge,Card,Stat}, feedback/{Alert,EmptyState}, forms/{Checkbox,Input,Select,Switch}, navigation/{Breadcrumb,Tabs}, overlays/{Dialog,Drawer}) |

### `.sql` in frontend

None found.

---

## Backend (`backend/`) — 4 files

### `.md` — KEEP (functional)

| # | Path | Verdict | Evidence |
|---|------|---------|----------|
| 1 | `backend/CLAUDE.md` | KEEP | Live AI-governance entry point, out of scope per the core rule. |
| 2 | `backend/.github/copilot-instructions.md` | KEEP | Live Copilot governance entry point, content confirmed current. |
| 3 | `backend/erp-security/README.md` | KEEP | Real module-level README for the `erp-security` Maven module (`erp-security/pom.xml` exists — genuine package boundary). Content is current and describes live API endpoints, test counts, and config — not a stale/frozen audit masquerading as a README. |

### `.sql` — MOVE (artifact) → `seed-scripts`

| # | Path | Evidence |
|---|------|----------|
| 4 | `erp-security/src/main/resources/db/scripts/seed_security_data.sql` | Checked every migration-tool angle: no `flyway.conf`/`liquibase.properties` exist anywhere in the repo; grepped `seed_security_data` across all `*.properties`, `*.yml`, `*.xml`, `*.java`, `Dockerfile*`, and `pom.xml` (excluding `/target/` build output) — zero hits. Not mentioned in any CI workflow (`.github/workflows/playwright.yml` doesn't reference it). Not even mentioned in `erp-security/README.md` (which references two *different*, non-existent manual scripts, `admin-setup.sql` and `migrate-to-unified-permissions.sql`, run manually via `sqlplus` — neither is this file, and neither exists in the repo). Confirmed one-off, orphaned seed script with no automated invocation anywhere. |

Note: `backend/tests/.venv/.../LICENSE.md` files (3 files, vendored pip package
licenses) were excluded from the inventory entirely — confirmed via
`git ls-files` that `.venv` is untracked, i.e. not part of the `backend` repo at
all (equivalent to `node_modules`), so there is nothing to move or delete there.

---

## Needs Your Decision

**The AVELYNQ design-system spec bundle** (25 files, listed above under
`design-system/avelynq-source/` and `design-system/avelynq-extensions/`) —
not moved, left in place.

**The uncertainty**: this bundle is simultaneously —

- **Evidence it's a live, functional asset:**
  - `avelynq-source/SKILL.md` has `user-invocable: true` frontmatter — it
    self-declares as a Claude Code Agent Skill ("Use this skill to generate
    well-branded interfaces and assets for AVELYNQ... for production or
    throwaway prototypes"), not a passive document.
  - Dozens of **currently shipping** production files cite specific files in
    this bundle, by path, as their design spec of record: `avl-button.component.ts`,
    `avl-checkbox.component.ts`, `avl-alert.component.ts`, `avl-input.component.ts`,
    `avl-switch.component.ts`, `avl-select.component.ts`, `avl-breadcrumb.component.ts`,
    `avl-icon-button.component.ts`, `avl-empty-state.component.ts`, `avl-tabs.component.ts`,
    `avl-avatar.component.ts`, `avl-stat.component.ts`, `avl-card.component.ts`,
    `avl-badge.component.ts`, `dialog.service.ts`, `drawer.service.ts`,
    `avl-toast-container.component.ts`, `avl-pagination.component.ts`,
    `dropdown.directive.ts`, `tooltip.directive.ts`, `typeahead.directive.ts`,
    and `src/scss/avelynq/shell/overlays.scss`.
  - Several of the `avelynq-extensions/*.prompt.md` files describe themselves as
    "pending human design review" — i.e. active, unresolved work, not closed history.

- **Evidence it's a non-functional artifact under the letter of the core rule:**
  - Every one of the citations above is a **prose comment** (JSDoc `/** ... */` or
    `//` / SCSS `//` comment) — none is a real `import`, `readFileSync`, build-tool
    reference, or anything parsed by Angular/webpack/esbuild at build or runtime.
    The core rule explicitly says a "mention in prose" doesn't qualify as KEEP
    evidence, and I already applied that exact standard to disqualify
    `NGB-USAGE-INVENTORY.md`'s similar comment citations (see MOVE list above).
  - There is no `.claude/skills/` directory anywhere in the `frontend` repo (checked)
    that would make `avelynq-source/SKILL.md` actually discoverable/invocable by
    Claude Code today — as currently laid out, it is inert.

**Why I didn't force a verdict**: applying the "prose doesn't count" standard
consistently would force MOVE on the whole bundle, exactly like `NGB-USAGE-INVENTORY.md`.
But `NGB-USAGE-INVENTORY.md` documents a **closed historical finding** ("no
consumers found... as of Phase 3"), while this bundle is the **current, actively
cited design specification** for ~20 already-shipped components plus one file
that is a real (if currently unwired) Agent Skill manifest. Moving it would sever
every one of those ~20 in-code path references and potentially deactivate a skill
someone intended to wire up. This is a materially different risk profile from a
one-off audit doc, so I'm flagging it rather than guessing.

**Decision needed**: either (a) confirm this bundle counts as "no ongoing
technical role" and should be moved (accepting that ~20 code comments will then
point at a path that no longer exists in this repo — those comments would need
a follow-up cleanup pass), or (b) confirm it should be treated as living
design-system documentation and explicitly whitelisted as KEEP going forward
(and, if intended to actually function as a Claude Code skill, wire it up via a
`.claude/skills/` entry).
