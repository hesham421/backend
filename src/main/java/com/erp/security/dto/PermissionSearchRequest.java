package com.erp.security.dto;

import com.erp.common.dto.BaseSearchContractRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * API-SEC-014 search request. Bound from GET query params (@ModelAttribute): the three EXACT
 * filters plus the inherited page/size/sort. pageFk and moduleFk are nested association paths on
 * the Permission entity ({@code page.id} / {@code page.module.id}), and permissionType is a fixed
 * CORE-9 code — none are generic scalar filters, so PermissionService applies them via an explicit
 * Specification (QR-SEC-0015) rather than the generic SpecBuilder filter list. Only the inherited
 * paging/sort flow through the shared builders.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Search request for Permission - طلب بحث الصلاحيات")
public class PermissionSearchRequest extends BaseSearchContractRequest {

    @Schema(description = "Filter by owning page id - تصفية حسب الشاشة", example = "3")
    private Long pageFk;

    @Schema(description = "Filter by permission type (VIEW/CREATE/UPDATE/DELETE) - تصفية حسب النوع", example = "VIEW")
    private String permissionType;

    @Schema(description = "Filter by owning module id (via page) - تصفية حسب الموديل", example = "1")
    private Long moduleFk;
}
