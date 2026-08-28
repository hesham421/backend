package com.erp.org.service.support;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.org.exception.OrgErrorCodes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

/**
 * TODO XM-ORG-1 DEFERRED: stand-in for NumberingEngine's generate endpoint, which doesn't exist
 * yet anywhere in this workspace. Sequential, zero-padded, collision-checked — the single call
 * point every entity's Service uses, so the eventual swap touches one class.
 */
@Component
@Slf4j
public class OrgNumberGenerator {

    private static final int SEQUENCE_WIDTH = 5;
    private static final int MAX_ATTEMPTS = 20;

    /**
     * Generates the next <prefix><NNNNN> code not already taken. seed is typically the current row
     * count in scope; existsByCode must be scoped to the same uniqueness boundary as the entity's
     * unique constraint.
     */
    public String next(String prefix, long seed, Predicate<String> existsByCode) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String code = prefix + String.format("%0" + SEQUENCE_WIDTH + "d", seed + attempt);
            if (!existsByCode.test(code)) {
                return code;
            }
        }
        log.warn("Number generation exhausted {} attempts for prefix={}", MAX_ATTEMPTS, prefix);
        throw new LocalizedException(Status.CONFLICT, OrgErrorCodes.CODE_GENERATION_CONFLICT);
    }

    /**
     * Extracts a parent's own suffix (not its full code) since every generated code ends in a
     * fixed-width sequence — deeper entities build their prefix from this to avoid the prefix
     * growing with hierarchy depth and overflowing VARCHAR(20).
     */
    public String parentSuffix(String parentCode) {
        return parentCode.length() >= SEQUENCE_WIDTH
            ? parentCode.substring(parentCode.length() - SEQUENCE_WIDTH)
            : parentCode;
    }
}
