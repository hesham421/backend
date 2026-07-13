package com.example.erp.file.security;

import com.example.erp.common.exception.LocalizedException;
import com.example.erp.common.i18n.LocalizationService;
import com.example.erp.common.web.ApiError;
import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

/**
 * Pre-controller Encrypted Token gate (CORE.md "Encrypted Token layer" — NOT part of the
 * controller/service/domain/repository layering) for {@code /upload/{token}},
 * {@code /download/{token}}, and {@code DELETE /{token}}. These 3 routes are permitAll'd in
 * erp-security's SecurityConfig (POLICY-CLI-06 — no JWT validation for these routes; the
 * Encrypted Token IS the auth mechanism), so this filter — not Spring Security, not
 * GlobalExceptionHandler — is the only thing standing between an unauthorized request and the
 * controller.
 *
 * {@code DELETE /{token}} is a bare, single-path-segment root route (no fixed prefix like
 * upload/download have) — matched here as "DELETE method + exactly one path segment", grep-
 * verified against every controller in this codebase as unambiguous (no other module defines a
 * root-level single-segment mapping for any HTTP method). Registered on Servlet pattern
 * {@code /*} (see FileTokenFilterConfig) rather than a path prefix, since Servlet url-patterns
 * cannot express "any single segment" directly.
 *
 * Runs outside the normal Spring MVC dispatch pipeline, so a thrown {@link LocalizedException}
 * would never reach {@code GlobalExceptionHandler} — it is caught here and the standard
 * {@code ApiResponse} envelope is written directly, same technique as erp-security's
 * CustomAuthenticationEntryPoint/CustomAccessDeniedHandler use for the same structural reason.
 */
@RequiredArgsConstructor
public class FileTokenFilter extends OncePerRequestFilter {

    public static final String TOKEN_PAYLOAD_ATTRIBUTE = "FILE_TOKEN_PAYLOAD";

    private static final String UPLOAD_PREFIX = "/upload/";
    private static final String DOWNLOAD_PREFIX = "/download/";

    private final FileTokenService fileTokenService;
    private final OperationCode operationCode;
    private final LocalizationService localizationService;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith(UPLOAD_PREFIX) || uri.startsWith(DOWNLOAD_PREFIX)) {
            return false;
        }
        return !(HttpMethod.DELETE.matches(request.getMethod()) && isSingleSegmentRootPath(uri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String expectedAction;
        if (uri.startsWith(UPLOAD_PREFIX)) {
            expectedAction = FileTokenService.ACTION_UPLOAD;
        } else if (uri.startsWith(DOWNLOAD_PREFIX)) {
            expectedAction = FileTokenService.ACTION_DOWNLOAD;
        } else {
            expectedAction = FileTokenService.ACTION_DELETE;
        }
        String token = uri.substring(uri.lastIndexOf('/') + 1);

        try {
            FileTokenPayload payload = fileTokenService.decodeAndConsume(token, expectedAction);
            request.setAttribute(TOKEN_PAYLOAD_ATTRIBUTE, payload);
            chain.doFilter(request, response);
        } catch (LocalizedException ex) {
            writeError(response, uri, ex);
        }
    }

    private static boolean isSingleSegmentRootPath(String uri) {
        return uri.length() > 1 && uri.indexOf('/', 1) == -1;
    }

    private void writeError(HttpServletResponse response, String path, LocalizedException ex) throws IOException {
        HttpStatus httpStatus = operationCode.toHttpStatus(ex.getStatusCode());
        String message = localizationService.getMessage(ex.getMessageKey(), ex.getArgs());

        ApiError error = new ApiError(ex.getMessageKey(), message, Instant.now(), path);
        ApiResponse<Void> body = ApiResponse.fail(message, error);

        response.setStatus(httpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
