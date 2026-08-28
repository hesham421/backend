package com.erp.file.config;

import com.erp.common.i18n.LocalizationService;
import com.erp.common.web.OperationCode;
import com.erp.file.security.FileTokenFilter;
import com.erp.file.security.FileTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Registered as a plain Servlet filter (not {@code @Component}, which Spring Boot would double-register) on the
 * Servlet {@code /*} pattern — the single-segment DELETE route can't be matched by an Ant pattern; {@link
 * FileTokenFilter#shouldNotFilter} does the real narrowing.
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
