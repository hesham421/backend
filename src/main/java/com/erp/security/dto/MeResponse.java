package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-021 response — the caller's own identity, active roles and granted modules/permissions,
 * derived solely from the JWT principal (RULE-SEC-015). roleCodes/roleNames/grantedModules/
 * grantedPermissions are the UNION across all the caller's active Roles (RULE-SEC-016) — never a
 * single role. grantedModules/grantedPermissions are code arrays (module/permission codes), not
 * full entity objects, per the spec's parallel-array response shape.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Current authenticated user's self identity, roles and grants - الهوية الذاتية للمستخدم الحالي وأدواره وصلاحياته")
public class MeResponse {

    @Schema(description = "Username of the authenticated caller - اسم مستخدم المستدعي المصادَق عليه", example = "admin")
    private String username;

    @Schema(description = "Full name of the authenticated caller - الاسم الكامل للمستدعي", example = "System Administrator")
    private String fullName;

    @Schema(description = "Codes of the caller's currently active roles (union) - رموز الأدوار النشطة للمستخدم (اتحاد)", example = "[\"SEC_ADMIN\"]")
    private List<String> roleCodes;

    @Schema(description = "Names of the caller's currently active roles (union) - أسماء الأدوار النشطة للمستخدم (اتحاد)", example = "[\"Security Administrator\"]")
    private List<String> roleNames;

    @Schema(description = "Codes of the modules granted to the caller across all active roles (Tier-1 union) - رموز الموديولات الممنوحة عبر كل الأدوار النشطة", example = "[\"SEC\"]")
    private List<String> grantedModules;

    @Schema(description = "Codes of the permissions granted to the caller across all active roles (Tier-2 union) - رموز الصلاحيات الممنوحة عبر كل الأدوار النشطة", example = "[\"PERM_SEC_ROLES_VIEW\"]")
    private List<String> grantedPermissions;
}
