package com.example.erp.org.dto;

import com.example.erp.common.dto.BaseSearchContractRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Search request for Profit Center - طلب بحث مركز الربح")
public class ProfitCenterSearchRequest extends BaseSearchContractRequest {
    // Allowed filters: profitCenterCode, nameAr, legalEntity.id, isActive (QR-ORG-018)
}
