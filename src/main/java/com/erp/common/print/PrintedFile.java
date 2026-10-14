package com.erp.common.print;

/** A printed file: its bytes, media type and download name. */
public record PrintedFile(byte[] content, String contentType, String fileName) {
}
