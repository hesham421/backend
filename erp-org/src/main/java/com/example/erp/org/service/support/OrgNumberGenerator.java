package com.example.erp.org.service.support;

import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.org.exception.OrgErrorCodes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

/**
 * TODO: XM-ORG-1 DEFERRED — replace with a call to NumberingEngine's
 * {@code POST /api/numbering/generate} (RULE-ORG-013, DRV-ORG-008) once the NumberingEngine
 * module is READY. Per master-registry.md (L1-4, "NumberingEngine — NOT STARTED"), no such
 * endpoint exists anywhere in this workspace yet, so this component is a local,
 * functionally-equivalent stand-in: sequential, zero-padded, collision-checked against the
 * repository. It is the single call point every entity's Service uses for RULE-ORG-013
 * ("Business Code via NumberingEngine only") so the eventual swap touches one class.
 */
@Component
@Slf4j
public class OrgNumberGenerator {

    private static final int SEQUENCE_WIDTH = 5;
    private static final int MAX_ATTEMPTS = 20;

    /**
     * Generates the next {@code <prefix><NNNNN>} code not already taken, per {@code existsByCode}.
     *
     * @param prefix      business-code prefix, e.g. {@code "LE-"} or {@code "BR-LE-00001-"}
     * @param seed        starting sequence value — typically the current row count in scope
     * @param existsByCode repository existence check scoped to the same uniqueness boundary
     *                    as the entity's unique constraint
     * @return a generated code guaranteed unused at generation time
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
}
