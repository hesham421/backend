package com.erp.security.jwt;

import com.erp.common.web.ApiError;
import com.erp.common.web.ApiResponse;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Entry point for unauthenticated requests to a protected endpoint. Without it Spring's default
 * {@code Http403ForbiddenEntryPoint} returns a bare 403 with no body, blurring "not authenticated"
 * (401) against "authenticated but forbidden" (403) and bypassing the {@link ApiResponse} envelope.
 * This returns a 401 wrapped in the same envelope every other error uses, so clients can distinguish
 * "log in" from "insufficient permission".
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ApiError error = ApiError.builder()
            .code("UNAUTHORIZED")
            .message("Authentication is required to access this resource")
            .build();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.failure(error));
    }
}
