package com.example.erp.file.config;

import com.example.erp.common.i18n.LocalizationService;
import com.example.erp.common.web.OperationCode;
import com.example.erp.file.security.FileTokenFilter;
import com.example.erp.file.security.FileTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Registers {@link FileTokenFilter} as a plain Servlet filter — deliberately NOT a
 * {@code @Component} (which Spring Boot would additionally auto-register with default
 * {@code /*} patterns, double-registering it), and deliberately NOT wired into erp-security's
 * {@code SecurityConfig} (that would invert the module dependency direction — Security must not
 * depend on File Service). Runs at the highest precedence so it rejects invalid tokens before
 * Spring Security's own chain does any work on these permitAll'd routes.
 *
 * Registered on the Servlet (not Ant) {@code /*} pattern — matches literally every request, not
 * just upload/download/delete — because the delete route ({@code DELETE /{token}}) is a bare
 * single-path-segment root path with no fixed prefix, and Servlet url-patterns cannot express
 * "any single segment" any other way. {@link FileTokenFilter#shouldNotFilter} does the real,
 * cheap (string-prefix / single-regex) narrowing for every other request.
 */
@Configuration
public class FileTokenFilterConfig {

    @Bean
    public FilterRegistrationBean<FileTokenFilter> fileTokenFilterRegistration(
            FileTokenService fileTokenService, OperationCode operationCode,
            LocalizationService localizationService, ObjectMapper objectMapper) {
        FilterRegistrationBean<FileTokenFilter> registration = new FilterRegistrationBean<>(
            new FileTokenFilter(fileTokenService, operationCode, localizationService, objectMapper));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
