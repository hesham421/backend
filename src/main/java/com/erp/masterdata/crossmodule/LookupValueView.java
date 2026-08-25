package com.erp.masterdata.crossmodule;

/**
 * Narrow read model returned by {@link MasterDataLookupApi} — deliberately not
 * {@code LookupValueResponse} (the internal DTO), so a consuming module never gains a
 * compile-time dependency on erp-masterdata's internal shape, only on this contract.
 */
public record LookupValueView(String code) {
}
