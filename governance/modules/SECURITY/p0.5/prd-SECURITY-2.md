══════════════════════════════════════════════════════════════════
⚠ RETROFIT MODE — reconstructed from existing implemented code
══════════════════════════════════════════════════════════════════
Module              : SECURITY
Source              : extracted-facts-SECURITY.md, SECTION LAYER 0.5
                       + backend.zip (direct code verification, 2026-08-23)
This artifact DOCUMENTS an already-implemented backend module. It is
NOT a forward design document — content reflects what the code
currently does, not necessarily what was originally intended. Every
US-ID traces to a real controller method; items re-verified directly
against source code are marked as such below.
══════════════════════════════════════════════════════════════════

# PRD — Security & Access Control (SEC)
══════════════════════════════════════════════════════════════════
Module          : Security & Access Control (SEC prefix)
Source artifacts: extracted-facts-SECURITY.md, SECTION LAYER 0.5
                  (Reverse-Engineering Extraction Agent); corrected
                  and re-verified against backend.zip, 2026-08-23
Status          : DRAFT — awaiting Reconciliation Gate (Project 2.5)
══════════════════════════════════════════════════════════════════

**Reverse-inference caveat (verbatim, carried forward per retrofit protocol):**
> This is a REVERSE-inferred product-intent section. It describes what
> the code currently DOES, not what was originally INTENDED. Treat every
> "user story" here as a hypothesis to confirm with a human familiar with
> the module's original business requirements — not as a confirmed PRD.

## USER STORIES

### Authentication & self-service (US-SEC-001 – 008)

US-SEC-001
  Story    : As an unauthenticated user, I need to log in with a
             username/password and receive a JWT access token (plus a
             refresh-token cookie)
  Priority : —
  Success metric : —
  Source   : AuthController.login()
  Status   : DRAFT

US-SEC-002
  Story    : As a client app, I need a single login call that also
             returns my full user info (roles, permissions) instead of
             just a token
  Priority : —
  Success metric : —
  Source   : AuthController.loginWithToken()
  Status   : DRAFT

US-SEC-003
  Story    : As a logged-in user, I need to refresh my access token
             using my refresh cookie without re-entering credentials
  Priority : —
  Success metric : —
  Source   : AuthController.refresh()
  Status   : DRAFT

US-SEC-004
  Story    : As a logged-in user, I need to log out and have my tokens
             invalidated
  Priority : —
  Success metric : —
  Source   : AuthController.logout()
  Status   : DRAFT

US-SEC-005
  Story    : As a prospective user, I need to self-register an account
             that starts disabled until I confirm it
  Priority : —
  Success metric : —
  Source   : AuthController.signup()
  Status   : DRAFT

US-SEC-006
  Story    : As a self-registered user, I need to activate my account
             via an emailed token link
  Priority : —
  Success metric : —
  Source   : AuthController.activate()
  Status   : DRAFT

US-SEC-007
  Story    : As a user who forgot my password, I need to request a
             reset without revealing whether my email exists in the
             system
  Priority : —
  Success metric : —
  Source   : AuthController.forgotPassword()
  Status   : DRAFT

US-SEC-008
  Story    : As a user with a valid reset token, I need to set a new
             password
  Priority : —
  Success metric : —
  Source   : AuthController.resetPassword()
  Status   : DRAFT

### Administration — Users, Roles, Permissions, Pages (US-SEC-009 – 013)

US-SEC-009
  Story    : As an administrator, I need to create, list, search,
             update, and delete user accounts and assign roles to them
  Priority : —
  Success metric : —
  Source   : UserController (create, all, search, assignRoles,
             getUserRoles, delete, update)
  Status   : DRAFT

