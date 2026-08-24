package com.example.security.service;

import com.example.security.config.properties.CookieProperties;
import com.example.security.config.properties.JwtProperties;
import com.example.security.config.properties.SelfServiceTokenProperties;
import com.example.security.dto.ForgotPasswordRequest;
import com.example.security.entity.PasswordResetToken;
import com.example.security.entity.UserAccount;
import com.example.security.event.PasswordResetRequestedEvent;
import com.example.security.repository.AccountActivationTokenRepository;
import com.example.security.repository.PasswordResetTokenRepository;
import com.example.security.repository.RefreshTokenRepository;
import com.example.security.repository.SecRoleBranchRepository;
import com.example.security.repository.UserAccountRepository;
import com.example.security.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RULE-SEC-038/039/031 regression coverage for {@link AuthService#forgotPassword} — token
 * expiration, prior-token invalidation, anti-enumeration, and that the built email contextData
 * (not the raw token) is what ends up on the published event.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final long RESET_TTL_SECONDS = 3600;

    @Mock private AuthenticationManager authManager;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private JwtService jwt;
    @Mock private RefreshTokenRepository refreshTokenRepo;
    @Mock private UserAccountRepository userAccountRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AccountActivationTokenRepository accountActivationTokenRepo;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepo;
    @Mock private SecRoleBranchRepository secRoleBranchRepo;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private PasswordResetEmailContextBuilder passwordResetEmailContextBuilder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        SelfServiceTokenProperties selfServiceTokenProperties =
                new SelfServiceTokenProperties(86400, RESET_TTL_SECONDS);
        JwtProperties jwtProperties = new JwtProperties("test-secret-at-least-32-characters-long!!", 3600, 604800);
        CookieProperties cookieProperties = new CookieProperties(null, "/", false, true, "Lax");

        authService = new AuthService(authManager, userDetailsService, jwt, refreshTokenRepo, userAccountRepo,
                passwordEncoder, accountActivationTokenRepo, passwordResetTokenRepo, secRoleBranchRepo,
                eventPublisher, passwordResetEmailContextBuilder, jwtProperties, cookieProperties,
                selfServiceTokenProperties);
    }

    @Test
    void forgotPassword_unknownEmail_doesNothing_noEventPublished() {
        when(userAccountRepo.findByEmailIgnoreCase("nobody@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword(new ForgotPasswordRequest("nobody@example.com"));

        verify(passwordResetTokenRepo, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void forgotPassword_knownEmail_issuesTokenWithConfiguredExpiration_andPublishesBuiltContextData() {
        UserAccount user = UserAccount.builder().id(42L).username("jdoe").email("jdoe@example.com").build();
        when(userAccountRepo.findByEmailIgnoreCase("jdoe@example.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepo.findByUser_IdAndUsedFlFalseAndExpiresAtAfter(eq(42L), any()))
                .thenReturn(Collections.emptyList());
        Map<String, Object> builtContext = Map.of("applicationName", "ERP System");
        when(passwordResetEmailContextBuilder.build(eq(user), any(), any())).thenReturn(builtContext);

        Instant before = Instant.now();
        authService.forgotPassword(new ForgotPasswordRequest("jdoe@example.com"));
        Instant after = Instant.now();

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepo).save(tokenCaptor.capture());
        PasswordResetToken savedToken = tokenCaptor.getValue();

        assertThat(savedToken.getToken()).isNotBlank();
        assertThat(savedToken.getUser()).isEqualTo(user);
        assertThat(savedToken.isUsed()).isFalse();
        assertThat(savedToken.getExpiresAt())
                .isAfterOrEqualTo(before.plusSeconds(RESET_TTL_SECONDS))
                .isBeforeOrEqualTo(after.plusSeconds(RESET_TTL_SECONDS));

        ArgumentCaptor<PasswordResetRequestedEvent> eventCaptor = ArgumentCaptor.forClass(PasswordResetRequestedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().userIdFk()).isEqualTo(42L);
        assertThat(eventCaptor.getValue().contextData()).isEqualTo(builtContext);
    }

    @Test
    void forgotPassword_invalidatesUnexpiredPriorTokens_onReissue() {
        UserAccount user = UserAccount.builder().id(9L).username("asmith").email("asmith@example.com").build();
        when(userAccountRepo.findByEmailIgnoreCase("asmith@example.com")).thenReturn(Optional.of(user));

        PasswordResetToken priorToken = PasswordResetToken.builder()
                .token("old-token").user(user).expiresAt(Instant.now().plusSeconds(1800)).usedFl(false).build();
        when(passwordResetTokenRepo.findByUser_IdAndUsedFlFalseAndExpiresAtAfter(eq(9L), any()))
                .thenReturn(List.of(priorToken));
        when(passwordResetEmailContextBuilder.build(any(), any(), any())).thenReturn(Map.of());

        authService.forgotPassword(new ForgotPasswordRequest("asmith@example.com"));

        assertThat(priorToken.isUsed()).isTrue();
        verify(passwordResetTokenRepo).saveAll(List.of(priorToken));
    }
}
