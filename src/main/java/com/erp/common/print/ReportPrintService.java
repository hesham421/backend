package com.erp.common.print;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.CommonErrorCodes;
import com.erp.common.exception.LocalizedException;
import com.erp.common.util.SecurityContextHelper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Prints a registered report as PDF or XLSX in the request's language: either a designed template filled
 * by its own SQL ({@link TemplateReportProvider}) or the generic layout over the rows a provider read
 * through its module's service. The provider applies the screen's permission. Each print is logged.
 */
@Service
@Slf4j
public class ReportPrintService {

    /** Optional criteria text printed after the title, e.g. the applied branch. */
    public static final String SUBTITLE = "subtitle";

    private static final String COMMON_BUNDLE = "reports/common/i18n/common";
    private static final ResourceBundle.Control NO_FALLBACK =
        ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);

    private final ReportRegistry registry;
    private final TabularReportLayout layout;
    private final TemplatePrinter templatePrinter;
    private final String companyNameAr;
    private final String companyNameEn;
    private final int maxRows;
    private final byte[] logo;

    public ReportPrintService(ReportRegistry registry, TabularReportLayout layout, TemplatePrinter templatePrinter,
                              ResourceLoader resourceLoader,
                              @Value("${app.print.company-name-ar:}") String companyNameAr,
                              @Value("${app.print.company-name-en:}") String companyNameEn,
                              @Value("${app.print.logo-path:classpath:reports/common/logo.png}") String logoPath,
                              @Value("${app.print.max-rows:10000}") int maxRows) {
        this.registry = registry;
        this.layout = layout;
        this.templatePrinter = templatePrinter;
        this.companyNameAr = companyNameAr;
        this.companyNameEn = companyNameEn;
        this.maxRows = maxRows;
        this.logo = readLogo(resourceLoader, logoPath);
    }

    @PreAuthorize("isAuthenticated()")
    public PrintedFile export(String code, String format, Map<String, Object> params) {
        long started = System.nanoTime();
        PrintFormat printFormat = PrintFormat.from(format);
        ReportProvider provider = registry.get(code);
        ReportParams criteria = new ReportParams(params, maxRows);
        Locale locale = LocaleContextHolder.getLocale();
        boolean rtl = "ar".equals(locale.getLanguage());

        Map<String, Object> parameters = new HashMap<>();
        PrintedReport report = provider instanceof TemplateReportProvider template
            ? templateReport(template, criteria, locale, rtl, parameters)
            : tabularReport(provider, criteria, locale, rtl, parameters);
        String company = rtl ? companyNameAr : companyNameEn;
        parameters.put(TabularReportLayout.COMPANY_NAME, company.isBlank() ? companyNameAr : company);
        if (logo.length > 0) {
            parameters.put(TabularReportLayout.COMPANY_LOGO, logo);
        }
        parameters.put(TabularReportLayout.PRINTED_BY, SecurityContextHelper.getCurrentUsername());
        parameters.put(TabularReportLayout.PRINTED_AT, new Date());
        parameters.put(JRParameter.REPORT_LOCALE, locale);
        if (printFormat == PrintFormat.XLSX) {
            parameters.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
        }

        byte[] content;
        int pages;
        try {
            JasperPrint print = report.filler().fill(parameters);
            pages = print.getPages().size();
            content = PrintExporters.export(print, printFormat);
        } catch (JRException | RuntimeException e) {
            log.error("Printing report {} as {} failed", code, printFormat, e);
            throw new LocalizedException(Status.INTERNAL_ERROR, CommonErrorCodes.PRINT_GENERATION_FAILED);
        }
        log.info("PRINT report={} source={} format={} rows={} pages={} lang={} user={} ms={}", code, report.source(),
            printFormat, report.rows(), pages, locale.getLanguage(), SecurityContextHelper.getCurrentUsername(),
            (System.nanoTime() - started) / 1_000_000);
        return new PrintedFile(content, printFormat.contentType(), code + "." + printFormat.extension());
    }

    /** A designed template: the provider checks and validates first, then the template's own SQL reads live. */
    private PrintedReport templateReport(TemplateReportProvider provider, ReportParams criteria, Locale locale,
                                         boolean rtl, Map<String, Object> parameters) {
        parameters.putAll(provider.parameters(criteria));
        parameters.put(TabularReportLayout.REPORT_TITLE, rtl ? provider.titleAr() : provider.titleEn());
        parameters.put(JRParameter.REPORT_RESOURCE_BUNDLE, TemplatePrinter.labels(provider.templatePath(), locale));
        return new PrintedReport("template " + provider.templatePath(), "sql",
            filled -> templatePrinter.fill(templatePrinter.report(provider.templatePath(), rtl), filled));
    }

    /** The generic layout over the rows the provider read through its module's service. */
    private PrintedReport tabularReport(ReportProvider provider, ReportParams criteria, Locale locale, boolean rtl,
                                        Map<String, Object> parameters) {
        ReportResult result = provider.run(criteria);
        if (result.rows().size() > maxRows) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, CommonErrorCodes.REPORT_EXPORT_TOO_LARGE, maxRows);
        }
        TabularReportLayout.TabularDocument document = layout.build(provider, result, locale, rtl);
        parameters.putAll(document.parameters());
        String subtitle = criteria.text(SUBTITLE);
        if (subtitle != null) {
            parameters.put(TabularReportLayout.REPORT_TITLE, parameters.get(TabularReportLayout.REPORT_TITLE) + " — " + subtitle);
        }
        parameters.put(JRParameter.REPORT_RESOURCE_BUNDLE, labels(locale));
        return new PrintedReport("tabular", String.valueOf(result.rows().size()),
            filled -> JasperFillManager.fillReport(document.report(), filled, document.dataSource()));
    }

    private record PrintedReport(String source, String rows, Filler filler) {
    }

    @FunctionalInterface
    private interface Filler {
        JasperPrint fill(Map<String, Object> parameters) throws JRException;
    }

    private static ResourceBundle labels(Locale locale) {
        try {
            return ResourceBundle.getBundle(COMMON_BUNDLE, locale, ReportPrintService.class.getClassLoader(), NO_FALLBACK);
        } catch (MissingResourceException e) {
            return ResourceBundle.getBundle(COMMON_BUNDLE, Locale.ROOT, ReportPrintService.class.getClassLoader(), NO_FALLBACK);
        }
    }

    private static byte[] readLogo(ResourceLoader resourceLoader, String location) {
        if (location == null || location.isBlank()) {
            return new byte[0];
        }
        Resource resource = resourceLoader.getResource(location);
        try (InputStream in = resource.getInputStream()) {
            return in.readAllBytes();
        } catch (IOException e) {
            log.warn("Print logo {} cannot be read ({}); reports print without a logo", location, e.getMessage());
            return new byte[0];
        }
    }
}
