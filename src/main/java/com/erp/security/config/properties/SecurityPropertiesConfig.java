package com.erp.security.config.properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Registers all security @ConfigurationProperties records as beans, validated at startup (fail-fast).
 */
@Configuration
@EnableConfigurationProperties({
    JwtProperties.class,
    CookieProperties.class,
    CorsProperties.class,
    RefreshTokenCleanupProperties.class,
    LoginRateLimitProperties.class,
    SelfServiceTokenProperties.class,
    PasswordResetEmailProperties.class
})
public class SecurityPropertiesConfig {
    // Configuration properties are automatically registered
}
