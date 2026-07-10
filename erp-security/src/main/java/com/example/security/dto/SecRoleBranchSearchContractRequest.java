package com.example.security.dto;

import com.example.erp.common.dto.BaseSearchContractRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * API Contract Request DTO for SEC_ROLE_BRANCH search.
 *
 * Allowed filter/sort fields: roleIdFk, branchIdFk, dataAccessLevel (per
 * execution-plan-SEC-gaps.md Section 2, PHASE CORE).
 *
 * @author ERP Team
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SecRoleBranchSearchContractRequest extends BaseSearchContractRequest {
}
