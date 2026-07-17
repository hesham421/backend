# HANDOFF — PHASE 3 (SVC+API) — PLAN-SEC-002

## Controllers/Services/Repos created

All paths relative to `backend/erp-security/src/main/java/com/example/security/` unless noted.

**Repositories (new):**
- `repository/SecUserProfileRepository.java`
- `repository/SecRoleBranchRepository.java` (`existsByRoleIdFkAndBranchIdFk` for RULE-SEC-036 pre-check)
- `repository/PasswordResetTokenRepository.java` (`findByToken`, `findByUser_IdAndUsedFlFalseAndExpiresAtAfter` for RULE-SEC-039)
- `repository/AccountActivationTokenRepository.java` (`findByToken`)
- `repository/UserAccountRepository.java` — **modified**: added `existsByEmailIgnoreCase`, `findByEmailIgnoreCase` (RULE-SEC-041, forgot-password lookup)

**Cross-module HTTP clients (new — see Deviations #6):**
- `config/InternalApiClientConfig.java` — `RestTemplate` bean (3s connect / 5s read timeout)
- `client/OrgBranchClient.java` + `client/OrgBranchLookup.java` — `GET /api/v1/org/branches/{id}` (API-ORG-012) for RULE-SEC-034
- `client/MasterDataLookupClient.java` + `client/LookupValueLookup.java` — `GET /api/lookups/DATA_ACCESS_LEVEL` for RULE-SEC-035 / LOV-SEC-002

**Config properties (new):**
- `config/properties/SelfServiceTokenProperties.java` — registered in `config/properties/SecurityPropertiesConfig.java` (modified)

**DTOs (new):**
- `dto/SecUserProfileDto.java`, `CreateSecUserProfileRequest.java`, `UpdateSecUserProfileRequest.java`, `SecUserProfileSearchContractRequest.java`
- `dto/SecRoleBranchDto.java`, `CreateSecRoleBranchRequest.java`, `UpdateSecRoleBranchRequest.java`, `SecRoleBranchSearchContractRequest.java`
- `dto/SignupRequest.java`, `SignupResponse.java`, `ActivateAccountRequest.java`, `ForgotPasswordRequest.java`, `ResetPasswordRequest.java`

**Mappers (new):**
- `mapper/SecUserProfileMapper.java`, `mapper/SecRoleBranchMapper.java` (`@UtilityClass` static `toDto`, matching `RoleMapper`'s established convention — no `toEntity`, services build entities via `.builder()` directly, same as `RoleService`)

**Services:**
- `service/SecUserProfileService.java` (new) — create/update/getById/listProfiles/search (API-SEC-032..035)
- `service/SecRoleBranchService.java` (new) — create/update/delete/getById/listRoleBranches/search (API-SEC-036..039)
- `service/AuthService.java` — **modified**: added `signup`, `activateAccount`, `forgotPassword`, `resetPassword` (API-SEC-040..043)

**Controllers:**
- `controller/SecUserProfileController.java` (new) — `/api/v1/security/user-profiles`
- `controller/SecRoleBranchController.java` (new) — `/api/v1/security/role-branches`
- `controller/AuthController.java` — **modified**: added `POST /api/auth/signup`, `/signup/activate`, `/forgot-password`, `/reset-password` (already covered by SecurityConfig's existing `/api/auth/**` permitAll — no SecurityConfig change needed)

**Error codes / i18n:**
- `exception/SecurityErrorCodes.java` — **modified**: 11 new constants (see ERR-ID table below)
- `erp-main/src/main/resources/i18n/messages.properties` / `messages_ar.properties` — **modified**: EN+AR entries for all 11

**Events (new):**
- `event/AccountActivationRequestedEvent.java`, `event/PasswordResetRequestedEvent.java`

Verified: full Maven reactor (`mvn -o clean compile`, all 7 modules) — **BUILD SUCCESS**.

## Event mechanism used for RULE-SEC-031

Spring's `ApplicationEventPublisher` (`eventPublisher.publishEvent(...)` in `AuthService.signup()` / `AuthService.forgotPassword()`). Confirmed during Phase 3 research that **no event-publishing mechanism exists anywhere else in this codebase** (no `ApplicationEventPublisher`, `@EventListener`, or `*Event` class in any module) — this is the first one, per the plan's explicit fallback instruction in Section 6.2. No subscriber/listener was added (NotificationService doesn't exist yet — that's INT-C's job); this phase only implements the publish side, which is all Phase 3 requires per the phase file.

## ERR-IDs verified against Section 4.2

| ERR-ID | SecurityErrorCodes constant | EN message (verbatim match) | AR message (verbatim match) |
|---|---|---|---|
| ERR-SEC-1032 | `ACTIVATION_TOKEN_INVALID_OR_EXPIRED` | ✓ "Activation token is invalid or expired" | ✓ |
| ERR-SEC-1033 | `TOKEN_ALREADY_USED` | ✓ "Activation/reset token already used" (shared, used by both activation and reset flows per the catalog's own wording) | ✓ |
| ERR-SEC-1034 | `SEC_USER_PROFILE_BRANCH_INACTIVE` | ✓ "An active branch is required for the user profile" | ✓ |
| ERR-SEC-1035 | `SEC_ROLE_BRANCH_DATA_ACCESS_LEVEL_REQUIRED` | ✓ "Data access level is required" | ✓ |
| ERR-SEC-1036 | `SEC_ROLE_BRANCH_DUPLICATE_ASSIGNMENT` | ✓ "This branch is already assigned to this role" | ✓ |
| ERR-SEC-1040 | `SIGNUP_USERNAME_ALREADY_EXISTS` (new constant, NOT the existing `USERNAME_ALREADY_EXISTS` — see Deviations #7) | ✓ "Username already in use" | ✓ |
| ERR-SEC-1041 | `SIGNUP_EMAIL_ALREADY_EXISTS` | ✓ "Email address already in use" | ✓ |
| ERR-SEC-1043 | `RESET_TOKEN_INVALID_OR_EXPIRED` | ✓ "Reset token is invalid or expired" | ✓ |
| ERR-SEC-1030, 1039 | — (internal, no HTTP-facing message per Section 4.2 — "not user-facing" / "silent invalidation") | n/a | n/a |
| RULE-SEC-038 | — (deliberately no ERR-ID, per Section 4.2's own note) | n/a | n/a |

## RULE-SEC-038 anti-enumeration verified how

`AuthService.forgotPassword()` has exactly one exit path: it always logs and returns `void` with no exception and no conditional response. The `userAccountRepo.findByEmailIgnoreCase(...).ifPresent(...)` lambda only produces *internal* side effects (token invalidation/issuance, event publish) when the email exists — it never returns a value or throws, so nothing downstream (controller, HTTP status, response body) can branch on whether the `Optional` was present. `AuthController.forgotPassword()` always returns `200 OK` with an empty body regardless. No try/catch is involved at all (there's nothing that throws in this path), so there's no risk of a caught-vs-uncaught-exception timing/response difference either.

## Endpoints NOT yet permission-gated (expected — Phase SEC)

Confirmed. `SecUserProfileService` and `SecRoleBranchService` have **no `@PreAuthorize`** on any method (deliberately, per the phase file's Definition of Done item 5 and Requirement 4 — "do not add permission annotations yet"). Both new controllers' endpoints sit under `/api/v1/security/**`, which is **not** in `SecurityConfig`'s `permitAll` list, so they still require a valid JWT (any authenticated user) via the existing `JwtAuthenticationFilter` — they are just not yet restricted to a specific permission. Phase SEC (Section 8.1 Permissions Matrix) owns adding the `SecurityPermissions` constants and `@PreAuthorize` annotations.

## Deviations from plan Section 4 (and other governed sections)

1. **Governance conflict surfaced before any code was written (resolved by explicit user confirmation, not silently):** `master-registry.md` Section 13/15 lists Conflict #19 (Security's extension-scope development still **OPEN**, no architect sign-off) and Conflict #20/BLK-SEC-002 (**OPEN**, Security↔NotificationService dependency cycle) as blocking, while `execution-plan-SEC-gaps.md` Section 6.2 asserts Conflict #20 is CLOSED via the event-publish pattern. I stopped and asked before writing any file. User confirmed: (a) proceed despite Conflict #19 remaining OPEN in the registry, (b) trust the execution-plan's event-based resolution for Conflict #20. **This still needs a real registry update by the Registry Builder / architect sign-off outside this session** — master-registry.md Section 13/15 was NOT edited by this phase (out of scope; only P0 REGISTRY UPDATE BLOCKS may do that per its own Section 13 rule).
2. **RULE-SEC-029 gap in the plan itself:** API-SEC-040's RULE-IDs column (Section 4.1) cites RULE-SEC-029, but Section 1.3's "full extraction" only defines RULE-SEC-030..041 — RULE-SEC-029 is never defined anywhere in execution-plan-SEC-gaps.md. Not silently ignored: signup's `SignupRequest` DTO carries standard `@NotBlank`/`@Size`/`@Email` validation as a reasonable placeholder, but no specific rule was implemented under that ID since its content is unknown. Flagging for Phase ALIGN / the plan author to resolve.
3. **API-SEC-038/039 path shape adapted:** the API register literally shows `PUT/DELETE .../role-branches/{id}`, but `SEC_ROLE_BRANCH` has no surrogate PK (composite `(roleIdFk, branchIdFk)` per Section 3 / db-script-SEC-gaps.md BLOCK 5a) — inventing a synthetic `id` column is prohibited by the high-precision rules. Implemented as `.../role-branches/{roleId}/{branchId}` instead.
4. **Extra `POST /search` endpoints added beyond the literal API register:** API-SEC-033/037 are listed as plain `GET` in Section 4.1, and that's implemented (paged list + `ALLOWED_SORT_FIELDS`, mirroring `UserController`'s existing `GET /api/users`). Additionally added `POST .../search` on both new controllers, matching `UserController`/`RoleController`'s existing side-by-side convention and Requirement 5's explicit `BaseSearchContractRequest` mandate — not a new pattern, just applying an existing one this plan didn't explicitly list.
5. **No `<Entity>Domain` companion objects for `SecUserProfile`/`SecRoleBranch`:** RULE-SEC-034/035/036 are enforced directly in the Service layer (`SecUserProfileService`, `SecRoleBranchService`), not via a dedicated Domain object. This matches execution-plan-SEC-gaps.md Section 3's own explicit assignment ("enforced at Service layer") for these exact RULE-IDs, and Security has zero Domain-object precedent anywhere in the module (confirmed: no `*Domain.java` file exists under `erp-security`, unlike `erp-org`'s `OrgBranchDomain` etc.) — consistent with Security's PERMANENT EXCEPTION status predating the Domain Layer convention.
6. **New cross-module HTTP client infrastructure (`OrgBranchClient`, `MasterDataLookupClient`, `RestTemplate` bean):** this is the **first** `RestTemplate`/`WebClient`/HTTP-client usage anywhere in the backend — confirmed no precedent exists in any module. Both clients call same-JVM endpoints (`http://localhost:${server.port}`) per XM-SEC-001/XM-SEC-002 and the LOOKUP CONSUMPTION RULES (master-registry.md Section 8). The target endpoints (`GET /api/v1/org/branches/{id}`, `GET /api/lookups/{lookupCode}`) are authenticated + permission-gated, and there is no service-to-service credential anywhere in this codebase — both clients forward the **caller's own incoming `Authorization` header** as a pragmatic stopgap. This means creating/updating a `SEC_USER_PROFILE` or `SEC_ROLE_BRANCH` requires the calling user to also hold `BRANCH_VIEW` (Organization) permission, which is not guaranteed by any documented RBAC design — flagging for architecture review, ideally resolved by a proper service-to-service credential before Phase SEC finalizes the permission matrix.
7. **`SIGNUP_USERNAME_ALREADY_EXISTS` added as a new constant instead of reusing `USERNAME_ALREADY_EXISTS`:** the existing constant's message ("Username already exists in tenant: {0}") is bound to the admin-facing `CreateUserRequest` flow and differs verbatim from ERR-SEC-1040's plan-mandated text ("Username already in use") — reusing it would have either broken the existing message or violated the "match Section 4.2 verbatim" rule. Added a new constant instead.
8. **Token TTLs (`SelfServiceTokenProperties`: 24h activation / 1h reset) and token format (`UUID.randomUUID()`, reusing `RefreshToken`'s existing pattern):** both are explicitly agent implementation details per Section 3 ("generation mechanism is an agent implementation detail — not specified by SRS"), not plan-mandated values.
9. **Baseline (non-RULE-ID) error codes added:** `SEC_USER_PROFILE_NOT_FOUND`, `SEC_USER_PROFILE_ALREADY_EXISTS`, `SEC_ROLE_BRANCH_NOT_FOUND` — ordinary CRUD errors not tied to a Section 4.2 ERR-ID (that catalog only lists new gap-specific rule-derived errors), same convention as the pre-existing `USER_NOT_FOUND`/`ROLE_NOT_FOUND`.

## Ready for Phase 4 (DOC)? yes
