package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-009 update request body. Excludes username — immutable after creation (RULE-SEC-001,
 * structurally enforced by omission) — and passwordHash (system-managed via activation/reset).
 * userStatusId drives a RULE-SEC-012 lifecycle transition; isActiveFl, when present, applies via
 * the entity's activate()/deactivate() helpers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update a user account - تحديث حساب مستخدم")
public class UserUpdateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 255, message = "{validation.size}")
    @Schema(description = "Email address - البريد الإلكتروني", example = "jdoe@example.com")
    private String email;

    @Size(max = 30, message = "{validation.size}")
    @Schema(description = "Phone number - رقم الهاتف", example = "+201234567890")
    private String phone;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Full name - الاسم الكامل", example = "John Doe")
    private String fullName;

    @NotBlank(message = "{validation.required}")
    @Size(max = 10, message = "{validation.size}")
    @Schema(description = "Preferred language code (LOV-SEC-001) - رمز اللغة المفضلة", example = "EN")
    private String preferredLangId;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Schema(description = "Account lifecycle status (LOV-SEC-002) - حالة الحساب", example = "ACTIVE")
    private String userStatusId;

    @Schema(description = "Active status; omit to leave unchanged - حالة التفعيل، اتركه فارغاً لعدم التغيير", example = "true")
    private Boolean isActiveFl;
}
