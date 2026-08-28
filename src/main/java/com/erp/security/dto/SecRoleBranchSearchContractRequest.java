package com.erp.security.dto;

import com.erp.common.dto.BaseSearchContractRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Search contract for SEC_ROLE_BRANCH: filters/sorts on roleIdFk, branchIdFk, dataAccessLevel.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SecRoleBranchSearchContractRequest extends BaseSearchContractRequest {
}
