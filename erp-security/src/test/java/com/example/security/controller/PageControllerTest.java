package com.example.security.controller;

import com.example.erp.common.web.OperationCode;
import com.example.security.service.PageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    PageControllerTest.TestConfig.class,
    PageController.class,
})
@WebAppConfiguration
class PageControllerTest {

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

    @MockitoBean PageService pageService;
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

    // ─── POST /api/pages/search ──────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "PERM_PAGE_VIEW")
    void searchPages_withViewAuthority_returns200() throws Exception {
        mockMvc.perform(post("/api/pages/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "PERM_WRONG")
    void searchPages_withoutViewAuthority_returns403() throws Exception {
        mockMvc.perform(post("/api/pages/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void searchPages_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/pages/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    // ─── GET /api/pages/active ───────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "PERM_PAGE_VIEW")
    void getActivePages_withViewAuthority_returns200() throws Exception {
        mockMvc.perform(get("/api/pages/active"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "PERM_WRONG")
    void getActivePages_withoutViewAuthority_returns403() throws Exception {
        mockMvc.perform(get("/api/pages/active"))
            .andExpect(status().isForbidden());
    }

    // ─── GET /api/pages/{id} ─────────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "PERM_PAGE_VIEW")
    void getPageById_withViewAuthority_returns200() throws Exception {
        mockMvc.perform(get("/api/pages/1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "PERM_WRONG")
    void getPageById_withoutViewAuthority_returns403() throws Exception {
        mockMvc.perform(get("/api/pages/1"))
            .andExpect(status().isForbidden());
    }

    // ─── PUT /api/pages/{id}/deactivate ──────────────────────────────────────

    @Test
    @WithMockUser(authorities = "PERM_PAGE_DELETE")
    void deactivatePage_withDeleteAuthority_returns200() throws Exception {
        mockMvc.perform(put("/api/pages/1/deactivate"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "PERM_WRONG")
    void deactivatePage_withoutDeleteAuthority_returns403() throws Exception {
        mockMvc.perform(put("/api/pages/1/deactivate"))
            .andExpect(status().isForbidden());
    }

    // ─── PUT /api/pages/{id}/reactivate ──────────────────────────────────────

    @Test
    @WithMockUser(authorities = "PERM_PAGE_UPDATE")
    void reactivatePage_withUpdateAuthority_returns200() throws Exception {
        mockMvc.perform(put("/api/pages/1/reactivate"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "PERM_WRONG")
    void reactivatePage_withoutUpdateAuthority_returns403() throws Exception {
        mockMvc.perform(put("/api/pages/1/reactivate"))
            .andExpect(status().isForbidden());
    }
}
