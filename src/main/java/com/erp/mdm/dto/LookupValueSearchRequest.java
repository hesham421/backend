package com.erp.mdm.dto;

import com.erp.common.dto.BaseSearchContractRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * API-MDM-007 search request (POST /lookup-types/{typeId}/values/search). Parent scoping comes from
 * the path {typeId} applied in the service — NOT a body filter — so this stays flat with no
 * parent-id extractor override; filtering on valueCode/nameAr/nameEn (LIKE) and isActive (EXACT)
 * flows through the inherited generic filters consumed by the shared SpecBuilder. No
 * {@code @AllArgsConstructor} — the class adds zero fields, so it would collide with the no-arg
 * constructor; {@code @SuperBuilder} still provides full construction.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Search request for LookupValue - طلب بحث قيم القوائم المرجعية")
public class LookupValueSearchRequest extends BaseSearchContractRequest {
}
