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
 * ERP System — unified Spring Boot entry point aggregating all ERP modules behind a single Swagger UI.
 */
@Import(JpaConfig.class)
@SpringBootApplication(
        excludeName = {
                "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration",
                "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration"
        }
)
@EntityScan(basePackages = "com.erp")
@EnableJpaRepositories(
        basePackages = "com.erp",
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
@EnableCaching
@EnableScheduling
@ComponentScan(basePackages = "com.erp")
public class ErpMainApplication {

    public static void main(String[] args) {
        // Set default locale to English with Western-Arabic numerals
        Locale.setDefault(Locale.forLanguageTag("en-US-u-nu-latn"));
        System.setProperty("user.language", "en");
        System.setProperty("user.country", "US");

        SpringApplication.run(ErpMainApplication.class, args);
    }
}
