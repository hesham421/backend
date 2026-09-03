package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-012 request body — assign a role to a user. The user is taken from the path
 * ({@code /users/{id}/roles}); the body carries only the role to assign.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Assign a role to a user - إسناد دور لمستخدم")
public class UserRoleAssignRequest {

    @NotNull(message = "{validation.required}")
    @Schema(description = "Role identifier to assign - معرف الدور المراد إسناده", example = "1")
    private Long roleId;
}
