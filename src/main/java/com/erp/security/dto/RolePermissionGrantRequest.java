package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-015 request body — the Tier-2 screen-permission grant command. The role is taken from the
 * path ({@code /roles/{id}/permissions}); the body carries only the permission to grant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Grant a screen permission to a role (Tier-2) - منح صلاحية شاشة لدور")
public class RolePermissionGrantRequest {

    @NotNull(message = "{validation.required}")
    @Schema(description = "Permission identifier to grant - معرف الصلاحية المراد منحها", example = "1")
    private Long permissionId;
}
