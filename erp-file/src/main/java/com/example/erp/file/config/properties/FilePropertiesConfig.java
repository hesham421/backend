package com.example.erp.file.config.properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Enables and registers all File Service configuration properties.
 */
@Configuration
@EnableConfigurationProperties({
    FileTokenProperties.class
})
public class FilePropertiesConfig {
    // Configuration properties are automatically registered
}
