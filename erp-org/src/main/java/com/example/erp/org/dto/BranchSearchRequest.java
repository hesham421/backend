package com.example.erp.org.dto;

import com.example.erp.common.dto.BaseSearchContractRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Search request for Branch - طلب بحث الفرع")
public class BranchSearchRequest extends BaseSearchContractRequest {
    // Allowed filters: branchCode, nameAr, nameEn, legalEntity.id, branchTypeId, isActive (API-ORG-008)
}
