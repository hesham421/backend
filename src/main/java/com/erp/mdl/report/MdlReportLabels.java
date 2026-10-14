package com.erp.mdl.report;

import com.erp.common.print.ReportParams;

/** Labels the MDL screens show from their own dictionary, printed with the same text. */
final class MdlReportLabels {

    private MdlReportLabels() {
    }

    /** The screens' status badge: inactive only when the flag is {@code false}. */
    static String activeStatus(Boolean isActiveFl, ReportParams params) {
        return Boolean.FALSE.equals(isActiveFl)
            ? params.localized("معطّل", "INACTIVE")
            : params.localized("نشط", "ACTIVE");
    }
}
