package com.erp.common.print;

import java.awt.Color;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignElement;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JRDesignLine;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignStyle;
import net.sf.jasperreports.engine.design.JRDesignTextElement;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.EvaluationTimeEnum;
import net.sf.jasperreports.engine.type.HorizontalImageAlignEnum;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.type.OnErrorTypeEnum;
import net.sf.jasperreports.engine.type.OrientationEnum;
import net.sf.jasperreports.engine.type.ScaleImageEnum;
import net.sf.jasperreports.engine.type.StretchTypeEnum;
import net.sf.jasperreports.engine.type.TextAdjustEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;
import net.sf.jasperreports.engine.type.WhenResourceMissingTypeEnum;
import org.springframework.stereotype.Component;

/**
 * Runtime layout of any {@link ReportResult} (no .jrxml): company, logo and title header, localized column
 * headers, rows, totals, "page X of Y"; mirrored for right-to-left, font {@value #FONT}. Heights allow a tall
 * font (a text whose first line does not fit prints nothing). Compiled once per report, columns and language.
 */
@Component
public class TabularReportLayout {

    /** Font family registered by the font extension ({@code reports/fonts/fonts.xml}). */
    public static final String FONT = "ERP Arabic";

    private static final int MARGIN = 20;
    private static final int PORTRAIT_MAX_COLUMNS = 5;
    private static final int HEADER_HEIGHT = 72;
    private static final int COLUMN_HEADER_HEIGHT = 22;
    private static final int ROW_HEIGHT = 18;
    private static final int FOOTER_HEIGHT = 20;
    private static final int LOGO_SIZE = 44;
    private static final int CELL_PADDING = 3;
    private static final float GRID_LINE = 0.5f;
    private static final String HEADER_BACKGROUND = "#E8EDF2";
    private static final String TOTALS_BACKGROUND = "#F4F6F8";
    private static final String FIELD = "F";
    private static final String TOTAL = "T";
    /** Totals whose key is no column, as {@code key: value} text after the totals label. */
    private static final String TOTALS_TEXT = "TOTALS_TEXT";

    /** Parameters every printed report receives. */
    static final String REPORT_TITLE = "REPORT_TITLE";
    static final String COMPANY_NAME = "COMPANY_NAME";
    static final String COMPANY_LOGO = "COMPANY_LOGO";
    static final String PRINTED_BY = "PRINTED_BY";
    static final String PRINTED_AT = "PRINTED_AT";

    private final Map<String, JasperReport> compiled = new ConcurrentHashMap<>();

    /**
     * A compiled layout for {@code result}, its rows as a data source and its own parameters
     * ({@code REPORT_TITLE} and the totals).
     */
    public TabularDocument build(ReportProvider provider, ReportResult result, Locale locale, boolean rtl) {
        List<ReportColumn> columns = result.columns();
        String title = rtl ? provider.titleAr() : provider.titleEn();
        String key = provider.code() + ":" + locale.getLanguage() + ":" + rtl + ":" + Objects.hash(columns, title)
            + ":" + totalsSignature(columns, result.totals());
        JasperReport report = compiled.computeIfAbsent(key, k -> compile(design(provider.code(), columns, result.totals(), rtl)));

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(REPORT_TITLE, title);
        for (int i = 0; i < columns.size(); i++) {
            ReportColumn column = columns.get(i);
            if (result.totals().containsKey(column.key())) {
                parameters.put(TOTAL + i, convert(column.type(), result.totals().get(column.key())));
            }
        }
        parameters.put(TOTALS_TEXT, unmatchedTotals(columns, result.totals()));
        return new TabularDocument(report, dataSource(columns, result.rows()), parameters);
    }

    /** The compiled layout, the rows converted to the field classes, and {@code REPORT_TITLE} plus the totals. */
    public record TabularDocument(JasperReport report, JRDataSource dataSource, Map<String, Object> parameters) {
    }

