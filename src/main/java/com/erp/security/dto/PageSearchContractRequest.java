package com.erp.security.dto;

import com.erp.common.dto.BaseSearchContractRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Search contract for Page: filters on pageCode/nameAr/nameEn (CONTAINS/STARTS_WITH/EQUALS),
 * module and active (EQUALS).
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PageSearchContractRequest extends BaseSearchContractRequest {
}
