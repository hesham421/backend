package com.erp.common.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * This is the Jackson 2 {@code ObjectMapper}; Spring Boot 4's web layer actually (de)serializes
 * HTTP traffic through its own, independently-built Jackson 3 mapper, so these settings have no
 * effect on {@code @RequestBody}/{@code @ResponseBody} — use {@code spring.jackson.*} properties
 * for that. This bean only affects call sites that explicitly {@code @Autowired ObjectMapper}.
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
