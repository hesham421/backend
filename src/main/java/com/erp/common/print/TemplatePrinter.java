package com.erp.common.print;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRGroup;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignElement;
import net.sf.jasperreports.engine.design.JRDesignFrame;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JRDesignTextElement;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.HorizontalImageAlignEnum;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.XlsReportConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Fills a designed template ({@code reports/<path>.jrxml}) from its own SQL on the application's connection,
 * inside a read-only transaction. One template serves both languages: for Arabic its layout is mirrored
 * (positions and left/right alignments) before compiling. Labels: {@code reports/<folder>/i18n/<folder>_<lang>}
 * in front of the common print labels. Compiled templates are cached per path and direction.
 */
@Component
public class TemplatePrinter {

    private static final String COMMON_BUNDLE = "reports/common/i18n/common";
    private static final ResourceBundle.Control NO_FALLBACK =
        ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);

    private final ResourceLoader resourceLoader;
    private final DataSource dataSource;
    private final TransactionTemplate readOnly;
    private final Map<String, JasperReport> compiled = new ConcurrentHashMap<>();

    public TemplatePrinter(ResourceLoader resourceLoader, DataSource dataSource,
                           PlatformTransactionManager transactionManager) {
        this.resourceLoader = resourceLoader;
        this.dataSource = dataSource;
        this.readOnly = new TransactionTemplate(transactionManager);
        this.readOnly.setReadOnly(true);
    }

    /** The compiled template, mirrored when {@code rtl}. */
    public JasperReport report(String templatePath, boolean rtl) {
        return compiled.computeIfAbsent(templatePath + (rtl ? ":rtl" : ":ltr"), key -> compile(templatePath, rtl));
    }

    /** Fills {@code report} with its own query on the application's connection. */
    public JasperPrint fill(JasperReport report, Map<String, Object> parameters) {
        return readOnly.execute(status -> {
            Connection connection = DataSourceUtils.getConnection(dataSource);
            try {
                return JasperFillManager.fillReport(report, parameters, connection);
            } catch (JRException e) {
                throw new JRRuntimeException(e);
            } finally {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        });
    }

    /** The template's own labels in front of the common print labels. */
    public static ResourceBundle labels(String templatePath, Locale locale) {
        int slash = templatePath.indexOf('/');
        String folder = slash < 0 ? templatePath : templatePath.substring(0, slash);
        return new Chained(bundle("reports/" + folder + "/i18n/" + folder, locale), bundle(COMMON_BUNDLE, locale), locale);
    }

    private JasperReport compile(String templatePath, boolean rtl) {
        Resource resource = resourceLoader.getResource("classpath:reports/" + templatePath + ".jrxml");
        try (InputStream in = resource.getInputStream()) {
            JasperDesign design = JRXmlLoader.load(in);
            if (rtl) {
                mirror(design);
                design.setProperty(XlsReportConfiguration.PROPERTY_SHEET_DIRECTION, "RTL");
            }
            return JasperCompileManager.compileReport(design);
        } catch (IOException | JRException e) {
            throw new IllegalStateException("Cannot compile the print template " + templatePath, e);
        }
    }

    /** Right-to-left: every element's position is mirrored within its band, and left/right alignments swap. */
    static void mirror(JasperDesign design) {
        int width = design.getColumnWidth();
        Set<JRBand> bands = new LinkedHashSet<>();
        Collections.addAll(bands, design.getTitle(), design.getPageHeader(), design.getColumnHeader(),
            design.getColumnFooter(), design.getPageFooter(), design.getLastPageFooter(), design.getSummary(),
            design.getNoData(), design.getBackground());
        Collections.addAll(bands, design.getDetailSection().getBands());
        for (JRGroup group : design.getGroups()) {
            Collections.addAll(bands, group.getGroupHeaderSection().getBands());
            Collections.addAll(bands, group.getGroupFooterSection().getBands());
        }
        for (JRBand band : bands) {
            if (band != null) {
                mirror(band.getElements(), width);
            }
        }
    }

    private static void mirror(JRElement[] elements, int width) {
        for (JRElement element : elements) {
            JRDesignElement designElement = (JRDesignElement) element;
            designElement.setX(width - element.getX() - element.getWidth());
            if (element instanceof JRDesignTextElement text) {
                HorizontalTextAlignEnum align = text.getOwnHorizontalTextAlign();
                if (align == null || align == HorizontalTextAlignEnum.LEFT) {
                    text.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
                } else if (align == HorizontalTextAlignEnum.RIGHT) {
                    text.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
                }
            } else if (element instanceof JRDesignImage image) {
                HorizontalImageAlignEnum align = image.getOwnHorizontalImageAlign();
                image.setHorizontalImageAlign(align == HorizontalImageAlignEnum.RIGHT
                    ? HorizontalImageAlignEnum.LEFT : HorizontalImageAlignEnum.RIGHT);
            } else if (element instanceof JRDesignFrame frame) {
                mirror(frame.getElements(), frame.getWidth());
            }
        }
    }

    private static ResourceBundle bundle(String baseName, Locale locale) {
        try {
            return ResourceBundle.getBundle(baseName, locale, TemplatePrinter.class.getClassLoader(), NO_FALLBACK);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    /** Looks a key up in the template's labels, then in the common ones. */
    private static final class Chained extends ResourceBundle {

        private final ResourceBundle first;
        private final ResourceBundle second;
        private final Locale locale;

        private Chained(ResourceBundle first, ResourceBundle second, Locale locale) {
            this.first = first;
            this.second = second;
            this.locale = locale;
        }

        @Override
        protected Object handleGetObject(String key) {
            if (first != null && first.containsKey(key)) {
                return first.getObject(key);
            }
            return second != null && second.containsKey(key) ? second.getObject(key) : null;
        }

        @Override
        public Enumeration<String> getKeys() {
            Set<String> keys = new LinkedHashSet<>();
            if (first != null) {
                keys.addAll(first.keySet());
            }
            if (second != null) {
                keys.addAll(second.keySet());
            }
            return Collections.enumeration(keys);
        }

        @Override
        public Locale getLocale() {
            return locale;
        }
    }
}