US-SEC-010 [SOURCE CORRECTED — 2026-08-23]
  Story    : As an administrator, I need to create, search, update,
             and delete roles, and activate or deactivate them
  Priority : —
  Success metric : —
  Source   : RoleController.activateRole() (PUT /{roleId}/activate) /
             RoleController.deactivateRole() (PUT /{roleId}/deactivate)
             — verified directly against backend.zip,
             RoleController.java:115-131.
  Correction note : The original extraction (and this PRD's first
             draft) cited a single "toggleRoleActive" method / a
             "/toggle-active" endpoint. Neither exists anywhere in
             erp-security. Confirmed against the module's own
             governance skill file (enforce-backend-contract/SKILL.md),
             which codifies separate activate/deactivate endpoints as
             the required convention — RoleController correctly
             follows it; only the stale citation was wrong.
  Status   : DRAFT

US-SEC-011
  Story    : As an administrator, I need to assign UI pages to a role
             with specific CRUD permissions, view a role's
             page/permission matrix, remove a page from a role, and
             copy one role's page permissions onto another
  Priority : —
  Success metric : —
  Source   : RoleController (getRolePages, addPageToRole, syncRolePages,
             removePageFromRole, copyFromRole)
  Status   : DRAFT

US-SEC-012
  Story    : As an administrator, I need to register new UI
             pages/screens (which auto-generates their 4 CRUD
             permission records), and update, deactivate, or
             reactivate them
  Priority : —
  Success metric : —
  Source   : PageController (createPage..reactivatePage)
  Status   : DRAFT

US-SEC-013
  Story    : As an administrator, I need to create/search/update raw
             permission records directly
  Priority : —
  Success metric : —
  Source   : PermissionController
  Status   : DRAFT

### Menu (US-SEC-014 – 015)

US-SEC-014
  Story    : As a logged-in user, I need a menu tree built from only
             the pages I have VIEW permission for
  Priority : —
  Success metric : —
  Source   : MenuController.getUserMenu()
  Status   : DRAFT

US-SEC-015
  Story    : As an administrator, I need to view any specific user's
             menu for debugging permission issues
  Priority : —
  Success metric : —
  Source   : MenuController.getUserMenuById()
  Status   : DRAFT

### DataScope (US-SEC-016 – 017)

US-SEC-016
  Story    : As an administrator, I need to scope a role's data access
             to specific branches with a data-access level
             (branch-only / branch-and-children / all)
  Priority : —
  Success metric : —
  Source   : SecRoleBranchController (create..delete)
  Status   : DRAFT

US-SEC-017
  Story    : As an administrator, I need to attach a branch/profile
             (full name, preferred language, employee link) to a user
             account
  Priority : —
  Success metric : —
  Source   : SecUserProfileController (create..update)
  Status   : DRAFT

### Cross-module integration (US-SEC-018 — added 2026-08-23)

US-SEC-018 [REWORDED — attribution corrected]
  Story    : As a self-registering or password-resetting user (both
             anonymous flows), I need my activation/reset email to
             actually be dispatched, not just an internal event
             published with no guaranteed consumer
  Priority : —
  Success metric : —
  Source   : service/AuthEventListener.java, client/NotificationClient.java
             — verified directly against backend.zip, 2026-08-23
  Note     : This need was already implicit in US-SEC-005/006/007/008;
             this entry makes the delivery guarantee explicit now that
             it is confirmed end-to-end in code. This story does NOT
             itself resolve AQ-SEC-001 — that closure is recorded in
             module-registry-SECURITY.md (owned by Project 0). This
             entry only cross-references it.
  Status   : DRAFT

## OPEN ITEMS (ambiguous, not yet a story)

  ? Priority ranking across all 18 stories — code encodes no business
    priority; requires direct human input.
  ? Whether the same delivery-confirmation pattern (US-SEC-018) should
    also apply once FILESVC integration (if any) is ever added — not
    applicable today, no FILESVC dependency detected.

══════════════════════════════════════════════════════════════════
*End of prd-SECURITY.md*
*Next stage: Project 2.5 (UI/UX Design Engine) may draft from this
 file alone, in parallel with Project 1 (SRS). CONTRACT-10's hard gate
 remains suspended for this retrofit session only.*
══════════════════════════════════════════════════════════════════
