package com.erp.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Locale;

/**
 * @EnableJpaRepositories is deliberately configured in JpaConfig instead of here, to avoid
 * duplicate bean definitions.
 */
@EnableCaching
@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.erp.security",                      // Security module
    "com.erp.common.web",               // Common web components (includes CommonWebConfig)
    "com.erp.common.multitenancy",      // Multi-tenancy support
    "com.erp.common.exception",         // Exception handling
    "com.erp.common.search",            // Search components
    "com.erp.common.i18n"               // Localization support (required by GlobalExceptionHandler)
})
public class SecurityOracleJwtApplication {

    public static void main(String[] args) {
        Locale.setDefault(Locale.forLanguageTag("en-US-u-nu-latn"));
        System.setProperty("user.language", "en");
        System.setProperty("user.country", "US");
        SpringApplication.run(SecurityOracleJwtApplication.class, args);
    }
}
