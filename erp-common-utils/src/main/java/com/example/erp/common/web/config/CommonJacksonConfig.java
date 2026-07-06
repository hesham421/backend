package com.example.erp.common.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * ✅ CENTRALIZED Jackson ObjectMapper Configuration for ALL ERP modules
 *
 * NOTE: this bean is {@code com.fasterxml.jackson.databind.ObjectMapper} (Jackson 2), present on
 * the classpath only transitively via jjwt-jackson. Spring Boot 4 / Spring Framework 7 default to
 * Jackson 3 ({@code tools.jackson.databind.ObjectMapper}) for the web layer — a different Java
 * type that this bean's {@code @Primary}/{@code @ConditionalOnMissingBean} cannot satisfy. Boot's
 * own Jackson 3 autoconfiguration builds its own mapper independently and is what actually
 * deserializes {@code @RequestBody}/serializes {@code @ResponseBody}; the settings on this bean
 * (JavaTimeModule, date format, etc.) have no effect on that path. Confirmed empirically: unknown
 * JSON properties were silently accepted despite Jackson 2's FAIL_ON_UNKNOWN_PROPERTIES default,
 * until fixed via the version-agnostic `spring.jackson.deserialization.fail-on-unknown-properties`
 * property in application.properties, which Boot applies to whichever mapper it actually builds.
 * Any module-wide Jackson customization intended for HTTP traffic should go through
 * `spring.jackson.*` properties (or a Boot-4-native Jackson 3 customizer bean), not this class.
 * This bean remains in use only by call sites that explicitly `@Autowired ObjectMapper` and invoke
 * it directly (e.g. CustomAuthenticationEntryPoint, CustomAccessDeniedHandler).
 *
 * In Spring Boot 4.0, ObjectMapper is not always auto-configured as a bean.
 * This configuration ensures ObjectMapper is available across all modules.
 * 
 * Features:
 * - Java 8 Time support (LocalDateTime, ZonedDateTime, etc.)
 * - ISO-8601 date format (not timestamps)
 * - Pretty print in development
 * 
 * Architecture Rule:
 * - Rule 12: Common configurations belong in erp-common-utils
 * 
 * Usage:
 * Just @ComponentScan("com.example.erp.common.web") in your module:
 * 
 * <pre>
 * &#64;Autowired
 * private ObjectMapper objectMapper;
 * 
 * String json = objectMapper.writeValueAsString(object);
 * </pre>
 * 
 * @author ERP Team
 */
@Configuration
public class CommonJacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Register Java 8 time module for LocalDateTime, ZonedDateTime, Instant, etc.
        objectMapper.registerModule(new JavaTimeModule());
        
        // Write dates as ISO-8601 strings instead of timestamps
        // Example: "2026-01-10T10:30:00Z" instead of 1736507400000
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Pretty print JSON for better readability in development
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        // Optional: Configure null handling
        // objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        return objectMapper;
    }
}
