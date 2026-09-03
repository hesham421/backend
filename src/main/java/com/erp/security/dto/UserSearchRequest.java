package com.erp.security.dto;

import com.erp.common.dto.BaseSearchContractRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * API-SEC-008 search request. Bound from GET query params (@ModelAttribute): username/email
 * (LIKE) and userStatusId/isActiveFl (EXACT), plus the inherited page/size/sort. These are the
 * account's own scalar columns, applied by UserService via an explicit Specification (QR-SEC-0003)
 * — only the inherited paging/sort flow through the shared PageableBuilder.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Search request for User - طلب بحث المستخدمين")
public class UserSearchRequest extends BaseSearchContractRequest {

    @Schema(description = "Filter by username (contains) - تصفية حسب اسم المستخدم", example = "jdoe")
    private String username;

    @Schema(description = "Filter by email (contains) - تصفية حسب البريد", example = "example.com")
    private String email;

    @Schema(description = "Filter by account status (LOV-SEC-002) - تصفية حسب الحالة", example = "ACTIVE")
    private String userStatusId;

    @Schema(description = "Filter by active status - تصفية حسب التفعيل", example = "true")
    private Boolean isActiveFl;
}
