package com.example.org.dto;

import com.example.erp.common.dto.BaseSearchContractRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Search request for Region Types - طلب بحث أنواع المناطق")
public class RegionTypeSearchRequest extends BaseSearchContractRequest {
}
