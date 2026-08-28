package com.erp.masterdata.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.masterdata.dto.LookupCacheEntry;
import com.erp.masterdata.dto.LookupValueResponse;
import com.erp.masterdata.exception.MasterDataErrorCodes;
import com.erp.masterdata.repository.MasterLookupRepository;
import com.erp.masterdata.repository.projection.LookupValueProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Provides generic, cached, read-only lookup-value access from one native JOIN query (avoids
 * N+1). Other in-JVM modules must go through {@link
 * com.erp.masterdata.crossmodule.MasterDataLookupApi} instead of injecting this class directly.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LookupConsumptionService {

    private final MasterLookupRepository masterLookupRepository;

    /**
     * Self-reference via Spring proxy so @Cacheable is intercepted correctly.
     * Injected lazily to avoid circular-dependency issues.
     */
    @Lazy
    @Autowired
    private LookupConsumptionService self;

    // ── Public API ─────────────────────────────────────────────

    /**
     * Delegates to {@link #loadCachedEntry(String)} through the Spring proxy so {@code @Cacheable}
     * is actually intercepted, caching a plain {@link LookupCacheEntry} instead of the
     * non-serializable {@code ServiceResult} wrapper.
     */
    public ServiceResult<List<LookupValueResponse>> fetchLookupValues(String lookupCode) {
        String key = normalize(lookupCode);
        log.debug("Fetching lookup values for key='{}'", key);

        LookupCacheEntry cached = self.loadCachedEntry(key);

        if (cached == null) {
            log.warn("Lookup not found: key='{}'", key);
            return ServiceResult.notFound("Lookup code does not exist: " + lookupCode);
        }

        if (cached.isInactive()) {
            log.warn("Lookup is inactive: key='{}'", key);
            return ServiceResult.notFound("Lookup code is inactive: " + lookupCode);
        }

        List<LookupValueResponse> values = cached.getValues();
        log.debug("Returning {} active values for key='{}'", values.size(), key);
        return ServiceResult.success(values, Status.SUCCESS);
    }

    /**
     * Returns a {@link LookupCacheEntry} (null = not found; inactive flag; or values) so Jackson can
     * (de)serialize the Redis cache entry.
     */
    @Cacheable(cacheNames = "lookupValues", key = "#key")
    public LookupCacheEntry loadCachedEntry(String key) {
        List<LookupValueProjection> rows = masterLookupRepository.findLookupValuesByKey(key, 1);

        if (rows.isEmpty()) {
            return null; // not found — not stored in Redis (disableCachingNullValues)
        }

        LookupValueProjection first = rows.get(0);
        if (first.getMasterIsActive() == null || first.getMasterIsActive() != 1) {
            return new LookupCacheEntry(true, List.of()); // inactive
        }

        List<LookupValueResponse> values = rows.stream()
                .filter(r -> r.getCode() != null)
                .map(this::toResponse)
                .toList();
        return new LookupCacheEntry(false, values); // active
    }

    /**
     * Single COUNT query with JOIN — no N+1.
     */
    public boolean isValid(String lookupCode, String value) {
        if (lookupCode == null || value == null || value.isBlank()) {
            return false;
        }
        String key = normalize(lookupCode);
        String code = value.trim().toUpperCase();
        log.debug("Validating lookup: key='{}', code='{}'", key, code);

        return masterLookupRepository.countActiveByKeyAndCode(key, code) > 0;
    }

    public void validateOrThrow(String lookupCode, String value) {
        if (!isValid(lookupCode, value)) {
            throw new LocalizedException(
                    Status.BAD_REQUEST,
                    MasterDataErrorCodes.LOOKUP_VALUE_INVALID,
                    lookupCode,
                    value
            );
        }
    }

    // ── Private helpers ──────────────────────────────────────────

    public Optional<Integer> getSortOrder(String lookupCode, String detailCode) {
        if (lookupCode == null || detailCode == null) {
            return Optional.empty();
        }
        String key = normalize(lookupCode);
        String normalizedCode = detailCode.trim().toUpperCase();

        List<LookupValueProjection> rows = masterLookupRepository.findLookupValuesByKey(key, 1);
        return rows.stream()
                .filter(r -> r.getCode() != null && r.getCode().equalsIgnoreCase(normalizedCode))
                .findFirst()
                .map(LookupValueProjection::getSortOrder);
    }

    // ── Private helpers (internal) ───────────────────────────────

    private LookupValueResponse toResponse(LookupValueProjection row) {
        return LookupValueResponse.builder()
                .code(row.getCode())
                .label(row.getNameAr())
                .labelEn(row.getNameEn())
                .sortOrder(row.getSortOrder())
                .build();
    }

    private String normalize(String lookupCode) {
        return lookupCode == null ? null : lookupCode.trim().toUpperCase();
    }
}
