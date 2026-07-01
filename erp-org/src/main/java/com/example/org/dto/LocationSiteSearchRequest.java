package com.example.org.dto;

import com.example.erp.common.dto.BaseSearchContractRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Search request for Location Sites - طلب بحث مواقع العمل")
public class LocationSiteSearchRequest extends BaseSearchContractRequest {
}
