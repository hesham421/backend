package com.erp.common.print;

import java.util.Map;

/**
 * A report printed from a designed template ({@code classpath:reports/<templatePath>.jrxml}) whose own SQL
 * query reads the database live. {@link #parameters} must check the permission and validate the criteria
 * exactly as the screen's service does, then return the template's parameters.
 */
public interface TemplateReportProvider extends ReportProvider {

    /** Path under {@code reports/} without extension, e.g. {@code fin/trial-balance}. */
    String templatePath();

    /** The template's parameters (SQL criteria, label maps, criteria text) for the screen's criteria. */
    Map<String, Object> parameters(ReportParams params);

    @Override
    default ReportResult run(ReportParams params) {
        throw new UnsupportedOperationException(code() + " prints from its template " + templatePath());
    }
}
