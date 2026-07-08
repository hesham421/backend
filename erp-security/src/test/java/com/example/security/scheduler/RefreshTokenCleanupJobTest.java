package com.example.security.scheduler;

import com.example.security.config.properties.RefreshTokenCleanupProperties;
import com.example.security.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenCleanupJobTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenCleanupJob job;

    @BeforeEach
    void setUp() {
        RefreshTokenCleanupProperties properties =
                new RefreshTokenCleanupProperties("0 0 3 * * *", 30);
        job = new RefreshTokenCleanupJob(refreshTokenRepository, properties);
    }

    @Test
    void cleanup_deletesExpiredTokens_regardlessOfRevokedStatus() {
        when(refreshTokenRepository.deleteByExpiresAtBefore(any())).thenReturn(3L);
        when(refreshTokenRepository.deleteByRevokedTrueAndCreatedAtBefore(any())).thenReturn(0L);

        job.cleanup();

        verify(refreshTokenRepository).deleteByExpiresAtBefore(any(Instant.class));
    }

    @Test
    void cleanup_deletesRevokedTokensOlderThanConfiguredRetentionWindow() {
        when(refreshTokenRepository.deleteByExpiresAtBefore(any())).thenReturn(0L);
        when(refreshTokenRepository.deleteByRevokedTrueAndCreatedAtBefore(any())).thenReturn(2L);

        Instant before = Instant.now();
        job.cleanup();
        Instant after = Instant.now();

        ArgumentCaptor<Instant> cutoffCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(refreshTokenRepository).deleteByRevokedTrueAndCreatedAtBefore(cutoffCaptor.capture());

        Instant expectedEarliest = before.minus(30, ChronoUnit.DAYS);
        Instant expectedLatest = after.minus(30, ChronoUnit.DAYS);
        assertThat(cutoffCaptor.getValue()).isBetween(expectedEarliest, expectedLatest);
    }
}
