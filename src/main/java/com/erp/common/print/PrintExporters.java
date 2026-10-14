package com.erp.common.print;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import net.sf.jasperreports.export.XlsReportConfiguration;
import net.sf.jasperreports.pdf.JRPdfExporter;
import net.sf.jasperreports.pdf.SimplePdfExporterConfiguration;

/** Writes a filled report as PDF (Arabic shaped, fonts embedded) or XLSX (typed cells, right-to-left sheet). */
final class PrintExporters {

    /** Excel's limit on a sheet name. */
    private static final int MAX_SHEET_NAME = 31;

    private PrintExporters() {
        throw new UnsupportedOperationException("Utility class — cannot be instantiated");
    }

    static byte[] export(JasperPrint print, PrintFormat format) throws JRException {
        return format == PrintFormat.PDF ? pdf(print) : xlsx(print);
    }

    private static byte[] pdf(JasperPrint print) throws JRException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(print));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
        SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
        configuration.setMetadataTitle(print.getName());
        configuration.setMetadataCreator("erp-system");
        exporter.setConfiguration(configuration);
        exporter.exportReport();
        return out.toByteArray();
    }

    private static byte[] xlsx(JasperPrint print) throws JRException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setExporterInput(new SimpleExporterInput(print));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setDetectCellType(true);
        configuration.setRemoveEmptySpaceBetweenRows(true);
        configuration.setRemoveEmptySpaceBetweenColumns(true);
        configuration.setOnePagePerSheet(false);
        configuration.setWhitePageBackground(false);
        configuration.setSheetNames(new String[] {sheetName(print.getName())});
        exporter.setConfiguration(configuration);
        exporter.exportReport();
        boolean rtl = "RTL".equalsIgnoreCase(print.getProperty(XlsReportConfiguration.PROPERTY_SHEET_DIRECTION));
        return rtl ? rightToLeft(out.toByteArray()) : out.toByteArray();
    }

    /** JasperReports 7's XLSX exporter ignores the sheet direction, so each sheet view is marked right-to-left here. */
    static byte[] rightToLeft(byte[] xlsx) throws JRException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(xlsx.length + 64);
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(xlsx));
             ZipOutputStream zip = new ZipOutputStream(out)) {
            for (ZipEntry entry = in.getNextEntry(); entry != null; entry = in.getNextEntry()) {
                byte[] content = in.readAllBytes();
                if (entry.getName().startsWith("xl/worksheets/sheet") && entry.getName().endsWith(".xml")) {
                    content = new String(content, StandardCharsets.UTF_8)
                        .replace("<sheetView ", "<sheetView rightToLeft=\"1\" ").getBytes(StandardCharsets.UTF_8);
                }
                zip.putNextEntry(new ZipEntry(entry.getName()));
                zip.write(content);
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new JRException("Cannot mark the XLSX sheets right-to-left", e);
        }
        return out.toByteArray();
    }

    /** Excel rejects {@code [ ] : * ? / \} in a sheet name and more than 31 characters. */
    static String sheetName(String reportName) {
        String name = reportName == null || reportName.isBlank()
            ? "Report" : reportName.replaceAll("[\\[\\]:*?/\\\\]", "_");
        return name.length() > MAX_SHEET_NAME ? name.substring(0, MAX_SHEET_NAME) : name;
    }
}
