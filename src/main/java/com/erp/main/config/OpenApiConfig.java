package com.erp.main.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for the unified ERP Main application — one Swagger group per
 * module, using packages-to-scan (not paths-to-match).
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:7272}")
    private String serverPort;

    /**
     * Global OpenAPI configuration
     * @Primary ensures this bean takes precedence over CommonOpenApiConfig
     */
    @Bean
    @Primary
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "Bearer Authentication";
        
        return new OpenAPI()
            // JWT Bearer token security scheme
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter JWT token from /api/auth/login (without 'Bearer' prefix)")
                )
            )
            // Apply security globally to all endpoints
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            
            // API Information
            .info(new Info()
                .title("ERP System API")
                .description("""
                    # 🏢 ERP System - Enterprise Resource Planning
                    
                    نظام تخطيط موارد المؤسسة المتكامل
                    
                    ## 📚 Available Modules:
                    
                    | Module | Description | Group |
                    |--------|-------------|-------|
                    | 🔐 **Security** | Authentication, Users, Roles, Permissions | `1-security` |
                    | 📋 **Master Data** | Activities and reference data | `2-masterdata` |
                    | 💰 **Finance GL** | General Ledger, Journals, Posting | `3-finance-gl` |
                    
                    ## 🔑 Authentication:
                    
                    1. Call `POST /api/auth/login` with username/password
                    2. Copy the `accessToken` from response
                    3. Click **Authorize** button (🔒) above
                    4. Enter token (without "Bearer" prefix)
                    5. Click **Authorize** → **Close**
                    
                    ## 📄 Pagination:
                    - Use `page` (0-based), `size` (default: 20, max: 100)
                    - Sort: `sort=field,direction` (e.g., `sort=name,asc`)
                    
                    ## 🔍 Advanced Search:
                    - POST `/search` endpoints support dynamic filters
                    - Operators: EQUALS, CONTAINS, GREATER_THAN, IN, etc.
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("ERP Development Team")
                    .email("dev@erp-system.com")
                    .url("https://github.com/erp-system"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("Development Server"),
                new Server()
                    .url("https://api.erp-system.com")
                    .description("Production Server")
            ));
    }

    /**
     * Security Module APIs — Authentication, User/Role/Permission/Page/Menu Management.
     */
    @Bean
    public GroupedOpenApi securityApi() {
        return GroupedOpenApi.builder()
            .group("1-security")
            .displayName("🔐 Security & Authentication")
            .packagesToScan("com.erp.security.controller")
            .build();
    }

    /**
     * 📋 Master Data Module APIs
     * - Activities
     * - (Future: Customers, Suppliers, Products, etc.)
     */
    @Bean
    public GroupedOpenApi masterDataApi() {
        return GroupedOpenApi.builder()
            .group("2-masterdata")
            .displayName("📋 Master Data")
            .packagesToScan("com.erp.masterdata.controller")
            .build();
    }

    /**
     * Finance GL Module APIs — GL Accounts, Manual Journals, Journal Queries, Posting Engine,
     * Account Balances, Fiscal Periods, Financial Reports.
     */
    @Bean
    public GroupedOpenApi financeGlApi() {
        return GroupedOpenApi.builder()
            .group("3-finance-gl")
            .displayName("💰 Finance - General Ledger")
            .packagesToScan("com.erp.finance.gl.controller")
            .build();
    }

    /**
     * Organization Module APIs — Legal Entities, Branches, Regions, Departments, Cost Centers,
     * Profit Centers, Location Sites.
     */
    @Bean
    public GroupedOpenApi organizationApi() {
        return GroupedOpenApi.builder()
            .group("4-organization")
            .displayName("🏢 Organization")
            .packagesToScan("com.erp.org.controller")
            .build();
    }

    /**
     * 📁 File Service Module APIs
     * - Upload tokens, uploads, downloads, access tokens, deletion
     */
    @Bean
    public GroupedOpenApi fileServiceApi() {
        return GroupedOpenApi.builder()
            .group("5-file-service")
            .displayName("📁 File Service")
            .packagesToScan("com.erp.file.controller")
            .build();
    }

    /**
     * 🔔 Notification Module APIs
     * - Channel Config, Templates, Send/Schedule, Inbox
     */
    @Bean
    public GroupedOpenApi notificationApi() {
        return GroupedOpenApi.builder()
            .group("6-notification")
            .displayName("🔔 Notification")
            .packagesToScan("com.erp.notification.controller")
            .build();
    }

    /**
     * 📊 All APIs (Combined view)
     * Shows all endpoints from all modules
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
            .group("0-all")
            .displayName("📊 All APIs")
            .packagesToScan(
                "com.erp.security.controller",
                "com.erp.masterdata.controller",
                "com.erp.finance.gl.controller",
                "com.erp.org.controller",
                "com.erp.file.controller",
                "com.erp.notification.controller"
            )
            .build();
    }
}
