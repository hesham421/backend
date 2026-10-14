package com.erp.fin.report;

import com.erp.common.print.ReportParams;
import com.erp.mdl.crossmodule.LookupOptionView;
import com.erp.mdl.crossmodule.MdlLookupApi;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Prints an MDL lookup code as its label in the print language, as the screens show it; an unknown code prints as itself. */
@Component
@RequiredArgsConstructor
@Slf4j
public class FinReportLabels {

    private final MdlLookupApi mdlLookupApi;

    public UnaryOperator<String> of(String typeKey, ReportParams params) {
        Map<String, String> labels = map(typeKey, params);
        return code -> code == null ? null : labels.getOrDefault(code, code);
    }

    /** Code → label, for a print template's label map parameter. */
    public Map<String, String> map(String typeKey, ReportParams params) {
        Map<String, String> labels = new HashMap<>();
        try {
            for (LookupOptionView value : mdlLookupApi.readActiveValuesByKey(typeKey)) {
                labels.put(value.code(), params.localized(value.labelAr(), value.labelEn()));
            }
        } catch (RuntimeException e) {
            log.debug("Lookup {} unavailable for printing: {}", typeKey, e.getMessage());
        }
        return labels;
    }
}
