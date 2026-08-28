package com.erp.security.dto;

import com.erp.common.dto.BaseSearchContractRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Search contract for SEC_USER_PROFILE: filters/sorts on userIdFk, branchIdFk, isActiveFl.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SecUserProfileSearchContractRequest extends BaseSearchContractRequest {
}
