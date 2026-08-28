package com.erp.security.dto;

import com.erp.common.dto.BaseSearchContractRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Search contract for Permission: filters on name (EQUALS/CONTAINS/STARTS_WITH) and module (EQUALS).
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PermissionSearchContractRequest extends BaseSearchContractRequest {
}
