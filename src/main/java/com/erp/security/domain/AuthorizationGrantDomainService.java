package com.erp.security.domain;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.security.exception.SecErrorCodes;

/**
 * Domain service for the two-tier RBAC grant invariants (RULE-SEC-013 / RULE-SEC-014). Pure
 * decision logic — no Spring, no JPA, no repository, no DB access. The service layer resolves the
 * required existence facts (does the role hold the page's module? does the role still hold screen
 * permissions in the module being revoked?) and passes them in as plain booleans; this class only
 * decides. Spans Role, RoleModule, RolePermission, Permission and Page, so it is a domain service
 * rather than an entity-bound Domain companion (contract A.0.7).
 *
 * RULE-SEC-013 (Tier-1): a Role→Module grant is a dashboard display filter AND a prerequisite for
 * any Tier-2 screen-permission grant within that module — the prerequisite is enforced by
 * {@link #assertScreenPermissionGrantAllowed(boolean)}.
 * RULE-SEC-014 (no orphan screen permission): the same prerequisite on grant, plus the mirror
 * constraint on revoke — enforced by {@link #assertModuleRevokeAllowed(boolean)}.
 */
public final class AuthorizationGrantDomainService {

    private AuthorizationGrantDomainService() {
        throw new UnsupportedOperationException("Domain service — cannot be instantiated");
    }

    /**
     * RULE-SEC-014 grant precondition (RULE-SEC-013 prerequisite): a role may hold a page's screen
     * permission only if it also holds that page's module. {@code roleHoldsPageModule} is the
     * existence of the role's SEC_ROLE_MODULE grant for the module of the permission's page,
     * resolved by the service (SEC_PERMISSION.PAGE_FK → SEC_PAGE.MODULE_FK). Violation is an
     * invariant breach (no orphan screen permission), not a missing referenced record → 422.
     */
    public static void assertScreenPermissionGrantAllowed(boolean roleHoldsPageModule) {
        if (!roleHoldsPageModule) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION,
                SecErrorCodes.ROLE_PERMISSION_MODULE_NOT_GRANTED);
        }
    }

    /**
     * RULE-SEC-014 mirror on revoke: revoking a module from a role is blocked while the role still
     * holds screen permissions belonging to that module — revoking would leave them orphaned.
     * {@code roleStillHoldsScreenPermissionsInModule} is resolved by the service. Blocked because a
     * dependent record exists → 409.
     */
    public static void assertModuleRevokeAllowed(boolean roleStillHoldsScreenPermissionsInModule) {
        if (roleStillHoldsScreenPermissionsInModule) {
            throw new LocalizedException(Status.CONFLICT,
                SecErrorCodes.ROLE_MODULE_REVOKE_HAS_DEPENDENTS);
        }
    }
}
