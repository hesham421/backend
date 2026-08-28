package com.erp.common.web.config;

import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;

/**
 * Fixes Pageable parameter display in Swagger UI; does NOT define the OpenAPI bean itself —
 * that lives in erp-main/OpenApiConfig.java.
 */
@Configuration
public class CommonOpenApiConfig {

    static {
        // Fix Pageable parameter display in Swagger UI
        // Prevents ["string"] default values in page/size/sort parameters
        SpringDocUtils.getConfig().replaceWithClass(
                org.springframework.data.domain.Pageable.class,
                org.springdoc.core.converters.models.Pageable.class
        );
    }

    // No @Bean methods - all OpenAPI configuration is in erp-main
}
