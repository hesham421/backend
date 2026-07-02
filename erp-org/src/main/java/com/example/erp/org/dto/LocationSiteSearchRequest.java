package com.example.erp.org.dto;

import com.example.erp.common.dto.BaseSearchContractRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Search request for Location Site - طلب بحث موقع العمل")
public class LocationSiteSearchRequest extends BaseSearchContractRequest {
    // Allowed filters: locationSiteCode, nameAr, branch.id, siteTypeId, isActive (QR-ORG-019)
}
