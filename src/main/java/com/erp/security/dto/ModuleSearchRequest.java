package com.erp.security.dto;

import com.erp.common.dto.BaseSearchContractRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * API-SEC-020 search request (POST /modules/search). Module is a flat/root reference entity, so
 * there is no parent-id extractor override; filtering on moduleCode (LIKE) / isActive (EXACT)
 * flows through the inherited generic filters consumed by the shared SpecBuilder. No
 * {@code @AllArgsConstructor} — the class adds zero fields, so it would collide with the no-arg
 * constructor; {@code @SuperBuilder} still provides full construction.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Search request for Module - طلب بحث الموديولات")
public class ModuleSearchRequest extends BaseSearchContractRequest {
}
