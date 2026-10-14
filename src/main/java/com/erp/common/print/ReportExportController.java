package com.erp.common.print;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The one export endpoint of every screen. Returns the file itself, not the {@code ApiResponse}
 * envelope (as {@code FileController.download}); errors stay the JSON envelope.
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Report printing", description = "Print any screen's report as PDF or Excel - طباعة التقارير")
public class ReportExportController {

    private final ReportPrintService service;

    @PostMapping("/{code}/export")
    @Operation(summary = "Print a report",
        description = "Every row matching the criteria, as PDF or XLSX in the Accept-Language language")
    public ResponseEntity<byte[]> export(@PathVariable String code,
                                         @RequestParam(defaultValue = "pdf") String format,
                                         @RequestBody(required = false) ReportExportRequest request) {
        PrintedFile file = service.export(code, format, request == null ? null : request.params());
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(file.contentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(file.fileName(), StandardCharsets.UTF_8).build().toString())
            .header("X-Content-Type-Options", "nosniff")
            .contentLength(file.content().length)
            .body(file.content());
    }
}
