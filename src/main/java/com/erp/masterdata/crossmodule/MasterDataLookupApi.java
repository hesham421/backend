package com.erp.masterdata.crossmodule;

import java.util.List;

/**
 * Cross-module read surface for erp-masterdata's generic lookup-value consumption (mirrors
 * {@code GET /api/lookups/{lookupCode}}, master-registry.md Section 8 "LOOKUP CONSUMPTION
 * RULES"). Injected directly by other modules in the same JVM — see
 * governance/.github/skills/backend/create-service/SKILL.md's "Cross-Module Calls (XM)"
 * section. This is the ONLY erp-masterdata surface another module may depend on; never inject
 * {@code LookupConsumptionService} or any other internal class directly.
 */
public interface MasterDataLookupApi {

    /**
     * Active lookup values for {@code lookupCode}, ordered per erp-masterdata's own rules.
     * Returns an empty list if the lookup code doesn't exist or has no active values — the
     * caller decides what an empty result means for its own validation, this method does not.
     */
    List<LookupValueView> getActiveValues(String lookupCode);
}
