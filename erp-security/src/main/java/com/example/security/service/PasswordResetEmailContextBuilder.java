package com.example.security.service;

import com.example.security.config.properties.PasswordResetEmailProperties;
import com.example.security.entity.SecUserProfile;
import com.example.security.entity.UserAccount;
import com.example.security.repository.SecUserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Assembles the notification contextData for the password-reset email (XM-SEC-005) —
 * called from {@link AuthService#forgotPassword} before the event is published. Never logs the
 * token or the assembled map; the token appears only inside {@code resetUrl}, not as a bare
 * standalone value, to keep its footprint in contextData to one field instead of two.
 */
@Component
@RequiredArgsConstructor
public class PasswordResetEmailContextBuilder {

    private static final DateTimeFormatter EXPIRY_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy, h:mm a");

    private final PasswordResetEmailProperties properties;
    private final SecUserProfileRepository secUserProfileRepository;

    public Map<String, Object> build(UserAccount user, String token, Instant expiresAt) {
        SecUserProfile profile = secUserProfileRepository.findById(user.getId()).orElse(null);
        String fallbackName = user.getUsername();
        ZoneId zone = resolveZone();

        Map<String, Object> contextData = new LinkedHashMap<>();
        contextData.put("applicationName", properties.applicationName());
        contextData.put("recipientNameAr", nameOrFallback(profile == null ? null : profile.getFullNameAr(), fallbackName));
        contextData.put("recipientNameEn", nameOrFallback(profile == null ? null : profile.getFullNameEn(), fallbackName));
        contextData.put("resetUrl", buildResetUrl(token));
        contextData.put("resetExpiryFormattedAr", EXPIRY_FORMAT.withLocale(Locale.forLanguageTag("ar")).withZone(zone).format(expiresAt));
        contextData.put("resetExpiryFormattedEn", EXPIRY_FORMAT.withLocale(Locale.ENGLISH).withZone(zone).format(expiresAt));
        contextData.put("supportEmail", properties.supportEmail() == null ? "" : properties.supportEmail());
        return contextData;
    }

    private String nameOrFallback(String name, String fallback) {
        return (name == null || name.isBlank()) ? fallback : name;
    }

    private String buildResetUrl(String token) {
        String base = properties.resetBaseUrl();
        if (base == null || base.isBlank()) {
            return "";
        }
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        return base + (base.contains("?") ? "&" : "?") + "token=" + encodedToken;
    }

    private ZoneId resolveZone() {
        try {
            return ZoneId.of(properties.defaultTimezone());
        } catch (DateTimeException ex) {
            return ZoneId.of("Asia/Muscat");
        }
    }
}
