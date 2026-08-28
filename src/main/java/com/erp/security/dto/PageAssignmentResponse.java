package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * VIEW permission is implicit and NOT included in {@code permissions} — only CREATE/UPDATE/DELETE are returned.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Page assignment with permission flags")
public class PageAssignmentResponse {

    @Schema(description = "Page code", example = "USER")
    private String pageCode;

    @Schema(description = "Page name (English)", example = "User Management")
    private String pageName;

    @Schema(description = "Page name (Arabic)", example = "إدارة المستخدمين")
    private String pageNameAr;

    @Schema(description = "CRUD permissions (VIEW is implicit, only CREATE/UPDATE/DELETE returned)", 
            example = "[\"CREATE\", \"UPDATE\"]")
    private List<String> permissions;
}
