package com.erp.fin.dto;

import com.erp.common.dto.BaseSearchContractRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * API-FIN-001 — search body for ENT-FIN-001 (Account), QR-FIN-001.
 *
 * <p>Filters the plan's API block names: {@code code} (LIKE), {@code nameAr}/{@code nameEn}
 * (LIKE), {@code accountTypeCode} (EXACT) and {@code isActiveFl} (EXACT). They travel in the
 * inherited {@code filters} list rather than as typed properties — the shared search contract
 * (CORE.md "Search contract": {@code {filters, page, size, sort}}) is the single input shape, and
 * {@code AccountService.ALLOWED_SORT_FIELDS} is what actually admits a field.
 *
 * <p><b>A.3.1 deviation, forced by the language.</b> The skill's DTO annotation set is
 * {@code @Data @Builder @NoArgsConstructor @AllArgsConstructor}; this class declares no instance
 * field of its own (every filter travels in the inherited {@code filters} list), so
 * {@code @AllArgsConstructor} would generate a second no-argument constructor and the class would
 * not compile. {@code @NoArgsConstructor} is the one Jackson needs to deserialize the body, so
 * {@code @AllArgsConstructor} is the one omitted; {@code @SuperBuilder} (not {@code @Builder})
 * is required in its place because the superclass uses it.
 */
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Search request for chart-of-accounts accounts - طلب بحث في دليل الحسابات")
public class AccountSearchRequest extends BaseSearchContractRequest {
}