    private static JRDataSource dataSource(List<ReportColumn> columns, List<Map<String, Object>> rows) {
        List<Map<String, ?>> converted = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Map<String, Object> values = new LinkedHashMap<>();
            for (int i = 0; i < columns.size(); i++) {
                ReportColumn column = columns.get(i);
                values.put(FIELD + i, convert(column.type(), row == null ? null : row.get(column.key())));
            }
            converted.add(values);
        }
        return new JRMapCollectionDataSource(converted);
    }

    /** A row value as an instance of the field class of its column type ({@code null} when it is not one). */
    static Object convert(ColumnType type, Object value) {
        if (value == null) {
            return null;
        }
        return switch (type == null ? ColumnType.STRING : type) {
            case STRING -> value.toString();
            case INTEGER -> toLong(value);
            case DECIMAL -> toDecimal(value);
            case DATE, DATETIME -> toDate(value);
            case BOOLEAN -> value instanceof Boolean flag ? flag : Boolean.valueOf(value.toString().trim());
        };
    }

    private static Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static BigDecimal toDecimal(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        try {
            return new BigDecimal(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Date toDate(Object value) {
        ZoneId zone = ZoneId.systemDefault();
        return switch (value) {
            case Date date -> date;
            case Instant instant -> Date.from(instant);
            case OffsetDateTime dateTime -> Date.from(dateTime.toInstant());
            case ZonedDateTime dateTime -> Date.from(dateTime.toInstant());
            case LocalDateTime dateTime -> Date.from(dateTime.atZone(zone).toInstant());
            case LocalDate date -> Date.from(date.atStartOfDay(zone).toInstant());
            default -> null;
        };
    }

    private static String unmatchedTotals(List<ReportColumn> columns, Map<String, Object> totals) {
        StringBuilder text = new StringBuilder();
        totals.forEach((key, value) -> {
            if (columns.stream().noneMatch(column -> column.key().equals(key))) {
                text.append(text.isEmpty() ? "" : "   ").append(key).append(": ").append(value == null ? "" : value);
            }
        });
        return text.isEmpty() ? null : text.toString();
    }

    private static String totalsSignature(List<ReportColumn> columns, Map<String, Object> totals) {
        StringBuilder signature = new StringBuilder();
        for (ReportColumn column : columns) {
            signature.append(totals.containsKey(column.key()) ? '1' : '0');
        }
        return signature.toString();
    }

    private static JasperReport compile(JasperDesign design) {
        try {
            return JasperCompileManager.compileReport(design);
        } catch (JRException e) {
            throw new IllegalStateException("Cannot compile the print layout " + design.getName(), e);
        }
    }

    private static JasperDesign design(String code, List<ReportColumn> columns, Map<String, Object> totals, boolean rtl) {
        try {
            boolean portrait = columns.size() <= PORTRAIT_MAX_COLUMNS;
            JasperDesign design = new JasperDesign();
            design.setName(code);
            design.setOrientation(portrait ? OrientationEnum.PORTRAIT : OrientationEnum.LANDSCAPE);
            design.setPageWidth(portrait ? 595 : 842);
            design.setPageHeight(portrait ? 842 : 595);
            design.setLeftMargin(MARGIN);
            design.setRightMargin(MARGIN);
            design.setTopMargin(MARGIN);
            design.setBottomMargin(MARGIN);
            int width = design.getPageWidth() - 2 * MARGIN;
            design.setColumnWidth(width);
            design.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
            design.setWhenResourceMissingType(WhenResourceMissingTypeEnum.KEY);
            // XLSX: one header at the top, no repeated page header/footer rows, sheet read right-to-left
            design.setProperty("net.sf.jasperreports.export.xlsx.exclude.origin.keep.first.band.1", "pageHeader");
            design.setProperty("net.sf.jasperreports.export.xlsx.exclude.origin.keep.first.band.2", "columnHeader");
            design.setProperty("net.sf.jasperreports.export.xlsx.exclude.origin.band.3", "pageFooter");
            if (rtl) {
                design.setProperty("net.sf.jasperreports.export.xls.sheet.direction", "RTL");
            }

            JRDesignStyle base = new JRDesignStyle();
            base.setName("ErpBase");
            base.setDefault(true);
            base.setFontName(FONT);
            base.setFontSize(9f);
            design.addStyle(base);

            parameter(design, REPORT_TITLE, String.class);
            parameter(design, COMPANY_NAME, String.class);
            parameter(design, COMPANY_LOGO, Object.class);
            parameter(design, PRINTED_BY, String.class);
            parameter(design, PRINTED_AT, Date.class);
            parameter(design, TOTALS_TEXT, String.class);
            for (int i = 0; i < columns.size(); i++) {
                JRDesignField field = new JRDesignField();
                field.setName(FIELD + i);
                field.setValueClass(fieldClass(columns.get(i).type()));
                design.addField(field);
                if (totals.containsKey(columns.get(i).key())) {
                    parameter(design, TOTAL + i, fieldClass(columns.get(i).type()));
                }
            }

            int[] x = new int[columns.size()];
            int[] w = new int[columns.size()];
            layoutColumns(width, columns.size(), rtl, x, w);

            design.setPageHeader(pageHeader(width, rtl));
            design.setColumnHeader(columnHeader(columns, x, w, rtl));
            ((JRDesignSection) design.getDetailSection()).addBand(detail(columns, x, w, rtl));
            if (!totals.isEmpty()) {
                design.setSummary(totalsBand(columns, totals, x, w, rtl));
            }
            design.setPageFooter(pageFooter(width, rtl));
            return design;
        } catch (JRException e) {
            throw new IllegalStateException("Invalid tabular print layout for " + code, e);
        }
    }

    /** Equal widths, the last column taking the remainder; mirrored for right-to-left. */
    private static void layoutColumns(int width, int count, boolean rtl, int[] x, int[] w) {
        if (count == 0) {
            return;
        }
        int each = width / count;
        int position = 0;
        for (int i = 0; i < count; i++) {
            w[i] = i == count - 1 ? width - position : each;
            x[i] = rtl ? width - position - w[i] : position;
            position += w[i];
        }
    }

    private static JRDesignBand pageHeader(int width, boolean rtl) {
        JRDesignBand band = band(HEADER_HEIGHT);
        JRDesignImage logo = new JRDesignImage(null);
        place(logo, rtl ? width - LOGO_SIZE : 0, 0, LOGO_SIZE, LOGO_SIZE);
        logo.setScaleImage(ScaleImageEnum.RETAIN_SHAPE);
        logo.setHorizontalImageAlign(rtl ? HorizontalImageAlignEnum.RIGHT : HorizontalImageAlignEnum.LEFT);
        logo.setOnErrorType(OnErrorTypeEnum.BLANK);
        logo.setExpression(expression("$P{COMPANY_LOGO}"));
        band.addElement(logo);

        int textWidth = width - 2 * (LOGO_SIZE + 8);
        JRDesignTextField company = textField("$P{COMPANY_NAME}", LOGO_SIZE + 8, 0, textWidth, 26, HorizontalTextAlignEnum.CENTER);
        company.setFontSize(13f);
        company.setBold(true);
        band.addElement(company);

        JRDesignTextField title = textField("$P{REPORT_TITLE}", LOGO_SIZE + 8, 26, textWidth, 22, HorizontalTextAlignEnum.CENTER);
        title.setFontSize(11f);
        title.setBold(true);
        band.addElement(title);

        int half = width / 2;
        HorizontalTextAlignEnum start = rtl ? HorizontalTextAlignEnum.RIGHT : HorizontalTextAlignEnum.LEFT;
        HorizontalTextAlignEnum end = rtl ? HorizontalTextAlignEnum.LEFT : HorizontalTextAlignEnum.RIGHT;
        JRDesignTextField printedBy = textField("$R{printedBy} + \": \" + ($P{PRINTED_BY} == null ? \"\" : $P{PRINTED_BY})",
            rtl ? half : 0, 50, half, 16, start);
        printedBy.setFontSize(8f);
        band.addElement(printedBy);
        JRDesignTextField printedAt = textField("$R{printedAt} + \": \" + ($P{PRINTED_AT} == null ? \"\" : "
                + "new java.text.SimpleDateFormat(\"yyyy-MM-dd HH:mm\").format($P{PRINTED_AT}))",
            rtl ? 0 : half, 50, width - half, 16, end);
        printedAt.setFontSize(8f);
        band.addElement(printedAt);

        JRDesignLine line = new JRDesignLine();
        place(line, 0, HEADER_HEIGHT - 3, width, 1);
        line.getLinePen().setLineWidth(GRID_LINE);
        band.addElement(line);
        return band;
    }

    private static JRDesignBand columnHeader(List<ReportColumn> columns, int[] x, int[] w, boolean rtl) {
        JRDesignBand band = band(COLUMN_HEADER_HEIGHT);
        for (int i = 0; i < columns.size(); i++) {
            ReportColumn column = columns.get(i);
            JRDesignStaticText label = new JRDesignStaticText();
            place(label, x[i], 0, w[i], COLUMN_HEADER_HEIGHT);
            label.setText(rtl ? column.labelAr() : column.labelEn());
            label.setBold(true);
            label.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
            cell(label, HEADER_BACKGROUND);
            band.addElement(label);
        }
        return band;
    }

    private static JRDesignBand detail(List<ReportColumn> columns, int[] x, int[] w, boolean rtl) {
        JRDesignBand band = band(ROW_HEIGHT);
        for (int i = 0; i < columns.size(); i++) {
            ColumnType type = columns.get(i).type();
            JRDesignTextField value = textField(valueExpression(type, "$F{" + FIELD + i + "}"), x[i], 0, w[i], ROW_HEIGHT,
                alignment(type, rtl));
            value.setPattern(pattern(type));
            value.setTextAdjust(TextAdjustEnum.STRETCH_HEIGHT);
            value.setStretchType(StretchTypeEnum.CONTAINER_HEIGHT);
            cell(value, null);
            band.addElement(value);
        }
        return band;
    }

    /** The totals label spans every column before the first total; the row grows with its longest cell. */
    private static JRDesignBand totalsBand(List<ReportColumn> columns, Map<String, Object> totals, int[] x, int[] w,
                                           boolean rtl) {
        JRDesignBand band = band(ROW_HEIGHT);
        int span = 0;
        while (span < columns.size() && !totals.containsKey(columns.get(span).key())) {
            span++;
        }
        boolean labelled = false;
        for (int i = 0; i < columns.size(); i++) {
            ColumnType type = columns.get(i).type();
            JRDesignTextField value;
            if (totals.containsKey(columns.get(i).key())) {
                value = textField(valueExpression(type, "$P{" + TOTAL + i + "}"), x[i], 0, w[i], ROW_HEIGHT, alignment(type, rtl));
                value.setPattern(pattern(type));
            } else if (!labelled) {
                int last = Math.max(i, span - 1);
                int width = 0;
                for (int k = i; k <= last; k++) {
                    width += w[k];
                }
                value = textField("$R{total} + ($P{" + TOTALS_TEXT + "} == null ? \"\" : \": \" + $P{" + TOTALS_TEXT + "})",
                    rtl ? x[last] : x[i], 0, width, ROW_HEIGHT, rtl ? HorizontalTextAlignEnum.RIGHT : HorizontalTextAlignEnum.LEFT);
                value.setTextAdjust(TextAdjustEnum.STRETCH_HEIGHT);
                labelled = true;
                i = last;
            } else {
                value = textField("\"\"", x[i], 0, w[i], ROW_HEIGHT, HorizontalTextAlignEnum.CENTER);
            }
            value.setBold(true);
            value.setStretchType(StretchTypeEnum.CONTAINER_HEIGHT);
            cell(value, TOTALS_BACKGROUND);
            band.addElement(value);
        }
        return band;
    }

    private static JRDesignBand pageFooter(int width, boolean rtl) {
        JRDesignBand band = band(FOOTER_HEIGHT);
        int half = width / 2;
        JRDesignTextField current = textField("$R{page} + \" \" + $V{PAGE_NUMBER} + \" \" + $R{of} + \" \"",
            rtl ? half : half - 120, 2, 120, 16, rtl ? HorizontalTextAlignEnum.LEFT : HorizontalTextAlignEnum.RIGHT);
        current.setFontSize(8f);
        band.addElement(current);
        JRDesignTextField total = textField("\"\" + $V{PAGE_NUMBER}", rtl ? half - 64 : half + 4, 2, 60, 16,
            rtl ? HorizontalTextAlignEnum.RIGHT : HorizontalTextAlignEnum.LEFT);
        total.setEvaluationTime(EvaluationTimeEnum.REPORT);
        total.setFontSize(8f);
        band.addElement(total);
        return band;
    }

    private static Class<?> fieldClass(ColumnType type) {
        return switch (type == null ? ColumnType.STRING : type) {
            case STRING -> String.class;
            case INTEGER -> Long.class;
            case DECIMAL -> BigDecimal.class;
            case DATE, DATETIME -> Date.class;
            case BOOLEAN -> Boolean.class;
        };
    }

    /** Booleans print as the localized yes/no labels; every other value as itself. */
    private static String valueExpression(ColumnType type, String reference) {
        return type == ColumnType.BOOLEAN
            ? reference + " == null ? null : (" + reference + ".booleanValue() ? $R{yes} : $R{no})"
            : reference;
    }

    private static String pattern(ColumnType type) {
        return switch (type == null ? ColumnType.STRING : type) {
            case INTEGER -> "#,##0";
            case DECIMAL -> "#,##0.00";
            case DATE -> "yyyy-MM-dd";
            case DATETIME -> "yyyy-MM-dd HH:mm";
            default -> null;
        };
    }

    /** Text at the reading start, numbers and dates at the reading end, booleans centred. */
    private static HorizontalTextAlignEnum alignment(ColumnType type, boolean rtl) {
        return switch (type == null ? ColumnType.STRING : type) {
            case STRING -> rtl ? HorizontalTextAlignEnum.RIGHT : HorizontalTextAlignEnum.LEFT;
            case BOOLEAN -> HorizontalTextAlignEnum.CENTER;
            default -> rtl ? HorizontalTextAlignEnum.LEFT : HorizontalTextAlignEnum.RIGHT;
        };
    }

    private static void parameter(JasperDesign design, String name, Class<?> type) throws JRException {
        JRDesignParameter parameter = new JRDesignParameter();
        parameter.setName(name);
        parameter.setValueClass(type);
        design.addParameter(parameter);
    }

    private static JRDesignBand band(int height) {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(height);
        return band;
    }

    private static JRDesignTextField textField(String expression, int x, int y, int width, int height,
                                               HorizontalTextAlignEnum align) {
        JRDesignTextField field = new JRDesignTextField();
        place(field, x, y, width, height);
        field.setExpression(expression(expression));
        field.setHorizontalTextAlign(align);
        field.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        field.setBlankWhenNull(true);
        return field;
    }

    /** A bordered table cell with padding and an optional background. */
    private static void cell(JRDesignTextElement element, String background) {
        element.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        element.getLineBox().getPen().setLineWidth(GRID_LINE);
        element.getLineBox().setLeftPadding(CELL_PADDING);
        element.getLineBox().setRightPadding(CELL_PADDING);
        if (background != null) {
            element.setMode(ModeEnum.OPAQUE);
            element.setBackcolor(Color.decode(background));
        }
    }

    private static void place(JRDesignElement element, int x, int y, int width, int height) {
        element.setX(x);
        element.setY(y);
        element.setWidth(width);
        element.setHeight(height);
    }

    private static JRDesignExpression expression(String text) {
        return new JRDesignExpression(text);
    }
}
