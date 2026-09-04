package com.erp.main.config;

import com.erp.security.jwt.JwtAuthenticationFilter;
import com.erp.security.jwt.JwtTokenProvider;
import com.erp.security.jwt.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless JWT security for the platform. The five pre-auth SEC endpoints (login, refresh,
 * forgot/reset password, activate) plus swagger/openapi/actuator are public; everything else
 * requires a valid Bearer token, and {@code @EnableMethodSecurity} keeps every {@code @PreAuthorize}
 * live for Tier-2 authorization. Logout is deliberately NOT public — it requires an authenticated
 * caller (SVC-API-AUTH spec).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/v1/security/auth/login",
        "/api/v1/security/auth/refresh",
        "/api/v1/security/auth/forgot-password",
        "/api/v1/security/auth/reset-password",
        "/api/v1/security/auth/activate",
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        // Only the health/info actuator endpoints are exposed (management.endpoints.web.exposure
        // .include=health,info) — scope the public matcher to those rather than the whole /actuator
        // namespace so any future exposed endpoint is not silently unauthenticated.
        "/actuator/health",
        "/actuator/health/**",
        "/actuator/info"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtTokenProvider jwtTokenProvider,
                                           RestAuthenticationEntryPoint authenticationEntryPoint) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
