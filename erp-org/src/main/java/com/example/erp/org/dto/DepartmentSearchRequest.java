package com.example.erp.org.dto;

import com.example.erp.common.dto.BaseSearchContractRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Search request for Department - طلب بحث القسم")
public class DepartmentSearchRequest extends BaseSearchContractRequest {
    // Allowed filters: branch.id, nameAr, nodeTypeId, isActive (QR-ORG-014)
}
