package com.example.erp.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate for same-JVM, cross-module REST calls (erp-notification -> erp-security) where
 * the calling module has no Maven dependency on the target module. Both modules are assembled
 * into the single erp-main deployable under one port. Mirrors
 * {@code com.example.security.config.InternalApiClientConfig}'s identical pattern for
 * erp-security -> erp-org/erp-masterdata. See
 * {@link com.example.erp.notification.client.SecUserProfileClient}.
 */
@Configuration
public class InternalApiClientConfig {

    @Bean
    public RestTemplate internalApiRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }
}
