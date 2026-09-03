package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-017 request body — the Tier-1 module→role grant command. The role is taken from the
 * path ({@code /roles/{id}/modules}); the body carries only the module to grant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Assign a module to a role (Tier-1 grant) - منح موديل لدور")
public class RoleModuleAssignRequest {

    @NotNull(message = "{validation.required}")
    @Schema(description = "Module identifier to grant - معرف الموديل المراد منحه", example = "1")
    private Long moduleId;
}
