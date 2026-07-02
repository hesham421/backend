package com.example.erp.org.dto;

import com.example.erp.common.dto.BaseSearchContractRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Search request for Legal Entity - طلب بحث الكيان القانوني")
public class LegalEntitySearchRequest extends BaseSearchContractRequest {
    // Allowed filters: legalEntityCode, nameAr, nameEn, entityTypeId, isActive (API-ORG-002)
}
