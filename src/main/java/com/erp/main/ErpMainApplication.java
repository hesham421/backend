package com.erp.main;

import com.erp.main.config.JpaConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Locale;

/**
 * ERP System - Unified Main Application
 *
 * Aggregates all ERP modules:
 * - Security & Authentication (Port 7272 standalone)
 * - Master Data Management (Port 7373 standalone)
 * - Finance - General Ledger (Port 7474 standalone)
 *
 * All APIs accessible through single Swagger UI at port 7272
 *
 * Architecture: Rule 6 - One-Way Dependencies (DAG)
 * common-utils → security → masterdata → finance-gl → main
 *
 * @author ERP Team
 */
@Import(JpaConfig.class)
@SpringBootApplication(
        excludeName = {
                "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration",
                "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration"
        }
)
@EntityScan(basePackages = {
        "com.erp.security.entity",
        "com.erp.masterdata.entity",
        "com.erp.finance.gl.entity",
        "com.erp.org.entity",
        "com.erp.file.entity",
        "com.erp.notification.entity"
})
@EnableJpaRepositories(
        basePackages = {
                "com.erp.security.repository",
                "com.erp.masterdata.repository",
                "com.erp.finance.gl.repository",
                "com.erp.org.repository",
                "com.erp.file.repository",
                "com.erp.notification.repository"
        },
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
@EnableCaching
@EnableScheduling
@ComponentScan(basePackages = {
    // Main module (must be included when overriding component scan)
    "com.erp.main",

    // Core modules
    "com.erp.security",                      // Security module
    "com.erp.masterdata",                    // Master Data module
    "com.erp.finance.gl",               // Finance GL module
    "com.erp.org",                       // Organization module
    "com.erp.file",                      // File Service module
    "com.erp.notification",              // Notification module

    // Common utilities
    "com.erp.common.web",               // Web components
    "com.erp.common.multitenancy",      // Multi-tenancy
    "com.erp.common.exception",         // Exception handling
    "com.erp.common.search",            // Search components
    "com.erp.common.i18n"               // Localization
})
public class ErpMainApplication {

    public static void main(String[] args) {
        // Set default locale to English with Western-Arabic numerals
        Locale.setDefault(Locale.forLanguageTag("en-US-u-nu-latn"));
        System.setProperty("user.language", "en");
        System.setProperty("user.country", "US");

        SpringApplication.run(ErpMainApplication.class, args);
    }
}
