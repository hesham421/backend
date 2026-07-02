package com.example.erp.org.dto;

import com.example.erp.common.dto.BaseSearchContractRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Search request for Region - طلب بحث المنطقة")
public class RegionSearchRequest extends BaseSearchContractRequest {
    // Allowed filters: regionCode, nameAr, nameEn, legalEntity.id, regionType.id, isActive (API-ORG-014)
}
