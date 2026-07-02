package com.example.erp.org.dto;

import com.example.erp.common.dto.BaseSearchContractRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Search request for Cost Center - طلب بحث مركز التكلفة")
public class CostCenterSearchRequest extends BaseSearchContractRequest {
    // Allowed filters: branch.id, nameAr, nodeTypeId, costCenterTypeId, isActive (QR-ORG-017)
}
