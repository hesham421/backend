package com.erp.main.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * TEMPORARY — permits every HTTP request. The SECURITY module (JWT authentication) has not
 * been built yet, so there is no login mechanism to gate on. {@code @EnableMethodSecurity} is
 * still on so every {@code @PreAuthorize} written by build-create-service already takes real
 * effect against the (anonymous) SecurityContext. Replace the permitAll filter chain with real
 * JWT authentication when the SECURITY module's CORE phase runs — do not let this linger.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
