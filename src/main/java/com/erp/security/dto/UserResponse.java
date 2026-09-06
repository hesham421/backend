package com.erp.security.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-007/008/009 response view of ENTITY-SEC-001 (UserAccount). Excludes passwordHash entirely
 * (RULE-SEC-004 — the hash is never exposed on any response). Also excludes the internal RULE-SEC-005
 * lockout state (failedLoginCount, lockedUntil): exposing how close each account is to lockout, and
 * its exact unlock time, to any USERS_VIEW caller is reconnaissance for a coordinated brute-force
 * attempt and is not needed by the user-management screens.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User account - حساب المستخدم")
public class UserResponse {

    @Schema(description = "Unique identifier - المعرف الفريد", example = "1")
    private Long id;

    @Schema(description = "Username - اسم المستخدم", example = "jdoe")
    private String username;

    @Schema(description = "Email address - البريد الإلكتروني", example = "jdoe@example.com")
    private String email;

    @Schema(description = "Phone number - رقم الهاتف", example = "+201234567890")
    private String phone;

    @Schema(description = "Full name - الاسم الكامل", example = "John Doe")
    private String fullName;

    @Schema(description = "Preferred language code (LOV-SEC-001) - رمز اللغة المفضلة", example = "EN")
    private String preferredLangId;

    @Schema(description = "Account lifecycle status (LOV-SEC-002) - حالة الحساب", example = "PENDING_ACTIVATION")
    private String userStatusId;

    @Schema(description = "Active status - حالة التفعيل", example = "true")
    private Boolean isActiveFl;

    @Schema(description = "Created timestamp - تاريخ الإنشاء")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    @Schema(description = "Created by - أنشئ بواسطة")
    private String createdBy;

    @Schema(description = "Updated timestamp - تاريخ التحديث")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;

    @Schema(description = "Updated by - حُدّث بواسطة")
    private String updatedBy;
}
