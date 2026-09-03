<!-- Source: PHASE:SVC-API / SUB:SVC-API-USERS -->
<!-- Context: see SVC-API-HEADER.md for phase-level strategy, registry table, and intro -->

<!-- API:API-SEC-007:START -->
### API-SEC-007 — Create user
POST /api/v1/security/users | UserController.create → UserService.create
REQUEST UserCreateRequest{username, email, phone?, fullName, preferredLangId} | RESPONSE 201 UserResponse (no passwordHash)
VALIDATIONS: RULE-SEC-002 (required username,email,fullName — Message-AR: حقول الحساب الأساسية إلزامية.);
  RULE-SEC-001 (unique username — Message-AR: اسم المستخدم مستخدَم مسبقاً.)
ERRORS: ERR-0002 → RULE-SEC-002 → 400; ERR-0001 → RULE-SEC-001 dup username → 409; ERR-0010 → dup email → 409
ORCHESTRATION: validate required → EXISTS username (QR-SEC-0005) & email (QR-SEC-0006) → create with userStatusId=PENDING_ACTIVATION → issue activation token (QR-SEC-0021) → publish CU event.
REPO: QR-SEC-0001 SAVE — READ_WRITE — Sequence SEQ_SEC_USER_ACCOUNT
SECURITY: SCR-SEC-001 CREATE (PERM_SEC_USERS_CREATE).
<!-- API:API-SEC-007:END -->
<!-- API:API-SEC-008:START -->
### API-SEC-008 — Search users
GET /api/v1/security/users | UserController.search → UserService.search
REQUEST params: username?(LIKE), email?(LIKE), userStatusId?(EXACT), isActiveFl?(EXACT), page,size,sortBy,sortDir; ALLOWED_SORT_FIELDS={username,email,userStatusId,createdAt}
RESPONSE 200 Page<UserResponse> (empty → 200 [], never 404) | ERRORS: none
REPO: QR-SEC-0003 FIND_BY_CRITERIA — READ_ONLY — Join NONE | SECURITY: SCR-SEC-001 VIEW.
<!-- API:API-SEC-008:END -->
<!-- API:API-SEC-009:START -->
### API-SEC-009 — Update user
PUT /api/v1/security/users/{id} | UserController.update → UserService.update
REQUEST UserUpdateRequest{email,phone?,fullName,preferredLangId,userStatusId,isActiveFl} (username immutable)
RESPONSE 200 UserResponse
VALIDATIONS: RULE-SEC-001 (email uniqueness on change)
ERRORS: ERR-0012 → NOT_FOUND → 404; ERR-0010 → dup email → 409
REPO: QR-SEC-0001 FIND_ONE + QR-SEC-0004 UPDATE — READ_WRITE | SECURITY: SCR-SEC-001 UPDATE.
<!-- API:API-SEC-009:END -->
<!-- API:API-SEC-010:START -->
### API-SEC-010 — Deactivate user (soft)
DELETE /api/v1/security/users/{id} | UserController.deactivate → UserService.deactivate
RESPONSE 200/204
VALIDATIONS: RULE-SEC-012 (allow deactivation; NO cascade to SOFT consumers; history retained; reactivation permitted — Message-AR: يُسمح بإلغاء تنشيط الحساب دون تعاقب؛ تُحفظ المراجع التاريخية ويُسمح بإعادة التنشيط.)
ERRORS: ERR-0012 → NOT_FOUND → 404
ORCHESTRATION: load (QR-SEC-0001) → set isActiveFl=0 & userStatusId=INACTIVE (QR-SEC-0004). No cascade. Consumers (NOTIF RULE-NOTIF-007) block NEW ops at their layer.
REPO: QR-SEC-0004 — READ_WRITE | SECURITY: SCR-SEC-001 DELETE.
<!-- API:API-SEC-010:END -->
<!-- API:API-SEC-012:START -->
### API-SEC-012 — Assign role to user
POST /api/v1/security/users/{id}/roles | UserController.assignRole → UserRoleService.assign
REQUEST {roleId} | RESPONSE 200
VALIDATIONS: existence of user & role (idempotent insert into SEC_USER_ROLE)
ERRORS: ERR-0012 → NOT_FOUND (user or role) → 404
REPO: QR-SEC-0017 SAVE(join) — READ_WRITE | SECURITY: SCR-SEC-001 UPDATE.
<!-- API:API-SEC-012:END -->
