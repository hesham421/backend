package com.example.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate for same-JVM, cross-module REST calls (e.g. erp-security -> erp-org,
 * erp-security -> erp-masterdata) where the calling module has no Maven dependency on
 * the target module. Both modules are assembled into the single erp-main deployable
 * under one port, so these calls target {@code http://localhost:<server.port>} — see
 * {@link com.example.security.client.OrgBranchClient} and
 * {@link com.example.security.client.MasterDataLookupClient}, and XM-SEC-001/XM-SEC-002
 * in execution-plan-SEC-gaps.md Section 6.1.
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
