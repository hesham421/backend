package com.erp.org.crossmodule;

import java.util.Optional;

/**
 * Cross-module read surface for erp-org's branch data (mirrors {@code GET
 * /api/v1/org/branches/{id}}, API-ORG-012). Injected directly by other modules in the same
 * JVM — see governance/.github/skills/backend/create-service/SKILL.md's "Cross-Module Calls
 * (XM)" section. This is the ONLY erp-org surface another module may depend on; never inject
 * {@code BranchService} or any other internal class directly.
 *
 * <p>Known, pre-existing gap carried over unchanged from the old REST-loopback client: the
 * underlying lookup requires {@code BRANCH_VIEW}, and the calling principal may not hold it
 * (see {@code OrgBranchApiService}). This migration does not fix or worsen that.
 */
public interface OrgBranchApi {

    /** Empty if the branch doesn't exist OR the caller lacks {@code BRANCH_VIEW} — the caller
     * decides what "unresolvable" means for its own validation, this method does not. */
    Optional<OrgBranchView> findBranch(Long branchId);
}
