package com.erp.file.security;

import com.erp.common.exception.LocalizedException;
import com.erp.common.i18n.LocalizationService;
import com.erp.common.web.ApiError;
import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
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
 * Pre-controller Encrypted Token gate for the permitAll'd token routes (POLICY-CLI-06) — the token IS the auth
 * mechanism, so this filter is the sole guard, and (running outside the MVC pipeline) writes a thrown {@link
 * LocalizedException} as the standard {@code ApiResponse} itself.
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
