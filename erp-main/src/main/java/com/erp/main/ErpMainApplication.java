package com.erp.main;

import com.erp.main.config.JpaConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

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
                "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
                "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration",
                "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration",
                "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration"
        }
)
@EntityScan(basePackages = {
        "com.example.security.entity",
        "com.example.masterdata.entity",
        "com.example.erp.finance.gl.entity",
        "com.example.erp.org.entity"
})
@EnableJpaRepositories(
        basePackages = {
                "com.example.security.repository",
                "com.example.masterdata.repository",
                "com.example.erp.finance.gl.repository",
                "com.example.erp.org.repository"
        },
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
// @EnableCaching  // ❌ DISABLED: Redis caching disabled - will be enabled later
@ComponentScan(basePackages = {
    // Main module (must be included when overriding component scan)
    "com.erp.main",

    // Core modules
    "com.example.security",                      // Security module
    "com.example.masterdata",                    // Master Data module
    "com.example.erp.finance.gl",               // Finance GL module
    "com.example.erp.org",                       // Organization module

    // Common utilities
    "com.example.erp.common.web",               // Web components
    "com.example.erp.common.multitenancy",      // Multi-tenancy
    "com.example.erp.common.exception",         // Exception handling
    "com.example.erp.common.search",            // Search components
    "com.erp.common.search",                    // Search components (alternative package)
    "com.example.erp.common.i18n"               // Localization
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
