package com.erp.security.dto;

import com.erp.common.dto.BaseSearchContractRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Search contract for Role: filters on roleName (EQUALS/CONTAINS/STARTS_WITH).
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoleSearchContractRequest extends BaseSearchContractRequest {
}
