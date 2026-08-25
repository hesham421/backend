package com.erp.org.crossmodule;

/**
 * Narrow read model returned by {@link OrgBranchApi} — deliberately not {@code BranchResponse}
 * (the internal DTO), so a consuming module never gains a compile-time dependency on erp-org's
 * internal shape, only on this contract.
 */
public record OrgBranchView(Long id, boolean active) {
}
