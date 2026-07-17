package com.example.erp.file.controller;

import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.erp.file.dto.FileAccessTokenResponse;
import com.example.erp.file.dto.FileCategoryOptionResponse;
import com.example.erp.file.dto.FileDeleteConfirmation;
import com.example.erp.file.dto.FileDocumentSummaryResponse;
import com.example.erp.file.dto.FileDownloadResult;
import com.example.erp.file.dto.FileUploadResponse;
import com.example.erp.file.dto.FileUploadTokenRequest;
import com.example.erp.file.dto.FileUploadTokenResponse;
import com.example.erp.file.security.FileTokenFilter;
import com.example.erp.file.security.FileTokenPayload;
import com.example.erp.file.service.FileAccessTokenService;
import com.example.erp.file.service.FileCategoryOptionService;
import com.example.erp.file.service.FileDeleteService;
import com.example.erp.file.service.FileDownloadService;
import com.example.erp.file.service.FileListService;
import com.example.erp.file.service.FileUploadService;
import com.example.erp.file.service.FileUploadTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * File Service controller. Endpoint paths deliberately do NOT share one {@code @RequestMapping}
 * prefix — API-FILE-002/003/004 use the token-embedded {@code /upload/{token}},
 * {@code /download/{token}}, {@code /{token}} routes per the documented STACK-1 deviation
 * (srs-file-001.md B5, ARCH-REF-1.10 AD-FILE-02/03); only API-FILE-001/005 (and the new
 * access-token endpoint) use the standard {@code /api/v1/files/...} pattern.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "File Service", description = "خدمة إدارة الملفات - File Service Management API")
public class FileController {

    private final FileUploadTokenService fileUploadTokenService;
    private final FileAccessTokenService fileAccessTokenService;
    private final FileUploadService fileUploadService;
    private final FileDownloadService fileDownloadService;
    private final FileDeleteService fileDeleteService;
    private final FileListService fileListService;
    private final FileCategoryOptionService fileCategoryOptionService;
    private final OperationCode operationCode;

    @PostMapping("/api/v1/files/upload-token")
    @Operation(summary = "Issue Upload Token", description = "إصدار رمز رفع الملف")
    public ResponseEntity<ApiResponse<FileUploadTokenResponse>> issueUploadToken(
            @Valid @RequestBody FileUploadTokenRequest request) {
        return operationCode.craftResponse(fileUploadTokenService.issueUploadToken(request));
    }

    @PostMapping("/api/v1/files/{fileDocumentPk}/access-token")
    @Operation(summary = "Issue Download/Delete Access Token", description = "إصدار رمز وصول للتنزيل أو الحذف")
    public ResponseEntity<ApiResponse<FileAccessTokenResponse>> issueAccessToken(
            @PathVariable Long fileDocumentPk, @RequestParam String action) {
        return operationCode.craftResponse(fileAccessTokenService.issueAccessToken(fileDocumentPk, action));
    }

    @PostMapping(value = "/upload/{encryptedToken}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload File", description = "رفع ملف")
    public ResponseEntity<ApiResponse<FileUploadResponse>> upload(
            @PathVariable String encryptedToken,
            @RequestParam("file") MultipartFile file,
            @RequestAttribute(FileTokenFilter.TOKEN_PAYLOAD_ATTRIBUTE) FileTokenPayload tokenPayload) {
        return operationCode.craftResponse(fileUploadService.upload(tokenPayload, file));
    }

    @GetMapping("/download/{encryptedToken}")
    @Operation(summary = "Download File", description = "تنزيل الملف")
    public ResponseEntity<byte[]> download(
            @PathVariable String encryptedToken,
            @RequestAttribute(FileTokenFilter.TOKEN_PAYLOAD_ATTRIBUTE) FileTokenPayload tokenPayload) {
        FileDownloadResult result = fileDownloadService.getForDownload(tokenPayload);
        ContentDisposition disposition = ContentDisposition.attachment()
            .filename(result.fileNameOriginal())
            .build();
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(result.mimeType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .body(result.content());
    }

    @DeleteMapping("/{encryptedToken}")
    @Operation(summary = "Delete File", description = "حذف الملف")
    public ResponseEntity<ApiResponse<FileDeleteConfirmation>> delete(
            @PathVariable String encryptedToken,
            @RequestAttribute(FileTokenFilter.TOKEN_PAYLOAD_ATTRIBUTE) FileTokenPayload tokenPayload) {
        return operationCode.craftResponse(fileDeleteService.delete(tokenPayload));
    }

    @GetMapping("/api/v1/files/categories")
    @Operation(summary = "List File Category Options", description = "قائمة خيارات تصنيفات الملفات")
    public ResponseEntity<ApiResponse<List<FileCategoryOptionResponse>>> listCategoryOptions(
            @RequestParam String moduleCode) {
        return operationCode.craftResponse(fileCategoryOptionService.listOptionsByModule(moduleCode));
    }

    @GetMapping("/api/v1/files/{ownerId}")
    @Operation(summary = "List Files for Owner Record", description = "قائمة ملفات سجل معيّن")
    public ResponseEntity<ApiResponse<Page<FileDocumentSummaryResponse>>> listByOwner(
            @PathVariable Long ownerId,
            @RequestParam(required = false) String ownerType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {
        return operationCode.craftResponse(
            fileListService.listByOwner(ownerId, ownerType, page, size, sortBy, sortDir));
    }
}
