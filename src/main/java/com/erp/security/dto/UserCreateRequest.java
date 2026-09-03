package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-007 create request body (ENTITY-SEC-001 UserAccount). Excludes id and audit fields, and
 * carries NO password — the account is created PENDING_ACTIVATION and the caller sets the password
 * through the activation flow (RULE-SEC-004). username is the create-only natural key, immutable
 * afterwards (structurally absent from the update DTO).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create a user account - إنشاء حساب مستخدم")
public class UserCreateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "Unique username - اسم المستخدم الفريد", example = "jdoe")
    private String username;

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
}
