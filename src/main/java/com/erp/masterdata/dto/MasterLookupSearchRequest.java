package com.erp.masterdata.dto;

import com.erp.common.dto.BaseSearchContractRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * API contract DTO for Master Lookup search — matches api-contracts/master-lookup.contract.md's
 * {@code {filters, sorts, page, size}} shape.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MasterLookupSearchRequest extends BaseSearchContractRequest {
}
