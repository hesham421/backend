package com.example.security.controller;

import com.example.erp.common.web.OperationCode;
import com.example.security.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    PermissionControllerTest.TestConfig.class,
    PermissionController.class,
})
@WebAppConfiguration
class PermissionControllerTest {

    @Configuration
    @EnableWebSecurity
    @EnableMethodSecurity
    @EnableWebMvc
    static class TestConfig {
        @org.springframework.context.annotation.Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            AuthenticationEntryPoint entryPoint = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
            return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
                .build();
        }
    }

    @MockitoBean PermissionService permissionService;
    @MockitoBean OperationCode operationCode;

    @Autowired WebApplicationContext wac;

    MockMvc mockMvc;

    @BeforeEach
    void setup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();

        when(operationCode.craftResponse(any())).thenReturn(ResponseEntity.ok().build());
    }

    private static final String VALID_BODY = "{\"name\":\"TEST_PERM\"}";

    // ─── POST /api/permissions ───────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "PERM_PERMISSION_CREATE")
    void createPermission_withCreateAuthority_returns200() throws Exception {
        mockMvc.perform(post("/api/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "PERM_WRONG")
    void createPermission_withoutCreateAuthority_returns403() throws Exception {
        mockMvc.perform(post("/api/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isForbidden());
    }

    @Test
    void createPermission_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isUnauthorized());
    }

    // ─── POST /api/permissions/search ────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "PERM_PERMISSION_VIEW")
    void searchPermissions_withViewAuthority_returns200() throws Exception {
        mockMvc.perform(post("/api/permissions/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "PERM_WRONG")
    void searchPermissions_withoutViewAuthority_returns403() throws Exception {
        mockMvc.perform(post("/api/permissions/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }
}
