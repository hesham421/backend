package com.example.security.controller;

import com.example.security.dto.ActivateAccountRequest;
import com.example.security.dto.AuthRequest;
import com.example.security.dto.AuthResponse;
import com.example.security.dto.ForgotPasswordRequest;
import com.example.security.dto.ResetPasswordRequest;
import com.example.security.dto.SignupRequest;
import com.example.security.dto.SignupResponse;
import com.example.security.dto.UserInfo;
import com.example.security.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication and token management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticates user and returns JWT access token. Use this token in the Authorize button above."
    )
    public AuthResponse login(@RequestBody @Valid AuthRequest req,
                              HttpServletResponse response){
        var t = authService.login(req.username(), req.password(), response);
        return new AuthResponse(t.access(), 900);
    }

    @PostMapping("/login-token")
    @Operation(
        summary = "User login with complete user information",
        description = "Authenticates user and returns access token, refresh token, and complete user information including roles and permissions"
    )
    public UserInfo loginWithToken(@RequestBody @Valid AuthRequest req,
                                    HttpServletResponse response){
        return authService.loginWithUserInfo(req.username(), req.password(), response);
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh access token",
        description = "Refreshes the access token using the refresh token cookie"
    )
    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response){
        var t = authService.refresh(request, response);
        return new AuthResponse(t.access(), 900);
    }

    @PostMapping("/logout")
    @Operation(
        summary = "User logout",
        description = "Logs out the user and invalidates all tokens"
    )
    @ApiResponses(@ApiResponse(responseCode = "204", description = "No Content"))
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response){
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/signup")
    @Operation(
        summary = "Self-registration sign up",
        description = "إنشاء حساب ذاتي - Creates a new self-registered account (disabled until activated via email link). RULE-SEC-030, RULE-SEC-040, RULE-SEC-041."
    )
    public SignupResponse signup(@RequestBody @Valid SignupRequest req) {
        return authService.signup(req);
    }

    @PostMapping("/signup/activate")
    @Operation(
        summary = "Activate a self-registered account",
        description = "تفعيل حساب مسجَّل ذاتياً - Consumes an activation token and enables the account. RULE-SEC-032, RULE-SEC-033."
    )
    public ResponseEntity<Void> activate(@RequestBody @Valid ActivateAccountRequest req) {
        authService.activateAccount(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    @Operation(
        summary = "Forgot password",
        description = "نسيت كلمة المرور - Always returns a generic success response regardless of whether the email exists (RULE-SEC-038, anti-enumeration)."
    )
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest req) {
        authService.forgotPassword(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(
        summary = "Reset password",
        description = "استعادة كلمة المرور - Consumes a password reset token and sets a new password. RULE-SEC-032, RULE-SEC-033."
    )
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ResponseEntity.ok().build();
    }
}
