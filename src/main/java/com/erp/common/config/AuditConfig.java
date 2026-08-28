package com.erp.common.config;

import org.springframework.context.annotation.Configuration;

/**
 * Marker class — audit fields are populated via plain JPA lifecycle callbacks
 * ({@link com.erp.common.audit.AuditEntityListener}), not Spring Data JPA Auditing.
 */
@Configuration
public class AuditConfig {
    // Intentionally empty — AuditEntityListener handles everything via JPA lifecycle callbacks
}
