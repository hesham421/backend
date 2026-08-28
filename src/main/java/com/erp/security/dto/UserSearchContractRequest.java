package com.erp.security.dto;

import com.erp.common.dto.BaseSearchContractRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Search contract for User: filters on username (EQUALS/CONTAINS/STARTS_WITH), enabled and createdAt (EQUALS).
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserSearchContractRequest extends BaseSearchContractRequest {
}
