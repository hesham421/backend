package com.erp.sec.controller;

import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.sec.dto.ConfirmationResponse;
import com.erp.sec.dto.LoginRequest;
import com.erp.sec.dto.LoginResponse;
import com.erp.sec.dto.PasswordResetCompleteRequest;
import com.erp.sec.dto.PasswordResetRequest;
import com.erp.sec.dto.SignupRequestResponse;
import com.erp.sec.dto.SignupSubmitRequest;
import com.erp.sec.service.AuthService;
import com.erp.sec.service.PasswordResetService;
import com.erp.sec.service.SignupRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin controller for API-SEC-001/002/003/004 (SCR-REQ-SEC-001/002/003). Every endpoint here is
 * pre-authentication; the REQ-SEC-033 gateway explicitly exempts them (CORE.md).
 */
@RestController
@RequestMapping("/api/v1/sec/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, sign-up and password reset - تسجيل الدخول والتسجيل وإعادة تعيين كلمة المرور")
public class AuthController {

    private final AuthService service;
    private final SignupRequestService signupRequestService;
    private final PasswordResetService passwordResetService;
    private final OperationCode operationCode;

    /** The caller's address is read straight off the servlet request — DBF-SEC-080/090 (REQ-SEC-027). */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "تسجيل الدخول وإصدار رمز وصول")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return operationCode.craftResponse(service.login(request, httpRequest.getRemoteAddr()));
    }

    @PostMapping("/signup")
    @Operation(summary = "Submit a sign-up request", description = "تقديم طلب تسجيل")
    public ResponseEntity<ApiResponse<SignupRequestResponse>> signup(
            @Valid @RequestBody SignupSubmitRequest request) {
        return operationCode.craftResponse(signupRequestService.submit(request));
    }

    @PostMapping("/password-reset/request")
    @Operation(summary = "Request a password reset", description = "طلب إعادة تعيين كلمة المرور")
    public ResponseEntity<ApiResponse<ConfirmationResponse>> requestReset(
            @Valid @RequestBody PasswordResetRequest request) {
        return operationCode.craftResponse(passwordResetService.request(request));
    }

    @PostMapping("/password-reset/complete")
    @Operation(summary = "Complete a password reset", description = "إتمام إعادة تعيين كلمة المرور")
    public ResponseEntity<ApiResponse<ConfirmationResponse>> completeReset(
            @Valid @RequestBody PasswordResetCompleteRequest request) {
        return operationCode.craftResponse(passwordResetService.complete(request));
    }
}
