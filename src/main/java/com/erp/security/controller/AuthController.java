package com.erp.security.controller;

import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.security.dto.ActivateAccountRequest;
import com.erp.security.dto.ForgotPasswordRequest;
import com.erp.security.dto.LoginRequest;
import com.erp.security.dto.LogoutRequest;
import com.erp.security.dto.RefreshTokenRequest;
import com.erp.security.dto.ResetPasswordRequest;
import com.erp.security.dto.TokenResponse;
import com.erp.security.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin controller for the six auth APIs (API-SEC-001..006). Login/refresh/forgot/reset/activate are
 * public pre-auth endpoints and logout requires only an authenticated caller, so — per the
 * SVC-API-AUTH spec — none carries a {@code @PreAuthorize}; SecurityConfig gates the paths instead.
 * Forgot-password returns a neutral 202 (no account enumeration); logout returns 204.
 */
@RestController
@RequestMapping("/api/v1/security/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, token rotation and self-service account recovery - المصادقة واسترجاع الحساب")
public class AuthController {

    private final AuthService authService;
    private final OperationCode operationCode;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "تسجيل الدخول وإصدار الرموز")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        return operationCode.craftResponse(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "تجديد رمز الوصول وتدوير رمز التجديد")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return operationCode.craftResponse(authService.refresh(request));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Logout", description = "إبطال رمز التجديد (idempotent)")
    public void logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Forgot password", description = "طلب إعادة تعيين كلمة المرور (استجابة محايدة)")
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestReset(request);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "إعادة تعيين كلمة المرور برمز صالح")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return operationCode.craftResponse(authService.resetPassword(request));
    }

    @PostMapping("/activate")
    @Operation(summary = "Activate account", description = "تفعيل الحساب برمز صالح")
    public ResponseEntity<ApiResponse<Void>> activate(@Valid @RequestBody ActivateAccountRequest request) {
        return operationCode.craftResponse(authService.activate(request));
    }
}
