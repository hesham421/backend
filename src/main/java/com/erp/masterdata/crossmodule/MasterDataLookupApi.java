package com.erp.masterdata.crossmodule;

import java.util.List;

/**
 * Cross-module read surface for erp-masterdata's lookup consumption (mirrors {@code GET
 * /api/lookups/{lookupCode}}). The ONLY erp-masterdata surface another module may depend on —
 * never inject {@code LookupConsumptionService} directly.
 */
public interface MasterDataLookupApi {

    /**
     * Active lookup values for {@code lookupCode}, ordered per erp-masterdata's own rules.
     * Returns an empty list if the lookup code doesn't exist or has no active values — the
     * caller decides what an empty result means for its own validation, this method does not.
     */
    List<LookupValueView> getActiveValues(String lookupCode);
}
