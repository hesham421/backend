package com.erp.common.print;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.CommonErrorCodes;
import com.erp.common.exception.LocalizedException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Component;

/** Every {@link ReportProvider} bean by code; a duplicate code fails the startup. */
@Component
public class ReportRegistry {

    private final Map<String, ReportProvider> providers = new TreeMap<>();

    public ReportRegistry(List<ReportProvider> providers) {
        for (ReportProvider provider : providers) {
            ReportProvider previous = this.providers.putIfAbsent(provider.code(), provider);
            if (previous != null) {
                throw new IllegalStateException("Duplicate report code " + provider.code() + ": "
                    + previous.getClass().getName() + " and " + provider.getClass().getName());
            }
        }
    }

    /** The provider of {@code code}; 404 {@code REPORT_NOT_FOUND} when none. */
    public ReportProvider get(String code) {
        ReportProvider provider = code == null ? null : providers.get(code);
        if (provider == null) {
            throw new LocalizedException(Status.NOT_FOUND, CommonErrorCodes.REPORT_NOT_FOUND, code);
        }
        return provider;
    }
}
