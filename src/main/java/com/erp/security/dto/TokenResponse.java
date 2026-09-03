package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-001/002 response body: the internal SSO access token plus a freshly rotated refresh
 * token. Raw token values are returned once here; only their hashes are persisted (RULE-SEC-004).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Issued token pair - رموز الدخول الصادرة")
public class TokenResponse {

    @Schema(description = "Signed JWT access token - رمز الوصول", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "Opaque rotating refresh token - رمز التجديد", example = "u8Zx1...")
    private String refreshToken;

    @Schema(description = "Access token lifetime in seconds - مدة صلاحية رمز الوصول بالثواني", example = "3600")
    private long expiresIn;
}
