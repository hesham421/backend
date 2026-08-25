package com.erp.security.service;

import com.erp.security.config.properties.PasswordResetEmailProperties;
import com.erp.security.entity.SecUserProfile;
import com.erp.security.entity.UserAccount;
import com.erp.security.repository.SecUserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

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
 *
 * <p>The NOTIF template is now real HTML (dir="rtl"/"ltr" markup) and
 * {@code NotificationEventProcessor.renderBody()} substitutes {{placeholder}} values with a
 * plain, unescaped {@code String.valueOf(...)} — so any value here that can contain
 * user-supplied text (a profile's display name) is HTML-escaped before being put in the map,
 * to stop a name like {@code <script>} from being interpreted as markup in the rendered email.
 * {@code applicationName}/expiry strings are operator config or our own formatting, not
 * user-supplied, and are left as-is.
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
        contextData.put("recipientNameAr", HtmlUtils.htmlEscape(nameOrFallback(profile == null ? null : profile.getFullNameAr(), fallbackName)));
        contextData.put("recipientNameEn", HtmlUtils.htmlEscape(nameOrFallback(profile == null ? null : profile.getFullNameEn(), fallbackName)));
        contextData.put("resetUrl", HtmlUtils.htmlEscape(buildResetUrl(token)));
        contextData.put("resetExpiryFormattedAr", EXPIRY_FORMAT.withLocale(Locale.forLanguageTag("ar")).withZone(zone).format(expiresAt));
        contextData.put("resetExpiryFormattedEn", EXPIRY_FORMAT.withLocale(Locale.ENGLISH).withZone(zone).format(expiresAt));
        contextData.put("supportEmail", HtmlUtils.htmlEscape(properties.supportEmail() == null ? "" : properties.supportEmail()));
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
