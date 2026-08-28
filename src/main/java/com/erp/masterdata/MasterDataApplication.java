package com.erp.masterdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * ERP Master Data module entry point — manages master lookup / lookup detail entities.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.erp.masterdata",                   // This module
    "com.erp.common.web",               // Common web components (Rule 12.3)
    "com.erp.common.multitenancy",      // Multi-tenancy support (Rule 12.3)
    "com.erp.common.exception",         // Exception handling
    "com.erp.common.search",                    // Search components (PageableBuilder, SearchRequest)
    "com.erp.common.i18n"               // Localization support (required by GlobalExceptionHandler)
})
public class MasterDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(MasterDataApplication.class, args);
    }
}
