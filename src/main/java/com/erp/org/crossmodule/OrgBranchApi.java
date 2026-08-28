package com.erp.org.crossmodule;

import java.util.Optional;

/**
 * Cross-module read surface for erp-org's branch data (API-ORG-012) — the ONLY erp-org surface
 * another module may depend on; never inject {@code BranchService} directly. Known gap: the
 * underlying lookup requires {@code BRANCH_VIEW}, which the calling principal may not hold.
 */
public interface OrgBranchApi {

    /** Empty if the branch doesn't exist OR the caller lacks {@code BRANCH_VIEW} — the caller
     * decides what "unresolvable" means for its own validation, this method does not. */
    Optional<OrgBranchView> findBranch(Long branchId);
}
