package com.erp.security.dto;

import com.erp.common.dto.BaseSearchContractRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * API-SEC-011 search request (POST /roles/search). Role is a flat/root reference entity, so there
 * is no parent-id extractor override; filtering on roleCode (LIKE) / isActive (EXACT) flows through
 * the inherited generic filters consumed by the shared SpecBuilder.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Search request for Role - طلب بحث الأدوار")
public class RoleSearchRequest extends BaseSearchContractRequest {
}
