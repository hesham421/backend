package com.erp.common.print;

/**
 * A printable report. A module registers one by exposing a Spring bean; {@link ReportRegistry}
 * collects them (a duplicate {@link #code()} fails the startup) and {@code POST
 * /api/v1/reports/{code}/export} prints it. {@link #run} must read through the module's own service,
 * so the screen's permission, filters and rules apply unchanged.
 */
public interface ReportProvider {

    /** Unique code, upper snake case, e.g. {@code FIN_TRIAL_BALANCE}. */
    String code();

    String titleAr();

    String titleEn();

    /** Every row to print for {@code params} (the screen's current criteria). */
    ReportResult run(ReportParams params);
}
