package com.erp.masterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Cacheable wrapper for lookup consumption — distinguishes three states without storing {@code null} (which Redis
 * can't cache): not-found (null), inactive master ({@code values=[]}), active master ({@code values=[...]}). Fully
 * Jackson-serializable for Redis round-tripping.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LookupCacheEntry {

    /** {@code true} when the master lookup is inactive. */
    private boolean inactive;

    /** Active detail values; may be empty for active masters with no details. */
    private List<LookupValueResponse> values;
}
