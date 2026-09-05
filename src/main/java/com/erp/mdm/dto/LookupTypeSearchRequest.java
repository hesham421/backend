package com.erp.mdm.dto;

import com.erp.common.dto.BaseSearchContractRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * API-MDM-002 search request (POST /lookup-types/search). LookupType is a flat/root reference
 * entity, so there is no parent-id extractor override; filtering on typeCode/nameAr/nameEn (LIKE)
 * and isActive (EXACT) flows through the inherited generic filters consumed by the shared
 * SpecBuilder. No {@code @AllArgsConstructor} — the class adds zero fields, so it would collide with
 * the no-arg constructor; {@code @SuperBuilder} still provides full construction.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Search request for LookupType - طلب بحث أنواع القوائم المرجعية")
public class LookupTypeSearchRequest extends BaseSearchContractRequest {
}
